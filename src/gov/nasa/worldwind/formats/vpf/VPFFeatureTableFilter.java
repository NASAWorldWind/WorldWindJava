/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

import java.io.FileFilter;

/**
 * @author dcollins
 * @version $Id: VPFFeatureTableFilter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFFeatureTableFilter implements FileFilter
{
    /** Creates a VPFFeatureTableFilter, but otherwise does nothing. */
    public VPFFeatureTableFilter()
    {
    }

    /**
     * Returns true if the specified file is a Feature Table.
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

        return VPFUtils.getFeatureTypeName(file.getName()) != null;
    }
}
