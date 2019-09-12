/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100Exception.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100Exception extends AbstractXMLEventParser
{
    protected List<String> formats = new ArrayList<String>(1);

    public WCS100Exception(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getFormats()
    {
        return this.formats;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "Format"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.formats.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
