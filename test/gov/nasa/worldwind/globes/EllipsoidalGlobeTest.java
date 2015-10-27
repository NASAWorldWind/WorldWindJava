/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.geom.*;
import junit.framework.*;
import junit.textui.TestRunner;
import org.junit.*;
import org.junit.Assert;

/**
 * @author tag
 * @version $Id: EllipsoidalGlobeTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class EllipsoidalGlobeTest
{
    private static final double TOLERANCE = 0.1d;

    public static class Tests extends TestCase
    {
        private Globe globe;

        @Before
        public void setUp()
        {
            this.globe = new Earth();
        }

        @After
        public void tearDown()
        {
            this.globe = null;
        }

        public void testEquatorialRadius()
        {
            double radius = this.globe.getEquatorialRadius();

            assertEquals("Equatorial radius", radius, 6378137d);
        }

        public void testgeodeticToCartesian()
        {
            Position orig = new Position(LatLon.fromDegrees(30.42515, -97.547562), 200.5d);

            Vec4 vec = globe.computePointFromPosition(orig);

            assertEquals("X comparision", vec.getX(), -5457021.181d, TOLERANCE);
            assertEquals("Y comparision", vec.getY(), 3211203.627d, TOLERANCE);
            assertEquals("Z comparision", vec.getZ(), -723039.434d, TOLERANCE);

            //now convert back and compare to original
            Position p = globe.computePositionFromPoint(vec);
            assertEquals("Latitude comparision", orig.getLatitude().degrees, p.getLatitude().degrees, TOLERANCE);
            assertEquals("Longitude comparision", orig.getLongitude().degrees, p.getLongitude().degrees, TOLERANCE);
            assertEquals("Height comparision", orig.getElevation(), p.getElevation(), TOLERANCE);
        }

        public void testgeodeticToCartesian2()
        {

            Position orig = new Position(LatLon.fromDegrees(88.582737, 60.245658), 200.5d);

            Vec4 vec = globe.computePointFromPosition(orig);

            assertEquals("X comparision", vec.getX(), 137419.7051d, TOLERANCE);
            assertEquals("Y comparision", vec.getY(), 6354995.0149d, TOLERANCE);
            assertEquals("Z comparision", vec.getZ(), 78555.6486d, TOLERANCE);

            //now convert back and compare to original
            Position p = globe.computePositionFromPoint(vec);
            assertEquals("Latitude comparision", orig.getLatitude().degrees, p.getLatitude().degrees, TOLERANCE);
            assertEquals("Longitude comparision", orig.getLongitude().degrees, p.getLongitude().degrees, TOLERANCE);
            assertEquals("Height comparision", orig.getElevation(), p.getElevation(), TOLERANCE);
        }

        public void testgeodeticToCartesian3()
        {

            Position orig = new Position(LatLon.fromDegrees(-33.903959, 18.505155), 200.5d);

            Vec4 vec = globe.computePointFromPosition(orig);

            assertEquals("X comparision", vec.getX(), 1681968.3306d, TOLERANCE);
            assertEquals("Y comparision", vec.getY(), -3537721.6660d, TOLERANCE);
            assertEquals("Z comparision", vec.getZ(), 5025370.8202d, TOLERANCE);

            //now convert back and compare to original
            Position p = globe.computePositionFromPoint(vec);
            assertEquals("Latitude comparision", orig.getLatitude().degrees, p.getLatitude().degrees, TOLERANCE);
            assertEquals("Longitude comparision", orig.getLongitude().degrees, p.getLongitude().degrees, TOLERANCE);
            assertEquals("Height comparision", orig.getElevation(), p.getElevation(), TOLERANCE);
        }

        public void testgeodeticToCartesian4()
        {

            Position orig = new Position(LatLon.fromDegrees(88.582737, 60.245658), 200.5d);

            Vec4 vec = globe.computePointFromPosition(orig);

            assertEquals("X comparision", vec.getX(), 137419.705d, TOLERANCE);
            assertEquals("Y comparision", vec.getY(), 6354995.001d, TOLERANCE);
            assertEquals("Z comparision", vec.getZ(), 78555.649d, TOLERANCE);

            //now convert back and compare to original
            Position p = globe.computePositionFromPoint(vec);
            assertEquals("Latitude comparision", orig.getLatitude().degrees, p.getLatitude().degrees, TOLERANCE);
            assertEquals("Longitude comparision", orig.getLongitude().degrees, p.getLongitude().degrees, TOLERANCE);
            assertEquals("Height comparision", orig.getElevation(), p.getElevation(), TOLERANCE);
        }
    }

    /**
     * These tests make sure that coordinate conversions in an EllipsoidalGlobe are accurate and consistent.
     *
     * @author nkronenfeld
     */
    public static class CoordinateConversionTests extends TestCase
    {
        private static final double REQUIRED_PRECISION = 0.00000001;

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
                    assertRelativelyEqual(msg, Math.sqrt(x * x + z * z) - a, p.elevation);
                    //noinspection SuspiciousNameCombination
                    Assert.assertEquals(msg, Math.atan2(x, z), p.longitude.radians, REQUIRED_PRECISION);
                    Assert.assertEquals(msg, 0, p.latitude.radians, REQUIRED_PRECISION);

                    // Make sure round trip works
                    Vec4 w = earth.computePointFromPosition(p);
                    assertRelativelyEqual(msg, v.x, w.x);
                    assertRelativelyEqual(msg, v.y, w.y);
                    assertRelativelyEqual(msg, v.z, w.z);
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

                    Assert.assertEquals(msg, 0, v.y, REQUIRED_PRECISION);
                    assertRelativelyEqual(msg, r * Math.sin(lon), v.x);
                    assertRelativelyEqual(msg, r * Math.cos(lon), v.z);

                    // Make sure round trip works
                    Position q = earth.computePositionFromPoint(v);
                    Assert.assertEquals(msg, p.latitude.radians, q.latitude.radians, REQUIRED_PRECISION);
                    Assert.assertEquals(msg, p.longitude.radians, q.longitude.radians, REQUIRED_PRECISION);
                    assertRelativelyEqual(msg, p.elevation, q.elevation);
                }
            }
        }

        public void testEllipsoidAxis()
        {
            // Test to make sure that coordinate transforms near the equatorial plane work correctly.
            Earth earth = new Earth();
            double a = earth.getEquatorialRadius();
            double b = earth.getPolarRadius();

            // This routine is more error-prone than all the others; it looks like
            // we just need to deal with that.  It's still pretty good.
            double errorAllowed = 0.0000007;
            // Check along the axis, cartesian->geodetic
            for (double y = -2 * a; y <= 2 * a; y += a / 17)
            {
                Vec4 v = new Vec4(0, y, 0);
                String msg = "At y=" + y;

                // Check cartesian->geodetic
                Position p = earth.computePositionFromPoint(v);

                // Longitude is unspecifiable along the axis
                Assert.assertEquals(msg, Math.PI / 2 * Math.signum(y), p.latitude.radians, REQUIRED_PRECISION);
                // System.out.println("Relative error at y=\t"+y+"\t"+((Math.abs(y)-b)/(p.elevation)));
                assertRelativelyEqual(msg, Math.abs(y) - b, p.elevation, errorAllowed);

                // Check geodetic->cartesian
                Vec4 w = earth.computePointFromPosition(p);
                assertRelativelyEqual(msg, v.x, w.x);
                assertRelativelyEqual(msg, v.y, w.y);
                assertRelativelyEqual(msg, v.z, w.z);
            }
        }

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
            // assertRelativelyEqual(-earth.getEquatorialRadius(), p.elevation);
            // Assert.assertEquals(0, p.latitude.radians, REQUIRED_PRECISION);
            // case b: center considered as part of the axis
            assertRelativelyEqual("At center", -earth.getPolarRadius(), p.elevation);
            // case b1: part of northern axis
            // Assert.assertEquals(Math.PI/2, p.latitude.radians, REQUIRED_PRECISION);
            // case b2: part of southern axis
            // Assert.assertEquals(-Math.PI/2, p.latitude.radians, REQUIRED_PRECISION);
            // It's largely because of the existence of b1 and b2 that I suspect a to be the proper solution.
            // I'm wrong - it's case b, and lat and lon are just wrong at the moment.  Perhaps fix?
        }

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
                        Assert.assertEquals(msg, Math.signum(y), Math.signum(p.latitude.degrees), REQUIRED_PRECISION);

                        Vec4 v = earth.computePointFromPosition(p);
                        // Check consistency
                        assertRelativelyEqual(msg, x, v.x);
                        assertRelativelyEqual(msg, y, v.y);
                        assertRelativelyEqual(msg, z, v.z);
                    }
                }
            }
        }

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
                Assert.assertEquals(msg, Math.signum(y), Math.signum(pos.latitude.degrees), REQUIRED_PRECISION);

                Vec4 w = earth.computePointFromPosition(pos);
                // Check consistency
                assertRelativelyEqual(msg, x, w.x);
                assertRelativelyEqual(msg, y, w.y);
                assertRelativelyEqual(msg, 0, w.z);
            }
        }

        private void assertRelativelyEqual(String msg, double expected, double actual)
        {
            assertRelativelyEqual(msg, expected, actual, REQUIRED_PRECISION);
        }

        private void assertRelativelyEqual(String msg, double expected, double actual, double precision)
        {
            if (Math.abs(expected) < 1.0)
            {
                Assert.assertEquals(msg, expected, actual, precision);
            }
            else
            {
                Assert.assertEquals(msg, 1.0, actual / expected, precision);
            }
        }
    }

    public static void main(String[] args)
    {
        new TestRunner().doRun(new TestSuite(Tests.class));
        new TestRunner().doRun(new TestSuite(CoordinateConversionTests.class));
    }
}
