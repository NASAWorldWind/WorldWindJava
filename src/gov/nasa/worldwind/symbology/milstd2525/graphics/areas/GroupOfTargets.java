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

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Series or Group of Targets graphic (2.X.4.3.1.3).
 *
 * @author pabercrombie
 * @version $Id: GroupOfTargets.java 1171 2013-02-11 21:45:02Z dcollins $
 */
// TODO: We might want to draw a white background behind the label to make it easier to read against the polygon line.
public class GroupOfTargets extends BasicArea
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_ARATGT_SGTGT);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public GroupOfTargets(String sidc)
    {
        super(sidc);
    }

    /**
     * Compute the position for the area's main label. This position indicates the position of the first line of the
     * label. If there are more lines, they will be arranged South of the first line.
     *
     * @param dc Current draw context.
     *
     * @return Position for the graphic's main label.
     */
    @Override
    protected Position determineMainLabelPosition(DrawContext dc)
    {
        Iterable<? extends LatLon> locations = this.polygon.getLocations();
        if (locations == null)
            return null;

        Iterator<? extends LatLon> iterator = locations.iterator();

        LatLon locA = iterator.next();
        LatLon northMost = locA;

        // Find the North-most segment in the polygon. The template in MIL-STD-2525C shows the label at the "top"
        // of the polygon. We will interpret this as the Northern edge of the polygon.
        while (iterator.hasNext())
        {
            LatLon locB = locA;
            locA = iterator.next();

            LatLon mid = LatLon.interpolateGreatCircle(0.5, locA, locB);

            // Determine if the midpoint of the segment is farther North our North-most point
            if (mid.latitude.compareTo(northMost.latitude) > 0)
            {
                northMost = mid;
            }
        }

        // Place the label at the midpoint of the North-most segment.
        return new Position(northMost, 0);
    }
}
