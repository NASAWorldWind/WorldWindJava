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
