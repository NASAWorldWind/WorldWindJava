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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.tiff.GeotiffImageReaderSpi;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import javax.imageio.spi.IIORegistry;
import java.awt.*;
import java.util.*;

/**
 * This example demonstrates how to use the {@link gov.nasa.worldwind.render.SurfaceImage} class to place images on the
 * surface of the globe.
 *
 * @author tag
 * @version $Id: SurfaceImages.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class SurfaceImages extends ApplicationTemplate {

    static {
        IIORegistry reg = IIORegistry.getDefaultInstance();
        reg.registerServiceProvider(GeotiffImageReaderSpi.inst());
    }

    protected static final String GEORSS_ICON_PATH = "gov/nasa/worldwindx/examples/images/georss.png";
    protected static final String TEST_PATTERN = "gov/nasa/worldwindx/examples/images/antenna.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false);

            try {
                SurfaceImage si1 = new SurfaceImage(GEORSS_ICON_PATH, new ArrayList<>(Arrays.asList(
                        LatLon.fromDegrees(20d, -115d),
                        LatLon.fromDegrees(20d, -105d),
                        LatLon.fromDegrees(32d, -102d),
                        LatLon.fromDegrees(30d, -115d)
                )));
                SurfaceImage si2 = new SurfaceImage(TEST_PATTERN, new ArrayList<>(Arrays.asList(
                        LatLon.fromDegrees(37.8677, -105.1668),
                        LatLon.fromDegrees(37.8677, -104.8332),
                        LatLon.fromDegrees(38.1321, -104.8326),
                        LatLon.fromDegrees(38.1321, -105.1674)
                )));
                Path boundary = new Path(si1.getCorners(), 0);
                boundary.setSurfacePath(true);
                boundary.setPathType(AVKey.RHUMB_LINE);
                var attrs = new BasicShapeAttributes();
                attrs.setOutlineMaterial(new Material(new Color(0, 255, 0)));
                boundary.setAttributes(attrs);
                boundary.makeClosed();

                Path boundary2 = new Path(si2.getCorners(), 0);
                boundary2.setSurfacePath(true);
                boundary2.setPathType(AVKey.RHUMB_LINE);
                attrs = new BasicShapeAttributes();
                attrs.setOutlineMaterial(new Material(new Color(0, 255, 0)));
                boundary2.setAttributes(attrs);
                boundary2.makeClosed();

                RenderableLayer layer = new RenderableLayer();
                layer.setName("Surface Images");
                layer.setPickEnabled(false);
                layer.addRenderable(si1);
                layer.addRenderable(si2);
                layer.addRenderable(boundary);
                layer.addRenderable(boundary2);

                insertBeforeCompass(this.getWwd(), layer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Surface Images", SurfaceImages.AppFrame.class);
    }
}
