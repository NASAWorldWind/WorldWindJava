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
import gov.nasa.worldwind.avlist.AVKey;

/**
 * Shows how to turn on stereo, which is requested via a Java VM property. This example sets the property directly, but
 * it's more conventional to set it on the java command line.
 *
 * @author tag
 * @version $Id: Stereo.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Stereo extends ApplicationTemplate
{
    public static void main(String[] args)
    {
        // Set the stereo.mode property to request stereo. Request red-blue anaglyph in this case. Can also request
        // "device" if the display device supports stereo directly. To prevent stereo, leave the property unset or set
        // it to an empty string.
        System.setProperty("gov.nasa.worldwind.stereo.mode", "redblue");

        // Configure the initial view parameters so that the balloons are immediately visible.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 46.7045);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -121.6242);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 10e3);
        Configuration.setValue(AVKey.INITIAL_HEADING, 342);
        Configuration.setValue(AVKey.INITIAL_PITCH, 80);

        ApplicationTemplate.start("WorldWind Anaglyph Stereo", AppFrame.class);
    }
}
