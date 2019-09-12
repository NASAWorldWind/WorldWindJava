/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: GeoJSONLineString.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONLineString extends GeoJSONGeometry
{
    public GeoJSONLineString(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isLineString()
    {
        return true;
    }

    public GeoJSONPositionArray getCoordinates()
    {
        return (GeoJSONPositionArray) this.getValue(GeoJSONConstants.FIELD_COORDINATES);
    }
}
