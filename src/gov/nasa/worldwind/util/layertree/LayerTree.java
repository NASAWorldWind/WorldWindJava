/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.layertree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.tree.*;

/**
 * A <code>Renderable</code> tree of <code>{@link gov.nasa.worldwind.layers.Layer}</code> objects and their content. By
 * default, a <code>LayerTree</code> is created with a <code>{@link LayerTreeModel}</code>, and a <code>{@link
 * gov.nasa.worldwind.util.tree.BasicTreeLayout}</code> that is configured for displaying a layer tree. Callers can
 * specify the model to use either by specifying one during construction, or by calling <code>{@link
 * LayerTree#setModel(gov.nasa.worldwind.util.tree.TreeModel)}</code>. Once created, callers add layers to the tree
 * using methods on <code>LayerTreeModel</code>.
 *
 * @author dcollins
 * @version $Id: LayerTree.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see LayerTreeModel
 * @see LayerTreeNode
 */
public class LayerTree extends BasicTree
{
    /** The default screen location: 20x140 pixels from the upper left screen corner. */
    protected static final Offset DEFAULT_OFFSET = new Offset(20d, 140d, AVKey.PIXELS, AVKey.INSET_PIXELS);
    /** The default frame image. Appears to the left of the frame title. */
    protected static final String DEFAULT_FRAME_IMAGE = "images/layer-manager-64x64.png";
    /** The default frame title: "Layers". */
    protected static final String DEFAULT_FRAME_TITLE = "Layers";

    /**
     * Creates a new <code>LayerTree</code> with an empty <code>LayerTreeModel</code> and the default screen location.
     * The tree's upper left corner is placed 20x140 pixels from the upper left screen corner.
     */
    public LayerTree()
    {
        this.initialize(null, null);
    }

    /**
     * Creates a new <code>LayerTree</code> with the specified <code>model</code> and the default screen location. The
     * tree's upper left corner is placed 20x140 pixels from the upper left screen corner.
     *
     * @param model the tree model to use.
     *
     * @throws IllegalArgumentException if <code>model</code> is <code>null</code>.
     */
    public LayerTree(LayerTreeModel model)
    {
        if (model == null)
        {
            String message = Logging.getMessage("nullValue.ModelIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.initialize(model, null);
    }

    /**
     * Creates a new <code>LayerTree</code> with an empty <code>LayerTreeModel</code> and the specified screen
     * location.
     *
     * @param offset the screen location of the tree's upper left corner, relative to the screen's upper left corner.
     *
     * @throws IllegalArgumentException if <code>offset</code> is <code>null</code>.
     */
    public LayerTree(Offset offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.initialize(null, offset);
    }

    /**
     * Creates a new <code>LayerTree</code> with the specified <code>model</code> and the specified screen location.
     *
     * @param model  the tree model to use.
     * @param offset the screen location of the tree's upper left corner, relative to the screen's upper left corner.
     *
     * @throws IllegalArgumentException if <code>model</code> is <code>null</code>, or if <code>offset</code> is
     *                                  <code>null</code>.
     */
    public LayerTree(LayerTreeModel model, Offset offset)
    {
        if (model == null)
        {
            String message = Logging.getMessage("nullValue.ModelIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.initialize(model, offset);
    }

    /**
     * Initializes this tree with the specified <code>model</code> and <code>offset</code>. This configures the tree's
     * model, its layout, and expands the path to the root node. If either parameter is <code>null</code> this uses a
     * suitable default.
     *
     * @param model  this tree's model to use, or <code>null</code> to create a new <code>LayerTreeModel</code>.
     * @param offset the screen location of this tree's upper left corner, or <code>null</code> to use the default.
     */
    protected void initialize(LayerTreeModel model, Offset offset)
    {
        if (model == null)
            model = this.createTreeModel();

        this.setModel(model);
        this.setLayout(this.createTreeLayout(offset));
        this.expandPath(this.getModel().getRoot().getPath());
    }

    /**
     * Returns a new <code>LayerTreeModel</code>. Called from <code>initialize</code> when no model is specified.
     *
     * @return a new <code>LayerTreeModel</code>.
     */
    protected LayerTreeModel createTreeModel()
    {
        return new LayerTreeModel();
    }

    /**
     * Returns a new <code>TreeLayout</code> suitable for displaying the layer tree on a <code>WorldWindow</code>. If
     * the <code>offset</code> is <code>null</code> this the default value.
     *
     * @param offset the screen location of this tree's upper left corner, or <code>null</code> to use the default.
     *
     * @return new <code>TreeLayout</code>.
     */
    protected TreeLayout createTreeLayout(Offset offset)
    {
        if (offset == null)
            offset = DEFAULT_OFFSET;

        BasicTreeLayout layout = new BasicTreeLayout(this, offset);
        layout.getFrame().setFrameTitle(DEFAULT_FRAME_TITLE);
        layout.getFrame().setIconImageSource(DEFAULT_FRAME_IMAGE);

        BasicTreeAttributes attributes = new BasicTreeAttributes();
        attributes.setRootVisible(false);
        layout.setAttributes(attributes);

        BasicFrameAttributes frameAttributes = new BasicFrameAttributes();
        frameAttributes.setBackgroundOpacity(0.7);
        layout.getFrame().setAttributes(frameAttributes);

        BasicTreeAttributes highlightAttributes = new BasicTreeAttributes(attributes);
        layout.setHighlightAttributes(highlightAttributes);

        BasicFrameAttributes highlightFrameAttributes = new BasicFrameAttributes(frameAttributes);
        highlightFrameAttributes.setForegroundOpacity(1.0);
        highlightFrameAttributes.setBackgroundOpacity(1.0);
        layout.getFrame().setHighlightAttributes(highlightFrameAttributes);

        return layout;
    }

    /** {@inheritDoc} */
    public LayerTreeModel getModel()
    {
        return (LayerTreeModel) super.getModel();
    }
}
