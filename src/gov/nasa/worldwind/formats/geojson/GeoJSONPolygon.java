/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.AVList;

import java.util.Arrays;

/**
 * @author dcollins
 * @version $Id: GeoJSONPolygon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONPolygon extends GeoJSONGeometry
{
    public GeoJSONPolygon(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isPolygon()
    {
        return true;
    }

    public int getInteriorRingCount()
    {
        GeoJSONPositionArray[] array = this.getCoordinates();
        return array != null && array.length > 1 ? array.length - 1 : 0;
    }

    public GeoJSONPositionArray[] getCoordinates()
    {
        return (GeoJSONPositionArray[]) this.getValue(GeoJSONConstants.FIELD_COORDINATES);
    }

    public GeoJSONPositionArray getExteriorRing()
    {
        GeoJSONPositionArray[] array = this.getCoordinates();
        return array != null && array.length > 0 ? array[0] : null;
    }

    public GeoJSONPositionArray getInteriorRing(int ring)
    {
        GeoJSONPositionArray[] array = this.getCoordinates();
        return array != null && array.length > 1 ? array[1 + ring] : null;
    }

    public GeoJSONPositionArray[] getInteriorRings()
    {
        GeoJSONPositionArray[] array = this.getCoordinates();
        return array != null && array.length > 1 ? Arrays.copyOfRange(array, 1, array.length) : null;
    }
}
