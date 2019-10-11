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
 * Implementation of the Ambush graphic (2.X.2.6.1.1).
 *
 * @author pabercrombie
 * @version $Id: Ambush.java 1585 2013-09-06 00:12:52Z pabercrombie $
 */
public class Ambush extends AbstractMilStd2525TacticalGraphic
{
    /** Default length of the arrowhead, as a fraction of the total line length. */
    public final static double DEFAULT_ARROWHEAD_LENGTH = 0.2;
    /**
     * Default angle of the arc. Control points 2 and 3 define a chord of a circle. This chord and the arc angle fully
     * define the circle, of which the arc is a segment.
     */
    public final static Angle DEFAULT_ARC_ANGLE = Angle.fromDegrees(60.0);
    /** Default angle of the arrowhead. */
    public final static Angle DEFAULT_ARROWHEAD_ANGLE = Angle.fromDegrees(70.0);
    /**
     * Default length of the legs of the graphic's base, as a fraction of the distance between the control points the
     * define the base.
     */
    public final static double DEFAULT_LEG_LENGTH = 0.5;

    /** Default number of intervals used to draw the arc. */
    public final static int DEFAULT_NUM_INTERVALS = 32;
    /** Default number of legs to draw on the graphic's arc. */
    public final static int DEFAULT_NUM_LEGS = 6;

    /** Number of intervals used to draw the arc. */
    protected int intervals = DEFAULT_NUM_INTERVALS;
    /** The arc is drawn as a segment of a circle intersected by this angle. */
    protected Angle arcAngle = DEFAULT_ARC_ANGLE;
    /** Length of the arrowhead from base to tip, as a fraction of the total line length. */
    protected Angle arrowAngle = DEFAULT_ARROWHEAD_ANGLE;
    /** Angle of the arrowhead. */
    protected double arrowLength = DEFAULT_ARROWHEAD_LENGTH;
    /** Number of "legs" drawn on this graphic's arc. */
    protected int numLegs = DEFAULT_NUM_LEGS;
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
     * Data required for intermediate calculations while generating the Ambush graphic. This object is created and
     * populated by {@link Ambush#computeArc(gov.nasa.worldwind.render.DrawContext)}.
     */
    protected static class ArcData
    {
        /** Position at the midpoint of the arc. This point falls on the arc. */
        Position midpoint;
        /** Center of the circle, of which the arc is segment. */
        LatLon center;
        /** Radius of the circle. */
        double radius;
        /** Angle from North at which the arc begins. */
        Angle startAngle;
        /**
         * Angular length of the arc. Note that this is different than {@link Ambush#arcAngle}: this angle is signed,
         * where as Ambush.arcAngle is unsigned. This angle is calculated such that the end angle of the arc is equal to
         * {@code arcData.arcAngle + arcData.startAngle}.
         */
        Angle arcAngle;
        /** Direction of the arc. This vector points from the center of the circle to the midpoint of the arc. */
        Vec4 direction;
    }

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_SPL_LNE_AMB);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public Ambush(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates the number of intervals used to draw the arc in this graphic.
     *
     * @return Intervals used to draw arc.
     */
    public int getIntervals()
    {
        return this.intervals;
    }

    /**
     * Specifies the number of intervals used to draw the arc in this graphic. More intervals will result in a smoother
     * looking arc.
     *
     * @param intervals Number of intervals for drawing the arc.
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
     * Indicates the angle of this graphic's arc. The arc is drawn as a segment of a circle defined by this angle and
     * the chord formed by control points 2 and 3.
     *
     * @return Angle of the circular segment that forms this graphic's arc.
     */
    public Angle getArcAngle()
    {
        return this.arcAngle;
    }

    /**
     * Specifies the angle of this graphic's arc. The arc is drawn as a segment of a circle defined by this angle and
     * the chord formed by control points 2 and 3. A greater angle makes the arc more curved and a smaller angle makes
     * the arc less curved.
     *
     * @param arcAngle Angle of the circular segment that forms this graphic's arc.
     */
    public void setArcAngle(Angle arcAngle)
    {
        if (arcAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.arcAngle = arcAngle;
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
        this.onShapeChanged();
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
        this.onShapeChanged();
    }

    /**
     * Indicates how many "legs" stick out of the back side of this graphic's arc.
     *
     * @return Number of legs drawn on the arc of this graphic.
     */
    public int getLegs()
    {
        return this.numLegs;
    }

    /**
     * Specifies how many "legs" stick out of the back side of this graphic's arc.
     *
     * @param numLegs Number of legs to draw on the arc of this graphic.
     */
    public void setLegs(int numLegs)
    {
        if (numLegs < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.numLegs = numLegs;
        this.onShapeChanged();
    }

    /**
     * Indicates the length of legs on the graphic's base.
     *
     * @return The length of the legs on the base, as a fraction of the distance between the control points that define
     *         the base.
     */
    public double getLegLength()
    {
        return this.legLength;
    }

    /**
     * Specifies the length of the legs on the graphic's base.
     *
     * @param legLength Length of the legs on the graphic's base, as a fraction of the distance between the control
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
        this.onShapeChanged();
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

        this.onShapeChanged();
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

    protected void onShapeChanged()
    {
        this.paths = null; // Need to recompute paths
    }

    /**
     * Create the paths required to draw the graphic.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        // This graphic requires three paths, plus one path for each of the legs.
        this.paths = new Path[3 + this.getLegs()];

        ArcData arcData = this.computeArc(dc);

        // Create a path for the arc.
        List<Position> positions = this.computeArcPositions(dc, arcData);
        this.paths[0] = this.createPath(positions);

        // Create a path for the line part of the arrow
        this.paths[1] = this.createPath(Arrays.asList(arcData.midpoint, this.position1));

        // Create a path for the arrow head
        positions = this.computeArrowheadPositions(dc, this.position1, arcData);
        this.paths[2] = this.createPath(positions);

        // Create the legs. The legs will be placed in the paths array starting at index 3.
        this.createLegs(dc, arcData, this.paths, 3, this.getLegs());
    }

    /**
     * Compute the arc. The arc is a segment of a circle defined by a chord and the arc angle. Control points 2 and 3
     * define the chord.
     *
     * @param dc Current draw context.
     *
     * @return Data that describes the arc.
     */
    protected ArcData computeArc(DrawContext dc)
    {
        Globe globe = dc.getGlobe();

        // The graphic looks like this:
        //
        //   A \
        //      \
        //   Mid |______\
        //       |      /
        //      /
        //   B /

        Vec4 pA = globe.computePointFromPosition(this.position2);
        Vec4 pB = globe.computePointFromPosition(this.position3);

        Position baseMidpoint = new Position(LatLon.interpolate(0.5, this.position2, this.position3), 0);
        Vec4 pMid = globe.computePointFromPosition(baseMidpoint);

        // Compute a vector perpendicular to the segment AB and the normal vector
        Vec4 vAB = pB.subtract3(pA);
        Vec4 normal = globe.computeSurfaceNormalAtPoint(pMid);
        Vec4 perpendicular = vAB.cross3(normal).normalize3();

        ArcData arcData = new ArcData();

        double chordLength = pA.distanceTo3(pB);
        Angle arcAngle = this.getArcAngle();

        // The arc is drawn as a segment of a circle defined by a chord and the arc angle. Control points 2 and 3 define
        // the chord. Compute the radius of the circle, and the distance from the center of the circle to the chord from
        // this information. See http://mathworld.wolfram.com/CircularSegment.html and
        // http://en.wikipedia.org/wiki/Circular_segment for background on these formulas.
        arcData.radius = chordLength / (2 * arcAngle.sinHalfAngle());
        double dist = arcData.radius * arcAngle.cosHalfAngle();

        // Determine which side of the line AB the arrow point falls on. We want the arc face away from the arrow, so
        // set the direction of the perpendicular component according to the sign of the scalar triple product.
        Vec4 vOrientation = globe.computePointFromPosition(this.position1).subtract3(pMid);
        double tripleProduct = perpendicular.dot3(vOrientation);
        double sign = (tripleProduct > 0) ? -1 : 1;

        arcData.direction = perpendicular.multiply3(sign);

        // Find the center point of the circle
        Vec4 pCenter = pMid.add3(arcData.direction.multiply3(dist));
        arcData.center = globe.computePositionFromPoint(pCenter);

        // Compute the start and sweep angles for the arc
        arcData.startAngle = LatLon.greatCircleAzimuth(arcData.center, this.position2);
        Angle endAngle = LatLon.greatCircleAzimuth(arcData.center, this.position3);

        // Compute the angle between the start and end points. Note that we cannot use Angle.angularDistance because
        // we need a signed distance here.
        double diffDegrees = endAngle.subtract(arcData.startAngle).degrees;
        if (diffDegrees < -180)
            diffDegrees += 360;
        else if (diffDegrees > 180)
            diffDegrees -= 360;

        arcData.arcAngle = Angle.fromDegrees(diffDegrees);

        // Find the midpoint of the arc
        double globeRadius = globe.getRadiusAt(arcData.center.getLatitude(), arcData.center.getLongitude());
        LatLon ll = LatLon.greatCircleEndPosition(arcData.center,
            arcData.arcAngle.divide(2.0).radians + arcData.startAngle.radians, arcData.radius / globeRadius);
        arcData.midpoint = new Position(ll, 0);

        return arcData;
    }

    /**
     * Compute positions required to draw the arc.
     *
     * @param dc      Current draw context.
     * @param arcData Data that describes the arc.
     *
     * @return Positions along the arc.
     */
    protected List<Position> computeArcPositions(DrawContext dc, ArcData arcData)
    {
        Globe globe = dc.getGlobe();

        Angle da = arcData.arcAngle.divide(this.intervals);
        double globeRadius = globe.getRadiusAt(arcData.center.getLatitude(), arcData.center.getLongitude());
        double radiusRadians = arcData.radius / globeRadius;

        // Compute the arc positions
        int intervals = this.getIntervals();
        List<Position> positions = new ArrayList<Position>(intervals);

        for (int i = 0; i < intervals; i++)
        {
            double angle = i * da.radians + arcData.startAngle.radians;

            LatLon ll = LatLon.greatCircleEndPosition(arcData.center, angle, radiusRadians);
            positions.add(new Position(ll, 0));
        }
        positions.add(this.position3); // Control point 3 is the end of the arc

        return positions;
    }

    /**
     * Create paths for the graphic's "legs". The legs stick out of the back side of the arc.
     *
     * @param dc         Current draw context.
     * @param arcData    Data that describes the graphic's arc.
     * @param paths      Array to receive the new paths.
     * @param startIndex Index into {@code paths} at which to place the first leg path.
     * @param pathCount  Number of leg paths to create. The {@code paths} array must have length of at least {@code
     *                   startIndex + pathCount}.
     */
    protected void createLegs(DrawContext dc, ArcData arcData, Path[] paths, int startIndex, int pathCount)
    {
        Globe globe = dc.getGlobe();

        Vec4 p1 = globe.computePointFromPosition(this.position1);
        Vec4 pMid = globe.computePointFromLocation(arcData.midpoint);

        //     __\
        //     ___\
        //     ____|Mid___\ Pt. 1
        //     ____|      /
        //     ___/
        //       /
        //  ^ Legs^

        // The end point of each leg will be computed by adding an offset in the direction of the arrow to a point on
        // the arc.
        Vec4 vOffset = pMid.subtract3(p1);
        vOffset = vOffset.normalize3().multiply3(vOffset.getLength3() * this.getLegLength());

        Angle da = arcData.arcAngle.divide(pathCount);
        double globeRadius = globe.getRadiusAt(arcData.center.getLatitude(), arcData.center.getLongitude());
        double radiusRadians = arcData.radius / globeRadius;

        for (int i = 0; i < pathCount; i++)
        {
            double angle = (i + 0.5) * da.radians + arcData.startAngle.radians;

            LatLon ll = LatLon.greatCircleEndPosition(arcData.center, angle, radiusRadians);

            Vec4 start = globe.computePointFromLocation(ll);
            Vec4 end = start.add3(vOffset);

            paths[startIndex + i] = this.createPath(TacticalGraphicUtil.asPositionList(globe, start, end));
        }
    }

    /**
     * Determine the positions that make up the arrowhead.
     *
     * @param dc      Current draw context.
     * @param tip     Position of the arrow head tip.
     * @param arcData Data that describes the arc of this graphic.
     *
     * @return Positions that define the arrowhead.
     */
    protected List<Position> computeArrowheadPositions(DrawContext dc, Position tip, ArcData arcData)
    {
        Globe globe = dc.getGlobe();
        //             _
        //        A\    | 1/2 width
        // ________B\  _|
        //          /
        //       C/
        //        | |
        //       Length

        Vec4 pB = globe.computePointFromPosition(tip);

        double baseLength = LatLon.greatCircleDistance(arcData.midpoint, tip).radians * globe.getRadius();
        double arrowLength = baseLength * this.getArrowLength();

        // Find the point at the base of the arrowhead
        Vec4 arrowBase = pB.add3(arcData.direction.normalize3().multiply3(arrowLength));

        Vec4 normal = globe.computeSurfaceNormalAtPoint(arrowBase);

        // Compute the length of the arrowhead
        double arrowHalfWidth = arrowLength * this.getArrowAngle().tanHalfAngle();

        // Compute a vector perpendicular to the segment and the normal vector
        Vec4 perpendicular = arcData.direction.cross3(normal);
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
        path.setSurfacePath(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
