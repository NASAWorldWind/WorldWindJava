/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwindx.examples.util.SessionState;

import java.awt.event.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: PersistSessionState.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class PersistSessionState extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        /**
         * Create a SessionState utility to load and save World Wind's layer and view state to the default location.
         * Initialized to a new SessionState with its session key set to this application's class name.
         */
        protected SessionState sessionState = new SessionState(PersistSessionState.class.getName());

        /** Creates a new AppFrame, but otherwise does nothing. */
        public AppFrame()
        {
            this.initSessionState();
        }

        /**
         * Restores any session state associated with this application and registers a listener to save session state
         * prior to this application exiting.
         */
        protected void initSessionState()
        {
            // Restore any session state associated with this application. This does nothing if there is no state
            // associated with this application in the default location.
            this.restoreSessionState();

            // Listen for the window closed event on this AppFrame and save this application's restorable state in the
            // default location just prior to this frame closing. This listener does not receive an event if the Java
            // Virtual Machine exits abnormally. We can reliably use this as an indicator for application shutdown
            // because we have configured the host application when running on Mac OS X to respond to the default quit
            // command by closing all windows. Using a shutdown hook is undesirable because we cannot rely on the state
            // of the WorldWindow from another thread, and attempting to synchronize with the EDT from a shutdown thread
            // risks a deadlock.
            this.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent windowEvent)
                {
                    saveSessionState();
                }
            });
        }

        protected void saveSessionState()
        {
            try
            {
                // Save the WorldWindow's current state. This state is restored the next time this example loads by the
                // call to restoreSessionState below.
                this.sessionState.saveSessionState(this.getWwd());
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "Unable to save session state", e);
            }
        }

        protected void restoreSessionState()
        {
            try
            {
                // Restore the WorldWindow's state to the last saved session state.
                this.sessionState.restoreSessionState(this.getWwd());
                // Update the layer panel to display changes in the layer list, and cause the WorldWindow to repaint
                // itself. These two lines should be omitted in applications that automatically handle layer panel
                // updates and WorldWindow repaints when the layer list changes.
                this.getWwd().redraw();
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "Unable to restore session state", e);
            }
        }
    }

    public static void main(String[] args)
    {
        // Configure the Mac OS X application's default quit action to close all windows instead of executing
        // System.exit. This enables us to detect the application exiting by listening to the window close event. See
        // the following URL for details:
        // http://developer.apple.com/library/mac/documentation/Java/Reference/JavaSE6_AppleExtensionsRef/api/com/apple/eawt/Application.html#setQuitStrategy(com.apple.eawt.QuitStrategy)
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
        }

        start("World Wind Persist Session State", AppFrame.class);
    }
}
