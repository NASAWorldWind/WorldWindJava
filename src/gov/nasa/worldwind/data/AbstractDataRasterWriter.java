/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: AbstractDataRasterWriter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractDataRasterWriter implements DataRasterWriter
{
    protected final String[] mimeTypes;
    protected  final String[] suffixes;

    /**
     * Constructor
     * @param mimeTypes MIME types as array of<code>Strings</code>
     * @param suffixes Suffixes (extensions) as array of<code>Strings</code>
     */
    public AbstractDataRasterWriter(String[] mimeTypes, String[] suffixes)
    {
        this.mimeTypes = this.copyAndConvertToLowerCase(mimeTypes);
        this.suffixes = this.copyAndConvertToLowerCase(suffixes);
    }

    /**
     * Default constructor
     */
    public AbstractDataRasterWriter()
    {
        this.mimeTypes = null;
        this.suffixes = null;
    }

    /** {@inheritDoc} */
    public boolean canWrite(DataRaster raster, String formatSuffix, java.io.File file)
    {
        if (formatSuffix == null)
            return false;

        formatSuffix = WWUtil.stripLeadingPeriod(formatSuffix);

        if( null != this.suffixes && this.suffixes.length > 0 )
        {
            boolean matchesAny = false;
            for (String suffix : this.suffixes)
            {
                if (suffix.equalsIgnoreCase(formatSuffix))
                {
                    matchesAny = true;
                    break;
                }
            }

            //noinspection SimplifiableIfStatement
            if (!matchesAny)
                return false;
        }

        return this.doCanWrite(raster, formatSuffix, file);
    }

    /** {@inheritDoc} */
    public void write(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException
    {
        if (raster == null)
        {
            String message = Logging.getMessage("nullValue.RasterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (formatSuffix == null)
        {
            String message = Logging.getMessage("nullValue.FormatSuffixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        formatSuffix = WWUtil.stripLeadingPeriod(formatSuffix);
        if (!this.canWrite(raster, formatSuffix, file))
        {
            String message = Logging.getMessage("DataRaster.CannotWrite", raster, formatSuffix, file);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doWrite(raster, formatSuffix, file);
    }

    protected abstract boolean doCanWrite(DataRaster raster, String formatSuffix, java.io.File file);

    protected abstract void doWrite(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException;

    //**************************************************************//
    //********************  Utilities  *****************************//
    //**************************************************************//

    /**
     * Clones string array and also converts clones to lower case
     *
     * @param array string array
     * @return cloned string array
     */
    protected String[] copyAndConvertToLowerCase(String[] array)
    {
        if( null == array )
            return null;

        String[] copy = new String[array.length];
        for (int i = 0; i < array.length; i++)
            copy[i] = array[i].toLowerCase();

        return copy;
    }
}
