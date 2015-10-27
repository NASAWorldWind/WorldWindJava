/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

/**
 * @author tag
 * @version $Id: WCS100CoverageOffering.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100CoverageOffering extends WCS100CoverageOfferingBrief
{
    public WCS100CoverageOffering(String namespaceURI)
    {
        super(namespaceURI);
    }

    public WCS100LonLatEnvelope getLonLatEnvelope()
    {
        return (WCS100LonLatEnvelope) this.getField("lonLatEnvelope");
    }

    public WCS100DomainSet getDomainSet()
    {
        return (WCS100DomainSet) this.getField("domainSet");
    }

    public WCS100RangeSetHolder getRangeSet()
    {
        return (WCS100RangeSetHolder) this.getField("rangeSet");
    }

    public WCS100SupportedFormats getSupportedFormats()
    {
        return (WCS100SupportedFormats) this.getField("supportedFormats");
    }

    public WCS100SupportedCRSs getSupportedCRSs()
    {
        return (WCS100SupportedCRSs) this.getField("supportedCRSs");
    }

    public WCS100SupportedInterpolations getSupportedInterpolations()
    {
        return (WCS100SupportedInterpolations) this.getField("supportedInterpolations");
    }
}
