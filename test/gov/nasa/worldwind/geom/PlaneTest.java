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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class PlaneTest
{
    @Test
    public void testSegmentIntersection()
    {
        Plane p = new Plane(new Vec4(0, 0, -1, 0));

        Vec4 pt = p.intersect(Vec4.ZERO, new Vec4(0, 0, -1));
        assertNotNull("Perpendicular, 0 at origin, not null", pt);
        assertTrue("Perpendicular, 0 at origin, should produce intersection at origin", pt.equals(Vec4.ZERO));

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
        assertNotNull("Line segment is in fact a point, located on the plane, not null", pt);
        assertTrue("Line segment is in fact a point, located on the plane, should produce intersection at (1, 0, 0)",
            pt.equals(new Vec4(1, 0, 0)));

        pt = p.intersect(new Vec4(0, 0, -1), new Vec4(0, 0, -1));
        assertNull("Line segment is in fact a point not on the plane, should produce null for no intersection", pt);

        pt = p.intersect(new Vec4(0, 0, 1), new Vec4(0, 0, -1));
        assertNotNull("Perpendicular, integer end points off origin, not null", pt);
        assertTrue("Perpendicular, integer end points off origin, should produce intersection at origin",
            pt.equals(Vec4.ZERO));

        pt = p.intersect(new Vec4(0, 0, 0.5), new Vec4(0, 0, -0.5));
        assertNotNull("Perpendicular, non-integer end points off origin, not null", pt);
        assertTrue("Perpendicular, non-integer end points off origin, should produce intersection at origin",
            pt.equals(Vec4.ZERO));

        pt = p.intersect(new Vec4(0.5, 0.5, 0.5), new Vec4(-0.5, -0.5, -0.5));
        assertNotNull("Not perpendicular, non-integer end points off origin, not null", pt);
        assertTrue("Not perpendicular, non-integer end points off origin, should produce intersection at origin",
            pt.equals(Vec4.ZERO));

        pt = p.intersect(new Vec4(1, 0, 0), new Vec4(2, 0, 0));
        assertNotNull("Parallel, in plane, not null", pt);
        assertTrue("Parallel, in plane, should produce intersection at origin",
            pt.equals(Vec4.INFINITY));

        pt = p.intersect(new Vec4(1, 0, 1), new Vec4(2, 0, 1));
        assertNull("Parallel, integer end points off origin, should produce null for no intersection", pt);
    }

    @Test
    public void testLineIntersection()
    {
        Plane p = new Plane(new Vec4(0, 0, 1, 0));

        Vec4 pt = p.intersect(new Line(new Vec4(807066.3082512334, 4864661.747666055, 4.5E7, 1.0),
            new Vec4(0.0, 0.0, -1.0, 0.0)));
        assertNotNull("Simple intersection", pt);
    }
}
