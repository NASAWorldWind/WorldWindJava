#include "stdafx.h"
#include "WebViewWindow.h"
#include "util/Logging.h"

#include <set>

const wchar_t *WEB_VIEW_WINDOW_LIST_KEY = L"gov.nasa.worldwind.webview.WebViewWindowList";

const wchar_t *MESSAGE_ONLY_WINDOW_CLASS = L"MessageOnlyWindow";

WebViewWindow* NewWebViewWindow(HWND messageWnd)
{
    if (messageWnd == NULL)
        return NULL;

    // Send a message to the message handling window telling it to create a new web view. SendMessage blocks until
    // the window has processed the message and returned the handle to the new web view window.
    LRESULT lr = SendMessage(reinterpret_cast<HWND>(messageWnd), WM_WEBVIEW_CREATE, 0, 0);

	return reinterpret_cast<WebViewWindow*>(lr);
}

HWND NewMessageLoop()
{
    // The webview windows need to be created on the message loop thread. Here we create a message-only window that will create web
    // views when we ask it to.
    return CreateWindowEx(0, MESSAGE_ONLY_WINDOW_CLASS, L"WebView Message Window", 0, 0, 0, 0, 0, HWND_MESSAGE, NULL, NULL, NULL);
}

void ReleaseMessageLoop(HWND messageWnd)
{
    if (messageWnd == NULL)
        return; // Nothing to release

    // Destroy the message processing window, which will end the message handling loop
    PostMessage(reinterpret_cast<HWND>(messageWnd), WM_DESTROY, 0, 0);
}

void ReleaseWebView(WebViewWindow *webViewWnd)
{
    if (webViewWnd == NULL)
        return; // Nothing to release

    // Post a message to the message handling window telling it to destroy the WebViewWindow.
    PostMessage(webViewWnd->GetControlWindow(), WM_WEBVIEW_DESTROY, reinterpret_cast<WPARAM>(webViewWnd->m_hWnd), 0);
}

/**
 * WinProc for the message-only window that exists for each message loop. This window is responsible for creating
 * and destroying instances of WebViewWindow.
 */
LRESULT CALLBACK MessageWndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    // List of all WebView windows owned by this thread

    switch (message)
    {
    case WM_CREATE:
        {
            // Create a set to hold the WebViews associated with this message window.
            // Attach the set to the window properties so that we can retrieve it later.
            std::set<WebViewWindow*> *webViewWindows = new std::set<WebViewWindow*>();
            SetProp(hWnd, WEB_VIEW_WINDOW_LIST_KEY, webViewWindows);
        }
        break;

    // Create a WebView
    case WM_WEBVIEW_CREATE:
        {
            CComObject<WebViewWindow> *webViewWnd = NULL;
            HRESULT hr = CComObject<WebViewWindow>::CreateInstance(&webViewWnd);

            webViewWnd->SetControlWindow(hWnd);

            if (FAILED(hr))
            {
                Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
                return NULL;
            }

            hr = webViewWnd->CreateWebBrowser();
            if (SUCCEEDED(hr))
            {
                std::set<WebViewWindow*> *webViewWindows = reinterpret_cast<std::set<WebViewWindow*>*>(GetProp(hWnd, WEB_VIEW_WINDOW_LIST_KEY));
                if (webViewWindows != NULL)
                    webViewWindows->insert(webViewWnd);

                return reinterpret_cast<LRESULT>(webViewWnd);
            }
            else
            {
                Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
                webViewWnd->Release();
                return NULL;
            }
        }
        return ERROR_SUCCESS;

    // Destroy a WebView
    case WM_WEBVIEW_DESTROY:
        {
            std::set<WebViewWindow*> *webViewWindows = reinterpret_cast<std::set<WebViewWindow*>*>(GetProp(hWnd, WEB_VIEW_WINDOW_LIST_KEY));

            if (webViewWindows != NULL)
            {
                WebViewWindow *webViewWnd = WebViewWindow::FindWebView(wParam);
                if (webViewWnd != NULL)
                {
                    webViewWnd->SendMessage(WM_CLOSE, 0, 0);

                    webViewWindows->erase(webViewWnd);
                    webViewWnd->Release();
                }
            }
        }
        return ERROR_SUCCESS;

    // Give all WebViews a chance to update
    case WM_WEBVIEW_UPDATE:
        {
            std::set<WebViewWindow*> *webViewWindows = reinterpret_cast<std::set<WebViewWindow*>*>(GetProp(hWnd, WEB_VIEW_WINDOW_LIST_KEY));

            if (webViewWindows != NULL)
            {
	            std::set<WebViewWindow*>::iterator it;
	            for (it = webViewWindows->begin(); it != webViewWindows->end(); it++)
	            {
		            (*it)->ScheduleCapture();
	            }
            }
        }
        return ERROR_SUCCESS;

    // Destroy the message loop
    case WM_DESTROY:
        {
            std::set<WebViewWindow*> *webViewWindows = reinterpret_cast<std::set<WebViewWindow*>*>(GetProp(hWnd, WEB_VIEW_WINDOW_LIST_KEY));

            if (webViewWindows != NULL)
            {
                // Release all WebView references
	            std::set<WebViewWindow*>::iterator it;
	            for (it = webViewWindows->begin(); it != webViewWindows->end(); it++)
	            {
                    (*it)->SendMessage(WM_CLOSE, 0, 0);
                    (*it)->Release();
	            }

                delete webViewWindows;
            }

            SetProp(hWnd, WEB_VIEW_WINDOW_LIST_KEY, NULL);

            PostQuitMessage(0);
        }
        break;
    }

    return DefWindowProc(hWnd, message, wParam, lParam);
}

/**
 * Get the next message in the queue, by priority. The WM_WEBVIEW_CAPTURE message is treated as low priority to avoid blocking user input
 * handling while the WebView is captured. This is similar to how Windows normally handles the WM_PAINT message.
 */
static BOOL GetMessage(MSG *msg)
{
    BOOL ret;

    // Check for high priority messages
    ret = PeekMessage(msg, NULL, 0, WM_WEBVIEW_HIPRIORITY_LAST, PM_REMOVE);
    if (ret)
        return ret;

    // Check for low priority capture messages
    ret = PeekMessage(msg, NULL, WM_WEBVIEW_CAPTURE, WM_WEBVIEW_CAPTURE, PM_REMOVE);
    if (ret)
        return ret;

    // Queue is empty, wait for any message
    return GetMessage(msg, NULL, 0, 0);
}

/**
 * Run a message loop until a WM_QUIT message is received. The message loop keeps track of which WebViewWindows
 * it owns, and gives the windows a chance to update themselves whenever a message is processed. WM_WEBVIEW_CAPTURE
 * messages are treated as low priority messages (similar to WM_PAINT) to prevent flooding the message queue with capture
 * messages.
 */
HRESULT RunWebViewMessageLoop(HWND messageWnd)
{
    BOOL ret;

    // Start the message pump
    MSG msg;
    while (ret = GetMessage(&msg))
    {
        if (ret == -1) // GetMessage uses a strange notion of boolean in which the valid values are nonzero, zero, and -1
        {
            return E_FAIL;
        }

        if (msg.message == WM_QUIT)
        {
            break;
        }

        TranslateMessage(&msg);
        DispatchMessage(&msg);

        // Synchronously send a message to the message-only control window to give all of the WebViews on this thread a chance to update
        SendMessage(messageWnd, WM_WEBVIEW_UPDATE, 0, 0);
	}

    return S_OK;
}
