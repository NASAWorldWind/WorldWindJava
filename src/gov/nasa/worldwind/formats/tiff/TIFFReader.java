/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.tiff;

import gov.nasa.worldwind.util.Logging;

import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;

/**
 * This is a package private class that contains methods of reading TIFF structures
 *
 *
 * @author Lado Garakanidze
 * @version $Id: TIFFReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class TIFFReader
{
    private static final int CLEAR_CODE = 256;
    private static final int EOI_CODE = 257;

    private static final int DOUBLE_SIZEOF = Double.SIZE / Byte.SIZE;
    private static final int FLOAT_SIZEOF = Float.SIZE / Byte.SIZE;
    private static final int INTEGER_SIZEOF = Integer.SIZE / Byte.SIZE;
    private static final int SHORT_SIZEOF = Short.SIZE / Byte.SIZE;


    private FileChannel theChannel;
    private ByteOrder   tiffFileOrder;

    public TIFFReader( FileChannel fileChannel, ByteOrder byteOrder )
    {
        this.theChannel = fileChannel;
        this.tiffFileOrder = byteOrder;
    }

    public TIFFReader( FileChannel fileChannel )
    {
        this( fileChannel,ByteOrder.BIG_ENDIAN );
    }

    public void setByteOrder(ByteOrder byteOrder)
    {
        this.tiffFileOrder = byteOrder;
    }

    public ByteOrder getByteOrder()
    {
        return this.tiffFileOrder;
    }


   /*
    *
    *
    */
    public byte[] readLZWCompressed(int width, int height, long offset, int samplesPerPixel,
        boolean differencing, long[] stripOffsets, long[] stripCounts)
        throws IOException
    {
        this.theChannel.position(offset);
        byte[] pixels = new byte[width * height * samplesPerPixel];
        int base = 0;
        for (int i = 0; i < stripOffsets.length; i++)
        {
            if (i > 0)
            {
                long skip = stripOffsets[i] - stripOffsets[i - 1] - stripCounts[i - 1];
                if (skip > 0)
                {
                    //in.skip(skip);
                    this.theChannel.position(this.theChannel.position() + skip);
                }
            }
            byte[] byteArray = new byte[(int) stripCounts[i]];
            ByteBuffer bBuffer = ByteBuffer.wrap(byteArray);
            int read = 0, left = byteArray.length;
            while (left > 0)
            {
                long r = this.theChannel.read(bBuffer);
                if (r == -1)
                {
                    break;
                }
                read += r;
                left -= r;
            }
            byteArray = lzwUncompress(byteArray, (width * samplesPerPixel));
            if (differencing)
            {
                for (int b = 0; b < byteArray.length; b++)
                {
                    if (b / samplesPerPixel % width == 0)
                        continue;
                    byteArray[b] += byteArray[b - samplesPerPixel];
                }
            }
            int k = 0;
            int bytesToRead = byteArray.length;
            bytesToRead = bytesToRead - (bytesToRead % width);
            int pmax = base + bytesToRead;
            if (pmax > width * height * samplesPerPixel)
                pmax = width * height * samplesPerPixel;

            for (int j = base; j < pmax; j++)
            {
                pixels[j] = byteArray[k++];
            }

            base += bytesToRead;
        }

        return pixels;
    }



    public byte[] lzwUncompress(byte[] input, int rowNumPixels)
    {
        if (input == null || input.length == 0)
            return input;
        byte[][] symbolTable = new byte[4096][1];
        int bitsToRead = 9; //default
        int nextSymbol = 258;
        int code;
        int oldCode = -1;

        ByteBuffer out = java.nio.ByteBuffer.allocate(rowNumPixels);
        CodeReader bb = new CodeReader(input);

        while (true)
        {
            code = bb.getCode(bitsToRead);

            if (code == EOI_CODE || code == -1)
                break;
            if (code == CLEAR_CODE)
            {
                // initialize symbol table
                for (int i = 0; i < 256; i++)
                {
                    symbolTable[i][0] = (byte) i;
                }
                nextSymbol = 258;
                bitsToRead = 9;
                code = bb.getCode(bitsToRead);

                if (code == EOI_CODE || code == -1)
                    break;

                out.put(symbolTable[code]);
                oldCode = code;
            }
            else
            {
                if (code < nextSymbol)
                {
                    out.put(symbolTable[code]);
                    ByteBuffer symbol = java.nio.ByteBuffer.allocate((symbolTable[oldCode].length + 1));
                    symbol.put(symbolTable[oldCode]);
                    symbol.put(symbolTable[code][0]);
                    symbolTable[nextSymbol] = symbol.array();
                    oldCode = code;
                    nextSymbol++;
                }
                else
                {
                    int size = symbolTable[oldCode].length + 1;
                    ByteBuffer symbol = java.nio.ByteBuffer.allocate(size);
                    symbol.put(symbolTable[oldCode]);
                    symbol.put(symbolTable[oldCode][0]);
                    byte[] outString = symbol.array();

                    out.put(outString);

                    symbolTable[nextSymbol] = outString;
                    oldCode = code;
                    nextSymbol++;
                }
                if (nextSymbol == 511)
                {
                    bitsToRead = 10;
                }
                if (nextSymbol == 1023)
                {
                    bitsToRead = 11;
                }
                if (nextSymbol == 2047)
                {
                    bitsToRead = 12;
                }
            }
        }
        return out.array();
    }

    /*
     * Reads BYTE image data organized as a singular image plane (and pixel interleaved, in the case of color images).
     *
     */
    public byte[][] readPixelInterleaved8(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts) throws IOException
    {
        byte[][] data = new byte[1][width * height * samplesPerPixel];
        int offset = 0;

        ByteBuffer buff = ByteBuffer.wrap(data[0]);
        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theChannel.position(stripOffsets[i]);
            int len = (int) stripCounts[i];
            if ((offset + len) >= data[0].length)
                len = data[0].length - offset;
            buff.limit(offset + len);
            this.theChannel.read(buff);
            offset += stripCounts[i];
        }

        return data;
    }

    /*
    * Reads BYTE image data organized as separate image planes.
    *
    */
    public byte[][] readPlanar8(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts, long rowsPerStrip) throws IOException
    {
        byte[][] data = new byte[samplesPerPixel][width * height];
        int band = 0;
        int offset = 0;
        int numRows = 0;

        ByteBuffer buff = ByteBuffer.wrap(data[band]);
        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theChannel.position(stripOffsets[i]);
            int len = (int) stripCounts[i];
            if ((offset + len) >= data[band].length)
                len = data[band].length - offset;
            buff.limit(offset + len);
            this.theChannel.read(buff);
            offset += stripCounts[i];
            numRows += rowsPerStrip;
            if (numRows >= height && band < (data.length - 1))
            {
                buff = ByteBuffer.wrap(data[++band]);
                numRows = 0;
                offset = 0;
            }
        }

        return data;
    }

/*
     * Reads SHORT image data organized as PIXEL interleaved
     * b1p1, b2p1, b3p1, b4p1, b1p2, b2p2, b3p2, b4p2, b1p3, ...
     *
     */
    public short[] read16bitPixelInterleavedImage(int band, int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts, long rowsPerStrip) throws IOException
    {
        short[] data = new short[width * height];
        int numRows = 0;

        ByteBuffer buff = null;

        int dataOffset = 0;
        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theChannel.position( stripOffsets[i] );

            int stripSize = (int) stripCounts[i];

            if( null == buff || buff.capacity() < stripSize )
            {
                buff = ByteBuffer.allocateDirect( stripSize );
                buff.order( this.getByteOrder() );
            }
            buff.clear().rewind();

            buff.limit( stripSize );

            this.theChannel.read( buff );

            buff.flip();
            ShortBuffer sb = buff.asShortBuffer();

            int b = 0;
            while(sb.hasRemaining())
            {
                if( band == (b++ % samplesPerPixel ))
                {
                    data[ dataOffset] = (short)(0xFFFF & sb.get());
                    dataOffset++;
                }
                else
                    sb.get();
            }
        }

        return data;
    }

/*
     * Reads SHORT image data organized as separate image planes.
     *
     */
    public short[][] readPlanar16(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts, long rowsPerStrip) throws IOException
    {
        short[][] data = new short[samplesPerPixel][width * height];
        int band = 0;
        int numRows = 0;

        ByteBuffer buff = ByteBuffer.allocateDirect(width * height * SHORT_SIZEOF);
        buff.order(this.getByteOrder());

        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theChannel.position(stripOffsets[i]);
            int len = (int) stripCounts[i];
            if ((buff.position() + len) > data[band].length * SHORT_SIZEOF)
                len = data[band].length * SHORT_SIZEOF - buff.position();
            buff.limit(buff.position() + len);
            this.theChannel.read(buff);
            numRows += rowsPerStrip;
            if (numRows >= height)
            {
                buff.flip();
                ShortBuffer sbuff = buff.asShortBuffer();
                sbuff.get(data[band]);
                buff.clear();
                ++band;
                numRows = 0;
            }
        }

        return data;
    }

    /*
     * Reads FLOAT image data organized as separate image planes.
     *
     */
    public float[][] readPlanarFloat32(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts, long rowsPerStrip) throws IOException
    {
        float[][] data = new float[samplesPerPixel][width * height];
        int band = 0;
        int numRows = 0;

        ByteBuffer buff = ByteBuffer.allocateDirect(width * height * FLOAT_SIZEOF);
        buff.order(this.getByteOrder());

        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theChannel.position(stripOffsets[i]);
            int len = (int) stripCounts[i];
            if ((buff.position() + len) >= data[band].length * FLOAT_SIZEOF)
                len = data[band].length * FLOAT_SIZEOF - buff.position();
            buff.limit(buff.position() + len);
            this.theChannel.read(buff);
            numRows += rowsPerStrip;
            if (numRows >= height)
            {
                buff.flip();
                FloatBuffer fbuff = buff.asFloatBuffer();
                fbuff.get(data[band]);
                buff.clear();
                ++band;
                numRows = 0;
            }
        }

        return data;
    }

    /*
     * Reads a ColorMap.
     *
     */
    public byte[][] readColorMap(TiffIFDEntry colorMapEntry) throws IOException
    {
        if (null == colorMapEntry)
        {
            String message = Logging.getMessage("GeotiffReader.MissingColormap");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        // NOTE: TIFF gives total number of cmap values, which is 3 times the size of cmap table...
        // CLUT is composed of shorts, but we'll read as bytes (thus, the factor of 2)...
        int numEntries = (int) colorMapEntry.count / 3;
        byte[][] tmp = new byte[3][numEntries * 2];

        this.theChannel.position(colorMapEntry.asLong());

        // Unroll the loop; the TIFF spec says "...3 is the number of the counting, and the counting shall be 3..."
        // TIFF spec also says that all red values precede all green, which precede all blue.
        ByteBuffer buff = ByteBuffer.wrap(tmp[0]);
        this.theChannel.read(buff);
        buff = ByteBuffer.wrap(tmp[1]);
        this.theChannel.read(buff);
        buff = ByteBuffer.wrap(tmp[2]);
        this.theChannel.read(buff);

        // TIFF gives a ColorMap composed of unsigned shorts. Java's IndexedColorModel wants unsigned bytes.
        // Something's got to give somewhere...we'll do our best.
        byte[][] cmap = new byte[3][numEntries];
        for (int i = 0; i < 3; i++)
        {
            buff = ByteBuffer.wrap(tmp[i]);
            buff.order(this.getByteOrder());
            for (int j = 0; j < numEntries; j++)
            {
                cmap[i][j] = (byte) (0x00ff & buff.getShort());
            }
        }

        return cmap;
    }

    public static int getUnsignedShort(ByteBuffer b)
    {
        return 0xffff & (int) b.getShort();
    }

    public static long getUnsignedInt(ByteBuffer b)
    {
        return 0xffffffffL & (long)b.getInt();
    }

    /*
     * Reads and returns an array of bytes from the file.
     *
     */
    public byte[] readBytes(TiffIFDEntry entry) throws IOException
    {
        byte[] bytes = new byte[(int) entry.count];
        ByteBuffer buff = ByteBuffer.wrap(bytes);
        this.theChannel.position(entry.asOffset());
        this.theChannel.read(buff);
        return bytes;
    }

    public String readString(TiffIFDEntry entry)
    {
        try
        {
            if( null != entry && entry.type == Tiff.Type.ASCII )
            {
                return new String( this.readBytes( entry ));
            }
        }
        catch(Exception e)
        {
            Logging.logger().severe(e.getMessage());
        }
        return null;
    }


    /*
     * Utility method intended to read the array of StripOffsets or StripByteCounts.
     */
//    public long[] readOffsetsAsLongs(TiffIFDEntry entry) throws IOException
//    {
//        long[] offsets = new long[(int) entry.count];
//        if (entry.count == 1)
//        {
//            // this is a special case, and it *does* happen!
//            offsets[0] = entry.asLong();
//        }
//        else
//        {
//            long fileOffset = entry.asLong();
//            this.theChannel.position(fileOffset);
//            if (entry.type == Tiff.Type.SHORT)
//            {
//                ByteBuffer buff = ByteBuffer.allocateDirect(offsets.length * SHORT_SIZEOF);
//                this.theChannel.read(buff);
//                buff.order(this.getByteOrder()).flip();
//                for (int i = 0; i < entry.count; i++)
//                {
//                    offsets[i] = getUnsignedShort(buff);
//                }
//            }
//            else
//            {
//                ByteBuffer buff = ByteBuffer.allocateDirect(offsets.length * INTEGER_SIZEOF);
//                this.theChannel.read(buff);
//                buff.order(this.getByteOrder()).flip();
//                for (int i = 0; i < entry.count; i++)
//                {
//                    offsets[i] = getUnsignedInt(buff);
//                }
//            }
//        }
//
//        return offsets;
//    }

        //Inner class for reading individual codes during decompression
    private class CodeReader
    {
        private int currentByte;
        private int currentBit;
        private byte[] byteBuffer;
        private int bufferLength;
        private int[] backMask = new int[] {0x0000, 0x0001, 0x0003, 0x0007,
            0x000F, 0x001F, 0x003F, 0x007F};
        private int[] frontMask = new int[] {0x0000, 0x0080, 0x00C0, 0x00E0,
            0x00F0, 0x00F8, 0x00FC, 0x00FE};
        private boolean atEof;

        public CodeReader(byte[] byteBuffer)
        {
            //todo validate byteBuffer
            this.byteBuffer = byteBuffer;
            currentByte = 0;
            currentBit = 0;
            bufferLength = byteBuffer.length;
        }

        public int getCode(int numBitsToRead)
        {
            if (numBitsToRead < 0)
                return 0;
            if (atEof)
                return -1; //end of file
            int returnCode = 0;
            while (numBitsToRead != 0 && !atEof)
            {
                if (numBitsToRead >= 8 - currentBit)
                {
                    if (currentBit == 0) //get first
                    {
                        returnCode = returnCode << 8;
                        int cb = ((int) byteBuffer[currentByte]);
                        returnCode += (cb < 0 ? 256 + cb : cb);
                        numBitsToRead -= 8;
                        currentByte++;
                    }
                    else
                    {
                        returnCode = returnCode << (8 - currentBit);
                        returnCode += ((int) byteBuffer[currentByte]) & backMask[8 - currentBit];
                        numBitsToRead -= (8 - currentBit);
                        currentBit = 0;
                        currentByte++;
                    }
                }
                else
                {
                    returnCode = returnCode << numBitsToRead;
                    int cb = ((int) byteBuffer[currentByte]);
                    cb = (cb < 0 ? 256 + cb : cb);
                    returnCode += ((cb) & (0x00FF - frontMask[currentBit])) >> (8 - (currentBit + numBitsToRead));
                    currentBit += numBitsToRead;
                    numBitsToRead = 0;
                }
                if (currentByte == bufferLength)  //at eof
                {
                    atEof = true;
                    return returnCode;
                }
            }
            return returnCode;
        }
    }
}
