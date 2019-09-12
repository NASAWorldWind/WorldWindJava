/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.*;

/**
 * @author dcollins
 * @version $Id: GeoJSONObject.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONObject extends AVListImpl
{
    public GeoJSONObject(AVList fields)
    {
        if (fields != null)
            this.setValues(fields);
    }

    public String getType()
    {
        return (String) this.getValue(GeoJSONConstants.FIELD_TYPE);
    }

    public AVList getCRS()
    {
        return (AVList) this.getValue(GeoJSONConstants.FIELD_CRS);
    }

    public Object[] getBoundingBox()
    {
        return (Object[]) this.getValue(GeoJSONConstants.FIELD_BBOX);
    }

    public boolean isGeometry()
    {
        return false;
    }

    public boolean isGeometryCollection()
    {
        return false;
    }

    public boolean isFeature()
    {
        return false;
    }

    public boolean isFeatureCollection()
    {
        return false;
    }

    public boolean isPoint()
    {
        return false;
    }

    public boolean isMultiPoint()
    {
        return false;
    }

    public boolean isLineString()
    {
        return false;
    }

    public boolean isMultiLineString()
    {
        return false;
    }

    public boolean isPolygon()
    {
        return false;
    }

    public boolean isMultiPolygon()
    {
        return false;
    }

    public GeoJSONGeometry asGeometry()
    {
        return (GeoJSONGeometry) this;
    }

    public GeoJSONGeometryCollection asGeometryCollection()
    {
        return (GeoJSONGeometryCollection) this;
    }

    public GeoJSONFeature asFeature()
    {
        return (GeoJSONFeature) this;
    }

    public GeoJSONFeatureCollection asFeatureCollection()
    {
        return (GeoJSONFeatureCollection) this;
    }

    public GeoJSONPoint asPoint()
    {
        return (GeoJSONPoint) this;
    }

    public GeoJSONMultiPoint asMultiPoint()
    {
        return (GeoJSONMultiPoint) this;
    }

    public GeoJSONLineString asLineString()
    {
        return (GeoJSONLineString) this;
    }

    public GeoJSONMultiLineString asMultiLineString()
    {
        return (GeoJSONMultiLineString) this;
    }

    public GeoJSONPolygon asPolygon()
    {
        return (GeoJSONPolygon) this;
    }

    public GeoJSONMultiPolygon asMultiPolygon()
    {
        return (GeoJSONMultiPolygon) this;
    }

    public String toString()
    {
        Object o = this.getValue(GeoJSONConstants.FIELD_TYPE);
        return o != null ? o.toString() : super.toString();
    }
}
