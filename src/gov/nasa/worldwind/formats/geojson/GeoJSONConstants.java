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
