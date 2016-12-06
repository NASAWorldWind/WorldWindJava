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
public class UserFacingIconTest
{
    //////////////////////////////////////////////////////////
    // Persistence Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testRestore_NewInstance()
    {
        UserFacingIcon icon = new UserFacingIcon("", null);
        assignExampleValues(icon);

        String stateInXml = icon.getRestorableState();
        icon = new UserFacingIcon("", null);
        icon.restoreState(stateInXml);

        UserFacingIcon expected = new UserFacingIcon("", null);
        assignExampleValues(expected);

        assertIconEquals(expected, icon);
    }

    @Test
    public void testRestore_SameInstance()
    {
        UserFacingIcon icon = new UserFacingIcon("", null);
        assignExampleValues(icon);

        String stateInXml = icon.getRestorableState();
        assignNullValues(icon);
        icon.restoreState(stateInXml);

        UserFacingIcon expected = new UserFacingIcon("", null);
        assignExampleValues(expected);

        assertIconEquals(expected, icon);
    }

    @Test
    public void testRestore_EmptyStateDocument()
    {
        UserFacingIcon icon = new UserFacingIcon("", null);
        assignExampleValues(icon);

        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        icon.restoreState(emptyStateInXml);

        // No attributes should have changed.
        UserFacingIcon expected = new UserFacingIcon("", null);
        assignExampleValues(expected);

        assertIconEquals(expected, icon);
    }

    @Test
    public void testRestore_InvalidStateDocument()
    {
        try
        {
            String badStateInXml = "!!invalid xml string!!";
            UserFacingIcon icon = new UserFacingIcon("", null);
            icon.restoreState(badStateInXml);

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
        UserFacingIcon icon = new UserFacingIcon("", null);
        assignNullValues(icon);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"highlighted\">true</stateObject>" +
                "<stateObject name=\"highlightScale\">3.141592</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        icon.restoreState(partialStateInXml);

        UserFacingIcon expected = new UserFacingIcon("", null);
        assignNullValues(expected);
        expected.setHighlighted(true);
        expected.setHighlightScale(3.141592);

        assertIconEquals(expected, icon);
    }

    //////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////

    @SuppressWarnings({"JavaDoc"})
    private static void assignExampleValues(WWIcon icon)
    {
        icon.setImageSource("path/to/image.ext");
        icon.setPosition(Position.fromDegrees(45.5, 55.5, 100.5));
        icon.setHighlighted(true);
        icon.setSize(new java.awt.Dimension(255, 255));
        icon.setVisible(false);
        icon.setHighlightScale(3.141592);
        icon.setToolTipText("Hello World!");
        icon.setToolTipFont(new Font("Arial", Font.ITALIC, 24));
        icon.setShowToolTip(true);
        icon.setToolTipTextColor(Color.MAGENTA);
        icon.setAlwaysOnTop(false);
    }

    private static void assignNullValues(WWIcon icon)
    {
        icon.setImageSource("");
        icon.setPosition(null);
        icon.setHighlighted(false);
        icon.setSize(null);
        icon.setVisible(false);
        icon.setHighlightScale(0.0);
        icon.setToolTipText(null);
        icon.setToolTipFont(null);
        icon.setShowToolTip(false);
        icon.setToolTipTextColor(null);
        icon.setAlwaysOnTop(false);
    }

    private static void assertIconEquals(WWIcon expected, WWIcon actual)
    {
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
    }
}
