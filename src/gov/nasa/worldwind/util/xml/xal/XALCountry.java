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

import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * @author tag
 * @version $Id: XALCountry.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class XALCountry extends XALAbstractObject
{
    protected List<XALAddressLine> addressLines;
    protected List<XALCountryNameCode> countryNameCodes;
    protected List<XALCountryName> countryNames;

    public XALCountry(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof XALAddressLine)
            this.addAddressLine((XALAddressLine) o);
        else if (o instanceof XALCountryNameCode)
            this.addCountryNameCode((XALCountryNameCode) o);
        else if (o instanceof XALCountryName)
            this.addCountryName((XALCountryName) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public List<XALAddressLine> getAddressLines()
    {
        return this.addressLines;
    }

    protected void addAddressLine(XALAddressLine o)
    {
        if (this.addressLines == null)
            this.addressLines = new ArrayList<XALAddressLine>();

        this.addressLines.add(o);
    }

    public List<XALCountryNameCode> getCountryNameCodes()
    {
        return this.countryNameCodes;
    }

    protected void addCountryNameCode(XALCountryNameCode o)
    {
        if (this.countryNameCodes == null)
            this.countryNameCodes = new ArrayList<XALCountryNameCode>();

        this.countryNameCodes.add(o);
    }

    public List<XALCountryName> getCountryNames()
    {
        return this.countryNames;
    }

    protected void addCountryName(XALCountryName o)
    {
        if (this.countryNames == null)
            this.countryNames = new ArrayList<XALCountryName>();

        this.countryNames.add(o);
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
}
