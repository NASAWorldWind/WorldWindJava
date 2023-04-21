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
 * Version $Id: NotificationAdapter.cpp 1171 2013-02-11 21:45:02Z dcollins $
 */

#include "stdafx.h"
#include "NotificationAdapter.h"
#include "util/Logging.h"

static jclass PropertyChangeListener;
static jmethodID PropertyChangeListener_propertyChange;

void NotificationAdapter_initializeNative(JNIEnv *env)
{
    PropertyChangeListener = (jclass)env->NewGlobalRef(env->FindClass("java/beans/PropertyChangeListener"));
    PropertyChangeListener_propertyChange = env->GetMethodID(PropertyChangeListener, "propertyChange", "(Ljava/beans/PropertyChangeEvent;)V");
}

NotificationAdapter::NotificationAdapter(JNIEnv *env, jobject jobj)
    : refCount(0)
{
    assert(env);
    assert(jobj);

    jObject = reinterpret_cast<jobject>(env->NewGlobalRef(jobj));

    NotificationAdapter_initializeNative(env);

    // Store a pointer to the Java VM so that we can get a JNIEnv reference later
    jint ret = env->GetJavaVM(&javaVM);
    if (ret != JNI_OK)
    {
        Logging::logger()->severe(L"NativeLib.ErrorInNativeLib", GetLastError());
        assert(false && "Failed to get Java VM from JNIEnv");
    }
}

NotificationAdapter::~NotificationAdapter()
{
    JNIEnv *env;
    jint ret = javaVM->AttachCurrentThread((void**)&env, NULL);
    if (ret != JNI_OK)
    {
        Logging::logger()->severe(L"NativeLib.FailedToAttachToVM");
        return;
    }

    env->DeleteGlobalRef(this->jObject);

    javaVM->DetachCurrentThread();
}

HRESULT NotificationAdapter::CreateInstance(JNIEnv *env, jobject jobj, NotificationAdapter **pBridge)
{
    HRESULT hr = E_OUTOFMEMORY;
    *pBridge = new NotificationAdapter(env, jobj);
    if (*pBridge != NULL)
    {
        (*pBridge)->AddRef();
        hr = S_OK;
    }

    return hr;
}

////////////////////////////////////////
// IAdviseSink
////////////////////////////////////////

void STDMETHODCALLTYPE NotificationAdapter::OnViewChange(DWORD dwAspect, LONG lindex)
{
    JNIEnv *env = NULL;

    // Attach to the Java thread
    jint ret = javaVM->AttachCurrentThread((void**)&env, NULL);
    if (ret != JNI_OK)
    {
        Logging::logger()->severe(L"NativeLib.FailedToAttachToVM");
        return;
    }

    // Invoke propertyChange
    env->CallVoidMethod(this->jObject, PropertyChangeListener_propertyChange, NULL);

    javaVM->DetachCurrentThread();    
}

////////////////////////////////////////
// IUnknown
////////////////////////////////////////

STDMETHODIMP NotificationAdapter::QueryInterface(REFIID riid, void **ppvObject)
{
    *ppvObject = NULL;

    if (riid == IID_IUnknown)
	    *ppvObject = reinterpret_cast<void**> (this);
    else if (riid == IID_IAdviseSink)
        *ppvObject = reinterpret_cast<IAdviseSink*>(this);

    if (*ppvObject)
    {
	    ((IUnknown*)*ppvObject)->AddRef();
	    return S_OK;
    }
    else return E_NOINTERFACE;
}

ULONG STDMETHODCALLTYPE NotificationAdapter::AddRef()
{
    return this->refCount++;
}

ULONG STDMETHODCALLTYPE NotificationAdapter::Release()
{
    this->refCount--;

    if (this->refCount == 0)
    {
        delete this;
        return 0;
    }

    return this->refCount;
}
