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