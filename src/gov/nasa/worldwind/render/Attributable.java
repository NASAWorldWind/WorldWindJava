/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

/**
 * Interface for shapes that use {@link gov.nasa.worldwind.render.ShapeAttributes}.
 *
 * @author tag
 * @version $Id: Attributable.java 2339 2014-09-22 18:22:37Z tgaskins $
 */
public interface Attributable
{
    /**
     * Set the shape's attributes.
     *
     * @param attributes the attributes to assign to the shape.
     */
    void setAttributes(ShapeAttributes attributes);

    /**
     * Return the shape's current attributes.
     *
     * @return the shape's current attributes.
     */
    ShapeAttributes getAttributes();

    /**
     * Set the shape's highlight attributes.
     *
     * @param highlightAttributes the highlight attributes to assign to the shape.
     */
    void setHighlightAttributes(ShapeAttributes highlightAttributes);

    /**
     * Return the shape's highlight attributes.
     *
     * @return the shape's highlight attributes.
     */
    ShapeAttributes getHighlightAttributes();
}
