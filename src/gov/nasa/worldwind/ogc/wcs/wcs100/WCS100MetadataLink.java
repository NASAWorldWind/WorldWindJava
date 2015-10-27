/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id$
 */
public class WCS100MetadataLink extends AbstractXMLEventParser
{
    public WCS100MetadataLink(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getAbout()
    {
        return (String) this.getField("about");
    }

    public String getMetadataType()
    {
        return (String) this.getField("metadataType");
    }

    public String getType()
    {
        return (String) this.getField("type");
    }

    public String getHref()
    {
        return (String) this.getField("href");
    }
}
