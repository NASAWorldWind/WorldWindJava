/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100RequestDescription.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100RequestDescription extends AbstractXMLEventParser
{
    protected String requestName;
    protected List<WCS100DCPType> dcpTypes = new ArrayList<WCS100DCPType>(2);

    public WCS100RequestDescription(String namespaceURI)
    {
        super(namespaceURI);
    }

    public void setRequestName(String requestName)
    {
        this.requestName = requestName;
    }

    public String getRequestName()
    {
        return this.requestName;
    }

    public List<WCS100DCPType> getDCPTypes()
    {
        return this.dcpTypes;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "DCPType"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCS100DCPType)
                    this.dcpTypes.add((WCS100DCPType) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
