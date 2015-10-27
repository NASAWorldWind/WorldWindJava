/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.render.*;

/**
 * Holds common attributes for World Wind {@link Airspace} shapes. AirspaceAttributes was originally designed as a
 * special purpose attribute bundle for Airspace, but is now a redundant subinterface of {@link
 * gov.nasa.worldwind.render.ShapeAttributes}. AirspaceAttributes is still used by Airspace shapes to ensure backward
 * compatibility with earlier versions of World Wind. Usage of methods unique to AirspaceAttributes should be replaced
 * with the equivalent methods in ShapeAttributes.
 *
 * @author dcollins
 * @version $Id: AirspaceAttributes.java 2222 2014-08-13 21:25:29Z dcollins $
 */
public interface AirspaceAttributes extends ShapeAttributes
{
    /**
     * Get the <code>Material</code> used to draw the shape interior or volume. This method is deprecated, and should be
     * replaced with usage of {@link #getInteriorMaterial()}.
     *
     * @return the <code>Material</code> used to draw the shape interior or volume.
     *
     * @deprecated Use {@link #getInteriorMaterial()} instead.
     */
    Material getMaterial();

    /**
     * Sets the <code>Material</code> used to draw the shape interior or volume. This method is deprecated, and should
     * be replaced with usage of {@link #setInteriorMaterial(gov.nasa.worldwind.render.Material)}.
     *
     * @param material the <code>Material</code> used to draw the shape interior or volume.
     *
     * @deprecated Use {@link #setInteriorMaterial(gov.nasa.worldwind.render.Material)} instead.
     */
    void setMaterial(Material material);

    /**
     * Returns the shape's opacity. This method is deprecated, and should be replaced with usage of {@link
     * #getInteriorOpacity()}.
     *
     * @return the shape's opacity in the range [0, 1], where 0 indicates full transparency and 1 indicates full
     *         opacity.
     *
     * @deprecated Use {@link #getInteriorOpacity()} instead.
     */
    double getOpacity();

    /**
     * Set the shape's opacity. This method is deprecated, and should be replaced with usage of {@link
     * #setInteriorOpacity(double)}.
     *
     * @param opacity the shape's opacity in the range [0, 1], where 0 indicates full transparency and 1 indicates full
     *                opacity.
     *
     * @deprecated Use {@link #setInteriorOpacity(double)} instead.
     */
    void setOpacity(double opacity);

    /**
     * Applies the interior attributes to the current OpenGL state. When enableMaterial is true, this sets the current
     * OpenGL material state with the interior material and the interior opacity. Otherwise, this sets the current
     * OpenGL color state to the interior material's diffuse color.
     *
     * @param dc             the current drawing context.
     * @param enableMaterial true to set OpenGL material state, false to set OpenGL color state.
     *
     * @throws IllegalArgumentException if the drawing context is null.
     * @deprecated Use {@link Material#apply(javax.media.opengl.GL2, int)} or make OpenGL state changes directly.
     */
    void applyInterior(DrawContext dc, boolean enableMaterial);

    /**
     * Applies the outline attributes to the current OpenGL state. When enableMaterial is true, this sets the current
     * OpenGL material state with the outline material and the outline opacity. Otherwise, this sets the current OpenGL
     * color state to the outline material's diffuse color.
     *
     * @param dc             the current drawing context.
     * @param enableMaterial true to set OpenGL material state, false to set OpenGL color state.
     *
     * @throws IllegalArgumentException if the drawing context is null.
     * @deprecated Use {@link Material#apply(javax.media.opengl.GL2, int)} or make OpenGL state changes directly.
     */
    void applyOutline(DrawContext dc, boolean enableMaterial);
}
