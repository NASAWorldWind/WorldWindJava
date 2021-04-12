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
package gov.nasa.worldwind.formats.dds;

/**
 * Uncompressed 4x4 color block.
 *
 * @author dcollins
 * @version $Id: ColorBlock4x4.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ColorBlock4x4
{
    /**
     * The 4x4 color values stored as an array of length 16. This property is publicly exposed, so its contents are
     * mutable. It is declared final to prevent a caller form reassigning the array reference.
     */
    public final Color32[] color = new Color32[16];

    /**
     * Creates a 4x4 color block with the color values initialized to non-null references.
     * Initially all color values are set to 0.
     */
    public ColorBlock4x4()
    {
        for (int i = 0; i < 16; i++)
        {
            this.color[i] = new Color32();
        }
    }

    /**
     * Returns the color value at the specified <code>index</code>.
     *
     * @param index the color index to return.
     * @return color value at the <code>index</code>.
     */
    public Color32 getColor(int index)
    {
        return this.color[index];
    }

    /**
     * Sets the color value at the specified <code>index</code>.
     *
     * @param index the color index to set.
     * @param color new color value at the specified <code>index</code>.
     */
    public void setColor(int index, Color32 color)
    {
        this.color[index] = color;
    }
}
