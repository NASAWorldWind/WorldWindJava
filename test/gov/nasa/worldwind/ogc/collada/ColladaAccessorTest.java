/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.WWIO;
import junit.framework.TestCase;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author pabercrombie
 * @version $Id: ColladaAccessorTest.java 650 2012-06-20 22:20:59Z pabercrombie $
 */
public class ColladaAccessorTest extends TestCase
{
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

        float[] expected = new float[] {4.0f, 5.0f, 6.0f, 7.0f, 8.0f};

        this.parseAndCompare(doc, expected);
    }

    protected void parseAndCompare(String doc, float[] expected) throws XMLStreamException, IOException
    {
        ColladaRoot root = ColladaRoot.createAndParse(WWIO.getInputStreamFromString(doc));
        ColladaAccessor accessor = (ColladaAccessor) root.resolveReference("#accessor");

        float[] actual = accessor.getFloats();

        assertArrayEquals(expected, actual);
    }

    protected boolean assertArrayEquals(float[] expected, float[] actual)
    {
        if (expected == null)
            return actual == null;

        if (expected.length != actual.length)
            return false;

        for (int i = 0; i < expected.length; i++)
        {
            if (expected[i] != actual[i])
                return false;
        }
        return true;
    }
}
