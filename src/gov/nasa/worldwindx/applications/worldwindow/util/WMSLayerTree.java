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
