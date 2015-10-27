/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.TacticalGraphicUtil;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Attack By Fire Position graphic (2.X.2.5.3.3).
 *
 * @author pabercrombie
 * @version $Id: AttackByFirePosition.java 555 2012-04-25 18:59:29Z pabercrombie $
 */
public class AttackByFirePosition extends AbstractMilStd2525TacticalGraphic
{
    /** Default length of the arrowhead, as a fraction of the total line length. */
    public final static double DEFAULT_ARROWHEAD_LENGTH = 0.2;
    /** Default angle of the arrowhead. */
    public final static Angle DEFAULT_ARROWHEAD_ANGLE = Angle.fromDegrees(70.0);
    /**
     * Default length of the legs of the graphic's base, as a fraction of the distance between the control points the
     * define the base.
     */
    public final static double DEFAULT_LEG_LENGTH = 0.25;

    /** Length of the arrowhead from base to tip, as a fraction of the total line length. */
    protected Angle arrowAngle = DEFAULT_ARROWHEAD_ANGLE;
    /** Angle of the arrowhead. */
    protected double arrowLength = DEFAULT_ARROWHEAD_LENGTH;
    /**
     * Length of the legs on the graphic's base, as a fraction of the distance between the control points that define
     * the base.
     */
    protected double legLength = DEFAULT_LEG_LENGTH;

    /** First control point. */
    protected Position position1;
    /** Second control point. */
    protected Position position2;
    /** Third control point. */
    protected Position position3;

    /** Path used to render the graphic. */
    protected Path[] paths;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_ARS_AFP);
    }

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public AttackByFirePosition(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates the angle of the arrowhead.
     *
     * @return Angle of the arrowhead in the graphic.
     */
    public Angle getArrowAngle()
    {
        return this.arrowAngle;
    }

    /**
     * Specifies the angle of the arrowhead in the graphic.
     *
     * @param arrowAngle The angle of the arrowhead. Must be greater than zero degrees and less than 90 degrees.
     */
    public void setArrowAngle(Angle arrowAngle)
    {
        if (arrowAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (arrowAngle.degrees <= 0 || arrowAngle.degrees >= 90)
        {
            String msg = Logging.getMessage("generic.AngleOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.arrowAngle = arrowAngle;
    }

    /**
     * Indicates the length of the arrowhead.
     *
     * @return The length of the arrowhead as a fraction of the total line length.
     */
    public double getArrowLength()
    {
        return this.arrowLength;
    }

    /**
     * Specifies the length of the arrowhead.
     *
     * @param arrowLength Length of the arrowhead as a fraction of the total line length. If the arrowhead length is
     *                    0.25, then the arrowhead length will be one quarter of the total line length.
     */
    public void setArrowLength(double arrowLength)
    {
        if (arrowLength < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.arrowLength = arrowLength;
    }

    /**
     * Indicates the length of legs of the graphic's base.
     *
     * @return The length of the legs of the base, as a fraction of the distance between the control points that define
     *         the base.
     */
    public double getLegLength()
    {
        return this.arrowLength;
    }

    /**
     * Specifies the length of the legs of the graphic's base.
     *
     * @param legLength Length of the legs of the graphic's base, as a fraction of the distance between the control
     *                  points that define the base.
     */
    public void setLegLength(double legLength)
    {
        if (legLength < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.legLength = legLength;
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

        try
        {
            Iterator<? extends Position> iterator = positions.iterator();
            this.position1 = iterator.next();
            this.position2 = iterator.next();
            this.position3 = iterator.next();
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.paths = null; // Need to recompute path for the new control points
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(this.position1, this.position2, this.position3);
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.position1;
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        if (this.paths == null)
        {
            this.createShapes(dc);
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
     * Create the paths required to draw the graphic.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        this.paths = new Path[3];

        Position baseMidpoint = new Position(LatLon.interpolate(0.5, this.position2, this.position3), 0);

        // Create a path for the line part of the arrow
        this.paths[0] = this.createPath(Arrays.asList(baseMidpoint, this.position1));

        // Create the arrowhead
        List<Position> positions = this.computeArrowheadPositions(dc, baseMidpoint, this.position1);
        this.paths[1] = createPath(positions);

        positions = this.computeBasePositions(dc, this.position2, this.position3, this.position1);
        this.paths[2] = createPath(positions);
    }

    /**
     * Determine the positions that make up the base of the graphic (a trapezoid missing one side).
     *
     * @param dc             Current draw context.
     * @param position1      The first control point that defines the graphic base.
     * @param position2      The second control point that defines the graphic base.
     * @param orientationPos A point on the arrow head side of the graphic. The legs of the base will point away from
     *                       this position.
     *
     * @return Positions that define the graphic's base.
     */
    protected List<Position> computeBasePositions(DrawContext dc, Position position1, Position position2,
        Position orientationPos)
    {
        Globe globe = dc.getGlobe();
        //   A \
        //      \
        //       | B
        //       |        x Orientation position
        //       | C
        //      /
        //   D /

        Vec4 pB = globe.computePointFromPosition(position1);
        Vec4 pC = globe.computePointFromPosition(position2);

        // Find vector in the direction of the arrow
        Vec4 vBC = pC.subtract3(pB);

        double legLength = vBC.getLength3() * this.getLegLength();

        Vec4 normal = globe.computeSurfaceNormalAtPoint(pB);

        // Compute a vector perpendicular to the segment and the normal vector
        Vec4 perpendicular = vBC.cross3(normal);

        // Compute a vector parallel to the base. We will find points A and D by adding parallel and perpendicular
        // components to the control points.
        Vec4 parallel = vBC.normalize3().multiply3(legLength);

        // Determine which side of the line BC the orientation point falls on. We want the legs of the base to point
        // away from the orientation position, so set the direction of the perpendicular component according to the
        // sign of the scalar triple product.
        Vec4 vOrientation = globe.computePointFromPosition(orientationPos).subtract3(pB);
        double tripleProduct = perpendicular.dot3(vOrientation);
        double sign = (tripleProduct > 0) ? -1 : 1;

        perpendicular = perpendicular.normalize3().multiply3(legLength * sign);

        // Find point A
        Vec4 pA = pB.add3(perpendicular).subtract3(parallel);

        // Find Point D
        normal = globe.computeSurfaceNormalAtPoint(pB);
        perpendicular = vBC.cross3(normal);
        perpendicular = perpendicular.normalize3().multiply3(legLength * sign);

        Vec4 pD = pC.add3(perpendicular).add3(parallel);

        return TacticalGraphicUtil.asPositionList(globe, pA, pB, pC, pD);
    }

    /**
     * Determine the positions that make up the arrowhead.
     *
     * @param dc   Current draw context.
     * @param base Position of the arrow's starting point.
     * @param tip  Position of the arrow head tip.
     *
     * @return Positions that define the arrowhead.
     */
    protected List<Position> computeArrowheadPositions(DrawContext dc, Position base, Position tip)
    {
        Globe globe = dc.getGlobe();
        //                  _
        //             A\    | 1/2 width
        // Pt. 1________B\  _|
        //               /
        //            C/
        //             | |
        //            Length

        Vec4 p1 = globe.computePointFromPosition(base);
        Vec4 pB = globe.computePointFromPosition(tip);

        // Find vector in the direction of the arrow
        Vec4 vB1 = p1.subtract3(pB);

        double arrowLengthFraction = this.getArrowLength();

        // Find the point at the base of the arrowhead
        Vec4 arrowBase = pB.add3(vB1.multiply3(arrowLengthFraction));

        Vec4 normal = globe.computeSurfaceNormalAtPoint(arrowBase);

        // Compute the length of the arrowhead
        double arrowLength = vB1.getLength3() * arrowLengthFraction;
        double arrowHalfWidth = arrowLength * this.getArrowAngle().tanHalfAngle();

        // Compute a vector perpendicular to the segment and the normal vector
        Vec4 perpendicular = vB1.cross3(normal);
        perpendicular = perpendicular.normalize3().multiply3(arrowHalfWidth);

        // Find points A and C
        Vec4 pA = arrowBase.add3(perpendicular);
        Vec4 pC = arrowBase.subtract3(perpendicular);

        return TacticalGraphicUtil.asPositionList(globe, pA, pB, pC);
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @param positions Positions that define the path.
     *
     * @return New path configured with defaults appropriate for this type of graphic.
     */
    protected Path createPath(List<Position> positions)
    {
        Path path = new Path(positions);
        path.setFollowTerrain(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
