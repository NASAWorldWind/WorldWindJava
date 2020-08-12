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
