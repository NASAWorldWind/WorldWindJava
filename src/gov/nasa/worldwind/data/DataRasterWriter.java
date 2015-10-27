/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

/**
 * <code>DataRasterWriter</code> is a common interface for objects
 * which can write a data raster in a particular file format.
 *
 * @author dcollins
 * @version $Id: DataRasterWriter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface DataRasterWriter
{
    /**
     * Checks if a data raster could be written to a File  the given format.
     *
     * @param raster a data raster to be written to a <code>File</code> in the given format.
     * @param formatSuffix a <code>String</code> containing the format suffix
     * @param file a <code>File</code> to be written to
     * @return <code>TRUE</code>, if a data raster could be written to the <code>File</code>
     *
     */
    boolean canWrite(DataRaster raster, String formatSuffix, java.io.File file);

    /**
     * Writes an data raster to a <code>File</code> in the given format.
     * If there is already a File present, its contents are discarded.
     *
     * @param raster a data raster to be written
     * @param formatSuffix a <code>String</code> containing the format suffix
     * @param file a <code>File</code> to be written to
     * @throws java.io.IOException if any parameter is <code>null</code> or invalid
     */
    void write(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException;
}
