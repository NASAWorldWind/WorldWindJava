/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Line of Contact graphic (2.X.2.1.2.3).
 *
 * @author pabercrombie
 * @version $Id: LineOfContact.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class LineOfContact extends ForwardLineOfOwnTroops
{
    /** Line of Contact is drawn with two paths. The super class manages the first path; this is the second. */
    protected Path path2;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_GNL_LNE_LOC);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc MIL-STD-2525C identifier code.
     */
    public LineOfContact(String sidc)
    {
        super(sidc);
        this.path2 = this.createPath();
    }

    /** {@inheritDoc} */
    @Override
    public void moveTo(Position position)
    {
        Position delta;
        Position ref1 = this.path.getReferencePosition();
        Position ref2 = this.path2.getReferencePosition();
        if (ref1 != null && ref2 != null)
            delta = ref2.subtract(ref1);
        else
            delta = Position.ZERO;

        // Move the first path
        super.moveTo(position);

        // Move the second path
        this.path2.moveTo(position.add(delta));
    }

    @Override
    protected void doRenderGraphic(DrawContext dc)
    {
        super.doRenderGraphic(dc);
        this.path2.render(dc);
    }

    @Override
    protected String getGraphicLabel()
    {
        StringBuilder sb = new StringBuilder();
        if (this.mustShowHostileIndicator())
        {
            sb.append(SymbologyConstants.HOSTILE_ENEMY);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Generate the positions required to draw the line.
     *
     * @param dc        Current draw context.
     * @param positions Positions that define the polygon boundary.
     */
    @Override
    protected void generateIntermediatePositions(DrawContext dc, Iterable<? extends Position> positions)
    {
        Globe globe = dc.getGlobe();

        boolean useDefaultWaveLength = false;
        double waveLength = this.getWaveLength();
        if (waveLength == 0)
        {
            waveLength = this.computeDefaultWavelength(positions, globe);
            useDefaultWaveLength = true;
        }

        // Generate lines that parallel the control line.
        List<Position> leftPositions = new ArrayList<Position>();
        List<Position> rightPositions = new ArrayList<Position>();
        this.generateParallelLines(positions.iterator(), leftPositions, rightPositions, waveLength / 2.0, globe);

        if (useDefaultWaveLength)
            waveLength = this.computeDefaultWavelength(leftPositions, globe);
        double radius = (waveLength) / 2.0;

        // Generate wavy line to the left of the control line.
        PositionIterator iterator = new PositionIterator(leftPositions, waveLength, globe);
        this.computedPositions = this.generateWavePositions(iterator, radius / globe.getRadius(), false);
        this.path.setPositions(this.computedPositions);

        if (useDefaultWaveLength)
            waveLength = this.computeDefaultWavelength(rightPositions, globe);
        radius = (waveLength) / 2.0;

        // Generate wavy line to the right of the control line.
        iterator = new PositionIterator(rightPositions, waveLength, globe);
        this.path2.setPositions(this.generateWavePositions(iterator, radius / globe.getRadius(), true));
    }

    /**
     * Create positions that describe lines parallel to a control line.
     *
     * @param iterator       Iterator of control line positions.
     * @param leftPositions  List to collect positions on the left line.
     * @param rightPositions List to collect positions on the right line.
     * @param halfWidth      Distance from the center line to the left or right lines.
     * @param globe          Current globe.
     */
    public void generateParallelLines(Iterator<? extends Position> iterator, List<Position> leftPositions,
        List<Position> rightPositions, double halfWidth, Globe globe)
    {
        // Starting at the start of the line, take points three at a time. B is the current control point, A is the next
        // point in the line, and C is the previous point. We need to a find a vector that bisects angle ABC.
        //       B
        //       ---------> C
        //      /
        //     /
        //    /
        // A /

        Position posB = iterator.next();
        Position posA = iterator.next();

        Vec4 ptA = globe.computePointFromLocation(posA);
        Vec4 ptB = globe.computePointFromLocation(posB);
        Vec4 ptC;

        // Compute side points at the start of the line.
        this.generateParallelPoints(ptB, null, ptA, leftPositions, rightPositions, halfWidth, globe);

        while (iterator.hasNext())
        {
            posA = iterator.next();

            ptC = ptB;
            ptB = ptA;
            ptA = globe.computePointFromLocation(posA);

            generateParallelPoints(ptB, ptC, ptA, leftPositions, rightPositions, halfWidth, globe);
        }

        // Compute side points at the end of the line.
        generateParallelPoints(ptA, ptB, null, leftPositions, rightPositions, halfWidth, globe);
    }

    /**
     * Compute points on either side of a line segment. This method requires a point on the line, and either a next
     * point, previous point, or both.
     *
     * @param point          Center point about which to compute side points.
     * @param prev           Previous point on the line. May be null if {@code next} is non-null.
     * @param next           Next point on the line. May be null if {@code prev} is non-null.
     * @param leftPositions  Left position will be added to this list.
     * @param rightPositions Right position will be added to this list.
     * @param halfWidth      Distance from the center line to the left or right lines.
     * @param globe          Current globe.
     */
    protected void generateParallelPoints(Vec4 point, Vec4 prev, Vec4 next, List<Position> leftPositions,
        List<Position> rightPositions, double halfWidth, Globe globe)
    {
        if ((point == null) || (prev == null && next == null))
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (leftPositions == null || rightPositions == null)
        {
            String message = Logging.getMessage("nullValue.PositionListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 offset;
        Vec4 normal = globe.computeSurfaceNormalAtPoint(point);

        // Compute vector in the direction backward along the line.
        Vec4 backward = (prev != null) ? prev.subtract3(point) : point.subtract3(next);

        // Compute a vector perpendicular to segment BC, and the globe normal vector.
        Vec4 perpendicular = backward.cross3(normal);

        double length;
        // If both next and previous points are supplied then calculate the angle that bisects the angle current, next, prev.
        if (next != null && prev != null && !Vec4.areColinear(prev, point, next))
        {
            // Compute vector in the forward direction.
            Vec4 forward = next.subtract3(point);

            // Calculate the vector that bisects angle ABC.
            offset = forward.normalize3().add3(backward.normalize3());
            offset = offset.normalize3();

            // Compute the scalar triple product of the vector BC, the normal vector, and the offset vector to
            // determine if the offset points to the left or the right of the control line.
            double tripleProduct = perpendicular.dot3(offset);
            if (tripleProduct < 0)
            {
                offset = offset.multiply3(-1);
            }

            // Determine the length of the offset vector that will keep the left and right lines parallel to the control
            // line.
            Angle theta = backward.angleBetween3(offset);
            if (!Angle.ZERO.equals(theta))
                length = halfWidth / theta.sin();
            else
                length = halfWidth;
        }
        else
        {
            offset = perpendicular.normalize3();
            length = halfWidth;
        }
        offset = offset.multiply3(length);

        // Determine the left and right points by applying the offset.
        Vec4 ptRight = point.add3(offset);
        Vec4 ptLeft = point.subtract3(offset);

        // Convert cartesian points to geographic.
        Position posLeft = globe.computePositionFromPoint(ptLeft);
        Position posRight = globe.computePositionFromPoint(ptRight);

        leftPositions.add(posLeft);
        rightPositions.add(posRight);
    }
}
