/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

/**
 * Version: $Id: WebViewProtocol.h 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"

#ifndef WEB_VIEW_PROTOCOL_H
#define WEB_VIEW_PROTOCOL_H

/** CLSID for the WebView protocol. */
extern const GUID CLSID_WebViewProtocol;

/**
 * Pluggable protocol for the WebView protocol. This protocol parses urls that use the "webview" scheme,
 * and interprets relative references against a "webview" base URL. Local resources in the "webview" scheme
 * are resolved by a ResourceLocatorAdapter, which connects to the Java process and gives the application a
 * chance to resolve the reference.
 *
 * The format of a URL in the WebView scheme is webview://[webViewId]/[path]
 * [webViewId] is an WebView identifier. A WebView ID identifies a single instance of WebView in the running process.
 *             This ID is not persistent; it applies only for the duration of the application execution.
 * [path] is a path to a local resource. The WebView protocol handler treats this string as an opaque identifier,
 *        and does not attempt to parse it. This string may contain delimiters of its own, and will be interpreted by
 *        the ResourceLocator. The path string may contain . and .. characters. The WebView protocol handlers does
 *        not attempt to simplify the path heirarchy by collapsing these sections. That is, a URL such as
 *        "webview://12345/../../path/to/resource" will NOT by simplified. The .. characters may be signifigant to
 *        the ResourceLocator.
 */
class WebViewProtocol :
    public IInternetProtocolInfo
{
public:
    WebViewProtocol()
        : refCount(1)
    {
    }

private:
    virtual ~WebViewProtocol() { } // Object is reference counted, do not allow others to delete it

    WebViewProtocol(WebViewProtocol& other) { }

public:

    ///////////////////////////////////////////////
    // IInternetProtocolInfo
    ///////////////////////////////////////////////

    HRESULT STDMETHODCALLTYPE ParseUrl(LPCWSTR pwzUrl, PARSEACTION ParseAction, DWORD dwParseFlags,
        LPWSTR pwzResult, DWORD cchResult, DWORD *pcchResult, DWORD dwReserved);
        
    HRESULT STDMETHODCALLTYPE CombineUrl(LPCWSTR pwzBaseUrl, LPCWSTR pwzRelativeUrl,
        DWORD dwCombineFlags, LPWSTR pwzResult, DWORD cchResult, DWORD *pcchResult,
        DWORD dwReserved);
        
    HRESULT STDMETHODCALLTYPE CompareUrl(LPCWSTR pwzUrl1, LPCWSTR pwzUrl2, DWORD dwCompareFlags)
    {
        return INET_E_DEFAULT_ACTION;
    }
        
    HRESULT STDMETHODCALLTYPE QueryInfo(LPCWSTR pwzUrl, QUERYOPTION queryOption, DWORD dwQueryFlags,
        LPVOID pBuffer, DWORD cbBuffer, DWORD *pcbBuf, DWORD dwReserved)
    {
        return E_NOTIMPL;
    }
    
    ///////////////////////////////////////////////
    // IUnknown
    ///////////////////////////////////////////////

    STDMETHODIMP QueryInterface(REFIID riid, void **ppvObject);

    ULONG STDMETHODCALLTYPE AddRef();

    ULONG STDMETHODCALLTYPE Release();

protected:
    int refCount;

protected:

    /**
     * Parse a WebView URL. If parsing succeeds, the {@code webViewId} and {@code path} fields will
     * be populated with portions of the parsed URL.
     *
     * @param url URL to parse.
     * @param webViewId Pointer to address that will receive the WebView identifier.
     * @param path Pointer to char* that be set to the beginning of the path portion of the URL.
     *             This function does NOT allocate a new string. The pointer returned is always set
     *             to a position in the {@code url} string, or set to NULL if parsing fails.
     * 
     * @return S_OK if parsing succeeds, otherwise an error code.
     */
    HRESULT ParseWebViewUrl(const wchar_t *url, LONG_PTR *webViewId, wchar_t **path);
};

#endif
