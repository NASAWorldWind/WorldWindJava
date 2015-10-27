/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.ogc.gml.*;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100SpatialDomain.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100SpatialDomain extends AbstractXMLEventParser
{
    protected List<GMLEnvelope> envelopes = new ArrayList<GMLEnvelope>(1);
    protected List<GMLRectifiedGrid> rectifiedGrids = new ArrayList<GMLRectifiedGrid>(1);
    protected List<GMLGrid> grids = new ArrayList<GMLGrid>(1);

    public WCS100SpatialDomain(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<GMLEnvelope> getEnvelopes()
    {
        return this.envelopes;
    }

    public List<GMLRectifiedGrid> getRectifiedGrids()
    {
        return this.rectifiedGrids;
    }

    public List<GMLGrid> getGrids()
    {
        return this.grids;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "Envelope") || ctx.isStartElement(event, "EnvelopeWithTimePeriod"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof GMLEnvelope)
                    this.envelopes.add((GMLEnvelope) o);
            }
        }
        else if (ctx.isStartElement(event, "RectifiedGrid"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof GMLRectifiedGrid)
                    this.rectifiedGrids.add((GMLRectifiedGrid) o);
            }
        }
        else if (ctx.isStartElement(event, "Grid"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof GMLGrid)
                    this.grids.add((GMLGrid) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
