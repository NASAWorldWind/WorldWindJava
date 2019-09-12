/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.layers.LatLonGraticuleLayer;

/**
 * Displays the globe with a latitude and longitude graticule (latitude and longitude grid). The graticule is its own
 * layer and can be turned on and off independent of other layers. As the view zooms in, the graticule adjusts to
 * display a finer grid.
 *
 * @author Patrick Murris
 * @version $Id: Graticule.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Graticule extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add the graticule layer
            insertBeforePlacenames(getWwd(), new LatLonGraticuleLayer());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Lat-Lon Graticule", AppFrame.class);
    }
}