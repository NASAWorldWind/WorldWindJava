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

package gov.nasa.worldwind.geom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
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
            assertTrue(e instanceof IllegalArgumentException, "Should raise an IllegalArgumentException");
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
            assertTrue(e instanceof IllegalArgumentException, "Should raise an IllegalArgumentException");
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
            assertTrue(e instanceof IllegalArgumentException, "Should raise an IllegalArgumentException");
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

        assertEquals(expectedDegrees, angle.degrees, DELTA, "test with zero degrees");
    }

    @Test
    public void testFromDMS_AboveZeroDegrees()
    {
        int degrees = 15;
        int minutes = 30;
        int seconds = 45;
        double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

        Angle angle = Angle.fromDMS(degrees, minutes, seconds);

        assertEquals(expectedDegrees, angle.degrees, DELTA, "test with zero degrees");
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
            assertTrue(e instanceof IllegalArgumentException, "Should raise an IllegalArgumentException");
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
            assertTrue(e instanceof IllegalArgumentException, "Should raise an IllegalArgumentException");
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

        assertEquals(expectedDegrees, angle.degrees, DELTA, "test with zero degrees");
    }

    @Test
    public void testFromDMdS_AboveZeroDegrees()
    {
        int degrees = 16;
        int minutes = 30;
        int seconds = 45;
        double expectedDegrees = degrees + minutes / 60d + seconds / 3600d;

        Angle angle = Angle.fromDMdS(degrees, minutes + seconds / 60d);

        assertEquals(expectedDegrees, angle.degrees, DELTA, "test with zero degrees");
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

        assertEquals(expectedValue, actualValue, 0.0, "standard positive format");
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

        assertEquals(expectedValue, actualValue, 0.0, "positive prefix format");
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

        assertEquals(expectedValue, actualValue, 0.0, "positive direction format");
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

        assertEquals(expectedValue, actualValue, 0.0, "negative prefix format");
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

        assertEquals(expectedValue, actualValue, 0.0, "negative direction format");
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

        assertEquals(expectedValue, actualValue, 0.0, "conflicting string format, positive sign and negative direction");
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

        assertEquals(expectedValue, actualValue, 0.0, "conflicting string format, negative sign and positive direction");
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

        assertEquals(expectedValue, actualValue, 0.0, "conflicting string format, negative sign and negative direction");
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

        assertEquals(expectedValue, actualValue, 0.0, "conflicting string format, positive sign and direction");
    }
}
