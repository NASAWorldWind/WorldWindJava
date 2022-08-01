/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Wiehann Matthysen
 */
public class WorldImageryLayer extends BasicTiledImageLayer
{

    public WorldImageryLayer()
    {
        super(makeLevels());
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/World Imagery");
        params.setValue(AVKey.SERVICE, "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_Imagery_World_2D/MapServer/export");
        params.setValue(AVKey.DATASET_NAME, "wi");
        params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
        params.setValue(AVKey.NUM_LEVELS, 15);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 9);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder
    {

        @Override
        public URL getURL(Tile tile, String altImageFormat) throws MalformedURLException
        {
            StringBuilder sb = new StringBuilder(tile.getLevel().getService());
            if (sb.lastIndexOf("?") != sb.length() - 1)
            {
                sb.append("?");
            }
            sb.append("format=jpg");
            sb.append("&f=image");
            sb.append("&size=");
            sb.append(tile.getLevel().getTileWidth());
            sb.append(",");
            sb.append(tile.getLevel().getTileHeight());

            Sector s = tile.getSector();
            sb.append("&bbox=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLatitude().getDegrees());

            return new java.net.URL(sb.toString());
        }
    }

    @Override
    public String toString()
    {
        return "World Imagery";
    }
}
