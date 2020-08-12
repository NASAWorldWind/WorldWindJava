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

import java.util.logging.*;

/**
 * Illustrate control and redirection of WorldWind logging.
 *
 * @author tag
 * @version $Id: LoggingControl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LoggingControl extends ApplicationTemplate
{
    // Use the standard WorldWind application template
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false); // status bar, layer panel, not statistics panel
        }
    }

    public static void main(String[] args)
    {
        // Get the WorldWind logger by name.
        Logger logger = Logger.getLogger("gov.nasa.worldwind");

        // Turn off logging to parent handlers of the WorldWind handler.
        logger.setUseParentHandlers(false);

        // Create a console handler (defined below) that we use to write log messages.
        final ConsoleHandler handler = new MyHandler();

        // Enable all logging levels on both the logger and the handler.
        logger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);

        // Add our handler to the logger
        logger.addHandler(handler);

        // Start the application.
        ApplicationTemplate.start("WorldWind Logging Control", AppFrame.class);
    }

    private static class MyHandler extends ConsoleHandler
    {
        public void publish(LogRecord logRecord)
        {
            // Just redirect the record to ConsoleHandler for printing.
            System.out.printf("Hey, this came from Me!\n");
            super.publish(logRecord);
        }
    }
}
