/*
 * Copyright (C) 2015 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.nio.FloatBuffer;
import java.util.*;

/**
 * A {@link SurfacePolyline} that draws arrowheads between the polyline positions to indicate direction. All arrowheads
 * are drawn at a constant geographic size (the arrows get smaller as the view moves away from the path, and larger as
 * the view get closer to the path). One arrowhead is drawn on each polyline segment, unless the segment is smaller than
 * the arrowhead, in which case the arrowhead is not drawn.
 *
 * @author dcollins
 * @version $Id: DirectedSurfacePolyline.java 3033 2015-04-17 17:54:56Z dcollins $
 */
public class DirectedSurfacePolyline extends SurfacePolyline
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

    /** Constructs a new directed surface polyline with the default attributes and no locations. */
    public DirectedSurfacePolyline()
    {
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public DirectedSurfacePolyline(DirectedSurfacePolyline source)
    {
        super(source);

        this.arrowLength = source.getArrowLength();
        this.arrowAngle = source.getArrowAngle();
        this.maxScreenSize = source.getMaxScreenSize();
    }

    /**
     * Constructs a new directed surface polyline with the specified normal (as opposed to highlight) attributes and no
     * locations. Modifying the attribute reference after calling this constructor causes this shape's appearance to
     * change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public DirectedSurfacePolyline(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    /**
     * Constructs a new directed surface polyline with the default attributes and the specified iterable of locations.
     * <p/>
     * Note: If fewer than two locations is specified, no polyline is drawn.
     *
     * @param iterable the polyline locations.
     *
     * @throws IllegalArgumentException if the locations iterable is null.
     */
    public DirectedSurfacePolyline(Iterable<? extends LatLon> iterable)
    {
        super(iterable);
    }

    /**
     * Constructs a new directed surface polyline with the specified normal (as opposed to highlight) attributes and the
     * specified iterable of locations. Modifying the attribute reference after calling this constructor causes this
     * shape's appearance to change accordingly.
     * <p/>
     * Note: If fewer than two locations is specified, no polyline is drawn.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param iterable    the polyline locations.
     *
     * @throws IllegalArgumentException if the locations iterable is null.
     */
    public DirectedSurfacePolyline(ShapeAttributes normalAttrs,
        Iterable<? extends LatLon> iterable)
    {
        super(normalAttrs, iterable);
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

        if (this.arrowLength != arrowLength)
        {
            this.arrowLength = arrowLength;
            this.onShapeChanged();
        }
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

        if (this.maxScreenSize != maxScreenSize)
        {
            this.maxScreenSize = maxScreenSize;
            this.onShapeChanged();
        }
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

        if (!this.arrowAngle.equals(arrowAngle))
        {
            this.arrowAngle = arrowAngle;
            this.onShapeChanged();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to also draw direction arrows.
     *
     * @param dc  the current draw context.
     * @param sdc the context containing a geographic region and screen viewport corresponding to a surface tile.
     */
    @Override
    protected void drawOutline(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        super.drawOutline(dc, sdc);
        this.computeDirectionArrows(dc, sdc);
        this.drawDirectionArrows(dc);
    }

    /**
     * Computes the geometry of the direction arrows. Called from {@link #drawOutline(gov.nasa.worldwind.render.DrawContext,
     * gov.nasa.worldwind.util.SurfaceTileDrawContext)}.
     *
     * @param dc  the current draw context.
     * @param sdc the context containing a geographic region and screen viewport corresponding to a surface tile.
     */
    protected void computeDirectionArrows(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Reset the global surface shape vertex buffer.
        if (vertexBuffer != null)
        {
            vertexBuffer.clear();
        }

        // Accumulate arrowhead geometry in the global surface shape vertex buffer.
        LatLon first = null, last = null;
        for (LatLon location : this.getLocations())
        {
            if (last != null)
            {
                this.computeArrowheadGeometry(dc, sdc, last, location);
            }
            else
            {
                first = location;
            }

            last = location;
        }

        // Generate the arrowhead geometry for a closed path when necessary.
        if (this.isClosed() && first != null && !first.equals(last))
        {
            this.computeArrowheadGeometry(dc, sdc, last, first);
        }

        // Prepare the global surface shape vertex buffer for drawing.
        if (vertexBuffer != null)
        {
            vertexBuffer.flip();
        }
    }

    /**
     * Compute the geometry of a direction arrow for a polyline segment.
     *
     * @param dc     the current draw context.
     * @param sdc    the context containing a geographic region and screen viewport corresponding to a surface tile.
     * @param begin  the polyline segment's beginning location.
     * @param end    the polyline segment's ending location.
     */
    protected void computeArrowheadGeometry(DrawContext dc, SurfaceTileDrawContext sdc, LatLon begin, LatLon end)
    {
        // Don't draw an arrowhead if the arrow length is smaller than one pixel.
        double arrowLength = (this.arrowLength / dc.getGlobe().getRadius()) * (180 / Math.PI);
        double pixelSize = sdc.getSector().getDeltaLatDegrees() / sdc.getViewport().getHeight();
        if (arrowLength <= pixelSize)
        {
            return;
        }

        // Limit the size of the arrowhead in pixels to ensure that the arrow does not exceed the maximum pixel size.
        double maxLength = pixelSize * this.maxScreenSize;
        if (arrowLength > maxLength)
        {
            arrowLength = maxLength;
        }

        // Don't draw an arrowhead if the segment is smaller than the arrow.
        double segmentLength = LatLon.greatCircleDistance(begin, end).degrees;
        if (segmentLength <= arrowLength)
        {
            return;
        }

        // Compute the location at which to center the arrowhead.
        LatLon mid = LatLon.interpolate(this.getPathType(), 0.5, begin, end);

        // Handle anti-meridian spanning segments when the path type indicates linear interpolation.
        if ((this.getPathType() == null || this.getPathType().equals(AVKey.LINEAR)) &&
            LatLon.locationsCrossDateline(begin, end))
        {
            double datelineOffset = begin.longitude.degrees < 0 ? -360 : 360;
            LatLon falseEnd = LatLon.fromDegrees(end.latitude.degrees, end.longitude.degrees + datelineOffset);
            mid = LatLon.interpolate(this.getPathType(), 0.5, begin, falseEnd);
        }

        // Compute the locations of the arrowhead's base and tip.
        Angle halfArrowLength = Angle.fromDegrees(arrowLength / 2);
        LatLon base = LatLon.greatCircleEndPosition(mid, LatLon.greatCircleAzimuth(mid, begin), halfArrowLength);
        LatLon tip = LatLon.greatCircleEndPosition(mid, LatLon.greatCircleAzimuth(mid, end), halfArrowLength);

        // Compute the locations of the arrowhead's left and right vertices.
        Angle halfBaseLength = Angle.fromDegrees(arrowLength * this.arrowAngle.tanHalfAngle());
        Angle azimuth = LatLon.greatCircleAzimuth(base, tip);
        LatLon left = LatLon.greatCircleEndPosition(base, azimuth.add(Angle.NEG90), halfBaseLength);
        LatLon right = LatLon.greatCircleEndPosition(base, azimuth.subtract(Angle.NEG90), halfBaseLength);

        // Add the arrowhead's geometry to the global surface shape vertex buffer, compensating for arrowheads that span
        // the anti-meridian
        List<List<LatLon>> drawLocations = this.repeatAroundDateline(Arrays.asList(tip, left, right));
        LatLon referenceLocation = this.getReferencePosition();

        for (List<LatLon> list : drawLocations)
        {
            // Allocate space in the global surface shape vertex buffer.
            int count = 2 * list.size();
            if (vertexBuffer == null)
            {
                vertexBuffer = Buffers.newDirectFloatBuffer(count);
            }
            else if (vertexBuffer.remaining() < count)
            {
                vertexBuffer.flip();
                FloatBuffer newBuffer = Buffers.newDirectFloatBuffer(vertexBuffer.capacity() + count);
                newBuffer.put(vertexBuffer);
                vertexBuffer = newBuffer;
            }

            for (LatLon location : list)
            {
                vertexBuffer.put((float) (location.longitude.degrees - referenceLocation.longitude.degrees));
                vertexBuffer.put((float) (location.latitude.degrees - referenceLocation.latitude.degrees));
            }
        }
    }

    /**
     * Draws this DirectedSurfacePolyline's direction arrows. Called from {@link #drawOutline(gov.nasa.worldwind.render.DrawContext,
     * gov.nasa.worldwind.util.SurfaceTileDrawContext)} after drawing the polyline's actual outline.
     *
     * @param dc the current draw context.
     */
    protected void drawDirectionArrows(DrawContext dc)
    {
        // Draw any arrowhead geometry accumulated in the global surface shape vertex buffer.
        if (vertexBuffer != null && vertexBuffer.remaining() > 0)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexBuffer.remaining() / 2);
        }
    }
}
