/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.*;

import java.util.List;

/**
 * @author tag
 * @version $Id: WCS100CoverageOfferingBrief.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100CoverageOfferingBrief extends AbstractXMLEventParser
{
    public WCS100CoverageOfferingBrief(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getDescription()
    {
        return (String) this.getField("description");
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    public String getLabel()
    {
        return (String) this.getField("label");
    }

    public AttributesOnlyXMLEventParser getMetadataLink()
    {
        return (AttributesOnlyXMLEventParser) this.getField("metadataLink");
    }

    public List<String> getKeywords()
    {
        return ((StringListXMLEventParser) this.getField("keywords")).getStrings();
    }

    public WCS100LonLatEnvelope getLonLatEnvelope()
    {
        return (WCS100LonLatEnvelope) this.getField("lonLatEnvelope");
    }
}
