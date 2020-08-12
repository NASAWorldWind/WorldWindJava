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
