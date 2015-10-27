/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.symbology.milstd2525.graphics.MetocSidc;
import junit.framework.TestCase;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Test parsing of all SIDC constants declared in {@link MetocSidc}.
 *
 * @author pabercrombie
 * @version $Id: MetocSidcTest.java 563 2012-04-26 18:16:22Z pabercrombie $
 */
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
            TestCase.assertEquals(sidc, code.toString());
        }
    }

    public static void main(String[] args)
    {
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(MetocSidcTest.class));
    }
}
