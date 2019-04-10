/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.nio.*;
import java.util.List;

/**
 * A {@link Path} that draws arrowheads between the path positions to indicate direction. All arrowheads are drawn at a
 * constant geographic size (the arrows get smaller as the view moves away from the path, and larger as the view get
 * closer to the path). One arrowhead is drawn on each path segment, unless the path segment is smaller than the
 * arrowhead, in which case the arrowhead is not drawn.
 *
 * @author pabercrombie
 * @version $Id: DirectedPath.java 3034 2015-04-17 18:04:14Z dcollins $
 */
public class DirectedPath extends Path
{
    /** Default arrow length, in meters. */
    public static final double DEFAULT_ARROW_LENGTH = 300;
    /** Default arrow angle. */
    public static final Angle DEFAULT_ARROW_ANGLE = Angle.fromDegrees(45.0);
    /** Default maximum screen size of the arrowheads, in pixels. */
    public static final double DEFAULT_MAX_SCREEN_SIZE = 20.0;

    /** The length, in meters, of the arrowhead, from tip to base. */
    protected double arrowLength = DEFAULT_ARROW_LENGTH;
    /** The angle of the arrowhead tip. */
    protected Angle arrowAngle = DEFAULT_ARROW_ANGLE;
    /** The maximum screen size, in pixels, of the direction arrowheads. */
    protected double maxScreenSize = DEFAULT_MAX_SCREEN_SIZE;

    /** Creates a path with no positions. */
    public DirectedPath()
    {
        super();
    }

    /**
     * Creates a path with specified positions.
     * <p/>
     * Note: If fewer than two positions is specified, no path is drawn.
     *
     * @param positions the path positions. This reference is retained by this shape; the positions are not copied. If
     *                  any positions in the set change, {@link #setPositions(Iterable)} must be called to inform this
     *                  shape of the change.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public DirectedPath(Iterable<? extends Position> positions)
    {
        super(positions);
    }

    /**
     * Creates a path with positions specified via a generic list.
     * <p/>
     * Note: If fewer than two positions is specified, the path is not drawn.
     *
     * @param positions the path positions. This reference is retained by this shape; the positions are not copied. If
     *                  any positions in the set change, {@link #setPositions(Iterable)} must be called to inform this
     *                  shape of the change.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public DirectedPath(Position.PositionList positions)
    {
        super(positions.list);
    }

    /**
     * Creates a path between two positions.
     *
     * @param posA the first position.
     * @param posB the second position.
     *
     * @throws IllegalArgumentException if either position is null.
     */
    public DirectedPath(Position posA, Position posB)
    {
        super(posA, posB);
    }

    /**
     * Indicates the length, in meters, of the direction arrowheads, from base to tip.
     *
     * @return The geographic length of the direction arrowheads.
     */
    public double getArrowLength()
    {
        return this.arrowLength;
    }

    /**
     * Specifies the length, in meters, of the direction arrowheads, from base to tip.
     *
     * @param arrowLength length, in meters, of the direction arrowheads. The length must be greater than zero.
     */
    public void setArrowLength(double arrowLength)
    {
        if (arrowLength <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", arrowLength);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.arrowLength = arrowLength;
    }

    /**
     * Indicates the maximum screen size, in pixels, of the direction arrowheads. The arrowheads are drawn a fixed
     * geographic, but they are not allowed to get bigger than {@code maxScreenSize} pixels.
     *
     * @return The maximum screen size, in pixels, of the direction arrowheads, measured tip to base.
     */
    public double getMaxScreenSize()
    {
        return this.maxScreenSize;
    }

    /**
     * Specifies the maximum screen size, in pixels, of the direction arrowheads. The arrowheads are drawn at a fixed
     * geographic size, but they will not allowed to get bigger than {@code maxScreenSize} pixels.
     *
     * @param maxScreenSize the maximum screen size, in pixels, of the direction arrowheads, measured tip to base.
     */
    public void setMaxScreenSize(double maxScreenSize)
    {
        if (maxScreenSize <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", maxScreenSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxScreenSize = maxScreenSize;
    }

    /**
     * Indicates the angle of the direction arrowheads. A larger angle draws a fat arrowhead, and a smaller angle draws
     * a narrow arrow head.
     *
     * @return The angle of the direction arrowhead tip.
     */
    public Angle getArrowAngle()
    {
        return this.arrowAngle;
    }

    /**
     * Specifies the angle of the direction arrowheads. A larger angle draws a fat arrowhead, and a smaller angle draws
     * a narrow arrow.
     *
     * @param arrowAngle angle of the direction arrowhead tip. Valid values are between 0 degrees and 90 degrees.
     */
    public void setArrowAngle(Angle arrowAngle)
    {
        if (arrowAngle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if ((arrowAngle.compareTo(Angle.ZERO) <= 0) || (arrowAngle.compareTo(Angle.POS90) >= 0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", arrowAngle);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.arrowAngle = arrowAngle;
    }

    protected static final String ARROWS_KEY = "DirectedPath.DirectionArrows";
    protected static final String ARROWS_EXTENT = "DirectedPath.DirectionArrowsExtent";

    protected boolean intersectsFrustum(DrawContext dc)
    {
        // Must override this method to account for the extent of the arrowheads.

        boolean intersects = super.intersectsFrustum(dc);
        if (intersects || !dc.isPickingMode())
        {
            return intersects;
        }

        Box box = (Box) this.currentData.getValue(ARROWS_EXTENT);
        return box == null || dc.getPickFrustums().intersectsAny(box);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to also compute the geometry of the direction arrows.
     */
    @Override
    protected void computePath(DrawContext dc, List<Position> positions, PathData pathData)
    {
        super.computePath(dc, positions, pathData);
//        this.computeDirectionArrows(dc, pathData);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to return a {@link gov.nasa.worldwindx.examples.util.DirectedSurfacePolyline}.
     */
    @Override
    protected SurfaceShape createSurfaceShape()
    {
        DirectedSurfacePolyline polyline = new DirectedSurfacePolyline();
        polyline.setLocations(this.getPositions());

        return polyline;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to update the arrow properties of {@link gov.nasa.worldwindx.examples.util.DirectedSurfacePolyline}.
     */
    @Override
    protected void updateSurfaceShape()
    {
        super.updateSurfaceShape();

        ((DirectedSurfacePolyline) this.surfaceShape).setArrowLength(this.getArrowLength());
        ((DirectedSurfacePolyline) this.surfaceShape).setMaxScreenSize(this.getMaxScreenSize());
        ((DirectedSurfacePolyline) this.surfaceShape).setArrowAngle(this.getArrowAngle());
    }

    /**
     * Compute the geometry of the direction arrows.
     *
     * @param dc       current draw context.
     * @param pathData the current globe-specific path data.
     */
    protected void computeDirectionArrows(DrawContext dc, PathData pathData)
    {
        IntBuffer polePositions = pathData.getPolePositions();
        int numPositions = polePositions.limit() / 2; // One arrow head for each path segment
        List<Position> tessellatedPositions = pathData.getTessellatedPositions();

        final int FLOATS_PER_ARROWHEAD = 9; // 3 points * 3 coordinates per point
        FloatBuffer buffer = (FloatBuffer) pathData.getValue(ARROWS_KEY);
        if (buffer == null || buffer.capacity() < numPositions * FLOATS_PER_ARROWHEAD)
        {
            buffer = Buffers.newDirectFloatBuffer(FLOATS_PER_ARROWHEAD * numPositions);
        }
        pathData.setValue(ARROWS_KEY, buffer);

        buffer.clear();

        Terrain terrain = dc.getTerrain();

        // Step through polePositions to find the original path locations.
        int poleA = polePositions.get(0) / 2;
        Vec4 polePtA = this.computePoint(dc, terrain, tessellatedPositions.get(poleA));

        // Draw one arrowhead for each segment in the original position list. The path may be tessellated,
        // so we need to find the tessellated segment halfway between each pair of original positions.
        // polePositions holds indices into the rendered path array of the original vertices. Step through
        // polePositions by 2 because we only care about where the top of the pole is, not the bottom.
        for (int i = 2; i < polePositions.limit(); i += 2)
        {
            // Find the position of this pole and the next pole. Divide by 2 to convert an index in the
            // renderedPath buffer to a index in the tessellatedPositions list.
            int poleB = polePositions.get(i) / 2;
            Vec4 polePtB = this.computePoint(dc, terrain, tessellatedPositions.get(poleB));

            this.computeArrowheadGeometry(dc, poleA, poleB, polePtA, polePtB, buffer, pathData);

            poleA = poleB;
            polePtA = polePtB;
        }

        buffer.flip();

        // Create an extent for the arrowheads if we're picking.
        if (dc.isPickingMode())
        {
            if (buffer.remaining() != 0)
            {
                Box box = Box.computeBoundingBox(new BufferWrapper.FloatBufferWrapper(buffer), 3);
                box = box.translate(pathData.getReferencePoint());
                pathData.setValue(ARROWS_EXTENT, box);
            }
            else
            {
                pathData.setValue(ARROWS_EXTENT, null);
            }
        }
    }

    /**
     * Compute the geometry of a direction arrow between two points.
     *
     * @param dc       current draw context
     * @param polePtA  the first pole position. This is one of the application defined path positions.
     * @param polePtB  second pole position
     * @param buffer   buffer in which to place computed points
     * @param pathData the current globe-specific path data.
     */
    protected void computeArrowheadGeometry(DrawContext dc, int poleA, int poleB, Vec4 polePtA, Vec4 polePtB,
        FloatBuffer buffer, PathData pathData)
    {
        // Build a triangle to represent the arrowhead. The triangle is built from two vectors, one parallel to the
        // segment, and one perpendicular to it. The plane of the arrowhead will be parallel to the surface.

        double arrowLength = this.getArrowLength();
        double arrowBase = arrowLength * this.getArrowAngle().tanHalfAngle();
        double poleDistance = polePtA.distanceTo3(polePtB);

        // Find the segment that is midway between the two poles.
        int midIndex = (poleA + poleB) / 2;
        List<Position> tessellatedPositions = pathData.getTessellatedPositions();
        Position posA = tessellatedPositions.get(midIndex);
        Position posB = tessellatedPositions.get(midIndex + 1);
        Terrain terrain = dc.getTerrain();
        Vec4 ptA = this.computePoint(dc, terrain, posA);
        Vec4 ptB = this.computePoint(dc, terrain, posB);

        // Compute parallel component
        Vec4 parallel = ptA.subtract3(ptB);

        // Compute perpendicular component
        Vec4 surfaceNormal = dc.getGlobe().computeSurfaceNormalAtPoint(ptB);
        Vec4 perpendicular = surfaceNormal.cross3(parallel);

        // Compute midpoint of segment. When the number of segments is odd, the midpoint falls between two tessellated
        // positions. When the number of segments is even, the midpoint falls on the middle tessellated position.
        Vec4 midPoint;
        if ((poleA - poleB) % 2 != 0)
        {
            midPoint = ptA.add3(ptB).divide3(2.0);
        }
        else
        {
            midPoint = ptA;
        }

        if (!this.isArrowheadSmall(dc, midPoint, 1))
        {
            // Compute the size of the arrowhead in pixels to ensure that the arrow does not exceed the maximum
            // screen size.
            View view = dc.getView();
            double midpointDistance = view.getEyePoint().distanceTo3(midPoint);
            double pixelSize = view.computePixelSizeAtDistance(midpointDistance);
            if (arrowLength / pixelSize > this.maxScreenSize)
            {
                arrowLength = this.maxScreenSize * pixelSize;
                arrowBase = arrowLength * this.getArrowAngle().tanHalfAngle();
            }

            // Don't draw an arrowhead if the path segment is smaller than the arrow
            if (poleDistance <= arrowLength)
            {
                return;
            }

            perpendicular = perpendicular.normalize3().multiply3(arrowBase);
            parallel = parallel.normalize3().multiply3(arrowLength);

            // Center the arrow on the midpoint.
            midPoint = midPoint.subtract3(parallel.divide3(2.0));

            // Compute geometry of direction arrow
            Vec4 vertex1 = midPoint.add3(parallel).add3(perpendicular);
            Vec4 vertex2 = midPoint.add3(parallel).add3(perpendicular.multiply3(-1.0));

            // Add geometry to the buffer
            Vec4 referencePoint = pathData.getReferencePoint();
            buffer.put((float) (vertex1.x - referencePoint.x));
            buffer.put((float) (vertex1.y - referencePoint.y));
            buffer.put((float) (vertex1.z - referencePoint.z));

            buffer.put((float) (vertex2.x - referencePoint.x));
            buffer.put((float) (vertex2.y - referencePoint.y));
            buffer.put((float) (vertex2.z - referencePoint.z));

            buffer.put((float) (midPoint.x - referencePoint.x));
            buffer.put((float) (midPoint.y - referencePoint.y));
            buffer.put((float) (midPoint.z - referencePoint.z));
        }
    }
//
//    /** {@inheritDoc} */
//    @Override
//    protected boolean mustRegenerateGeometry(DrawContext dc)
//    {
//        // Path never regenerates geometry for absolute altitude mode paths, but the direction arrows in DirectedPath
//        // need to be recomputed because the view may have changed and the size of the arrows needs to be recalculated.
//        if (this.getCurrentPathData().isExpired(dc))
//            return true;
//
//        return super.mustRegenerateGeometry(dc);
//    }

    /**
     * Determines if an direction arrow drawn a point will be less than a specified number of pixels.
     *
     * @param dc        current draw context
     * @param arrowPt   point at which to draw direction arrow
     * @param numPixels the number of pixels which is considered to be "small"
     *
     * @return {@code true} if an arrow drawn at {@code arrowPt} would occupy less than or equal to {@code numPixels}.
     */
    protected boolean isArrowheadSmall(DrawContext dc, Vec4 arrowPt, int numPixels)
    {
        return this.getArrowLength() <= numPixels * dc.getView().computePixelSizeAtDistance(
            dc.getView().getEyePoint().distanceTo3(arrowPt));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to also draw direction arrows.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        this.computeDirectionArrows(dc, this.getCurrentPathData());
        this.drawDirectionArrows(dc, this.getCurrentPathData());
        super.doDrawOutline(dc);
    }

    /**
     * Draws this DirectedPath's direction arrows. Called from {@link #doDrawOutline(gov.nasa.worldwind.render.DrawContext)}
     * before drawing the Path's actual outline.
     * <p/>
     * If this Path is entirely located on the terrain, this applies an offset to the arrow's depth values to to ensure
     * they shows over the terrain. This does not apply a depth offset in any other case to avoid incorrectly drawing
     * the arrows over objects they should be behind, including the terrain. In addition to applying a depth offset,
     * this disables writing to the depth buffer to avoid causing subsequently drawn ordered renderables to incorrectly
     * fail the depth test. Since the arrows are located on the terrain, the terrain already provides the necessary
     * depth values and we can be certain that other ordered renderables should appear on top of them.
     *
     * @param dc       Current draw context.
     * @param pathData the current globe-specific path data.
     */
    protected void drawDirectionArrows(DrawContext dc, PathData pathData)
    {
        FloatBuffer points = (FloatBuffer) pathData.getValue(ARROWS_KEY);
        if (points == null || points.remaining() == 0)
        {
            return;
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        boolean projectionOffsetPushed = false; // keep track for error recovery

        try
        {
            if (this.isSurfacePath(dc))
            {
                // Pull the arrow triangles forward just a bit to ensure they show over the terrain.
                dc.pushProjectionOffest(SURFACE_PATH_DEPTH_OFFSET);
                gl.glDepthMask(false);
                projectionOffsetPushed = true;
            }

            gl.glVertexPointer(3, GL.GL_FLOAT, 0, points);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, points.remaining() / 3);
        }
        finally
        {
            if (projectionOffsetPushed)
            {
                dc.popProjectionOffest();
                gl.glDepthMask(true);
            }
        }
    }
}
