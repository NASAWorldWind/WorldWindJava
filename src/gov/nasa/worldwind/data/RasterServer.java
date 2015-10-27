/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;

import java.nio.ByteBuffer;

/**
 * @author tag
 * @version $Id: RasterServer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface RasterServer
{
    /**
     * Composes a Raster and returns as ByteBuffer in the requested format (image or elevation)
     *
     * @param params Required parameters in params:
     *               <p/>
     *               AVKey.WIDTH - the height of the requested raster AVKey.HEIGHT - the height of the requested raster
     *               AVKey.SECTOR - a regular Geographic Sector defined by lat/lon coordinates of corners
     *
     * @return ByteBuffer of the requested file format
     */
    ByteBuffer getRasterAsByteBuffer(AVList params);

    /**
     * Returns a Geographic extend (coverage) of the composer
     *
     * @return returns a Geographic extend (coverage) of the composer as a Sector
     */
    public Sector getSector();
}
