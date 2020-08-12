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
