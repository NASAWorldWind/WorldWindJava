/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.Logging;

import java.nio.*;

/**
 * @author Patrick Murris
 * @version $Id: DBaseField.java 1867 2014-03-14 18:52:11Z dcollins $
 */
public class DBaseField
{
    public static final String TYPE_CHAR = "DBase.FieldTypeChar";
    public static final String TYPE_NUMBER = "DBase.FieldTypeNumber";
    public static final String TYPE_DATE = "DBase.FieldTypeDate";
    public static final String TYPE_BOOLEAN = "DBase.FieldTypeBoolean";
    protected static final int FIELD_NAME_LENGTH = 11;

    private String name;
    private String type;
    private char typeCode;
    private int length;
    private int decimals;

    public DBaseField(DBaseFile dbaseFile, ByteBuffer buffer)
    {
        if (dbaseFile == null)
        {
            String message = Logging.getMessage("nullValue.DBaseFileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.readFromBuffer(dbaseFile, buffer);
    }

    public String getName()
    {
        return this.name;
    }

    public String getType()
    {
        return this.type;
    }

    public int getLength()
    {
        return this.length;
    }

    public int getDecimals()
    {
        return this.decimals;
    }

    protected void readFromBuffer(DBaseFile dbaseFile, ByteBuffer buffer)
    {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int pos = buffer.position();

        byte[] bytes = new byte[FIELD_NAME_LENGTH];
        int numRead = dbaseFile.readZeroTerminatedString(buffer, bytes, FIELD_NAME_LENGTH);
        this.name = dbaseFile.decodeString(bytes, numRead);

        this.typeCode = (char) buffer.get();
        this.type = getFieldType(this.typeCode);
        if (this.type == null)
        {
            String message = Logging.getMessage("SHP.UnsupportedDBaseFieldType", this.typeCode);
            Logging.logger().log(java.util.logging.Level.SEVERE, message);
            throw new WWRuntimeException(message);
        }

        // Skip four bytes
        buffer.getInt();

        this.length = 0xff & buffer.get();    // unsigned
        this.decimals = 0xff & buffer.get();

        buffer.position(pos + DBaseFile.FIELD_DESCRIPTOR_LENGTH); // move to next field
    }

    public static String getFieldType(char type)
    {
        switch (type)
        {
            case 'C':
                return TYPE_CHAR;
            case 'D':
                return TYPE_DATE;
            case 'F':
                return TYPE_NUMBER;
            case 'L':
                return TYPE_BOOLEAN;
            case 'N':
                return TYPE_NUMBER;
            default:
                return null;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append("(").append(this.typeCode).append(")");

        return sb.toString();
    }
}
