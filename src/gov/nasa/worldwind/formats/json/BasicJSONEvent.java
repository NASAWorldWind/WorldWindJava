/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

import gov.nasa.worldwind.util.Logging;
import org.codehaus.jackson.*;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: BasicJSONEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicJSONEvent implements JSONEvent
{
    protected final JsonToken token;
    protected final String fieldName;
    protected final Object scalarValue;

    public BasicJSONEvent(JsonParser parser, JsonToken token, String fieldName) throws IOException
    {
        if (parser == null)
        {
            String message = Logging.getMessage("nullValue.ParserIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (token == null)
        {
            String message = Logging.getMessage("nullValue.TokenIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.token = token;
        this.fieldName = fieldName;

        if (this.token.isScalarValue())
        {
            if (this.token == JsonToken.VALUE_NULL)
                this.scalarValue = null;

            else if (this.token == JsonToken.VALUE_STRING)
                this.scalarValue = parser.getText();

            else if (this.token == JsonToken.VALUE_NUMBER_INT)
                this.scalarValue = parser.getIntValue();

            else if (this.token == JsonToken.VALUE_NUMBER_FLOAT)
                this.scalarValue = parser.getDoubleValue();

            else if (this.token == JsonToken.VALUE_TRUE || token == JsonToken.VALUE_FALSE)
                this.scalarValue = parser.getBooleanValue();

            else if (this.token == JsonToken.VALUE_EMBEDDED_OBJECT)
                this.scalarValue = parser.getEmbeddedObject();

            else
            {
                Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", this.token));
                this.scalarValue = null;
            }
        }
        else
        {
            this.scalarValue = null;
        }
    }

    public boolean isStartObject()
    {
        return this.token == JsonToken.START_OBJECT;
    }

    public boolean isEndObject()
    {
        return this.token == JsonToken.END_OBJECT;
    }

    public boolean isStartArray()
    {
        return this.token == JsonToken.START_ARRAY;
    }

    public boolean isEndArray()
    {
        return this.token == JsonToken.END_ARRAY;
    }

    public boolean isFieldName()
    {
        return this.token == JsonToken.FIELD_NAME;
    }

    public boolean isScalarValue()
    {
        return this.token.isScalarValue();
    }

    public boolean isNumericValue()
    {
        return this.token == JsonToken.VALUE_NUMBER_INT || this.token == JsonToken.VALUE_NUMBER_FLOAT;
    }

    public String getFieldName()
    {
        return this.fieldName;
    }

    public Object asScalarValue()
    {
        return this.scalarValue;
    }

    public double asNumericValue()
    {
        return ((Number) this.scalarValue).doubleValue();
    }

    @Override
    public String toString()
    {
        return this.token.asString();
    }
}
