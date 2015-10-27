/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.UnitsFormat;
import junit.framework.TestCase;

/**
 * Unit test for MilStd2525UnitsFormat.
 *
 * @author pabercrombie
 * @version $Id: MilStd2525UnitsFormatTest.java 482 2012-03-27 01:27:15Z pabercrombie $
 */
public class MilStd2525UnitsFormatTest
{
    @org.junit.Test
    public void testNE() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(23.751472222222223, 117.4025);

        TestCase.assertEquals("234505.3N1172409.0E", format.latLon(ll));
    }

    @org.junit.Test
    public void testSE() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(-12.203364444444444, 5.084722222222222);

        TestCase.assertEquals("121212.1S0050505.0E", format.latLon(ll));
    }

    @org.junit.Test
    public void testNW() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(90.98333333333333, -179.98672222222223);

        TestCase.assertEquals("905900.0N1795912.2W", format.latLon(ll));
    }

    @org.junit.Test
    public void testSW() throws IllegalAccessException
    {
        UnitsFormat format = new MilStd2525UnitsFormat();
        LatLon ll = LatLon.fromDegrees(-45.01663888888889, -70.75225277777778);

        TestCase.assertEquals("450059.9S0704508.1W", format.latLon(ll));
    }

    public static void main(String[] args)
    {
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(MilStd2525UnitsFormatTest.class));
    }
}
