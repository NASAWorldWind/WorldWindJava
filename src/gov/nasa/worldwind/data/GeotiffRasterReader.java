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
