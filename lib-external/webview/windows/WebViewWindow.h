/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Version: $Id: WebViewWindow.h 1171 2013-02-11 21:45:02Z dcollins $
 */

#ifndef WEB_VIEW_WINDOW_H
#define WEB_VIEW_WINDOW_H

#include "stdafx.h"
#include "HTMLMoniker.h"
#include "WebResourceResolver.h"
#include "LinkParams.h"
#include "LinkParamCollection.h"
#include "HTMLMoniker.h"
#include "util/Logging.h"

#include <GL/gl.h>    
#include <vector>
#include <tlogstg.h>

/** Default base URL. */
extern const wchar_t *DEFAULT_BASE_URL;

/** Default value for the minimum content width. */
const int DEFAULT_MIN_CONTENT_WIDTH = 300;
/** Default value for the minimum content height. */
const int DEFAULT_MIN_CONTENT_HEIGHT = 100;

/**
 * Message that tells the window to load new HTML content.
 *
 * LPARAM: HTMLMoniker*. This moniker will be handed to MSHTML to provide a stream
 * of new content.
 */
const UINT WM_SET_HTML = WM_APP + 0;

/** Message to navigate browser back. */
const UINT WM_GO_BACK = WM_APP + 2;

/** Message to navigate browser forward. */
const UINT WM_GO_FORWARD = WM_APP + 3;

/** Message send to WebView control window to create a new WebViewWindow. */
const UINT WM_WEBVIEW_CREATE = WM_APP + 4;

/**
 * Message send to WebView control window to destroy a WebViewWindow.
 *
 * WPARAM: HWND handle to WebView window to destroy.
 */
const UINT WM_WEBVIEW_DESTROY = WM_APP + 5;

/** Message send to WebView control window trigger updates in all the WebViewWindows managed by the control window. */
const UINT WM_WEBVIEW_UPDATE = WM_APP + 6;

/**
 * Message for simulating WM_MOUSEWHEEL messages. We can't just send WM_MOUSEWHEEL
 * to the WebView window and let it pass along to the browser window because the browser
 * sends the mouse wheel message back to the window if it can't scroll, and there would
 * be no way to tell the first simulated event from the one sent back from the browser.
 *
 * This message takes the same parameters as WM_MOUSEWHEEL.
 */
const UINT WM_SIM_MOUSEWHEEL = WM_APP + 8;

/**
 * Message to alert the WebView that it has received user input focus.
 *
 * WPARAM: BOOL true to indicate that the WebView is being activated.
 */
const UINT WM_WEBVIEW_ACTIVATE = WM_APP + 9;

/**
 * Message to set the background color of the WebView.
 *
 * LPARAM: wchar_t* Hex string that specifies background color.
 */
const UINT WM_WEBVIEW_SET_BACKGROUND_COLOR = WM_APP + 10;

/**
 * Message to set the WebView resource resolver.
 *
 * LPARAM: WebResourceResolver*, or NULL.
 */
const UINT WM_WEBVIEW_SET_RESOURCE_RESOLVER = WM_APP + 11;

/**
 * Message to set the WebView notification listener.
 *
 * LPARAM: IAdviseSink*, or NULL.
 */
const UINT WM_WEBVIEW_SET_ADVISE = WM_APP + 12;

/**
 * Message to set the WebView minimum content size.
 *
 * WPARAM: minimum width. If width <= 0, the default width will be set.
 * LPARAM: mimimum height. If height <= 0, the default height will be set.
 */
const UINT WM_WEBVIEW_SET_MIN_CONTENT_SIZE = WM_APP + 13;

/** Marker message for the last high priority WebView message. Messages with codes higher than this are considered low priority. */
const UINT WM_WEBVIEW_HIPRIORITY_LAST = WM_WEBVIEW_SET_ADVISE;

/** Message capture the WebView to a bitmap. This is processed as a low priority message, similar to how Windows normally handles the WM_PAINT message. */
const UINT WM_WEBVIEW_CAPTURE = WM_APP + 14;

/**
 * Helper class to hold details about an entry in the back/forward navigation list (TravelLog).
 */
class WebViewTravelLogEntry
{
public:
    WebViewTravelLogEntry(BSTR url, BSTR title)
    {
        this->url = url;
        this->title = title;
    }

    std::wstring url;
    std::wstring title;
};

/**
 * WebViewWindow provides a window to host the WebBrowser control, functions to load content and manage navigation, methods to capture
 * the rendered page to a bitmap, and methods to simulate input to the WebBrowser. Most functions of the WebViewWindow can be accessed
 * by sending WM_WEBVIEW_* messages to the window. This provides a safe way to manipulate the WebBrowser from a thread other than the
 * window's UI thread.
 */
class ATL_NO_VTABLE WebViewWindow :
    public CComObjectRootEx<CComSingleThreadModel>,
    public CWindowImpl<WebViewWindow, CAxWindow>,
    public IDispatch,
    public IAdviseSink
{
public:
    WebViewWindow();
    void FinalRelease();

    BEGIN_COM_MAP(WebViewWindow)
        COM_INTERFACE_ENTRY(IDispatch)
        COM_INTERFACE_ENTRY(IAdviseSink)
    END_COM_MAP()
    DECLARE_NOT_AGGREGATABLE(WebViewWindow)

    BEGIN_MSG_MAP(WebViewWindow)
        MESSAGE_HANDLER(WM_WEBVIEW_CAPTURE, OnCapture)
        MESSAGE_HANDLER(WM_WEBVIEW_ACTIVATE, OnActivate)
        MESSAGE_HANDLER(WM_SET_HTML, OnSetHTML)
        MESSAGE_HANDLER(WM_TIMER, OnTimer)
        MESSAGE_HANDLER(WM_GO_BACK, OnGoBack)
        MESSAGE_HANDLER(WM_GO_FORWARD, OnGoForward)
        MESSAGE_HANDLER(WM_WEBVIEW_SET_BACKGROUND_COLOR, OnSetBackgroundColor)
        MESSAGE_HANDLER(WM_WEBVIEW_SET_RESOURCE_RESOLVER, OnSetResourceResolver)
        MESSAGE_HANDLER(WM_WEBVIEW_SET_ADVISE, OnSetAdvise)
        MESSAGE_HANDLER(WM_WEBVIEW_SET_MIN_CONTENT_SIZE, OnSetMinContentSize)

        MESSAGE_HANDLER(WM_SIZE, OnFrameSizeChanged);

        // The following events will be forwarded to the web browser control
        MESSAGE_HANDLER(WM_KEYDOWN, OnSimulateInput);
        MESSAGE_HANDLER(WM_KEYUP, OnSimulateInput);
        MESSAGE_HANDLER(WM_MOUSEMOVE, OnSimulateInput);
        MESSAGE_HANDLER(WM_LBUTTONDOWN, OnSimulateInput);
        MESSAGE_HANDLER(WM_LBUTTONUP, OnSimulateInput);
        MESSAGE_HANDLER(WM_RBUTTONDOWN, OnSimulateInput);
        MESSAGE_HANDLER(WM_RBUTTONUP, OnSimulateInput);
        MESSAGE_HANDLER(WM_MBUTTONDOWN, OnSimulateInput);
        MESSAGE_HANDLER(WM_MBUTTONUP, OnSimulateInput);
        MESSAGE_HANDLER(WM_LBUTTONDBLCLK, OnSimulateInput);
        MESSAGE_HANDLER(WM_RBUTTONDBLCLK, OnSimulateInput);
        MESSAGE_HANDLER(WM_MBUTTONDBLCLK, OnSimulateInput);
        MESSAGE_HANDLER(WM_SIM_MOUSEWHEEL, OnSimulateInput);
    END_MSG_MAP()

public:
    /**
     * Find a WebViewWindow by ID.
     *
     * @param webViewId ID of the window to locate.
     *
     * @return A pointer to the desired window, or NULL if the window cannot be located.
     */
    static WebViewWindow* WebViewWindow::FindWebView(LONG_PTR webViewId);

    /** Create the WebBrowser control. This method must be called on the thread will own this window. */
    HRESULT CreateWebBrowser();

    /**
     * Get the ID number for this WebView.
     *
     * @return WebView ID. This number can be passed to FindWebView to locate the WebView instance.
     */
    LONG_PTR GetWebViewId() const;

    /**
     * Get a reference to the WebResourceResolver that this WebView uses to resolve relative links.
     *
     * @param Handle to pointer that will receiver the resolver reference. This object must be released by calling
     *        IUnknown::Release.
     */
    void GetResourceResolver(WebResourceResolver **resolver) const;

    /**
     * Upload the captured WebView bitmap to a GL texture.
      *
     * @param target Texture to upload to.
     */
    BOOL CaptureToGLTexture(GLenum target);

    /**
     * Indicates the time at which the WebView rendered content most recently changed.
     * 
     * @return the time at which the rendered page was updated.
     */
    DWORD GetUpdateTime() const { return this->updateTime; }

    /**
     * Get the links in the currently loaded content.
     *
     * @param linkCollection Pointer receive collection of links that are visible in the currently rendered page.
     *        This collection must be released by calling IUnknown::Release when the caller is finished with it.
     */
    void GetLinks(LinkParamCollection **linkCollection) const;

    /** Get the size of the scrollable content. */
    void GetContentSize(int *width, int *height) const;

    /**
     * Get the minimum size of the WebView content.
     *
     * @param width integer to receive the minimum content width.
     * @param height integer to receive the minimum content height.
     */
    void GetMinContentSize(int *width, int *height) const;

    /**
     * Get the URL of the current content.
     *
     * @return the current URL, or NULL if the content was loaded from a string.
     */
    BSTR GetContentURL() const;

    /**
     * Schedule a capture operation, if the WebView needs to capture. Has no effect
     * if the WebView does not need to capture.
     */
	void ScheduleCapture();

    /**
     * Get the message-only control window that manages this WebView.
     *
     * @return a handle the control window for this WebView.
     */
    HWND GetControlWindow() const { return this->controlWnd; }

    /**
     * Set the message-only control window that manages this WebView.
     *
     * @param wnd A handle the control window for this WebView.
     */
    void SetControlWindow(HWND wnd) { this->controlWnd = wnd; }

public:
    // IDispatch
    STDMETHODIMP GetTypeInfoCount(UINT *pctinfo);
    STDMETHODIMP GetTypeInfo(UINT iTInfo, LCID lcid, ITypeInfo **ppTInfo);
    STDMETHODIMP GetIDsOfNames(REFIID riid, LPOLESTR *rgszNames, UINT cNames, LCID lcid, DISPID *rgDispId);
    STDMETHODIMP Invoke(DISPID dispIdMember, REFIID riid, LCID lcid, WORD wFlags,
                        DISPPARAMS *pDispParams, VARIANT *pVarResult, EXCEPINFO *pExcepInfo,
                        UINT *puArgErr);

public:
    // IAdviseSink
    void STDMETHODCALLTYPE OnDataChange(FORMATETC *pFormatetc, STGMEDIUM *pStgmed) { };
    void STDMETHODCALLTYPE OnViewChange(DWORD dwAspect, LONG lindex);
    void STDMETHODCALLTYPE OnRename(IMoniker *pmk) { }
    void STDMETHODCALLTYPE OnSave() { }
    void STDMETHODCALLTYPE OnClose() { }

protected:
    /**
     * Flag to indicate that the WebView protocol has been initialized. This is only done
     * per process.
     */
    static BOOL protocolInitialized;

    /**
     * Register a protocol namespace handler for the "webview" protocol. This custom protocol
     * allows the WebView to invoke the ResourceLocator to resolve local references. To use this
     * feature, the WebView base URL must be of the form "webview://[webViewId]/", where [webViewId]
     * is the ID number of a WebView. (See GetWebViewId).
     */
    void InitializeWebViewProtocol();

protected:
    /** Handle to the message-only control window that manages this WebView. */
    HWND controlWnd;

    /** The WebBrowser control. */
    IWebBrowser2 *browser;

    /** Cookie needed for IDispatch. */
    DWORD dispatchCookie;

    /**
     * Handle to the web browser window. Note that this is not the top level window that we create, it is
     * a child window that teh ActiveX container creates.
     */
    HWND hWndBrowser;
    /**  Handle to the hidden window that handles scrollbar input. */
    HWND hWndScrollControl;
    /**
     * Handle to the window that is under the cursor. This may be the browser window, or it may be an 
     * embedded child window. Flash content, for example, runs in an embedded ActiveX window.
     */
    HWND hWndUnderCursor;

    /** Indicates that the rendered content has changed, and needs to be captured. */
    BOOL needToCapture;
    /** Indicates that the WebViewWindow has user input focus. The mouse is within the window. */
    BOOL active; 
    /**
     * Indicates that the contents will be captured periodically even if we haven't received a view change event.
     * Embedded content, such as Flash video, sometimes does not trigger view change events even though the content
     * has changed.
     */
    BOOL alwaysCapture;

    /** Content string that was originally loaded into browser. */
    HTMLMoniker *htmlContent;
    /**
     * Indicates that the browser is fully initalized. The browser is initialized when it finishes loading
     * the initial about:blank page.
     */
    BOOL browserInitialized;

    /** Mutex to synchronize access to the bitmap. */
    HANDLE bitmapMutex;
    /** Handle to Device Independent Bitmap. */
    HBITMAP hCaptureDIB;
    /** Width of the captured image. */
    int captureWidth;
    /** Height of the captured image. */
    int captureHeight;
    /** Bitmap contents. */
    char *captureBits;
    /** Indicates that a message has been sent that will trigger a WebView capture some time in the future. */
	BOOL captureScheduled;

    /** Time at which the rendered page was last updated. */
    DWORD updateTime;

    /** Mutex to synchronize access fields read by the WebView thread and Java threads. */
    HANDLE mutex;
    /** Links in the current content. */
    LinkParamCollection *links;

    /** Listener that will be notified when the rendered page changes. */
    IAdviseSink *adviseSink;

    /** Object to resolver relative references. */
    WebResourceResolver *resourceResolver;

    /** Indicates if the WebView is scrolling. */
    BOOL isScrolling;
    /** Indicates which scroll bar thumb is active. NULL if no thumb is active. */
    const char *activeScroller;
    /** The mouse point at which a scroll operation started. */
    LPARAM scrollRefPoint;
    /** The element that is scrolling. NULL if no element is scrolling. */
    IHTMLElement2 *scrollElement;
    /**
     * The scroll position of the page when scrolling started. This is measured from the top
     * or left of the scroll area, depending on which scroll bar is active.
     */
    long scrollRefPosition;

    /** Last point to receive mouse input. This is used to fake mouse hover events. */
    LPARAM lastInputPoint;

    /** Background color (as a Hex string) of the WebView. */
    wchar_t *backgroundColor;
    
    /**
     * ID of that identifies the last time that the content metadata was updated. The metadata must be redetermined when this ID
     * does not match contentLoadID.
     */
    DWORD contentMetadataUpdateID;
    /** ID that is incremented when the new content is loaded. */
    DWORD contentLoadID;

    /** Width of the WebView content. This is equal to the scroll width of the page, as reported by the WebBrowser. */
    int contentWidth;
    /** Height of the WebView content. This is equal to the scroll width of the page, as reported by the WebBrowser. */
    int contentHeight;

    /** Minimum width of the WebView content. */
    int minContentWidth;
    /** Minimum height of the WebView content. */
    int minContentHeight;

   /** Indicates whether or not the original WebView content is loaded. */
    BOOL originalContentLoaded;
    /** URL of the currently loaded content, or NULL if the content was loaded from an HTML moniker. */
    BSTR contentURL;

    /**
     * Saved copy of the browser's back/forward history. The history is saved when the browser
     * navigates back to the original content, and is restored when the browser navigates forward
     * from the original content.
     */
    std::vector<WebViewTravelLogEntry> savedTravelLog;

    /**
     * Flag to indicate that the browser's travel log should be cleared the next time a page (other than the original content) loads.
     * Clearing the travel log at this point is necessary to correctly handle the case where the browser navigates away from the original
     * content, then back, and then away from the original content via a different link than the first navigation.
     */
    BOOL mustClearTravelLog;

protected:
    /** Load the original HTML content from the htmlContent field. */
    HRESULT SetHTML();

    /**
     * Capture the current state of the WebView for hand-off to the Java code. The capture includes
     * generating a bitmap of the current WebView contents, determining the position of links on the page,
     * determining the content URL, and determining the content size.
     */
    BOOL CaptureWebView();

    /** Assign an ID number to this WebView. */
    void AssignWebViewId();

    /** Create a bitmap representation of the current WebView contents. */
    int CaptureBitMap(IViewObject *pViewObject);

    /**
     * Draw a caret in a DeviceContext in the same location as the active caret in the WebViewWindow.
     * The caret is drawn as an rectangle with colors inverted.
     *
     * @param hDC DeviceContext to draw on.
     */
    void DrawCaret(HDC hDC) const;

    /**
     * Called when a document loads. This method is called when the DWebBrowserEvents2::DocumentComplete event is received.
     * This event is fired once for each frame that loads.
     */
    BOOL OnDocumentComplete();

    /**
     * Move the window to the current cursor position. This is necessary to make text selection work. (Text
     * selection uses the actual mouse position, so it can't be faked by sending Windows messages.)
     *
     * @param clientX Cursor X position within the WebViewWindow.
     * @param clientY Cursor Y position within the WebViewWindow.
     */
    void MoveWindowToCursor(int clientX, int clientY);

    /**
     * Get the component identifier string for the element at a point.
     * Possible returns values are the same as for IHTMLElement2::componentFromPoint.
     * (See http://msdn.microsoft.com/en-us/library/aa703978%28VS.85%29.aspx)
     */
    BSTR GetComponentAtPoint(int x, int y) const;

    /**
     * Apply the background color to the WebBrowser document.
     *
     * @return true if the color was applied, or false if it could not be applied.
     */
    HRESULT ApplyBackgroundColor();

    /**
     * Determine if the current content contains embedded objects, such as Flash video.
     * Such objects are embedded using the HTML "embed" tag.
     *
     * @return true if the current page contains embedded objects.
     */
    BOOL ContainsEmbeddedContent() const;

    /** Enable or disable scroll bars depending on the size of the content. */
    void DetermineScrollBars();

    /** Determine the size of the scrollable content. */
    HRESULT DetermineContentSize();

    /** Determine the size of the scrollable content in compatibility mode (quirks mode). */
    HRESULT DetermineContentSizeCompatibilityMode(IHTMLDocument2 *pDoc);

    /** Determine the size of the scrollable content in standards mode (strict mode). */
    HRESULT DetermineContentSizeStandardsMode(IHTMLDocument2 *pDoc);

    /** Determine the URL of the content currently loaded in the WebView. */
    void DetermineContentURL();

    /**
     * Determine the URL for a link. If the WebView has a resource resolver then the resolver is invoked
     * to determine the URL. If there is no resource resolver, this method returns the link's absolute
     * URL, or the relative URL if the base URL is the default (about:blank). This method avoids
     * exposing the WebView's internal marker URL (webview://) to application code.
     *
     * @param anchor Anchor for which to determine URL.
     * 
     * @return The URL or NULL if the URL cannot be determined. This string must be freed with SysFreeString.
     */
    BSTR GetLinkUrl(IHTMLAnchorElement *anchor) const;

    /**
     * Track mouse drag events on a scroll bar thumb and scroll the page appropriately.
     *
     * @param x Mouse coordinate x.
     * @param y Mouse coordinate y.
     */
    void TrackScrollThumb(int x, int y);

    /**
     * Scroll the WebView because the mouse is pressed on a scroll arrow. How the WebView
     * scrolls depends on the active scrollElement.
     */
    void AutoScroll();

    /** Remove all entries from the brower's back/forward navigation list (TravelLog). */
    void ClearTravelLog();

protected: // Link finding methods

    /**
     * Find all the links in the currently loaded content, and build the list of links
     * and bounds.
     */
    HRESULT FindLinks();

    /**
     * Get the parameters for an HTML anchor element.
     *
     * @param anchor anchor for which to build a parameters bundle.
     * @param viewport Visible portion of the page, used to clip link rectangles.
     *
     * @return a new LinkParams bundle. This object must be freed with the delete operator
     *         when the caller has finished with it.
     */
    LinkParams *GetLinkParams(IHTMLAnchorElement *anchor, RECT *viewport) const;

    /**
     * Determine if an HTML element is visible. The element is considered visible if it has a size
     * and does not have a CSS style that would hide it.
     */
    BOOL IsVisible(IHTMLElement *element) const;

    /**
     * Add rectangles for text links to a LinkParams bundle.
     *
     * @param linkParams Bundle to receive rectangles.
     * @param rect       Link rectangles.
     * @param viewport   Visible area of page to clip link rectangles against.
     */
    void AddLineBoxRects(LinkParams *linkParams, IHTMLRectCollection *rects, RECT *viewport) const;

    /**
     * Add rectangles for image links to a LinkParams bundle. This method searches the tags under an anchor element, and adds
     * a link rectangle for each visible image that it finds.
     *
     * @param linkParams  Bundle to receive rectangles.
     * @param element2    The HTML anchor element.
     * @param anchorStyle CSS style for the anchor element.
     * @param linkRect    Link bounding rectangle.
     * @param viewport    Visible area of page to clip link rectangles against.
     */
    void AddImageRects(LinkParams *linkParams, IHTMLElement2 *element2, IHTMLCurrentStyle *anchorStyle, RECT *linkRect, RECT *viewport) const;

    /**
     * Compute the intersection of two rectangles.
     *
     * @param intersection Receives the intersection of r1 and r2.
     * @param r1 First rectangle.
     * @param r2 Second rectangle.
     */
    void ComputeRectIntersect(RECT *intersection, const RECT *r1, const RECT *r2) const;

    /**
     * Compute the union of two rectangles.
     *
     * @param unionRect Receives the union of r1 and r2.
     * @param r1 First rectangle.
     * @param r2 Second rectangle.
     */
    void ComputeRectUnion(RECT *unionRect, const RECT *r1, const RECT *r2) const;

    /**
     * Determine if a rectangle is empty.
     *
     * @param rect Rectangle to test.
     *
     * @return true if the rectangle is empty (width and height <= 0).
     */
    BOOL IsEmptyRect(const RECT *rect) const;

    /**
     * Determine if two rectangles intersect.
     *
     * @param r1 First rect.
     * @param r2 Second rect.
     *
     * @return true if the rectangles intersect.
     */
    BOOL RectsIntersect(const RECT *r1, const RECT *r2) const;

protected:
    // Message handlers

    /** Handler for WM_SIZE message. */
    LRESULT OnFrameSizeChanged(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);

    /** Handler for WM_TIMER message. */
    LRESULT OnTimer(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);

    /** Handler for WM_WEBVIEW_ACTIVATE message. */
    LRESULT OnActivate(UINT uMsg, WPARAM active, LPARAM unused, BOOL bHandled);

    /** Send a simulated input message to the web browser. */
    LRESULT OnSimulateInput(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);

    /** Handler for WM_SET_HTML message. */
    LRESULT OnSetHTML(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);

    /** Handler for WM_GO_BACK message. */
    LRESULT OnGoBack(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);
    /** Handler for WM_GO_FORWARD message. */
    LRESULT OnGoForward(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);
    /** Handler for WM_WEBVIEW_SET_BACKGROUND_COLOR message. */
    LRESULT OnSetBackgroundColor(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);

    /** Handler for WM_WEBVIEW_SET_RESOURCE_RESOLVER message. */
    LRESULT OnSetResourceResolver(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);
    /** Handler for WM_WEBVIEW_SET_ADVISE message. */
    LRESULT OnSetAdvise(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);

    /** Handler for WM_WEBVIEW_SET_MIN_CONTENT_SIZE message. */
    LRESULT OnSetMinContentSize(UINT uMsg, WPARAM width, LPARAM height, BOOL bHandled);

    /** Handler for WM_WEBVIEW_CAPTURE message. */
	LRESULT OnCapture(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL bHandled);

private: // Debugging support
    
    /** Save the current bitmap capture to a file. */
    void WriteBitmapToFile(wchar_t *fileName) const;

    /** Write the contents of the TravelLog to the debugger console. */
    void DumpTravelLog() const;
};

#endif
