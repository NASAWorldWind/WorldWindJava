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
import gov.nasa.worldwind.symbology.TacticalGraphicUtil;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Implementation of the Infiltration Lane graphic (2.X.2.5.2.4).
 *
 * @author pabercrombie
 * @version $Id: InfiltrationLane.java 555 2012-04-25 18:59:29Z pabercrombie $
 */
public class InfiltrationLane extends AbstractMilStd2525TacticalGraphic
{
    /** Default number of intervals used to draw the curve. */
    public final static int DEFAULT_NUM_INTERVALS = 32;
    /** Default factor that determines the curvature of the line. */
    public final static double DEFAULT_CURVATURE = 0.3;
    /** Number of control points that define the curve. */
    protected final static int NUM_CONTROL_POINTS = 9;

    /** Number of intervals used to draw the curve. */
    protected int intervals = DEFAULT_NUM_INTERVALS;
    /**
     * Factor that controls the curve of the line. Valid values are 0 to 1. Larger values result in a more pronounced
     * curve.
     */
    protected double curvature = DEFAULT_CURVATURE;

    /** First control point. */
    protected Position position1;
    /** Second control point. */
    protected Position position2;
    /** Third control point. */
    protected Position position3;

    /** Path used to render the line. */
    protected Path[] paths;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_INFNLE);
    }

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public InfiltrationLane(String sidc)
    {
        super(sidc);
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
     * @param intervals Number of intervals for drawing the curve.
     */
    public void setIntervals(int intervals)
    {
        if (intervals < 1)
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

    /**
     * Indicates the number of control points that define the curve.
     *
     * @return the number of control points.
     */
    protected int getNumControlPoints()
    {
        return NUM_CONTROL_POINTS;
    }

    protected void onShapeChanged()
    {
        this.paths = null; // Need to recompute paths
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
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        this.paths = new Path[2];

        Globe globe = dc.getGlobe();

        Vec4 pt1 = globe.computePointFromLocation(this.position1);
        Vec4 pt2 = globe.computePointFromLocation(this.position2);
        Vec4 pt3 = globe.computePointFromLocation(this.position3);

        // Compute control points for the Bezier curve.
        Vec4[] controlPoints = this.computeBezierControlPoints(dc, pt1, pt2, this.getNumControlPoints(),
            this.getCurvature());

        int intervals = this.getIntervals();
        List<Position> curvePositionsLeft = new ArrayList<Position>(intervals);
        List<Position> curvePositionsRight = new ArrayList<Position>(intervals);

        // Compute binomial coefficients for the Bezier function. Do this now so we don't have to recompute coefficients
        // for every iteration of the loop below.
        int[] coefficients = new int[controlPoints.length];

        // Project Pt. 3 onto the control line and compute an offset that will shift the curve from the control line to
        // Pt. 3. We want Pt. 3 to be the final curve.
        Vec4 projectedPt3 = Line.nearestPointOnSegment(pt1, pt2, pt3);
        // Find the point on the Bezier curve that is nearest to the segment through Pt. 3.
        Vec4 offsetPt = this.bezierNearestPointToSegment(projectedPt3, pt3, controlPoints, coefficients, 0.01);
        Vec4 offset = pt3.subtract3(offsetPt);

        // Invoke the Bezier curve function to compute points along the curve.
        double delta = 1.0 / intervals;
        for (int i = 0; i <= intervals; i++)
        {
            double t = i * delta;
            Vec4 p = TacticalGraphicUtil.bezierCurve(controlPoints, t, coefficients);

            // Compute points to the left and right of the control line.
            Vec4 ptLeft = p.add3(offset);
            Vec4 ptRight = p.subtract3(offset);

            curvePositionsLeft.add(globe.computePositionFromPoint(ptLeft));
            curvePositionsRight.add(globe.computePositionFromPoint(ptRight));
        }

        this.paths[0] = this.createPath(curvePositionsLeft);
        this.paths[1] = this.createPath(curvePositionsRight);
    }

    /**
     * Determine the point along a Bezier curve that is closest to a line segment.
     *
     * @param p0            First line segment point.
     * @param p1            Second line segment point.
     * @param controlPoints Control points for Bezier curve.
     * @param coefficients  Binomial coefficients for computing curve.
     * @param tolerance     Numerical tolerance. Smaller values will yield a more accurate answer, but will take more
     *                      iterations to compute.
     *
     * @return the point on the curve that is closest to the specified line segment.
     */
    protected Vec4 bezierNearestPointToSegment(Vec4 p0, Vec4 p1, Vec4[] controlPoints, int[] coefficients,
        double tolerance)
    {
        double dist1;
        double dist2;

        double t1 = 0.0;
        double t2 = 1.0;

        // Find the nearest point by performing a bisection search along the Bezier function. Starting at the ends of
        // the curve, test the distance at two points to the target line. Divide the interval in two, and repeat with
        // the side that is closer to the line segment. Continue doing this until the difference in the two distances
        // becomes less than the threshold.
        Vec4 p = TacticalGraphicUtil.bezierCurve(controlPoints, t1, coefficients);
        Vec4 nearest = Line.nearestPointOnSegment(p0, p1, p);
        dist1 = nearest.distanceTo3(p);

        double delta;
        do
        {
            p = TacticalGraphicUtil.bezierCurve(controlPoints, t2, coefficients);
            nearest = Line.nearestPointOnSegment(p0, p1, p);
            dist2 = nearest.distanceTo3(p);

            double avg = (t1 + t2) / 2;
            delta = Math.abs(dist1 - dist2);

            if (dist2 < dist1)
            {
                t1 = t2;
                dist1 = dist2;
            }
            t2 = avg;
        }
        while (delta > tolerance);

        return p;
    }

    /**
     * Compute the position of control points that will generate a Bezier curve that looks like the Infiltration Lane
     * graphic in MIL-STD-2525C (pg. 526).
     *
     * @param dc               Current draw context.
     * @param start            Beginning of the infiltration lane control line.
     * @param end              End of the infiltration lane control line.
     * @param numControlPoints Number of control points to generate. More control points result in more "wiggles" in the
     *                         line.
     * @param curvature        Factor that controls the curvature of the line. Valid values are between zero and one. A
     *                         higher value results in a more pronounced curve.
     *
     * @return Control points for a Bezier curve. The first control point is equal to {@code start}, and the last point
     *         is equal to {@code end}.
     */
    protected Vec4[] computeBezierControlPoints(DrawContext dc, Vec4 start, Vec4 end, int numControlPoints,
        double curvature)
    {
        Globe globe = dc.getGlobe();

        // Find length and direction of the control line.
        Vec4 dir = end.subtract3(start);
        double length = dir.getLength3();
        dir = dir.normalize3();

        // Generate control points to the left and right of the control line to generate a curve that wiggles back and
        // forth.

        Vec4 normal = globe.computeSurfaceNormalAtPoint(start);
        Vec4 perpendicular = dir.cross3(normal).normalize3().multiply3(length * curvature);

        Vec4[] controlPoints = new Vec4[numControlPoints];

        controlPoints[0] = start; // First control point is start position
        controlPoints[numControlPoints - 1] = end; // Last control point is end position

        // Generate a point to the left of the line, then on the line, then the right of the line, then on the line, etc.
        int[] signs = new int[] {1, 0, -1, 0};

        // Choose regularly spaced points along the control line. At each point select a point to the left of the line,
        // on the line, or to the right of the line.
        double delta = length / numControlPoints;
        for (int i = 1; i < numControlPoints - 1; i++)
        {
            int sign = signs[i % signs.length];
            controlPoints[i] = start.add3(dir.multiply3(i * delta))
                .add3(perpendicular.multiply3(sign));
        }

        return controlPoints;
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        String text = this.getText();
        if (!WWUtil.isEmpty(text))
            this.addLabel(text);
    }

    /**
     * Determine positions for the start and end labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (WWUtil.isEmpty(this.labels))
            return;

        LatLon ll = LatLon.interpolate(0.5, this.position1, this.position2);
        this.labels.get(0).setPosition(new Position(ll, 0));
        this.labels.get(0).setOrientationPosition(this.position2);
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
        path.setSurfacePath(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
