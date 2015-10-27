/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwindx.applications.worldwindow.features.AbstractFeature;

import javax.swing.*;

/**
 * @author tag
 * @version $Id: MenuBarImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MenuBarImpl extends AbstractFeature implements MenuBar
{
    // These are the menus in the menu bar. To add new menus, add them to this list in the order they should appear.
    private static final String[] menuIDs = new String[]
        {
//            Constants.SDF_MENU,
        };

    private JMenuBar menuBar;

    public MenuBarImpl(Registry registry)
    {
        super("Menu Bar", Constants.MENU_BAR, registry);
    }

    public void initialize(Controller controller)
    {
        this.menuBar = new JMenuBar();

        for (String menuID : menuIDs)
        {
            Menu menu = (Menu) controller.getRegisteredObject(menuID);
            if (menu != null)
            {
                getJMenuBar().add(menu.getJMenu());
            }
        }
    }

    public JMenuBar getJMenuBar()
    {
        return this.menuBar;
    }

    public void addMenu(Menu menu)
    {
        if (menu != null)
            getJMenuBar().add(menu.getJMenu());
    }
}
