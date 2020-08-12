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

package gov.nasa.worldwind.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class WWUtilTest
{
    /** Test parsing time strings. */
    @Test
    public void testParseTimeString()
    {
        Calendar expected = Calendar.getInstance();
        expected.set(Calendar.MILLISECOND, 0);

        Long time = WWUtil.parseTimeString("1997");
        expected.set(1997, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07");
        expected.set(1997, Calendar.JULY, 1, 0, 0, 0);
        assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07-01");
        expected.set(1997, Calendar.JULY, 1, 0, 0, 0);
        assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07-01T07:30:15Z");
        expected.set(1997, Calendar.JULY, 1, 7, 30, 15);
        assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("1997-07-01T07:30:15+03:00");
        expected.set(1997, Calendar.JULY, 1, 7, 30, 15);
        expected.setTimeZone(TimeZone.getTimeZone("EAT"));
        assertEquals((Long) expected.getTimeInMillis(), time);

        time = WWUtil.parseTimeString("invalid time");
        assertNull(time);
    }
}
