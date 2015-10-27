/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.combine.Combinable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Shows how to use the {@link gov.nasa.worldwind.util.combine.Combinable} interface and the {@link
 * gov.nasa.worldwind.util.combine.ShapeCombiner} class to compute the intersection of a World Wind surface shapes with
 * Earth's land and water.
 * <p/>
 * This example provides an editable surface circle indicating a region to clip against either land or water. The land
 * and water are represented by an ESRI shapefile containing polygons of Earth's continents, including major islands.
 * Clipping against land is accomplished by computing the intersection of the surface circle and the shapefile polygons.
 * Clipping against water is accomplished by subtracting the shapefile polygons from the surface circle. The user
 * specifies the location of the surface circle, whether to clip against land or water, and the desired resolution of
 * the resultant shape, in kilometers.
 *
 * @author dcollins
 * @version $Id: ShapeClipping.java 2411 2014-10-30 21:27:00Z dcollins $
 */
public class ShapeClipping extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame implements SelectListener
    {
        protected ShapeEditor editor;
        protected ShapeAttributes lastAttrs;
        protected ShapeClippingPanel clippingPanel;

        public AppFrame()
        {
            this.clippingPanel = new ShapeClippingPanel(this.getWwd());
            this.getControlPanel().add(this.clippingPanel, BorderLayout.SOUTH);

            this.createLandShape();
            this.createClipShape();
        }

        protected void createLandShape()
        {
            ShapefileLayerFactory factory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(
                AVKey.SHAPEFILE_LAYER_FACTORY);

            factory.createFromShapefileSource("testData/shapefiles/ne_10m_land.shp",
                new ShapefileLayerFactory.CompletionCallback()
                {
                    @Override
                    public void completion(final Object result)
                    {
                        if (!SwingUtilities.isEventDispatchThread())
                        {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    completion(result);
                                }
                            });
                            return;
                        }

                        RenderableLayer layer = (RenderableLayer) result;
                        Renderable renderable = layer.getRenderables().iterator().next();
                        clippingPanel.setLandShape((Combinable) renderable);
                    }

                    @Override
                    public void exception(Exception e)
                    {
                        Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                    }
                });
        }

        protected void createClipShape()
        {
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setInteriorOpacity(0.3);
            attrs.setOutlineMaterial(new Material(Color.RED));
            attrs.setOutlineWidth(2);

            ShapeAttributes highlightAttrs = new BasicShapeAttributes(attrs);
            highlightAttrs.setInteriorOpacity(0.6);
            highlightAttrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.RED)));
            highlightAttrs.setOutlineWidth(4);

            SurfaceCircle circle = new SurfaceCircle(attrs, LatLon.fromDegrees(42.5, -116), 1e6);
            circle.setHighlightAttributes(highlightAttrs);
            this.clippingPanel.setClipShape(circle);

            RenderableLayer shapeLayer = new RenderableLayer();
            shapeLayer.setName("Clipping Shape");
            shapeLayer.addRenderable(circle);
            this.getWwd().getModel().getLayers().add(shapeLayer);
            this.getWwd().addSelectListener(this);
        }

        @Override
        public void selected(SelectEvent event)
        {
            // This select method identifies the shape to edit.

            PickedObject topObject = event.getTopPickedObject();

            if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
            {
                if (topObject != null && topObject.getObject() instanceof Renderable)
                {
                    if (this.editor == null)
                    {
                        // Enable editing of the selected shape.
                        this.editor = new ShapeEditor(getWwd(), (Renderable) topObject.getObject());
                        this.editor.setArmed(true);
                        this.keepShapeHighlighted(true);
                        event.consume();
                    }
                    else if (this.editor.getShape() != event.getTopObject())
                    {
                        // Switch editor to a different shape.
                        this.keepShapeHighlighted(false);
                        this.editor.setArmed(false);
                        this.editor = new ShapeEditor(getWwd(), (Renderable) topObject.getObject());
                        this.editor.setArmed(true);
                        this.keepShapeHighlighted(true);
                        event.consume();
                    }
                    else if ((event.getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == 0
                        && (event.getMouseEvent().getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == 0)
                    {
                        // Disable editing of the current shape. Shift and Alt are used by the editor, so ignore
                        // events with those buttons down.
                        this.editor.setArmed(false);
                        this.keepShapeHighlighted(false);
                        this.editor = null;
                        event.consume();
                    }
                }
            }
        }

        protected void keepShapeHighlighted(boolean tf)
        {
            if (tf)
            {
                this.lastAttrs = ((Attributable) this.editor.getShape()).getAttributes();
                ((Attributable) this.editor.getShape()).setAttributes(
                    ((Attributable) this.editor.getShape()).getHighlightAttributes());
            }
            else
            {
                ((Attributable) this.editor.getShape()).setAttributes(this.lastAttrs);
            }
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Shape Clipping", AppFrame.class);
    }
}
