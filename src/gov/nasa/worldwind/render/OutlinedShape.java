/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

/**
 * Defines the interface for the multi-pass rendering technique implemented by {@link
 * DrawContext#drawOutlinedShape(OutlinedShape, Object)} that renders outlines around filled shapes correctly and
 * resolves depth-buffer fighting between the shape being drawn and those previously drawn. The methods of this
 * interface are called by the draw context during the multi-pass rendering.
 *
 * @author tag
 * @version $Id: OutlinedShape.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface OutlinedShape
{
    /**
     * Indicates whether the shape's outline is drawn.
     *
     * @param dc    the current draw context.
     * @param shape the shape being drawn.
     *
     * @return true if the shape's outline should be drawn, otherwise false.
     */
    boolean isDrawOutline(DrawContext dc, Object shape);

    /**
     * Indicates whether the shape's faces are drawn.
     *
     * @param dc    the current draw context.
     * @param shape the shape being drawn.
     *
     * @return true if the shape's faces should be drawn, otherwise false.
     */
    boolean isDrawInterior(DrawContext dc, Object shape);

    /**
     * Indicates whether the shape's depth should be adjusted to give its filled faces priority over coincident items
     * previously drawn.
     *
     * @param dc    the current draw context.
     * @param shape the shape being drawn.
     *
     * @return true if the shape should have priority, otherwise false.
     */
    boolean isEnableDepthOffset(DrawContext dc, Object shape);

    /**
     * Draws the shape's outline.
     *
     * @param dc    the current draw context.
     * @param shape the shape being drawn.
     */
    void drawOutline(DrawContext dc, Object shape);

    /**
     * Draws the shape's filled faces.
     *
     * @param dc    the current draw context.
     * @param shape the shape being drawn.
     */
    void drawInterior(DrawContext dc, Object shape);

    /**
     * Returns the depth-offset factor.
     * <p>
     * The amount of depth offset when depth offset is enabled is computed by the formula <i>factor</i> * DZ + r *
     * <i>units</i>, where DZ is a measurement of the change in depth relative to the screen area of the shape, and r is
     * the smallest value guaranteed to produce a resolvable offset. <i>units</i> is the value return by {@link
     * #getDepthOffsetUnits(DrawContext, Object)}.
     *
     * @param dc    the current draw context.
     * @param shape the shape being drawn.
     *
     * @return the depth offset factor to use for the shape.
     */
    Double getDepthOffsetFactor(DrawContext dc, Object shape);

    /**
     * Returns the depth-offset units.
     * <p>
     * The amount of depth offset when depth offset is enabled is computed by the formula <i>factor</i> * DZ + r *
     * <i>units</i>, where DZ is a measurement of the change in depth relative to the screen area of the shape, and r is
     * the smallest value guaranteed to produce a resolvable offset. <i>factor</i> is the value return by {@link
     * #getDepthOffsetFactor(DrawContext, Object)}.
     *
     * @param dc    the current draw context.
     * @param shape the shape being drawn.
     *
     * @return the depth units to use for the shape.
     */
    Double getDepthOffsetUnits(DrawContext dc, Object shape);
}
