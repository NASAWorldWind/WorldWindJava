/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.Version;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.tiff.GeoTiff;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import java.util.Calendar;

/**
 * @author dcollins
 * @version $Id: ByteBufferRaster.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ByteBufferRaster extends BufferWrapperRaster
{
    private java.nio.ByteBuffer byteBuffer;

    public ByteBufferRaster(int width, int height, Sector sector, java.nio.ByteBuffer byteBuffer, AVList list)
    {
        super(width, height, sector, BufferWrapper.wrap(byteBuffer, list), list);

        this.byteBuffer = byteBuffer;

        this.validateParameters(list);
    }

    private void validateParameters(AVList list) throws IllegalArgumentException
    {
        this.doValidateParameters(list);
    }

    protected void doValidateParameters(AVList list) throws IllegalArgumentException
    {
    }

    public ByteBufferRaster(int width, int height, Sector sector, AVList params)
    {
        this(width, height, sector, createCompatibleBuffer(width, height, params), params);
    }

    public static java.nio.ByteBuffer createCompatibleBuffer(int width, int height, AVList params)
    {
        if (width < 1)
        {
            throw new IllegalArgumentException();
        }
        if (height < 1)
        {
            throw new IllegalArgumentException();
        }
        if (params == null)
        {
            throw new IllegalArgumentException();
        }

        Object dataType = params.getValue(AVKey.DATA_TYPE);

        int sizeOfDataType = 0;
        if (AVKey.INT8.equals(dataType))
            sizeOfDataType = (Byte.SIZE / 8);
        else if (AVKey.INT16.equals(dataType))
            sizeOfDataType = (Short.SIZE / 8);
        else if (AVKey.INT32.equals(dataType))
            sizeOfDataType = (Integer.SIZE / 8);
        else if (AVKey.FLOAT32.equals(dataType))
            sizeOfDataType = (Float.SIZE / 8);

        int sizeInBytes = sizeOfDataType * width * height;
        return java.nio.ByteBuffer.allocate(sizeInBytes);
    }

    public java.nio.ByteBuffer getByteBuffer()
    {
        return this.byteBuffer;
    }

    public static DataRaster createGeoreferencedRaster(AVList params)
    {
        if (null == params)
        {
            throw new IllegalArgumentException();
        }

        if (!params.hasKey(AVKey.WIDTH))
        {
            throw new IllegalArgumentException();
        }

        int width = (Integer) params.getValue(AVKey.WIDTH);

        if (!(width > 0))
        {
            throw new IllegalArgumentException();
        }

        if (!params.hasKey(AVKey.HEIGHT))
        {
            throw new IllegalArgumentException();
        }

        int height = (Integer) params.getValue(AVKey.HEIGHT);

        if (!(height > 0))
        {
            throw new IllegalArgumentException();
        }

        if (!params.hasKey(AVKey.SECTOR))
        {
            throw new IllegalArgumentException();
        }

        Sector sector = (Sector) params.getValue(AVKey.SECTOR);
        if (null == sector)
        {
            throw new IllegalArgumentException();
        }

        if (!params.hasKey(AVKey.COORDINATE_SYSTEM))
        {
            // assume Geodetic Coordinate System
            params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
        }

        String cs = params.getStringValue(AVKey.COORDINATE_SYSTEM);
        if (!params.hasKey(AVKey.PROJECTION_EPSG_CODE))
        {
            if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
            {
                // assume WGS84
                params.setValue(AVKey.PROJECTION_EPSG_CODE, GeoTiff.GCS.WGS_84);
            }
            else
            {
                    throw new IllegalArgumentException();
            }
        }

        // if PIXEL_WIDTH is specified, we are not overriding it because UTM images
        // will have different pixel size
        if (!params.hasKey(AVKey.PIXEL_WIDTH))
        {
            if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
            {
                double pixelWidth = sector.getDeltaLonDegrees() / (double) width;
                params.setValue(AVKey.PIXEL_WIDTH, pixelWidth);
            }
            else
            {
                    throw new IllegalArgumentException();
            }
        }

        // if PIXEL_HEIGHT is specified, we are not overriding it
        // because UTM images will have different pixel size
        if (!params.hasKey(AVKey.PIXEL_HEIGHT))
        {
            if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
            {
                double pixelHeight = sector.getDeltaLatDegrees() / (double) height;
                params.setValue(AVKey.PIXEL_HEIGHT, pixelHeight);
            }
            else
            {
                    throw new IllegalArgumentException();
            }
        }

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
        {
            throw new IllegalArgumentException();
        }
        else
        {
            String pixelFormat = params.getStringValue(AVKey.PIXEL_FORMAT);
            if (!AVKey.ELEVATION.equals(pixelFormat) && !AVKey.IMAGE.equals(pixelFormat))
            {
                    throw new IllegalArgumentException();
            }
        }

        if (!params.hasKey(AVKey.DATA_TYPE))
        {
            throw new IllegalArgumentException();
        }

        // validate elevation parameters
        if (AVKey.ELEVATION.equals(params.getValue(AVKey.PIXEL_FORMAT)))
        {
            String type = params.getStringValue(AVKey.DATA_TYPE);
            if (!AVKey.FLOAT32.equals(type) && !AVKey.INT16.equals(type))
            {
                    throw new IllegalArgumentException();
            }
        }

        if (!params.hasKey(AVKey.ORIGIN) && AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
        {
            // set UpperLeft corner as the origin, if not specified
            LatLon origin = new LatLon(sector.getMaxLatitude(), sector.getMinLongitude());
            params.setValue(AVKey.ORIGIN, origin);
        }

        if (!params.hasKey(AVKey.BYTE_ORDER))
        {
            throw new IllegalArgumentException();
        }

        if (!params.hasKey(AVKey.DATE_TIME))
        {
            // add NUL (\0) termination as required by TIFF v6 spec (20 bytes length)
            String timestamp = String.format("%1$tY:%1$tm:%1$td %tT\0", Calendar.getInstance());
            params.setValue(AVKey.DATE_TIME, timestamp);
        }

        if (!params.hasKey(AVKey.VERSION))
        {
            params.setValue(AVKey.VERSION, Version.getVersion());
        }

        return new ByteBufferRaster(width, height, sector, params);
    }
}
