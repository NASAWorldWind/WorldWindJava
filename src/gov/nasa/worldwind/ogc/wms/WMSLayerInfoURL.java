/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.ogc.OGCOnlineResource;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * Parses a WMS layer info URL, including FeatureListURL, MetadataURL and DataURL. Provides the base class for
 * AuthorityURL and LogoURL.
 *
 * @author tag
 * @version $Id: WMSLayerInfoURL.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSLayerInfoURL extends AbstractXMLEventParser
{
    protected QName FORMAT;
    protected QName ONLINE_RESOURCE;

    protected OGCOnlineResource onlineResource;
    protected String name;
    protected String format;

    public WMSLayerInfoURL(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        FORMAT = new QName(this.getNamespaceURI(), "Format");
        ONLINE_RESOURCE = new QName(this.getNamespaceURI(), "OnlineResource");
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        if (ctx.isStartElement(event, ONLINE_RESOURCE))
            defaultParser = new OGCOnlineResource(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, FORMAT))
        {
            this.setFormat(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, ONLINE_RESOURCE))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCOnlineResource)
                    this.setOnlineResource((OGCOnlineResource) o);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args)
    {
        Iterator iter = event.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (attr.getName().getLocalPart().equals("name") && attr.getValue() != null)
                this.setName(attr.getValue());
        }
    }

    public OGCOnlineResource getOnlineResource()
    {
        return onlineResource;
    }

    protected void setOnlineResource(OGCOnlineResource onlineResource)
    {
        this.onlineResource = onlineResource;
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }

    public String getFormat()
    {
        return format;
    }

    protected void setFormat(String format)
    {
        this.format = format;
    }
}
