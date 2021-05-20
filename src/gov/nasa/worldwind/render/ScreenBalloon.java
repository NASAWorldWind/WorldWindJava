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

import java.awt.*;

/**
 * A {@link Balloon} attached to a location on the screen.
 *
 * @author pabercrombie
 * @version $Id: ScreenBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface ScreenBalloon extends Balloon
{
    /**
     * Get the position of the balloon on the screen.
     *
     * @return The screen location of the balloon.
     */
    Point getScreenLocation();

    /**
     * Set the screen position of the balloon. This point is interpreted in a coordinate system with the origin at the
     * upper left corner of the screen.
     *
     * @param point Point in screen coordinates, with origin at upper left corner.
     */
    void setScreenLocation(Point point);
}
