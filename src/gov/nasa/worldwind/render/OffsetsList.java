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

package gov.nasa.worldwind.render;

import java.util.*;

/**
 * Defines a structure to hold all the uv offsets for textures applied to a particular shape.
 *
 * @author ccrick
 * @version $Id: OffsetsList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OffsetsList
{
    protected Map<Integer, float[]> offsets;

    public OffsetsList()
    {
        offsets = new HashMap<Integer, float[]>();

        // set default values to zero offset
        float[] zeroOffset = {0.0f, 0.0f};
        for (int i = 0; i < 4; i++)
        {
            offsets.put(i, zeroOffset);
        }
    }

    public float[] getOffset(int index)
    {
        return offsets.get(index);
    }

    public void setOffset(int index, float uOffset, float vOffset)
    {
        float[] offsetPair = {uOffset, vOffset};
        offsets.put(index, offsetPair);
    }
}