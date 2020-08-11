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

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Position;

/**
 * An interface provided by objects that can be moved geographically. Typically, implementing objects move the entire
 * object as a whole in response to the methods in this interface. See the documentation for each implementing class to
 * determine whether the class deviates from this.
 *
 * @author tag
 * @version $Id: Movable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Movable
{
    /**
     * A position associated with the object that indicates its aggregate geographic position. The chosen position
     * varies among implementers of this interface. For objects defined by a list of positions, the reference position
     * is typically the first position in the list. For symmetric objects the reference position is often the center of
     * the object. In many cases the object's reference position may be explicitly specified by the application.
     *
     * @return the object's reference position, or null if no reference position is available.
     */
    Position getReferencePosition();

    /**
     * Shift the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
     * North.
     *
     * @param position the latitude and longitude to add to the shape's reference position.
     */
    void move(Position position);

    /**
     * Move the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
     * North.
     *
     * @param position the new position of the shape's reference position.
     */
    void moveTo(Position position);
}
