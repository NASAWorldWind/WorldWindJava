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
