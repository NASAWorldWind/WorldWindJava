/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
 * Version $Id: WindowsWebViewJNI.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include "WebViewControl.h"
#include "WindowsWebViewJNI.h"
#include "WebViewWindow.h"
#include "HTMLMoniker.h"
#include "AWTEventSupport.h"
#include "NotificationAdapter.h"
#include "LinkParams.h"
#include "LinkParamCollection.h"
#include "util/Logging.h"
#include "util/JNIUtil.h"

#include <GL/gl.h>    

/* AVKey class, member, and method info global variables. The first parameter specifies the global variable name. */
static jclass AVKey;
static jfieldID AVKey_ALLOW;
static jfieldID AVKey_IGNORE;
static jfieldID AVKey_MIME_TYPE;
static jfieldID AVKey_TARGET;
static jfieldID AVKey_URL;
static jfieldID AVKey_BOUNDS;
static jfieldID AVKey_RECTANGLES;

/* AVList class, member, and method info global variables. The first parameter specifies the global variable name. */
static jclass AVList;
static jmethodID AVList_setValue;

/* AVListImpl class, member, and method info global variables. */
static jclass AVListImpl;
static jmethodID AVListImpl_init;

static jclass String;
static jmethodID String_getBytesCharset;

/* Rectangle and Dimension class, member, and method info global variables. */
static jclass AWTRectangle;
static jmethodID AWTRectangle_init;
static jclass AWTDimension;
static jmethodID AWTDimension_init;

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_initialize
  (JNIEnv *env, jclass jclz)
{
    AVKey = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("gov/nasa/worldwind/avlist/AVKey")));
    AVKey_MIME_TYPE = env->GetStaticFieldID(AVKey, "MIME_TYPE", "Ljava/lang/String;");
    AVKey_TARGET = env->GetStaticFieldID(AVKey, "TARGET", "Ljava/lang/String;");
    AVKey_URL = env->GetStaticFieldID(AVKey, "URL", "Ljava/lang/String;");
    AVKey_BOUNDS = env->GetStaticFieldID(AVKey, "BOUNDS", "Ljava/lang/String;");
    AVKey_RECTANGLES = env->GetStaticFieldID(AVKey, "RECTANGLES", "Ljava/lang/String;");

    AVList = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("gov/nasa/worldwind/avlist/AVList")));
    AVList_setValue = env->GetMethodID(AVList, "setValue", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;");

    AVListImpl = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("gov/nasa/worldwind/avlist/AVListImpl")));
    AVListImpl_init = env->GetMethodID(AVListImpl, "<init>", "()V");

    String = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/String")));
    String_getBytesCharset = env->GetMethodID(String, "getBytes", "(Ljava/lang/String;)[B");

    AWTRectangle = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("java/awt/Rectangle")));
    AWTRectangle_init = env->GetMethodID(AWTRectangle, "<init>", "(IIII)V");

    AWTDimension = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass("java/awt/Dimension")));
    AWTDimension_init = env->GetMethodID(AWTDimension, "<init>", "(II)V");

    Logging::initialize(env);
    AWTEvent_Initialize(env);
}

/**
 * Convert a Java String to an array of bytes in UTF-16 Little Endian encoding, with a Byte Order Marker.
 * Based on example in "The Java Native Interface Programmers Guide and Specification" 8.2.2.
 */
BOOL JNU_GetStringCharsUTF16(JNIEnv *env, jstring jstr, char **dest, jint *size)
{
    jbyteArray bytes = 0;
    jthrowable exc;
    if (env->EnsureLocalCapacity(2) < 0)
    {
        JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
        return FALSE;
    }

    jstring encoding = (jstring)env->NewStringUTF("UTF-16LE");
    bytes = reinterpret_cast<jbyteArray>(env->CallObjectMethod(jstr, String_getBytesCharset, encoding));
    exc = env->ExceptionOccurred();
    if (!exc)
    {
        jint len = env->GetArrayLength(bytes);
        len += 2; // Add 2 bytes for the Byte Order Marker
        *size = len;
        *dest = (char*) malloc(len + 1);
        if (dest == NULL)
        {
            JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
            env->DeleteLocalRef(bytes);
            return FALSE;
        }

        // Write the Byte Order Marker to the first two bytes of the array
        (*dest)[0] = (char) 0xFF;
        (*dest)[1] = (char) 0xFE;

        env->GetByteArrayRegion(bytes, 0, len - 2, reinterpret_cast<jbyte*>(*dest) + 2);
        (*dest)[len] = '\0'; // Null terminator
    }
    else
    {
        env->DeleteLocalRef(exc);
    }
    env->DeleteLocalRef(bytes);

    return TRUE;
}

///////////////////////////////////////////////////////////////////////
// Functions to manage WebView life cycle.
///////////////////////////////////////////////////////////////////////

JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_newWebViewWindow
  (JNIEnv *env, jclass jclz, jlong messageWnd)
{
    if (messageWnd == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return 0;
    }

    WebViewWindow* newWindow = NewWebViewWindow(reinterpret_cast<HWND>(messageWnd));
    return reinterpret_cast<jlong>(newWindow);
}

JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_newMessageLoop
  (JNIEnv *env, jclass jclz)
{
    HWND messageWnd = NewMessageLoop();
    if (messageWnd == NULL)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
    }

    return reinterpret_cast<jlong>(messageWnd);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_releaseMessageLoop
  (JNIEnv *env, jclass jclz, jlong messageWnd)
{
    if (messageWnd == NULL)
        return; // Nothing to release

    // Destroy the message processing window, which will end the message handling loop
    ReleaseMessageLoop(reinterpret_cast<HWND>(messageWnd));
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_releaseWebView
  (JNIEnv *env, jclass jclz, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
        return; // Nothing to release

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    ReleaseWebView(webViewWnd);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_releaseComObject
  (JNIEnv *env, jclass jclz, jlong unknownPtr)
{
    if (unknownPtr == NULL)
        return; // Nothing to release
    
    IUnknown *unkwn = reinterpret_cast<IUnknown*>(unknownPtr);
    unkwn->Release();
}

/**
 * Run a message loop until a WM_QUIT message is received. The message loop keeps track of which WebViewWindows
 * it owns, and gives the windows a chance to update themselves whenever a message is processed. WM_WEBVIEW_CAPTURE
 * messages are treated as low priority messages (similar to WM_PAINT) to prevent flooding the message queue with capture
 * messages.
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_runMessageLoop
  (JNIEnv *env, jclass jclz, jlong messageWnd)
{
    HRESULT hr = RunWebViewMessageLoop(reinterpret_cast<HWND>(messageWnd));

    if (FAILED(hr))
    {
        JNU_ThrowByName(env, WW_RUNTIME_EXCEPTION, L"NativeLib.ErrorInNativeLib", GetLastError());
    }
} 

//////////////////////////////////////////////////
// Functions to load content into WebView.
//////////////////////////////////////////////////

void SetHTMLString(JNIEnv *env, WebViewWindow *webViewWnd, jstring html, const wchar_t *baseUrl, jsize baseUrlLen)
{
    char *htmlStr = NULL;
    jint htmlLen = 0;

    if (html != NULL)
    {
        // HTML content is loaded into the browser as a byte array. We need to convert the Java string into a byte array using
        // an encoding that the web browser will be able to figure out. UTF-16 Little Endian with a Byte Order Marker works well
        // for this. The BOM hints to the WebBrowser that the content is UTF-16, and causes it to ignore a Content-Type tag
        // in the HTML content, which may no longer reflect the actual encoding.
        JNU_GetStringCharsUTF16(env, html, &htmlStr, &htmlLen);
    }
    else
    {
        htmlStr = "";
        htmlLen = 0;
    }

    // Create an HTMLMoniker to create a stream from the HTML data. The moniker is capable of creating
    // a content stream from the HTML buffer and feeding it to MSHTML.
    HTMLMoniker *pHtmlMoniker;
    HRESULT hr = HTMLMoniker::CreateInstance(&pHtmlMoniker);
    if (FAILED(hr))
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", hr);
        return;
    }

    pHtmlMoniker->SetHTML(reinterpret_cast<BYTE*>(htmlStr), htmlLen);
    pHtmlMoniker->SetBaseURL(reinterpret_cast<const wchar_t*>(baseUrl), baseUrlLen);

    // Send the WebView window a message to tell it update. We pass the HTMLMoniker as the LPARAM.
    // The WebViewWindow is responsible for releasing this resource when it is done with it.
    PostMessage(webViewWnd->m_hWnd, WM_SET_HTML, NULL, reinterpret_cast<LPARAM>(pHtmlMoniker));
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setHTMLString
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jstring html, jstring baseUrl)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    const jchar *baseUrlStr = NULL;
    jsize baseStrLen = 0;

    if (baseUrl != NULL)
    {
        baseUrlStr = env->GetStringChars(baseUrl, NULL);
        baseStrLen = env->GetStringLength(baseUrl);
    }
    else
    {        
        baseUrlStr = reinterpret_cast<const jchar*>(DEFAULT_BASE_URL);
        baseStrLen = static_cast<jsize>(wcslen(DEFAULT_BASE_URL));
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    // Clear the WebView resource locator because new content is being set.
    webViewWnd->PostMessage(WM_WEBVIEW_SET_RESOURCE_RESOLVER, 0, NULL);

    SetHTMLString(env, webViewWnd, html, reinterpret_cast<const wchar_t*>(baseUrlStr), baseStrLen);

    if (baseUrl != NULL)
        env->ReleaseStringChars(baseUrl, baseUrlStr);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setHTMLStringWithResourceResolver
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jstring html, jobject resourceResolver)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    // Send the WebView window a message to tell it update. We pass the HTMLMoniker as the LPARAM.
    // The WebViewWindow is responsible for releasing this resource when it is done with it.
    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    // Create a base URL that will tell the WebView to resolve local references against the resource locator.
    const int MAX_BASE_URL_LEN = 255;
    wchar_t baseUrl[MAX_BASE_URL_LEN + 1];
    ZeroMemory(baseUrl, (MAX_BASE_URL_LEN + 1) * sizeof(wchar_t));

    // Set the resource locator
    if (resourceResolver != NULL)
    {
        WebResourceResolver *pAdapter = new WebResourceResolver(env, resourceResolver);
        if (pAdapter == NULL)
        {
            Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", E_OUTOFMEMORY);
            return;
        }

        // Set the resource resolver. The WebView will take ownership of the reference.
        webViewWnd->PostMessage(WM_WEBVIEW_SET_RESOURCE_RESOLVER, 0, reinterpret_cast<LPARAM>(pAdapter));

        // Create the base URL to reference the resource resolver
        LONG_PTR webViewId = webViewWnd->GetWebViewId();
        _snwprintf_s(baseUrl, MAX_BASE_URL_LEN, MAX_BASE_URL_LEN, L"webview://%u/", webViewId);
    }

    const jchar *htmlStr = NULL;
    jsize htmlStrLen = 0;
    if (html != NULL)
    {
        htmlStr = env->GetStringChars(html, NULL);
        htmlStrLen = env->GetStringLength(html);
    }

    SetHTMLString(env, webViewWnd, html, baseUrl, static_cast<jsize>(wcslen(baseUrl)));
}

/////////////////////////////////////////////////////
// Functions to set properties of WebView
/////////////////////////////////////////////////////

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setActive
  (JNIEnv *env, jclass jclz, jlong webViewWindowPtr, jboolean active)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    PostMessage(webViewWnd->m_hWnd, WM_WEBVIEW_ACTIVATE, active, NULL);
}

JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_newNotificationAdapter(JNIEnv *env,
    jclass clazz, jobject listener)
{
    if (listener == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.ListenerIsNull");
        return 0;
    }

    NotificationAdapter *pAdapter = NULL;
    HRESULT hr = NotificationAdapter::CreateInstance(env, listener, &pAdapter);
    if (FAILED(hr))
    {
        Logging::logger()->warning(L"NativeLib.ErrorInNativeLib", GetLastError());
        return NULL;
    }

    return reinterpret_cast<jlong>(pAdapter);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_addWindowUpdateObserver(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr, jlong observerPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.ListenerIsNull");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    NotificationAdapter *notificationAdapter = reinterpret_cast<NotificationAdapter*>(observerPtr);

    IAdviseSink* sink;
    notificationAdapter->QueryInterface(IID_IAdviseSink, (void**)&sink);

    // WebView will take ownership of our reference to the advise sink, so do not release it here
    webViewWnd->PostMessage(WM_WEBVIEW_SET_ADVISE, 0, reinterpret_cast<LPARAM>(sink));
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_removeWindowUpdateObserver(JNIEnv *env,
    jclass clazz, jlong webViewWindowPtr, jlong observerPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    if (observerPtr == NULL)
        return;

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    NotificationAdapter *notificationAdapter = reinterpret_cast<NotificationAdapter*>(observerPtr);

    webViewWnd->PostMessage(WM_WEBVIEW_SET_ADVISE, 0, NULL);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setBackgroundColor
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jstring colorStr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }
    if (colorStr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.ColorIsNull");
        return;
    }

    const jchar *jColor = env->GetStringChars(colorStr, NULL);
    jsize colorStrLen = env->GetStringLength(colorStr);

    // Copy the characters to a null terminated string. The Java string is not guaranteed to be null terminated.
    wchar_t *colorBuffer = new wchar_t[colorStrLen + 1];
    ZeroMemory(colorBuffer, (colorStrLen + 1) * sizeof(wchar_t));
    wcsncpy_s(colorBuffer, colorStrLen + 1, reinterpret_cast<const wchar_t*>(jColor), colorStrLen);

    // Send the WebView window a message to tell set the color. We pass the color string as the LPARAM.
    // The WebViewWindow is responsible for releasing this resource when it is done with it.
    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    SendMessage(webViewWnd->m_hWnd, WM_WEBVIEW_SET_BACKGROUND_COLOR, NULL, reinterpret_cast<LPARAM>(colorBuffer));

    env->ReleaseStringChars(colorStr, jColor);
    delete colorBuffer;
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setFrameSize
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jint width, jint height)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }
    if (width < 0)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"generic.InvalidWidth");
        return;
    }
    if (height < 0)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"generic.InvalidHeight");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    // Get the current position of the window. We want to change the width and height, but maintain the position.
    RECT rect;
    GetWindowRect(webViewWnd->m_hWnd, &rect);

    int x = rect.left;
    int y = rect.top;

    MoveWindow(webViewWnd->m_hWnd, x, y, width, height, TRUE);            
}

//////////////////////////////////
// Input handling
//////////////////////////////////

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_sendEvent
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jobject event)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    PostMsgFromAWTEvent(env, event, webViewWnd->m_hWnd);
}

//////////////////////////////////////////////////
// Functions to get information from the WebView.
//////////////////////////////////////////////////

/**
 * Create a java.awt.Rectangle from a RECT. The rect is interpreted in GL coordinates, with
 * the origin at the lower left corner of the screen. The new Java object will take its x and
 * y coordinates from the bottom left corner of the RECT.
 */
jobject CreateJavaRect(JNIEnv *env, RECT *rect)
{
    long top, left, bottom, right;

    top = rect->top;
    bottom = rect->bottom;
    left = rect->left;
    right = rect->right;
    
    long width = abs(right - left);
    long height = abs(bottom - top);

    jobject jRect = env->NewObject(AWTRectangle, AWTRectangle_init, left, bottom, width, height);

    return jRect;
}

JNIEXPORT jobjectArray JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getLinks
  (JNIEnv *env, jobject, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return NULL;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    CComPtr<LinkParamCollection> linkCollection;
    webViewWnd->GetLinks(&linkCollection);

    if (linkCollection == NULL)
        return NULL;

    std::vector<LinkParams*>& linkParamVector = linkCollection->GetParams();

    jobjectArray jLinkArray = env->NewObjectArray(static_cast<jsize>(linkParamVector.size()), AVList, NULL);

    for (UINT i = 0; i < linkParamVector.size(); ++i)
    {
        LinkParams *linkParams = linkParamVector.at(i);

        // Create Java strings for the URL, mime type, and navigation target
        jstring jUrl = env->NewString(reinterpret_cast<jchar*>(linkParams->url), SysStringLen(linkParams->url));

        jstring navTarget = NULL;
        if (linkParams->target != NULL)
        {
            navTarget = env->NewString(reinterpret_cast<jchar*>(linkParams->target), SysStringLen(linkParams->target));
        }

        jstring jMimeType = NULL;
        if (linkParams->type != NULL)
        {
            jMimeType = env->NewString(reinterpret_cast<jchar*>(linkParams->type), SysStringLen(linkParams->type));
        }

        jobject paramsAVList = env->NewObject(AVListImpl, AVListImpl_init);
        env->CallObjectMethod(paramsAVList, AVList_setValue, env->GetStaticObjectField(AVKey, AVKey_URL), jUrl);
        env->CallObjectMethod(paramsAVList, AVList_setValue, env->GetStaticObjectField(AVKey, AVKey_TARGET), navTarget);
        env->CallObjectMethod(paramsAVList, AVList_setValue, env->GetStaticObjectField(AVKey, AVKey_MIME_TYPE), jMimeType);

        jobject jRect = CreateJavaRect(env, &(linkParams->bounds));
        env->CallObjectMethod(paramsAVList, AVList_setValue, env->GetStaticObjectField(AVKey, AVKey_BOUNDS), jRect);

        const std::vector<RECT> &rectList = linkParams->GetRects();        

        jobjectArray jrectArray = env->NewObjectArray(static_cast<jsize>(rectList.size()), env->FindClass("java/awt/Rectangle"), NULL);

        for (UINT j = 0; j < rectList.size(); j++)
        {
            RECT r = rectList.at(j);

            jobject jr = CreateJavaRect(env, &r);
            env->SetObjectArrayElement(jrectArray, j, jr);
        }
        env->CallObjectMethod(paramsAVList, AVList_setValue, env->GetStaticObjectField(AVKey, AVKey_RECTANGLES), jrectArray);

        env->SetObjectArrayElement(jLinkArray, i, paramsAVList);
    }

    return jLinkArray;
}

JNIEXPORT jlong JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getUpdateTime
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return 0;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    return webViewWnd->GetUpdateTime();
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_loadDisplayInGLTexture
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jint target)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    webViewWnd->CaptureToGLTexture(static_cast<GLenum>(target));
}

JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getContentSize
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return 0;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    int scrollWidth;
    int scrollHeight;

    webViewWnd->GetContentSize(&scrollWidth, &scrollHeight);

    jobject jDim = env->NewObject(AWTDimension, AWTDimension_init, scrollWidth, scrollHeight);
    return jDim;
}

JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getMinContentSize
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return 0;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    int width;
    int height;

    webViewWnd->GetMinContentSize(&width, &height);

    jobject jDim = env->NewObject(AWTDimension, AWTDimension_init, width, height);
    return jDim;
}

/*
 * Class:     gov_nasa_worldwind_util_webview_WindowsWebViewJNI
 * Method:    setMinContentSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_setMinContentSize
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr, jint width, jint height)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }
    if (width < 0)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"generic.InvalidWidth");
        return;
    }
    if (height < 0)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"generic.InvalidHeight");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);
    webViewWnd->PostMessage(WM_WEBVIEW_SET_MIN_CONTENT_SIZE, width, height);
}


JNIEXPORT jobject JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_getContentURL
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return 0;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    BSTR url = webViewWnd->GetContentURL();
    if (url != NULL)
    {
        jstring urlStr = env->NewString(reinterpret_cast<const jchar*>(url), SysStringLen(url));
        SysFreeString(url);

        return urlStr;
    }
    else
    {
        return NULL;
    }
}

///////////////////////////////////////////////////
// Navigation functions.
///////////////////////////////////////////////////

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_goBack
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    PostMessage(webViewWnd->m_hWnd, WM_GO_BACK, NULL, NULL);
}

JNIEXPORT void JNICALL Java_gov_nasa_worldwind_util_webview_WindowsWebViewJNI_goForward
  (JNIEnv *env, jobject jobj, jlong webViewWindowPtr)
{
    if (webViewWindowPtr == NULL)
    {
        JNU_ThrowByName(env, ILLEGAL_ARGUMENT_EXCEPTION, L"nullValue.WebViewIsNull");
        return;
    }

    WebViewWindow *webViewWnd = reinterpret_cast<WebViewWindow*>(webViewWindowPtr);

    PostMessage(webViewWnd->m_hWnd, WM_GO_FORWARD, NULL, NULL);
}
