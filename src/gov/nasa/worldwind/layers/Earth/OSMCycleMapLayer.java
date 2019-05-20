/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.mercator.*;

import java.net.*;

/**
 * @version $Id: OSMCycleMapLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OSMCycleMapLayer extends BasicMercatorTiledImageLayer
{
    public OSMCycleMapLayer()
    {
        super("h", "Earth/OSM-Mercator/OpenStreetMap Cycle", 19, 256, false, ".png", new URLBuilder());
    }
    
    private static class URLBuilder extends MercatorTileUrlBuilder
    {
        private String apiKey;
        
        @Override
        protected URL getMercatorURL(int x, int y, int z) throws MalformedURLException
        {
            String urlPostfix = (this.apiKey != null) ? "?apikey=" + this.apiKey : "";
            return new URL("https://a.tile.thunderforest.com/cycle/" + z + "/" + x + "/" + y + ".png" + urlPostfix);
        }
    }
    
    public void setAPIKey(String apiKey)
    {
        URLBuilder urlBuilder = (URLBuilder)getURLBuilder();
        urlBuilder.apiKey = apiKey;
    }
    
    public String getAPIKey()
    {
        URLBuilder urlBuilder = (URLBuilder)getURLBuilder();
        return urlBuilder.apiKey;
    }

    @Override
    public String toString()
    {
        return "OpenStreetMap Cycle";
    }
}
