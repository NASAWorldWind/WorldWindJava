/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.dds;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Documentation on the DDS header format is available at http://msdn.microsoft.com/en-us/library/bb943982(VS.85).aspx
 *
 * @author dcollins
 * @version $Id: DDSHeader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DDSHeader
{
    protected final int size = DDSConstants.DDS_HEADER_SIZE;
    protected int flags;
    protected int width;
    protected int height;
    protected int linearSize;
    protected int depth;
    protected int mipMapCount;
    //protected int[] reserved1 = new int[11]; // Unused
    protected DDSPixelFormat pixelFormat;
    protected int caps;
    protected int caps2;
    protected int caps3;
    protected int caps4;
    //protected int reserved2; // Unused

    public DDSHeader()
    {
        this.pixelFormat = new DDSPixelFormat();
    }

    /**
     * Returns the size of the header structure in bytes. Will always return 124.
     *
     * @return header size in bytes.
     */
    public final int getSize()
    {
        return this.size;
    }

    public int getFlags()
    {
        return this.flags;
    }

    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    public int getWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getLinearSize()
    {
        return this.linearSize;
    }

    public void setLinearSize(int size)
    {
        this.linearSize = size;
    }

    public int getDepth()
    {
        return this.depth;
    }

    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    public int getMipMapCount()
    {
        return this.mipMapCount;
    }

    public void setMipMapCount(int mipMapCount)
    {
        this.mipMapCount = mipMapCount;
    }

    public DDSPixelFormat getPixelFormat()
    {
        return this.pixelFormat;
    }

    public void setPixelFormat(DDSPixelFormat pixelFormat)
    {
        if (pixelFormat == null)
        {
            String message = Logging.getMessage("nullValue.PixelFormatIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pixelFormat = pixelFormat;
    }

    public int getCaps()
    {
        return this.caps;
    }

    public void setCaps(int caps)
    {
        this.caps = caps;
    }

    public int getCaps2()
    {
        return this.caps2;
    }

    public void setCaps2(int caps)
    {
        this.caps2 = caps;
    }

    public int getCaps3()
    {
        return this.caps3;
    }

    public void setCaps3(int caps)
    {
        this.caps3 = caps;
    }

    public int getCaps4()
    {
        return this.caps4;
    }

    public void setCaps4(int caps)
    {
        this.caps4 = caps;
    }

    public static DDSHeader readFrom(Object source) throws Exception
    {
        boolean sourceIsInputStream = (null != source && source instanceof InputStream);

        InputStream inputStream = WWIO.openStream(source);
        ReadableByteChannel channel = Channels.newChannel(WWIO.getBufferedInputStream(inputStream));

        try
        {
            int size = DDSConstants.DDS_SIGNATURE_SIZE + DDSConstants.DDS_HEADER_SIZE;

            ByteBuffer buffer = ByteBuffer.allocate(size);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            WWIO.readChannelToBuffer(channel, buffer);
            return DDSHeader.readFrom(buffer);
        }
        finally
        {
            if (!sourceIsInputStream)
            {
                WWIO.closeStream(inputStream, ((null != source) ? source.toString() : "unknown"));
            }
        }
    }

    /**
     * Creates (reads) a DDSHeader from a ByteBuffer.
     *
     * @param buffer The buffer to read from
     * @return DDSHeader
     * @throws IllegalArgumentException if the ByteBuffer is null
     * @throws IOException              if the buffer length or content is invalid
     */
    public static DDSHeader readFrom(ByteBuffer buffer) throws IllegalArgumentException, IOException
    {
        if (null == buffer)
        {
            String message = Logging.getMessage("nullValue.BufferNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer.order() != ByteOrder.LITTLE_ENDIAN)
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        int ddsHeaderSize = DDSConstants.DDS_SIGNATURE_SIZE + DDSConstants.DDS_HEADER_SIZE;
        if (buffer.remaining() < ddsHeaderSize)
        {
            String reason = buffer.remaining() + " < " + ddsHeaderSize;
            String message = Logging.getMessage("generic.LengthIsInvalid", reason);
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        int signature = buffer.getInt();
        if (DDSConstants.MAGIC != signature)
        {
            String message = Logging.getMessage("generic.UnknownFileFormat", signature);
            Logging.logger().fine(message);
            throw new IOException(message);
        }

        int dwSize = buffer.getInt();
        if (dwSize != DDSConstants.DDS_HEADER_SIZE)
        {
            String message = Logging.getMessage("generic.UnknownContentType", dwSize);
            Logging.logger().fine(message);
            throw new IOException(message);
        }

        DDSHeader ddsHeader = new DDSHeader();

        ddsHeader.setFlags(buffer.getInt());
        ddsHeader.setHeight(buffer.getInt());
        ddsHeader.setWidth(buffer.getInt());
        ddsHeader.setLinearSize(buffer.getInt());
        ddsHeader.setDepth(buffer.getInt());
        ddsHeader.setMipMapCount(buffer.getInt());

        // skip 11 reserved integers (DWORD) ( 4 * 11 )
        buffer.position(DDSConstants.DDS_PIXEL_FORMAT_OFFSET);

        DDSPixelFormat pixelFormat = new DDSPixelFormat();

        dwSize = buffer.getInt();
        if (dwSize != DDSConstants.DDS_PIXEL_FORMAT_SIZE)
        {
            String message = Logging.getMessage("generic.UnknownContentType", dwSize);
            Logging.logger().fine(message);
            throw new IOException(message);
        }

        pixelFormat.setFlags(buffer.getInt());
        pixelFormat.setFourCC(buffer.getInt());
        pixelFormat.setRGBBitCount(buffer.getInt());
        pixelFormat.setRBitMask(buffer.getInt());
        pixelFormat.setGBitMask(buffer.getInt());
        pixelFormat.setBBitMask(buffer.getInt());
        pixelFormat.setABitMask(buffer.getInt());

        ddsHeader.setPixelFormat(pixelFormat);

        ddsHeader.setCaps(buffer.getInt());
        ddsHeader.setCaps2(buffer.getInt());
        ddsHeader.setCaps3(buffer.getInt());
        ddsHeader.setCaps4(buffer.getInt());
        buffer.getInt();

        return ddsHeader;
    }
}
