/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.*;

/**
 * @author tag
 * @version $Id: OWSServiceContact.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class OWSServiceContact extends AbstractXMLEventParser
{
    public OWSServiceContact(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getIndividualName()
    {
        return (String) this.getField("IndividualName");
    }

    public String getPositionName()
    {
        return (String) this.getField("PositionName");
    }

    public String getRole()
    {
        return (String) this.getField("Role");
    }

    public OWSContactInfo getContactInfo()
    {
        return (OWSContactInfo) this.getField("ContactInfo");
    }

    public OWSPhone getPhone()
    {
        return (OWSPhone) this.getField("Phone");
    }

    public OWSAddress getAddress()
    {
        return (OWSAddress) getField("Address");
    }
}
