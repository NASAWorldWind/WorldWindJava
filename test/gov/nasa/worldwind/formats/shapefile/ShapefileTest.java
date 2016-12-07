/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.net.*;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ShapefileTest
{
    private static final String STATE_BOUNDS_PATH = "testData/shapefiles/state_bounds.shp";
    private static final String WORLD_BORDERS_PATH = "testData/shapefiles/TM_WORLD_BORDERS-0.3.shp";
    private static final String SPRINGFIELD_URBAN_GROWTH_PATH = "testData/shapefiles/SPR_UGB.shp";

    //////////////////////////////////////////////////////////
    // Test Basic Reading
    //////////////////////////////////////////////////////////

    @Test
    public void testOpenFile()
    {
        Shapefile shapefile = new Shapefile(new File(STATE_BOUNDS_PATH));
        assertEquals("Shape type is not as expected", Shapefile.SHAPE_POLYLINE, shapefile.getShapeType());

        while (shapefile.hasNext())
        {
            assertRecordAppearsNormal(shapefile, shapefile.nextRecord());
        }

        shapefile.close();
    }

    @Test
    public void testOpenPath()
    {
        Shapefile shapefile = new Shapefile(STATE_BOUNDS_PATH);
        assertEquals("Shape type is not as expected", Shapefile.SHAPE_POLYLINE, shapefile.getShapeType());

        while (shapefile.hasNext())
        {
            assertRecordAppearsNormal(shapefile, shapefile.nextRecord());
        }

        shapefile.close();
    }

//    @Test
//    public void testOpenURL() throws MalformedURLException
//    {
//        Shapefile shapefile = new Shapefile(new URL(SPRINGFIELD_URBAN_GROWTH_URL));
//        assertEquals("Shape type is not as expected", Shapefile.SHAPE_POLYGON, shapefile.getShapeType());
//
//        while (shapefile.hasNext())
//        {
//            assertRecordAppearsNormal(shapefile, shapefile.nextRecord());
//        }
//
//        shapefile.close();
//    }
//
//    @Test
//    public void testOpenURLString()
//    {
//        Shapefile shapefile = new Shapefile(SPRINGFIELD_URBAN_GROWTH_URL);
//        assertEquals("Shape type is not as expected", Shapefile.SHAPE_POLYGON, shapefile.getShapeType());
//
//        while (shapefile.hasNext())
//        {
//            assertRecordAppearsNormal(shapefile, shapefile.nextRecord());
//        }
//
//        shapefile.close();
//    }

    @Test
    public void testOpenSingleInputStream() throws Exception
    {
        Shapefile shapefile = new Shapefile(WWIO.openStream(STATE_BOUNDS_PATH));
        assertEquals("Shape type is not as expected", Shapefile.SHAPE_POLYLINE, shapefile.getShapeType());

        while (shapefile.hasNext())
        {
            assertRecordAppearsNormal(shapefile, shapefile.nextRecord());
        }

        shapefile.close();
    }

    @Test
    public void testOpenMultipleInputStreams() throws Exception
    {
        Shapefile shapefile = new Shapefile(
            WWIO.openStream(STATE_BOUNDS_PATH),
            WWIO.openStream(WWIO.replaceSuffix(STATE_BOUNDS_PATH, ".shx")),
            WWIO.openStream(WWIO.replaceSuffix(STATE_BOUNDS_PATH, ".dbf")),
            WWIO.openStream(WWIO.replaceSuffix(STATE_BOUNDS_PATH, ".prj")));
        assertEquals("Shape type is not as expected", shapefile.getShapeType(), Shapefile.SHAPE_POLYLINE);

        while (shapefile.hasNext())
        {
            assertRecordAppearsNormal(shapefile, shapefile.nextRecord());
        }

        shapefile.close();
    }

    //////////////////////////////////////////////////////////
    // Test Coordinate Conversion
    //////////////////////////////////////////////////////////

    @Test
    public void testUTMCoordinates()
    {
        Shapefile shapefile = new Shapefile(SPRINGFIELD_URBAN_GROWTH_PATH);
        assertEquals("Shape type is not as expected", Shapefile.SHAPE_POLYGON, shapefile.getShapeType());
        assertShapefileAppearsNormal(shapefile);
        shapefile.close();
    }

    @Test
    public void testGeographicCoordinates()
    {
        Shapefile shapefile = new Shapefile(WORLD_BORDERS_PATH);
        assertEquals("Shape type is not as expected", Shapefile.SHAPE_POLYGON, shapefile.getShapeType());
        assertShapefileAppearsNormal(shapefile);
        shapefile.close();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Test
    public void testUnsupportedCoordinates() throws Exception
    {
        AVList params = new AVListImpl();
        params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);

        try
        {
            Shapefile shapefile = new Shapefile(WWIO.openStream(STATE_BOUNDS_PATH), null, null, params);
            fail("Unknown coordinate system not detected.");
        }
        catch (WWRuntimeException e)
        {
            // WWRuntimeException expected from unknown coordinate system.
        }
    }

    //////////////////////////////////////////////////////////
    // Test Expected Values
    //////////////////////////////////////////////////////////

    @Test
    public void testExpectedValuesForStateBounds()
    {
        Shapefile shapefile = new Shapefile(STATE_BOUNDS_PATH);
        assertEquals("Version not as expected", 1000, shapefile.getVersion());
        assertEquals("Length not as expected", 2750692, shapefile.getLength());
        assertEquals("Shape type not as expected", Shapefile.SHAPE_POLYLINE, shapefile.getShapeType());
        assertEquals("Number of records not as expected", 19, shapefile.getNumberOfRecords());
        assertTrue("Bounds not as expected", Arrays.equals(
            new double[] {25.837377, 49.384359, -124.211606, -67.158958},
            shapefile.getBoundingRectangle()));

        while (shapefile.hasNext())
        {
            ShapefileRecord record = shapefile.nextRecord();
            assertRecordAppearsNormal(shapefile, record);

            if (record.getRecordNumber() != 19)
                continue;

            assertTrue("Record type not as expected", Shapefile.isPolylineType(record.getShapeType()));
            assertEquals("Record number of parts not as expected", 1, record.getNumberOfParts());
            assertEquals("Record number of points not as expected", 10, record.getNumberOfPoints());
            assertEquals("Record first part number not as expected", 64, record.getFirstPartNumber());
            assertTrue("Record bounds not as expected", Arrays.equals(
                new double[] {39.5345, 39.53649, -75.530616, -75.527447},
                record.getBoundingRectangle()));

            assertEquals("Record point not as expected", LatLon.fromDegrees(39.53649, -75.530616),
                record.getPointBuffer(0).getLocation(0));

            assertNotNull("Record attributes is null", record.getAttributes());
            assertEquals("Record attribute not as expected", 912L, record.getAttributes().getValue("ID"));
            assertEquals("Record attribute not as expected", 0.004, record.getAttributes().getValue("LENGTH"));
        }

        shapefile.close();
    }

    //////////////////////////////////////////////////////////
    // Utilities
    //////////////////////////////////////////////////////////

    public static void assertShapefileAppearsNormal(Shapefile shapefile)
    {
        double[] rect = shapefile.getBoundingRectangle();
        assertBoundingRectangleAppearsGeographic("Shapefile bounds not geographic", rect);

        while (shapefile.hasNext())
        {
            ShapefileRecord record = shapefile.nextRecord();
            assertRecordAppearsNormal(shapefile, record);
            assertTrue("Record type not Polygon", Shapefile.isPolygonType(record.getShapeType()));

            rect = record.getBoundingRectangle();
            assertCoordAppearsGeographic("Record bounds not geographic", rect[2], rect[0]);
            assertCoordAppearsGeographic("Record bounds not geographic", rect[3], rect[1]);

            for (double[] coord : record.getCompoundPointBuffer().getCoords())
            {
                assertCoordAppearsGeographic("Record point not geographic", coord[0], coord[1]);
            }
        }
    }

    public static void assertRecordAppearsNormal(Shapefile shapefile, ShapefileRecord record)
    {
        assertNotNull("Record is null", record);
        assertSame("Record shapefile is not as expected", shapefile, record.getShapeFile());
        assertTrue("Record has no parts", record.getNumberOfParts() > 0);
        assertTrue("Record has no points", record.getNumberOfPoints() > 0);
        assertFalse("Record has no type", WWUtil.isEmpty(record.getShapeType()));

        if (Shapefile.isNullType(record.getShapeType()))
            assertTrue("Record type is not as expected", record instanceof ShapefileRecordNull);

        else if (Shapefile.isPointType(record.getShapeType()))
            assertTrue("Record type is not as expected", record instanceof ShapefileRecordPoint);

        else if (Shapefile.isMultiPointType(record.getShapeType()))
            assertTrue("Record type is not as expected", record instanceof ShapefileRecordMultiPoint);

        else if (Shapefile.isPolylineType(record.getShapeType()))
            assertTrue("Record type is not as expected", record instanceof ShapefileRecordPolyline);

        else if (Shapefile.isPolygonType(record.getShapeType()))
            assertTrue("Record type is not as expected", record instanceof ShapefileRecordPolygon);

        int expectedNumPoints = record.getNumberOfPoints();
        int actualNumPoints = 0; // Accumulated in the loop below.

        for (int i = 0; i < record.getNumberOfParts(); i++)
        {
            assertNotNull("Record point buffer is null", record.getPointBuffer(i));
            actualNumPoints += record.getPointBuffer(i).getSize();
        }

        assertEquals("Record num points is not as expected", expectedNumPoints, actualNumPoints);
        assertNotNull("Record compound point buffer is null", record.getCompoundPointBuffer());
    }

    public static void assertBoundingRectangleAppearsGeographic(String message, double[] coords)
    {
        assertTrue(message, Angle.isValidLatitude(coords[0]));
        assertTrue(message, Angle.isValidLatitude(coords[1]));
        assertTrue(message, Angle.isValidLongitude(coords[2]));
        assertTrue(message, Angle.isValidLongitude(coords[3]));
    }

    public static void assertCoordAppearsGeographic(String message, double x, double y)
    {
        assertTrue(message, Angle.isValidLongitude(x));
        assertTrue(message, Angle.isValidLatitude(y));
    }
}
