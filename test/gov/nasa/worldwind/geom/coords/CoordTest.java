/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import gov.nasa.worldwind.geom.LatLon;
import org.junit.Test;

import static org.junit.Assert.*;

public class CoordTest
{
    private static boolean isClose(double x, double y, double limit)
    {
        return (Math.abs(x - y) < limit);
    }

    private static boolean isClose(LatLon a, LatLon b)
    {
        double epsilonRad = Math.toRadians(9.0e-6);
        return isClose(a, b, epsilonRad);
    }

    private static boolean isClose(LatLon a, LatLon b, double limit)
    {
        return isClose(a.latitude.radians, b.latitude.radians, limit)
            && isClose(a.longitude.radians, b.longitude.radians, limit);
    }
    
    private static final LatLon[] TEST_POSITIONS = 
    {
        LatLon.fromDegrees(-74.37916, 155.02235),
        LatLon.fromDegrees(0, 0),
        LatLon.fromDegrees(0.1300, -0.2324),
        LatLon.fromDegrees(-45.6456, 23.3545),
        LatLon.fromDegrees(-12.7650, -33.8765),
        LatLon.fromDegrees(23.4578, -135.4545),
        LatLon.fromDegrees(77.3450, 156.9876)
    };
    
    @Test
    public void utmConstructionTest()
    {
        for (LatLon input : TEST_POSITIONS)
        {
            UTMCoord fromLatLon = UTMCoord.fromLatLon(input.latitude, input.longitude);
            UTMCoord utmCoord = UTMCoord.fromUTM(fromLatLon.getZone(), fromLatLon.getHemisphere(), fromLatLon.getEasting(), fromLatLon.getNorthing());
            LatLon position = LatLon.fromRadians(utmCoord.getLatitude().radians, utmCoord.getLongitude().radians);
            assertTrue(isClose(input, position));
        }
    }

    @Test
    public void mgrsConstructionTest()
    {
        for (LatLon input : TEST_POSITIONS)
        {
            MGRSCoord fromLatLon = MGRSCoord.fromLatLon(input.latitude, input.longitude);
            MGRSCoord fromString = MGRSCoord.fromString(fromLatLon.toString(), null);
            LatLon position = LatLon.fromRadians(fromString.getLatitude().radians, fromString.getLongitude().radians);
            assertTrue(isClose(input, position, 000020));
        }
    }
    
    private static final LatLon[] MGRS_ONLY_POSITIONS =
    {
        LatLon.fromDegrees(-89.3454, -48.9306),
        LatLon.fromDegrees(-80.5434, -170.6540),
    };
    
    @Test
    public void mgrsOnlyConstructionTest()
    {
        for (LatLon input : MGRS_ONLY_POSITIONS)
        {
            MGRSCoord fromLatLon = MGRSCoord.fromLatLon(input.latitude, input.longitude);
            MGRSCoord fromString = MGRSCoord.fromString(fromLatLon.toString(), null);
            LatLon position = LatLon.fromRadians(fromString.getLatitude().radians, fromString.getLongitude().radians);
            assertTrue(isClose(input, position, 000020));
        }
    }
    
    private static final LatLon[] NO_INVERSE_POSITIONS =
    {
        LatLon.fromDegrees(90.0000, 177.0000),
        LatLon.fromDegrees(-90.0000, -177.0000),
        LatLon.fromDegrees(90.0000, 3.0000)
    };
    
    private static final String[] NO_INVERSE_TO_MGRS =
    {
        "ZAH 00000 00000", "BAN 00000 00000", "ZAH 00000 00000"
    };
    
    @Test
    public void noInverseToMGRSTest()
    {
        for (int i = 0; i < NO_INVERSE_POSITIONS.length; i++)
        {
            LatLon input = NO_INVERSE_POSITIONS[i];
            MGRSCoord fromLatLon = MGRSCoord.fromLatLon(input.latitude, input.longitude);
            String mgrsString = fromLatLon.toString().trim();
            assertEquals(mgrsString, NO_INVERSE_TO_MGRS[i]);
        }
    }
}
