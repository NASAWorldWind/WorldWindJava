/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
