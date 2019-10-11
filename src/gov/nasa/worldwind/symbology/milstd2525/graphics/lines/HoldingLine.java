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
import gov.nasa.worldwind.symbology.TacticalGraphicLabel;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * This class implements the following graphics:
 * <ul> <li>Holding Line (2.X.2.6.1.2)</li> <li>Bridgehead (2.X.2.6.1.4)</li> </ul>
 * <p>
 * Note: These graphics require three control points. The first two points define the end points of the graphic, and the
 * third point defines the top of an arc that connects the endpoints. The specification for Holding Line and Bridgehead
 * (MIL-STD-2525C pgs 538 and 540) states that "Additional points can be defined to extend the line". However, the
 * specification is unclear as to how additional control points should be interpreted. This implementation ignores
 * additional control points.
 *
 * @author pabercrombie
 * @version $Id: HoldingLine.java 555 2012-04-25 18:59:29Z pabercrombie $
 */
public class HoldingLine extends AbstractMilStd2525TacticalGraphic
{
    /** Default number of intervals used to draw the arc. */
    public final static int DEFAULT_NUM_INTERVALS = 32;
    /** Scale factor that determines the curvature of the corners of the arc. */
    public final static double DEFAULT_CURVATURE = 0.3;

    /** Path used to render the line. */
    protected Path path;

    /**
     * Scale factor that determines the curvature of the corners of the arc. Valid values are zero to one, where zero
     * produced square corners.
     */
    protected double curvature = DEFAULT_CURVATURE;
    /** Number of intervals used to draw the arc. */
    protected int intervals = DEFAULT_NUM_INTERVALS;

    /** First control point, defines the start of the line. */
    protected Position position1;
    /** Second control point, defines the end of the line. */
    protected Position position2;
    /** Third control point, defines the top of the arc. */
    protected Position position3;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_SPL_LNE_HGL,
            TacGrpSidc.C2GM_SPL_LNE_BRGH);
    }

    /**
     * Create a new Holding Line graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public HoldingLine(String sidc)
    {
        super(sidc);
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

        this.path = null; // Need to regenerate
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
     * Indicates a factor that controls the curvatures of the arcs in this graphic.
     *
     * @return Factor that determines arc curvature.
     *
     * @see #setCurvature(double)
     */
    public double getCurvature()
    {
        return this.curvature;
    }

    /**
     * Specifies a factor that determines the curvature of the arcs in this graphic. Valid values are zero to one, where
     * zero creates square corners and one creates extremely rounded corners.
     *
     * @param curvature Factor that determines curvature of the arc.
     */
    public void setCurvature(double curvature)
    {
        if (curvature < 0.0 || curvature > 1.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", curvature);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.curvature = curvature;
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

    protected void onShapeChanged()
    {
        this.path = null; // Need to recompute path
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        if (this.path == null)
        {
            this.createShape(dc);
        }

        this.path.render(dc);
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        if (this.path != null)
            this.path.setDelegateOwner(owner);
    }

    /**
     * Create a Path to render the line.
     *
     * @param dc Current draw context.
     */
    protected void createShape(DrawContext dc)
    {
        Globe globe = dc.getGlobe();

        // The graphic looks like this:
        //
        // Pt. 1 __________  Corner 1
        //                 \
        //                 |
        //                 | Pt. 3
        //                 |
        //                 |
        // Pt. 2 _________/  Corner 2

        Vec4 pt1 = globe.computePointFromLocation(this.position1);
        Vec4 pt2 = globe.computePointFromLocation(this.position2);
        Vec4 pt3 = globe.computePointFromLocation(this.position3);

        LatLon mid = LatLon.interpolateGreatCircle(0.5, this.position1, this.position2);
        Vec4 ptMid = globe.computePointFromLocation(mid);

        Vec4 offset = pt3.subtract3(ptMid);

        double width = pt1.subtract3(pt2).getLength3();
        double length = offset.getLength3();

        // Compute distance from the corners to the ends of the corner arcs by multiplying the curvature factor by the
        // smaller of the width and length dimensions.
        double distance = Math.min(width, length) * this.getCurvature();

        Vec4 ptCorner1 = pt1.add3(offset);
        Vec4 ptCorner2 = pt2.add3(offset);

        // Create list to hold the path positions, and start populating.
        List<Position> positions = new ArrayList<Position>();
        int intervals = this.getIntervals();

        positions.add(this.position1); // Line starts at Pt. 1.
        this.computeRoundCorner(globe, positions, pt1, ptCorner1, pt3, distance, intervals);
        positions.add(this.position3); // Pt. 3 is the top of the graphic.
        this.computeRoundCorner(globe, positions, pt3, ptCorner2, pt2, distance, intervals);
        positions.add(this.position2); // The line ends at Pt. 2.

        this.path = this.createPath();
        this.path.setPositions(positions);
    }

    /**
     * Compute positions to draw a rounded corner between three points.
     *
     * @param globe     Current globe.
     * @param positions Positions will be added to this list.
     * @param ptLeg1    Point at the end of the one leg.
     * @param ptVertex  Point at the vertex of the corner.
     * @param ptLeg2    Point at the end of the other let.
     * @param distance  Distance from the vertex at which the arc should begin and end.
     * @param intervals Number of intervals to use to generate the arc.
     */
    protected void computeRoundCorner(Globe globe, List<Position> positions, Vec4 ptLeg1, Vec4 ptVertex, Vec4 ptLeg2,
        double distance, int intervals)
    {
        Vec4 vertexTo1 = ptLeg1.subtract3(ptVertex);
        Vec4 vertexTo2 = ptLeg2.subtract3(ptVertex);

        // The angle is formed by a vertex and two legs. We'll draw the arc as a segment of a circle inscribed between
        // the legs. We need to find the angle (theta) between the legs. This angle forms the tip of a right triangle
        // with sides r and d where r is the radius our arc, and d is the distance from the vertex to points A and B,
        // where the circle touches the legs.
        //
        //       Leg 1
        //         |     /
        //   Pt. B |____/.Arc center
        //         |   /|
        //         | /  | r
        // Theta ->/____|________ Leg 2
        //  Vertex   d  Pt. A
        //

        Angle theta = vertexTo1.angleBetween3(vertexTo2);
        if (Angle.ZERO.equals(theta))
            return;

        double radius = distance * theta.tanHalfAngle();

        // Find points A and B where the arc will start and end.
        Vec4 ptA = ptVertex.add3(vertexTo1.normalize3().multiply3(distance));
        Vec4 ptB = ptVertex.add3(vertexTo2.normalize3().multiply3(distance));

        // Compute a vector perpendicular to one of the legs and the normal vector.
        Vec4 normal = globe.computeSurfaceNormalAtPoint(ptA);
        Vec4 perpendicular = normal.cross3(vertexTo1);
        Vec4 offset = perpendicular.normalize3().multiply3(radius);

        // Determine which direction the offset points by computing the scalar triple product of the perpendicular
        // vector and the vector from the vertex along leg 2. Reverse the sign of offset if necessary.
        double tripleProduct = perpendicular.dot3(vertexTo2);
        if (tripleProduct < 0)
        {
            offset = offset.multiply3(-1);
        }

        // Find the center point of the arc by adding the offset to Pt. A
        Vec4 ptArcCenter = ptA.add3(offset);

        Position arcCenter = globe.computePositionFromPoint(ptArcCenter);
        Position posA = globe.computePositionFromPoint(ptA);
        Position posB = globe.computePositionFromPoint(ptB);

        // Compute the arc from A to B
        this.computeArc(globe, positions, arcCenter,
            LatLon.greatCircleAzimuth(arcCenter, posA),
            LatLon.greatCircleAzimuth(arcCenter, posB),
            radius, intervals);
    }

    /**
     * Compute the positions required to draw an arc.
     *
     * @param globe        Current globe.
     * @param positions    Add arc positions to this list.
     * @param center       Center point of the arc.
     * @param startAzimuth Starting azimuth.
     * @param endAzimuth   Ending azimuth.
     * @param radius       Radius of the arc, in meters.
     * @param intervals    Number of intervals to generate.
     */
    protected void computeArc(Globe globe, List<Position> positions, Position center, Angle startAzimuth,
        Angle endAzimuth, double radius, int intervals)
    {
        // Compute the sweep between the start and end positions, and normalize to the range [-180, 180].
        Angle sweep = endAzimuth.subtract(startAzimuth).normalizedLongitude();

        Angle da = sweep.divide(intervals);
        double globeRadius = globe.getRadiusAt(center.getLatitude(), center.getLongitude());
        double radiusRadians = radius / globeRadius;

        // Compute the arc positions
        for (int i = 0; i < intervals; i++)
        {
            double angle = i * da.radians + startAzimuth.radians;

            LatLon ll = LatLon.greatCircleEndPosition(center, angle, radiusRadians);
            positions.add(new Position(ll, 0));
        }
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        String text = this.getGraphicLabel();

        this.addLabel(text); // Start label
        this.addLabel(text); // End label
    }

    protected String getGraphicLabel()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("PL ");

        String text = this.getText();
        if (!WWUtil.isEmpty(text))
            sb.append(text);

        if (TacGrpSidc.C2GM_SPL_LNE_HGL.equalsIgnoreCase(this.maskedSymbolCode))
            sb.append("\n(HOLDING LINE)");
        else if (TacGrpSidc.C2GM_SPL_LNE_BRGH.equalsIgnoreCase(this.maskedSymbolCode))
            sb.append("\n(BRIDGEHEAD LINE)");

        return sb.toString();
    }

    /**
     * Determine positions for the start and end labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (WWUtil.isEmpty(labels))
            return;

        TacticalGraphicLabel startLabel = this.labels.get(0);
        TacticalGraphicLabel endLabel = this.labels.get(1);

        startLabel.setPosition(this.position1);
        endLabel.setPosition(this.position2);
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @return New path configured with defaults appropriate for this type of graphic.
     */
    protected Path createPath()
    {
        Path path = new Path();
        path.setSurfacePath(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
