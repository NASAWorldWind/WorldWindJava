/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.tiff.GeotiffReader;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: GeotiffRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeotiffRasterReader extends AbstractDataRasterReader
{
    private static final String[] geotiffMimeTypes = {"image/tiff", "image/geotiff"};
    private static final String[] geotiffSuffixes = {"tif", "tiff", "gtif", "tif.zip", "tiff.zip", "tif.gz", "tiff.gz"};

    public GeotiffRasterReader()
    {
        super(geotiffMimeTypes, geotiffSuffixes);
    }

    protected boolean doCanRead(Object source, AVList params)
    {
        String path = WWIO.getSourcePath(source);
        if (path == null)
        {
            return false;
        }

        GeotiffReader reader = null;
        try
        {
            reader = new GeotiffReader(path);
            boolean isGeoTiff = reader.isGeotiff(0);
            if (!isGeoTiff)
            {
                isGeoTiff = WorldFile.hasWorldFiles(source);
            }
            return isGeoTiff;
        }
        catch (Exception e)
        {
            // Intentionally ignoring exceptions.
            return false;
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    protected DataRaster[] doRead(Object source, AVList params) throws java.io.IOException
    {
        String path = WWIO.getSourcePath(source);
        if (path == null)
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        AVList metadata = new AVListImpl();
        if (null != params)
            metadata.setValues(params);

        GeotiffReader reader = null;
        DataRaster[] rasters = null;
        try
        {
            this.readMetadata(source, metadata);

            reader = new GeotiffReader(path);
            reader.copyMetadataTo(metadata);

            rasters = reader.readDataRaster();

            if (null != rasters)
            {
                String[] keysToCopy = new String[] {AVKey.SECTOR};
                for (DataRaster raster : rasters)
                {
                    WWUtil.copyValues(metadata, raster, keysToCopy, false);
                }
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
        return rasters;
    }

    protected void doReadMetadata(Object source, AVList params) throws java.io.IOException
    {
        String path = WWIO.getSourcePath(source);
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        GeotiffReader reader = null;
        try
        {
            reader = new GeotiffReader(path);
            reader.copyMetadataTo(params);

            boolean isGeoTiff = reader.isGeotiff(0);
            if (!isGeoTiff && params.hasKey(AVKey.WIDTH) && params.hasKey(AVKey.HEIGHT))
            {
                int[] size = new int[2];

                size[0] = (Integer) params.getValue(AVKey.WIDTH);
                size[1] = (Integer) params.getValue(AVKey.HEIGHT);

                params.setValue(WorldFile.WORLD_FILE_IMAGE_SIZE, size);

                WorldFile.readWorldFiles(source, params);

                Object o = params.getValue(AVKey.SECTOR);
                if (o == null || !(o instanceof Sector))
                {
                    ImageUtil.calcBoundingBoxForUTM(params);
                }
            }
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }
}
