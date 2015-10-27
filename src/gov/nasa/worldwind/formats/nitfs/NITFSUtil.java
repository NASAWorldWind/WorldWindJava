/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.nitfs;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Lado Garakanidze
 * @version $Id: NITFSUtil.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NITFSUtil
{
    public static String getString(java.nio.ByteBuffer buffer, int offset, int len)
    {
        String s = "";
        if (null != buffer && buffer.capacity() >= offset + len)
        {
            byte[] dest = new byte[len];
            buffer.position(offset);
            buffer.get(dest, 0, len);
            s = new String(dest).trim();
        }
        return s;
    }

    public static String getString(java.nio.ByteBuffer buffer, int len)
    {
        String s = "";
        if (null != buffer && buffer.remaining() >= len)
        {
            byte[] dest = new byte[len];
            buffer.get(dest, 0, len);
            s = new String(dest).trim();
        }
        return s;
    }

    public static int getNumeric(java.nio.ByteBuffer buffer, int len)
    {
        String s = "";
        if (null != buffer && buffer.remaining() >= len)
        {
            byte[] dest = new byte[len];
            buffer.get(dest, 0, len);
            s = new String(dest);
        }
        return Integer.parseInt(s);
    }

    public static short getShortNumeric(java.nio.ByteBuffer buffer, int len)
    {
        String s = "";
        if (null != buffer && buffer.remaining() >= len)
        {
            byte[] dest = new byte[len];
            buffer.get(dest, 0, len);
            s = new String(dest);
        }
        return (short) (0xFFFF & Integer.parseInt(s));
    }

    public static boolean getBoolean(java.nio.ByteBuffer buffer)
    {
        return !((byte) 0 == buffer.get()); // 0 = false, non-zero = true
    }

    public static short getByteAsShort(java.nio.ByteBuffer buffer)
    {
        return (short) (0xFF & buffer.get());
    }

    public static int getUShort(java.nio.ByteBuffer buffer)
    {
        return 0xFFFF & buffer.getShort();
    }

    public static long getUInt(java.nio.ByteBuffer buffer)
    {
        return 0xFFFFFFFFL & (long) buffer.getInt();
    }

    public static String getBitString(java.nio.ByteBuffer buffer, int lenBits)
    {
        String s = "";
        int len = (int) Math.ceil(lenBits / (double) Byte.SIZE);
        if (null != buffer && buffer.remaining() >= len)
        {
            byte[] dest = new byte[len];
            buffer.get(dest, 0, len);

            char[] bits = new char[lenBits];
            for (int i = 0; i < lenBits; i++)
            {
                int mask = 0x1 << (Byte.SIZE - (i % Byte.SIZE) - 1);
                // U+0030 : unicode zero
                // U+0031 : unicode one
                bits[i] = (mask & dest[i / Byte.SIZE]) == 0 ? '\u0030' : '\u0031';
            }
            s = new String(bits);
        }
        return s;
    }

    private static final int PAGE_SIZE = 4096;


    public static java.nio.ByteBuffer readEntireFile(java.io.File file) throws java.io.IOException
    {
        return readFileToBuffer(file);
//        return memoryMapFile(file);
//        return readFile(file);
    }

    private static java.nio.ByteBuffer readFileToBuffer(java.io.File file) throws IOException
    {
        FileInputStream is = new FileInputStream(file);
        try
        {
            FileChannel fc = is.getChannel();
            java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate((int) fc.size());
            for (int count = 0; count >= 0 && buffer.hasRemaining();)
            {
                count = fc.read(buffer);
            }
            buffer.flip();
            return buffer;
        }
        finally
        {
            is.close();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static java.nio.ByteBuffer readFile(java.io.File file) throws java.io.IOException
    {
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(PAGE_SIZE);
        java.nio.channels.ReadableByteChannel channel = java.nio.channels.Channels.newChannel(fis);

        int count = 0;
        while (count >= 0)
        {
            count = channel.read(buffer);
            if (count > 0 && !buffer.hasRemaining())
            {
                java.nio.ByteBuffer biggerBuffer = java.nio.ByteBuffer.allocate(buffer.limit() + PAGE_SIZE);
                biggerBuffer.put((java.nio.ByteBuffer) buffer.rewind());
                buffer = biggerBuffer;
            }
        }

        if (buffer != null)
            buffer.flip();

        return buffer;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static java.nio.ByteBuffer memoryMapFile(java.io.File file) throws IOException
    {
        FileChannel roChannel = new RandomAccessFile(file, "r").getChannel();
        long fileSize = roChannel.size();
        MappedByteBuffer mapFile = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
        if (!mapFile.isLoaded())
            mapFile.load();
        roChannel.close();
        return mapFile;
    }
}
