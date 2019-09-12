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
 * Illustrates how to use the WorldWind <code>{@link Ellipsoid}</code> rigid shape to display an arbitrarily sized and
 * oriented ellipsoid at a geographic position on the Globe.
 *
 * @author ccrick
 * @version $Id: Ellipsoids.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Ellipsoids extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
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

            // ********* sample  Ellipsoids  *******************

            // Ellipsoid with equal axes, ABSOLUTE altitude mode
            Ellipsoid ellipsoid3 = new Ellipsoid(Position.fromDegrees(40, -120, 80000), 50000, 50000, 50000);
            ellipsoid3.setAltitudeMode(WorldWind.ABSOLUTE);
            ellipsoid3.setAttributes(attrs);
            ellipsoid3.setVisible(true);
            ellipsoid3.setValue(AVKey.DISPLAY_NAME, "Ellipsoid with equal axes, ABSOLUTE altitude mode");
            layer.addRenderable(ellipsoid3);

            // Ellipsoid with equal axes, RELATIVE_TO_GROUND
            Ellipsoid ellipsoid4 = new Ellipsoid(Position.fromDegrees(37.5, -115, 50000), 50000, 50000, 50000);
            ellipsoid4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            ellipsoid4.setAttributes(attrs);
            ellipsoid4.setVisible(true);
            ellipsoid4.setValue(AVKey.DISPLAY_NAME, "Ellipsoid with equal axes, RELATIVE_TO_GROUND altitude mode");
            layer.addRenderable(ellipsoid4);

            // Ellipsoid with equal axes, CLAMP_TO_GROUND
            Ellipsoid ellipsoid5 = new Ellipsoid(Position.fromDegrees(35, -110, 50000), 50000, 50000, 50000);
            ellipsoid5.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            ellipsoid5.setAttributes(attrs);
            ellipsoid5.setVisible(true);
            ellipsoid5.setValue(AVKey.DISPLAY_NAME, "Ellipsoid with equal axes, CLAMP_TO_GROUND altitude mode");
            layer.addRenderable(ellipsoid5);

            // Ellipsoid with a texture
            Ellipsoid ellipsoid9 = new Ellipsoid(Position.fromDegrees(0, -90, 600000), 600000, 600000, 600000);
            ellipsoid9.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            ellipsoid9.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            ellipsoid9.setAttributes(attrs);
            ellipsoid9.setVisible(true);
            ellipsoid9.setValue(AVKey.DISPLAY_NAME, "Ellipsoid with a texture");
            layer.addRenderable(ellipsoid9);

            // Scaled Ellipsoid with default orientation
            Ellipsoid ellipsoid = new Ellipsoid(Position.ZERO, 1000000, 500000, 100000);
            ellipsoid.setAltitudeMode(WorldWind.ABSOLUTE);
            ellipsoid.setAttributes(attrs);
            ellipsoid.setVisible(true);
            ellipsoid.setValue(AVKey.DISPLAY_NAME, "Scaled Ellipsoid with default orientation");
            layer.addRenderable(ellipsoid);

            // Scaled Ellipsoid with a pre-set orientation
            Ellipsoid ellipsoid2 = new Ellipsoid(Position.fromDegrees(0, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            ellipsoid2.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            ellipsoid2.setAttributes(attrs2);
            ellipsoid2.setVisible(true);
            ellipsoid2.setValue(AVKey.DISPLAY_NAME, "Scaled Ellipsoid with a pre-set orientation");
            layer.addRenderable(ellipsoid2);

            // Scaled Ellipsoid with a pre-set orientation
            Ellipsoid ellipsoid6 = new Ellipsoid(Position.fromDegrees(30, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            ellipsoid6.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            ellipsoid6.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            ellipsoid6.setAttributes(attrs2);
            ellipsoid6.setVisible(true);
            ellipsoid6.setValue(AVKey.DISPLAY_NAME, "Scaled Ellipsoid with a pre-set orientation");
            layer.addRenderable(ellipsoid6);

            // Scaled Ellipsoid with a pre-set orientation
            Ellipsoid ellipsoid7 = new Ellipsoid(Position.fromDegrees(60, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            ellipsoid7.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            ellipsoid7.setAttributes(attrs2);
            ellipsoid7.setVisible(true);
            ellipsoid7.setValue(AVKey.DISPLAY_NAME, "Scaled Ellipsoid with a pre-set orientation");
            layer.addRenderable(ellipsoid7);

            // Scaled, oriented Ellipsoid in 3rd "quadrant" (-X, -Y, -Z)
            Ellipsoid ellipsoid8 = new Ellipsoid(Position.fromDegrees(-45, -180, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            ellipsoid8.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            ellipsoid8.setAttributes(attrs2);
            ellipsoid8.setVisible(true);
            ellipsoid8.setValue(AVKey.DISPLAY_NAME, "Scaled, oriented Ellipsoid in the 3rd 'quadrant' (-X, -Y, -Z)");
            layer.addRenderable(ellipsoid8);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }

        protected JPanel makeDetailHintControlPanel()
        {
            JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Detail Hint")));

            JPanel detailHintSliderPanel = new JPanel(new BorderLayout(0, 5));
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
                        setEllipsoidDetailHint(hint);
                        getWwd().redraw();
                    }
                });
                detailHintSliderPanel.add(slider, BorderLayout.SOUTH);
            }

            JPanel sliderPanel = new JPanel(new GridLayout(2, 0));
            sliderPanel.add(detailHintSliderPanel);

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

        protected void setEllipsoidDetailHint(double hint)
        {
            for (Renderable renderable : getLayer().getRenderables())
            {
                Ellipsoid current = (Ellipsoid) renderable;
                current.setDetailHint(hint);
            }
            System.out.println("Ellipsoid detail hint set to " + hint);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Ellipsoids", AppFrame.class);
    }
}
