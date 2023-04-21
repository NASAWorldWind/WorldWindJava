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