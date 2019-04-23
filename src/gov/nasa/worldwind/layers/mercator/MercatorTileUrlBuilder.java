/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers.mercator;

import gov.nasa.worldwind.util.*;

import java.net.*;

/**
 * @author Sufaev
 */
public abstract class MercatorTileUrlBuilder implements TileUrlBuilder
{
    private static final int DEFAULT_FIRST_LEVEL_OFFSET = 3;
    
    private int firstLevelOffset;
    
    protected MercatorTileUrlBuilder()
    {
        this.firstLevelOffset = DEFAULT_FIRST_LEVEL_OFFSET;
    }

    public MercatorTileUrlBuilder setFirstLevelOffset(int firstLevelOffset) {
        this.firstLevelOffset = firstLevelOffset;
        return this;
    }

    public int getFirstLevelOffset() {
        return firstLevelOffset;
    }

    @Override
    public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
    {
        return getMercatorURL(tile.getColumn(), (1 << (tile.getLevelNumber() + firstLevelOffset)) - 1 - tile.getRow(), tile.getLevelNumber() + firstLevelOffset);
    }

    protected abstract URL getMercatorURL(int x, int y, int z) throws MalformedURLException;
}
