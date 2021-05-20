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
package gov.nasa.worldwind.layers.placename;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;

import java.util.*;

/**
 * @author Paul Collins
 * @version $Id: PlaceNameServiceSet.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PlaceNameServiceSet extends WWObjectImpl implements AVList
{
    private final List<PlaceNameService> serviceList = new LinkedList<PlaceNameService>();
    private long expiryTime = 0;

    public PlaceNameServiceSet()
    {
    }

    /**
     * Add a service to the service set.
     *
     * @param placeNameService Service to add. May not be null.
     * @param replace          {@code true} if the service may replace an equivalent service already in the set. {@code
     *                         false} if the service may not replace a service already in the set.
     *
     * @return {@code true} if the service was added to the service set, or if the service replaced an existing item in
     *         the service set. Returns {@code false} if the set was not changed.
     *
     * @throws IllegalArgumentException if <code>placeNameService</code> is null
     */
    public boolean addService(PlaceNameService placeNameService, boolean replace)
    {
        if (placeNameService == null)
        {
            String message = Logging.getMessage("nullValue.PlaceNameServiceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < this.serviceList.size(); i++)
        {
            final PlaceNameService other = this.serviceList.get(i);
            if (placeNameService.getService().equals(other.getService()) && placeNameService.getDataset().equals(
                other.getDataset()))
            {
                if (replace)
                {
                    this.serviceList.set(i, placeNameService);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        this.serviceList.add(placeNameService);
        return true;
    }

    public PlaceNameServiceSet deepCopy()
    {
        PlaceNameServiceSet copy = new PlaceNameServiceSet();

        // Copy params
        copy.setValues(this);

        // Creates a deep copy of this.serviceList in copy.serviceList.
        for (int i = 0; i < this.serviceList.size(); i++)
        {
            copy.serviceList.add(i, this.serviceList.get(i).deepCopy());
        }

        copy.expiryTime = this.expiryTime;

        return copy;
    }

    public final int getServiceCount()
    {
        return this.serviceList.size();
    }

    public final PlaceNameService getService(int index)
    {
        return this.serviceList.get(index);
    }

    public final long getExpiryTime()
    {
        return this.expiryTime;
    }

    public final void setExpiryTime(long expiryTime)
    {
        this.expiryTime = expiryTime;
    }

    public List<PlaceNameService> getServices()
    {
        return serviceList;
    }

    public PlaceNameService getService(String name)
    {
        for (int i = 0; i < this.serviceList.size(); i++)
        {
            if (this.serviceList.get(i).getDataset().equalsIgnoreCase(name))
                return this.serviceList.get(i);
        }

        return null;
    }
}
