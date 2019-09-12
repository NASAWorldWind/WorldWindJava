/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.render.*;

import java.awt.geom.*;
import java.util.*;

/**
 * A simple clutter filter that compares bounding rectangles to each other.
 *
 * @author tag
 * @version $Id: BasicClutterFilter.java 726 2012-08-29 03:16:03Z tgaskins $
 */
public class BasicClutterFilter implements ClutterFilter
{
    protected List<Rectangle2D> rectList = new ArrayList<Rectangle2D>();

    public void apply(DrawContext dc, List<Declutterable> shapes)
    {
        for (Declutterable shape : shapes)
        {
            Rectangle2D bounds = shape.getBounds(dc);
            if (bounds == null)
                continue;

            // Check for an intersecting region. If none, then add the incoming region to the region list. Subsequent
            // regions will be checked for intersection with it.
            Rectangle2D intersectingRegion = this.intersects(bounds);
            if (intersectingRegion == null)
            {
                dc.addOrderedRenderable(shape);
                this.rectList.add(bounds);
            }
        }

        this.clear();
    }

    protected void clear()
    {
        this.rectList.clear();
    }

    /**
     * Indicates whether a specified region intersects a region in the filter.
     *
     * @param rectangle the region to test.
     *
     * @return true if the region intersects one or more other regions in the filter, otherwise false.
     */
    protected Rectangle2D intersects(Rectangle2D rectangle)
    {
        if (rectangle == null)
            return null;

        // Performs a simple linear search. This is a performance bottleneck for very large lists.
        for (Rectangle2D rect : this.rectList)
        {
            if (rectangle.intersects(rect))
                return rect;
        }

        return null;
    }
}
