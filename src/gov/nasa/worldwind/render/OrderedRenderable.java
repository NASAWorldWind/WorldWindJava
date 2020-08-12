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
 * @author tag
 * @version $Id: OrderedRenderable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface OrderedRenderable extends Renderable
{
    /**
     * Returns the ordered renderable's distance from the current view's eye point. Intended to be used only to sort a
     * list of ordered renderables according to eye distance, and only during frame generation when a view is active.
     *
     * @return the distance of the ordered renderable from the current view's eye point.
     */
    double getDistanceFromEye();

    /**
     * Executes a pick of the ordered renderable.
     *
     * @param dc        the current draw context.
     * @param pickPoint the pick point.
     */
    public void pick(DrawContext dc, java.awt.Point pickPoint);
}
