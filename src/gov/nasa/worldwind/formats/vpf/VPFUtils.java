/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author dcollins
 * @version $Id: VPFUtils.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFUtils
{
    public static VPFBufferedRecordData readTable(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException();
        }

        if (!file.exists())
        {
            return null;
        }

        try
        {
            VPFTableReader tableReader = new VPFTableReader();
            return tableReader.read(file);
        }
        catch (WWRuntimeException e)
        {
            // Exception already logged by VPFTableReader.
            return null;
        }
    }

    public static VPFDatabase readDatabase(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException();
        }

        if (!file.exists())
        {
            return null;
        }

        try
        {
            return VPFDatabase.fromFile(file.getPath());
        }
        catch (WWRuntimeException e)
        {
            // Exception already logged by VPFLibrary.
            return null;
        }
    }

    public static VPFLibrary readLibrary(VPFDatabase database, String name)
    {
        if (database == null)
        {
            throw new IllegalArgumentException();
        }

        if (name == null)
        {
            throw new IllegalArgumentException();
        }

        try
        {
            return VPFLibrary.fromFile(database, name);
        }
        catch (WWRuntimeException e)
        {
            // Exception already logged by VPFLibrary.
            return null;
        }
    }

    public static VPFCoverage readCoverage(VPFLibrary library, String name)
    {
        if (library == null)
        {
            throw new IllegalArgumentException();
        }

        if (name == null)
        {
            throw new IllegalArgumentException();
        }

        try
        {
            return VPFCoverage.fromFile(library, name);
        }
        catch (WWRuntimeException e)
        {
            // Exception already logged by VPFCoverage.
            return null;
        }
    }

    public static VPFFeatureClass[] readFeatureClasses(VPFCoverage coverage, FileFilter featureTableFilter)
    {
        if (coverage == null)
        {
            throw new IllegalArgumentException();
        }

        if (featureTableFilter == null)
        {
            throw new IllegalArgumentException();
        }

        VPFFeatureClassSchema[] schemas = coverage.getFeatureClasses(featureTableFilter);
        VPFFeatureClass[] cls = new VPFFeatureClass[schemas.length];

        VPFFeatureClassFactory factory = new VPFBasicFeatureClassFactory();
        for (int i = 0; i < schemas.length; i++)
        {
            cls[i] = factory.createFromSchema(coverage, schemas[i]);
        }

        return cls;
    }

    public static String readDelimitedText(ByteBuffer buffer, char delim)
    {
        if (buffer == null)
        {
            throw new IllegalArgumentException();
        }

        StringBuilder sb = new StringBuilder();
        int remain = buffer.remaining();

        int i;
        for (i = 0; i < remain; i++)
        {
            byte b = buffer.get();
            if (delim == (char) b)
                break;

            sb.append((char) b);
        }

        return (i < remain) ? sb.toString().trim() : null;
    }

    public static void checkAndSetValue(VPFRecord record, String paramName, String paramKey, AVList params)
    {
        if (record == null)
        {
            throw new IllegalArgumentException();
        }

        if (paramName == null)
        {
            throw new IllegalArgumentException();
        }

        if (paramKey == null)
        {
            throw new IllegalArgumentException();
        }

        if (params == null)
        {
            throw new IllegalArgumentException();
        }

        if (record.hasValue(paramName))
        {
            Object o = record.getValue(paramName);
            if (o != null)
                params.setValue(paramKey, o);
        }
    }

    /**
     * Returns the extent ("xmin", "ymin", "xmax", "ymax") for the specified row as a {@link VPFBoundingBox}.
     *
     * @param record the record to extract the bound attributes from.
     *
     * @return extent of the specified row.
     */
    public static VPFBoundingBox getExtent(VPFRecord record)
    {
        if (record == null)
        {
            throw new IllegalArgumentException();
        }

        return new VPFBoundingBox(
            ((Number) record.getValue("xmin")).doubleValue(),
            ((Number) record.getValue("ymin")).doubleValue(),
            ((Number) record.getValue("xmax")).doubleValue(),
            ((Number) record.getValue("ymax")).doubleValue());
    }

    public static String getFeatureTypeName(String tableName)
    {
        if (tableName == null)
        {
            throw new IllegalArgumentException();
        }

        String suffix = WWIO.getSuffix(tableName);
        if (suffix == null)
            return null;

        suffix = "." + suffix;

        if (suffix.equalsIgnoreCase(VPFConstants.POINT_FEATURE_TABLE))
            return VPFConstants.POINT_FEATURE_TYPE;
        else if (suffix.equalsIgnoreCase(VPFConstants.LINE_FEATURE_TABLE))
            return VPFConstants.LINE_FEATURE_TYPE;
        else if (suffix.equalsIgnoreCase(VPFConstants.AREA_FEATURE_TABLE))
            return VPFConstants.AREA_FEATURE_TYPE;
        else if (suffix.equalsIgnoreCase(VPFConstants.TEXT_FEATURE_TABLE))
            return VPFConstants.TEXT_FEATURE_TYPE;
        else if (suffix.equalsIgnoreCase(VPFConstants.COMPLEX_FEATURE_TABLE))
            return VPFConstants.COMPLEX_FEATURE_TYPE;

        return null;
    }
}
