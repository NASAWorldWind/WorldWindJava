/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: OpenStreetMapShapefileLoader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OpenStreetMapShapefileLoader
{
    /**
     * Returns true if the specified Shapefile source is an OpenStreetMap Shapefile containing placemarks, and false
     * otherwise. The source is considered to be an OpenStreetMap source if it can be converted to an abstract path, and
     * the path's filename is equal to "places.shp", ignoring case.
     *
     * @param source the source of the Shapefile.
     *
     * @return true if the Shapefile is an OpenStreetMap Shapefile; false otherwise.
     *
     * @throws IllegalArgumentException if the source is null or an empty string.
     */
    public static boolean isOSMPlacesSource(Object source)
    {
        if (source == null || WWUtil.isEmpty(source))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String path = WWIO.getSourcePath(source);
        return path != null && WWIO.getFilename(path).equalsIgnoreCase("places.shp");
    }

    /**
     * Creates a {@link gov.nasa.worldwind.layers.Layer} from an OpenStreetMap Shapefile source of placemarks. The
     * source type may be one of the following: <ul> <li>{@link java.io.InputStream}</li> <li>{@link java.net.URL}</li>
     * <li>absolute {@link java.net.URI}</li> <li>{@link java.io.File}</li> <li>{@link String} containing a valid URL
     * description or a file or resource name available on the classpath.</li> </ul>
     * <p/>
     * The returned Layer renders each Shapefile record as a surface circle with an associated screen label. The label
     * text is taken from the Shapefile record attribute key "name". This determines each surface circle's appearance
     * from the Shapefile record attribute key "type" as follows: <table> <tr><th>Type</th><th>Color</th></tr>
     * <tr><td>hamlet</td><td>Black</td></tr> <tr><td>village</td><td>Green</td></tr>
     * <tr><td>town</td><td>Cyan</td></tr> <tr><td>city</td><td>Yellow</td></tr> </table>
     *
     * @param source the source of the OpenStreetMap Shapefile.
     *
     * @return a Layer that renders the Shapefile's contents on the surface of the Globe.
     *
     * @throws IllegalArgumentException if the source is null or an empty string.
     */
    public static Layer makeLayerFromOSMPlacesSource(Object source)
    {
        if (source == null || WWUtil.isEmpty(source))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Shapefile shp = null;
        Layer layer = null;
        try
        {
            shp = new Shapefile(source);
            layer = makeLayerFromOSMPlacesShapefile(shp);
        }
        finally
        {
            if (shp != null)
                shp.close();
        }

        return layer;
    }

    /**
     * Creates a {@link gov.nasa.worldwind.layers.Layer} from an OpenStreetMap Shapefile of placemarks.
     * <p/>
     * The returned Layer renders each Shapefile record as a surface circle with an associated screen label. The label
     * text is taken from the Shapefile record attribute key "name". This determines each surface circle's appearance
     * from the Shapefile record attribute key "type" as follows: <table> <tr><th>Type</th><th>Color</th></tr>
     * <tr><td>hamlet</td><td>Black</td></tr> <tr><td>village</td><td>Green</td></tr>
     * <tr><td>town</td><td>Cyan</td></tr> <tr><td>city</td><td>Yellow</td></tr> </table>
     *
     * @param shp the Shapefile to create a layer for.
     *
     * @return a Layer that renders the Shapefile's contents on the surface of the Globe.
     *
     * @throws IllegalArgumentException if the Shapefile is null, or if the Shapefile's primitive type is unrecognized.
     */
    public static Layer makeLayerFromOSMPlacesShapefile(Shapefile shp)
    {
        if (shp == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OSMShapes[] shapeArray =
            {
                new OSMShapes(Color.BLACK, .3, 30e3), // hamlet
                new OSMShapes(Color.GREEN, .5, 100e3), // village
                new OSMShapes(Color.CYAN, 1, 500e3), // town
                new OSMShapes(Color.YELLOW, 2, 3000e3) // city
            };

        // Filter records for a particular sector
        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();
            if (record == null || !record.getShapeType().equals(Shapefile.SHAPE_POINT))
                continue;

            Object o = record.getAttributes().getValue("type");
            if (o == null || !(o instanceof String))
                continue;

            // Add points with different rendering attribute for different subsets
            OSMShapes shapes = null;
            String type = (String) o;
            if (type.equalsIgnoreCase("hamlet"))
            {
                shapes = shapeArray[0];
            }
            else if (type.equalsIgnoreCase("village"))
            {
                shapes = shapeArray[1];
            }
            else if (type.equalsIgnoreCase("town"))
            {
                shapes = shapeArray[2];
            }
            else if (type.equalsIgnoreCase("city"))
            {
                shapes = shapeArray[3];
            }

            if (shapes == null)
                continue;

            String name = null;

            AVList attr = record.getAttributes();
            if (attr.getEntries() != null)
            {
                for (Map.Entry<String, Object> entry : attr.getEntries())
                {
                    if (entry.getKey().equalsIgnoreCase("name"))
                    {
                        name = (String) entry.getValue();
                        break;
                    }
                }
            }

            // Note: points are stored in the buffer as a sequence of X and Y with X = longitude, Y = latitude.
            double[] pointCoords = ((ShapefileRecordPoint) record).getPoint();
            LatLon location = LatLon.fromDegrees(pointCoords[1], pointCoords[0]);

            if (!WWUtil.isEmpty(name))
            {
                Label label = new Label(name, new Position(location, 0));
                label.setFont(shapes.font);
                label.setColor(shapes.foreground);
                label.setBackgroundColor(shapes.background);
                label.setMaxActiveAltitude(shapes.labelMaxAltitude);
                label.setPriority(shapes.labelMaxAltitude);
                shapes.labels.add(label);
            }

            shapes.locations.add(location);
        }

        TextAndShapesLayer layer = new TextAndShapesLayer();

        for (OSMShapes shapes : shapeArray)
        {
            // Use one SurfaceIcons instance for all points.
            BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f, shapes.foreground);
            SurfaceIcons sis = new SurfaceIcons(image, shp.getPointBuffer().getLocations());
            sis.setMaxSize(4e3 * shapes.scale); // 4km
            sis.setMinSize(100);  // 100m
            sis.setScale(shapes.scale);
            sis.setOpacity(.8);
            layer.addRenderable(sis);
            shapes.locations.clear();

            for (Label label : shapes.labels)
            {
                layer.addLabel(label);
            }
            shapes.labels.clear();
        }

        return layer;
    }

    //**************************************************************//
    //********************  Helper Classes  ************************//
    //**************************************************************//

    protected static class OSMShapes
    {
        public ArrayList<LatLon> locations = new ArrayList<LatLon>();
        public ArrayList<Label> labels = new ArrayList<Label>();
        public Color foreground;
        public Color background;
        public Font font;
        public double scale;
        public double labelMaxAltitude;

        public OSMShapes(Color color, double scale, double labelMaxAltitude)
        {
            this.foreground = color;
            this.background = WWUtil.computeContrastingColor(color);
            this.font = new Font("Arial", Font.BOLD, 10 + (int) (3 * scale));
            this.scale = scale;
            this.labelMaxAltitude = labelMaxAltitude;
        }
    }

    protected static class TextAndShapesLayer extends RenderableLayer
    {
        protected ArrayList<GeographicText> labels = new ArrayList<GeographicText>();
        protected GeographicTextRenderer textRenderer = new GeographicTextRenderer();

        public TextAndShapesLayer()
        {
            this.textRenderer.setCullTextEnabled(true);
            this.textRenderer.setCullTextMargin(2);
            this.textRenderer.setDistanceMaxScale(2);
            this.textRenderer.setDistanceMinScale(.5);
            this.textRenderer.setDistanceMinOpacity(.5);
            this.textRenderer.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        }

        public void addLabel(GeographicText label)
        {
            this.labels.add(label);
        }

        public void doRender(DrawContext dc)
        {
            super.doRender(dc);
            this.setActiveLabels(dc);
            this.textRenderer.render(dc, this.labels);
        }

        protected void setActiveLabels(DrawContext dc)
        {
            for (GeographicText text : this.labels)
            {
                if (text instanceof Label)
                    text.setVisible(((Label) text).isActive(dc));
            }
        }
    }

    protected static class Label extends UserFacingText
    {
        protected double minActiveAltitude = -Double.MAX_VALUE;
        protected double maxActiveAltitude = Double.MAX_VALUE;

        public Label(String text, Position position)
        {
            super(text, position);
        }

        public void setMinActiveAltitude(double altitude)
        {
            this.minActiveAltitude = altitude;
        }

        public void setMaxActiveAltitude(double altitude)
        {
            this.maxActiveAltitude = altitude;
        }

        public boolean isActive(DrawContext dc)
        {
            double eyeElevation = dc.getView().getEyePosition().getElevation();
            return this.minActiveAltitude <= eyeElevation && eyeElevation <= this.maxActiveAltitude;
        }
    }
}
