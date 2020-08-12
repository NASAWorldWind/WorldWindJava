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
package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.render.DrawContext;

/**
 * @author tag
 * @version $Id: Tessellator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Tessellator extends WWObject
{
    /**
     * Tessellate a globe for the currently visible region.
     *
     * @param dc the current draw context.
     *
     * @return the tessellation, or null if the tessellation failed or the draw context identifies no visible region.
     *
     * @throws IllegalStateException if the globe has no tessellator and a default tessellator cannot be created.
     */
    SectorGeometryList tessellate(DrawContext dc);

    /**
     * Indicates whether the tessellator creates "skirts" around the tiles in order to hide gaps between adjacent tiles
     * with differing tessellations.
     *
     * @return true if skirts are created, otherwise false.
     */
    boolean isMakeTileSkirts();

    /**
     * Specifies whether the tessellator creates "skirts" around the tiles in order to hide gaps between adjacent tiles
     * with differing tessellations.
     *
     * @param makeTileSkirts true if skirts are created, otherwise false.
     */
    void setMakeTileSkirts(boolean makeTileSkirts);

    /**
     * Indicates the maximum amount of time that may elapse between re-tessellation. Re-tessellation is performed to
     * synchronize the terrain's resolution into with the current viewing state and availability of elevations.
     *
     * @return the update frequency, in milliseconds.
     */
    long getUpdateFrequency();

    /**
     * Indicates the maximum amount of time that may elapse between re-tessellation. Re-tessellation is performed to
     * synchronize the terrain's resolution into with the current viewing state and availability of elevations.
     *
     * @param updateFrequency the update frequency, in milliseconds.
     */
    void setUpdateFrequency(long updateFrequency);
}
