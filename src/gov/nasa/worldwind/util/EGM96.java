/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;

import java.io.*;

/**
 * Computes EGM96 geoid offsets.
 * <p>
 * A file with the offset grid must be passed to the constructor. This file must have 721 rows of 1440 2-byte integer
 * values. Each row corresponding to a latitude, with the first row corresponding to +90 degrees (90 North). The integer
 * values must be in centimeters.
 * <p>
 * Once constructed, the instance can be passed to {@link gov.nasa.worldwind.globes.EllipsoidalGlobe#applyEGMA96Offsets(String)}
 * to apply the offets to elevations produced by the globe.
 *
 * @author tag
 * @version $Id: EGM96.java 770 2012-09-13 02:48:23Z tgaskins $
 */
public class EGM96
{
    protected String offsetsFilePath;
    protected BufferWrapper deltas;

    /**
     * Construct an instance.
     *
     * @param offsetsFilePath a path pointing to a file with the geoid offsets. See the class description above for a
     *                        description of the file.
     * @throws java.io.IOException if there's a problem reading the file.
     */
    public EGM96(String offsetsFilePath) throws IOException
    {
        if (offsetsFilePath == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.offsetsFilePath = offsetsFilePath;

        this.loadOffsetFile();
    }

    protected void loadOffsetFile() throws IOException
    {
        InputStream is = WWIO.openFileOrResourceStream(this.offsetsFilePath, EGM96.class);
        if (is == null)
        {
            String msg = Logging.getMessage("generic.CannotOpenFile", this.offsetsFilePath);
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        try
        {
            AVList bufferParams = new AVListImpl();
            bufferParams.setValue(AVKey.DATA_TYPE, AVKey.INT16);
            bufferParams.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);
            this.deltas = BufferWrapper.wrap(WWIO.readStreamToBuffer(is, true), bufferParams);
        }
        catch (IOException e)
        {
            String msg = Logging.getMessage("generic.ExceptionAttemptingToReadFile", this.offsetsFilePath);
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            throw e;
        }
        finally
        {
            WWIO.closeStream(is, this.offsetsFilePath);
        }
    }

    // Description of the EGMA96 offsets file:
    // See http://earth-info.nga.mil/GandG/wgs84/gravitymod/egm96/binary/binarygeoid.html
    //    The total size of the file is 2,076,480 bytes. This file was created
    //    using an INTEGER*2 data type format and is an unformatted direct access
    //    file. The data on the file is arranged in records from north to south.
    //    There are 721 records on the file starting with record 1 at 90 N. The
    //    last record on the file is at latitude 90 S. For each record, there
    //    are 1,440 15 arc-minute geoid heights arranged by longitude from west to
    //    east starting at the Prime Meridian (0 E) and ending 15 arc-minutes west
    //    of the Prime Meridian (359.75 E). On file, the geoid heights are in units
    //    of centimeters. While retrieving the Integer*2 values on file, divide by
    //    100 and this will produce a geoid height in meters.

    protected static Angle INTERVAL = Angle.fromDegrees(15d / 60d); // 15' angle delta
    protected static int NUM_ROWS = 721;
    protected static int NUM_COLS = 1440;

    public double getOffset(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Return 0 for all offsets if the file failed to load. A log message of the failure will have been generated
        // by the load method.
        if (this.deltas == null)
            return 0;

        double lat = latitude.degrees;
        double lon = longitude.degrees >= 0 ? longitude.degrees : longitude.degrees + 360;

        int topRow = (int) ((90 - lat) / INTERVAL.degrees);
        if (lat <= -90)
            topRow = NUM_ROWS - 2;
        int bottomRow = topRow + 1;

        // Note that the number of columns does not repeat the column at 0 longitude, so we must force the right
        // column to 0 for any longitude that's less than one interval from 360, and force the left column to the
        // last column of the grid.
        int leftCol = (int) (lon / INTERVAL.degrees);
        int rightCol = leftCol + 1;
        if (lon >= 360 - INTERVAL.degrees)
        {
            leftCol = NUM_COLS - 1;
            rightCol = 0;
        }

        double latBottom = 90 - bottomRow * INTERVAL.degrees;
        double lonLeft = leftCol * INTERVAL.degrees;

        double ul = this.gePostOffset(topRow, leftCol);
        double ll = this.gePostOffset(bottomRow, leftCol);
        double lr = this.gePostOffset(bottomRow, rightCol);
        double ur = this.gePostOffset(topRow, rightCol);

        double u = (lon - lonLeft) / INTERVAL.degrees;
        double v = (lat - latBottom) / INTERVAL.degrees;

        double pll = (1.0 - u) * (1.0 - v);
        double plr = u * (1.0 - v);
        double pur = u * v;
        double pul = (1.0 - u) * v;

        double offset = pll * ll + plr * lr + pur * ur + pul * ul;

        return offset / 100d; // convert centimeters to meters
    }

    protected double gePostOffset(int row, int col)
    {
        int k = row * NUM_COLS + col;

        if (k >= this.deltas.length())
            System.out.println(k);

        return this.deltas.getInt(k);
    }
//
//    public static void main(String[] args)
//    {
//        EGM96 egm96 = new EGM96("config/EGM96.dat");
//
//        LatLon latLon = LatLon.fromDegrees(46.166195, -94.405447);
////        double offset = egm96.getOffset(latLon.getLatitude(), latLon.getLongitude());
////        double offset = egm96.getOffset(Angle.NEG90, Angle.POS360);
//        double offset = egm96.getOffset(Angle.fromDegrees(-89.9), Angle.fromDegrees(359.9));
//        System.out.println(offset / 100);
//    }
}
