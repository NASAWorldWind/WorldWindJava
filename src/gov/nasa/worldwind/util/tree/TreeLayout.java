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

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.render.*;

/**
 * Handles rendering a {@link Tree}. The layout is responsible for the overall arrangement of the tree.
 *
 * @author pabercrombie
 * @version $Id: TreeLayout.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see Tree
 */
public interface TreeLayout extends WWObject, Renderable
{
    /**
     * Render a tree.
     *
     * @param dc Draw context to draw in.
     */
    void render(DrawContext dc);

    /**
     * Set the tree attributes.
     *
     * @param attributes New attributes.
     *
     * @see #getAttributes()
     */
    void setAttributes(TreeAttributes attributes);

    /**
     * Get the tree attributes.
     *
     * @return Tree attributes.
     *
     * @see #setAttributes(TreeAttributes)
     */
    TreeAttributes getAttributes();

    /**
     * Make a node in the tree visible in the rendered tree. For example, scroll the tree viewport so that a path is
     * visible.
     *
     * @param path Path to make visible.
     */
    void makeVisible(TreePath path);
}
