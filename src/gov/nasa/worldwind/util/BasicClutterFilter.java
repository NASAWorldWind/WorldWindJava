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
