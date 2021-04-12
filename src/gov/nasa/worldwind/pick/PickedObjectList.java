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
