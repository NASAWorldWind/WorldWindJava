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

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.UnitsFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for MilStd2525UnitsFormat.
 */
@RunWith(JUnit4.class)
public class MilStd2525UnitsFormatTest
{
    @Test
    public void testNE() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(23.751472222222223, 117.4025);

        assertEquals("234505.3N1172409.0E", format.latLon(ll));
    }

    @Test
    public void testSE() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(-12.203364444444444, 5.084722222222222);

        assertEquals("121212.1S0050505.0E", format.latLon(ll));
    }

    @Test
    public void testNW() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(90.98333333333333, -179.98672222222223);

        assertEquals("905900.0N1795912.2W", format.latLon(ll));
    }

    @Test
    public void testSW() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(-45.01663888888889, -70.75225277777778);

        assertEquals("450059.9S0704508.1W", format.latLon(ll));
    }
}
