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

import gov.nasa.worldwind.geom.Position;
import javax.swing.SwingUtilities;

import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.layers.RenderableLayer;

public class KMLReload extends ApplicationTemplate {

    public static class LoaderThread extends Thread {

        protected String kmlSource;

        public LoaderThread(String kmlSource) {
            this.kmlSource = kmlSource;
        }

        @Override
        public void run() {
            try {
//                Class c = KMLReload.class;

                final KMLRoot kmlRoot = KMLRoot.createAndParse(kmlSource);

                // Schedule a task on the EDT to add the parsed document to a layer
                SwingUtilities.invokeLater(() -> {
                    KMLReload.AppFrame.thisFrame.addKml(kmlRoot);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private final RenderableLayer kmlLayer;
        private KMLController kmlController;
        private final LoaderThread loaderThread;
        public static AppFrame thisFrame;

        public AppFrame() {
            kmlController = null;
            kmlLayer = new RenderableLayer();
//            loaderThread=new LoaderThread("/gov/nasa/worldwindx/examples/data/IconExpiration.kml");
            loaderThread = new LoaderThread("http://localhost:8000/jump-around.kml");
//            loaderThread=new LoaderThread("http://localhost/Tile.kmz");

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), kmlLayer);
            AppFrame.thisFrame = this;
        }

        public void start() {
            loaderThread.start();
        }

        public void addKml(KMLRoot root) {
            if (kmlController != null) {
                kmlLayer.removeRenderable(kmlController);
            }

            kmlController = new KMLController(root);
            kmlLayer.addRenderable(kmlController);
            this.getWwd().redraw();

            getWwd().getView().setEyePosition(Position.fromDegrees(78, 60, 10000000));
        }

    }

    public static void main(String[] args) {
        KMLReload.AppFrame frame = (KMLReload.AppFrame) ApplicationTemplate.start("KML Reload", AppFrame.class);
        frame.start();
    }
}
