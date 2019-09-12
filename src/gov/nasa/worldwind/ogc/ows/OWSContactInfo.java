/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.*;

/**
 * @author tag
 * @version $Id: OWSContactInfo.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSContactInfo extends AbstractXMLEventParser
{
    public OWSContactInfo(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getHoursOfService()
    {
        return (String) this.getField("HoursOfService");
    }

    public String getContactInstructions()
    {
        return (String) this.getField("ContactInstructions");
    }

    public OWSAddress getAddress()
    {
        return (OWSAddress) (this.getField("Address") != null ? this.getField("Address") : this.getField("address"));
    }

    public OWSPhone getPhone()
    {
        return (OWSPhone) (this.getField("Phone") != null ? this.getField("Phone") : this.getField("phone"));
    }

    public String getOnlineResource()
    {
        AttributesOnlyXMLEventParser parser = (AttributesOnlyXMLEventParser)
            (this.getField("OnlineResource") != null ? this.getField("OnlineResource")
            : this.getField("onlineResource"));

        return parser != null ? (String) parser.getField("href") : null;
    }
}
