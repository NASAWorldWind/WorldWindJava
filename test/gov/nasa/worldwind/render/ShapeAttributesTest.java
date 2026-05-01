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
import gov.nasa.worldwind.util.RestorableSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ShapeAttributesTest
{
    /**
     * Provides pairs of (defaultAttributes, exampleAttributes) — one pair per test invocation.
     */
    @SuppressWarnings("unused")
    static Stream<Arguments> data()
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

        return Stream.of(
            Arguments.of(defaultBasicAttrs, exampleBasicAttrs),
            Arguments.of(defaultBalloonAttrs, exampleBalloonAttrs)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testBasicSaveRestore(ShapeAttributes defaultAttributes, ShapeAttributes exampleAttributes)
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();

        ShapeAttributes expected = exampleAttributes.copy();
        expected.getRestorableState(rs, null);

        ShapeAttributes actual = defaultAttributes.copy();
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRestoreSameInstance(ShapeAttributes defaultAttributes, ShapeAttributes exampleAttributes)
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();

        ShapeAttributes expected = exampleAttributes.copy();

        ShapeAttributes actual = exampleAttributes.copy();
        actual.getRestorableState(rs, null);
        actual.copy(defaultAttributes.copy());
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRestoreNullDocument(ShapeAttributes defaultAttributes, ShapeAttributes exampleAttributes)
    {
        assertThrows(IllegalArgumentException.class, () -> defaultAttributes.copy().restoreState(null, null));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRestoreEmptyDocument(ShapeAttributes defaultAttributes, ShapeAttributes exampleAttributes)
    {
        ShapeAttributes expected = exampleAttributes.copy();

        // Restoring an empty state document should not change any attributes.
        ShapeAttributes actual = exampleAttributes.copy();
        String emptyStateInXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<emptyDocumentRoot/>";
        RestorableSupport rs = RestorableSupport.parse(emptyStateInXml);
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRestoreOneAttribute(ShapeAttributes defaultAttributes, ShapeAttributes exampleAttributes)
    {
        ShapeAttributes expected = exampleAttributes.copy();
        expected.setOutlineWidth(11);

        ShapeAttributes actual = exampleAttributes.copy();
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
