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
