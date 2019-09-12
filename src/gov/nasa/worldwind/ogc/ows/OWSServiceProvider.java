/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.*;

/**
 * @author tag
 * @version $Id: OWSServiceProvider.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class OWSServiceProvider extends AbstractXMLEventParser
{
    public OWSServiceProvider(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getProviderName()
    {
        return (String) this.getField("ProviderName");
    }

    public String getProviderSite()
    {
        AttributesOnlyXMLEventParser parser = (AttributesOnlyXMLEventParser) this.getField("ProviderSite");

        return parser != null ? (String) parser.getField("href") : null;
    }

    public OWSServiceContact getServiceContact()
    {
        return (OWSServiceContact) this.getField("ServiceContact");
    }
}
