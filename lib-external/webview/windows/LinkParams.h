/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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