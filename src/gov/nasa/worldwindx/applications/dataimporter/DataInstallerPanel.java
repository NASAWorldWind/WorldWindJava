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

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.beans.*;

/**
 * This panel holds the data installation panel and the installed-data display panel in a tabbed pane. In addition to
 * its use in the data installer app, it can be used independently.
 *
 * @author tag
 * @version $Id: DataInstallerPanel.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class DataInstallerPanel extends JPanel
{
    protected FileSetPanel fileSetPanel; // data available on disk
    protected FileStorePanel fileStorePanel; // data currently installed
    protected WorldWindow wwd;

    public DataInstallerPanel(final WorldWindow wwd)
    {
        super(new BorderLayout(5, 5));

        this.wwd = wwd;

        this.setBorder(new EmptyBorder(30, 10, 10, 10));

        this.fileSetPanel = new FileSetPanel(wwd);

        this.fileStorePanel = new FileStorePanel(wwd);
        this.fileStorePanel.update(WorldWind.getDataFileStore());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Available Data", this.fileSetPanel);
        tabbedPane.add("Installed Data", this.fileStorePanel);

        this.add(tabbedPane, BorderLayout.CENTER);

        this.fileSetPanel.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent event)
            {
                // Forward the event to this instance's listeners.
                firePropertyChange(event.getPropertyName(), event.getOldValue(), event.getNewValue());
            }
        });

        this.fileSetPanel.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                // Update the installed-data panel when a new data set is installed.
                if (propertyChangeEvent.getPropertyName().equals(DataInstaller.INSTALL_COMPLETE))
                    fileStorePanel.update(WorldWind.getDataFileStore());
            }
        });
    }
}
