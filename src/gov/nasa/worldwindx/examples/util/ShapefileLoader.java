/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

/**
 * Converts Shapefile geometry into World Wind renderable objects. Shapefile geometries are mapped to World Wind objects
 * as follows: <table> <tr><th>Shapefile Geometry</th><th>World Wind Object</th></tr> <tr><td>Point</td><td>{@link
 * gov.nasa.worldwind.render.WWIcon}</td></tr> <tr><td>MultiPoint</td><td>List of {@link
 * gov.nasa.worldwind.render.WWIcon}</td></tr> <tr><td>Polyline</td><td>{@link gov.nasa.worldwind.render.SurfacePolylines}</td></tr>
 * <tr><td>Polygon</td><td>{@link gov.nasa.worldwind.render.SurfacePolygons}</td></tr> </table>
 * <p/>
 * Shapefiles do not contain a standard definition for color and other visual attributes. Though some Shapefiles contain
 * color information in each record's key-value attributes, ShapefileLoader does not attempt to interpret that
 * information. Instead, the World Wind renderable objects created by ShapefileLoader are assigned a random color.
 * Callers can replace or extend this behavior by defining a subclass of ShapefileLoader and overriding the following
 * methods: <ul> <li>{@link #nextPointAttributes()}</li> <li>{@link #nextPolylineAttributes()}</li> <li>{@link
 * #nextPolygonAttributes()}</li></ul>.
 *
 * @author dcollins
 * @version $Id: ShapefileLoader.java 2326 2014-09-17 22:35:45Z dcollins $
 * @deprecated Use {@link ShapefileLayerFactory} instead.
 */
public class ShapefileLoader
{
    protected static final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

    /** Constructs a ShapefileLoader, but otherwise does nothing. */
    public ShapefileLoader()
    {
    }

    /**
     * Creates a {@link gov.nasa.worldwind.layers.Layer} from a general Shapefile source. The source type may be one of
     * the following: <ul> <li>{@link java.io.InputStream}</li> <li>{@link java.net.URL}</li> <li>{@link
     * java.io.File}</li> <li>{@link String} containing a valid URL description or a file or resource name available on
     * the classpath.</li> </ul>
     *
     * @param source the source of the Shapefile.
     *
     * @return a Layer that renders the Shapefile's contents on the surface of the Globe.
     *
     * @throws IllegalArgumentException if the source is null or an empty string, or if the Shapefile's primitive type
     *                                  is unrecognized.
     */
    public Layer createLayerFromSource(Object source)
    {
        if (WWUtil.isEmpty(source))
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
            layer = this.createLayerFromShapefile(shp);
        }
        finally
        {
            WWIO.closeStream(shp, source.toString());
        }

        return layer;
    }

    /**
     * Creates a {@link gov.nasa.worldwind.layers.Layer} from a general Shapefile.
     *
     * @param shp the Shapefile to create a layer for.
     *
     * @return a Layer that renders the Shapefile's contents on the surface of the Globe.
     *
     * @throws IllegalArgumentException if the Shapefile is null, or if the Shapefile's primitive type is unrecognized.
     */
    public Layer createLayerFromShapefile(Shapefile shp)
    {
        if (shp == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Layer layer = null;

        if (Shapefile.isPointType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForPoints(shp, (RenderableLayer) layer);
        }
        else if (Shapefile.isMultiPointType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForMultiPoints(shp, (RenderableLayer) layer);
        }
        else if (Shapefile.isPolylineType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForPolylines(shp, (RenderableLayer) layer);
        }
        else if (Shapefile.isPolygonType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForPolygons(shp, (RenderableLayer) layer);
        }
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedShapeType", shp.getShapeType()));
        }

        if (layer != null && shp.getBoundingRectangle() != null)
        {
            layer.setValue(AVKey.SECTOR, Sector.fromDegrees(shp.getBoundingRectangle()));
        }

        return layer;
    }

    //**************************************************************//
    //********************  Geometry Conversion  *******************//
    //**************************************************************//

    protected void addRenderablesForPoints(Shapefile shp, RenderableLayer layer)
    {
        PointPlacemarkAttributes attrs = this.nextPointAttributes();

        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();

            if (!Shapefile.isPointType(record.getShapeType()))
                continue;

            double[] point = ((ShapefileRecordPoint) record).getPoint();
            layer.addRenderable(this.createPoint(record, point[1], point[0], attrs));
        }
    }

    protected void addRenderablesForMultiPoints(Shapefile shp, RenderableLayer layer)
    {
        PointPlacemarkAttributes attrs = this.nextPointAttributes();

        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();

            if (!Shapefile.isMultiPointType(record.getShapeType()))
                continue;

            Iterable<double[]> iterable = ((ShapefileRecordMultiPoint) record).getPoints(0);

            for (double[] point : iterable)
            {
                layer.addRenderable(this.createPoint(record, point[1], point[0], attrs));
            }
        }
    }

    protected void addRenderablesForPolylines(Shapefile shp, RenderableLayer layer)
    {
        // Reads all records from the Shapefile, but ignores each records unique information. We do this to create one
        // WWJ object representing the entire shapefile, which as of 8/10/2010 is required to display very large
        // polyline Shapefiles. To create one WWJ object for each Shapefile record, replace this method's contents with
        // the following:
        //
        //while (shp.hasNext())
        //{
        //    ShapefileRecord record = shp.nextRecord();
        //
        //    if (!Shapefile.isPolylineType(record.getShapeType()))
        //        continue;
        //
        //    ShapeAttributes attrs = this.createPolylineAttributes(record);
        //    layer.addRenderable(this.createPolyline(record, attrs));
        //}

        while (shp.hasNext())
        {
            shp.nextRecord();
        }

        ShapeAttributes attrs = this.nextPolylineAttributes();
        layer.addRenderable(this.createPolyline(shp, attrs));
    }

    protected void addRenderablesForPolygons(Shapefile shp, RenderableLayer layer)
    {
        if (this.hasHeight(shp))
        {
            this.addRenderablesForExtrudedPolygons(shp, layer);
        }
        else
        {
            this.addRenderablesForSurfacePolygons(shp, layer);
        }
    }

    protected void addRenderablesForSurfacePolygons(Shapefile shp, RenderableLayer layer)
    {
        ShapeAttributes attrs = this.nextPolygonAttributes();

        int recordNumber = 0;
        while (shp.hasNext())
        {
            try
            {
                ShapefileRecord record = shp.nextRecord();
                recordNumber = record.getRecordNumber();

                if (!Shapefile.isPolygonType(record.getShapeType()))
                    continue;

                this.createPolygon(record, attrs, layer);
            }
            catch (Exception e)
            {
                Logging.logger().warning(Logging.getMessage("SHP.ExceptionAttemptingToConvertShapefileRecord",
                    recordNumber, e));
                // continue with the remaining records
            }
        }
    }

    protected void addRenderablesForExtrudedPolygons(Shapefile shp, RenderableLayer layer)
    {
        ShapeAttributes attrs = this.nextPolygonAttributes();
        ShapefileExtrudedPolygons shape = new ShapefileExtrudedPolygons(shp, attrs, null, null);
        layer.addRenderable(shape);
    }

    //**************************************************************//
    //********************  Primitive Geometry Construction  *******//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected Renderable createPoint(ShapefileRecord record, double latDegrees, double lonDegrees,
        PointPlacemarkAttributes attrs)
    {
        PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(latDegrees, lonDegrees, 0));
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(attrs);

        return placemark;
    }

    protected Renderable createPolyline(ShapefileRecord record, ShapeAttributes attrs)
    {
        SurfacePolylines shape = new SurfacePolylines(
            Sector.fromDegrees(((ShapefileRecordPolyline) record).getBoundingRectangle()),
            record.getCompoundPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected Renderable createPolyline(Shapefile shp, ShapeAttributes attrs)
    {
        SurfacePolylines shape = new SurfacePolylines(Sector.fromDegrees(shp.getBoundingRectangle()),
            shp.getPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected void createPolygon(ShapefileRecord record, ShapeAttributes attrs, RenderableLayer layer)
    {
        Double height = this.getHeight(record);
        if (height != null) // create extruded polygons
        {
            ExtrudedPolygon ep = new ExtrudedPolygon(height);
            ep.setAttributes(attrs);
            layer.addRenderable(ep);

            for (int i = 0; i < record.getNumberOfParts(); i++)
            {
                // Although the shapefile spec says that inner and outer boundaries can be listed in any order, it's
                // assumed here that inner boundaries are at least listed adjacent to their outer boundary, either
                // before or after it. The below code accumulates inner boundaries into the extruded polygon until an
                // outer boundary comes along. If the outer boundary comes before the inner boundaries, the inner
                // boundaries are added to the polygon until another outer boundary comes along, at which point a new
                // extruded polygon is started.

                VecBuffer buffer = record.getCompoundPointBuffer().subBuffer(i);
                if (WWMath.computeWindingOrderOfLocations(buffer.getLocations()).equals(AVKey.CLOCKWISE))
                {
                    if (!ep.getOuterBoundary().iterator().hasNext()) // has no outer boundary yet
                    {
                        ep.setOuterBoundary(buffer.getLocations());
                    }
                    else
                    {
                        ep = new ExtrudedPolygon();
                        ep.setAttributes(attrs);
                        ep.setOuterBoundary(record.getCompoundPointBuffer().getLocations());
                        layer.addRenderable(ep);
                    }
                }
                else
                {
                    ep.addInnerBoundary(buffer.getLocations());
                }
            }
        }
        else // create surface polygons
        {
            SurfacePolygons shape = new SurfacePolygons(
                Sector.fromDegrees(((ShapefileRecordPolygon) record).getBoundingRectangle()),
                record.getCompoundPointBuffer());
            shape.setAttributes(attrs);
            // Configure the SurfacePolygons as a single large polygon.
            // Configure the SurfacePolygons to correctly interpret the Shapefile polygon record. Shapefile polygons may
            // have rings defining multiple inner and outer boundaries. Each ring's winding order defines whether it's an
            // outer boundary or an inner boundary: outer boundaries have a clockwise winding order. However, the
            // arrangement of each ring within the record is not significant; inner rings can precede outer rings and vice
            // versa.
            //
            // By default, SurfacePolygons assumes that the sub-buffers are arranged such that each outer boundary precedes
            // a set of corresponding inner boundaries. SurfacePolygons traverses the sub-buffers and tessellates a new
            // polygon each  time it encounters an outer boundary. Outer boundaries are sub-buffers whose winding order
            // matches the SurfacePolygons' windingRule property.
            //
            // This default behavior does not work with Shapefile polygon records, because the sub-buffers of a Shapefile
            // polygon record can be arranged arbitrarily. By calling setPolygonRingGroups(new int[]{0}), the
            // SurfacePolygons interprets all sub-buffers as boundaries of a single tessellated shape, and configures the
            // GLU tessellator's winding rule to correctly interpret outer and inner boundaries (in any arrangement)
            // according to their winding order. We set the SurfacePolygons' winding rule to clockwise so that sub-buffers
            // with a clockwise winding ordering are interpreted as outer boundaries.
            shape.setWindingRule(AVKey.CLOCKWISE);
            shape.setPolygonRingGroups(new int[] {0});
            shape.setPolygonRingGroups(new int[] {0});
            layer.addRenderable(shape);
        }
    }

    /**
     * Get the height of a record.
     *
     * @param record shape for which to find height
     *
     * @return the height of the shape, or {@code null} if there is no height.
     */
    protected Double getHeight(ShapefileRecord record)
    {
        return ShapefileUtils.extractHeightAttribute(record);
    }

    protected boolean hasHeight(Shapefile shapefile)
    {
        return ShapefileUtils.hasHeightAttribute(shapefile);
    }

    //**************************************************************//
    //********************  Attribute Construction  ****************//
    //**************************************************************//

    protected PointPlacemarkAttributes nextPointAttributes()
    {
        synchronized (randomAttrs)
        {
            return randomAttrs.nextAttributes().asPointAttributes();
        }
    }

    protected ShapeAttributes nextPolylineAttributes()
    {
        synchronized (randomAttrs)
        {
            return randomAttrs.nextAttributes().asShapeAttributes();
        }
    }

    protected ShapeAttributes nextPolygonAttributes()
    {
        synchronized (randomAttrs)
        {
            return randomAttrs.nextAttributes().asShapeAttributes();
        }
    }
}