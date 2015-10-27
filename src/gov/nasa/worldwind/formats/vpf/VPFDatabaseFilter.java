/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: VPFDatabaseFilter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFDatabaseFilter implements java.io.FileFilter
{
    /** Constructs a VPFDatabaseFilter. */
    public VPFDatabaseFilter()
    {
    }

    /**
     * Returns true if the specified file can be opened as an VPF database.
     *
     * @param file the file in question.
     *
     * @return true if the file should be accepted; false otherwise.
     *
     * @throws IllegalArgumentException if the file is null.
     */
    public boolean accept(java.io.File file)
    {
        if (file == null)
        {
            String msg = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // First check the file path, optionally returning false if the path cannot be accepted for any reason.
        if (!this.acceptFilePath(file))
            return false;

        try
        {
            return VPFDatabase.isDatabase(file.getPath());
        }
        catch (Exception e)
        {
            // Not interested in logging or reporting the exception; just return false indicating that the file is not
            // a VPF database.
        }

        return false;
    }

    protected boolean acceptFilePath(java.io.File file)
    {
        if (file == null)
        {
            String msg = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return file.getName().equalsIgnoreCase(VPFConstants.DATABASE_HEADER_TABLE);
    }
}
