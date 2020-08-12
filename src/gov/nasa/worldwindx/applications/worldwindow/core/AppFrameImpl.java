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

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwindx.applications.worldwindow.features.AbstractFeature;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: AppFrameImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AppFrameImpl extends AbstractFeature implements AppFrame
{
    // only one of these will be non-null
    protected JFrame frame;

    public AppFrameImpl(Registry registry)
    {
        super("App Frame", Constants.APP_FRAME, registry);
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);
        this.initializeApp();
    }

    protected void initializeApp()
    {
        try
        {
            frame = new JFrame();
            frame.setTitle(controller.getAppTitle());
            frame.getContentPane().add(controller.getAppPanel().getJPanel(), BorderLayout.CENTER);

            ToolBar toolBar = controller.getToolBar();
            if (toolBar != null)
                frame.add(toolBar.getJToolBar(), BorderLayout.PAGE_START);

            StatusPanel statusPanel = controller.getStatusPanel();
            if (statusPanel != null)
                frame.add(statusPanel.getJPanel(), BorderLayout.PAGE_END);

            MenuBar menuBar = controller.getMenuBar();
            if (menuBar != null)
                frame.setJMenuBar(menuBar.getJMenuBar());

            frame.pack();

            ToolTipManager.sharedInstance().setDismissDelay(60000);

            // Center the application on the screen.
            Dimension prefSize = frame.getPreferredSize();
            Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
            frame.setLocation(x, y);
            frame.setResizable(true);

            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        catch (Exception e)
        {
            String msg = "Unable to initialize the application.";
            Util.getLogger().log(Level.SEVERE, msg, e);
            this.controller.showErrorDialogLater(null, "Initialization Error", msg);
        }
    }

    public Frame getFrame()
    {
        return this.frame;
    }
}
