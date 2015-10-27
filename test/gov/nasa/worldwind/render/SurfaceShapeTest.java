/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;

import java.util.*;

/**
 * @author tag
 * @version $Id: SurfaceShapeTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceShapeTest extends junit.framework.TestCase
{
    private static final ArrayList<LatLon> emptyLocations = new ArrayList<LatLon>();
    private static final List<LatLon> sampleLocations = Arrays.asList(
        LatLon.fromDegrees(2, 3),
        LatLon.fromDegrees(4, 9),
        LatLon.fromDegrees(8, 27));
    private static final List<LatLon> sampleLocations2 = Arrays.asList(
        LatLon.fromDegrees(18, 127),
        LatLon.fromDegrees(14, 19),
        LatLon.fromDegrees(12, 13));

    public void testSaveAndRestoreOnNewObject()
    {
        AbstractSurfaceShape shape = new SurfacePolygon(emptyLocations);
        assignExampleValues(shape);

        String stateInXml = shape.getRestorableState();
        shape = new SurfacePolygon(sampleLocations);
        shape.restoreState(stateInXml);

        AbstractSurfaceShape expected = new SurfacePolygon(emptyLocations);
        assignExampleValues(expected);

        assertEquals(expected, shape);
    }

    public void testSaveAndRestoreOnSameObject()
    {
        AbstractSurfaceShape shape = new SurfacePolygon(emptyLocations);
        assignExampleValues(shape);

        String stateInXml = shape.getRestorableState();
        assignNullValues(shape);
        shape.restoreState(stateInXml);

        AbstractSurfaceShape expected = new SurfacePolygon(emptyLocations);
        assignExampleValues(expected);

        assertEquals(expected, shape);
    }

    public void testEmptyStateDocument()
    {
        AbstractSurfaceShape shape = new SurfacePolygon(emptyLocations);
        assignExampleValues(shape);

        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        shape.restoreState(emptyStateInXml);

        // No attributes should have changed.
        AbstractSurfaceShape expected = new SurfacePolygon(emptyLocations);
        assignExampleValues(expected);

        assertEquals(expected, shape);
    }

    public void testInvalidStateDocument()
    {
        try
        {
            String badStateInXml = "!!invalid xml string!!";
            Polyline polyline = new Polyline();
            polyline.restoreState(badStateInXml);

            fail("Expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void testPartialStateDocument()
    {
        AbstractSurfaceShape shape = new SurfacePolygon(sampleLocations);
        assignNullValues(shape);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"drawBorder\">true</stateObject>" +
                "<stateObject name=\"texelsPerEdgeInterval\">10</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        shape.restoreState(partialStateInXml);

        AbstractSurfaceShape expected = new SurfacePolygon(sampleLocations);
        assignNullValues(expected);
        expected.getAttributes().setDrawOutline(true);
        expected.setTexelsPerEdgeInterval(10);

        assertEquals(expected, shape);
    }

    public void testLegacyStateDocument()
    {
        AbstractSurfaceShape shape = new SurfacePolygon(sampleLocations);
        assignNullValues(shape);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"drawBorder\">true</stateObject>" +
                "<stateObject name=\"texelsPerEdgeInterval\">10</stateObject>" +
                "<stateObject name=\"locationList\">" +
                "  <stateObject name=\"location\">" +
                "    <stateObject name=\"latitudeDegrees\">24</stateObject>" +
                "    <stateObject name=\"longitudeDegrees\">32</stateObject>" +
                "  </stateObject>" +
                "</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        shape.restoreState(partialStateInXml);

        SurfacePolygon expected = new SurfacePolygon();
        assignNullValues(expected);
        expected.setOuterBoundary(Arrays.asList(LatLon.fromDegrees(24, 32)));
        expected.getAttributes().setDrawOutline(true);
        expected.setTexelsPerEdgeInterval(10);

        assertEquals(expected, shape);
    }

    public void testSaveAndRestoreOnPolyline()
    {
        SurfacePolyline shape = new SurfacePolyline(sampleLocations);

        String stateInXml = shape.getRestorableState();
        SurfacePolyline shape2 = new SurfacePolyline(emptyLocations);
        shape2.restoreState(stateInXml);

        assertEquals(shape, shape2);
    }

    public void testSaveAndRestoreOnPolygon()
    {
        SurfacePolygon shape = new SurfacePolygon(sampleLocations);
        shape.addInnerBoundary(sampleLocations2);

        String stateInXml = shape.getRestorableState();
        SurfacePolygon shape2 = new SurfacePolygon(emptyLocations);
        shape2.restoreState(stateInXml);

        assertEquals(shape, shape2);
    }

    public void testSaveAndRestoreOnEllipse()
    {
        SurfaceEllipse shape = new SurfaceEllipse(LatLon.fromDegrees(24, 32), 6d, 5d, Angle.POS90);

        String stateInXml = shape.getRestorableState();
        SurfaceEllipse shape2 = new SurfaceEllipse(LatLon.fromDegrees(45, 52), 7d, 4d, Angle.NEG90);
        shape2.restoreState(stateInXml);

        assertEquals(shape, shape2);
    }

    public void testSaveAndRestoreOnQuad()
    {
        SurfaceQuad shape = new SurfaceQuad(LatLon.fromDegrees(24, 32), 6d, 5d, Angle.POS90);

        String stateInXml = shape.getRestorableState();
        SurfaceQuad shape2 = new SurfaceQuad(LatLon.fromDegrees(45, 52), 7d, 4d, Angle.NEG90);
        shape2.restoreState(stateInXml);

        assertEquals(shape, shape2);
    }

    public void testSaveAndRestoreOnSquare()
    {
        SurfaceSquare shape = new SurfaceSquare(LatLon.fromDegrees(24, 32), 6d);

        String stateInXml = shape.getRestorableState();
        SurfaceSquare shape2 = new SurfaceSquare(LatLon.fromDegrees(45, 52), 7d);
        shape2.restoreState(stateInXml);

        assertEquals(shape, shape2);
    }

    public void testSaveAndRestoreOnSector()
    {
        SurfaceSector shape = new SurfaceSector(Sector.fromDegrees(23, 36, 51, 87));

        String stateInXml = shape.getRestorableState();
        SurfaceSector shape2 = new SurfaceSector(Sector.fromDegrees(18, 29, 67, 79));
        shape2.restoreState(stateInXml);

        assertEquals(shape, shape2);
    }

    /*************************************************************************************************************/
    /** Helper Methods **/
    /** ************************************************************************************************ */

    @SuppressWarnings({"JavaDoc"})
    private static void assignExampleValues(AbstractSurfaceShape shape)
    {
        shape.setVisible(true);
        shape.setPathType(AVKey.RHUMB_LINE);
        shape.setTexelsPerEdgeInterval(5);
        ShapeAttributes attr = new BasicShapeAttributes();
        attr.setDrawInterior(true);
        attr.setDrawOutline(true);
        attr.setEnableAntialiasing(true);
        attr.setInteriorMaterial(Material.BLUE);
        attr.setOutlineMaterial(Material.RED);
        attr.setInteriorOpacity(0.5);
        attr.setOutlineOpacity(0.7);
        attr.setOutlineWidth(5d);
        shape.setAttributes(attr);

        if (shape instanceof SurfacePolygon) // Includes SurfacePolyline
        {
            ((SurfacePolygon) shape).setLocations(sampleLocations);
        }
        else if (shape instanceof SurfaceSector)
        {
            ((SurfaceSector) shape).setSector(Sector.boundingSector(sampleLocations));
        }
        else if (shape instanceof SurfaceEllipse)
        {
            ((SurfaceEllipse) shape).setCenter(sampleLocations.get(0));
            ((SurfaceEllipse) shape).setMajorRadius(100);
            ((SurfaceEllipse) shape).setMinorRadius(25);
            ((SurfaceEllipse) shape).setHeading(Angle.fromDegrees(30));
        }
        else if (shape instanceof SurfaceQuad)
        {
            ((SurfaceQuad) shape).setCenter(sampleLocations.get(0));
            ((SurfaceQuad) shape).setWidth(3);
            ((SurfaceQuad) shape).setHeight(200);
            ((SurfaceQuad) shape).setHeading(Angle.fromDegrees(50));
        }
    }

    private static void assignNullValues(AbstractSurfaceShape shape)
    {
        shape.setVisible(false);
        shape.setPathType("");
        shape.setTexelsPerEdgeInterval(1);
        ShapeAttributes attr = new BasicShapeAttributes();
        attr.setDrawInterior(false);
        attr.setDrawOutline(false);
        attr.setEnableAntialiasing(false);
        attr.setInteriorMaterial(Material.WHITE);
        attr.setOutlineMaterial(Material.WHITE);
        attr.setInteriorOpacity(0d);
        attr.setOutlineOpacity(0d);
        attr.setOutlineWidth(0d);
        shape.setAttributes(attr);

        if (shape instanceof SurfacePolygon) // Includes SurfacePolyline
        {
            ((SurfacePolygon) shape).setLocations(new ArrayList<LatLon>());
        }
        else if (shape instanceof SurfaceSector)
        {
            ((SurfaceSector) shape).setSector(Sector.EMPTY_SECTOR);
        }
        else if (shape instanceof SurfaceEllipse)
        {
            ((SurfaceEllipse) shape).setCenter(LatLon.ZERO);
            ((SurfaceEllipse) shape).setMajorRadius(0);
            ((SurfaceEllipse) shape).setMinorRadius(0);
            ((SurfaceEllipse) shape).setHeading(Angle.ZERO);
        }
        else if (shape instanceof SurfaceQuad)
        {
            ((SurfaceQuad) shape).setCenter(LatLon.ZERO);
            ((SurfaceQuad) shape).setWidth(0);
            ((SurfaceQuad) shape).setHeight(0);
            ((SurfaceQuad) shape).setHeading(Angle.ZERO);
        }
    }

    private static void assertEquals(AbstractSurfaceShape expected, AbstractSurfaceShape actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Actual is null", actual);
        assertEquals("class", expected.getClass(), actual.getClass());

        int[] expectedEdgeIntervals = expected.getMinAndMaxEdgeIntervals();
        int[] actualEdgeIntervals = actual.getMinAndMaxEdgeIntervals();

        assertEquals("isVisible", expected.isVisible(), actual.isVisible());
        assertEquals("attributes", expected.getAttributes(), actual.getAttributes());
        assertEquals("pathType", expected.getPathType(), actual.getPathType());
        assertEquals("texelsPerEdgeInterval", expected.getTexelsPerEdgeInterval(), actual.getTexelsPerEdgeInterval());
        assertEquals("minEdgeIntervals", expectedEdgeIntervals[0], actualEdgeIntervals[0]);
        assertEquals("maxEdgeIntervals", expectedEdgeIntervals[1], actualEdgeIntervals[1]);

        if (expected instanceof SurfacePolyline)
        {
            assertEquals("locations", ((SurfacePolyline) expected).getLocations(),
                ((SurfacePolyline) actual).getLocations());
        }
        else if (expected instanceof SurfacePolygon)
        {
            assertEquals("boundaries", ((SurfacePolygon) expected).boundaries, ((SurfacePolygon) actual).boundaries);
        }
        else if (expected instanceof SurfaceSector)
        {
            assertEquals("sector", ((SurfaceSector) expected).getSector(), ((SurfaceSector) actual).getSector());
        }
        else if (expected instanceof SurfaceEllipse)
        {
            assertEquals("center", ((SurfaceEllipse) expected).getCenter(), ((SurfaceEllipse) actual).getCenter());
            assertEquals("majorRadius", ((SurfaceEllipse) expected).getMajorRadius(),
                ((SurfaceEllipse) actual).getMajorRadius());
            assertEquals("minorRadius", ((SurfaceEllipse) expected).getMinorRadius(),
                ((SurfaceEllipse) actual).getMinorRadius());
            assertEquals("heading", ((SurfaceEllipse) expected).getHeading(), ((SurfaceEllipse) actual).getHeading());
        }
        else if (expected instanceof SurfaceQuad)
        {
            assertEquals("center", ((SurfaceQuad) expected).getCenter(), ((SurfaceQuad) actual).getCenter());
            assertEquals("width", ((SurfaceQuad) expected).getWidth(), ((SurfaceQuad) actual).getWidth());
            assertEquals("height", ((SurfaceQuad) expected).getHeight(), ((SurfaceQuad) actual).getHeight());
            assertEquals("heading", ((SurfaceQuad) expected).getHeading(), ((SurfaceQuad) actual).getHeading());
        }
    }

    protected static void assertEquals(String name, Iterable<? extends LatLon> expected,
        Iterable<? extends LatLon> actual)
    {
        Iterator<? extends LatLon> a = expected.iterator();
        Iterator<? extends LatLon> b = actual.iterator();

        while (a.hasNext() && b.hasNext())
        {
            assertEquals(name, a.next(), b.next());
        }

        if (a.hasNext() || b.hasNext())
        {
            fail();
        }
    }

    public static void main(String[] args)
    {
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(PolylineTest.class));
    }
}
