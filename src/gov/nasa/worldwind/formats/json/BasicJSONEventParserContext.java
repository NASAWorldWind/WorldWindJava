/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
