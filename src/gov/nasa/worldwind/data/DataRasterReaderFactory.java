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

/**
 * @author Lado Garakanidze
 * @version $Id: DataRasterReaderFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface DataRasterReaderFactory
{
    /**
     * Search the list of available data raster readers for one that will read a specified data source. The
     * determination is based on both the data type and the data source reference; some readers may be able to open data
     * of the corresponding type but not as, for example, an InputStream or a URL.
     * <p>
     * The list of readers searched is determined by the DataRasterReaderFactory associated with the current {@link
     * gov.nasa.worldwind.Configuration}, as specified by the {@link gov.nasa.worldwind.avlist.AVKey#DATA_RASTER_READER_FACTORY_CLASS_NAME}.
     * If no factory is specified in the configuration, {@link gov.nasa.worldwind.data.BasicDataRasterReaderFactory} is
     * used.
     *
     * @param source the source to read. May by a {@link java.io.File}, a file path, a URL or an {@link
     *               java.io.InputStream}.
     * @param params optional metadata associated with the data source that might be useful in determining the data
     *               reader. TODO: How does the caller determine which parameters are necessary or useful?
     *
     * @return a data reader for the specified source, or null if no reader can be found.
     *
     * @throws IllegalArgumentException if the source is null.
     */
    public DataRasterReader findReaderFor(Object source, AVList params);

    /**
     * Search a specified list of data raster readers for one that will read a specified data source. The determination
     * is based on both the data type and the data source reference; some readers may be able to open data of the
     * corresponding type but not as, for example, an InputStream or a URL.
     *
     * @param source  the source to read. May by a {@link java.io.File}, a file path, a URL or an {@link
     *                java.io.InputStream}.
     * @param params  optional metadata associated with the data source that might be useful in determining the data
     *                reader.
     * @param readers the list of readers to search.
     *
     * @return a data reader for the specified source, or null if no reader can be found.
     *
     * @throws IllegalArgumentException if either the source or the reader list is null.
     */
    public DataRasterReader findReaderFor(Object source, AVList params, DataRasterReader[] readers);

    /**
     * Returns this class' list of readers.
     *
     * @return the list of readers.
     */
    DataRasterReader[] getReaders();
}
