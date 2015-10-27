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
 * @version $Id: OWSAddress.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSAddress extends AbstractXMLEventParser
{
    protected List<String> deliveryPoints = new ArrayList<String>(1);
    protected List<String> postalCodes = new ArrayList<String>(1);
    protected List<String> countries = new ArrayList<String>(1);
    protected List<String> emails = new ArrayList<String>(1);

    public OWSAddress(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getCity()
    {
        return (String) (this.getField("City") != null ? this.getField("City") : this.getField("city"));
    }

    public String getAdministrativeArea()
    {
        return (String) (this.getField("AdministrativeArea") != null
            ? this.getField("AdministrativeArea") : this.getField("administrativeArea"));
    }

    public List<String> getDeliveryPoints()
    {
        return this.deliveryPoints;
    }

    public List<String> getPostalCodes()
    {
        return this.postalCodes;
    }

    public List<String> getCountries()
    {
        return this.countries;
    }

    public List<String> getElectronicMailAddresses()
    {
        return this.emails;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "DeliveryPoint") || ctx.isStartElement(event, "deliveryPoint"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.deliveryPoints.add(s);
        }
        else if (ctx.isStartElement(event, "PostalCode") || ctx.isStartElement(event, "postalCode"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.postalCodes.add(s);
        }
        else if (ctx.isStartElement(event, "Country") || ctx.isStartElement(event, "country"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.countries.add(s);
        }
        else if (ctx.isStartElement(event, "ElectronicMailAddress")
            || ctx.isStartElement(event, "electronicMailAddress"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.emails.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
