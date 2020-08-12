/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.render.*;

/**
 * Holds common attributes for WorldWind {@link Airspace} shapes. AirspaceAttributes was originally designed as a
 * special purpose attribute bundle for Airspace, but is now a redundant subinterface of {@link
 * gov.nasa.worldwind.render.ShapeAttributes}. AirspaceAttributes is still used by Airspace shapes to ensure backward
 * compatibility with earlier versions of WorldWind. Usage of methods unique to AirspaceAttributes should be replaced
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
    @Deprecated
    Material getMaterial();

    /**
     * Sets the <code>Material</code> used to draw the shape interior or volume. This method is deprecated, and should
     * be replaced with usage of {@link #setInteriorMaterial(gov.nasa.worldwind.render.Material)}.
     *
     * @param material the <code>Material</code> used to draw the shape interior or volume.
     *
     * @deprecated Use {@link #setInteriorMaterial(gov.nasa.worldwind.render.Material)} instead.
     */
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
     * @deprecated Use {@link Material#apply(com.jogamp.opengl.GL2, int)} or make OpenGL state changes directly.
     */
    @Deprecated
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
     * @deprecated Use {@link Material#apply(com.jogamp.opengl.GL2, int)} or make OpenGL state changes directly.
     */
    @Deprecated
    void applyOutline(DrawContext dc, boolean enableMaterial);
}
