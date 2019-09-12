/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * Parses an OGC OnlineResource element.
 *
 * @author tag
 * @version $Id: OGCOnlineResource.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGCOnlineResource extends AbstractXMLEventParser
{
    protected QName HREF;
    protected QName TYPE;

    protected String type;
    protected String href;

    public OGCOnlineResource(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        HREF = new QName(WWXML.XLINK_URI, "href");
        TYPE = new QName(WWXML.XLINK_URI, "type");
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args)
    {
        Iterator iter = event.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (ctx.isSameAttributeName(attr.getName(), HREF))
                this.setHref(attr.getValue());
            else if (ctx.isSameAttributeName(attr.getName(), TYPE))
                this.setType(attr.getValue());
        }
    }

    public String getType()
    {
        return type;
    }

    protected void setType(String type)
    {
        this.type = type;
    }

    public String getHref()
    {
        return href;
    }

    protected void setHref(String href)
    {
        this.href = href;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("href: ").append(this.href != null ? this.href : "null");
        sb.append(", type: ").append(this.type != null ? this.type : "null");

        return sb.toString();
    }
}
