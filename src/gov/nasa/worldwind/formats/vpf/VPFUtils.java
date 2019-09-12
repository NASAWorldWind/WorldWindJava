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
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.DatabaseIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.LibraryIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.CoverageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (featureTableFilter == null)
        {
            String message = Logging.getMessage("nullValue.FilterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.RecordIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.RecordIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
