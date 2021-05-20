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

import gov.nasa.worldwind.ogc.*;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Parses the Capability section of a WMS capabilities document.
 *
 * @author tag
 * @version $Id: WMSCapabilityInformation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSCapabilityInformation extends OGCCapabilityInformation
{
    private static final String[] rNames = new String[]
        {
            "GetCapabilities", "GetMap", "GetFeatureInfo", "DescribeLayer", "GetLegendGraphic"
        };

    protected QName LAYER;

    protected List<QName> requestNames;
    protected List<WMSLayerCapabilities> layerCapabilities;

    public WMSCapabilityInformation(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        LAYER = new QName(this.getNamespaceURI(), "Layer");

        this.requestNames = new ArrayList<QName>(rNames.length);
        for (String name : rNames)
        {
            this.requestNames.add(new QName(this.getNamespaceURI(), name));
        }

        this.setLayerCapabilities(new ArrayList<WMSLayerCapabilities>());
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        if (ctx.isStartElement(event, LAYER))
            return ctx.allocate(event, new WMSLayerCapabilities(this.getNamespaceURI()));
        else
            return super.allocate(ctx, event);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, LAYER))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerCapabilities)
                {
                    WMSLayerCapabilities caps = (WMSLayerCapabilities) o;
                    caps.setEnclosingCapabilityInformation(this);
                    caps.resolveAttributes(null);
                    this.getLayerCapabilities().add(caps);
                }
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }

    protected boolean isRequestName(XMLEventParserContext ctx, QName name)
    {
        for (QName requestName : this.requestNames)
        {
            if (ctx.isSameName(requestName, name))
                return true;
        }

        return false;
    }

    public List<WMSLayerCapabilities> getLayerCapabilities()
    {
        return layerCapabilities;
    }

    protected void setLayerCapabilities(List<WMSLayerCapabilities> layerCapabilities)
    {
        this.layerCapabilities = layerCapabilities;
    }

    public Set<String> getImageFormats()
    {
        Set<OGCRequestDescription> requestDescriptions = this.getRequestDescriptions();
        for (OGCRequestDescription rd : requestDescriptions)
        {
            if (rd.getRequestName().equals("GetMap"))
                return rd.getFormats();
        }

        return null;
    }
}
