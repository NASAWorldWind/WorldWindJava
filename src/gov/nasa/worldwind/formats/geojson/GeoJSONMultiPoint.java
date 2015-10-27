/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;

/**
 * @author dcollins
 * @version $Id: GeoJSONMultiPoint.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONMultiPoint extends GeoJSONGeometry
{
    public GeoJSONMultiPoint(AVList fields)
    {
        super(fields);
    }

    @Override
    public boolean isMultiPoint()
    {
        return true;
    }

    public GeoJSONPositionArray getCoordinates()
    {
        return (GeoJSONPositionArray) this.getValue(GeoJSONConstants.FIELD_COORDINATES);
    }

    public int getPointCount()
    {
        GeoJSONPositionArray array = this.getCoordinates();
        return array != null ? array.length() : 0;
    }

    public Position getPosition(int point)
    {
        GeoJSONPositionArray array = this.getCoordinates();
        return array != null ? array.getPosition(point) : null;
    }
}
