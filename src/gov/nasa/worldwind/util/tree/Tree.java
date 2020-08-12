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
import gov.nasa.worldwind.render.OrderedRenderable;

/**
 * A tree of objects, drawn in the WorldWindow, that the user can interact with. How the tree is drawn is determined by
 * the {@link TreeLayout}.
 *
 * @author pabercrombie
 * @version $Id: Tree.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TreeModel
 * @see TreeLayout
 */
public interface Tree extends WWObject, OrderedRenderable
{
    /**
     * Set the tree layout. The layout determines how the tree will be rendered.
     *
     * @param layout New layout.
     *
     * @see #getLayout()
     */
    void setLayout(TreeLayout layout);

    /**
     * Get the tree layout. The layout determines how the tree will be rendered.
     *
     * @return The tree layout.
     *
     * @see #setLayout(TreeLayout)
     */
    TreeLayout getLayout();

    /**
     * Set the tree model. The model determines the contents of the tree.
     *
     * @param model New tree model.
     *
     * @see #getModel()
     */
    void setModel(TreeModel model);

    /**
     * Get the tree model. The model determines the contents of the tree.
     *
     * @return the tree model.
     *
     * @see #setModel(TreeModel)
     */
    TreeModel getModel();

    /**
     * Locate a node in the tree.
     *
     * @param path Path to the node.
     *
     * @return Node identified by {@code path} if it exists in the tree.
     */
    TreeNode getNode(TreePath path);

    /**
     * Make a node in the tree visible in the rendered tree. For example, scroll the tree viewport so that a path is
     * visible.
     *
     * @param path Path to make visible.
     */
    void makeVisible(TreePath path);

    /**
     * Expand a path in the tree. Has no effect on leaf nodes.
     *
     * @param path Path to expand.
     */
    void expandPath(TreePath path);

    /**
     * Collapse a path in the tree. Has no effect on leaf nodes.
     *
     * @param path Path to collapse.
     */
    void collapsePath(TreePath path);

    /**
     * Expand a collapsed path, or collapse an expanded path. Has no effect on leaf nodes.
     *
     * @param path Path to operate on. If the node defined by {@code path} is expanded, it will be collapsed. If it is
     *             collapsed it will be expanded.
     */
    public void togglePath(TreePath path);

    /**
     * Is a path expanded in the tree?
     *
     * @param path Path to test.
     *
     * @return true if the path is expanded, false if collapsed. Always returns false for leaf nodes.
     */
    boolean isPathExpanded(TreePath path);

    /**
     * Is a node expanded?
     *
     * @param node Node to test.
     *
     * @return true if the node is expanded, false if collapsed. Always returns false for leaf nodes.
     */
    boolean isNodeExpanded(TreeNode node);
}
