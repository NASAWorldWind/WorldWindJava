/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
