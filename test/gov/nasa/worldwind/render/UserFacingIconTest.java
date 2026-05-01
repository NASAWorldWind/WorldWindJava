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
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
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
        assertNotNull(expected, "Expected is null");
        assertNotNull(actual, "Actual is null");
        assertEquals(expected.getImageSource(), actual.getImageSource(), "imageSource");
        if (expected.getPosition() != null && actual.getPosition() != null)
        {
            assertEquals(expected.getPosition().getLatitude(), actual.getPosition().getLatitude(), "position.latitude");
            assertEquals(expected.getPosition().getLongitude(), actual.getPosition().getLongitude(), "position.longitude");
            assertEquals(expected.getPosition().getElevation(), actual.getPosition().getElevation(), 0.0, "position.elevation");
        }
        else
        {
            assertNull(expected.getPosition(), "Expected position is not null");
            assertNull(actual.getPosition(), "Actual position is not null");
        }
        assertEquals(expected.isHighlighted(), actual.isHighlighted(), "highlighted");
        assertEquals(expected.getSize(), actual.getSize(), "size");
        assertEquals(expected.isVisible(), actual.isVisible(), "visible");
        assertEquals(expected.getHighlightScale(), actual.getHighlightScale(), 0.0, "highlightScale");
        assertEquals(expected.getToolTipText(), actual.getToolTipText(), "toolTipText");
        assertEquals(expected.getToolTipFont(), actual.getToolTipFont(), "toolTipFont");
        assertEquals(expected.isShowToolTip(), actual.isShowToolTip(), "showToolTip");
        assertEquals(expected.getToolTipTextColor(), actual.getToolTipTextColor(), "toolTipTextColor");
        assertEquals(expected.isAlwaysOnTop(), actual.isAlwaysOnTop(), "alwaysOnTop");
    }
}
