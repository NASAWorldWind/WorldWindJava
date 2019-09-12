/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.ogc.ows.OWSWGS84BoundingBox;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCSCoverageSummary.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCSCoverageSummary extends AbstractXMLEventParser
{
    // TODO: metadata

    protected List<String> abstracts = new ArrayList<String>(1);
    protected List<OWSWGS84BoundingBox> boundingBoxes = new ArrayList<OWSWGS84BoundingBox>(1);
    protected List<WCSCoverageSummary> coverageSummaries = new ArrayList<WCSCoverageSummary>(1);
    protected List<String> supportedCRSs = new ArrayList<String>(1);
    protected List<String> supportedFormats = new ArrayList<String>(1);
    protected List<String> titles = new ArrayList<String>(1);

    public WCSCoverageSummary(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getAbstracts()
    {
        return this.abstracts;
    }

    public String getAbstract()
    {
        Iterator<String> iter = this.abstracts.iterator();

        return iter.hasNext() ? iter.next() : null;
    }

    public List<OWSWGS84BoundingBox> getBoundingBoxes()
    {
        return this.boundingBoxes;
    }

    public OWSWGS84BoundingBox getBoundingBox()
    {
        Iterator<OWSWGS84BoundingBox> iter = this.boundingBoxes.iterator();

        return iter.hasNext() ? iter.next() : null;
    }

    public List<WCSCoverageSummary> getCoverageSummaries()
    {
        return this.coverageSummaries;
    }

    public String getIdentifier()
    {
        return (String) this.getField("Identifier");
    }

    public List<String> getKeywords()
    {
        return ((StringListXMLEventParser) this.getField("Keywords")).getStrings();
    }

    public List<String> getSupportedCRSs()
    {
        return this.supportedCRSs;
    }

    public List<String> getSupportedFormats()
    {
        return this.supportedFormats;
    }

    public List<String> getTitles()
    {
        return this.titles;
    }

    public String getTitle()
    {
        Iterator<String> iter = this.titles.iterator();

        return iter.hasNext() ? iter.next() : null;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "Abstract"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.abstracts.add(s);
        }
        else if (ctx.isStartElement(event, "WGS84BoundingBox"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OWSWGS84BoundingBox)
                    this.boundingBoxes.add((OWSWGS84BoundingBox) o);
            }
        }
        else if (ctx.isStartElement(event, "CoverageSummary"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCSCoverageSummary)
                    this.coverageSummaries.add((WCSCoverageSummary) o);
            }
        }
        else if (ctx.isStartElement(event, "SupportedCRS"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.supportedCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "SupportedFormat"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.supportedFormats.add(s);
        }
        else if (ctx.isStartElement(event, "Title"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.titles.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
