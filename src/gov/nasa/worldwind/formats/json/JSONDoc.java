/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.shaded.org.codehaus.jackson.*;

import java.io.*;

/**
 * @author dcollins
 * @version $Id: JSONDoc.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class JSONDoc implements Closeable
{
    protected JsonParser jsonParser;
    protected Object rootObject;
    protected String displayName;

    public JSONDoc(Object source)
    {
        if (WWUtil.isEmpty(source))
        {
            throw new IllegalArgumentException();
        }

        try
        {
            this.displayName = WWIO.getSourcePath(source);
            this.initialize(source);
        }
        catch (Exception e)
        {
            throw new WWRuntimeException(e);
        }
    }

    protected void initialize(Object source) throws Exception
    {
        JsonFactory factory = new JsonFactory();
        this.jsonParser = factory.createJsonParser(WWIO.openStream(source));
    }

    public Object getRootObject()
    {
        return this.rootObject;
    }

    public void parse() throws IOException
    {
        if (this.jsonParser == null)
        {
            return;
        }

        JSONEventParserContext ctx = this.createEventParserContext(this.jsonParser);
        if (ctx == null)
        {
            return;
        }

        if (!ctx.hasNext())
            return;

        JSONEventParser rootParser = this.createRootObjectParser();
        if (rootParser == null)
        {
            return;
        }

        this.rootObject = rootParser.parse(ctx, ctx.nextEvent());
    }

    public void close()
    {
        if (this.jsonParser != null)
        {
            WWIO.closeStream(this.jsonParser, this.displayName);
            this.jsonParser = null;
        }
    }

    protected JSONEventParserContext createEventParserContext(JsonParser parser) throws IOException
    {
        return new BasicJSONEventParserContext(parser);
    }

    protected JSONEventParser createRootObjectParser() throws IOException
    {
        return new BasicJSONEventParser();
    }
}
