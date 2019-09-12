/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.RestorableSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.awt.*;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ShapeAttributesTest
{
    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        BasicShapeAttributes defaultBasicAttrs = new BasicShapeAttributes();
        BasicShapeAttributes exampleBasicAttrs = new BasicShapeAttributes();
        exampleBasicAttrs.setUnresolved(true); // set unresolved to true; it is false by default.
        exampleBasicAttrs.setDrawInterior(false); // set drawInterior to false; it is true by default.
        exampleBasicAttrs.setDrawOutline(false); // set drawOutline to false; it is true by default.
        exampleBasicAttrs.setEnableAntialiasing(false);
        exampleBasicAttrs.setEnableLighting(true); // set enableLighting to true; it is false by default.
        exampleBasicAttrs.setInteriorMaterial(Material.RED);
        exampleBasicAttrs.setOutlineMaterial(Material.GREEN);
        exampleBasicAttrs.setInteriorOpacity(0.5);
        exampleBasicAttrs.setOutlineOpacity(0.75);
        exampleBasicAttrs.setOutlineWidth(10.0);
        exampleBasicAttrs.setOutlineStippleFactor(256);
        exampleBasicAttrs.setOutlineStipplePattern((short) 0xABAB);
        exampleBasicAttrs.setImageSource("images/pushpins/plain-black.png");
        exampleBasicAttrs.setImageScale(2.0);

        BalloonAttributes defaultBalloonAttrs = new BasicBalloonAttributes();
        BalloonAttributes exampleBalloonAttrs = new BasicBalloonAttributes();
        exampleBalloonAttrs.setUnresolved(true);
        exampleBalloonAttrs.setDrawInterior(false);
        exampleBalloonAttrs.setDrawOutline(false);
        exampleBalloonAttrs.setEnableAntialiasing(false);
        exampleBalloonAttrs.setEnableLighting(false);
        exampleBalloonAttrs.setInteriorMaterial(Material.RED);
        exampleBalloonAttrs.setOutlineMaterial(Material.GREEN);
        exampleBalloonAttrs.setInteriorOpacity(0.5);
        exampleBalloonAttrs.setOutlineOpacity(0.75);
        exampleBalloonAttrs.setOutlineWidth(10.0);
        exampleBalloonAttrs.setOutlineStippleFactor(256);
        exampleBalloonAttrs.setOutlineStipplePattern((short) 0xABAB);
        exampleBalloonAttrs.setSize(new Size(Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION,
            Size.EXPLICIT_DIMENSION, 100.0, AVKey.PIXELS));
        exampleBalloonAttrs.setOffset(new Offset(0.5, 0.0, AVKey.FRACTION, AVKey.PIXELS));
        exampleBalloonAttrs.setInsets(new Insets(5, 10, 15, 20));
        exampleBalloonAttrs.setBalloonShape(AVKey.SHAPE_ELLIPSE);
        exampleBalloonAttrs.setLeaderShape(AVKey.SHAPE_NONE);
        exampleBalloonAttrs.setLeaderWidth(100);
        exampleBalloonAttrs.setCornerRadius(5);
        exampleBalloonAttrs.setFont(Font.decode("Arial-BOLD-24"));
        exampleBalloonAttrs.setTextColor(Color.BLUE);
        exampleBalloonAttrs.setImageSource("images/pushpins/plain-black.png");
        exampleBalloonAttrs.setImageScale(2.0);
        exampleBalloonAttrs.setImageOffset(new Point(5, 10));
        exampleBalloonAttrs.setImageOpacity(0.5);
        exampleBalloonAttrs.setImageRepeat(AVKey.REPEAT_NONE);

        return Arrays.asList(new Object[][] {
            {defaultBasicAttrs, exampleBasicAttrs},
            {defaultBalloonAttrs, exampleBalloonAttrs}
        });
    }

    private ShapeAttributes defaultAttributes;
    private ShapeAttributes exampleAttributes;

    public ShapeAttributesTest(ShapeAttributes defaultAttributes, ShapeAttributes exampleAttributes)
    {
        this.defaultAttributes = defaultAttributes;
        this.exampleAttributes = exampleAttributes;
    }

    @Test
    public void testBasicSaveRestore()
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();

        ShapeAttributes expected = this.exampleAttributes.copy();
        expected.getRestorableState(rs, null);

        ShapeAttributes actual = this.defaultAttributes.copy();
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @Test
    public void testRestoreSameInstance()
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();

        ShapeAttributes expected = this.exampleAttributes.copy();

        ShapeAttributes actual = this.exampleAttributes.copy();
        actual.getRestorableState(rs, null);
        actual.copy(this.defaultAttributes.copy());
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @Test
    public void testRestoreNullDocument()
    {
        try
        {
            ShapeAttributes attrs = this.defaultAttributes.copy();
            attrs.restoreState(null, null);
            fail("Expected an IllegalArgumentException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testRestoreEmptyDocument()
    {
        ShapeAttributes expected = this.exampleAttributes.copy();

        // Restoring an empty state document should not change any attributes.
        ShapeAttributes actual = this.exampleAttributes.copy();
        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        RestorableSupport rs = RestorableSupport.parse(emptyStateInXml);
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @Test
    public void testRestoreOneAttribute()
    {
        ShapeAttributes expected = this.exampleAttributes.copy();
        expected.setOutlineWidth(11);

        ShapeAttributes actual = this.exampleAttributes.copy();
        String partialStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<restorableState>" +
                "<stateObject name=\"outlineWidth\">11</stateObject>" +
                "<unknownElement name=\"unknownName\">unknownValue</unknownElement>" +
                "</restorableState>";
        RestorableSupport rs = RestorableSupport.parse(partialStateInXml);
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }
}
