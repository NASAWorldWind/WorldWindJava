/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Sector;

import java.util.*;

/**
 * This abstract class filters items through a quad tree to perform an operation on those items that intersect the
 * tree's cells. For each submitted item, this class' {@link #doOperation} method is called for each leaf or
 * intermediate cell that intersects the item. That method typically stores the item's association with the intersecting
 * cell, either to populate a quadtree membership list or to mark it as a visible item.
 * <p/>
 * The filter uses a bit-set to identify cells that intersect submitted items. This minimal memory eliminates the need
 * to retain cell information other than identity. Only cell identities are retained by this abstract class. Subclasses
 * provide the means to retain item information associated with intersecting cells.
 *
 * @author tag
 * @version $Id: BitSetQuadTreeFilter.java 1939 2014-04-15 22:50:19Z tgaskins $
 */
public abstract class BitSetQuadTreeFilter
{
    protected BitSet bits;
    protected int maxLevel;
    protected int numLevels;
    protected int[] powersOf4;
    protected int[] levelSizes; // Cumulative bits at start of each level. Used for position calculations.
    protected int[] path; // valid only during traversal. Maintains the path to a cell from level 0.
    protected boolean stopped;

    /**
     * A method implemented by subclasses and called during tree traversal to perform an operation on an intersecting
     * item. The method's implementation typically stores a reference to the intersecting item, or stores the cell's
     * identity for later reference. See for example {@link gov.nasa.worldwind.util.BasicQuadTree} and {@link
     * gov.nasa.worldwind.util.BitSetQuadTreeFilter.FindIntersectingBitsOp}.
     *
     * @param level      the quadtree level currently being traversed.
     * @param position   the position of the cell in its parent cell, either 0, 1, 2, or 3. Cell positions starts with 0
     *                   at the southwest corner of the parent cell and increment counter-clockwise: cell 1 is SE, cell
     *                   2 is NE and cell 3 is NW.
     * @param cellRegion an array specifying the coordinates of the cell's region. The first two entries are the minimum
     *                   and maximum values on the Y axis (typically latitude). The last two entries are the minimum and
     *                   maximum values on the X axis, (typically longitude).
     * @param itemCoords an array specifying the region or location of the intersecting item. If the array's length is 2
     *                   it represents a location in [latitude, longitude]. If its length is 4 it represents a region,
     *                   with the same layout as the <code>nodeRegion</code> argument.
     *
     * @return true if traversal should continue to the cell's descendants, false if traversal should not continue to
     *         the cell's descendants.
     */
    abstract protected boolean doOperation(int level, int position, double[] cellRegion, double[] itemCoords);

    /**
     * Constructs an instance of this class.
     *
     * @param numLevels the number of levels in the quadtree.
     * @param bitSet    a {@link BitSet} to use as the quadtree index. If null, a new set is created. Depending on the
     *                  operation, the bit-set is modified or only read.
     *
     * @throws IllegalArgumentException if <code>numLevels</code> is less than 1.
     */
    public BitSetQuadTreeFilter(int numLevels, BitSet bitSet)
    {
        if (numLevels < 1)
        {
            String message = Logging.getMessage("generic.DepthOutOfRange", numLevels);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.numLevels = numLevels;
        this.maxLevel = numLevels - 1;

        this.powersOf4 = WWMath.computePowers(4, numLevels);
        this.levelSizes = computeLevelSizes(numLevels);

        this.path = new int[this.numLevels];
        this.bits = bitSet != null ? bitSet : new BitSet(this.levelSizes[numLevels]);
    }

    /**
     * Returns the number of levels in the filter.
     *
     * @return the number of levels in the filter.
     */
    public int getNumLevels()
    {
        return this.numLevels;
    }

    /**
     * Stop the current traversal of the quadtree. {@link #start()} must be called before attempting a subsequent
     * traversal.
     */
    public void stop()
    {
        this.stopped = true;
    }

    /**
     * Indicates whether traversal has been stopped.
     *
     * @return <code>true</code> if traversal has been stopped, otherwise false.
     */
    public boolean isStopped()
    {
        return stopped;
    }

    /**
     * Re-initialize for traversal. Must be called to perform subsequent traversals after having called {@link
     * #stop()}.
     */
    public void start()
    {
        this.stopped = false;
    }

    /**
     * An internal method that computes the number of ancestor cells at each level. Level 0 has 0 ancestor cells, level
     * 1 has 4, level 2 has 20 (16 + 4), etc.
     *
     * @param numLevels the number of quadtree levels.
     *
     * @return an array of <code>numLevels + 1</code> elements containing the sums of ancestor-level cell counts for
     *         each quadtree level. The last element in the array contains the total number of cells in the tree.
     */
    protected static int[] computeLevelSizes(int numLevels)
    {
        int[] sizes = new int[numLevels + 1];

        sizes[0] = 0;

        double accumulatedSize = 0;
        for (int i = 1; i <= numLevels; i++)
        {
            accumulatedSize += Math.pow(4, i);
            sizes[i] = (int) accumulatedSize;
        }

        return sizes;
    }

    /**
     * The main, recursive traversal method. Called once for each cell. Tests for intersection between items and cells,
     * subdivides cells and continues depth-first traversal on those descendants. Terminates traversal when the end of
     * the tree is reached or when the subclass's {@link #doOperation} method returns false.
     *
     * @param level      the quadtree level currently being traversed.
     * @param position   the position o f the cell in its parent cell, either 0, 1, 2, or 3. Cell positions starts with
     *                   0 at the southwest corner of the parent cell and increment counter-clockwise: cell 1 is SE,
     *                   cell 2 is NE and cell 3 is NW.
     * @param cellRegion an array specifying the coordinates of the cell's region. The first two entries are the minimum
     *                   and maximum values on the Y axis (typically latitude). The last two entries are the minimum and
     *                   maximum values on the X axis, (typically longitude).
     * @param itemCoords an array specifying the region or location of the item. If the array's length is 2 it
     *                   represents a location in [latitude, longitude]. If its length is 4 it represents a region, with
     *                   the same layout as the <code>nodeRegion</code> argument.
     */
    protected void testAndDo(int level, int position, double[] cellRegion, double[] itemCoords)
    {
        if (this.stopped)
            return;

        if (this.intersects(cellRegion, itemCoords) == 0)
            return;

        this.path[level] = position;

        if (!this.doOperation(level, position, cellRegion, itemCoords) || this.stopped)
            return;

        if (level == this.maxLevel)
            return;

        double latMid = (cellRegion[1] + cellRegion[0]) / 2;
        double lonMid = (cellRegion[3] + cellRegion[2]) / 2;

        double[] subRegion = new double[4];

        subRegion[0] = cellRegion[0];
        subRegion[1] = latMid;
        subRegion[2] = cellRegion[2];
        subRegion[3] = lonMid;
        this.testAndDo(level + 1, 0, subRegion, itemCoords);
        if (this.stopped)
            return;

        subRegion[2] = lonMid;
        subRegion[3] = cellRegion[3];
        this.testAndDo(level + 1, 1, subRegion, itemCoords);
        if (this.stopped)
            return;

        subRegion[0] = latMid;
        subRegion[1] = cellRegion[1];
        this.testAndDo(level + 1, 2, subRegion, itemCoords);
        if (this.stopped)
            return;

        subRegion[2] = cellRegion[2];
        subRegion[3] = lonMid;
        this.testAndDo(level + 1, 3, subRegion, itemCoords);
    }

    /**
     * Determines whether an item intersects a cell.
     *
     * @param cellRegion an array specifying the coordinates of the cell's region. The first two entries are the minimum
     *                   and maximum values on the Y axis (typically latitude). The last two entries are the minimum and
     *                   maximum values on the X axis, (typically longitude).
     * @param itemCoords an array specifying the region or location of the item. If the array's length is 2 it
     *                   represents a location in [latitude, longitude]. If its length is 4 it represents a region, with
     *                   the same layout as the <code>nodeRegion</code> argument.
     *
     * @return non-zero if the item intersects the region. 0 if no intersection.
     */
    protected int intersects(double[] cellRegion, double[] itemCoords)
    {
        if (itemCoords.length == 4) // treat test region as a sector
            return !(itemCoords[1] < cellRegion[0] || itemCoords[0] > cellRegion[1]
                || itemCoords[3] < cellRegion[2] || itemCoords[2] > cellRegion[3]) ? 1 : 0;
        else // assume test region is a 2-tuple location
            return itemCoords[0] >= cellRegion[0] && itemCoords[0] <= cellRegion[1]
                && itemCoords[1] >= cellRegion[2] && itemCoords[1] <= cellRegion[3] ? 1 : 0;
    }

    /**
     * Computes the bit position of a quadtree cell.
     *
     * @param level    the quadtree level currently being traversed.
     * @param position the position of the cell in its parent cell, either 0, 1, 2, or 3. Cell positions starts with 0
     *                 at the southwest corner of the parent cell and increment counter-clockwise: cell 1 is SE, cell 2
     *                 is NE and cell 3 is NW.
     *
     * @return the cell's bit position in the class' bit-list.
     */
    protected int computeBitPosition(int level, int position)
    {
        int bitPosition = position;

        // Compute the index of the position within the level
        for (int i = 0; i < level; i++)
        {
            bitPosition += this.path[i] * this.powersOf4[level - i];
        }

        // The index within the BitSet is the index within the level plus the number of bits prior to the level's bits
        return bitPosition + this.levelSizes[level];
    }

    /**
     * A quadtree filter that determines the bit positions of cells associated with items and intersecting a specified
     * region. Typically used to traverse the bit-set index of a populated quadtree to determine potentially visible
     * items.
     * <p/>
     * This class requires a previously populated filter and determines which of its cells intersect a specified
     * sector.
     */
    public static class FindIntersectingBitsOp extends BitSetQuadTreeFilter
    {
        protected List<Integer> intersectingBits;

        /**
         * Constructs a filter instance.
         *
         * @param filter a filter identifying significant cells in a quadtree, typically produced by applying a filter
         *               that identifies quadtree cells associated with items.
         *
         * @throws NullPointerException     if <code>filter</code> is null.
         * @throws IllegalArgumentException if <code>filter</code> is invalid.
         */
        public FindIntersectingBitsOp(BitSetQuadTreeFilter filter)
        {
            super(filter.getNumLevels(), filter.bits);
        }

        /**
         * Returns the bit positions of significant cells that intersect a specified sector. Significant cells are those
         * identified by the filter specified to the constructor.
         *
         * @param topRegions the zeroth-level regions of a quadtree.
         * @param testSector the sector of interest. Significant cells that intersect this sector are returned.
         * @param outIds     a list in which to place the bit positions of intersecting significant cells. May be null,
         *                   in which case a new list is created. In either case the list is the return value of the
         *                   method.
         *
         * @return the bit positions of intersecting significant cells. This is the list specified as the non-null
         *         <code>outIds</code> argument, or a new list if that argument is null.
         *
         * @throws IllegalArgumentException if either <code>topRegions</code> or <code>testSector</code> is null.
         */
        public List<Integer> getOnBits(List<double[]> topRegions, Sector testSector, List<Integer> outIds)
        {
            if (testSector == null)
            {
                String message = Logging.getMessage("nullValue.SectorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return this.getOnBits(topRegions, testSector.asDegreesArray(), outIds);
        }

        /**
         * Returns the bit positions of significant cells that intersect a specified sector. Significant cells are those
         * identified by the filter specified to the constructor.
         *
         * @param topRegions the zeroth-level regions of a quadtree.
         * @param testRegion the sector of interest, specified as a four-element array containing minimum latitude,
         *                   maximum latitude, minimum longitude and maximum longitude, in that order. Significant cells
         *                   that intersect this sector are returned.
         * @param outIds     a list in which to place the bit positions of intersecting significant cells. May be null,
         *                   in which case a new list is created. In either case the list is the return value of the
         *                   method.
         *
         * @return the bit positions of intersecting significant cells. This is the list specified as the non-null
         *         <code>outIds</code> argument, or a new list if that argument is null.
         *
         * @throws IllegalArgumentException if either <code>topRegions</code> or <code>testSector</code> is null.
         */
        public List<Integer> getOnBits(List<double[]> topRegions, double[] testRegion, List<Integer> outIds)
        {
            if (topRegions == null)
            {
                String message = Logging.getMessage("generic.DepthOutOfRange", numLevels);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (testRegion == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.intersectingBits = outIds != null ? outIds : new ArrayList<Integer>();

            for (int i = 0; i < topRegions.size(); i++)
            {
                this.testAndDo(0, i, topRegions.get(i), testRegion);
            }

            return this.intersectingBits;
        }

        /**
         * Assembles the output bit-list during traversal. Implements the abstract method {@link
         * BitSetQuadTreeFilter#doOperation(int, int, double[], double[])}. See the description of that method for
         * further detail.
         *
         * @param level      the quadtree level currently being traversed.
         * @param position   the position of the cell in its parent cell, either 0, 1, 2, or 3. Cell positions starts
         *                   with 0 at the southwest corner of the parent cell and increment counter-clockwise: cell 1
         *                   is SE, cell 2 is NE and cell 3 is NW.
         * @param cellRegion an array specifying the coordinates of the cell's region. The first two entries are the
         *                   minimum and maximum values on the Y axis (typically latitude). The last two entries are the
         *                   minimum and maximum values on the X axis, (typically longitude).
         * @param testSector an array specifying the region or location of the intersecting item. If the array's length
         *                   is 2 then it represents a location in [latitude, longitude]. If its length is 4 it
         *                   represents a region, with the same layout as the <code>nodeRegion</code> argument.
         *
         * @return true if traversal should continue to the cell's descendants, false if traversal should not continue
         *         to the cell's descendants.
         */
        protected boolean doOperation(int level, int position, double[] cellRegion, double[] testSector)
        {
            int bitNum = this.computeBitPosition(level, position);

            if (!this.bits.get(bitNum))
                return false;

            if (level < this.maxLevel)
                return true;

            this.intersectingBits.add(bitNum);

            return false;
        }
    }
}
