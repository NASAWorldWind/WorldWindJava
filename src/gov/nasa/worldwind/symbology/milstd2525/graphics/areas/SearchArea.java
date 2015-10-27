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
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Search Area/Reconnaissance Area graphic (2.X.2.1.3.9).
 *
 * @author pabercrombie
 * @version $Id: SearchArea.java 560 2012-04-26 16:28:24Z pabercrombie $
 */
public class SearchArea extends AbstractMilStd2525TacticalGraphic implements PreRenderable
{
    /** Default length of the arrowhead, as a fraction of the total line length. */
    public final static double DEFAULT_ARROWHEAD_LENGTH = 0.1;
    /** Default angle of the arrowhead. */
    public final static Angle DEFAULT_ARROWHEAD_ANGLE = Angle.fromDegrees(60.0);

    /** Length of the arrowhead from base to tip, as a fraction of the total line length. */
    protected Angle arrowAngle = DEFAULT_ARROWHEAD_ANGLE;
    /** Angle of the arrowhead. */
    protected double arrowLength = DEFAULT_ARROWHEAD_LENGTH;

    /** First control point. */
    protected Position position1;
    /** Second control point. */
    protected Position position2;
    /** Third control point. */
    protected Position position3;

    /** Path used to render the line. */
    protected Path[] paths;
    /** Symbol drawn at the center of the range fan. */
    protected TacticalSymbol symbol;
    /** Attributes applied to the symbol. */
    protected TacticalSymbolAttributes symbolAttributes;

    /** Polygon render an arrow head at the end of one of the graphic's legs. */
    protected SurfacePolygon arrowHead2;
    /** Polygon render an arrow head at the end of one of the graphic's legs. */
    protected SurfacePolygon arrowHead1;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_GNL_ARS_SRHARA);
    }

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public SearchArea(String sidc)
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
     * Indicates a symbol drawn at the center of the range fan.
     *
     * @return The symbol drawn at the center of the range fan. May be null.
     */
    public String getSymbol()
    {
        return this.symbol != null ? this.symbol.getIdentifier() : null;
    }

    /**
     * Specifies a symbol to draw at the center of the range fan. Equivalent to setting the {@link
     * SymbologyConstants#SYMBOL_INDICATOR} modifier. The symbol's position will be changed to match the range fan
     * center position.
     *
     * @param sidc Identifier for a MIL-STD-2525C symbol to draw at the center of the range fan. May be null to indicate
     *             that no symbol is drawn.
     */
    public void setSymbol(String sidc)
    {
        if (sidc != null)
        {
            if (this.symbolAttributes == null)
                this.symbolAttributes = new BasicTacticalSymbolAttributes();

            this.symbol = this.createSymbol(sidc, this.position1, this.symbolAttributes);
        }
        else
        {
            // Null value indicates no symbol.
            this.symbol = null;
            this.symbolAttributes = null;
        }
        this.onModifierChanged();
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
        this.arrowHead1 = null;
        this.arrowHead2 = null;

        if (this.symbol != null)
        {
            this.symbol.setPosition(this.position1);
        }
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
    @Override
    @SuppressWarnings("unchecked")
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier) && value instanceof String)
        {
            this.setSymbol((String) value);
        }
        else
        {
            super.setModifier(modifier, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getModifier(String modifier)
    {
        if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier))
        {
            return this.getSymbol();
        }
        else
        {
            return super.getModifier(modifier);
        }
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        if (this.paths == null)
        {
            this.createShapes(dc);
        }

        this.determinePerFrameAttributes(dc);

        this.arrowHead1.preRender(dc);
        this.arrowHead2.preRender(dc);
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        for (Path path : this.paths)
        {
            path.render(dc);
        }

        this.arrowHead1.render(dc);
        this.arrowHead2.render(dc);
    }

    /** {@inheritDoc} */
    @Override
    protected void doRenderGraphicModifiers(DrawContext dc)
    {
        super.doRenderGraphicModifiers(dc);

        if (this.symbol != null)
        {
            this.symbol.render(dc);
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

        if (this.symbol != null)
            this.symbol.setDelegateOwner(owner);

        this.arrowHead1.setDelegateOwner(owner);
        this.arrowHead2.setDelegateOwner(owner);
    }

    /**
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        this.paths = new Path[2];

        int i = 0;

        Angle azimuth1 = LatLon.greatCircleAzimuth(this.position1, this.position2);
        Angle azimuth2 = LatLon.greatCircleAzimuth(this.position1, this.position3);

        Angle delta = azimuth2.subtract(azimuth1);
        int sign = delta.degrees > 0 ? 1 : -1;

        delta = Angle.fromDegrees(sign * 5.0);

        // Create a path for the line part of the arrow
        List<Position> positions = this.computePathPositions(this.position1, this.position2, delta);
        this.paths[i++] = this.createPath(positions);

        // Create a polygon to draw the arrow head.
        double arrowLength = this.getArrowLength();
        Angle arrowAngle = this.getArrowAngle();
        positions = this.computeArrowheadPositions(dc, positions.get(2), positions.get(3), arrowLength, arrowAngle);
        this.arrowHead1 = this.createPolygon(positions);
        this.arrowHead1.setLocations(positions);

        delta = delta.multiply(-1.0);
        positions = this.computePathPositions(this.position1, this.position3, delta);
        this.paths[i] = this.createPath(positions);

        positions = this.computeArrowheadPositions(dc, positions.get(2), positions.get(3), arrowLength, arrowAngle);
        this.arrowHead2 = this.createPolygon(positions);
        this.arrowHead2.setLocations(positions);
    }

    protected List<Position> computePathPositions(Position startPosition, Position endPosition, Angle delta)
    {
        Angle dist = LatLon.greatCircleDistance(startPosition, endPosition);
        dist = dist.multiply(0.6);

        Angle azimuth = LatLon.greatCircleAzimuth(startPosition, endPosition);

        LatLon locA = LatLon.greatCircleEndPosition(startPosition, azimuth.add(delta), dist);

        dist = dist.multiply(0.9);
        LatLon locB = LatLon.greatCircleEndPosition(startPosition, azimuth.subtract(delta), dist);

        return Arrays.asList(startPosition, new Position(locA, 0), new Position(locB, 0), endPosition);
    }

    /**
     * Compute the positions of the arrow head of the graphic's legs.
     *
     * @param dc          Current draw context
     * @param base        Position of the arrow's starting point.
     * @param tip         Position of the arrow head tip.
     * @param arrowLength Length of the arrowhead as a fraction of the total line length.
     * @param arrowAngle  Angle of the arrow head.
     *
     * @return Positions required to draw the arrow head.
     */
    protected List<Position> computeArrowheadPositions(DrawContext dc, Position base, Position tip, double arrowLength,
        Angle arrowAngle)
    {
        // Build a triangle to represent the arrowhead. The triangle is built from two vectors, one parallel to the
        // segment, and one perpendicular to it.

        Globe globe = dc.getGlobe();

        Vec4 ptA = globe.computePointFromPosition(base);
        Vec4 ptB = globe.computePointFromPosition(tip);

        // Compute parallel component
        Vec4 parallel = ptA.subtract3(ptB);

        Vec4 surfaceNormal = globe.computeSurfaceNormalAtPoint(ptB);

        // Compute perpendicular component
        Vec4 perpendicular = surfaceNormal.cross3(parallel);

        double finalArrowLength = arrowLength * parallel.getLength3();
        double arrowHalfWidth = finalArrowLength * arrowAngle.tanHalfAngle();

        perpendicular = perpendicular.normalize3().multiply3(arrowHalfWidth);
        parallel = parallel.normalize3().multiply3(finalArrowLength);

        // Compute geometry of direction arrow
        Vec4 vertex1 = ptB.add3(parallel).add3(perpendicular);
        Vec4 vertex2 = ptB.add3(parallel).subtract3(perpendicular);

        return TacticalGraphicUtil.asPositionList(globe, vertex1, vertex2, ptB);
    }

    /**
     * Determine the positions that make up the arrowhead.
     *
     * @param dc            Current draw context.
     * @param startPosition Position of the arrow's base.
     * @param endPosition   Position of the arrow head tip.
     *
     * @return Positions that define the arrowhead.
     */
    protected List<Position> computeArrowheadPositions(DrawContext dc, Position startPosition, Position endPosition)
    {
        Globe globe = dc.getGlobe();

        // Arrowhead looks like this:
        //                  _
        //        A\         | 1/2 width
        // ________B\       _|
        // Pt. 1    /
        //        C/
        //         | |
        //      Length

        Vec4 p1 = globe.computePointFromPosition(startPosition);
        Vec4 pB = globe.computePointFromPosition(endPosition);

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

    /** {@inheritDoc} */
    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        // Enable the polygon interior for the "thick line" polygon. All other parts of the graphic are drawn with Path,
        // so the draw interior setting will not affect them.
        attributes.setDrawInterior(true);
    }

    /** {@inheritDoc} Overridden to update symbol attributes. */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // Apply active attributes to the symbol.
        if (this.symbolAttributes != null)
        {
            ShapeAttributes activeAttributes = this.getActiveShapeAttributes();
            this.symbolAttributes.setOpacity(activeAttributes.getInteriorOpacity());
            this.symbolAttributes.setScale(this.activeOverrides.getScale());
        }
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

    protected SurfacePolygon createPolygon(List<? extends LatLon> positions)
    {
        SurfacePolygon polygon = new SurfacePolygon(positions);
        polygon.setAttributes(this.getActiveShapeAttributes());
        return polygon;
    }
}
