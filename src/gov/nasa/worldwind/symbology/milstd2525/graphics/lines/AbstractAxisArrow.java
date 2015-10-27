/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Base class for axis of advance arrow graphics. These arrows are specified by control points along the center line of
 * the arrow. The final control point determines either the width of the arrowhead (default) or the width of the route.
 * MIL-STD-2525C (pg. 517) specifies that the final control point determines the width of the arrowhead, but some
 * applications may prefer to use this control point to set the width of the route precisely. The {@link
 * #isFinalPointWidthOfRoute() finalPointWidthOfRoute} field controls this behavior.
 *
 * @author pabercrombie
 * @version $Id: AbstractAxisArrow.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractAxisArrow extends AbstractMilStd2525TacticalGraphic
{
    /** Path used to render the line. */
    protected Path[] paths;

    /** Control points that define the shape. */
    protected Iterable<? extends Position> positions;

    /** Positions computed from the control points, used to draw the arrow path. */
    protected List<? extends Position> arrowPositions;

    /**
     * Indicates whether the final control point marks the width of the route or the width of the arrowhead. Default is
     * the width of the arrowhead.
     */
    protected boolean finalPointWidthOfRoute;

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public AbstractAxisArrow(String sidc)
    {
        this(sidc, 1);
    }

    /**
     * Create a new arrow graphic made up of more than one path. This constructor allocates the {@link #paths}, and
     * creates the requested number of paths. The first element in the array is the main path that outlines the arrow.
     * Subclasses are responsible for configuring the other paths.
     *
     * @param sidc     Symbol code the identifies the graphic.
     * @param numPaths Number of paths to create.
     */
    public AbstractAxisArrow(String sidc, int numPaths)
    {
        super(sidc);

        if (numPaths < 1)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", numPaths);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.paths = new Path[numPaths];
        for (int i = 0; i < numPaths; i++)
        {
            this.paths[i] = this.createPath();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points that orient the graphic. Must provide at least three points.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Ensure that the position list provides at least 3 control points.
        try
        {
            Iterator<? extends Position> iterator = positions.iterator();
            iterator.next();
            iterator.next();
            iterator.next();
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.positions = positions;
        this.arrowPositions = null; // Need to recompute path for the new control points
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return this.positions;
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        if (this.positions != null)
        {
            return this.positions.iterator().next(); // use the first position
        }
        return null;
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        if (this.arrowPositions == null)
        {
            this.createShapePositions(dc);
        }

        for (Path path : this.paths)
        {
            path.render(dc);
        }
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        if (this.paths == null)
            return;

        for (Path path : this.paths)
        {
            path.setDelegateOwner(owner);
        }
    }

    /**
     * Indicates whether or not the final control point marks the width of the arrowhead or the width of the route.
     * MIL-STD-2525C specifies that the final control point marks the edge of the arrow (pg. 517), and this is the
     * default. However, some applications may prefer to specify the width of the route rather than the width of the
     * arrowhead. In the diagram below, the default behavior is for the final control point to specify point A. When
     * {@code finalPointWidthOfRoute} is true the final control point specifies point B instead.
     * <p/>
     * <pre>
     *                 A
     *                 |\
     *                 | \
     * ----------------|  \
     *                 B   \
     *                     /
     * ----------------|  /
     *                 | /
     *                 |/
     * </pre>
     *
     * @return True if the final control point determines the width of the route instead of the width of the arrow head
     *         (point B in the diagram above).
     */
    public boolean isFinalPointWidthOfRoute()
    {
        return this.finalPointWidthOfRoute;
    }

    /**
     * Specifies whether or not the final control point marks the edge of the arrow head or the width of the route. See
     * {@link #isFinalPointWidthOfRoute()} for details.
     *
     * @param finalPointIsWidthOfRoute True if the final control point determines the width of the route instead of the
     *                                 width of the arrow head.
     */
    public void setFinalPointWidthOfRoute(boolean finalPointIsWidthOfRoute)
    {
        this.finalPointWidthOfRoute = finalPointIsWidthOfRoute;
    }

    /**
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    protected void createShapePositions(DrawContext dc)
    {
        Globe globe = dc.getGlobe();

        // Collect positions in two lists, one for points on the left side of the control line, and one for the right side.
        List<Position> leftPositions = new ArrayList<Position>();
        List<Position> rightPositions = new ArrayList<Position>();
        List<Position> arrowHeadPositions = new ArrayList<Position>();

        double halfWidth = this.createArrowHeadPositions(leftPositions, rightPositions, arrowHeadPositions, globe);

        this.createLinePositions(leftPositions, rightPositions, halfWidth, globe);

        Collections.reverse(leftPositions);

        List<Position> allPositions = new ArrayList<Position>(leftPositions);
        allPositions.addAll(arrowHeadPositions);
        allPositions.addAll(rightPositions);

        this.arrowPositions = allPositions;
        this.paths[0].setPositions(allPositions); // Apply positions to the main path
    }

    /**
     * Create positions that make up the arrow head.
     * <p/>
     * The arrow head is defined by the first two control points, and the last point. Pt. 1' is the point on the center
     * line at the base of the arrow head, and Pt. N' is the reflection of Pt. N about the center line.
     * <pre>
     *                 Pt N
     *                 |\
     * Left line       | \
     * ----------------|  \
     * Pt 2        Pt 1'   \ Pt 1
     *                     /
     * ----------------|  /
     * Right line      | /
     *                 |/Pt N'
     * </pre>
     *
     * @param leftPositions      List to collect positions on the left arrow line. This list receives the position where
     *                           the left line meets the arrow head.
     * @param rightPositions     List to collect positions on the right arrow line. This list receives the position
     *                           where the right line meets the arrow head.
     * @param arrowHeadPositions List to collect positions that make up the arrow head. This list receives positions for
     *                           Pt. N, Pt. 1, and Pt. N', in that order.
     * @param globe              Current globe.
     *
     * @return The distance from the center line to the left and right lines.
     */
    protected double createArrowHeadPositions(List<Position> leftPositions, List<Position> rightPositions,
        List<Position> arrowHeadPositions, Globe globe)
    {
        Iterator<? extends Position> iterator = this.positions.iterator();

        Position pos1 = iterator.next();
        Position pos2 = iterator.next();

        Position posN = null;
        while (iterator.hasNext())
        {
            posN = iterator.next();
        }

        if (posN == null)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 pt1 = globe.computePointFromLocation(pos1);
        Vec4 pt2 = globe.computePointFromLocation(pos2);
        Vec4 ptN = globe.computePointFromLocation(posN);

        // Compute a vector that points from Pt. 1 toward Pt. 2
        Vec4 v12 = pt1.subtract3(pt2).normalize3();

        Vec4 pt1_prime = pt1.add3(v12.multiply3(ptN.subtract3(pt1).dot3(v12)));

        // If the final control point determines the width of the route (not the width of the arrowhead) then compute
        // Point N from the final control point.
        if (this.isFinalPointWidthOfRoute())
        {
            ptN = ptN.add3(ptN.subtract3(pt1_prime));
            posN = globe.computePositionFromPoint(ptN);
        }

        Vec4 ptN_prime = pt1_prime.subtract3(ptN.subtract3(pt1_prime));

        Vec4 normal = globe.computeSurfaceNormalAtPoint(pt1_prime);

        // Compute the distance from the center line to the left and right lines.
        double halfWidth = ptN.subtract3(pt1_prime).getLength3() / 2;

        Vec4 offset = normal.cross3(v12).normalize3().multiply3(halfWidth);

        Vec4 pLeft = pt1_prime.add3(offset);
        Vec4 pRight = pt1_prime.subtract3(offset);

        Position posLeft = globe.computePositionFromPoint(pLeft);
        Position posRight = globe.computePositionFromPoint(pRight);

        leftPositions.add(posLeft);
        rightPositions.add(posRight);

        Position posN_prime = globe.computePositionFromPoint(ptN_prime);

        // Compute the scalar triple product of the vector 12, the normal vector, and a vector from the center line
        // toward Pt. N to determine if the offset points to the left or the right of the control line.
        double tripleProduct = offset.dot3(ptN.subtract3(pt1_prime));
        if (tripleProduct < 0)
        {
            Position tmp = posN; // Swap N and N'
            posN = posN_prime;
            posN_prime = tmp;
        }

        arrowHeadPositions.add(posN);
        arrowHeadPositions.add(pos1);
        arrowHeadPositions.add(posN_prime);

        return halfWidth;
    }

    /**
     * Create positions that make up the left and right arrow lines.
     *
     * @param leftPositions  List to collect positions on the left line.
     * @param rightPositions List to collect positions on the right line.
     * @param halfWidth      Distance from the center line to the left or right lines. Half the width of the arrow's
     *                       double lines.
     * @param globe          Current globe.
     */
    protected void createLinePositions(List<Position> leftPositions, List<Position> rightPositions, double halfWidth,
        Globe globe)
    {
        Iterator<? extends Position> iterator = positions.iterator();

        Position posB = iterator.next();
        Position posA = iterator.next();

        // Starting at the arrow head end of the line, take points three at a time. B is the current control point, A is
        // the next point in the line, and C is the previous point. We need to a find a vector that bisects angle ABC.
        //       B
        //       ---------> C
        //      /
        //     /
        //    /
        // A /

        Vec4 pA = globe.computePointFromLocation(posA);
        Vec4 pB = globe.computePointFromLocation(posB);
        Vec4 pC;
        while (iterator.hasNext())
        {
            posA = iterator.next();

            pC = pB;
            pB = pA;
            pA = globe.computePointFromLocation(posA);

            Vec4 offset;
            Vec4 normal = globe.computeSurfaceNormalAtPoint(pB);

            Vec4 vBC = pC.subtract3(pB);

            // Compute a vector perpendicular to segment BC, and the globe normal vector.
            Vec4 perpendicular = vBC.cross3(normal);

            if (iterator.hasNext() && !Vec4.areColinear(pA, pB, pC))
            {
                Vec4 vBA = pA.subtract3(pB);

                // Calculate the vector that bisects angle ABC.
                offset = vBA.normalize3().add3(vBC.normalize3());
                offset = offset.normalize3();

                // Compute the scalar triple product of the vector BC, the normal vector, and the offset vector to
                // determine if the offset points to the left or the right of the control line.
                double tripleProduct = perpendicular.dot3(offset);
                if (tripleProduct < 0)
                {
                    offset = offset.multiply3(-1);
                }
            }
            else
            {
                // If this is the last control point then don't consider the surrounding points, just compute an offset
                // perpendicular to the control line.
                offset = perpendicular.normalize3();
            }

            // Determine the length of the offset vector that will keep the left and right lines parallel to the control
            // line.
            Angle theta = vBC.angleBetween3(offset);
            double length = halfWidth / theta.sin();
            offset = offset.multiply3(length);

            // Determine the left and right points by applying the offset.
            Vec4 pRight = pB.add3(offset);
            Vec4 pLeft = pB.subtract3(offset);

            // Convert cartesian points to geographic.
            Position posLeft = globe.computePositionFromPoint(pLeft);
            Position posRight = globe.computePositionFromPoint(pRight);

            leftPositions.add(posLeft);
            rightPositions.add(posRight);
        }
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @return New path configured with defaults appropriate for this type of graphic.
     */
    protected Path createPath()
    {
        Path path = new Path();
        path.setFollowTerrain(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
