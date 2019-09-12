/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Defines the interface for XML event parsers.
 *
 * @author tag
 * @version $Id: XMLEventParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface XMLEventParser
{
    /**
     * Parse the event and initialize the parser's values to those found in the event.
     *
     * @param context a current parser context.
     * @param event   the event to parse.
     * @param args    an optional list of arguments that may by used by subclasses.
     *
     * @return if parsing is successful, returns <code>this</code>, otherwise returns null.
     *
     * @throws XMLStreamException if an exception occurs during event-stream reading.
     */
    Object parse(XMLEventParserContext context, XMLEvent event, Object... args) throws XMLStreamException;

    /**
     * Creates a new empty parser instance of the same type. This is used by {@link
     * gov.nasa.worldwind.util.xml.XMLEventParserContext} when creating parsers associated with specific event types.
     * The returned parser has the same namespace as the instance creating it, but has empty fields for all other
     * values.
     *
     * @return a new parser instance. The namespace URI is the same as the creating parser, but all other fields are
     *         empty.
     *
     * @throws Exception if an error or exception occurs while attempting to create the parser.
     */
    XMLEventParser newInstance() throws Exception;

    /**
     * Specifies the parent parser of this parser.
     *
     * @param parent this parser's parent parser.
     */
    void setParent(XMLEventParser parent);

    /**
     * Returns this parser's parent parser.
     *
     * @return this parser's parent parser.
     */
    XMLEventParser getParent();

    /**
     * Release large resources used by the parser, but does not dispose of the parser itself. Call this method once its
     * data is no longer needed, such as after mappings and related representations are created. For example, if a
     * parser holds a large number of positions, and those positions are no longer needed after the positions are
     * converted to points or copied to other storage, this method releases the memory associated with the original
     * positions.
     */
    void freeResources();
}
