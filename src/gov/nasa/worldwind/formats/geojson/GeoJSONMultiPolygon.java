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
 * @version $Id: GeoJSONMultiPolygon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONMultiPolygon extends GeoJSONGeometry
{
    public GeoJSONMultiPolygon(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isMultiPolygon()
    {
        return true;
    }

    public GeoJSONPositionArray[][] getCoordinates()
    {
        return (GeoJSONPositionArray[][]) this.getValue(GeoJSONConstants.FIELD_COORDINATES);
    }

    public int getPolygonCount()
    {
        GeoJSONPositionArray[][] array = this.getCoordinates();
        return array != null ? array.length : 0;
    }

    public int getInteriorRingCount(int polygon)
    {
        GeoJSONPositionArray[] array = this.getCoordinates(polygon);
        return array != null && array.length > 1 ? array.length - 1 : 0;
    }

    public GeoJSONPositionArray[] getCoordinates(int polygon)
    {
        GeoJSONPositionArray[][] array = this.getCoordinates();
        return array != null && array.length > 0 ? array[polygon] : null;
    }

    public GeoJSONPositionArray getExteriorRing(int polygon)
    {
        GeoJSONPositionArray[] array = this.getCoordinates(polygon);
        return array != null && array.length > 0 ? array[0] : null;
    }

    public GeoJSONPositionArray getInteriorRing(int polygon, int ring)
    {
        GeoJSONPositionArray[] array = this.getCoordinates(polygon);
        return array != null && array.length > 1 ? array[1 + ring] : null;
    }

    public GeoJSONPositionArray[] getInteriorRings(int polygon)
    {
        GeoJSONPositionArray[] array = this.getCoordinates(polygon);
        return array != null && array.length > 1 ? Arrays.copyOfRange(array, 1, array.length) : null;
    }
}
