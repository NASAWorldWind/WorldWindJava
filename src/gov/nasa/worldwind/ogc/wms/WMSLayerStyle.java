/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Parses a WMS layer Style element.
 *
 * @author tag
 * @version $Id: WMSLayerStyle.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSLayerStyle extends AbstractXMLEventParser
{
    protected QName NAME;
    protected QName TITLE;
    protected QName ABSTRACT;
    protected QName LEGEND_URL;
    protected QName STYLE_SHEET_URL;
    protected QName STYLE_URL;

    protected String name;
    protected String title;
    protected String styleAbstract;
    protected WMSLayerInfoURL styleSheetURL;
    protected WMSLayerInfoURL styleURL;
    protected Set<WMSLogoURL> legendURLs;

    public WMSLayerStyle(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        NAME = new QName(this.getNamespaceURI(), "Name");
        TITLE = new QName(this.getNamespaceURI(), "Title");
        ABSTRACT = new QName(this.getNamespaceURI(), "Abstract");
        LEGEND_URL = new QName(this.getNamespaceURI(), "LegendURL");
        STYLE_SHEET_URL = new QName(this.getNamespaceURI(), "StyleSheetURL");
        STYLE_URL = new QName(this.getNamespaceURI(), "StyleURL");
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        XMLEventParser parser = super.allocate(ctx, event);
        if (parser != null)
            return parser;

        if (ctx.isStartElement(event, LEGEND_URL))
            defaultParser = new WMSLogoURL(this.getNamespaceURI());
        else if (ctx.isStartElement(event, STYLE_SHEET_URL))
            defaultParser = new WMSLayerInfoURL(this.getNamespaceURI());
        else if (ctx.isStartElement(event, STYLE_URL))
            defaultParser = new WMSLayerInfoURL(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, TITLE))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setTitle(s);
        }
        else if (ctx.isStartElement(event, NAME))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setName(s);
        }
        else if (ctx.isStartElement(event, ABSTRACT))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setStyleAbstract(s);
        }
        else if (ctx.isStartElement(event, LEGEND_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLogoURL)
                    this.addLegendURL((WMSLogoURL) o);
            }
        }
        else if (ctx.isStartElement(event, STYLE_SHEET_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerInfoURL)
                    this.setStyleSheetURL((WMSLayerInfoURL) o);
            }
        }
        else if (ctx.isStartElement(event, STYLE_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerInfoURL)
                    this.setStyleURL((WMSLayerInfoURL) o);
            }
        }
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }

    public String getTitle()
    {
        return title;
    }

    protected void setTitle(String title)
    {
        this.title = title;
    }

    public String getStyleAbstract()
    {
        return styleAbstract;
    }

    protected void setStyleAbstract(String styleAbstract)
    {
        this.styleAbstract = styleAbstract;
    }

    public WMSLayerInfoURL getStyleSheetURL()
    {
        return styleSheetURL;
    }

    protected void setStyleSheetURL(WMSLayerInfoURL styleSheetURL)
    {
        this.styleSheetURL = styleSheetURL;
    }

    public WMSLayerInfoURL getStyleURL()
    {
        return styleURL;
    }

    protected void setStyleURL(WMSLayerInfoURL styleURL)
    {
        this.styleURL = styleURL;
    }

    public Set<WMSLogoURL> getLegendURLs()
    {
        if (this.legendURLs != null)
            return legendURLs;
        else
            return Collections.emptySet();
    }

    protected void setLegendURLs(Set<WMSLogoURL> legendURLs)
    {
        this.legendURLs = legendURLs;
    }

    protected void addLegendURL(WMSLogoURL url)
    {
        if (this.legendURLs == null)
            this.legendURLs = new HashSet<WMSLogoURL>();

        this.getLegendURLs().add(url);
    }
}
