/**
 * Copyright (C) 2014, United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration,
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.LevelSet;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Procedural height-map layer.
 * @author Patrick Murris
 * @version $Id:$
 */
public class HeightmapLayer extends ProceduralTiledImageLayer
{
    
    private final Globe globe;
    
    public HeightmapLayer(Globe globe)
    {
        super(makeLevels());
        this.globe = globe;
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();
        params.setValue(AVKey.TILE_WIDTH, 128);
        params.setValue(AVKey.TILE_HEIGHT, 128);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Heightmap");
        params.setValue(AVKey.DATASET_NAME, "Heightmap");
        params.setValue(AVKey.FORMAT_SUFFIX, ".png");
        params.setValue(AVKey.NUM_LEVELS, 10);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36.0), Angle.fromDegrees(36.0)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        return new LevelSet(params);
    }

    @Override
    protected BufferedImage createTileImage(TextureTile tile, BufferedImage image)
    {
        int width = tile.getLevel().getTileWidth();
        int height = tile.getLevel().getTileHeight();
        double latStep = tile.getSector().getDeltaLatDegrees() / height;
        double lonStep = tile.getSector().getDeltaLonDegrees() / width;

        for (int x = 0; x < width; x++)
        {
            double lon = tile.getSector().getMinLongitude().degrees + lonStep * x + (lonStep / 2.0);
            for (int y = 0; y < height; y++)
            {
                double lat = tile.getSector().getMaxLatitude().degrees - latStep * y - (latStep / 2.0);
                double elevation = this.globe.getElevation(Angle.fromDegrees(lat), Angle.fromDegrees(lon));
                double ratio = (elevation  - this.globe.getMinElevation()) / (this.globe.getMaxElevation() - this.globe.getMinElevation());
                float hue = (float)Math.atan(Math.pow(1.0 - ratio, 2.0));
                image.setRGB(x, y, Color.HSBtoRGB(hue, 1f, 1f));
            }
        }
        return image;
    }

    @Override
    public String toString()
    {
        return "Heightmap Layer";
    }
}