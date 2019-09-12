/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCSContents.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCSContents extends AbstractXMLEventParser
{
    protected List<WCSCoverageSummary> coverageSummaries = new ArrayList<WCSCoverageSummary>(1);
    protected List<AttributesOnlyXMLEventParser> otherSources = new ArrayList<AttributesOnlyXMLEventParser>(1);
    protected List<String> supportedCRSs = new ArrayList<String>(1);
    protected List<String> supportedFormats = new ArrayList<String>(1);

    public WCSContents(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<WCSCoverageSummary> getCoverageSummaries()
    {
        return this.coverageSummaries;
    }

    public List<String> getSupportedCRSs()
    {
        return this.supportedCRSs;
    }

    public List<String> getSupportedFormats()
    {
        return this.supportedFormats;
    }

    public List<String> getOtherSources()
    {
        List<String> strings = new ArrayList<String>(1);

        for (AttributesOnlyXMLEventParser parser : this.otherSources)
        {
            String url = (String) parser.getField("href");
            if (url != null)
                strings.add(url);
        }

        return strings.size() > 0 ? strings : null;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "CoverageSummary"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCSCoverageSummary)
                    this.coverageSummaries.add((WCSCoverageSummary) o);
            }
        }
        else if (ctx.isStartElement(event, "OtherSource"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof AttributesOnlyXMLEventParser)
                    this.otherSources.add((AttributesOnlyXMLEventParser) o);
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
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
