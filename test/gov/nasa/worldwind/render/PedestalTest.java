/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class PedestalTest
{
    //////////////////////////////////////////////////////////
    // Persistence Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testRestore_NewInstance()
    {
        Pedestal pedestal = new Pedestal("", null);
        assignExampleValues(pedestal);

        String stateInXml = pedestal.getRestorableState();
        pedestal = new Pedestal("", null);
        pedestal.restoreState(stateInXml);

        Pedestal expected = new Pedestal("", null);
        assignExampleValues(expected);

        assertPedestalEquals(expected, pedestal);
    }

    @Test
    public void testRestore_SameInstance()
    {
        Pedestal pedestal = new Pedestal("", null);
        assignExampleValues(pedestal);

        String stateInXml = pedestal.getRestorableState();
        assignNullValues(pedestal);
        pedestal.restoreState(stateInXml);

        Pedestal expected = new Pedestal("", null);
        assignExampleValues(expected);

        assertPedestalEquals(expected, pedestal);
    }

    @Test
    public void testRestore_EmptyStateDocument()
    {
        Pedestal pedestal = new Pedestal("", null);
        assignExampleValues(pedestal);

        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        pedestal.restoreState(emptyStateInXml);

        // No attributes should have changed.
        Pedestal expected = new Pedestal("", null);
        assignExampleValues(expected);

        assertPedestalEquals(expected, pedestal);
    }

    @Test
    public void testRestore_InvalidStateDocument()
    {
        try
        {
            String badStateInXml = "!!invalid xml string!!";
            Pedestal pedestal = new Pedestal("", null);
            pedestal.restoreState(badStateInXml);

            fail("Expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testRestore_PartialStateDocument()
    {
        Pedestal pedestal = new Pedestal("", null);
        assignNullValues(pedestal);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"highlighted\">true</stateObject>" +
                "<stateObject name=\"highlightScale\">3.141592</stateObject>" +
                "<stateObject name=\"spacingPixels\">3.5</stateObject>" +
                "<stateObject name=\"scale\">5.5</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        pedestal.restoreState(partialStateInXml);

        Pedestal expected = new Pedestal("", null);
        assignNullValues(expected);
        expected.setHighlighted(true);
        expected.setHighlightScale(3.141592);
        expected.setSpacingPixels(3.5);
        expected.setScale(5.5);

        assertPedestalEquals(expected, pedestal);
    }

    //////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////

    private static void assignExampleValues(Pedestal pedestal)
    {
        // WWIcon properties
        pedestal.setImageSource("path/to/image.ext");
        pedestal.setPosition(Position.fromDegrees(45.5, 55.5, 100.5));
        pedestal.setHighlighted(true);
        pedestal.setSize(new java.awt.Dimension(255, 255));
        pedestal.setVisible(false);
        pedestal.setHighlightScale(3.141592);
        pedestal.setToolTipText("Hello World!");
        pedestal.setToolTipFont(new Font("Arial", Font.ITALIC, 24));
        pedestal.setShowToolTip(true);
        pedestal.setToolTipTextColor(Color.MAGENTA);
        pedestal.setAlwaysOnTop(false);
        // Pedestal properties
        pedestal.setSpacingPixels(3.5);
        pedestal.setScale(5.5);
    }

    private static void assignNullValues(Pedestal pedestal)
    {
        // WWIcon properties
        pedestal.setImageSource("");
        pedestal.setPosition(null);
        pedestal.setHighlighted(false);
        pedestal.setSize(null);
        pedestal.setVisible(false);
        pedestal.setHighlightScale(0.0);
        pedestal.setToolTipText(null);
        pedestal.setToolTipFont(null);
        pedestal.setShowToolTip(false);
        pedestal.setToolTipTextColor(null);
        pedestal.setAlwaysOnTop(false);
        // Pedestal properties
        pedestal.setSpacingPixels(0.0);
        pedestal.setScale(0.0);
    }

    private static void assertPedestalEquals(Pedestal expected, Pedestal actual)
    {
        // WWIcon equality
        assertNotNull("Expected is null", expected);
        assertNotNull("Actual is null", actual);
        assertEquals("imageSource", expected.getImageSource(), actual.getImageSource());
        if (expected.getPosition() != null && actual.getPosition() != null)
        {
            assertEquals("position.latitude", expected.getPosition().getLatitude(), actual.getPosition().getLatitude());
            assertEquals("position.longitude", expected.getPosition().getLongitude(),
                actual.getPosition().getLongitude());
            assertEquals("position.elevation", expected.getPosition().getElevation(),
                actual.getPosition().getElevation(), 0.0);
        }
        else
        {
            assertNull("Expected position is not null", expected.getPosition());
            assertNull("Actual position is not null", actual.getPosition());
        }
        assertEquals("highlighted", expected.isHighlighted(), actual.isHighlighted());
        assertEquals("size", expected.getSize(), actual.getSize());
        assertEquals("visible", expected.isVisible(), actual.isVisible());
        assertEquals("highlightScale", expected.getHighlightScale(), actual.getHighlightScale(), 0.0);
        assertEquals("toolTipText", expected.getToolTipText(), actual.getToolTipText());
        assertEquals("toolTipFont", expected.getToolTipFont(), actual.getToolTipFont());
        assertEquals("showToolTip", expected.isShowToolTip(), actual.isShowToolTip());
        assertEquals("toolTipTextColor", expected.getToolTipTextColor(), actual.getToolTipTextColor());
        assertEquals("alwaysOnTop", expected.isAlwaysOnTop(), actual.isAlwaysOnTop());
        // Pedestal equality
        assertEquals("spacingPixels", expected.getSpacingPixels(), actual.getSpacingPixels(), 0.0);
        assertEquals("scale", expected.getScale(), actual.getScale(), 0.0);
    }
}
