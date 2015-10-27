/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Fortified Area graphic (2.X.2.1.3.4).
 *
 * @author pabercrombie
 * @version $Id: FortifiedArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FortifiedArea extends BasicArea
{
    /** Default number of wave lengths for a simple shape. This number is used to compute a default wave length. */
    public static final int DEFAULT_NUM_WAVES = 20;

    /** Original positions specified by the application. */
    protected Iterable<? extends Position> positions;
    /**
     * Positions computed from the original positions. This list includes the positions necessary to draw the square
     * wave.
     */
    protected List<Position> computedPositions;

    /** Indicates the wavelength of the square wave that forms the graphic's border. */
    protected double waveLength;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_GNL_ARS_FTFDAR);
    }

    public FortifiedArea(String sidc)
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

        // Just move the polygon instead of recomputing the square wave using the shifted position list. This makes
        // moving the polygon using a dragger smoother.
        this.polygon.moveTo(position);
    }

    /**
     * Indicates the wavelength of the square wave that forms the graphic's boundary. This is the length from the start
     * of one square "tooth" to the start of the next.
     * <p/>
     * <pre>
     *    __    __
     * __|  |__|  |__
     *   ^     ^
     *   Wavelength
     * </pre>
     *
     * @return The wave length, in meters.
     */
    public double getWaveLength()
    {
        return waveLength;
    }

    /**
     * Specifies the wavelength of the square wave that forms the graphic's boundary. See {@link #getWaveLength()} for
     * more information on how this distance is interpreted.
     *
     * @param waveLength The wavelength, in meters.
     */
    public void setWaveLength(double waveLength)
    {
        this.waveLength = waveLength;
    }

    /** {@inheritDoc} */
    @Override
    public void computeGeometry(DrawContext dc)
    {
        if (this.computedPositions == null && this.positions != null)
        {
            this.generateIntermediatePositions(dc, this.positions);
            this.polygon.setLocations(this.computedPositions);
        }
        super.computeGeometry(dc);
    }

    /**
     * Generate the positions required to draw the polygon with a square wave boundary.
     *
     * @param dc        Current draw context.
     * @param positions Positions that define the polygon boundary.
     */
    protected void generateIntermediatePositions(DrawContext dc, Iterable<? extends Position> positions)
    {
        Iterator<? extends Position> iterator = positions.iterator();

        Globe globe = dc.getGlobe();
        List<Position> toothPositions = new ArrayList<Position>();

        double waveLength = this.getWaveLength();
        if (waveLength == 0)
        {
            waveLength = this.computeDefaultWavelength(dc.getGlobe());
        }

        // A single tooth is half of a full wave.
        double toothSize = waveLength / 2.0;

        // Flag to indicate whether the current segment is a tooth or a straight lines between the teeth.
        boolean isTooth = true;

        Position firstPos = iterator.next();
        Vec4 thisPoint = globe.computePointFromPosition(firstPos);

        Position nextPos = iterator.next();
        Vec4 pNext = globe.computePointFromPosition(nextPos);

        while (true)
        {
            double dist = pNext.distanceTo3(thisPoint);

            Position pos = globe.computePositionFromPoint(thisPoint);
            toothPositions.add(pos);

            // If the distance to the next point is less than the tooth size, walk
            // around the polygon until we find a point far enough away to draw a tooth.
            while (dist < toothSize && iterator.hasNext())
            {
                // If we're drawing a straight segment, add the short line
                // segment to the boundary in order to preserve the shape
                // of the polygon as much as possible between the teeth.
                if (!isTooth)
                {
                    toothPositions.add(nextPos);
                }

                nextPos = iterator.next();
                pNext = globe.computePointFromPosition(nextPos);
                dist = pNext.distanceTo3(thisPoint);
            }

            // Break if we can't find a position far enough away to draw a tooth.
            if (dist < toothSize)
            {
                // If the polygon is not closed, close it.
                if (!firstPos.equals(nextPos))
                {
                    nextPos = firstPos;
                    pNext = globe.computePointFromPosition(nextPos);
                }
                else
                {
                    break;
                }
            }

            // Compute the point where the current segment (either tooth or straight) ends.
            Vec4 vAB = pNext.subtract3(thisPoint).normalize3();
            Vec4 endSegment = thisPoint.add3(vAB.multiply3(toothSize));

            if (isTooth)
            {
                Vec4 normal = globe.computeSurfaceNormalAtPoint(thisPoint);
                Vec4 perpendicular = vAB.cross3(normal);
                perpendicular = perpendicular.normalize3().multiply3(toothSize);

                // Compute the two points that form the tooth.
                Vec4 toothPoint1 = thisPoint.subtract3(perpendicular);
                Vec4 toothPoint2 = endSegment.subtract3(perpendicular);

                toothPositions.add(globe.computePositionFromPoint(toothPoint1));
                toothPositions.add(globe.computePositionFromPoint(toothPoint2));
            }

            pos = globe.computePositionFromPoint(endSegment);
            toothPositions.add(pos);

            thisPoint = endSegment;
            isTooth = !isTooth;
        }

        this.computedPositions = toothPositions;
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
        Vec4 first = null;
        Vec4 prev = null;
        for (Position pos : this.positions)
        {
            Vec4 current = globe.computePointFromPosition(pos);

            if (prev != null)
            {
                perimeter += current.distanceTo3(prev);
            }
            else
            {
                first = current;
            }

            prev = current;
            count += 1;
        }

        // If the polygon is not closed account for the missing segment.
        if (prev != null && !prev.equals(first))
        {
            perimeter += prev.distanceTo3(first);
        }

        // Compute the "complexity" of the polygon. A triangle has complexity of one, and complexity increases as
        // vertices are added.
        double complexity = Math.sqrt(count / 3.0);

        return perimeter / (complexity * DEFAULT_NUM_WAVES);
    }
}
