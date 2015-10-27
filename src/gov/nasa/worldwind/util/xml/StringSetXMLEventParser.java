/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.WWUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Parses a list of XML strings.
 *
 * @author tag
 * @version $Id: StringSetXMLEventParser.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class StringSetXMLEventParser extends AbstractXMLEventParser implements Iterable<String>
{
    protected QName elementName;
    protected Set<String> strings = new HashSet<String>();

    public StringSetXMLEventParser()
    {
    }

    public StringSetXMLEventParser(String namespaceUri)
    {
        super(namespaceUri);
    }

    /**
     * Create a parser. All sub-elements of a specified name are parsed as strings and retained.
     *
     * @param namespaceURI the namespace URI to attach to this parser. May be null.
     * @param elementName  the name of the sub-elements that contain the strings.
     */
    public StringSetXMLEventParser(String namespaceURI, QName elementName)
    {
        super(namespaceURI);

        this.elementName = elementName;
    }

    @Override
    public XMLEventParser newInstance() throws Exception
    {
        StringSetXMLEventParser copy = (StringSetXMLEventParser) super.newInstance();
        if (copy != null)
            copy.elementName = this.elementName;

        return copy;
    }

    public Object parse(XMLEventParserContext ctx, XMLEvent listEvent, Object... args) throws XMLStreamException
    {
        this.strings.clear();

        return super.parse(ctx, listEvent, args);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, this.elementName))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.addString(s);
        }
    }

    public Iterator<String> iterator()
    {
        return this.strings.iterator();
    }

    public Set<String> getStrings()
    {
        return this.strings;
    }

    protected void addString(String string)
    {
        this.strings.add(string);
    }
}
