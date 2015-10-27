/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Aviation offensive graphic (hierarchy 2.X.2.5.2.1.1, SIDC: G*GPOLAV--****X).
 *
 * @author pabercrombie
 * @version $Id: Aviation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Aviation extends AbstractAxisArrow
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_AXSADV_AVN);
    }

    /**
     * Create a new Aviation graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public Aviation(String sidc)
    {
        this(sidc, 1);
    }

    /**
     * Create a new Aviation graphic, composed of more than one path. This constructor is for use by subclasses that
     * extend the base Aviation graphic by adding additional paths.
     *
     * @param sidc     Symbol code the identifies the graphic.
     * @param numPaths Number of paths to create.
     */
    protected Aviation(String sidc, int numPaths)
    {
        super(sidc, numPaths);
    }

    /** {@inheritDoc} */
    @Override
    protected double createArrowHeadPositions(List<Position> leftPositions, List<Position> rightPositions,
        List<Position> arrowHeadPositions, Globe globe)
    {
        double halfWidth = super.createArrowHeadPositions(leftPositions, rightPositions, arrowHeadPositions, globe);

        // Aviation graphic is the same as the base graphic, except that the left and right lines cross between
        // points 1 and 2. Swap the control points in the left and right lists to achieve this effect.
        if (rightPositions.size() > 0 && leftPositions.size() > 0)
        {
            Position temp = leftPositions.get(0);

            leftPositions.set(0, rightPositions.get(0));
            rightPositions.set(0, temp);
        }

        // Arrow head points need to be in reverse order to match the reversed first line positions.
        Collections.reverse(arrowHeadPositions);

        return halfWidth;
    }
}
