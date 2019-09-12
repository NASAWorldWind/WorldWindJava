/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.Initializable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author tag
 * @version $Id: Feature.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Feature extends Initializable, Action, PropertyChangeListener
{
    String getFeatureID();

    boolean isOn();

    /**
     * Indicates whether the feature can be either on or off, without any other states. This is used by the tool bar and
     * menu-bar menus to determine whether the feature's enable/disable button or menu item should be displayed with an
     * indicator that it's either on or off. In the case of a menu the indicator is a check box. In the case of a tool
     * bar button the indicator is a dot above the button.
     *
     * @return true if the feature has only two states, otherwise off.
     */
    boolean isTwoState();

    void turnOn(boolean tf);

    String getName();
}
