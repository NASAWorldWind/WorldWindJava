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

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwindx.examples.BulkDownloadPanel;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author Patrick Murris
 * @version $Id: BulkDownloadFrame.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BulkDownloadFrame extends JFrame
{
    public BulkDownloadFrame(WorldWindow wwd)
    {
        final BulkDownloadPanel panel = new BulkDownloadPanel(wwd);
        final JFrame frameInstance = this;

        this.setTitle("Bulk Download");
        this.add(panel);
        this.pack();
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                // Check whether some downloads are active before closing the frame
                if(panel.hasActiveDownloads())
                {
                    int choice = JOptionPane.showConfirmDialog(frameInstance, "Cancel all active downloads?",
                        "Active downloads", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.CANCEL_OPTION)
                        return;

                    // Cancel active downloads and clear the monitor panel
                    panel.cancelActiveDownloads();
                    panel.clearInactiveDownloads();
                }
                // Clear sector selector
                panel.clearSector();
                // Close now
                setVisible(false);
            }
        });

    }
}
