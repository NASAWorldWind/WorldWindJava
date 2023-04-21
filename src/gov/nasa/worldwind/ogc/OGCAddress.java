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

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Parses an OGC Address element.
 *
 * @author tag
 * @version $Id: OGCAddress.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGCAddress extends AbstractXMLEventParser
{
    protected QName ADDRESS_TYPE;
    protected QName ADDRESS;
    protected QName CITY;
    protected QName STATE_OR_PROVINCE;
    protected QName POST_CODE;
    protected QName COUNTRY;

    protected String addressType;
    protected String address;
    protected String city;
    protected String stateOrProvince;
    protected String postCode;
    protected String country;

    public OGCAddress(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    protected void initialize()
    {
        ADDRESS_TYPE = new QName(this.getNamespaceURI(), "AddressType");
        ADDRESS = new QName(this.getNamespaceURI(), "Address");
        CITY = new QName(this.getNamespaceURI(), "City");
        STATE_OR_PROVINCE = new QName(this.getNamespaceURI(), "StateOrProvince");
        POST_CODE = new QName(this.getNamespaceURI(), "PostCode");
        COUNTRY = new QName(this.getNamespaceURI(), "Country");
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, ADDRESS_TYPE))
        {
            this.setAddressType(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, ADDRESS))
        {
            this.setAddress(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, CITY))
        {
            this.setCity(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, STATE_OR_PROVINCE))
        {
            this.setStateOrProvince(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, POST_CODE))
        {
            this.setPostCode(ctx.getStringParser().parseString(ctx, event));
        }
        else if (ctx.isStartElement(event, COUNTRY))
        {
            this.setCountry(ctx.getStringParser().parseString(ctx, event));
        }
    }

    public String getAddressType()
    {
        return addressType;
    }

    protected void setAddressType(String addressType)
    {
        this.addressType = addressType;
    }

    public String getAddress()
    {
        return address;
    }

    protected void setAddress(String address)
    {
        this.address = address;
    }

    public String getCity()
    {
        return city;
    }

    protected void setCity(String city)
    {
        this.city = city;
    }

    public String getStateOrProvince()
    {
        return stateOrProvince;
    }

    protected void setStateOrProvince(String stateOrProvince)
    {
        this.stateOrProvince = stateOrProvince;
    }

    public String getPostCode()
    {
        return postCode;
    }

    protected void setPostCode(String postCode)
    {
        this.postCode = postCode;
    }

    public String getCountry()
    {
        return country;
    }

    protected void setCountry(String country)
    {
        this.country = country;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("AddressType: ").append(this.addressType != null ? this.addressType : "none").append(" ");
        sb.append("Address: ").append(this.address != null ? this.address : "none").append(" ");
        sb.append("City: ").append(this.city != null ? this.city : "none").append(" ");
        sb.append("StateOrProvince: ").append(this.stateOrProvince != null ? this.stateOrProvince : "none").append(" ");
        sb.append("PostCode: ").append(this.postCode != null ? this.postCode : "none").append(" ");
        sb.append("Country: ").append(this.country != null ? this.country : "none");

        return sb.toString();
    }
}
