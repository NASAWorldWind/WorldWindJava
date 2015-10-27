/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.terrain.*;

import java.util.*;

/**
 * Implements a quadtree backed by a bit-set index. A bit-set provides a minimal-memory index. Each bit identifies one
 * cell in the quadtree.
 * <p/>
 * This class provides methods to add and remove items from the quadtree, and to determine the items intersecting
 * specified regions.
 * <p/>
 * Items can be added with an associated name, and can be retrieved and removed by name.
 *
 * @author tag
 * @version $Id: BasicQuadTree.java 1938 2014-04-15 22:34:52Z tgaskins $
 */
public class BasicQuadTree<T> extends BitSetQuadTreeFilter implements Iterable<T>
{
    protected ArrayList<double[]> levelZeroCells;
    protected Map<String, List<T>> items; // the tree's list of items
    protected T currentItem; // used during add() to pass the added item to doOperation().
    protected String currentName; // used during add() to pass the optional name of the added item to doOperation().
    protected HashMap<String, T> nameMap = new HashMap<String, T>(); // maps names to items
    protected boolean allowDuplicates = true;

    /**
     * Constructs a quadtree of a specified level and spanning a specified region.
     * <p/>
     * The number of levels in the quadtree must be specified to the constructor. The more levels there are the more
     * discriminating searches will be, but at the cost of some performance because more cells are searched. For the
     * Earth, a level count of 8 provides leaf cells about 75 km along their meridian edges (edges of constant Earth, a
     * level count of 8 provides leaf cells about 75 km along their meridian edges (edges of constant longitude).
     * Additional levels successfully halve the distance, fewer levels double that distance.
     *
     * @param numLevels the number of levels in the quadtree. The more levels there are the more discriminating searches
     *                  will be, but at the cost of some performance.
     * @param sector    the region the tree spans.
     * @param itemMap   a {@link Map} to hold the items added to the quadtree. May be null, in which case a new map is
     *                  created.
     *
     * @throws IllegalArgumentException if <code>numLevels</code> is less than 1.
     */
    public BasicQuadTree(int numLevels, Sector sector, Map<String, List<T>> itemMap)
    {
        super(numLevels, null);

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeLevelZeroCells(sector);
        this.items = itemMap != null ? itemMap : new HashMap<String, List<T>>();
    }

    /**
     * Constructs a quadtree of a specified level and spanning a specified region.
     * <p/>
     * The number of levels in the quadtree must be specified to the constructor. The more levels there are the more
     * discriminating searches will be, but at the cost of some performance because more cells are searched. For the
     * Earth, a level count of 8 provides leaf cells about 75 km along their meridian edges (edges of constant Earth, a
     * level count of 8 provides leaf cells about 75 km along their meridian edges (edges of constant longitude).
     * Additional levels successfully halve the distance, fewer levels double that distance.
     *
     * @param numLevels       the number of levels in the quadtree. The more levels there are the more discriminating
     *                        searches will be, but at the cost of some performance.
     * @param sector          the region the tree spans.
     * @param itemMap         a {@link Map} to hold the items added to the quadtree. May be null, in which case a new
     *                        map is created.
     * @param allowDuplicates Indicates whether the collection held by this quadtree may contain duplicate entries.
     *                        Specifying <code>true</code>, which is the default, may cause an individual item to be
     *                        associated with multiple quadtree regions if the item's coordinates fall on a region
     *                        boundary. In this case that item will be returned multiple times from an iterator created
     *                        by this class. Specifying <code>false</code> prevents this.
     *
     * @throws IllegalArgumentException if <code>numLevels</code> is less than 1.
     */
    public BasicQuadTree(int numLevels, Sector sector, Map<String, List<T>> itemMap, boolean allowDuplicates)
    {
        this(numLevels, sector, itemMap);

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.allowDuplicates = allowDuplicates;

        this.makeLevelZeroCells(sector);
        this.items = itemMap != null ? itemMap : new HashMap<String, List<T>>();
    }

    /**
     * Creates the quadtree's level-zero cells.
     *
     * @param sector the sector to subdivide to create the cells.
     */
    protected void makeLevelZeroCells(Sector sector)
    {
        Sector[] subSectors = sector.subdivide();

        this.levelZeroCells = new ArrayList<double[]>(4);

        this.levelZeroCells.add(subSectors[0].asDegreesArray());
        this.levelZeroCells.add(subSectors[1].asDegreesArray());
        this.levelZeroCells.add(subSectors[3].asDegreesArray());
        this.levelZeroCells.add(subSectors[2].asDegreesArray());
    }

    /**
     * Indicates whether the tree contains any items.
     *
     * @return true if the tree contains items, otherwise false.
     */
    synchronized public boolean hasItems()
    {
        return !this.items.isEmpty();
    }

    /**
     * Indicates whether an item is contained in the tree.
     *
     * @param item the item to check. If null, false is returned.
     *
     * @return true if the item is in the tree, otherwise false.
     */
    synchronized public boolean contains(T item)
    {
        if (item == null)
            return false;

        for (Map.Entry<String, List<T>> entry : this.items.entrySet())
        {
            List<T> itemList = entry.getValue();
            if (itemList == null)
                continue;

            if (itemList.contains(item))
                return true;
        }

        return false;
    }

    /**
     * Add a named item to the quadtree. Any item duplicates are duplicated in the tree. Any name duplicates replace the
     * current name association; the name then refers to the item added.
     *
     * @param item       the item to add.
     * @param itemCoords an array specifying the region or location of the item. If the array's length is 2 it
     *                   represents a location in [latitude, longitude]. If its length is 4 it represents a region, with
     *                   the same layout as the <code>nodeRegion</code> argument.
     * @param itemName   the item name. If null, the item is added without a name.
     *
     * @throws IllegalArgumentException if either <code>item</code> or <code>itemCoords</code> is null.
     */
    synchronized public void add(T item, double[] itemCoords, String itemName)
    {
        this.addItem(item, itemCoords, itemName);
    }

    /**
     * Add an item to the quadtree. Any duplicates are duplicated in the tree.
     *
     * @param item       the item to add.
     * @param itemCoords an array specifying the region or location of the item. If the array's length is 2 it
     *                   represents a location in [latitude, longitude]. If its length is 4 it represents a region, with
     *                   the same layout as the <code>nodeRegion</code> argument.
     *
     * @throws IllegalArgumentException if either <code>item</code> or <code>itemCoords</code> is null.
     */
    synchronized public void add(T item, double[] itemCoords)
    {
        this.addItem(item, itemCoords, null);
    }

    protected void addItem(T item, double[] itemCoords, String name)
    {
        if (item == null)
        {
            String message = Logging.getMessage("nullValue.ItemIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (itemCoords == null)
        {
            String message = Logging.getMessage("nullValue.CoordinatesAreNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.currentItem = item;
        this.currentName = name;

        this.start();

        for (int i = 0; i < levelZeroCells.size(); i++)
        {
            this.testAndDo(0, i, levelZeroCells.get(i), itemCoords);
        }
    }

    /**
     * Removes an item from the tree.
     * <p/>
     * <em>Note:</em> For large collections, this can be an expensive operation.
     *
     * @param item the item to remove. If null, no item is removed.
     */
    synchronized public void remove(T item)
    {
        if (item == null)
            return;

        List<String> bitsToClear = new ArrayList<String>();

        for (Map.Entry<String, List<T>> entry : this.items.entrySet())
        {
            List<T> itemList = entry.getValue();
            if (itemList == null)
                continue;

            if (itemList.contains(item))
                itemList.remove(item);

            if (itemList.size() == 0)
                bitsToClear.add(entry.getKey());
        }

        for (String bitNum : bitsToClear)
        {
            this.bits.clear(Integer.parseInt(bitNum));
        }
    }

    /**
     * Removes an item from the tree by name.
     * <p/>
     * <em>Note:</em> For large collections, this can be an expensive operation.
     *
     * @param name the name of the item to remove. If null, no item is removed.
     */
    synchronized public void removeByName(String name)
    {
        T item = this.getByName(name);

        this.nameMap.remove(name);

        if (item == null)
            return;

        for (Map.Entry<String, List<T>> entry : this.items.entrySet())
        {
            List<T> itemList = entry.getValue();
            if (itemList == null)
                continue;

            if (itemList.contains(item))
                itemList.remove(item);
        }
    }

    /** Removes all items from the tree. */
    synchronized public void clear()
    {
        this.items.clear();
        this.bits.clear();
    }

    /**
     * Returns a named item.
     *
     * @param name the item name. If null, null is returned.
     *
     * @return the named item, or null if the item is not in the tree or the specified name is null.
     */
    synchronized public T getByName(String name)
    {
        return name != null ? this.nameMap.get(name) : null;
    }

    /**
     * Returns an iterator over the items in the tree. There is no specific iteration order and the iterator may return
     * duplicate entries.
     * <p/>
     * <em>Note</em> The {@link java.util.Iterator#remove()} operation is not supported.
     *
     * @return an iterator over the items in the tree.
     */
    synchronized public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            // The items are stored in lists associated with each cell (each bit of the bit-set), so two internal
            // iterators are needed: one for the map of populated cells and one for a cell's list of items.
            private Iterator<List<T>> mapIterator;
            private Iterator<T> listIterator;
            private T nextItem;

            { // constructor
                mapIterator = BasicQuadTree.this.items.values().iterator();
            }

            /** {@inheritDoc} **/
            public boolean hasNext()
            {
                // This is the only method that causes the list to increment, so call it before every call to next().

                if (this.nextItem != null)
                    return true;

                this.moveToNextItem();

                return this.nextItem != null;
            }

            /** {@inheritDoc} **/
            public T next()
            {
                if (!this.hasNext())
                    throw new NoSuchElementException("Iteration has no more elements.");

                T lastNext = this.nextItem;
                this.nextItem = null;
                return lastNext;
            }

            /**
             * This operation is not supported and will produce a {@link UnsupportedOperationException} if invoked.
             */
            public void remove()
            {
                throw new UnsupportedOperationException("The remove() operations is not supported by this Iterator.");
            }

            private void moveToNextItem()
            {
                // Use the next item in a cell's item list, if there is an item list and it has a next item.
                if (this.listIterator != null && this.listIterator.hasNext())
                {
                    this.nextItem = this.listIterator.next();
                    return;
                }

                // Find the next map entry with a non-null item list. Use the first item in that list.
                this.listIterator = null;
                while (this.mapIterator.hasNext())
                {
                    if (this.mapIterator.hasNext())
                        this.listIterator = this.mapIterator.next().iterator();

                    if (this.listIterator != null && this.listIterator.hasNext())
                    {
                        this.nextItem = this.listIterator.next();
                        return;
                    }
                }
            }
        };
    }

    /**
     * Finds and returns the items within a tree cell containing a specified location.
     *
     * @param location the location of interest.
     * @param outItems a {@link Set} in which to place the items. If null, a new set is created.
     *
     * @return the set of intersecting items. The same set passed as the <code>outItems</code> argument is returned, or
     *         a new set if that argument is null.
     *
     * @throws IllegalArgumentException if <code>location</code> is null.
     */
    synchronized public Set<T> getItemsAtLocation(LatLon location, Set<T> outItems)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        FindIntersectingBitsOp op = new FindIntersectingBitsOp(this);

        List<Integer> bitIds = op.getOnBits(this.levelZeroCells, location.asDegreesArray(), new ArrayList<Integer>());

        return this.buildItemSet(bitIds, outItems);
    }

    /**
     * Finds and returns the items within tree cells containing specified locations.
     *
     * @param locations the locations of interest.
     * @param outItems  a {@link Set} in which to place the items. If null, a new set is created.
     *
     * @return the set of intersecting items. The same set passed as the <code>outItems</code> argument is returned, or
     *         a new set if that argument is null.
     *
     * @throws IllegalArgumentException if <code>locations</code> is null.
     */
    synchronized public Set<T> getItemsAtLocation(Iterable<LatLon> locations, Set<T> outItems)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LatLonListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        FindIntersectingBitsOp op = new FindIntersectingBitsOp(this);

        List<Integer> bitIds = new ArrayList<Integer>();
        for (LatLon location : locations)
        {
            if (location != null)
                bitIds = op.getOnBits(this.levelZeroCells, location.asDegreesArray(), bitIds);
        }

        return this.buildItemSet(bitIds, outItems);
    }

    /**
     * Finds and returns the items intersecting a specified sector.
     *
     * @param testSector the sector of interest.
     * @param outItems   a {@link Set} in which to place the items. If null, a new set is created.
     *
     * @return the set of intersecting items. The same set passed as the <code>outItems</code> argument is returned, or
     *         a new set if that argument is null.
     *
     * @throws IllegalArgumentException if <code>testSector</code> is null.
     */
    synchronized public Set<T> getItemsInRegion(Sector testSector, Set<T> outItems)
    {
        if (testSector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        FindIntersectingBitsOp op = new FindIntersectingBitsOp(this);

        List<Integer> bitIds = op.getOnBits(this.levelZeroCells, testSector, new ArrayList<Integer>());

        return this.buildItemSet(bitIds, outItems);
    }

    /**
     * Finds and returns the items intersecting a specified collection of sectors.
     *
     * @param testSectors the sectors of interest.
     * @param outItems    a {@link Set} in which to place the items. If null, a new set is created.
     *
     * @return the set of intersecting items. The same set passed as the <code>outItems</code> argument is returned, or
     *         a new set if that argument is null.
     *
     * @throws IllegalArgumentException if <code>testSectors</code> is null.
     */
    public Set<T> getItemsInRegions(Iterable<Sector> testSectors, Set<T> outItems)
    {
        if (testSectors == null)
        {
            String message = Logging.getMessage("nullValue.SectorListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        FindIntersectingBitsOp op = new FindIntersectingBitsOp(this);

        List<Integer> bitIds = new ArrayList<Integer>();
        for (Sector testSector : testSectors)
        {
            if (testSector != null)
                bitIds = op.getOnBits(this.levelZeroCells, testSector, bitIds);
        }

        return this.buildItemSet(bitIds, outItems);
    }

    /**
     * Finds and returns the items intersecting a specified collection of {@link gov.nasa.worldwind.terrain.SectorGeometry}.
     * This method is a convenience for finding the items intersecting the current visible regions.
     *
     * @param geometryList the list of sector geometry.
     * @param outItems     a {@link Set} in which to place the items. If null, a new set is created.
     *
     * @return the set of intersecting items. The same set passed as the <code>outItems</code> argument is returned, or
     *         a new set if that argument is null.
     *
     * @throws IllegalArgumentException if <code>geometryList</code> is null.
     */
    synchronized public Set<T> getItemsInRegions(SectorGeometryList geometryList, Set<T> outItems)
    {
        if (geometryList == null)
        {
            String message = Logging.getMessage("nullValue.SectorGeometryListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        FindIntersectingBitsOp op = new FindIntersectingBitsOp(this);

        List<Integer> bitIds = new ArrayList<Integer>();
        for (SectorGeometry testSector : geometryList)
        {
            if (testSector != null)
                bitIds = op.getOnBits(this.levelZeroCells, testSector.getSector(), bitIds);
        }

        return this.buildItemSet(bitIds, outItems);
    }

    /**
     * Adds the items identified by a list of bit IDs to the set returned by the get methods.
     *
     * @param bitIds   the bit numbers of the cells containing the items to return.
     * @param outItems a {@link Set} in which to place the items. If null, a new set is created.
     *
     * @return the set of items. The value passed as the <code>outItems</code> is returned.
     */
    protected Set<T> buildItemSet(List<Integer> bitIds, Set<T> outItems)
    {
        if (outItems == null)
            outItems = new HashSet<T>();

        if (bitIds == null)
            return outItems;

        for (Integer id : bitIds)
        {
            List<T> regionItems = this.items.get(id.toString());
            if (regionItems == null)
                continue;

            for (T item : regionItems)
            {
                outItems.add(item);
            }
        }

        return outItems;
    }

    /**
     * Performs the add operation of the quadtree.
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
    protected boolean doOperation(int level, int position, double[] cellRegion, double[] itemCoords)
    {
        int bitNum = this.computeBitPosition(level, position);

        this.bits.set(bitNum);

        if (level < this.maxLevel)
            return true;

        String bitName = Integer.toString(bitNum);

        List<T> regionItems = this.items.get(bitName);
        if (regionItems == null)
        {
            regionItems = new ArrayList<T>();
            this.items.put(bitName, regionItems);
        }

        regionItems.add(this.currentItem);

        if (this.currentName != null)
            this.nameMap.put(this.currentName, this.currentItem);

        if (!this.allowDuplicates)
            this.stop();

        return false;
    }

    ////////////////////// ONLY TEST CODE BELOW ////////////////////////////////////////////

//    public static void main(String[] args)
//    {
////        basicTest();
////        iteratorTest();
////        perfTestVisit();
//        perfTestFind();
//    }
//
//    public abstract static class PerfTestRunner
//    {
//        protected abstract void doOp();
//
//        protected int numIterations;
//
//        protected long run(int numIterations)
//        {
//            this.numIterations = numIterations;
//            long start = System.currentTimeMillis();
//
//            for (int i = 0; i < numIterations; i++)
//            {
//                this.doOp();
//            }
//
//            return System.currentTimeMillis() - start;
//        }
//
//        public void print(long elapsedTime)
//        {
//            System.out.printf("Elapsed time for %d iterations = %d milliseconds\n", this.numIterations, elapsedTime);
//        }
//    }
//
//    protected static void basicTest()
//    {
//        final int treeDepth = 8;
//
//        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
//        final BasicQuadTree<String> tree = new BasicQuadTree<String>(treeDepth, Sector.FULL_SPHERE, map);
//
//        LatLon ll = LatLon.fromDegrees(0, 0);
//        tree.add(ll.toString(), ll.asDegreesArray());
//
//        ll = LatLon.fromDegrees(40, 50);
//        tree.add(ll.toString(), ll.asDegreesArray());
//
//        ll = LatLon.fromDegrees(-60, -80);
//        tree.add(ll.toString(), ll.asDegreesArray());
//
//        Set<String> items = tree.getItemsInRegion(Sector.fromDegrees(0, 45, 0, 55), new HashSet<String>());
//
//        System.out.printf("Tree depth %d, %d items found\n", treeDepth, items.size());
//
//        for (String item : items)
//        {
//            System.out.println(item);
//        }
//    }
//
//    protected static void perfTestVisit()
//    {
//        VisitOp filter = new VisitOp<String>(10, Sector.FULL_SPHERE);
//
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 10; i++)
//        {
//            filter.visit(Sector.FULL_SPHERE);
//        }
//        long end = System.currentTimeMillis();
//
//        System.out.println(
//            "DONE " + filter.bits.cardinality() + " bits set in " + (end - start) / 10 + " milliseconds");
//    }

    //
//
//    protected static void perfTestFind()
//    {
//        final int treeDepth = 8;
//
//        Map<String, List<String>> map = new HashMap<String, List<String>>();
//        final BasicQuadTree<String> tree = new BasicQuadTree<String>(treeDepth, Sector.FULL_SPHERE, map);
//
//        LatLon ll = LatLon.fromDegrees(0, 0);
//        tree.add(ll.toString(), ll.asDegreesArray());
//
//        ll = LatLon.fromDegrees(40, 50);
//        tree.add(ll.toString(), ll.asDegreesArray());
//
//        ll = LatLon.fromDegrees(-60, -80);
//        tree.add(ll.toString(), ll.asDegreesArray());
//
//        int count = 0;
//        for (double lat = -45; lat <= 45; lat += 0.25)
//        {
//            for (double lon = 6; lon <= 100; lon += 0.25)
//            {
//                ll = LatLon.fromDegrees(lat, lon);
//                tree.add(ll.toString(), ll.asDegreesArray());
//                ++count;
////                if (lat >= 1 && lat <= 5 && lon >= 1 && lon <= 5)
////                    System.out.println(ll);
//            }
//        }
//
//        final int finalCount = count;
//        PerfTestRunner runner = new PerfTestRunner()
//        {
//            public Set<String> items;
//
//            protected void doOp()
//            {
//                this.items = tree.getItemsInRegion(Sector.fromDegrees(-22, 22, 6, 54), new HashSet<String>());
//            }
//
//            @Override
//            public void print(long elapsedMillis)
//            {
//                System.out.printf("Tree depth %d, %d locations in tree, %d items found\n",
//                    treeDepth, finalCount, items.size());
//                super.print(elapsedMillis);
//
////                for (String item : this.items)
////                {
////                    System.out.println(item);
////                }
//            }
//        };
//        System.out.println("STARTING");
//        runner.print(runner.run(100));
//    }
//
//    protected static void iteratorTest()
//    {
//        final int treeDepth = 8;
//
//        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
//        final BasicQuadTree<String> tree = new BasicQuadTree<String>(treeDepth, Sector.FULL_SPHERE, map);
//
//        int count = 0;
//        for (double lat = -5; lat <= 5; lat += 1)
//        {
//            for (double lon = 5; lon <= 10; lon += 1)
//            {
//                LatLon ll = LatLon.fromDegrees(lat, lon);
//                tree.add(ll.toString(), ll.asDegreesArray());
//                ++count;
//            }
//        }
//
//        int actualCount = 0;
//        for (String s : tree)
//        {
//            ++actualCount;
//            System.out.println(s);
//        }
//
//        System.out.printf("Actual count (%d) == Total count (%d): %b\n", actualCount, count, (actualCount == count));
//    }
//
//    /**
//     * A test class that prints the set bits in the quadtree.
//     * @param <T> the item type.
//     */
//    protected static class PrintBitsOp<T> extends BasicQuadTree<T>
//    {
//        public PrintBitsOp(int maxLevel)
//        {
//            super(maxLevel, Sector.FULL_SPHERE, null);
//        }
//
//        protected boolean doOperation(int level, int position, double[] nodeRegion, double[] testRegion)
//        {
//            int bitNum = this.computeBitPosition(level, position);
//
//            this.bits.set(bitNum);
//
//            System.out.printf("Level %d, %s, Position %d\n", level, this.makePathString(this.path, level), bitNum);
//
//            return level < this.maxLevel;
//        }
//
//        private String makePathString(int[] path, int endIndex)
//        {
//            StringBuilder sb = new StringBuilder();
//
//            for (int i = 0; i <= endIndex; i++)
//            {
//                sb.append(Integer.toString(path[i]));
//            }
//
//            return sb.toString();
//        }
//
//        public void printBits()
//        {
//            for (int i = 0; i < levelZeroCells.size(); i++)
//            {
//                this.testAndDo(0, i, levelZeroCells.get(i), Sector.FULL_SPHERE.asDegreesArray());
//            }
//        }
//    }
//
//    /**
//     * A test class that visits all the cells in the quadtree and sets their bit in the bit-set.
//     * @param <T> the item type.
//     */
//    protected static class VisitOp<T> extends BasicQuadTree<T>
//    {
//        public VisitOp(int maxLevel, Sector region)
//        {
//            super(maxLevel, region, null);
//        }
//
//        protected boolean doOperation(int level, int position, double[] nodeRegion, double[] testRegion)
//        {
//            int bitNum = this.computeBitPosition(level, position);
//
//            this.bits.set(bitNum);
//
//            return level < this.maxLevel;
//        }
//
//        public void visit(Sector sector)
//        {
//            for (int i = 0; i < levelZeroCells.size(); i++)
//            {
//                this.testAndDo(0, i, levelZeroCells.get(i), sector.asDegreesArray());
//            }
//        }
//    }
}
