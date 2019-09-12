/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.pick;

import java.util.*;

/**
 * @author tag
 * @version $Id: PickedObjectList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PickedObjectList extends ArrayList<PickedObject>
{
    public PickedObjectList()
    {
    }

    public PickedObjectList(PickedObjectList list) // clone a shallow copy
    {
        super(list);
    }

    public PickedObject getTopPickedObject()
    {
        int size = this.size();

        if (1 < size)
        {
            for (PickedObject po : this)
            {
                if (po.isOnTop())
                    return po;
            }
        }

        if (0 < size)
        {   // if we are here, then no objects were mark as 'top'
            return this.get(0);
        }

        return null;
    }

    public Object getTopObject()
    {
        PickedObject po = this.getTopPickedObject();
        return po != null ? po.getObject() : null;
    }

    public PickedObject getTerrainObject()
    {
        for (PickedObject po : this)
        {
            if (po.isTerrain())
                return po;
        }

        return null;
    }

    public PickedObject getMostRecentPickedObject()
    {
        return this.size() > 0 ? this.get(this.size() - 1) : null;
    }

    /**
     * Returns a list of all picked objects in this list who's onTop flag is set to true. This returns <code>null</code>
     * if this list is empty, or does not contain any picked objects marked as on top.
     *
     * @return a new list of the picked objects marked as on top, or <code>null</code> if nothing is marked as on top.
     */
    public List<PickedObject> getAllTopPickedObjects()
    {
        List<PickedObject> list = null; // Lazily create the list to avoid unnecessary allocations.

        for (PickedObject po : this)
        {
            if (po.isOnTop())
            {
                if (list == null)
                    list = new ArrayList<PickedObject>();
                list.add(po);
            }
        }

        return list;
    }

    /**
     * Returns a list of all objects associated with a picked object in this list who's onTop flag is set to true. This
     * returns <code>null</code> if this list is empty, or does not contain any picked objects marked as on top.
     *
     * @return a new list of the objects associated with a picked object marked as on top, or <code>null</code> if
     *         nothing is marked as on top.
     */
    public List<?> getAllTopObjects()
    {
        List<Object> list = null; // Lazily create the list to avoid unnecessary allocations.

        for (PickedObject po : this)
        {
            if (po.isOnTop())
            {
                if (list == null)
                    list = new ArrayList<Object>();
                list.add(po.getObject());
            }
        }

        return list;
    }

    public boolean hasNonTerrainObjects()
    {
        return this.size() > 1 || (this.size() == 1 && this.getTerrainObject() == null);
    }
}
