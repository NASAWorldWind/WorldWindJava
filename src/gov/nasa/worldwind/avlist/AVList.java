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
package gov.nasa.worldwind.avlist;

import java.util.*;

/**
 * An interface for managing an attribute-value pair collection.
 *
 * @author Tom Gaskins
 * @version $Id: AVList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface AVList
{
    /**
     * Adds a key/value pair to the list. Replaces an existing key/value pair if the list already contains the key.
     *
     * @param key   the attribute name. May not be <code>null</code>.
     * @param value the attribute value. May be <code>null</code>, in which case any existing value for the key is
     *              removed from the collection.
     *
     * @return previous value associated with specified key, or null  if there was no mapping for key. A null return can
     *         also indicate that the map previously associated null  with the specified key, if the implementation
     *         supports null values.
     *
     * @throws NullPointerException if <code>key</code> is <code>null</code>.
     */
    Object setValue(String key, Object value);

    /**
     * Adds the contents of another attribute-value list to the list. Replaces an existing key/value pair if the list
     * already contains the key.
     *
     * @param avList the list to copy. May not be <code>null</code>.
     *
     * @return <code>this</code>, a self reference.
     *
     * @throws NullPointerException if <code>avList</code> is <code>null</code>.
     */
    AVList setValues(AVList avList);

    /**
     * Returns the value for a specified key.
     *
     * @param key the attribute name. May not be <code>null</code>.
     *
     * @return the attribute value if one exists in the collection, otherwise <code>null</code>.
     *
     * @throws NullPointerException if <code>key</code> is <code>null</code>.
     */
    Object getValue(String key);

    Collection<Object> getValues();

    /**
     * Returns the value for a specified key. The value must be a {@link String}.
     *
     * @param key the attribute name. May not be <code>null</code>.
     *
     * @return the attribute value if one exists in the collection, otherwise <code>null</code>.
     *
     * @throws NullPointerException if <code>key</code> is <code>null</code>.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                              if the value in the collection is not a <code>String</code> type.
     */
    String getStringValue(String key);

    Set<Map.Entry<String, Object>> getEntries();

    /**
     * Indicates whether a key is in the collection.
     *
     * @param key the attribute name. May not be <code>null</code>.
     *
     * @return <code>true</code> if the key exists in the collection, otherwise <code>false</code>.
     *
     * @throws NullPointerException if <code>key</code> is <code>null</code>.
     */
    boolean hasKey(String key);

    /**
     * Removes a specified key from the collection if the key exists, otherwise returns without affecting the
     * collection.
     *
     * @param key the attribute name. May not be <code>null</code>.
     *
     * @return previous value associated with specified key, or null  if there was no mapping for key.
     *
     * @throws NullPointerException if <code>key</code> is <code>null</code>.
     */
    Object removeKey(String key);

    /**
     * Adds a property change listener for the specified key.
     *
     * @param propertyName the key to associate the listener with.
     * @param listener     the listener to associate with the key.
     *
     * @throws IllegalArgumentException if either <code>propertyName</code> or <code>listener</code> is null
     * @see java.beans.PropertyChangeSupport
     */
    void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener);

    /**
     * Removes a property change listener associated with the specified key.
     *
     * @param propertyName the key associated with the change listener.
     * @param listener     the listener to remove.
     *
     * @throws IllegalArgumentException if either <code>propertyName</code> or <code>listener</code> is null
     * @see java.beans.PropertyChangeSupport
     */
    void removePropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener);

    /**
     * Adds the specified all-property property change listener that will be called for all list changes.
     *
     * @param listener the listener to call.
     *
     * @throws IllegalArgumentException if <code>listener</code> is null
     * @see java.beans.PropertyChangeSupport
     */
    void addPropertyChangeListener(java.beans.PropertyChangeListener listener);

    /**
     * Removes the specified all-property property change listener.
     *
     * @param listener the listener to remove.
     *
     * @throws IllegalArgumentException if <code>listener</code> is null
     * @see java.beans.PropertyChangeSupport
     */
    void removePropertyChangeListener(java.beans.PropertyChangeListener listener);

    /**
     * Calls all property change listeners associated with the specified key. No listeners are called if
     * <code>odValue</code> and <code>newValue</code> are equal and non-null.
     *
     * @param propertyName the key
     * @param oldValue     the value associated with the key before the even causing the firing.
     * @param newValue     the new value associated with the key.
     *
     * @throws IllegalArgumentException if <code>propertyName</code> is null
     * @see java.beans.PropertyChangeSupport
     */
    void firePropertyChange(String propertyName, Object oldValue, Object newValue);

    /**
     * Calls all registered property change listeners with the specified property change event.
     *
     * @param propertyChangeEvent the event
     *
     * @throws IllegalArgumentException if <code>propertyChangeEvent</code> is null
     * @see java.beans.PropertyChangeSupport
     */
    void firePropertyChange(java.beans.PropertyChangeEvent propertyChangeEvent);

    /**
     * Returns a shallow copy of this <code>AVList</code> instance: the keys and values themselves are not cloned.
     *
     * @return a shallow copy of this <code>AVList</code>.
     */
    AVList copy();

    AVList clearList();
}
