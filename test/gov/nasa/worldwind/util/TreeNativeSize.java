/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.HotSpotController;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.util.tree.*;

/**
 * Test of tree frame that sizes to fit its content. The tree frame should change size to fit the content as the nodes
 * expand and collapse. The frame should not show scroll bars.
 *
 * @author pabercrombie
 * @version $Id: TreeNativeSize.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class TreeNativeSize extends ApplicationTemplate
{
    private static final String ICON_PATH = "images/16x16-icon-nasa.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        HotSpotController controller;

        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            BasicTree tree = new BasicTree();

            BasicTreeLayout layout = new BasicTreeLayout(tree, 100, 200);
            layout.getFrame().setFrameTitle("TreeControl");
            layout.getFrame().setSize(new Size(Size.NATIVE_DIMENSION, 0, null, Size.NATIVE_DIMENSION, 0, null));
            layout.getFrame().setEnableResizeControl(false);
            tree.setLayout(layout);

            BasicTreeModel model = new BasicTreeModel();

            BasicTreeNode root = new BasicTreeNode("Root", ICON_PATH);
            model.setRoot(root);

            BasicTreeNode child = new BasicTreeNode("Child 1", ICON_PATH);
            child.setDescription("This is a child node");
            child.addChild(new BasicTreeNode("Subchild 1,1"));
            child.addChild(new BasicTreeNode("Subchild 1,2"));
            child.addChild(new BasicTreeNode("Subchild 1,3", ICON_PATH));
            root.addChild(child);

            child = new BasicTreeNode("Child 2", ICON_PATH);
            child.addChild(new BasicTreeNode("Subchild 2,1"));
            child.addChild(new BasicTreeNode("Subchild 2,2"));
            child.addChild(new BasicTreeNode("Subchild 2,3"));
            root.addChild(child);

            child = new BasicTreeNode("Child 3");
            child.addChild(new BasicTreeNode("Subchild 3,1"));
            child.addChild(new BasicTreeNode("Subchild 3,2"));
            child.addChild(new BasicTreeNode("Subchild 3,3"));
            root.addChild(child);

            tree.setModel(model);

            tree.expandPath(root.getPath());

            controller = new HotSpotController(this.getWwd());

            layer.addRenderable(tree);

            // Add the layer to the model.
            insertBeforeCompass(this.getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Tree Control", AppFrame.class);
    }
}
