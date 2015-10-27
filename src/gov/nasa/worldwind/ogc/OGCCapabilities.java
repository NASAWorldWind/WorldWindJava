/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * Parses OGC Capabilities documents and holds the parsed information.
 *
 * @author tag
 * @version $Id: OGCCapabilities.java 1171 2013-02-11 21:45:02Z dcollins $
 */
abstract public class OGCCapabilities extends AbstractXMLEventParser
{
    /**
     * Returns the default namespace URI. Must be overridden by subclasses to provide a specific URI. The default
     * namespace is used to match XML elements found in the default namespace of the XML stream.
     *
     * @return the default namespace URI.
     */
    abstract public String getDefaultNamespaceURI();

    // Element names, constructed when the namespaceURI is known
    protected QName SERVICE;
    protected QName CAPABILITY;
    protected QName VERSION;
    protected QName UPDATE_SEQUENCE;

    protected String version;
    protected String updateSequence;

    protected OGCServiceInformation serviceInformation;
    protected OGCCapabilityInformation capabilityInformation;

    protected XMLEventReader eventReader;
    protected XMLEventParserContext parserContext;

    /**
     * Determines whether a specified element name is the root element name of the schema.
     *
     * @param name the name to test.
     *
     * @return true if the name is the schema's root element, otherwise false.
     */
    abstract public boolean isRootElementName(QName name); // implement to test name of root element

    /**
     * Create a new capabilities parser.
     *
     * @param namespaceURI the default namespace URI.
     * @param docSource    the XML source. May be a filename, file, stream or other type allowed by {@link
     *                     WWXML#openEventReader(Object)}.
     *
     * @throws IllegalArgumentException if the document source is null.
     */
    public OGCCapabilities(String namespaceURI, Object docSource)
    {
        super(namespaceURI);

        this.eventReader = this.createReader(docSource);
        this.initialize();
    }

    private void initialize()
    {
        this.parserContext = this.createParserContext(this.eventReader);

        SERVICE = new QName(this.getNamespaceURI(), "Service");
        CAPABILITY = new QName(this.getNamespaceURI(), "Capability");
        VERSION = new QName(this.getNamespaceURI(), "version");
        UPDATE_SEQUENCE = new QName(this.getNamespaceURI(), "updateSequence");

        this.getParserContext().registerParser(SERVICE, new OGCServiceInformation(this.getNamespaceURI()));
        // Capability parser is registered by subclass.
    }

    protected XMLEventReader createReader(Object docSource)
    {
        return WWXML.openEventReader(docSource);
    }

    protected XMLEventParserContext createParserContext(XMLEventReader reader)
    {
        this.parserContext = new BasicXMLEventParserContext(reader);
        this.parserContext.setDefaultNamespaceURI(this.getDefaultNamespaceURI());

        return this.parserContext;
    }

    /** {@inheritDoc} */
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        if (ctx == null)
        {
            String message = Logging.getMessage("nullValue.ParserContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XMLEventParser defaultParser = null;

        if (ctx.isStartElement(event, SERVICE))
            defaultParser = new OGCServiceInformation(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    /**
     * Starts document parsing. This method initiates parsing of the XML document and returns when the full capabilities
     * document has been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise  null.
     *
     * @throws XMLStreamException if an exception occurs while attempting to read the event stream.
     */
    public OGCCapabilities parse(Object... args) throws XMLStreamException
    {
        XMLEventParserContext ctx = this.parserContext;

        for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isStartElement() && this.isRootElementName(event.asStartElement().getName()))
            {
                super.parse(ctx, event, args);
                return this;
            }
        }

        return null;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, SERVICE))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCServiceInformation)
                    this.setServiceInformation((OGCServiceInformation) o);
            }
        }
        else if (ctx.isStartElement(event, CAPABILITY))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCCapabilityInformation)
                    this.setCapabilityInformation((OGCCapabilityInformation) o);
            }
        }
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args)
    {
        Iterator iter = event.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (ctx.isSameAttributeName(attr.getName(), VERSION))
                this.setVersion(attr.getValue());
            else if (ctx.isSameAttributeName(attr.getName(), UPDATE_SEQUENCE))
                this.setUpdateSequence(attr.getValue());
        }
    }

    protected XMLEventParserContext getParserContext()
    {
        return this.parserContext;
    }

    /**
     * Returns the document's service information.
     *
     * @return the document's service information.
     */
    public OGCServiceInformation getServiceInformation()
    {
        return serviceInformation;
    }

    protected void setServiceInformation(OGCServiceInformation serviceInformation)
    {
        this.serviceInformation = serviceInformation;
    }

    protected void setCapabilityInformation(OGCCapabilityInformation capabilityInformation)
    {
        this.capabilityInformation = capabilityInformation;
    }

    /**
     * Returns the document's capability information.
     *
     * @return the document's capability information.
     */
    public OGCCapabilityInformation getCapabilityInformation()
    {
        return capabilityInformation;
    }

    /**
     * Returns the document's version number.
     *
     * @return the document's version number.
     */
    public String getVersion()
    {
        return version;
    }

    protected void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * Returns the document's update sequence.
     *
     * @return the document's update sequence.
     */
    public String getUpdateSequence()
    {
        return updateSequence;
    }

    protected void setUpdateSequence(String updateSequence)
    {
        this.updateSequence = updateSequence;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Version: ").
            append(this.getVersion() != null ? this.getVersion() : "none").append("\n");
        sb.append("UpdateSequence: ").
            append(this.getUpdateSequence() != null ? this.getUpdateSequence() : "none");
        sb.append("\n");
        sb.append(this.getServiceInformation() != null ? this.getServiceInformation() : "Service Information: none");
        sb.append("\n");
        sb.append(this.getCapabilityInformation() != null
            ? this.getCapabilityInformation() : "Capability Information: none");
        sb.append("\n");

        return sb.toString();
    }
}
