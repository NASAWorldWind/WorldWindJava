/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

#include "stdafx.h"

#ifndef HTML_MONIKER_H
#define HTML_MONIKER_H

// The SHCreateMemStream function has existed since Windows 2000, but was only added to the header files
// in Windows Vista. To use this function in Windows 2000 and Windows XP, we need to explicitly load it
// from the DLL. SHCreateMemStream creates a COM stream object from an array of bytes in local memory.
// For more information see: http://msdn.microsoft.com/en-us/library/bb773831%28VS.85%29.aspx

typedef IStream* (__stdcall *fnSHCreateMemStream)(const BYTE *pInit, UINT cbInit);

class HTMLMoniker : public IMoniker
{
private:
    HTMLMoniker();

public:
    /** Create a new instance of the HTMLMoniker. */
	static HRESULT CreateInstance(HTMLMoniker **pMoniker)
	{
        HRESULT hr = E_OUTOFMEMORY;
		*pMoniker = new HTMLMoniker();
        if (*pMoniker != NULL)
        {
            (*pMoniker)->AddRef();
            hr = S_OK;
        }

        return hr;
    }

    /** Destroy the moniker. */
    virtual ~HTMLMoniker();

    /**
     * Set the HTML content from a buffer in memory.
     *
     * @param buffer Buffer containing HTML data. The data can be encoded in any
     *        encoding that the web browser control can parse.
     * @param bufferSize Size of the buffer in bytes.
     *
     * @return HRESULT indicating success or failure.
     */
    HRESULT SetHTML(BYTE *buffer, UINT bufferSize);

    /**
     * Set the content base URL.
     *
     * @param baseUrl New base URL. Does not need to be NULL terminated.
     * @param baseUrlLen String length (in wchars) of the base URL.
     */
    HRESULT SetBaseURL(const wchar_t *baseUrl, size_t baseUrlLen);

    /** Determine if the base URL for this content is the default base URL (about:blank). */
    BOOL IsDefaultBaseUrl() const;

public:
    
    // IMoniker
    STDMETHODIMP BindToStorage(IBindCtx *pbc, IMoniker *pmkToLeft, REFIID riid, void **ppvObj);
    STDMETHODIMP GetDisplayName(IBindCtx *pbc, IMoniker *pmkToLeft, LPOLESTR *ppszDisplayName);

    STDMETHODIMP BindToObject(IBindCtx *pbc, IMoniker *pmkToLeft, REFIID riidResult, void **ppvResult)
    { return E_NOTIMPL; }

    STDMETHODIMP Reduce(IBindCtx *pbc, DWORD dwReduceHowFar, IMoniker **ppmkToLeft, IMoniker **ppmkReduced)    
    { return E_NOTIMPL; }
    
    STDMETHODIMP ComposeWith(IMoniker *pmkRight, BOOL fOnlyIfNotGeneric, IMoniker **ppmkComposite)
    { return E_NOTIMPL; }
    
    STDMETHODIMP Enum(BOOL fForward, IEnumMoniker **ppenumMoniker)
    { return E_NOTIMPL; }
    
    STDMETHODIMP IsEqual(IMoniker *pmkOtherMoniker)
    { return E_NOTIMPL; }

    STDMETHODIMP Hash(DWORD *pdwHash)
    { return E_NOTIMPL; }

    STDMETHODIMP IsRunning(IBindCtx *pbc, IMoniker *pmkToLeft, IMoniker *pmkNewlyRunning)
    { return E_NOTIMPL; }
    
    STDMETHODIMP GetTimeOfLastChange(IBindCtx *pbc, IMoniker *pmkToLeft, FILETIME *pFileTime)
    { return E_NOTIMPL; }
    
    STDMETHODIMP Inverse(IMoniker **ppmk)
    { return E_NOTIMPL; }
    
    STDMETHODIMP CommonPrefixWith(IMoniker *pmkOther, IMoniker **ppmkPrefix)
    { return E_NOTIMPL; }
    
    STDMETHODIMP RelativePathTo(IMoniker *pmkOther, IMoniker **ppmkRelPath)
    { return E_NOTIMPL; }
        
    STDMETHODIMP ParseDisplayName(IBindCtx *pbc, IMoniker *pmkToLeft,LPOLESTR pszDisplayName,
        ULONG *pchEaten, IMoniker **ppmkOut)
    { return E_NOTIMPL; }
    
    STDMETHODIMP IsSystemMoniker(DWORD *pdwMksys)
    {
        if (!pdwMksys)
            return E_POINTER;

        *pdwMksys = MKSYS_NONE;
        return S_OK;
    }

	// IPersistStream methods
	STDMETHODIMP Save(IStream *pStm, BOOL fClearDirty)  { return E_NOTIMPL; }
	STDMETHODIMP IsDirty() { return E_NOTIMPL; }
	STDMETHODIMP Load(IStream *pStm) { return E_NOTIMPL; }
	STDMETHODIMP GetSizeMax(ULARGE_INTEGER *pcbSize) { return E_NOTIMPL; }

	// IPersist
	STDMETHODIMP GetClassID(CLSID *pClassID) { return E_NOTIMPL; }

    // IUnknown
    STDMETHODIMP QueryInterface(REFIID riid, void **ppvObject);
    virtual ULONG STDMETHODCALLTYPE AddRef(void);
    virtual ULONG STDMETHODCALLTYPE Release(void);

private:
    BYTE *htmlBuffer;
    wchar_t *baseUrl;
    IStream *htmlStream;

    int refCount;

    HMODULE libShlWapi;
    fnSHCreateMemStream SHCreateMemStream;
};

#endif