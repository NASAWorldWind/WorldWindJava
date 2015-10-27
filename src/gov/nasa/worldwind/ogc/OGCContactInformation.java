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

/**
 * Parses an OGC ContactInformation element.
 *
 * @author tag
 * @version $Id: OGCContactInformation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGCContactInformation extends AbstractXMLEventParser
{
    protected QName CONTACT_POSITION;
    protected QName CONTACT_VOICE_TELEPHONE;
    protected QName CONTACT_FACSIMILE_TELEPHONE;
    protected QName CONTACT_ELECTRONIC_MAIL_ADDRESS;
    protected QName CONTACT_PERSON_PRIMARY;
    protected QName CONTACT_ADDRESS;
    protected QName CONTACT_PERSON;
    protected QName CONTACT_ORGANIZATION;

    protected String personPrimary;
    protected String organization;
    protected String position;
    protected String voiceTelephone;
    protected String facsimileTelephone;
    protected String electronicMailAddress;
    protected OGCAddress contactAddress;

    public OGCContactInformation(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        CONTACT_POSITION = new QName(this.getNamespaceURI(), "ContactPosition");
        CONTACT_VOICE_TELEPHONE = new QName(this.getNamespaceURI(), "ContactVoiceTelephone");
        CONTACT_FACSIMILE_TELEPHONE = new QName(this.getNamespaceURI(), "ContactFacsimileTelephone");
        CONTACT_ELECTRONIC_MAIL_ADDRESS = new QName(this.getNamespaceURI(), "ContactElectronicMailAddress");
        CONTACT_PERSON_PRIMARY = new QName(this.getNamespaceURI(), "ContactPersonPrimary");
        CONTACT_ADDRESS = new QName(this.getNamespaceURI(), "ContactAddress");
        CONTACT_PERSON = new QName(this.getNamespaceURI(), "ContactPerson");
        CONTACT_ORGANIZATION = new QName(this.getNamespaceURI(), "ContactOrganization");
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        if (ctx.isStartElement(event, CONTACT_ADDRESS))
            defaultParser = new OGCAddress(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, CONTACT_POSITION))
        {
            this.setPosition(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, CONTACT_VOICE_TELEPHONE))
        {
            this.setVoiceTelephone(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, CONTACT_FACSIMILE_TELEPHONE))
        {
            this.setFacsimileTelephone(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, CONTACT_ELECTRONIC_MAIL_ADDRESS))
        {
            this.setElectronicMailAddress(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, CONTACT_PERSON_PRIMARY))
        {
            String[] sa = this.parseContactPersonPrimary(ctx, event);
            this.setPersonPrimary(sa[0]);
            this.setOrganization(sa[1]);
        }
        else if (ctx.isStartElement(event, CONTACT_ADDRESS))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCAddress)
                    this.setContactAddress((OGCAddress) o);
            }
        }
    }

    protected String[] parseContactPersonPrimary(XMLEventParserContext ctx, XMLEvent cppEvent) throws XMLStreamException
    {
        String[] items = new String[2];

        for (XMLEvent event = ctx.nextEvent(); event != null; event = ctx.nextEvent())
        {
            if (ctx.isEndElement(event, cppEvent))
                return items;

            if (ctx.isStartElement(event, CONTACT_PERSON))
            {
                items[0] = ctx.getStringParser().parseString(ctx, event);
            }
            else if (ctx.isStartElement(event, CONTACT_ORGANIZATION))
            {
                items[1] = ctx.getStringParser().parseString(ctx, event);
            }
        }

        return null;
    }

    public String getPersonPrimary()
    {
        return personPrimary;
    }

    protected void setPersonPrimary(String personPrimary)
    {
        this.personPrimary = personPrimary;
    }

    public String getOrganization()
    {
        return organization;
    }

    protected void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public String getPosition()
    {
        return position;
    }

    protected void setPosition(String position)
    {
        this.position = position;
    }

    public String getVoiceTelephone()
    {
        return voiceTelephone;
    }

    protected void setVoiceTelephone(String voiceTelephone)
    {
        this.voiceTelephone = voiceTelephone;
    }

    public String getFacsimileTelephone()
    {
        return facsimileTelephone;
    }

    protected void setFacsimileTelephone(String facsimileTelephone)
    {
        this.facsimileTelephone = facsimileTelephone;
    }

    public String getElectronicMailAddress()
    {
        return electronicMailAddress;
    }

    protected void setElectronicMailAddress(String electronicMailAddress)
    {
        this.electronicMailAddress = electronicMailAddress;
    }

    public OGCAddress getContactAddress()
    {
        return contactAddress;
    }

    protected void setContactAddress(OGCAddress contactAddress)
    {
        this.contactAddress = contactAddress;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("PersonPrimary: ").append(this.personPrimary != null ? this.personPrimary : "none").append("\n");
        sb.append("Organization: ").append(this.organization != null ? this.organization : "none").append("\n");
        sb.append("Position: ").append(this.position != null ? this.position : "none").append("\n");
        sb.append("VoiceTelephone: ").append(this.voiceTelephone != null ? this.voiceTelephone : "none").append("\n");
        sb.append("FacsimileTelephone: ").append(
            this.facsimileTelephone != null ? this.facsimileTelephone : "none").append("\n");
        sb.append("ElectronicMailAddress: ").append(
            this.electronicMailAddress != null ? this.electronicMailAddress : "none").append("\n");
        sb.append(this.contactAddress != null ? this.contactAddress : "none");

        return sb.toString();
    }
}
