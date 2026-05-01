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
package gov.nasa.worldwind.geom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class PlaneTest
{
    @Test
    public void testSegmentIntersection()
    {
        Plane p = new Plane(new Vec4(0, 0, -1, 0));

        Vec4 pt = p.intersect(Vec4.ZERO, new Vec4(0, 0, -1));
        assertNotNull(pt, "Perpendicular, 0 at origin, not null");
        assertTrue(pt.equals(Vec4.ZERO), "Perpendicular, 0 at origin, should produce intersection at origin");

        try
        {
            //noinspection UnusedAssignment
            pt = p.intersect(null, new Vec4(0, 0, -1));
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        pt = p.intersect(new Vec4(1, 0, 0), new Vec4(1, 0, 0));
        assertNotNull(pt, "Line segment is in fact a point, located on the plane, not null");
        assertTrue(pt.equals(new Vec4(1, 0, 0)), "Line segment is in fact a point, located on the plane, should produce intersection at (1, 0, 0)");

        pt = p.intersect(new Vec4(0, 0, -1), new Vec4(0, 0, -1));
        assertNull(pt, "Line segment is in fact a point not on the plane, should produce null for no intersection");

        pt = p.intersect(new Vec4(0, 0, 1), new Vec4(0, 0, -1));
        assertNotNull(pt, "Perpendicular, integer end points off origin, not null");
        assertTrue(pt.equals(Vec4.ZERO), "Perpendicular, integer end points off origin, should produce intersection at origin");

        pt = p.intersect(new Vec4(0, 0, 0.5), new Vec4(0, 0, -0.5));
        assertNotNull(pt, "Perpendicular, non-integer end points off origin, not null");
        assertTrue(pt.equals(Vec4.ZERO), "Perpendicular, non-integer end points off origin, should produce intersection at origin");

        pt = p.intersect(new Vec4(0.5, 0.5, 0.5), new Vec4(-0.5, -0.5, -0.5));
        assertNotNull(pt, "Not perpendicular, non-integer end points off origin, not null");
        assertTrue(pt.equals(Vec4.ZERO), "Not perpendicular, non-integer end points off origin, should produce intersection at origin");

        pt = p.intersect(new Vec4(1, 0, 0), new Vec4(2, 0, 0));
        assertNotNull(pt, "Parallel, in plane, not null");
        assertTrue(pt.equals(Vec4.INFINITY), "Parallel, in plane, should produce intersection at origin");

        pt = p.intersect(new Vec4(1, 0, 1), new Vec4(2, 0, 1));
        assertNull(pt, "Parallel, integer end points off origin, should produce null for no intersection");
    }

    @Test
    public void testLineIntersection()
    {
        Plane p = new Plane(new Vec4(0, 0, 1, 0));

        Vec4 pt = p.intersect(new Line(new Vec4(807066.3082512334, 4864661.747666055, 4.5E7, 1.0),
            new Vec4(0.0, 0.0, -1.0, 0.0)));
        assertNotNull(pt, "Simple intersection");
    }
}
