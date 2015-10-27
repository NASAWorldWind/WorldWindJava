/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: LatLonGraticule.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LatLonGraticule extends GraticuleLayer
{
    public LatLonGraticule()
    {
        this(null);
    }

    public LatLonGraticule(Registry registry)
    {
        super("Lat/Lon Graticule", Constants.FEATURE_LATLON_GRATICULE, null, null, registry);
    }

    @Override
    protected Layer doCreateLayer()
    {
        return new LatLonGraticuleLayer();
    }
}
