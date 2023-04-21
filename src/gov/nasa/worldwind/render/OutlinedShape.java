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
