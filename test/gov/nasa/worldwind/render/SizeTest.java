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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SizeTest
{
    @Test
    public void testSize()
    {
        // Test with native width and fractional height
        Size size = new Size(Size.NATIVE_DIMENSION, 0, AVKey.PIXELS, Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION);
        Dimension dim = size.compute(70, 10, 100, 100);
        assertTrue("Dimension should be 70 x 50", dim.equals(new Dimension(70, 50)));

        // Test with maintain aspect ratio
        size = new Size(Size.MAINTAIN_ASPECT_RATIO, 0, AVKey.PIXELS, Size.EXPLICIT_DIMENSION, 50, AVKey.PIXELS);
        dim = size.compute(20, 10, 100, 100);
        assertTrue("Dimension should be 100 x 50", dim.equals(new Dimension(100, 50)));
    }

    @Test
    public void testZeroSizeContainer()
    {
        Size size = new Size(Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION,
            Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION);

        Dimension dim = size.compute(100, 100, 0, 0);

        assertTrue("Dimension != null", dim != null);
        assertTrue("Dimension should be zero", dim.equals(new Dimension(0, 0)));
    }

    @Test
    public void testZeroSizeRect()
    {
        // Test with fractional dimensions
        Size size = new Size(Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION, Size.EXPLICIT_DIMENSION, 0.5,
            AVKey.FRACTION);
        Dimension dim = size.compute(0, 0, 100, 100);
        assertTrue("Dimension should be 50 x 50", dim.equals(new Dimension(50, 50)));

        // Test with pixel dimensions
        size = new Size(Size.EXPLICIT_DIMENSION, 50, AVKey.PIXELS, Size.EXPLICIT_DIMENSION, 50, AVKey.PIXELS);
        dim = size.compute(0, 0, 100, 100);
        assertTrue("Dimension should be 50 x 50", dim.equals(new Dimension(50, 50)));

        // Test with maintain aspect radio 
        size = new Size(Size.MAINTAIN_ASPECT_RATIO, 0, AVKey.PIXELS, Size.MAINTAIN_ASPECT_RATIO, 0, AVKey.PIXELS);
        dim = size.compute(0, 0, 100, 100);
        assertTrue("Dimension should be 0 x 0", dim.equals(new Dimension(0, 0)));

        // Test with native dimension
        size = new Size(Size.NATIVE_DIMENSION, 0, AVKey.PIXELS, Size.NATIVE_DIMENSION, 0, AVKey.PIXELS);
        dim = size.compute(0, 0, 100, 100);
        assertTrue("Dimension should be 0 x 0", dim.equals(new Dimension(0, 0)));
    }

    @Test
    public void testRestorableStateExplicit()
    {
        // Test with fractional dimensions
        Size expected = new Size(Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION, Size.EXPLICIT_DIMENSION, 0.5,
            AVKey.FRACTION);

        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        expected.getRestorableState(rs, null);

        Size actual = new Size();
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @Test
    public void testRestorableStateNative()
    {
        // Test with fractional dimensions
        Size expected = new Size(Size.NATIVE_DIMENSION, 0, null, Size.NATIVE_DIMENSION, 0, null);

        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        expected.getRestorableState(rs, null);

        Size actual = new Size();
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @Test
    public void testRestorableStateAspectRatio()
    {
        // Test with fractional dimensions
        Size expected = new Size(Size.MAINTAIN_ASPECT_RATIO, 0, null, Size.MAINTAIN_ASPECT_RATIO, 0, null);

        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        expected.getRestorableState(rs, null);

        Size actual = new Size();
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }

    @Test
    public void testRestorableStateLegacy()
    {
        // Test with fractional dimensions
        Size input = new Size("MaintainAspectRatio", 0, null, "ExplicitDimension", 100, AVKey.PIXELS);
        Size expected = new Size(Size.MAINTAIN_ASPECT_RATIO, 0, null, Size.EXPLICIT_DIMENSION, 100, AVKey.PIXELS);

        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        input.getRestorableState(rs, null);

        Size actual = new Size();
        actual.restoreState(rs, null);

        assertEquals(expected, actual);
    }
}
