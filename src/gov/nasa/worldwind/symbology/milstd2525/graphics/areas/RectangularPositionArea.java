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
 * Implementation of the Position Area for Artillery, Rectangular graphic (2.X.4.3.2.6.1).
 *
 * @author pabercrombie
 * @version $Id: RectangularPositionArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RectangularPositionArea extends AbstractRectangularGraphic
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_C2ARS_PAA_RTG);
    }

    /**
     * Create a new target.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public RectangularPositionArea(String sidc)
    {
        super(sidc);
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        // This graphic has labels at the top, bottom, left, and right of the quad.
        this.addLabel("PAA");
        this.addLabel("PAA");
        this.addLabel("PAA");
        this.addLabel("PAA");
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        Iterable<? extends LatLon> corners = this.quad.getLocations(dc.getGlobe());
        if (corners == null)
            return;

        Iterator<? extends LatLon> iterator = corners.iterator();

        // Position the labels at the midpoint of each side of the quad.
        int i = 0;
        LatLon locA = iterator.next();
        while (iterator.hasNext() && i < this.labels.size())
        {
            LatLon locB = iterator.next();

            // Find the midpoint of the two corners
            LatLon mid = LatLon.interpolateGreatCircle(0.5d, locA, locB);

            // Position the label at the midpoint.
            this.labels.get(i).setPosition(new Position(mid, 0));

            locA = locB;
            i += 1;
        }
    }
}