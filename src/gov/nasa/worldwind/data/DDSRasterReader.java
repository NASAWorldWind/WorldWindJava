/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.dds.DDSDecompressor;
import gov.nasa.worldwind.formats.dds.DDSHeader;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWUtil;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: DDSRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class DDSRasterReader extends AbstractDataRasterReader
{
    protected static final String[] ddsMimeTypes = new String[]{"image/dds"};
    protected static final String[] ddsSuffixes = new String[]{"dds"};

    public DDSRasterReader()
    {
        super(ddsMimeTypes, ddsSuffixes);
    }

    @Override
    protected boolean doCanRead(Object source, AVList params)
    {
        try
        {
            DDSHeader header = DDSHeader.readFrom(source);
            if (null != header && header.getWidth() > 0 && header.getHeight() > 0)
            {
                if (null != params && !params.hasKey(AVKey.PIXEL_FORMAT))
                {
                    params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                }

                return true;
            }
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            message = (null == message && null != e.getCause()) ? e.getCause().getMessage() : message;
            Logging.logger().log(Level.FINEST, message, e);
        }
        return false;
    }

    @Override
    protected DataRaster[] doRead(Object source, AVList params) throws IOException
    {
        if (null == params || !params.hasKey(AVKey.SECTOR))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        DataRaster raster = null;

        try
        {
            DDSDecompressor decompressor = new DDSDecompressor();
            raster = decompressor.decompress(source, params);
            if (null != raster)
            {
                raster.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
            }
        }
        catch (WWRuntimeException wwe)
        {
            throw new IOException(wwe.getMessage());
        }
        catch (Throwable t)
        {
            String message = t.getMessage();
            message = (WWUtil.isEmpty(message) && null != t.getCause()) ? t.getCause().getMessage() : message;
            Logging.logger().log(Level.FINEST, message, t);
            throw new IOException(message);
        }

        return (null != raster) ? new DataRaster[]{raster} : null;
    }

    @Override
    protected void doReadMetadata(Object source, AVList params) throws IOException
    {
        try
        {
            DDSHeader header = DDSHeader.readFrom(source);
            if (null != header && null != params)
            {
                params.setValue(AVKey.WIDTH, header.getWidth());
                params.setValue(AVKey.HEIGHT, header.getHeight());
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
            }
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            message = (WWUtil.isEmpty(message) && null != e.getCause()) ? e.getCause().getMessage() : message;
            Logging.logger().log(Level.FINEST, message, e);
            throw new IOException(message);
        }
    }
}
