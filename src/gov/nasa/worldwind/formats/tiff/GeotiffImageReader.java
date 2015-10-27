/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.tiff;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * @author brownrigg
 * @version $Id: GeotiffImageReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeotiffImageReader extends ImageReader
{

    public GeotiffImageReader(ImageReaderSpi provider)
    {
        super(provider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException
    {
        // TODO:  This should allow for multiple images that may be present. For now, we'll ignore all but first.
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException
    {
        if (imageIndex < 0 || imageIndex >= getNumImages(true))
            throw new IllegalArgumentException(
                this.getClass().getName() + ".getWidth(): illegal imageIndex: " + imageIndex);

        if (ifds.size() == 0)
            readIFDs();
        
        TiffIFDEntry widthEntry = getByTag(ifds.get(imageIndex), Tiff.Tag.IMAGE_WIDTH);
        return (int) widthEntry.asLong();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException
    {
        if (imageIndex < 0 || imageIndex >= getNumImages(true))
            throw new IllegalArgumentException(
                this.getClass().getName() + ".getHeight(): illegal imageIndex: " + imageIndex);

        if (ifds.size() == 0)
            readIFDs();

        TiffIFDEntry heightEntry = getByTag(ifds.get(imageIndex), Tiff.Tag.IMAGE_LENGTH);
        return (int) heightEntry.asLong();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException
    {
        // TODO: For this first implementation, we are completely ignoring the ImageReadParam given to us.
        //       Our target functionality is not the entire ImageIO, but only that needed to support the static
        //       read method ImageIO.read("myImage.tif").

        // TODO: more generally, the following test should reflect that more than one image is possible in a Tiff.
        if (imageIndex != 0)
            throw new IllegalArgumentException(
                this.getClass().getName() + ".read(): illegal imageIndex: " + imageIndex);

        readIFDs();

        // Extract the various IFD tags we need to read this image...
        TiffIFDEntry widthEntry = null;
        TiffIFDEntry lengthEntry = null;
        TiffIFDEntry bitsPerSampleEntry = null;
        TiffIFDEntry samplesPerPixelEntry = null;
        TiffIFDEntry photoInterpEntry = null;
        TiffIFDEntry stripOffsetsEntry = null;
        TiffIFDEntry stripCountsEntry = null;
        TiffIFDEntry rowsPerStripEntry = null;
        TiffIFDEntry planarConfigEntry = null;
        TiffIFDEntry colorMapEntry = null;
        TiffIFDEntry sampleFormatEntry = null;

        TiffIFDEntry[] ifd = ifds.get(imageIndex);
        for (TiffIFDEntry entry : ifd)
        {
            switch (entry.tag)
            {
                case Tiff.Tag.IMAGE_WIDTH:
                    widthEntry = entry;
                    break;
                case Tiff.Tag.IMAGE_LENGTH:
                    lengthEntry = entry;
                    break;
                case Tiff.Tag.BITS_PER_SAMPLE:
                    bitsPerSampleEntry = entry;
                    break;
                case Tiff.Tag.SAMPLES_PER_PIXEL:
                    samplesPerPixelEntry = entry;
                    break;
                case Tiff.Tag.PHOTO_INTERPRETATION:
                    photoInterpEntry = entry;
                    break;
                case Tiff.Tag.STRIP_OFFSETS:
                    stripOffsetsEntry = entry;
                    break;
                case Tiff.Tag.STRIP_BYTE_COUNTS:
                    stripCountsEntry = entry;
                    break;
                case Tiff.Tag.ROWS_PER_STRIP:
                    rowsPerStripEntry = entry;
                    break;
                case Tiff.Tag.PLANAR_CONFIGURATION:
                    planarConfigEntry = entry;
                    break;
                case Tiff.Tag.COLORMAP:
                    colorMapEntry = entry;
                    break;
                case Tiff.Tag.SAMPLE_FORMAT:
                    sampleFormatEntry = entry;
                    break;
            }
        }

        // Check that we have the mandatory tags present...
        if (widthEntry == null || lengthEntry == null || samplesPerPixelEntry == null || photoInterpEntry == null ||
            stripOffsetsEntry == null || stripCountsEntry == null || rowsPerStripEntry == null
            || planarConfigEntry == null)
            throw new IIOException(this.getClass().getName() + ".read(): unable to decipher image organization");

        int width = (int) widthEntry.asLong();
        int height = (int) lengthEntry.asLong();
        int samplesPerPixel = (int) samplesPerPixelEntry.asLong();
        long photoInterp = photoInterpEntry.asLong();
        long rowsPerStrip = rowsPerStripEntry.asLong();
        long planarConfig = planarConfigEntry.asLong();
        int[] bitsPerSample = getBitsPerSample(bitsPerSampleEntry);
        long[] stripOffsets = getStripsArray(stripOffsetsEntry);
        long[] stripCounts = getStripsArray(stripCountsEntry);

        ColorModel colorModel;
        WritableRaster raster;

        //
        // TODO: This isn't terribly robust; we know how to deal with a few specific types...
        //

        if (samplesPerPixel == 1 && bitsPerSample.length == 1 && bitsPerSample[0] == 16)
        {
            // 16-bit grayscale (typical of elevation data, for example)...
            long sampleFormat =
                (sampleFormatEntry != null) ? sampleFormatEntry.asLong() : Tiff.SampleFormat.UNSIGNED;
            int dataBuffType =
                (sampleFormat == Tiff.SampleFormat.SIGNED) ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_USHORT;

            colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), bitsPerSample, false,
                false, Transparency.OPAQUE, dataBuffType);
            int[] offsets = new int[]{0};
            ComponentSampleModel sampleModel = new ComponentSampleModel(dataBuffType, width, height, 1, width, offsets);
            short[][] imageData = readPlanar16(width, height, samplesPerPixel, stripOffsets, stripCounts, rowsPerStrip);
            DataBuffer dataBuff = (dataBuffType == DataBuffer.TYPE_SHORT) ?
                new DataBufferShort(imageData, width * height, offsets) :
                new DataBufferUShort(imageData, width * height, offsets);

            raster = Raster.createWritableRaster(sampleModel, dataBuff, new Point(0, 0));
        }
        else if (samplesPerPixel == 1 && bitsPerSample.length == 1 && bitsPerSample[0] == 32 &&
            sampleFormatEntry != null && sampleFormatEntry.asLong() == Tiff.SampleFormat.IEEEFLOAT)
        {
            // 32-bit grayscale (typical of elevation data, for example)...
            colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), bitsPerSample, false,
                false, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
            int[] offsets = new int[]{0};
            ComponentSampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1, width,
                offsets);
            float[][] imageData = readPlanarFloat32(width, height, samplesPerPixel, stripOffsets, stripCounts,
                rowsPerStrip);
            DataBuffer dataBuff = new DataBufferFloat(imageData, width * height, offsets);
            raster = Raster.createWritableRaster(sampleModel, dataBuff, new Point(0, 0));
        }
        else
        {

            // make sure a DataBufferByte is going to do the trick
            for (int bits : bitsPerSample)
            {
                if (bits != 8)
                    throw new IIOException(this.getClass().getName() + ".read(): only expecting 8 bits/sample; found " +
                        bits);
            }

            // byte image data; could be RGB-component, grayscale, or indexed-color.
            // Set up an appropriate ColorModel...
            colorModel = null;
            if (samplesPerPixel > 1)
            {
                int transparency = Transparency.OPAQUE;
                boolean hasAlpha = false;
                if (samplesPerPixel == 4)
                {
                    transparency = Transparency.TRANSLUCENT;
                    hasAlpha = true;
                }
                colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), bitsPerSample,
                    hasAlpha,
                    false, transparency, DataBuffer.TYPE_BYTE);
            }
            else
            {
                // grayscale or indexed-color?
                if (photoInterp == Tiff.Photometric.Color_Palette)
                {
                    // indexed...
                    if (colorMapEntry == null)
                        throw new IIOException(
                            this.getClass().getName() + ".read(): no ColorMap found for indexed image type");
                    byte[][] cmap = readColorMap(colorMapEntry);
                    colorModel = new IndexColorModel(bitsPerSample[0], (int) colorMapEntry.count / 3, cmap[0], cmap[1],
                        cmap[2]);
                }
                else
                {
                    // grayscale...
                    colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), bitsPerSample,
                        false,
                        false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
                }
            }

            int[] bankOffsets = new int[samplesPerPixel];
            for (int i = 0; i < samplesPerPixel; i++)
            {
                bankOffsets[i] = i;
            }
            int[] offsets = new int[(planarConfig == Tiff.PlanarConfiguration.CHUNKY) ? 1 : samplesPerPixel];
            for (int i = 0; i < offsets.length; i++)
            {
                offsets[i] = 0;
            }

            // construct the right SampleModel...
            ComponentSampleModel sampleModel;
            if (samplesPerPixel == 1)
                sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width, bankOffsets);
            else
                sampleModel = (planarConfig == Tiff.PlanarConfiguration.CHUNKY) ?
                    new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, samplesPerPixel,
                        width * samplesPerPixel, bankOffsets) :
                    new BandedSampleModel(DataBuffer.TYPE_BYTE, width, height, width, bankOffsets, offsets);

            // Get the image data and make our Raster...
            byte[][] imageData;
            if (planarConfig == Tiff.PlanarConfiguration.CHUNKY)
                imageData = readPixelInterleaved8(width, height, samplesPerPixel, stripOffsets, stripCounts);
            else
                imageData = readPlanar8(width, height, samplesPerPixel, stripOffsets, stripCounts, rowsPerStrip);
            DataBufferByte dataBuff = new DataBufferByte(imageData, width * height, offsets);
            raster = Raster.createWritableRaster(sampleModel, dataBuff, new Point(0, 0));
        }

        /**************************************/
        decodeGeotiffInfo();
        /**************************************/

        // Finally, put it all together to get our BufferedImage...
        return new BufferedImage(colorModel, raster, false, null);
    }

    private void decodeGeotiffInfo() throws IOException
    {
        readIFDs();
        TiffIFDEntry[] ifd = ifds.get(0);
        for (TiffIFDEntry entry : ifd)
        {
            switch (entry.tag)
            {
                case GeoTiff.Tag.MODEL_PIXELSCALE:
                    geoPixelScale = readDoubles(entry);
                    break;
                case GeoTiff.Tag.MODEL_TIEPOINT:
                    geoTiePoints = readDoubles(entry);
                    break;
                case GeoTiff.Tag.MODEL_TRANSFORMATION:
                    geoMatrix = readDoubles(entry);
                    break;
                case GeoTiff.Tag.GEO_KEY_DIRECTORY:
                    readGeoKeys(entry);
                    break;
                case GeoTiff.Tag.GEO_DOUBLE_PARAMS:
                    break;
                case GeoTiff.Tag.GEO_ASCII_PARAMS:
                    break;
            }
        }
    }

    /*
     * Coordinates reading all the ImageFileDirectories in a Tiff file (there's typically only one).
     * 
     */
    private void readIFDs() throws IOException
    {
        if (this.theStream != null)
            return;

        if (super.input == null || !(super.input instanceof ImageInputStream))
        {
            throw new IIOException(this.getClass().getName() + ": null/invalid ImageInputStream");
        }
        this.theStream = (ImageInputStream) super.input;

        // determine byte ordering...
        byte[] ifh = new byte[2];  // Tiff image-file header
        try
        {
            theStream.readFully(ifh);
            if (ifh[0] == 0x4D && ifh[1] == 0x4D)
            {
                theStream.setByteOrder(ByteOrder.BIG_ENDIAN);
            }
            else if (ifh[0] == 0x49 && ifh[1] == 0x49)
            {
                theStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            }
            else
            {
                throw new IOException();
            }
        }
        catch (IOException ex)
        {
            throw new IIOException(this.getClass().getName() + ": error reading signature");
        }

        // skip the magic number and get offset to first (and likely only) ImageFileDirectory...
        theStream.readFully(ifh);
        long ifdOffset = theStream.readUnsignedInt();
        readIFD(ifdOffset);
    }

    /*
    * Reads an ImageFileDirectory and places it in our list.  Calls itself recursively if additional
    * IFDs are indicated.
    *
    */
    private void readIFD(long offset) throws IIOException
    {
        try
        {
            theStream.seek(offset);
            int numEntries = theStream.readUnsignedShort();
            TiffIFDEntry[] ifd = new TiffIFDEntry[numEntries];
            for (int i = 0; i < numEntries; i++)
            {
                int tag = theStream.readUnsignedShort();
                int type = theStream.readUnsignedShort();
                long count = theStream.readUnsignedInt();
                long valoffset;
                if (type == Tiff.Type.SHORT && count == 1) {
                    // these get packed left-justified in the bytes...
                    int upper = theStream.readUnsignedShort();
                    int lower = theStream.readUnsignedShort();
                    valoffset = (0xffff & upper) << 16 | (0xffff & lower);
                }
                else
                    valoffset = theStream.readUnsignedInt();
                ifd[i] = new TiffIFDEntry(tag, type, count, valoffset);
            }

            ifds.add(ifd);

            /****** TODO: UNCOMMENT;  IN GENERAL, THERE CAN BE MORE THAN ONE IFD IN A TIFF FILE
             long nextIFDOffset = theStream.readUnsignedInt();
             if (nextIFDOffset > 0)
             readIFD(nextIFDOffset);
             */

        }
        catch (Exception ex)
        {
            throw new IIOException("Error reading Tiff IFD: " + ex.getMessage());
        }
    }

    /*
    * Reads BYTE image data organized as a singular image plane (and pixel interleaved, in the case of color images).
    *
    */
    private byte[][] readPixelInterleaved8(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts) throws IOException
    {
        byte[][] data = new byte[1][width * height * samplesPerPixel];
        int offset = 0;
        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theStream.seek(stripOffsets[i]);
            int len = (int) stripCounts[i];
            if ((offset + len) >= data[0].length)
                len = data[0].length - offset;
            this.theStream.readFully(data[0], offset, len);
            offset += stripCounts[i];
        }
        return data;
    }

    /*
     * Reads BYTE image data organized as separate image planes.
     *
     */
    private byte[][] readPlanar8(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts, long rowsPerStrip) throws IOException
    {
        byte[][] data = new byte[samplesPerPixel][width * height];
        int band = 0;
        int offset = 0;
        int numRows = 0;
        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theStream.seek(stripOffsets[i]);
            int len = (int) stripCounts[i];
            if ((offset + len) >= data[band].length)
                len = data[band].length - offset;
            this.theStream.readFully(data[band], offset, len);
            offset += stripCounts[i];
            numRows += rowsPerStrip;
            if (numRows >= height)
            {
                ++band;
                numRows = 0;
                offset = 0;
            }
        }

        return data;
    }

    /*
     * Reads SHORT image data organized as separate image planes.
     *
     */
    private short[][] readPlanar16(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts, long rowsPerStrip) throws IOException
    {
        short[][] data = new short[samplesPerPixel][width * height];
        int band = 0;
        int offset = 0;
        int numRows = 0;
        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theStream.seek(stripOffsets[i]);
            int len = (int) stripCounts[i] / Short.SIZE;    // strip-counts are in bytes, we're reading shorts...
            if ((offset + len) >= data[band].length)
                len = data[band].length - offset;
            this.theStream.readFully(data[band], offset, len);
            offset += stripCounts[i] / Short.SIZE;
            numRows += rowsPerStrip;
            if (numRows >= height)
            {
                ++band;
                numRows = 0;
                offset = 0;
            }
        }

        return data;
    }

    /*
     * Reads FLOAT image data organized as separate image planes.
     *
     */
    private float[][] readPlanarFloat32(int width, int height, int samplesPerPixel,
        long[] stripOffsets, long[] stripCounts, long rowsPerStrip) throws IOException
    {
        float[][] data = new float[samplesPerPixel][width * height];
        int band = 0;
        int offset = 0;
        int numRows = 0;
        for (int i = 0; i < stripOffsets.length; i++)
        {
            this.theStream.seek(stripOffsets[i]);
            int len = (int) stripCounts[i] / Float.SIZE;    // strip-counts are in bytes, we're reading floats...
            if ((offset + len) >= data[band].length)
                len = data[band].length - offset;
            this.theStream.readFully(data[band], offset, len);
            offset += stripCounts[i] / Float.SIZE;
            numRows += rowsPerStrip;
            if (numRows >= height)
            {
                ++band;
                numRows = 0;
                offset = 0;
            }
        }

        return data;
    }

    /*
     * Reads a ColorMap.
     *
     */
    private byte[][] readColorMap(TiffIFDEntry colorMapEntry) throws IOException
    {
        // NOTE: TIFF gives total number of cmap values, which is 3 times the size of cmap table...
        int numEntries = (int) colorMapEntry.count / 3;
        short[][] tmp = new short[3][numEntries];
        this.theStream.seek(colorMapEntry.asLong());

        // Unroll the loop; the TIFF spec says "...3 is the number of the counting, and the counting shall be 3..."
        // TIFF spec also says that all red values precede all green, which precede all blue.
        this.theStream.readFully(tmp[0], 0, numEntries);
        this.theStream.readFully(tmp[1], 0, numEntries);
        this.theStream.readFully(tmp[2], 0, numEntries);

        // TIFF gives a ColorMap composed of unsigned shorts. Java's IndexedColorModel wants unsigned bytes.
        // Something's got to give somewhere...we'll do our best.
        byte[][] cmap = new byte[3][numEntries];
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < numEntries; j++)
            {
                cmap[i][j] = (byte) (0x00ff & tmp[i][j]);
            }
        }

        return cmap;
    }

    /*
     * Reads and returns an array of doubles from the file.
     *
     */
    private double[] readDoubles(TiffIFDEntry entry) throws IOException
    {
        double[] doubles = new double[(int) entry.count];
        this.theStream.seek(entry.asOffset());
        this.theStream.readFully(doubles, 0, doubles.length);
        return doubles;
    }

    private void readGeoKeys(TiffIFDEntry entry) throws IOException
    {
        short[] keyValRec = new short[4];
        this.theStream.seek(entry.asLong());

        // get the number of key-value pairs...
        this.theStream.readFully(keyValRec, 0, keyValRec.length);
        int numKeys = keyValRec[3];
        geoKeys = new GeoKey[numKeys];
        keyValRec = new short[numKeys * 4];
        this.theStream.readFully(keyValRec, 0, numKeys * 4);

        int j = 0;
        for (int i = 0; i < numKeys * 4; i += 4)
        {
            GeoKey key = new GeoKey();
            key.key = keyValRec[i];
            if (keyValRec[i + 1] == 0)
                key.value = new Integer(keyValRec[i + 3]);
            else
            {
                // TODO: This isn't quite right....
                key.value = getByTag(ifds.get(0),  (0x0000ffff & keyValRec[i + 1]));
            }
            geoKeys[j++] = key;
        }
    }

    /*
     * Returns the (first!) IFD-Entry with the given tag, or null if not found.
     * 
     */
    private TiffIFDEntry getByTag(TiffIFDEntry[] ifds, int tag)
    {
        for (TiffIFDEntry ifd : ifds)
        {
            if (ifd.tag == tag)
            {
                return ifd;
            }
        }
        return null;
    }

    /*
    * Utility method intended to read the array of StripOffsets or StripByteCounts.
    */
    private long[] getStripsArray(TiffIFDEntry stripsEntry) throws IOException
    {
        long[] offsets = new long[(int) stripsEntry.count];
        if (stripsEntry.count == 1)
        {
            // this is a special case, and it *does* happen!
            offsets[0] = stripsEntry.asLong();
        }
        else
        {
            long fileOffset = stripsEntry.asLong();
            this.theStream.seek(fileOffset);
            if (stripsEntry.type == Tiff.Type.SHORT)
                for (int i = 0; i < stripsEntry.count; i++)
                {
                    offsets[i] = this.theStream.readUnsignedShort();
                }
            else
                for (int i = 0; i < stripsEntry.count; i++)
                {
                    offsets[i] = this.theStream.readUnsignedInt();
                }
        }
        return offsets;
    }

    /*
     * Utility to extract bitsPerSample info (if present). This is a bit tricky, because if the samples/pixel == 1,
     * the bitsPerSample will fit in the offset/value field of the ImageFileDirectory element. In contrast, when 
     * samples/pixel == 3, the 3 shorts that make up bitsPerSample don't fit in the offset/value field, so we have
     * to go track them down elsewhere in the file.  Finally, as bitsPerSample is optional for bilevel images,
     * we'll return something sane if this tag is absent.
     */
    private int[] getBitsPerSample(TiffIFDEntry entry) throws IOException
    {
        if (entry == null)
        {
            return new int[]{1};
        }  // the default according to the Tiff6.0 spec.

        if (entry.count == 1)
        {
            return new int[]{(int) entry.asLong()};
        }

        long[] tmp = getStripsArray(entry);
        int[] bits = new int[tmp.length];
        for (int i = 0; i < tmp.length; i++)
        {
            bits[i] = (int) tmp[i];
        }

        return bits;
    }

    private class GeoKey
    {
        short key;
        Object value;
    }

    private ImageInputStream theStream = null;
    private ArrayList<TiffIFDEntry[]> ifds = new ArrayList<TiffIFDEntry[]>(1);

    // Geotiff info...
    private double[] geoPixelScale = null;
    private double[] geoTiePoints = null;
    private double[] geoMatrix = null;
    private GeoKey[] geoKeys = null;
}
