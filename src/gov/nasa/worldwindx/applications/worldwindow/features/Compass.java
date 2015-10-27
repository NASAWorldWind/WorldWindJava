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
 * @version $Id: Compass.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Compass extends AbstractFeatureLayer
{
    public Compass()
    {
        this(null);
    }

    public Compass(Registry registry)
    {
        super("Compass", Constants.FEATURE_COMPASS, null, true, registry);
    }

    protected Layer doAddLayer()
    {
        CompassLayer layer = new CompassLayer();
        layer.setIconScale(0.3);

        layer.setPosition(AVKey.NORTHWEST);
        layer.setValue(Constants.SCREEN_LAYER, true);

        this.controller.addInternalLayer(layer);

        return layer;
    }
}
