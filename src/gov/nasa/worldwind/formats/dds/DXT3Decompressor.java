/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.dds;

import gov.nasa.worldwind.util.Logging;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: DXT3Decompressor.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class DXT3Decompressor implements DXTDecompressor
{
    public static final int DXT3_BLOCK_SIZE = 4;

    public DXT3Decompressor()
    {

    }

    public BufferedImage decompress(ByteBuffer buffer, int width, int height) throws IOException, IllegalArgumentException
    {
        if (null == buffer)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        if (width <= 0 || height <= 0)
        {
            String message = Logging.getMessage("generic.InvalidImageSize", width, height);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        // TODO check buffer's remaining with image size

        return this.decodeDxt3Buffer(buffer, width, height);
    }

    protected BufferedImage decodeDxt3Buffer(ByteBuffer buffer, int width, int height)
            throws IllegalArgumentException, IOException
    {
        if (null == buffer)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        if (width < DXT3_BLOCK_SIZE || height < DXT3_BLOCK_SIZE)
        {
            String message = Logging.getMessage("generic.InvalidImageSize", width, height);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (buffer.order() != ByteOrder.LITTLE_ENDIAN)
            {
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }

            int numTilesWide = width / DXT3_BLOCK_SIZE;
            int numTilesHigh = height / DXT3_BLOCK_SIZE;

            // 8 bit per color ARGB packed in to an integer as a8r8g8b8
            int[] pixels = new int[DXT3_BLOCK_SIZE * width];

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

            for (int row = 0; row < numTilesHigh; row++)
            {
                for (int col = 0; col < numTilesWide; col++)
                {
                    long alphaData = buffer.getLong();
                    short minColor = buffer.getShort();
                    short maxColor = buffer.getShort();
                    int colorIndexMask = buffer.getInt();

                    Color24[] lookupTable = Color24.expandLookupTable(minColor, maxColor);

                    for (int k = DXT3_BLOCK_SIZE * DXT3_BLOCK_SIZE - 1; k >= 0; k--)
                    {
                        int alpha = (int) (alphaData >>> (k * 4)) & 0xF; // Alphas are just 4 bits per pixel
                        alpha <<= 4;

                        int colorIndex = (colorIndexMask >>> k * 2) & 0x03;

                        // No need to multiply alpha, it is already pre-multiplied
//                      Color24 color = Color24.multiplyAlpha(lookupTable[colorIndex], alpha );

                        Color24 color = lookupTable[colorIndex];
                        int pixel8888 = (alpha << 24) | color.getPixel888();

                        int h = k / DXT3_BLOCK_SIZE, w = k % DXT3_BLOCK_SIZE;
                        int pixelIndex = h * width + (col * DXT3_BLOCK_SIZE + w);

                        pixels[pixelIndex] = pixel8888;
                    }
                }

                result.setRGB(0, row * DXT3_BLOCK_SIZE, width, DXT3_BLOCK_SIZE, pixels, 0, width);
            }
            return result;
        }
        catch (Throwable t)
        {
            String message = t.getMessage();
            message = (null == message) ? t.getCause().getMessage() : message;
            Logging.logger().log(Level.FINEST, message, t);
        }
        return null;
    }
}
