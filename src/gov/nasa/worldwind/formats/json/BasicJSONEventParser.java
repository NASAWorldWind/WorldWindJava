/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

import gov.nasa.worldwind.avlist.*;

import java.io.IOException;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: BasicJSONEventParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicJSONEventParser implements JSONEventParser
{
    protected AVList fields;
    protected List<Object> array;

    public BasicJSONEventParser()
    {
    }

    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (ctx == null)
        {
            throw new IllegalArgumentException();
        }

        if (event == null)
        {
            throw new IllegalArgumentException();
        }

        if (event.isStartObject())
            return this.parseObject(ctx, event);

        else if (event.isStartArray())
            return this.parseArray(ctx, event);

        else if (event.isScalarValue())
            return this.parseScalarContent(ctx, event);

        else
        {
            return null;
        }
    }

    protected JSONEventParser allocate(JSONEventParserContext ctx, JSONEvent event)
    {
        if (ctx == null)
        {
            throw new IllegalArgumentException();
        }

        if (event == null)
        {
            throw new IllegalArgumentException();
        }

        return ctx.allocate(event);
    }

    //**************************************************************//
    //********************  Object Parsing  ************************//
    //**************************************************************//

    protected Object parseObject(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartObject())
        {
            throw new IllegalArgumentException();
        }

        for (event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isEndObject())
                break;

            else if (event.isFieldName())
                this.parseObjectField(ctx, event);

            else
            {
            }
        }

        return this.resolveObject(ctx, event);
    }

    protected void parseObjectField(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isFieldName())
        {
            throw new IllegalArgumentException();
        }

        ctx.pushFieldName(event.getFieldName());

        if (ctx.hasNext())
        {
            JSONEvent valueEvent = ctx.nextEvent();

            if (valueEvent.isStartObject() || valueEvent.isStartArray())
                this.addFieldContent(ctx, this.parseComplexContent(ctx, valueEvent));

            else if (valueEvent.isScalarValue())
                this.addFieldContent(ctx, this.parseScalarContent(ctx, valueEvent));

            else
            {
            }
        }
        else
        {
            this.addFieldContent(ctx, null);
        }

        ctx.popFieldName();
    }

    protected void addFieldContent(JSONEventParserContext ctx, Object value)
    {
        if (this.fields == null)
            this.fields = new AVListImpl();

        this.fields.setValue(ctx.getCurrentFieldName(), value);
    }

    protected Object resolveObject(JSONEventParserContext ctx, JSONEvent event)
    {
        return this.fields;
    }

    //**************************************************************//
    //********************  Array Parsing  *************************//
    //**************************************************************//

    protected Object parseArray(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartArray())
        {
            throw new IllegalArgumentException();
        }

        for (event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isEndArray())
                break;

            this.parseArrayEntry(ctx, event);
        }

        return this.resolveArray(ctx, event);
    }

    protected void parseArrayEntry(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (event.isStartObject() || event.isStartArray())
            this.addArrayEntry(this.parseComplexContent(ctx, event));

        else if (event.isScalarValue())
            this.addArrayEntry(this.parseScalarContent(ctx, event));

        else
        {
        }
    }

    protected void addArrayEntry(Object o)
    {
        if (this.array == null)
            this.array = new ArrayList<Object>();

        this.array.add(o);
    }

    protected Object resolveArray(JSONEventParserContext ctx, JSONEvent event)
    {
        return this.array.toArray(new Object[this.array.size()]);
    }

    //**************************************************************//
    //********************  Content Parsing  ************************//
    //**************************************************************//

    protected Object parseComplexContent(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        JSONEventParser parser = this.allocate(ctx, event);

        if (parser == null)
            parser = ctx.getUnrecognizedParser();

        return (parser != null) ? parser.parse(ctx, event) : null;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected Object parseScalarContent(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        return event.asScalarValue();
    }
}
