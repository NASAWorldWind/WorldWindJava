/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;

/**
 * @author tag
 * @version $Id: GXTour.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GXTour extends KMLAbstractFeature
{
    public GXTour(String namespaceURI)
    {
        super(namespaceURI);
    }

    public GXPlaylist getPlaylist()
    {
        return (GXPlaylist) this.getField("Playlist");
    }
}
