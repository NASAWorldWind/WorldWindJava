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

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: AbstractFeature.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AbstractFeature extends AbstractAction implements Feature
{
    protected String featureID;
    protected Controller controller;

    protected AbstractFeature(String s, String featureID, Registry registry)
    {
        super(s);

        this.putValue(Constants.ACTION_COMMAND, s);

        if (featureID != null && featureID.length() > 0 && registry != null)
        {
            this.featureID = featureID;
            registry.registerObject(featureID, this);
        }
    }

    protected AbstractFeature(String s, String featureID, String largeIconPath, Registry registry)
    {
        this(s, featureID, registry);

        if (largeIconPath != null && largeIconPath.length() > 0)
        {
            Icon icon = ImageLibrary.getIcon(largeIconPath);
            if (icon != null)
                this.putValue(Action.LARGE_ICON_KEY, icon);
            else
                this.putValue(Action.LARGE_ICON_KEY, ImageLibrary.getWarningIcon(64));
        }
    }

    public void initialize(Controller controller)
    {
        this.controller = controller;
        this.setMenuAccellerator(this.controller);
    }

    public boolean isInitialized()
    {
        return this.controller != null;
    }

    protected Object register(String featureID, Registry registry)
    {
        return registry.registerObject(featureID, this);
    }

    public Controller getController()
    {
        return this.controller;
    }

    public String getFeatureID()
    {
        return this.featureID;
    }

    public String getStringValue(String key)
    {
        return (String) this.getValue(key);
    }

    public String getName()
    {
        return (String) this.getValue(Action.NAME);
    }

    public boolean isOn()
    {
        return this.isEnabled();
    }

    public boolean isTwoState()
    {
        return false;
    }

    public void turnOn(boolean tf)
    {
    }

    protected void addToToolBar()
    {
        ToolBar toolBar = this.controller.getToolBar();
        if (toolBar != null)
            toolBar.addFeature(this);
    }

    protected void setMenuAccellerator(Controller controller)
    {
        if (controller == null)
            return;

        Object accelerator = controller.getRegisteredObject(this.getClass().getName() + Constants.ACCELERATOR_SUFFIX);
        if (accelerator == null)
            return;

        if (accelerator instanceof String)
        {
            KeyStroke keyStroke = KeyStroke.getKeyStroke((String) accelerator);
            if (keyStroke != null)
                this.putValue(Action.ACCELERATOR_KEY, keyStroke);
        }
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try // Protect application execution from exceptions thrown during action processing
        {
            doActionPerformed(actionEvent);
        }
        catch (Exception e)
        {
            Util.getLogger().log(Level.SEVERE, String.format("Error executing action %s.", getValue(Action.NAME)), e);
        }
    }

    protected void doActionPerformed(ActionEvent actionEvent)
    {
        this.turnOn(!this.isOn());
        this.controller.redraw();
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        try // Protect application execution from exceptions thrown during property processing
        {
            this.doPropertyChange(propertyChangeEvent);
        }
        catch (Exception e)
        {
            Util.getLogger().log(Level.SEVERE, String.format(
                "Error handling property change %s.", getValue(Action.NAME)), e);
        }
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    public void doPropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        // Override this method to respond to property changes
    }
}
