/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.util.*;

/**
 * Basic implementation of a {@link Tree} control.
 *
 * @author pabercrombie
 * @version $Id: BasicTree.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicTree extends WWObjectImpl implements Tree, PreRenderable
{
    protected TreeLayout layout;

    protected TreeModel model;

    protected Set<TreePath> expandedNodes = new HashSet<TreePath>();

    /** Create an empty tree. */
    public BasicTree()
    {
    }

    /** {@inheritDoc} */
    public void makeVisible(TreePath path)
    {
        TreeLayout layout = this.getLayout();
        if (layout != null)
            layout.makeVisible(path);
    }

    /** {@inheritDoc} */
    public void expandPath(TreePath path)
    {
        this.expandedNodes.add(path);
        this.firePropertyChange(AVKey.TREE, null, this);
    }

    /** {@inheritDoc} */
    public void collapsePath(TreePath path)
    {
        this.expandedNodes.remove(path);
        this.firePropertyChange(AVKey.TREE, null, this);
    }

    /** {@inheritDoc} */
    public TreeNode getNode(TreePath path)
    {
        TreeNode node = this.getModel().getRoot();
        if (!node.getText().equals(path.get(0))) // Test root node
            return null;

        Iterator<String> iterator = path.iterator();
        iterator.next(); // Skip root node, we already tested it above
        while (iterator.hasNext())
        {
            String nodeText = iterator.next();
            boolean foundMatch = false;
            for (TreeNode child : node.getChildren())
            {
                if (child.getText().equals(nodeText))
                {
                    node = child;
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch)
                return null;
        }
        return node;
    }

    /** {@inheritDoc} */
    public void togglePath(TreePath path)
    {
        if (this.isPathExpanded(path))
            this.collapsePath(path);
        else
            this.expandPath(path);
    }

    /** {@inheritDoc} */
    public boolean isPathExpanded(TreePath path)
    {
        return this.expandedNodes.contains(path);
    }

    /** {@inheritDoc} */
    public boolean isNodeExpanded(TreeNode node)
    {
        return this.expandedNodes.contains(node.getPath());
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        TreeLayout layout = this.getLayout();
        if (layout instanceof PreRenderable)
        {
            ((PreRenderable) layout).preRender(dc);
        }
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        TreeLayout layout = this.getLayout();
        if (layout != null)
        {
            if (!dc.isOrderedRenderingMode())
                dc.addOrderedRenderable(this);
            else
                layout.render(dc);
        }
    }

    /** {@inheritDoc} */
    public void pick(DrawContext dc, Point pickPoint)
    {
        TreeLayout layout = this.getLayout();
        if (layout != null)
            layout.render(dc);
    }

    /** {@inheritDoc} */
    public double getDistanceFromEye()
    {
        return 1;
    }

    /** {@inheritDoc} */
    public TreeLayout getLayout()
    {
        return layout;
    }

    /** {@inheritDoc} */
    public void setLayout(TreeLayout layout)
    {
        if (this.layout != null)
            this.layout.removePropertyChangeListener(this);

        this.layout = layout;

        if (this.layout != null)
            this.layout.addPropertyChangeListener(this);
    }

    /** {@inheritDoc} */
    public TreeModel getModel()
    {
        return model;
    }

    /** {@inheritDoc} */
    public void setModel(TreeModel model)
    {
        if (this.model != null)
            this.model.removePropertyChangeListener(this);

        this.model = model;

        if (this.model != null)
            this.model.addPropertyChangeListener(this);
    }
}
