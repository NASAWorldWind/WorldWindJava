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
