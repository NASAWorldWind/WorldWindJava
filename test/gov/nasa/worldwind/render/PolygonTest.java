/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
