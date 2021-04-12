/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
