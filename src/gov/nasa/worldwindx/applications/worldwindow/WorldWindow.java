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

package gov.nasa.worldwindx.applications.worldwindow;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import java.awt.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: WorldWindow.java 1923 2014-04-10 23:35:03Z tgaskins $
 */
public class WorldWindow
{
    static
    {
        System.setProperty("gov.nasa.worldwind.app.config.document",
            "gov/nasa/worldwindx/applications/worldwindow/config/worldwindow.worldwind.xml");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            String s = Configuration.getStringValue(Constants.APPLICATION_DISPLAY_NAME);
            if (s == null)
                s = "WorldWindow";
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", s);
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    private static final String APP_CONFIGURATION
        = "gov/nasa/worldwindx/applications/worldwindow/config/AppConfiguration.xml";

    public static void main(String[] args)
    {
        Controller controller = new Controller();

        Dimension appSize = null;
        if (args.length >= 2) // The first two arguments are the application width and height.
            appSize = new Dimension(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        try
        {
            controller.start(APP_CONFIGURATION, appSize);
        }
        catch (Exception e)
        {
            String msg = "Fatal application error";
            controller.showErrorDialog(null, "Cannot Start Application", msg);
            Util.getLogger().log(Level.SEVERE, msg);
        }
    }
}
