/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Shows how to detach WorldWind from the network and reattach it.
 *
 * @author tag
 * @version $Id: NetworkOfflineMode.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class NetworkOfflineMode extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);
        }

        protected JPanel makeControlPanel()
        {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 15, 10), new EtchedBorder()));

            JCheckBox modeSwitch = new JCheckBox(new AbstractAction(" Online")
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Get the current status
                    boolean offline = WorldWind.getNetworkStatus().isOfflineMode();

                    // Change it to its opposite
                    offline = !offline;
                    WorldWind.getNetworkStatus().setOfflineMode(offline);

                    // Cause data retrieval to resume if now online
                    if (!offline)
                        getWwd().redraw();
                }
            });
            modeSwitch.setSelected(true); // WW starts out online
            panel.add(modeSwitch, BorderLayout.CENTER);

            return panel;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Network Offline Mode", AppFrame.class);
    }
}
