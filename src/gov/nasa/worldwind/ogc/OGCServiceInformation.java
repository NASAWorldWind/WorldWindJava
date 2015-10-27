/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Parses an OGC Service element.
 *
 * @author tag
 * @version $Id: OGCServiceInformation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGCServiceInformation extends AbstractXMLEventParser
{
    protected QName NAME;
    protected QName TITLE;
    protected QName ABSTRACT;
    protected QName FEES;
    protected QName ACCESS_CONSTRAINTS;
    protected QName KEYWORD_LIST;
    protected QName KEYWORD;
    protected QName ONLINE_RESOURCE;
    protected QName CONTACT_INFORMATION;

    protected String serviceName;
    protected String serviceTitle;
    protected String serviceAbstract;
    protected String fees;
    protected String accessConstraints;
    protected Set<String> keywords;
    protected OGCOnlineResource onlineResource;
    protected OGCContactInformation contactInformation;

    public OGCServiceInformation(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        NAME = new QName(this.getNamespaceURI(), "Name");
        TITLE = new QName(this.getNamespaceURI(), "Title");
        ABSTRACT = new QName(this.getNamespaceURI(), "Abstract");
        FEES = new QName(this.getNamespaceURI(), "Fees");
        ACCESS_CONSTRAINTS = new QName(this.getNamespaceURI(), "AccessConstraints");
        KEYWORD_LIST = new QName(this.getNamespaceURI(), "KeywordList");
        KEYWORD = new QName(this.getNamespaceURI(), "Keyword");
        ONLINE_RESOURCE = new QName(this.getNamespaceURI(), "OnlineResource");
        CONTACT_INFORMATION = new QName(this.getNamespaceURI(), "ContactInformation");
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        if (ctx.isStartElement(event, ONLINE_RESOURCE))
            defaultParser = new OGCOnlineResource(this.getNamespaceURI());
        else if (ctx.isStartElement(event, CONTACT_INFORMATION))
            defaultParser = new OGCContactInformation(this.getNamespaceURI());
        else if (ctx.isStartElement(event, KEYWORD_LIST))
            defaultParser = new StringSetXMLEventParser(this.getNamespaceURI(), KEYWORD);

        return ctx.allocate(event, defaultParser);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, NAME))
        {
            this.setServiceName(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, TITLE))
        {
            this.setServiceTitle(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, ABSTRACT))
        {
            this.setServiceAbstract(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, FEES))
        {
            this.setFees(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, ACCESS_CONSTRAINTS))
        {
            this.setAccessConstraints(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, KEYWORD_LIST))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof StringSetXMLEventParser)
                    this.setKeywords(((StringSetXMLEventParser) o).getStrings());
            }
        }
        else if (ctx.isStartElement(event, ONLINE_RESOURCE))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCOnlineResource)
                    this.setOnlineResource((OGCOnlineResource) o);
            }
        }
        else if (ctx.isStartElement(event, CONTACT_INFORMATION))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCContactInformation)
                    this.setContactInformation((OGCContactInformation) o);
            }
        }
    }

    public OGCContactInformation getContactInformation()
    {
        return contactInformation;
    }

    protected void setContactInformation(OGCContactInformation contactInformation)
    {
        this.contactInformation = contactInformation;
    }

    public OGCOnlineResource getOnlineResource()
    {
        return onlineResource;
    }

    protected void setOnlineResource(OGCOnlineResource onlineResource)
    {
        this.onlineResource = onlineResource;
    }

    public Set<String> getKeywords()
    {
        if (keywords != null)
            return keywords;
        else
            return Collections.emptySet();
    }

    protected void setKeywords(Set<String> keywords)
    {
        this.keywords = keywords;
    }

    public String getAccessConstraints()
    {
        return accessConstraints;
    }

    protected void setAccessConstraints(String accessConstraints)
    {
        this.accessConstraints = accessConstraints;
    }

    public String getFees()
    {
        return fees;
    }

    protected void setFees(String fees)
    {
        this.fees = fees;
    }

    public String getServiceAbstract()
    {
        return serviceAbstract;
    }

    protected void setServiceAbstract(String serviceAbstract)
    {
        this.serviceAbstract = serviceAbstract;
    }

    public String getServiceTitle()
    {
        return serviceTitle;
    }

    protected void setServiceTitle(String serviceTitle)
    {
        this.serviceTitle = serviceTitle;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    protected void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ServiceName: ").append(this.serviceName != null ? this.serviceName : "none").append("\n");
        sb.append("ServiceTitle: ").append(this.serviceTitle != null ? this.serviceTitle : "none").append("\n");
        sb.append("ServiceAbstract: ").append(this.serviceAbstract != null ? this.serviceAbstract : "none").append(
            "\n");
        sb.append("Fees: ").append(this.fees != null ? this.fees : "none").append("\n");
        sb.append("AccessConstraints: ").append(
            this.accessConstraints != null ? this.accessConstraints : "none").append("\n");
        this.keywordsToString(sb);
        sb.append("OnlineResource: ").append(this.onlineResource != null ? this.onlineResource : "none").append("\n");
        sb.append(this.contactInformation != null ? this.contactInformation : "none").append("\n");

        return sb.toString();
    }

    protected void keywordsToString(StringBuilder sb)
    {
        sb.append("Keywords: ");
        if (this.getKeywords().size() == 0)
            sb.append(" none");
        else
        {
            for (String keyword : this.getKeywords())
            {
                sb.append(keyword != null ? keyword : "null").append(", ");
            }
        }
        sb.append("\n");
    }
}
