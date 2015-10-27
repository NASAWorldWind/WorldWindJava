/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.dds;

/**
 * <code>BlockDXT1</code> is a data structure representing the compressed color values in a single 64 bit DXT1 block.
 * The DXT1 block contains two explicit 16 bit colors (quantized as RGB 565), which define one or two additional
 * implicit colors. If the first color is greater than the second, then two additional colors are defined for a total
 * of four. The two colors are defined as color2=(2*color0 + 1*color1)/3, and color3=(1*color0 + 2*color1)/3. If the
 * first color is less than the second color, then one additional color is defined for a total of three colors, and the
 * fourth color is interpreted as transparent black. Finally, the block contains 4x4 2 bit indices into the array of
 * four colors (one of which may be transparent black).
 * <p/>
 * From http://msdn.microsoft.com/en-us/library/bb204843(VS.85).aspx:
 * If 64-bit blocks - that is, format DXT1 - are used for the texture, it is possible to mix the opaque and 1-bit alpha
 * formats on a per-block basis within the same texture. In other words, the comparison of the unsigned integer
 * magnitude of color_0 and color_1 is performed uniquely for each block of 16 texels.
 *
 * @author dcollins
 * @version $Id: BlockDXT1.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BlockDXT1
{
    /**
     * The first color stored as a 16 bit 565 RGB color.
     */
    public int color0;
    /**
     * The second color stored as a 16 bit 565 RGB color.
     */
    public int color1;
    /**
     * The 4x4 block of 2 bit indices stored as a 64 bit long number.
     */
    public long colorIndexMask;

    /**
     * Creates a new DXT1 color block with colors and indices set to 0.
     */
    public BlockDXT1()
    {
    }

    public BlockDXT1(int color0, int color1, long colorIndexMask)
    {
        this.color0 = color0;
        this.color1 = color1;
        this.colorIndexMask = colorIndexMask;
    }

    /**
     * Returns the first color as a 16 bit 565 RGB color.
     *
     * @return 16 bit 565 RGB color.
     */
    public int getColor0()
    {
        return this.color0;
    }

    /**
     * Sets the first color as a 16 bit 565 RGB color.
     *
     * @param color0 16 bit 565 RGB color.
     */
    public void setColor0(int color0)
    {
        this.color0 = color0;
    }

    /**
     * Returns the second color as a 16 bit 565 RGB color.
     *
     * @return 16 bit 565 RGB color.
     */
    public int getColor1()
    {
        return this.color1;
    }

    /**
     * Sets the second color as a 16 bit 565 RGB color.
     *
     * @param color1 16 bit 565 RGB color.
     */
    public void setColor1(int color1)
    {
        this.color1 = color1;
    }

    /**
     * Returns the 4x4 block of 2 bit indices as a 64 bit number.
     *
     * @return 4x4 block of 2 bit indices.
     */
    public long getColorIndexMask()
    {
        return this.colorIndexMask;
    }

    /**
     * Sets the 4x4 block of 2 bit indices as a 64 bit number.
     *
     * @param indexMask 4x4 block of 2 bit indices.
     */
    public void setColorIndexMask(long indexMask)
    {
        this.colorIndexMask = indexMask;
    }
}
