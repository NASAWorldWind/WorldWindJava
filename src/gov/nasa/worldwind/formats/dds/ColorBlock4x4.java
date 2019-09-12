/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
