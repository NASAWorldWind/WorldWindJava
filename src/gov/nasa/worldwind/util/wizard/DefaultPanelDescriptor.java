/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.wizard;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: DefaultPanelDescriptor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DefaultPanelDescriptor implements WizardPanelDescriptor
{
    private Wizard wizard;
    private Object panelIdentifier;
    private Component panelComponent;

    private static final String DEFAULT_PANEL_IDENTIFIER = "wizard.DefaultPanelIdentifier";

    public DefaultPanelDescriptor()
    {
        this.panelIdentifier = DEFAULT_PANEL_IDENTIFIER;
        this.panelComponent = new JPanel();
    }

    public DefaultPanelDescriptor(Object id, Component panel)
    {
        if (id == null)
        {
            String message = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (panel == null)
        {
            String message = "Component is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.panelIdentifier = id;
        this.panelComponent = panel;
    }

    public final Wizard getWizard()
    {
        return this.wizard;
    }

    public final WizardModel getWizardModel()
    {
        return this.wizard != null ? this.wizard.getModel() : null;
    }

    public final Object getPanelIdentifier()
    {
        return this.panelIdentifier;
    }

    public final void setPanelIdentifier(Object panelIdentifier)
    {
        if (panelIdentifier == null)
        {
            String message = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.panelIdentifier = panelIdentifier;
    }

    public final Component getPanelComponent()
    {
        return this.panelComponent;
    }

    public final void setPanelComponent(Component panel)
    {
        if (panel == null)
        {
            String message = "Component is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.panelComponent = panel;
    }

    public Object getBackPanelDescriptor()
    {
        return null;
    }

    public Object getNextPanelDescriptor()
    {
        return null;
    }

    public void registerPanel(Wizard wizard)
    {
        this.wizard = wizard;
    }

    public void aboutToDisplayPanel()
    {
    }

    public void displayingPanel()
    {
    }

    public void aboutToHidePanel()
    {
    }
}
