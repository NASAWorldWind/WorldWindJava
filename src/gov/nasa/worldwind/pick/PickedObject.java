/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
