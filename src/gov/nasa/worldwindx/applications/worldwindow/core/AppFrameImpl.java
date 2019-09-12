/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
