/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Forward Line of Own Troops graphic (2.X.2.1.2.2).
 *
 * @author pabercrombie
 * @version $Id: ForwardLineOfOwnTroops.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class ForwardLineOfOwnTroops extends PhaseLine
{
    /** Default number of wave lengths for a simple shape. This number is used to compute a default wave length. */
    public static final int DEFAULT_NUM_WAVES = 20;
    /** Default number of intervals used to draw the arcs. */
    public final static int DEFAULT_NUM_INTERVALS = 32;

    /** Original positions specified by the application. */
    protected Iterable<? extends Position> positions;
    /**
     * Positions computed from the original positions. This list includes the positions necessary to draw the triangle
     * wave.
     */
    protected List<Position> computedPositions;

    /** Indicates wave length (in meters) of the semicircle wave along the graphic boundary. */
    protected double waveLength;
    /** Number of intervals used to draw the arcs along the line. */
    protected int intervals = DEFAULT_NUM_INTERVALS;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_GNL_LNE_FLOT);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc MIL-STD-2525C identifier code.
     */
    public ForwardLineOfOwnTroops(String sidc)
    {
        super(sidc);
    }

    /** {@inheritDoc} */
    @Override
    public void setPositions(Iterable<? extends Position> positions)
    {
        this.positions = positions;
        this.computedPositions = null;
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<? extends Position> getPositions()
    {
        return this.positions;
    }

    /**
     * Indicates the wavelength of the semicircle wave that forms the graphic's boundary. This is the length from the
     * start of one "tooth" to the start of the next. If not wave length is specified a default wave length will be
     * computed.
     * <pre>
     * /\/\/\/\/\
     * ^ ^
     * Wavelength
     * </pre>
     *
     * @return The wave length, in meters.
     */
    public double getWaveLength()
    {
        return this.waveLength;
    }

    /**
     * Specifies the wavelength of the triangle wave that forms the graphic's boundary. See {@link #getWaveLength()} for
     * more information on how this distance is interpreted.
     *
     * @param waveLength The wavelength, in meters.
     */
    public void setWaveLength(int waveLength)
    {
        this.waveLength = waveLength;
        this.onShapeChanged();
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

    /** {@inheritDoc} */
    @Override
    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position oldPosition = this.getReferencePosition();

        // The reference position is null if this shape has no positions. In this case moving the shape to a new
        // reference position is meaningless. Therefore we fail softly by exiting and doing nothing.
        if (oldPosition == null)
            return;

        this.positions = Position.computeShiftedPositions(oldPosition, position, this.getPositions());

        // Just move the path instead of recomputing the wave using the shifted position list. This makes moving the
        // path using a dragger smoother.
        this.path.moveTo(position);
    }

    /** {@inheritDoc} */
    @Override
    protected void computeGeometry(DrawContext dc)
    {
        if (this.computedPositions == null && this.positions != null)
        {
            this.generateIntermediatePositions(dc, this.positions);
            this.path.setPositions(this.computedPositions);
        }
        super.computeGeometry(dc);
    }

    protected void onShapeChanged()
    {
        this.computedPositions = null; // Need to recompute paths
    }

    /**
     * Generate the positions required to draw the polygon with a triangle wave boundary.
     *
     * @param dc        Current draw context.
     * @param positions Positions that define the polygon boundary.
     */
    protected void generateIntermediatePositions(DrawContext dc, Iterable<? extends Position> positions)
    {
        Globe globe = dc.getGlobe();

        double waveLength = this.getWaveLength();
        if (waveLength == 0)
            waveLength = this.computeDefaultWavelength(positions, globe);
        double radius = (waveLength / 2.0) / globe.getRadius();

        PositionIterator iterator = new PositionIterator(positions, waveLength, globe);
        this.computedPositions = this.generateWavePositions(iterator, radius, false);
    }

    protected List<Position> generateWavePositions(Iterator<? extends Position> iterator, double radius,
        boolean reverse)
    {
        List<Position> wavePositions = new ArrayList<Position>();

        int intervals = this.getIntervals();
        int sign = reverse ? -1 : 1;

        Position posB = iterator.next();
        while (iterator.hasNext())
        {
            Position posA = iterator.next();
            if (posA == null) // Iterator returns null if there is not enough room for a full wave.
                continue;

            LatLon midPoint = LatLon.interpolateGreatCircle(0.5, posA, posB);
            Angle azimuth = LatLon.greatCircleAzimuth(posA, posB);

            // Generate positions for a semicircle centered on the midpoint.
            double delta = Angle.POS180.radians / intervals;
            for (int i = 0; i < intervals; i++)
            {
                LatLon ll = LatLon.greatCircleEndPosition(midPoint, azimuth.radians + delta * i * sign, radius);
                wavePositions.add(new Position(ll, 0));
            }
            posB = posA;
        }

        return wavePositions;
    }

    protected double computeDefaultWavelength(Iterable<? extends Position> positions, Globe globe)
    {
        Sector sector = Sector.boundingSector(positions);
        double diagonal = Math.hypot(sector.getDeltaLatRadians(), sector.getDeltaLonRadians());

        return (diagonal * globe.getRadius()) / DEFAULT_NUM_WAVES;
    }

    protected Angle computeGreatCirclePathLength(Iterable<? extends Position> positions)
    {
        double length = 0;

        // Compute the number of vertices and the length of the path.
        Position prev = null;
        for (Position pos : positions)
        {
            if (prev != null)
            {
                Angle dist = LatLon.greatCircleDistance(pos, prev);
                length += dist.radians;
            }

            prev = pos;
        }

        return Angle.fromRadians(length);
    }

    @Override
    protected String getGraphicLabel()
    {
        StringBuilder sb = new StringBuilder();
        if (this.mustShowHostileIndicator())
        {
            sb.append(SymbologyConstants.HOSTILE_ENEMY);
            sb.append("\n");
        }
        sb.append("FLOT");
        return sb.toString();
    }

    /** Iterator to generate equally spaced positions along a control line. */
    protected static class PositionIterator implements Iterator<Position>
    {
        /** Control positions. */
        protected Iterator<? extends Position> positions;

        /** Wavelength, as a geographic angle. */
        protected Angle interval;

        protected double tolerance = 0.25;

        /** Current position. */
        protected Position thisPosition;
        /** Position of the next control point. */
        protected Position nextControlPosition;

        /**
         * Create a new iterator to compute the positions of a triangle wave.
         *
         * @param positions Control positions for the triangle wave line.
         * @param interval  Generate positions along the control line at this interval.
         * @param globe     Globe used to compute geographic positions.
         */
        protected PositionIterator(Iterable<? extends Position> positions, double interval, Globe globe)
        {
            if (positions == null)
            {
                String message = Logging.getMessage("nullValue.PositionsListIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (globe == null)
            {
                String message = Logging.getMessage("nullValue.GlobeIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (interval <= 0)
            {
                String message = Logging.getMessage("generic.LengthIsInvalid");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.interval = Angle.fromRadians(interval / globe.getRadius());

            this.positions = positions.iterator();
            this.nextControlPosition = this.positions.next();
        }

        /** {@inheritDoc} */
        public boolean hasNext()
        {
            return this.nextControlPosition != null;
        }

        /** {@inheritDoc} */
        public Position next()
        {
            // thisPosition is null the first time that next() is called.
            if (this.thisPosition == null)
            {
                this.thisPosition = this.nextControlPosition;
                return this.thisPosition;
            }

            Angle distToNext = LatLon.greatCircleDistance(this.thisPosition, this.nextControlPosition);
            double thisStep = this.interval.degrees;
            double diff = distToNext.degrees - thisStep;

            while (diff < 0)
            {
                if (this.positions.hasNext())
                {
                    this.thisPosition = this.nextControlPosition;
                    this.nextControlPosition = this.positions.next();
                }
                else
                {
                    Position next = this.nextControlPosition;
                    this.nextControlPosition = null;

                    if (Math.abs(diff) < this.interval.degrees * this.tolerance)
                        return next;
                    else
                        return null;
                }

                // The sample distance wraps around a corner. Adjust step size.
                thisStep -= distToNext.degrees;

                distToNext = LatLon.greatCircleDistance(this.thisPosition, this.nextControlPosition);
                diff = distToNext.degrees - thisStep;
            }

            Angle azimuth = LatLon.greatCircleAzimuth(this.thisPosition, this.nextControlPosition);
            LatLon ll = LatLon.greatCircleEndPosition(this.thisPosition, azimuth, Angle.fromDegrees(thisStep));

            this.thisPosition = new Position(ll, 0);
            return this.thisPosition;
        }

        /** Not supported. */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
