/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
//import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.ogc.gltf.*;
//import gov.nasa.worldwind.ogc.gltf.impl.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Test loading GLTF files.
 */
public class GLTFViewer extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false); // Don't include the layer panel; we're using the on-screen layer tree.

            // Size the WorldWindow to take up the space typically used by the layer panel.
            Dimension size = new Dimension(1400, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        /**
         * Adds the specified <code>gltfRoot</code> to this app frame's
         * <code>WorldWindow</code> as a new <code>Layer</code>.
         *
         * @param gltfRoot the GLTFRoot to add a new layer for.
         */
//        protected void addGLTFLayer(GLTFRoot gltfRoot)
//        {
//            // Create a GLTFController to adapt the GLTFRoot to the WorldWind renderable interface.
//            GLTFController gltfController = new GLTFController(gltfRoot);
//
//            // Adds a new layer containing the GLTFRoot to the end of the WorldWindow's layer list.
//            RenderableLayer layer = new RenderableLayer();
//            layer.addRenderable(gltfController);
//            this.getWwd().getModel().getLayers().add(layer);
//        }
    }

    /**
     * A <code>Thread</code> that loads a COLLADA file and displays it in an
     * <code>AppFrame</code>.
     */
    public static class WorkerThread extends Thread {

        /**
         * Indicates the source of the COLLADA file loaded by this thread.
         * Initialized during construction.
         */
        protected Object gltfSource;
        /**
         * Geographic position of the COLLADA model.
         */
        protected Position position;
        /**
         * Indicates the <code>AppFrame</code> the COLLADA file content is
         * displayed in. Initialized during construction.
         */
        protected AppFrame appFrame;

        /**
         * Creates a new worker thread from a specified <code>gltfSource</code>
         * and <code>appFrame</code>.
         *
         * @param gltfSource the source of the COLLADA file to load. May be a
         * {@link java.io.File}, a {@link
         *                      java.net.URL}, or an {@link java.io.InputStream}, or a {@link String}
         * identifying a file path or URL.
         * @param position the geographic position of the COLLADA model.
         * @param appFrame the <code>AppFrame</code> in which to display the
         * COLLADA source.
         */
        public WorkerThread(Object gltfSource, Position position, AppFrame appFrame) {
            this.gltfSource = gltfSource;
            this.position = position;
            this.appFrame = appFrame;
        }

        /**
         * Loads this worker thread's COLLADA source into a new
         * <code>{@link gov.nasa.worldwind.ogc.gltf.GLTFRoot}</code>, then adds
         * the new <code>GLTFRoot</code> to this worker thread's
         * <code>AppFrame</code>.
         */
        public void run() {
            try {
                final GLTFRoot gltfRoot = GLTFRoot.createAndParse(this.gltfSource);
                System.out.println(gltfRoot);
//                gltfRoot.setPosition(this.position);
//                gltfRoot.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);

                // Schedule a task on the EDT to add the parsed document to a layer
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
//                        appFrame.addGLTFLayer(gltfRoot);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40.028);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -105.27284091410579);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 4000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 50);

        final AppFrame af = (AppFrame) start("WorldWind GLTF Viewer", AppFrame.class);

        new WorkerThread("testData/gltf/Triangle/glTF-Embedded/Triangle.gltf",
                Position.fromDegrees(40.009993372683, -105.272774533734, 300), af).start();

    }
}
