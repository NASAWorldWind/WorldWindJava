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

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import gov.nasa.worldwind.geom.Vec4;

@RunWith(JUnit4.class)
public class WWMathTest {

    private static final double DELTA = 1e-9;

    /**
     * Test triangle normal computation
     */
    @Test
    public void testParseTimeString() {
        Vec4 v1 = new Vec4(26, 2, 1);
        Vec4 v2 = new Vec4(26, 2, 13);
        Vec4 v3 = new Vec4(12, -23, 13);
        Vec4 expectedNormal = new Vec4(0.8725060159497201, -0.48860336893184325, 0.0);
        Vec4 normal = WWMath.computeTriangleNormal(v1, v2, v3);
        assertEquals("Normal computation 1 X", expectedNormal.x, normal.x, DELTA);
        assertEquals("Normal computation 1 Y", expectedNormal.y, normal.y, DELTA);
        assertEquals("Normal computation 1 Z", expectedNormal.z, normal.z, DELTA);

        v1 = new Vec4(-12, 12, 26);
        v2 = new Vec4(23, -23, 2);
        v3 = new Vec4(13, 13, 13);
        expectedNormal = new Vec4(0.4612242682795252, -0.1396190373706287, 0.8762298207398077);
        normal = WWMath.computeTriangleNormal(v1, v2, v3);
        assertEquals("Normal computation 2 X", expectedNormal.x, normal.x, DELTA);
        assertEquals("Normal computation 2 Y", expectedNormal.y, normal.y, DELTA);
        assertEquals("Normal computation 2 Z", expectedNormal.z, normal.z, DELTA);
    }
}
