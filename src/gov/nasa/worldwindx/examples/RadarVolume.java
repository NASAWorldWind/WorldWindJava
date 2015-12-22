/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import javax.xml.stream.*;
import java.io.IOException;
import java.nio.*;
import java.util.List;

/**
 * Displays a volume defined by a near and far grid of positions. This shape is meant to represent a radar volume, with
 * the radar having a minimum and maximum range.
 *
 * @author tag
 * @version $Id: RadarVolume.java 2438 2014-11-18 02:11:29Z tgaskins $
 */
public class RadarVolume extends AbstractShape
{
    public static final int NO_OBSTRUCTION = 0;
    public static final int EXTERNAL_OBSTRUCTION = 1;
    public static final int INTERNAL_OBSTRUCTION = 2;

    protected static final int VERTEX_NORMAL = 0;
    protected static final int TRIANGLE_NORMAL = 1;

    protected List<Position> positions; // the grid positions, near grid first, followed by far grid
    protected int[] obstructionFlags; // flags indicating where obstructions occur
    protected int width; // the number of horizontal positions in the grid.
    protected int height; // the number of vertical positions in the grid.
    protected IntBuffer sideIndices; // OpenGL indices defining the sides of the area between the grids.
    protected boolean enableSides = true; // sides show up inside conical volumes to enable the app to turn them off

    /**
     * This class holds globe-specific data for this shape. It's managed via the shape-data cache in {@link
     * gov.nasa.worldwind.render.AbstractShape.AbstractShapeData}.
     */
    protected static class ShapeData extends AbstractShapeData
    {
        // The grid vertices and grid normals below are used only during volume creation and are cleared afterwards.
        protected FloatBuffer gridVertices; // Cartesian versions of the grid vertices, referenced only, not displayed
        protected FloatBuffer gridNormals; // the normals for the gridVertices buffer
        protected FloatBuffer triangleVertices; // vertices of the grid and floor triangles
        protected FloatBuffer triangleNormals; // normals of the grid and floor triangles
        protected FloatBuffer sideVertices; // vertices of the volume's sides -- all but the grids and the floor
        protected FloatBuffer sideNormals; // normals of the side vertices
        protected Vec4 centerPoint; // the volume's approximate center; used to determine eye distance

        /**
         * Construct a cache entry using the boundaries of this shape.
         *
         * @param dc    the current draw context.
         * @param shape this shape.
         */
        public ShapeData(DrawContext dc, RadarVolume shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);
        }

        @Override
        public boolean isValid(DrawContext dc)
        {
            return super.isValid(dc) && this.gridVertices != null;// && this.normals != null;
        }

        @Override
        public boolean isExpired(DrawContext dc)
        {
            return false; // the computed data is terrain independent and therevore never expired
        }
    }

    /**
     * Constructs a radar volume.
     *
     * @param positions        the volume's positions, organized as two grids. The near grid is held in the first width
     *                         x height entries, the far grid is held in the next width x height entries. This list is
     *                         retained as-is and is not copied.
     * @param obstructionFlags flags indicating the obstruction state of the specified positions. This array is retained
     *                         as-is and is not copied. Recognized values are <code>NO_OBSTRUCTION</code> indicating
     *                         that the specified position is unobstructed, <code>INTERNAL_OBSTRUCTION</code> indicating
     *                         that the position is obstructed beyond the near grid but before the far grid,
     *                         <code>EXTERNAL_OBSTRUCTION</code> indicating that the position is obstructed before the
     *                         near grid.
     * @param width            the horizontal dimension of the grid.
     * @param height           the vertical dimension of the grid.
     *
     * @throws java.lang.IllegalArgumentException if the positions list or inclusion flags array is null, the size of
     *                                            the inclusion flags array is less than the number of grid positions,
     *                                            the positions list is less than the specified size, or the width or
     *                                            height are less than 2.
     */
    public RadarVolume(List<Position> positions, int[] obstructionFlags, int width, int height)
    {
        if (positions == null || obstructionFlags == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width < 2)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 2)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (positions.size() < 2 * (width * height))
        {
            String message = Logging.getMessage("generic.ListLengthInsufficient", positions.size());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (obstructionFlags.length < positions.size())
        {
            String message = Logging.getMessage("generic.ListLengthInsufficient", obstructionFlags.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.positions = positions;
        this.obstructionFlags = obstructionFlags;
        this.width = width;
        this.height = height;
    }

    public boolean isEnableSides()
    {
        return enableSides;
    }

    public void setEnableSides(boolean enableSides)
    {
        this.enableSides = enableSides;
    }

    @Override
    protected void initialize()
    {
        // Nothing unique to initialize.
    }

    @Override
    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new ShapeData(dc, this);
    }

    /**
     * Returns the current shape data cache entry.
     *
     * @return the current data cache entry.
     */
    protected ShapeData getCurrent()
    {
        return (ShapeData) this.getCurrentData();
    }

    /**
     * Returns the grid positions as specified to this object's constructor.
     *
     * @return this object's grid positions.
     */
    public List<Position> getPositions()
    {
        return positions;
    }

    /**
     * Returns the inclusion flags as specified to this object's constructor.
     *
     * @return this object's inclusion flags.
     */
    public int[] getObstructionFlags()
    {
        return this.obstructionFlags;
    }

    /**
     * Indicates the grid width.
     *
     * @return the grid width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Indicates the grid height.
     *
     * @return the grid height.
     */
    public int getHeight()
    {
        return height;
    }

    @Override
    protected boolean mustApplyTexture(DrawContext dc)
    {
        return false;
    }

    @Override
    protected boolean shouldUseVBOs(DrawContext dc)
    {
        return false;
    }

    @Override
    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        return shapeData.triangleVertices != null && shapeData.triangleVertices.capacity() > 0;
    }

    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        if (!this.intersectsFrustum(dc))
        {
            return false;
        }

        ShapeData shapeData = this.getCurrent();

        if (shapeData.triangleVertices == null)
        {
            this.makeGridVertices(dc);
            this.computeCenterPoint();
            this.makeGridNormals();
            this.makeGridTriangles();
            this.makeSides();

            // No longer need the grid vertices or normals
            shapeData.gridVertices = null;
            shapeData.gridNormals = null;
        }

        shapeData.setEyeDistance(dc.getView().getEyePoint().distanceTo3(shapeData.centerPoint));

        return true;
    }

    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        // The shape does not have an outline
    }

    @Override
    protected void doDrawInterior(DrawContext dc)
    {
        this.drawModel(dc, GL2.GL_FILL);
    }

    protected void drawModel(DrawContext dc, int displayMode)
    {
        ShapeData shapeData = this.getCurrent();
        GL2 gl = dc.getGL().getGL2();

        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, displayMode);

        // Draw the volume's near and far grids and floor.
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.triangleVertices.rewind());
        gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.triangleNormals.rewind());
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, shapeData.triangleVertices.limit() / 3);

        if (this.isEnableSides())
        {
            // Draw the volume's sides.
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.sideVertices.rewind());
            gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.sideNormals.rewind());
            gl.glDrawElements(GL.GL_TRIANGLE_STRIP, this.sideIndices.limit(), GL.GL_UNSIGNED_INT,
                this.sideIndices.rewind());
        }
    }

    protected void makeGridVertices(DrawContext dc)
    {
        // The Cartesian coordinates of the grid are computed but only used to construct the displayed volume. They are
        // not themselves rendered, and are cleared once construction is done.

        // The grid consists of independent triangles. A tri-strip can't be used because not all positions in the
        // input grids participate in triangle formation because they may be obstructed.

        // Get the current shape data.
        ShapeData shapeData = this.getCurrent();

        // Set the reference point to the grid's origin.
        Vec4 refPt = dc.getGlobe().computePointFromPosition(this.positions.get(0));
        shapeData.setReferencePoint(refPt);

        // Allocate the grid vertices.
        shapeData.gridVertices = Buffers.newDirectFloatBuffer(3 * this.positions.size());

        // Compute the grid vertices.
        for (Position position : this.positions)
        {
            Vec4 point = dc.getGlobe().computePointFromPosition(position).subtract3(refPt);
            shapeData.gridVertices.put((float) point.x).put((float) point.y).put((float) point.z);
        }
    }

    protected void makeGridNormals()
    {
        // Like the grid vertices, the grid normals are computed only for construction of the volume and determination
        // of its normals. The grid normals are not used otherwise and are cleared once construction is done.

        // The grid normals are defined by a vector from each position in the near grid to the corresponding
        // position in the far grid.

        ShapeData shapeData = this.getCurrent();
        FloatBuffer vertices = shapeData.gridVertices;

        shapeData.gridNormals = Buffers.newDirectFloatBuffer(shapeData.gridVertices.limit());
        int gridSize = this.getWidth() * this.getHeight();
        int separation = 3 * gridSize;
        for (int i = 0; i < gridSize; i++)
        {
            int k = i * 3;
            double nx = vertices.get(k + separation) - vertices.get(k);
            double ny = vertices.get(k + separation + 1) - vertices.get(k + 1);
            double nz = vertices.get(k + separation + 2) - vertices.get(k + 2);

            double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length > 0)
            {
                nx /= length;
                ny /= length;
                nz /= length;
            }

            shapeData.gridNormals.put((float) nx).put((float) ny).put((float) nz);
            shapeData.gridNormals.put(k + separation, (float) nx);
            shapeData.gridNormals.put(k + separation + 1, (float) ny);
            shapeData.gridNormals.put(k + separation + 2, (float) nz);
        }
    }

    protected void computeCenterPoint()
    {
        ShapeData shapeData = this.getCurrent();

        int gridSize = this.width * this.height;
        int k = 3 * gridSize / 2;

        double xNear = shapeData.gridVertices.get(k);
        double yNear = shapeData.gridVertices.get(k + 1);
        double zNear = shapeData.gridVertices.get(k + 2);

        k += 3 * gridSize;

        double xFar = shapeData.gridVertices.get(k);
        double yFar = shapeData.gridVertices.get(k + 1);
        double zFar = shapeData.gridVertices.get(k + 2);

        Vec4 pNear = (new Vec4(xNear, yNear, zNear)).add3(shapeData.getReferencePoint());
        Vec4 pFar = (new Vec4(xFar, yFar, zFar)).add3(shapeData.getReferencePoint());

        shapeData.centerPoint = pNear.add3(pFar).multiply3(0.5);
    }

    /**
     * Forms the volume's front, back and bottom vertices and computes appropriate normals.
     */
    protected void makeGridTriangles()
    {
        // This method first computes the triangles that form the near and far grid surfaces, then it computes the
        // floor connecting those surface to either each other or the terrain intersections within the volume. For
        // the grid face there are five relevant cases, each described in their implementation below. For the floor
        // there are 8 relevant cases, also described in their implementation below.

        ShapeData shapeData = this.getCurrent();
        FloatBuffer vs = shapeData.gridVertices;

        // Allocate the most we'll need because we don't yet know exactly how much we'll use. We  need at most room
        // for 9 floats per triangle, 4 triangles per grid cell and 2 sets of grid cells (near and far).
        int maxSize = 9 * 4 * 2 * ((this.width - 1) * (this.height - 1));
        shapeData.triangleVertices = Buffers.newDirectFloatBuffer(maxSize);
        shapeData.triangleNormals = Buffers.newDirectFloatBuffer(maxSize);

        FloatBuffer triVerts = shapeData.triangleVertices;

        int[] triFlags = new int[3];
        int[] triIndices = new int[3];

        for (int n = 0; n < 2; n++) // once for near grid, then again for far grid
        {
            int base = n * this.width * this.height;

            for (int j = 0; j < this.height - 1; j++)
            {
                for (int i = 0; i < this.width - 1; i++)
                {
                    // k identifies the grid index of the lower left position in each cell
                    int k = base + j * this.width + i;
                    boolean ll, lr, ul, ur;

                    // Determine the status of the four grid positions.
                    if (n == 0) // near grid
                    {
                        ll = this.obstructionFlags[k] == NO_OBSTRUCTION;
                        lr = this.obstructionFlags[k + 1] == NO_OBSTRUCTION;
                        ul = this.obstructionFlags[k + this.width] == NO_OBSTRUCTION;
                        ur = this.obstructionFlags[k + this.width + 1] == NO_OBSTRUCTION;
                    }
                    else // far grid
                    {
                        ll = this.obstructionFlags[k] != EXTERNAL_OBSTRUCTION;
                        lr = this.obstructionFlags[k + 1] != EXTERNAL_OBSTRUCTION;
                        ul = this.obstructionFlags[k + this.width] != EXTERNAL_OBSTRUCTION;
                        ur = this.obstructionFlags[k + this.width + 1] != EXTERNAL_OBSTRUCTION;
                    }
                    int gridSize = this.width * this.height;

                    int llv = k; // index of lower left cell position
                    int lrv = k + 1;
                    int ulv = (k + width);
                    int urv = k + width + 1;

                    int kk; // index into the grid vertices buffer

                    if (ul && ur && ll && lr) // case 6
                    {
                        // Show both triangles.

                        // It matters how we decompose the cell into triangles. The order in these two clauses
                        // ensures that the correct half cells are drawn when one of the lower positions has an
                        // internal obstruction -- is not on the face of the grid.

                        if (this.obstructionFlags[llv] == INTERNAL_OBSTRUCTION)
                        {
                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[0] = this.obstructionFlags[kk / 3];
                            triIndices[0] = kk;

                            kk = ulv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[1] = this.obstructionFlags[kk / 3];
                            triIndices[1] = kk;

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[2] = this.obstructionFlags[kk / 3];
                            triIndices[2] = kk;

                            this.setTriangleNormals(triFlags, triIndices);

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[0] = this.obstructionFlags[kk / 3];
                            triIndices[0] = kk;

                            kk = ulv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[1] = this.obstructionFlags[kk / 3];
                            triIndices[1] = kk;

                            kk = urv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[2] = this.obstructionFlags[kk / 3];
                            triIndices[2] = kk;

                            this.setTriangleNormals(triFlags, triIndices);
                        }
                        else
                        {
                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[0] = this.obstructionFlags[kk / 3];
                            triIndices[0] = kk;

                            kk = urv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[1] = this.obstructionFlags[kk / 3];
                            triIndices[1] = kk;

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[2] = this.obstructionFlags[kk / 3];
                            triIndices[2] = kk;

                            this.setTriangleNormals(triFlags, triIndices);

                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[0] = this.obstructionFlags[kk / 3];
                            triIndices[0] = kk;

                            kk = ulv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[1] = this.obstructionFlags[kk / 3];
                            triIndices[1] = kk;

                            kk = urv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triFlags[2] = this.obstructionFlags[kk / 3];
                            triIndices[2] = kk;

                            this.setTriangleNormals(triFlags, triIndices);
                        }
                    }
                    else if (ul && !ur && ll && lr) // case 5
                    {
                        // Show the lower left triangle

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[0] = this.obstructionFlags[kk / 3];
                        triIndices[0] = kk;

                        kk = lrv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[1] = this.obstructionFlags[kk / 3];
                        triIndices[1] = kk;

                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[2] = this.obstructionFlags[kk / 3];
                        triIndices[2] = kk;

                        this.setTriangleNormals(triFlags, triIndices);
                    }
                    else if (ul && ur && ll && !lr) // case 7
                    {
                        // Show the upper left triangle.

                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[0] = this.obstructionFlags[kk / 3];
                        triIndices[0] = kk;

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[1] = this.obstructionFlags[kk / 3];
                        triIndices[1] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[2] = this.obstructionFlags[kk / 3];
                        triIndices[2] = kk;

                        this.setTriangleNormals(triFlags, triIndices);
                    }
                    else if (!ul && ur && ll && lr) // case 8
                    {
                        // Show the lower right triangle

                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[0] = this.obstructionFlags[kk / 3];
                        triIndices[0] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[1] = this.obstructionFlags[kk / 3];
                        triIndices[1] = kk;

                        kk = lrv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[2] = this.obstructionFlags[kk / 3];
                        triIndices[2] = kk;

                        this.setTriangleNormals(triFlags, triIndices);
                    }
                    else if (ul && ur && !ll && lr) // case 11
                    {
                        // Show the right triangle.

                        kk = lrv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[0] = this.obstructionFlags[kk / 3];
                        triIndices[0] = kk;

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[1] = this.obstructionFlags[kk / 3];
                        triIndices[1] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triFlags[2] = this.obstructionFlags[kk / 3];
                        triIndices[2] = kk;

                        this.setTriangleNormals(triFlags, triIndices);
                    }

                    if (n == 0) // no need to calculate floor for the near grid
                        continue;

                    // Form the cell's "floor".

                    if (!ul && !ur && ll && lr) // case 2
                    {
                        // Draw the cell bottom.

                        if (this.obstructionFlags[llv] == INTERNAL_OBSTRUCTION
                            || this.obstructionFlags[lrv] == INTERNAL_OBSTRUCTION)
                        {
                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (llv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = (lrv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (llv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);
                        }
                        else
                        {
                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = (llv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (lrv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);

                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (lrv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);
                        }
                    }
                    else if (ul && !ur && ll && !lr) // case 3
                    {
                        // Draw the left side of the cell.

                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = (llv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = (llv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = (ulv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);
                    }
                    else if (ul && !ur && ll && lr) // case 5
                    {
                        // Draw the ul to lr diagonal of the cell.

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = (ulv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = (lrv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);

                        kk = (lrv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = lrv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);
                    }
                    else if (ul && ur && ll && !lr) // case 7
                    {
                        // Draw the ur to ll diagonal of the cell.

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = (urv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = (llv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);

                        kk = (llv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);
                    }
                    else if (!ul && ur && ll && lr) // case 8
                    {
                        // Draw the ll to ur diagonal of the cell.

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = (urv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = (llv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);

                        kk = (llv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);
                    }
                    else if (!ul && ur && !ll && lr) // case 10
                    {
                        // Draw the right side of the cell.

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = (urv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = (lrv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);

                        kk = (lrv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = lrv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);
                    }
                    else if (ul && ur && !ll && lr) // case 11
                    {
                        // Draw the ul to lr diagonal of the cell.

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = (ulv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = (lrv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);

                        kk = (lrv - gridSize) * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = lrv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);
                    }
                    else if (ul && ur && !ll && !lr) // case 13
                    {
                        // Draw the cell top.

                        llv = ulv - gridSize;
                        lrv = urv - gridSize;

                        // Draw the floor.
                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = lrv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);

                        kk = llv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[0] = kk;

                        kk = ulv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[1] = kk;

                        kk = urv * 3;
                        triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                        triIndices[2] = kk;

                        this.setTriangleNormals(null, triIndices);
                    }

                    // If this is the bottom row of cells, then we may need to draw the floor connecting
                    // the far grid to the near grid along the edge.

                    if (j == 0 && ll && lr)
                    {
                        if (this.obstructionFlags[llv] == INTERNAL_OBSTRUCTION
                            || this.obstructionFlags[lrv] == INTERNAL_OBSTRUCTION)
                        {
                            // Draw the floor.
                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (llv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = (lrv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (llv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);
                        }
                        else
                        {
                            // Draw the floor.
                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = (llv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (lrv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);

                            kk = llv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[0] = kk;

                            kk = lrv * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[1] = kk;

                            kk = (lrv - gridSize) * 3;
                            triVerts.put(vs.get(kk)).put(vs.get(kk + 1)).put(vs.get(kk + 2));
                            triIndices[2] = kk;

                            this.setTriangleNormals(null, triIndices);
                        }
                    }
                }
            }
        }

        shapeData.triangleVertices.flip(); // capture the currently used buffer size as the limit.
        shapeData.triangleVertices = trimBuffer(shapeData.triangleVertices);

        shapeData.triangleNormals.flip();
        shapeData.triangleNormals = trimBuffer(shapeData.triangleNormals);
    }

    protected void setTriangleNormals(int[] flags, int[] indices)
    {
        ShapeData shapeData = this.getCurrent();

        // We want to use the actual normals -- the rays from the radar position to the grid positions -- when the
        // triangle is fully outward facing and not part of the floor. This prevents faceting of the volume's surface.

        if (flags != null && flags[0] == flags[1] && flags[1] == flags[2] && flags[2] == NO_OBSTRUCTION)
        {
            // Use the actual normal of each position.
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[0]));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[0] + 1));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[0] + 2));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[1]));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[1] + 1));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[1] + 2));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[2]));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[2] + 1));
            shapeData.triangleNormals.put(shapeData.gridNormals.get(indices[2] + 2));
        }
        else
        {
            // Compute a single normal for the triangle and assign it to all three vertices.
            double x0 = shapeData.gridVertices.get(indices[0]);
            double y0 = shapeData.gridVertices.get(indices[0] + 1);
            double z0 = shapeData.gridVertices.get(indices[0] + 2);
            double x1 = shapeData.gridVertices.get(indices[1]);
            double y1 = shapeData.gridVertices.get(indices[1] + 1);
            double z1 = shapeData.gridVertices.get(indices[1] + 2);
            double x2 = shapeData.gridVertices.get(indices[2]);
            double y2 = shapeData.gridVertices.get(indices[2] + 1);
            double z2 = shapeData.gridVertices.get(indices[2] + 2);

            double ux = x1 - x0;
            double uy = y1 - y0;
            double uz = z1 - z0;

            double vx = x2 - x0;
            double vy = y2 - y0;
            double vz = z2 - z0;

            double nx = uy * vz - uz * vy;
            double ny = uz * vx - ux * vz;
            double nz = ux * vy - uy * vx;

            double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length > 0)
            {
                nx /= length;
                ny /= length;
                nz /= length;
            }

            shapeData.triangleNormals.put((float) nx).put((float) ny).put((float) nz);
            shapeData.triangleNormals.put((float) nx).put((float) ny).put((float) nz);
            shapeData.triangleNormals.put((float) nx).put((float) ny).put((float) nz);
        }
    }

    protected static FloatBuffer trimBuffer(FloatBuffer buffer)
    {
        FloatBuffer outputBuffer = Buffers.newDirectFloatBuffer(buffer.limit());

        buffer.rewind();
        while (buffer.hasRemaining())
        {
            outputBuffer.put(buffer.get());
        }

        return outputBuffer;
    }

    protected void makeSides()
    {
        // The sides consist of a single triangle strip going around the left, top and right sides of the volume.
        // Obscured positions on the sides are skipped.

        ShapeData shapeData = this.getCurrent();

        int numSideVertices = 2 * (2 * this.getHeight() + this.getWidth() - 2);

        shapeData.sideVertices = Buffers.newDirectFloatBuffer(3 * numSideVertices);
        int gridSize = this.getWidth() * this.getHeight();

        // Left side vertices.
        for (int i = 0; i < this.getHeight(); i++)
        {
            int k = gridSize + i * this.getWidth();
            if (this.obstructionFlags[k] != EXTERNAL_OBSTRUCTION)
            {
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 1));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 2));

                k -= gridSize;
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 1));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 2));
            }
        }

        // Top vertices.
        for (int i = 1; i < this.getWidth(); i++)
        {
            int k = 2 * gridSize - this.getWidth() + i;
            if (this.obstructionFlags[k] != EXTERNAL_OBSTRUCTION)
            {
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 1));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 2));

                k -= gridSize;
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 1));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 2));
            }
        }

        // Right side vertices.
        for (int i = 1; i < this.getHeight(); i++)
        {
            int k = 2 * gridSize - 1 - i * this.getWidth();
            if (this.obstructionFlags[k] != EXTERNAL_OBSTRUCTION)
            {
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 1));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 2));

                k -= gridSize;
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 1));
                shapeData.sideVertices.put(shapeData.gridVertices.get(3 * k + 2));
            }
        }

        shapeData.sideVertices.flip();

        // Create the side indices.
        this.sideIndices = Buffers.newDirectIntBuffer(shapeData.sideVertices.limit() / 3);
        for (int i = 0; i < this.sideIndices.limit(); i++)
        {
            this.sideIndices.put(i);
        }

        // Allocate and zero a buffer for the side normals then generate the side normals.
        shapeData.sideNormals = Buffers.newDirectFloatBuffer(shapeData.sideVertices.limit());
        while (shapeData.sideNormals.position() < shapeData.sideNormals.limit())
        {
            shapeData.sideNormals.put(0);
        }
        WWUtil.generateTriStripNormals(shapeData.sideVertices, this.sideIndices, shapeData.sideNormals);
    }

    @Override
    protected void fillVBO(DrawContext dc)
    {
        // Not using VBOs.
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        // See if we've cached an extent associated with the globe.
        Extent extent = super.getExtent(globe, verticalExaggeration);
        if (extent != null)
        {
            return extent;
        }

        this.getCurrent().setExtent(super.computeExtentFromPositions(globe, verticalExaggeration, this.positions));

        return this.getCurrent().getExtent();
    }

    @Override
    public Sector getSector()
    {
        if (this.sector != null)
        {
            return this.sector;
        }

        this.sector = Sector.boundingSector(this.positions);

        return this.sector;
    }

    @Override
    public Position getReferencePosition()
    {
        return this.positions.get(0);
    }

    @Override
    public void moveTo(Position position)
    {
        // Not supported
    }

    @Override
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        return null;
    }

    @Override
    public String isExportFormatSupported(String mimeType)
    {
        return Exportable.FORMAT_NOT_SUPPORTED;
    }

    @Override
    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        throw new UnsupportedOperationException("KML output not supported for RadarVolume");
    }
}
