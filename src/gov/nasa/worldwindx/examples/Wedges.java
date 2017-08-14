/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * Example of {@link gov.nasa.worldwind.render.Wedge} usage. Shows examples of pyramids with various orientations,
 * materials, and textures applied.
 *
 * @author ccrick
 * @version $Id: Wedges.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Wedges extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add detail hint slider panel
            this.getControlPanel().add(makeDetailHintControlPanel(), BorderLayout.SOUTH);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.YELLOW);
            attrs.setInteriorOpacity(0.7);
            attrs.setEnableLighting(true);
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);
            attrs.setDrawInterior(true);
            attrs.setDrawOutline(false);

            // Create and set an attribute bundle.
            ShapeAttributes attrs2 = new BasicShapeAttributes();
            attrs2.setInteriorMaterial(Material.PINK);
            attrs2.setInteriorOpacity(1);
            attrs2.setEnableLighting(true);
            attrs2.setOutlineMaterial(Material.WHITE);
            attrs2.setOutlineWidth(2d);
            attrs2.setDrawOutline(false);

            // ********* sample  Wedges  *******************

            // Wedge with equal axes, ABSOLUTE altitude mode
            Wedge wedge3 = new Wedge(Position.fromDegrees(40, -120, 80000), Angle.POS90, 50000, 50000, 50000);
            wedge3.setAltitudeMode(WorldWind.ABSOLUTE);
            wedge3.setAttributes(attrs);
            wedge3.setVisible(true);
            wedge3.setValue(AVKey.DISPLAY_NAME, "Wedge with equal axes, ABSOLUTE altitude mode");
            layer.addRenderable(wedge3);

            // Wedge with equal axes, RELATIVE_TO_GROUND
            Wedge wedge4 = new Wedge(Position.fromDegrees(37.5, -115, 50000), Angle.POS90, 50000, 50000, 50000);
            wedge4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            wedge4.setAttributes(attrs);
            wedge4.setVisible(true);
            wedge4.setValue(AVKey.DISPLAY_NAME, "Wedge with equal axes, RELATIVE_TO_GROUND altitude mode");
            layer.addRenderable(wedge4);

            // Wedge with equal axes, CLAMP_TO_GROUND
            Wedge wedge5 = new Wedge(Position.fromDegrees(35, -110, 50000), Angle.POS90, 50000, 50000, 50000);
            wedge5.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            wedge5.setAttributes(attrs);
            wedge5.setVisible(true);
            wedge5.setValue(AVKey.DISPLAY_NAME, "Wedge with equal axes, CLAMP_TO_GROUND altitude mode");
            layer.addRenderable(wedge5);

            // Wedge with a texture, using Wedge(position, angle, height, radius) constructor
            Wedge wedge9 = new Wedge(Position.fromDegrees(0, -90, 600000), Angle.fromDegrees(225), 1200000, 600000);
            wedge9.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            wedge9.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            wedge9.setAttributes(attrs);
            wedge9.setVisible(true);
            wedge9.setValue(AVKey.DISPLAY_NAME, "Wedge with a texture");
            layer.addRenderable(wedge9);

            // Scaled Wedge with default orientation
            Wedge wedge = new Wedge(Position.ZERO, Angle.fromDegrees(125), 500000, 500000, 500000);
            wedge.setAltitudeMode(WorldWind.ABSOLUTE);
            wedge.setAttributes(attrs);
            wedge.setVisible(true);
            wedge.setValue(AVKey.DISPLAY_NAME, "Scaled Wedge with default orientation");
            layer.addRenderable(wedge);

            // Scaled Wedge with a pre-set orientation
            Wedge wedge2 = new Wedge(Position.fromDegrees(0, 30, 750000), Angle.POS90, 500000, 500000, 500000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            wedge2.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            wedge2.setAttributes(attrs2);
            wedge2.setVisible(true);
            wedge2.setValue(AVKey.DISPLAY_NAME, "Scaled Wedge with a pre-set orientation");
            layer.addRenderable(wedge2);

            // Scaled Wedge with a pre-set orientation
            Wedge wedge6 = new Wedge(Position.fromDegrees(30, 30, 750000), Angle.POS90, 500000, 500000, 500000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            wedge6.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            wedge6.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            wedge6.setAttributes(attrs2);
            wedge6.setVisible(true);
            wedge6.setValue(AVKey.DISPLAY_NAME, "Scaled Wedge with a pre-set orientation");
            layer.addRenderable(wedge6);

            // Scaled Wedge with a pre-set orientation
            Wedge wedge7 = new Wedge(Position.fromDegrees(60, 30, 750000), Angle.POS90, 500000, 500000, 500000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            wedge7.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            wedge7.setAttributes(attrs2);
            wedge7.setVisible(true);
            wedge7.setValue(AVKey.DISPLAY_NAME, "Scaled Wedge with a pre-set orientation");
            layer.addRenderable(wedge7);

            // Scaled, oriented Wedge in 3rd "quadrant" (-X, -Y, -Z)
            Wedge wedge8 = new Wedge(Position.fromDegrees(-45, -180, 750000), Angle.POS90, 500000, 1000000, 500000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            wedge8.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            wedge8.setAttributes(attrs2);
            wedge8.setVisible(true);
            wedge8.setValue(AVKey.DISPLAY_NAME, "Scaled, oriented Wedge with in the 3rd 'quadrant' (-X, -Y, -Z)");
            layer.addRenderable(wedge8);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }

        protected JPanel makeDetailHintControlPanel()
        {
            JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Detail Hint")));

            JPanel elevationSliderPanel = new JPanel(new BorderLayout(0, 5));
            {
                int MIN = -10;
                int MAX = 10;
                int cur = 0;
                JSlider slider = new JSlider(MIN, MAX, cur);
                slider.setMajorTickSpacing(10);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put(-10, new JLabel("-1.0"));
                labelTable.put(0, new JLabel("0.0"));
                labelTable.put(10, new JLabel("1.0"));
                slider.setLabelTable(labelTable);
                slider.setPaintLabels(true);
                slider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        double hint = ((JSlider) e.getSource()).getValue() / 10d;
                        setWedgeDetailHint(hint);
                        getWwd().redraw();
                    }
                });
                elevationSliderPanel.add(slider, BorderLayout.SOUTH);
            }

            JPanel sliderPanel = new JPanel(new GridLayout(2, 0));
            sliderPanel.add(elevationSliderPanel);

            controlPanel.add(sliderPanel, BorderLayout.SOUTH);
            return controlPanel;
        }

        protected RenderableLayer getLayer()
        {
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer.getName().contains("Renderable"))
                {
                    return (RenderableLayer) layer;
                }
            }

            return null;
        }

        protected void setWedgeDetailHint(double hint)
        {
            for (Renderable renderable : getLayer().getRenderables())
            {
                Wedge current = (Wedge) renderable;
                current.setDetailHint(hint);
            }
            System.out.println("wedge detail hint set to " + hint);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Wedges", AppFrame.class);
    }
}

