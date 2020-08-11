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
package gov.nasa.worldwind.pick;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;

import java.awt.*;

/**
 * @author lado
 * @version $Id: PickedObject.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PickedObject extends AVListImpl
{
    private final Point pickPoint;
    private final int colorCode;
    private final Object userObject;
    private boolean isOnTop = false;
    private boolean isTerrain = false;

    public PickedObject(int colorCode, Object userObject)
    {
        super();

        this.pickPoint = null;
        this.colorCode = colorCode;
        this.userObject = userObject;
        this.isOnTop = false;
        this.isTerrain = false;
    }

    public PickedObject(int colorCode, Object userObject, Position position, boolean isTerrain)
    {
        super();

        this.pickPoint = null;
        this.colorCode = colorCode;
        this.userObject = userObject;
        this.isOnTop = false;
        this.isTerrain = isTerrain;
        this.setPosition(position);
    }

    public PickedObject(Point pickPoint, int colorCode, Object userObject, Angle lat, Angle lon, double elev,
        boolean isTerrain)
    {
        super();

        this.pickPoint = pickPoint;
        this.colorCode = colorCode;
        this.userObject = userObject;
        this.isOnTop = false;
        this.isTerrain = isTerrain;
        this.setPosition(new Position(lat, lon, elev));
    }

    public Point getPickPoint()
    {
        return pickPoint;
    }

    public int getColorCode()
    {
        return this.colorCode;
    }

    public Object getObject()
    {
        return userObject;
    }

    public void setOnTop()
    {
        this.isOnTop = true;
    }

    public boolean isOnTop()
    {
        return this.isOnTop;
    }

    public boolean isTerrain()
    {
        return this.isTerrain;
    }

    public void setParentLayer(Layer layer)
    {
        this.setValue(AVKey.PICKED_OBJECT_PARENT_LAYER, layer);
    }

    public Layer getParentLayer()
    {
        return (Layer) this.getValue(AVKey.PICKED_OBJECT_PARENT_LAYER);
    }

    public void setPosition(Position position)
    {
        this.setValue(AVKey.POSITION, position);
    }

    public Position getPosition()
    {
        return (Position) this.getValue(AVKey.POSITION);
    }

    public boolean hasPosition()
    {
        return this.hasKey(AVKey.POSITION);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PickedObject that = (PickedObject) o;

        if (colorCode != that.colorCode)
            return false;
        if (isOnTop != that.isOnTop)
            return false;
        //noinspection RedundantIfStatement
        if (userObject != null ? !userObject.equals(that.userObject) : that.userObject != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = colorCode;
        result = 31 * result + (userObject != null ? userObject.hashCode() : 0);
        result = 31 * result + (isOnTop ? 1 : 0);
        return result;
    }
}
