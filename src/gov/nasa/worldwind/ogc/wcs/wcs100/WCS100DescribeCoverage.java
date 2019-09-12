/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.ogc.gml.*;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.wms.Request;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.net.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100DescribeCoverage.java 2072 2014-06-21 21:20:25Z tgaskins $
 */
public class WCS100DescribeCoverage extends AbstractXMLEventParser
{
    protected XMLEventReader eventReader;
    protected XMLEventParserContext parserContext;
    protected List<WCS100CoverageOffering> coverageOfferings = new ArrayList<WCS100CoverageOffering>(1);

    public static WCS100DescribeCoverage retrieve(URI uri, final String coverageName) throws URISyntaxException
    {
        Request request = new Request(uri, "WCS")
        {
            @Override
            protected void initialize(String service)
            {
                super.initialize(service);
                this.setParam("REQUEST", "DescribeCoverage");
                this.setParam("VERSION", "1.0.0");
                this.setParam("coverage", coverageName);
            }
        };

        return new WCS100DescribeCoverage(request.toString());
    }

    public WCS100DescribeCoverage(Object docSource)
    {
        super(OGCConstants.WCS_1_0_0_NAMESPACE_URI);

        this.eventReader = this.createReader(docSource);

        this.initialize();
    }

    protected void initialize()
    {
        this.parserContext = this.createParserContext(this.eventReader);
    }

    protected XMLEventReader createReader(Object docSource)
    {
        return WWXML.openEventReader(docSource);
    }

    protected XMLEventParserContext createParserContext(XMLEventReader reader)
    {
        this.parserContext = new BasicXMLEventParserContext(reader);
        this.parserContext.setDefaultNamespaceURI(this.getNamespaceURI());

        return this.parserContext;
    }

    public XMLEventParserContext getParserContext()
    {
        return this.parserContext;
    }

    /**
     * Returns the document's version number.
     *
     * @return the document's version number.
     */
    public String getVersion()
    {
        return (String) this.getField("version");
    }

    /**
     * Returns the document's update sequence.
     *
     * @return the document's update sequence.
     */
    public String getUpdateSequence()
    {
        return (String) this.getField("updateSequence");
    }

    public List<WCS100CoverageOffering> getCoverageOfferings()
    {
        return this.coverageOfferings;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "CoverageOffering"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCS100CoverageOffering)
                    this.coverageOfferings.add((WCS100CoverageOffering) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }

    /**
     * Starts document parsing. This method initiates parsing of the XML document and returns when the full
     * DescribeCoverage document has been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise  null.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if an exception occurs while attempting to read the event stream.
     */
    public WCS100DescribeCoverage parse(Object... args) throws XMLStreamException
    {
        XMLEventParserContext ctx = this.parserContext;
        QName docName = new QName(this.getNamespaceURI(), "CoverageDescription");

        for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isStartElement() && event.asStartElement().getName().equals(docName))
            {
                // Parse the attributes in order to get the version number.
                this.doParseEventAttributes(ctx, event);
                ctx.setDefaultNamespaceURI(this.getNamespaceURI());

                // Now register the parsers.
                this.registerParsers(ctx);

                super.parse(ctx, event, args);

                return this;
            }
        }

        return null;
    }

    protected void registerParsers(XMLEventParserContext ctx)
    {
        ctx.addStringParsers(this.getNamespaceURI(), new String[]
            {
                "description",
                "label",
                "keyword",
                "name",
                "res",
                "singleValue",
            });

        ctx.addStringParsers(OGCConstants.GML_NAMESPACE_URI, new String[]
            {
                "axisName",
                "high",
                "low",
                "offsetVector",
                "timePosition",
            });

        ctx.registerParser(new QName(this.getNamespaceURI(), "axisDescription"),
            new WCS100AxisDescriptionHolder(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "AxisDescription"),
            new WCS100AxisDescription(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "CoverageOffering"),
            new WCS100CoverageOffering(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "domainSet"),
            new WCS100DomainSet(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.GML_NAMESPACE_URI, "Envelope"),
            new GMLEnvelope(OGCConstants.GML_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.GML_NAMESPACE_URI, "Grid"),
            new GMLGrid(OGCConstants.GML_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.GML_NAMESPACE_URI, "GridEnvelope"),
            new GMLGridEnvelope(OGCConstants.GML_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "interval"),
            new WCS100Interval(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "keywords"),
            new StringListXMLEventParser(this.getNamespaceURI(), new QName(this.getNamespaceURI(), "keyword")));

        ctx.registerParser(new QName(OGCConstants.GML_NAMESPACE_URI, "limits"),
            new GMLLimits(OGCConstants.GML_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "lonLatEnvelope"),
            new WCS100LonLatEnvelope(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "max"),
            new WCS100Max(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "metadataLink"),
            new WCS100MetadataLink(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "min"),
            new WCS100Min(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "nullValues"),
            new WCS100Values(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.GML_NAMESPACE_URI, "origin"),
            new GMLOrigin(OGCConstants.GML_NAMESPACE_URI));

        ctx.registerParser(new QName(OGCConstants.GML_NAMESPACE_URI, "pos"),
            new GMLPos(OGCConstants.GML_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "rangeSet"),
            new WCS100RangeSetHolder(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "rangeSet"),
            new WCS100RangeSetHolder(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "RangeSet"),
            new WCS100RangeSet(this.getNamespaceURI()));

        ctx.registerParser(new QName(OGCConstants.GML_NAMESPACE_URI, "RectifiedGrid"),
            new GMLRectifiedGrid(OGCConstants.GML_NAMESPACE_URI));

        ctx.registerParser(new QName(this.getNamespaceURI(), "supportedFormats"),
            new WCS100SupportedFormats(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "supportedInterpolations"),
            new WCS100SupportedInterpolations(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "singleValue"),
            new WCS100SingleValue(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "spatialDomain"),
            new WCS100SpatialDomain(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "supportedCRSs"),
            new WCS100SupportedCRSs(this.getNamespaceURI()));

        ctx.registerParser(new QName(this.getNamespaceURI(), "values"),
            new WCS100Values(this.getNamespaceURI()));
    }
}
