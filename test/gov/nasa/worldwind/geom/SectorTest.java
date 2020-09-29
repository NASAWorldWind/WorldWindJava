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

import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SectorTest {

    @Test
    public void test_intersectsSegmentIllegalArguments() {
        Sector s = Sector.fromDegrees(0, 0, 0, 0);
        try {
            s.intersectsSegment(null, LatLon.ZERO);
            fail("Should raise an IllegalArgumentException");
        } catch (Exception e) {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
        try {
            s.intersectsSegment(LatLon.ZERO, null);
            fail("Should raise an IllegalArgumentException");
        } catch (Exception e) {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void test_intersectsSegment() {
        Sector s = Sector.fromDegrees(44, 45, -96, -95);
        LatLon begin = LatLon.fromDegrees(44.5, -97);
        LatLon end = LatLon.fromDegrees(44.5, -94);
        boolean result = s.intersectsSegment(begin, end);
        assertTrue("Should intersect", result);

        begin = LatLon.fromDegrees(46, -97);
        end = LatLon.fromDegrees(43, -94);
        result = s.intersectsSegment(begin, end);
        assertTrue("Should intersect", result);

        begin = LatLon.fromDegrees(43, -97);
        end = LatLon.fromDegrees(43, -94);
        result = s.intersectsSegment(begin, end);
        assertFalse("Should not intersect", result);
    }

    @Test
    public void test_intersectsPathIllegalArguments() {
        Sector s = Sector.fromDegrees(0, 0, 0, 0);
        try {
            s.intersectsPath(null);
            fail("Should raise an IllegalArgumentException");
        } catch (Exception e) {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void test_intersectsPath() {
        Sector s = Sector.fromDegrees(44, 45, -96, -95);

        ArrayList<LatLon> path = new ArrayList<>();
        path.add(LatLon.fromDegrees(44.5, -97));
        path.add(LatLon.fromDegrees(44.5, -94));
        path.add(LatLon.fromDegrees(43, -97));
        boolean result = s.intersectsPath(path);
        assertTrue("Should intersect", result);

        path = new ArrayList<>();
        path.add(LatLon.fromDegrees(46, -97));
        path.add(LatLon.fromDegrees(43, -97));
        path.add(LatLon.fromDegrees(43, -94));
        path.add(LatLon.fromDegrees(46, -94));
        result = s.intersectsPath(path);
        assertFalse("Shouldn't intersect", result);

    }
}
