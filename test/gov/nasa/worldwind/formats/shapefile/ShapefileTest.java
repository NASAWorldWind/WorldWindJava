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
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
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
        assertEquals(Shapefile.SHAPE_POLYLINE, shapefile.getShapeType(), "Shape type is not as expected");

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
        assertEquals(Shapefile.SHAPE_POLYLINE, shapefile.getShapeType(), "Shape type is not as expected");

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
//        assertEquals(Shapefile.SHAPE_POLYGON, shapefile.getShapeType(), "Shape type is not as expected");
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
//        assertEquals(Shapefile.SHAPE_POLYGON, shapefile.getShapeType(), "Shape type is not as expected");
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
        assertEquals(Shapefile.SHAPE_POLYLINE, shapefile.getShapeType(), "Shape type is not as expected");

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
        assertEquals(shapefile.getShapeType(), Shapefile.SHAPE_POLYLINE, "Shape type is not as expected");

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
        assertEquals(Shapefile.SHAPE_POLYGON, shapefile.getShapeType(), "Shape type is not as expected");
        assertShapefileAppearsNormal(shapefile);
        shapefile.close();
    }

    @Test
    public void testGeographicCoordinates()
    {
        Shapefile shapefile = new Shapefile(WORLD_BORDERS_PATH);
        assertEquals(Shapefile.SHAPE_POLYGON, shapefile.getShapeType(), "Shape type is not as expected");
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
        assertEquals(1000, shapefile.getVersion(), "Version not as expected");
        assertEquals(2750692, shapefile.getLength(), "Length not as expected");
        assertEquals(Shapefile.SHAPE_POLYLINE, shapefile.getShapeType(), "Shape type not as expected");
        assertEquals(19, shapefile.getNumberOfRecords(), "Number of records not as expected");
        assertTrue(Arrays.equals(
            new double[] {25.837377, 49.384359, -124.211606, -67.158958},
            shapefile.getBoundingRectangle()), "Bounds not as expected");

        while (shapefile.hasNext())
        {
            ShapefileRecord record = shapefile.nextRecord();
            assertRecordAppearsNormal(shapefile, record);

            if (record.getRecordNumber() != 19)
                continue;

            assertTrue(Shapefile.isPolylineType(record.getShapeType()), "Record type not as expected");
            assertEquals(1, record.getNumberOfParts(), "Record number of parts not as expected");
            assertEquals(10, record.getNumberOfPoints(), "Record number of points not as expected");
            assertEquals(64, record.getFirstPartNumber(), "Record first part number not as expected");
            assertTrue(Arrays.equals(
                new double[] {39.5345, 39.53649, -75.530616, -75.527447},
                record.getBoundingRectangle()), "Record bounds not as expected");

            assertEquals(LatLon.fromDegrees(39.53649, -75.530616), record.getPointBuffer(0).getLocation(0), "Record point not as expected");

            assertNotNull(record.getAttributes(), "Record attributes is null");
            assertEquals(912L, record.getAttributes().getValue("ID"), "Record attribute not as expected");
            assertEquals(0.004, record.getAttributes().getValue("LENGTH"), "Record attribute not as expected");
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
            assertTrue(Shapefile.isPolygonType(record.getShapeType()), "Record type not Polygon");

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
        assertNotNull(record, "Record is null");
        assertSame(shapefile, record.getShapeFile(), "Record shapefile is not as expected");
        assertTrue(record.getNumberOfParts() > 0, "Record has no parts");
        assertTrue(record.getNumberOfPoints() > 0, "Record has no points");
        assertFalse(WWUtil.isEmpty(record.getShapeType()), "Record has no type");

        if (Shapefile.isNullType(record.getShapeType()))
            assertTrue(record instanceof ShapefileRecordNull, "Record type is not as expected");

        else if (Shapefile.isPointType(record.getShapeType()))
            assertTrue(record instanceof ShapefileRecordPoint, "Record type is not as expected");

        else if (Shapefile.isMultiPointType(record.getShapeType()))
            assertTrue(record instanceof ShapefileRecordMultiPoint, "Record type is not as expected");

        else if (Shapefile.isPolylineType(record.getShapeType()))
            assertTrue(record instanceof ShapefileRecordPolyline, "Record type is not as expected");

        else if (Shapefile.isPolygonType(record.getShapeType()))
            assertTrue(record instanceof ShapefileRecordPolygon, "Record type is not as expected");

        int expectedNumPoints = record.getNumberOfPoints();
        int actualNumPoints = 0; // Accumulated in the loop below.

        for (int i = 0; i < record.getNumberOfParts(); i++)
        {
            assertNotNull(record.getPointBuffer(i), "Record point buffer is null");
            actualNumPoints += record.getPointBuffer(i).getSize();
        }

        assertEquals(expectedNumPoints, actualNumPoints, "Record num points is not as expected");
        assertNotNull(record.getCompoundPointBuffer(), "Record compound point buffer is null");
    }

    public static void assertBoundingRectangleAppearsGeographic(String message, double[] coords)
    {
        assertTrue(Angle.isValidLatitude(coords[0]), message);
        assertTrue(Angle.isValidLatitude(coords[1]), message);
        assertTrue(Angle.isValidLongitude(coords[2]), message);
        assertTrue(Angle.isValidLongitude(coords[3]), message);
    }

    public static void assertCoordAppearsGeographic(String message, double x, double y)
    {
        assertTrue(Angle.isValidLongitude(x), message);
        assertTrue(Angle.isValidLatitude(y), message);
    }
}
