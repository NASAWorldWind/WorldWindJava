/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Implementation of the Direction of Attack for Feint graphic (2.X.2.3.3).
 *
 * @author pabercrombie
 * @version $Id: DirectionOfAttackForFeint.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class DirectionOfAttackForFeint extends DirectionOfAttack
{
    /**
     * Offset applied to the label. This offset aligns the bottom edge of the label with the geographic position, in
     * order to keep the label above the graphic as the zoom changes.
     */
    protected final static Offset LABEL_OFFSET = new Offset(0.0, -1.0, AVKey.FRACTION, AVKey.FRACTION);
    /** Default angle of the arrowhead. */
    public final static Angle DEFAULT_ARROWHEAD_ANGLE = Angle.POS90;
    /**
     * Factor used to compute the distance between the solid and dashed lines in the arrow head. A larger value will
     * move the dashed line farther from the solid line.
     */
    protected static final double DASHED_LINE_DISTANCE = 0.5;
    /** Default number of intervals used to draw the curve. */
    public final static int DEFAULT_NUM_INTERVALS = 32;
    /** Default factor that determines the curvature of the line. */
    public final static double DEFAULT_CURVATURE = 0.5;

    /** Number of intervals used to draw the curve. */
    protected int intervals = DEFAULT_NUM_INTERVALS;
    /**
     * Factor that controls the curve of the line. Valid values are 0 to 1. Larger values result in a more pronounced
     * curve.
     */
    protected double curvature = DEFAULT_CURVATURE;

    /** Shape attributes for the dashed part of the graphic. */
    protected ShapeAttributes dashedAttributes = new BasicShapeAttributes();

    /** Position of the label along the curve. */
    protected Position labelPosition;
    /**
     * Orientation position for the label. (The label is drawn on a line between this position and {@link
     * #labelPosition}.
     */
    protected Position labelOrientationPosition;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_DCPN_DAFF);
    }

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public DirectionOfAttackForFeint(String sidc)
    {
        super(sidc);
        this.setArrowAngle(DEFAULT_ARROWHEAD_ANGLE);
    }

    /**
     * Indicates the number of intervals used to draw the curve in this graphic. More intervals results in a smoother
     * curve.
     *
     * @return Intervals used to draw arc.
     */
    public int getIntervals()
    {
        return this.intervals;
    }

    /**
     * Specifies the number of intervals used to draw the curve in this graphic. More intervals will result in a
     * smoother looking curve.
     *
     * @param intervals Number of intervals for drawing the curve. Must at least three.
     */
    public void setIntervals(int intervals)
    {
        if (intervals < 3)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", intervals);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.intervals = intervals;
        this.onShapeChanged();
    }

    /**
     * Indicates a factor that determines the curvature of the line. Valid values are zero to one. A large value results
     * in a more pronounced curve.
     *
     * @return The factor that determines the curvature of the line.
     */
    public double getCurvature()
    {
        return this.curvature;
    }

    /**
     * Specifies a factor that determines the curvature of the line. Valid values are zero to one. A large value results
     * in a more pronounced curve.
     *
     * @param factor The factor that determines the curvature of the line.
     */
    public void setCurvature(double factor)
    {
        if (factor < 0.0 || factor > 1.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.curvature = factor;
    }

    protected void onShapeChanged()
    {
        this.paths = null; // Need to recompute paths
    }

    /**
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void createShapes(DrawContext dc)
    {
        // This graphic is composed of three paths:
        // 1) Curve from start position to the tip of the arrow head
        // 3) The arrow head
        // 4) The dashed line around the arrow head
        this.paths = new Path[3];

        Globe globe = dc.getGlobe();
        Vec4 pt1 = globe.computePointFromLocation(this.startPosition);
        Vec4 pt2 = globe.computePointFromLocation(this.endPosition);

        // Compute control points for the Bezier curve.
        Vec4[] controlPoints = this.computeBezierControlPoints(dc, pt1, pt2, this.getCurvature());

        // We need to find the point on the curve that is furthest from the line between the end points of the arrow.
        // We'll place the label at this point. We'll do this as we compute the curve.
        Line controlLine = Line.fromSegment(pt1, pt2);
        int furthestPoint = 0;
        double maxDistance = -Double.MAX_VALUE;

        List<Position> curvePositions = new ArrayList<Position>();
        int[] coefficients = new int[controlPoints.length]; // Array to hold binomial coefficients

        // Invoke the Bezier curve function to compute points along the curve.
        int intervals = this.getIntervals();
        double delta = 1.0 / intervals;
        for (int i = 0; i < intervals; i++)
        {
            double t = i * delta;
            Vec4 p = TacticalGraphicUtil.bezierCurve(controlPoints, t, coefficients);
            Position pos = globe.computePositionFromPoint(p);
            curvePositions.add(pos);

            // Determine if this point is further from the control line than previous points.
            double dist = controlLine.distanceTo(p);
            if (dist > maxDistance)
            {
                furthestPoint = i;
                maxDistance = dist;
            }
        }
        curvePositions.add(this.endPosition); // Make sure the curve ends where it should

        // Determine where to put the label.
        this.labelPosition = curvePositions.get(furthestPoint);
        if (furthestPoint != curvePositions.size() - 1)
        {
            this.labelOrientationPosition = curvePositions.get(furthestPoint + 1);
        }
        else
        {
            // If the furthest point is at the end of line then use the previous point as the orientation position.
            // This should never happen due to the shape of the curve in this graphic, but include this check in
            // case a subclass changes the curve control points.
            this.labelOrientationPosition = curvePositions.get(furthestPoint - 1);
        }

        // Create a path for the line part of the arrow.
        this.paths[0] = this.createPath(curvePositions);

        // Create the arrowhead.
        Vec4 dir = pt1.subtract3(pt2);
        double arrowheadLength = pt2.subtract3(pt1).getLength3() * this.getArrowLength();
        List<Position> positions = this.computeArrowheadPositions(dc, pt2, dir, arrowheadLength);
        this.paths[1] = this.createPath(positions);

        // Create the dashed line around the arrowhead. First compute the tip of the dashed arrowhead.
        pt2 = pt2.subtract3(dir.normalize3().multiply3(arrowheadLength * DASHED_LINE_DISTANCE));

        // Invoke computeArrowheadPositions again using a larger arrow length, to get a larger arrow head. Compute the
        // length of this arrow so that the ends of the dashed arrow head will fall inline with the solid arrow head.
        positions = this.computeArrowheadPositions(dc, pt2, dir, arrowheadLength * (1 + DASHED_LINE_DISTANCE));

        this.paths[2] = this.createPath(positions);
        this.paths[2].setAttributes(this.dashedAttributes);
    }

    /** Determine active attributes for this frame. */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // Copy active attributes to the dashed attribute bundle.
        this.dashedAttributes.copy(this.getActiveShapeAttributes());

        // Re-apply stipple pattern.
        this.dashedAttributes.setOutlineStipplePattern(this.getOutlineStipplePattern());
        this.dashedAttributes.setOutlineStippleFactor(this.getOutlineStippleFactor());
    }

    @Override
    protected void createLabels()
    {
        String text = this.getText();
        if (!WWUtil.isEmpty(text))
        {
            TacticalGraphicLabel label = this.addLabel(text);
            label.setOffset(LABEL_OFFSET);
        }
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (WWUtil.isEmpty(this.labels))
            return;

        this.labels.get(0).setPosition(this.labelPosition);
        this.labels.get(0).setOrientationPosition(this.labelOrientationPosition);
    }

    /** {@inheritDoc} */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        return LABEL_OFFSET;
    }

    /**
     * Compute the position of control points that will generate a Bezier curve that looks like the Direction of Attack
     * for Feint graphic in MIL-STD-2525C (pg. 499).
     *
     * @param dc        Current draw context.
     * @param start     Beginning of the infiltration lane control line.
     * @param end       End of the infiltration lane control line.
     * @param curvature Factor that controls the curvature of the line. Valid values are between zero and one. A higher
     *                  value results in a more pronounced curve.
     *
     * @return Control points for a Bezier curve. The first control point is equal to {@code start}, and the last point
     *         is equal to {@code end}.
     */
    protected Vec4[] computeBezierControlPoints(DrawContext dc, Vec4 start, Vec4 end, double curvature)
    {
        Globe globe = dc.getGlobe();

        // Find length and direction of the control line.
        Vec4 dir = end.subtract3(start);
        double length = dir.getLength3();
        dir = dir.normalize3();

        // Generate control points to the left and right of the control line to generate a curve that wiggles back and
        // forth. We do this by selecting points along the control line and adding a perpendicular vector multiplied by
        // a scaling factor.
        Vec4 normal = globe.computeSurfaceNormalAtPoint(start).normalize3();
        Vec4 perpendicular = dir.cross3(normal).multiply3(length * curvature);

        // Coefficients to offset control points from the control line. These numbers are chosen to yield a curve that
        // looks like the one in template for Direction of Attack for Feint in MIL-STD-2525C, pg. 499.
        double[] coefficients = {0.2, -0.6, 0.2, 0};

        Vec4[] controlPoints = new Vec4[coefficients.length + 2];
        controlPoints[0] = start; // First control point is start position
        controlPoints[controlPoints.length - 1] = end; // Last control point is end position

        // Choose regularly spaced points along the control line. At each point select a point to the left of the line,
        // on the line, or to the right of the line.
        double delta = length / (coefficients.length + 1);
        for (int i = 0; i < coefficients.length; i++)
        {
            controlPoints[i + 1] = start.add3(dir.multiply3((i + 1) * delta))
                .add3(perpendicular.multiply3(coefficients[i]));
        }

        return controlPoints;
    }
}