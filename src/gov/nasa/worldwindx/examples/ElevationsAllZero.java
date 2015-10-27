/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.terrain.ZeroElevationModel;

import javax.swing.*;

/**
 * Shows how to use {@link ZeroElevationModel} to eliminate all elevations on the globe.
 *
 * @author tag
 * @version $Id: ElevationsAllZero.java 699 2012-07-13 17:53:47Z tgaskins $
 */
public class ElevationsAllZero
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected SurfaceImage surfaceImage;
        protected JSlider opacitySlider;

        public AppFrame()
        {
            super(true, true, false);

            // Eliminate elevations by simply setting the globe's elevation model to ZeroElevationModel.

            this.getWwd().getModel().getGlobe().setElevationModel(new ZeroElevationModel());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Zero Elevations", AppFrame.class);
    }
}
