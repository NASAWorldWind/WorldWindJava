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
 * <code>AlphaBlockDXT3</code> is a data structure representing the compressed alpha values in a single DXT2/DXT3 block.
 * The DXT2/DXT3 alpha block contains alpha values for 4x4 pixels, each quantized to fit into 4 bits. The alpha values
 * are tightly packed into 64 bits in the DXT file as follows, where the value aN represents the Nth alpha value in
 * hexadecimal notation:
 * <p>
 * | 63-56 | 55-48 | 47-40 | 39-32 | 31-24 | 23-16 | 15-8  | 7-0    |
 * | aFaE  | aDaC  | aBaA  | a9a8  | a7a6  | a5a4  | a3a2  | a1a0   |
 *
 * @author dcollins
 * @version $Id: AlphaBlockDXT3.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AlphaBlockDXT3
{
    /**
     * The 4x4 block of 4 bit alpha values stored as a 64 bit long number.
     */
    public long alphaValueMask;

    /**
     * Creates a new DXT2/DXT3 alpha block with all alpha values set to 0.
     */
    public AlphaBlockDXT3()
    {
    }

    public AlphaBlockDXT3(long alphaValueMask)
    {
        this.alphaValueMask = alphaValueMask;
    }

    /**
     * Returns the 4x4 block of 4 bit alpha values as a 64 bit number.
     *
     * @return 4x4 block of 4 bit alpha values.
     */
    public long getAlphaValueMask()
    {
        return this.alphaValueMask;
    }

    /**
     * Sets the 4x4 block of 4 bit alpha values as a 64 bit number.
     *
     * @param valueMask 4x4 block of 4 bit alpha values.
     */
    public void setAlphaValueMask(long valueMask)
    {
        this.alphaValueMask = valueMask;
    }
}
