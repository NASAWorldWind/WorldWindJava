/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

/**
 * @author dcollins
 * @version $Id: JSONEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface JSONEvent
{
    boolean isStartObject();

    boolean isEndObject();

    boolean isStartArray();

    boolean isEndArray();

    boolean isFieldName();

    boolean isScalarValue();

    boolean isNumericValue();

    String getFieldName();

    Object asScalarValue();

    double asNumericValue();
}
