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

import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tag
 * @version $Id: XALParserContext.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class XALParserContext extends BasicXMLEventParserContext
{
    protected static final String[] StringFields = new String[]
        {
            "Address"
        };

    public static Map<QName, XMLEventParser> getDefaultParsers()
    {
        ConcurrentHashMap<QName, XMLEventParser> parsers = new ConcurrentHashMap<QName, XMLEventParser>();

        String xns = XALConstants.XAL_NAMESPACE;
        parsers.put(new QName(xns, "Address"), new XALAddress(xns));
        parsers.put(new QName(xns, "AddressDetails"), new XALAddressDetails(xns));
        parsers.put(new QName(xns, "AddressLine"), new XALAddressLine(xns));
        parsers.put(new QName(xns, "AddressLines"), new XALAddressLines(xns));
        parsers.put(new QName(xns, "AdministrativeArea"), new XALAdministrativeArea(xns));
        parsers.put(new QName(xns, "Country"), new XALCountry(xns));
        parsers.put(new QName(xns, "CountryName"), new XALCountryName(xns));
        parsers.put(new QName(xns, "CountryNameCode"), new XALCountryNameCode(xns));
        parsers.put(new QName(xns, "Locality"), new XALLocality(xns));
        parsers.put(new QName(xns, "PostalServiceElements"), new XALPostalServiceElements(xns));
        parsers.put(new QName(xns, "Thoroughfare"), new XALThoroughfare(xns));

        StringXMLEventParser stringParser = new StringXMLEventParser();
        for (String s : StringFields)
        {
            parsers.put(new QName(xns, s), stringParser);
        }

        return parsers;
    }
}
