/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.ogc.ows.OWSContactInfo;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: WCS100ResponsibleParty.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100ResponsibleParty extends AbstractXMLEventParser
{
    public WCS100ResponsibleParty(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getIndividualName()
    {
        return (String) this.getField("individualName");
    }

    public String getOrganisationName()
    {
        return (String) this.getField("organisationName");
    }

    public String getPositionName()
    {
        return (String) this.getField("positionName");
    }

    public OWSContactInfo getContactInfo()
    {
        return (OWSContactInfo) this.getField("contactInfo");
    }
}
