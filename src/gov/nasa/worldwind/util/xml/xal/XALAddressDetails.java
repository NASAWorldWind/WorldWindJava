/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml.xal;

import gov.nasa.worldwind.ogc.kml.KMLAbstractObject;

/**
 * @author tag
 * @version $Id: XALAddressDetails.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class XALAddressDetails extends KMLAbstractObject // TODO: Postal service parsers
{
    public XALAddressDetails(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getAddressType()
    {
        return (String) this.getField("AddressType");
    }

    public String getCurrentStatus()
    {
        return (String) this.getField("CurrentStatus");
    }

    public String getValidFromDate()
    {
        return (String) this.getField("ValidFromDate");
    }

    public String getValidToDate()
    {
        return (String) this.getField("ValidToDate");
    }

    public String getUsage()
    {
        return (String) this.getField("Usage");
    }

    public String getCode()
    {
        return (String) this.getField("Code");
    }

    public String getAddressDetailsKey()
    {
        return (String) this.getField("AddressDetailsKey");
    }

    public String getAddress()
    {
        return (String) this.getField("Address");
    }

    public XALAddressLines getAddressLines()
    {
        return (XALAddressLines) this.getField("AddressLines");
    }

    public XALCountry getCountry()
    {
        return (XALCountry) this.getField("Country");
    }

    public XALAdministrativeArea getAdministrativeArea()
    {
        return (XALAdministrativeArea) this.getField("AdministrativeArea");
    }

    public XALLocality getLocality()
    {
        return (XALLocality) this.getField("Locality");
    }

    public XALThoroughfare getThoroughfare()
    {
        return (XALThoroughfare) this.getField("Thoroughfare");
    }

    public XALPostalServiceElements getPostalServiceElements()
    {
        return (XALPostalServiceElements) this.getField("PostalServiceElements");
    }
}
