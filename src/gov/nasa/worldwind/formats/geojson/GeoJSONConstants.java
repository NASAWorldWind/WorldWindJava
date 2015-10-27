/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

/**
 * @author dcollins
 * @version $Id: GeoJSONConstants.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GeoJSONConstants
{
    final String FIELD_TYPE = "type";
    final String FIELD_CRS = "crs";
    final String FIELD_BBOX = "bbox";
    final String FIELD_COORDINATES = "coordinates";
    final String FIELD_GEOMETRIES = "geometries";
    final String FIELD_GEOMETRY = "geometry";
    final String FIELD_PROPERTIES = "properties";
    final String FIELD_FEATURES = "features";

    final String TYPE_POINT = "Point";
    final String TYPE_MULTI_POINT = "MultiPoint";
    final String TYPE_LINE_STRING = "LineString";
    final String TYPE_MULTI_LINE_STRING = "MultiLineString";
    final String TYPE_POLYGON = "Polygon";
    final String TYPE_MULTI_POLYGON = "MultiPolygon";
    final String TYPE_GEOMETRY_COLLECTION = "GeometryCollection";
    final String TYPE_FEATURE = "Feature";
    final String TYPE_FEATURE_COLLECTION = "FeatureCollection";
}
