/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
