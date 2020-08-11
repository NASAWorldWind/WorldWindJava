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
 * Version: $Id: HTMLMoniker.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include "HTMLMoniker.h"
#include "util/Logging.h"

extern const wchar_t *DEFAULT_BASE_URL;

const wchar_t *SHLWAPI_DLL = L"shlwapi.dll";

// Ordinal of the SHCreateMemStream function in ShlWapi.dll
#define INDEX_OF_SHCreateMemStream 12

HTMLMoniker::HTMLMoniker()
    : refCount(0),
      baseUrl(NULL),
      htmlBuffer(NULL),
      htmlStream(NULL),
      libShlWapi(NULL),
      SHCreateMemStream(NULL)
{
    // Load the SHCreateMemStream function from ShWapi.dll.
    libShlWapi = LoadLibrary(SHLWAPI_DLL);  
    if (this->libShlWapi != NULL)  
    {  
        this->SHCreateMemStream = (fnSHCreateMemStream) GetProcAddress(this->libShlWapi, (LPCSTR) INDEX_OF_SHCreateMemStream);
        if (this->SHCreateMemStream == NULL)  
        {
            Logging::logger()->severe(L"NativeLib.LibraryNotAvailable", L"shlwapi.dll:SHCreateMemStream");
            assert(FALSE && "Failed to load SHCreateMemStream from shawapi.dll");
        }
    }
    else
    {
        Logging::logger()->severe(L"NativeLib.LibraryNotAvailable", SHLWAPI_DLL);
        assert(FALSE && "Failed to load shawapi.dll");
    }
}

HTMLMoniker::~HTMLMoniker()
{
    if (htmlStream)
        htmlStream->Release();

    if (htmlBuffer)
        free(htmlBuffer);

    if (baseUrl)
        free(baseUrl);

    // Release our reference to ShWapi.dll. FreeLibrary decrements the reference count on the loaded library,
    // so this will not interfere with any other code in our process that is using the library.
    FreeLibrary(libShlWapi);
}

HRESULT HTMLMoniker::SetHTML(BYTE *buffer, UINT bufferSize)
{
    if (htmlBuffer)
        free(htmlBuffer);

    this->htmlBuffer = buffer;

    if (this->htmlStream)
        this->htmlStream->Release();

    if (this->SHCreateMemStream)
        this->htmlStream = SHCreateMemStream(buffer, bufferSize);
    else
        Logging::logger()->severe(L"NativeLib.LibraryNotAvailable", L"shlwapi.dll:SHCreateMemStream");

    return S_OK;
}

HRESULT HTMLMoniker::SetBaseURL(const wchar_t *baseUrl, size_t baseUrlLen)
{
    if (this->baseUrl)
        free(this->baseUrl);

    this->baseUrl = (wchar_t*)calloc(baseUrlLen + 1, sizeof(wchar_t));
    wcsncat_s(this->baseUrl, baseUrlLen + 1, reinterpret_cast<const wchar_t*>(baseUrl), baseUrlLen);

    return S_OK;
}

BOOL HTMLMoniker::IsDefaultBaseUrl() const
{
    return (wcscmp(this->baseUrl, DEFAULT_BASE_URL) == 0);
}

////////////////////////////////////////
// IMoniker
////////////////////////////////////////

/**
 * Get a stream of the HTML content.
 */
STDMETHODIMP HTMLMoniker::BindToStorage(IBindCtx *pbc, IMoniker *pmkToLeft, REFIID riid, void **ppvObj)
{
    // Reset the stream cursor to the beginning of the stream
    LARGE_INTEGER seek = {0};
    this->htmlStream->Seek(seek, STREAM_SEEK_SET, NULL);

    return this->htmlStream->QueryInterface(riid, ppvObj);
}

/**
 * Get the moniker display name. We return our base URL. MSHTML will use this as this value as the base URL
 * for resolving relative links.
 */
STDMETHODIMP HTMLMoniker::GetDisplayName(IBindCtx *pbc, IMoniker *pmkToLeft, LPOLESTR *ppszDisplayName)
{
    if (!ppszDisplayName)
        return E_POINTER;
    *ppszDisplayName = NULL;

    size_t baseUrlLen = wcslen(baseUrl);
    LPOLESTR displayName = (LPOLESTR) CoTaskMemAlloc((baseUrlLen + 1) * sizeof(wchar_t));
    *displayName = L'\0';

    if (baseUrlLen > 0)
        wcscpy_s(displayName, baseUrlLen + 1, baseUrl);
    *ppszDisplayName = displayName;

    return S_OK;    
}

////////////////////////////////////////
// IUnknown
////////////////////////////////////////

STDMETHODIMP HTMLMoniker::QueryInterface(REFIID riid, void **ppvObject)
{
    *ppvObject = NULL;

    if (riid == IID_IUnknown)
	    *ppvObject = static_cast<IUnknown*>(this);
    else if (riid == IID_IMoniker)
        *ppvObject = static_cast<IMoniker*>(this);

    if (*ppvObject)
    {
	    ((IUnknown*)*ppvObject)->AddRef();
	    return S_OK;
    }
    else return E_NOINTERFACE;
}

ULONG STDMETHODCALLTYPE HTMLMoniker::AddRef()
{
    return this->refCount++;
}

ULONG STDMETHODCALLTYPE HTMLMoniker::Release()
{
    this->refCount--;

    if (this->refCount == 0)
    {
        delete this;
        return 0;
    }

    return this->refCount;
}
