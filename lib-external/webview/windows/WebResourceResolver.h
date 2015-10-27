/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
 * Version $Id: WebResourceResolver.h 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include <jni.h>

#ifndef WEB_RESOURCE_RESOLVER_ADAPTER_H
#define WEB_RESOURCE_RESOLVER_ADAPTER_H

/**
 * Adapter to provide a JNI bridge to a gov.nasa.worldwind.util.webview.WebResourceResolver.
 */
class WebResourceResolver : public IUnknown
{
public:
    WebResourceResolver(JNIEnv *env, jobject jobj);

protected:
    virtual ~WebResourceResolver(); // Object is reference counted, don't allow anyone else to destroy it.

public:

    /**
     * Resolve a reference.
     *
     * @param address Local address to resolve.
     * @param result Buffer to receive result.
     * @param chResult Size of result buffer, in wchars.
     *
     * @return S_OK or error code. A return value of S_OK does not necessarily imply that the reference was resolved,
     *         only that no error occurred. S_FALSE indicates that the result buffer is too small to hold the resolved
     *         URL. In this case, chResult is set to the required buffer size, in wchars.
     */
    HRESULT resolve(const wchar_t *address, wchar_t *result, DWORD *chResult) const;

public:
    // IUnknown
    STDMETHODIMP QueryInterface(REFIID riid, void **ppvObject);
    virtual ULONG STDMETHODCALLTYPE AddRef(void);
    virtual ULONG STDMETHODCALLTYPE Release(void);

protected:

    int refCount;
    jobject jObject;
    JavaVM *javaVM;

};

#endif
