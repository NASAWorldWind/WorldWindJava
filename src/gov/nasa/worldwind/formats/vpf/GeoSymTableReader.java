/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.util.*;

/**
 * MIL-DTL-89045 3.5.3.1
 *
 * @author dcollins
 * @version $Id: GeoSymTableReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymTableReader
{
    public GeoSymTableReader()
    {
    }

    public boolean canRead(String filePath)
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object streamOrException = null;
        boolean result = false;

        try
        {
            streamOrException = WWIO.getFileOrResourceAsStream(filePath, this.getClass());
            result = (streamOrException != null && streamOrException instanceof InputStream);
        }
        finally
        {
            if (streamOrException instanceof InputStream)
            {
                WWIO.closeStream(streamOrException, filePath);
            }
        }

        return result;
    }

    public GeoSymTable read(String filePath)
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            return this.doRead(filePath);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("VPF.ExceptionAttemptingToReadTable", filePath);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    protected GeoSymTable doRead(String filePath) throws IOException
    {
        InputStream inputStream = null;
        GeoSymTable result = null;

        try
        {
            inputStream = WWIO.openFileOrResourceStream(filePath, this.getClass());
            result = this.readTable(filePath, inputStream);
        }
        finally
        {
            WWIO.closeStream(inputStream, filePath);
        }

        return result;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected GeoSymTable readTable(String filePath, InputStream inputStream)
    {
        Scanner scanner = new Scanner(inputStream);

        // Read the table header.
        GeoSymTableHeader header = new GeoSymTableHeader();
        this.readHeader(scanner, header);

        GeoSymTable table = new GeoSymTable(header);
        this.readRecords(scanner, table);

        return table;
    }

    protected void readHeader(Scanner scanner, GeoSymTableHeader header)
    {
        header.removeAllColumns();

        String string = scanner.nextLine();
        String[] tokens = string.split("[,:]");

        // Read the file name.
        String s = tokens[0].trim();
        if (s != null && !isEmpty(s))
            header.setFileName(s);

        // Read the description.
        s = tokens[1].trim();
        if (s != null && !isEmpty(s))
            header.setDescription(s);

        while (!(string = scanner.nextLine()).equals(";"))
        {
            GeoSymColumn col = this.readColumn(string);
            header.addColumn(col);
        }
    }

    protected GeoSymColumn readColumn(String string)
    {
        String[] tokens = string.split("[=,:]");

        String s = tokens[0].trim();
        if (s == null)
        {
            String message = Logging.getMessage("VPF.MissingColumnName");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        GeoSymColumn col = new GeoSymColumn(s);

        s = tokens[1].trim();
        if (s != null)
            col.setDataType(s);

        s = tokens[2].trim();
        if (s != null)
            col.setDataSize(s);

        s = tokens[3].trim();
        if (s != null)
            col.setDescription(s);

        s = tokens[4].trim();
        if (s != null && !isEmpty(s))
            col.setCodeRef(s);

        return col;
    }

    protected void readRecords(Scanner scanner, GeoSymTable table)
    {
        ArrayList<AVList> list = new ArrayList<AVList>();

        while (scanner.hasNextLine())
        {
            String s = scanner.nextLine().trim();
            if (s.length() == 0)
                continue;

            AVList record = new AVListImpl();
            this.readRecord(s, table, record);
            list.add(record);
        }

        AVList[] array = new AVList[list.size()];
        list.toArray(array);

        table.setRecords(array);
    }

    protected void readRecord(String string, GeoSymTable table, AVList record)
    {
        Collection<? extends GeoSymColumn> columns = table.getHeader().getColumns();
        String[] tokens = string.split("[|]");

        int index = 0;
        for (GeoSymColumn col : columns)
        {
            String s = (index < tokens.length) ? tokens[index++].trim() : null;
            Object o = null;

            if (col.getDataType().equalsIgnoreCase(GeoSymConstants.INTEGER))
            {
                if (s != null)
                    o = WWUtil.convertStringToInteger(s);
            }
            else if (col.getDataType().equalsIgnoreCase(GeoSymConstants.CHARACTER_STRING))
            {
                if (s != null && s.length() > 0)
                    o = s;
            }

            record.setValue(col.getName(), o);
        }
    }

    protected static boolean isEmpty(String s)
    {
        return s.length() == 0 || s.equals("-");
    }
}
