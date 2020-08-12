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
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.globes.Globe;

/**
 * ExtentHolder provides an interface to query an object's enclosing volume in model coordinates.
 *
 * @author dcollins
 * @version $Id: ExtentHolder.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.geom.Extent
 */
public interface ExtentHolder
{
    /**
     * Returns the objects enclosing volume as an {@link gov.nasa.worldwind.geom.Extent} in model coordinates, given a
     * specified {@link gov.nasa.worldwind.globes.Globe} and vertical exaggeration (see {@link
     * gov.nasa.worldwind.SceneController#getVerticalExaggeration()}.
     *
     * @param globe                the Globe the object is related to.
     * @param verticalExaggeration the vertical exaggeration of the scene containing this object.
     *
     * @return the object's Extent in model coordinates.
     *
     * @throws IllegalArgumentException if the Globe is null.
     */
    Extent getExtent(Globe globe, double verticalExaggeration);
}
