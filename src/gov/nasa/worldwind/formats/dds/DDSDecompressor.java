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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.data.DataRaster;
import gov.nasa.worldwind.data.MipMappedBufferedImageRaster;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWMath;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * @author Lado Garakanidze
 * @version $Id: DDSDecompressor.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class DDSDecompressor
{
    public DDSDecompressor()
    {

    }

    /**
     * Reconstructs image raster from a DDS source. The source type may be one of the following: <ul><li>{@link java.net.URL}</li> <li>{@link
     * java.net.URI}</li> <li>{@link java.io.File}</li> <li>{@link String} containing a valid URL description, a valid
     * URI description, or a valid path to a local file.</li> </ul>
     *
     * @param source the source to convert to local file path.
     * @param params The AVList is a required parameter, Cannot be null. Requires AVK.Sector to be present.
     * @return MipMappedBufferedImageRaster if the DDS source contains mipmaps, otherwise returns a BufferedImageRaster
     * @throws Exception when source or params is null
     */
    public DataRaster decompress(Object source, AVList params) throws Exception
    {
        return this.doDecompress(source, params);
    }

    protected DataRaster doDecompress(Object source, AVList params) throws Exception
    {
        if (null == params || !params.hasKey(AVKey.SECTOR))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        File file = WWIO.getFileForLocalAddress(source);
        if (null == file)
        {
            String message = Logging.getMessage("generic.UnrecognizedSourceType", source.getClass().getName());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", file.getAbsolutePath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }

        if (!file.canRead())
        {
            String message = Logging.getMessage("generic.FileNoReadPermission", file.getAbsolutePath());
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        RandomAccessFile raf = null;
        FileChannel channel = null;
        DataRaster raster = null;

        try
        {
            raf = new RandomAccessFile(file, "r");
            channel = raf.getChannel();

            java.nio.MappedByteBuffer buffer = this.mapFile(channel, 0, channel.size());

            buffer.position(0);
            DDSHeader header = DDSHeader.readFrom(source);

            int width = header.getWidth();
            int height = header.getHeight();

            if (!WWMath.isPowerOfTwo(width) || !WWMath.isPowerOfTwo(height))
            {
                String message = Logging.getMessage("generic.InvalidImageSize", width, height);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            int mipMapCount = header.getMipMapCount();
//            int ddsFlags = header.getFlags();

            DDSPixelFormat pixelFormat = header.getPixelFormat();
            if (null == pixelFormat)
            {
                String reason = Logging.getMessage("generic.MissingRequiredParameter", "DDSD_PIXELFORMAT");
                String message = Logging.getMessage("generic.InvalidImageFormat", reason);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            DXTDecompressor decompressor = null;

            int dxtFormat = pixelFormat.getFourCC();
            if (dxtFormat == DDSConstants.D3DFMT_DXT3)
            {
                decompressor = new DXT3Decompressor();
            }
            else if (dxtFormat == DDSConstants.D3DFMT_DXT1)
            {
                decompressor = new DXT1Decompressor();
            }

            if (null == decompressor)
            {
                String message = Logging.getMessage("generic.UnsupportedCodec", dxtFormat);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            Sector sector = (Sector) params.getValue(AVKey.SECTOR);
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);

            if (mipMapCount == 0)
            {
                // read max resolution raster
                buffer.position(DDSConstants.DDS_DATA_OFFSET);
                BufferedImage image = decompressor.decompress(buffer, header.getWidth(), header.getHeight());
                raster = new BufferedImageRaster(sector, image, params);
            }
            else if (mipMapCount > 0)
            {
                ArrayList<BufferedImage> list = new ArrayList<BufferedImage>();

                int mmLength = header.getLinearSize();
                int mmOffset = DDSConstants.DDS_DATA_OFFSET;

                for (int i = 0; i < mipMapCount; i++)
                {
                    int zoomOut = (int) Math.pow(2d, (double) i);

                    int mmWidth = header.getWidth() / zoomOut;
                    int mmHeight = header.getHeight() / zoomOut;

                    if (mmWidth < 4 || mmHeight < 4)
                    {
                        break;
                    }

                    buffer.position(mmOffset);
                    BufferedImage image = decompressor.decompress(buffer, mmWidth, mmHeight);
                    list.add(image);

                    mmOffset += mmLength;
                    mmLength /= 4;
                }

                BufferedImage[] images = new BufferedImage[list.size()];
                images = (BufferedImage[]) list.toArray(images);

                raster = new MipMappedBufferedImageRaster(sector, images);
            }

            return raster;
        }
        finally
        {
            String name = (null != file) ? file.getAbsolutePath() : ((null != source) ? source.toString() : "unknown");
            WWIO.closeStream(channel, name);
            WWIO.closeStream(raf, name);
        }
    }

    protected java.nio.MappedByteBuffer mapFile(FileChannel channel, long offset, long length) throws Exception
    {
        if (null == channel || !channel.isOpen())
        {
            String message = Logging.getMessage("nullValue.ChannelIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        if (channel.size() < (offset + length))
        {
            String reason = channel.size() + " < " + (offset + length);
            String message = Logging.getMessage("generic.LengthIsInvalid", reason);
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        return channel.map(FileChannel.MapMode.READ_ONLY, offset, length);
    }
}
