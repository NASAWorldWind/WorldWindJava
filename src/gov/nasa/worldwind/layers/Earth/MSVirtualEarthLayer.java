/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.WWXML;
import org.w3c.dom.Document;

/**
 * @author Patrick Murris
 * @version $Id: MSVirtualEarthLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MSVirtualEarthLayer extends BasicTiledImageLayer
{
    public static final String LAYER_AERIAL = "gov.nasa.worldwind.layers.Earth.MSVirtualEarthLayer.Aerial";
    public static final String LAYER_ROADS = "gov.nasa.worldwind.layers.Earth.MSVirtualEarthLayer.Roads";
    public static final String LAYER_HYBRID = "gov.nasa.worldwind.layers.Earth.MSVirtualEarthLayer.Hybrid";

    public MSVirtualEarthLayer(String layerName)
    {
        super(getConfigurationDocument(layerName), null);
    }

    public MSVirtualEarthLayer()
    {
       this(LAYER_AERIAL);
    }

    protected static Document getConfigurationDocument(String layerName)
    {
        String filePath;

        if (layerName != null && layerName.equals(LAYER_HYBRID))
        {
            filePath = "config/Earth/MSVirtualEarthHybridLayer.xml";
        }
        else if (layerName != null && layerName.equals(LAYER_ROADS))
        {
            filePath = "config/Earth/MSVirtualEarthRoadsLayer.xml";
        }
        else
        {
            // Default to MS Virtual Earth Aerial.
            filePath = "config/Earth/MSVirtualEarthAerialLayer.xml";
        }

        return WWXML.openDocumentFile(filePath, null);
    }
}
