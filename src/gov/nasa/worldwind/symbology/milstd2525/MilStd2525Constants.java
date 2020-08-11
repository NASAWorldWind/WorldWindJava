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

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.render.Material;

import java.awt.*;

/**
 * Defines constants used by the MIL-STD-2525 symbology classes.
 *
 * @author dcollins
 * @version $Id: MilStd2525Constants.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface MilStd2525Constants
{
    /**
     * The default location that MIL-STD-2525 tactical symbols and tactical point graphics retrieve their icons from:
     * https://worldwind.arc.nasa.gov/milstd2525c/rev1/
     */
    String DEFAULT_ICON_RETRIEVER_PATH = "https://worldwind.arc.nasa.gov/milstd2525c/rev1/";

    // Color RGB values from MIL-STD-2525C Table XIII, pg. 44.
    /** Default material used to color tactical graphics that represent friendly entities. */
    Material MATERIAL_FRIEND = Material.BLACK;
    /** Default material used to color tactical graphics that represent hostile entities. */
    Material MATERIAL_HOSTILE = new Material(new Color(255, 48, 49));
    /** Default material used to color tactical graphics that represent neutral entities. */
    Material MATERIAL_NEUTRAL = new Material(new Color(0, 226, 0));
    /** Default material used to color tactical graphics that represent unknown entities. */
    Material MATERIAL_UNKNOWN = new Material(new Color(255, 255, 0));
    /** Default material used to color tactical graphics that represent obstacles. */
    Material MATERIAL_OBSTACLE = new Material(new Color(0, 226, 0));
}
