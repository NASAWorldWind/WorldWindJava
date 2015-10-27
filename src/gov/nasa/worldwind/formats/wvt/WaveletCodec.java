/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.wvt;

import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * @author brownrigg
 * @version $Id: WaveletCodec.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class WaveletCodec
{
    private final int type;
    private final int resolutionX;
    private final int resolutionY;
    private byte[][] xform;
    public static final int TYPE_BYTE_GRAY  = 0x67726179; // ascii "gray"
    public static final int TYPE_3BYTE_BGR  = 0x72676220; // ascii "rgb "
    public static final int TYPE_4BYTE_ARGB = 0x61726762; // ascii "argb"
    /**
     * A suggested filename extension for wavelet-encodings.
     */
    public static final String WVT_EXT = ".wvt";

    private WaveletCodec(int type, int resolutionX, int resolutionY)
    {
        if (!isTypeValid(type))
        {
            String message = "Invalid type: " + type;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.type = type;
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
    }

    public final int getType()
    {
        return type;
    }

    /**
     * Returns the resolution of this wavelet encoding.
     *
     * @return resolution
     */
    public final int getResolutionX()
    {
        return this.resolutionX;
    }

    /**
     * Returns the resolution of this wavelet encoding.
     *
     * @return resolution
     */
    public final int getResolutionY()
    {
        return this.resolutionY;
    }

    /**
     * Reconstructs an image from this wavelet encoding at the given resolution. The specified resolution
     * must be a power of two, and must be less than or equal to the resolution of the encoding.
     *
     * This reconstruction algorithm was hinted at in:
     *
     *    "Principles of Digital Image Synthesis"
     *    Andrew Glassner
     *    1995, pp. 296
     *
     * @param resolution
     * @return reconstructed image.
     * @throws IllegalArgumentException
     */
    public BufferedImage reconstruct(int resolution) throws IllegalArgumentException {

        // Allocate memory for the BufferedImage
        int numBands = this.xform.length;
        int[][] imageData = new int[numBands][this.resolutionX * this.resolutionY];
        byte[][] imageBytes = new byte[numBands][this.resolutionX * this.resolutionY];

        // we need working buffers as large as 1/2 the output resolution...
        // Note how these are named after Glassner's convention...

        int res2 = (resolution/2) * (resolution/2);
        int[][] A = new int[numBands][res2];
        int[][] D = new int[numBands][res2];
        int[][] V = new int[numBands][res2];
        int[][] H = new int[numBands][res2];

        // Prime the process. Recall that the first byte of each channel is a color value, not
        // signed coefficients. So treat it as an unsigned value.
        for (int k=0; k < numBands; k++)
            imageData[k][0] = 0x000000ff & this.xform[k][0];

        int scale = 1;
        int offset = 1;
        do {
            // load up our A,D,V,H component arrays...
            int numVals = scale*scale;
            if (numVals >= resolution*resolution) break;

            int next = 0;
            for (int j=0; j<scale; j++) {
                for (int i=0; i<scale; i++, next++) {
                    for (int k=0; k<numBands; k++) {
                        A[k][next] = imageData[k][j*resolution + i];
                    }
                }
            }
            for (int i=0; i<numVals; i++, offset++) {
                for (int k=0; k<numBands; k++) {
                   H[k][i] = this.xform[k][offset];
                }
            }
            for (int i=0; i<numVals; i++, offset++) {
                for (int k=0; k<numBands; k++) {
                    V[k][i] = this.xform[k][offset];
                }
            }
            for (int i=0; i<numVals; i++, offset++) {
                for (int k=0; k<numBands; k++) {
                    D[k][i] = this.xform[k][offset];
                }
            }

            next = 0;
            for (int j = 0; j < scale; j++) {
                for (int i = 0; i < scale; i++, next++) {
                    for (int k = 0; k < numBands; k++) {
                        int a = A[k][next] + H[k][next] + V[k][next] + D[k][next];
                        int b = A[k][next] - H[k][next] + V[k][next] - D[k][next];
                        int c = A[k][next] + H[k][next] - V[k][next] - D[k][next];
                        int d = A[k][next] - H[k][next] - V[k][next] + D[k][next];
                        imageData[k][2*j*resolution + (i*2)] =  a;
                        imageData[k][2*j*resolution + (i*2) + 1] = b;
                        imageData[k][2*j*resolution + resolution + (i*2)] = c;
                        imageData[k][2*j*resolution + resolution + (i*2) + 1] = d;
                    }
                }
            }

            scale *= 2;
        } while (scale < resolution);

        // Copy to bytes and clamp to byte-range...
        for (int j = 0; j < resolution; j++) {
            for (int i = 0; i < resolution; i++) {
                for (int k = 0; k < numBands; k++) {
                    imageBytes[k][j*resolution+i] = (byte) Math.max(0, Math.min(255, imageData[k][j*resolution+i]));
                }
            }
        }

        // Finally, construct a BufferedImage...
        BandedSampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE, resolution, resolution, numBands);
        DataBufferByte dataBuff = new DataBufferByte(imageBytes, imageBytes[0].length);
        WritableRaster rast = Raster.createWritableRaster(sm, dataBuff, new Point(0, 0));
        int imageType = getBufferedImageType(this);
        BufferedImage image = new BufferedImage(resolution, resolution, imageType);
        image.getRaster().setRect(rast);

        return image;
    }

    public static java.nio.ByteBuffer save(WaveletCodec codec) throws IOException
    {
        if (codec == null)
        {
            String message = "WaveletCodec is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int length = (4 * Integer.SIZE) / 8;
        for (int k = 0; k < codec.xform.length; k++)
        {
            length += (codec.xform[k].length * Byte.SIZE) / 8;
        }

        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(length);
        buffer.putInt(codec.resolutionX);
        buffer.putInt(codec.resolutionY);
        buffer.putInt(codec.type);
        buffer.putInt(codec.xform.length); // Number of bands.
        for (int k = 0; k < codec.xform.length; k++)
        {
            buffer.put(codec.xform[k], 0, codec.xform[k].length);
        }
        buffer.flip();
        return buffer;
    }

    private static boolean isTypeValid(int type)
    {
        return type == TYPE_BYTE_GRAY
            || type == TYPE_3BYTE_BGR
            || type == TYPE_4BYTE_ARGB;
    }

    private static int getBufferedImageType(WaveletCodec codec)
    {
        if (codec == null)
        {
            String message = "WaveletCodec is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int biType = -1;
        switch (codec.type)
        {
            case TYPE_BYTE_GRAY:
                biType = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case TYPE_3BYTE_BGR:
                biType = BufferedImage.TYPE_3BYTE_BGR;
                break;
            case TYPE_4BYTE_ARGB:
                biType = BufferedImage.TYPE_4BYTE_ABGR;
                break;
        }
        return biType;
    }

    private static int getWaveletType(BufferedImage image)
    {
        if (image == null)
        {
            String message = "BufferedImage is null";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int type = -1;
        switch (image.getType())
        {
            case BufferedImage.TYPE_BYTE_GRAY:
                type = TYPE_BYTE_GRAY;
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
                type = TYPE_3BYTE_BGR;
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                type = TYPE_4BYTE_ARGB;
                break;
        }
        return type;
    }

    public static WaveletCodec load(java.nio.ByteBuffer buffer) throws IOException
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int resolutionX = buffer.getInt();
        int resolutionY = buffer.getInt();
        int type = buffer.getInt();
        if (!isTypeValid(type))
            throw new IllegalArgumentException("WaveletCodec.loadFully(): invalid encoding type");

        int numBands = buffer.getInt();
        byte[][] xform = new byte[numBands][resolutionX * resolutionY];
        for (int k = 0; k < numBands; k++)
        {
            buffer.get(xform[k], 0, xform[k].length);
        }

        WaveletCodec codec = new WaveletCodec(type, resolutionX, resolutionY);
        codec.xform = xform;
        return codec;
    }

    public static WaveletCodec loadPartial(java.nio.ByteBuffer buffer, int resolution) throws IOException
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int resolutionX = buffer.getInt();
        int resolutionY = buffer.getInt();
        if (resolution > resolutionX || resolution > resolutionY)
            throw new IllegalArgumentException("WaveletCodec.loadPartially(): input resolution greater than encoded image");

        int type = buffer.getInt();
        if (!isTypeValid(type))
            throw new IllegalArgumentException("WaveletCodec.loadPartially(): invalid encoding type");

        int numBands = buffer.getInt();
        byte[][] xform = new byte[numBands][resolution*resolution];
        for (int k = 0; k < numBands; k++) {
            buffer.position(4*(Integer.SIZE/Byte.SIZE) + k * (resolutionX * resolutionY));
            buffer.get(xform[k], 0, xform[k].length);
        }

        WaveletCodec codec = new WaveletCodec(type, resolutionX, resolutionY);
        codec.xform = xform;
        return codec;
    }

    /**
     * Creates a wavelet encoding from the given BufferedImage. The image must have dimensions that are
     * a power of 2. If the incoming image has at least 3 bands, the first three are assumed to be RGB channels.
     * If only one-band, it is assumed to be grayscale. The SampleModel component-type must be BYTE.
     *
     * @param image
     * @return
     * @throws IllegalArgumentException
     */
    public static WaveletCodec encode(BufferedImage image) throws IllegalArgumentException {

        if (image == null)
            throw new IllegalArgumentException("WaveletCodec.encode: null image");

        // Does image have the required resolution constraints?
        int xRes = image.getWidth();
        int yRes = image.getHeight();
        if (!WWMath.isPowerOfTwo(xRes) || !WWMath.isPowerOfTwo(yRes))
            throw new IllegalArgumentException("Image dimensions are not a power of 2");

        // Try to determine image type...
        SampleModel sampleModel = image.getSampleModel();
        int numBands = sampleModel.getNumBands();
        if ( !(numBands == 1 || numBands == 3 || numBands == 4) || sampleModel.getDataType() != DataBuffer.TYPE_BYTE)
            throw new IllegalArgumentException("Image is not of BYTE type, or not recognized as grayscale, RGB, or ARGB");

        int type = getWaveletType(image);
        if (!isTypeValid(type))
            throw new IllegalArgumentException("Image is not recognized as grayscale, RGB, or ARGB");                

        // Looks good to go;  grab the image data.  We'll need to make a copy, as we need some
        // temp working space and we don't want to corrupt the BufferedImage's data...

        int bandSize = xRes * yRes;
        //int next = 0;
        Raster rast = image.getRaster();
        //float[] dataElems = new float[numBands];
        float[][] imageData = new float[numBands][bandSize];

        for (int k = 0; k < numBands; k++) {
            rast.getSamples(0, 0, xRes, yRes, k, imageData[k]);
        }
        //for (int j = 0; j < yRes; j++) {
        //    for (int i = 0; i < xRes; i++) {
        //        rast.getPixel(i, j, dataElems);
        //        for (int k = 0; k < numBands; k++) {
        //            imageData[k][next] = dataElems[k];
        //        }
        //        ++next;
        //    }
        //}

        // We need some temporary work space the size of the image...
        float[][] workspace = new float[numBands][bandSize];

        // Perform the transformation...
        int level = 0;
        int xformXres = xRes;
        int xformYres = yRes;

        while (true) {
            ++level;

            if ( !(xformXres > 0 || xformYres > 0)) break;
            int halfXformXres = xformXres / 2;
            int halfXformYres = xformYres / 2;

            // transform along the rows...
            for (int j = 0; j < xformYres; j++) {

                int offset = j * yRes;      // IMPORTANT THAT THIS REFLECT SOURCE IMAGE, NOT THE CURRENT LEVEL!

                for (int i = 0; i < halfXformXres; i++) {
                    int indx1 = offset + i*2;
                    int indx2 = offset + i*2 + 1;

                    // horizontally...
                    for (int k = 0; k < numBands; k++) {
                        float average = (imageData[k][indx1] + imageData[k][indx2]) / 2f;
                        float detail = imageData[k][indx1] - average;
                        workspace[k][offset + i] = average;
                        workspace[k][offset + i + halfXformXres] = detail;
                    }
                }

            }

            // copy transformed data from this iteration back into our source arrays...
            for (int k=0; k < numBands; k++)
                System.arraycopy(workspace[k], 0, imageData[k], 0, workspace[k].length);

            // now transform along columns...
            for (int j = 0; j < xformXres; j++) {
                for (int i = 0; i < halfXformYres; i++) {
                    int indx1 = j + (i*2)*yRes;
                    int indx2 = j + (i*2+1)*yRes;

                    // horizontally...
                    for (int k = 0; k < numBands; k++) {
                        float average = (imageData[k][indx1] + imageData[k][indx2]) / 2f;
                        float detail = imageData[k][indx1] - average;
                        workspace[k][j + i*yRes] = average;
                        workspace[k][j + (i+halfXformYres)*yRes] = detail;
                    }
                }

            }

            xformXres /= 2;
            xformYres /= 2;

            // copy transformed data from this iteration back into our source arrays...
            for (int k=0; k < numBands; k++)
                System.arraycopy(workspace[k], 0, imageData[k], 0, workspace[k].length);
        }

        // Our return WaveletCodec...
        WaveletCodec codec = new WaveletCodec(type, xRes, yRes);
        codec.xform = new byte[numBands][bandSize];

        //
        // Rearrange in memory for optimal, hierarchical layout on disk, quantizing down to
        // byte values as we go.
        //

        // NOTE: the first byte of each channel is different; it represents the average color of the
        // overall image, and as such should be an unsigned quantity in the range 0..255.
        // All other values are signed coefficents, so the clamping boundaries are different.
        for (int k=0; k<numBands; k++)
            codec.xform[k][0] = (byte) Math.min(255, Math.max(0, Math.round(imageData[k][0])));

        int scale = 1;   // actually inverse of the magnification level...
        int next = 1;
        while (scale < xRes) {
            for (int subBlock = 0; subBlock < 3; subBlock++) {
                int colOffset = ((subBlock % 2) == 0) ? scale : 0;
                int rowOffset = (subBlock > 0) ? scale * xRes : 0;
                for (int j = 0; j < scale; j++) {
                    for (int i = 0; i < scale; i++, next++) {
                        int indx = rowOffset + colOffset + j*xRes + i;
                        for (int k = 0; k < numBands; k++) {
                           codec.xform[k][next] = (byte) Math.max(Byte.MIN_VALUE, Math.min(Byte.MAX_VALUE, Math.round(imageData[k][indx])));
                        }
                    }
                }
            }
            scale *= 2;
        }

        // Done!
        return codec;
    }

}
