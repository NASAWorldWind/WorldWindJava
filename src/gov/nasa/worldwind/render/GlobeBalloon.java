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

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

/**
 * A {@link Balloon} attached to a position on the globe.
 *
 * @author pabercrombie
 * @version $Id: GlobeBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GlobeBalloon extends Balloon
{
    /**
     * Get the position of the balloon.
     *
     * @return The position of the balloon.
     */
    Position getPosition();

    /**
     * Set the balloon to a position on the globe.
     *
     * @param position New position for the balloon.
     */
    void setPosition(Position position);

    /**
     * Returns the balloon's altitude mode. See {@link #setAltitudeMode(int)} for a description of the modes.
     *
     * @return the balloon's altitude mode.
     */
    public int getAltitudeMode();

    /**
     * Specifies the balloon's altitude mode. Recognized modes are: <ul> <li><b>@link WorldWind#CLAMP_TO_GROUND}</b> --
     * the balloon is placed on the terrain at the latitude and longitude of its position.</li> <li><b>@link
     * WorldWind#RELATIVE_TO_GROUND}</b> -- the balloon is placed above the terrain at the latitude and longitude of its
     * position and the distance specified by its elevation.</li> <li><b>{@link gov.nasa.worldwind.WorldWind#ABSOLUTE}</b>
     * -- the balloon is placed at its specified position. </ul>
     *
     * @param altitudeMode the altitude mode
     */
    public void setAltitudeMode(int altitudeMode);
}
