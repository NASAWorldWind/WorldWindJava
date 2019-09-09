/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.dds;

import gov.nasa.worldwind.util.Logging;

/**
 * Compressor for DXT2/DXT3 alpha and color blocks. This class is not thread safe. Unsynchronized access will result in
 * unpredictable behavior. Access to methods of this class must be synchronized by the caller.
 * <p>
 * Documentation on the DXT2/DXT3 format is available at http://msdn.microsoft.com/en-us/library/bb694531.aspx under
 * the name "BC2".
 *
 * @author dcollins
 * @version $Id: BlockDXT3Compressor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BlockDXT3Compressor
{
    // Implementation based on the NVidia Texture Tools
    // http://code.google.com/p/nvidia-texture-tools/

    protected BlockDXT1Compressor dxt1Compressor;

    /**
     * Creates a new DXT2/DXT3 block compressor.
     */
    public BlockDXT3Compressor()
    {
        this.dxt1Compressor = new BlockDXT1Compressor();
    }

    /**
     * Compress the 4x4 color block into a DXT2/DXT3 block using 16 4 bit alpha values, and four colors. This method
     * compresses the color block exactly as a DXT1 compressor, except that it guarantees that the DXT1 block will use
     * four colors.
     * <p>
     * Access to this method must be synchronized by the caller. This method is frequently invoked by the DXT
     * compressor, so in order to reduce garbage each instance of this class has unsynchronized properties that are
     * reused during each call.
     *
     * @param colorBlock the 4x4 color block to compress.
     * @param attributes attributes that will control the compression.
     * @param dxtBlock   the DXT2/DXT3 block that will receive the compressed data.
     * @throws IllegalArgumentException if either <code>colorBlock</code> or <code>dxtBlock</code> are null.
     */
    public void compressBlockDXT3(ColorBlock4x4 colorBlock, DXTCompressionAttributes attributes, BlockDXT3 dxtBlock)
    {
        if (colorBlock == null)
        {
            String message = Logging.getMessage("nullValue.ColorBlockIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dxtBlock == null)
        {
            String message = Logging.getMessage("nullValue.DXTBlockIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // The DXT3 color block is compressed exactly like the DXT1 color block, except that the four color palette is
        // always used, no matter the ordering of color0 and color1. At this stage we only consider color values,
        // not alpha.
        this.dxt1Compressor.compressBlockDXT1(colorBlock, attributes, dxtBlock.colorBlock);

        // The DXT3 alpha block can be compressed separately.
        this.compressBlockDXT3a(colorBlock, dxtBlock.alphaBlock);
    }

    protected void compressBlockDXT3a(ColorBlock4x4 colorBlock, AlphaBlockDXT3 dxtBlock)
    {
        dxtBlock.alphaValueMask = computeAlphaValueMask(colorBlock);
    }

    //**************************************************************//
    //********************  Alpha Block Assembly  ******************//
    //**************************************************************//

    protected static long computeAlphaValueMask(ColorBlock4x4 colorBlock)
    {
        // Alpha is encoded as 4 bit values. Each pair of values will be packed into one byte. The first value goes
        // in bits 0-4, and the second value goes in bits 5-8. The resultant 64 bit value is structured so that when
        // converted to little endian ordering, the alpha values will be in the correct order. Here's what the
        // structure looks like packed into Java's long, where the value aN represents the Nth alpha value in
        // hexadecimal notation.
        //
        //  | 63-56 | 55-48 | 47-40 | 39-32 | 31-24 | 23-16 | 15-8  | 7-0   |
        //  | aFaE  | aDaC  | aBaA  | a9a8  | a7a6  | a5a4  | a3a2  | a1a0  |

        long bitmask = 0L;

        for (int i = 0; i < 8; i++)
        {
            int a0 = 0xF & alpha4FromAlpha8(colorBlock.color[2 * i].a);
            int a1 = 0xF & alpha4FromAlpha8(colorBlock.color[2 * i + 1].a);
            long mask10 = (a1 << 4) | a0;
            bitmask |= (mask10 << (8 * i));
        }

        return bitmask;
    }

    //**************************************************************//
    //********************  Alpha Arithmetic  **********************//
    //**************************************************************//

    protected static int alpha4FromAlpha8(int alpha8)
    {
        // Quantizes an 8 bit alpha value into 4 bits. To reduce rounding error, this will compare the three nearest
        // 4 bit values and choose the closest one.

        int q0 = Math.max((alpha8 >> 4) - 1, 0);
        int q1 = (alpha8 >> 4);
        int q2 = Math.min((alpha8 >> 4) + 1, 0xF);

        q0 = (q0 << 4) | q0;
        q1 = (q1 << 4) | q1;
        q2 = (q2 << 4) | q2;

        int d0 = alphaDistanceSquared(q0, alpha8);
        int d1 = alphaDistanceSquared(q1, alpha8);
        int d2 = alphaDistanceSquared(q2, alpha8);

        if (d0 < d1 && d0 < d2)
        {
            return q0 >> 4;
        }
        if (d1 < d2)
        {
            return q1 >> 4;
        }
        return q2 >> 4;
    }

    protected static int alphaDistanceSquared(int a0, int a1)
    {
        return (a0 - a1) * (a0 - a1);
    }
}
