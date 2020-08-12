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

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;

// TODO: Document the conditions determining when the sub-raster methods would fail and its response when the input
// sector is not fully within the raster's sector. Also do so appropriately in the implementing classes.
// What's the response from getSubRegion if a necessary projection can't be performed?

/**
 * Represents a raster of imagery or elevations.
 *
 * @author dcollins
 * @version $Id: DataRaster.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface DataRaster extends AVList, Disposable
{
    /**
     * Returns the raster's width in raster units.
     *
     * @return the raster's width in raster units.
     */
    int getWidth(); // TODO: what's returned if the raster is invalid or the width is unknown?

    /**
     * Returns the raster's height in raster units.
     *
     * @return the raster's height in raster units.
     */
    int getHeight(); // TODO: what's returned if the raster is invalid or the width is unknown?

    /**
     * Returns the region covered be the data.
     *
     * @return a sector indicating the region covered by the raster.
     */
    Sector getSector(); // TODO: what's returned if the raster is invalid or the sector is unknown?

    /**
     * Copies this raster into a specified raster.
     *
     * @param canvas - the raster to copy into.
     */
    void drawOnTo(DataRaster canvas);

    /**
     * Returns a portion of this raster as another raster. Input values are the desired width and height of the return
     * sub-raster, and the sector to copy from this raster to the sub-raster. The returned raster conforms to EPSG:4326
     * even if this raster does not.
     *
     * @param params a list of parameters that specify the width, height and sector of the region to return. Specify
     *               these values using {@link gov.nasa.worldwind.avlist.AVKey#WIDTH}, {@link
     *               gov.nasa.worldwind.avlist.AVKey#HEIGHT} and {@link gov.nasa.worldwind.avlist.AVKey#SECTOR}.
     *
     * @return the requested raster, or null if the raster could not be created.
     *
     * @throws IllegalArgumentException if the specified parameter list is missing the width, height or sector keys or
     *                                  any of those values are invalid.
     */
    DataRaster getSubRaster(AVList params);

    /**
     * Returns a portion of this raster as another raster. The returned raster conforms to EPSG:4326 even if this raster
     * does not.
     *
     * @param width  the width to make the returned sub-raster.
     * @param height the height to make the returned sub-raster.
     * @param sector the sector to copy.
     * @param params a list of parameters that specify the width, height and sector of the region to return. Specify
     *               these values using {@link gov.nasa.worldwind.avlist.AVKey#WIDTH}, {@link
     *               gov.nasa.worldwind.avlist.AVKey#HEIGHT} and {@link gov.nasa.worldwind.avlist.AVKey#SECTOR}.
     *
     * @return the requested raster, or null if the raster could not be created.
     *
     * @throws IllegalArgumentException if the sector is null or the width, height or sector are invalid.
     */
    DataRaster getSubRaster(int width, int height, Sector sector, AVList params);
}
