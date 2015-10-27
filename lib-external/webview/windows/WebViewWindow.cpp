/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

#include "stdafx.h"
#include "WebViewWindow.h"
#include "WebViewProtocol.h"
#include "WebViewProtocolFactory.h"
#include "util/MutexLock.h"
#include "util/WinUtil.h"

#include <exdispid.h>
#include <vector>
#include <tlogstg.h>

/**
 * Version: $Id: WebViewWindow.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

// Define this symbol to make the native window visible. This is often useful for debugging window capture
// or input handling issues.
//#define DEBUG_WINDOW_VISIBLE 1

///////////////////////////////////////////
// Resolving local resources
//
// The WebView can have a WebResourceResolver that will assist in relative references in the HTML content. The
// ResourceResolver is only used when the browser's base URL is a "webview" protocol URL. The webview protocol follows
// the form webview://[webViewId]/[path], where [webViewId] is a WebView identifier that uniquely identifies a
// WebView instance (see GetWebViewId). If a ResourceResolver is used, the base URL should be set to "webview://[id]/".
//
// WebViewWindow registers a custom protocol handler for the "webview" protocol. When a URL in the webview protocol is processed,
// the custom handler is invoked to parse the URL. The handler parses out the WebView ID, locates the WebViewWindow object for
// that ID, and asks the WebView's ResourceResolver to resolve the reference.

///////////////////////////////////////////
// Notes on scrollbars
//
// Scrollbars in the web browser handle input differently than all the other controls. For most scroll bars, the WebView
// can detect that the user has selected a scroll bar, and it can handle the scroll events manually. When the user drags the
// scroll bar thumb, the WebView tracks the position of the mouse and scrolls the document appropriately. When the user clicks
// and holds on an arrow, a timer is started that will scroll the page until the mouse is released.
//
// However, some pages include scroll bars that the WebView cannot identify. The browser reports that the mouse is "outside"
// of the page area when these scroll bars are selected. These "external" scroll bars have only been observed in Google search
// results, but may also appear in other pages.
//
// In order handle these "external" scroll bars, the WebView mimics the sequence of mouse events that occur when the user interacts
// with scroll bars in the native WebBrowser: When the user clicks on a scrollbar, the first mouse-down event goes to the web browser
// window, and then a hidden window (of window class "Internet Explorer_Hidden") receives all mouse moved events until a mouse-up
// event occurs.
//
///////////////////////////////////////////
// Notes on detecting changes in the view
//
// This class uses IViewObject::Advise to listen for changes in the rendered HTML document, which triggers a callback when the
// rendered page changes. For most sites it works perfectly, but for some we get a constant stream of notifications, even though
// nothing has actually changed. To work around this problem, the message loop that handles WebViewWindow can call
// WebViewWindow::ScheduleCapture periodically as input is processed. ScheduleCapture posts the WM_WEBVIEW_CAPTURE message to the window,
// causing it to update the captured bitmap when the message is processed.
//
// IViewObject::Advise has proven unreliable for pages that contain embedded content, such as Flash video. If the page contains
// EMBED tags, the WebView captures the contents periodically regardless of whether or not view change events have occurred.

const wchar_t *DEFAULT_BASE_URL = L"about:blank";

// File type used in header of bitmap files
const WORD BITMAP_FILE_TYPE = 0x4D42;

// ID number of the scroll update timer.
const int SCROLL_TIMER_ID = 2;

// Key used to store a pointer to the WebViewWindow in the Window property list
const wchar_t *WEB_VIEW_PTR_KEY = L"gov.nasa.worldwind.webview.WebViewPtr";

// Constants to identify different types of scroll bars
const char *SCROLLBAR_VERTICAL = "gov.nasa.worldwind.webview.Vertical";
const char *SCROLLBAR_HORIZONTAL = "gov.nasa.worldwind.webview.Horizontal";
// The browser reports that the selected component is "outside" of the page area when certain scroll bars are
// selected. We call these "external" scroll bars, and need to handle them specially.
const char *SCROLLBAR_EXTERNAL = "gov.nasa.worldwind.webview.ScrollBarExternal";

/** Indicates that the WebView protocol has been initialized. */
BOOL WebViewWindow::protocolInitialized;

/**
 * RAII class to resize a window, and restore the previous size when the RestorableWindow object goes out of scope.
 */
class RestorableWindow
{
public:
    /**
     * Create the restorable object to wrap an existing window.
     *
     * @param hwnd HWND of the window to resize.
     */
    RestorableWindow(HWND hwnd)
        : hwnd(hwnd)
    {
        GetWindowRect(hwnd, &rect);
    }

    /** Restore the previous window size. */
    ~RestorableWindow()
    {
        MoveWindow(this->hwnd, this->rect.left, this->rect.top,
            this->rect.right - this->rect.left, this->rect.bottom - this->rect.top, FALSE);
    }

    /**
     * Resize the window.
     *
     * @param width New width.
     * @param height New height.
     *
     * @return TRUE if the operation succeeds, or FALSE if it fails. Call GetLastError() for more information.
     */
    BOOL Resize(int width, int height)
    {
        return MoveWindow(this->hwnd, this->rect.left, this->rect.top, width, height, FALSE);
    }

private:
    HWND hwnd;
    RECT rect;
};

WebViewWindow::WebViewWindow()
    : controlWnd(NULL),
      browser(NULL),
      htmlContent(NULL),
      captureBits(NULL),
      captureWidth(0),
      captureHeight(0),
      captureScheduled(FALSE),
      hWndBrowser(NULL),
      hWndScrollControl(NULL),
      dispatchCookie(0),
      hCaptureDIB(NULL),
      updateTime(0),
      needToCapture(FALSE),
      alwaysCapture(FALSE),
      adviseSink(NULL),
      resourceResolver(NULL),
      isScrolling(FALSE),
      lastInputPoint(NULL),
      activeScroller(NULL),
      scrollElement(NULL),
      scrollRefPosition(0),
      backgroundColor(NULL),
      originalContentLoaded(FALSE),
      contentURL(NULL),
      links(NULL),
      contentWidth(0),
      contentHeight(0),
      minContentWidth(DEFAULT_MIN_CONTENT_WIDTH),
      minContentHeight(DEFAULT_MIN_CONTENT_HEIGHT),
      browserInitialized(FALSE),
      contentLoadID(0),
      contentMetadataUpdateID(0),
      mustClearTravelLog(FALSE)
{
    bitmapMutex = CreateMutex(NULL,  // Default security attributes
                        FALSE, // Initially not owned
                        NULL); // Unnamed

    mutex = CreateMutex(NULL, FALSE, L"WebView Mutex");
}

void WebViewWindow::FinalRelease()
{
    if (hCaptureDIB)
        DeleteObject(hCaptureDIB);

    if (browser)
        browser->Release();

    if (adviseSink)
        adviseSink->Release();

    if (backgroundColor)
        free(backgroundColor);

    if (htmlContent)
        htmlContent->Release();

    if (scrollElement)
        scrollElement->Release();

    if (links)
        links->Release();

    if (resourceResolver)
        resourceResolver->Release();

    CloseHandle(bitmapMutex);
    CloseHandle(mutex);
}

void WebViewWindow::InitializeWebViewProtocol()
{
    CComPtr<IInternetSession> internetSession;
    HRESULT hr = CoInternetGetSession(0, &internetSession, 0);
    if (FAILED(hr))
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return;
    }

    CComPtr<WebViewProtocolFactory> factory = new WebViewProtocolFactory();

    // Register our protocol factory for the "webview" protocol. When a URL needs to be resolved in the "webview" protocol,
    // URLMON will call our protocol handler to resolve the URL.
    internetSession->RegisterNameSpace(factory, CLSID_WebViewProtocol, L"webview", 0, NULL, 0);
}

HRESULT WebViewWindow::CreateWebBrowser()
{
    UINT winStyle = WS_POPUP | WS_EX_NOACTIVATE;

    if (!WebViewWindow::protocolInitialized)
    {
        this->InitializeWebViewProtocol();
        WebViewWindow::protocolInitialized = TRUE;
    }

// If the debug visible flag is defined, make the window visible. By default it is invisible.
#if defined(DEBUG) && defined(DEBUG_WINDOW_VISIBLE)
    winStyle |= WS_VISIBLE;
#endif

    // Create the window
    Create(NULL, CWindow::rcDefault, NULL, winStyle);
    if (this->m_hWnd == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
        return E_FAIL;
    }

    // Assign an ID
    this->AssignWebViewId();

    // Create the web browser control
    CComPtr<IAxWinHostWindow> spHost;
    HRESULT hr = QueryHost(&spHost);
    if (FAILED(hr))
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return hr;
    }

    hr = spHost->CreateControl(L"about:blank", m_hWnd, NULL);
    if (FAILED(hr))
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return hr;
    }

    // We need to customize a few parameters of the ActiveX host. By default CAxWindow disables scrollbars, unless
    // the WS_VSCROLL or WS_HSCROLL window styles are set. We want scrollbars, but only if the page needs them.
    // We want MSHTML to manage its own scrollbars, so we need to tell CAxWindow to allow them by clearing the
    // DOCHOSTUIFLAG_SCROLL_NO bit in the doc host flags.
    CComPtr<IAxWinAmbientDispatch> winDispatch = NULL;
    hr = QueryHost(IID_IAxWinAmbientDispatch, (void**) &winDispatch);

    if (SUCCEEDED(hr) && winDispatch != NULL)
    {
        DWORD docHostFlags = 0;
        hr = winDispatch->get_DocHostFlags(&docHostFlags);
        assert(SUCCEEDED(hr) && "Failed to get doc host flags from IAxWinAmbientDispath");
        if (FAILED(hr)) 
            Logging::logger()->warning(L"NativeLib.ErrorInNativeLib", hr);

        // Clear the no scrollbar flag. We want scrollbars
        docHostFlags = docHostFlags & ~DOCHOSTUIFLAG_SCROLL_NO;

        hr = winDispatch->put_DocHostFlags(docHostFlags);
        assert(SUCCEEDED(hr) && "Failed to set doc host flags in IAxWinAmbientDispath");
        if (FAILED(hr)) 
            Logging::logger()->warning(L"NativeLib.ErrorInNativeLib", hr);

        // Disable the right click context menu
        winDispatch->put_AllowContextMenu(VARIANT_FALSE);
    }
    else
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
    }

    // Get a reference to the web browser
    hr = QueryControl(IID_IWebBrowser2, (void**) &browser);
    assert(SUCCEEDED(hr) && browser != NULL);

    if (FAILED(hr) || browser == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return hr;
    }

    // Add a reference to the browser because we hold this pointer until the window is destroyed. QueryControl does not seem to add a ref for us.
    browser->AddRef();

    // Do not allow the browser to display pop up dialogs. This means that any script errors on the
    // page will be silently ignored.
    browser->put_Silent(VARIANT_TRUE);

    // Retrieve and store the IConnectionPointerContainer pointer 
    CComQIPtr<IConnectionPointContainer, &IID_IConnectionPointContainer> spCPC;
    spCPC = browser;
    if (spCPC == NULL) 
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib");
        return E_POINTER;
    }

    CComPtr<IConnectionPoint> spCP;

    // Get the connection point for WebBrowser events
    hr = spCPC->FindConnectionPoint(DIID_DWebBrowserEvents2, &spCP);
    if (FAILED(hr))
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return hr;
    }

    // Subscribe to web browser events. Invoke will be called when events occur.
    CComQIPtr<IDispatch> disp = this;
    hr = spCP->Advise(disp, &dispatchCookie);
    if (FAILED(hr))
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return hr; 
    }
    
    return S_OK;
}

HRESULT WebViewWindow::ApplyBackgroundColor()
{
    HRESULT ret = E_FAIL;
    if (this->backgroundColor == NULL)
        return S_OK;

    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = this->browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return E_FAIL;
    }

    // Get the document from the dispatch interface
    CComQIPtr<IHTMLDocument2> pDoc = pDispatch;
    if (pDoc == NULL)
        return E_FAIL;

    CComPtr<IHTMLElement> pElement = NULL;
    hr = pDoc->get_body(&pElement);
    if (SUCCEEDED(hr) && pElement != NULL)
    {
        VARIANT color;
        VariantInit(&color);
        color.vt = VT_BSTR;
        color.bstrVal = SysAllocString(this->backgroundColor);

        CComPtr<IHTMLStyle> pStyle = NULL;
        pElement->get_style(&pStyle);
        if (pStyle != NULL)
            ret = pStyle->put_backgroundColor(color);

        VariantClear(&color);
    }

    return ret;
}

////////////////////////////////////////////////////
// WebView ID handling
////////////////////////////////////////////////////

LONG_PTR WebViewWindow::GetWebViewId() const
{
    // Use the Window handle as the WebView ID number
    return reinterpret_cast<LONG_PTR>(this->m_hWnd);
}

void WebViewWindow::AssignWebViewId()
{
    // Store a pointer to this object in the window's property list. This allows us to get back to the WebViewWindow
    // object from just the HWND, which is used as the WebViewId.
    SetProp(this->m_hWnd, WEB_VIEW_PTR_KEY, this);
}

WebViewWindow* WebViewWindow::FindWebView(LONG_PTR webViewId)
{
    // Treat the ID as an HWND. Look for a pointer to a WebViewWindow instance in the window's property list
    return reinterpret_cast<WebViewWindow*>(GetProp(reinterpret_cast<HWND>(webViewId), WEB_VIEW_PTR_KEY));
}

///////////////////////////////////////////////////
// Methods invoked in response to Windows messages
///////////////////////////////////////////////////

LRESULT WebViewWindow::OnFrameSizeChanged(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    // Determine if scroll bars are required in the new size.
    this->DetermineScrollBars();
    return ERROR_SUCCESS;
}

LRESULT WebViewWindow::OnActivate(UINT uMsg, WPARAM active, LPARAM unused, BOOL bHandled)
{
    this->active = static_cast<BOOL>(active);
    return ERROR_SUCCESS;
}

LRESULT WebViewWindow::OnTimer(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    switch(wParam)
    {
    case SCROLL_TIMER_ID:
        this->AutoScroll();
        break;
    }
    return ERROR_SUCCESS;
}

LRESULT WebViewWindow::OnSetBackgroundColor(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    const wchar_t *colorStr = reinterpret_cast<const wchar_t*>(lParam);

    this->backgroundColor = _wcsdup(colorStr);
    this->ApplyBackgroundColor();

    return ERROR_SUCCESS;
}

///////////////////////////////////////////////////
// Methods for capturing the WebView contents
///////////////////////////////////////////////////

LRESULT WebViewWindow::OnCapture(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    this->CaptureWebView();
    this->captureScheduled = FALSE;

    return DefWindowProc();
}

void WebViewWindow::ScheduleCapture()
{
    // If a capture is already scheduled, do not schedule another
    if (this->captureScheduled)
        return;

    // Only capture the window if something has changed.
    if (this->needToCapture || this->alwaysCapture)
    {
        this->captureScheduled = TRUE;
        PostMessage(WM_WEBVIEW_CAPTURE, 0, 0);

        this->needToCapture = FALSE;
    }
}

BOOL WebViewWindow::CaptureWebView()
{
    // If the window has user input focus, send a simulated mouse move message to make sure that the window thinks the mouse
    // is where we want it. If we don't do this, most hover events don't work. The browser window will forget where the mouse
    // is when another window receives input. Sending this message makes the browser think that it is the active window before
    // we capture it.
    if (this->active)
        ::SendMessage(this->hWndUnderCursor, WM_MOUSEMOVE, 0, this->lastInputPoint);

    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = browser->get_Document((IDispatch**)&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
        return FALSE;

    CComQIPtr<IHTMLDocument2> pDoc = pDispatch;
    if (pDoc == NULL)
        return FALSE;

    CComQIPtr<IViewObject> pViewObject = pDoc;
    if (pViewObject != NULL)
    {
        // Capture a bitmap of the WebView contents
        CaptureBitMap(pViewObject);

        // Timestamp this capture
        this->updateTime = GetTickCount();

        // Find the position of the links on the page
        this->FindLinks();

        // Determine the content URL if new content has been loaded since the metadata was last determined.
        if (this->contentMetadataUpdateID != this->contentLoadID)
        {
            this->DetermineContentURL();
            this->DetermineContentSize();
            this->contentMetadataUpdateID = this->contentLoadID;
        }

        // Notify listener that capture changed.
        if (this->adviseSink)
            this->adviseSink->OnViewChange(DVASPECT_CONTENT, -1);
    }

    return TRUE;
}

///////////////////////////////////////////////////
// Methods for loading content
///////////////////////////////////////////////////

HRESULT WebViewWindow::SetHTML()
{
    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
        return FALSE;

    CComQIPtr<IHTMLDocument2> pDoc = pDispatch;
    if (pDoc == NULL)
        return FALSE;

    CComQIPtr<IPersistMoniker> pMon = pDoc;
    CComQIPtr<IMoniker> iMon = this->htmlContent;

    // Load the HTML string into the browser
    hr = pMon->Load(TRUE, iMon, NULL, STGM_READ);

    // IPersistMoniker::Load returns S_FALSE and does not actually load the content if it doesn't
    // understand the content's base URL. This happens with invalid URLs, and also some flavors of file URL.
    // IPersistMoniker considers "file:C:\somedir" to be invalid, but "file:/C:/somedir" to be valid.
    // If the document load failed, we'll retry with the default base URL.
    if (hr == S_FALSE)
    {
        // Capture the original base URL so that we can include it in a logger statement
        wchar_t *baseUrl;
        this->htmlContent->GetDisplayName(NULL, NULL, &baseUrl);

        // Set the default base URL and try loading the content again
        this->htmlContent->SetBaseURL(DEFAULT_BASE_URL, wcslen(DEFAULT_BASE_URL));
        hr = pMon->Load(TRUE, iMon, NULL, STGM_READ);
        
        // If the load succeeded, we'll assume that the base URL was the problem and log
        // a warning to that effect
        if (hr == S_OK)
        {
            Logging::logger()->warning(L"WebView.InvalidResourceResolver", baseUrl);
        }
        else
        {
            // Load failed for some other reason, not the base URL
            Logging::logger()->severe(L"WebView.NativeExceptionSettingHTMLString", hr);
        }

        if (baseUrl != NULL)
            CoTaskMemFree(baseUrl);
    }
    else if (FAILED(hr))
    {
        Logging::logger()->severe(L"WebView.NativeExceptionSettingHTMLString", hr);
    }

    this->originalContentLoaded = TRUE;

    return S_OK;
}

LRESULT WebViewWindow::OnSetHTML(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    if (this->htmlContent != NULL)
        this->htmlContent->Release();

    // Do not AddRef. We take over ownership of the caller's reference
    this->htmlContent = reinterpret_cast<HTMLMoniker*>(lParam);

    // Load the new content
    this->SetHTML();

    // Clear the back/forward history since a new browser session has been initiated.
    this->ClearTravelLog();

    return ERROR_SUCCESS;
}

///////////////////////////////////////////////////
// Methods dealing with the back/forward history
///////////////////////////////////////////////////

LRESULT WebViewWindow::OnGoBack(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    HRESULT hr = E_FAIL;

    // If we're not on the original page, just let the browser navigate back.
    if (!this->originalContentLoaded)
        hr = this->browser->GoBack();

    // If the browser failed to go back, and we we're not already on the original page, reload the original content.
    if (FAILED(hr) && !this->originalContentLoaded)
    {
        CComQIPtr<IServiceProvider> serviceProvider = browser;
        if (serviceProvider == NULL)
            return 0;

        CComPtr<ITravelLogStg> travelLog = NULL;
        serviceProvider->QueryService(SID_STravelLogCursor, IID_ITravelLogStg, (void**)&travelLog);

        if (travelLog == NULL)
            return 0;

        CComPtr<IEnumTravelLogEntry> entries = NULL;
        travelLog->EnumEntries(TLEF_ABSOLUTE, &entries);

        wchar_t *url = NULL;
        wchar_t *title = NULL;
        wchar_t *previousUrl = NULL;

        this->savedTravelLog.clear();

        // Save a copy of the browser travel log. We'll restore the travel log when the browser navigates forward.
        ITravelLogEntry *entry;
        while (entries->Next(1, &entry, NULL) == S_OK)
        {
            entry->GetURL(&url);
            entry->GetTitle(&title);

            // Do not add duplicate URLs to the saved travel log.
            if (url != NULL && (previousUrl == NULL || wcscmp(url, previousUrl) != 0))
            {
                WebViewTravelLogEntry savedEntry(url, title);
                this->savedTravelLog.push_back(savedEntry);

                if (previousUrl != NULL)
                    CoTaskMemFree(previousUrl);

                previousUrl = url;
            }
            else
            {
                CoTaskMemFree(url); // Duplicate URL
            }
            CoTaskMemFree(title);
            entry->Release();
        }

        if (previousUrl != NULL)
            CoTaskMemFree(previousUrl);

        // Internet Explorer does not fill TravelLog details for the current page until the browser navigates
        // away from the page. If the travel log is empty, add the current page to it. This will handle the case
        // when the browser navigates away from the original content, and then directly back to the original.
        if (this->savedTravelLog.size() == 0)
        {
            BSTR url = NULL;
            BSTR title = NULL;
            browser->get_LocationURL(&url);
            browser->get_LocationName(&title);

            WebViewTravelLogEntry savedEntry(url, title);
            this->savedTravelLog.push_back(savedEntry);

            // The travel log entry creates a copy of the strings, so free our references
            SysFreeString(url);
            SysFreeString(title);
        }

        // Set the clear travel log flag. OnDocumentComplete checks this flag when a page is loaded. If the flag is set,
        // and the browser has moved away from the original content, the travel log is cleared. The browser doesn't consider
        // loading content using HTMLMoniker to a real navigation event, so it doesn't realize that it needs to clear the travel
        // log if the browser goes back to the original content, and then follows a different link. (We can't clear the travel
        // log here because you can't remove the current entry from the travel log, we have to wait until the browser navigates.)
        this->mustClearTravelLog = TRUE;

        // Load original content
        this->SetHTML();
    }

    return ERROR_SUCCESS;
}

LRESULT WebViewWindow::OnGoForward(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{           
    // If we're on the original page we need to restore the travel log from the copy that we've saved. If the browser
    // has never navigated from the original page, this is harmless.
    if (this->originalContentLoaded)
    {
        // Remove all entries from the travel log because we are going to rebuild it from the saved copy.
        this->ClearTravelLog();

        CComQIPtr<IServiceProvider> serviceProvider = browser;
        if (serviceProvider == NULL)
            return 0;

        CComPtr<ITravelLogStg> travelLog = NULL;
        serviceProvider->QueryService(SID_STravelLogCursor, IID_ITravelLogStg, (void**)&travelLog);

        if (travelLog == NULL)
            return 0;

        // Restore previous entries. Iterate through the list in reverse order so that we can insert entries afer the current TravelLog
        // entry and end up with a list in the correct order.
        for (int i = static_cast<int>(this->savedTravelLog.size()) - 1; i >=0; i--)
        {
            WebViewTravelLogEntry savedEntry = this->savedTravelLog.at(i);
            travelLog->CreateEntry(savedEntry.url.c_str(), savedEntry.title.c_str(), NULL, FALSE, NULL);
        }

        // Clear the clear travel log flag. The browser is navigating forward, so we want to maintain the travel log.
        this->mustClearTravelLog = FALSE;
    }

    // Navigate forward. If we just restored the travel log, this will navigate to the first page. Otherwise,
    // it will navigate to the next page in the history, if there is a next page.
    this->browser->GoForward();
    
    return ERROR_SUCCESS;
}

void WebViewWindow::ClearTravelLog()
{
    CComQIPtr<IServiceProvider> serviceProvider = this->browser;
    if (serviceProvider == NULL)
        return;

    CComPtr<ITravelLogStg> travelLog = NULL;
    serviceProvider->QueryService(SID_STravelLogCursor, IID_ITravelLogStg, (void**)&travelLog);

    if (travelLog == NULL)
        return;

    DWORD flags = TLEF_ABSOLUTE | TLEF_RELATIVE_INCLUDE_CURRENT | TLEF_INCLUDE_UNINVOKEABLE;
    DWORD count = 0;

    // Find the number of entries in the log
    HRESULT hr = travelLog->GetCount(flags, &count);
    if (FAILED(hr) || count == 0)
        return;

    // Create an array to hold a pointer to each travel log entry
    ITravelLogEntry** entryAry = (ITravelLogEntry**)malloc(count * sizeof(ITravelLogEntry*));

    // Get a travel log enumerator
    CComPtr<IEnumTravelLogEntry> entries = NULL;
    travelLog->EnumEntries(flags, &entries);

    // Add all of the travel log entries to our array. We can't remove entries from the log while
    // using the enumeration.
    ULONG i = 0;
    while (i < count)
    {
        ULONG fetch;
        entries->Next(count - i, entryAry + i, &fetch);
        i += fetch;
    }

    // Loop through the entries and remove each one from the TravelLog
    for (i = 0; i < count; i++)
    {
        ITravelLogEntry *entry = entryAry[i];
        travelLog->RemoveEntry(entry);
        entry->Release();
    }

    free(entryAry);
}

LRESULT WebViewWindow::OnSetAdvise(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    if (this->adviseSink)
        this->adviseSink->Release();

    this->adviseSink = reinterpret_cast<IAdviseSink*>(lParam);

    return ERROR_SUCCESS;
}

LRESULT WebViewWindow::OnSetResourceResolver(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    if (this->resourceResolver)
        this->resourceResolver->Release();

    this->resourceResolver = reinterpret_cast<WebResourceResolver*>(lParam);

    return ERROR_SUCCESS;
}

//////////////////////////////////////
// Input handling
//////////////////////////////////////

void WebViewWindow::TrackScrollThumb(int x, int y)
{
    if (this->activeScroller == SCROLLBAR_VERTICAL)
    {
        int scrollArrowHeight = GetSystemMetrics(SM_CYVSCROLL);

        long scrollTop;
        this->scrollElement->get_scrollTop(&scrollTop);

        long scrollHeight;
        this->scrollElement->get_scrollHeight(&scrollHeight);

        long clientHeight;
        this->scrollElement->get_clientHeight(&clientHeight);

        // Assume that the scroll thumb range (in pixels) is the client area of the scroll element minus space for the up and down arrows
        double scrollAmount = static_cast<double>(y - GET_Y_LPARAM(this->scrollRefPoint)) / (clientHeight - scrollArrowHeight * 2);
        long scrollY = static_cast<long>(scrollAmount * scrollHeight + this->scrollRefPosition);

        this->scrollElement->put_scrollTop(scrollY);
    }
    else if (this->activeScroller == SCROLLBAR_HORIZONTAL)
    {
        int scrollArrowWidth = GetSystemMetrics(SM_CYHSCROLL);

        long scrollLeft;
        this->scrollElement->get_scrollLeft(&scrollLeft);

        long scrollWidth;
        this->scrollElement->get_scrollWidth(&scrollWidth);

        long clientWidth;
        this->scrollElement->get_clientWidth(&clientWidth);

        // Assume that the scroll thumb range (in pixels) is the client area of the scroll element minus space for the left and right arrows
        double scrollAmount = static_cast<double>(x - GET_X_LPARAM(this->scrollRefPoint)) / (clientWidth - scrollArrowWidth* 2);
        long scrollX = static_cast<long>(scrollAmount * scrollWidth + this->scrollRefPosition);

        this->scrollElement->put_scrollLeft(scrollX);
    }
}

void WebViewWindow::AutoScroll()
{
    if (this->scrollElement != NULL)
    {
        int x = GET_X_LPARAM(this->lastInputPoint);
        int y = GET_Y_LPARAM(this->lastInputPoint);

        BSTR componentAtPoint = GetComponentAtPoint(x, y);

        VARIANT scrollVar;
        VariantInit(&scrollVar);
        scrollVar.vt = VT_BSTR;
        scrollVar.bstrVal = componentAtPoint;

        // Simulate a click on whatever scroll component is under the mouse.
        // If the component under the mouse is not part of a scroll bar, this
        // is a no-op.
        this->scrollElement->doScroll(scrollVar);
        SysFreeString(componentAtPoint);
    }
}

LRESULT WebViewWindow::OnSimulateInput(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled)
{
    LRESULT ret = ERROR_SUCCESS;

    int clientX = GET_X_LPARAM(lParam);
    int clientY = GET_Y_LPARAM(lParam);

    POINT point;
    point.x = clientX;
    point.y = clientY;

    // Find the window under the cursor. This may be either the browser window,
    // or an embedded child window (for example, an embedded Flash player).
    this->hWndUnderCursor = ::ChildWindowFromPoint(this->hWndBrowser, point);

    int childX = 0;
    int childY = 0;

    // If the window under the cursor is not the browser window we need to translate the input
    // point to be relative to the child window client area.
    if (this->hWndUnderCursor != this->hWndBrowser)
    {
        RECT parentRect;
        ::GetWindowRect(this->m_hWnd, &parentRect);

        RECT childRect;
        ::GetWindowRect(this->hWndUnderCursor, &childRect);

        childX = clientX + (parentRect.left - childRect.left);
        childY = clientY + (parentRect.top- childRect.top);
    }
    else
    {
        childX = clientX;
        childY = clientY;
    }

    // Scrollbars require special input handling. See the note at the top of this file for a full explanation.

    // Get the component under the cursor and determine if it is a scroll component. If the component is outside of the
    // document area, we assume that it is a scrollbar because there are no other window decorations.
    BOOL isScrollComponent = FALSE;
    BOOL verticalScrollThumbActive = FALSE;
    BOOL horizontalScrollThumbActive = FALSE;
    BOOL outsideContent = FALSE;

    BSTR componentAtPoint = this->GetComponentAtPoint(clientX, clientY);
    if (componentAtPoint != NULL)
    {
        isScrollComponent = wcsstr(componentAtPoint, L"scrollbar") != NULL;
        outsideContent = wcscmp(componentAtPoint, L"outside") == 0;

        horizontalScrollThumbActive = wcscmp(componentAtPoint, L"scrollbarHThumb") == 0;
        verticalScrollThumbActive = wcscmp(componentAtPoint, L"scrollbarVThumb") == 0;

        SysFreeString(componentAtPoint);
    }

    LPARAM previousInputPoint = this->lastInputPoint;

    // If this is a mouse message, save the mouse point to lastInputPoint. This is used to start and stop scroll operations.
    if (uMsg >= WM_MOUSEFIRST && uMsg <= WM_MOUSELAST)
    {
        this->lastInputPoint = MAKELPARAM(childX, childY);

        // Translate mouse point to the window under the cursor
        lParam = MAKELPARAM(childX, childY);
    }

    HWND targetHwnd;

    switch (uMsg)
    {
    case WM_LBUTTONDOWN:
    case WM_RBUTTONDOWN:
    case WM_MBUTTONDOWN:

        if (isScrollComponent)
        {
            this->isScrolling = TRUE;
            this->scrollRefPoint = this->lastInputPoint;			

            CComPtr<IDispatch> pDispatch = NULL;
            HRESULT hr = this->browser->get_Document(&pDispatch);
            if (FAILED(hr) || pDispatch == NULL)
            {
                Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
                return NULL;
            }

            CComQIPtr<IHTMLDocument2> pDoc = pDispatch;
            if (pDoc == NULL)
                return NULL;

            CComPtr<IHTMLElement> elementAtPoint;
            pDoc->elementFromPoint(clientX, clientY, &elementAtPoint);

            if (elementAtPoint != NULL)
                elementAtPoint->QueryInterface(IID_IHTMLElement2, (void**)&(this->scrollElement));

            if (horizontalScrollThumbActive)
            {
                this->activeScroller = SCROLLBAR_HORIZONTAL;
                this->scrollElement->get_scrollLeft(&(this->scrollRefPosition));
            }
            else if (verticalScrollThumbActive)
            {
                this->activeScroller = SCROLLBAR_VERTICAL;
                this->scrollElement->get_scrollTop(&(this->scrollRefPosition));
            }
            else
            {
                // If neither the of the scroll bar thumbs are active, then the user must have clicked on an arrow, or the page up/down
                // part of the scroll bar. In this case, start a timer that will scroll the page until the mouse button is released.
                UINT_PTR timer = SetTimer(SCROLL_TIMER_ID, 100, NULL) ;
                if (timer == 0)
                {
                    Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
                }
            }
        }
        else if (outsideContent)
        {
            // If the active element is outside of the content area, assume that an external scroll bar is active.
            SetWindowPos(0, 0, 0, 0, 0, SWP_NOZORDER | SWP_NOSIZE | SWP_NOACTIVATE);
            this->activeScroller = SCROLLBAR_EXTERNAL;
        }
        else
        {
            // Some operations, such as text selection, use the current mouse position, which we can't fake by sending 
            // mouse moved events. But we can move the hidden window to where the cursor is, so that the mouse position will
            // select the right text. Note that this is code calculates the window position using the current mouse position
            // and point on the window that was clicked. This isn't completely correct because the mouse may have moved since
            // the click event was fired, but in practice this method works pretty well. This technique also makes drop down
            // menus in forms appear in the right place (they are drawn as child windows, so they appear over the real parent
            // window).
            //
            // However, note that this approach does not work if the webview texture is scaled. The mouse coordinates wil not
            // line up.
            this->MoveWindowToCursor(clientX, clientY);
        }

        targetHwnd = this->hWndUnderCursor;
        break;

    case WM_LBUTTONUP:
    case WM_RBUTTONUP:
    case WM_MBUTTONUP:

        // Mouse up always cancels scrolling
        if (this->isScrolling)
        {            
            this->isScrolling = FALSE;

            if (this->scrollElement != NULL)
            {
                this->scrollElement->Release();
                this->scrollElement = NULL;
            }

            this->activeScroller = NULL;
            KillTimer(SCROLL_TIMER_ID);

            // No need to send the mouse message on, we already handled it
            targetHwnd = NULL;
        }
        // If an external scroll bar is active, send the mouse message to the scroll control window
        else if (this->activeScroller == SCROLLBAR_EXTERNAL)
        {
            targetHwnd = this->hWndScrollControl;
            this->activeScroller = NULL;
        }
        else
        {
            targetHwnd = this->hWndUnderCursor;
        }
        break;

    case WM_MOUSEMOVE:
        // If the WebView is scrolling, track the position of the scroll bar thumb
        if (this->isScrolling && this->scrollElement != NULL)
        {
            this->TrackScrollThumb(clientX, clientY);

            // No need to forward the mouse message. If a scroll bar thumb is active, TrackScrollThumb
            // handled the event. If another part of the scroll bar is active, the move doesn't have any affect.
            targetHwnd = NULL;
        }
        // If an external scroll bar is active, forward the mouse event to the scroll control window
        else if (this->activeScroller == SCROLLBAR_EXTERNAL)
        {
            targetHwnd = this->hWndScrollControl;
        }
        else
        {
            targetHwnd = this->hWndUnderCursor;
        }
        break;

    case WM_SIM_MOUSEWHEEL:
        targetHwnd = this->hWndBrowser;

        // Convert message id to WM_MOUSEWHEEL.
        uMsg = WM_MOUSEWHEEL;
        break;

    case WM_KEYDOWN:
        {
            // Key accelerators (ctrl-c, ctrl-v, del, etc) require special handling. These key messages need to be sent to
            // the web browser's TranslateAccelerator method instead of posting to the message queue like a normal key message.

            targetHwnd = this->hWndUnderCursor; // Assume that we will send the message as a normal key event

            // Query web browser for IOleInPlaceActiveObject, this interface handles accelerators.
            HRESULT hr = S_FALSE;
            CComQIPtr<IOleInPlaceActiveObject, &IID_IOleInPlaceActiveObject> inplaceActiveObj(this->browser);

            if (inplaceActiveObj)
            {
                MSG msg;
                msg.message = uMsg;
                msg.wParam = wParam;
                msg.lParam = lParam;

                // Give IOleInPlaceActiveObject a chance to process the key message. If the message is processed as an
                // accelerator, this returns S_OK, and we don't need to send the message to the browser's message queue.
                hr = inplaceActiveObj->TranslateAccelerator(&msg);
                if (hr == S_OK)
                    targetHwnd = NULL;
            }
        }
        break;

    default:
        targetHwnd = this->hWndUnderCursor;
    }

    if (targetHwnd != NULL)
        ::PostMessage(targetHwnd, uMsg, wParam, lParam);

    return ERROR_SUCCESS;
}

/**
 * Move a window to the current position of the mouse cursor. This function
 * will align a point on the window with the current position of the mouse cursor.
 *
 * @param wnd HWND of the window to move.
 * @param windowX X coordinate (relative to upper left corner of window) of the point to align with
 *        the mouse cursor.
 * @param windowY X coordinate (relative to upper left corner of window) of the point to align with
 *        the mouse cursor.
 */
void WebViewWindow::MoveWindowToCursor(int clientX, int clientY)
{
    // Get the current mouse position.
    POINT mousePoint;
    GetCursorPos(&mousePoint);

    // Figure out where to put the window.
    int newX = mousePoint.x - clientX;
    int newY = mousePoint.y - clientY;

    SetWindowPos(0, newX, newY, 0, 0, SWP_NOZORDER | SWP_NOSIZE | SWP_NOACTIVATE);
}

BSTR WebViewWindow::GetComponentAtPoint(int x, int y) const
{
    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = this->browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return NULL;
    }

    CComQIPtr<IHTMLDocument2> pDoc = pDispatch;
    if (pDoc == NULL)
        return NULL;

    CComPtr<IHTMLElement> elementAtPoint;
    pDoc->elementFromPoint(x, y, &elementAtPoint);

    CComQIPtr<IHTMLElement2> element2 = elementAtPoint;
    if (element2 != NULL)
    {
        BSTR active;
        element2->componentFromPoint(x, y, &active);

        return active;
    }
    return NULL;
}

//////////////////////////////////////
// Image capture
//////////////////////////////////////

int WebViewWindow::CaptureBitMap(IViewObject *pViewObject)
{
    HDC hdcWindow;
    HDC hdcMemDC = NULL;

    HBITMAP hCaptureBmp;

    // Retrieve the handle to a display device context for the client
    // area of the window. 
    hdcWindow = GetDC();

    // Create a compatible DC which is used in a BitBlt from the window DC
    hdcMemDC = CreateCompatibleDC(hdcWindow); 
    if(!hdcMemDC)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
        goto done;
    }

    // Get the client area for size calculation
    RECT rcClient;
    GetClientRect(&rcClient);

    // Create a compatible bitmap from the Window DC
    hCaptureBmp = CreateCompatibleBitmap(hdcWindow, rcClient.right - rcClient.left, rcClient.bottom - rcClient.top);
    
    if (!hCaptureBmp)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
        goto done;
    }

    // Select the compatible bitmap into the compatible memory DC.
    SelectObject(hdcMemDC, hCaptureBmp);

    DWORD start = GetTickCount();
    pViewObject->Draw(DVASPECT_CONTENT, -1, NULL, NULL, NULL, hdcMemDC, reinterpret_cast<LPRECTL>(&rcClient), NULL, NULL, 0);

    DWORD end = GetTickCount();
//    ATLTRACE("Actual drawing of Capture: %d ms", (end - start));

    // Draw the caret in the captured image. IViewObject::Draw does not capture the caret.
    this->DrawCaret(hdcMemDC);

    // Get the BITMAP from the HBITMAP
    BITMAP captureBmp;    
    GetObject(hCaptureBmp, sizeof(BITMAP), &captureBmp);

    captureWidth = captureBmp.bmWidth;
    captureHeight = captureBmp.bmHeight;

    BITMAPINFOHEADER bi;
    bi.biSize = sizeof(BITMAPINFOHEADER);
    bi.biWidth = captureBmp.bmWidth;
    bi.biHeight = -captureBmp.bmHeight;
    bi.biPlanes = 1;
    bi.biBitCount = 24;
    bi.biCompression = BI_RGB;    
    bi.biSizeImage = 0;
    bi.biXPelsPerMeter = 0;
    bi.biYPelsPerMeter = 0;
    bi.biClrUsed = 0;
    bi.biClrImportant = 0;

    { // Begin critical section
        MutexLock lock(bitmapMutex);

        if (hCaptureDIB)
            DeleteObject(hCaptureDIB);

        // Create a device independent bitmap
        hCaptureDIB = CreateDIBSection(hdcMemDC,
                                      (BITMAPINFO*)&bi,
                                      DIB_RGB_COLORS,
                                      (VOID **) &captureBits,
                                      NULL, 0);
        if (!hCaptureDIB)
        {
            Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
            goto done;
        }

        // Get the raw data from the bitmap
        int ret = GetDIBits(hdcWindow, hCaptureBmp, 0,
                            (UINT)captureBmp.bmHeight,
                            captureBits,
                            (BITMAPINFO*)&bi, DIB_RGB_COLORS);
    } // End critical section

done:
    DeleteObject(hdcMemDC);
    DeleteObject(hCaptureBmp);
    ::ReleaseDC(this->m_hWnd, hdcWindow);

    return 0;
}

void WebViewWindow::DrawCaret(HDC hDC) const
{
    GUITHREADINFO threadInfo;
    ZeroMemory(&threadInfo, sizeof(GUITHREADINFO));
    threadInfo.cbSize = sizeof(GUITHREADINFO);

    // Call GetGUIThreadInfo to find the current position and size
    // of the caret.
    DWORD threadId;
    threadId = GetWindowThreadProcessId(this->m_hWnd, NULL);
    BOOL ret = GetGUIThreadInfo(threadId, &threadInfo);
    if (!ret)
        return;

    InvertRect(hDC, &(threadInfo.rcCaret));
}

BOOL WebViewWindow::CaptureToGLTexture(GLenum target)
{
    MutexLock lock(bitmapMutex);

    if (captureBits)
    {
        // Upload the cached display bitmap to the currently bound OpenGL texture.
        glTexSubImage2D(
            target, // target
            0, // level
            0, 0, // xoffset, yoffset
            captureWidth, captureHeight, // pixel data width, height
            GL_BGR_EXT, // format of the pixel data
            GL_UNSIGNED_BYTE, // type of the pixel data
            (void*)captureBits); // pointer to pixel data in memory

        // Free the bitmap memory
        DeleteObject(hCaptureDIB);
        hCaptureDIB = NULL;
        captureBits = NULL;
        captureWidth = 0;
        captureHeight = 0;
    }

    return TRUE;
}

//////////////////////////////////////
// Link handling
//////////////////////////////////////

HRESULT WebViewWindow::FindLinks()
{
    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return hr;
    }

    // Get the document from the dispatch interface
    CComQIPtr<IHTMLDocument3> pDoc = pDispatch;
    if (pDoc == NULL)
        return hr;

    CComQIPtr<IHTMLDocument2> pDoc2 = pDoc;

    long contentLeft = 0;
    long contentTop = 0;
    long contentWidth = 0;
    long contentHeight = 0;

    CComPtr<IHTMLElement> documentElement = NULL;
    pDoc->get_documentElement(&documentElement);

    // Try to get the visible portion of the document. This works correctly
    // in standards mode, but not in compatibility mode.
    CComQIPtr<IHTMLElement2> documentElement2 = documentElement;
    if (documentElement2)
    {
        documentElement2->get_clientLeft(&contentLeft);
        documentElement2->get_clientTop(&contentTop);
        documentElement2->get_clientWidth(&contentWidth);
        documentElement2->get_clientHeight(&contentHeight);
    }

    // If we didn't get a size from the document, try getting the size of
    // the body element.
    if (contentWidth == 0 || contentHeight == 0)
    {
        CComPtr<IHTMLElement> body;
        CComQIPtr<IHTMLElement2> body2;

        pDoc2->get_body(&body);
        if (body != NULL)
            body2 = body;

        if (body2 != NULL)
        {
            body2->get_clientLeft(&contentLeft);
            body2->get_clientTop(&contentTop);
            body2->get_clientWidth(&contentWidth);
            body2->get_clientHeight(&contentHeight);
        }
    }

    RECT viewport;
    viewport.top = contentTop;
    viewport.left = contentLeft;
    viewport.right = contentLeft + contentWidth;
    viewport.bottom = contentTop + contentHeight;

    // Find all of the links in the document
    CComPtr<IHTMLElementCollection> anchors;
    hr = pDoc->getElementsByTagName(L"a", &anchors);
    assert(SUCCEEDED(hr));

    long numItems = 0;
    hr = anchors->get_length(&numItems);
    assert(SUCCEEDED(hr));    

    LinkParamCollection *linkList = new LinkParamCollection();

    VARIANT indexVariant;
    VariantInit(&indexVariant);
    indexVariant.vt = VT_I4;

    for (int i = 0; i < numItems; i++)
    {
        CComPtr<IDispatch> dispatch = NULL;
        indexVariant.intVal = i;
        hr = anchors->item(indexVariant, indexVariant, &dispatch);
        assert(SUCCEEDED(hr));

        CComQIPtr<IHTMLAnchorElement> anchor = dispatch;
        if (anchor == NULL)
        {
            continue; // Not an anchor
        }

        // Only process the anchor if it is is visible
        CComQIPtr<IHTMLElement> anchorElement = anchor;
        if (this->IsVisible(anchorElement))
        {
            LinkParams *params = this->GetLinkParams(anchor, &viewport);
            if (params != NULL)
                linkList->Add(params);
        }
    }

    VariantClear(&indexVariant);

    {
        MutexLock lock(this->mutex);

        if (this->links != NULL)
            this->links->Release();

        this->links = linkList;
    }

    return S_OK;
}

BOOL WebViewWindow::IsVisible(IHTMLElement *element) const
{
    if (element == NULL)
        return TRUE;

    BOOL hasArea = true;
    BOOL visible = true;

    // Determine if the element takes up any screen space.
    long offsetWidth, offsetHeight;
    element->get_offsetWidth(&offsetWidth);
    element->get_offsetHeight(&offsetHeight);

    hasArea = offsetWidth > 0 || offsetHeight > 0;

    if (!hasArea)
        return FALSE;

    // Determine if the element has a visibility style that
    // prevents it from rendering.
    CComQIPtr<IHTMLElement2> element2 = element;
    if (element2 != NULL)
    {
        CComPtr<IHTMLCurrentStyle> style = NULL;
        element2->get_currentStyle(&style);

        BSTR visibility = NULL;
        style->get_visibility(&visibility);

        visible = (visibility == NULL)
            || ((wcscmp(visibility, L"hidden") != 0)
                && (wcscmp(visibility, L"collapsed") != 0)
                && (wcscmp(visibility, L"none")));

        // If the style is "inherit" look at the parent's visibility
        if (wcscmp(visibility, L"inherit") == 0)
        {
            CComPtr<IHTMLElement> parent = NULL;
            element->get_parentElement(&parent);
            if (parent != NULL)
                visible = this->IsVisible(parent);
        }

        SysFreeString(visibility);
    }

    return visible;
}

LinkParams* WebViewWindow::GetLinkParams(IHTMLAnchorElement *anchor, RECT *viewport) const
{
    assert(anchor != NULL);

    HRESULT hr;

    BSTR url = this->GetLinkUrl(anchor);
    if (url == NULL)
        return NULL;

    BSTR target = NULL;
    BSTR mimeType = NULL;

    // Get the URL, mime type, and target from the anchor
    anchor->get_target(&target);

    VARIANT typeVariant;
    VariantInit(&typeVariant);
    typeVariant.vt = VT_EMPTY;

    CComQIPtr<IHTMLElement> anchorElement = anchor;
    hr = anchorElement->getAttribute(L"type", 2, &typeVariant);
    if (SUCCEEDED(hr) && typeVariant.vt == VT_BSTR)
    {
        mimeType = typeVariant.bstrVal;
    }
        
    LinkParams *linkParams = new LinkParams;

    linkParams->url = url;
    linkParams->type = SysAllocString(mimeType);
    linkParams->target = target;

    VariantClear(&typeVariant);

    CComQIPtr<IHTMLElement2> anchorElement2 = anchor;

    // Compute the link's bounding box in the document view's coordinate system, and clip it by the document's
    // visible rectangle.
    CComPtr<IHTMLRect> linkRect = NULL;
    anchorElement2->getBoundingClientRect(&linkRect);

    // Convert IHTMLRect to normal Windows RECT
    RECT linkBounds;
    linkRect->get_left(&(linkBounds.left));
    linkRect->get_right(&(linkBounds.right));
    linkRect->get_top(&(linkBounds.top));
    linkRect->get_bottom(&(linkBounds.bottom));

    RECT linkVisibleRect;
    SetRect(&linkVisibleRect, 0, 0, 0, 0);
    this->ComputeRectIntersect(&linkVisibleRect, viewport, &linkBounds);

    CComPtr<IHTMLCurrentStyle> style;
    anchorElement2->get_currentStyle(&style);

    // Add any visible images to the link's rectangles. We must collect the link's images before testing the link's
    // visibility, because some images exceed the link's bounding box.
    this->AddImageRects(linkParams, anchorElement2, style, &linkVisibleRect, viewport);

    // Ignore the node if it is not in the document view's visible rectangle, and it has no visible images. Some
    // link images exceed the link's bounding box, so we include them as a test of the link's true bounds.
    if (IsEmptyRect(&linkVisibleRect) && !linkParams->HasLinkRects())
    {
        delete linkParams;
        return NULL;
    }

    BSTR displayStyle = NULL;
    style->get_display(&displayStyle);
    
    CComPtr<IHTMLRectCollection> rects;
    hr = anchorElement2->getClientRects(&rects);

    long numRects = 0;
    if (SUCCEEDED(hr))
        rects->get_length(&numRects);

    if (numRects == 0 || (displayStyle != NULL && wcscmp(L"block", displayStyle) == 0))
    {
        // Add the link's bounding rectangle if its CSS display attribute is "block", or if the link does not have
        // line bounding rectangles. In this case, the link's bounding rectangle represents its entire pickable
        // area.
        linkParams->AddLinkRect(&linkVisibleRect);
    }
    else
    {
        // Add the bounding rectangles for each line of text if the link has lines of text with separate pickable
        // areas, and its CSS display attribute is not "block". In this case, each line of text is separately
        // pickable.
        this->AddLineBoxRects(linkParams, rects, viewport);
    }

    if (displayStyle)
        SysFreeString(displayStyle);

    // Ignore the link if it has no visible pickable areas.
    if (!linkParams->HasLinkRects())
    {
        delete linkParams;
        return NULL;
    }

    // Compute the link's bounding rectangle from the list of pickable areas.
    RECT linkBoundingBox;
    SetRect(&linkBoundingBox, 0, 0, 0, 0);

    RECT windowRect;
    this->GetWindowRect(&windowRect);
    long windowHeight = windowRect.bottom - windowRect.top;

    std::vector<RECT> &rectVector = linkParams->GetRects();
    std::vector<RECT>::iterator i;
    for (i = rectVector.begin(); i != rectVector.end(); ++i)
    {
        RECT *r = &(*i);
        this->ComputeRectUnion(&linkBoundingBox, &linkBoundingBox, r);

        // Clip rectangles against the visible part of the window.
        this->ComputeRectIntersect(r, r, viewport);

        // Adjust y values to GL coordinates (origin at bottom left corner).
        r->top = windowHeight - r->top;
        r->bottom = windowHeight - r->bottom;
    }

    // Clip bounding box against the visible part of the window.
    this->ComputeRectIntersect(&linkBoundingBox, &linkBoundingBox, viewport);

    // Adjust y values to GL coordinates (origin at bottom left corner).
    linkBoundingBox.top = windowHeight - linkBoundingBox.top;
    linkBoundingBox.bottom = windowHeight - linkBoundingBox.bottom;

    linkParams->SetBounds(&linkBoundingBox);

    return linkParams;
}

BSTR WebViewWindow::GetLinkUrl(IHTMLAnchorElement *anchor) const
{
    HRESULT hr;
    BSTR url = NULL;

    // IHTMLElement version of the anchor
    CComQIPtr<IHTMLElement> anchorElement = anchor;

    // If there is a resource resolver, invoke the resolver to resolve the link.
    if (this->resourceResolver != NULL)
    {
        BSTR href;

        VARIANT hrefVariant;
        VariantInit(&hrefVariant);
        hrefVariant.vt = VT_EMPTY;

        // Read the href parameter to get the link as it is in the HTML.
        hr = anchorElement->getAttribute(L"href", 2, &hrefVariant);
        if (SUCCEEDED(hr) && hrefVariant.vt == VT_BSTR)
        {
            href = hrefVariant.bstrVal;
        }
        else
        {
            return NULL;
        }

        // Invoke the resource resolver.
        DWORD strLen = 255;
        wchar_t *resolvedUrl = (wchar_t*) malloc(strLen * sizeof(wchar_t));
        HRESULT hr = this->resourceResolver->resolve(href, resolvedUrl, &strLen);
        if (hr == S_FALSE) // Buffer too small
        {
            resolvedUrl = (wchar_t*) realloc(resolvedUrl, strLen * sizeof(wchar_t));
            hr = this->resourceResolver->resolve(href, resolvedUrl, &strLen);
        }

        // If the resolver returned something, use that as the link. Otherwise return the href.
        if (wcslen(resolvedUrl) > 0)
            url = SysAllocString(resolvedUrl);
        else
            url = SysAllocString(href);

        free(resolvedUrl);
        VariantClear(&hrefVariant);
    }
    // If there is no resource resolver, and we're on the original content, and using the
    // default base URL, just return the href. We don't want to return the full URL in this case
    // because a relative link would turn into "about:page.html", and we want just "page.html".
    else if (this->originalContentLoaded && this->htmlContent != NULL && this->htmlContent->IsDefaultBaseUrl())
    {
        VARIANT hrefVariant;
        VariantInit(&hrefVariant);
        hrefVariant.vt = VT_EMPTY;

        hr = anchorElement->getAttribute(L"href", 2, &hrefVariant);
        if (SUCCEEDED(hr) && hrefVariant.vt == VT_BSTR)
        {
            url = SysAllocString(hrefVariant.bstrVal);
        }
        VariantClear(&hrefVariant);
    }
    // Otherwise return the absolute URL according to the browser.
    else
    {
        anchor->get_href(&url);
    }

    return url;
}

void WebViewWindow::AddLineBoxRects(LinkParams *linkParams, IHTMLRectCollection *rects, RECT *viewport) const
{
    long numRects;
    rects->get_length(&numRects);

    VARIANT rectIndex;
    VARIANT dispatchVariant;

    VariantInit(&rectIndex);
    VariantInit(&dispatchVariant);

    rectIndex.vt = VT_I4;
    dispatchVariant.vt = VT_EMPTY;

    for (long j = 0; j < numRects; j++)
    {
        rectIndex.lVal = j;

        HRESULT hr = rects->item(&rectIndex, &dispatchVariant);
        if (SUCCEEDED(hr) && dispatchVariant.vt == VT_DISPATCH)
        {
            CComQIPtr<IHTMLRect> htmlRect = dispatchVariant.pdispVal;
            assert(htmlRect != NULL);

            RECT elementRect;
            htmlRect->get_top(&(elementRect.top));
            htmlRect->get_left(&(elementRect.left));
            htmlRect->get_bottom(&(elementRect.bottom));
            htmlRect->get_right(&(elementRect.right));

            if (this->RectsIntersect(&elementRect, viewport))
            {
                linkParams->AddLinkRect(&elementRect);
            }
        }
    }
    VariantClear(&rectIndex);
    VariantClear(&dispatchVariant);
}

void WebViewWindow::AddImageRects(LinkParams *linkParams, IHTMLElement2 *element2, IHTMLCurrentStyle *anchorStyle, RECT *linkRect, RECT *viewport) const
{
    CComPtr<IHTMLElementCollection> imgNodes = NULL;
    element2->getElementsByTagName(L"img", &imgNodes);

    if (imgNodes == NULL)
        return;

    long numChildNodes = 0;
    imgNodes->get_length(&numChildNodes);

    VARIANT indexVariant;
    VariantInit(&indexVariant);
    indexVariant.vt = VT_I4;

    for (long i = 0; i < numChildNodes; i++)
    {        
        indexVariant.intVal = i;
        
        CComPtr<IDispatch> dispatch;
        HRESULT hr = imgNodes->item(indexVariant, indexVariant, &dispatch);

        if (SUCCEEDED(hr) && dispatch != NULL)
        {
            CComQIPtr<IHTMLElement> element = dispatch;
            if (element == NULL)
                continue;

            CComQIPtr<IHTMLElement2> element2 = element;
            if (element2 == NULL)
                continue;

            if (!this->IsVisible(element))
                continue;

            // Compute the image's bounding box in the document view's coordinate system, and clip it by the document's
            // visible rectangle.
            CComPtr<IHTMLRect> imgRect = NULL;
            element2->getBoundingClientRect(&imgRect);

            // Convert IHTMLRect to RECT
            RECT elementRect;
            imgRect->get_top(&(elementRect.top));
            imgRect->get_left(&(elementRect.left));
            imgRect->get_bottom(&(elementRect.bottom));
            imgRect->get_right(&(elementRect.right));

            RECT imgVisibleRect;
            this->ComputeRectIntersect(&imgVisibleRect, &elementRect, viewport);

            // Clip the image's bounding box against the link's bounding box if the links's CSS overflow attribute is
            // either "hidden" or "scroll". This prevents invisible portions of the image from contributing to the
            // link's pickable area.
            BSTR overflow = NULL;
            anchorStyle->get_overflow(&overflow);

            if (overflow != NULL && (wcscmp(L"hidden", overflow) == 0 || wcscmp(L"scroll", overflow) == 0))
            {
                this->ComputeRectIntersect(&imgVisibleRect, &imgVisibleRect, linkRect);
            }

            if (overflow)
                SysFreeString(overflow);

            // Ignore the image if it is not in the document view's visible rectangle.
            if (this->IsEmptyRect(&imgVisibleRect))
                continue;

            // Add the rectangle to the list.
            linkParams->AddLinkRect(&imgVisibleRect);
        }
    }
    VariantClear(&indexVariant);
}

BOOL WebViewWindow::RectsIntersect(const RECT *elementRect, const RECT *viewport) const
{
    return elementRect->right > viewport->left
        && elementRect->bottom > viewport->top
        && elementRect->left < viewport->right
        && elementRect->top < viewport->bottom;
}

void WebViewWindow::ComputeRectIntersect(RECT *intersection, const RECT *r1, const RECT *r2) const
{
    BOOL intersect = this->RectsIntersect(r1, r2);

    if (intersect)
    {
        SetRect(intersection,
            max(r1->left, r2->left),
            max(r1->top, r2->top),
            min(r1->right, r2->right),
            min(r1->bottom, r2->bottom));
    }
    else
    {
        SetRect(intersection, 0, 0, 0, 0);
    }
}

void WebViewWindow::ComputeRectUnion(RECT *unionRect, const RECT *r1, const RECT *r2) const
{
    // If the r1 rect is empty, set the union to be the other rect
    if (this->IsEmptyRect(r1))
    {
        SetRect(unionRect, r2->left, r2->top, r2->right, r2->bottom);
    }
    // If r2 is empty, make the union r1
    else if (this->IsEmptyRect(r2))
    {
        SetRect(unionRect, r1->left, r1->top, r1->right, r1->bottom);
    }
    // If both rects are non-empty, actually find the union
    else
    {
        SetRect(unionRect,
            min(r1->left, r2->left),
            min(r1->top, r2->top),
            max(r1->right, r2->right),
            max(r1->bottom, r2->bottom));
    }
}

BOOL WebViewWindow::IsEmptyRect(const RECT *rect) const
{
    if (rect == NULL)
        return TRUE;

    return (rect->right - rect->left == 0) && (rect->bottom - rect->top == 0);
}

/////////////////////////////////////////////////////////
// Methods determining things about the current content
/////////////////////////////////////////////////////////

BOOL WebViewWindow::ContainsEmbeddedContent() const
{
    if (this->browser == NULL)
        return FALSE;

    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = this->browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return FALSE;
    }

    CComQIPtr<IHTMLDocument3> pDoc3 = pDispatch;
    if (pDoc3 == NULL)
        return FALSE;

    // Search the document for OBJECT tags. If any are found, assume that the page
    // contains embedded content that may not report when it has changed.
    CComPtr<IHTMLElementCollection> objectTags = NULL;
    pDoc3->getElementsByTagName(L"object", &objectTags);

    if (objectTags == NULL)
    {
        return FALSE;
    }

    long length = 0;
    objectTags->get_length(&length);

    if (length > 0)
        return TRUE;

    // If no object tags were found, look for EMBED tags
    CComQIPtr<IHTMLDocument2> pDoc2 = pDispatch;
    if (pDoc2 == NULL)
        return FALSE;

    // Search the document for EMBED tags. If any are found, assume that the page
    // contains embedded content that may not report when it has changed.
    CComPtr<IHTMLElementCollection> embedTags = NULL;
    pDoc2->get_embeds(&embedTags);

    embedTags->get_length(&length);

    return length > 0;
}

void WebViewWindow::DetermineScrollBars()
{
    // Determine if the page needs scroll bars. We want scroll bars only if the
    // page actually needs them. We don't want inactive scroll bars that are not needed.
    //
    // The WebBrowser has an "auto" scroll bar mode, but this mode doesn't work
    // for our purposes when the browser renders pages in standards mode. The scroll
    // bars show up in the real browser window, but not in the captured window. For some
    // reason these scroll bars cannot be captured by IViewObject::Draw. To work around
    // this, we need to explicit turn the scroll bars on or off in the page's CSS.
    //
    // However, we still need to set "auto" on the body element, because otherwise pages
    // that render in compatibility mode have inactive scroll bars when they don't need them.

    if (this->browser == NULL)
        return;

    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = this->browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return;
    }

    CComQIPtr<IHTMLDocument2> pDoc2 = pDispatch;
    if (pDoc2 == NULL)
        return;
    
    CComPtr<IHTMLElement> pElement = NULL;
    hr = pDoc2->get_body(&pElement);
    if (FAILED(hr) || pElement == NULL)
        return;

    // Set scroll bars on the body element to "auto". The makes scroll bars work correctly
    // on pages that render in compatibility mode.
    CComQIPtr<IHTMLBodyElement> pBody = pElement;
    if (pBody != NULL)
        pBody->put_scroll(L"auto");

    // "auto" doesn't work for pages that render in standards mode. For these pages we need
    // to explicitly enable or disable the scroll bars in the page's CSS.

    // Get the document from the dispatch interface
    CComQIPtr<IHTMLDocument3> pDoc3 = pDispatch;
    if (pDoc3 == NULL)
        return;

    // Get the document's root element
    CComPtr<IHTMLElement> rootElement  = NULL;
    pDoc3->get_documentElement(&rootElement);
    if (rootElement == NULL)
        return;

    CComQIPtr<IHTMLElement2> rootElement2 = rootElement;
    if (rootElement2 == NULL)
        return;

    // Temporarily turn off scroll bars. The presence of scroll bars changes the size of the visible area.
    // We need to know the size without scroll bars in order to know if the scroll bars are required.
    CComPtr<IHTMLStyle> rootStyle = NULL;
    rootElement->get_style(&rootStyle);
    if (rootStyle != NULL)
    {
        rootStyle->put_overflow(L"hidden");
    }

    // Determine the total size of the content, and the size of the visible area. scrollWidth/Height is
    // the total size, and clientWidth/Height is the visible size.
    // See http://msdn.microsoft.com/en-us/library/ms530302%28VS.85%29.aspx for more on what these values
    // mean to Internet Explorer.
    long scrollHeight, scrollWidth;
    rootElement2->get_scrollWidth(&scrollWidth);
    rootElement2->get_scrollHeight(&scrollHeight);

    long clientHeight, clientWidth;
    rootElement2->get_clientWidth(&clientWidth);
    rootElement2->get_clientHeight(&clientHeight);

    // Determine if the WebView needs scroll bars by comparing the total size to the visible size.
    // Setting the scrollbars to "auto" does not always work with IViewObject::Draw. Some pages work
    // fine, but other pages that should have scrollbars do not in the captured view. Here we
    // explicitly turn on scrollbars if we determine if the page needs them. This setting only affects
    // pages that render in standards mode.
    BSTR scrollString = NULL;
    if (scrollHeight > clientHeight || scrollWidth > clientWidth)
        scrollString = L"scroll";
    else
        scrollString = L"hidden";

    // Turn scroll bars on or off by setting the overflow CSS property.
    if (rootStyle != NULL)
    {
        rootStyle->put_overflow(scrollString);
    }
}

void WebViewWindow::DetermineContentURL()
{
    if (this->contentURL != NULL)
        SysFreeString(this->contentURL);

    // If the browser does not have the original content loaded, capture the current URL. If the original content is loaded
    // set the URL to NULL.
    if (!this->originalContentLoaded)
    {
        browser->get_LocationURL(&(this->contentURL));
    }
    else
    {
        this->contentURL = NULL;
    }
}

HRESULT WebViewWindow::DetermineContentSize()
{
    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = this->browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return E_FAIL;
    }

    // Get the document from the dispatch interface
    CComQIPtr<IHTMLDocument2> pDoc2 = pDispatch;
    if (pDoc2 == NULL)
        return E_FAIL;

    CComQIPtr<IHTMLDocument5> pDoc5 = pDoc2;
    if (pDoc5 == NULL)
        return E_FAIL;

    {
        // Wrap the window in a Restorable window object to so that we can change its size
        // and be sure that the previous size will be restored, even if something goes wrong.
        RestorableWindow restorableWin(this->m_hWnd);
        
        // Resize the window to the minimum content size so that we can determine the content size
        // independent of the current frame size. The window will resize back to the previous size
        // when restorableWin goes out of scope.
        restorableWin.Resize(this->minContentWidth, this->minContentHeight);

        // Determine if the browser is using quirks mode or standards mode. The scroll size
        // is retrieved from different properties, depending on the mode.
        BSTR compatMode;
        pDoc5->get_compatMode(&compatMode);
        if (wcscmp(compatMode, L"BackCompat") == 0)
        {
            this->DetermineContentSizeCompatibilityMode(pDoc2);
        }
        else
        {
            this->DetermineContentSizeStandardsMode(pDoc2);
        }

        SysFreeString(compatMode);
    }

    // Add space for a vertical scroll bar
    const int verticalScrollbarSize = GetSystemMetrics(SM_CXVSCROLL);
    this->contentWidth += verticalScrollbarSize;

    // Add space for a horizontal scroll bar
    const int horizontalScrollbarSize = GetSystemMetrics(SM_CXHSCROLL);
    this->contentHeight += horizontalScrollbarSize;

    return S_OK;
}

HRESULT WebViewWindow::DetermineContentSizeStandardsMode(IHTMLDocument2 *pDoc)
{
    // Get the document's root element
    CComQIPtr<IHTMLDocument3> pDoc3 = pDoc;
    if (pDoc3 == NULL)
        return E_FAIL;

    CComPtr<IHTMLElement> rootElement = NULL;
    HRESULT hr = pDoc3->get_documentElement(&rootElement);
    if (FAILED(hr) || rootElement == NULL)
        return E_FAIL;

    CComQIPtr<IHTMLElement2> rootElement2 = rootElement;
    if (rootElement2 == NULL)
        return E_FAIL;

    long scrollWidth = 0;
    long scrollHeight = 0;

    rootElement2->get_scrollWidth(&scrollWidth);
    rootElement2->get_scrollHeight(&scrollHeight);

    this->contentWidth = scrollWidth;
    this->contentHeight = scrollHeight;

    return S_OK;
}

HRESULT WebViewWindow::DetermineContentSizeCompatibilityMode(IHTMLDocument2 *pDoc)
{
    CComPtr<IHTMLElement> body;

    HRESULT hr = pDoc->get_body(&body);
    if (FAILED(hr) || body == NULL)
        return E_FAIL;

    CComQIPtr<IHTMLElement2> body2 = body;
    if (body2 == NULL)
        return E_FAIL;

    long scrollWidth;
    long scrollHeight;

    body2->get_scrollWidth(&scrollWidth);
    body2->get_scrollHeight(&scrollHeight);

    this->contentWidth = scrollWidth;
    this->contentHeight = scrollHeight;

    return S_OK;
}

BOOL WebViewWindow::OnDocumentComplete()
{
    wchar_t *IEServerWndClass = L"Internet Explorer_Server";
    wchar_t *IEHiddenWndClass = L"Internet Explorer_Hidden";

    // Find the child window that holds the web browser control. We'll need this window handle to send events
    // to the browser.
    this->hWndBrowser = FindChildWindow(m_hWnd, IEServerWndClass);
    if (this->hWndBrowser == NULL)
        Logging::logger()->warning(L"WebView.CannotFindWindow", IEServerWndClass);

    // Find the hidden window that controls user input on scroll bars.
    if (this->hWndScrollControl == NULL)
    {
        this->hWndScrollControl = FindThreadWindow(GetCurrentThreadId(), IEHiddenWndClass);
        if (this->hWndScrollControl == NULL)
            Logging::logger()->warning(L"WebView.CannotFindWindow", IEHiddenWndClass);
    }

    CComPtr<IDispatch> pDispatch = NULL;
    HRESULT hr = browser->get_Document(&pDispatch);
    if (FAILED(hr) || pDispatch == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return FALSE;
    }

    // Get the document from the dispatch interface
    CComQIPtr<IHTMLDocument2> pDoc = pDispatch;
    if (pDoc == NULL)
        return FALSE;

    // On Internet Explorer 6, IHTMLDocument2 does not always support IViewObject. If the document does not support IViewObject
    // we silently ignore it. The documents that do not support IViewObject seem to be in intermediate states, or maybe image and
    // script files referenced by the main document. Ignoring them here does not affect our ability to capture the main document.
    CComQIPtr<IViewObject> pViewObject = pDoc;
    if (pViewObject != NULL)
    {
        // Subscribe to changes in the document view. This will cause a call to OnViewChanged when the 
        // the rendered page changes. This method works perfectly for most pages, but for some pages,
        // including http://maps.google.com and some (though not all) pages that use Flash, we get a
        // constant stream of updates, even if nothing has actually changed.
        CComQIPtr<IAdviseSink> adviseSink = this;
        assert(adviseSink != NULL);
        hr = pViewObject->SetAdvise(DVASPECT_CONTENT, 0, adviseSink);
        if (FAILED(hr))
            Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
    }

    // Turn scroll bars on or off depending on the size of the content.
    this->DetermineScrollBars();

    // If the content contains EMBED tags, assume that we can't rely on IViewObject to tell us when the page has changed.
    // Sometimes Flash content does not properly notify us of changes to the view, so we'll just capture periodically if
    // there is embedded content.
    this->alwaysCapture = this->ContainsEmbeddedContent();

    // Set the page background color
    this->ApplyBackgroundColor();

    // New content loaded, make sure that it is captured
    this->needToCapture = TRUE;
    this->CaptureWebView();

    // If the new document is not the original content, and the clear travel log flag is set, clear the travel log.
    // This resets the travel log when the browser navigates away from the original page, then back to the original,
    // and then off to a different page. The browser doesn't consider loading content using HTMLMoniker to be a real
    // navigation event, so it doesn't realize that the history needs to be truncated if the browser goes back to the
    // original content and navigates to a different page.
    if (this->mustClearTravelLog && !this->originalContentLoaded)
    {
        this->ClearTravelLog();
        this->mustClearTravelLog = FALSE;
    }

    return TRUE;
}

void WebViewWindow::GetContentSize(int *width, int *height) const
{
    MutexLock lock(this->mutex);

    if (width != NULL)
        *width = this->contentWidth;

    if (height != NULL)
        *height = this->contentHeight;
}

void WebViewWindow::GetMinContentSize(int *width, int *height) const
{
    MutexLock lock(this->mutex);

    if (width != NULL)
        *width = this->minContentWidth;

    if (height != NULL)
        *height = this->minContentHeight;
}

LRESULT WebViewWindow::OnSetMinContentSize(UINT uMsg, WPARAM width, LPARAM height, BOOL bHandled)
{
    MutexLock lock(this->mutex);

    this->minContentWidth = static_cast<int>(width > 0 ? width : DEFAULT_MIN_CONTENT_WIDTH);
    this->minContentHeight = static_cast<int>(height > 0 ? height : DEFAULT_MIN_CONTENT_HEIGHT);

    // Increment the content load ID so that the content size will be redetermined the next time that the
    // WebView is captured.
    this->contentLoadID++;

    // Ensure that the WebView will update using the new size.
    this->ScheduleCapture();

    return ERROR_SUCCESS;
}

void WebViewWindow::GetLinks(LinkParamCollection **linkCollection) const
{
    MutexLock lock(this->mutex);
    
    if (this->links != NULL)
        this->links->AddRef();
    
    *linkCollection = this->links;
}

void WebViewWindow::GetResourceResolver(WebResourceResolver **resolver) const
{
    *resolver = this->resourceResolver;
    (*resolver)->AddRef();
}

BSTR WebViewWindow::GetContentURL() const
{
    if (this->contentURL != NULL)
        return SysAllocString(this->contentURL);

    return NULL;
}

////////////////////////////////
// IAdviseSink implementation
////////////////////////////////

void WebViewWindow::OnViewChange(DWORD dwAspect, LONG lindex)
{
    this->needToCapture = TRUE;
}

////////////////////////////////
// IDispatch implementation
////////////////////////////////

STDMETHODIMP WebViewWindow::Invoke(DISPID dispIdMember, REFIID riid, LCID lcid, WORD wFlags,
                                   DISPPARAMS *pDispParams, VARIANT *pVarResult,
                                   EXCEPINFO *pExcepInfo,UINT *puArgErr)
{
    switch (dispIdMember)
    {
    case DISPID_DOCUMENTCOMPLETE:
        {
            this->OnDocumentComplete();

            VARIANT dispVar;
            VariantInit(&dispVar);

            // The DocumentComplete event fires once for each frame in a page. Determine if this document complete
            // is the final event for the top level frame. When the final document complete fires, the pDisp parameter
            // will be the same object as the web browser. See http://support.microsoft.com/kb/180366 for more information
            // on this method of determining when the page is done loading.
            // We don't want to update the content size as each frame loads because some pages (http://maps.google.com)
            // report an invalid content size before the top level frame finishes loading.
            HRESULT hr = VariantChangeType(&dispVar, &pDispParams->rgvarg[1], 0, VT_DISPATCH);
            if (SUCCEEDED(hr))
            {
                CComQIPtr<IWebBrowser2> dispBrowser = dispVar.pdispVal;
                if (dispBrowser == this->browser)
                {
                    // The browser needs to load a page before it is fully initialized. We create the browser with the page
                    // "about:blank". When this page loads, consider the browser initialized. But don't fire the usual
                    // document complete events for the about:blank page load, it's not real content.
                    if (this->browserInitialized)
                    {
                        // The content is finished loading. Increment the contentLoadID so that the content URL and size will
                        // be updated.
                        this->contentLoadID++;

                        // Ensure that the new content is captured at least once.
                        this->CaptureWebView();
                    }
                    else
                        this->browserInitialized = TRUE;
                }
            }

            VariantClear(&dispVar);
        }
        break;

    case DISPID_NAVIGATEERROR:
        {
            VARIANT dispVar;
            VariantInit(&dispVar);

            // If the top level frame failed to load, set the content load time so that the content
            // size will be computed. The top level frame is identified using the technique described
            // above for DocumentComplete.
            HRESULT hr = VariantChangeType(&dispVar, &pDispParams->rgvarg[4], 0, VT_DISPATCH);
            if (SUCCEEDED(hr))
            {
                CComQIPtr<IWebBrowser2> dispBrowser = dispVar.pdispVal;
                if (dispBrowser == this->browser)
                {
                    this->contentLoadID++;
                }
            }

            VariantClear(&dispVar);
        }
        break;

    case DISPID_BEFORENAVIGATE2:
        this->originalContentLoaded = FALSE;
        break;

    // Do not allow links to be opened in a new window.
    case DISPID_NEWWINDOW2:
    case DISPID_NEWWINDOW3:
        {
            // Get a reference to the cancel parameter. We set this parameter to TRUE to cancel navigation.
            VARIANT_BOOL *cancel = V_BOOLREF(&pDispParams->rgvarg[0]);
            *cancel = VARIANT_TRUE;
        }
        break;
    }
    return S_OK;
}

STDMETHODIMP WebViewWindow::GetTypeInfoCount(UINT *pctinfo)
{
    return E_NOTIMPL;
}

STDMETHODIMP WebViewWindow::GetTypeInfo(UINT iTInfo,LCID lcid,ITypeInfo **ppTInfo)
{
    return E_NOTIMPL;
}

STDMETHODIMP WebViewWindow::GetIDsOfNames(REFIID riid,LPOLESTR *rgszNames,UINT cNames,LCID lcid,DISPID *rgDispId)
{
    return E_NOTIMPL;
}

/////////////////////////////
// Debugging support
/////////////////////////////

void WebViewWindow::DumpTravelLog() const
{
    CComQIPtr<IServiceProvider> serviceProvider = browser;
    if (serviceProvider == NULL)
        return;

    CComPtr<ITravelLogStg> travelLog = NULL;
    serviceProvider->QueryService(SID_STravelLogCursor, IID_ITravelLogStg, (void**)&travelLog);

    if (travelLog == NULL)
        return;

    CComPtr<IEnumTravelLogEntry> entries = NULL;
    HRESULT hr = travelLog->EnumEntries(TLEF_RELATIVE_INCLUDE_CURRENT | TLEF_ABSOLUTE, &entries);

    ATLTRACE(L"=== Dumping travel log ===");
    int i = 0;
    BOOL moreEntries = TRUE;
    ITravelLogEntry *e = NULL;
    while (entries->Next(1, &e, NULL) == S_OK)
    {
        BSTR url = NULL;
        e->GetURL(&url);
        ATLTRACE(L"%i: %s", i, url);

        e->Release();
        SysFreeString(url);

        i++;
    }
}

void WebViewWindow::WriteBitmapToFile(wchar_t *fileName) const
{
    MutexLock lock(bitmapMutex);

    BITMAPFILEHEADER bmfHeader;    
    BITMAPINFOHEADER bi;
     
    bi.biSize = sizeof(BITMAPINFOHEADER);    
    bi.biWidth = this->captureWidth;
    bi.biHeight = -this->captureHeight;
    bi.biPlanes = 1;    
    bi.biBitCount = 24;    
    bi.biCompression = BI_RGB;    
    bi.biSizeImage = 0;  
    bi.biXPelsPerMeter = 0;    
    bi.biYPelsPerMeter = 0;    
    bi.biClrUsed = 0;    
    bi.biClrImportant = 0;

    // A file is created, this is where we will save the screen capture.
    HANDLE hFile = CreateFile(fileName, GENERIC_WRITE, 0, NULL,CREATE_ALWAYS,
        FILE_ATTRIBUTE_NORMAL, NULL);   
    
    DWORD bitmapSize = this->captureWidth * this->captureHeight * (bi.biBitCount / 8);

    // Add the size of the headers to the size of the bitmap to get the total file size
    DWORD dwSizeofDIB = bitmapSize + sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER);
 
    //Offset to where the actual bitmap bits start.
    bmfHeader.bfOffBits = static_cast<DWORD>(sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER)); 
    
    //Size of the file
    bmfHeader.bfSize = dwSizeofDIB;    
    bmfHeader.bfType = BITMAP_FILE_TYPE;
 
    DWORD dwBytesWritten = 0;
    WriteFile(hFile, reinterpret_cast<void*>(&bmfHeader), sizeof(BITMAPFILEHEADER), &dwBytesWritten, NULL);
    WriteFile(hFile, reinterpret_cast<void*>(&bi), sizeof(BITMAPINFOHEADER), &dwBytesWritten, NULL);
    WriteFile(hFile, reinterpret_cast<void*>(this->captureBits), bitmapSize, &dwBytesWritten, NULL);
    
    //Close the handle for the file that was created
    CloseHandle(hFile);
}
