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
 * @version $Id: DXT1Decompressor.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class DXT1Decompressor implements DXTDecompressor
{
    public static final int DXT1_BLOCK_SIZE = 4;

    public DXT1Decompressor()
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

        return this.decodeDxt1Buffer(buffer, width, height);
    }

    protected BufferedImage decodeDxt1Buffer(ByteBuffer buffer, int width, int height)
            throws IllegalArgumentException, IOException
    {
        if (null == buffer)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        if (width < DXT1_BLOCK_SIZE || height < DXT1_BLOCK_SIZE)
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

            int numTilesWide = width / DXT1_BLOCK_SIZE;
            int numTilesHigh = height / DXT1_BLOCK_SIZE;

            // 8 bit per color RGB packed in to an integer as r8g8b8
            int[] pixels = new int[DXT1_BLOCK_SIZE * width];

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int row = 0; row < numTilesHigh; row++)
            {
                for (int col = 0; col < numTilesWide; col++)
                {
                    short minColor = buffer.getShort();
                    short maxColor = buffer.getShort();
                    int colorIndexMask = buffer.getInt();

                    Color24[] lookupTable = Color24.expandLookupTable(minColor, maxColor);

                    for (int k = DXT1_BLOCK_SIZE * DXT1_BLOCK_SIZE - 1; k >= 0; k--)
                    {
                        int h = k / DXT1_BLOCK_SIZE, w = k % DXT1_BLOCK_SIZE;
                        int pixelIndex = h * width + (col * DXT1_BLOCK_SIZE + w);

                        int colorIndex = (colorIndexMask >>> k * 2) & 0x03;

                        pixels[pixelIndex] = lookupTable[colorIndex].getPixel888();
                    }
                }

                result.setRGB(0, row * DXT1_BLOCK_SIZE, width, DXT1_BLOCK_SIZE, pixels, 0, width);
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
