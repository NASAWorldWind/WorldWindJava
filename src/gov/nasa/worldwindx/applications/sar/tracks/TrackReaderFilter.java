/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.tracks;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: TrackReaderFilter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TrackReaderFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter
{
    protected final TrackReader trackReader;

    public TrackReaderFilter(TrackReader trackReader)
    {
        this.trackReader = trackReader;
    }

    public final TrackReader getTrackReader()
    {
        return this.trackReader;
    }

    public String getDescription()
    {
        return this.trackReader.getDescription();
    }

    public boolean accept(java.io.File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return file.isDirectory() || this.trackReader.canRead(file);
    }
}
