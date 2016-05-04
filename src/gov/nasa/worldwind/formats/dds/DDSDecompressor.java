/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.dds;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.data.DataRaster;
import gov.nasa.worldwind.data.MipMappedBufferedImageRaster;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

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
            throw new WWRuntimeException();
        }

        File file = WWIO.getFileForLocalAddress(source);
        if (null == file)
        {
            throw new IllegalArgumentException();
        }

        if (!file.exists())
        {
            throw new FileNotFoundException();
        }

        if (!file.canRead())
        {
            throw new IOException();
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
                    throw new WWRuntimeException();
            }

            int mipMapCount = header.getMipMapCount();
//            int ddsFlags = header.getFlags();

            DDSPixelFormat pixelFormat = header.getPixelFormat();
            if (null == pixelFormat)
            {
                String reason = null;
                    throw new WWRuntimeException();
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
                    throw new WWRuntimeException();
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
            throw new IllegalArgumentException();
        }

        if (channel.size() < (offset + length))
        {
            String reason = channel.size() + " < " + (offset + length);
            throw new IOException();
        }

        return channel.map(FileChannel.MapMode.READ_ONLY, offset, length);
    }
}
