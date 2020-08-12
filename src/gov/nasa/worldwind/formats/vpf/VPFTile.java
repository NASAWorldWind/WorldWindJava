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
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: VPFTile.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFTile implements ExtentHolder
{
    private int id;
    private String name;
    private VPFBoundingBox bounds;

    public VPFTile(int id, String name, VPFBoundingBox bounds)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (bounds == null)
        {
            String message = Logging.getMessage("nullValue.BoundingBoxIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.id = id;
        this.name = name;
        this.bounds = bounds;
    }

    public int getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public VPFBoundingBox getBounds()
    {
        return this.bounds;
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Sector.computeBoundingCylinder(globe, verticalExaggeration, this.bounds.toSector());
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VPFTile vpfTile = (VPFTile) o;

        if (id != vpfTile.id)
            return false;
        if (bounds != null ? !bounds.equals(vpfTile.bounds) : vpfTile.bounds != null)
            return false;
        //noinspection RedundantIfStatement
        if (name != null ? !name.equals(vpfTile.name) : vpfTile.name != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (bounds != null ? bounds.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.id);
        sb.append(": ");
        sb.append(this.name);
        return sb.toString();
    }
}
