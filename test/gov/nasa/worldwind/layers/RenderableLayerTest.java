/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: RenderableLayerTest.java 3435 2015-10-13 10:32:43Z dcollins $
 */
public class RenderableLayerTest extends junit.framework.TestCase
{
    /*************************************************************************************************************/
    /** Basic Operation Tests **/
    /** ****************************************************************************************************** */

    public void testConstructor()
    {
        RenderableLayer layer;

        // Test the parameterless constructor.
        layer = new RenderableLayer();
        assertNotNull("", layer);
    }

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

    public void testAddRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderables(renderables);

        // Test that the layer contains the renderables.
        assertEquals("", renderables, layer.getRenderables());
    }

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

    public void testInsertRenderableAtBeginning()
    {
        Collection<Renderable> source = createExampleIterable();

        List<Renderable> renderables = new ArrayList<Renderable>();
        RenderableLayer layer = new RenderableLayer();
        renderables.addAll(source);
        layer.addRenderables(source);

        Polyline inserted = new Polyline();
        renderables.add(0, inserted);
        layer.addRenderable(0, inserted);

        assertEquals("", renderables, layer.getRenderables());
    }

    public void testInsertRenderableAfterFirst()
    {
        Collection<Renderable> source = createExampleIterable();

        List<Renderable> renderables = new ArrayList<Renderable>();
        RenderableLayer layer = new RenderableLayer();
        renderables.addAll(source);
        layer.addRenderables(source);

        Polyline inserted = new Polyline();
        renderables.add(1, inserted);
        layer.addRenderable(1, inserted);

        assertEquals("", renderables, layer.getRenderables());
    }

    public void testInsertRenderableAtEnd()
    {
        Collection<Renderable> source = createExampleIterable();

        List<Renderable> renderables = new ArrayList<Renderable>();
        RenderableLayer layer = new RenderableLayer();
        renderables.addAll(source);
        layer.addRenderables(source);

        Polyline inserted = new Polyline();
        renderables.add(renderables.size(), inserted);
        layer.addRenderable(layer.getNumRenderables(), inserted);

        assertEquals("", renderables, layer.getRenderables());
    }

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

    public void testRemoveAllRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.addRenderables(renderables);
        layer.removeAllRenderables();

        // Test that the layer contains no renderables.
        assertFalse("", layer.getRenderables().iterator().hasNext());
    }

    public void testSetRenderables()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        // Test that the layer points to the Iterable.
        assertSame("", renderables, layer.getRenderables());
    }

    /*************************************************************************************************************/
    /** Edge Case Tests **/
    /** ****************************************************************************************************** */

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

    public void testMaliciousSetRenderables()
    {
        // Create an Iterable with null elements.
        java.util.List<Renderable> list = new java.util.ArrayList<Renderable>();
        list.add(null);

        RenderableLayer layer = new RenderableLayer()
        {
            // Override to avoid View initialization issues.
            public boolean isLayerActive(DrawContext dc)
            {
                return true;
            }
        };
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

    /*************************************************************************************************************/
    /** Exceptional Condition Tests **/
    /** ****************************************************************************************************** */

    public void testAddRenderableFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.addRenderable(new Polyline());
            fail("");
        }
        catch (IllegalStateException e)
        {
        }
    }

    public void testAddRenderablesFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.addRenderables(renderables);
            fail("");
        }
        catch (IllegalStateException e)
        {
        }
    }

    public void testInsertRenderableFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.addRenderable(0, new Polyline());
            fail("");
        }
        catch (IllegalStateException e)
        {
        }
    }

    public void testRemoveRenderableFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.removeRenderable(new Polyline());
            fail("");
        }
        catch (IllegalStateException e)
        {
        }
    }

    public void testRemoveAllRenderablesFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.removeAllRenderables();
            fail("");
        }
        catch (IllegalStateException e)
        {
        }
    }

    public void testDisposeFail()
    {
        Iterable<Renderable> renderables = createExampleIterable();

        RenderableLayer layer = new RenderableLayer();
        layer.setRenderables(renderables);

        try
        {
            // Expecting an IllegalStateException here.
            layer.dispose();
            fail("");
        }
        catch (IllegalStateException e)
        {
        }
    }

    /*************************************************************************************************************/
    /** Helper Methods **/
    /** ****************************************************************************************************** */

    @SuppressWarnings({"JavaDoc"})
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
                assertEquals(message, expectedIter.next(), actualIter.next());
            }
            // If either iterator has more elements, then their lengths are different.
            assertFalse(message, expectedIter.hasNext() || actualIter.hasNext());
        }
    }

    private static Collection<Renderable> createExampleIterable()
    {
        //noinspection RedundantArrayCreation
        return java.util.Arrays.asList(new Renderable[] {
            new Polyline(),
            new Polyline(),
            new Polyline()});
    }

    public static void main(String[] args)
    {
        new junit.textui.TestRunner().doRun(new junit.framework.TestSuite(RenderableLayerTest.class));
    }
}
