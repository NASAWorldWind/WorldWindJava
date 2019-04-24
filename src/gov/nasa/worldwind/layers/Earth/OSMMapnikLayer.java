/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.mercator.*;

import java.net.*;

/**
 * @version $Id: OSMMapnikLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OSMMapnikLayer extends BasicMercatorTiledImageLayer
{
    public OSMMapnikLayer()
    {
        super("h", "Earth/OSM-Mercator/OpenStreetMap Mapnik", 19, 256, false, ".png", new URLBuilder());
    }

    private static class URLBuilder extends MercatorTileUrlBuilder
    {
        @Override
        protected URL getMercatorURL(int x, int y, int z) throws MalformedURLException
        {
            return new URL("https://a.tile.openstreetmap.org/" + z + "/" + x + "/" + y + ".png");
        }
    }

    @Override
    public String toString()
    {
        return "OpenStreetMap";
    }
}
