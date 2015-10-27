/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.tracks;

import gov.nasa.worldwind.util.*;

import java.io.File;

/**
 * @author dcollins
 * @version $Id: SaveTrackFilter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SaveTrackFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter
{
    private final int format;
    private final String description;
    private final String[] suffixes;

    public SaveTrackFilter(int format, String description, String[] suffixes)
    {
        this.format = format;
        this.description = description;
        this.suffixes = new String[suffixes.length];
        System.arraycopy(suffixes, 0, this.suffixes, 0, suffixes.length);
    }

    public int getFormat()
    {
        return this.format;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String[] getSuffixes()
    {
        String[] copy = new String[this.suffixes.length];
        System.arraycopy(this.suffixes, 0, copy, 0, this.suffixes.length);
        return copy;
    }

    public boolean accept(java.io.File file)
    {
        return true;
    }

    public java.io.File appendSuffix(java.io.File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String path = file.getPath();

        String lowerCasePath = path.toLowerCase();
        for (String suffix : this.suffixes)
        {
            if (lowerCasePath.endsWith(suffix))
                return file;
        }

        return new File(WWIO.replaceSuffix(path, this.suffixes[0]));
    }
}