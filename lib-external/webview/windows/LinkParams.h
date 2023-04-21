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

#ifndef LINK_PARAMS_H
#define LINK_PARAMS_H

#include <vector>

/**
 * Simple value object to hold parameters for a link on a web page. The link is defined
 * by a URL, mime type, and target, and also a bounding box and a list of rectangles.
 * The bounding box encloses the total extent of the link in the viewport, and the rectangles
 * are the individual pieces of the link (for example, if the link text wraps to two lines there
 * will be two rectangles, and the bounding box will enclose both).
 * 
 * The link strings set in this object are freed when the object is destroyed.
 */
class LinkParams
{
public:
    /** Create a params object. */
    LinkParams()
        : url(NULL), type(NULL), target(NULL)
    {
        SetRect(&bounds, 0, 0, 0, 0);
    }

    /** Destroy the params object. */
    virtual ~LinkParams()
    {
        if (url)
            SysFreeString(url);

        if (target)
            SysFreeString(target);

        if (type)
            SysFreeString(type);
    }

    /**
     * Set the bounds of the link.
     *
     * @param r Rectangle that defines the link bounds.
     */
    void SetBounds(RECT *r)
    {
        SetRect(&bounds, r->left, r->top, r->right, r->bottom);
    }

    /**
     * Add a rectangle to the list of rectangles that make up the link.
     *
     * @param r New rectangle.
     */
    void AddLinkRect(RECT *r)
    {
        rectangles.push_back(*r);
    }

    /**
     * Indicates if the link has any rectangles.
     *
     * @return true if the link has at least one rectangle defined.
     */
    BOOL HasLinkRects() const
    {
        return !rectangles.empty();
    }

    /**
     * Get the rectangles for this link.
     *
     * @return pointer to a vector of rectangles. This vector will be
     *         destroyed when the LinkParams object is destroyed.
     */
    std::vector<RECT>& GetRects()
    {
        return rectangles;
    }

public:
    /** Link URL. */
    BSTR url;
    /** Link MIME type. */     
    BSTR type;
    /** Link target. */
    BSTR target;

    /** Bounding box for the link. */
    RECT bounds;
    /** Rectangles that make up the link. */
    std::vector<RECT> rectangles;
};

#endif