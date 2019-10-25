/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.util.*;

/**
 * Illustrates how to display lines that stand out from the background imagery. The technique is to draw behind the line
 * a slightly wider, slightly opaque line in a contrasting color. This backing line makes the primary line stand out
 * more than it otherwise would. This example shows how to do this for both SurfacePolyline and terrain-conforming Path.
 *
 * @author tag
 * @version $Id: LineBackground.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class LineBackground extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false);

            try {
                // Specify attributes for the foreground line.
                ShapeAttributes foregroundAttrs = new BasicShapeAttributes();
                foregroundAttrs.setOutlineMaterial(new Material(Color.BLUE));
                foregroundAttrs.setOutlineStipplePattern((short) 0xAAAA);
                foregroundAttrs.setOutlineStippleFactor(8);

                // Specify attributes for the background line.
                ShapeAttributes backgroundAttrs = new BasicShapeAttributes();
                backgroundAttrs.setOutlineMaterial(new Material(Color.WHITE));
                backgroundAttrs.setOutlineOpacity(0.1);
                backgroundAttrs.setOutlineWidth(foregroundAttrs.getOutlineWidth() + 2);

                // Create the primary line as a SurfacePolyline and set its attributes.
                SurfacePolyline si1 = new SurfacePolyline(new ArrayList<>(Arrays.asList(
                        LatLon.fromDegrees(33.7, 119.6),
                        LatLon.fromDegrees(33.5, 125),
                        LatLon.fromDegrees(35.1, 129.1),
                        LatLon.fromDegrees(35.8, 127.1)
                )));
                si1.setClosed(true);
                si1.setAttributes(foregroundAttrs);

                // Create the background SurfacePolyline and set its attributes.
                SurfacePolyline si2 = new SurfacePolyline(si1.getLocations());
                si2.setClosed(true);
                si2.setAttributes(backgroundAttrs);

                // Now do the same for the Polyline version, which is placed 2 degrees above the SurfacePolyline.
                ArrayList<LatLon> plPoints = new ArrayList<>();
                for (LatLon ll : si1.getLocations()) {
                    plPoints.add(ll.add(LatLon.fromDegrees(2, 0))); // add 2 degrees of latitude to separate the lines
                }
                Path path1 = new Path(plPoints, 0); // the primary Path
                path1.setSurfacePath(true);
                path1.setPathType(AVKey.RHUMB_LINE);
                path1.setAttributes(foregroundAttrs);
                path1.makeClosed();

                Path path2 = new Path(plPoints, 0); // the background Path
                path2.setSurfacePath(true);
                path2.setPathType(AVKey.RHUMB_LINE);
                float[] c = backgroundAttrs.getOutlineMaterial().getDiffuse().getColorComponents(new float[3]);
                var attrs = new BasicShapeAttributes(backgroundAttrs);
                attrs.setOutlineMaterial(new Material(new Color(c[0], c[1], c[2], (float) backgroundAttrs.getOutlineOpacity())));
                path2.setAttributes(attrs);
                path2.makeClosed();

                // Add all the lines to the scene.
                RenderableLayer layer = new RenderableLayer();
                layer.setName("Lines");
                layer.setPickEnabled(false);
                layer.addRenderable(si2);
                layer.addRenderable(si1);
                layer.addRenderable(path1); // Must draw the primary Path before drawing the background Path
                layer.addRenderable(path2);

                insertBeforeCompass(this.getWwd(), layer);

                // Move the view to the line locations.
                View view = getWwd().getView();
                view.setEyePosition(Position.fromDegrees(35.3, 124.6, 1500e3));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Line Backgrounds", AppFrame.class);
    }
}
