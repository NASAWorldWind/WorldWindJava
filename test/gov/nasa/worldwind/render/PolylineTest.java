/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

import java.awt.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: PolylineTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PolylineTest extends junit.framework.TestCase
{
    /*************************************************************************************************************/
    /** Persistence Tests **/
    /** ******************************************************************************************************** */

    public void testSaveAndRestoreOnNewObject_Polyline()
    {
        Polyline polyline = new Polyline();
        assignExampleValues(polyline);

        String stateInXml = polyline.getRestorableState();
        polyline = new Polyline();
        polyline.restoreState(stateInXml);

        Polyline expected = new Polyline();
        assignExampleValues(expected);

        assertEquals(expected, polyline);
    }

    public void testSaveAndRestoreOnSameObject_Polyline()
    {
        Polyline polyline = new Polyline();
        assignExampleValues(polyline);

        String stateInXml = polyline.getRestorableState();
        assignNullValues(polyline);
        polyline.restoreState(stateInXml);

        Polyline expected = new Polyline();
        assignExampleValues(expected);

        assertEquals(expected, polyline);
    }

    public void testEmptyStateDocument_Polyline()
    {
        Polyline polyline = new Polyline();
        assignExampleValues(polyline);

        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        polyline.restoreState(emptyStateInXml);

        // No attributes should have changed.
        Polyline expected = new Polyline();
        assignExampleValues(expected);

        assertEquals(expected, polyline);
    }

    public void testInvalidStateDocument_Polyline()
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

    public void testPartialStateDocument_Polyline()
    {
        Polyline polyline = new Polyline();
        assignNullValues(polyline);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"filled\">true</stateObject>" +
                "<stateObject name=\"offset\">1000.5</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        polyline.restoreState(partialStateInXml);

        Polyline expected = new Polyline();
        assignNullValues(expected);
        expected.setFilled(true);
        expected.setOffset(1000.5);

        assertEquals(expected, polyline);
    }

    /*************************************************************************************************************/
    /** Helper Methods **/
    /** ******************************************************************************************************** */

    @SuppressWarnings({"JavaDoc"})
    private static void assignExampleValues(Polyline polyline)
    {
        polyline.setColor(Color.MAGENTA);
        polyline.setHighlightColor(Color.CYAN);
        polyline.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
        polyline.setFilled(true);
        polyline.setClosed(true);
        polyline.setHighlighted(true);
        polyline.setPathType(Polyline.GREAT_CIRCLE);
        polyline.setFollowTerrain(true);
        polyline.setOffset(10.5);
        polyline.setTerrainConformance(0.5);
        polyline.setLineWidth(3.5);
        polyline.setStipplePattern((short) 0xfc0c);
        polyline.setStippleFactor(128);
        polyline.setNumSubsegments(100);
        polyline.setPositions(Arrays.asList(
            Position.fromDegrees(2, 3, 0.5),
            Position.fromDegrees(4, 9, 111.5),
            Position.fromDegrees(8, 27, 222.5)));
    }

    private static void assignNullValues(Polyline polyline)
    {
        polyline.setColor(Color.WHITE);
        polyline.setAntiAliasHint(Polyline.ANTIALIAS_DONT_CARE);
        polyline.setFilled(false);
        polyline.setClosed(false);
        polyline.setPathType(Polyline.LINEAR);
        polyline.setFollowTerrain(false);
        polyline.setOffset(0.0);
        polyline.setTerrainConformance(1.0);
        polyline.setLineWidth(1.0);
        polyline.setStipplePattern((short) 0xffff);
        polyline.setStippleFactor(1);
        polyline.setNumSubsegments(1);
        polyline.setPositions(new ArrayList<Position>());
    }

    private static void assertEquals(Polyline expected, Polyline actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Actual is null", actual);

        assertEquals("color", expected.getColor(), actual.getColor());
        assertEquals("highlightColor", expected.getColor(), actual.getColor());
        assertEquals("antiAliasHint", expected.getAntiAliasHint(), actual.getAntiAliasHint());
        assertEquals("filled", expected.isFilled(), actual.isFilled());
        assertEquals("closed", expected.isClosed(), actual.isClosed());
        assertEquals("highlighted", expected.isFilled(), actual.isFilled());
        assertEquals("pathType", expected.getPathType(), actual.getPathType());
        assertEquals("followTerrain", expected.isFollowTerrain(), actual.isFollowTerrain());
        assertEquals("offset", expected.getOffset(), actual.getOffset());
        assertEquals("terrainConformance", expected.getTerrainConformance(), actual.getTerrainConformance());
        assertEquals("lineWidth", expected.getLineWidth(), actual.getLineWidth());
        assertEquals("stipplePattern", expected.getStipplePattern(), actual.getStipplePattern());
        assertEquals("stippleFactor", expected.getStippleFactor(), actual.getStippleFactor());
        assertEquals("numSubsegments", expected.getNumSubsegments(), actual.getNumSubsegments());
        // Position does not override equals(), so we must compare the contents of "positions" ourselves.
        Iterator<Position> expectedPositions = expected.getPositions().iterator();
        Iterator<Position> actualPositions = actual.getPositions().iterator();
        while (expectedPositions.hasNext() && actualPositions.hasNext())
        {
            Position expectedPos = expectedPositions.next(), actualPos = actualPositions.next();
            assertEquals("positions.i.latitude", expectedPos.getLatitude(), actualPos.getLatitude());
            assertEquals("positions.i.longitude", expectedPos.getLongitude(), actualPos.getLongitude());
            assertEquals("positions.i.elevation", expectedPos.getElevation(), actualPos.getElevation());
        }
        // If either iterator has more elements, then their lengths are different.
        assertFalse("positions.length", expectedPositions.hasNext() || actualPositions.hasNext());
    }

    public static void main(String[] args)
    {
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(PolylineTest.class));
    }
}
