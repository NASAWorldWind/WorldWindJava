/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Implementation of the Direction of Attack, Aviation graphic (2.X.2.5.2.2.1).
 *
 * @author pabercrombie
 * @version $Id: DirectionOfAttackAviation.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class DirectionOfAttackAviation extends DirectionOfAttack
{
    /** Default number of intervals used to draw the curve. */
    public final static int DEFAULT_NUM_INTERVALS = 32;
    /** Default length of the bow tie part of the graphic, as a fraction of the graphic's total length. */
    public final static double DEFAULT_BOW_TIE_LENGTH = 0.05;
    /** Default width of the bow tie part of the graphic, as a fraction of the length of the bow tie. */
    public final static double DEFAULT_BOW_TIE_WIDTH = 0.25;
    /** Default angle that determines the curvature of the line. */
    public final static Angle DEFAULT_CURVATURE = Angle.fromDegrees(25);

    /** Number of intervals used to draw the curve. */
    protected int intervals = DEFAULT_NUM_INTERVALS;
    /** Length of the bow tie part of the graphic, as a fraction of the graphic's total length. */
    protected double bowTieLength = DEFAULT_BOW_TIE_LENGTH;
    /** Width of the bow tie part of the graphic, as a fraction of the length of the bow tie. */
    protected double bowTieWidth = DEFAULT_BOW_TIE_WIDTH;
    /**
     * Angle that controls the curve of the line. A large angle results in a more pronounced curve. An angle of zero
     * results in a straight line.
     */
    protected Angle curvature = DEFAULT_CURVATURE;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_DIRATK_AVN);
    }

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public DirectionOfAttackAviation(String sidc)
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
     * Indicates the length of the bow tie part of the graphic, as a fraction of the graphic's total length.
     *
     * @return Length of the bow tie as a fraction of the total length of the graphic.
     */
    public double getBowTieLength()
    {
        return this.bowTieLength;
    }

    /**
     * Specifies the length of the bow tie part of the graphic, as a fraction of the graphic's total length.
     *
     * @param bowTieLength Length of the bow tie as a fraction of the total length of the graphic.
     */
    public void setBowTieLength(double bowTieLength)
    {
        if (bowTieLength < 0.0 || bowTieLength > 1.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", bowTieLength);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.bowTieLength = bowTieLength;
    }

    /**
     * Indicates the width of the bow tie part of the graphic, as a fraction of the length of the bow tie.
     *
     * @return Width of the bow tie as a fraction of the length of the bow tie.
     */
    public double getBowTieWidth()
    {
        return this.bowTieWidth;
    }

    /**
     * Specifies the width of the bow tie part of the graphic, as a fraction of the length of the bow tie.
     *
     * @param bowTieWidth Width of the bow tie as a fraction of the length of the bow tie.
     */
    public void setBowTieWidth(double bowTieWidth)
    {
        if (bowTieWidth < 0.0 || bowTieWidth > 1.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", bowTieWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.bowTieWidth = bowTieWidth;
    }

    /**
     * Indicates the angle that determines the curvature of the line. A large angle results in a more pronounced curve.
     * An angle of zero results in a straight line.
     *
     * @return The angle that determines the curvature of the line.
     */
    public Angle getCurvature()
    {
        return this.curvature;
    }

    /**
     * Specifies the angle that determines the curvature of the line. A large angle results in a more pronounced curve.
     * An angle of zero results in a straight line.
     *
     * @param angle The angle that determines the curvature of the line.
     */
    public void setCurvature(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.curvature = angle;
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
        // This graphic is composed of four paths:
        // 1) Curve from the start position to the bow tie.
        // 2) Curve from the bow tie to the end position.
        // 3) The arrow head.
        // 4) The bow tie.
        this.paths = new Path[4];

        List<Position> curvePositions = new ArrayList<Position>();

        Globe globe = dc.getGlobe();
        Vec4 p1 = globe.computePointFromLocation(this.startPosition);
        Vec4 p2 = globe.computePointFromLocation(this.endPosition);
        Vec4 t1 = p2.subtract3(p1);

        // Compute the tangent vector by rotating a vector in the direction of the arrow by the curvature arrow.
        Vec4 normal = globe.computeSurfaceNormalAtPoint(p1);
        Matrix rot = Matrix.fromAxisAngle(this.getCurvature(), normal);
        t1 = t1.transformBy3(rot);

        // Invoke the Hermite curve function to compute points along the curve.
        int intervals = this.getIntervals();
        double delta = 1.0 / intervals;
        for (int i = 0; i < intervals; i++)
        {
            double t = i * delta;
            Vec4 p = this.hermiteCurve(p1, p2, t1, t1, t);
            Position pos = globe.computePositionFromPoint(p);
            curvePositions.add(pos);
        }
        curvePositions.add(this.endPosition); // Make sure the curve ends where it should

        // Choose two points to be the start and end of the bow tie.
        int numPositions = curvePositions.size();
        double bowTieScale = this.getBowTieLength();
        int bowTieIndex1 = (int) (numPositions * (0.5 - bowTieScale));
        int bowTieIndex2 = (int) (numPositions * (0.5 + bowTieScale));

        // Make sure the bow tie indices are in the valid.
        bowTieIndex1 = WWMath.clamp(bowTieIndex1, 0, numPositions);
        bowTieIndex2 = WWMath.clamp(bowTieIndex2, 0, numPositions);

        Position bowTiePos1 = curvePositions.get(bowTieIndex1);
        Position bowTiePos2 = curvePositions.get(bowTieIndex2);

        // Create a path for the line parts of the arrow. Divide curvePositions into two paths, one from the start of
        // the arrow to the bow tie, and one from the bow tie to the end of the arrow.
        this.paths[0] = this.createPath(curvePositions.subList(0, bowTieIndex1 + 1));
        this.paths[1] = this.createPath(curvePositions.subList(bowTieIndex2, numPositions));

        // Create the arrowhead.
        double arrowheadLength = p2.subtract3(p1).getLength3() * this.getArrowLength();
        List<Position> positions = this.computeArrowheadPositions(dc, p2, t1.multiply3(-1), arrowheadLength);
        this.paths[2] = this.createPath(positions);

        // Create the bow tie.
        positions = this.createBowTie(dc, bowTiePos1, bowTiePos2);
        this.paths[3] = this.createPath(positions);
    }

    /**
     * Create positions required to to draw the bow tie part of the graphic.
     *
     * @param dc   Current draw context.
     * @param pos1 Position at the center of one side of the bow tie.
     * @param pos2 Position at the center of the other side of the bow tie.
     *
     * @return Positions that describe the bow tie.
     */
    protected List<Position> createBowTie(DrawContext dc, Position pos1, Position pos2)
    {
        //       A     C
        //       |\  /|
        // Pt. 1 | \/ | Pt. 2
        //       | /\ |
        //       |/  \|
        //       B    D

        Globe globe = dc.getGlobe();
        Vec4 pt1 = globe.computePointFromLocation(pos1);
        Vec4 pt2 = globe.computePointFromLocation(pos2);

        Vec4 v12 = pt2.subtract3(pt1);
        double dist = pt1.subtract3(pt2).getLength3() * this.getBowTieWidth();

        Vec4 normal = globe.computeSurfaceNormalAtPoint(pt1);
        Vec4 perpendicular = v12.cross3(normal);
        perpendicular = perpendicular.normalize3().multiply3(dist);

        Vec4 ptA = pt1.add3(perpendicular);
        Vec4 ptB = pt1.subtract3(perpendicular);

        normal = globe.computeSurfaceNormalAtPoint(pt2);
        perpendicular = v12.cross3(normal);
        perpendicular = perpendicular.normalize3().multiply3(dist);

        Vec4 ptC = pt2.add3(perpendicular);
        Vec4 ptD = pt2.subtract3(perpendicular);

        return TacticalGraphicUtil.asPositionList(globe, ptA, ptB, ptC, ptD, ptA);
    }

    /**
     * Compute a point along a Hermite curve defined by two control point and tangent vectors at those points.
     * <p>
     * This function implements the Hermite curve equation from "Mathematics for 3D Game Programming and Computer
     * Graphics, Second Edition" by Eric Lengyel (equation 15.15, pg. 457).
     * <p>
     * H(t) = (1 - 3t<sup>2</sup> + 2t<sup>3</sup>)P<sub>1</sub> + t<sup>2</sup>(3 - 2t)P<sub>2</sub> + t(t -
     * 1)<sup>2</sup>T<sub>1</sub> + t<sup>2</sup>(t - 1)T<sub>2</sub>
     *
     * @param pt1      First control point.
     * @param pt2      Second control point.
     * @param tangent1 Vector tangent to the curve at the first control point.
     * @param tangent2 Vector tangent to the curve at the second control point.
     * @param t        Interpolation parameter in the range [0..1].
     *
     * @return A point along the curve.
     */
    protected Vec4 hermiteCurve(Vec4 pt1, Vec4 pt2, Vec4 tangent1, Vec4 tangent2, double t)
    {
        double c1 = (1 - 3 * t * t + 2 * Math.pow(t, 3));
        double c2 = (3 - 2 * t) * t * t;
        double c3 = t * Math.pow(t - 1, 2);
        double c4 = (t - 1) * t * t;

        return pt1.multiply3(c1)
            .add3(pt2.multiply3(c2))
            .add3(tangent1.multiply3(c3))
            .add3(tangent2.multiply3(c4));
    }

    @Override
    protected void createLabels()
    {
        // This graphic supports only the hostile indicator label.
        if (this.mustShowHostileIndicator())
        {
            this.addLabel(SymbologyConstants.HOSTILE_ENEMY);
        }
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (WWUtil.isEmpty(this.labels) || this.paths == null)
            return;

        Angle angle = LatLon.greatCircleDistance(this.startPosition, this.endPosition);
        double length = angle.radians * dc.getGlobe().getRadius();

        // Position the label 10% of the way along the path.
        Iterable<? extends Position> positions = this.paths[0].getPositions();
        TacticalGraphicUtil.placeLabelsOnPath(dc, positions, this.labels.get(0), null, 0.1 * length);
    }
}
