/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
