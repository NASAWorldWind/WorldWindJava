/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.symbology.IconRetriever;
import org.junit.Assert;

import java.awt.image.*;

/**
 * @author pabercrombie
 * @version $Id: PointGraphicRetrievalTest.java 563 2012-04-26 18:16:22Z pabercrombie $
 */
public class PointGraphicRetrievalTest
{
    // This path should correspond to the location of the appropriate symbology source icons on your system
    private final static String ICON_RETRIEVER_PATH = Configuration.getStringValue(
        AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH, MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);

    /** Valid status characters for MIL-STD-2525C tactical graphics (see Table B-I, pg. 305). */
    protected static final char[] ALL_STATUS = {'A', 'S', 'P', 'K'};

    //////////////////////////////////////////////////////////
    // Test retrieval of a MilStd2525 point graphic from both a remote
    // server and the local file system.
    //////////////////////////////////////////////////////////

    @org.junit.Test
    public void testServerRetrieval()
    {
        IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
        BufferedImage img = symGen.createIcon("GFFPPCB-------X", null);
        Assert.assertNotNull(img);
    }

    //////////////////////////////////////////////////////////
    // Test parsing of the Symbol Code.
    // MilStd2525 SymCodes should be exactly 15 characters.
    //////////////////////////////////////////////////////////

    @org.junit.Test
    public void testParseCodeTooShort()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
            symGen.createIcon("SUAPC", null);
            Assert.fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
        }
    }

    @org.junit.Test
    public void testParseCodeTooLong()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
            symGen.createIcon("SUAPCTEST", null);
            Assert.fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
        }
    }

    @org.junit.Test
    public void testParseNullCode()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
            symGen.createIcon(null, null);
            Assert.fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
        }
    }

    //////////////////////////////////////////////////////////
    // Test validity of Symbol Code.
    // Codes containing invalid letters should retrieve a null image.
    // TODO: is this correct?
    //////////////////////////////////////////////////////////

    @org.junit.Test
    public void testInvalidCodingScheme()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
            symGen.createIcon(".FFPPCB-------X", null);
            Assert.fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
        }
    }

    @org.junit.Test
    public void testInvalidStandardIdentity()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
            symGen.createIcon("G.FPPCB-------X", null);
            Assert.fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
        }
    }

    @org.junit.Test
    public void testInvalidStatus()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
            symGen.createIcon("GFF.PCB-------X", null);
            Assert.fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
        }
    }

    @org.junit.Test
    public void testInvalidFunctionID()
    {
        try
        {
            IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
            symGen.createIcon("GFFP...-------X", null);
            Assert.fail("Should raise an IllegalArgumentException");
        }
        catch (Exception e)
        {
        }
    }

    /** Test for the presence and retrieval of a every tactical point graphic */
    @org.junit.Test
    public void testTacticalGraphicRetrieval()
    {
        IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);

        for (String s : MilStd2525PointGraphic.getTacGrpGraphics())
        {
            StringBuilder sidc = new StringBuilder(s);

            for (char status : ALL_STATUS)
            {
                sidc.setCharAt(1, 'F'); // Standard identity: friendly
                sidc.setCharAt(3, status);

                BufferedImage img = symGen.createIcon(sidc.toString(), null);
                Assert.assertNotNull("Icon " + s.toLowerCase() + "-----.png not found.", img);
            }
        }
    }

    /*
     * Test for the presence and retrieval of a every possible Meteorological point graphic
     */
    @org.junit.Test
    public void testMeteorologicalSymbolRetrieval()
    {
        IconRetriever symGen = new MilStd2525PointGraphicRetriever(ICON_RETRIEVER_PATH);
        AVList params = new AVListImpl();
        BufferedImage img;

        for (String s : MilStd2525PointGraphic.getMetocGraphics())
        {
            img = symGen.createIcon(s, params);
            Assert.assertNotNull("Icon " + s.toLowerCase() + ".png not found.", img);
        }
    }

    public static void main(String[] args)
    {
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(PointGraphicRetrievalTest.class));
    }
}
