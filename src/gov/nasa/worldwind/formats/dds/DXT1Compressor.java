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
 * @author dcollins
 * @version $Id: DXT1Compressor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DXT1Compressor implements DXTCompressor
{
    public DXT1Compressor()
    {
    }

    public int getDXTFormat()
    {
        return DDSConstants.D3DFMT_DXT1;
    }

    public int getCompressedSize(java.awt.image.BufferedImage image, DXTCompressionAttributes attributes)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: comment, provide documentation reference

        int width = Math.max(image.getWidth(), 4);
        int height = Math.max(image.getHeight(), 4);

        return (width * height) / 2;
    }

    public void compressImage(java.awt.image.BufferedImage image, DXTCompressionAttributes attributes,
        java.nio.ByteBuffer buffer)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If it is determined that the image and block have no alpha component, then we compress with DXT1 using a
        // four color palette. Otherwise, we use the three color palette (with the fourth color as transparent black).

        ColorBlock4x4 colorBlock = new ColorBlock4x4();
        ColorBlockExtractor colorBlockExtractor = this.getColorBlockExtractor(image);

        BlockDXT1 dxt1Block = new BlockDXT1();
        BlockDXT1Compressor dxt1Compressor = new BlockDXT1Compressor();

        int width = image.getWidth();
        int height = image.getHeight();

        boolean imageHasAlpha = image.getColorModel().hasAlpha();
        boolean enableAlpha = attributes.isEnableDXT1Alpha();
        int alphaThreshold = attributes.getDXT1AlphaThreshold();

        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                colorBlockExtractor.extractColorBlock4x4(attributes, i, j, colorBlock);

                if (enableAlpha && imageHasAlpha && blockHasDXT1Alpha(colorBlock, alphaThreshold))
                {
                    dxt1Compressor.compressBlockDXT1a(colorBlock, attributes, dxt1Block);
                }
                else
                {
                    dxt1Compressor.compressBlockDXT1(colorBlock, attributes, dxt1Block);
                }

                buffer.putShort((short) dxt1Block.color0);
                buffer.putShort((short) dxt1Block.color1);
                buffer.putInt((int) dxt1Block.colorIndexMask);
            }
        }
    }

    protected boolean blockHasDXT1Alpha(ColorBlock4x4 colorBlock, int alphaThreshold)
    {
        // DXT1 provides support for binary alpha. Therefore we determine treat a color block as needing alpha support
        // if any of the alpha values are less than a certain threshold.

        for (int i = 0; i < 16; i++)
        {
            if (colorBlock.color[i].a < alphaThreshold)
            {
                return true;
            }
        }

        return false;
    }

    protected ColorBlockExtractor getColorBlockExtractor(java.awt.image.BufferedImage image)
    {
        return new BasicColorBlockExtractor(image);
    }
}
