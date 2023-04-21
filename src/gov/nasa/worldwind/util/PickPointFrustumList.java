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

import gov.nasa.worldwind.geom.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Jeff Addison
 * @version $Id: PickPointFrustumList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PickPointFrustumList extends ArrayList<PickPointFrustum>
{
    public PickPointFrustumList()
    {
    }

    public PickPointFrustumList(PickPointFrustumList list)
    {
        super(list);
    }

    /**
     * Returns true if the specified point is inside the space enclosed by ALL of the frustums
     *
     * @param point the point to test.
     *
     * @return true if the specified point is inside the space enclosed by ALL the Frustums, and false otherwise.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public final boolean containsInAll(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (!frustum.contains(point))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the specified point is inside the space enclosed by ANY of the frustums
     *
     * @param point the point to test.
     *
     * @return true if the specified point is inside the space enclosed by ANY the Frustums, and false otherwise.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public final boolean containsInAny(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (frustum.contains(point))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the specified 2D screen point is inside the 2D screen rectangle enclosed by ALL of the frustums
     *
     * @param point the point to test.
     *
     * @return true if the specified point is inside the space enclosed by ALL the Frustums, and false otherwise.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public final boolean containsInAll(Point point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (!frustum.contains(point))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the specified 2D point is inside the 2D screen rectangle enclosed by ANY of the frustums
     *
     * @param x the x coordinate to test.
     * @param y the y coordinate to test.
     *
     * @return true if the specified point is inside the space enclosed by ANY the Frustums, and false otherwise.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public final boolean containsInAny(double x, double y)
    {
        for (PickPointFrustum frustum : this)
        {
            if (frustum.contains(x, y))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the specified 2D point is inside the 2D screen rectangle enclosed by ANY of the frustums
     *
     * @param point the point to test.
     *
     * @return true if the specified point is inside the space enclosed by ANY the Frustums, and false otherwise.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public final boolean containsInAny(Point point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (frustum.contains(point))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the specified {@link Extent} intersects the space enclosed by ALL the Frustums. NOTE: Cannot be
     * true if all frustums do not intersect.
     *
     * @param extent the Extent to test.
     *
     * @return true if the specified Extent intersects the space enclosed by ALL Frustums, and false otherwise.
     *
     * @throws IllegalArgumentException if the extent is null.
     */
    public final boolean intersectsAll(Extent extent)
    {
        if (extent == null)
        {
            String msg = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (!frustum.intersects(extent))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the specified {@link Extent} intersects the space enclosed by ANY of the Frustums.
     *
     * @param extent the Extent to test.
     *
     * @return true if the specified Extent intersects the space enclosed by ANY of the Frustums, and false otherwise.
     *
     * @throws IllegalArgumentException if the extent is null.
     */
    public final boolean intersectsAny(Extent extent)
    {
        if (extent == null)
        {
            String msg = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (frustum.intersects(extent))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if a specified line segment intersects the space enclosed by ANY of the Frustums.
     *
     * @param pa one end of the segment.
     * @param pb the other end of the segment.
     *
     * @return true if the specified segment intersects the space enclosed by ANY of the Frustums, otherwise false.
     *
     * @throws IllegalArgumentException if either point is null.
     */
    public final boolean intersectsAny(Vec4 pa, Vec4 pb)
    {
        for (PickPointFrustum frustum : this)
        {
            if (frustum.intersectsSegment(pa, pb))
                return true;
        }

        return false;
    }

    /**
     * Returns true if the specified {@link java.awt.Rectangle} intersects the 2D screen space enclosed by ALL the
     * Frustums. NOTE: Cannot be true if all frustums do not intersect.
     *
     * @param rect the Rectangle to test.
     *
     * @return true if the specified Rectangle intersects the 2D screen space enclosed by ALL Frustums, and false
     *         otherwise.
     *
     * @throws IllegalArgumentException if the extent is null.
     */
    public final boolean intersectsAll(Rectangle rect)
    {
        if (rect == null)
        {
            String msg = Logging.getMessage("nullValue.RectangleIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (!frustum.intersects(rect))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the specified {@link java.awt.Rectangle} intersects the 2D screen space enclosed by ANY of the
     * Frustums.
     *
     * @param rect the Rectangle to test.
     *
     * @return true if the specified Rectangle intersects the 2D screen space enclosed by ANY of the Frustums, and false
     *         otherwise.
     *
     * @throws IllegalArgumentException if the extent is null.
     */
    public final boolean intersectsAny(Rectangle rect)
    {
        if (rect == null)
        {
            String msg = Logging.getMessage("nullValue.RectangleIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        for (PickPointFrustum frustum : this)
        {
            if (frustum.intersects(rect))
            {
                return true;
            }
        }

        return false;
    }
}