/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.symbology.milstd2525.graphics.MetocSidc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/**
 * Test parsing of all SIDC constants declared in {@link MetocSidc}.
 */
@RunWith(JUnit4.class)
public class MetocSidcTest
{
    @Test
    public void testParse() throws IllegalAccessException
    {
        // MetocSidc declares constants for each SIDC. Grab all of these fields and make sure that each one can be
        // parsed successfully.
        Field[] fields = MetocSidc.class.getDeclaredFields();

        for (Field f : fields)
        {
            String sidc = (String) f.get(null);

            SymbolCode code = new SymbolCode(sidc);
            assertEquals(sidc, code.toString());
        }
    }
}
