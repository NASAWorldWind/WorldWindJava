/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import java.net.URL;

/**
 * @author lado
 * @version $Id: TileUrlBuilder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface TileUrlBuilder
{
        public URL getURL(Tile tile, String imageFormat) throws java.net.MalformedURLException;
}
