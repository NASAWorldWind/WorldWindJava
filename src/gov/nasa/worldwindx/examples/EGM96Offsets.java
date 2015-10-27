/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.globes.Earth;

import java.io.IOException;

/**
 * Shows how to apply EGM96 offsets to the Earth.
 * @author tag
 * @version $Id: EGM96Offsets.java 1501 2013-07-11 15:59:11Z tgaskins $
 */
public class EGM96Offsets extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            Model m = this.wwjPanel.getWwd().getModel();
            try
            {
                ((Earth) m.getGlobe()).applyEGMA96Offsets("config/EGM96.dat");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind EGM96 Offsets", AppFrame.class);
    }
}
