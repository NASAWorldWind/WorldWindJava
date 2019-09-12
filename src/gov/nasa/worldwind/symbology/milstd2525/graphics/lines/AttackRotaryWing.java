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
 * Implementation of the Aviation offensive graphic (hierarchy 2.X.2.5.2.1.3, SIDC: G*GPOLAR--****X).
 *
 * @author pabercrombie
 * @version $Id: AttackRotaryWing.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AttackRotaryWing extends Aviation
{
    /** Index of the left vertical path in the {@code paths} array. */
    protected final static int LEFT_VERTICAL = 1;
    /** Index of the right vertical path in the {@code paths} array. */
    protected final static int RIGHT_VERTICAL = 2;
    /** Index of the rotor symbol path in the {@code paths} array. */
    protected final static int ROTOR_SYMBOL = 3;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_AXSADV_ATK);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public AttackRotaryWing(String sidc)
    {
        super(sidc, 4);
    }

    @Override
    protected void createLinePositions(List<Position> leftPositions, List<Position> rightPositions, double halfWidth,
        Globe globe)
    {
        super.createLinePositions(leftPositions, rightPositions, halfWidth, globe);

        // Rotary Wing is based on the Aviation graphic, but adds a symbol for the rotor (an upward pointing arrow),
        // and two vertical lines on either side of the cross over point (X) between points 1 and 2.
        //            |\
        //______      | \
        //     C\    / A \
        //       \  /     \
        // Pt. 2  \/X      \ Pt. 1
        //        /\       /
        //       /  \     /
        // ____B/    \ D /
        //            | /
        //            |/

        Iterator<? extends Position> iterator = this.positions.iterator();
        final Position pos1 = iterator.next();
        final Position pos2 = iterator.next();

        final Position posA = rightPositions.get(0);
        final Position posB = rightPositions.get(1);
        final Position posC = leftPositions.get(1);
        final Position posD = leftPositions.get(0);

        final Vec4 p1 = globe.computePointFromLocation(pos1);
        final Vec4 p2 = globe.computePointFromLocation(pos2);

        final Vec4 pA = globe.computePointFromLocation(posA);
        final Vec4 pB = globe.computePointFromLocation(posB);
        final Vec4 pC = globe.computePointFromLocation(posC);
        final Vec4 pD = globe.computePointFromLocation(posD);

        final Vec4 vAB = pB.subtract3(pA);
        final Vec4 vCD = pD.subtract3(pC);

        final Vec4 v21 = p1.subtract3(p2);

        // Find the cross over point, Pt. X
        final Vec4 pMidCA = pA.subtract3(pC).divide3(2).add3(pC); // Find midpoint of segment CA
        final Vec4 pMidBD = pD.subtract3(pB).divide3(2).add3(pB); // Find midpoint of segment BD

        Vec4 pX = pMidBD.subtract3(pMidCA).divide3(2).add3(pMidCA);

        // Make sure that pX falls on one of the lines
        pX = Line.fromSegment(pD, pC).nearestPointTo(pX);

        // Find points required to draw a vertical line on one side of Pt. X
        double offsetDist = vAB.getLength3() / 4;
        Vec4 start = pX.add3(vAB.normalize3().multiply3(offsetDist));
        Vec4 end = pX.add3(vCD.normalize3().multiply3(-offsetDist));

        Position posStart = globe.computePositionFromPoint(start);
        Position posEnd = globe.computePositionFromPoint(end);

        this.paths[LEFT_VERTICAL].setPositions(Arrays.asList(posStart, posEnd));

        // Find points required to draw a vertical line on the other side of Pt. X
        start = pX.add3(vAB.normalize3().multiply3(-offsetDist));
        end = pX.add3(vCD.normalize3().multiply3(offsetDist));

        posStart = globe.computePositionFromPoint(start);
        posEnd = globe.computePositionFromPoint(end);

        this.paths[RIGHT_VERTICAL].setPositions(Arrays.asList(posStart, posEnd));

        // Compute positions for the rotor symbol. Points are named according to this diagram:
        //     H
        //    /|\
        //   / | \
        // I   |  J
        //     |
        // E ----- F
        //     G

        // Compute the width of the symbol base from the width of the entire graphic
        final double halfBaseWidth = halfWidth / 4;

        Vec4 normal = globe.computeSurfaceNormalAtPoint(pX);
        Vec4 perpendicular = v21.cross3(normal).normalize3().multiply3(halfWidth);

        final Vec4 pH = pX.subtract3(perpendicular);
        final Vec4 pG = pX.add3(perpendicular);

        normal = globe.computeSurfaceNormalAtPoint(end);
        perpendicular = start.subtract3(end).cross3(normal).normalize3().multiply3(halfBaseWidth);

        final Vec4 pE = pG.add3(perpendicular);
        final Vec4 pF = pG.subtract3(perpendicular);

        // Find the point on segment HG that is halfBaseWidth away from point H
        Vec4 pMidIJ = pG.subtract3(pH).normalize3().multiply3(halfBaseWidth).add3(pH);

        final Vec4 pI = pMidIJ.add3(perpendicular);
        final Vec4 pJ = pMidIJ.subtract3(perpendicular);

        Position posE = globe.computePositionFromPoint(pE);
        Position posF = globe.computePositionFromPoint(pF);
        Position posG = globe.computePositionFromPoint(pG);
        Position posH = globe.computePositionFromPoint(pH);
        Position posI = globe.computePositionFromPoint(pI);
        Position posJ = globe.computePositionFromPoint(pJ);

        // Draw the symbol as a single path (the path doubles back itself when drawing the base of the symbol, and the
        // arrow head.
        this.paths[ROTOR_SYMBOL].setPositions(Arrays.asList(posE, posF, posG, posH, posI, posH, posJ));
    }
}
