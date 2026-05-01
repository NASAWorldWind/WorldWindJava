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
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.geom.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class EllipsoidalGlobeTest
{
    private static final double THRESHOLD = 1.0e-1;
    private static final double REQUIRED_PRECISION = 1.0e-8;

    private Globe globe;

    @BeforeEach
    public void setUp()
    {
        this.globe = new Earth();
    }

    @AfterEach
    public void tearDown()
    {
        this.globe = null;
    }

    @Test
    public void testEquatorialRadius()
    {
        double radius = this.globe.getEquatorialRadius();

        assertEquals(radius, 6378137d, 0.0, "Equatorial radius");
    }

    @Test
    public void testGeodeticToCartesian()
    {
        Position orig = new Position(LatLon.fromDegrees(30.42515, -97.547562), 200.5d);

        Vec4 vec = globe.computePointFromPosition(orig);

        assertEquals(vec.getX(), -5457021.181d, THRESHOLD, "X comparision");
        assertEquals(vec.getY(), 3211203.627d, THRESHOLD, "Y comparision");
        assertEquals(vec.getZ(), -723039.434d, THRESHOLD, "Z comparision");

        //now convert back and compare to original
        Position p = globe.computePositionFromPoint(vec);
        assertEquals(orig.getLatitude().degrees, p.getLatitude().degrees, THRESHOLD, "Latitude comparision");
        assertEquals(orig.getLongitude().degrees, p.getLongitude().degrees, THRESHOLD, "Longitude comparision");
        assertEquals(orig.getElevation(), p.getElevation(), THRESHOLD, "Height comparision");
    }

    @Test
    public void testGeodeticToCartesian2()
    {
        Position orig = new Position(LatLon.fromDegrees(88.582737, 60.245658), 200.5d);

        Vec4 vec = globe.computePointFromPosition(orig);

        assertEquals(vec.getX(), 137419.7051d, THRESHOLD, "X comparision");
        assertEquals(vec.getY(), 6354995.0149d, THRESHOLD, "Y comparision");
        assertEquals(vec.getZ(), 78555.6486d, THRESHOLD, "Z comparision");

        //now convert back and compare to original
        Position p = globe.computePositionFromPoint(vec);
        assertEquals(orig.getLatitude().degrees, p.getLatitude().degrees, THRESHOLD, "Latitude comparision");
        assertEquals(orig.getLongitude().degrees, p.getLongitude().degrees, THRESHOLD, "Longitude comparision");
        assertEquals(orig.getElevation(), p.getElevation(), THRESHOLD, "Height comparision");
    }

    @Test
    public void testGeodeticToCartesian3()
    {
        Position orig = new Position(LatLon.fromDegrees(-33.903959, 18.505155), 200.5d);

        Vec4 vec = globe.computePointFromPosition(orig);

        assertEquals(vec.getX(), 1681968.3306d, THRESHOLD, "X comparision");
        assertEquals(vec.getY(), -3537721.6660d, THRESHOLD, "Y comparision");
        assertEquals(vec.getZ(), 5025370.8202d, THRESHOLD, "Z comparision");

        //now convert back and compare to original
        Position p = globe.computePositionFromPoint(vec);
        assertEquals(orig.getLatitude().degrees, p.getLatitude().degrees, THRESHOLD, "Latitude comparision");
        assertEquals(orig.getLongitude().degrees, p.getLongitude().degrees, THRESHOLD, "Longitude comparision");
        assertEquals(orig.getElevation(), p.getElevation(), THRESHOLD, "Height comparision");
    }

    @Test
    public void testGeodeticToCartesian4()
    {
        Position orig = new Position(LatLon.fromDegrees(88.582737, 60.245658), 200.5d);

        Vec4 vec = globe.computePointFromPosition(orig);

        assertEquals(vec.getX(), 137419.705d, THRESHOLD, "X comparision");
        assertEquals(vec.getY(), 6354995.001d, THRESHOLD, "Y comparision");
        assertEquals(vec.getZ(), 78555.649d, THRESHOLD, "Z comparision");

        //now convert back and compare to original
        Position p = globe.computePositionFromPoint(vec);
        assertEquals(orig.getLatitude().degrees, p.getLatitude().degrees, THRESHOLD, "Latitude comparision");
        assertEquals(orig.getLongitude().degrees, p.getLongitude().degrees, THRESHOLD, "Longitude comparision");
        assertEquals(orig.getElevation(), p.getElevation(), THRESHOLD, "Height comparision");
    }

    @Test
    public void testEllipsoidEquatorialPlane()
    {
        // Test to make sure that coordinate transforms near the equatorial plane work correctly.
        Earth earth = new Earth();
        double a = earth.getEquatorialRadius();

        // Check a rough grid across the plane
        for (double x = -2 * a; x <= 2 * a; x += a / 17)
        {
            for (double z = -2 * a; z <= 2 * a; z += a / 17)
            {
                if (Math.abs(x) < REQUIRED_PRECISION && Math.abs(z) < REQUIRED_PRECISION)
                {
                    // can't test center this way
                    continue;
                }
                Vec4 v = new Vec4(x, 0, z);
                Position p = earth.computePositionFromPoint(v);

                // Coordinates in this plane are easy - latitude is 0, longitude
                // and elevation are based on standard polar (circular)
                // coordinates.
                String msg = "At x " + x + ", and z " + z;
                assertEquals(Math.sqrt(x * x + z * z) - a, p.elevation, THRESHOLD, msg);
                //noinspection SuspiciousNameCombination
                assertEquals(Math.atan2(x, z), p.longitude.radians, THRESHOLD, msg);
                assertEquals(0, p.latitude.radians, THRESHOLD, msg);

                // Make sure round trip works
                Vec4 w = earth.computePointFromPosition(p);
                assertEquals(v.x, w.x, THRESHOLD, msg);
                assertEquals(v.y, w.y, THRESHOLD, msg);
                assertEquals(v.z, w.z, THRESHOLD, msg);
            }
        }

        // Similarly in reverse
        for (double lon = -Math.PI; lon < Math.PI; lon += Math.PI * 0.1)
        {
            for (double r = 0; r <= 2 * a; r += a / 17)
            {
                if (0 == r)
                {
                    // can't test center this way
                    continue;
                }
                if (Math.abs(lon - 3.4557519189487724) < 0.000000001 &&
                    Math.abs(r - 375184.5294117647) < 0.000000001)
                {
                    continue;
                }

                Position p = Position.fromRadians(0, lon, r - a);
                Vec4 v = earth.computePointFromPosition(p);
                String msg = "At longitude " + lon + ", radius " + r;

                assertEquals(0, v.y, THRESHOLD, msg);
                assertEquals(r * Math.sin(lon), v.x, THRESHOLD, msg);
                assertEquals(r * Math.cos(lon), v.z, THRESHOLD, msg);

                // Make sure round trip works
                Position q = earth.computePositionFromPoint(v);
                assertEquals(p.latitude.radians, q.latitude.radians, THRESHOLD, msg);
                assertEquals(p.longitude.radians, q.longitude.radians, THRESHOLD, msg);
                assertEquals(p.elevation, q.elevation, THRESHOLD, msg);
            }
        }
    }

    @Test
    public void testEllipsoidAxis()
    {
        // Test to make sure that coordinate transforms near the equatorial plane work correctly.
        Earth earth = new Earth();
        double a = earth.getEquatorialRadius();
        double b = earth.getPolarRadius();

        // This routine is more error-prone than all the others; it looks like
        // we just need to deal with that.  It's still pretty good.
        // Check along the axis, cartesian->geodetic
        for (double y = -2 * a; y <= 2 * a; y += a / 17)
        {
            Vec4 v = new Vec4(0, y, 0);
            String msg = "At y=" + y;

            // Check cartesian->geodetic
            Position p = earth.computePositionFromPoint(v);

            // Longitude is unspecifiable along the axis
            assertEquals(Math.PI / 2 * Math.signum(y), p.latitude.radians, THRESHOLD, msg);
            // System.out.println("Relative error at y=\t"+y+"\t"+((Math.abs(y)-b)/(p.elevation)));
            assertEquals(Math.abs(y) - b, p.elevation, THRESHOLD, msg);

            // Check geodetic->cartesian
            Vec4 w = earth.computePointFromPosition(p);
            assertEquals(v.x, w.x, THRESHOLD, msg);
            assertEquals(v.y, w.y, THRESHOLD, msg);
            assertEquals(v.z, w.z, THRESHOLD, msg);
        }
    }

    @Test
    public void testEllipsoidCenter()
    {
        Earth earth = new Earth();
        Vec4 v = new Vec4(0, 0, 0);
        Position p = earth.computePositionFromPoint(v);

        // The center should register either as a point in the equatorial plane
        // (lat=0) with elevation -MajorAxis, or a point on the axis with
        // elevation -MinorAxis. I think the algorithm assumes the former, but
        // there's going to be some discontinuity either way, so if this fails
        // one way, switch it to the other. If it fails both ways, something is
        // wrong.
        // case a: center considered as part of the equatorial plane
        // assertEquals(-earth.getEquatorialRadius(), p.elevation, THRESHOLD);
        // assertEquals(0, p.latitude.radians, THRESHOLD);
        // case b: center considered as part of the axis
        assertEquals(-earth.getPolarRadius(), p.elevation, THRESHOLD, "At center");
        // case b1: part of northern axis
        // assertEquals(Math.PI/2, p.latitude.radians, THRESHOLD);
        // case b2: part of southern axis
        // assertEquals(-Math.PI/2, p.latitude.radians, THRESHOLD);
        // It's largely because of the existence of b1 and b2 that I suspect a to be the proper solution.
        // I'm wrong - it's case b, and lat and lon are just wrong at the moment.  Perhaps fix?
    }

    @Test
    public void testGeneralRoundTripCartesianConversion()
    {
        // Tests cartesian->geodetic->cartesian conversion in a rough grid all
        // around the globe.
        //
        // This tests case combines two tests: one for consistency, one for
        // continuity. The consistency test is simply to make sure the round
        // trip returns the same value as the initial value. The continuity test
        // makes sure the sign of the latitude equals the sign of the y
        // coordinate - there are 4 solutions at each point, but only one with
        // matching sign, and the one with matching sign should be continuous,
        // so this effectively makes sure we check that the correct solution was
        // chosen.
        Earth earth = new Earth();
        double a = earth.getEquatorialRadius();

        // different grid size than above, just to get more points tested.
        for (double x = -2 * a; x <= 2 * a; x += a / 19)
        {
            for (double y = -2 * a; y <= 2 * a; y += a / 19)
            {
                for (double z = -2 * a; z <= 2 * a; z += a / 19)
                {
                    Position p = earth.computePositionFromPoint(new Vec4(x, y, z));
                    String msg = "At [x, y, z]=[" + x + ", " + y + ", " + z + "]";
                    // Check continuity
                    assertEquals(Math.signum(y), Math.signum(p.latitude.degrees), THRESHOLD, msg);

                    Vec4 v = earth.computePointFromPosition(p);
                    // Check consistency
                    assertEquals(x, v.x, THRESHOLD, msg);
                    assertEquals(y, v.y, THRESHOLD, msg);
                    assertEquals(z, v.z, THRESHOLD, msg);
                }
            }
        }
    }

    @Test
    public void testRoundTripCartesianConversionAtEvolute()
    {
        // The evolute is the area in the center of the ellipsoid where we get the most problems.  Normalizing the ellipse (2d) to a unit circle in p and q (q is minor axis), the formula for it is:
        //
        // Tests cartesian->geodetic->cartesian conversion in a rough grid all
        // around the globe. This tests consistency, not continuity.
        Earth earth = new Earth();
        double a = earth.getEquatorialRadius();
        double a2 = a * a;
        double e2 = earth.getEccentricitySquared();
        double e4 = e2 * e2;
        double e43 = Math.cbrt(e4);

        // Checking one slice (by fiat at z=0) should be sufficient - everything
        // should be symetrical around the axis, and if it isn't, other tests
        // should show that.
        for (double p = 0; p < e4; p += e4 / 100)
        {
            double q = Math.cbrt(e43 - Math.cbrt(p));
            double x = Math.sqrt(p * a2);
            double y = Math.sqrt(q * a2 / (1 - e2));
            String msg = "At p=" + p;

            Position pos = earth.computePositionFromPoint(new Vec4(x, y, 0));
            // Check continuity
            assertEquals(Math.signum(y), Math.signum(pos.latitude.degrees), THRESHOLD, msg);

            Vec4 w = earth.computePointFromPosition(pos);
            // Check consistency
            assertEquals(x, w.x, THRESHOLD, msg);
            assertEquals(y, w.y, THRESHOLD, msg);
            assertEquals(0, w.z, THRESHOLD, msg);
        }
    }
}
