/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.util.*;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.*;

/**
 * Default implementation of a {@link TreeNode}.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeNode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicTreeNode extends WWObjectImpl implements TreeNode
{
    protected String text;
    protected Object imageSource;
    protected BasicWWTexture texture;

    protected String description;

    protected TreeNode parent;
    protected List<TreeNode> children; // List is created when children are added

    protected boolean enabled = true;
    protected boolean selected;
    protected boolean visible = true;

    /**
     * Flag to indicate that any part of the sub-tree rooted at this node is selected. This value is computed on demand
     * and cached.
     */
    protected String treeSelected;

    /**
     * Create a node with text.
     *
     * @param text Node text.
     */
    public BasicTreeNode(String text)
    {
        this(text, null);
    }

    /**
     * Create a node with text and an icon.
     *
     * @param text        Node text.
     * @param imageSource Image source for the node icon. May be a String, URL, or BufferedImage.
     */
    public BasicTreeNode(String text, Object imageSource)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.text = text.trim();
        this.setImageSource(imageSource);
    }

    /** {@inheritDoc} */
    public String getText()
    {
        return this.text;
    }

    /** {@inheritDoc} */
    public TreeNode getParent()
    {
        return this.parent;
    }

    /** {@inheritDoc} */
    public void setParent(TreeNode node)
    {
        this.parent = node;
    }

    /** {@inheritDoc} */
    public Iterable<TreeNode> getChildren()
    {
        if (this.children != null)
            return Collections.unmodifiableList(this.children);
        else
            return Collections.emptyList();
    }

    /** {@inheritDoc} */
    public boolean isEnabled()
    {
        return this.enabled;
    }

    /** {@inheritDoc} */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /** {@inheritDoc} */
    public boolean isSelected()
    {
        return this.selected;
    }

    /** {@inheritDoc} */
    public void setSelected(boolean selected)
    {
        boolean prevSelected = this.isSelected();
        this.selected = selected;
        this.treeSelected = null; // Need to recompute tree selected field

        if (prevSelected != selected)
            this.firePropertyChange(AVKey.TREE_NODE, null, this);
    }

    /** {@inheritDoc} */
    public String isTreeSelected()
    {
        if (this.treeSelected == null)
            this.treeSelected = this.computeTreeSelected();

        return this.treeSelected;
    }

    /**
     * Determine if any part of the sub-tree rooted at this node is selected.
     *
     * @return {@link #SELECTED}, {@link #NOT_SELECTED}, {@link #PARTIALLY_SELECTED}.
     */
    protected String computeTreeSelected()
    {
        String selected = this.isSelected() ? SELECTED : NOT_SELECTED;

        for (TreeNode child : this.getChildren())
        {
            String childSelected = child.isTreeSelected();

            if (!selected.equals(childSelected))
            {
                selected = PARTIALLY_SELECTED;
                break; // No need to look at other nodes
            }
        }

        return selected;
    }

    /** {@inheritDoc} */
    public boolean isVisible()
    {
        return this.visible;
    }

    /** {@inheritDoc} */
    public boolean isLeaf()
    {
        return WWUtil.isEmpty(this.children);
    }

    /** {@inheritDoc} */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description != null ? description.trim() : null;
    }

    /** {@inheritDoc} */
    public Object getImageSource()
    {
        return imageSource;
    }

    /** {@inheritDoc} */
    public void setImageSource(Object imageSource)
    {
        this.imageSource = imageSource;
        this.texture = null;
    }

    /** {@inheritDoc} */
    public boolean hasImage()
    {
        return this.getImageSource() != null;
    }

    /** {@inheritDoc} */
    public BasicWWTexture getTexture()
    {
        if (this.texture == null)
            this.initializeTexture();

        return this.texture;
    }

    /**
     * Create and initialize the texture from the image source. If the image is not in memory this method will request
     * that it be loaded.
     */
    protected void initializeTexture()
    {
        Object imageSource = this.getImageSource();
        if (imageSource instanceof String || imageSource instanceof URL)
        {
            URL imageURL = WorldWind.getDataFileStore().requestFile(imageSource.toString());
            if (imageURL != null)
            {
                this.texture = new BasicWWTexture(imageURL, true);
            }
        }
        else if (imageSource != null)
        {
            this.texture = new BasicWWTexture(imageSource, true);
        }
    }

    /** {@inheritDoc} */
    public void addChild(TreeNode child)
    {
        if (this.children == null)
            this.children = new ArrayList<TreeNode>();
        this.addChild(this.children.size(), child);
    }

    /** {@inheritDoc} */
    public void addChild(int index, TreeNode child)
    {
        if (this.children == null)
            this.children = new ArrayList<TreeNode>();
        this.children.add(index, child);

        this.treeSelected = null;  // Need to recompute tree selected field
        child.setParent(this);
        child.addPropertyChangeListener(this);
        this.firePropertyChange(AVKey.TREE_NODE, null, this);
    }

    /** {@inheritDoc} */
    public void removeChild(TreeNode child)
    {
        if (this.children != null)
            this.children.remove(child);

        if (child != null && child.getParent() == this)
        {
            this.treeSelected = null;  // Need to recompute tree selected field
            child.setParent(null);
            child.removePropertyChangeListener(this);
            this.firePropertyChange(AVKey.TREE_NODE, null, this);
        }
    }

    /** {@inheritDoc} */
    public void removeAllChildren()
    {
        if (this.children == null)
            return;

        Iterator<TreeNode> iterator = this.children.iterator();
        if (!iterator.hasNext())
            return;

        while (iterator.hasNext())
        {
            TreeNode child = iterator.next();
            iterator.remove();

            child.setParent(null);
            child.removePropertyChangeListener(this);
        }

        this.treeSelected = null;  // Need to recompute tree selected field
        this.firePropertyChange(AVKey.TREE_NODE, null, this);
    }

    /** {@inheritDoc} */
    public TreePath getPath()
    {
        TreePath path = new TreePath();

        TreeNode node = this;
        while (node != null)
        {
            path.add(0, node.getText());
            node = node.getParent();
        }

        return path;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        this.treeSelected = null;  // Need to recompute tree selected field
        super.propertyChange(propertyChangeEvent);
    }
}
