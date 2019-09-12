/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;

/**
 * This example demonstrates how to retrieve a remote image and display it as a {@link
 * gov.nasa.worldwind.render.SurfaceImage}. <code>SurfaceImage</code> downloads the image in a separate thread via the
 * WorldWind retrieval service. Once the image is retrieved, <code>SurfaceImage</code> renders it on the globe.
 *
 * @author dcollins
 * @version $Id: RemoteSurfaceImage.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class RemoteSurfaceImage extends ApplicationTemplate
{
    // The remote image to display.
    protected static final String IMAGE_URL = "https://eoimages.gsfc.nasa.gov/ve//1438/earth_lights_lrg.jpg";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            SurfaceImage image = new SurfaceImage(IMAGE_URL, Sector.FULL_SPHERE);

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Remote Surface Image");
            layer.addRenderable(image);
            // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
            layer.setPickEnabled(false);

            insertBeforePlacenames(this.getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Remote Surface Image", RemoteSurfaceImage.AppFrame.class);
    }
}
