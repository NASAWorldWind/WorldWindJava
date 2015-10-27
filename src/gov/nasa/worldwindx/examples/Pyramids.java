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

/**
 * Example of {@link Pyramid} usage. Shows examples of pyramids with various orientations, materials, and textures
 * applied.
 *
 * @author ccrick
 * @version $Id: Pyramids.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Pyramids extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

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

            // ********* sample  Pyramids  *******************

            // Pyramid with equal axes, ABSOLUTE altitude mode
            Pyramid pyramid3 = new Pyramid(Position.fromDegrees(40, -120, 80000), 50000, 50000, 50000);
            pyramid3.setAltitudeMode(WorldWind.ABSOLUTE);
            pyramid3.setAttributes(attrs);
            pyramid3.setVisible(true);
            pyramid3.setValue(AVKey.DISPLAY_NAME, "Pyramid with equal axes, ABSOLUTE altitude mode");
            layer.addRenderable(pyramid3);

            // Pyramid with equal axes, RELATIVE_TO_GROUND
            Pyramid pyramid4 = new Pyramid(Position.fromDegrees(37.5, -115, 50000), 50000, 50000, 50000);
            pyramid4.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pyramid4.setAttributes(attrs);
            pyramid4.setVisible(true);
            pyramid4.setValue(AVKey.DISPLAY_NAME, "Pyramid with equal axes, RELATIVE_TO_GROUND altitude mode");
            layer.addRenderable(pyramid4);

            // Pyramid with equal axes, CLAMP_TO_GROUND
            Pyramid pyramid5 = new Pyramid(Position.fromDegrees(35, -110, 50000), 50000, 50000, 50000);
            pyramid5.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pyramid5.setAttributes(attrs);
            pyramid5.setVisible(true);
            pyramid5.setValue(AVKey.DISPLAY_NAME, "Pyramid with equal axes, CLAMP_TO_GROUND altitude mode");
            layer.addRenderable(pyramid5);

            // Pyramid with a texture, using Pyramid(position, height, width) constructor
            Pyramid pyramid9 = new Pyramid(Position.fromDegrees(0, -90, 600000), 1200000, 1200000);
            pyramid9.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pyramid9.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            pyramid9.setAttributes(attrs);
            pyramid9.setVisible(true);
            pyramid9.setValue(AVKey.DISPLAY_NAME, "Pyramid with a texture");
            layer.addRenderable(pyramid9);

            // Scaled Pyramid with default orientation
            Pyramid pyramid = new Pyramid(Position.ZERO, 1000000, 500000, 100000);
            pyramid.setAltitudeMode(WorldWind.ABSOLUTE);
            pyramid.setAttributes(attrs);
            pyramid.setVisible(true);
            pyramid.setValue(AVKey.DISPLAY_NAME, "Scaled Pyramid with default orientation");
            layer.addRenderable(pyramid);

            // Scaled Pyramid with a pre-set orientation
            Pyramid pyramid2 = new Pyramid(Position.fromDegrees(0, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            pyramid2.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pyramid2.setAttributes(attrs2);
            pyramid2.setVisible(true);
            pyramid2.setValue(AVKey.DISPLAY_NAME, "Scaled Pyramid with a pre-set orientation");
            layer.addRenderable(pyramid2);

            // Scaled Pyramid with a pre-set orientation
            Pyramid pyramid6 = new Pyramid(Position.fromDegrees(30, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            pyramid6.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pyramid6.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
            pyramid6.setAttributes(attrs2);
            pyramid6.setVisible(true);
            pyramid6.setValue(AVKey.DISPLAY_NAME, "Scaled Pyramid with a pre-set orientation");
            layer.addRenderable(pyramid6);

            // Scaled Pyramid with a pre-set orientation
            Pyramid pyramid7 = new Pyramid(Position.fromDegrees(60, 30, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            pyramid7.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pyramid7.setAttributes(attrs2);
            pyramid7.setVisible(true);
            pyramid7.setValue(AVKey.DISPLAY_NAME, "Scaled Pyramid with a pre-set orientation");
            layer.addRenderable(pyramid7);

            // Scaled, oriented pyramid in 3rd "quadrant" (-X, -Y, -Z)
            Pyramid pyramid8 = new Pyramid(Position.fromDegrees(-45, -180, 750000), 1000000, 500000, 100000,
                Angle.fromDegrees(90), Angle.fromDegrees(45), Angle.fromDegrees(30));
            pyramid8.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pyramid8.setAttributes(attrs2);
            pyramid8.setVisible(true);
            pyramid8.setValue(AVKey.DISPLAY_NAME, "Scaled, oriented Pyramid in the 3rd 'quadrant' (-X, -Y, -Z)");
            layer.addRenderable(pyramid8);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Pyramids", AppFrame.class);
    }
}
