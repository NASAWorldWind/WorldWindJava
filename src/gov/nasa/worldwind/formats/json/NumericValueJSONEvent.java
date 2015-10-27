/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: NumericValueJSONEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NumericValueJSONEvent implements JSONEvent
{
    protected final String fieldName;
    protected final double numericValue;

    public NumericValueJSONEvent(String fieldName, double value) throws IOException
    {
        this.fieldName = fieldName;
        this.numericValue = value;
    }

    public boolean isStartObject()
    {
        return false;
    }

    public boolean isEndObject()
    {
        return false;
    }

    public boolean isStartArray()
    {
        return false;
    }

    public boolean isEndArray()
    {
        return false;
    }

    public boolean isFieldName()
    {
        return false;
    }

    public boolean isScalarValue()
    {
        return true;
    }

    public boolean isNumericValue()
    {
        return true;
    }

    public String getFieldName()
    {
        return this.fieldName;
    }

    public Object asScalarValue()
    {
        return this.numericValue;
    }

    public double asNumericValue()
    {
        return this.numericValue;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.numericValue);
    }
}
