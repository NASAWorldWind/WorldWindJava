/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;

/**
 * Represents the KML <i>LineString</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLineString.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLLineString extends KMLAbstractGeometry
{
    public KMLLineString(String namespaceURI)
    {
        super(namespaceURI);
    }

    public boolean isExtrude()
    {
        return this.getExtrude() == Boolean.TRUE;
    }

    public Boolean getExtrude()
    {
        return (Boolean) this.getField("extrude");
    }

    public boolean isTessellate()
    {
        return this.getTessellate() == Boolean.TRUE;
    }

    public Boolean getTessellate()
    {
        return (Boolean) this.getField("tessellate");
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }

    public Position.PositionList getCoordinates()
    {
        return (Position.PositionList) this.getField("coordinates");
    }
}
