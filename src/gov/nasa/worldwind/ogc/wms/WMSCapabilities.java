/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.ogc.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.wms.CapabilitiesRequest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: WMSCapabilities.java 2072 2014-06-21 21:20:25Z tgaskins $
 */
public class WMSCapabilities extends OGCCapabilities
{
    protected static final QName ROOT_ELEMENT_NAME_1_1_1 = new QName("WMT_MS_Capabilities");
    protected static final QName ROOT_ELEMENT_NAME_1_3_0 = new QName("WMS_Capabilities");

    /**
     * Retrieves the WMS capabilities document from a specified WMS server.
     *
     * @param uri The URI of the server.
     *
     * @return The WMS capabilities document for the specified server.
     * @throws java.lang.Exception if a general error occurs. 
     *
     * @throws IllegalArgumentException if the specified URI is invalid.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if an error occurs retrieving the document.
     */
    public static WMSCapabilities retrieve(URI uri) throws Exception
    {
        try
        {
            CapabilitiesRequest request = new CapabilitiesRequest(uri);

            return new WMSCapabilities(request);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Parses a WMS capabilities document.
     *
     * @param docSource the XML source. May be a filename, file, stream or other type allowed by {@link
     *                  gov.nasa.worldwind.util.WWXML#openEventReader(Object)}.
     *
     * @throws IllegalArgumentException if the document source is null.
     */
    public WMSCapabilities(Object docSource)
    {
        super(OGCConstants.WMS_NAMESPACE_URI, docSource);

        this.initialize();
    }

    public WMSCapabilities(CapabilitiesRequest docSource) throws URISyntaxException, MalformedURLException
    {
        super(OGCConstants.WMS_NAMESPACE_URI, docSource.getUri().toURL());

        this.initialize();
    }

    private void initialize()
    {
        this.getParserContext().registerParser(new QName(this.getDefaultNamespaceURI(), "Service"),
            new WMSServiceInformation(this.getNamespaceURI()));
        this.getParserContext().registerParser(new QName("Capability"),
            new WMSCapabilityInformation(this.getNamespaceURI()));
    }

    @Override
    public String getDefaultNamespaceURI()
    {
        return OGCConstants.WMS_NAMESPACE_URI;
    }

    public boolean isRootElementName(QName candidate)
    {
        return this.getParserContext().isSameName(candidate, ROOT_ELEMENT_NAME_1_1_1)
            || this.getParserContext().isSameName(candidate, ROOT_ELEMENT_NAME_1_3_0);
    }

    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        if (ctx.isStartElement(event, CAPABILITY))
            return ctx.allocate(event, new WMSCapabilityInformation(this.getNamespaceURI()));
        else
            return super.allocate(ctx, event);
    }

    @Override
    public WMSCapabilities parse(Object... args) throws XMLStreamException
    {
        return (WMSCapabilities) super.parse(args);
    }

    /**
     * Returns all named layers in the capabilities document.
     *
     * @return an unordered list of the document's named layers.
     */
    public List<WMSLayerCapabilities> getNamedLayers()
    {
        if (this.getCapabilityInformation() == null || this.getCapabilityInformation().getLayerCapabilities() == null)
            return null;

        List<WMSLayerCapabilities> namedLayers = new ArrayList<WMSLayerCapabilities>();

        for (WMSLayerCapabilities layer : this.getCapabilityInformation().getLayerCapabilities())
        {
            namedLayers.addAll(layer.getNamedLayers());
        }

        return namedLayers;
    }

    public WMSLayerCapabilities getLayerByName(String name)
    {
        if (WWUtil.isEmpty(name))
            return null;

        List<WMSLayerCapabilities> namedLayers = this.getNamedLayers();
        for (WMSLayerCapabilities layer : namedLayers)
        {
            if (layer.getName().equals(name))
                return layer;
        }

        return null;
    }

    public WMSCapabilityInformation getCapabilityInformation()
    {
        return (WMSCapabilityInformation) super.getCapabilityInformation();
    }

    public Set<String> getImageFormats()
    {
        Set<OGCRequestDescription> requestDescriptions = this.getCapabilityInformation().getRequestDescriptions();
        for (OGCRequestDescription rd : requestDescriptions)
        {
            if (rd.getRequestName().equals("GetMap"))
                return rd.getFormats();
        }

        return null;
    }

    public Long getLayerLatestLastUpdateTime(String[] layerNames)
    {
        if (layerNames == null)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String lastUpdate = null;

        for (String name : layerNames)
        {
            WMSLayerCapabilities layer = this.getLayerByName(name);
            if (layer == null)
                continue;

            String update = this.getLayerLastUpdate(layer);
            if (update != null && update.length() > 0 && (lastUpdate == null || update.compareTo(lastUpdate) > 0))
                lastUpdate = update;
        }

        if (lastUpdate != null)
        {
            try
            {
                return Long.parseLong(lastUpdate);
            }
            catch (NumberFormatException e)
            {
                String message = Logging.getMessage("generic.ConversionError", lastUpdate);
                Logging.logger().warning(message);
            }
        }

        return null;
    }

    /**
     * Checks the WMS layer capabilities for a LastUpdate entry, either an explicit element by that name or a layer
     * keyword.
     *
     * @param layerCaps The layer's capabilities taken from the server's capabilities document.
     *
     * @return A string representation of the epoch time for the last update string, if any, otherwise null.
     */
    protected String getLayerLastUpdate(WMSLayerCapabilities layerCaps)
    {
        // See if there's an explicit element. This is what the original WW servers contained in their caps docs.
        String update = layerCaps.getLastUpdate();
        if (update != null)
            return update;

        // See if there's a last-update keyword. This is the new mechanism for WW servers passing a last-update.
        Set<String> keywords = layerCaps.getKeywords();
        for (String keyword : keywords)
        {
            if (keyword.startsWith("LastUpdate="))
            {
                return parseLastUpdate(keyword);
            }
        }

        return null;
    }

    /**
     * Parse a LastUpdate string.
     *
     * @param lastUpdateString The string containing the LastUpdate string in the format "LastUpdate=yyyy-MM-dd'T'HH:mm:ssZ"
     *
     * @return A string representation of the epoch time for the last update string, of null if the string can't be
     *         parsed as a date.
     */
    protected String parseLastUpdate(String lastUpdateString)
    {
        String[] splitKeyword = lastUpdateString.split("=");
        if (splitKeyword.length != 2)
            return null;

        String dateString = splitKeyword[1];
        if (dateString == null || dateString.length() == 0)
            return null;

        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // ISO 8601:2000 format
            dateString = dateString.replaceAll("Z", "-0000"); // replace the UTC designator
            return Long.toString(dateFormat.parse(dateString).getTime());
        }
        catch (ParseException e)
        {
            String message = Logging.getMessage("WMS.LastUpdateFormatUnrecognized", dateString);
            Logging.logger().info(message);
            return null;
        }
    }

    public Double[] getLayerExtremeElevations(String[] layerNames)
    {
        if (layerNames == null)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Double extremeMin = null;
        Double extremeMax = null;

        for (String name : layerNames)
        {
            WMSLayerCapabilities layer = this.getLayerByName(name);
            if (layer == null)
                continue;

            Double min = layer.getExtremeElevationMin();
            if (min != null && (extremeMin == null || min.compareTo(min) > 0))
                extremeMin = min;

            Double max = layer.getExtremeElevationMax();
            if (max != null && (extremeMax == null || max.compareTo(max) > 0))
                extremeMax = max;
        }

        if (extremeMin != null || extremeMax != null)
        {
            Double[] extremes = new Double[] {null, null};

            if (extremeMin != null)
                extremes[0] = extremeMin;
            if (extremeMax != null)
                extremes[1] = extremeMax;

            return extremes;
        }

        return null;
    }

    public OGCRequestDescription getRequestDescription(String requestName)
    {
        for (OGCRequestDescription rd : this.getCapabilityInformation().getRequestDescriptions())
        {
            if (rd.getRequestName().equalsIgnoreCase(requestName))
                return rd;
        }

        return null;
    }

    public String getRequestURL(String requestName, String protocol, String requestMethod)
    {
        OGCRequestDescription rd = this.getRequestDescription(requestName);
        if (rd != null)
        {
            OGCOnlineResource ol = rd.getOnlineResouce(protocol, requestMethod);
            return ol != null ? ol.getHref() : null;
        }

        return null;
    }

    /**
     * Indicates whether the layers corresponding to a specified list of layer names support a specified coordinate
     * system. If any of the named layers are not in this capabilities document, false is returned.
     *
     * @param layerNames The names of the layers to check.
     * @param coordSys   The coordinate system to search for, e.g., "EPSG:4326".
     *
     * @return true if all the layers support the specified coordinate system, otherwise false.
     *
     * @throws IllegalArgumentException if the layer names array is null or empty or the specified coordinate system is
     *                                  null or the empty string.
     */
    public boolean layerHasCoordinateSystem(String[] layerNames, String coordSys)
    {
        if (layerNames == null || layerNames.length == 0)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(coordSys))
        {
            String message = Logging.getMessage("nullValue.WMSCoordSys");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String name : layerNames)
        {
            WMSLayerCapabilities layerCaps = this.getLayerByName(name);
            if (layerCaps == null || !layerCaps.hasCoordinateSystem(coordSys))
                return false;
        }

        return true;
    }

    @Override
    public String toString() // TODO: Complete this method
    {
        StringBuilder sb = new StringBuilder(super.toString());

        sb.append("LAYERS\n");

        for (WMSLayerCapabilities layerCaps : this.getNamedLayers())
        {
            sb.append(layerCaps.toString()).append("\n");
        }

        return sb.toString();
    }
}
