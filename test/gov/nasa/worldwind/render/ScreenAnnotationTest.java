/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;

/**
 * @author dcollins
 * @version $Id: ScreenAnnotationTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenAnnotationTest extends junit.framework.TestCase
{
    /*************************************************************************************************************/
    /** Persistence Tests **/
    /** ******************************************************************************************************** */

    public void testRestore_NewInstance()
    {
        ScreenAnnotation annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(annotation);

        String stateInXml = annotation.getRestorableState();
        annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        annotation.restoreState(stateInXml);

        ScreenAnnotation expected = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(expected);

        assertEquals(expected, annotation);
    }

    public void testRestore_SameInstance()
    {
        ScreenAnnotation annotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(annotation);

        String stateInXml = annotation.getRestorableState();
        assignNullValues(annotation);
        annotation.restoreState(stateInXml);

        ScreenAnnotation expected = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(expected);

        assertEquals(expected, annotation);
    }

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

        assertEquals(expected, annotation);
    }

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
        }
    }

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

        assertEquals(expected, annotation);
    }

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

        assertSame("Attributes are shared", annotation1.getAttributes(), annotation2.getAttributes());
        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);
        assertEquals(expected, annotation2.getAttributes());
    }

    public void test_SaveScreenAnnotation_RestoreGlobeAnnotation()
    {
        ScreenAnnotation screenAnnotation = new ScreenAnnotation("", new java.awt.Point(0, 0));
        assignExampleValues(screenAnnotation);

        String stateInXml = screenAnnotation.getRestorableState();

        GlobeAnnotation globeAnnotation = new GlobeAnnotation("", Position.fromDegrees(0.0, 0.0, 0.0));
        globeAnnotation.restoreState(stateInXml);

        //noinspection RedundantCast
        assertEquals((Annotation) screenAnnotation, (Annotation) globeAnnotation);
    }

    /*************************************************************************************************************/
    /** Helper Methods **/
    /** ******************************************************************************************************** */

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

    private static void assertEquals(Annotation expected, Annotation actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Acutal is null", actual);
        assertEquals("text", expected.getText(), actual.getText());
        assertEquals(expected.getAttributes(), actual.getAttributes());
    }

    private static void assertEquals(ScreenAnnotation expected, ScreenAnnotation actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Acutal is null", actual);
        assertEquals("text", expected.getText(), actual.getText());
        assertEquals("screenPoint", expected.getScreenPoint(), actual.getScreenPoint());
        assertEquals(expected.getAttributes(), actual.getAttributes());
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

    private static void assertEquals(AnnotationAttributes expected, AnnotationAttributes actual)
    {
        assertNotNull("Expected is null", expected);
        assertNotNull("Acutal is null", actual);
        assertEquals("frameShape", expected.getFrameShape(), actual.getFrameShape());
        assertEquals("highlighted", expected.isHighlighted(), actual.isHighlighted());
        assertEquals("highlightScale", expected.getHighlightScale(), actual.getHighlightScale());
        assertEquals("size", expected.getSize(), actual.getSize());
        assertEquals("scale", expected.getScale(), actual.getScale());
        assertEquals("opacity", expected.getOpacity(), actual.getOpacity());
        assertEquals("leader", expected.getLeader(), actual.getLeader());
        assertEquals("cornerRadius", expected.getCornerRadius(), actual.getCornerRadius());
        assertEquals("adjustWidthToText", expected.getAdjustWidthToText(), actual.getAdjustWidthToText());
        assertEquals("drawOffset", expected.getDrawOffset(), actual.getDrawOffset());
        assertEquals("insets", expected.getInsets(), actual.getInsets());
        assertEquals("borderWidth", expected.getBorderWidth(), actual.getBorderWidth());
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
        assertEquals("imageScale", expected.getImageScale(), actual.getImageScale());
        assertEquals("imageOffset", expected.getImageOffset(), actual.getImageOffset());
        assertEquals("imageOpacity", expected.getImageOpacity(), actual.getImageOpacity());
        assertEquals("imageRepeat", expected.getImageRepeat(), actual.getImageRepeat());
        assertEquals("distanceMinScale", expected.getDistanceMinScale(), actual.getDistanceMinScale());
        assertEquals("distanceMaxScale", expected.getDistanceMaxScale(), actual.getDistanceMaxScale());
        assertEquals("distanceMinOpacity", expected.getDistanceMinOpacity(), actual.getDistanceMinOpacity());
        assertEquals("effect", expected.getEffect(), actual.getEffect());
    }

    public static void main(String[] args)
    {
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(ScreenAnnotationTest.class));
    }
}
