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

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class EntityMapTest
{
    /** Test basic entity replacement. */
    @Test
    public void testReplace()
    {
        String expected = "text < > & more text";
        String actual = EntityMap.replaceAll("text &lt; &gt; &amp; more text");

        assertEquals(expected, actual);
    }

    /** Test replacement of each entity in EntityMap. */
    @Test
    public void testAllReplacements() throws IllegalAccessException
    {
        for (int i = 0; i < EntityMap.entityKeys.length; i++)
        {
            String expected = EntityMap.entityReplacements[i];
            String actual = EntityMap.replaceAll(EntityMap.entityKeys[i]);

            assertEquals("Failed entity replacement: " + EntityMap.entityKeys[i], expected, actual);
        }
    }

    /** Test with a missing entity. (Missing entity should NOT be replaced.) */
    @Test
    public void testMissingEntity()
    {
        String expected = "text &thisIsNotAnEntity; more text";
        String actual = EntityMap.replaceAll(expected);

        assertEquals(expected, actual);
    }
}
