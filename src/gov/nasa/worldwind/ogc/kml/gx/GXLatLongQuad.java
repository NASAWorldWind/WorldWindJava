/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractObject;

/**
 * @author tag
 * @version $Id: GXLatLongQuad.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GXLatLongQuad extends KMLAbstractObject
{
    public GXLatLongQuad(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Position.PositionList getCoordinates()
    {
        return (Position.PositionList) this.getField("coordinates");
    }
}
