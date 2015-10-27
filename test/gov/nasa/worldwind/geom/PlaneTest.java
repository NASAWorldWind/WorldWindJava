/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import junit.framework.*;
import junit.textui.TestRunner;
import org.junit.*;

/**
 * @author tag
 * @version $Id: PlaneTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PlaneTest
{
    public static class Tests extends TestCase
    {
        @Before
        public void setUp()
        {
        }

        @After
        public void tearDown()
        {
        }

        public void testSegmentIntersection()
        {
            Plane p = new Plane(new Vec4(0, 0, -1, 0));

            Vec4 pt = p.intersect(Vec4.ZERO, new Vec4(0, 0, -1));
            assertTrue("Perpendicular, 0 at origin, should produce intersection at origin", pt.equals(Vec4.ZERO));

            try
            {
                //noinspection UnusedAssignment
                pt = p.intersect(null, new Vec4(0, 0, -1));
                fail("Should raise an IllegalArgumentException");
            }
            catch (Exception e)
            {
            }

            pt = p.intersect(new Vec4(1, 0, 0), new Vec4(1, 0, 0));
            assertTrue(
                "Line segment is in fact a point, located on the plane, should produce intersection at (1, 0, 0)",
                pt.equals(new Vec4(1, 0, 0)));

            pt = p.intersect(new Vec4(0, 0, -1), new Vec4(0, 0, -1));
            assertNull("Line segment is in fact a point not on the plane, should produce null for no intersection", pt);

            pt = p.intersect(new Vec4(0, 0, 1), new Vec4(0, 0, -1));
            assertTrue("Perpendicular, integer end points off origin, should produce intersection at origin",
                pt.equals(Vec4.ZERO));

            pt = p.intersect(new Vec4(0, 0, 0.5), new Vec4(0, 0, -0.5));
            assertTrue("Perpendicular, non-integer end points off origin, should produce intersection at origin",
                pt.equals(Vec4.ZERO));

            pt = p.intersect(new Vec4(0.5, 0.5, 0.5), new Vec4(-0.5, -0.5, -0.5));
            assertTrue("Not perpendicular, non-integer end points off origin, should produce intersection at origin",
                pt.equals(Vec4.ZERO));

            pt = p.intersect(new Vec4(1, 0, 0), new Vec4(2, 0, 0));
            assertTrue("Parallel, in plane, should produce intersection at origin",
                pt.equals(Vec4.INFINITY));

            pt = p.intersect(new Vec4(1, 0, 1), new Vec4(2, 0, 1));
            assertNull("Parallel, integer end points off origin, should produce null for no intersection", pt);
        }

        public void testLineIntersection()
        {
            Plane p = new Plane(new Vec4(0, 0, 1, 0));

            Vec4 pt = p.intersect(new Line(new Vec4(807066.3082512334, 4864661.747666055, 4.5E7, 1.0),
                new Vec4(0.0, 0.0, -1.0, 0.0)));
            assertNotNull("Simple intersection", pt);
        }
    }

    public static void main(String[] args)
    {
        new TestRunner().doRun(new TestSuite(Tests.class));
    }
}
