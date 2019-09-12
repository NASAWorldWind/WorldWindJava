/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: OWSServiceIdentification.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSServiceIdentification extends AbstractXMLEventParser
{
    protected List<String> abstracts = new ArrayList<String>(1);
    protected List<String> accessConstraints = new ArrayList<String>(1);
    protected List<String> profiles = new ArrayList<String>(1);
    protected List<String> titles = new ArrayList<String>(1);
    protected List<String> serviceTypeVersions = new ArrayList<String>(1);

    public OWSServiceIdentification(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getTitles()
    {
        return this.titles;
    }

    public List<String> getAbstracts()
    {
        return this.abstracts;
    }

    public List<String> getKeywords()
    {
        return ((StringListXMLEventParser) this.getField("Keywords")).getStrings();
    }

    public String getServiceType()
    {
        return (String) this.getField("ServiceType");
    }

    public List<String> getServiceTypeVersions()
    {
        return this.serviceTypeVersions;
    }

    public String getFees()
    {
        return (String) this.getField("Fees");
    }

    public List<String> getAccessConstraints()
    {
        return this.accessConstraints;
    }

    public List<String> getProfiles()
    {
        return this.profiles;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "ServiceTypeVersion"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.serviceTypeVersions.add(s);
        }
        else if (ctx.isStartElement(event, "Abstract"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.abstracts.add(s);
        }
        else if (ctx.isStartElement(event, "AccessConstraints"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.accessConstraints.add(s);
        }
        else if (ctx.isStartElement(event, "Title"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.titles.add(s);
        }
        else if (ctx.isStartElement(event, "Profile"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.profiles.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
