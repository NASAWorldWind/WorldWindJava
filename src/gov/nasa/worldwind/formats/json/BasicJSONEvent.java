/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
