/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.mercator.*;

import java.net.*;

/**
 * @author Sufaev
 */
public class OpenTopoMapLayer extends BasicMercatorTiledImageLayer
{
    public OpenTopoMapLayer()
    {
        super("otm", "Earth/OpenTopoMap", 17, 256, false, ".png", new URLBuilder());
    }

    private static class URLBuilder extends MercatorTileUrlBuilder
    {
        @Override
        protected URL getMercatorURL(int x, int y, int z) throws MalformedURLException
        {
            return new URL("https://a.tile.opentopomap.org/" + z + "/" + x + "/"  + y + ".png");
        }
    }

    @Override
    public String toString()
    {
        return "OpenTopoMap";
    }
}
