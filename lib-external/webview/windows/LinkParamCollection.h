/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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