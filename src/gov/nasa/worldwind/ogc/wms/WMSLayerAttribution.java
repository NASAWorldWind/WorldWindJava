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
import javax.xml.stream.events.XMLEvent;

/**
 * Parses a WMS layer Attribution element.
 *
 * @author tag
 * @version $Id: WMSLayerAttribution.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSLayerAttribution extends AbstractXMLEventParser
{
    protected QName TITLE;
    protected QName ONLINE_RESOURCE;
    protected QName LOGO_URL;

    protected String title;
    protected OGCOnlineResource onlineResource;
    protected WMSLogoURL logoURL;

    public WMSLayerAttribution(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        TITLE = new QName(this.getNamespaceURI(), "Title");
        ONLINE_RESOURCE = new QName(this.getNamespaceURI(), "OnlineResource");
        LOGO_URL = new QName(this.getNamespaceURI(), "LogoURL");
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        if (ctx.isStartElement(event, ONLINE_RESOURCE))
            defaultParser = new OGCOnlineResource(this.getNamespaceURI());
        else if (ctx.isStartElement(event, LOGO_URL))
            defaultParser = new WMSLayerInfoURL(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, TITLE))
        {
            this.setTitle(ctx.getStringParser().parseString(ctx, event));
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
        else if (ctx.isStartElement(event, LOGO_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLogoURL)
                    this.setLogoURL((WMSLogoURL) o);
            }
        }
    }

    public String getTitle()
    {
        return title;
    }

    protected void setTitle(String title)
    {
        this.title = title;
    }

    public OGCOnlineResource getOnlineResource()
    {
        return onlineResource;
    }

    protected void setOnlineResource(OGCOnlineResource onlineResource)
    {
        this.onlineResource = onlineResource;
    }

    public WMSLogoURL getLogoURL()
    {
        return logoURL;
    }

    protected void setLogoURL(WMSLogoURL logoURL)
    {
        this.logoURL = logoURL;
    }
}
