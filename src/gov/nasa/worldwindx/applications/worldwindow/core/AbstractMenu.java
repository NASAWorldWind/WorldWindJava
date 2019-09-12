/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwindx.applications.worldwindow.features.Feature;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: AbstractMenu.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AbstractMenu extends JMenu implements Menu, MenuListener, WWMenu
{
    protected Controller controller;

    protected AbstractMenu(String title, String menuID, Registry registry)
    {
        super(title);

        if (menuID != null && registry != null)
            registry.registerObject(menuID, this);
    }

    public void initialize(Controller controller)
    {
        this.controller = controller;

        this.addMenuListener(this);
    }

    public boolean isInitialized()
    {
        return this.controller != null;
    }

    protected void addToMenuBar()
    {
        if (this.controller.getMenuBar() != null)
            this.controller.getMenuBar().addMenu(this);
    }

    public void addMenu(String featureID)
    {
        this.addMenus(new String[] {featureID});
    }

    public void addMenus(String[] featureIDs)
    {
        boolean radioGroup = false;

        for (String featureID : featureIDs)
        {
            if (featureID == null || featureID.length() == 0)
            {
                radioGroup = false;
                this.add(new JSeparator());
            }
            else if (featureID.equals(Constants.RADIO_GROUP))
            {
                radioGroup = true;
            }
            else if (featureID.startsWith("gov.nasa.worldwindx.applications.worldwindow.menu"))
            {
                Object o = this.controller.getRegisteredObject(featureID);
                if (o instanceof Menu)
                    this.add(((Menu) o).getJMenu());
            }
            else
            {
                Feature feature = (Feature) controller.getRegisteredObject(featureID);
                if (feature != null)
                {
                    if (feature.isTwoState())
                    {
                        if (radioGroup)
                        {
                            final JMenuItem menuItem = new RadioMenuItem(feature);

                            this.add(menuItem);
                            feature.addPropertyChangeListener(new PropertyChangeListener()
                            {
                                public void propertyChange(PropertyChangeEvent event)
                                {
                                    if (event.getPropertyName().equals(Constants.ON_STATE))
                                    {
                                        menuItem.setSelected((Boolean) event.getNewValue());
                                        menuItem.repaint();
                                    }
                                }
                            });
                        }
                        else
                        {
                            this.add(new ToggleMenuItem(feature));
                        }
                    }
                    else
                    {
                        radioGroup = false;
                        this.add(new JMenuItem(feature));
                    }
                }
            }
        }
    }

    public void menuSelected(MenuEvent menuEvent)
    {
        for (int i = 0; i < this.getItemCount(); i++)
        {
            JMenuItem mi = this.getItem(i);
            if (mi instanceof ToggleMenuItem)
            {
                ToggleMenuItem tmi = (ToggleMenuItem) mi;
                Feature feature = (Feature) tmi.getAction();
                tmi.setState(feature.isOn());
            }
        }
    }

    public void menuDeselected(MenuEvent menuEvent)
    {
    }

    public void menuCanceled(MenuEvent menuEvent)
    {
    }

    protected Controller getController()
    {
        return controller;
    }

    public JMenu getJMenu()
    {
        return this;
    }

    private static class ToggleMenuItem extends JCheckBoxMenuItem
    {
        public ToggleMenuItem(Action action)
        {
            super(action);
        }
    }

    private static class RadioMenuItem extends JRadioButtonMenuItem
    {
        public RadioMenuItem(Action action)
        {
            super(action);
        }
    }

    protected List<Feature> getFeatures()
    {
        ArrayList<Feature> featureList = new ArrayList<Feature>();

        for (int i = 0; i < this.getItemCount(); i++)
        {
            Object o = this.getItem(i) != null ? this.getItem(i).getAction() : null;
            if (o != null && o instanceof Feature)
                featureList.add((Feature) o);
        }

        return featureList;
    }
}
