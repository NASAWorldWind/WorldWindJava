/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.*;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Encirclement graphic (2.X.2.6.2.3).
 *
 * @author pabercrombie
 * @version $Id: Encirclement.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class Encirclement extends BasicArea
{
    /** Default number of wave lengths for a simple shape. This number is used to compute a default wave length. */
    public static final int DEFAULT_NUM_WAVES = 10;

    /** Original positions specified by the application. */
    protected Iterable<? extends Position> positions;
    /**
     * Positions computed from the original positions. This list includes the positions necessary to draw the triangle
     * wave.
     */
    protected List<Position> computedPositions;

    /** Indicates the wavelength of the triangle wave that forms the graphic's border. */
    protected double waveLength;
    /** The polygon with triangle wave. */
    protected SurfacePolygon wavePolygon = new SurfacePolygon();

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_SPL_ARA_ENCMT);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc MIL-STD-2525C identifier code.
     */
    public Encirclement(String sidc)
    {
        super(sidc);
        this.wavePolygon = this.createPolygon();
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

        // Just move the polygon instead of recomputing the triangle wave using the shifted position list. This makes
        // moving the polygon using a dragger smoother.
        this.polygon.moveTo(position);
        this.wavePolygon.moveTo(position);
    }

    /**
     * Indicates the wavelength of the triangle wave that forms the graphic's boundary. This is the length from the
     * start of one "tooth" to the start of the next.
     * <p/>
     * <pre>
     * __/\__/\__
     *  ^   ^
     *   Wavelength
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
    public void setWaveLength(double waveLength)
    {
        this.waveLength = waveLength;
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        super.preRender(dc);
        this.wavePolygon.preRender(dc);
    }

    @Override
    protected void doRenderGraphic(DrawContext dc)
    {
        super.doRenderGraphic(dc);
        this.wavePolygon.render(dc);
    }

    /** {@inheritDoc} */
    @Override
    protected void computeGeometry(DrawContext dc)
    {
        if (this.computedPositions == null && this.positions != null)
        {
            this.generateIntermediatePositions(dc, this.positions);
            this.polygon.setLocations(this.positions);
            this.wavePolygon.setLocations(this.computedPositions);
        }
        super.computeGeometry(dc);
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
        List<Position> wavePositions = new ArrayList<Position>();

        double waveLength = this.getWaveLength();
        if (waveLength == 0)
        {
            waveLength = this.computeDefaultWavelength(dc.getGlobe());
        }
        double amplitude = waveLength / 2.0;

        TriangleWavePositionIterator iterator = new TriangleWavePositionIterator(positions, waveLength, amplitude,
            globe);
        while (iterator.hasNext())
        {
            wavePositions.add(iterator.next());
        }

        this.computedPositions = wavePositions;
    }

    /**
     * Compute a default tooth size for the polygon. This method computes the default wavelength as a function of the
     * size of the polygon and the number of vertices. As the polygon gets bigger the wavelength will increase, but as
     * the number of the vertices increases the wavelength will decrease to prevent the waves from obscuring the shape
     * of the polygon.
     *
     * @param globe Globe on which the area will be rendered.
     *
     * @return The wave length.
     */
    // TODO: this algorithm assumes that more control points means that the shape is more complicated, so the teeth
    // TODO: need to be smaller. But this isn't always the case; adding points to increase the resolution of a circle
    // TODO: shouldn't make the teeth smaller.
    protected double computeDefaultWavelength(Globe globe)
    {
        double perimeter = 0;
        int count = 0;

        // Compute the number of vertices and the perimeter of the polygon.
        Position first = null;
        Position prev = null;

        for (Position pos : this.positions)
        {
            if (prev != null)
            {
                Angle dist = LatLon.greatCircleDistance(pos, prev);
                perimeter += dist.radians;
            }
            else
            {
                first = pos;
            }

            prev = pos;
            count += 1;
        }

        // If the polygon is not closed account for the missing segment.
        if (prev != null && !prev.equals(first))
        {
            Angle dist = LatLon.greatCircleDistance(first, prev);
            perimeter += dist.radians;
        }

        // Compute the "complexity" of the polygon. A triangle has complexity of one, and complexity increases as
        // vertices are added.
        double complexity = Math.sqrt(count / 3.0);

        perimeter = perimeter * globe.getRadius(); // Convert perimeter to meters.

        return perimeter / (complexity * DEFAULT_NUM_WAVES);
    }
}
