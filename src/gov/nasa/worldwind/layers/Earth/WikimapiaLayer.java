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
public class WikimapiaLayer extends BasicMercatorTiledImageLayer
{
    public enum Type {MAP, HYBRID}
    
    public WikimapiaLayer()
    {
        super("wm", "Earth/Wikimapia", 19, 256, true, ".png", new URLBuilder());
    }

    private static class URLBuilder extends MercatorTileUrlBuilder
    {
        private Type type;
        
        private URLBuilder()
        {
            this.type = Type.HYBRID;
        }
        
        @Override
        protected URL getMercatorURL(int x, int y, int z) throws MalformedURLException
        {
            int i = x % 4 + (y % 4) * 4;
            return new URL("http://i" + i + ".wikimapia.org/?lng=0&x=" + x + "&y=" + y + "&zoom=" + z + "&type=" + this.type.name().toLowerCase());
        }
    }
    
    public void setType(String type)
    {
        URLBuilder urlBuilder = (URLBuilder)getURLBuilder();
        urlBuilder.type = Type.valueOf(type);
        
        // Toggle overlay based on whether it is a hybrid map or not.
        boolean isHybrid = urlBuilder.type.equals(Type.HYBRID);
        setUseTransparentTextures(isHybrid);
    }
    
    public String getType()
    {
        URLBuilder urlBuilder = (URLBuilder)getURLBuilder();
        return urlBuilder.type.name();
    }

    @Override
    public String toString()
    {
        return "Wikimapia";
    }
}