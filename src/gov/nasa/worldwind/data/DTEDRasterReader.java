/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.dted.DTED;
import gov.nasa.worldwind.util.*;

import java.io.*;

/**
 * @author Lado Garakanidze
 * @version $Id: DTEDRasterReader.java 3037 2015-04-17 23:08:47Z tgaskins $
 */

public class DTEDRasterReader extends AbstractDataRasterReader
{
    protected static final String[] dtedMimeTypes = new String[] {
        "application/dted",
        "application/dt0", "application/dted-0",
        "application/dt1", "application/dted-1",
        "application/dt2", "application/dted-2",
    };

    protected static final String[] dtedSuffixes = new String[]
        {"dt0", "dt1", "dt2"};

    public DTEDRasterReader()
    {
        super(dtedMimeTypes, dtedSuffixes);
    }

    @Override
    protected boolean doCanRead(Object source, AVList params)
    {
        File file = this.getFile(source);
        if (null == file)
        {
            return false;
        }

        // Assume that a proper suffix reliably identifies a DTED file. Otherwise the file will have to be loaded
        // to determine that, and there are often tens of thousands of DTED files, which causes raster server start-up
        // times to be excessive.
        if (this.canReadSuffix(source))
        {
            if (null != params)
            {
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION); // we know that DTED is elevation data
            }

            return true;
        }

        boolean canRead = false;
        try
        {
            AVList metadata = DTED.readMetadata(file);
            if (null != metadata)
            {
                if (null != params)
                {
                    params.setValues(metadata);
                }

                canRead = AVKey.ELEVATION.equals(metadata.getValue(AVKey.PIXEL_FORMAT));
            }
        }
        catch (Throwable t)
        {
            Logging.logger().finest(t.getMessage());
            canRead = false;
        }

        return canRead;
    }

    @Override
    protected DataRaster[] doRead(Object source, AVList params) throws IOException
    {
        File file = this.getFile(source);
        if (null == file)
        {
            String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource", source);
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        // This may be the first time the file has been opened, so pass the metadata list to the read method
        // in order to update that list with the file's metadata.
        DataRaster raster = DTED.read(file, params);
        if (raster instanceof ByteBufferRaster)
            ElevationsUtil.rectify((ByteBufferRaster) raster);

        return new DataRaster[] {raster};
    }

    @Override
    protected void doReadMetadata(Object source, AVList params) throws IOException
    {
        File file = this.getFile(source);
        if (null == file)
        {
            String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource", source);
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        AVList metadata = DTED.readMetadata(file);
        if (null != metadata && null != params)
        {
            params.setValues(metadata);
        }
    }

    protected File getFile(Object source)
    {
        if (null == source)
        {
            return null;
        }
        else if (source instanceof java.io.File)
        {
            return (File) source;
        }
        else if (source instanceof java.net.URL)
        {
            return WWIO.convertURLToFile((java.net.URL) source);
        }
        else
        {
            return null;
        }
    }

    protected String validateMetadata(Object source, AVList params)
    {
        // Don't validate anything so we can avoid reading the metadata at start-up. Assume that the
        // sector will come from the config file and that the pixel type is specified in doCanRead above.
        return null;
    }
}
