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

package gov.nasa.worldwind.globes;

/**
 * An interface for controlling aspects of a 2D {@link Globe}.
 *
 * @author tag
 * @version $Id: Globe2D.java 2158 2014-07-19 00:00:57Z pabercrombie $
 */
public interface Globe2D
{
    /**
     * Specifies whether to treat the associated projection as contiguous with itself. If true, the scene controller
     * will make the implementing globe appear to scroll continuously horizontally. Calling this method overrides the
     * associated projection's value for this field.
     *
     * @param continuous <code>true</code> if it makes sense to treat the associated projection as continuous, otherwise
     *                   <code>false</code>.
     *
     * @see gov.nasa.worldwind.globes.GeographicProjection#isContinuous()
     */
    void setContinuous(boolean continuous);

    /**
     * Indicates whether it makes sense to treat the associated projection as contiguous with itself. If true, the scene
     * controller will make the implementing globe appear to scroll continuously horizontally.
     *
     * @return <code>true</code> if it makes sense to treat the associated projection as continuous, otherwise
     *         <code>false</code>.
     */
    boolean isContinuous();

    int getOffset();

    /**
     * Indicates an offset to apply to Cartesian points computed by this globe. The offset is in units of globe widths,
     * e.g., an offset of one indicates a Cartesian offset of the globe's width in meters.
     *
     * @param offset The offset to apply, in units of globe widths.
     */
    void setOffset(int offset);

    /**
     * Specifies the geographic projection for this globe. The geographic projection converts geographic positions to
     * Cartesian coordinates and back. Implementations of this interface define their default projection.
     *
     * @param projection The projection to apply to this globe.
     *
     * @throws IllegalArgumentException if the projection is null.
     * @see GeographicProjection
     */
    void setProjection(GeographicProjection projection);

    /**
     * Returns the geographic projection for this globe.
     *
     * @return The geographic projection for this globe.
     */
    GeographicProjection getProjection();
}
