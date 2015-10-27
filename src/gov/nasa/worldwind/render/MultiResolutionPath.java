/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.nio.IntBuffer;
import java.util.Iterator;

/**
 * A Version of {@link Path} that provides level-of-detail. Positions in a {@code Path} are filtered based on a "skip
 * count", the number of positions to skip between positions that are drawn. The skip-count algorithm can be replaced by
 * the application. The default algorithm skips up to four positions, depending on the eye distance from the positions.
 * Also, if the segment between any two positions is too small to be distinguished, it is not drawn. See {@link
 * #makePositions(DrawContext, gov.nasa.worldwind.render.Path.PathData)}.
 * <p/>
 * NOTE: This shape does not draw correctly on a 2D globe when its positions span the dateline.
 *
 * @author tag
 * @version $Id: MultiResolutionPath.java 2185 2014-07-29 20:15:04Z tgaskins $
 * @deprecated
 */
public class MultiResolutionPath extends Path
{
    /**
     * This interface provides the means for the application to specify the algorithm used to determine the number of
     * specified positions skipped during path tessellation.
     * <p/>
     * This class overrides the method {@link Path#makePositions(DrawContext, PathData)}.
     */
    public interface SkipCountComputer
    {
        /**
         * Determines the number of positions to skip for the current viewing state. Determines the number of positions
         * to skip for the current viewing state.
         *
         * @param dc       the current draw context.
         * @param pathData this shape's current path data.
         *
         * @return the number of positions to skip when computing the tessellated or non-tessellated path.
         */
        public int computeSkipCount(DrawContext dc, PathData pathData);
    }

    /** Subclass of PathData that adds the capability to map which ordinal number corresponds to each rendered position. */
    protected static class MultiResolutionPathData extends PathData
    {
        /** Maps indices of rendered positions to their corresponding ordinal numbers. */
        protected IntBuffer positionOrdinals;

        /**
         * Creates a new MultiResolutionPathData with the specified draw context and path.
         *
         * @param dc    the draw context associated with this path data.
         * @param shape the shape associated with this path data.
         */
        public MultiResolutionPathData(DrawContext dc, Path shape)
        {
            super(dc, shape);
        }

        /**
         * Returns a buffer mapping indices of rendered positions to their corresponding ordinal numbers.
         *
         * @return a buffer mapping positions to ordinal numbers.
         */
        public IntBuffer getPositionOrdinals()
        {
            return this.positionOrdinals;
        }

        /**
         * Specifies a buffer that maps indices of rendered positions to their corresponding ordinal numbers.
         *
         * @param posOrdinals a buffer that maps positions to ordinal numbers.
         */
        public void setPositionOrdinals(IntBuffer posOrdinals)
        {
            this.positionOrdinals = posOrdinals;
        }
    }

    /**
     * The default implementation of <code>SkipCountComputer</code>. This implementation returns a value of 4 when the
     * eye distance to the path is greater than 10e3, a value of 2 when the eye distance is greater than 1e3 meters but
     * less then 10e3, and a value of 1 when the eye distance is less than 1e3.
     */
    protected SkipCountComputer skipCountComputer = new SkipCountComputer()
    {
        public int computeSkipCount(DrawContext dc, PathData pathData)
        {
            double d = getDistanceMetric(dc, pathData);

            return d > 10e3 ? 4 : d > 1e3 ? 2 : 1;
        }
    };

    /**
     * Creates a path with specified positions. When the path is rendered, only path positions that are visually
     * distinct for the current viewing state are considered. The path adjusts the positions it uses as the view state
     * changes, using more of the specified positions as the eye point comes closer to the shape.
     * <p/>
     * Note: If fewer than two positions are specified, no path is drawn.
     *
     * @param positions the path positions. This reference is retained by this shape; the positions are not copied. If
     *                  any positions in the set change, {@link #setPositions(Iterable)} must be called to inform this
     *                  shape of the change.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public MultiResolutionPath(Iterable<? extends Position> positions)
    {
        super(positions);
    }

    /**
     * Creates a path with specified positions specified via a generic list. When the path is rendered, only path
     * positions that are visually distinct for the current viewing state are considered. The path adjusts the positions
     * it uses as the view state changes, using more of the specified positions as the eye point comes closer to the
     * shape.
     * <p/>
     * Note: If fewer than two positions are specified, no path is drawn.
     *
     * @param positions the path positions. This reference is retained by this shape; the positions are not copied. If
     *                  any positions in the set change, {@link #setPositions(Iterable)} must be called to inform this
     *                  shape of the change.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public MultiResolutionPath(Position.PositionList positions)
    {
        super(positions);
    }

    /**
     * Indicates the SkipCountComputer that is used to determine the number of specified positions skipped during path
     * tessellation.
     *
     * @return the SkipCountComputer used during path tessellation.
     */
    public SkipCountComputer getSkipCountComputer()
    {
        return this.skipCountComputer;
    }

    /**
     * Specifies the SkipCountComputer that determines the number of specified positions skipped during path
     * tessellation.
     *
     * @param computer the SkipCountComputer to use during path tessellation.
     *
     * @throws IllegalArgumentException if the computer is null.
     */
    public void setSkipCountComputer(SkipCountComputer computer)
    {
        if (computer == null)
        {
            String message = Logging.getMessage("nullValue.CallbackIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.skipCountComputer = computer;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to return a new instance of MultiResolutionPathData.
     */
    @Override
    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new MultiResolutionPathData(dc, this);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to initialize and build the PathData's positionOrdinals buffer.
     */
    @Override
    protected void makeTessellatedPositions(DrawContext dc, PathData pathData)
    {
        if (this.numPositions < 2)
            return;

        MultiResolutionPathData mrpd = (MultiResolutionPathData) pathData;
        if (mrpd.positionOrdinals == null || mrpd.positionOrdinals.capacity() < this.numPositions)
            mrpd.positionOrdinals = Buffers.newDirectIntBuffer(this.numPositions);
        else
            mrpd.positionOrdinals.clear();

        super.makeTessellatedPositions(dc, pathData);

        mrpd.positionOrdinals.flip();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to skip positions from this Path's original positions list. Positions are skipped first according to
     * this Path's skipCountComputer. The skipCountComputer determines how many positions this path skips between
     * tessellated positions. Any positions remaining after this step are skipped if the segment they are part of is
     * either very small or not visible.
     */
    @Override
    protected void makePositions(DrawContext dc, PathData pathData)
    {
        Iterator<? extends Position> iter = this.positions.iterator();
        Position posA = iter.next();
        int ordinalA = 0;
        Color colorA = this.getColor(posA, ordinalA);
        this.addTessellatedPosition(posA, colorA, ordinalA, pathData); // add the first position of the path

        int skipCount = this.skipCountComputer.computeSkipCount(dc, pathData);

        // Tessellate each segment of the path.
        Vec4 ptA = this.computePoint(dc.getTerrain(), posA);

        for (int i = 1; iter.hasNext(); i++)
        {
            Position posB = iter.next();

            if (i % skipCount != 0 && iter.hasNext())
            {
                continue;
            }

            Vec4 ptB = this.computePoint(dc.getTerrain(), posB);

            if (iter.hasNext()) // if this is not the final position
            {
                // If the segment is very small or not visible, don't use it.
                if (this.isSmall(dc, ptA, ptB, 8) || !this.isSegmentVisible(dc, posA, posB, ptA, ptB))
                    continue;
            }

            Color colorB = this.getColor(posB, i);
            this.makeSegment(dc, posA, posB, ptA, ptB, colorA, colorB, ordinalA, i, pathData);
            posA = posB;
            ptA = ptB;
            colorA = colorB;
            ordinalA = i;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to create a mapping between the current tessellated position and the specified ordinal, if the ordinal
     * is not null.
     */
    @Override
    protected void addTessellatedPosition(Position pos, Color color, Integer ordinal, PathData pathData)
    {
        if (ordinal != null)
        {
            // NOTE: Assign these indices before adding the new position to the tessellatedPositions list.
            MultiResolutionPathData mrpd = (MultiResolutionPathData) pathData;
            mrpd.positionOrdinals.put(ordinal);
        }

        super.addTessellatedPosition(pos, color, ordinal, pathData);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to use the MultiResolutionPathData's positionOrdinals buffer to map the specified position index to
     * its corresponding ordinal number.
     */
    @Override
    protected Integer getOrdinal(int positionIndex)
    {
        MultiResolutionPathData mrpd = (MultiResolutionPathData) this.getCurrentPathData();
        return mrpd.positionOrdinals.get(positionIndex);
    }
}
