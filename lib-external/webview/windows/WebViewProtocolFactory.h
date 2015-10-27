/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * Version: $Id: WebViewProtocolFactory.h 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"

#ifndef WEB_VIEW_PROTOCOL_FACTORY_H
#define WEB_VIEW_PROTOCOL_FACTORY_H

/**
 * Class factory to create instances of the WebView protocol handler.
 */
class WebViewProtocolFactory : public IClassFactory
{
protected:
    virtual ~WebViewProtocolFactory() { } // Object is reference counted

public:
    WebViewProtocolFactory() : refCount(1) { }

public:

    ///////////////////////////////////////////////
    // IClassFactory
    ///////////////////////////////////////////////

    HRESULT STDMETHODCALLTYPE CreateInstance(IUnknown *pUnkOuter, REFIID riid, void **ppvObject);

    HRESULT STDMETHODCALLTYPE LockServer(BOOL fLock);

    ///////////////////////////////////////////////
    // IUnknown
    ///////////////////////////////////////////////

    STDMETHODIMP QueryInterface(REFIID riid, void **ppvObject);

    ULONG STDMETHODCALLTYPE AddRef();

    ULONG STDMETHODCALLTYPE Release();

protected:
    int refCount;
};

#endif
