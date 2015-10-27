/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;

import java.awt.*;

/**
 * Provides searching and interpolation of a grid of scalars.
 *
 * @author tag
 * @version $Id: ImageInterpolator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ImageInterpolator
{
    protected static class Cell implements Cacheable
    {
        protected final int m0, m1, n0, n1;
        protected float minx, maxx, miny, maxy;
        protected Cell[] children;

        public Cell(int m0, int m1, int n0, int n1)
        {
            this.m0 = m0;
            this.m1 = m1;
            this.n0 = n0;
            this.n1 = n1;
        }

        protected Cell makeChildCell(int m0, int m1, int n0, int n1)
        {
            return new Cell(m0, m1, n0, n1);
        }

        public long getSizeInBytes()
        {
            return 13 * 4;
        }

        public void build(int numLevels, int cellSize)
        {
            if (numLevels == 0)
                return;

            if (this.m1 - this.m0 <= cellSize && this.n1 - this.n0 <= cellSize)
                return;

            this.children = this.split(this.m0, this.m1, this.n0, this.n1);
            for (Cell t : this.children)
            {
                t.build(numLevels - 1, cellSize);
            }
        }

        public Cell[] split(int mm0, int mm1, int nn0, int nn1)
        {
            int mma = (mm1 - mm0 > 1 ? mm0 + (mm1 - mm0) / 2 : mm0 + 1);
            int nna = (nn1 - nn0 > 1 ? nn0 + (nn1 - nn0) / 2 : nn0 + 1);
            int mmb = mm1 - mm0 > 1 ? mma : mm0;
            int nnb = nn1 - nn0 > 1 ? nna : nn0;

            return new Cell[] {
                this.makeChildCell(mm0, mma, nn0, nna),
                this.makeChildCell(mmb, mm1, nn0, nna),
                this.makeChildCell(mm0, mma, nnb, nn1),
                this.makeChildCell(mmb, mm1, nnb, nn1)
            };
        }

        public boolean intersects(float x, float y)
        {
            return x >= this.minx && x <= this.maxx && y >= this.miny && y <= this.maxy;
        }
        
        public void computeBounds(Dimension dim, float[] xs, float[] ys)
        {
            if (this.children != null)
            {
                for (Cell t : this.children)
                {
                    t.computeBounds(dim, xs, ys);
                }

                this.computeExtremesFromChildren();
            }
            else
            {
                this.computeExtremesFromLocations(dim, xs, ys);
            }
        }

        protected void computeExtremesFromLocations(Dimension dim, float[] xs, float[] ys)
        {
            this.minx = Float.MAX_VALUE;
            this.maxx = -Float.MAX_VALUE;
            this.miny = Float.MAX_VALUE;
            this.maxy = -Float.MAX_VALUE;

            for (int j = this.n0; j <= this.n1; j++)
            {
                for (int i = this.m0; i <= this.m1; i++)
                {
                    int k = j * dim.width + i;
                    float x = xs[k];
                    float y = ys[k];

                    if (x < this.minx)
                        this.minx = x;
                    if (x > this.maxx)
                        this.maxx = x;

                    if (y < this.miny)
                        this.miny = y;
                    if (y > this.maxy)
                        this.maxy = y;
                }
            }
        }

        protected void computeExtremesFromChildren()
        {
            this.minx = Float.MAX_VALUE;
            this.maxx = -Float.MAX_VALUE;
            this.miny = Float.MAX_VALUE;
            this.maxy = -Float.MAX_VALUE;

            if (this.children == null)
                return;

            for (Cell t : children)
            {
                if (t.minx < this.minx)
                    this.minx = t.minx;
                if (t.maxx > this.maxx)
                    this.maxx = t.maxx;

                if (t.miny < this.miny)
                    this.miny = t.miny;
                if (t.maxy > this.maxy)
                    this.maxy = t.maxy;
            }
        }
    }

    public static class ContainingCell
    {
        public final int m0, m1, n0, n1;
        public final float minx, maxx, miny, maxy;
        public final double[] uv;
        public final int[] fieldIndices;

        private ContainingCell(Cell cell, double uv[], int[] fieldIndices)
        {
            this.uv = uv;

            this.m0 = cell.m0;
            this.m1 = cell.m1;
            this.n0 = cell.n0;
            this.n1 = cell.n1;

            this.minx = cell.minx;
            this.maxx = cell.maxx;
            this.miny = cell.miny;
            this.maxy = cell.maxy;

            this.fieldIndices = fieldIndices;
        }
    }

    protected final Dimension gridSize;
    protected final Cell root;
    protected final float[] xs;
    protected final float[] ys;
    protected final int cellSize;
    protected final BasicMemoryCache kidCache = new BasicMemoryCache(750000L, 1000000L);

    public ImageInterpolator(Dimension gridSize, float[] xs, float[] ys, int depth, int cellSize)
    {
        if (gridSize == null)
        {
            String message = Logging.getMessage("nullValue.DimensionIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (gridSize.width < 2 || gridSize.height < 2)
        {
            String message = Logging.getMessage("generic.DimensionsTooSmall");
            Logging.logger().log(java.util.logging.Level.SEVERE, message,
                new Object[] {gridSize.width, gridSize.height});
            throw new IllegalStateException(message);
        }

        if (xs == null || ys == null || xs.length < 4 || ys.length < 4)
        {
            String message = Logging.getMessage("Grid.ArraysInvalid");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (depth < 0)
        {
            String message = Logging.getMessage("Grid.DepthInvalid");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (cellSize < 1)
        {
            String message = Logging.getMessage("Grid.CellSizeInvalid");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.gridSize = gridSize;
        this.cellSize = cellSize;

        this.xs = xs;
        this.ys = ys;

        this.root = this.makeRootCell(0, this.gridSize.width - 1, 0, this.gridSize.height - 1);
        this.root.build(depth, this.cellSize);
        this.root.computeBounds(this.gridSize, this.xs, this.ys);
    }

    protected Cell makeRootCell(int m0, int m1, int n0, int n1)
    {
        return new Cell(m0, m1, n0, n1);
    }

    public ContainingCell findContainingCell(float x, float y)
    {
        return this.findContainingCell(this.root, x, y);
    }

    protected ContainingCell findContainingCell(Cell cell, float x, float y)
    {
        if (!cell.intersects(x, y))
            return null;

        if (cell.m1 - cell.m0 <= this.cellSize && cell.n1 - cell.n0 <= this.cellSize)
            return this.checkContainment(x, y, cell);

        Cell[] kids = cell.children != null ? cell.children : (Cell[]) this.kidCache.getObject(cell);
        if (kids == null)
        {
            kids = cell.split(cell.m0, cell.m1, cell.n0, cell.n1);
            for (Cell child : kids)
            {
                child.computeExtremesFromLocations(this.gridSize, this.xs, this.ys);
            }
            if (cell.children == null)
                this.kidCache.add(cell, kids, 4 * kids[0].getSizeInBytes());
        }

        for (Cell t : kids)
        {
            ContainingCell cellFound = this.findContainingCell(t, x, y);
            if (cellFound != null)
                return cellFound;
        }

        return null;
    }

    protected ContainingCell checkContainment(float x, float y, Cell cell)
    {
        double[] uv = this.computeBilinearCoordinates(x, y, cell);

        return uv != null
            && uv[0] <= 1 && uv[1] <= 1 && uv[0] >= 0 && uv[1] >= 0
            ? new ContainingCell(cell, uv, getFieldIndices(cell)) : null;
    }

    protected double[] computeBilinearCoordinates(float x, float y, Cell cell)
    {
        int i = index(cell.m0, cell.n0);
        int j = index(cell.m1, cell.n0);
        int k = index(cell.m1, cell.n1);
        int l = index(cell.m0, cell.n1);

        return BarycentricQuadrilateral.invertBilinear(
            new Vec4(x, y, 0),
            new Vec4(this.xs[i], this.ys[i], 0),
            new Vec4(this.xs[j], this.ys[j], 0),
            new Vec4(this.xs[k], this.ys[k], 0),
            new Vec4(this.xs[l], this.ys[l], 0));
    }

    protected int[] getFieldIndices(Cell cell)
    {
        return new int[] {
            index(cell.m0, cell.n0),
            index(cell.m1, cell.n0),
            index(cell.m1, cell.n1),
            index(cell.m0, cell.n1)
        };
    }

    private int index(int i, int j)
    {
        return j * this.gridSize.width + i;
    }
}
