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
