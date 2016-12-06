/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/**
 * Test parsing of all SIDC constants declared in {@link TacGrpSidc}.
 */
@SuppressWarnings("WeakerAccess")
@RunWith(JUnit4.class)
public class TacGrpSidcTest
{
    /** Valid standard identity characters for MIL-STD-2525C tactical graphics (see Table B-I, pg. 305). */
    public static final char[] ALL_STANDARD_IDENTITY = {'P', 'U', 'A', 'F', 'N', 'S', 'H', 'G', 'W', 'M', 'D', 'L',
        'J', 'K'};

    /** Valid status characters for MIL-STD-2525C tactical graphics (see Table B-I, pg. 305). */
    public static final char[] ALL_STATUS = {'A', 'S', 'P', 'K'};

    /** Valid echelon characters for MIL-STD-2525C tactical graphics (see Table B-II, pg. 305). */
    public static final char[] ALL_ECHELON = {'-', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N'};

    @Test
    public void testParse() throws IllegalAccessException
    {
        // TacGrpSidc declares constants for each SIDC. Grab all of these fields and make
        // sure that each one can be parsed successfully with all combinations of standard identity and status.
        Field[] fields = TacGrpSidc.class.getDeclaredFields();

        for (Field f : fields)
        {
            StringBuilder sidc = new StringBuilder((String) f.get(null));

            for (char stdId : ALL_STANDARD_IDENTITY)
            {
                for (char status : ALL_STATUS)
                {
                    for (char echelon : ALL_ECHELON)
                    {
                        sidc.setCharAt(1, stdId);
                        sidc.setCharAt(3, status);
                        sidc.setCharAt(11, echelon);

                        SymbolCode code = new SymbolCode(sidc.toString());
                        assertEquals(sidc.toString(), code.toString());
                    }
                }
            }
        }
    }
}
