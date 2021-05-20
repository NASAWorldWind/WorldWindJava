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

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: OWSAddress.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSAddress extends AbstractXMLEventParser
{
    protected List<String> deliveryPoints = new ArrayList<String>(1);
    protected List<String> postalCodes = new ArrayList<String>(1);
    protected List<String> countries = new ArrayList<String>(1);
    protected List<String> emails = new ArrayList<String>(1);

    public OWSAddress(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getCity()
    {
        return (String) (this.getField("City") != null ? this.getField("City") : this.getField("city"));
    }

    public String getAdministrativeArea()
    {
        return (String) (this.getField("AdministrativeArea") != null
            ? this.getField("AdministrativeArea") : this.getField("administrativeArea"));
    }

    public List<String> getDeliveryPoints()
    {
        return this.deliveryPoints;
    }

    public List<String> getPostalCodes()
    {
        return this.postalCodes;
    }

    public List<String> getCountries()
    {
        return this.countries;
    }

    public List<String> getElectronicMailAddresses()
    {
        return this.emails;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "DeliveryPoint") || ctx.isStartElement(event, "deliveryPoint"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.deliveryPoints.add(s);
        }
        else if (ctx.isStartElement(event, "PostalCode") || ctx.isStartElement(event, "postalCode"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.postalCodes.add(s);
        }
        else if (ctx.isStartElement(event, "Country") || ctx.isStartElement(event, "country"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.countries.add(s);
        }
        else if (ctx.isStartElement(event, "ElectronicMailAddress")
            || ctx.isStartElement(event, "electronicMailAddress"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.emails.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
