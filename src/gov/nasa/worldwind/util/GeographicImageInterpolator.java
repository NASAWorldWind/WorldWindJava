/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;

import java.awt.*;

/**
 * GeographicImageInterpolator extends the functionality of {@link gov.nasa.worldwind.util.ImageInterpolator} to
 * correctly map from geographic coordinates to image coordinates. Unlike its superclass, which works in Cartesian
 * coordinates, GeographicImageInterpolator handles the singularities of geographic coordinates. For example,
 * GeographicImageInterpolator can map images which cross the international dateline.
 *
 * @author dcollins
 * @version $Id: GeographicImageInterpolator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeographicImageInterpolator extends ImageInterpolator
{
    /**
     * GeographicCell extends {@link gov.nasa.worldwind.util.ImageInterpolator.Cell} to correctly handle image cells
     * which have geographic coordinates. Unlike its superclass, which works in Cartesian coordinates, GeographicCell
     * handles the singularities of geographic coordinates, such as image cells which cross the international dateline.
     */
    protected static class GeographicCell extends Cell
    {
        /** Denotes if the pixels in this geographic image cell crosses the international dateline. */
        protected boolean crossesDateline;

        /**
         * Constructs a new Geographic Cell, but otherwise does nothing.
         *
         * @param m0 the cell's left image coordinate.
         * @param m1 the cell's right image coordinate.
         * @param n0 the cell's bottom image coordinate.
         * @param n1 the cell's top image coordinate.
         */
        public GeographicCell(int m0, int m1, int n0, int n1)
        {
            super(m0, m1, n0, n1);
        }

        /**
         * Overridden to create a {@link gov.nasa.worldwind.util.GeographicImageInterpolator.GeographicCell}.
         *
         * @param m0 the cell's left image coordinate.
         * @param m1 the cell's right image coordinate.
         * @param n0 the cell's bottom image coordinate.
         * @param n1 the cell's top image coordinate.
         *
         * @return a new GeographicCell with the specified image coordinates.
         */
        @Override
        protected Cell makeChildCell(int m0, int m1, int n0, int n1)
        {
            return new GeographicCell(m0, m1, n0, n1);
        }

        /**
         * Returns true if this image cell crosses the international dateline, and false otherwise.
         *
         * @return true if this cell crosses the international dateline; false otherwise.
         */
        public boolean isCrossesDateline()
        {
            return this.crossesDateline;
        }

        /**
         * Overridden to correctly compute intersection for cells which cross the international dateline. If the
         * specified cell does not cross the dateline, this invokes the superclass' functionality.
         *
         * @param x the x-component to test for intersection with this cell.
         * @param y the y-component to test for intersection with this cell.
         *
         * @return true if the (x, y) point intersects this cell; false otherwise.
         */
        @Override
        public boolean intersects(float x, float y)
        {
            // Invoke the superclass functionality if this cell doesn't cross the international dateline.
            if (!this.isCrossesDateline())
            {
                return super.intersects(x, y);
            }

            // The cell crosses the international dateline. The cell's minx and maxx define its extremes to either side
            // of the dateline. minx is the extreme value in the eastern hemisphere, and maxx is the extreme value in
            // the western hemisphere.
            return ((x >= this.minx && x <= 180f) || (x >= -180f && x <= this.maxx))
                && y >= this.miny && y <= this.maxy;
        }

        /**
         * Overridden to correctly compute the extremes for leaf cells which cross the international dateline. If this
         * cell does not cross the dateline, this invokes the superclass' functionality.
         *
         * @param dim the image's dimensions.
         * @param xs  the x-coordinates of the image's pixels. Must contain at least <code>gridSize.width *
         *            gridSize.height</code> elements.
         * @param ys  the y-coordinates of the image's pixels. Must contain at least <code>gridSize.width *
         *            gridSize.height</code> elements.
         */
        @Override
        protected void computeExtremesFromLocations(Dimension dim, float[] xs, float[] ys)
        {
            // Invoke the superclass functionality if this cell doesn't cross the international dateline.
            if (!this.longitudesCrossDateline(dim, xs))
            {
                super.computeExtremesFromLocations(dim, xs, ys);
                return;
            }

            // The cell crosses the international dateline. The cell's minx and maxx define its extremes to either side
            // of the dateline. minx is the extreme value in the eastern hemisphere, and maxx is the extreme value in
            // the western hemisphere. Therefore minx and maxx are initialized with values nearest to the dateline.
            this.minx = 180f;
            this.maxx = -180f;
            this.miny = Float.MAX_VALUE;
            this.maxy = -Float.MAX_VALUE;
            this.crossesDateline = true;

            // Assume that dateline crossing cells span the shorter of two possible paths around the globe. Therefore
            // each location contributes to the extreme in its hemisphere. minx is the furthest value from the dateline
            // in the eastern hemisphere. maxx is the furthest value from the dateline in the western hemisphere.
            for (int j = this.n0; j <= this.n1; j++)
            {
                for (int i = this.m0; i <= this.m1; i++)
                {
                    int k = j * dim.width + i;
                    float x = xs[k];
                    float y = ys[k];

                    if (this.minx > x && x > 0f)
                        this.minx = x;
                    if (this.maxx < x && x < 0f)
                        this.maxx = x;

                    if (this.miny > y)
                        this.miny = y;
                    if (this.maxy < y)
                        this.maxy = y;
                }
            }
        }

        /**
         * Overridden to correctly compute the extremes for parent cells who's children cross the international
         * dateline. If the this cell does not cross the dateline, this invokes the superclass' functionality.
         */
        @Override
        protected void computeExtremesFromChildren()
        {
            // Invoke the superclass functionality if this cell doesn't cross the international dateline.
            if (!this.childrenCrossDateline())
            {
                super.computeExtremesFromChildren();
                return;
            }

            // The cell crosses the international dateline. The cell's minx and maxx define its extremes to either side
            // of the dateline. minx is the extreme value in the eastern hemisphere, and maxx is the extreme value in
            // the western hemisphere. Therefore minx and maxx are initialized with values nearest to the dateline.
            this.minx = 180f;
            this.maxx = -180f;
            this.miny = Float.MAX_VALUE;
            this.maxy = -Float.MAX_VALUE;
            this.crossesDateline = true;

            // Assume that dateline crossing cells span the shorter of two possible paths around the globe. Therefore
            // each location contributes to the extreme in its hemisphere. minx is the furthest value from the dateline
            // in the eastern hemisphere. maxx is the furthest value from the dateline in the western hemisphere.
            for (Cell t : this.children)
            {
                // The child cell crosses the dateline. This cell's minx and maxx have the same meaning as the child
                // cell, so a simple comparison determines this cell's extreme x values.
                if (((GeographicCell) t).isCrossesDateline())
                {
                    if (this.minx > t.minx)
                        this.minx = t.minx;
                    if (this.maxx < t.maxx)
                        this.maxx = t.maxx;
                }
                // The child cell doesn't cross the dateline. This cell's minx and maxx have different meaning than the
                // child cell. If the child cell is entirely contained within either the eastern or western hemisphere,
                // we adjust this cell's minx or maxx to include it. If the child cell spans the prime meridian, this
                // cell's minx and maxx must extent to the prime meridian to include it.
                else
                {
                    if (this.minx > t.minx && t.minx > 0f) // Cell is entirely within the eastern hemisphere.
                        this.minx = t.minx;
                    if (this.maxx < t.maxx && t.maxx < 0f) // Cell is entirely within the western hemisphere.
                        this.maxx = t.maxx;
                    if (t.minx <= 0f && t.maxx >= 0f) // Cell is in both the western and eastern hemispheres.
                        this.minx = this.maxx = 0f;
                }

                if (this.miny > t.miny)
                    this.miny = t.miny;
                if (this.maxy < t.maxy)
                    this.maxy = t.maxy;
            }
        }

        /**
         * Returns true if a line segment from the first pixel in this cell to any other pixel in this cell crosses the
         * international dateline, and false otherwise.
         *
         * @param dim        the image's dimensions.
         * @param longitudes the longitude coordinates of the image's pixels in degrees. Must contain at least
         *                   <code>dim.width * dim.height</code> elements.
         *
         * @return true if this image cell's crosses the international dateline; false otherwise.
         */
        protected boolean longitudesCrossDateline(Dimension dim, float[] longitudes)
        {
            Float x1 = null;

            for (int j = this.n0; j <= this.n1; j++)
            {
                for (int i = this.m0; i <= this.m1; i++)
                {
                    int k = j * dim.width + i;
                    float x2 = longitudes[k];

                    if (x1 != null)
                    {
                        // A segment cross the line if end pos have different longitude signs
                        // and are more than 180 degress longitude apart
                        if (Math.signum(x1) != Math.signum(x2))
                        {
                            float delta = Math.abs(x1 - x2);
                            if (delta > 180f && delta < 360f)
                                return true;
                        }
                    }

                    x1 = x2;
                }
            }

            return false;
        }

        /**
         * Returns true if any of this image cell's children cross the international dateline, and false otherwise.
         * Returns false if this image cell has no children.
         *
         * @return true if any children cross the international dateline; false otherwise.
         */
        protected boolean childrenCrossDateline()
        {
            if (this.children == null || this.children.length == 0)
                return false;

            for (Cell t : this.children)
            {
                if (((GeographicCell) t).isCrossesDateline())
                    return true;
            }

            return false;
        }
    }

    /**
     * Creates a new GeographicImageInterpolator, initializing this interpolator's internal image cell tree with root
     * cell dimensions of <code>(gridSize.width * gridSize.height)</code> and with the specified <code>depth</code>.
     *
     * @param gridSize the image's dimensions.
     * @param xs       the x-coordinates of the image's pixels. Must contain at least <code>gridSize.width *
     *                 gridSize.height</code> elements.
     * @param ys       the y-coordinates of the image's pixels. Must contain at least <code>gridSize.width *
     *                 gridSize.height</code> elements.
     * @param depth    the initial depth of this interpolator's image cell tree.
     * @param cellSize the size of a leaf cell in this interpolator's image cell tree, in pixels.
     *
     * @throws IllegalStateException if any of the the gridSize, x-coordinates, or y-coordinates are null, if either the
     *                               x-coordinates or y-coordinates contain less than <code>gridSize.width *
     *                               gridSize.height</code> elements, if the depth is less than zero, or if the cell
     *                               size is less than one.
     */
    public GeographicImageInterpolator(Dimension gridSize, float[] xs, float[] ys, int depth, int cellSize)
    {
        super(gridSize, xs, ys, depth, cellSize);
    }

    /**
     * Overridden to create a {@link gov.nasa.worldwind.util.GeographicImageInterpolator.GeographicCell}.
     *
     * @param m0 the root cell's left image coordinate.
     * @param m1 the root cell's right image coordinate.
     * @param n0 the root cell's bottom image coordinate.
     * @param n1 the root cell's top image coordinate.
     *
     * @return a new GeographicCell with the specified image coordinates.
     */
    @Override
    protected Cell makeRootCell(int m0, int m1, int n0, int n1)
    {
        return new GeographicCell(m0, m1, n0, n1);
    }

    /**
     * Returns the sector containing the image's geographic coordinates. This returns a sector which spans the longitude
     * range [-180, 180] if the image crosses the international dateline.
     *
     * @return the image's bounding sector.
     */
    public Sector getSector()
    {
        return ((GeographicCell) this.root).isCrossesDateline() ?
            Sector.fromDegrees(this.root.miny, this.root.maxy, -180, 180) :
            Sector.fromDegrees(this.root.miny, this.root.maxy, this.root.minx, this.root.maxx);
    }

    /**
     * Overridden to correctly compute bilinear interpolation coordinates for image cells which cross the international
     * dateline. If the specified cell does not cross the dateline, this invokes the superclass' functionality. This
     * returns null if the specified (x, y) point does not intersect the cell.
     *
     * @param x    the x-component of the point to compute bilinear coordinate for.
     * @param y    the y-component of the point to compute bilinear coordinate for.
     * @param cell the cell to compute bilinear coordinates for.
     *
     * @return the bilinear coordinates of the specified (x, y) point in the specified cell, or null if the point does
     *         not intersect the cell.
     */
    @Override
    protected double[] computeBilinearCoordinates(float x, float y, Cell cell)
    {
        // Invoke the superclass functionality if the cell doesn't cross the international dateline.
        if (!((GeographicCell) cell).isCrossesDateline())
        {
            return super.computeBilinearCoordinates(x, y, cell);
        }

        int[] indices = this.getFieldIndices(cell);
        Vec4[] points = new Vec4[4];

        // The cell crosses the international dateline. Adjust the cell's coordinates so the're in the same hemisphere
        // as the x-coordinate. This will result in longitude values outside of the range [-180, 180], but preserves
        // the size and shape relative to the (x, y) point.
        for (int i = 0; i < 4; i++)
        {
            double lon = this.xs[indices[i]];
            double lat = this.ys[indices[i]];

            if (x < 0f && lon >= 0f)
                lon -= 360f;
            else if (x >= 0f && lon < 0f)
                lon += 360f;

            points[i] = new Vec4(lon, lat);
        }

        // Use the adjusted cell coordinates to compute the (x, y) point's bilinear coordinates in the cell. The
        // adjusted coordinates contain nonstandard longitudes, but produce the correct result here because the
        // coordinates are interpreted as Cartesian.
        return BarycentricQuadrilateral.invertBilinear(
            new Vec4(x, y),
            points[0],
            points[1],
            points[2],
            points[3]);
    }
}
