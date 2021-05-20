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
public class LineTest
{
    @Test
    public void testFrustumClipping()
    {
        Frustum f = new Frustum(); // default frustum is [-1,1] in all 3 dimensions
        Vec4 pa, pb;
        Vec4[] clipped;

        pa = Vec4.ZERO;
        pb = new Vec4(1, 0, 0);
        clipped = Line.clipToFrustum(pa, pb, f);
        assertTrue("Trivial accept A", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

        pa = Vec4.ZERO;
        pb = new Vec4(0.5, 0, 0);
        clipped = Line.clipToFrustum(pa, pb, f);
        assertTrue("Trivial accept B", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

        pa = Vec4.ZERO;
        pb = new Vec4(-0.5, 0, 0);
        clipped = Line.clipToFrustum(pa, pb, f);
        assertTrue("Trivial accept C", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

        pa = new Vec4(.5, .5, 2);
        pb = new Vec4(.5, .5, -2);
        clipped = Line.clipToFrustum(pa, pb, f);
        assertTrue("Clipped at near and far",
            clipped != null && clipped[0].equals(new Vec4(.5, .5, 1)) && clipped[1].equals(new Vec4(.5, .5, -1)));

        pa = new Vec4(.5, .5, .5);
        pb = new Vec4(.5, .5, -.5);
        clipped = Line.clipToFrustum(pa, pb, f);
        assertTrue("Trivial accept D", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

        pa = new Vec4(2, 2, 2);
        pb = new Vec4(4, 4, 4);
        clipped = Line.clipToFrustum(pa, pb, f);
        assertTrue("Segment not in frustum", clipped == null);
    }

    @Test
    public void testDistanceToPoint()
    {
        Vec4 p0 = Vec4.ZERO;
        Vec4 p1 = new Vec4(2, 0, 0);

        double d;
        Vec4 p;
        p = new Vec4(0, 0, 0);
        d = Line.distanceToSegment(p0, p1, p);
        assertEquals("Point at start of line at origin", d, 0d, 0.0);

        p = new Vec4(-2, 0, 0);
        d = Line.distanceToSegment(p0, p1, p);
        assertEquals("Point to left of segment on x axis", d, 2d, 0.0);

        p = new Vec4(3, 0, 0);
        d = Line.distanceToSegment(p0, p1, p);
        assertEquals("Point to right of segment on x axis", d, 1d, 0.0);

        p = new Vec4(2, 7, 0);
        d = Line.distanceToSegment(p0, p1, p);
        assertEquals("Point 7 above segment end point", d, 7d, 0.0);

        p = new Vec4(1, 7, 0);
        d = Line.distanceToSegment(p0, p1, p);
        assertEquals("Point 7 above segment mid-point", d, 7d, 0.0);

        p = new Vec4(1, -7, 0);
        d = Line.distanceToSegment(p0, p1, p);
        assertEquals("Point 7 below segment mid-point", d, 7d, 0.0);

        p = new Vec4(1, 0.5, 0);
        d = Line.distanceToSegment(p0, p1, p);
        assertEquals("Point 1/2 above segment 1/4-point", d, 0.5, 0.0);
    }
}
