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
 * Implementation of the Position Area for Artillery, Circular graphic (2.X.4.3.2.6.2).
 *
 * @author pabercrombie
 * @version $Id: CircularPositionArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CircularPositionArea extends AbstractCircularGraphic
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_C2ARS_PAA_CIRCLR);
    }

    /**
     * Create a new circular area.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public CircularPositionArea(String sidc)
    {
        super(sidc);
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        // This graphic has labels at the top, bottom, left, and right of the circle.
        this.addLabel("PAA");
        this.addLabel("PAA");
        this.addLabel("PAA");
        this.addLabel("PAA");
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        Position center = new Position(this.circle.getCenter(), 0);
        Angle radius = Angle.fromRadians(this.circle.getRadius() / dc.getGlobe().getRadius());

        Angle[] cardinalDirections = new Angle[] {
            Angle.NEG90, // Due West
            Angle.POS90, // Due East
            Angle.ZERO, // Due North
            Angle.POS180 // Due South
        };

        int i = 0;
        for (Angle dir : cardinalDirections)
        {
            LatLon loc = LatLon.greatCircleEndPosition(center, dir, radius);
            this.labels.get(i).setPosition(new Position(loc, 0));
            i += 1;
        }
    }
}