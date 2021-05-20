/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;

/**
 * Illustrates how to specify a configuration file that specifies alternate locations for the WorldWind local cache.
 * This example works in conjunction with the companion file CacheLocationConfiguration.xml, which specifies a
 * non-default location for the writable WorldWind cache. That file also includes the standard read locations of the
 * cache so that any previously cached data will be found and used.
 *
 * @author tag
 * @version $Id: CacheLocationConfiguration.java 2851 2015-02-26 01:09:46Z tgaskins $
 */
public class CacheLocationConfiguration extends ApplicationTemplate
{
    public static void main(String[] args)
    {
        // Prior to starting WorldWind, specify the cache configuration file to Configuration.
        Configuration.setValue(
            "gov.nasa.worldwind.avkey.DataFileStoreConfigurationFileName",
            "gov/nasa/worldwindx/examples/data/CacheLocationConfiguration.xml");

        ApplicationTemplate.start("WorldWind Cache Configuration", AppFrame.class);
    }
}
