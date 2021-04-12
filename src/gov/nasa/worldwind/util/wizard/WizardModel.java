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

import javax.swing.*;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * @author dcollins
 * @version $Id: WizardModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WizardModel extends WizardProperties
{
    private Map<Object, WizardPanelDescriptor> panels;

    public static final String CURRENT_PANEL_DESCRIPTOR = "wizard.CurrentPanelDescriptor";
    public static final String BACK_BUTTON_TEXT = "wizard.BackButtonText";
    public static final String NEXT_BUTTON_TEXT = "wizard.NextButtonText";
    public static final String CANCEL_BUTTON_TEXT = "wizard.CancelButtonText";
    public static final String BACK_BUTTON_ENABLED = "wizard.BackButtonEnabled";
    public static final String NEXT_BUTTON_ENABLED = "wizard.NextButtonEnabled";
    public static final String CANCEL_BUTTON_ENABLED = "wizard.CancelButtonEnabled";
    public static final String BACK_BUTTON_ICON = "wizard.BackButtonIcon";
    public static final String NEXT_BUTTON_ICON = "wizard.NextButtonIcon";
    public static final String CANCEL_BUTTON_ICON = "wizard.CancelButtonIcon";

    public WizardModel()
    {
        this.panels = new HashMap<Object, WizardPanelDescriptor>();
    }

    public WizardPanelDescriptor getWizardPanel(Object id)
    {
        return this.panels.get(id);
    }

    public void registerWizardPanel(Object id, WizardPanelDescriptor panel)
    {
        if (id != null && panel != null)
        {
            this.panels.put(id, panel);
        }
    }

    public WizardPanelDescriptor getCurrentPanel()
    {
        Object value = getProperty(CURRENT_PANEL_DESCRIPTOR);
        return (value != null && value instanceof WizardPanelDescriptor) ? (WizardPanelDescriptor) value : null;
    }

    public boolean setCurrentPanel(Object id)
    {
        boolean success = false;
        WizardPanelDescriptor newPanel = this.panels.get(id);
        if (newPanel != null)
        {
            WizardPanelDescriptor oldPanel = getCurrentPanel();
            if (oldPanel != newPanel)
            {
                setProperty(CURRENT_PANEL_DESCRIPTOR, newPanel);
                firePropertyChange(CURRENT_PANEL_DESCRIPTOR, oldPanel, newPanel);
            }
            success = true;
        }
        return success;
    }

    public String getBackButtonText()
    {
        return getStringProperty(BACK_BUTTON_TEXT);
    }

    public void setBackButtonText(String newText)
    {
        setProperty(BACK_BUTTON_TEXT, newText);
    }

    public String getNextButtonText()
    {
        return getStringProperty(NEXT_BUTTON_TEXT);
    }

    public void setNextButtonText(String newText)
    {
        setProperty(NEXT_BUTTON_TEXT, newText);
    }

    public String getCancelButtonText()
    {
        return getStringProperty(CANCEL_BUTTON_TEXT);
    }

    public void setCancelButtonText(String newText)
    {
        setProperty(CANCEL_BUTTON_TEXT, newText);
    }

    public Boolean isBackButtonEnabled()
    {
        return getBooleanProperty(BACK_BUTTON_ENABLED);
    }

    public void setBackButtonEnabled(Boolean newValue)
    {
        setProperty(BACK_BUTTON_ENABLED, newValue);
    }

    public Boolean isNextButtonEnabled()
    {
        return getBooleanProperty(NEXT_BUTTON_ENABLED);
    }

    public void setNextButtonEnabled(Boolean newValue)
    {
        setProperty(NEXT_BUTTON_ENABLED, newValue);
    }

    public Boolean isCancelButtonEnabled()
    {
        return getBooleanProperty(CANCEL_BUTTON_ENABLED);
    }

    public void setCancelButtonEnabled(Boolean newValue)
    {
        setProperty(CANCEL_BUTTON_ENABLED, newValue);
    }

    public Icon getBackButtonIcon()
    {
        return getIconProperty(BACK_BUTTON_ICON);
    }

    public void setBackButtonIcon(Icon newIcon)
    {
        setProperty(BACK_BUTTON_ICON, newIcon);
    }

    public Icon getNextButtonIcon()
    {
        return getIconProperty(NEXT_BUTTON_ICON);
    }

    public void setNextButtonIcon(Icon newIcon)
    {
        setProperty(NEXT_BUTTON_ICON, newIcon);
    }

    public Icon getCancelButtonIcon()
    {
        return getIconProperty(CANCEL_BUTTON_ICON);
    }

    public void setCancelButtonIcon(Icon newIcon)
    {
        setProperty(CANCEL_BUTTON_ICON, newIcon);
    }

    public Icon getIconProperty(String propertyName)
    {
        Object value = getProperty(propertyName);
        return (value != null && value instanceof Icon) ? (Icon) value : null;
    }
}
