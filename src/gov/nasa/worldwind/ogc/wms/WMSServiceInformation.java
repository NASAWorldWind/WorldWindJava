/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.ogc.OGCServiceInformation;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Parses a WMS Service element.
 *
 * @author tag
 * @version $Id: WMSServiceInformation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSServiceInformation extends OGCServiceInformation
{
    protected QName MAX_WIDTH;
    protected QName MAX_HEIGHT;
    protected QName LAYER_LIMIT;

    protected int maxWidth;
    protected int maxHeight;
    protected int layerLimit;

    public WMSServiceInformation(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        MAX_WIDTH = new QName(this.getNamespaceURI(), "MaxWidth");
        MAX_HEIGHT = new QName(this.getNamespaceURI(), "MaxHeight");
        LAYER_LIMIT = new QName(this.getNamespaceURI(), "LayerLimit");
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, MAX_WIDTH))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.maxWidth = d.intValue();
        }
        else if (ctx.isStartElement(event, MAX_HEIGHT))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.maxHeight = d.intValue();
        }
        else if (ctx.isStartElement(event, LAYER_LIMIT))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.layerLimit = d.intValue();
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }

    public int getMaxWidth()
    {
        return maxWidth;
    }

    protected void setMaxWidth(int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    protected void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString());

        sb.append("Max width = ").append(this.getMaxWidth());
        sb.append(" Max height = ").append(this.getMaxHeight()).append("\n");

        return sb.toString();
    }
}
