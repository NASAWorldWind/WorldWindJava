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
 * @version $Id: LineTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LineTest
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

        public void testFrustumClipping()
        {
            Frustum f = new Frustum(); // default frustum is [-1,1] in all 3 dimensions
            Vec4 pa, pb;
            Vec4[] clipped;

            pa = Vec4.ZERO;
            pb = new Vec4(1, 0, 0);
            clipped = Line.clipToFrustum(pa, pb, f);
            assertTrue("Trivial accept A", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

            pa = Vec4.ZERO;
            pb = new Vec4(0.5, 0, 0);
            clipped = Line.clipToFrustum(pa, pb, f);
            assertTrue("Trivial accept B", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

            pa = Vec4.ZERO;
            pb = new Vec4(-0.5, 0, 0);
            clipped = Line.clipToFrustum(pa, pb, f);
            assertTrue("Trivial accept C", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

            pa = new Vec4(.5, .5, 2);
            pb = new Vec4(.5, .5, -2);
            clipped = Line.clipToFrustum(pa, pb, f);
            assertTrue("Clipped at near and far",
                clipped != null && clipped[0].equals(new Vec4(.5, .5, 1)) && clipped[1].equals(new Vec4(.5, .5, -1)));

            pa = new Vec4(.5, .5, .5);
            pb = new Vec4(.5, .5, -.5);
            clipped = Line.clipToFrustum(pa, pb, f);
            assertTrue("Trivial accept D", clipped != null && clipped[0].equals(pa) && clipped[1].equals(pb));

            pa = new Vec4(2, 2, 2);
            pb = new Vec4(4, 4, 4);
            clipped = Line.clipToFrustum(pa, pb, f);
            assertTrue("Segment not in frustum", clipped == null);
        }

        public void testDistanceToPoint()
        {
            Vec4 p0 = Vec4.ZERO;
            Vec4 p1 = new Vec4(2, 0, 0);

            double d;
            Vec4 p;
            p = new Vec4(0, 0, 0);
            d = Line.distanceToSegment(p0, p1, p);
            assertEquals("Point at start of line at origin", d, 0d);

            p = new Vec4(-2, 0, 0);
            d = Line.distanceToSegment(p0, p1, p);
            assertEquals("Point to left of segment on x axis", d, 2d);

            p = new Vec4(3, 0, 0);
            d = Line.distanceToSegment(p0, p1, p);
            assertEquals("Point to right of segment on x axis", d, 1d);

            p = new Vec4(2, 7, 0);
            d = Line.distanceToSegment(p0, p1, p);
            assertEquals("Point 7 above segment end point", d, 7d);

            p = new Vec4(1, 7, 0);
            d = Line.distanceToSegment(p0, p1, p);
            assertEquals("Point 7 above segment mid-point", d, 7d);

            p = new Vec4(1, -7, 0);
            d = Line.distanceToSegment(p0, p1, p);
            assertEquals("Point 7 below segment mid-point", d, 7d);

            p = new Vec4(1, 0.5, 0);
            d = Line.distanceToSegment(p0, p1, p);
            assertEquals("Point 1/2 above segment 1/4-point", d, 0.5);
        }
    }

    public static void main(String[] args)
    {
        new TestRunner().doRun(new TestSuite(Tests.class));
    }
}
