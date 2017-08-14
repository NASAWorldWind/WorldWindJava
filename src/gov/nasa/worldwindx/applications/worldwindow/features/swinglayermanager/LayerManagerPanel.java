/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.*;
import gov.nasa.worldwindx.applications.worldwindow.features.AbstractFeaturePanel;
import gov.nasa.worldwindx.applications.worldwindow.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Enumeration;

/**
 * @author tag
 * @version $Id: LayerManagerPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerManagerPanel extends AbstractFeaturePanel implements LayerManager, TreeModelListener
{
    private static final String TOOL_TIP = "Select layers to add to the active layer list.";
    private static final String ICON_PATH
        = "gov/nasa/worldwindx/applications/worldwindow/images/layer-manager-64x64.png";
    private LayerTree layerTree;
    private boolean on = false;

    public LayerManagerPanel(Registry registry)
    {
        super("Layer Manager", Constants.FEATURE_LAYER_MANAGER, ICON_PATH, new ShadedPanel(new BorderLayout()),
            registry);
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        LayerList layerList = controller.getWWd().getModel().getLayers();
        layerList.setDisplayName("Base Layers");
        layerTree = new LayerTree(new LayerTreeModel(layerList));
        layerTree.setOpaque(false);
        layerTree.setBorder(new EmptyBorder(10, 10, 10, 10));
//        layerTree.setToolTipText(TOOL_TIP);

        this.layerTree.getModel().addTreeModelListener(this);

        JScrollPane scrollPane = new JScrollPane(layerTree);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel np = new JPanel(new BorderLayout(5, 5));
        np.setOpaque(false);
        np.add(scrollPane, BorderLayout.CENTER);

        PanelTitle panelTitle = new PanelTitle("Available Layers", SwingConstants.CENTER);
        panelTitle.setToolTipText(TOOL_TIP);

        this.panel.add(panelTitle, BorderLayout.NORTH);
        this.panel.add(np, BorderLayout.CENTER);

        layerList.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getSource() instanceof LayerList) // the layer list lost, gained or swapped layers
                {
                    ((LayerTreeModel) layerTree.getModel()).refresh((LayerList) event.getSource());
                    controller.redraw();
                }
                else if (event.getSource() instanceof Layer)
                {
                    // Just the state of the layer changed.
                    layerTree.repaint();
                }
            }
        });

        this.panel.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent componentEvent) // TODO: how is this used?
            {
                on = panel.isVisible() && panel.getSize().width > 0 && panel.getSize().height > 0;
                firePropertyChange("FeatureResized", null, panel.getSize());
            }
        });
    }

    protected LayerTreeModel getModel()
    {
        return (LayerTreeModel) this.layerTree.getModel();
    }

    public void redraw()
    {
        this.layerTree.repaint();
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    @Override
    public boolean isOn()
    {
        return this.on;
    }

    @Override
    public void turnOn(boolean tf)
    {
        this.firePropertyChange("ShowLayerManager", this.on, tf); // TODO: remove if no longer used
        this.on = !this.on;
    }

    public void scrollToLayer(Layer layer)
    {
        // Make the specified layer visible in the layer tree.
        LayerTreeNode layerNode = this.getModel().findLayer(layer, null);

        if (layerNode == null || layerNode == this.getModel().getRootNode())
            layerNode = this.getModel().getDefaultGroupNode();

        this.layerTree.scrollPathToVisible(new TreePath(layerNode.getPath()));
    }

    public void expandGroup(String groupName)
    {
        LayerTreeNode groupNode = this.getModel().findByTitle(groupName);
        if (groupNode != null)
        {
            this.layerTree.expandPath(new TreePath(groupNode.getPath()));
        }
    }

    public void expandPath(LayerPath path)
    {
        LayerTreeNode groupNode = (LayerTreeNode) this.getNode(path);
        if (groupNode != null)
        {
            this.layerTree.expandPath(new TreePath(groupNode.getPath()));
        }
    }

    public void enableGroupSelection(LayerPath path, boolean tf)
    {
        LayerNode groupNode = this.getModel().getLastNode(path);
        if (groupNode != null)
        {
            groupNode.setEnableSelectionBox(tf);
            this.layerTree.repaint();
        }
    }

    // Called when the user selects or deselects a cell's check box
    public void treeNodesChanged(TreeModelEvent event)
    {
        Object[] changedNodes = event.getChildren();
        if (changedNodes != null && changedNodes.length > 0)
        {
            LayerList layerList = this.controller.getWWd().getModel().getLayers();
            if (layerList == null)
                return;

            for (Object o : changedNodes)
            {
                if (o == null || !(o instanceof LayerNode))
                    continue;

                if (o instanceof LayerTreeGroupNode)
                    this.handleGroupSelection((LayerTreeGroupNode) o, layerList);
                else
                {
                    this.handleLayerSelection((LayerTreeNode) o, layerList);
                }
            }

            this.updateGroupSelections();
            this.layerTree.repaint();
            this.controller.redraw();
        }
    }

    protected void handleLayerSelection(LayerTreeNode treeNode, LayerList layerList)
    {
        // Many layers do not exist until they're selected. This eliminates the overhead of layers never used.
        if (treeNode.getLayer() == null)
            this.createLayer(treeNode);

        if (treeNode.getLayer() == null)
        {
            // unable to create the layer
            Util.getLogger().warning("Unable to create the layer named " + treeNode.getTitle());
            return;
        }

        // Update the active layers list: Add a missing layer to the list if selected, remove a layer that is
        // not selected.
        if (treeNode.isSelected() && !layerList.contains(treeNode.getLayer()))
        {
            this.performSmartInsertion(treeNode, layerList);
            treeNode.getLayer().setEnabled(true);
        }
        else if (!treeNode.isSelected() && layerList.contains(treeNode.getLayer()))
        {
            layerList.remove(treeNode.getLayer());
        }
    }

    protected void updateGroupSelections()
    {
        // Ensure that group nodes have their selection box checked if any sub-layer is selected, or does not have
        // its selection box checked if no sub-layer is active.
        Enumeration iter = ((LayerTreeModel) this.layerTree.getModel()).getRootNode().depthFirstEnumeration();
        while (iter.hasMoreElements())
        {
            LayerTreeNode node = (LayerTreeNode) iter.nextElement();
            if (!(node instanceof LayerTreeGroupNode))
                continue;

            this.updateGroupSelection(node);
        }
    }

    protected void updateGroupSelection(LayerTreeNode groupNode)
    {
        // Ensure that group nodes have their selection box checked if any child is selected, or does not have
        // its selection box checked if no child is active.
        if (groupNode == null || groupNode == ((LayerTreeModel) layerTree.getModel()).getDefaultGroupNode())
            return;

        for (int i = 0; i < groupNode.getChildCount(); i++)
        {
            if (((LayerNode) groupNode.getChildAt(i)).isSelected())
            {
                groupNode.setSelected(true);
                return;
            }
        }

        groupNode.setSelected(false);
    }

    protected void handleGroupSelection(LayerTreeNode group, LayerList layerList)
    {
        Enumeration iter = group.breadthFirstEnumeration();
        while (iter.hasMoreElements())
        {
            Object o = iter.nextElement();
            if (!(o instanceof LayerNode) || (o instanceof LayerTreeGroupNode))
                continue;

            LayerTreeNode layerNode = (LayerTreeNode) o;
            layerNode.setSelected(group.isSelected());
            this.handleLayerSelection(layerNode, layerList);
        }
    }

    // Insert a layer into the active layers list at its same position relative to its siblings in the layer tree.
    protected void performSmartInsertion(LayerTreeNode treeNode, LayerList layerList)
    {
        if (this.insertAfterPreviousSibling(treeNode, layerList))
            return;

        if (this.insertBeforeSubsequentSibling(treeNode, layerList))
            return;

        // No siblings found. Just append the layer to the layer list.
        layerList.add(treeNode.getLayer());
    }

    protected boolean insertAfterPreviousSibling(LayerTreeNode treeNode, LayerList layerList)
    {
        LayerTreeNode previousTreeNode = (LayerTreeNode) treeNode.getPreviousSibling();
        while (previousTreeNode != null)
        {
            int index = layerList.indexOf(previousTreeNode.getLayer());
            if (index >= 0)
            {
                layerList.add(index + 1, treeNode.getLayer());
                return true;
            }
            previousTreeNode = (LayerTreeNode) previousTreeNode.getPreviousSibling();
        }

        return false;
    }

    protected boolean insertBeforeSubsequentSibling(LayerTreeNode treeNode, LayerList layerList)
    {
        LayerTreeNode subsequentTreeNode = (LayerTreeNode) treeNode.getNextSibling();
        while (subsequentTreeNode != null)
        {
            int index = layerList.indexOf(subsequentTreeNode.getLayer());
            if (index >= 0)
            {
                layerList.add(index, treeNode.getLayer());
                return true;
            }
            subsequentTreeNode = (LayerTreeNode) subsequentTreeNode.getNextSibling();
        }

        return false;
    }

    public void treeNodesInserted(TreeModelEvent event)
    {
    }

    public void treeNodesRemoved(TreeModelEvent event)
    {
    }

    public void treeStructureChanged(TreeModelEvent event)
    {
    }

    public String getDefaultGroupName()
    {
        return this.getModel().getDefaultGroupNode().getTitle();
    }

    public LayerPath getDefaultGroupPath()
    {
        return new LayerPath(this.getDefaultGroupName());
    }

    // Add a new group node to the layer tree.
    public void addGroup(LayerPath pathToGroup)
    {
        this.createPath(pathToGroup);
    }

    public boolean containsPath(LayerPath pathToGroup)
    {
        return this.getNode(pathToGroup) != null;
    }

    public LayerNode getNode(LayerPath path)
    {
        return this.getModel().getLastNode(path);
    }

    // Change a layer's selection state, which determines whether it's in the active layers list.
    public void selectLayer(Layer layer, boolean tf)
    {
        this.getModel().selectLayer(layer, tf);
    }

    // Find a named layer that is a descendant of a specified layer tree group.
    public Layer findLayerByTitle(String layerTitle, String groupTitle)
    {
        LayerNode treeNode = this.getModel().findByTitle(layerTitle, groupTitle);
        return treeNode != null ? treeNode.getLayer() : null;
    }

    // Add a layer to a specified group in the layer tree.
    public void addLayer(Layer layer, LayerPath pathToParent)
    {
        if (layer == null)
        {
            String msg = "Layer is null";
            Util.getLogger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LayerNode layerNode = new LayerTreeNode(layer);
        this.addLayer(layerNode, pathToParent);
    }

    // Add a layer to a specified group in the layer tree.
    public void addLayer(LayerNode layerNode, LayerPath pathToParent)
    {
        if (layerNode == null || layerNode.getLayer() == null)
        {
            String msg = "LayerNode or Layer is null";
            Util.getLogger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LayerTreeNode parentNode = LayerPath.isEmptyPath(pathToParent)
            ? this.getModel().getRootNode() : this.getModel().getLastNode(pathToParent);

        // Remove from the group any existing layer having the same name.
        LayerTreeNode existingNode = this.getModel().findByTitle(layerNode.getLayer().getName(), parentNode);
        if (existingNode != null)
            this.removeLayer(existingNode.getLayer());

        // Create the path elements leading to the layer's parent node.
        LayerTreeNode parent = this.createPath(pathToParent);

        layerNode.setAllowsChildren(false); // marks the layer node as a leaf

        // Append the layer to the parent's list of children.
        this.getModel().insertNodeInto((LayerTreeNode) layerNode, parent, parent.getChildCount());
    }

    // Ensure that each group in a specified path exists.
    protected LayerTreeNode createPath(LayerPath path)
    {
        LayerTreeNode parent = this.getModel().getRootNode();

        if (LayerPath.isEmptyPath(path))
            return parent;

        for (String nodeName : path)
        {
            LayerTreeNode groupNode = this.getModel().findChild(nodeName, parent);
            if (groupNode == null) // path element does not exist, so create it
            {
                groupNode = new LayerTreeGroupNode(nodeName);
                this.getModel().insertNodeInto(groupNode, parent, parent.getChildCount());
                parent = groupNode;
            }
            else if (groupNode instanceof LayerTreeGroupNode)
            {
                parent = groupNode; // path element exists, skip to next one.
            }
            else
            {
                throw new IllegalArgumentException("Path element is not a group");
            }
        }

        return parent;
    }

    // Remove all instances of a layer from the layer tree.
    public void removeLayer(Layer layer)
    {
        java.util.List<LayerTreeNode> instances = this.getModel().findLayerInstances(layer, null);
        if (instances == null)
            return;

        for (LayerNode layerNode : instances)
        {
            if (layerNode != null)
                this.removeLayer(layerNode);
        }
    }

    // Remove a node, not necessarily a node with a layer, from the layer tree. Remove any ancestors who would then
    // resolve only to the removed node.
    public void removeLayer(LayerNode layerNode)
    {
        if (layerNode == null)
            return;

        TreeNode[] pathFromRoot = ((LayerTreeNode) layerNode).getPath();
        this.getModel().removeNodeFromParent((LayerTreeNode) layerNode);

        // Remove any ancestors whose descendants have no leaf node but the one removed.
        for (int i = pathFromRoot.length - 2; i >= 1; i--) // length - 2 ==> don't remove root node
        {
            LayerTreeNode groupNode = (LayerTreeNode) pathFromRoot[i];

            // Don't delete the default group node (or its ancestors).
            if (groupNode == this.getModel().getDefaultGroupNode())
                break;

            if (groupNode.getChildCount() == 0)
                this.getModel().removeNodeFromParent(groupNode);
        }

        // Remove layer from the WWJ layer list, which causes it to be removed from the active-layers panel.
        if (layerNode.getLayer() != null)
        {
            LayerList layerList = this.controller.getWWd().getModel().getLayers();
            if (layerList != null)
                layerList.remove(layerNode.getLayer());

            layerNode.setLayer(null);
        }
    }

    // Remove all layers in a specified list of layers in the branch identified by the layer list's display name.
    public void removeLayers(LayerList layerList)
    {
        if (layerList == null)
            return;

        LayerTreeGroupNode branchRoot = this.getModel().getRootNode();
        if (WWUtil.isEmpty(layerList.getDisplayName()))
        {
            LayerNode groupNode = this.getModel().findChild(layerList.getDisplayName(), this.getModel().getRootNode());
            if (groupNode != null && groupNode instanceof LayerTreeGroupNode)
                branchRoot = (LayerTreeGroupNode) groupNode;
        }

        for (Layer layer : layerList)
        {
            LayerNode layerNode = this.getModel().findLayer(layer, branchRoot);
            if (layerNode != null)
                this.removeLayer(layerNode);
        }
    }

    // Remove one instance of a layer from the layer tree. Will also work for group nodes.
    public void removeLayer(LayerPath path)
    {
        LayerNode layerNode = this.getModel().getLastNode(path);
        if (layerNode != null)// && layerNode.getLayer() != null)
            this.removeLayer(layerNode);
    }

    // Get the layer at the end of a specific path.
    public Layer getLayerFromPath(LayerPath path)
    {
        LayerNode layerNode = this.getModel().getLastNode(path);
        return layerNode != null && layerNode.getLayer() != null ? layerNode.getLayer() : null;
    }

    protected void createLayer(LayerNode layerNode)
    {
        if (layerNode == null)
        {
            String msg = "LayerNode is null";
            Util.getLogger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (layerNode.getWmsLayerInfo() != null)
        {
            WMSLayerInfo wmsInfo = layerNode.getWmsLayerInfo();
            AVList configParams = wmsInfo.getParams().copy(); // Copy to insulate changes from the caller.

            // Some wms servers are slow, so increase the timeouts and limits used by WorldWind's retrievers.
            configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
            configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
            configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

            Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
            Layer layer = (Layer) factory.createFromConfigSource(wmsInfo.getCaps(), configParams);
            layerNode.setLayer(layer);
        }
    }
}
