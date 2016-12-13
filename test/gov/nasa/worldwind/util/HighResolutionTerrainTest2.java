/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.fail;

/**
 * Checks for re-occurrence of WWJ-521.
 */
@Ignore
@RunWith(JUnit4.class)
public class HighResolutionTerrainTest2
{
    /**
     * Small (< 1 km x 1 km) sector crosses sector resolution limit between US and Canada borders. (10m - 30m range of
     * resolutions)
     */
    private static final Sector SECTOR = Sector.boundingSector(
        LatLon.fromDegrees(49.99657, -122.25573),
        LatLon.fromDegrees(50.00240, -122.24467));

    @SuppressWarnings("FieldCanBeLocal")
    private int GRID_SIZE = 3;//50;

    @Test
    public void testConsistencyOfBulkPositions()
    {
        ArrayList<Position> referencePositions = generateReferenceLocations(SECTOR, GRID_SIZE, GRID_SIZE);

        Globe globe = new Earth();

        // Use old elevation model for this example...
        globe.setElevationModel(EllipsoidalGlobe.makeElevationModel(
            "config/Earth/EarthElevationModelAsBil16.xml",
            "config/Earth/EarthElevationModelAsBil16.xml"));

        HighResolutionTerrain hrt = new HighResolutionTerrain(globe, SECTOR, null, 1.0);
        hrt.setTimeout(20000L);
        try
        {
            hrt.intersect(referencePositions, new HighResolutionTerrain.IntersectionCallback()
            {
                @Override
                public void intersection(Position pA, Position pB, Intersection[] intersections)
                {
                    // do nothing - we  just want to complete
                }

                public void exception(Exception e)
                {
                    fail("Intersection calculation timed out");
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static ArrayList<Position> generateReferenceLocations(Sector sector, int numLats, int numLons)
    {
        int decimalPlaces = 5;
        ArrayList<Position> locations = new ArrayList<Position>();
        double dLat = (sector.getMaxLatitude().degrees - sector.getMinLatitude().degrees) / (numLats - 1);
        double dLon = (sector.getMaxLongitude().degrees - sector.getMinLongitude().degrees) / (numLons - 1);

        Position p0 = Position.fromDegrees(
            round(decimalPlaces, sector.getMinLatitude().degrees),
            round(decimalPlaces, sector.getMinLongitude().degrees), 0);
        for (int j = 1; j < numLats; j++)
        {
            double lat = sector.getMinLatitude().degrees + j * dLat;

            for (int i = 0; i < numLons; i++)
            {
                double lon = sector.getMinLongitude().degrees + i * dLon;

                locations.add(p0);
                locations.add(Position.fromDegrees(round(decimalPlaces, lat), round(decimalPlaces, lon), 0));
            }
        }

        return locations;
    }

    private static double round(int decimalPlaces, double value)
    {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }
}

