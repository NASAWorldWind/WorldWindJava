/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.tiff;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * @author brownrigg
 * @version $Id: GeotiffReader.java 3289 2015-06-30 15:55:33Z tgaskins $
 */
public class GeotiffReader implements Disposable
{
    private TIFFReader tiffReader = null;

    private String sourceFilename;
    private RandomAccessFile sourceFile;
    private FileChannel theChannel;

    private GeoCodec gc = new GeoCodec();

    private ArrayList<TiffIFDEntry[]> tiffIFDs = null;
    private ArrayList<AVList> metadata = null;

    public GeotiffReader(String sourceFilename) throws IOException
    {
        this.sourceFilename = sourceFilename;
        this.sourceFile = new RandomAccessFile(sourceFilename, "r");
        this.theChannel = this.sourceFile.getChannel();

        this.tiffReader = new TIFFReader(this.theChannel);

        readTiffHeaders();
    }

    public GeotiffReader(File sourceFile) throws IOException
    {
        this(sourceFile.getAbsolutePath());
    }

    protected AVList getMetadata(int imageIndex) throws IOException
    {
        this.checkImageIndex(imageIndex);
        AVList values = this.metadata.get(imageIndex);
        return (null != values) ? values.copy() : null;
    }

    public AVList copyMetadataTo(int imageIndex, AVList values) throws IOException
    {
        AVList list = this.getMetadata(imageIndex);
        if (null != values)
        {
            values.setValues(list);
        }
        else
        {
            values = list;
        }
        return values;
    }

    public AVList copyMetadataTo(AVList list) throws IOException
    {
        return this.copyMetadataTo(0, list);
    }

//    public AVList getMetadata() throws IOException
//    {
//        return this.getMetadata(0);
//    }

    public void close()
    {
        try
        {
            this.sourceFile.close();
        }
        catch (Exception ex)
        { /* best effort */ }
    }

    public int getNumImages() throws IOException
    {
        return (this.tiffIFDs != null) ? this.tiffIFDs.size() : 0;
    }

    public int getWidth(int imageIndex) throws IOException
    {
        checkImageIndex(imageIndex);
        AVList values = this.metadata.get(imageIndex);
        return (values.hasKey(AVKey.WIDTH)) ? (Integer) values.getValue(AVKey.WIDTH) : 0;
    }

    public int getHeight(int imageIndex) throws IOException
    {
        checkImageIndex(imageIndex);
        AVList values = this.metadata.get(imageIndex);
        return (values.hasKey(AVKey.HEIGHT)) ? (Integer) values.getValue(AVKey.HEIGHT) : 0;
    }

    public DataRaster[] readDataRaster() throws IOException
    {
        int num = this.getNumImages();

        if (num <= 0)
        {
            return null;
        }

        DataRaster[] rasters = new DataRaster[num];
        for (int i = 0; i < num; i++)
        {
            rasters[i] = this.doRead(i);
        }
        return rasters;
    }

    public DataRaster readDataRaster(int imageIndex) throws IOException
    {
        checkImageIndex(imageIndex);
        return this.doRead(imageIndex);
    }

    public BufferedImage read() throws IOException
    {
        return this.read(0);
    }

    public BufferedImage read(int imageIndex) throws IOException
    {
        DataRaster raster = this.doRead(imageIndex);

        if (null == raster)
        {
            return null;
        }

        if (raster instanceof BufferedImageRaster)
        {
            return ((BufferedImageRaster) raster).getBufferedImage();
        }

        String message = Logging.getMessage("Geotiff.IsNotAnImage");
        Logging.logger().severe(message);
        throw new IOException(message);
    }

    public boolean isGeotiff(int imageIndex) throws IOException
    {
        AVList values = this.metadata.get(imageIndex);
        return (null != values && values.hasKey(AVKey.COORDINATE_SYSTEM));
    }

    public DataRaster doRead(int imageIndex) throws IOException
    {
        checkImageIndex(imageIndex);
        AVList values = this.metadata.get(imageIndex);

        // Extract the various IFD tags we need to read this image. We want to loop over the tag set once, instead
        // multiple times if we simply used our general getByTag() method.

        long[] stripOffsets = null;
        byte[][] cmap = null;
        long[] stripCounts = null;

        boolean tiffDifferencing = false;

        TiffIFDEntry[] ifd = this.tiffIFDs.get(imageIndex);

        BaselineTiff tiff = BaselineTiff.extract(ifd, this.tiffReader);

        if (null == tiff)
        {
            String message = Logging.getMessage("GeotiffReader.BadGeotiff");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (tiff.width <= 0)
        {
            String msg = Logging.getMessage("GeotiffReader.InvalidIFDEntryValue", tiff.width,
                "width", Tiff.Tag.IMAGE_WIDTH);
            Logging.logger().severe(msg);
            throw new IOException(msg);
        }

        if (tiff.height <= 0)
        {
            String msg = Logging.getMessage("GeotiffReader.InvalidIFDEntryValue", tiff.height,
                "height", Tiff.Tag.IMAGE_LENGTH);
            Logging.logger().severe(msg);
            throw new IOException(msg);
        }

        if (tiff.samplesPerPixel <= Tiff.Undefined)
        {
            String msg = Logging.getMessage("GeotiffReader.InvalidIFDEntryValue", tiff.samplesPerPixel,
                "samplesPerPixel", Tiff.Tag.SAMPLES_PER_PIXEL);
            Logging.logger().severe(msg);
            throw new IOException(msg);
        }

        if (tiff.photometric <= Tiff.Photometric.Undefined || tiff.photometric > Tiff.Photometric.YCbCr)
        {
            String msg = Logging.getMessage("GeotiffReader.InvalidIFDEntryValue", tiff.photometric,
                "PhotoInterpretation", Tiff.Tag.PHOTO_INTERPRETATION);
            Logging.logger().severe(msg);
            throw new IOException(msg);
        }

        if (tiff.rowsPerStrip <= Tiff.Undefined)
        {
            String msg = Logging.getMessage("GeotiffReader.InvalidIFDEntryValue", tiff.rowsPerStrip,
                "RowsPerStrip", Tiff.Tag.ROWS_PER_STRIP);
            Logging.logger().fine(msg);
            tiff.rowsPerStrip = Integer.MAX_VALUE;
        }

        if (tiff.planarConfig != Tiff.PlanarConfiguration.PLANAR
            && tiff.planarConfig != Tiff.PlanarConfiguration.CHUNKY)
        {
            String msg = Logging.getMessage("GeotiffReader.InvalidIFDEntryValue", tiff.planarConfig,
                "PhotoInterpretation", Tiff.Tag.PHOTO_INTERPRETATION);
            Logging.logger().severe(msg);
            throw new IOException(msg);
        }

        for (TiffIFDEntry entry : ifd)
        {
            try
            {
                switch (entry.tag)
                {
                    case Tiff.Tag.STRIP_OFFSETS:
                        stripOffsets = entry.getAsLongs();
                        break;

                    case Tiff.Tag.STRIP_BYTE_COUNTS:
                        stripCounts = entry.getAsLongs();
                        break;

                    case Tiff.Tag.COLORMAP:
                        cmap = this.tiffReader.readColorMap(entry);
                        break;
                }
            }
            catch (IOException e)
            {
                Logging.logger().finest(e.toString());
            }
        }

        if (null == stripOffsets || 0 == stripOffsets.length)
        {
            String message = Logging.getMessage("GeotiffReader.MissingRequiredTag", "StripOffsets");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (null == stripCounts || 0 == stripCounts.length)
        {
            String message = Logging.getMessage("GeotiffReader.MissingRequiredTag", "StripCounts");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        TiffIFDEntry notToday = getByTag(ifd, Tiff.Tag.COMPRESSION);
        boolean lzwCompressed = false;
        if (notToday != null && notToday.asLong() == Tiff.Compression.LZW)
        {
            lzwCompressed = true;
            TiffIFDEntry predictorEntry = getByTag(ifd, Tiff.Tag.TIFF_PREDICTOR);
            if ((predictorEntry != null) && (predictorEntry.asLong() != 0))
            {
                tiffDifferencing = true;
            }
        }
        else if (notToday != null && notToday.asLong() != Tiff.Compression.NONE)
        {
            String message = Logging.getMessage("GeotiffReader.CompressionFormatNotSupported");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        notToday = getByTag(ifd, Tiff.Tag.TILE_WIDTH);
        if (notToday != null)
        {
            String message = Logging.getMessage("GeotiffReader.NoTiled");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        long offset = stripOffsets[0];
//        int sampleFormat = (null != tiff.sampleFormat) ? tiff.sampleFormat[0] : Tiff.Undefined;
//        int bitsPerSample = (null != tiff.bitsPerSample) ? tiff.bitsPerSample[0] : Tiff.Undefined;

        if (values.getValue(AVKey.PIXEL_FORMAT) == AVKey.ELEVATION)
        {
            ByteBufferRaster raster = new ByteBufferRaster(tiff.width, tiff.height,
                (Sector) values.getValue(AVKey.SECTOR), values);

            if (raster.getValue(AVKey.DATA_TYPE) == AVKey.INT8)
            {
                byte[][] data = this.tiffReader.readPlanar8(tiff.width, tiff.height, tiff.samplesPerPixel,
                    stripOffsets, stripCounts, tiff.rowsPerStrip);

                int next = 0;
                for (int y = 0; y < tiff.height; y++)
                {
                    for (int x = 0; x < tiff.width; x++)
                    {
                        raster.setDoubleAtPosition(y, x, (double) data[0][next++]);
                    }
                }
            }
            else if (raster.getValue(AVKey.DATA_TYPE) == AVKey.INT16)
            {
                short[][] data = this.tiffReader.readPlanar16(tiff.width, tiff.height, tiff.samplesPerPixel,
                    stripOffsets, stripCounts, tiff.rowsPerStrip);

                int next = 0;
                for (int y = 0; y < tiff.height; y++)
                {
                    for (int x = 0; x < tiff.width; x++)
                    {
                        raster.setDoubleAtPosition(y, x, (double) data[0][next++] );
                    }
                }
            }
            else if (raster.getValue(AVKey.DATA_TYPE) == AVKey.FLOAT32)
            {
                float[][] data = this.tiffReader.readPlanarFloat32(tiff.width, tiff.height, tiff.samplesPerPixel,
                    stripOffsets, stripCounts, tiff.rowsPerStrip);

                int next = 0;
                for (int y = 0; y < tiff.height; y++)
                {
                    for (int x = 0; x < tiff.width; x++)
                    {
                        raster.setDoubleAtPosition(y, x, (double) data[0][next++]);
                    }
                }
            }
            else
            {
                String message = Logging.getMessage("Geotiff.UnsupportedDataTypeRaster", tiff.toString());
                Logging.logger().severe(message);
                throw new IOException(message);
            }

            ElevationsUtil.rectify( raster );

            return raster;
        }
        else if (values.getValue(AVKey.PIXEL_FORMAT) == AVKey.IMAGE
            && values.getValue(AVKey.IMAGE_COLOR_FORMAT) == AVKey.GRAYSCALE)
        {
            BufferedImage grayImage = null;

            if (values.getValue(AVKey.DATA_TYPE) == AVKey.INT8)
            {
                byte[][] image = this.tiffReader.readPlanar8(tiff.width, tiff.height, tiff.samplesPerPixel,
                    stripOffsets, stripCounts, tiff.rowsPerStrip);

                grayImage = new BufferedImage(tiff.width, tiff.height, BufferedImage.TYPE_BYTE_GRAY);
                WritableRaster wrRaster = grayImage.getRaster();

                int next = 0;
                for (int y = 0; y < tiff.height; y++)
                {
                    for (int x = 0; x < tiff.width; x++)
                    {
                        wrRaster.setSample(x, y, 0, 0xFF & (int) (image[0][next++]));
                    }
                }
            }
            else if (values.getValue(AVKey.DATA_TYPE) == AVKey.INT16 && tiff.samplesPerPixel == 1)
            {
                short[][] image = this.tiffReader.readPlanar16(tiff.width, tiff.height, tiff.samplesPerPixel,
                    stripOffsets, stripCounts, tiff.rowsPerStrip);

                grayImage = new BufferedImage(tiff.width, tiff.height, BufferedImage.TYPE_USHORT_GRAY);
                WritableRaster wrRaster = grayImage.getRaster();

                int next = 0;
                for (int y = 0; y < tiff.height; y++)
                {
                    for (int x = 0; x < tiff.width; x++)
                    {
                        wrRaster.setSample(x, y, 0, 0xFFFF & (int) (image[0][next++]));
                    }
                }
            }
            else if (values.getValue(AVKey.DATA_TYPE) == AVKey.INT16 && tiff.samplesPerPixel > 1)
            {
                short[] image = this.tiffReader.read16bitPixelInterleavedImage(imageIndex, tiff.width, tiff.height,
                    tiff.samplesPerPixel, stripOffsets, stripCounts, tiff.rowsPerStrip);

                grayImage = new BufferedImage(tiff.width, tiff.height, BufferedImage.TYPE_USHORT_GRAY);
                WritableRaster wrRaster = grayImage.getRaster();

                int next = 0;
                for (int y = 0; y < tiff.height; y++)
                {
                    for (int x = 0; x < tiff.width; x++)
                    {
                        wrRaster.setSample(x, y, 0, 0xFFFF & (int) (image[next++]));
                    }
                }
            }

            if (null == grayImage)
            {
                String message = Logging.getMessage("Geotiff.UnsupportedDataTypeRaster", tiff.toString());
                Logging.logger().severe(message);
                throw new IOException(message);
            }

            grayImage = ImageUtil.toCompatibleImage(grayImage);
            return BufferedImageRaster.wrap(grayImage, values);
        }
        else if (values.getValue(AVKey.PIXEL_FORMAT) == AVKey.IMAGE
            && values.getValue(AVKey.IMAGE_COLOR_FORMAT) == AVKey.COLOR)
        {

            ColorModel colorModel = null;
            WritableRaster raster;
            BufferedImage colorImage;

            // make sure a DataBufferByte is going to do the trick
            for (int bits : tiff.bitsPerSample)
            {
                if (bits != 8)
                {
                    String message = Logging.getMessage("GeotiffReader.Not8bit", bits);
                    Logging.logger().warning(message);
                    throw new IOException(message);
                }
            }

            if (tiff.photometric == Tiff.Photometric.Color_RGB)
            {
                int transparency = Transparency.OPAQUE;
                boolean hasAlpha = false;

                if (tiff.samplesPerPixel == Tiff.SamplesPerPixel.RGB)
                {
                    transparency = Transparency.OPAQUE;
                    hasAlpha = false;
                }
                else if (tiff.samplesPerPixel == Tiff.SamplesPerPixel.RGBA)
                {
                    transparency = Transparency.TRANSLUCENT;
                    hasAlpha = true;
                }
                colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), tiff.bitsPerSample,
                    hasAlpha, false, transparency, DataBuffer.TYPE_BYTE);
            }
            else if (tiff.photometric == Tiff.Photometric.Color_Palette)
            {
                colorModel = new IndexColorModel(tiff.bitsPerSample[0], cmap[0].length, cmap[0], cmap[1], cmap[2]);
            }
            else if (tiff.photometric == Tiff.Photometric.CMYK)
            {
//                colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_), tiff.bitsPerSample,
//                    false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            }

            int[] bankOffsets = new int[tiff.samplesPerPixel];
            for (int i = 0; i < tiff.samplesPerPixel; i++)
            {
                bankOffsets[i] = i;
            }
            int[] offsets = new int[(tiff.planarConfig == Tiff.PlanarConfiguration.CHUNKY) ? 1 : tiff.samplesPerPixel];
            for (int i = 0; i < offsets.length; i++)
            {
                offsets[i] = 0;
            }

            // construct the right SampleModel...
            ComponentSampleModel sampleModel;

            if (tiff.samplesPerPixel == Tiff.SamplesPerPixel.MONOCHROME)
            {
                sampleModel = new ComponentSampleModel(DataBuffer.TYPE_BYTE, tiff.width, tiff.height, 1, tiff.width,
                    bankOffsets);
            }
            else
            {
                sampleModel = (tiff.planarConfig == Tiff.PlanarConfiguration.CHUNKY) ?
                    new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, tiff.width, tiff.height, tiff.samplesPerPixel,
                        tiff.width * tiff.samplesPerPixel, bankOffsets) :
                    new BandedSampleModel(DataBuffer.TYPE_BYTE, tiff.width, tiff.height, tiff.width, bankOffsets,
                        offsets);
            }

            // Get the image data and make our Raster...
            byte[][] imageData;
            if (tiff.planarConfig == Tiff.PlanarConfiguration.CHUNKY)
            {
                if (lzwCompressed && (tiff.samplesPerPixel > 2))
                {
                    imageData = new byte[1][tiff.width * tiff.height * tiff.samplesPerPixel];

                    imageData[0] = this.tiffReader.readLZWCompressed(tiff.width, tiff.height, offset,
                        tiff.samplesPerPixel, tiffDifferencing, stripOffsets, stripCounts);
                }
                else
                {
                    imageData = this.tiffReader.readPixelInterleaved8(tiff.width, tiff.height, tiff.samplesPerPixel,
                        stripOffsets, stripCounts);
                }
            }
            else
            {
                imageData = this.tiffReader.readPlanar8(tiff.width, tiff.height, tiff.samplesPerPixel, stripOffsets,
                    stripCounts, tiff.rowsPerStrip);
            }

            DataBufferByte dataBuff = new DataBufferByte(imageData, tiff.width * tiff.height, offsets);
            raster = Raster.createWritableRaster(sampleModel, dataBuff, new Point(0, 0));

            colorImage = new BufferedImage(colorModel, raster, false, null);

            if (null == colorImage)
            {
                String message = Logging.getMessage("Geotiff.UnsupportedDataTypeRaster", tiff.toString());
                Logging.logger().severe(message);
                throw new IOException(message);
            }

            colorImage = ImageUtil.toCompatibleImage(colorImage);
            return BufferedImageRaster.wrap(colorImage, values);
        }

        String message = Logging.getMessage("Geotiff.UnsupportedDataTypeRaster", tiff.toString());
        Logging.logger().severe(message);
        throw new IOException(message);
    }

    /**
     * Returns true if georeferencing information was found in this file.
     * <p/>
     * Note: see getGeoKeys() for determining if projection information is contained in the file.
     *
     * @throws java.io.IOException if data type is not supported or unknown
     */

    private void repackageGeoReferencingTags() throws IOException
    {
        for (int i = 0; i < this.getNumImages(); i++)
        {
            TiffIFDEntry[] ifd = tiffIFDs.get(i);
            AVList values = this.metadata.get(i);

            values.setValue(AVKey.FILE_NAME, this.sourceFilename);
            // after we read all data, we have evrything as BIG_ENDIAN
            values.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

            BaselineTiff tiff = BaselineTiff.extract(ifd, this.tiffReader);

            if (null == tiff)
            {
                String message = Logging.getMessage("GeotiffReader.BadGeotiff");
                Logging.logger().severe(message);
                throw new IOException(message);
            }

            if (tiff.width == Tiff.Undefined)
            {
                String message = Logging.getMessage("generic.InvalidWidth", tiff.width);
                Logging.logger().severe(message);
                throw new IOException(message);
            }
            values.setValue(AVKey.WIDTH, tiff.width);

            if (tiff.height == Tiff.Undefined)
            {
                String message = Logging.getMessage("generic.InvalidHeight", tiff.height);
                Logging.logger().severe(message);
                throw new IOException(message);
            }
            values.setValue(AVKey.HEIGHT, tiff.height);

            int sampleFormat = (null != tiff.sampleFormat) ? tiff.sampleFormat[0] : Tiff.Undefined;
            int bitsPerSample = (null != tiff.bitsPerSample) ? tiff.bitsPerSample[0] : Tiff.Undefined;

            if (null != tiff.displayName)
            {
                values.setValue(AVKey.DISPLAY_NAME, tiff.displayName);
            }

            if (null != tiff.imageDescription)
            {
                values.setValue(AVKey.DESCRIPTION, tiff.imageDescription);
            }

            if (null != tiff.softwareVersion)
            {
                values.setValue(AVKey.VERSION, tiff.softwareVersion);
            }

            if (null != tiff.dateTime)
            {
                values.setValue(AVKey.DATE_TIME, tiff.dateTime);
            }

            if (tiff.photometric == Tiff.Photometric.Color_RGB)
            {
                values.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                values.setValue(AVKey.IMAGE_COLOR_FORMAT, AVKey.COLOR);
                values.setValue(AVKey.DATA_TYPE, AVKey.INT8);
            }
            else if (tiff.photometric == Tiff.Photometric.CMYK)
            {
                values.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                values.setValue(AVKey.IMAGE_COLOR_FORMAT, AVKey.COLOR);
                values.setValue(AVKey.DATA_TYPE, AVKey.INT8);
            }
            else if (tiff.photometric == Tiff.Photometric.Color_Palette)
            {
                values.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                values.setValue(AVKey.IMAGE_COLOR_FORMAT, AVKey.COLOR);
                values.setValue(AVKey.DATA_TYPE, AVKey.INT8);
            }
            else if (tiff.samplesPerPixel == Tiff.SamplesPerPixel.MONOCHROME)
            {   // Tiff.Photometric.Grayscale_BlackIsZero or Tiff.Photometric.Grayscale_WhiteIsZero
                if (sampleFormat == Tiff.SampleFormat.SIGNED)
                {
                    values.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                    if (bitsPerSample == Short.SIZE)
                    {
                        values.setValue(AVKey.DATA_TYPE, AVKey.INT16);
                    }
                    else if (bitsPerSample == Byte.SIZE)
                    {
                        values.setValue(AVKey.DATA_TYPE, AVKey.INT8);
                    }
                    else if (bitsPerSample == Integer.SIZE)
                    {
                        values.setValue(AVKey.DATA_TYPE, AVKey.INT32);
                    }
                }
                else if (sampleFormat == Tiff.SampleFormat.IEEEFLOAT)
                {
                    values.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                    if (bitsPerSample == Float.SIZE)
                    {
                        values.setValue(AVKey.DATA_TYPE, AVKey.FLOAT32);
                    }
                }
                else if (sampleFormat == Tiff.SampleFormat.UNSIGNED)
                {
                    values.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                    values.setValue(AVKey.IMAGE_COLOR_FORMAT, AVKey.GRAYSCALE);
                    if (bitsPerSample == Short.SIZE)
                    {
                        values.setValue(AVKey.DATA_TYPE, AVKey.INT16);
                    }
                    else if (bitsPerSample == Byte.SIZE)
                    {
                        values.setValue(AVKey.DATA_TYPE, AVKey.INT8);
                    }
                    else if (bitsPerSample == Integer.SIZE)
                    {
                        values.setValue(AVKey.DATA_TYPE, AVKey.INT32);
                    }
                }
            }

            if (!values.hasKey(AVKey.PIXEL_FORMAT) || !values.hasKey(AVKey.DATA_TYPE))
            {
                String message = Logging.getMessage("Geotiff.UnsupportedDataTypeRaster", tiff.toString());
                Logging.logger().severe(message);
//                throw new IOException(message);
            }

            // geo keys
            for (TiffIFDEntry entry : ifd)
            {
                try
                {
                    switch (entry.tag)
                    {
                        case GeoTiff.Tag.GDAL_NODATA:
                            Double d = Double.parseDouble(this.tiffReader.readString(entry));
                            values.setValue(AVKey.MISSING_DATA_SIGNAL, d);
                            break;

                        case Tiff.Tag.MIN_SAMPLE_VALUE:
                            values.setValue(AVKey.ELEVATION_MIN, entry.getAsDouble());
                            break;

                        case Tiff.Tag.MAX_SAMPLE_VALUE:
                            values.setValue(AVKey.ELEVATION_MAX, entry.getAsDouble());
                            break;

                        case GeoTiff.Tag.MODEL_PIXELSCALE:
                            this.gc.setModelPixelScale(entry.getDoubles());
                            break;

                        case GeoTiff.Tag.MODEL_TIEPOINT:
                            this.gc.addModelTiePoints(entry.getDoubles());
                            break;

                        case GeoTiff.Tag.MODEL_TRANSFORMATION:
                            this.gc.setModelTransformation(entry.getDoubles());
                            break;

                        case GeoTiff.Tag.GEO_KEY_DIRECTORY:
                            this.gc.setGeokeys(entry.getShorts());
                            break;

                        case GeoTiff.Tag.GEO_DOUBLE_PARAMS:
                            this.gc.setDoubleParams(entry.getDoubles());
                            break;

                        case GeoTiff.Tag.GEO_ASCII_PARAMS:
                            this.gc.setAsciiParams(this.tiffReader.readBytes(entry));
                            break;
                    }
                }
                catch (Exception e)
                {
                    Logging.logger().finest(e.toString());
                }
            }

            this.processGeoKeys(i);
        }
    }

    /*
     * Coordinates reading all the ImageFileDirectories in a Tiff file (there's typically only one).
     *
     */

    private void readTiffHeaders() throws IOException
    {
        if (this.tiffIFDs != null)
        {
            return;
        }

        if (this.theChannel == null)
        {
            String message = Logging.getMessage("GeotiffReader.NullInputFile", this.sourceFilename);
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        // Tiff image-file header (IFH)
        byte[] array = new byte[8];
        ByteBuffer ifh = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN);
        // determine byte ordering...
        this.theChannel.read(ifh);
        byte b0 = array[0];
        byte b1 = array[1];

        ByteOrder byteOrder = (b0 == 0x4D && b1 == 0x4D) ? ByteOrder.BIG_ENDIAN
            : ((b0 == 0x49 && b1 == 0x49) ? ByteOrder.LITTLE_ENDIAN : null);

        if (null == byteOrder)
        {
            String message = Logging.getMessage("GeotiffReader.BadTiffSig");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        this.tiffReader.setByteOrder(byteOrder);

        // skip the magic number and get offset to first (and likely only) ImageFileDirectory...
        ifh = ByteBuffer.wrap(array).order(byteOrder);
        ifh.position(4);
        long ifdOffset = TIFFReader.getUnsignedInt(ifh);

        // position the channel to the ImageFileDirectory...
        this.theChannel.position(ifdOffset);
        ifh.clear().limit(2);
        this.theChannel.read(ifh);
        ifh.flip();
        readIFD(ifh.getShort());

        // decode any geotiff tags and structures that may be present into a manager object...
        this.repackageGeoReferencingTags();
    }

    private void processGeoKeys(int imageIndex) throws IOException
    {
        this.checkImageIndex(imageIndex);

        AVList values = this.metadata.get(imageIndex);

        if (null == values
            || null == this.gc
            || !this.gc.hasGeoKey(GeoTiff.GeoKey.ModelType)
            || !values.hasKey(AVKey.WIDTH)
            || !values.hasKey(AVKey.HEIGHT)
            )
        {
            return;
        }

        int width = (Integer) values.getValue(AVKey.WIDTH);
        int height = (Integer) values.getValue(AVKey.HEIGHT);

        // geo-tiff spec requires the VerticalCSType to be present for elevations (but ignores its value)
        if (this.gc.hasGeoKey(GeoTiff.GeoKey.VerticalCSType))
        {
            values.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
        }

        if (this.gc.hasGeoKey(GeoTiff.GeoKey.VerticalUnits))
        {
            int[] v = this.gc.getGeoKeyAsInts(GeoTiff.GeoKey.VerticalUnits);
            int units = (null != v && v.length > 0) ? v[0] : GeoTiff.Undefined;

            if (units == GeoTiff.Unit.Linear.Meter)
            {
                values.setValue(AVKey.ELEVATION_UNIT, AVKey.UNIT_METER);
            }
            else if (units == GeoTiff.Unit.Linear.Foot)
            {
                values.setValue(AVKey.ELEVATION_UNIT, AVKey.UNIT_FOOT);
            }
        }

        if (this.gc.hasGeoKey(GeoTiff.GeoKey.RasterType))
        {
            int[] v = this.gc.getGeoKeyAsInts(GeoTiff.GeoKey.RasterType);
            int rasterType = (null != v && v.length > 0) ? v[0] : GeoTiff.Undefined;

            if (rasterType == GeoTiff.RasterType.RasterPixelIsArea)
            {
                values.setValue(AVKey.RASTER_PIXEL, AVKey.RASTER_PIXEL_IS_AREA);
            }
            else if (rasterType == GeoTiff.RasterType.RasterPixelIsPoint)
            {
                values.setValue(AVKey.RASTER_PIXEL, AVKey.RASTER_PIXEL_IS_POINT);
            }
        }

        if (this.gc.hasGeoKey(GeoTiff.GeoKey.GeogAngularUnits))
        {
//            int[] v = this.gc.getGeoKeyAsInts( GeoTiff.GeoKey.GeogAngularUnits );
//            int unit = ( null != v && v.length > 0 ) ? v[0] : GeoTiff.Undefined;
//
//            if( unit == GeoTiff.Unit.Angular.Angular_Degree )
//            else if( unit == GeoTiff.Unit.Angular.Angular_Radian )
        }

//    AVKey.PROJECTION_DATUM  Optional,
//    AVKey.PROJECTION_DESC   Optional,
//    AVKey.PROJECTION_NAME   Optional,
//    AVKey.PROJECTION_UNITS  Optional,

        int gtModelTypeGeoKey = GeoTiff.ModelType.Undefined;

        if (this.gc.hasGeoKey(GeoTiff.GeoKey.ModelType))
        {
            int[] gkValues = this.gc.getGeoKeyAsInts(GeoTiff.GeoKey.ModelType);
            if (null != gkValues && gkValues.length > 0)
            {
                gtModelTypeGeoKey = gkValues[0];
            }
        }

        if (gtModelTypeGeoKey == GeoTiff.ModelType.Geographic)
        {
            values.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);

            int epsg = GeoTiff.GCS.Undefined;

            if (this.gc.hasGeoKey(GeoTiff.GeoKey.GeographicType))
            {
                int[] gkValues = this.gc.getGeoKeyAsInts(GeoTiff.GeoKey.GeographicType);
                if (null != gkValues && gkValues.length > 0)
                {
                    epsg = gkValues[0];
                }
            }

            if (epsg != GeoTiff.GCS.Undefined)
            {
                values.setValue(AVKey.PROJECTION_EPSG_CODE, epsg);
            }

            // TODO Assumes WGS84(4326)- should we check for this ?

            double[] bbox = this.gc.getBoundingBox(width, height);
            values.setValue(AVKey.SECTOR, Sector.fromDegrees(bbox[3], bbox[1], bbox[0], bbox[2]));
            values.setValue(AVKey.ORIGIN, LatLon.fromDegrees(bbox[1], bbox[0]));
        }
        else if (gtModelTypeGeoKey == GeoTiff.ModelType.Projected)
        {
            values.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_PROJECTED);

            int projection = GeoTiff.PCS.Undefined;
            String hemi;
            int zone;

            int[] vals = null;
            if (this.gc.hasGeoKey(GeoTiff.GeoKey.Projection))
            {
                vals = this.gc.getGeoKeyAsInts(GeoTiff.GeoKey.Projection);
            }
            else if (this.gc.hasGeoKey(GeoTiff.GeoKey.ProjectedCSType))
            {
                vals = this.gc.getGeoKeyAsInts(GeoTiff.GeoKey.ProjectedCSType);
            }

            if (null != vals && vals.length > 0)
            {
                projection = vals[0];
            }

            if (projection != GeoTiff.PCS.Undefined)
            {
                values.setValue(AVKey.PROJECTION_EPSG_CODE, projection);
            }

            // TODO read more GeoKeys and GeoKeyDirectoryTag values

            /*
            from http://www.remotesensing.org/geotiff/spec/geotiff6.html#6.3.3.2
            UTM (North)	Format:  160zz
            UTM (South)	Format:  161zz
            */
            if ((projection >= 16100) && (projection <= 16199))  //UTM Zone South
            {
                hemi = AVKey.SOUTH;
                zone = projection - 16100;
            }
            else if ((projection >= 16000) && (projection <= 16099))  //UTM Zone North
            {
                hemi = AVKey.NORTH;
                zone = projection - 16000;
            }
            else if ((projection >= 26900) && (projection <= 26999))     //UTM : NAD83
            {
                hemi = AVKey.NORTH;
                zone = projection - 26900;
            }
            else if ((projection >= 32201) && (projection <= 32260))     //UTM : WGS72 N
            {
                hemi = AVKey.NORTH;
                zone = projection - 32200;
            }
            else if ((projection >= 32301) && (projection <= 32360))     //UTM : WGS72 S
            {
                hemi = AVKey.SOUTH;
                zone = projection - 32300;
            }
            else if ((projection >= 32401) && (projection <= 32460))     //UTM : WGS72BE N
            {
                hemi = AVKey.NORTH;
                zone = projection - 32400;
            }
            else if ((projection >= 32501) && (projection <= 32560))     //UTM : WGS72BE S
            {
                hemi = AVKey.SOUTH;
                zone = projection - 32500;
            }
            else if ((projection >= 32601) && (projection <= 32660))     //UTM : WGS84 N
            {
                hemi = AVKey.NORTH;
                zone = projection - 32600;
            }
            else if ((projection >= 32701) && (projection <= 32760))     //UTM : WGS84 S
            {
                hemi = AVKey.SOUTH;
                zone = projection - 32700;
            }
            else
            {
                String message = Logging.getMessage("generic.UnknownProjection", projection);
                Logging.logger().severe(message);
//                throw new IOException(message);
                return;
            }

            double pixelScaleX = this.gc.getModelPixelScaleX();
            double pixelScaleY = Math.abs(this.gc.getModelPixelScaleY());

            //dump "world file" values into values
            values.setValue(AVKey.PROJECTION_HEMISPHERE, hemi);
            values.setValue(AVKey.PROJECTION_ZONE, zone);
            values.setValue(WorldFile.WORLD_FILE_X_PIXEL_SIZE, pixelScaleX);
            values.setValue(WorldFile.WORLD_FILE_Y_PIXEL_SIZE, -pixelScaleY);

            //shift to center
            GeoCodec.ModelTiePoint[] tps = this.gc.getTiePoints();
            if (null != tps && tps.length > imageIndex)
            {
                GeoCodec.ModelTiePoint tp = tps[imageIndex];

                double xD = tp.getX() + (pixelScaleX / 2d);
                double yD = tp.getY() - (pixelScaleY / 2d);

                values.setValue(WorldFile.WORLD_FILE_X_LOCATION, xD);
                values.setValue(WorldFile.WORLD_FILE_Y_LOCATION, yD);
            }

            values.setValue(AVKey.SECTOR, ImageUtil.calcBoundingBoxForUTM(values));
        }
        else
        {
            String msg = Logging.getMessage("Geotiff.UnknownGeoKeyValue", gtModelTypeGeoKey, GeoTiff.GeoKey.ModelType);
            Logging.logger().severe(msg);
//            throw new IOException(msg);
        }
    }

    /*
     * Reads an ImageFileDirectory and places it in our list.  It is assumed the caller has
     * prepositioned the file to the first entry (i.e., just past the short designating the
     * number of entries).
     *
     * Calls itself recursively if additional IFDs are indicated.
     *
     */

    private void readIFD(int numEntries) throws IOException
    {
        try
        {
            if (null == this.tiffIFDs)
            {
                this.tiffIFDs = new ArrayList<TiffIFDEntry[]>();
            }

            java.util.List<TiffIFDEntry> ifd = new ArrayList<TiffIFDEntry>();
            for (int i = 0; i < numEntries; i++)
            {
                ifd.add(TIFFIFDFactory.create(this.theChannel, this.tiffReader.getByteOrder()));
            }

            TiffIFDEntry[] array = ifd.toArray(new TiffIFDEntry[ifd.size()]);
            this.tiffIFDs.add(array);

            if (null == this.metadata)
            {
                this.metadata = new ArrayList<AVList>();
            }
            this.metadata.add(new AVListImpl());

            ByteBuffer bb = ByteBuffer.allocate(4).order(this.tiffReader.getByteOrder());
            this.theChannel.read(bb);
            bb.flip();

            // If there's another IFD in this file, go get it (recursively)...
            long nextIFDOffset = TIFFReader.getUnsignedInt(bb);
            if (nextIFDOffset > 0)
            {
                this.theChannel.position(nextIFDOffset);
                bb.clear().limit(2);
                this.theChannel.read(bb);
                bb.flip();
                readIFD(bb.getShort());
            }
        }
        catch (Exception ex)
        {
            String message = Logging.getMessage("GeotiffReader.BadIFD", ex.getMessage());
            Logging.logger().severe(message);
            throw new IOException(message);
        }
    }

    /*
    * Returns the (first!) IFD-Entry with the given tag, or null if not found.
    *
    */

    private TiffIFDEntry getByTag(TiffIFDEntry[] ifd, int tag)
    {
        for (TiffIFDEntry anIfd : ifd)
        {
            if (anIfd.tag == tag)
            {
                return anIfd;
            }
        }
        return null;
    }

    /*
     * We need to check for a valid image index in several places. Consolidate that all here.
     * We throw an IllegalArgumentException if the index is not valid, otherwise, silently return.
     *
     */

    private void checkImageIndex(int imageIndex) throws IOException
    {
        if (imageIndex < 0 || imageIndex >= getNumImages())
        {
            String message = Logging.getMessage("GeotiffReader.BadImageIndex", imageIndex, 0, getNumImages());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    /*
     * Make sure we release this resource...
     *
     */
    public void dispose()
    {
        try
        {
            WWIO.closeStream(this.theChannel, this.sourceFilename);
            WWIO.closeStream(this.sourceFile, this.sourceFilename);
        }
        catch (Throwable t)
        {
            String message = t.getMessage();
            message = (WWUtil.isEmpty(message)) ? t.getCause().getMessage() : message;
            Logging.logger().log(java.util.logging.Level.FINEST, message, t);
        }
    }

    protected void finalize() throws Throwable
    {
        this.dispose();
        super.finalize();
    }
}