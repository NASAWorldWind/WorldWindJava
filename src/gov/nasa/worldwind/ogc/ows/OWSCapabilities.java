/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;

/**
 * Parses OGC Capabilities documents and holds the parsed information.
 *
 * @author tag
 * @version $Id: OWSCapabilities.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public abstract class OWSCapabilities extends AbstractXMLEventParser
{
    abstract protected void determineNamespaces();

    protected String owsNamespaceURI;

    protected XMLEventReader eventReader;
    protected XMLEventParserContext parserContext;

    /**
     * Create a new capabilities parser.
     *
     * @param namespaceURI the default namespace URI.
     * @param docSource    the XML source. May be a filename, file, stream or other type allowed by {@link
     *                     WWXML#openEventReader(Object)}.
     *
     * @throws IllegalArgumentException if the document source is null.
     */
    public OWSCapabilities(String namespaceURI, Object docSource)
    {
        super(namespaceURI);

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

    public OWSServiceIdentification getServiceIdentification()
    {
        return (OWSServiceIdentification) this.getField("ServiceIdentification");
    }

    public OWSServiceProvider getServiceProvider()
    {
        return (OWSServiceProvider) this.getField("ServiceProvider");
    }

    public OWSOperationsMetadata getOperationsMetadata()
    {
        return (OWSOperationsMetadata) this.getField("OperationsMetadata");
    }

    /**
     * Starts document parsing. This method initiates parsing of the XML document and returns when the full capabilities
     * document has been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise  null.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if an exception occurs while attempting to read the event stream.
     */
    public OWSCapabilities parse(Object... args) throws XMLStreamException
    {
        XMLEventParserContext ctx = this.parserContext;
        QName capsName = new QName(this.getNamespaceURI(), "Capabilities");

        for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isStartElement() && event.asStartElement().getName().equals(capsName))
            {
                // Parse the attributes in order to get the version number in order to determine the namespaces.
                this.doParseEventAttributes(ctx, event);
                this.determineNamespaces(); // calls the subclass to do this
                ctx.setDefaultNamespaceURI(this.getNamespaceURI());

                // Now register the parsers.
                this.registerParsers(ctx);

                super.parse(ctx, event, args);
                return this;
            }
        }

        return null;
    }

    protected void setOWSNamespaceURI(String ns)
    {
        this.owsNamespaceURI = ns;
    }

    public String getOWSNamespaceURI()
    {
        return owsNamespaceURI;
    }

    protected void registerParsers(XMLEventParserContext ctx)
    {
        ctx.addStringParsers(this.getOWSNamespaceURI(), new String[]
            {
                "Abstract",
                "AccessConstraints",
                "AdministrativeArea",
                "City",
                "ContactInstructions",
                "Country",
                "DeliveryPoint",
                "ElectronicMailAddress",
                "Facsimile",
                "Fees",
                "HoursOfService",
                "IndividualName",
                "Keyword",
                "LowerCorner",
                "PositionName",
                "PostalCode",
                "Profile",
                "ProviderName",
                "Role",
                "ServiceType",
                "ServiceTypeVersion",
                "Title",
                "UpperCorner",
                "Value",
                "Voice"
            });

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Address"),
            new OWSAddress(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "AllowedValues"),
            new OWSAllowedValues(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "WGS84BoundingBox"),
            new OWSWGS84BoundingBox(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Constraint"),
            new OWSConstraint(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "ContactInfo"),
            new OWSContactInfo(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "DCP"),
            new OWSDCP(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Get"),
            new AttributesOnlyXMLEventParser(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "HTTP"),
            new OWSHTTP(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Keywords"),
            new StringListXMLEventParser(this.getOWSNamespaceURI(), new QName(this.getOWSNamespaceURI(), "Keyword")));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "OnlineResource"),
            new AttributesOnlyXMLEventParser(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Operation"),
            new OWSOperation(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "OperationsMetadata"),
            new OWSOperationsMetadata(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Parameter"),
            new OWSParameter(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Phone"),
            new OWSPhone(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "Post"),
            new AttributesOnlyXMLEventParser(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "ProviderSite"),
            new AttributesOnlyXMLEventParser(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "ServiceContact"),
            new OWSServiceContact(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "ServiceIdentification"),
            new OWSServiceIdentification(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "ServiceProvider"),
            new OWSServiceProvider(this.getOWSNamespaceURI()));

        ctx.registerParser(new QName(this.getOWSNamespaceURI(), "WGS84BoundingBox"),
            new OWSWGS84BoundingBox(this.getOWSNamespaceURI()));

        // Protocol specific parsers are registered by subclass.
    }
}
