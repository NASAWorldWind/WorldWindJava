/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.awt.*;
import java.nio.DoubleBuffer;

/**
 * Static class for drawing 2D frames. <p> All shapes are drawn inside a bounding rectangle whose lower left corner is
 * at the origin. Shapes with a leader use an offset point that indicate where the leader triangle should point at - it
 * usually has a negative y since the leader connects at the bottom of the frame (at y = 0). </p>
 *
 * @author Patrick Murris
 * @version $Id: FrameFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see AbstractAnnotation
 */
public class FrameFactory
{
    /** @deprecated Use {@link AVKey#SHAPE_RECTANGLE} instead. */
    @Deprecated
    public static final String SHAPE_RECTANGLE = AVKey.SHAPE_RECTANGLE;
    /** @deprecated Use {@link AVKey#SHAPE_ELLIPSE} instead. */
    @Deprecated
    public static final String SHAPE_ELLIPSE = AVKey.SHAPE_ELLIPSE;
    /** @deprecated Use {@link AVKey#SHAPE_NONE} instead. */
    @Deprecated
    public static final String SHAPE_NONE = AVKey.SHAPE_NONE;
    /** @deprecated Use {@link AVKey#SHAPE_TRIANGLE} instead. */
    @Deprecated
    public static final String LEADER_TRIANGLE = AVKey.SHAPE_TRIANGLE;
    /** @deprecated Use {@link AVKey#SHAPE_NONE} instead. */
    @Deprecated
    public static final String LEADER_NONE = AVKey.SHAPE_NONE;

    private static int cornerSteps = 16;
    private static int circleSteps = 64;

    /**
     * Draw a shape with the specified width and height, gl mode and corner radius. GL mode came be one of
     * <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>. Corner radius only apply to
     * <code>SHAPE_RECTANGLE</code> - set to zero for square corners.
     *
     * @param dc           the current <code>DrawContext</code>.
     * @param shape        the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width        the width of the overall shape.
     * @param height       the height of the shape.
     * @param glMode       the GL mode - can be one of <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>.
     * @param cornerRadius the rounded corners radius. Set to zero for square corners.
     */
    public static void drawShape(DrawContext dc, String shape, double width, double height, int glMode,
        int cornerRadius)
    {
        if (!shape.equals(AVKey.SHAPE_NONE))
            drawBuffer(dc, glMode, createShapeBuffer(shape, width, height, cornerRadius, null));
    }

    /**
     * Draw a shape with the specified width and height, gl mode and corner radius. The shape includes a leader triangle
     * pointing to a specified point. GL mode came be one of <code>GL.GL_TRIANGLE_FAN</code> and
     * <code>GL.LINE_STRIP</code>. Corner radius only apply to <code>SHAPE_RECTANGLE</code> - set to zero for square
     * corners.
     *
     * @param dc             the current <code>DrawContext</code>.
     * @param shape          the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width          the width of the overall shape.
     * @param height         the height of the shape excluding the leader.
     * @param leaderOffset   the coordinates of the point to which the leader leads.
     * @param leaderGapWidth the starting width of the leader shape.
     * @param glMode         the GL mode - can be one of <code>GL.GL_TRIANGLE_FAN</code> and
     *                       <code>GL.LINE_STRIP</code>.
     * @param cornerRadius   the rounded corners radius. Set to zero for square corners.
     */
    public static void drawShapeWithLeader(DrawContext dc, String shape, double width, double height,
        Point leaderOffset, double leaderGapWidth, int glMode, int cornerRadius)
    {
        if (!shape.equals(AVKey.SHAPE_NONE))
            drawBuffer(dc, glMode,
                createShapeWithLeaderBuffer(shape, width, height, leaderOffset, leaderGapWidth, cornerRadius, null));
    }

    /**
     * Create a vertex buffer for a shape with the specified width, height and corner radius. Corner radius only apply
     * to <code>SHAPE_RECTANGLE</code> - set to zero for square corners.
     *
     * @param shape        the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width        the width of the overall shape.
     * @param height       the height of the shape.
     * @param cornerRadius the rounded corners radius. Set to zero for square corners.
     * @param buffer       the buffer to store shape vertices, or null to allocate a new buffer.
     *
     * @return the vertex buffer.
     */
    public static DoubleBuffer createShapeBuffer(String shape, double width, double height, int cornerRadius,
        DoubleBuffer buffer)
    {
        if (shape.equals(AVKey.SHAPE_RECTANGLE))
            return createRoundedRectangleBuffer(width, height, cornerRadius, buffer);
        else if (shape.equals(AVKey.SHAPE_ELLIPSE))
            return createEllipseBuffer(width, height, circleSteps, buffer);
        else if (shape.equals(AVKey.SHAPE_NONE))
            return null;
        else
            // default to rectangle if shape unknown
            return createRoundedRectangleBuffer(width, height, cornerRadius, buffer);
    }

    /**
     * Create a vertex buffer for a shape with the specified width, height and corner radius. The shape includes a
     * leader triangle pointing to a specified point. Corner radius only apply to <code>SHAPE_RECTANGLE</code> - set to
     * zero for square corners.
     *
     * @param shape          the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width          the width of the overall shape.
     * @param height         the height of the shape excluding the leader.
     * @param leaderOffset   the coordinates of the point to which the leader leads.
     * @param leaderGapWidth the starting width of the leader shape.
     * @param cornerRadius   the rounded corners radius. Set to zero for square corners.
     * @param buffer         the buffer to store shape vertices, or null to allocate a new buffer.
     *
     * @return the vertex buffer.
     */
    public static DoubleBuffer createShapeWithLeaderBuffer(String shape, double width, double height,
        Point leaderOffset, double leaderGapWidth, int cornerRadius, DoubleBuffer buffer)
    {
        if (shape.equals(AVKey.SHAPE_RECTANGLE))
            return createRoundedRectangleWithLeaderBuffer(width, height, leaderOffset, leaderGapWidth, cornerRadius,
                buffer);
        else if (shape.equals(AVKey.SHAPE_ELLIPSE))
            return createEllipseWithLeaderBuffer(width, height, leaderOffset, leaderGapWidth, circleSteps, buffer);
        else if (shape.equals(AVKey.SHAPE_NONE))
            return null;
        else
            // default to rectangle if shape unknown
            return createRoundedRectangleWithLeaderBuffer(width, height, leaderOffset, leaderGapWidth, cornerRadius,
                buffer);
    }

    /**
     * Draw a vertex buffer in a given gl mode. Vertex buffers coming from the createShapeBuffer() methods support both
     * <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>.
     *
     * @param dc    the current DrawContext.
     * @param mode  the desired drawing GL mode.
     * @param count the number of vertices to draw.
     * @param verts the vertex buffer to draw.
     */
    public static void drawBuffer(DrawContext dc, int mode, int count, DoubleBuffer verts)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (verts == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        // Set up
        gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL2.GL_DOUBLE, 0, verts);
        // Draw
        gl.glDrawArrays(mode, 0, count);
        // Restore
        gl.glPopClientAttrib();
    }

    /**
     * Draw a vertex buffer with texture coordinates in a given gl mode. Vertex buffers coming from the
     * createShapeBuffer() methods support both <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>.
     *
     * @param dc     the current DrawContext.
     * @param mode   the desired drawing GL mode.
     * @param count  the number of vertices to draw.
     * @param verts  the vertex buffer to draw.
     * @param coords the buffer containing the shape texture coordinates.
     */
    public static void drawBuffer(DrawContext dc, int mode, int count, DoubleBuffer verts, DoubleBuffer coords)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (verts == null || coords == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        // Set up
        gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glVertexPointer(2, GL2.GL_DOUBLE, 0, verts);
        gl.glTexCoordPointer(2, GL2.GL_DOUBLE, 0, coords);
        // Draw
        gl.glDrawArrays(mode, 0, count);
        // Restore
        gl.glPopClientAttrib();
    }

    public static void drawBuffer(DrawContext dc, int mode, DoubleBuffer verts)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (verts == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int count = verts.remaining() / 2;
        drawBuffer(dc, mode, count, verts);
    }

    //-- Shape creation
    //-- Rectangle ------------------------------------------------------------------

    private static DoubleBuffer createRoundedRectangleBuffer(double width, double height, int cornerRadius,
        DoubleBuffer buffer)
    {
        int numVertices = 9 + (cornerRadius < 1 ? 0 : 4 * (cornerSteps - 2));
        buffer = allocateVertexBuffer(numVertices, buffer);

        int idx = 0;
        // Drawing counter clockwise from bottom-left
        // Bottom
        buffer.put(idx++, (double) cornerRadius);
        buffer.put(idx++, 0d);
        buffer.put(idx++, width - cornerRadius);
        buffer.put(idx++, 0d);
        idx = drawCorner(width - cornerRadius, cornerRadius, cornerRadius, -Math.PI / 2, 0, cornerSteps, buffer, idx);
        // Right
        buffer.put(idx++, width);
        buffer.put(idx++, (double) cornerRadius);
        buffer.put(idx++, width);
        buffer.put(idx++, height - cornerRadius);
        idx = drawCorner(width - cornerRadius, height - cornerRadius, cornerRadius, 0, Math.PI / 2, cornerSteps, buffer,
            idx);
        // Top
        buffer.put(idx++, width - cornerRadius);
        buffer.put(idx++, height);
        buffer.put(idx++, (double) cornerRadius);
        buffer.put(idx++, height);
        idx = drawCorner(cornerRadius, height - cornerRadius, cornerRadius, Math.PI / 2, Math.PI, cornerSteps, buffer,
            idx);
        // Left
        buffer.put(idx++, 0d);
        buffer.put(idx++, height - cornerRadius);
        buffer.put(idx++, 0d);
        buffer.put(idx++, (double) cornerRadius);
        idx = drawCorner(cornerRadius, cornerRadius, cornerRadius, Math.PI, Math.PI * 1.5, cornerSteps, buffer, idx);
        // Finish up to starting point
        buffer.put(idx++, (double) cornerRadius);
        buffer.put(idx++, 0d);

        buffer.limit(idx);
        return buffer;
    }

    private static DoubleBuffer createRoundedRectangleWithLeaderBuffer(double width, double height, Point leaderOffset,
        double leaderGapWidth, int cornerRadius, DoubleBuffer buffer)
    {
        int numVertices = 12 + (cornerRadius < 1 ? 0 : 4 * (cornerSteps - 2));
        buffer = allocateVertexBuffer(numVertices, buffer);

        int idx = 0;
        // Drawing counter clockwise from right leader connection at the bottom
        // so as to accommodate GL_TRIANGLE_FAN and GL_LINE_STRIP (inside and border)
        // Bottom right
        buffer.put(idx++, width / 2 + leaderGapWidth / 2);
        buffer.put(idx++, 0d);
        buffer.put(idx++, width - cornerRadius);
        buffer.put(idx++, 0d);
        idx = drawCorner(width - cornerRadius, cornerRadius, cornerRadius, -Math.PI / 2, 0, cornerSteps, buffer, idx);
        // Right
        buffer.put(idx++, width);
        buffer.put(idx++, (double) cornerRadius);
        buffer.put(idx++, width);
        buffer.put(idx++, height - cornerRadius);
        idx = drawCorner(width - cornerRadius, height - cornerRadius, cornerRadius, 0, Math.PI / 2, cornerSteps, buffer,
            idx);
        // Top
        buffer.put(idx++, width - cornerRadius);
        buffer.put(idx++, height);
        buffer.put(idx++, (double) cornerRadius);
        buffer.put(idx++, height);
        idx = drawCorner(cornerRadius, height - cornerRadius, cornerRadius, Math.PI / 2, Math.PI, cornerSteps, buffer,
            idx);
        // Left
        buffer.put(idx++, 0d);
        buffer.put(idx++, height - cornerRadius);
        buffer.put(idx++, 0d);
        buffer.put(idx++, (double) cornerRadius);
        idx = drawCorner(cornerRadius, cornerRadius, cornerRadius, Math.PI, Math.PI * 1.5, cornerSteps, buffer, idx);
        // Bottom left
        buffer.put(idx++, (double) cornerRadius);
        buffer.put(idx++, 0d);
        buffer.put(idx++, width / 2 - leaderGapWidth / 2);
        buffer.put(idx++, 0d);
        // Draw leader
        buffer.put(idx++, leaderOffset.x);
        buffer.put(idx++, leaderOffset.y);
        buffer.put(idx++, width / 2 + leaderGapWidth / 2);
        buffer.put(idx++, 0d);

        buffer.limit(idx);
        return buffer;
    }

    private static int drawCorner(double x0, double y0, double cornerRadius, double start, double end, int steps,
        DoubleBuffer buffer, int startIdx)
    {
        if (cornerRadius < 1)
            return startIdx;

        double step = (end - start) / (steps - 1);
        for (int i = 1; i < steps - 1; i++)
        {
            double a = start + step * i;
            double x = x0 + Math.cos(a) * cornerRadius;
            double y = y0 + Math.sin(a) * cornerRadius;
            buffer.put(startIdx++, x);
            buffer.put(startIdx++, y);
        }

        return startIdx;
    }

    //-- Circle / Ellipse -----------------------------------------------------------

    private static DoubleBuffer createEllipseBuffer(double width, double height, int steps, DoubleBuffer buffer)
    {
        int numVertices = steps + 1;
        buffer = allocateVertexBuffer(numVertices, buffer);

        // Drawing counter clockwise from bottom-left
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfPI = Math.PI / 2;
        double x0 = halfWidth;
        double y0 = halfHeight;
        double step = Math.PI * 2 / steps;

        int idx = 0;
        for (int i = 0; i <= steps; i++)
        {
            double a = step * i - halfPI;
            double x = x0 + Math.cos(a) * halfWidth;
            double y = y0 + Math.sin(a) * halfHeight;
            buffer.put(idx++, x);
            buffer.put(idx++, y);
        }

        buffer.limit(idx);
        return buffer;
    }

    private static DoubleBuffer createEllipseWithLeaderBuffer(double width, double height, Point leaderOffset,
        double leaderGapWidth, int steps, DoubleBuffer buffer)
    {
        int numVertices = steps + 3;
        buffer = allocateVertexBuffer(numVertices, buffer);

        // Drawing counter clockwise from right leader connection at the bottom
        // so as to accomodate GL_TRIANGLE_FAN and GL_LINE_STRIP (inside and border)
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfPI = Math.PI / 2;
        double x0 = halfWidth;
        double y0 = halfHeight;
        double step = Math.PI * 2 / steps;
        double halfGap = leaderGapWidth / 2 / halfWidth;

        int idx = 0;
        for (int i = 0; i <= steps; i++)
        {
            double a = step * i - halfPI;
            if (i == 0)
                a += halfGap;
            if (i == steps)
                a -= halfGap;
            double x = x0 + Math.cos(a) * halfWidth;
            double y = y0 + Math.sin(a) * halfHeight;
            buffer.put(idx++, x);
            buffer.put(idx++, y);
        }

        // Draw leader
        buffer.put(idx++, leaderOffset.x);
        buffer.put(idx++, leaderOffset.y);
        buffer.put(idx++, x0 + Math.cos(halfGap - halfPI) * halfWidth);
        buffer.put(idx++, y0 + Math.sin(halfGap - halfPI) * halfHeight);

        buffer.limit(idx);
        return buffer;
    }

    //-- Utility Methods

    private static DoubleBuffer allocateVertexBuffer(int numVertices, DoubleBuffer buffer)
    {
        int numCoords = 2 * numVertices;

        if (buffer != null)
            buffer.clear();

        if (buffer == null || buffer.capacity() < numCoords)
            buffer = Buffers.newDirectDoubleBuffer(numCoords);

        return buffer;
    }
}
