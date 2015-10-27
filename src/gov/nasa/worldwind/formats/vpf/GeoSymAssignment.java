/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: GeoSymAssignment.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymAssignment
{
    protected String filePath;
    protected Map<String, GeoSymTable> tableMap = new HashMap<String, GeoSymTable>();

    protected static String[] tableNames =
        {
            GeoSymConstants.ATTRIBUTE_EXPRESSION_FILE,
            GeoSymConstants.CODE_VALUE_DESCRIPTION_FILE,
            GeoSymConstants.COLOR_ASSIGNMENT_FILE,
            GeoSymConstants.FULL_SYMBOL_ASSIGNMENT_FILE,
            GeoSymConstants.SIMPLIFIED_SYMBOL_ASSIGNMENT_FILE,
            //GeoSymConstants.TEXT_ABBREVIATIONS_ASSIGNMENT_FILE, // Has a unique file format. See MIL-DTL-89045 3.5.3.1.3.4
            GeoSymConstants.TEXT_LABEL_CHARACTERISTICS_FILE,
            GeoSymConstants.TEXT_LABEL_JOIN_FILE,
            GeoSymConstants.TEXT_LABEL_LOCATION_FILE,
        };

    public GeoSymAssignment()
    {
    }

    public static GeoSymAssignment fromFile(String filePath)
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GeoSymAssignment assignment = new GeoSymAssignment();
        assignment.filePath = filePath;

        GeoSymTableReader reader = new GeoSymTableReader();

        for (String name : tableNames)
        {
            GeoSymTable table = reader.read(getTablePath(filePath, name));
            if (table != null)
                assignment.putTable(name, table);
        }

        return assignment;
    }

    public static boolean isGeoSymAssignment(String filePath)
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GeoSymTableReader reader = new GeoSymTableReader();

        for (String name : tableNames)
        {
            if (!reader.canRead(getTablePath(filePath, name)))
                return false;
        }

        return true;
    }

    public String getFilePath()
    {
        return this.filePath;
    }

    public GeoSymTable getTable(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.tableMap.get(name);
    }

    public void putTable(String name, GeoSymTable table)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.tableMap.put(name, table);
    }

    protected static String getTablePath(String filePath, String tableName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(filePath);
        sb.append("/");
        sb.append(tableName);

        return sb.toString();
    }
}
