/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
