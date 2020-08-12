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

/**
 * Provides access to 4x4 blocks of pixel data from a <code>BufferedImage</code> via the
 * <code>ColorBlockExtractor</code> interface. This class is not thread safe. Unsynchronized access will result in
 * unpredictable behavior. Acces to methods of this class must be synchronized by the caller.
 *
 * @see java.awt.image.BufferedImage
 * 
 * @author dcollins
 * @version $Id: BasicColorBlockExtractor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicColorBlockExtractor implements ColorBlockExtractor
{
    protected int width;
    protected int height;
    protected java.awt.image.BufferedImage image;
    private int[] buffer;

    protected static int[] remainder =
    {
        0, 0, 0, 0,
        0, 1, 0, 1,
        0, 1, 2, 0,
        0, 1, 2, 3,
    };

    /**
     * Creates a <code>BasicColorBlockExtrator</code> which will draw its data from the <code>BufferedImage</code>.
     * The <code>BufferedImage</code> may be of any type, so long as a call to <code>image.getRGB()</code> will
     * succeed.
     *
     * @param image the image to draw data from.
     *
     * @throws IllegalArgumentException if <code>image</code> is null.
     */
    public BasicColorBlockExtractor(java.awt.image.BufferedImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.image = image;
        this.buffer = new int[16];
    }

    /**
     * Returns the image this <code>BasicColorBlockExtrator</code> will draw its data from.
     *
     * @return image data is drawn from.
     */
    public java.awt.image.BufferedImage getImage()
    {
        return this.image;
    }

    /**
     * Extracts a 4x4 block of pixel data at the specified coordinate <code>(x, y)</code>, and places the data in the
     * specified <code>colorBlock</code>. If the coordinate <code>(x, y)</code> with the image, but the entire 4x4
     * block is not, this will either truncate the block to fit the image, or copy nearby pixels to fill the block. If
     * the <code>attributes</code> specify that color components should be premultiplied by alpha, this extactor will
     * perform the premultiplication operation on the incoming colors.
     * <p>
     * Access to this method must be synchronized by the caller. This method is frequenty invoked by the DXT
     * compressor, so in order to reduce garbage each instance of this class has unsynchronized properties that are
     * reused during each call.
     *
     * @param attributes the DXT compression attributes which may affect how colors are accessed.
     * @param x horizontal coordinate origin to extract pixel data from.
     * @param y vertical coordainte origin to extract pixel data from.
     * @param colorBlock 4x4 block of pixel data that will receive the data.
     *
     * @throws IllegalArgumentException if either <code>attributes</code> or <code>colorBlock</code> is null.
     */
    public void extractColorBlock4x4(DXTCompressionAttributes attributes, int x, int y, ColorBlock4x4 colorBlock)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (colorBlock == null)
        {
            String message = Logging.getMessage("nullValue.ColorBlockIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Image blocks that are smaller than 4x4 are handled by repeating the image pixels that intersect the
        // requested block range.
        
        int bw = Math.min(this.width - x, 4);
        int bh = Math.min(this.height - y, 4);
        int bxOffset = 4 * (bw - 1);
        int byOffset = 4 * (bh - 1);
        int bx, by;
        int blockPos = 0;

        // Extracts color data from the image in INT_ARGB format. So each integer in the buffer is a tightly packed
        // 8888 ARGB int, where the color components are not considered to be premultiplied.
        this.image.getRGB(x, y, bw, bh, this.buffer, 0, 4);

        for (int j = 0; j < 4; j++)
        {
            by = remainder[byOffset + j];

            bx = remainder[bxOffset];
            int32ToColor32(this.buffer[bx + by * 4], colorBlock.color[blockPos++]);

            bx = remainder[bxOffset + 1];
            int32ToColor32(this.buffer[bx + by * 4], colorBlock.color[blockPos++]);

            bx = remainder[bxOffset + 2];
            int32ToColor32(this.buffer[bx + by * 4], colorBlock.color[blockPos++]);

            bx = remainder[bxOffset + 3];
            int32ToColor32(this.buffer[bx + by * 4], colorBlock.color[blockPos++]);
        }

        if (attributes.isPremultiplyAlpha())
        {
            for (int i = 0; i < 16; i++)
            {
                premultiplyAlpha(colorBlock.color[i]);
            }
        }
    }

    protected static void int32ToColor32(int int32, Color32 color)
    {
        // Unpack a 32 bit 8888 ARGB integer into the destination color. The components are assumed to be tightly
        // packed in the integer as follows:
        //
        //  31-24   | 23-16 | 15-8  | 0-7
        //  A       | R     | G     | B

        color.a = (0xFF & (int32 >> 24));
        color.r = (0xFF & (int32 >> 16));
        color.g = (0xFF & (int32 >> 8));
        color.b = (0xFF & (int32));
    }

    protected static void premultiplyAlpha(Color32 color)
    {
        color.r = div255(color.r * color.a);
        color.g = div255(color.g * color.a);
        color.b = div255(color.b * color.a);
    }

    private static int div255(int a)
    {
        return (a + (a >> 8) + 128) >> 8;
    }
}
