/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: GeoJSONFeatureCollection.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONFeatureCollection extends GeoJSONObject
{
    public GeoJSONFeatureCollection(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isFeatureCollection()
    {
        return true;
    }

    public GeoJSONFeature[] getFeatures()
    {
        return (GeoJSONFeature[]) this.getValue(GeoJSONConstants.FIELD_FEATURES);
    }
}
