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

