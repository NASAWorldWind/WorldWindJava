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
