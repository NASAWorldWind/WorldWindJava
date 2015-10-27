/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.util.*;

import javax.swing.filechooser.*;
import java.io.File;

/**
 * Provides a File filter that identifies installable data sets.
 *
 * @author tag
 * @version $Id: FileSetFilter.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileSetFilter extends FileFilter implements java.io.FileFilter
{
    protected static final String[] SUFFIXES_TO_IGNORE = new String[]{"blw", "prj", "stx"};

    @Override
    public String getDescription()
    {
        return "Imagery and Elevations";
    }

    @Override
    public boolean accept(File file)
    {
        if (file == null)
            return false;

        String suffix = WWIO.getSuffix(file.getPath());
        if (suffix == null)
            return false;

        // The GDAL reader returns true for ancillary files as well as the basic raster, so filter out the ancillary.
        for (String s : SUFFIXES_TO_IGNORE)
        {
            if (suffix.endsWith(s))
                return false;
        }

        return this.isDataRaster(file, null);
    }

    public boolean isDataRaster(Object source, AVList params)
    {
        // This  method was taken from DataStoreUtils and modified with additional tests for determining whether the
        // source is a raster. In particular, that the raster has a Sector associated with it. This prevents simple
        // image files with no associated geo-location information from slipping through.

        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        DataRasterReaderFactory readerFactory;
        try
        {
            readerFactory = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
                AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
        }
        catch (Exception e)
        {
            readerFactory = new BasicDataRasterReaderFactory();
        }

        params = (null == params) ? new AVListImpl() : params;
        DataRasterReader reader = readerFactory.findReaderFor(source, params);
        if (reader == null)
            return false;

        try
        {
            reader.readMetadata(source, params);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileReading", e.getMessage());
            Logging.logger().finest(message);
        }

        if (!(AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT))
            || AVKey.ELEVATION.equals(params.getStringValue(AVKey.PIXEL_FORMAT))))
            return false;

        // Verify that it's a fully defined raster.
        if (params.getValue(AVKey.SECTOR) == null)
            return false;

        return true;
    }
}
