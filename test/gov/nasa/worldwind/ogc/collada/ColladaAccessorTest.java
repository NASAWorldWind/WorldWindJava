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

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.WWIO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ColladaAccessorTest
{
    @Test
    public void testFloatAccessor() throws IllegalAccessException, IOException, XMLStreamException
    {
        final String doc =
            "<COLLADA>"
                + "<source>"
                + "<float_array id=\"values\" count=\"9\">"
                + "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0"
                + "</float_array>"
                + "<technique_common>"
                + "<accessor id=\"accessor\" source=\"#values\" count=\"3\" stride=\"3\">"
                + "<param name=\"A\" type=\"float\"/>"
                + "<param name=\"F\" type=\"float\"/>"
                + "<param name=\"X\" type=\"float\"/>"
                + "</accessor>"
                + "</technique_common>"
                + "</source>"
                + "</COLLADA>";

        float[] expected = new float[] {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f};

        this.parseAndCompare(doc, expected);
    }

    @Test
    public void testOffset() throws IllegalAccessException, IOException, XMLStreamException
    {
        final String doc =
            "<COLLADA>"
                + "<source>"
                + "<float_array id=\"values\" count=\"9\">"
                + "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0"
                + "</float_array>"
                + "<technique_common>"
                + "<accessor id=\"accessor\" source=\"#values\" offset=\"3\" count=\"2\" stride=\"3\">"
                + "<param name=\"A\" type=\"float\"/>"
                + "<param name=\"F\" type=\"float\"/>"
                + "<param name=\"X\" type=\"float\"/>"
                + "</accessor>"
                + "</technique_common>"
                + "</source>"
                + "</COLLADA>";

        float[] expected = new float[] {4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f};

        this.parseAndCompare(doc, expected);
    }

    @Test
    public void testUnnamedParam() throws IllegalAccessException, IOException, XMLStreamException
    {
        final String doc =
            "<COLLADA>"
                + "<source>"
                + "<float_array id=\"values\" count=\"9\">"
                + "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0"
                + "</float_array>"
                + "<technique_common>"
                + "<accessor id=\"accessor\" source=\"#values\" count=\"3\" stride=\"3\">"
                + "<param name=\"A\" type=\"float\"/>"
                + "<param type=\"float\"/>"
                + "<param name=\"X\" type=\"float\"/>"
                + "</accessor>"
                + "</technique_common>"
                + "</source>"
                + "</COLLADA>";

        float[] expected = new float[] {1.0f, 3.0f, 4.0f, 6.0f, 7.0f, 9.0f};

        this.parseAndCompare(doc, expected);
    }

    @Test
    public void testStride() throws IllegalAccessException, IOException, XMLStreamException
    {
        final String doc =
            "<COLLADA>"
                + "<source>"
                + "<float_array id=\"values\" count=\"9\">"
                + "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0"
                + "</float_array>"
                + "<technique_common>"
                + "<accessor id=\"accessor\" source=\"#values\" count=\"3\" stride=\"3\">"
                + "<param name=\"A\" type=\"float\"/>"
                + "</accessor>"
                + "</technique_common>"
                + "</source>"
                + "</COLLADA>";

        float[] expected = new float[] {1.0f, 4.0f, 7.0f};

        this.parseAndCompare(doc, expected);
    }

    @Test
    public void testTooFewElements() throws IllegalAccessException, IOException, XMLStreamException
    {
        final String doc =
            "<COLLADA>"
                + "<source>"
                + "<float_array id=\"values\" count=\"9\">"
                + "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0"
                + "</float_array>"
                + "<technique_common>"
                + "<accessor id=\"accessor\" source=\"#values\" count=\"3\">"
                + "<param name=\"A\" type=\"float\"/>"
                + "<param name=\"F\" type=\"float\"/>"
                + "<param name=\"X\" type=\"float\"/>"
                + "</accessor>"
                + "</technique_common>"
                + "</source>"
                + "</COLLADA>";

        float[] expected = new float[] {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f, 0.0f};

        this.parseAndCompare(doc, expected);
    }

    private void parseAndCompare(String doc, float[] expected) throws XMLStreamException, IOException
    {
        ColladaRoot root = ColladaRoot.createAndParse(WWIO.getInputStreamFromString(doc));
        ColladaAccessor accessor = (ColladaAccessor) root.resolveReference("#accessor");

        float[] actual = accessor.getFloats();

        assertArrayEquals(expected, actual);
    }

    private static void assertArrayEquals(float[] expected, float[] actual)
    {
        if (expected == null)
        {
            assertNull(actual);
        }

        assertEquals(expected.length, actual.length);

        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], actual[i], 0.0);
        }
    }
}
