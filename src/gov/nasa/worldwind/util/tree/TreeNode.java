/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.render.WWTexture;

/**
 * Describes a node in a {@link Tree}. A node must have a string of text. It may also have an icon.
 *
 * @author pabercrombie
 * @version $Id: TreeNode.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TreeModel
 */
public interface TreeNode extends WWObject
{
    /** All nodes in a subtree are selected. */
    final static String SELECTED = "util.tree.Selected";

    /** No nodes in a subtree are selected. */
    final static String NOT_SELECTED = "util.tree.NotSelected";

    /** Some nodes in a subtree are selected, and some are not. */
    final static String PARTIALLY_SELECTED = "util.tree.PartiallySelected";

    /**
     * Get the text of this node.
     *
     * @return Node text.
     */
    String getText();

    /**
     * Get extra text associated with this node.
     *
     * @return Description of node.
     */
    String getDescription();

    /**
     * Set the node description. The description can hold any extra text associated with the node.
     *
     * @param description New description.
     */
    void setDescription(String description);

    /**
     * Get the node's parent. Each node in the tree has a parent, except for the root node. The parent of the root node
     * is {@code null}.
     *
     * @return Parent node, or {@code null} if this the root node.
     *
     * @see #setParent(TreeNode)
     */
    TreeNode getParent();

    /**
     * Set the parent node.
     *
     * @param node New parent node.
     *
     * @see #getParent()
     */
    void setParent(TreeNode node);

    /**
     * Get the children of this node.
     *
     * @return Child nodes, or an empty iterator if the node does not have children.
     *
     * @see #addChild(TreeNode)
     */
    Iterable<TreeNode> getChildren();

    /**
     * Is the node enabled?
     *
     * @return True if the node is enabled.
     *
     * @see #setEnabled(boolean)
     */
    boolean isEnabled();

    /**
     * Set the node to enabled or not enabled. A node that is not enabled will not respond to user input.
     *
     * @param enabled New enabled state.
     *
     * @see #isEnabled()
     */
    void setEnabled(boolean enabled);

    /**
     * Is the node selected?
     *
     * @return True if the node is selected.
     *
     * @see #setSelected(boolean)
     * @see #isTreeSelected()
     */
    boolean isSelected();

    /**
     * Set the node to selected or not selected.
     *
     * @param selected New selection value.
     *
     * @see #isSelected()
     */
    void setSelected(boolean selected);

    /**
     * Is any part of the sub-tree rooted at this node selected?
     *
     * @return {@link #SELECTED}, {@link #NOT_SELECTED}, {@link #PARTIALLY_SELECTED}.
     */
    String isTreeSelected();

    /**
     * Is the node visible?
     *
     * @return True if the node is visible.
     *
     * @see #setVisible(boolean)
     */
    boolean isVisible();

    /**
     * Set the node to visible or not visible. If the node is not visible it will not be drawn by the tree layout.
     *
     * @param visible New visibility setting.
     *
     * @see #isVisible()
     */
    void setVisible(boolean visible);

    /**
     * Is the node a leaf node.
     *
     * @return True if this is a leaf node.
     */
    boolean isLeaf();

    /**
     * Add a child node.
     *
     * @param child New child.
     */
    void addChild(TreeNode child);

    /**
     * Add a child node at a specified position in the list of children.
     *
     * @param index Index at which the new child will be inserted.
     * @param child New child.
     *
     * @throws IndexOutOfBoundsException if {@code index} is less than zero or greater than the number of children
     *                                   already in the list.
     */
    void addChild(int index, TreeNode child) throws IndexOutOfBoundsException;

    /**
     * Remove a child node.
     *
     * @param child Child to remove.
     */
    void removeChild(TreeNode child);

    /** Remove all of the child nodes from this node. */
    void removeAllChildren();

    /**
     * Get the path from the root node to this node.
     *
     * @return Tree path to this node.
     */
    TreePath getPath();

    /**
     * Get the source of the node icon.
     *
     * @return Image source of the icon. {@code null} if the node does not have an icon.
     */
    Object getImageSource();

    /**
     * Set the node's icon.
     *
     * @param imageSource New icon source. May be a String, URL, or BufferedImage.
     */
    void setImageSource(Object imageSource);

    /**
     * Does this node have an icon? This method returns true if an image source has been set. The method returns true
     * even if the image has not been fully loaded.
     *
     * @return True if the node has an image.
     */
    boolean hasImage();

    /**
     * Get the texture loaded for the node's icon.
     *
     * @return Icon texture, or null if the texture has not been loaded yet.
     */
    WWTexture getTexture();
}
