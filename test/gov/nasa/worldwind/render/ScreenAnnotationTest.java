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
import gov.nasa.worldwind.geom.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class ScreenAnnotationTest
{
    //////////////////////////////////////////////////////////
    // Persistence Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testRestore_NewInstance()
    {
        ScreenAnnotation annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(annotation);

        String stateInXml = annotation.getRestorableState();
        annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        annotation.restoreState(stateInXml);

        ScreenAnnotation expected = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(expected);

        assertScreenAnnotationEquals(expected, annotation);
    }

    @Test
    public void testRestore_SameInstance()
    {
        ScreenAnnotation annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(annotation);

        String stateInXml = annotation.getRestorableState();
        assignNullValues(annotation);
        annotation.restoreState(stateInXml);

        ScreenAnnotation expected = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(expected);

        assertScreenAnnotationEquals(expected, annotation);
    }

    @Test
    public void testRestore_EmptyStateDocument()
    {
        ScreenAnnotation annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(annotation);

        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        annotation.restoreState(emptyStateInXml);

        // No attributes should have changed.
        ScreenAnnotation expected = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(expected);

        assertScreenAnnotationEquals(expected, annotation);
    }

    @Test
    public void testRestore_InvalidStateDocument()
    {
        try
        {
            String badStateInXml = "!!invalid xml string!!";
            ScreenAnnotation annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
            annotation.restoreState(badStateInXml);

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
        ScreenAnnotation annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(annotation);

        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"text\">Hello, World!</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        annotation.restoreState(partialStateInXml);

        ScreenAnnotation expected = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(expected);
        expected.setText("Hello, World!");

        assertScreenAnnotationEquals(expected, annotation);
    }

    @Test
    public void testRestore_AnnotationSharing()
    {
        ScreenAnnotation annotation1 = new ScreenAnnotation("", new java.awt.Point(0, 0));
        ScreenAnnotation annotation2 = new ScreenAnnotation("", new java.awt.Point(0, 0));
        AnnotationAttributes sharedAttributes = new AnnotationAttributes();
        annotation1.setAttributes(sharedAttributes);
        annotation2.setAttributes(sharedAttributes);
        assignExampleValues(annotation1);

        String stateInXml = annotation1.getRestorableState();
        assignNullValues(annotation1);
        annotation1.restoreState(stateInXml);

        assertSame(annotation1.getAttributes(), annotation2.getAttributes(), "Attributes are shared");
        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);
        assertAnnotationAttributesEquals(expected, annotation2.getAttributes());
    }

    @Test
    public void test_SaveScreenAnnotation_RestoreGlobeAnnotation()
    {
        ScreenAnnotation screenAnnotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(screenAnnotation);

        String stateInXml = screenAnnotation.getRestorableState();

        GlobeAnnotation globeAnnotation = new GlobeAnnotation("", Position.fromDegrees(0.0, 0.0, 0.0));
        globeAnnotation.restoreState(stateInXml);

        //noinspection RedundantCast
        assertAnnotationEquals(screenAnnotation, globeAnnotation);
    }

    //////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////

    @SuppressWarnings({"JavaDoc"})
    private static void assignExampleValues(ScreenAnnotation annotation)
    {
        annotation.setText(
            "<p>\n<b><font color=\"#664400\">LA CLAPI\u00c8RE</font></b><br />\n<i>Alt: 1100-1700m</i>\n</p>\n<p>\n<b>Glissement de terrain majeur</b> dans la haute Tin\u00e9e, sur un flanc du <a href=\"http://www.mercantour.eu\">Parc du Mercantour</a>, Alpes Maritimes.\n</p>\n<p>\nRisque aggrav\u00e9 d'<b>inondation</b> du village de <i>Saint \u00c9tienne de Tin\u00e9e</i> juste en amont.\n</p>");
        annotation.setScreenPoint(new java.awt.Point(321, 105));
        assignExampleValues(annotation.getAttributes());
    }

    private static void assignNullValues(ScreenAnnotation annotation)
    {
        annotation.setText("");
        annotation.setScreenPoint(new java.awt.Point(0, 0));
        assignNullValues(annotation.getAttributes());
    }

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

    private static void assertAnnotationEquals(Annotation expected, Annotation actual)
    {
        assertNotNull(expected, "Expected is null");
        assertNotNull(actual, "Acutal is null");
        assertEquals(expected.getText(), actual.getText(), "text");
        assertAnnotationAttributesEquals(expected.getAttributes(), actual.getAttributes());
    }

    private static void assertScreenAnnotationEquals(ScreenAnnotation expected, ScreenAnnotation actual)
    {
        assertNotNull(expected, "Expected is null");
        assertNotNull(actual, "Acutal is null");
        assertEquals(expected.getText(), actual.getText(), "text");
        assertEquals(expected.getScreenPoint(), actual.getScreenPoint(), "screenPoint");
        assertAnnotationAttributesEquals(expected.getAttributes(), actual.getAttributes());
    }

    private static void assertAnnotationAttributesEquals(AnnotationAttributes expected, AnnotationAttributes actual)
    {
        assertNotNull(expected, "Expected is null");
        assertNotNull(actual, "Acutal is null");
        assertEquals(expected.getFrameShape(), actual.getFrameShape(), "frameShape");
        assertEquals(expected.isHighlighted(), actual.isHighlighted(), "highlighted");
        assertEquals(expected.getHighlightScale(), actual.getHighlightScale(), 0.0, "highlightScale");
        assertEquals(expected.getSize(), actual.getSize(), "size");
        assertEquals(expected.getScale(), actual.getScale(), 0.0, "scale");
        assertEquals(expected.getOpacity(), actual.getOpacity(), 0.0, "opacity");
        assertEquals(expected.getLeader(), actual.getLeader(), "leader");
        assertEquals(expected.getCornerRadius(), actual.getCornerRadius(), "cornerRadius");
        assertEquals(expected.getAdjustWidthToText(), actual.getAdjustWidthToText(), "adjustWidthToText");
        assertEquals(expected.getDrawOffset(), actual.getDrawOffset(), "drawOffset");
        assertEquals(expected.getInsets(), actual.getInsets(), "insets");
        assertEquals(expected.getBorderWidth(), actual.getBorderWidth(), 0.0, "borderWidth");
        assertEquals(expected.getBorderStippleFactor(), actual.getBorderStippleFactor(), "borderStippleFactor");
        assertEquals(expected.getBorderStipplePattern(), actual.getBorderStipplePattern(), "borderStipplePattern");
        assertEquals(expected.getAntiAliasHint(), actual.getAntiAliasHint(), "antiAliasHint");
        assertEquals(expected.isVisible(), actual.isVisible(), "visible");
        assertEquals(expected.getFont(), actual.getFont(), "font");
        assertEquals(expected.getTextAlign(), actual.getTextAlign(), "textAlign");
        assertEquals(expected.getTextColor(), actual.getTextColor(), "textColor");
        assertEquals(expected.getBackgroundColor(), actual.getBackgroundColor(), "backgroundColor");
        assertEquals(expected.getBorderColor(), actual.getBorderColor(), "borderColor");
        assertEquals(expected.getImageSource(), actual.getImageSource(), "imageSource");
        assertEquals(expected.getImageScale(), actual.getImageScale(), 0.0, "imageScale");
        assertEquals(expected.getImageOffset(), actual.getImageOffset(), "imageOffset");
        assertEquals(expected.getImageOpacity(), actual.getImageOpacity(), 0.0, "imageOpacity");
        assertEquals(expected.getImageRepeat(), actual.getImageRepeat(), "imageRepeat");
        assertEquals(expected.getDistanceMinScale(), actual.getDistanceMinScale(), 0.0, "distanceMinScale");
        assertEquals(expected.getDistanceMaxScale(), actual.getDistanceMaxScale(), 0.0, "distanceMaxScale");
        assertEquals(expected.getDistanceMinOpacity(), actual.getDistanceMinOpacity(), 0.0, "distanceMinOpacity");
        assertEquals(expected.getEffect(), actual.getEffect(), "effect");
    }
}
