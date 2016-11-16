/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import junit.framework.*;
import junit.textui.TestRunner;

public class AngleTest
{

    public static final double DELTA = 1e-9;

    public static void main(String[] args)
    {
        TestSuite testSuite = new TestSuite();
        testSuite.addTestSuite(AngleDMSTests.class);
        new TestRunner().doRun(testSuite);
    }

    public static class AngleDMSTests extends TestCase
    {

        public void testFromDMS_lessThanZeroDegrees()
        {
            Exception ex = null;
            try
            {
                Angle angle = Angle.fromDMS(-60, 14, 23);
            }
            catch (IllegalArgumentException e)
            {
                ex = e;
            }

            assertNotNull("test exception thrown on less than zero degrees", ex);
            assertTrue("correct exception", ex instanceof IllegalArgumentException);
        }

        public void testFromDMS_lessThanZeroMinutes()
        {
            Exception ex = null;
            try
            {
                Angle angle = Angle.fromDMS(42, -14, 23);
            }
            catch (IllegalArgumentException e)
            {
                ex = e;
            }

            assertNotNull("test exception thrown on less than zero minutes", ex);
            assertTrue("correct exception", ex instanceof IllegalArgumentException);
        }

        public void testFromDMS_lessThanZeroSeconds()
        {
            Exception ex = null;
            try
            {
                Angle angle = Angle.fromDMS(42, 32, -15);
            }
            catch (IllegalArgumentException e)
            {
                ex = e;
            }

            assertNotNull("test exception thrown on less than zero seconds", ex);
            assertTrue("correct exception", ex instanceof IllegalArgumentException);
        }

        public void testFromDMS_ZeroDegree()
        {
            int degrees = 0;
            int minutes = 30;
            int seconds = 45;
            double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

            Angle angle = Angle.fromDMS(degrees, minutes, seconds);

            assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
        }

        public void testFromDMS_AboveZeroDegrees()
        {
            int degrees = 15;
            int minutes = 30;
            int seconds = 45;
            double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

            Angle angle = Angle.fromDMS(degrees, minutes, seconds);

            assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
        }

        public void testFromDMdS_lessThanZeroDegrees()
        {
            Exception ex = null;
            try
            {
                Angle angle = Angle.fromDMdS(-60, 23.4);
            }
            catch (IllegalArgumentException e)
            {
                ex = e;
            }

            assertNotNull("test exception thrown on less than zero degrees", ex);
            assertTrue("correct exception", ex instanceof IllegalArgumentException);
        }

        public void testFromDMdS_lessThanZeroMinutes()
        {
            Exception ex = null;
            try
            {
                Angle angle = Angle.fromDMdS(42, -12.486);
            }
            catch (IllegalArgumentException e)
            {
                ex = e;
            }

            assertNotNull("test exception thrown on less than zero minutes", ex);
            assertTrue("correct exception", ex instanceof IllegalArgumentException);
        }

        public void testFromDMdS_ZeroDegree()
        {
            int degrees = 0;
            int minutes = 30;
            int seconds = 45;
            double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

            Angle angle = Angle.fromDMdS(degrees, minutes + seconds / 60d);

            assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
        }

        public void testFromDMdS_AboveZeroDegrees()
        {
            int degrees = 16;
            int minutes = 30;
            int seconds = 45;
            double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

            Angle angle = Angle.fromDMdS(degrees, minutes + seconds / 60d);

            assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
        }

        public void testFromDMString_PositiveCoordinate()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees;
            String angleString = degrees + " " + minutes + " " + seconds;

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("standard positive format", expectedValue, actualValue);
        }

        public void testFromDMString_PositiveSign()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees;
            String angleString = "+" + degrees + " " + minutes + " " + seconds;

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("positive prefix format", expectedValue, actualValue);
        }

        public void testFromDMString_PositiveDirection()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees;
            String angleString = degrees + " " + minutes + " " + seconds + " N";

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("positive direction format", expectedValue, actualValue);
        }

        public void testFromDMString_NegativeSign()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
            String angleString = "-" + degrees + " " + minutes + " " + seconds;

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("negative prefix format", expectedValue, actualValue);
        }

        public void testFromDMString_NegativeDirection()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
            String angleString = degrees + " " + minutes + " " + seconds + " S";

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("negative direction format", expectedValue, actualValue);
        }

        public void testFromDMSString_ConflictingPrefixSuffixCaseOne()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
            String angleString = "+" + degrees + " " + minutes + " " + seconds + " S";

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("conflicting string format, positive sign and negative direction", expectedValue, actualValue);
        }

        public void testFromDMSString_ConflictingPrefixSuffixCaseTwo()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees; // North
            String angleString = "-" + degrees + " " + minutes + " " + seconds + " N";

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("conflicting string format, negative sign and positive direction", expectedValue, actualValue);
        }

        public void testFromDMSString_ConflictingPrefixSuffixCaseThree()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
            String angleString = "-" + degrees + " " + minutes + " " + seconds + " S";

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("conflicting string format, negative sign and negative direction", expectedValue, actualValue);
        }

        public void testFromDMSString_ConflictingPrefixSuffixCaseFour()
        {
            int degrees = 16;
            int minutes = 58;
            int seconds = 27;
            double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees; // North
            String angleString = "+" + degrees + " " + minutes + " " + seconds + " N";

            double actualValue = Angle.fromDMS(angleString).degrees;

            assertEquals("conflicting string format, positive sign and direction", expectedValue, actualValue);
        }
    }
}
