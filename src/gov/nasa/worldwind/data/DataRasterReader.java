/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;

// DataRasterReader is the interface class for all data raster readers implementations
/**
 * @author dcollins
 * @version $Id: DataRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface DataRasterReader extends AVList
{
    String getDescription(); // TODO: remove

    String[] getSuffixes(); // TODO: remove

    /**
     * Indicates whether this reader can read a specified data source.
     * The source may be one of the following:
     * <ul>
     *     <li>{@link java.io.File}</li>
     *     <li>{@link String}</li>
     *     <li>{@link java.io.InputStream}</li>
     *     <li>{@link java.net.URL}</li>
     * </ul>
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers.
     *
     * @return true if this reader can read the data source, otherwise false.
     */
    boolean canRead(Object source, AVList params);

    /**
     * Reads and returns the DataRaster instances from a data source.
     * The source may be one of the following:
     * The source may be one of the following:
     * <ul>
     *     <li>{@link java.io.File}</li>
     *     <li>{@link String}</li>
     *     <li>{@link java.io.InputStream}</li>
     *     <li>{@link java.net.URL}</li>
     * </ul>
     *
     * @param source the source to read.
     * @param params parameters required by certain reader implementations. May be null for most readers. If non-null,
     *               the metadata is added to this list, and the list reference is the return value of this method.
     *
     * @return the list of metadata read from the data source. The list is empty if the data source has no metadata.
     *
     * @throws java.io.IOException if an IO error occurs.
     */
    DataRaster[] read(Object source, AVList params) throws java.io.IOException;

    /**
     * Reads and returns the metadata from a data source.
     * The source may be one of the following:
     * <ul>
     *     <li>{@link java.io.File}</li>
     *     <li>{@link String}</li>
     *     <li>{@link java.io.InputStream}</li>
     *     <li>{@link java.net.URL}</li>
     * </ul>
     *
     * TODO: Why would the caller specify parameters to this method?
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers. If non-null,
     *               the metadata is added to this list, and the list reference is the return value of this method.
     *
     * @return the list of metadata read from the data source. The list is empty if the data source has no metadata.
     *
     * @throws java.io.IOException if an IO error occurs.
     */
    AVList readMetadata(Object source, AVList params) throws java.io.IOException;

    /**
     * Indicates whether a data source is imagery.
     * TODO: Identify when parameters must be passed.
     * The source may be one of the following:
     * <ul>
     *     <li>{@link java.io.File}</li>
     *     <li>{@link String}</li>
     *     <li>{@link java.io.InputStream}</li>
     *     <li>{@link java.net.URL}</li>
     * </ul>
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers.
     *
     * @return true if the source is imagery, otherwise false.
     */
    boolean isImageryRaster(Object source, AVList params);

    /**
     * Indicates whether a data source is elevation data.
     * TODO: Identify when parameters must be passed.
     *
     * The source may be one of the following:
     * <ul>
     *     <li>{@link java.io.File}</li>
     *     <li>{@link String}</li> 
     *     <li>{@link java.io.InputStream}</li>
     *     <li>{@link java.net.URL}</li>
     * </ul>
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers.
     * TODO: Identify when parameters must be passed.
     *
     * @return true if the source is elevation data, otherwise false.
     */
    boolean isElevationsRaster(Object source, AVList params);
}