/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.applications.worldwindow.core.WMSLayerInfo;

/**
 * @author tag
 * @version $Id: LayerNode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface LayerNode
{
    Object getID();

    String getTitle();

    void setTitle(String title);

    Layer getLayer();

    void setLayer(Layer layer);

    boolean isSelected();

    void setSelected(boolean selected);

    WMSLayerInfo getWmsLayerInfo();

    String getToolTipText();

    void setToolTipText(String toolTipText);

    void setEnableSelectionBox(boolean tf);

    boolean isEnableSelectionBox();

    void setAllowsChildren(boolean tf);
}
