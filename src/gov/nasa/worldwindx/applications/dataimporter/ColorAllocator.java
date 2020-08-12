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

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Allocates colors used to key data sets to the visible sectors they cover.
 *
 * @author tag
 * @version $Id: ColorAllocator.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class ColorAllocator
{
    protected static ConcurrentLinkedQueue<Color> initialColors = new ConcurrentLinkedQueue<Color>();

    static
    {
        initializeColors();
    }

    public static void initializeColors()
    {
        initialColors.clear();

        // Create some standard first-used colors. Just add to this list to define more.
        initialColors.add(Color.YELLOW);
        initialColors.add(Color.GREEN);
        initialColors.add(Color.BLUE);
        initialColors.add(Color.CYAN);
        initialColors.add(Color.MAGENTA);
        initialColors.add(Color.RED);
        initialColors.add(Color.ORANGE);
        initialColors.add(Color.PINK);
    }

    public static Color getNextColor()
    {
        // Try to use a pre-defined color.
        if (initialColors.size() > 0)
            return initialColors.poll();

        // No more pre-defined colors left, so use a random color.
        return WWUtil.makeRandomColor(null);
    }
}
