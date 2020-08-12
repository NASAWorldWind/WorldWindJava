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
