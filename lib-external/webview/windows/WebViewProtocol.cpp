/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Version: $Id: WebViewProtocol.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include "WebViewProtocol.h"
#include "WebViewWindow.h"
#include "WebResourceResolver.h"
#include "util/Logging.h"

#include <wininet.h>

// {6D406BC3-97DD-49F9-9E37-DA6A78A6173B}
static const GUID CLSID_WebViewProtocol = { 0x6d406bc3, 0x97dd, 0x49f9, { 0x9e, 0x37, 0xda, 0x6a, 0x78, 0xa6, 0x17, 0x3b } };

///////////////////////////////////////////////
// IInternetProtocolInfo
///////////////////////////////////////////////

HRESULT STDMETHODCALLTYPE WebViewProtocol::ParseUrl(LPCWSTR pwzUrl, PARSEACTION ParseAction,
    DWORD dwParseFlags, LPWSTR pwzResult, DWORD cchResult, DWORD *pcchResult, DWORD dwReserved)
{
    if (pwzUrl == NULL || pwzResult == NULL || pcchResult == NULL)
        return E_POINTER;

    BOOL foundResource = FALSE;

    // Parse URL to find window ID
    LONG_PTR webViewId = 0;
    wchar_t *path = NULL;
    this->ParseWebViewUrl(pwzUrl, &webViewId, &path);    

    // If the path is empty there is nothing to resolve. Just use the default behavior.
    if (path == NULL || wcslen(path) == 0)
        return INET_E_DEFAULT_ACTION;

    // Attempt to find the WebView window to get the appropriate ResourceResolver
    WebViewWindow *webViewWnd = WebViewWindow::FindWebView(webViewId);
    if (webViewWnd != NULL)
    {
        CComPtr<WebResourceResolver> resolver;
        webViewWnd->GetResourceResolver(&resolver);

        // Invoke the resource resolver to resolve the resource
        if (resolver != NULL)
        {
            HRESULT hr = resolver->resolve(path, pwzResult, &cchResult);
            if (SUCCEEDED(hr))
            {
                foundResource = TRUE;
                *pcchResult = static_cast<DWORD>(wcslen(pwzResult) + 1);
            }
            else if (hr == S_FALSE) // Buffer too small
            {
                return S_FALSE;
            }
        }
    }

    // If the URL was not translated by the ResourceResolver translate it to the about: scheme and let
    // the web browser handle it as if the base URL was "about:blank".
    if (!foundResource)
    {
        size_t len = wcslen(L"about:") + wcslen(path) + 1;
        if (len > cchResult)
            return S_FALSE; // Buffer too small

        swprintf_s(pwzResult, cchResult, L"about:%s", path);
        *pcchResult = static_cast<DWORD>(len);
    }

    return S_OK;
}

HRESULT STDMETHODCALLTYPE WebViewProtocol::CombineUrl(LPCWSTR pwzBaseUrl, LPCWSTR pwzRelativeUrl,
    DWORD dwCombineFlags, LPWSTR pwzResult, DWORD cchResult, DWORD *pcchResult,
    DWORD dwReserved)
{
    if (pwzBaseUrl == NULL || pwzRelativeUrl == NULL || pwzResult == NULL || pcchResult == NULL)
        return E_POINTER;

    *pcchResult = cchResult;
    BOOL ret = InternetCombineUrl(pwzBaseUrl, pwzRelativeUrl, pwzResult, pcchResult, ICU_NO_ENCODE | ICU_NO_META);
    if (!ret)
    {
        DWORD err = GetLastError();
        if (err == ERROR_INSUFFICIENT_BUFFER)
            return S_FALSE; // Buffer too small
        else
            return E_FAIL;
    }

    return S_OK;
}

HRESULT WebViewProtocol::ParseWebViewUrl(const wchar_t *url, LONG_PTR *webViewId, wchar_t **path)
{
    *webViewId = 0;
    *path = NULL;

    // Parse out the numeric web view ID
    swscanf_s(url, L"webview://%d/", webViewId);

    if (*webViewId != 0)
    {
        // Find the // authority section marker. If the string ends with //, authDelim
        // points to the null terminator
        wchar_t *authDelim = wcsstr(const_cast<wchar_t*>(url), L"//") + 1;

        if (authDelim != NULL)
        {
            // Find the first slash after the authority section.  If the string ends with /,
            /// path points to the null terminator
            *path = wcschr(authDelim + wcslen(L"//"), '/') + 1;
        }
    }

    return S_OK;
}

///////////////////////////////////////////////
// IUnknown
///////////////////////////////////////////////

STDMETHODIMP WebViewProtocol::QueryInterface(REFIID riid, void **ppvObject)
{
    *ppvObject = NULL;

    if (riid == IID_IUnknown)
	    *ppvObject = static_cast<IUnknown*>(this);
    else if (riid == IID_IInternetProtocolInfo)
        *ppvObject = static_cast<IInternetProtocolInfo*>(this);

    if (*ppvObject)
    {
	    static_cast<IUnknown*>(*ppvObject)->AddRef();
	    return S_OK;
    }

    return E_NOINTERFACE;
}

ULONG STDMETHODCALLTYPE WebViewProtocol::AddRef()
{
    return this->refCount++;
}

ULONG STDMETHODCALLTYPE WebViewProtocol::Release()
{
    this->refCount--;

    if (this->refCount == 0)
    {
        delete this;
        return 0;
    }

    return this->refCount;
}
