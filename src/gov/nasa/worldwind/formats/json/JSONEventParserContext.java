/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.json;

import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: JSONEventParserContext.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface JSONEventParserContext
{
    boolean hasNext();

    JSONEvent nextEvent() throws IOException;

    JSONEvent peek();

    String getCurrentFieldName();

    void pushFieldName(String name);

    void popFieldName();

    JSONEventParser allocate(JSONEvent event);

    JSONEventParser getUnrecognizedParser();

    void registerParser(String fieldName, BasicJSONEventParser parser);
}
