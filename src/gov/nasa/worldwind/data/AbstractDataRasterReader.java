/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * Abstract base class for most {@link DataRasterReader} implementations.
 *
 * @author dcollins
 * @version $Id: AbstractDataRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractDataRasterReader extends AVListImpl implements DataRasterReader
{
    protected abstract boolean doCanRead(Object source, AVList params);

    protected abstract DataRaster[] doRead(Object source, AVList params) throws java.io.IOException;

    protected abstract void doReadMetadata(Object source, AVList params) throws java.io.IOException;

    protected final String description;
    protected final String[] mimeTypes;
    protected final String[] suffixes;

    public AbstractDataRasterReader(String description, String[] mimeTypes, String[] suffixes)
    {
        this.description = description;
        this.mimeTypes = Arrays.copyOf(mimeTypes, mimeTypes.length);
        this.suffixes = Arrays.copyOf(suffixes, suffixes.length);

        this.setValue(AVKey.SERVICE_NAME, AVKey.SERVICE_NAME_OFFLINE);
    }

    public AbstractDataRasterReader(String[] mimeTypes, String[] suffixes)
    {
        this(descriptionFromSuffixes(suffixes), mimeTypes, suffixes);
    }

    protected AbstractDataRasterReader(String description)
    {
        this(description, new String[0], new String[0]);
    }

    /** {@inheritDoc} */
    public String getDescription()
    {
        return this.description;
    }

    public String[] getMimeTypes()
    {
        String[] copy = new String[mimeTypes.length];
        System.arraycopy(mimeTypes, 0, copy, 0, mimeTypes.length);
        return copy;
    }

    /** {@inheritDoc} */
    public String[] getSuffixes()
    {
        String[] copy = new String[suffixes.length];
        System.arraycopy(suffixes, 0, copy, 0, suffixes.length);
        return copy;
    }

    /** {@inheritDoc} */
    public boolean canRead(Object source, AVList params)
    {
        if (source == null)
            return false;

        //noinspection SimplifiableIfStatement
        if (!this.canReadSuffix(source))
            return false;

        return this.doCanRead(source, params);
    }

    protected boolean canReadSuffix(Object source)
    {
        // If the source has no path, we cannot return failure, so return that the test passed.
        String path = WWIO.getSourcePath(source);
        if (path == null)
            return true;

        // If the source has a suffix, then we return success if this reader supports the suffix.
        String pathSuffix = WWIO.getSuffix(path);
        boolean matchesAny = false;
        for (String suffix : suffixes)
        {
            if (suffix.equalsIgnoreCase(pathSuffix))
            {
                matchesAny = true;
                break;
            }
        }
        return matchesAny;
    }

    /** {@inheritDoc} */
    public DataRaster[] read(Object source, AVList params) throws java.io.IOException
    {
        if (!this.canRead(source, params))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        return this.doRead(source, params);
    }

    /** {@inheritDoc} */
    public AVList readMetadata(Object source, AVList params) throws java.io.IOException
    {
        if (!this.canRead(source, params))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        this.doReadMetadata(source, params);

        String message = this.validateMetadata(source, params);
        if (message != null)
            throw new java.io.IOException(message);

        return params;
    }

    protected String validateMetadata(Object source, AVList params)
    {
        StringBuilder sb = new StringBuilder();

        Object o = (params != null) ? params.getValue(AVKey.WIDTH) : null;
        if (o == null || !(o instanceof Integer))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSizeSpecified", source));

        o = (params != null) ? params.getValue(AVKey.HEIGHT) : null;
        if (o == null || !(o instanceof Integer))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSizeSpecified", source));

        o = (params != null) ? params.getValue(AVKey.SECTOR) : null;
        if (o == null || !(o instanceof Sector))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSectorSpecified", source));

        if (sb.length() == 0)
            return null;

        return sb.toString();
    }

    /** {@inheritDoc} */
    public boolean isImageryRaster(Object source, AVList params)
    {
        if (params != null && AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT)))
            return true;

        try
        {
            AVList metadata = this.readMetadata(source, params);
            return metadata != null && AVKey.IMAGE.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT));
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /** {@inheritDoc} */
    public boolean isElevationsRaster(Object source, AVList params)
    {
        if (params != null && AVKey.ELEVATION.equals(params.getStringValue(AVKey.PIXEL_FORMAT)))
            return true;

        try
        {
            AVList metadata = this.readMetadata(source, params);
            return metadata != null && AVKey.ELEVATION.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT));
        }
        catch (IOException e)
        {
            return false;
        }
    }

    //**************************************************************//
    //********************  Utilities  *****************************//
    //**************************************************************//

    private static String descriptionFromSuffixes(String[] suffixes)
    {
        StringBuilder sb = new StringBuilder();
        for (String suffix : suffixes)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append("*.").append(suffix.toLowerCase());
        }
        return sb.toString();
    }
}
