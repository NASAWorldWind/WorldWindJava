/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Parses an OGC Request element.
 *
 * @author tag
 * @version $Id: OGCRequestDescription.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGCRequestDescription extends AbstractXMLEventParser
{
    protected QName FORMAT;
    protected QName DCPTYPE;

    protected String requestName;
    protected Set<String> formats;
    protected Set<OGCDCType> dcpTypes;

    public OGCRequestDescription(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        FORMAT = new QName(this.getNamespaceURI(), "Format");
        DCPTYPE = new QName(this.getNamespaceURI(), "DCPType");
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        if (ctx.isStartElement(event, DCPTYPE))
            defaultParser = new OGCDCType(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    public Object parse(XMLEventParserContext ctx, XMLEvent rqstEvent, Object... args) throws XMLStreamException
    {
        if (this.formats != null)
            this.formats.clear();
        if (this.dcpTypes != null)
            this.dcpTypes.clear();

        if (rqstEvent.isStartElement())
            this.setRequestName(rqstEvent.asStartElement().getName().getLocalPart());

        return super.parse(ctx, rqstEvent, args);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, FORMAT))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.addFormat(s);
        }
        else if (ctx.isStartElement(event, DCPTYPE))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCDCType)
                    this.addDCPType((OGCDCType) o);
            }
        }
    }

    public OGCOnlineResource getOnlineResouce(String protocol, String requestMethod)
    {
        for (OGCDCType dct : this.getDCPTypes())
        {
            OGCOnlineResource olr = dct.getOnlineResouce(protocol, requestMethod);
            if (olr != null)
                return olr;
        }

        return null;
    }

    public Set<String> getFormats()
    {
        if (this.formats != null)
            return formats;
        else
            return Collections.emptySet();
    }

    protected void setFormats(Set<String> formats)
    {
        this.formats = formats;
    }

    protected void addFormat(String format)
    {
        if (this.formats == null)
            this.formats = new HashSet<String>();

        this.formats.add(format);
    }

    protected void setDCPTypes(Set<OGCDCType> dcTypes)
    {
        this.dcpTypes = dcTypes;
    }

    public Set<OGCDCType> getDCPTypes()
    {
        if (this.dcpTypes != null)
            return dcpTypes;
        else
            return Collections.emptySet();
    }

    public void addDCPType(OGCDCType dct)
    {
        if (this.dcpTypes == null)
            this.dcpTypes = new HashSet<OGCDCType>();

        this.dcpTypes.add(dct);
    }

    public String getRequestName()
    {
        return requestName;
    }

    protected void setRequestName(String requestName)
    {
        this.requestName = requestName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if (this.getRequestName() != null)
            sb.append(this.getRequestName()).append("\n");

        sb.append("\tFormats: ");
        for (String format : this.getFormats())
        {
            sb.append("\t").append(format).append(", ");
        }

        sb.append("\n\tDCPTypes:\n");
        for (OGCDCType dcpt : this.getDCPTypes())
        {
            sb.append("\t\t").append(dcpt.toString()).append("\n");
        }

        return sb.toString();
    }
}
