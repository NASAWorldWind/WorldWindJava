/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.wizard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dcollins
 * @version $Id: WizardProperties.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WizardProperties
{
    private Map<String, Object> properties;
    private PropertyChangeSupport propertyChangeSupport;

    public WizardProperties()
    {
        this.properties = new HashMap<String, Object>();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public Object getProperty(String propertyName)
    {
        return propertyName != null ? this.properties.get(propertyName) : null;
    }

    public String getStringProperty(String propertyName)
    {
        Object value = getProperty(propertyName);
        return (value != null && value instanceof String) ? (String) value : null;
    }

    public Boolean getBooleanProperty(String propertyName)
    {
        Object value = getProperty(propertyName);
        return (value != null && value instanceof Boolean) ? (Boolean) value : null;
    }

    public Integer getIntegerProperty(String propertyName)
    {
        Object value = getProperty(propertyName);
        return (value != null && value instanceof Integer) ? (Integer) value : null;
    }

    public void setProperty(String propertyName, Object newValue)
    {
        if (propertyName != null)
        {
            Object oldValue = this.properties.get(propertyName);
            if (newValue != null ? !newValue.equals(oldValue) : oldValue != null)
            {
                this.properties.put(propertyName, newValue);
                firePropertyChange(propertyName, oldValue, newValue);
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
