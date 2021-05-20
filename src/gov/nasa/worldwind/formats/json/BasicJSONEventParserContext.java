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
import java.util.*;

/**
 * @author dcollins
 * @version $Id: BasicJSONEventParserContext.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicJSONEventParserContext implements JSONEventParserContext
{
    protected JsonParser parser;
    protected boolean hasNext;
    protected JSONEvent nextEvent;
    protected Deque<String> fieldNameStack = new ArrayDeque<String>();
    protected Map<String, JSONEventParser> parsers = new HashMap<String, JSONEventParser>();

    public BasicJSONEventParserContext(JsonParser parser) throws IOException
    {
        if (parser == null)
        {
            String message = Logging.getMessage("nullValue.ParserIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.parser = parser;
        this.advance(); // Initializes hasNext and nextEvent.
    }

    public boolean hasNext()
    {
        return this.hasNext;
    }

    public JSONEvent nextEvent() throws IOException
    {
        JSONEvent e = this.nextEvent;
        this.advance();
        return e;
    }

    public JSONEvent peek()
    {
        return this.nextEvent;
    }

    public String getCurrentFieldName()
    {
        return this.fieldNameStack.peek();
    }

    public void pushFieldName(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.fieldNameStack.push(name);
    }

    public void popFieldName()
    {
        this.fieldNameStack.pop();
    }

    public JSONEventParser allocate(JSONEvent event)
    {
        return this.parsers.get(this.getCurrentFieldName()); // HashMap accepts the null key.
    }

    public JSONEventParser getUnrecognizedParser()
    {
        return new BasicJSONEventParser();
    }

    public void registerParser(String fieldName, BasicJSONEventParser parser)
    {
        this.parsers.put(fieldName, parser);
    }

    protected void advance() throws IOException
    {
        this.parser.nextToken();

        if (!this.parser.hasCurrentToken())
        {
            this.hasNext = false;
            this.nextEvent = null;
        }
        else
        {
            this.hasNext = true;
            this.nextEvent = this.createEvent(this.parser.getCurrentToken());
        }
    }

    protected JSONEvent createEvent(JsonToken token) throws IOException
    {
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT)
        {
            return new NumericValueJSONEvent(this.parser.getCurrentName(), this.parser.getDoubleValue());
        }
        else
        {
            return new BasicJSONEvent(this.parser, token, this.parser.getCurrentName());
        }
    }
}
