/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
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

import java.awt.*;

/**
 * Illustrates how to use a level of detail selector to declutter tactical symbols.
 *
 * @author tag
 * @version $Id: DeclutterTacticalSymbols.java 2366 2014-10-02 23:16:31Z tgaskins $
 */
public class DeclutterTacticalSymbols extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected RenderableLayer symbolLayer;
        protected TacticalSymbolAttributes sharedHighlightAttrs;

        public AppFrame()
        {
            /**
             * Define an LOD selector to adjust a symbol's attributes and properties during rendering in order to
             * declutter symbols.
             */
            TacticalSymbol.LODSelector lodSelector = new TacticalSymbol.LODSelector()
            {
                @Override
                public void selectLOD(DrawContext dc, TacticalSymbol symbol, double eyeDistance)
                {
                    // Show text modifiers only when eye distance is less than 50 km.
                    if (eyeDistance < 50e3)
                    {
                        symbol.setShowTextModifiers(true);
                    }
                    else
                    {
                        symbol.setShowTextModifiers(false);
                    }

                    // Scale the symbol when the eye distance is between 1 and 100 km. The symbol is scaled between
                    // 100% and 25% of its normal size.

                    // The scale is an attribute, so determine which attributes -- normal or highlight -- need to be
                    // set for this rendering.
                    TacticalSymbolAttributes attributes = symbol.isHighlighted()
                        ? symbol.getHighlightAttributes() : symbol.getAttributes();

                    double minScaleDistance = 1e3;
                    double maxScaleDistance = 100e3;

                    if (eyeDistance > minScaleDistance && eyeDistance < maxScaleDistance)
                    {
                        double scale = 0.25 + (maxScaleDistance - eyeDistance) / (maxScaleDistance - minScaleDistance);
                        attributes.setScale(scale);
                    }
                    else
                    {
                        attributes.setScale(1.0);
                    }

                    // Show only the symbol's alternate representation when the eye distance is greater than 100 km.
                    if (eyeDistance < 100e3)
                    {
                        symbol.setShowGraphicModifiers(true);
                        ((MilStd2525TacticalSymbol) symbol).setShowFrame(true);
                        ((MilStd2525TacticalSymbol) symbol).setShowIcon(true);
                    }
                    else
                    {
                        // Setting the symbol's show-frame and  show-icon properties to false causes the symbol's
                        // alternate representation to be drawn. The alternate representation is a filled circle.
                        symbol.setShowGraphicModifiers(false);
                        ((MilStd2525TacticalSymbol) symbol).setShowFrame(false);
                        ((MilStd2525TacticalSymbol) symbol).setShowIcon(false);
                    }
                }
            };

            // Create a renderable layer to display the tactical symbols.
            this.symbolLayer = new RenderableLayer();
            this.symbolLayer.setName("Tactical Symbols");

            this.sharedHighlightAttrs = new BasicTacticalSymbolAttributes();
            this.sharedHighlightAttrs.setInteriorMaterial(Material.WHITE);
            this.sharedHighlightAttrs.setTextModifierMaterial(Material.WHITE);
            this.sharedHighlightAttrs.setOpacity(1.0);

            // Create an air tactical symbol for the MIL-STD-2525 symbology set.
            TacticalSymbolAttributes attributes = new BasicTacticalSymbolAttributes();
            attributes.setTextModifierMaterial(Material.RED);
            MilStd2525TacticalSymbol airSymbol = new MilStd2525TacticalSymbol("SFAPMFQM--GIUSA",
                Position.fromDegrees(32.4520, 63.44553, 3000));
            airSymbol.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Friendly SOF Drone Aircraft"); // Tool tip text.
            airSymbol.setAttributes(attributes);
            airSymbol.setHighlightAttributes(this.sharedHighlightAttrs);
            airSymbol.setModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(235));
            airSymbol.setShowLocation(false);
            airSymbol.setLODSelector(lodSelector); // specify the LOD selector
            this.symbolLayer.addRenderable(airSymbol);

            // Create a ground tactical symbol for the MIL-STD-2525 symbology set.
            attributes = new BasicTacticalSymbolAttributes();
            attributes.setTextModifierMaterial(Material.RED);
            MilStd2525TacticalSymbol groundSymbol = new MilStd2525TacticalSymbol("SHGXUCFRMS----G",
                Position.fromDegrees(32.4014, 63.3894, 0));
            groundSymbol.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Hostile Self-Propelled Rocket Launchers");
            groundSymbol.setAttributes(attributes);
            groundSymbol.setHighlightAttributes(this.sharedHighlightAttrs);
            groundSymbol.setModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(90));
            groundSymbol.setModifier(SymbologyConstants.SPEED_LEADER_SCALE, 0.5);
            groundSymbol.setShowLocation(false);
            groundSymbol.setLODSelector(lodSelector); // specify the LOD selector
            this.symbolLayer.addRenderable(groundSymbol);

            // Create a ground tactical symbol for the MIL-STD-2525 symbology set.
            attributes = new BasicTacticalSymbolAttributes();
            attributes.setTextModifierMaterial(Material.RED);
            MilStd2525TacticalSymbol machineGunSymbol = new MilStd2525TacticalSymbol("SFGPEWRH--MTUSG",
                Position.fromDegrees(32.3902, 63.4161, 0));
            machineGunSymbol.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Friendly Heavy Machine Gun");
            machineGunSymbol.setAttributes(attributes);
            machineGunSymbol.setHighlightAttributes(this.sharedHighlightAttrs);
            machineGunSymbol.setModifier(SymbologyConstants.QUANTITY, 200);
            machineGunSymbol.setModifier(SymbologyConstants.STAFF_COMMENTS, "FOR REINFORCEMENTS");
            machineGunSymbol.setModifier(SymbologyConstants.ADDITIONAL_INFORMATION, "ADDED SUPPORT FOR JJ");
            machineGunSymbol.setModifier(SymbologyConstants.TYPE, "MACHINE GUN");
            machineGunSymbol.setModifier(SymbologyConstants.DATE_TIME_GROUP, "30140000ZSEP97");
            machineGunSymbol.setLODSelector(lodSelector); // specify the LOD selector
            this.symbolLayer.addRenderable(machineGunSymbol);

            // Add the symbol layer to the WorldWind model.
            this.getWwd().getModel().getLayers().add(symbolLayer);

            // Size the WorldWindow to provide enough screen space for the symbols and center the WorldWindow on the
            // screen.
            Dimension size = new Dimension(1800, 1000);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
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

        start("WorldWind Tactical Symbol Decluttering", AppFrame.class);
    }
}
