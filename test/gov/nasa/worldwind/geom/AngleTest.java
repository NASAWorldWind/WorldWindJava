/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class AngleTest
{
    private static final double DELTA = 1e-9;

    @Test
    public void testFromDMS_lessThanZeroDegrees()
    {
        try
        {
            Angle.fromDMS(-60, 14, 23);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testFromDMS_lessThanZeroMinutes()
    {
        try
        {
            Angle.fromDMS(42, -14, 23);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testFromDMS_lessThanZeroSeconds()
    {
        try
        {
            Angle.fromDMS(42, 32, -15);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testFromDMS_ZeroDegree()
    {
        int degrees = 0;
        int minutes = 30;
        int seconds = 45;
        double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

        Angle angle = Angle.fromDMS(degrees, minutes, seconds);

        assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
    }

    @Test
    public void testFromDMS_AboveZeroDegrees()
    {
        int degrees = 15;
        int minutes = 30;
        int seconds = 45;
        double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

        Angle angle = Angle.fromDMS(degrees, minutes, seconds);

        assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
    }

    @Test
    public void testFromDMdS_lessThanZeroDegrees()
    {
        try
        {
            Angle.fromDMdS(-60, 23.4);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testFromDMdS_lessThanZeroMinutes()
    {
        try
        {
            Angle.fromDMdS(42, -12.486);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            assertTrue("Should raise an IllegalArgumentException", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testFromDMdS_ZeroDegree()
    {
        int degrees = 0;
        int minutes = 30;
        int seconds = 45;
        double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

        Angle angle = Angle.fromDMdS(degrees, minutes + seconds / 60d);

        assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
    }

    @Test
    public void testFromDMdS_AboveZeroDegrees()
    {
        int degrees = 16;
        int minutes = 30;
        int seconds = 45;
        double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

        Angle angle = Angle.fromDMdS(degrees, minutes + seconds / 60d);

        assertEquals("test with zero degrees", expectedDegrees, angle.degrees, DELTA);
    }

    @Test
    public void testFromDMString_PositiveCoordinate()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees;
        String angleString = degrees + " " + minutes + " " + seconds;

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("standard positive format", expectedValue, actualValue, 0.0);
    }

    @Test
    public void testFromDMString_PositiveSign()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees;
        String angleString = "+" + degrees + " " + minutes + " " + seconds;

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("positive prefix format", expectedValue, actualValue, 0.0);
    }

    @Test
    public void testFromDMString_PositiveDirection()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees;
        String angleString = degrees + " " + minutes + " " + seconds + " N";

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("positive direction format", expectedValue, actualValue, 0.0);
    }

    @Test
    public void testFromDMString_NegativeSign()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
        String angleString = "-" + degrees + " " + minutes + " " + seconds;

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("negative prefix format", expectedValue, actualValue, 0.0);
    }

    @Test
    public void testFromDMString_NegativeDirection()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
        String angleString = degrees + " " + minutes + " " + seconds + " S";

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("negative direction format", expectedValue, actualValue, 0.0);
    }

    @Test
    public void testFromDMSString_ConflictingPrefixSuffixCaseOne()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
        String angleString = "+" + degrees + " " + minutes + " " + seconds + " S";

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("conflicting string format, positive sign and negative direction", expectedValue, actualValue,
            0.0);
    }

    @Test
    public void testFromDMSString_ConflictingPrefixSuffixCaseTwo()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees; // North
        String angleString = "-" + degrees + " " + minutes + " " + seconds + " N";

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("conflicting string format, negative sign and positive direction", expectedValue, actualValue,
            0.0);
    }

    @Test
    public void testFromDMSString_ConflictingPrefixSuffixCaseThree()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).multiply(-1.0).degrees;
        String angleString = "-" + degrees + " " + minutes + " " + seconds + " S";

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("conflicting string format, negative sign and negative direction", expectedValue, actualValue,
            0.0);
    }

    @Test
    public void testFromDMSString_ConflictingPrefixSuffixCaseFour()
    {
        int degrees = 16;
        int minutes = 58;
        int seconds = 27;
        double expectedValue = Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d).degrees; // North
        String angleString = "+" + degrees + " " + minutes + " " + seconds + " N";

        double actualValue = Angle.fromDMS(angleString).degrees;

        assertEquals("conflicting string format, positive sign and direction", expectedValue, actualValue, 0.0);
    }
}
