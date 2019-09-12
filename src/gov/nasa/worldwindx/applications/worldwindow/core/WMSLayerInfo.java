/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.ogc.OGCCapabilities;
import gov.nasa.worldwind.ogc.wms.*;

import java.util.*;

/**
 * @author tag
 * @version $Id: WMSLayerInfo.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSLayerInfo
{
    private OGCCapabilities caps;
    private AVListImpl params = new AVListImpl();

    public WMSLayerInfo(OGCCapabilities caps, WMSLayerCapabilities layerCaps, WMSLayerStyle style)
    {
        this.caps = caps;
        this.params = new AVListImpl();
        this.params.setValue(AVKey.LAYER_NAMES, layerCaps.getName());
        if (style != null)
            this.params.setValue(AVKey.STYLE_NAMES, style.getName());

        String layerTitle = layerCaps.getTitle();
        this.params.setValue(AVKey.DISPLAY_NAME, layerTitle);
    }

    public String getTitle()
    {
        return params.getStringValue(AVKey.DISPLAY_NAME);
    }

    public OGCCapabilities getCaps()
    {
        return caps;
    }

    public AVListImpl getParams()
    {
        return params;
    }

    public static java.util.List<WMSLayerInfo> createLayerInfos(OGCCapabilities caps, WMSLayerCapabilities layerCaps)
    {
        // Create the layer info specified by the layer's capabilities entry and the selected style.
        ArrayList<WMSLayerInfo> layerInfos = new ArrayList<WMSLayerInfo>();

        // An individual layer may have independent styles, and each layer/style combination is effectively one
        // visual layer. So here the individual layer/style combinations are formed.
        Set<WMSLayerStyle> styles = layerCaps.getStyles();
        if (styles == null || styles.size() == 0)
        {
            layerInfos.add(new WMSLayerInfo(caps, layerCaps, null));
        }
        else
        {
            for (WMSLayerStyle style : styles)
            {
                WMSLayerInfo layerInfo = new WMSLayerInfo(caps, layerCaps, style);
                layerInfos.add(layerInfo);
            }
        }

        return layerInfos;
    }
}
