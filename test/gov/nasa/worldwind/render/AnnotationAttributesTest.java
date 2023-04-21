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

import gov.nasa.worldwind.avlist.AVKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class AnnotationAttributesTest
{
    //////////////////////////////////////////////////////////
    // Persistence Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testRestore_NewInstance()
    {
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignExampleValues(attrib);

        String stateInXml = attrib.getRestorableState();
        attrib = new AnnotationAttributes();
        attrib.restoreState(stateInXml);

        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);

        assertAnnotationAttributesEquals(expected, attrib);
    }

    @Test
    public void testRestore_SameInstance()
    {
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignExampleValues(attrib);

        String stateInXml = attrib.getRestorableState();
        assignNullValues(attrib);
        attrib.restoreState(stateInXml);

        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);

        assertAnnotationAttributesEquals(expected, attrib);
    }

    @Test
    public void testRestore_EmptyStateDocument()
    {
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignExampleValues(attrib);

        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        attrib.restoreState(emptyStateInXml);

        // No attributes should have changed.        
        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);

        assertAnnotationAttributesEquals(expected, attrib);
    }

    @Test
    public void testRestore_InvalidStateDocument()
    {
        try
        {
            String badStateInXml = "!!invalid xml string!!";
            AnnotationAttributes attrib = new AnnotationAttributes();
            attrib.restoreState(badStateInXml);

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
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignExampleValues(attrib);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"scale\">10.5</stateObject>" +
                "<stateObject name=\"cornerRadius\">11</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        attrib.restoreState(partialStateInXml);

        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);
        expected.setScale(10.5);
        expected.setCornerRadius(11);

        assertAnnotationAttributesEquals(expected, attrib);
    }

    @Test
    public void testRestore_LegacyStateDocument()
    {
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignExampleValues(attrib);
        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"textAlign\">2</stateObject>" +
                "<stateObject name=\"imageRepeat\">render.Annotation.RepeatXY</stateObject>" +
                "<stateObject name=\"adjustWidthToText\">render.Annotation.SizeFitText</stateObject>" +
                "</restorableState>";
        attrib.restoreState(partialStateInXml);

        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);
        expected.setTextAlign(AVKey.RIGHT); // The integer 2 corresponds to RIGHT text alignment.
        expected.setImageRepeat(AVKey.REPEAT_XY); // render.Annotation.RepeatXY corresponds to AVKey.REPEAT_XY
        expected.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);

        assertAnnotationAttributesEquals(expected, attrib);
    }

    @Test
    public void testRestore_PartialSave()
    {
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignPartialExampleValues(attrib);

        // Only those values assigned to should be saved.
        String stateInXml = attrib.getRestorableState();
        attrib = new AnnotationAttributes();
        attrib.restoreState(stateInXml);

        AnnotationAttributes expected = new AnnotationAttributes();
        assignPartialExampleValues(expected);

        assertAnnotationAttributesEquals(expected, attrib);
    }

    @Test
    public void testRestore_CustomDefaults()
    {
        AnnotationAttributes defaults = new AnnotationAttributes();
        assignExampleValues(defaults);
        AnnotationAttributes attrib = new AnnotationAttributes();
        attrib.setDefaults(defaults);

        String stateInXml = attrib.getRestorableState();
        attrib = new AnnotationAttributes();
        attrib.restoreState(stateInXml);

        AnnotationAttributes expectedDefaults = new AnnotationAttributes();
        assignExampleValues(expectedDefaults);
        AnnotationAttributes expected = new AnnotationAttributes();
        expected.setDefaults(expectedDefaults);

        // "expected" and "attrib" will return values from their defaults.
        assertAnnotationAttributesEquals(expected, attrib);
    }

    //////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////

    @SuppressWarnings({"JavaDoc"})
    private static void assignExampleValues(AnnotationAttributes attrib)
    {
        attrib.setFrameShape(AVKey.SHAPE_ELLIPSE);
        attrib.setHighlighted(true);
        attrib.setHighlightScale(2.5);
        attrib.setSize(new java.awt.Dimension(255, 255));
        attrib.setScale(3.5);
        attrib.setOpacity(0.5);
        attrib.setLeader(AVKey.SHAPE_NONE);
        attrib.setLeaderGapWidth(100);
        attrib.setCornerRadius(4);
        attrib.setAdjustWidthToText(AVKey.SIZE_FIXED);
        attrib.setDrawOffset(new java.awt.Point(-3, -3));
        attrib.setInsets(new java.awt.Insets(11, 11, 11, 11));
        attrib.setBorderWidth(5.5);
        attrib.setBorderStippleFactor(6);
        attrib.setBorderStipplePattern((short) 0xFC0C);
        attrib.setAntiAliasHint(Annotation.ANTIALIAS_NICEST);
        attrib.setVisible(false);
        attrib.setFont(java.awt.Font.decode("Arial-ITALIC-24"));
        attrib.setTextAlign(AVKey.CENTER);
        attrib.setTextColor(java.awt.Color.PINK);
        attrib.setBackgroundColor(java.awt.Color.MAGENTA);
        attrib.setBorderColor(java.awt.Color.CYAN);
        attrib.setImageSource("path/to/image.ext");
        attrib.setImageScale(7.5);
        attrib.setImageOffset(new java.awt.Point(-4, -4));
        attrib.setImageOpacity(0.4);
        attrib.setImageRepeat(AVKey.REPEAT_Y);
        attrib.setDistanceMaxScale(0.1);
        attrib.setDistanceMaxScale(8.5);
        attrib.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
    }

    private static void assignNullValues(AnnotationAttributes attrib)
    {
        attrib.setFrameShape(null);
        attrib.setHighlighted(false);
        attrib.setHighlightScale(-1);
        attrib.setSize(null);
        attrib.setScale(-1);
        attrib.setOpacity(-1);
        attrib.setLeader(null);
        attrib.setLeaderGapWidth(-1);
        attrib.setCornerRadius(-1);
        attrib.setAdjustWidthToText(null);
        attrib.setDrawOffset(null);
        attrib.setInsets(null);
        attrib.setBorderWidth(-1);
        attrib.setBorderStippleFactor(-1);
        attrib.setBorderStipplePattern((short) 0x0000);
        attrib.setAntiAliasHint(-1);
        attrib.setVisible(false);
        attrib.setFont(null);
        attrib.setTextAlign(null);
        attrib.setTextColor(null);
        attrib.setBackgroundColor(null);
        attrib.setBorderColor(null);
        attrib.setImageSource(null);
        attrib.setImageScale(-1);
        attrib.setImageOffset(null);
        attrib.setImageOpacity(-1);
        attrib.setImageRepeat(null);
        attrib.setDistanceMaxScale(-1);
        attrib.setDistanceMaxScale(-1);
        attrib.setEffect(null);
    }

    private static void assignPartialExampleValues(AnnotationAttributes attrib)
    {
        attrib.setFrameShape(AVKey.SHAPE_ELLIPSE);
        attrib.setHighlighted(true);
        attrib.setHighlightScale(2.5);
        attrib.setSize(new java.awt.Dimension(255, 255));
        attrib.setScale(3.5);
        attrib.setOpacity(0.5);
        attrib.setLeader(AVKey.SHAPE_NONE);
    }

    private static void assertAnnotationAttributesEquals(AnnotationAttributes expected, AnnotationAttributes actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Acutal is null", actual);
        assertEquals("frameShape", expected.getFrameShape(), actual.getFrameShape());
        assertEquals("highlighted", expected.isHighlighted(), actual.isHighlighted());
        assertEquals("highlightScale", expected.getHighlightScale(), actual.getHighlightScale(), 0.0);
        assertEquals("size", expected.getSize(), actual.getSize());
        assertEquals("scale", expected.getScale(), actual.getScale(), 0.0);
        assertEquals("opacity", expected.getOpacity(), actual.getOpacity(), 0.0);
        assertEquals("leader", expected.getLeader(), actual.getLeader());
        assertEquals("leaderGapWidth", expected.getLeaderGapWidth(), actual.getLeaderGapWidth());
        assertEquals("cornerRadius", expected.getCornerRadius(), actual.getCornerRadius());
        assertEquals("adjustWidthToText", expected.getAdjustWidthToText(), actual.getAdjustWidthToText());
        assertEquals("drawOffset", expected.getDrawOffset(), actual.getDrawOffset());
        assertEquals("insets", expected.getInsets(), actual.getInsets());
        assertEquals("borderWidth", expected.getBorderWidth(), actual.getBorderWidth(), 0.0);
        assertEquals("borderStippleFactor", expected.getBorderStippleFactor(), actual.getBorderStippleFactor());
        assertEquals("borderStipplePattern", expected.getBorderStipplePattern(), actual.getBorderStipplePattern());
        assertEquals("antiAliasHint", expected.getAntiAliasHint(), actual.getAntiAliasHint());
        assertEquals("visible", expected.isVisible(), actual.isVisible());
        assertEquals("font", expected.getFont(), actual.getFont());
        assertEquals("textAlign", expected.getTextAlign(), actual.getTextAlign());
        assertEquals("textColor", expected.getTextColor(), actual.getTextColor());
        assertEquals("backgroundColor", expected.getBackgroundColor(), actual.getBackgroundColor());
        assertEquals("borderColor", expected.getBorderColor(), actual.getBorderColor());
        assertEquals("imageSource", expected.getImageSource(), actual.getImageSource());
        assertEquals("imageScale", expected.getImageScale(), actual.getImageScale(), 0.0);
        assertEquals("imageOffset", expected.getImageOffset(), actual.getImageOffset());
        assertEquals("imageOpacity", expected.getImageOpacity(), actual.getImageOpacity(), 0.0);
        assertEquals("imageRepeat", expected.getImageRepeat(), actual.getImageRepeat());
        assertEquals("distanceMinScale", expected.getDistanceMinScale(), actual.getDistanceMinScale(), 0.0);
        assertEquals("distanceMaxScale", expected.getDistanceMaxScale(), actual.getDistanceMaxScale(), 0.0);
        assertEquals("distanceMinOpacity", expected.getDistanceMinOpacity(), actual.getDistanceMinOpacity(), 0.0);
        assertEquals("effect", expected.getEffect(), actual.getEffect());
    }
}
