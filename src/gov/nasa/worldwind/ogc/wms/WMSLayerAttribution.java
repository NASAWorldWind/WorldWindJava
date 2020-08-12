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
