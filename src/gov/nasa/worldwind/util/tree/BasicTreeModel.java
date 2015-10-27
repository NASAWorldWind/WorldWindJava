/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.WWObjectImpl;

/**
 * Basic implementation of a {@link TreeModel}.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicTreeModel extends WWObjectImpl implements TreeModel
{
    /** The root node. */
    protected TreeNode root;

    /** Create a new tree model. */
    public BasicTreeModel()
    {
    }

    /**
     * Create a tree model with a root node.
     *
     * @param root The root node.
     */
    public BasicTreeModel(TreeNode root)
    {
        this.setRoot(root);
    }

    /** {@inheritDoc} */
    public TreeNode getRoot()
    {
        return this.root;
    }

    /**
     * Set the root node.
     *
     * @param root New root.
     */
    public void setRoot(TreeNode root)
    {
        if (this.root != null)
            this.root.removePropertyChangeListener(this);

        this.root = root;

        if (this.root != null)
            this.root.addPropertyChangeListener(this);
    }
}
