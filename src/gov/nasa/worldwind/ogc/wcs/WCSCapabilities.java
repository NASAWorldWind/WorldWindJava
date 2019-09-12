/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.ogc.ows.OWSCapabilities;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;

/**
 * @author tag
 * @version $Id: WCSCapabilities.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class WCSCapabilities extends OWSCapabilities
{
    public WCSCapabilities(Object docSource)
    {
        super(OGCConstants.WCS_1_1_1_NAMESPACE_URI, docSource);
    }

    public WCSContents getContents()
    {
        return (WCSContents) this.getField("Contents");
    }

    public String getOtherSource()
    {
        AttributesOnlyXMLEventParser parser = (AttributesOnlyXMLEventParser) this.getField("OtherSource");

        return parser != null ? (String) parser.getField("href") : null;
    }

    public String getDefaultNamespaceURI()
    {
        return this.namespaceURI;
    }

    public String getWCSNamespace()
    {
        return this.namespaceURI;
    }

    protected void determineNamespaces()
    {
        String version = this.getVersion();
        if (version == null)
        {
            this.setOWSNamespaceURI(OGCConstants.OWS_1_1_0_NAMESPACE_URI);
            this.setNamespaceURI(OGCConstants.WCS_1_1_1_NAMESPACE_URI);
        }
        else if (WWUtil.compareVersion(version, "1.1.1") == 0)
        {
            this.setOWSNamespaceURI(OGCConstants.OWS_1_1_0_NAMESPACE_URI);
            this.setNamespaceURI(OGCConstants.WCS_1_1_1_NAMESPACE_URI);
        }
        else
        {
            this.setOWSNamespaceURI(OGCConstants.OWS_1_1_0_NAMESPACE_URI);
            this.setNamespaceURI(OGCConstants.WCS_1_1_1_NAMESPACE_URI);
        }
    }

    @Override
    protected void registerParsers(XMLEventParserContext ctx)
    {
        super.registerParsers(ctx);

        ctx.addStringParsers(this.namespaceURI, new String[] {
            "Identifier",
            "SupportedCRS",
            "SupportedFormat"
        });

        ctx.registerParser(new QName(this.getWCSNamespace(), "Contents"),
            new WCSContents(this.namespaceURI));

        ctx.registerParser(new QName(this.getWCSNamespace(), "CoverageSummary"),
            new WCSCoverageSummary(this.namespaceURI));

        ctx.registerParser(new QName(this.getWCSNamespace(), "OtherSource"),
            new AttributesOnlyXMLEventParser(this.namespaceURI));
    }
}
