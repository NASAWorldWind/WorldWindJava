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

package gov.nasa.worldwind.formats.tiff;

import gov.nasa.worldwind.Version;

import javax.imageio.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import java.io.*;
import java.util.*;

/**
 * GeotiffImageReaderSpi is a singleton class. Multiply registering it should be harmless.
 *
 * @author brownrigg
 * @version $Id: GeotiffImageReaderSpi.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeotiffImageReaderSpi extends ImageReaderSpi
{

    public static GeotiffImageReaderSpi inst()
    {
        if (theInstance == null)
            theInstance = new GeotiffImageReaderSpi();
        return theInstance;
    }

    private GeotiffImageReaderSpi()
    {
        super(vendorName, version, names, suffixes, mimeTypes,
            readerClassname, new Class[] {ImageInputStream.class},
            null, false, null, null, null, null,
            false, null, null, null, null);
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException
    {
        if (source == null || !(source instanceof ImageInputStream))
            return false;

        ImageInputStream inp = (ImageInputStream) source;
        byte[] ifh = new byte[8];  // Tiff image-file header
        try
        {
            inp.mark();
            inp.readFully(ifh);
            inp.reset();
        }
        catch (IOException ex)
        {
            return false;
        }

        return (ifh[0] == 0x4D && ifh[1] == 0x4D && ifh[2] == 0x00 && ifh[3] == 0x2A) ||  // big-endian
            (ifh[0] == 0x49 && ifh[1] == 0x49 && ifh[2] == 0x2A && ifh[3] == 0x00);    // little-endian
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException
    {
        return new GeotiffImageReader(this);
    }

    @Override
    public String getDescription(Locale locale)
    {
        return "NASA WorldWind Geotiff Image Reader";
    }

    private static GeotiffImageReaderSpi theInstance = null;

    private static final String vendorName = Version.getVersionName();
    private static final String version = Version.getVersionNumber();
    private static final String[] names = {"tiff", "GTiff", "geotiff"};
    private static final String[] suffixes = {"tif", "tiff", "gtif"};
    private static final String[] mimeTypes = {"image/tiff", "image/geotiff"};
    private static final String readerClassname = "gov.nasa.worldwind.servers.wms.utilities.TiffImageReader";
}
