/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.*;

/**
 * Utilities for working with shapefiles.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileUtils.java 2068 2014-06-20 21:33:09Z dcollins $
 */
public class ShapefileUtils
{
    public static Shapefile openZippedShapefile(File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream shpStream = null, shxStream = null, dbfStream = null, prjStream = null;

        ZipFile zipFile;
        try
        {
            zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements())
            {
                ZipEntry entry = zipEntries.nextElement();
                if (entry == null)
                    continue;

                if (entry.getName().toLowerCase().endsWith(Shapefile.SHAPE_FILE_SUFFIX))
                {
                    shpStream = zipFile.getInputStream(entry);
                }
                else if (entry.getName().toLowerCase().endsWith(Shapefile.INDEX_FILE_SUFFIX))
                {
                    shxStream = zipFile.getInputStream(entry);
                }
                else if (entry.getName().toLowerCase().endsWith(Shapefile.ATTRIBUTE_FILE_SUFFIX))
                {
                    dbfStream = zipFile.getInputStream(entry);
                }
                else if (entry.getName().toLowerCase().endsWith(Shapefile.PROJECTION_FILE_SUFFIX))
                {
                    prjStream = zipFile.getInputStream(entry);
                }
            }
        }
        catch (Exception e)
        {
            throw new WWRuntimeException(
                Logging.getMessage("generic.ExceptionAttemptingToReadFrom", file.getPath()), e);
        }

        if (shpStream == null)
        {
            String message = Logging.getMessage("SHP.UnrecognizedShapefile", file.getPath());
            Logging.logger().severe(message);
            throw new WWUnrecognizedException(message);
        }

        return new Shapefile(shpStream, shxStream, dbfStream, prjStream);
    }

    /**
     * Reads and returns an array of integers from a byte buffer.
     *
     * @param buffer     the byte buffer to read from.
     * @param numEntries the number of integers to read.
     *
     * @return the integers read.
     *
     * @throws IllegalArgumentException if the specified buffer reference is null.
     */
    public static int[] readIntArray(ByteBuffer buffer, int numEntries)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int[] array = new int[numEntries];
        for (int i = 0; i < numEntries; i++)
        {
            array[i] = buffer.getInt();
        }

        return array;
    }

    /**
     * Reads and returns an array of doubles from a byte buffer.
     *
     * @param buffer     the byte buffer to read from.
     * @param numEntries the number of doubles to read.
     *
     * @return the doubles read.
     *
     * @throws IllegalArgumentException if the specified buffer reference is null.
     */
    public static double[] readDoubleArray(ByteBuffer buffer, int numEntries)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] array = new double[numEntries];
        for (int i = 0; i < numEntries; i++)
        {
            array[i] = buffer.getDouble();
        }

        return array;
    }

    /**
     * Determines whether a shapefile record contains a height attribute and return it if it does.
     *
     * @param record the record to search.
     *
     * @return the height value if a height attribute is found, otherwise null.
     */
    public static Double extractHeightAttribute(ShapefileRecord record)
    {
        if (record.getAttributes() == null)
            return null;

        for (Map.Entry<String, Object> attr : record.getAttributes().getEntries())
        {
            String hKey = attr.getKey().trim().toLowerCase();
            if (!(hKey.equals("height") || hKey.equals("hgt")))
                continue;

            Object o = attr.getValue();
            if (o instanceof Number)
                return ((Number) o).doubleValue();

            if (o instanceof String)
                return WWUtil.convertStringToDouble(o.toString());
        }

        return null;
    }

    /**
     * Determines whether a shapefile's records contain a height attribute.
     *
     * @param shapefile the shapefile to search.
     *
     * @return true if the shapefile's records contain a height attribute, otherwise false.
     */
    public static boolean hasHeightAttribute(Shapefile shapefile)
    {
        Set<String> attrNames = shapefile.getAttributeNames();
        if (attrNames == null)
            return false;

        for (String name : attrNames)
        {
            if (name.equalsIgnoreCase("height") || name.equalsIgnoreCase("hgt"))
            {
                return true;
            }
        }

        return false;
    }
}
