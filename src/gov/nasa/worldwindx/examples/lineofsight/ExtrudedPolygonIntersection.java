/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.lineofsight;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;

import java.awt.event.*;
import java.util.*;

/**
 * Shows how to determine and display the intersection of a line with an {@link ExtrudedPolygon}.
 *
 * @author tag
 * @version $Id: ExtrudedPolygonIntersection.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ExtrudedPolygonIntersection extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected HighResolutionTerrain terrain; // Use this class to test against high-resolution terrain
        protected ExtrudedPolygon polygon; // the polygon to intersect
        protected RenderableLayer resultsLayer; // holds the intersection geometry
        protected RenderableLayer shapeLayer; // holds the shape

        public AppFrame()
        {
            super(true, true, false);

            // Create the extruded polygon boundary and then the extruded polygon.
            List<LatLon> positions = new ArrayList<LatLon>();
            positions.add(LatLon.fromDegrees(40.4, -120.6));
            positions.add(LatLon.fromDegrees(40.4, -120.4));
            positions.add(LatLon.fromDegrees(40.6, -120.4));
            positions.add(LatLon.fromDegrees(40.6, -120.6));

            this.polygon = new ExtrudedPolygon(positions, 10e3);

            // Set some of the shape's attributes
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.LIGHT_GRAY);
            attrs.setInteriorOpacity(0.6);
            this.polygon.setCapAttributes(attrs);
            this.polygon.setSideAttributes(attrs);

            // Add the shape to its display layer.
            this.shapeLayer = new RenderableLayer();
            this.shapeLayer.addRenderable(this.polygon);
            insertBeforeCompass(getWwd(), this.shapeLayer);

            // Prepare the results layer.
            this.resultsLayer = new RenderableLayer();
            insertBeforeCompass(getWwd(), this.resultsLayer);

            // Create high-resolution terrain for the intersection calculations
            this.terrain = new HighResolutionTerrain(this.getWwd().getModel().getGlobe(), 20d);

            // Perform the intersection test within a timer callback. Intersection calculations would normally be done
            // on a separate, non-EDT thread, however.
            final javax.swing.Timer timer = new javax.swing.Timer(3000, new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Intersect the sides.
                    Position pA = Position.fromDegrees(40.5, -120.7, 5e3);
                    Position pB = Position.fromDegrees(40.5, -120.3, 5e3);
                    drawLine(pA, pB);
                    performIntersection(pA, pB);

                    // Intersect the cap.
                    pA = Position.fromDegrees(40.5, -120.5, 0);
                    pB = new Position(pA, 20e3);
                    drawLine(pA, pB);
                    performIntersection(pA, pB);

                    ((javax.swing.Timer) actionEvent.getSource()).stop();
                }
            });
            timer.start();
        }

        protected void performIntersection(Position pA, Position pB)
        {
            try
            {
                // Create the line to intersect with the shape.
                Vec4 refPoint = terrain.getSurfacePoint(pA);
                Vec4 targetPoint = terrain.getSurfacePoint(pB);
                Line line = new Line(targetPoint, refPoint.subtract3(targetPoint));

                // Perform the intersection.
                List<Intersection> intersections = this.polygon.intersect(line, this.terrain);

                // Get and display the intersections.
                if (intersections != null)
                {
                    for (Intersection intersection : intersections)
                    {
                        drawIntersection(intersection);
                    }
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        protected void drawLine(Position pA, Position pB)
        {
            // Create and display the intersection line.
            Path path = new Path(pA, pB);
            ShapeAttributes pathAttributes = new BasicShapeAttributes();
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pathAttributes.setOutlineMaterial(Material.GREEN);
            pathAttributes.setOutlineOpacity(0.6);
            pathAttributes.setDrawOutline(true);
            pathAttributes.setDrawInterior(false);
            path.setAttributes(pathAttributes);
            this.resultsLayer.addRenderable(path);

            this.getWwd().redraw();
        }

        protected void drawIntersection(Intersection intersection)
        {
            // Display a point at the intersection.
            PointPlacemark iPoint = new PointPlacemark(intersection.getIntersectionPosition());
            iPoint.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            PointPlacemarkAttributes pointAttributes = new PointPlacemarkAttributes();
            pointAttributes.setLineMaterial(Material.CYAN);
            pointAttributes.setScale(8d);
            pointAttributes.setUsePointAsDefaultImage(true);
            iPoint.setAttributes(pointAttributes);
            this.resultsLayer.addRenderable(iPoint);

            this.getWwd().redraw();
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the balloons are immediately visible.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40.5);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -120.4);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 125e3);
        Configuration.setValue(AVKey.INITIAL_HEADING, 27);
        Configuration.setValue(AVKey.INITIAL_PITCH, 30);

        ApplicationTemplate.start("World Wind Extruded Polygon Intersection", AppFrame.class);
    }
}
