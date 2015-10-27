/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;

import java.util.*;

/**
 * Generates contour lines at threshold values in a rectangular array of numeric values. ContourBuilder differs from the
 * ContourLine renderable shape in that ContourBuilder can compute the coordinates of contour lines within arbitrary
 * two-dimensional scalar data, whereas the ContourLine shape operates only on elevation values associated with a World
 * Wind globe. Note that ContourBuilder can be used to compute contour line coordinates within a rectangular array of
 * elevation values.
 * <p/>
 * ContourBuilder operates on a caller specified rectangular array. The array is specified as a one dimensional array of
 * floating point numbers, and is understood to be organized in row-major order, with the first index indicating the
 * value at the rectangle's upper-left corner. The domain of array values is any value that fits in a 64-bit floating
 * point number.
 * <p/>
 * Contour lines may be computed at any threshold value (i.e. isovalue) by calling {@link #buildContourLines(double)} or
 * {@link #buildContourLines(double, gov.nasa.worldwind.geom.Sector, double)}. The latter method maps contour line
 * coordinates to geographic positions by associating the rectangular array with a geographic sector. It is valid to
 * compute contour lines for a threshold value that is less than the rectangular array's minimum value or greater than
 * the rectangular array's maximum value, though the result is an empty list of contour lines. The domain of contour
 * line coordinates is the XY Cartesian space defined by the rectangular array's width and height. X coordinates range
 * from 0 to width-1, and Y coordinates range from 0 to height-1.
 *
 * @author dcollins
 * @version $Id: ContourBuilder.java 2436 2014-11-14 23:20:50Z danm $
 */
public class ContourBuilder
{
    protected static class CellInfo
    {
        public final int x;
        public final int y;
        public final int contourMask;
        public final Map<Direction, Double> edgeWeights = new HashMap<Direction, Double>();
        public final Set<Direction> visitedDirections = new HashSet<Direction>(4);

        public CellInfo(int x, int y, int contourMask)
        {
            this.x = x;
            this.y = y;
            this.contourMask = contourMask;
        }
    }

    protected static class CellKey
    {
        public final int x;
        public final int y;

        public CellKey(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            CellKey that = (CellKey) o;
            return this.x == that.x && this.y == that.y;
        }

        @Override
        public int hashCode()
        {
            return 31 * this.x + this.y;
        }
    }

    protected static enum Direction
    {
        NORTH, SOUTH, EAST, WEST
    }

    protected int width;
    protected int height;
    protected double[] values;
    protected Map<CellKey, CellInfo> contourCellMap = new HashMap<CellKey, CellInfo>();
    protected List<CellKey> contourCellList = new ArrayList<CellKey>();
    protected List<List<double[]>> contourList = new ArrayList<List<double[]>>();
    protected List<double[]> currentContour;

    protected static Map<Direction, Direction> dirRev = new HashMap<Direction, Direction>();
    protected static Map<Integer, LinkedHashMap<Direction, Direction>> dirNext
        = new HashMap<Integer, LinkedHashMap<Direction, Direction>>();

    static
    {
        dirRev.put(Direction.NORTH, Direction.SOUTH);
        dirRev.put(Direction.SOUTH, Direction.NORTH);
        dirRev.put(Direction.EAST, Direction.WEST);
        dirRev.put(Direction.WEST, Direction.EAST);

        // Use LinkedHaspMap to store the maps in dirNext in order to preserve enumeration order. The method
        // traverseContourCells requires that the directions are enumerated in the order listed here.
        LinkedHashMap<Direction, Direction> map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.SOUTH, Direction.WEST);
        map.put(Direction.WEST, Direction.SOUTH);
        dirNext.put(1, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.SOUTH, Direction.EAST);
        map.put(Direction.EAST, Direction.SOUTH);
        dirNext.put(2, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.EAST, Direction.WEST);
        map.put(Direction.WEST, Direction.EAST);
        dirNext.put(3, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.EAST);
        map.put(Direction.EAST, Direction.NORTH);
        dirNext.put(4, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.WEST);
        map.put(Direction.WEST, Direction.NORTH);
        map.put(Direction.SOUTH, Direction.EAST);
        map.put(Direction.EAST, Direction.SOUTH);
        dirNext.put(5, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.SOUTH);
        map.put(Direction.SOUTH, Direction.NORTH);
        dirNext.put(6, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.WEST);
        map.put(Direction.WEST, Direction.NORTH);
        dirNext.put(7, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.WEST);
        map.put(Direction.WEST, Direction.NORTH);
        dirNext.put(8, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.SOUTH);
        map.put(Direction.SOUTH, Direction.NORTH);
        dirNext.put(9, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.EAST);
        map.put(Direction.EAST, Direction.NORTH);
        map.put(Direction.SOUTH, Direction.WEST);
        map.put(Direction.WEST, Direction.SOUTH);
        dirNext.put(10, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.NORTH, Direction.EAST);
        map.put(Direction.EAST, Direction.NORTH);
        dirNext.put(11, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.EAST, Direction.WEST);
        map.put(Direction.WEST, Direction.EAST);
        dirNext.put(12, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.SOUTH, Direction.EAST);
        map.put(Direction.EAST, Direction.SOUTH);
        dirNext.put(13, map);

        map = new LinkedHashMap<Direction, Direction>();
        map.put(Direction.SOUTH, Direction.WEST);
        map.put(Direction.WEST, Direction.SOUTH);
        dirNext.put(14, map);
    }

    /**
     * Creates a new ContourBuilder with the specified rectangular array arguments. The array is understood to be
     * organized in row-major order, with the first index indicating the value at the rectangle's upper-left corner.
     *
     * @param width  the rectangular array width.
     * @param height the rectangular array height.
     * @param values the rectangular array values, as a one-dimensional array. Must contain at least width * height
     *               values. This array is understood to be organized in row-major order, with the first index
     *               indicating the value at the rectangle's upper-left corner.
     *
     * @throws java.lang.IllegalArgumentException if either the width or the height are less than 1, if the array is
     *                                            null, or if the array length is insufficient for the specified width
     *                                            and height.
     */
    public ContourBuilder(int width, int height, double[] values)
    {
        if (width < 1)
        {
            String msg = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (height < 1)
        {
            String msg = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (values == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (values.length != width * height)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", values.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.width = width;
        this.height = height;
        this.values = values;
    }

    /**
     * Computes the contour lines at a specified threshold value. The returned list represents a collection of
     * individual geographic polylines, which may or may not represent a closed loop. Each polyline is represented as a
     * list of two-element arrays, with the X coordinate at index 0 and the Y coordinate at index 1. The domain of
     * contour line coordinates is the XY Cartesian space defined by the rectangular array's width and height. X
     * coordinates range from 0 to width-1, and Y coordinates range from 0 to height-1.
     * <p/>
     * <p/>
     * This returns an empty list if there are no contour lines associated with the value. This occurs when the value is
     * less than the rectangular array's minimum value, or when the value is greater than the rectangular array's
     * maximum value.
     *
     * @param value the threshold value (i.e. isovalue) to compute contour lines for.
     *
     * @return a list containing the contour lines for the threshold value.
     */
    public List<List<double[]>> buildContourLines(double value)
    {
        this.assembleContourCells(value);
        this.traverseContourCells();

        List<List<double[]>> result = new ArrayList<List<double[]>>(this.contourList); // return a copy to insulate from changes

        this.clearContourCells(); // clears contourList

        return result;
    }

    /**
     * Computes the geographic contour lines at a specified threshold value. The returned list represents a collection
     * of individual geographic polylines, which may or may not represent a closed loop. This maps contour line
     * coordinates to geographic positions by associating the rectangular array with a geographic sector. The array's
     * upper left corner is mapped to the sector's Northwest corner, and the array's lower right corner is mapped to the
     * sector's Southeast corner.
     * <p/>
     * The domain of contour line coordinates is the geographic space defined by the specified sector. Prior to the
     * mapping into geographic coordinates, contour line X coordinates range from 0 to width-1, and Y coordinates range
     * from 0 to height-1. After the mapping into geographic coordinates, contour line X coordinates range from
     * sector.getMinLongitude() to sector.getMaxLongitude(), and Y coordinates range from sector.getMaxLatitude() to
     * sector.getMinLatitude().
     * <p/>
     * This returns an empty list if there are no contour lines associated with the value. This occurs when the value is
     * less than the rectangular array's minimum value, or when the value is greater than the rectangular array's
     * maximum value.
     *
     * @param value    the threshold value (i.e. isovalue) to compute contour lines for.
     * @param sector   the sector to associate with the rectangular array. The array's upper left corner is mapped to
     *                 the sector's Northwest corner, and the array's lower right corner is mapped to the sector's
     *                 Southeast corner.
     * @param altitude the altitude to assign to the geographic positions.
     *
     * @return a list containing the geographic contour lines for the threshold value.
     *
     * @throws java.lang.IllegalArgumentException if the sector is null.
     */
    public List<List<Position>> buildContourLines(double value, Sector sector, double altitude)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.assembleContourCells(value);
        this.traverseContourCells();

        double maxLat = sector.getMaxLatitude().degrees;
        double minLon = sector.getMinLongitude().degrees;
        double deltaLat = sector.getDeltaLatDegrees();
        double deltaLon = sector.getDeltaLonDegrees();

        List<List<Position>> result = new ArrayList<List<Position>>();

        for (List<double[]> coordList : this.contourList)
        {
            ArrayList<Position> positionList = new ArrayList<Position>();

            for (double[] coord : coordList)
            {
                double s = coord[0] / (this.width - 1); // normalized x coordinate in the range 0 to 1
                double t = coord[1] / (this.height - 1); // normalized y coordinate in the range 0 to 1
                double lat = maxLat - t * deltaLat; // map y coordinate to latitude
                double lon = minLon + s * deltaLon; // map x coordinate to longitude
                positionList.add(Position.fromDegrees(lat, lon, altitude));
            }

            result.add(positionList);
        }

        this.clearContourCells(); // clears contourList

        return result;
    }

    protected void assembleContourCells(double value)
    {
        // Divide the 2D scalar field into a grid of evenly spaced contouring cells. Every 2x2 block of field values
        // forms a cell. The contouring grid's dimensions are therefore one less than the 2D scalar field. Based on
        // the approach outlined at http://en.wikipedia.org/wiki/Marching_squares

        this.contourCellMap.clear();
        this.contourCellList.clear();

        for (int y = 0; y < this.height - 1; y++)
        {
            for (int x = 0; x < this.width - 1; x++)
            {
                // Get the field values associated with the contouring cell's four corners.
                double nw = this.valueFor(x, y);
                double ne = this.valueFor(x + 1, y);
                double se = this.valueFor(x + 1, y + 1);
                double sw = this.valueFor(x, y + 1);

                // Assemble a 4-bit mask indicating whether or not the field values at the cell's corners are above or
                // below the threshold. The mask has 1 where the field value is above the threshold, and 0 otherwise.
                int mask = 0;
                mask |= (nw > value) ? 1 : 0; // 1000
                mask <<= 1;
                mask |= (ne > value) ? 1 : 0; // 0100
                mask <<= 1;
                mask |= (se > value) ? 1 : 0; // 0010
                mask <<= 1;
                mask |= (sw > value) ? 1 : 0; // 0001

                if (mask == 0 || mask == 15)
                    continue; // no contour; all values above or below the threshold value

                // Disambiguate saddle point for masks 0x0101 and 0x1010, per Wikipedia page suggestion.
                if (mask == 5 || mask == 10)
                {
                    double ctr = (nw + ne + se + sw) / 4; // sample center value as the average of four corners
                    if (mask == 5 && ctr <= value) // center value causes change in direction; flip the mask to 10
                        mask = 10;
                    else if (mask == 10 && ctr <= value) // center value causes change in direction; flip the mask to 5
                        mask = 5;
                }

                CellInfo cell = new CellInfo(x, y, mask);

                // Compute weights associated with edge intersections.
                if ((ne > value) ^ (nw > value))
                    cell.edgeWeights.put(Direction.NORTH, (value - nw) / (ne - nw));
                if ((se > value) ^ (sw > value))
                    cell.edgeWeights.put(Direction.SOUTH, (value - sw) / (se - sw));
                if ((se > value) ^ (ne > value))
                    cell.edgeWeights.put(Direction.EAST, (value - ne) / (se - ne));
                if ((sw > value) ^ (nw > value))
                    cell.edgeWeights.put(Direction.WEST, (value - nw) / (sw - nw));

                this.putContourCell(cell);
            }
        }
    }

    protected void traverseContourCells()
    {
        List<List<double[]>> contours = new ArrayList<List<double[]>>();

        this.contourList.clear();

        for (CellKey key : this.contourCellList) // iterate over all possible contour starting points
        {
            CellInfo cell = this.contourCellMap.get(key);

            for (Direction dir : dirNext.get(cell.contourMask).keySet()) // either 2 or 4 starting directions
            {
                if (cell.visitedDirections.contains(dir))
                {
                    continue;
                }

                this.currentContour = new ArrayList<double[]>();
                this.traverseContour(cell, dir);
                contours.add(this.currentContour);
                this.currentContour = null;

                if (contours.size() == 2) // combine each pair of starting directions into a single polyline
                {
                    if (contours.get(0).size() == 0 && contours.get(1).size() == 0)
                    {
                        String msg = Logging.getMessage("generic.UnexpectedCondition", "both contours are of zero length");
                        Logging.logger().severe(msg);
                    }
                    else
                    {
                        Collections.reverse(contours.get(0));
                        contours.get(0).addAll(contours.get(1));
                        this.contourList.add(contours.get(0));
                    }

                    contours.clear();
                }
            }

            if (contours.size() != 0)
            {
                String msg = Logging.getMessage("generic.UnexpectedCondition", "non-empty contours list");
                Logging.logger().severe(msg);
            }
        }
    }

    protected void traverseContour(CellInfo cell, Direction dir)
    {
        Direction dirNext = dir;
        Direction dirPrev = dir;  // use Prev same as Next for first iteration (i.e., for seed cell)

        while (cell != null && !cell.visitedDirections.contains(dirNext))
        {
            // Mark the contour cell as visited.
            cell.visitedDirections.add(dirNext);
            cell.visitedDirections.add(dirPrev);

            addIntersection(cell, dirNext);

            // Advance to the next cell.
            cell = this.nextCell(cell, dirNext);

            // guard cell use in computing dirNext
            if (cell != null)
            {
                // Advance to the next direction.
                dirPrev = ContourBuilder.dirRev.get(dirNext);
                dirNext = ContourBuilder.dirNext.get(cell.contourMask).get(dirPrev);
            }
        }
    }

    protected void addIntersection(CellInfo cell, Direction dir)
    {
        // Compute the intersection of the contour cell in the next direction. The cell's xy coordinates initially
        // indicate the cell's Southwest corner.
        double xIntersect = cell.x;
        double yIntersect = cell.y;

        switch (dir)
        {
            case NORTH:
                xIntersect += cell.edgeWeights.get(dir); // interpolate along the north edge
                break;
            case SOUTH:
                xIntersect += cell.edgeWeights.get(dir); // interpolate along the south edge
                yIntersect += 1; // move from the north to the south
                break;
            case EAST:
                xIntersect += 1; // move from the west to the east
                yIntersect += cell.edgeWeights.get(dir); // interpolate along the east edge
                break;
            case WEST:
                yIntersect += cell.edgeWeights.get(dir); // interpolate along the west edge
                break;
            default:
                String msg = Logging.getMessage("generic.UnexpectedDirection", dirNext);
                Logging.logger().severe(msg);
                break;
        }

        this.currentContour.add(new double[] {xIntersect, yIntersect});
    }

    protected void clearContourCells()
    {
        this.contourCellMap.clear();
        this.contourCellList.clear();
        this.contourList.clear();
        this.currentContour = null;
    }

    protected CellInfo nextCell(CellInfo cell, Direction dir)
    {
        int x = cell.x;
        int y = cell.y;

        switch (dir)
        {
            case NORTH:
                return this.getContourCell(x, y - 1);
            case SOUTH:
                return this.getContourCell(x, y + 1);
            case EAST:
                return this.getContourCell(x + 1, y);
            case WEST:
                return this.getContourCell(x - 1, y);
            default:
                String msg = Logging.getMessage("generic.UnexpectedDirection", dirNext);
                Logging.logger().severe(msg);
                return null;
        }
    }

    protected double valueFor(int x, int y)
    {
        return this.values[x + y * this.width];
    }

    protected void putContourCell(CellInfo cell)
    {
        CellKey key = new CellKey(cell.x, cell.y);
        this.contourCellMap.put(key, cell);
        this.contourCellList.add(key);
    }

    protected CellInfo getContourCell(int x, int y)
    {
        return this.contourCellMap.get(new CellKey(x, y));
    }
}
