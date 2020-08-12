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
