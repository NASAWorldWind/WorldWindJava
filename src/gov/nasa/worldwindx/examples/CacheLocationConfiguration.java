/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;

/**
 * Illustrates how to specify a configuration file that specifies alternate locations for the World Wind local cache.
 * This example works in conjunction with the companion file CacheLocationConfiguration.xml, which specifies a
 * non-default location for the writable World Wind cache. That file also includes the standard read locations of the
 * cache so that any previously cached data will be found and used.
 *
 * @author tag
 * @version $Id: CacheLocationConfiguration.java 2851 2015-02-26 01:09:46Z tgaskins $
 */
public class CacheLocationConfiguration extends ApplicationTemplate
{
    public static void main(String[] args)
    {
        // Prior to starting World Wind, specify the cache configuration file to Configuration.
        Configuration.setValue(
            "gov.nasa.worldwind.avkey.DataFileStoreConfigurationFileName",
            "gov/nasa/worldwindx/examples/data/CacheLocationConfiguration.xml");

        ApplicationTemplate.start("World Wind Cache Configuration", AppFrame.class);
    }
}
