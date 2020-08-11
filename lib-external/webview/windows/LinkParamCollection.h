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

#ifndef LINK_PARAM_COLLECTION_H
#define LINK_PARAM_COLLECTION_H

#include "stdafx.h"
#include "LinkParams.h"
#include <vector>

/**
 * Object to hold a collection of LinkParam.
 */
class LinkParamCollection : public IUnknown
{
public:
    /** Create a new collection. */
    LinkParamCollection() : refCount(1)
    {}

    /** Destroy the collection. */
    virtual ~LinkParamCollection()
    {
        // Free the LinkParams objects stored in the list.
        for (UINT i = 0; i < params.size(); i++)
        {
            delete params[i];
        }
    }

    /**
     * Add link parameters to the list.
     *
     * @param param New link parameters. The container assumes ownership of this reference,
     *        and will delete it when the container is destroyed.
     */
    void Add(LinkParams *param)
    {
        params.push_back(param);
    }

    /**
     * Get the link parameters in the collection.
     *
     * @return reference to vector of parameters. This vector will be destroyed
     *         when the LinkParamCollection is destroyed.
     */
    std::vector<LinkParams*>& GetParams()
    {
        return params;
    }

protected:

    int refCount;
    std::vector<LinkParams*> params;

public: // IUnknown
    STDMETHODIMP QueryInterface(REFIID riid, void **ppvObject)
    {
        *ppvObject = NULL;
        return E_NOINTERFACE;
    }

    ULONG STDMETHODCALLTYPE AddRef()
    {
        return this->refCount++;
    }

    ULONG STDMETHODCALLTYPE Release()
    {
        this->refCount--;

        if (this->refCount == 0)
        {
            delete this;
            return 0;
        }

        return this->refCount;
    }
};

#endif