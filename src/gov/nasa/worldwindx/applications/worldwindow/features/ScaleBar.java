/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: ScaleBar.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScaleBar extends AbstractFeatureLayer
{
    public ScaleBar()
    {
        this(null);
    }

    public ScaleBar(Registry registry)
    {
        super("Scale Bar", Constants.FEATURE_SCALE_BAR, null, true, registry);
    }

    protected Layer doAddLayer()
    {
        ScalebarLayer layer = new ScalebarLayer();

        layer.setPosition(AVKey.SOUTHEAST);
        layer.setValue(Constants.SCREEN_LAYER, true);

        this.controller.addInternalLayer(layer);

        return layer;
    }
}
