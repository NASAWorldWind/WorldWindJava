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
import gov.nasa.worldwind.util.*;

import java.text.*;
import java.util.*;

/**
 * Implementation of the Sector Weapon/Sensor Range Fans graphic (2.X.4.3.4.2). The range fans are defined by a center
 * position, list of radii, and list of left and right azimuths. If no azimuths are specified, the fans will be drawn as
 * full circles.
 *
 * @author pabercrombie
 * @version $Id: SectorRangeFan.java 1055 2013-01-01 17:09:32Z dcollins $
 */
public class SectorRangeFan extends AbstractMilStd2525TacticalGraphic implements PreRenderable
{
    /** Default number of intervals used to draw each arcs. */
    public final static int DEFAULT_NUM_INTERVALS = 32;

    /** Default length of the Center Of Sector line, as a fraction of the final range fan radius. */
    public final static double DEFAULT_CENTER_OF_SECTOR_LENGTH = 1.2;
    /** Default length of the arrowhead, as a fraction of the Center Of Sector line length. */
    public final static double DEFAULT_ARROWHEAD_LENGTH = 0.05;
    /** Default angle of the arrowhead. */
    public final static Angle DEFAULT_ARROWHEAD_ANGLE = Angle.fromDegrees(60.0);
    /**
     * Azimuth labels are placed to the side of the line that they label. This value specifies a percentage that is
     * multiplied by the maximum radius in the range fan to compute a distance for this offset.
     */
    protected final static double AZIMUTH_LABEL_OFFSET = 0.03;

    /** Default number format used to create azimuth and radius labels. */
    public final static NumberFormat DEFAULT_NUMBER_FORMAT = new DecimalFormat("#");

    /** Length of the arrowhead from base to tip, as a fraction of the Center Of Sector line length. */
    protected Angle arrowAngle = DEFAULT_ARROWHEAD_ANGLE;
    /** Angle of the arrowhead. */
    protected double arrowLength = DEFAULT_ARROWHEAD_LENGTH;
    /** Length of the Center Of Sector line, as a fraction of the last range fan radius. */
    protected double centerOfSectorLength = DEFAULT_CENTER_OF_SECTOR_LENGTH;
    /** Number of intervals used to draw each arcs. */
    protected int intervals = DEFAULT_NUM_INTERVALS;

    /** Number format used to create azimuth labels. */
    protected NumberFormat azimuthFormat = DEFAULT_NUMBER_FORMAT;
    /** Number format used to create radius labels. */
    protected NumberFormat radiusFormat = DEFAULT_NUMBER_FORMAT;

    /** Position of the center of the range fan. */
    protected Position position;
    /** Rings that make up the range fan. */
    protected List<Path> paths;
    /** Polygon to draw a filled arrow head on the Center Of Sector line. */
    protected SurfacePolygon arrowHead;
    /** Symbol drawn at the center of the range fan. */
    protected TacticalSymbol symbol;
    /** Attributes applied to the symbol. */
    protected TacticalSymbolAttributes symbolAttributes;

    /** Radii of the range fans, in meters. */
    protected Iterable<Double> radii;
    /** Azimuths of the range fans. The azimuths are specified in pairs, first the left azimuth, then the right azimuth. */
    protected Iterable<? extends Angle> azimuths;
    /** Altitudes of the range fans. */
    protected Iterable<String> altitudes;

    /** Azimuth of the Center Of Sector arrow. */
    protected Angle centerAzimuth;
    /** Maximum radius in the range fan. */
    protected double maxRadius;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_WPNRF_SCR);
    }

    /**
     * Create the range fan.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public SectorRangeFan(String sidc)
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
     * Indicates the length of the Center Of Sector line.
     *
     * @return The length of the Center Of Sector as a fraction of the final range fan radius.
     */
    public double getCenterOfSectorLength()
    {
        return this.centerOfSectorLength;
    }

    /**
     * Specifies the length of the Center Of Sector line.
     *
     * @param centerOfSectorLength Length of the Center Of Sector arrow as a fraction of the final range fan radius.
     */
    public void setCenterOfSector(double centerOfSectorLength)
    {
        if (centerOfSectorLength < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.centerOfSectorLength = centerOfSectorLength;
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
        this.reset();
    }

    /**
     * Indicates the number format applied to azimuth values to create the azimuth labels.
     *
     * @return NumberFormat used to create azimuth labels.
     */
    public NumberFormat getAzimuthFormat()
    {
        return this.azimuthFormat;
    }

    /**
     * Specifies the number format applied to azimuth values to create the azimuth labels.
     *
     * @param azimuthFormat NumberFormat to create azimuth labels.
     */
    public void setAzimuthFormat(NumberFormat azimuthFormat)
    {
        if (azimuthFormat == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.azimuthFormat = azimuthFormat;
    }

    /**
     * Indicates the number format applied to radius values to create the radius labels.
     *
     * @return NumberFormat used to create radius labels.
     */
    public NumberFormat getRadiusFormat()
    {
        return this.radiusFormat;
    }

    /**
     * Specifies the number format applied to radius values to create the radius labels.
     *
     * @param radiusFormat NumberFormat to create radius labels.
     */
    public void setRadiusFormat(NumberFormat radiusFormat)
    {
        if (radiusFormat == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.radiusFormat = radiusFormat;
    }

    /**
     * Indicates the center position of the range ran.
     *
     * @return The range fan center position.
     */
    public Position getPosition()
    {
        return this.getReferencePosition();
    }

    /**
     * Specifies the center position of the range ran.
     *
     * @param position The new center position.
     */
    public void setPosition(Position position)
    {
        this.moveTo(position);
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points. This graphic uses only one control point, which determines the center of the
     *                  circle.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterator<? extends Position> iterator = positions.iterator();
        if (!iterator.hasNext())
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = iterator.next();
        this.reset();

        if (this.symbol != null)
            this.symbol.setPosition(this.position);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.DISTANCE.equals(modifier))
        {
            if (value instanceof Iterable)
            {
                this.setRadii((Iterable) value);
            }
            else if (value instanceof Double)
            {
                this.setRadii(Arrays.asList((Double) value));
            }
        }
        else if (SymbologyConstants.AZIMUTH.equals(modifier))
        {
            if (value instanceof Iterable)
            {
                // Store the Iterable in an unnecessary variable to suppress Java 7 compiler warnings on Windows.
                Iterable<? extends Angle> iterable = (Iterable<? extends Angle>) value;
                this.setAzimuths(iterable);
            }
            else if (value instanceof Angle)
            {
                this.setAzimuths(Arrays.asList((Angle) value));
            }
        }
        else if (SymbologyConstants.ALTITUDE_DEPTH.equals(modifier))
        {
            if (value instanceof Iterable)
            {
                // Store the Iterable in an unnecessary variable to suppress Java 7 compiler warnings on Windows.
                Iterable<String> iterable = (Iterable<String>) value;
                this.setAltitudes(iterable);
            }
            else if (value != null)
            {
                this.setAltitudes(Arrays.asList(value.toString()));
            }
        }
        else if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier) && value instanceof String)
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
        if (SymbologyConstants.DISTANCE.equals(modifier))
            return this.getRadii();
        else if (SymbologyConstants.AZIMUTH.equals(modifier))
            return this.getAzimuths();
        else if (SymbologyConstants.ALTITUDE_DEPTH.equals(modifier))
            return this.getAltitudes();
        else if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier))
            return this.getSymbol();
        else
            return super.getModifier(modifier);
    }

    /**
     * Indicates the radii of the rings that make up the range fan.
     *
     * @return List of radii, in meters. This method never returns null. If there are no rings this returns an empty
     *         list.
     */
    public Iterable<Double> getRadii()
    {
        if (this.radii != null)
            return this.radii;
        return Collections.emptyList();
    }

    /**
     * Specifies the radii of the rings that make up the range fan.
     *
     * @param radii List of radii, in meters. A circle will be created for each radius.
     */
    public void setRadii(Iterable<Double> radii)
    {
        this.radii = radii;
        this.onModifierChanged();
        this.reset();
    }

    /**
     * Indicates the left and right azimuths of the fans in this graphic. The list contains pairs of azimuths, first
     * left and then right.
     *
     * @return Left and right azimuths, measured clockwise from North. This method never returns null. If there are no
     *         azimuths, returns an empty list.
     */
    public Iterable<? extends Angle> getAzimuths()
    {
        if (this.azimuths != null)
            return this.azimuths;
        return Collections.emptyList();
    }

    /**
     * Specifies the left and right azimuths of the range fans in this graphic. The provided iterable must specify pairs
     * of azimuths: first left azimuth, first right azimuth, second left, second right, etc.
     *
     * @param azimuths Left and right azimuths, measured clockwise from North.
     */
    public void setAzimuths(Iterable<? extends Angle> azimuths)
    {
        if (azimuths == null)
        {
            String message = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.azimuths = azimuths;
        this.onModifierChanged();
        this.reset();
    }

    /**
     * Indicates the altitudes of the rings that make up the range fan. Note that the range fan is always drawn on the
     * surface. The altitude strings are displayed as text.
     *
     * @return List of altitude strings. This method never returns null. If there are no altitudes, returns an empty
     *         list.
     */
    public Iterable<String> getAltitudes()
    {
        if (this.altitudes != null)
            return this.altitudes;
        return Collections.emptyList();
    }

    /**
     * Specifies the altitudes of the rings that make up the range fan. Note that the range fan is always drawn on the
     * surface. The altitude strings are displayed as text.
     *
     * @param altitudes List of altitude strings.
     */
    public void setAltitudes(Iterable<String> altitudes)
    {
        if (altitudes == null)
        {
            String message = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.altitudes = altitudes;
    }

    /**
     * Indicates a symbol drawn at the center of the range fan.
     *
     * @return The identifier of a symbol drawn at the center of the range fan. May be null.
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
     * @param sidc The identifier of a MIL-STD-2525C tactical symbol to draw at the center of the range fan. May be null
     *             to indicate that no symbol is drawn.
     */
    public void setSymbol(String sidc)
    {
        if (sidc != null)
        {
            if (this.symbolAttributes == null)
                this.symbolAttributes = new BasicTacticalSymbolAttributes();

            this.symbol = this.createSymbol(sidc, this.getPosition(), this.symbolAttributes);
        }
        else
        {
            // Null value indicates no symbol.
            this.symbol = null;
            this.symbolAttributes = null;
        }
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(this.position);
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        this.determineActiveAttributes();

        if (this.paths == null)
        {
            this.createShapes(dc);
        }

        if (this.arrowHead != null)
        {
            this.arrowHead.preRender(dc);
        }
    }

    /**
     * Render the polygon.
     *
     * @param dc Current draw context.
     */
    protected void doRenderGraphic(DrawContext dc)
    {
        for (Path path : this.paths)
        {
            path.render(dc);
        }

        if (this.arrowHead != null)
        {
            this.arrowHead.render(dc);
        }
    }

    /** {@inheritDoc} Overridden to render symbol at the center of the range fan. */
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
        if (this.paths != null)
        {
            for (Path path : this.paths)
            {
                path.setDelegateOwner(owner);
            }
        }

        if (this.arrowHead != null)
        {
            this.arrowHead.setDelegateOwner(owner);
        }
    }

    /** Regenerate the graphics positions on the next frame. */
    protected void reset()
    {
        this.paths = null;
    }

    /**
     * Create the paths required to draw the graphic.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        this.paths = new ArrayList<Path>();

        Iterator<Double> radii = this.getRadii().iterator();
        Iterator<? extends Angle> azimuths = this.getAzimuths().iterator();

        Angle prevLeftAzimuth = Angle.NEG180;
        Angle prevRightAzimuth = Angle.POS180;
        double prevRadius = 0;

        // Create range fan arcs.
        while (radii.hasNext())
        {
            double radius = radii.next();

            if (radius > this.maxRadius)
                this.maxRadius = radius;

            Angle leftAzimuth = azimuths.hasNext() ? azimuths.next() : prevLeftAzimuth;
            Angle rightAzimuth = azimuths.hasNext() ? azimuths.next() : prevRightAzimuth;

            leftAzimuth = this.normalizeAzimuth(leftAzimuth);
            rightAzimuth = this.normalizeAzimuth(rightAzimuth);

            List<Position> positions = new ArrayList<Position>();

            // Create an arc to complete the left side of the previous fan, if this fan is larger. If this fan is smaller
            // this will add a single point to the position list at the range of the previous radius.
            this.createArc(dc, prevRadius, Angle.max(leftAzimuth, prevLeftAzimuth), leftAzimuth, positions);

            // Create the arc for this fan.
            this.createArc(dc, radius, leftAzimuth, rightAzimuth, positions);

            // Create an arc to complete the right side of the previous fan.
            this.createArc(dc, prevRadius, rightAzimuth, Angle.min(rightAzimuth, prevRightAzimuth), positions);

            this.paths.add(this.createPath(positions));

            prevRadius = radius;
            prevLeftAzimuth = leftAzimuth;
            prevRightAzimuth = rightAzimuth;
        }

        // Create the Center of Sector arrow if the fan is less than a full circle. If the fan is a full circle then it
        // doesn't make sense to draw an arbitrary arrow.
        boolean fullCircle = Math.abs(prevLeftAzimuth.subtract(prevRightAzimuth).degrees) >= 360;
        if (!fullCircle)
        {
            this.centerAzimuth = this.computeCenterSectorAngle(prevLeftAzimuth, prevRightAzimuth);
            this.createCenterOfSectorArrow(dc, centerAzimuth, prevRadius);
        }
        else
        {
            this.centerAzimuth = Angle.POS180; // Due South
        }
    }

    /**
     * Create shapes to draw the Center Of Sector arrow. This arrow bisects the final range fan.
     *
     * @param dc            Current draw context.
     * @param centerAzimuth Azimuth of the Center Of Sector arrow.
     * @param finalRadius   Radius, in meters, of the final range fan.
     */
    protected void createCenterOfSectorArrow(DrawContext dc, Angle centerAzimuth, double finalRadius)
    {
        Position center = this.getPosition();

        // Create the line par of the arrow.
        List<Position> positions = new ArrayList<Position>();
        positions.add(center);
        this.createArc(dc, finalRadius * this.getCenterOfSectorLength(), centerAzimuth, centerAzimuth,
            positions);

        this.paths.add(this.createPath(positions));

        // The final position added by createArc above is the tip of the Center Of Sector arrow head.
        Position arrowTip = positions.get(positions.size() - 1);

        // Create a polygon to draw the arrow head.
        this.arrowHead = this.createPolygon();
        positions = this.computeArrowheadPositions(dc, center, arrowTip, this.getArrowLength(),
            this.getArrowAngle());
        this.arrowHead.setLocations(positions);
    }

    /**
     * Compute the angle of the Center Of Sector line. The center sector angle is computed as the average of the left
     * and right azimuths of the last range fan in the graphic. MIL-STD-2525C does not specify how this angle should be
     * computed, but the graphic template (pg. 693) suggests that the Center Of Sector line should bisect the last range
     * fan arc.
     *
     * @param finalLeftAzimuth  Left azimuth of the last range fan in the graphic.
     * @param finalRightAzimuth Right azimuth of the last range fan in the graphic.
     *
     * @return Azimuth, from North, of the Center Of Sector line.
     */
    protected Angle computeCenterSectorAngle(Angle finalLeftAzimuth, Angle finalRightAzimuth)
    {
        return finalLeftAzimuth.add(finalRightAzimuth).divide(2.0);
    }

    /**
     * Create positions to draw an arc around the graphic's center position. The arc is described by a radius, left
     * azimuth, and right azimuth. The arc positions are added onto an existing list of positions, in left to right
     * order (the arc draws from the left azimuth to the right azimuth).
     * <p/>
     * If the left and right azimuths are equal, then this methods adds a single position to the list at the desired
     * azimuth and radius.
     *
     * @param dc           Current draw context.
     * @param radius       Radius of the circular segment, in meters.
     * @param leftAzimuth  Azimuth (from North) of the left side of the arc.
     * @param rightAzimuth Azimuth (from North) of teh right side of the arc.
     * @param positions    List to collect positions for the arc.
     */
    protected void createArc(DrawContext dc, double radius, Angle leftAzimuth, Angle rightAzimuth,
        List<Position> positions)
    {
        Globe globe = dc.getGlobe();

        int intervals = this.getIntervals();

        Position center = this.getPosition();
        double globeRadius = globe.getRadiusAt(center.getLatitude(), center.getLongitude());
        double radiusRadians = radius / globeRadius;

        // If the left and right azimuths are equal then just add a single point and return.
        if (leftAzimuth.equals(rightAzimuth))
        {
            LatLon ll = LatLon.greatCircleEndPosition(center, leftAzimuth.radians, radiusRadians);
            positions.add(new Position(ll, 0));
            return;
        }

        Angle arcAngle = rightAzimuth.subtract(leftAzimuth);

        Angle da = arcAngle.divide(intervals);

        for (int i = 0; i < intervals + 1; i++)
        {
            double angle = i * da.radians + leftAzimuth.radians;

            LatLon ll = LatLon.greatCircleEndPosition(center, angle, radiusRadians);
            positions.add(new Position(ll, 0));
        }
    }

    /**
     * Compute the positions of the arrow head for the sector center line.
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

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        Iterable<Double> radii = this.getRadii();
        if (radii == null)
            return;

        Iterator<String> altitudes = this.getAltitudes().iterator();
        Iterator<? extends Angle> azimuths = this.getAzimuths().iterator();

        Angle leftAzimuth = null;
        Angle rightAzimuth = null;

        for (Double radius : radii)
        {
            if (azimuths.hasNext())
                leftAzimuth = azimuths.next();
            if (azimuths.hasNext())
                rightAzimuth = azimuths.next();

            String alt = null;
            if (altitudes.hasNext())
                alt = altitudes.next();

            this.addLabel(this.createRangeLabelString(radius, alt));

            if (leftAzimuth != null)
                this.addLabel(this.createAzimuthLabelString(leftAzimuth));

            if (rightAzimuth != null)
                this.addLabel(this.createAzimuthLabelString(rightAzimuth));
        }
    }

    /** Create azimuth labels. */
    protected void createAzimuthLabels()
    {
        Iterable<? extends Angle> azimuths = this.getAzimuths();
        if (azimuths == null)
            return;

        for (Angle azimuth : azimuths)
        {
            this.addLabel(this.createAzimuthLabelString(azimuth));
        }
    }

    /**
     * Create text for a range label.
     *
     * @param radius   Range of the ring, in meters.
     * @param altitude Altitude string.
     *
     * @return Range label text for this ring.
     */
    protected String createRangeLabelString(double radius, String altitude)
    {
        NumberFormat df = this.getRadiusFormat();

        StringBuilder sb = new StringBuilder();
        sb.append("RG ").append(df.format(radius));

        if (!WWUtil.isEmpty(altitude))
        {
            sb.append("\nALT ").append(altitude);
        }

        return sb.toString();
    }

    /**
     * Create text for an azimuth label.
     *
     * @param azimuth Azimuth, measured clockwise from North.
     *
     * @return Azimuth label text.
     */
    protected String createAzimuthLabelString(Angle azimuth)
    {
        NumberFormat df = this.getAzimuthFormat();
        return df.format(azimuth.degrees);
    }

    /** {@inheritDoc} */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (this.labels == null)
            return;

        Position center = this.getPosition();

        Iterator<TacticalGraphicLabel> labelIterator = this.labels.iterator();
        Iterator<Double> radii = this.getRadii().iterator();
        Iterator<? extends Angle> azimuths = this.getAzimuths().iterator();

        Angle leftAzimuth = null;
        Angle rightAzimuth = null;

        double prevRadius = 0;
        double globeRadius = dc.getGlobe().getRadiusAt(center);

        while (radii.hasNext() && labelIterator.hasNext())
        {
            if (azimuths.hasNext())
                leftAzimuth = azimuths.next();
            if (azimuths.hasNext())
                rightAzimuth = azimuths.next();

            leftAzimuth = this.normalizeAzimuth(leftAzimuth);
            rightAzimuth = this.normalizeAzimuth(rightAzimuth);

            // The labels come in sets of three: radius, left azimuth, right azimuth
            TacticalGraphicLabel rangeLabel = labelIterator.next();

            TacticalGraphicLabel leftLabel = null; // Left azimuth label
            TacticalGraphicLabel rightLabel = null; // Right azimuth label

            if (leftAzimuth != null && labelIterator.hasNext())
                leftLabel = labelIterator.next();
            if (rightAzimuth != null && labelIterator.hasNext())
                rightLabel = labelIterator.next();

            double radius = radii.next();
            double avgRadius = (radius + prevRadius) / 2.0;
            double radiusRadians = avgRadius / globeRadius;

            // Find the range label position
            Position position = this.determineRangeLabelPosition(center, this.centerAzimuth, leftAzimuth, rightAzimuth,
                radiusRadians);
            rangeLabel.setPosition(position);

            // Compute an angular offset that will put the label a little bit to the side of range fan line.
            double offset = this.computeAzimuthLabelOffset(avgRadius, this.maxRadius);

            // Find left azimuth label position
            if (leftAzimuth != null && leftLabel != null)
            {
                LatLon ll = LatLon.greatCircleEndPosition(center, leftAzimuth.radians - offset, radiusRadians);
                leftLabel.setPosition(new Position(ll, 0));
            }

            // Find right azimuth label position
            if (rightAzimuth != null && rightLabel != null)
            {
                LatLon ll = LatLon.greatCircleEndPosition(center, rightAzimuth.radians + offset, radiusRadians);
                rightLabel.setPosition(new Position(ll, 0));
            }

            prevRadius = radius;
        }
    }

    /**
     * Compute an angular offset to apply to a azimuth label. This angle will be added to the azimuth of the label's
     * azimuth in order to place the label a little bit to the side of the line that it applies to.
     *
     * @param radius    Radius at which the label will be placed.
     * @param maxRadius Maximum radius in the range fan.
     *
     * @return Angle, in radians, to add to the range fan azimuth in order to determine the label position.
     */
    protected double computeAzimuthLabelOffset(double radius, double maxRadius)
    {
        return Math.asin(AZIMUTH_LABEL_OFFSET * maxRadius / radius);
    }

    /**
     * Determine the position of a range label for a ring in the range fan. The method finds a point to either the left
     * or right of the center line, depending on which has more space for the label.
     *
     * @param center        Center of the range fan.
     * @param centerAzimuth Azimuth of the Center Of Sector arrow.
     * @param leftAzimuth   Left azimuth of this ring.
     * @param rightAzimuth  Right azimuth of this ring.
     * @param radiusRadians Radius, in radians, at which to place the label.
     *
     * @return Position for the range label on this ring.
     */
    protected Position determineRangeLabelPosition(Position center, Angle centerAzimuth, Angle leftAzimuth,
        Angle rightAzimuth, double radiusRadians)
    {
        // If either left or right azimuth is not specified, use the center instead.
        leftAzimuth = (leftAzimuth != null) ? leftAzimuth : centerAzimuth;
        rightAzimuth = (rightAzimuth != null) ? rightAzimuth : centerAzimuth;

        // Determine the angular distance between the Center Of Sector line and the left and right sides of the fan.
        double deltaLeft = Math.abs(centerAzimuth.subtract(leftAzimuth).degrees);
        double deltaRight = Math.abs(centerAzimuth.subtract(rightAzimuth).degrees);

        // Place the range label in the larger wedge.
        Angle labelAzimuth = (deltaLeft > deltaRight) ? leftAzimuth : rightAzimuth;

        // Place the label midway between the Center Of Sector arrow and the side of the fan.
        labelAzimuth = labelAzimuth.add(centerAzimuth).divide(2.0);

        LatLon ll = LatLon.greatCircleEndPosition(center, labelAzimuth.radians, radiusRadians);
        return new Position(ll, 0);
    }

    /**
     * Normalize an azimuth angle to the range [-180:180] degrees.
     *
     * @param azimuth Azimuth to normalize.
     *
     * @return Normalized azimuth. Returns null if {@code azimuth} is null.
     */
    protected Angle normalizeAzimuth(Angle azimuth)
    {
        // The azimuth is not actually a longitude, but the normalization formula is the same as for longitude.
        if (azimuth != null)
            return Angle.normalizedLongitude(azimuth);
        return null;
    }

    /** {@inheritDoc} Overridden to turn on shape interiors. */
    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        // Turn on the shape interior for the arrow head. All other parts of the graphic are Paths, which do not draw
        // an interior, so this setting only affects the arrow head.
        Material material = this.getDefaultMaterial();
        attributes.setInteriorMaterial(material);

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

    /**
     * Create and configure a SurfacePolygon to render the arrow head on the sector center line.
     *
     * @return New surface polygon.
     */
    protected SurfacePolygon createPolygon()
    {
        SurfacePolygon polygon = new SurfacePolygon();
        polygon.setDelegateOwner(this.getActiveDelegateOwner());
        polygon.setAttributes(this.getActiveShapeAttributes());
        return polygon;
    }
}