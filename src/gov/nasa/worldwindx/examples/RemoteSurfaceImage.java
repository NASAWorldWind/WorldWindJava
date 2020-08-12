/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
