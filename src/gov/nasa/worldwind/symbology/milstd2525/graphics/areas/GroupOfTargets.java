/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
