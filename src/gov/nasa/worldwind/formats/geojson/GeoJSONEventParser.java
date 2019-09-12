/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.formats.json.*;

/**
 * @author dcollins
 * @version $Id: GeoJSONEventParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONEventParser extends BasicJSONEventParser
{
    public GeoJSONEventParser()
    {
    }

    protected Object resolveObject(JSONEventParserContext ctx, JSONEvent event)
    {
        if (this.fields == null)
            return super.resolveObject(ctx, event);

        Object type = this.fields.getValue(GeoJSONConstants.FIELD_TYPE);

        if (GeoJSONConstants.TYPE_POINT.equals(type))
            return new GeoJSONPoint(this.fields);

        else if (GeoJSONConstants.TYPE_MULTI_POINT.equals(type))
            return new GeoJSONMultiPoint(this.fields);

        else if (GeoJSONConstants.TYPE_LINE_STRING.equals(type))
            return new GeoJSONLineString(this.fields);

        else if (GeoJSONConstants.TYPE_MULTI_LINE_STRING.equals(type))
            return new GeoJSONMultiLineString(this.fields);

        else if (GeoJSONConstants.TYPE_POLYGON.equals(type))
            return new GeoJSONPolygon(this.fields);

        else if (GeoJSONConstants.TYPE_MULTI_POLYGON.equals(type))
            return new GeoJSONMultiPolygon(this.fields);

        else if (GeoJSONConstants.TYPE_GEOMETRY_COLLECTION.equals(type))
            return new GeoJSONGeometryCollection(this.fields);

        else if (GeoJSONConstants.TYPE_FEATURE.equals(type))
            return new GeoJSONFeature(this.fields);

        else if (GeoJSONConstants.TYPE_FEATURE_COLLECTION.equals(type))
            return new GeoJSONFeatureCollection(this.fields);

        else
            return super.resolveObject(ctx, event);
    }

    @SuppressWarnings({"SuspiciousToArrayCall"})
    @Override
    protected Object resolveArray(JSONEventParserContext ctx, JSONEvent event)
    {
        if (GeoJSONConstants.FIELD_FEATURES.equals(ctx.getCurrentFieldName()))
            return this.array.toArray(new GeoJSONFeature[this.array.size()]);

        else if (GeoJSONConstants.FIELD_GEOMETRIES.equals(ctx.getCurrentFieldName()))
            return this.array.toArray(new GeoJSONFeature[this.array.size()]);

        return super.resolveArray(ctx, event);
    }
}
