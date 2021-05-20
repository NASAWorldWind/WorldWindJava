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

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.tree.*;

/**
 * This example demonstrates the use of the on-screen tree control using {@link gov.nasa.worldwind.util.tree.BasicTree}.
 *
 * @author pabercrombie
 * @version $Id: TreeControl.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class TreeControl extends ApplicationTemplate
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
