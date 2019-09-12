/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

import javax.swing.tree.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tag
 * @version $Id: LayerTreeNode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerTreeNode extends DefaultMutableTreeNode implements LayerNode
{
    public static final String NODE_ID = "LayerManager.LayerNode.NodeID"; // identifies the node ID property

    private static AtomicLong nextID = new AtomicLong(1L); // provides unique node IDs

    private long id;
    private Layer layer;
    private String title;
    private boolean selected = false;
    private WMSLayerInfo wmsLayerInfo;
    private String toolTipText;
    private boolean enableSelectionBox = true;

    static long getNewID()
    {
        return nextID.getAndIncrement();
    }

    public LayerTreeNode()
    {
        this.id = getNewID();
        this.title = Long.toString(this.id);
    }

    public LayerTreeNode(String title)
    {
        this.id = getNewID();
        this.title = title;
    }

    public LayerTreeNode(Layer layer)
    {
        this.id = getNewID();
        this.layer = layer;
        this.selected = layer.isEnabled();
        this.layer.setValue(NODE_ID, this.id);
    }

    public LayerTreeNode(WMSLayerInfo layerInfo)
    {
        this(layerInfo.getTitle());
        this.wmsLayerInfo = layerInfo;
    }

    public LayerTreeNode(LayerTreeNode that)
    {
        this.id = (Long) that.getID();
        this.layer = that.layer;
        this.title = that.title;
        this.selected = that.selected;
        this.wmsLayerInfo = that.wmsLayerInfo;
    }

    public Object getID()
    {
        return this.id;
    }

    public String getTitle()
    {
        return this.title != null ? this.title : this.layer != null ? this.layer.getName() : Long.toString(this.id);
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Layer getLayer()
    {
        return layer;
    }

    public void setLayer(Layer layer)
    {
        this.layer = layer;
    }

    public boolean isSelected()
    {
        return this.selected;
    }

    public void setSelected(boolean selected)
    {
//        boolean old = this.selected;
        this.selected = selected;
//
//        this.layer.firePropertyChange(Constants.SELECTED, old, this.selected); // Not enabled yet. Must test first.
    }

    public WMSLayerInfo getWmsLayerInfo()
    {
        return wmsLayerInfo;
    }

    public String getToolTipText()
    {
        return toolTipText;
    }

    public void setToolTipText(String toolTipText)
    {
        this.toolTipText = toolTipText;
    }

    public void setEnableSelectionBox(boolean tf)
    {
        this.enableSelectionBox = tf;
    }

    public boolean isEnableSelectionBox()
    {
        return this.enableSelectionBox;
    }

    @Override
    public String toString()
    {
        return this.getTitle() != null ? this.getTitle() : Long.toString(this.id);
    }
}
