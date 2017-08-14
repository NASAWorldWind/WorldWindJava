/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

/**
 * Illustrates how to configure WorldWind with a custom <code>{@link gov.nasa.worldwind.globes.ElevationModel}</code>
 * from a configuration file.
 *
 * @author tag
 * @version $Id: CustomElevationModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CustomElevationModel extends ApplicationTemplate
{
    public static void main(String[] args)
    {
        // Specify the configuration file for the elevation model prior to starting WorldWind:
        Configuration.setValue(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE,
            "gov/nasa/worldwindx/examples/data/CustomElevationModel.xml");

        ApplicationTemplate.start("WorldWind Custom Elevation Model", AppFrame.class);
    }
}
