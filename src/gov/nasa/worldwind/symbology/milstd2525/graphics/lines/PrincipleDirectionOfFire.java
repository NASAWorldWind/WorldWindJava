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
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Implementation of the Principle Direction of Fire graphic (2.X.2.4.2.2).
 *
 * @author pabercrombie
 * @version $Id: PrincipleDirectionOfFire.java 560 2012-04-26 16:28:24Z pabercrombie $
 */
public class PrincipleDirectionOfFire extends AbstractMilStd2525TacticalGraphic implements PreRenderable
{
    /** Default length of the arrowhead, as a fraction of the total line length. */
    public final static double DEFAULT_ARROWHEAD_LENGTH = 0.1;
    /** Default angle of the arrowhead. */
    public final static Angle DEFAULT_ARROWHEAD_ANGLE = Angle.fromDegrees(60.0);
    /** Default width of the arrowhead outline. */
    public final static double DEFAULT_ARROWHEAD_OUTLINE_WIDTH = 0.3;

    /** Length of the arrowhead from base to tip, as a fraction of the total line length. */
    protected Angle arrowAngle = DEFAULT_ARROWHEAD_ANGLE;
    /** Angle of the arrowhead. */
    protected double arrowLength = DEFAULT_ARROWHEAD_LENGTH;
    /** Width of the arrowhead outline, as a fraction of the arrowhead length. */
    protected double outlineWidth = DEFAULT_ARROWHEAD_OUTLINE_WIDTH;

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

    /** Polygon used to render the "thick" line on the left leg of the graphic. */
    protected SurfacePolygon thickLine;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_DEF_LNE_PDF);
    }

    /**
     * Create a new arrow graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public PrincipleDirectionOfFire(String sidc)
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
        this.thickLine = null;

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

        if (this.thickLine != null)
            this.thickLine.preRender(dc);
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        for (Path path : this.paths)
        {
            path.render(dc);
        }

        if (this.thickLine != null)
        {
            this.thickLine.render(dc);
        }
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

        if (this.thickLine != null)
            this.thickLine.setDelegateOwner(owner);
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        this.addLabel("(PDF)");
    }

    /**
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        this.paths = new Path[4];

        int i = 0;

        // Create a path for the line part of the arrow
        this.paths[i++] = this.createPath(Arrays.asList(this.position1, this.position2));
        this.paths[i++] = this.createPath(Arrays.asList(this.position1, this.position3));

        // Create the arrowheads
        List<Position> positions = this.computeArrowheadPositions(dc, this.position1, this.position2);
        this.paths[i++] = createPath(positions);
        positions = this.computeArrowheadPositions(dc, this.position1, this.position3);
        this.paths[i] = createPath(positions);

        // Create polygon to render the "thick" line on the leg of the graphic between positions 1 and 2.
        LatLon loc1 = LatLon.interpolateGreatCircle(0.25, this.position1, this.position2);
        LatLon loc2 = LatLon.interpolateGreatCircle(0.75, this.position1, this.position2);

        Angle azimuth = LatLon.greatCircleAzimuth(this.position1, this.position2);

        Angle azimuth2 = LatLon.greatCircleAzimuth(this.position1, this.position3);
        if (azimuth.compareTo(azimuth2) > 0)
            azimuth = azimuth.add(Angle.NEG90);
        else
            azimuth = azimuth.add(Angle.POS90);

        Angle distance = LatLon.greatCircleDistance(this.position1, this.position2);
        LatLon loc3 = LatLon.greatCircleEndPosition(loc1, azimuth, distance.multiply(0.01));
        LatLon loc4 = LatLon.greatCircleEndPosition(loc2, azimuth, distance.multiply(0.01));

        this.thickLine = this.createPolygon(Arrays.asList(loc1, loc2, loc4, loc3));
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

    /** {@inheritDoc} */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (WWUtil.isEmpty(this.labels))
            return;

        Angle azimuth = LatLon.greatCircleAzimuth(this.position1, this.position2);
        Angle distance = LatLon.greatCircleDistance(this.position1, this.position2);
        Angle pathLength = Angle.fromDegrees(1.1 * distance.degrees);

        LatLon ll = LatLon.greatCircleEndPosition(this.position1, azimuth, pathLength);
        this.labels.get(0).setPosition(new Position(ll, 0));
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
