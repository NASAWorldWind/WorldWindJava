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
        // Pedestal equality
        assertEquals(expected.getSpacingPixels(), actual.getSpacingPixels(), 0.0, "spacingPixels");
        assertEquals(expected.getScale(), actual.getScale(), 0.0, "scale");
    }
}
