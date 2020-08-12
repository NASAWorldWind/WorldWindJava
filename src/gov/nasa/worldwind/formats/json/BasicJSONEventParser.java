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

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.Logging;

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
            String message = Logging.getMessage("nullValue.ParserContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (event == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (event.isStartObject())
            return this.parseObject(ctx, event);

        else if (event.isStartArray())
            return this.parseArray(ctx, event);

        else if (event.isScalarValue())
            return this.parseScalarContent(ctx, event);

        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", event));
            return null;
        }
    }

    protected JSONEventParser allocate(JSONEventParserContext ctx, JSONEvent event)
    {
        if (ctx == null)
        {
            String message = Logging.getMessage("nullValue.ParserContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (event == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
                Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", event));
        }

        return this.resolveObject(ctx, event);
    }

    protected void parseObjectField(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isFieldName())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
                Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", valueEvent));
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
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
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
            Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", event));
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
