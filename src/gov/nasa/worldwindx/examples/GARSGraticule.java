/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.layers.GARSGraticuleLayer;

import java.awt.*;

/**
 * Displays the globe with a GARS graticule. The graticule is its own layer and can be turned on and off independent
 * of other layers. As the view zooms in, the graticule adjusts to display a finer grid. The example provides controls
 * to customize the color and opacity of the grid.
 *
 * @version $Id: GARSGraticule.java 2385 2014-10-14 21:56:07Z tgaskins $
 */
public class GARSGraticule extends ApplicationTemplate
{

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            GARSGraticuleLayer layer = new GARSGraticuleLayer();

            layer.setGraticuleLineColor(Color.WHITE, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_0);
            layer.setGraticuleLineColor(Color.YELLOW, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_1);
            layer.setGraticuleLineColor(Color.GREEN, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_2);
            layer.setGraticuleLineColor(Color.CYAN, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_3);

            layer.set30MinuteThreshold(1200e3);
            layer.set15MinuteThreshold(600e3);
            layer.set5MinuteThreshold(180e3);

            insertBeforePlacenames(this.getWwd(), layer);
        }
    }


    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind GARS Graticule", AppFrame.class);
    }
}