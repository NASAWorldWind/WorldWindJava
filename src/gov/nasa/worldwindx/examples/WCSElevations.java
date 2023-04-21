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

import gov.nasa.worldwindx.examples.util.WCSCoveragePanel;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.net.URISyntaxException;

/**
 * @author tag
 * @version $Id: WCSElevations.java 2136 2014-07-10 01:01:13Z pabercrombie $
 */
public class WCSElevations extends ApplicationTemplate
{
    protected static final String[] servers = new String[]
        {
            "https://worldwind26.arc.nasa.gov/wcs",
        };

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected final Dimension wcsPanelSize = new Dimension(400, 600);
        protected JTabbedPane tabbedPane;
        protected int previousTabIndex;

        public AppFrame()
        {
            this.tabbedPane = new JTabbedPane();

            this.tabbedPane.add(new JPanel());
            this.tabbedPane.setTitleAt(0, "+");
            this.tabbedPane.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                    if (tabbedPane.getSelectedIndex() != 0)
                    {
                        previousTabIndex = tabbedPane.getSelectedIndex();
                        return;
                    }

                    String server = JOptionPane.showInputDialog("Enter WCS server URL");
                    if (server == null || server.length() < 1)
                    {
                        tabbedPane.setSelectedIndex(previousTabIndex);
                        return;
                    }

                    // Respond by adding a new WMSLayerPanel to the tabbed pane.
                    if (addTab(tabbedPane.getTabCount(), server.trim()) != null)
                        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                }
            });

            // Create a tab for each server and add it to the tabbed panel.
            for (int i = 0; i < servers.length; i++)
            {
                this.addTab(i + 1, servers[i]); // i+1 to place all server tabs to the right of the Add Server tab
            }

            // Display the first server pane by default.
            this.tabbedPane.setSelectedIndex(this.tabbedPane.getTabCount() > 0 ? 1 : 0);
            this.previousTabIndex = this.tabbedPane.getSelectedIndex();

            // Add the tabbed pane to a frame separate from the WorldWindow.
            JFrame controlFrame = new JFrame();
            controlFrame.getContentPane().add(tabbedPane);
            controlFrame.pack();
            controlFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            controlFrame.setVisible(true);
        }

        protected WCSCoveragePanel addTab(int position, String server)
        {
            // Add a server to the tabbed dialog.
            try
            {
                WCSCoveragePanel coveragePanel = new WCSCoveragePanel(AppFrame.this.getWwd(), server, wcsPanelSize);
                this.tabbedPane.add(coveragePanel, BorderLayout.CENTER);
                String title = coveragePanel.getServerDisplayString();
                this.tabbedPane.setTitleAt(position, title != null && title.length() > 0 ? title : server);

                return coveragePanel;
            }
            catch (URISyntaxException e)
            {
                JOptionPane.showMessageDialog(null, "Server URL is invalid", "Invalid Server URL",
                    JOptionPane.ERROR_MESSAGE);
                tabbedPane.setSelectedIndex(previousTabIndex);
                return null;
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind WCS Layers", AppFrame.class);
    }
}
