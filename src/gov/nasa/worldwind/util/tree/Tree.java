/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
