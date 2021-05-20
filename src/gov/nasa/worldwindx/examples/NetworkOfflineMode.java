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
