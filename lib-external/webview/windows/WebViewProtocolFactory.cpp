/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Version: $Id: WebViewProtocolFactory.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include "WebViewProtocolFactory.h"
#include "WebViewProtocol.h"
#include "util/Logging.h"

static long serverLocks = 0;

///////////////////////////////////////////////
// IClassFactory
///////////////////////////////////////////////

HRESULT STDMETHODCALLTYPE WebViewProtocolFactory::CreateInstance(IUnknown *pUnkOuter, REFIID riid, void **ppvObject)
{
    if (pUnkOuter != NULL)
        return CLASS_E_NOAGGREGATION;

    WebViewProtocol *protocol = new WebViewProtocol();
    if (protocol == NULL)
        return E_OUTOFMEMORY;

    HRESULT hr = protocol->QueryInterface(riid, (void**) ppvObject);

    protocol->Release();
    return hr;
}
        
HRESULT STDMETHODCALLTYPE WebViewProtocolFactory::LockServer(BOOL fLock)
{
    if (fLock)
        InterlockedIncrement(&serverLocks);
    else
        InterlockedDecrement(&serverLocks);

    return S_OK;
}

///////////////////////////////////////////////
// IUnknown
///////////////////////////////////////////////

STDMETHODIMP WebViewProtocolFactory::QueryInterface(REFIID riid, void **ppvObject)
{
    *ppvObject = NULL;

    if (riid == IID_IUnknown)
        *ppvObject = static_cast<IUnknown*>(this);
    else if (riid == IID_IClassFactory)
        *ppvObject = static_cast<IClassFactory*>(this);

    if (*ppvObject)
    {
        static_cast<IUnknown*>(*ppvObject)->AddRef();
        return S_OK;
    }
    return E_NOINTERFACE;
}

ULONG STDMETHODCALLTYPE WebViewProtocolFactory::AddRef()
{
    return this->refCount++;
}

ULONG STDMETHODCALLTYPE WebViewProtocolFactory::Release()
{
    this->refCount--;

    if (this->refCount == 0)
    {
        delete this;
        return 0;
    }

    return this->refCount;
}
