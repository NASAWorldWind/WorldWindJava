/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.Box;

/**
 * Illustrates how to use the World Wind <code>{@link Box}</code> rigid shape to display an arbitrarily sized and
 * oriented box at a geographic position on the Globe.
 *
 * @author ccrick
 * @version $Id: Boxes.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Boxes extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
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

            // ********* sample  Boxes  *******************

            // Box with equal axes, ABSOLUTE altitude mode
            Box box3 = new Box(Position.fromDegrees(40, -120, 80000), 50000, 50000, 50000);
            box3.setAltitudeMode(WorldWind.ABSOLUTE);
            box3.setAttributes(attrs);
            box3.setVisible(true);
            box3.setValue(AVKey.DISPLAY_NAME, "Box with equal axes, ABSOLUTE altitude mode");
            layer.addRenderable(box3);

            // Box with equal axes, RELATIVE_TO_GROUND
            Box box4 = new Box(Position.fromDegrees(37.5, -115, 50000), 50000, 50000, 50000);
            box4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            box4.setAttributes(attrs);
            box4.setVisible(true);
            box4.setValue(AVKey.DISPLAY_NAME, "Box with equal axes, RELATIVE_TO_GROUND altitude mode");
            layer.addRenderable(box4);

            // Box with equal axes, CLAMP_TO_GROUND
            Box box5 = new Box(Position.fromDegrees(35, -110, 50000), 50000, 50000, 50000);
            box5.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            box5.setAttributes(attrs);
            box5.setVisible(true);
            box5.setValue(AVKey.DISPLAY_NAME, "Box with equal axes, CLAMP_TO_GROUND altitude mode");
            layer.addRenderable(box5);

            // Box with a texture
            Box box9 = new Box(Position.fromDegrees(0, -90, 600000), 600000, 600000, 600000);
            box9.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            box9.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            box9.setAttributes(attrs);
            box9.setVisible(true);
            box9.setValue(AVKey.DISPLAY_NAME, "Box with a texture");
            layer.addRenderable(box9);

            // Scaled Box with default orientation
            Box box = new Box(Position.ZERO, 1000000, 500000, 100000);
            box.setAltitudeMode(WorldWind.ABSOLUTE);
            box.setAttributes(attrs);
            box.setVisible(true);
            box.setValue(AVKey.DISPLAY_NAME, "Scaled Box with default orientation");
            layer.addRenderable(box);

            // Scaled Box with a pre-set orientation
            Box box2 = new Box(Position.fromDegrees(0, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            box2.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            box2.setAttributes(attrs2);
            box2.setVisible(true);
            box2.setValue(AVKey.DISPLAY_NAME, "Scaled Box with a pre-set orientation");
            layer.addRenderable(box2);

            // Scaled Box with a pre-set orientation
            Box box6 = new Box(Position.fromDegrees(30, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            box6.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            box6.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            box6.setAttributes(attrs2);
            box6.setVisible(true);
            box6.setValue(AVKey.DISPLAY_NAME, "Scaled Box with a pre-set orientation");
            layer.addRenderable(box6);

            // Scaled Box with a pre-set orientation
            Box box7 = new Box(Position.fromDegrees(60, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            box7.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            box7.setAttributes(attrs2);
            box7.setVisible(true);
            box7.setValue(AVKey.DISPLAY_NAME, "Scaled Box with a pre-set orientation");
            layer.addRenderable(box7);

            // Scaled, oriented Box in 3rd "quadrant" (-X, -Y, -Z)
            Box box8 = new Box(Position.fromDegrees(-45, -180, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            box8.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            box8.setAttributes(attrs2);
            box8.setVisible(true);
            box8.setValue(AVKey.DISPLAY_NAME, "Scaled, oriented Box in the 3rd 'quadrant' (-X, -Y, -Z)");
            layer.addRenderable(box8);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Boxes", AppFrame.class);
    }
}
