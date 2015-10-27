/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;

/**
 * A gazetteer that uses Yahoo's geocoding service to find locations for requested places.
 *
 * @author tag
 * @version $Id: YahooGazetteer.java 1395 2013-06-03 22:59:07Z tgaskins $
 */
public class YahooGazetteer implements Gazetteer
{
    protected static final String GEOCODE_SERVICE =
        "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.placefinder%20where%20text%3D";

    public List<PointOfInterest> findPlaces(String lookupString) throws NoItemException, ServiceException
    {
        if (lookupString == null || lookupString.length() < 1)
        {
            return null;
        }

        String urlString;
        try
        {
            urlString = GEOCODE_SERVICE + "%22" + URLEncoder.encode(lookupString, "UTF-8") + "%22";
        }
        catch (UnsupportedEncodingException e)
        {
            urlString = GEOCODE_SERVICE + "%22" + lookupString.replaceAll(" ", "+") + "%22";
        }

        if (isNumber(lookupString))
            lookupString += "%20and%20gflags%3D%22R%22";

        String locationString = POIUtils.callService(urlString);

        if (locationString == null || locationString.length() < 1)
        {
            return null;
        }

        return this.parseLocationString(locationString);
    }

    protected boolean isNumber(String lookupString)
    {
        lookupString = lookupString.trim();

        return lookupString.startsWith("-") || lookupString.startsWith("+") || Character.isDigit(lookupString.charAt(0));
    }

    protected ArrayList<PointOfInterest> parseLocationString(String locationString) throws WWRuntimeException
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(new ByteArrayInputStream(locationString.getBytes("UTF-8")));

            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xpath = xpFactory.newXPath();

            org.w3c.dom.NodeList resultNodes =
                (org.w3c.dom.NodeList) xpath.evaluate("/query/results/Result", doc, XPathConstants.NODESET);

            ArrayList<PointOfInterest> positions = new ArrayList<PointOfInterest>(resultNodes.getLength());

            for (int i = 0; i < resultNodes.getLength(); i++)
            {
                org.w3c.dom.Node location = resultNodes.item(i);
                String lat = xpath.evaluate("latitude", location);
                String lon = xpath.evaluate("longitude", location);
                StringBuilder displayName = new StringBuilder();

                String house = xpath.evaluate("house", location);
                String street = xpath.evaluate("street", location);

                if (house != null && !house.equals(""))
                {
                    displayName.append(house);
                    displayName.append(" ");
                }

                if (street != null && !street.equals(""))
                {
                    displayName.append(street);
                    displayName.append(", ");
                }

                displayName.append(xpath.evaluate("city", location));
                displayName.append(", ");
                displayName.append(xpath.evaluate("state", location));

                if (lat != null && lon != null)
                {
                    LatLon latlon = LatLon.fromDegrees(Double.parseDouble(lat), Double.parseDouble(lon));
                    PointOfInterest loc = new BasicPointOfInterest(latlon);
                    loc.setValue(AVKey.DISPLAY_NAME, displayName.toString());
                    positions.add(loc);
                }
            }

            return positions;
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("Gazetteer.URLException", locationString);
            Logging.logger().log(Level.SEVERE, msg);
            throw new WWRuntimeException(msg);
        }
    }
}
