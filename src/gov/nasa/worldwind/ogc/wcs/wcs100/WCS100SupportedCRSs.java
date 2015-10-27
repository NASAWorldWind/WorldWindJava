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
 * @version $Id: WCS100SupportedCRSs.java 2062 2014-06-19 20:10:41Z tgaskins $
 */
public class WCS100SupportedCRSs extends AbstractXMLEventParser
{
    protected List<String> requestResponseCRSs = new ArrayList<String>(1);
    protected List<String> requestCRSs = new ArrayList<String>(1);
    protected List<String> responseCRSs = new ArrayList<String>(1);
    protected List<String> nativeCRSs = new ArrayList<String>(1);

    public WCS100SupportedCRSs(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getRequestResponseCRSs()
    {
        return requestResponseCRSs;
    }

    public List<String> getRequestCRSs()
    {
        return requestCRSs;
    }

    public List<String> getResponseCRSs()
    {
        return responseCRSs;
    }

    public List<String> getNativeCRSs()
    {
        return nativeCRSs;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "requestResponseCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.requestResponseCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "requestCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.requestCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "responseCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.responseCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "nativeCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.nativeCRSs.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
