/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

import java.io.File;

/**
 * @author dcollins
 * @version $Id: BILRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BILRasterReader extends AbstractDataRasterReader
{
    private static final String[] bilMimeTypes = new String[]
        {"image/bil", "application/bil", "application/bil16", "application/bil32"};

    private static final String[] bilSuffixes = new String[]
        {"bil", "bil16", "bil32", "bil.gz", "bil16.gz", "bil32.gz"};

    private boolean mapLargeFiles = false;
    private long largeFileThreshold = 16777216L; // 16 megabytes

    public BILRasterReader()
    {
        super(bilMimeTypes, bilSuffixes);
    }

    public boolean isMapLargeFiles()
    {
        return this.mapLargeFiles;
    }

    public void setMapLargeFiles(boolean mapLargeFiles)
    {
        this.mapLargeFiles = mapLargeFiles;
    }

    public long getLargeFileThreshold()
    {
        return this.largeFileThreshold;
    }

    public void setLargeFileThreshold(long largeFileThreshold)
    {
        if (largeFileThreshold < 0L)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "largeFileThreshold < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.largeFileThreshold = largeFileThreshold;
    }

    protected boolean doCanRead(Object source, AVList params)
    {
        if (!(source instanceof java.io.File) && !(source instanceof java.net.URL))
        {
            return false;
        }

        // If the data source doesn't already have all the necessary metadata, then we determine whether or not
        // the missing metadata can be read.
        String error = this.validateMetadata(source, params);
        if (!WWUtil.isEmpty(error))
        {
            if (!WorldFile.hasWorldFiles(source))
            {
                Logging.logger().fine(error);
                return false;
            }
        }

        if (null != params)
        {
            if (!params.hasKey(AVKey.PIXEL_FORMAT))
            {
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
            }
        }

        return true;
    }

    protected DataRaster[] doRead(Object source, AVList params) throws java.io.IOException
    {
        java.nio.ByteBuffer byteBuffer = this.readElevations(source);

        // If the parameter list is null, or doesn't already have all the necessary metadata, then we copy the parameter
        // list and attempt to populate the copy with any missing metadata.        
        if (this.validateMetadata(source, params) != null)
        {
            // Copy the parameter list to insulate changes from the caller.
            params = (params != null) ? params.copy() : new AVListImpl();
            params.setValue(AVKey.FILE_SIZE, byteBuffer.capacity());
            WorldFile.readWorldFiles(source, params);
        }

        int width = (Integer) params.getValue(AVKey.WIDTH);
        int height = (Integer) params.getValue(AVKey.HEIGHT);
        Sector sector = (Sector) params.getValue(AVKey.SECTOR);

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
        {
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
        }

        ByteBufferRaster raster = new ByteBufferRaster(width, height, sector, byteBuffer, params);
        ElevationsUtil.rectify(raster);
        return new DataRaster[] { raster };
    }

    protected void doReadMetadata(Object source, AVList params) throws java.io.IOException
    {
        if (this.validateMetadata(source, params) != null)
        {
            WorldFile.readWorldFiles(source, params);
        }
    }

    protected String validateMetadata(Object source, AVList params)
    {
        StringBuilder sb = new StringBuilder();

        String message = super.validateMetadata(source, params);
        if (message != null)
        {
            sb.append(message);
        }

        Object o = (params != null) ? params.getValue(AVKey.BYTE_ORDER) : null;
        if (o == null || !(o instanceof String))
        {
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoByteOrderSpecified", source));
        }

        o = (params != null) ? params.getValue(AVKey.PIXEL_FORMAT) : null;
        if (o == null)
        {
            sb.append(sb.length() > 0 ? ", " : "").append(
                Logging.getMessage("WorldFile.NoPixelFormatSpecified", source));
        }
        else if (!AVKey.ELEVATION.equals(o))
        {
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.InvalidPixelFormat", source));
        }

        o = (params != null) ? params.getValue(AVKey.DATA_TYPE) : null;
        if (o == null)
        {
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoDataTypeSpecified", source));
        }

        if (sb.length() == 0)
        {
            return null;
        }

        return sb.toString();
    }

    private java.nio.ByteBuffer readElevations(Object source) throws java.io.IOException
    {
        if (!(source instanceof java.io.File) && !(source instanceof java.net.URL))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        File file = (source instanceof java.io.File) ? (File) source : null;
        java.net.URL url = (source instanceof java.net.URL) ? (java.net.URL) source : null;

        if (null == file && "file".equalsIgnoreCase(url.getProtocol()))
        {
            file = new File(url.getFile());
        }

        if (null != file)
        {
            // handle .bil.zip, .bil16.zip, and .bil32.gz files
            if (file.getName().toLowerCase().endsWith(".zip"))
            {
                return WWIO.readZipEntryToBuffer(file, null);
            }
            // handle bil.gz, bil16.gz, and bil32.gz files
            else if (file.getName().toLowerCase().endsWith(".gz"))
            {
                return WWIO.readGZipFileToBuffer(file);
            }
            else if (!this.isMapLargeFiles() || (this.getLargeFileThreshold() > file.length()))
            {
                return WWIO.readFileToBuffer(file);
            }
            else
            {
                return WWIO.mapFile(file);
            }
        }
        else // (source instanceof java.net.URL)
        {
            return WWIO.readURLContentToBuffer(url);
        }
    }
}
