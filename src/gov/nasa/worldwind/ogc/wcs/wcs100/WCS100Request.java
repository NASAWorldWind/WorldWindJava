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
 * @version $Id: WCS100Request.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100Request extends AbstractXMLEventParser
{
    private static final String[] rNames = new String[]
        {
            "GetCapabilities", "DescribeCoverage", "GetCoverage"
        };

    protected List<WCS100RequestDescription> requests = new ArrayList<WCS100RequestDescription>(2);

    public WCS100Request(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<WCS100RequestDescription> getRequests()
    {
        return this.requests;
    }

    public WCS100RequestDescription getRequest(String requestName)
    {
        for (WCS100RequestDescription description : this.requests)
        {
            if (description.getRequestName().equalsIgnoreCase(requestName))
                return description;
        }

        return null;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        String requestName = this.isRequestName(ctx, event);
        if (requestName != null)
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCS100RequestDescription)
                {
                    ((WCS100RequestDescription) o).setRequestName(requestName);
                    this.requests.add((WCS100RequestDescription) o);
                }
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }

    protected String isRequestName(XMLEventParserContext ctx, XMLEvent event)
    {
        for (String requestName : rNames)
        {
            if (ctx.isStartElement(event, requestName))
                return requestName;
        }

        return null;
    }
}
