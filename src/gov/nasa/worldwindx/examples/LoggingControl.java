/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import java.util.logging.*;

/**
 * Illustrate control and redirection of World Wind logging.
 *
 * @author tag
 * @version $Id: LoggingControl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LoggingControl extends ApplicationTemplate
{
    // Use the standard World Wind application template
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false); // status bar, layer panel, not statistics panel
        }
    }

    public static void main(String[] args)
    {
        // Get the World Wind logger by name.
        Logger logger = Logger.getLogger("gov.nasa.worldwind");

        // Turn off logging to parent handlers of the World Wind handler.
        logger.setUseParentHandlers(false);

        // Create a console handler (defined below) that we use to write log messages.
        final ConsoleHandler handler = new MyHandler();

        // Enable all logging levels on both the logger and the handler.
        logger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);

        // Add our handler to the logger
        logger.addHandler(handler);

        // Start the application.
        ApplicationTemplate.start("World Wind Logging Control", AppFrame.class);
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
