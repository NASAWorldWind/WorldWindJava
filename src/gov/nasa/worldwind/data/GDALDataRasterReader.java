/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.gdal.GDALUtils;

import java.io.*;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALDataRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class GDALDataRasterReader extends AbstractDataRasterReader
{
    // Extract list of mime types supported by GDAL
    protected static final String[] mimeTypes = new String[] {
        "image/jp2", "image/jpeg2000", "image/jpeg2000-image", "image/x-jpeg2000-image",
        "image/x-mrsid-image",
        "image/jpeg", "image/png", "image/bmp", "image/tif"
    };

    // TODO Extract list of extensions supported by GDAL
    protected static final String[] suffixes = new String[] {
        "jp2", "sid", "ntf", "nitf",
        "JP2", "SID", "NTF", "NITF",

        "jpg", "jpe", "jpeg",   /* "image/jpeg" */
        "png",                  /* "image/png" */
        "bmp",                  /* "image/bmp" */
        "TIF", "TIFF", "GTIF", "GTIFF", "tif", "tiff", "gtif", "gtiff",     /* "image/tif" */

        // Elevations

        // DTED
        "dt0", "dt1", "dt2",
        "asc", "adf", "dem"
    };

    public GDALDataRasterReader()
    {
        super("GDAL-based Data Raster Reader", mimeTypes, suffixes);
    }

    @Override
    public boolean canRead(Object source, AVList params)
    {
        // RPF imagery cannot be identified by a small set of suffixes or mime types, so we override the standard
        // suffix comparison behavior here.
        return this.doCanRead(source, params);
    }

    @Override
    protected boolean doCanRead(Object source, AVList params)
    {
        if (WWUtil.isEmpty(source))
        {
            return false;
        }

        if (null == params)
        {
            File file = WWIO.getFileForLocalAddress(source);
            if (null == file)
            {
                return false;
            }

            return GDALUtils.canOpen(file);
        }

        boolean canOpen = false;
        GDALDataRaster raster = null;
        try
        {
            raster = new GDALDataRaster(source, true); // read data raster quietly
            params.setValues(raster.getMetadata());
            canOpen = true;
        }
        catch (Throwable t)
        {
            // we purposely ignore any exception here, this should be a very quiet mode
            canOpen = false;
        }
        finally
        {
            if (null != raster)
            {
                raster.dispose();
                raster = null;
            }
        }

        return canOpen;
    }

    @Override
    protected DataRaster[] doRead(Object source, AVList params) throws IOException
    {
        GDALDataRaster raster = this.readDataRaster(source, false);
        if (null != raster && null != params)
        {
            params.setValues(raster.getMetadata());
            WWUtil.copyValues(params, raster, new String[] {AVKey.SECTOR}, false);
        }

        return (null == raster) ? null : new DataRaster[] {raster};
    }

    @Override
    protected void doReadMetadata(Object source, AVList params) throws IOException
    {
        GDALDataRaster raster = this.readDataRaster(source, true);
        if (null != raster && null != params)
        {
            params.setValues(raster.getMetadata());
            WWUtil.copyValues(params, raster, new String[] {AVKey.SECTOR}, false);
            raster.dispose();
        }
    }

    protected GDALDataRaster readDataRaster(Object source, boolean quickReadingMode) throws IOException
    {
        if (null == source)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            GDALDataRaster raster = new GDALDataRaster(source, quickReadingMode);
            if (null == raster)
            {
                String message = Logging.getMessage("generic.CannotOpenFile", GDALUtils.getErrorMessage());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            return raster;
        }
        catch (WWRuntimeException wwre)
        {
            throw wwre;
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("generic.CannotOpenFile", GDALUtils.getErrorMessage());
            Logging.logger().log(Level.SEVERE, message, t);
            throw new WWRuntimeException(t);
        }
    }
}
