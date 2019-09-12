/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.WWUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * @author tag
 * @version $Id: IntegerXMLEventParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class IntegerXMLEventParser extends AbstractXMLEventParser
{
    public IntegerXMLEventParser()
    {
    }

    public IntegerXMLEventParser(String namespaceUri)
    {
        super(namespaceUri);
    }

    public Object parse(XMLEventParserContext ctx, XMLEvent integerEvent, Object... args) throws XMLStreamException
    {
        String s = this.parseCharacterContent(ctx, integerEvent);
        return s != null ? WWUtil.convertStringToInteger(s) : null;
    }
}
