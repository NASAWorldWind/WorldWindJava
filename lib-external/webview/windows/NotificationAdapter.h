/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
 * Version $Id: NotificationAdapter.h 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include <jni.h>

#ifndef NOTIFICATION_ADAPTER_H
#define NOTIFICATION_ADAPTER_H

class NotificationAdapter
    : public IAdviseSink
{
protected:
    NotificationAdapter(JNIEnv *env, jobject jobj);

    virtual ~NotificationAdapter(); // Object is reference counted, don't allow anyone else to destroy it.

public:
    /** Create a new instance of the PropertyChangeBridge. */
	static HRESULT CreateInstance(JNIEnv *env, jobject jobj, NotificationAdapter **pAdapter);

public:
    // IAdviseSink
    virtual void STDMETHODCALLTYPE OnDataChange(FORMATETC *pFormatetc, STGMEDIUM *pStgmed) { };
    virtual void STDMETHODCALLTYPE OnViewChange(DWORD dwAspect, LONG lindex);
    virtual void STDMETHODCALLTYPE OnRename(IMoniker *pmk) { }
    virtual void STDMETHODCALLTYPE OnSave(void) { }
    virtual void STDMETHODCALLTYPE OnClose(void) { }

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