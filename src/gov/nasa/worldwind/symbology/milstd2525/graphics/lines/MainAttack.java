/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Main Attack graphic (hierarchy 2.X.2.5.2.1.4.1, SIDC: G*GPOLAGM-****X).
 *
 * @author pabercrombie
 * @version $Id: MainAttack.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MainAttack extends AbstractAxisArrow
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_AXSADV_GRD_MANATK);
    }

    public MainAttack(String sidc)
    {
        super(sidc, 2);
    }

    @Override
    protected double createArrowHeadPositions(List<Position> leftPositions, List<Position> rightPositions,
        List<Position> arrowHeadPositions, Globe globe)
    {
        double halfWidth = super.createArrowHeadPositions(leftPositions, rightPositions, arrowHeadPositions, globe);

        if (rightPositions.size() > 0 && leftPositions.size() > 0)
        {
            Position left = leftPositions.get(0);
            Position right = rightPositions.get(0);
            Position pos1 = this.positions.iterator().next();

            Vec4 pLeft = globe.computePointFromLocation(left);
            Vec4 pRight = globe.computePointFromLocation(right);
            Vec4 p1 = globe.computePointFromLocation(pos1);

            Vec4 vLeftRight = pRight.subtract3(pLeft);
            Vec4 midPoint = vLeftRight.divide3(2).add3(pLeft);

            Vec4 vMidP1 = p1.subtract3(midPoint).divide3(2);

            midPoint = midPoint.add3(vMidP1);

            Position midPos = globe.computePositionFromPoint(midPoint);

            this.paths[1].setPositions(Arrays.asList(left, midPos, right));
        }

        return halfWidth;
    }
}
