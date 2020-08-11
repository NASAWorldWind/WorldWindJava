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
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RenderableLayerTest
{
    //////////////////////////////////////////////////////////
    // Basic Operation Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testConstructor()
    {
        RenderableLayer layer;

        // Test the parameterless constructor.
        layer = new RenderableLayer();
        assertNotNull("", layer);
    }

    @Test
    public void testAddRenderable()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        for (Renderable item : renderables)
        {
            layer.addRenderable(item);
        }

        // Test that the layer contains the renderables.
        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testAddRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderables(renderables);

        // Test that the layer contains the renderables.
        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testInsertRenderable()
    {
        Iterable<Renderable> source = createExampleIterable();

        List<Renderable> renderables = new ArrayList<Renderable>();
        RenderableLayer layer = new RenderableLayer();

        for (Renderable renderable : source)
        {
            renderables.add(renderables.size(), renderable);
            layer.addRenderable(layer.getNumRenderables(), renderable);
        }

        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testInsertRenderableAtBeginning()
    {
        Collection<Renderable> source = createExampleIterable();

        List<Renderable> renderables = new ArrayList<Renderable>();
        RenderableLayer layer = new RenderableLayer();
        renderables.addAll(source);
        layer.addRenderables(source);

        Path inserted = new Path();
        renderables.add(0, inserted);
        layer.addRenderable(0, inserted);

        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testInsertRenderableAfterFirst()
    {
        Collection<Renderable> source = createExampleIterable();

        List<Renderable> renderables = new ArrayList<Renderable>();
        RenderableLayer layer = new RenderableLayer();
        renderables.addAll(source);
        layer.addRenderables(source);

        Path inserted = new Path();
        renderables.add(1, inserted);
        layer.addRenderable(1, inserted);

        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testInsertRenderableAtEnd()
    {
        Collection<Renderable> source = createExampleIterable();

        List<Renderable> renderables = new ArrayList<Renderable>();
        RenderableLayer layer = new RenderableLayer();
        renderables.addAll(source);
        layer.addRenderables(source);

        Path inserted = new Path();
        renderables.add(renderables.size(), inserted);
        layer.addRenderable(layer.getNumRenderables(), inserted);

        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testRemoveRenderable()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        for (Renderable item : renderables)
        {
            layer.addRenderable(item);
        }
        for (Renderable item : renderables)
        {
            layer.removeRenderable(item);
        }

        // Test that the layer contains no renderables.
        assertFalse("", layer.getRenderables().iterator().hasNext());
    }

    @Test
    public void testRemoveAllRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderables(renderables);
        layer.removeAllRenderables();

        // Test that the layer contains no renderables.
        assertFalse("", layer.getRenderables().iterator().hasNext());
    }

    @Test
    public void testSetRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        // Test that the layer points to the Iterable.
        assertSame("", renderables, layer.getRenderables());
    }

    //////////////////////////////////////////////////////////
    // Edge Case Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testSetRenderablesClearsRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderables(renderables);
        layer.setRenderables(renderables);
        layer.setRenderables(null);

        // Test that the layer does not point to the Iterable.
        assertNotSame("", renderables, layer.getRenderables());
        // Test that the layer contains no renderables.
        assertFalse("", layer.getRenderables().iterator().hasNext());
    }

    @Test
    public void testSetRenderablesThenAddRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);
        layer.setRenderables(null);
        layer.addRenderables(renderables);

        // Test that the layer does not point to the Iterable.
        assertNotSame("", renderables, layer.getRenderables());
        // Test that the layer contains the renderables.
        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testMaliciousGetRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderables(renderables);

        Iterable<? extends Renderable> layerRenderables = layer.getRenderables();

        // Test that the returned list cannot be modified.
        try
        {
            if (layerRenderables instanceof java.util.Collection)
            {
                java.util.Collection collection = (java.util.Collection) layerRenderables;
                collection.clear();
            }
            else
            {
                java.util.Iterator<? extends Renderable> iter = layerRenderables.iterator();
                while (iter.hasNext())
                {
                    iter.next();
                    iter.remove();
                }
            }
        }
        catch (UnsupportedOperationException e)
        {
            e.printStackTrace();
        }

        // Test that the layer contents do not change, even if the returned list can be modified.
        assertEquals("", renderables, layer.getRenderables());
    }

    @Test
    public void testMaliciousSetRenderables()
    {
        // Create an Iterable with null elements.
        java.util.List<Renderable> list = new java.util.ArrayList<Renderable>();
        list.add(null);

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(list);

        DrawContext dc = new DrawContextImpl();
        dc.setModel(new BasicModel());
        dc.setView(new BasicOrbitView());

        try
        {
            // Test that the layer does not fail when the Iterable is used.
            layer.render(dc);
        }
        catch (NullPointerException e)
        {
            fail("Layer does not check for null elements in Iterable");
        }
    }

    @Test
    public void testDisposeDoesNotClearRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();
        Iterable<Renderable> emptyRenderables = new ArrayList<Renderable>();

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderables(renderables);
        layer.dispose();

        // Test that the layer contains the renderables.
        assertEquals("", emptyRenderables, layer.getRenderables());
    }

    //////////////////////////////////////////////////////////
    // Exceptional Condition Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testAddRenderableFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.addRenderable(new Path());
            fail("Should raise an IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddRenderablesFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.addRenderables(renderables);
            fail("Should raise an IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testInsertRenderableFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.addRenderable(0, new Path());
            fail("Should raise an IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testRemoveRenderableFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.removeRenderable(new Path());
            fail("Should raise an IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testRemoveAllRenderablesFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.removeAllRenderables();
            fail("Should raise an IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testDisposeFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.dispose();
            fail("Should raise an IllegalStateException");
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////

    private static void assertEquals(String message, Iterable<Renderable> expected, Iterable<Renderable> actual)
    {
        if (expected == null)
        {
            assertNull(message, actual);
        }
        else
        {
            java.util.Iterator<Renderable> expectedIter = expected.iterator(), actualIter = actual.iterator();
            // Compare the elements in each iterator, as long as they both have elements.
            while (expectedIter.hasNext() && actualIter.hasNext())
            {
                Assert.assertEquals(message, expectedIter.next(), actualIter.next());
            }
            // If either iterator has more elements, then their lengths are different.
            assertFalse(message, expectedIter.hasNext() || actualIter.hasNext());
        }
    }

    private static Collection<Renderable> createExampleIterable()
    {
        //noinspection RedundantArrayCreation
        return java.util.Arrays.asList(new Renderable[] {
            new Path(),
            new Path(),
            new Path()});
    }
}
