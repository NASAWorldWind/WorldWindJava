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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.KMLDocument;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLPoint;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.firstperson.FlyToFlyViewAnimator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Shows how to load <a href="https://www.khronos.org/kml/">KML</a> files and zoom to a feature.
 */
public class SimpleKMLViewer extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame implements RenderingListener {

        public BasicFlyView view;
        public boolean wwReady;

        public AppFrame() {
            super(true, true, false); // Don't include the layer panel; we're using the on-screen layer tree.

            // Size the WorldWindow to take up the space typically used by the layer panel.
            Dimension size = new Dimension(1400, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            LayerList layers = getWwd().getModel().getLayers();
            for (Layer layer : layers) {
                String layerName = layer.getName();
                if (layerName != null && layerName.toLowerCase().startsWith("bing")) {
                    layer.setEnabled(true);
                    break;
                }
            }
            // Force the view to be a FlyView
            view = new BasicFlyView();
            getWwd().setView(view);
            getWwd().addRenderingListener(this);
            wwReady = false;
        }

        @Override
        public void stageChanged(RenderingEvent event) {
            if (event.getStage().equals(RenderingEvent.BEFORE_RENDERING)) {
                wwReady = true;
                getWwd().removeRenderingListener(this);
            }
        }

        public void moveToLocation(Position location) {
            FlyToFlyViewAnimator animator
                    = FlyToFlyViewAnimator.createFlyToFlyViewAnimator(view,
                            view.getEyePosition(),
                            location,
                            view.getHeading(), view.getHeading(),
                            view.getPitch(), view.getPitch(),
                            view.getEyePosition().getElevation(), location.elevation,
                            10000, WorldWind.ABSOLUTE);
            view.addAnimator(animator);
            animator.start();
            view.firePropertyChange(AVKey.VIEW, null, view);
        }

        /**
         * Adds the specified <code>kmlRoot</code> to this app frame's <code>WorldWindow</code> as a new
         * <code>Layer</code>.
         *
         * @param kmlRoot the KMLRoot to add a new layer for.
         */
        protected void addKMLLayer(KMLRoot kmlRoot) {
            // Create a KMLController to adapt the KMLRoot to the WorldWind renderable interface.
            KMLController kmlController = new KMLController(kmlRoot);

            // Adds a new layer containing the KMLRoot to the end of the WorldWindow's layer list.
            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(kmlController);
            this.getWwd().getModel().getLayers().add(layer);
            AVList fields = kmlRoot.getFields();
            KMLDocument doc = (KMLDocument) fields.getValue("Document");
            List<KMLAbstractFeature> features = doc.getFeatures();
            KMLPlacemark placemark = (KMLPlacemark) features.get(0);
            KMLPoint placePoint = (KMLPoint) placemark.getGeometry();
            Position placePosition = new Position((LatLon) placePoint.getCoordinates(), 5000);
            moveToLocation(placePosition);
        }
    }

    // A <code>Thread</code> that loads a KML file and displays it in an <code>AppFrame</code>.
    public static class WorkerThread extends Thread {

        // Indicates the source of the KML file loaded by this thread. Initialized during construction.
        protected Object kmlSource;

        // Indicates the <code>AppFrame</code> the KML file content is displayed in. Initialized during
        // construction.
        protected AppFrame appFrame;

        /**
         * Creates a new worker thread from a specified <code>kmlSource</code> and <code>appFrame</code>.
         *
         * @param kmlSource the source of the KML file to load. May be a {@link java.io.File}, a {@link
         *                      java.net.URL}, or an {@link java.io.InputStream}, or a {@link String} identifying a file path or URL.
         * @param appFrame the <code>AppFrame</code> in which to display the KML source.
         */
        public WorkerThread(Object kmlSource, AppFrame appFrame) {
            this.kmlSource = kmlSource;
            this.appFrame = appFrame;
        }

        /**
         * Loads this worker thread's KML source into a new <code>{@link gov.nasa.worldwind.ogc.kml.KMLRoot}</code>,
         * then adds the new <code>KMLRoot</code> to this worker thread's <code>AppFrame</code>.
         */
        @Override
        public void run() {
            try {
                final KMLRoot kmlRoot = KMLRoot.createAndParse(this.kmlSource);
                while (!appFrame.wwReady) {
                    Thread.sleep(100);
                }
                // Schedule a task on the EDT to add the parsed document to a layer
                SwingUtilities.invokeLater(() -> {
                    appFrame.addKMLLayer(kmlRoot);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Set the application frame to update, a position for the model, and a path to the KML file.
        final AppFrame af = (AppFrame) start("WorldWind Simple KML Viewer", AppFrame.class);
        final File kmlFile = new File("testData/KML/PointPlacemark.kml");

        // Invoke the <code>Thread</code> to load the KML file asynchronously.
        new WorkerThread(kmlFile, af).start();

    }
}
