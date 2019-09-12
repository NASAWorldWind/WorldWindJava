/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A collection of static utility methods used by the example programs.
 *
 * @author tag
 * @version $Id: ExampleUtil.java 2425 2014-11-13 19:44:19Z dcollins $
 */
public class ExampleUtil
{
    /**
     * Unzips the sole entry in the specified zip file, and saves it in a temporary directory, and returns a File to the
     * temporary location.
     *
     * @param path   the path to the source file.
     * @param suffix the suffix to give the temp file.
     *
     * @return a {@link File} for the temp file.
     *
     * @throws IllegalArgumentException if the <code>path</code> is <code>null</code> or empty.
     */
    public static File unzipAndSaveToTempFile(String path, String suffix)
    {
        if (WWUtil.isEmpty(path))
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream stream = null;

        try
        {
            stream = WWIO.openStream(path);

            ByteBuffer buffer = WWIO.readStreamToBuffer(stream);
            File file = WWIO.saveBufferToTempFile(buffer, WWIO.getFilename(path));

            buffer = WWIO.readZipEntryToBuffer(file, null);
            return WWIO.saveBufferToTempFile(buffer, suffix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            WWIO.closeStream(stream, path);
        }

        return null;
    }

    /**
     * Saves the file at the specified path in a temporary directory and returns a File to the temporary location.  The
     * path may be one of the following: <ul> <li>{@link java.io.InputStream}</li> <li>{@link java.net.URL}</li>
     * <li>absolute {@link java.net.URI}</li> <li>{@link java.io.File}</li> <li>{@link String} containing a valid URL
     * description or a file or resource name available on the classpath.</li> </ul>
     *
     * @param path   the path to the source file.
     * @param suffix the suffix to give the temp file.
     *
     * @return a {@link File} for the temp file.
     *
     * @throws IllegalArgumentException if the <code>path</code> is <code>null</code> or empty.
     */
    public static File saveResourceToTempFile(String path, String suffix)
    {
        if (WWUtil.isEmpty(path))
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream stream = null;
        try
        {
            stream = WWIO.openStream(path);

            ByteBuffer buffer = WWIO.readStreamToBuffer(stream);
            return WWIO.saveBufferToTempFile(buffer, suffix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            WWIO.closeStream(stream, path);
        }

        return null;
    }

    /**
     * Causes the View attached to the specified WorldWindow to animate to the specified sector. The View starts
     * animating at its current location and stops when the sector fills the window.
     *
     * @param wwd    the WorldWindow who's View animates.
     * @param sector the sector to go to.
     *
     * @throws IllegalArgumentException if either the <code>wwd</code> or the <code>sector</code> are
     *                                  <code>null</code>.
     */
    public static void goTo(WorldWindow wwd, Sector sector)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Create a bounding box for the specified sector in order to estimate its size in model coordinates.
        Box extent = Sector.computeBoundingBox(wwd.getModel().getGlobe(),
            wwd.getSceneController().getVerticalExaggeration(), sector);

        // Estimate the distance between the center position and the eye position that is necessary to cause the sector to
        // fill a viewport with the specified field of view. Note that we change the distance between the center and eye
        // position here, and leave the field of view constant.
        Angle fov = wwd.getView().getFieldOfView();
        double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

        // Configure OrbitView to look at the center of the sector from our estimated distance. This causes OrbitView to
        // animate to the specified position over several seconds. To affect this change immediately use the following:
        // ((OrbitView) wwd.getView()).setCenterPosition(new Position(sector.getCentroid(), 0d));
        // ((OrbitView) wwd.getView()).setZoom(zoom);
        wwd.getView().goTo(new Position(sector.getCentroid(), 0d), zoom);
    }

    /**
     * Reads the file at the specified path as a list of floating point numbers encoded as comma delimited plain text,
     *
     * @param path the path to the source file.
     *
     * @return an array of floating point numbers extracted from the file.
     *
     * @throws IllegalArgumentException if the <code>path</code> is <code>null</code> or empty.
     */
    public static double[] readCommaDelimitedNumbers(String path)
    {
        if (WWUtil.isEmpty(path))
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<Double> list = new ArrayList<Double>();

        InputStream is = null;
        BufferedReader reader = null;
        try
        {
            is = WWIO.openFileOrResourceStream(path, null);
            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null)
            {
                for (String token : line.split(","))
                {
                    Double d = WWUtil.convertStringToDouble(token);
                    list.add(d);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            WWIO.closeStream(is, path);
            WWIO.closeStream(reader, path);
        }

        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            array[i] = list.get(i);
        }

        return array;
    }

    /**
     * Creates a 2D grid of random values with an optional smoothing parameter. Random grid values are in the range from
     * min to max, inclusive. Increasing the number of iterations results in a less uniform grid.
     *
     * @param width         the grid width.
     * @param height        the grid height.
     * @param min           the minimum random value.
     * @param max           the maximum random value.
     * @param numIterations the number of random iterations to perform, a larger number decreases grid uniformity.
     * @param smoothness    the amount of smoothing to apply to the random values. 0 indicates no smoothing, while 1
     *                      indicates the most smoothing.
     *
     * @return an array containing width * height floating point numbers in the range from min to max.
     */
    public static double[] createRandomGridValues(int width, int height, double min, double max, int numIterations,
        double smoothness)
    {
        int numValues = width * height;
        double[] values = new double[numValues];

        for (int i = 0; i < numIterations; i++)
        {
            double offset = 1d - (i / (double) numIterations);

            int x1 = (int) Math.round(Math.random() * (width - 1));
            int x2 = (int) Math.round(Math.random() * (width - 1));
            int y1 = (int) Math.round(Math.random() * (height - 1));
            int y2 = (int) Math.round(Math.random() * (height - 1));
            int dx1 = x2 - x1;
            int dy1 = y2 - y1;

            for (int y = 0; y < height; y++)
            {
                int dy2 = y - y1;
                for (int x = 0; x < width; x++)
                {
                    int dx2 = x - x1;

                    if ((dx2 * dy1 - dx1 * dy2) >= 0)
                        values[x + y * width] += offset;
                }
            }
        }

        smoothValues(width, height, values, smoothness);
        scaleValues(values, numValues, min, max);

        return values;
    }

    protected static void scaleValues(double[] values, int count, double minValue, double maxValue)
    {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < count; i++)
        {
            if (min > values[i])
                min = values[i];
            if (max < values[i])
                max = values[i];
        }

        for (int i = 0; i < count; i++)
        {
            values[i] = (values[i] - min) / (max - min);
            values[i] = minValue + values[i] * (maxValue - minValue);
        }
    }

    protected static void smoothValues(int width, int height, double[] values, double smoothness)
    {
        // top to bottom
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x, width, height, smoothness);
        }

        // bottom to top
        int lastRowOffset = (height - 1) * width;
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x + lastRowOffset, -width, height, smoothness);
        }

        // left to right
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, y * width, 1, width, smoothness);
        }

        // right to left
        int lastColOffset = width - 1;
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, lastColOffset + y * width, -1, width, smoothness);
        }
    }

    protected static void smoothBand(double[] values, int start, int stride, int count, double smoothness)
    {
        double prevValue = values[start];
        int j = start + stride;

        for (int i = 0; i < count - 1; i++)
        {
            values[j] = smoothness * prevValue + (1 - smoothness) * values[j];
            prevValue = values[j];
            j += stride;
        }
    }
}
