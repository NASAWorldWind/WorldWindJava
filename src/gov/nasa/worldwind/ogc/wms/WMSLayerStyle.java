/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
