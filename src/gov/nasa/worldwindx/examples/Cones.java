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
 * Illustrates how to use the WorldWind <code>{@link Cone}</code> rigid shape to display an arbitrarily sized and
 * oriented cone at a geographic position on the Globe.
 *
 * @author ccrick
 * @version $Id: Cones.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Cones extends ApplicationTemplate
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

            // ********* sample  Cones  *******************

            // Cone with equal axes, ABSOLUTE altitude mode
            Cone cone3 = new Cone(Position.fromDegrees(40, -120, 80000), 100000, 50000);
            cone3.setAltitudeMode(WorldWind.ABSOLUTE);
            cone3.setAttributes(attrs);
            cone3.setVisible(true);
            cone3.setValue(AVKey.DISPLAY_NAME, "Cone with equal axes, ABSOLUTE altitude mode");
            layer.addRenderable(cone3);

            // Cone with equal axes, RELATIVE_TO_GROUND
            Cone cone4 = new Cone(Position.fromDegrees(37.5, -115, 50000), 50000, 50000, 50000);
            cone4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cone4.setAttributes(attrs);
            cone4.setVisible(true);
            cone4.setValue(AVKey.DISPLAY_NAME, "Cone with equal axes, RELATIVE_TO_GROUND altitude mode");
            layer.addRenderable(cone4);

            // Cone with equal axes, CLAMP_TO_GROUND
            Cone cone5 = new Cone(Position.fromDegrees(35, -110, 50000), 50000, 50000, 50000);
            cone5.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            cone5.setAttributes(attrs);
            cone5.setVisible(true);
            cone5.setValue(AVKey.DISPLAY_NAME, "Cone with equal axes, CLAMP_TO_GROUND altitude mode");
            layer.addRenderable(cone5);

            // Cone with a texture, using Cone(position, height, radius) constructor
            Cone cone9 = new Cone(Position.fromDegrees(0, -90, 600000), 1200000, 600000);
            cone9.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cone9.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            cone9.setAttributes(attrs);
            cone9.setVisible(true);
            cone9.setValue(AVKey.DISPLAY_NAME, "Cone with a texture");
            layer.addRenderable(cone9);

            // Scaled Cone with default orientation
            Cone cone = new Cone(Position.ZERO, 1000000, 500000, 100000);
            cone.setAltitudeMode(WorldWind.ABSOLUTE);
            cone.setAttributes(attrs);
            cone.setVisible(true);
            cone.setValue(AVKey.DISPLAY_NAME, "Scaled Cone with default orientation");
            layer.addRenderable(cone);

            // Scaled Cone with a pre-set orientation
            Cone cone2 = new Cone(Position.fromDegrees(0, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            cone2.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cone2.setAttributes(attrs2);
            cone2.setValue(AVKey.DISPLAY_NAME, "Scaled Cone with a pre-set orientation");
            cone2.setVisible(true);

            layer.addRenderable(cone2);

            // Scaled Cone with a pre-set orientation
            Cone cone6 = new Cone(Position.fromDegrees(30, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            cone6.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cone6.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            cone6.setAttributes(attrs2);
            cone6.setValue(AVKey.DISPLAY_NAME, "Scaled Cone with a pre-set orientation");
            cone6.setVisible(true);
            layer.addRenderable(cone6);

            // Scaled Cone with a pre-set orientation
            Cone cone7 = new Cone(Position.fromDegrees(60, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            cone7.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cone7.setAttributes(attrs2);
            cone7.setVisible(true);
            cone7.setValue(AVKey.DISPLAY_NAME, "Scaled Cone with a pre-set orientation");
            layer.addRenderable(cone7);

            // Scaled, oriented Cone in 3rd "quadrant" (-X, -Y, -Z)
            Cone cone8 = new Cone(Position.fromDegrees(-45, -180, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            cone8.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cone8.setAttributes(attrs2);
            cone8.setVisible(true);
            cone8.setValue(AVKey.DISPLAY_NAME, "Scaled, oriented Cone in the 3rd 'quadrant' (-X, -Y, -Z)");
            layer.addRenderable(cone8);

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
                        setConeDetailHint(hint);
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

        protected void setConeDetailHint(double hint)
        {
            for (Renderable renderable : getLayer().getRenderables())
            {
                Cone current = (Cone) renderable;
                current.setDetailHint(hint);
            }
            System.out.println("Cone detail hint set to " + hint);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Cones", AppFrame.class);
    }
}

