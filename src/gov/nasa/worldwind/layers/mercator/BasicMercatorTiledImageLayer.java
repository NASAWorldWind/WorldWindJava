/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.mercator;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;

/**
 * @author Sufaev
 */
public class BasicMercatorTiledImageLayer extends BasicTiledImageLayer
{
    private static LevelSet makeLevels(String datasetName, String dataCacheName, int numLevels, int tileSize, String formatSuffix, MercatorTileUrlBuilder buider)
    {
        double delta = Angle.POS360.degrees / (1 << buider.getFirstLevelOffset());
        AVList params = new AVListImpl();
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(delta / 2), Angle.fromDegrees(delta)));
        params.setValue(AVKey.NUM_LEVELS, numLevels - buider.getFirstLevelOffset());
        params.setValue(AVKey.FORMAT_SUFFIX, formatSuffix);
        params.setValue(AVKey.TILE_WIDTH, tileSize);
        params.setValue(AVKey.TILE_HEIGHT, tileSize);
        params.setValue(AVKey.DATASET_NAME, datasetName);
        params.setValue(AVKey.DATA_CACHE_NAME, dataCacheName);
        params.setValue(AVKey.TILE_URL_BUILDER, buider);
        return new LevelSet(params);
    }

    public BasicMercatorTiledImageLayer(String datasetName, String dataCacheName, int numLevels, int tileSize, boolean overlay, String formatSuffix, MercatorTileUrlBuilder builder)
    {
        this(makeLevels(datasetName, dataCacheName, numLevels, tileSize, formatSuffix, builder));
        setUseTransparentTextures(overlay);
    }

    public BasicMercatorTiledImageLayer(LevelSet levelSet)
    {
        super(levelSet);
    }

    @Override
    protected void createTopLevelTiles()
    {
        MercatorSector sector = (MercatorSector) this.levels.getSector();

        Level level = levels.getFirstLevel();
        Angle dLat = level.getTileDelta().getLatitude();
        Angle dLon = level.getTileDelta().getLongitude();

        Angle latOrigin = this.levels.getTileOrigin().getLatitude();
        Angle lonOrigin = this.levels.getTileOrigin().getLongitude();

        // Determine the row and column offset from the common WorldWind global tiling origin.
        int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
        int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
        int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
        int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

        int nLatTiles = lastRow - firstRow + 1;
        int nLonTiles = lastCol - firstCol + 1;

        this.topLevels = new ArrayList<TextureTile>(nLatTiles * nLonTiles);

        double deltaLat = dLat.degrees / 90;
        double d1 = sector.getMinLatPercent() + deltaLat * firstRow;
        for (int row = firstRow; row <= lastRow; row++)
        {
            double d2 = d1 + deltaLat;
            Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
            for (int col = firstCol; col <= lastCol; col++)
            {
                Angle t2;
                t2 = t1.add(dLon);
                this.topLevels.add(new MercatorTextureTile(new MercatorSector(d1, d2, t1, t2), level, row, col));
                t1 = t2;
            }
            d1 = d2;
        }
    }
    
    protected MercatorTileUrlBuilder getURLBuilder()
    {
        LevelSet levelSet = getLevels();
        Level firstLevel = levelSet.getFirstLevel();
        AVList params = firstLevel.getParams();
        Object value = params.getValue(AVKey.TILE_URL_BUILDER);
        MercatorTileUrlBuilder urlBuilder = (MercatorTileUrlBuilder)value;
        return urlBuilder;
    }

    @Override
    protected boolean needToSplit(DrawContext dc, Sector sector, Level level)
    {
        double texelSize = level.getTexelSize() * dc.getGlobe().getRadius();
        double pixelSize = dc.getView().computePixelSizeAtDistance(sector.distanceTo(dc, dc.getView().getEyePoint()));
        return texelSize > pixelSize * this.getDetailFactor();
    }

    @Override
    protected DownloadPostProcessor createDownloadPostProcessor(TextureTile tile)
    {
        return new MercatorDownloadPostProcessor((MercatorTextureTile) tile, this);
    }

    private static class MercatorDownloadPostProcessor extends DownloadPostProcessor
    {

        MercatorDownloadPostProcessor(MercatorTextureTile tile, BasicMercatorTiledImageLayer layer) 
        {
            super(tile, layer);
        }

        @Override
        protected BufferedImage transformPixels()
        {
            // Make parent transformations
            BufferedImage image = super.transformPixels();

            // Read image from buffer
            if (image == null)
            {
                try
                {
                    image = ImageIO.read(new ByteArrayInputStream(this.getRetriever().getBuffer().array()));
                }
                catch (IOException ignored)
                {
                    return null;
                }
            }

            // Transform mercator tile to equirectangular projection
            if (image != null)
            {
                int type = image.getType();
                if (type == BufferedImage.TYPE_CUSTOM)
                {
                    type = BufferedImage.TYPE_INT_RGB;
                }
                else if (type == BufferedImage.TYPE_BYTE_INDEXED)
                {
                    type = BufferedImage.TYPE_INT_ARGB;
                }

                BufferedImage trans = new BufferedImage(image.getWidth(), image.getHeight(), type);
                double miny = ((MercatorSector) tile.getSector()).getMinLatPercent();
                double maxy = ((MercatorSector) tile.getSector()).getMaxLatPercent();
                for (int y = 0; y < image.getHeight(); y++)
                {
                    double sy = 1.0 - y / (double) (image.getHeight() - 1);
                    Angle lat = Angle.fromRadians(sy * tile.getSector().getDeltaLatRadians() + tile.getSector().getMinLatitude().radians);
                    double dy = 1.0 - (MercatorSector.gudermannianInverse(lat) - miny) / (maxy - miny);
                    dy = Math.max(0.0, Math.min(1.0, dy));
                    int iy = (int) (dy * (image.getHeight() - 1));
                    for (int x = 0; x < image.getWidth(); x++)
                    {
                        trans.setRGB(x, y, image.getRGB(x, iy));
                    }
                }

                return trans;
            }
            else
            {
                return null;
            }
        }
    }
}