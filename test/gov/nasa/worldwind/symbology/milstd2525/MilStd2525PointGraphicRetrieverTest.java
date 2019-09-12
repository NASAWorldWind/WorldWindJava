/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.symbology.IconRetriever;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.image.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class MilStd2525PointGraphicRetrieverTest
{
    // This path should correspond to the location of the appropriate symbology source icons on your system
    private final static String LOCAL_SYMBOLS_ZIP = "jar:file:testData/milstd2525-symbols.zip!";

    /** Valid status characters for MIL-STD-2525C tactical graphics (see Table B-I, pg. 305). */
    private static final char[] ALL_STATUS = {'A', 'S', 'P', 'K'};

    //////////////////////////////////////////////////////////
    // Test parsing of the Symbol Code.
    // MilStd2525 SymCodes should be exactly 15 characters.
    //////////////////////////////////////////////////////////

    @Test
    public void testParseCodeTooShort()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
            symGen.createIcon("SUAPC", null);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseCodeTooLong()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
            symGen.createIcon("SUAPCTEST", null);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseNullCode()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
            symGen.createIcon(null, null);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////
    // Test validity of Symbol Code.
    // Codes containing invalid letters should retrieve a null image.
    // TODO: is this correct?
    //////////////////////////////////////////////////////////

    @Test
    public void testInvalidCodingScheme()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
            symGen.createIcon(".FFPPCB-------X", null);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testInvalidStandardIdentity()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
            symGen.createIcon("G.FPPCB-------X", null);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testInvalidStatus()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
            symGen.createIcon("GFF.PCB-------X", null);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testInvalidFunctionID()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
            symGen.createIcon("GFFP...-------X", null);
            fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Test for the presence and retrieval of a every tactical point graphic */
    @Test
    public void testTacticalGraphicRetrieval()
    {
        IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);

        for (String s : MilStd2525PointGraphic.getTacGrpGraphics())
        {
            StringBuilder sidc = new StringBuilder(s);

            for (char status : ALL_STATUS)
            {
                sidc.setCharAt(1, 'F'); // Standard identity: friendly
                sidc.setCharAt(3, status);

                BufferedImage img = symGen.createIcon(sidc.toString(), null);
                assertNotNull("Icon " + s.toLowerCase() + "-----.png not found.", img);
            }
        }
    }

    /*
     * Test for the presence and retrieval of a every possible Meteorological point graphic
     */
    @Test
    public void testMeteorologicalSymbolRetrieval()
    {
        IconRetriever symGen = new MilStd2525PointGraphicRetriever(LOCAL_SYMBOLS_ZIP);
        AVList params = new AVListImpl();
        BufferedImage img;

        for (String s : MilStd2525PointGraphic.getMetocGraphics())
        {
            img = symGen.createIcon(s, params);
            assertNotNull("Icon " + s.toLowerCase() + ".png not found.", img);
        }
    }
}
