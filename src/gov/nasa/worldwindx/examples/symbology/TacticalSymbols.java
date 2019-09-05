/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.symbology;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525TacticalSymbol;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Demonstrates how to create and display WorldWind tactical symbols. See the 
 * <a href="https://worldwind.arc.nasa.gov/java/tutorials/tactical-graphics/" target="_blank">Tutorial</a>
 * for more
 * information on symbology support in WorldWind.
 * <p>
 * See the {@link TacticalGraphics} for a detailed example of using WorldWind tactical graphics in an application.
 *
 * @author dcollins
 * @version $Id: TacticalSymbols.java 2196 2014-08-06 19:42:15Z tgaskins $
 */
public class TacticalSymbols extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected RenderableLayer symbolLayer;
        protected TacticalSymbolAttributes sharedAttrs;
        protected TacticalSymbolAttributes sharedHighlightAttrs;
        protected BasicDragger dragger;

        public AppFrame()
        {
            // Create a renderable layer to display the tactical symbols. This example adds only three symbols, but many
            // symbols can be added to a single layer. Note that tactical symbols and tactical graphics can be combined
            // in the same RenderableLayer, along with any WorldWind object implementing the Renderable interface.
            this.symbolLayer = new RenderableLayer();
            this.symbolLayer.setName("Tactical Symbols");

            // Create normal and highlight attribute bundles that are shared by all tactical symbols. Changes to these
            // attribute bundles are reflected in all symbols. We specify both attribute bundle types in this example in
            // order to keep a symbol's scale constant when it's highlighted, and change only its opacity.
            this.sharedAttrs = new BasicTacticalSymbolAttributes();
            this.sharedAttrs.setTextModifierMaterial(Material.RED);
            this.sharedHighlightAttrs = new BasicTacticalSymbolAttributes();
            this.sharedHighlightAttrs.setInteriorMaterial(Material.WHITE);
            this.sharedHighlightAttrs.setTextModifierMaterial(Material.WHITE);
            this.sharedHighlightAttrs.setOpacity(1.0);

            // Create an air tactical symbol for the MIL-STD-2525 symbology set. This symbol identifier specifies a
            // MIL-STD-2525 friendly Special Operations Forces Drone Aircraft. MilStd2525TacticalSymbol automatically
            // sets the altitude mode to WorldWind.ABSOLUTE. We've configured this symbol's modifiers to display the
            // represented object's Echelon, Task Force Indicator, Feint/Dummy Indicator, and Direction of Movement.
            // The Echelon, Task Force Indicator, and Feint/Dummy Indicator are specified in characters 11-12 of the
            // symbol identifier ("GI"). The Direction of Movement is specified by calling TacticalSymbol.setModifier
            // with the appropriate key and value.
            TacticalSymbol airSymbol = new MilStd2525TacticalSymbol("SFAPMFQM--GIUSA",
                Position.fromDegrees(32.4520, 63.44553, 3000));
            airSymbol.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Friendly SOF Drone Aircraft"); // Tool tip text.
            airSymbol.setAttributes(this.sharedAttrs);
            airSymbol.setHighlightAttributes(this.sharedHighlightAttrs);
            airSymbol.setModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(235));
            airSymbol.setShowLocation(false);
            this.symbolLayer.addRenderable(airSymbol);

            // Create a ground tactical symbol for the MIL-STD-2525 symbology set. This symbol identifier specifies
            // multiple hostile Self-Propelled Rocket Launchers with a destroyed state. MilStd2525TacticalSymbol
            // automatically sets the altitude mode to WorldWind.CLAMP_TO_GROUND. We've configured this symbol's
            // modifiers to display the represented object's Operational Condition, Direction of Movement and Speed
            // Leader. The Operational Condition is specified in character 4 of the symbol identifier ("X"). The
            // Direction of Movement and Speed Leader are specified by calling TacticalSymbol.setModifier with the
            // appropriate key and value.The Speed Leader modifier has the effect of scaling the Direction of Movement's
            // line segment. In this example, we've scaled the line to 50% of its original length.
            TacticalSymbol groundSymbol = new MilStd2525TacticalSymbol("SHGXUCFRMS----G",
                Position.fromDegrees(32.4014, 63.3894, 0));
            groundSymbol.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Hostile Self-Propelled Rocket Launchers");
            groundSymbol.setAttributes(this.sharedAttrs);
            groundSymbol.setHighlightAttributes(this.sharedHighlightAttrs);
            groundSymbol.setModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(90));
            groundSymbol.setModifier(SymbologyConstants.SPEED_LEADER_SCALE, 0.5);
            groundSymbol.setShowLocation(false);
            this.symbolLayer.addRenderable(groundSymbol);

            // Create a ground tactical symbol for the MIL-STD-2525 symbology set. This symbol identifier specifies a
            // MIL-STD-2525 friendly Heavy Machine Gun that's currently mobilized via rail. This symbol is taken from
            // the MIL-STD-2525C specification, section 5.9.3 (page 49). We've configured this symbol's modifiers to
            // display the Mobility Indicator, and six text modifiers. The Mobility Indicator is specified in characters
            // 11-12 of the symbol identifier ("MT"). The text modifiers are specified by calling
            // TacticalSymbol.setModifier with the appropriate keys and values.
            TacticalSymbol machineGunSymbol = new MilStd2525TacticalSymbol("SFGPEWRH--MTUSG",
                Position.fromDegrees(32.3902, 63.4161, 0));
            machineGunSymbol.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Friendly Heavy Machine Gun");
            machineGunSymbol.setAttributes(this.sharedAttrs);
            machineGunSymbol.setHighlightAttributes(this.sharedHighlightAttrs);
            machineGunSymbol.setModifier(SymbologyConstants.QUANTITY, 200);
            machineGunSymbol.setModifier(SymbologyConstants.STAFF_COMMENTS, "FOR REINFORCEMENTS");
            machineGunSymbol.setModifier(SymbologyConstants.ADDITIONAL_INFORMATION, "ADDED SUPPORT FOR JJ");
            machineGunSymbol.setModifier(SymbologyConstants.TYPE, "MACHINE GUN");
            machineGunSymbol.setModifier(SymbologyConstants.DATE_TIME_GROUP, "30140000ZSEP97");
            this.symbolLayer.addRenderable(machineGunSymbol);

            // Add the same symbol at the dateline to test that all aspects display correctly there.
            TacticalSymbol machineGunSymbolAtDateline = new MilStd2525TacticalSymbol("SFGPEWRH--MTUSG",
                Position.fromDegrees(32.3902, 180, 0));
            machineGunSymbolAtDateline.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Friendly Heavy Machine Gun at Dateline");
            machineGunSymbolAtDateline.setAttributes(this.sharedAttrs);
            machineGunSymbolAtDateline.setHighlightAttributes(this.sharedHighlightAttrs);
            machineGunSymbolAtDateline.setModifier(SymbologyConstants.QUANTITY, 200);
            machineGunSymbolAtDateline.setModifier(SymbologyConstants.STAFF_COMMENTS, "FOR REINFORCEMENTS");
            machineGunSymbolAtDateline.setModifier(SymbologyConstants.ADDITIONAL_INFORMATION, "ADDED SUPPORT FOR JJ");
            machineGunSymbolAtDateline.setModifier(SymbologyConstants.TYPE, "MACHINE GUN");
            machineGunSymbolAtDateline.setModifier(SymbologyConstants.DATE_TIME_GROUP, "30140000ZSEP97");
            this.symbolLayer.addRenderable(machineGunSymbolAtDateline);

            // Add the symbol layer to the WorldWind model.
            this.getWwd().getModel().getLayers().add(symbolLayer);

            // Add a dragging controller to enable user click-and-drag control over tactical symbols.
            this.dragger = new BasicDragger(this.getWwd());
            this.getWwd().addSelectListener(this.dragger);

            // Create a Swing control panel that provides user control over the symbol's appearance.
            this.addSymbolControls();

            // Size the WorldWindow to provide enough screen space for the symbols and center the WorldWindow on the
            // screen.
            Dimension size = new Dimension(1800, 1000);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void addSymbolControls()
        {
            Box box = Box.createVerticalBox();
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
                    getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
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
                    sharedAttrs.setOpacity(opacity);
                    getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                }
            });
            box.add(Box.createVerticalStrut(10));
            label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            slider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(label);
            box.add(slider);

            // Create a check box that toggles the visibility of graphic modifiers for all symbols.
            JCheckBox cb = new JCheckBox("Graphic Modifiers", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : symbolLayer.getRenderables())
                    {
                        if (r instanceof TacticalSymbol)
                            ((TacticalSymbol) r).setShowGraphicModifiers(tf);
                        getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the visibility of text modifiers for all symbols.
            cb = new JCheckBox("Text Modifiers", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : symbolLayer.getRenderables())
                    {
                        if (r instanceof TacticalSymbol)
                            ((TacticalSymbol) r).setShowTextModifiers(tf);
                        getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the frame visibility for all symbols.
            cb = new JCheckBox("Show Frame", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : symbolLayer.getRenderables())
                    {
                        if (r instanceof TacticalSymbol)
                            ((MilStd2525TacticalSymbol) r).setShowFrame(tf);
                        getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the fill visibility for all symbols.
            cb = new JCheckBox("Show Fill", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : symbolLayer.getRenderables())
                    {
                        if (r instanceof TacticalSymbol)
                            ((MilStd2525TacticalSymbol) r).setShowFill(tf);
                        getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the icon visibility for all symbols.
            cb = new JCheckBox("Show Icon", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    for (Renderable r : symbolLayer.getRenderables())
                    {
                        if (r instanceof TacticalSymbol)
                            ((MilStd2525TacticalSymbol) r).setShowIcon(tf);
                        getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(Box.createVerticalStrut(10));
            box.add(cb);

            this.getControlPanel().add(box, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that this example starts looking at the symbols.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 32.49);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, 63.455);
        Configuration.setValue(AVKey.INITIAL_HEADING, 22);
        Configuration.setValue(AVKey.INITIAL_PITCH, 82);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 20000);

        start("WorldWind Tactical Symbols", AppFrame.class);
    }
}
