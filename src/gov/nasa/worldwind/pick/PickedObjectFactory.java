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

/**
 * PickedObjectFactory provides an interface for lazily creating PickedObject instances.
 * <p>
 * This interface is used by PickSupport to associate a collection of picked objects with a range of pick colors.
 * PickSupport uses this factory to delay PickedObject construction until a matching pick color is identified. This
 * eliminates the overhead of creating and managing a large collection of PickedObject instances when only a few may
 * actually be picked.
 *
 * @author dcollins
 * @version $Id: PickedObjectFactory.java 2281 2014-08-29 23:08:04Z dcollins $
 */
public interface PickedObjectFactory
{
    /**
     * Create a picked object from the specified pick color code.
     *
     * @param colorCode the pick color code to associate with the picked object.
     *
     * @return the new picked object.
     */
    PickedObject createPickedObject(int colorCode);
}
