/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.tiff;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.geom.*;

import java.util.*;

/**
 * A class to bundle the GeoTiff structures found in a GeoTiff file, and to assist in the coding/decoding of those
 * structures.
 *
 * @author brownrigg
 * @version $Id: GeoCodec.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class GeoCodec
{
    private HashMap<Integer, GeoKeyEntry> geoKeys = null;

    // Collection of ModelTiePoints.
    private Vector<ModelTiePoint> tiePoints = new Vector<ModelTiePoint>(1);

    // ModelPixelScale values...
    private double xScale;
    private double yScale;
    private double zScale;

    private Matrix modelTransform;  // the ModelTransformation matrix

    private short[] shortParams;    // raw short parameters array
    private double[] doubleParams;  // raw double parameters array
    private byte[] asciiParams;     // raw ascii parameters array

    
    public GeoCodec()
    {

    }

    /**
     * Adds ModelTiePoints from an array. Recall that by definition, a tie point is a 6-tuple of <i,j,k><x,y,z> values.
     *
     * @param values A 6-tuple representing a Geotiff ModelTiePoint.
     * @throws IllegalArgumentException if values not a multiple of 6.
     */
    public void addModelTiePoints(double[] values) throws IllegalArgumentException
    {
        if (values == null || values.length == 0 || (values.length % 6) != 0)
        {
            String message = Logging.getMessage("GeoCodec.BadTiePoints");
            Logging.logger().severe(message);
            throw new UnsupportedOperationException(message);
        }

        for (int i = 0; i < values.length; i += 6)
        {
            addModelTiePoint(values[i], values[i + 1], values[i + 2], values[i + 3], values[i + 4], values[i + 5]);
        }
    }

    public void addModelTiePoint(double i, double j, double k, double x, double y, double z)
    {
        ModelTiePoint t = new ModelTiePoint(i, j, k, x, y, z);
        this.tiePoints.add(t);
    }

    public ModelTiePoint[] getTiePoints()
    {
        ModelTiePoint[] tiePoints = new ModelTiePoint[this.tiePoints.size()];
        return this.tiePoints.toArray(tiePoints);
    }

    /**
     * Sets the 3 ModelPixelScale values.
     *
     * @param values The ModelPixelScale values.
     * @throws IllegalArgumentException if values is not of length 3.
     */
    public void setModelPixelScale(double[] values)
    {
        if (values == null || values.length != 3)
        {
            String message = Logging.getMessage("GeoCodec.BadPixelValues");
            Logging.logger().severe(message);
            throw new UnsupportedOperationException(message);
        }

        this.setModelPixelScale( values[0], values[1], values[2] );
    }

    public void setModelPixelScale(double xScale, double yScale, double zScale)
    {
        this.xScale = xScale;
        this.yScale = yScale;
        this.zScale = zScale;
    }

    public double getModelPixelScaleX()
    {
        return this.xScale;
    }

    public double getModelPixelScaleY()
    {
        return this.yScale;
    }

    /**
     * Sets the ModelTransformation matrix. This is logically a 4x4 matrix of doubles by definition.
     *
     * @param matrix A logical 4x4 matrix, in row-major order.
     * @throws IllegalArgumentException if matrix is not of length 16.
     */
    public void setModelTransformation(double[] matrix) throws IllegalArgumentException
    {
        if (matrix == null || matrix.length != 16)
        {
            String message = Logging.getMessage("GeoCodec.BadMatrix");
            Logging.logger().severe(message);
            throw new UnsupportedOperationException(message);
        }

        this.setModelTransformation( Matrix.fromArray(matrix, 0, true) );
    }

    public void setModelTransformation(Matrix matrix)
    {
        this.modelTransform = matrix;

        // There could be only ModelTransformation (34264) or ModelTiePoints (33922) & ModelPixelScale(3355)
        // If we are here, we have ModelTransformation,
        // so let's calculate ModelTiePoints of the image origin (0,0)

        Matrix tp = this.modelTransform.multiply( Matrix.fromTranslation( 0d, 0d, 0d ));
        this.addModelTiePoint( 0d, 0d, 0d, tp.m14, tp.m24, tp.m34 );

        // Now, since ModelTiePoints (33922) & ModelPixelScale(3355) always go together
        // let's set ModelPixelScale values

        this.setModelPixelScale( matrix.m11 , matrix.m22, 0d );
    }

    /**
     * Returns the bounding box of an image that is width X height pixels, as determined by this GeoCodec. Returns
     * UnsupportedOperationException if the transformation can not be determined (see getXYZAtPixel()). The bounding Box
     * is returned as an array of double of length 4: [0] is x coordinate of upper-left corner [1] is y coordinate of
     * upper-left corner [2] is x coordinate of lower-right corner [3] is y coordinate of lower-right corner Note that
     * coordinate units are those of the underlying modeling transformation, and are not guaranteed to be in lon/lat.
     *
     * @param width  Width of a hypothetical image.
     * @param height Height of a hypothetical image.
     * @return Returns xUL, yUL, xLR, yLR of bounding box.
     * @throws UnsupportedOperationException if georeferencing can not be computed.
     */
    public double[] getBoundingBox(int width, int height) throws UnsupportedOperationException
    {
        double[] bbox = new double[4];
        double[] pnt = getXYAtPixel(0, 0);
        bbox[0] = pnt[0];
        bbox[1] = pnt[1];
        pnt = getXYAtPixel( height, width);
        bbox[2] = pnt[0];
        bbox[3] = pnt[1];
        return bbox;
    }

    /**
     * Returns the geocoordinates for a given pixel, as determined by the modeling coordinate tranformation embodied in
     * the GeoCodec.
     * <p/>
     * TODO: Also throws UnsupportedOperationException if this is anything other than a "simple" georeferenced mapping,
     * meaning that there's a single tie-point known about the point 0,0, we know the inter-pixel spacing, and there's
     * no rotation of the image required.  Geo referencing may also be specified via a general 4x4 matrix, or by a list
     * if tie-points, implying a rubbersheeting transformation. These two cases remain to be implemented.
     * <p/>
     *
     * @param row pixel-row index
     * @param col pixel-column index
     * @return double[2] containing x,y coordinate of pixel in modelling coordinate units.
     * @throws IllegalArgumentException      if row or column outside image bounds.
     * @throws UnsupportedOperationException if georeferencing can not be determined.
     */
    public double[] getXYAtPixel(int row, int col) throws UnsupportedOperationException
    {
        if (this.tiePoints.size() == 0 )
        {
            String message = Logging.getMessage("GeotiffReader.NotSimpleGeotiff");
            Logging.logger().severe(message);
            throw new UnsupportedOperationException(message);
        }

        double[] xy = new double[2];
        ModelTiePoint t = this.tiePoints.get(0);
        xy[0] = t.x + col * this.xScale;
        xy[1] = t.y - row * this.yScale;
        return xy;
    }

    /**
     * Gets the values of the given GeoKey as an array of ints.
     * <p/>
     * While this method handles the general case of multiple ints associated with a key, typically there will be only a
     * single value.
     *
     * @param key GeoKey value
     * @return Array of int values associated with the key, or null if the key was not found.
     * @throws IllegalArgumentException Thrown if the key does not embody integer values.
     */
    public int[] getGeoKeyAsInts(int key) throws IllegalArgumentException
    {
        int[] vals = null;
        GeoKeyEntry entry;
        if (this.geoKeys != null && (entry = this.geoKeys.get(key)) != null)
        {
            if (entry.array != this.shortParams)
            {
                String message = Logging.getMessage("GeoCodec.NotIntegerKey", key);
                Logging.logger().severe(message);
                throw new UnsupportedOperationException(message);
            }

            vals = new int[entry.count];
            for (int i = 0; i < vals.length; i++)
            {
                vals[i] = 0xffff & (int) this.shortParams[entry.offset + i];
            }
        }
        return vals;
    }


    /*
     * Returns true if the given key is a GeoKey in this file; false otherwise.
     */
    public boolean hasGeoKey(int key)
    {
        return (this.geoKeys != null && this.geoKeys.get(key) != null);
    }

    //
    // Package visibility. Not generally intended for use by end users.
    //
    void setGeokeys(short[] keys)
    {
        // Decode the geokey entries into our internal management structure. Recall that the keys are organized as
        // entries of 4 shorts, where the first 4-tuple contains versioning and the number of geokeys to follow.
        // The remaining entries look very much like regular Tiff tags.

        if (keys != null && keys.length > 4)
        {
            this.shortParams = new short[keys.length];
            System.arraycopy(keys, 0, this.shortParams, 0, keys.length);

            int numKeys = keys[3];
            this.geoKeys = new HashMap<Integer, GeoKeyEntry>();
            int i = 0;
            for (int k = 0; k < numKeys; k++ )
            {
                i += 4;
                int tag = 0x0000ffff & keys[i];
                int tagLoc = 0x0000ffff & keys[i + 1];
                if (tagLoc == 0)
                {
                    // value is in the 4th field of this entry...
                    this.geoKeys.put(tag, new GeoKeyEntry(tag, 1, i + 3, this.shortParams));
                }
                else
                {
                    // in this case, one or more values are given relative to one of the params arrays...
                    Object sourceArray = null;
                    if (tagLoc == GeoTiff.Tag.GEO_KEY_DIRECTORY)
                        sourceArray = this.shortParams;
                    else if (tagLoc == GeoTiff.Tag.GEO_DOUBLE_PARAMS)
                        sourceArray = this.doubleParams;
                    else if (tagLoc == GeoTiff.Tag.GEO_ASCII_PARAMS)
                        sourceArray = this.asciiParams;

                    if (sourceArray != null)
                        this.geoKeys.put(tag, new GeoKeyEntry(tag, 0x0000ffff & keys[i + 2],
                            0x0000ffff & keys[i + 3], sourceArray));
                }
            }
        }
    }

    //
    // Package visibility. Not generally intended for use by end users.
    //
    void setDoubleParams(double[] params)
    {
        this.doubleParams = new double[params.length];
        System.arraycopy(params, 0, this.doubleParams, 0, params.length);
    }

    //
    // Package visibility. Not generally intended for use by end users.
    //
    void setAsciiParams(byte[] params)
    {
        this.asciiParams = new byte[params.length];
        System.arraycopy(params, 0, this.asciiParams, 0, params.length);
    }

    /*
    * A class to bundle up ModelTiePoints. From the Geotiff spec, a ModelTiePoint is a 6-tuple that is an
    * association of the pixel <i,j,k> to the model coordinate <x,y,z>.
    *
    */
    public class ModelTiePoint
    {
        public double i, j, k, x, y, z;

        public ModelTiePoint(double i, double j, double k, double x, double y, double z)
        {
            this.i = i;
            this.j = j;
            this.k = k;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getRow()
        {
            return this.j;
        }

        public double getColumn()
        {
            return this.i;
        }

        public double getX()
        {
            return this.x;
        }

        public double getY()
        {
            return this.y;
        }
    }

    /*
     * A little class that we use to manage GeoKeys.
     */
    private class GeoKeyEntry
    {
        int tag;
        int count;
        int offset;
        Object array;  // a reference to one of the short/double/asciiParams arrays

        GeoKeyEntry(int tag, int count, int offset, Object array)
        {
            this.tag = tag;
            this.count = count;
            this.offset = offset;
            this.array = array;
        }
    }
}
