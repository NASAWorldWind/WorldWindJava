/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwindx.applications.worldwindow.core.Controller;

import java.util.List;

/**
 * @author tag
 * @version $Id: WMSLayerTree.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSLayerTree extends LayerTree
{
    public WMSLayerTree(Controller controller)
    {
        super(controller);
    }

    public void createLayers(Object infoItem, AVList commonLayerParams)
    {
        if (infoItem instanceof WMSCapabilities)
        {
            WMSCapabilities capsDoc = (WMSCapabilities) infoItem;

            String serviceTitle = capsDoc.getServiceInformation().getServiceTitle();
            if (!WWUtil.isEmpty(serviceTitle))
                this.setDisplayName(serviceTitle);

            List<WMSLayerCapabilities> layerCaps = capsDoc.getCapabilityInformation().getLayerCapabilities();
            if (layerCaps == null)
                return; // TODO: issue warning

            for (WMSLayerCapabilities caps : layerCaps)
            {
                LayerTree subTree = this.createSubTree(capsDoc, caps, commonLayerParams);
                if (subTree != null)
                    this.children.add(subTree);
            }
        }
    }

    public LayerTree createSubTree(WMSCapabilities capsDoc, WMSLayerCapabilities layerCaps, AVList commonLayerParams)
    {
        WMSLayerTree tree = new WMSLayerTree(this.controller);

        // Determine the tree's display name.
        if (!WWUtil.isEmpty(layerCaps.getTitle()))
            tree.setDisplayName(layerCaps.getTitle());
        else if (!WWUtil.isEmpty(layerCaps.getName()))
            tree.setDisplayName(layerCaps.getName());
        else
            tree.setDisplayName("No name");

        // Create an image layer if this is a named layer.
        if (layerCaps.getName() != null)
        {
            TiledImageLayer layer = tree.createImageLayer(capsDoc, layerCaps, commonLayerParams);
            if (layer == null)
                return null;

            tree.getLayers().add(layer);
        }

        // Create any sublayers.
        if (layerCaps.getLayers() != null)
        {
            for (WMSLayerCapabilities subLayerCaps : layerCaps.getLayers())
            {
                if (subLayerCaps.isLeaf())
                {
                    TiledImageLayer layer = tree.createImageLayer(capsDoc, subLayerCaps, commonLayerParams);
                    if (layer != null)
                        tree.getLayers().add(layer);
                }
                else
                {
                    LayerTree subTree = this.createSubTree(capsDoc, subLayerCaps, commonLayerParams);
                    if (subTree != null)
                        tree.children.add(subTree);
                }
            }
        }

        return tree;
    }

    protected TiledImageLayer createImageLayer(WMSCapabilities capsDoc, WMSLayerCapabilities layerCaps,
        AVList commonLayerParams)
    {
        AVList layerParams = new AVListImpl();
        if (commonLayerParams != null)
            layerParams.setValues(commonLayerParams);
        layerParams.setValue(AVKey.LAYER_NAMES, layerCaps.getName());

        return new WMSTiledImageLayer(capsDoc, layerParams);
    }
}
