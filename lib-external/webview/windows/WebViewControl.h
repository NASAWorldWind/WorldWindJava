#include "stdafx.h"
#include "WebViewWindow.h"
#include "util/Logging.h"

/** Window class of the message-only window that is used to manage instances of WebViewWindow. */
extern const wchar_t *MESSAGE_ONLY_WINDOW_CLASS;

/**
 * Create a message loop to manage WebViews.
 *
 * @return HWND of the message-only WebView control window for the new loop.
 */
HWND NewMessageLoop();

/**
 * Terminate a message loop.
 *
 * @param HWND of the loop's message-only control window.
 */
void ReleaseMessageLoop(HWND messageWnd);

/**
 * Create a WebViewWindow.
 *
 * @param messageWnd The message-only control window that will
 *        manage the new WebView.
 *
 * @return New WebViewWindow
 */
WebViewWindow* NewWebViewWindow(HWND messageWnd);

/**
 * Destroy a WebView.
 *
 * @param webViewWnd WebViewWindow to destroy.
 */
void ReleaseWebView(WebViewWindow *webViewWnd);

/**
 * Run a message loop until a WM_QUIT message is received. The message loop keeps track of which WebViewWindows
 * it owns, and gives the windows a chance to update themselves whenever a message is processed. WM_WEBVIEW_CAPTURE
 * messages are treated as low priority messages (similar to WM_PAINT) to prevent flooding the message queue with capture
 * messages.
 *
 * The function must be called from the thread that will run the message loop.
 */
HRESULT RunWebViewMessageLoop(HWND messageWnd);
