/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;
import java.util.Arrays;

/**
 * Example of displaying a flat globe instead of a round globe. The flat globe displays elevation in the same way as the
 * round globe (mountains rise out of the globe). One advantage of using a flat globe is that a user can see the entire
 * globe at once. The globe can be configured with different map projections.
 *
 * @author Patrick Murris
 * @version $Id: FlatWorld.java 2219 2014-08-11 21:39:44Z dcollins $
 * @see gov.nasa.worldwind.globes.FlatGlobe
 * @see EarthFlat
 */
public class FlatWorld extends ApplicationTemplate
{
    protected static final String SURFACE_POLYGON_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/georss.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            this.makePaths();
            this.makeSurfaceShapes();
        }

        protected void makePaths()
        {
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Paths");

            // Path over Florida.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.GREEN);
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            double originLat = 28;
            double originLon = -82;
            Iterable<Position> locations = Arrays.asList(
                Position.fromDegrees(originLat + 5.0, originLon + 2.5, 100e3),
                Position.fromDegrees(originLat + 5.0, originLon - 2.5, 100e3),
                Position.fromDegrees(originLat + 2.5, originLon - 5.0, 100e3),
                Position.fromDegrees(originLat - 2.5, originLon - 5.0, 100e3),
                Position.fromDegrees(originLat - 5.0, originLon - 2.5, 100e3),
                Position.fromDegrees(originLat - 5.0, originLon + 2.5, 100e3),
                Position.fromDegrees(originLat - 2.5, originLon + 5.0, 100e3),
                Position.fromDegrees(originLat + 2.5, originLon + 5.0, 100e3),
                Position.fromDegrees(originLat + 5.0, originLon + 2.5, 100e3));
            Path shape = new Path(locations);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Path spanning the international dateline.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.RED);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.RED)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            locations = Arrays.asList(
                Position.fromDegrees(20, -170, 100e3),
                Position.fromDegrees(15, 170, 100e3),
                Position.fromDegrees(10, -175, 100e3),
                Position.fromDegrees(5, 170, 100e3),
                Position.fromDegrees(0, -170, 100e3),
                Position.fromDegrees(20, -170, 100e3));
            shape = new Path(locations);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Path around the north pole.
            attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.GREEN)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            locations = Arrays.asList(
                Position.fromDegrees(80, 0, 100e3),
                Position.fromDegrees(80, 90, 100e3),
                Position.fromDegrees(80, 180, 100e3),
//                Position.fromDegrees(80, -180, 100e3),
                Position.fromDegrees(80, -90, 100e3),
                Position.fromDegrees(80, 0, 100e3));
            shape = new Path(locations);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            ApplicationTemplate.insertBeforePlacenames(this.getWwd(), layer);
        }

        protected void makeSurfaceShapes()
        {
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Surface Shapes");

            // Surface polygon over Florida.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.WHITE);
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);
            attrs.setImageSource(SURFACE_POLYGON_IMAGE_PATH);
            attrs.setImageScale(0.5);

            double originLat = 28;
            double originLon = -82;
            Iterable<LatLon> locations = Arrays.asList(
                LatLon.fromDegrees(originLat + 5.0, originLon + 2.5),
                LatLon.fromDegrees(originLat + 5.0, originLon - 2.5),
                LatLon.fromDegrees(originLat + 2.5, originLon - 5.0),
                LatLon.fromDegrees(originLat - 2.5, originLon - 5.0),
                LatLon.fromDegrees(originLat - 5.0, originLon - 2.5),
                LatLon.fromDegrees(originLat - 5.0, originLon + 2.5),
                LatLon.fromDegrees(originLat - 2.5, originLon + 5.0),
                LatLon.fromDegrees(originLat + 2.5, originLon + 5.0),
                LatLon.fromDegrees(originLat + 5.0, originLon + 2.5));
            SurfaceShape shape = new SurfacePolygon(locations);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface polygon spanning the international dateline.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.RED);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.RED)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            locations = Arrays.asList(
                LatLon.fromDegrees(20, -170),
                LatLon.fromDegrees(15, 170),
                LatLon.fromDegrees(10, -175),
                LatLon.fromDegrees(5, 170),
                LatLon.fromDegrees(0, -170),
                LatLon.fromDegrees(20, -170));
            shape = new SurfacePolygon(locations);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface ellipse over the center of the United States.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.CYAN);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.CYAN)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceEllipse(LatLon.fromDegrees(38, -104), 1.5e5, 1e5, Angle.fromDegrees(15));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface circle over the center of the United states.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.GREEN);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.GREEN)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceCircle(LatLon.fromDegrees(36, -104), 1e5);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface circle over the North Pole
            shape = new SurfaceCircle(LatLon.fromDegrees(90, 0), 1e6);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface quadrilateral over the center of the United States.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.ORANGE);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.ORANGE)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceQuad(LatLon.fromDegrees(42, -104), 1e5, 1.3e5, Angle.fromDegrees(20));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface square over the center of the United states.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.MAGENTA);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.MAGENTA)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceSquare(LatLon.fromDegrees(45, -104), 1e5);
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface sector over Mt. Shasta.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.YELLOW);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.YELLOW)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceSector(new Sector(
                Angle.fromDegrees(41.0), Angle.fromDegrees(41.6),
                Angle.fromDegrees(-122.5), Angle.fromDegrees(-121.7)));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface sector over Lake Tahoe.
            attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.CYAN);
            attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(Color.CYAN)));
            attrs.setInteriorOpacity(0.5);
            attrs.setOutlineOpacity(0.8);
            attrs.setOutlineWidth(3);

            shape = new SurfaceSector(new Sector(
                Angle.fromDegrees(38.9), Angle.fromDegrees(39.3),
                Angle.fromDegrees(-120.2), Angle.fromDegrees(-119.9)));
            shape.setAttributes(attrs);
            layer.addRenderable(shape);

            // Surface polyline spanning the international dateline.
            attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(3);
            attrs.setOutlineStippleFactor(2);

            locations = Arrays.asList(
                LatLon.fromDegrees(-10, 170),
                LatLon.fromDegrees(-10, -170));
            shape = new SurfacePolyline(locations);
            shape.setAttributes(attrs);
            ((SurfacePolyline) shape).setClosed(false);
            layer.addRenderable(shape);

            ApplicationTemplate.insertBeforePlacenames(this.getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        start("World Wind Flat World", AppFrame.class);
    }
}
