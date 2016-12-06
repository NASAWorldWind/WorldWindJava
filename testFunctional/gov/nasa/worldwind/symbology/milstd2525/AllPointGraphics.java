/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Display all MIL-STD-2525 point graphics in a grid so that a human operator can confirm that they are rendered
 * correctly.
 *
 * @author pabercrombie
 * @version $Id: AllPointGraphics.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class AllPointGraphics extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected RenderableLayer tacgrpLayer;
        protected RenderableLayer metocLayer;
        protected RenderableLayer emsLayer;

        protected TacticalGraphicAttributes sharedAttrs;
        protected TacticalGraphicAttributes sharedHighlightAttrs;

        public AppFrame()
        {
            super(true, true, false);

            this.tacgrpLayer = new RenderableLayer();
            this.tacgrpLayer.setName("Tactical Graphics (Appendix B)");

            this.metocLayer = new RenderableLayer();
            this.metocLayer.setName("METOC (Appendix C)");
            this.metocLayer.setEnabled(false);

            this.emsLayer = new RenderableLayer();
            this.emsLayer.setName("Emergency Management (Appendix G)");
            this.emsLayer.setEnabled(false);

            this.sharedAttrs = new BasicTacticalGraphicAttributes();
            this.sharedHighlightAttrs = new BasicTacticalGraphicAttributes();
            this.sharedHighlightAttrs.setInteriorMaterial(Material.WHITE);

            this.createTacGrpPoints(this.tacgrpLayer);
            this.createMetocPoints(this.metocLayer);
            this.createEmsPoints(this.emsLayer);

            WorldWindow wwd = this.getWwd();
            insertBeforePlacenames(wwd, this.tacgrpLayer);
            insertBeforePlacenames(wwd, this.metocLayer);
            insertBeforePlacenames(wwd, this.emsLayer);

            this.addGraphicControls();

            // Size the World Window to provide enough screen space for the graphics, and center the World Window
            // on the screen.
            Dimension size = new Dimension(1800, 1000);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void createTacGrpPoints(RenderableLayer layer)
        {
            List<String> allGraphics = MilStd2525PointGraphic.getTacGrpGraphics();
            int numGraphics = allGraphics.size();
            int cols = (int) Math.sqrt(numGraphics * 2);

            double startLon = -118.5439;

            double latitude = 43.3464;
            double longitude = startLon;
            double delta = 0.02;

            for (int i = 0; i < numGraphics; i++)
            {
                Position pos = Position.fromDegrees(latitude, longitude, 0);

                StringBuilder sidc = new StringBuilder(allGraphics.get(i));

                sidc.setCharAt(1, 'F'); // Standard identify: Friend
                sidc.setCharAt(3, 'P'); // Present

                TacticalPoint graphic = new MilStd2525PointGraphic(sidc.toString());
                graphic.setPosition(pos);

                // Set all modifiers, even on graphics that do not support them. This allows us to confirm
                // that modifiers are only drawn for graphics that ^do^ support them.
                graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, Arrays.asList("T", "T1"));
                graphic.setModifier(SymbologyConstants.ADDITIONAL_INFORMATION, Arrays.asList("H", "H1"));
                graphic.setModifier(SymbologyConstants.ALTITUDE_DEPTH, "X");
                graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("W", "W1"));
                graphic.setModifier(SymbologyConstants.QUANTITY, "C"); // Only applies to Nuclear graphic
                graphic.setModifier(SymbologyConstants.TYPE, "V"); // Applies only to Nuclear graphic

                // Location and Direction of Movement apply only to CBRN graphics.
                graphic.setModifier(SymbologyConstants.LOCATION, "Y");
                graphic.setModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(45));

                graphic.setAttributes(this.sharedAttrs);
                graphic.setHighlightAttributes(this.sharedHighlightAttrs);

                graphic.setValue(AVKey.DISPLAY_NAME, sidc.toString());

                layer.addRenderable(graphic);

                if ((i + 1) % cols == 0)
                {
                    latitude -= delta;
                    longitude = startLon;
                }
                else
                {
                    longitude += delta;
                }
            }
        }

        protected void createMetocPoints(RenderableLayer layer)
        {
            List<String> allGraphics = MilStd2525PointGraphic.getMetocGraphics();
            int numGraphics = allGraphics.size();
            int cols = (int) Math.sqrt(numGraphics);

            double startLon = -118.5439;

            double latitude = 43.3464;
            double longitude = startLon;
            double delta = 0.02;

            for (int i = 0; i < numGraphics; i++)
            {
                String sidc = allGraphics.get(i);
                Position pos = Position.fromDegrees(latitude, longitude, 0);

                TacticalPoint graphic = new MilStd2525PointGraphic(sidc);
                graphic.setPosition(pos);

                graphic.setAttributes(this.sharedAttrs);
                graphic.setHighlightAttributes(this.sharedHighlightAttrs);

                graphic.setValue(AVKey.DISPLAY_NAME, sidc);

                layer.addRenderable(graphic);

                if ((i + 1) % cols == 0)
                {
                    latitude -= delta;
                    longitude = startLon;
                }
                else
                {
                    longitude += delta;
                }
            }
        }

        protected void createEmsPoints(RenderableLayer layer)
        {
            List<String> allGraphics = MilStd2525PointGraphic.getEmsGraphics();
            int numGraphics = allGraphics.size();
            int cols = (int) Math.sqrt(numGraphics * 2);

            double startLon = -118.5439;

            double latitude = 43.3464;
            double longitude = startLon;
            double delta = 0.02;

            for (int i = 0; i < numGraphics; i++)
            {
                Position pos = Position.fromDegrees(latitude, longitude, 0);

                StringBuilder sidc = new StringBuilder(allGraphics.get(i));

                sidc.setCharAt(1, 'F'); // Standard identify: Friend
                sidc.setCharAt(3, 'A');

                TacticalPoint graphic = new MilStd2525PointGraphic(sidc.toString());
                graphic.setPosition(pos);

                graphic.setAttributes(this.sharedAttrs);
                graphic.setHighlightAttributes(this.sharedHighlightAttrs);

                graphic.setValue(AVKey.DISPLAY_NAME, sidc.toString());

                layer.addRenderable(graphic);

                if ((i + 1) % cols == 0)
                {
                    latitude -= delta;
                    longitude = startLon;
                }
                else
                {
                    longitude += delta;
                }
            }
        }

        protected void addGraphicControls()
        {
            javax.swing.Box box = javax.swing.Box.createVerticalBox();
            box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Create a slider that controls the scale factor of all symbols.
            JLabel label = new JLabel("Scale");
            JSlider slider = new JSlider(0, 100, 100);
            slider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                    // Scale both the normal and the highlight attributes for each symbol. This prevents the symbol
                    // from suddenly appearing larger when highlighted. Changes in these attributes are reflected in all
                    // symbols that use them.
                    JSlider slider = (JSlider) changeEvent.getSource();
                    double scale = (double) slider.getValue() / 100d;
                    sharedAttrs.setScale(scale);
                    sharedHighlightAttrs.setScale(scale);
                    getWwd().redraw(); // Cause the World Window to refresh in order to make these changes visible.
                }
            });
            label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            slider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(label);
            box.add(slider);

            // Create a slider that controls the opacity of all symbols.
            label = new JLabel("Opacity");
            slider = new JSlider(0, 100, 100);
            slider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                    // Set the opacity for only the normal attributes. This causes symbols to return to 100% opacity
                    // when highlighted. Changes in these attributes are reflected in all symbols that use them.
                    JSlider slider = (JSlider) changeEvent.getSource();
                    double opacity = (double) slider.getValue() / 100d;
                    sharedAttrs.setInteriorOpacity(opacity);
                    getWwd().redraw(); // Cause the World Window to refresh in order to make these changes visible.
                }
            });
            box.add(javax.swing.Box.createVerticalStrut(10));
            label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            slider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(label);
            box.add(slider);

            // Create a check box that toggles the visibility of text modifiers for all symbols.
            JCheckBox cb = new JCheckBox("Show text modifiers", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : tacgrpLayer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowTextModifiers(tf);
                    }
                    for (Renderable r : metocLayer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowTextModifiers(tf);
                    }
                    getWwd().redraw(); // Cause the World Window to refresh in order to make these changes visible.
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the visibility of graphic modifiers for all symbols.
            cb = new JCheckBox("Show graphic modifiers", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : tacgrpLayer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowGraphicModifiers(tf);
                    }
                    for (Renderable r : metocLayer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowGraphicModifiers(tf);
                    }
                    getWwd().redraw(); // Cause the World Window to refresh in order to make these changes visible.
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the visibility of the hostile/enemy indicator.
            cb = new JCheckBox("Show hostile indicator", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : tacgrpLayer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowHostileIndicator(tf);
                    }
                    for (Renderable r : metocLayer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowHostileIndicator(tf);
                    }
                    getWwd().redraw(); // Cause the World Window to refresh in order to make these changes visible.
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the visibility of the location modifier.
            cb = new JCheckBox("Show location", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : tacgrpLayer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowLocation(tf);
                    }
                    getWwd().redraw(); // Cause the World Window to refresh in order to make these changes visible.
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the visibility of the location modifier.
            cb = new JCheckBox("Present/anticipated", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : tacgrpLayer.getRenderables())
                    {
                        String status = tf ? SymbologyConstants.STATUS_PRESENT : SymbologyConstants.STATUS_ANTICIPATED;
                        if (r instanceof MilStd2525TacticalGraphic)
                            ((MilStd2525TacticalGraphic) r).setStatus(status);
                    }
                    getWwd().redraw(); // Cause the World Window to refresh in order to make these changes visible.
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            this.getControlPanel().add(box, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 43.3046);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -118.4961);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 20000);

        ApplicationTemplate.start("World Wind MIL-STD2525 Tactical Point Graphics", AppFrame.class);
    }
}
