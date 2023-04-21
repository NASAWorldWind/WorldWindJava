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
 * @version $Id: DXT3Compressor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DXT3Compressor implements DXTCompressor
{
    public DXT3Compressor()
    {
    }

    public int getDXTFormat()
    {
        return DDSConstants.D3DFMT_DXT3;
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
        
        return (width * height);
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

        ColorBlock4x4 colorBlock = new ColorBlock4x4();
        ColorBlockExtractor colorBlockExtractor = this.getColorBlockExtractor(image);

        BlockDXT3 dxt3Block = new BlockDXT3();
        BlockDXT3Compressor dxt3Compressor = new BlockDXT3Compressor();

        int width = image.getWidth();
        int height = image.getHeight();

        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                colorBlockExtractor.extractColorBlock4x4(attributes, i, j, colorBlock);
                dxt3Compressor.compressBlockDXT3(colorBlock, attributes, dxt3Block);

                AlphaBlockDXT3 dxtAlphaBlock = dxt3Block.getAlphaBlock();
                buffer.putLong(dxtAlphaBlock.alphaValueMask);

                BlockDXT1 dxtColorBlock = dxt3Block.getColorBlock();
                buffer.putShort((short) dxtColorBlock.color0);
                buffer.putShort((short) dxtColorBlock.color1);
                buffer.putInt((int) dxtColorBlock.colorIndexMask);
            }
        }
    }

    protected ColorBlockExtractor getColorBlockExtractor(java.awt.image.BufferedImage image)
    {
        return new BasicColorBlockExtractor(image);
    }
}
