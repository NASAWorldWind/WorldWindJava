/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.util.Logging;

import java.io.*;

/**
 * @author Lado Garakanidze
 * @version $Id: GeotiffRasterWriter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeotiffRasterWriter extends AbstractDataRasterWriter
{
    protected static final String[] geotiffMimeTypes = {"image/tiff", "image/geotiff"};
    protected static final String[] geotiffSuffixes = {"tif", "tiff", "gtif"};

    public GeotiffRasterWriter()
    {
        super(geotiffMimeTypes, geotiffSuffixes);
    }

    protected boolean doCanWrite(DataRaster raster, String formatSuffix, File file)
    {
        return (raster != null) && (raster instanceof BufferedImageRaster || raster instanceof BufferWrapperRaster);
    }

    protected void doWrite(DataRaster raster, String formatSuffix, File file) throws IOException
    {
        if (null == file)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (null == raster)
        {
            String message = Logging.getMessage("nullValue.RasterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GeotiffWriter writer = null;

        try
        {
            writer = new GeotiffWriter(file);
            writer.write(raster);
        }
        finally
        {
            if (null != writer)
                writer.close();
        }
    }
}
