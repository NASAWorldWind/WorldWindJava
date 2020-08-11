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
