/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * @author dcollins
 * @version $Id: AnnotationAttributesTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AnnotationAttributesTest extends junit.framework.TestCase
{
    /*************************************************************************************************************/
    /** Persistence Tests **/
    /** ******************************************************************************************************** */

    public void testRestore_NewInstance()
    {
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignExampleValues(attrib);

        String stateInXml = attrib.getRestorableState();
        attrib = new AnnotationAttributes();
        attrib.restoreState(stateInXml);

        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);

        assertEquals(expected, attrib);
    }

    public void testRestore_SameInstance()
    {
        AnnotationAttributes attrib = new AnnotationAttributes();
        assignExampleValues(attrib);

        String stateInXml = attrib.getRestorableState();
        assignNullValues(attrib);
        attrib.restoreState(stateInXml);

        AnnotationAttributes expected = new AnnotationAttributes();
        assignExampleValues(expected);

        assertEquals(expected, attrib);
    }

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

        assertEquals(expected, attrib);
    }

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
        }
    }

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

        assertEquals(expected, attrib);
    }

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

        assertEquals(expected, attrib);
    }

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

        assertEquals(expected, attrib);
    }

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
        assertEquals(expected, attrib);
    }

    /*************************************************************************************************************/
    /** Helper Methods **/
    /** ******************************************************************************************************** */

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
        assertEquals("leaderGapWidth", expected.getLeaderGapWidth(), actual.getLeaderGapWidth());
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
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(AnnotationAttributesTest.class));
    }
}
