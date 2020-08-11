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
