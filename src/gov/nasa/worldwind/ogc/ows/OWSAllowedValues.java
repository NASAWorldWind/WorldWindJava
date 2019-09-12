/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: OWSAllowedValues.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSAllowedValues extends AbstractXMLEventParser
{
    protected List<String> values = new ArrayList<String>(2);

    public OWSAllowedValues(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getValues()
    {
        return this.values;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "Value"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.values.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
