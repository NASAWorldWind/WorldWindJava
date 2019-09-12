/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwindx.examples.util.ScreenShotAction;

import javax.swing.*;

/**
 * This example demonstrates how to take screenshots with WWJ using the {@link gov.nasa.worldwindx.examples.util.ScreenShotAction}
 * class.
 *
 * @author tag
 * @version $Id: ScreenShots.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenShots extends JFrame
{
    static
    {
        // Ensure that menus and tooltips interact successfully with the WWJ window.
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    private WorldWindow wwd;

    public ScreenShots()
    {
        WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();
        this.wwd = wwd;
        wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
        this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
        wwd.setModel(new BasicModel());
    }

    private JMenuBar createMenuBar()
    {
        JMenu menu = new JMenu("File");

        JMenuItem snapItem = new JMenuItem("Save Snapshot...");
        snapItem.addActionListener(new ScreenShotAction(this.wwd));
        menu.add(snapItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        return menuBar;
    }

    public static void main(String[] args)
    {
        // Swing components should always be instantiated on the event dispatch thread.
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                ScreenShots frame = new ScreenShots();

                frame.setJMenuBar(frame.createMenuBar()); // Create menu and associate with frame

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
