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

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class PolygonTest
{
    private Globe globe;
    private double verticalExaggeration;
    private List<Position> positions;
    private Sector sector;

    @Before
    public void setUp()
    {
        this.globe = new Earth();
        this.verticalExaggeration = 1.0;
        this.positions = Arrays.asList(
            Position.fromDegrees(28, -106, 0),
            Position.fromDegrees(35, -104, 0),
            Position.fromDegrees(28, -107, 100),
            Position.fromDegrees(28, -106, 0));
        this.sector = Sector.boundingSector(this.positions);
    }

    @After
    public void tearDown()
    {
        this.globe = null;
    }

    @Test
    public void testGetExtentClampToGround()
    {
        double[] minAndMaxElevations = this.globe.getMinAndMaxElevations(this.sector);

        Extent expected = Sector.computeBoundingBox(this.globe, this.verticalExaggeration, this.sector,
            minAndMaxElevations[0],
            minAndMaxElevations[1]);

        Polygon pgon = new Polygon(this.positions);
        pgon.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        Extent actual = pgon.getExtent(this.globe, this.verticalExaggeration);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetExtentAbsolute()
    {
        Extent expected = Sector.computeBoundingBox(this.globe, this.verticalExaggeration, this.sector, 0, 100);

        Polygon pgon = new Polygon(this.positions);
        pgon.setAltitudeMode(WorldWind.ABSOLUTE);

        Extent actual = pgon.getExtent(this.globe, this.verticalExaggeration);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetExtentRelative()
    {
        double[] minAndMaxElevations = this.globe.getMinAndMaxElevations(this.sector);

        Extent expected = Sector.computeBoundingBox(this.globe, this.verticalExaggeration, this.sector,
            minAndMaxElevations[1], minAndMaxElevations[1] + 100);

        Polygon pgon = new Polygon(this.positions);
        pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);

        Extent actual = pgon.getExtent(this.globe, this.verticalExaggeration);

        assertEquals(expected, actual);
    }
}
