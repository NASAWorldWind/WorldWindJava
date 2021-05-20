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

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: VPFFeature.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFFeature extends AVListImpl
{
    protected VPFFeatureClass featureClass;
    protected int id;
    protected VPFBoundingBox bounds;
    private int[] primitiveIds;

    public VPFFeature(VPFFeatureClass featureClass, int id, VPFBoundingBox bounds, int[] primitiveIds)
    {
        if (featureClass == null)
        {
            String message = Logging.getMessage("nullValue.FeatureClassIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (bounds == null)
        {
            String message = Logging.getMessage("nullValue.BoundingBoxIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.featureClass = featureClass;
        this.id = id;
        this.bounds = bounds;
        this.primitiveIds = primitiveIds;
    }

    public VPFFeatureClass getFeatureClass()
    {
        return this.featureClass;
    }

    public VPFFeatureType getType()
    {
        return this.featureClass.getType();
    }

    public int getId()
    {
        return this.id;
    }

    public VPFBoundingBox getBounds()
    {
        return this.bounds;
    }

    public int[] getPrimitiveIds()
    {
        return this.primitiveIds;
    }
}
