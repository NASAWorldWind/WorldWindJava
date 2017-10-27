/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.mercator.*;
import gov.nasa.worldwind.util.*;

import java.net.*;

/**
 * @version $Id: OSMMapnikLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OSMMapnikLayer extends BasicMercatorTiledImageLayer
{
    static final double DEFAULT_OPACITY = 0.5d;

    public OSMMapnikLayer()
    {
        super(makeLevels());
        setOpacity(DEFAULT_OPACITY);
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 256);
        params.setValue(AVKey.TILE_HEIGHT, 256);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/OSM-Mercator/OpenStreetMap Mapnik");
        params.setValue(AVKey.SERVICE, "http://a.tile.openstreetmap.org/");
        params.setValue(AVKey.DATASET_NAME, "h");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 16);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
            .fromDegrees(22.5d), Angle.fromDegrees(45d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder
    {
        public URL getURL(Tile tile, String imageFormat)
            throws MalformedURLException
        {
            return new URL(tile.getLevel().getService()
                + (tile.getLevelNumber() + 3) + "/" + tile.getColumn() + "/"
                + ((1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow()) + ".png");
        }
    }

    @Override
    public String toString()
    {
        return "OpenStreetMap";
    }
}
