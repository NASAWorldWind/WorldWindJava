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
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.LatLon;

import java.util.*;
import java.beans.*;

/**
 * @author dcollins
 * @version $Id: RasterControlPointList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RasterControlPointList extends java.util.ArrayList<RasterControlPointList.ControlPoint> implements AVList
{
    public static class ControlPoint extends AVListImpl
    {
        private final double wx;
        private final double wy;
        private final double rx;
        private final double ry;

        public ControlPoint(double worldX, double worldY, double rasterX, double rasterY)
        {
            this.wx = worldX;
            this.wy = worldY;
            this.rx = rasterX;
            this.ry = rasterY;
        }

        public java.awt.geom.Point2D getWorldPoint()
        {
            return new java.awt.geom.Point2D.Double(this.wx, this.wy);
        }

        public LatLon getWorldPointAsLatLon()
        {
            return LatLon.fromDegrees(this.wy, this.wx);
        }

        public java.awt.geom.Point2D getRasterPoint()
        {
            return new java.awt.geom.Point2D.Double(this.rx, this.ry);
        }
    }

    private AVList avList = new AVListImpl();

    public RasterControlPointList(java.util.Collection<? extends ControlPoint> c)
    {
        super(c);
    }

    public RasterControlPointList()
    {
    }

    public Object setValue(String key, Object value)
    {
        return this.avList.setValue(key, value);
    }

    public AVList setValues(AVList avList)
    {
        return this.avList.setValues(avList);
    }

    public Object getValue(String key)
    {
        return this.avList.getValue(key);
    }

    public Collection<Object> getValues()
    {
        return this.avList.getValues();
    }

    public String getStringValue(String key)
    {
        return this.avList.getStringValue(key);
    }

    public Set<Map.Entry<String, Object>> getEntries()
    {
        return this.avList.getEntries();
    }

    public boolean hasKey(String key)
    {
        return this.avList.hasKey(key);
    }

    public Object removeKey(String key)
    {
        return this.avList.removeKey(key);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.avList.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.avList.removePropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.avList.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.avList.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.avList.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        this.avList.firePropertyChange(propertyChangeEvent);
    }

    public AVList copy()
    {
        return this.avList.copy();
    }

    public AVList clearList()
    {
        return this.avList.clearList();
    }
}
