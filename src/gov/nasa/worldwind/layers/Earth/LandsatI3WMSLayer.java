/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: LandsatI3WMSLayer.java 1958 2014-04-24 19:25:37Z tgaskins $
 */
public class LandsatI3WMSLayer extends WMSTiledImageLayer
{
    public LandsatI3WMSLayer()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("config/Earth/LandsatI3WMSLayer2.xml", null);
    }
}
