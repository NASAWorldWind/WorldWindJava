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
 * <code>BlockDXT3</code> is a data structure representing the compressed alpha and color values in a single DXT2/DXT3
 * block. The DXT3 block contains a 64 bit alpha block, and a 64 bit color block, stored here as the properties
 * <code>alphaBlock</code> and <code>colorBlock</code>. The 64 bit alpha block contains 4x4 alpha values quantized
 * into 4 bits. The 64 bit color block is formatted exactly like the DXT1 color block, except that the color block
 * always represents four colors, regardless of the color ordering in the DXT1 block.
 *
 * @author dcollins
 * @version $Id: BlockDXT3.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see AlphaBlockDXT3
 * @see BlockDXT1
 */
public class BlockDXT3
{
    /**
     * The DXT2/DXT3 alpha block.
     */
    public AlphaBlockDXT3 alphaBlock;
    /**
     * The DXT1 color block.
     */
    public BlockDXT1 colorBlock;

    /**
     * Creates a new DXT2/DXT3 alpha block with all alpha and color values set to 0.
     */
    public BlockDXT3()
    {
        this.alphaBlock = new AlphaBlockDXT3();
        this.colorBlock = new BlockDXT1();
    }

    public BlockDXT3(long alphaValueMask, int color0, int color1, long colorIndexMask)
    {
        this.alphaBlock = new AlphaBlockDXT3(alphaValueMask);
        this.colorBlock = new BlockDXT1(color0, color1, colorIndexMask);
    }

    /**
     * Returns the DXT2/DXT3 alpha block.
     *
     * @return DXT2/DXT3 alpha block.
     */
    public AlphaBlockDXT3 getAlphaBlock()
    {
        return this.alphaBlock;
    }

    /**
     * Sets the DXT2/DXT3 alpha block.
     *
     * @param alphaBlock DXT2/DXT3 alpha block.
     */
    public void setAlphaBlock(AlphaBlockDXT3 alphaBlock)
    {
        this.alphaBlock = alphaBlock;
    }

    /**
     * Returns the DXT1 color block.
     *
     * @return DXT1 color block.
     */
    public BlockDXT1 getColorBlock()
    {
        return this.colorBlock;
    }

    /**
     * Sets the DXT1 color block.
     *
     * @param colorBlock DXT1 color block.
     */
    public void setColorBlock(BlockDXT1 colorBlock)
    {
        this.colorBlock = colorBlock;
    }
}
