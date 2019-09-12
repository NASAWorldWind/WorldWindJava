/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.applications.worldwindow.core.Constants;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.LayerPath;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.tree.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: LayerTreeModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerTreeModel extends DefaultTreeModel
{
    private boolean includeInternalLayers = false;

    public LayerTreeModel()
    {
        super(new LayerTreeGroupNode(("Root")), true);
    }

    public LayerTreeModel(LayerList layerList)
    {
        this();
        this.getRootNode().add(this.makeGroup(layerList));
    }

    public LayerTreeGroupNode getRootNode()
    {
        return (LayerTreeGroupNode) getRoot();
    }

    public LayerTreeGroupNode getDefaultGroupNode()
    {
        if (!(this.getRootNode().getFirstChild() instanceof LayerTreeGroupNode))
        {
            Util.getLogger().severe("Illegal State: The default group node is not a layer node.");
            return null;
        }
        return (LayerTreeGroupNode) this.getRootNode().getFirstChild();
    }

    public boolean isIncludeInternalLayers()
    {
        return includeInternalLayers;
    }

    public static boolean isInternalLayer(Layer layer)
    {
        return layer.getValue(Constants.INTERNAL_LAYER) != null;
    }

    public void setIncludeInternalLayers(boolean includeInternalLayers)
    {
        if (includeInternalLayers == this.includeInternalLayers)
            return;

        this.includeInternalLayers = includeInternalLayers;
        this.reload();
    }

    public void selectLayer(Layer layer, boolean tf)
    {
        // Select all instances of the layer.
        List<LayerTreeNode> layerNodes = findLayerInstances(layer, null);
        if (layerNodes == null)
            return;

        for (LayerTreeNode layerNode : layerNodes)
        {
            layerNode.setSelected(tf);
            this.nodeChanged(layerNode);
        }
    }

    public LayerTreeNode findChild(String childName, LayerTreeNode parent)
    {
        if (childName == null)
            return null;

        if (parent == null)
            parent = this.getRootNode();

        for (int i = 0; i < parent.getChildCount(); i++)
        {
            if (((LayerTreeNode) parent.getChildAt(i)).getTitle().equals(childName))
                return (LayerTreeNode) parent.getChildAt(i);
        }

        return null;
    }

    public LayerTreeNode getLastNode(LayerPath path)
    {
        if (LayerPath.isEmptyPath(path))
            return null;

        LayerTreeNode currentNode = null;

        for (String nodeName : path)
        {
            if (currentNode == null)
                currentNode = this.getRootNode();

            Enumeration iter = currentNode.children();
            currentNode = null;
            while (iter.hasMoreElements())
            {
                LayerTreeNode child = (LayerTreeNode) iter.nextElement();
                if (child.getTitle().equals(nodeName))
                {
                    currentNode = child;
                    break; // out of while
                }
            }
            if (currentNode == null)
                return null; // path does not exist
        }

        return currentNode;
    }

    protected LayerTreeGroupNode makeGroup(LayerList layerList)
    {
        LayerTreeGroupNode groupNode = new LayerTreeGroupNode(layerList.getDisplayName());

        for (Layer layer : layerList)
        {
            if (layer.getValue(Constants.INTERNAL_LAYER) != null && !this.isIncludeInternalLayers())
                continue;

            LayerTreeNode layerNode = new LayerTreeNode(layer);
            layerNode.setAllowsChildren(false);
            groupNode.add(layerNode);
        }

        return groupNode;
    }

    /**
     * Synchronize the layer tree with the state of the specified layer list. This method adds layers from the list that
     * aren't already in the tree, and removes layers from the tree if they are labeled internal. It does not remove
     * other layers in the model but missing from the specified layer list. Use {@link #removeNode(Object)} for that.
     *
     * @param layerList the layerlist to synchronize with, typically the active layer list of the WorldWindow.
     */
    public void refresh(LayerList layerList)
    {
        if (layerList == null || layerList.size() == 0)
            return;

        for (Layer layer : layerList)
        {
            // See if the layer is contained in the tree
            LayerTreeNode layerNode = findLayer(layer, null);

            // Remove any layers recently designated as internal
            if (layerNode != null && isInternalLayer(layer) && !this.isIncludeInternalLayers())
            {
                removeNodeFromParent(layerNode);
                continue;
            }

            // Don't add new internal layers
            if (isInternalLayer(layer) && !this.isIncludeInternalLayers())
                continue;

            if (layerNode == null)
            {
                // put the new layer in the base-layer group
                LayerTreeNode groupNode = (LayerTreeNode) this.getRootNode().getChildAt(0);
                if (!(groupNode instanceof LayerTreeGroupNode))
                {
                    Util.getLogger().severe("Illegal State: The root node is not a layer node.");
                    return;
                }
                layerNode = new LayerTreeNode(layer);
                layerNode.setAllowsChildren(false);
                insertNodeInto(layerNode, groupNode, groupNode.getChildCount());
            }

            // Update the tree-node state to indicate the layer's presence in the active layer list
            layerNode.setSelected(true);
        }
    }

    public void removeNode(Object o)
    {
        if (o == null)
            return;

        LayerTreeNode layerNode = null;

        if (o instanceof Layer)
            layerNode = this.findLayer((Layer) o, null);
        else if (o instanceof LayerNode)
            layerNode = this.find((LayerTreeNode) o);

        if (layerNode != null)
            removeNodeFromParent(layerNode);
    }

    public LayerTreeNode findLayer(Layer layer, LayerTreeGroupNode groupNode)
    {
        if (layer == null)
            return null;

        if (groupNode == null)
            groupNode = getRootNode();

        Enumeration treeNodes = groupNode.depthFirstEnumeration();
        while (treeNodes.hasMoreElements())
        {
            LayerTreeNode treeNode = (LayerTreeNode) treeNodes.nextElement();
            if (treeNode != null && !(treeNode instanceof LayerTreeGroupNode))
            {
                if (treeNode.getLayer() == layer)
                    return treeNode;
            }
        }

        return null;
    }

    // Find all paths to a layer.
    public List<LayerTreeNode> findLayerInstances(Layer layer, LayerTreeGroupNode groupNode)
    {
        if (layer == null)
            return null;

        if (groupNode == null)
            groupNode = getRootNode();

        List<LayerTreeNode> instances = new ArrayList<LayerTreeNode>();

        Enumeration treeNodes = groupNode.depthFirstEnumeration();
        while (treeNodes.hasMoreElements())
        {
            LayerTreeNode treeNode = (LayerTreeNode) treeNodes.nextElement();
            if (treeNode != null && !(treeNode instanceof LayerTreeGroupNode))
            {
                if (treeNode.getLayer() == layer)
                    instances.add(treeNode);
            }
        }

        return instances;
    }

    // Find a layer by name in a specified layer group.
    public LayerTreeNode findByTitle(String title, LayerTreeNode groupNode)
    {
        if (WWUtil.isEmpty(title))
            return null;

        if (groupNode == null)
            groupNode = this.getRootNode();

        Enumeration treeNodes = groupNode.depthFirstEnumeration();
        while (treeNodes.hasMoreElements())
        {
            LayerTreeNode treeNode = (LayerTreeNode) treeNodes.nextElement();
            if (treeNode != null && treeNode.getTitle().equals(title))
                return treeNode;
        }

        return null;
    }

    // Find a layer by name no matter what group it's in.
    public LayerTreeNode findByTitle(String title)
    {
        if (getRoot() == null || title == null)
            return null;

        Enumeration treeNodes = getRootNode().breadthFirstEnumeration();
        while (treeNodes.hasMoreElements())
        {
            LayerTreeNode layerNode = (LayerTreeNode) treeNodes.nextElement();
            if (layerNode != null && layerNode.getTitle() != null && layerNode.getTitle().equals(title))
                return layerNode;
        }

        return null;
    }

    // Find a layer by name in a specified layer group.
    public LayerTreeNode findByTitle(String layerTitle, String groupTitle)
    {
        if (layerTitle == null)
            return null;

        LayerTreeNode groupNode = groupTitle != null ? this.findByTitle(groupTitle) : this.getRootNode();
        if (!(groupNode instanceof LayerTreeGroupNode))
            return null;

        Enumeration treeNodes = groupNode.breadthFirstEnumeration();
        while (treeNodes.hasMoreElements())
        {
            LayerTreeNode layerNode = (LayerTreeNode) treeNodes.nextElement();
            if (layerNode != null && layerNode.getTitle() != null && layerNode.getTitle().equals(layerTitle))
                return layerNode;
        }

        return null;
    }

    // Determine whether a layer node is in the model.
    public LayerTreeNode find(LayerNode layerNodeRequested)
    {
        if (getRoot() == null || layerNodeRequested == null)
            return null;

        Enumeration treeNodes = getRootNode().preorderEnumeration();
        while (treeNodes.hasMoreElements())
        {
            LayerTreeNode layerNode = (LayerTreeNode) treeNodes.nextElement();
            if (layerNode != null && layerNode.getID().equals(layerNodeRequested.getID()))
                return layerNode;
        }

        return null;
    }
}
