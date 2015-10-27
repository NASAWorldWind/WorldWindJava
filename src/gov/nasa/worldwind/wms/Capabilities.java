/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.wms;

import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: Capabilities.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class Capabilities
{
    public static final String WMS_SERVICE_NAME = "OGC:WMS";

    protected Document doc;
    protected Element service;
    protected Element capability;
    protected XPath xpath;
    protected URL capsURL;

    public static Capabilities retrieve(URI uri, String service) throws Exception
    {
        return retrieve(uri, service, null, null);
    }

    public static Capabilities retrieve(URI uri, Integer connectTimeout, Integer readTimeout) throws Exception
    {
        return retrieve(uri, null, connectTimeout, readTimeout);
    }

    public static Capabilities retrieve(URI uri, String service, Integer connectTimeout, Integer readTimeout)
        throws Exception
    {
        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream is = null;

        try
        {
            // Request the capabilities document from the server.
            CapabilitiesRequest req = new CapabilitiesRequest(uri, service);
            URL capsURL = req.getUri().toURL();

            URLRetriever retriever = URLRetriever.createRetriever(capsURL, new RetrievalPostProcessor()
            {
                public ByteBuffer run(Retriever retriever)
                {
                    return retriever.getBuffer();
                }
            });

            if (retriever == null)
            {
                String message = Logging.getMessage("generic.UnrecognizedProtocol");
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            if (connectTimeout != null)
                retriever.setConnectTimeout(connectTimeout);

            if (readTimeout != null)
                retriever.setReadTimeout(readTimeout);

            retriever.call();

            if (!retriever.getState().equals(URLRetriever.RETRIEVER_STATE_SUCCESSFUL))
            {
                String message = Logging.getMessage("generic.RetrievalFailed", uri.toString());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            if (retriever.getBuffer() == null || retriever.getBuffer().limit() == 0)
            {
                String message = Logging.getMessage("generic.RetrievalReturnedNoContent", uri.toString());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            if (retriever.getContentType().equalsIgnoreCase("application/vnd.ogc.se_xml"))
            {
                String exceptionMessage = WWXML.extractOGCServiceException(retriever.getBuffer());
                String message = Logging.getMessage("WMS.ServiceException",
                    uri.toString() + ": " + (exceptionMessage != null ? exceptionMessage : ""));
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            // Parse the DOM as a capabilities document.
            is = WWIO.getInputStreamFromByteBuffer(retriever.getBuffer());

            Capabilities caps = Capabilities.parse(WWXML.createDocumentBuilder(true).parse(is));
            if (caps != null)
                caps.capsURL = capsURL;
            
            return caps;
        }
        catch (URISyntaxException e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE,
                Logging.getMessage("generic.URIInvalid", uri.toString()), e);
            throw e;
        }
        catch (ParserConfigurationException e)
        {
            Logging.logger().fine(Logging.getMessage("WMS.ParserConfigurationException", uri.toString()));
            throw e;
        }
        catch (IOException e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE,
                Logging.getMessage("generic.ExceptionAttemptingToReadFrom", uri.toString()), e);
            throw e;
        }
        catch (SAXException e)
        {
            Logging.logger().fine(Logging.getMessage("WMS.ParsingError", uri.toString()));
            throw e;
        }
        finally
        {
            WWIO.closeStream(is, uri.toString());
        }
    }

    public static Capabilities parse(Document doc)
    {
        XPath xpath = WWXML.makeXPath();
        xpath.setNamespaceContext(new WMSNamespaceContext());

        try
        {
            String exceptionMessage = WWXML.checkOGCException(doc);
            if (exceptionMessage != null)
            {
                String message = Logging.getMessage("WMS.ServiceException", exceptionMessage);
                Logging.logger().severe(message);
                throw new ServiceException(exceptionMessage);
            }

            String version = xpath.evaluate(altPaths("*/@wms:version"), doc);
            if (version == null || version.length() == 0)
                return null;

            if (version.compareTo("1.3") < 0)
                return new CapabilitiesV111(doc, xpath);
            else
                return new CapabilitiesV130(doc, xpath);
        }
        catch (XPathExpressionException e)
        {
            Logging.logger().log(Level.SEVERE, "WMS.ParsingError", e);
            return null;
        }
    }

    protected Capabilities(Document doc, XPath xpath)
    {
        this.doc = doc;
        this.xpath = xpath;

        try
        {
            this.service = (Element) this.xpath.evaluate(altPaths("*/wms:Service"), doc, XPathConstants.NODE);
            if (this.service == null)
            {
                String message = Logging.getMessage("WMS.NoServiceElement", "XML document");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.capability = (Element) this.xpath.evaluate(altPaths("*/wms:Capability"), doc, XPathConstants.NODE);
            if (this.capability == null)
            {
                String message = Logging.getMessage("WMS.NoCapabilityElement", "XML document");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }
        catch (XPathExpressionException e)
        {
            Logging.logger().log(Level.SEVERE, "WMS.ParsingError", e);
        }
    }

    public URL getCapsURL()
    {
        return capsURL;
    }

    private static String altPaths(String path) // hack for WW server layer names with leading pipe
    {
        return path != null ? path + "|" + path.replaceAll("wms:", "") : null;
    }

    protected String getText(String path)
    {
        return this.getText(null, path);
    }

    protected String getText(Element context, String path)
    {
        try
        {
            return this.xpath.evaluate(altPaths(path), context != null ? context : doc);
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    protected String[] getTextArray(Element context, String path)
    {
        try
        {
            NodeList nodes = (NodeList) this.xpath.evaluate(altPaths(path), context != null ? context : doc,
                XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return null;

            String[] strings = new String[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++)
            {
                strings[i] = nodes.item(i).getTextContent();
            }
            return strings;
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    protected String[] getUniqueText(Element context, String path)
    {
        String[] strings = this.getTextArray(context, path);
        if (strings == null)
            return null;

        ArrayList<String> sarl = new ArrayList<String>();
        for (String s : strings)
        {
            if (!sarl.contains(s))
                sarl.add(s);
        }

        return sarl.toArray(new String[1]);
    }

    protected Element getElement(Element context, String path)
    {
        try
        {
            Node node = (Node) this.xpath.evaluate(altPaths(path), context != null ? context : doc,
                XPathConstants.NODE);
            if (node == null)
                return null;

            return node instanceof Element ? (Element) node : null;
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    protected Element[] getElements(Element context, String path)
    {
        try
        {
            NodeList nodes = (NodeList) this.xpath.evaluate(altPaths(path), context != null ? context : doc,
                XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return null;

            Element[] elements = new Element[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                if (node instanceof Element)
                    elements[i] = (Element) node;
            }
            return elements;
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    protected Element[] getUniqueElements(Element context, String path, String uniqueTag)
    {
        Element[] elements = this.getElements(context, path);
        if (elements == null)
            return null;

        HashMap<String, Element> styles = new HashMap<String, Element>();
        for (Element e : elements)
        {
            String name = this.getText(e, uniqueTag);
            if (name != null)
                styles.put(name, e);
        }

        return styles.values().toArray(new Element[1]);
    }

    private HashMap<Element, Layer> namedLayerElements = new HashMap<Element, Layer>();
    private HashMap<String, Layer> namedLayers = new HashMap<String, Layer>();

    private void fillLayerList()
    {
        if (this.namedLayers.size() == 0)
        {
            Element[] nels = this.getElements(this.capability, "descendant::wms:Layer[wms:Name]");
            if (nels == null || nels.length == 0)
                return;

            for (Element le : nels)
            {
                String name = this.getLayerName(le);
                if (name != null)
                {
                    Layer layer = new Layer(le);
                    this.namedLayers.put(name, layer);
                    this.namedLayerElements.put(le, layer);
                }
            }
        }
    }

    public Document getDocument()
    {
        return this.doc;
    }

    public Element[] getNamedLayers()
    {
        if (this.namedLayerElements.size() == 0)
            this.fillLayerList();

        return this.namedLayerElements.keySet().toArray(new Element[this.namedLayerElements.size()]);
    }

    public Element getLayerByName(String layerName)
    {
        if (this.namedLayers.size() == 0)
            this.fillLayerList();

        Layer l = this.namedLayers.get(layerName);
        return l != null ? l.element : null;
    }

    public Long getLayerLatestLastUpdateTime(Capabilities caps, String[] layerNames)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (layerNames == null)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String lastUpdate = null;

        for (String name : layerNames)
        {
            Element layer = caps.getLayerByName(name);
            if (layer == null)
                continue;

            String update = caps.getLayerLastUpdate(layer);
            if (update != null && update.length() > 0 && (lastUpdate == null || update.compareTo(lastUpdate) > 0))
                lastUpdate =  update;
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

    public Double[] getLayerExtremeElevations(Capabilities caps, String[] layerNames)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (layerNames == null)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String extremeMin = null;
        String extremeMax = null;

        for (String name : layerNames)
        {
            Element layer = caps.getLayerByName(name);
            if (layer == null)
                continue;

            String min = caps.getLayerExtremeElevationsMin(layer);
            if (min != null && (extremeMin == null || min.compareTo(min) > 0))
                extremeMin =  min;

            String max = caps.getLayerExtremeElevationsMax(layer);
            if (max != null && (extremeMax == null || max.compareTo(max) > 0))
                extremeMax =  max;
        }

        if (extremeMin != null || extremeMax != null)
        {
            try
            {
                Double[] extremes = new Double[] {null, null};

                if (extremeMin != null)
                    extremes[0] = Double.parseDouble(extremeMin);
                if (extremeMax != null)
                    extremes[1] = Double.parseDouble(extremeMax);

                return extremes;
            }
            catch (NumberFormatException e)
            {
                String message = Logging.getMessage("generic.ConversionError",
                    extremeMin != null ? extremeMin : "" + extremeMax != null ? extremeMax : "");
                Logging.logger().severe(message);
            }
        }

        return null;
    }

    // ********* Document Items ********* //

    public String getVersion()
    {
        return this.getText("*/@wms:version");
    }

    public String getUpdateSequence()
    {
        return this.getText("*/@wms:updateSequence");
    }

    // ********* Service Items ********* //

    public String getAbstract()
    {
        return this.getText(this.service, "wms:Abstract");
    }

    public String getAccessConstraints()
    {
        return this.getText(this.service, "wms:AccessConstraints");
    }

    public String getContactOrganization()
    {
        return this.getText(
            this.service, "wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactOrganization");
    }

    public String getContactPerson()
    {
        return this.getText(
            this.service, "wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactPerson");
    }

    public String getFees()
    {
        return this.getText(this.service, "wms:Fees");
    }

    public String[] getKeywordList()
    {
        return this.getTextArray(this.service, "wms:KeywordList/wms:Keyword");
    }

    public String getLayerLimit()
    {
        return this.getText(this.service, "wms:LayerLimit");
    }

    public String getMaxWidth()
    {
        return this.getText(this.service, "wms:MaxWidth");
    }

    public String getMaxHeight()
    {
        return this.getText(this.service, "wms:MaxHeight");
    }

    public String getServiceName()
    {
        return this.getText(this.service, "wms:Name");
    }

    public String getTitle()
    {
        return this.getText(this.service, "wms:Title");
    }

    // ********* Capability Items ********* //

    public String getOnlineResource()
    {
        return this.getText(this.capability, "wms:OnlineResource/@xlink:href");
    }

    public String[] getGetCapabilitiesFormats()
    {
        return this.getTextArray(this.capability,
            "wms:Request/wms:GetCapabilities/wms:Format");
    }

    public String getGetCapabilitiesRequestGetURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href");
    }

    public String getGetCapabilitiesRequestPostURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href");
    }

    public String[] getExceptionFormats()
    {
        return this.getTextArray(this.capability, "wms:Exception/wms:Format");
    }

    public String getFeatureInfoRequestGetURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href");
    }

    public String getFeatureInfoRequestPostURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href");
    }

    public String[] getGetMapFormats()
    {
        return this.getTextArray(this.capability,
            "wms:Request/wms:GetMap/wms:Format");
    }

    public String getGetMapRequestGetURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetMap/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href");
    }

    public String getGetMapRequestPostURL()
    {
        return this.getText(this.capability,
            "wms:Request/wms:GetMap/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href");
    }

    public String getVendorSpecificCapabilities()
    {
        return this.getText(this.capability, "wms:VendorSpecificCapabilities");
    }

    public Element getLayer()
    {
        return this.getElement(this.capability, "wms:Layer");
    }

    // ********* Layer Items ********* //

    protected static class Layer
    {
        protected HashMap<Element, Style> styleElements = new HashMap<Element, Style>();
        protected final Element element;
        protected Layer layer;
        protected String name;
        protected String title;

        public Layer(Element element)
        {
            this.element = element;
        }
    }

    public String getLayerAbstract(Element layer)
    {
        return this.getText(layer, "wms:Abstract");
    }

    public String getLayerAttributionTitle(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:Title");
    }

    public String getLayerAttributionURL(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:OnlineResource/@xlink:href");
    }

    public String getLayerAttributionLogoFormat(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/wms:Format");
    }

    public String getLayerAttributionLogoHeight(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/@wms:height");
    }

    public String getLayerAttributionLogoURL(Element layer)
    {
        return this.getText(layer,
            "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/wms:OnlineResource/@xlink:href");
    }

    public String getLayerAttributionLogoWidth(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:Attribution/wms:LogoURL/@wms:width");
    }

    public Element[] getLayerAuthorityURLs(Element layer)
    {
        return this.getUniqueElements(layer, "ancestor-or-self::wms:Layer/wms:AuthorityURL", "@wms:type");
    }

    public abstract BoundingBox[] getLayerBoundingBoxes(Element layer);

    public String getLayerCascaded(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@cascaded");
    }

    public String[] getLayerCRS(Element layer)
    {
        return this.getUniqueText(layer, "ancestor-or-self::wms:Layer/wms:CRS");
    }

    public String getLayerDataURLFormat(Element layer)
    {
        return this.getText(layer, "wms:DataURL/wms:Format");
    }

    public String getLayerDataURL(Element layer)
    {
        return this.getText(layer, "wms:DataURL/wms:OnlineResource/@xlink:href");
    }

    public Element[] getLayerDimensions(Element layer)
    {
        Element[] dims = this.getElements(layer, "ancestor-or-self::wms:Layer/wms:Dimension");

        if (dims == null || dims.length == 0)
            return null;

        ArrayList<Element> uniqueDims = new ArrayList<Element>();
        ArrayList<String> dimNames = new ArrayList<String>();
        for (Element e : dims)
        {
            // Filter out dimensions with same name.
            // Keep all those with a null name, even though wms says they're invalid. Let the app decide.
            String name = this.getDimensionName(e);
            if (name != null && dimNames.contains(name))
                continue;

            uniqueDims.add(e);
            dimNames.add(name);
        }

        return uniqueDims.toArray(new Element[uniqueDims.size()]);
    }

    public Element[] getLayerExtents(Element layer)
    {
        Element[] extents = this.getElements(layer, "ancestor-or-self::wms:Layer/wms:Extent");

        if (extents == null || extents.length == 0)
            return null;

        ArrayList<Element> uniqueExtents = new ArrayList<Element>();
        ArrayList<String> extentNames = new ArrayList<String>();
        for (Element e : extents)
        {
            // Filter out dimensions with same name.
            // Keep all those with a null name, even though wms says they're invalid. Let the app decide.
            String name = this.getDimensionName(e);
            if (name != null && extentNames.contains(name))
                continue;

            uniqueExtents.add(e);
            extentNames.add(name);
        }

        return uniqueExtents.toArray(new Element[uniqueExtents.size()]);
    }

    public abstract BoundingBox getLayerGeographicBoundingBox(Element layer);

    public String getLayerFeatureListFormat(Element layer)
    {
        return this.getText(layer, "wms:FeatureListURL/wms:Format");
    }

    public String getLayerFeatureListURL(Element layer)
    {
        return this.getText(layer, "wms:FeatureListURL/wms:OnlineResource/@xlink:href");
    }

    public String getLayerFixedHeight(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@fixedHeight");
    }

    public String getLayerFixedWidth(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@fixedWidth");
    }

    public Element[] getLayerIdentifiers(Element layer)
    {
        return this.getUniqueElements(layer, "wms:Identifier", "wms:authority");
    }

    public String[] getLayerKeywordList(Element layer)
    {
        return this.getTextArray(layer, "wms:KeywordList/wms:Keyword");
    }

    public abstract String getLayerMaxScaleDenominator(Element layer);

    public Element[] getLayerMetadataURLs(Element layer)
    {
        return this.getElements(layer, "wms:MetadataURL");
    }

    public abstract String getLayerMinScaleDenominator(Element layer);

    public String getLayerName(Element layerElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        return layer != null && layer.name != null ? layer.name : this.getText(layerElement, "wms:Name");
    }

    public String getLayerNoSubsets(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@noSubsets");
    }

    public String getLayerOpaque(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@opaque");
    }

    public String getLayerQueryable(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/@queryable");
    }

    public String[] getLayerSRS(Element layer)
    {
        return this.getUniqueText(layer, "ancestor-or-self::wms:Layer/wms:SRS");
    }

    public Element[] getLayerStyles(Element layerElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null)
            return null;

        if (layer.styleElements != null && layer.styleElements.size() != 0)
            return layer.styleElements.keySet().toArray(new Element[1]);

        Element[] styleElements = this.getUniqueElements(layerElement, "ancestor-or-self::wms:Layer/wms:Style", "Name");
        if (styleElements == null)
            return null;

        layer.styleElements = new HashMap<Element, Style>();
        for (Element se : styleElements)
        {
            Style style = new Style(se, layer);
            layer.styleElements.put(se, style);
            this.styleElements.put(se, style);
        }

        return layer.styleElements.keySet().toArray(new Element[1]);
    }

    public Element[] getLayerSubLayers(Element layer)
    {
        return this.getElements(layer, "wms:Layer");
    }

    public String getLayerTitle(Element layerElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null)
            return this.getText(layerElement, "wms:Title");

        return layer.title != null ? layer.title : (layer.title = this.getText(layerElement, "wms:Title"));
    }

    public Element getLayerStyleByName(Element layerElement, String styleName)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null)
            return null;

        if (layer.styleElements == null || layer.styleElements.size() == 0)
        {
            // Initialize the layer's style list.
            this.getLayerStyles(layerElement);
            if (layer.styleElements == null || layer.styleElements.size() == 0)
                return null;
        }

        Collection<Style> styles = layer.styleElements.values();
        for (Style s : styles)
        {
            if (s != null && s.name != null && s.name.equals(styleName))
                return s.element;
        }

        return null;
    }

    public String getLayerLastUpdate(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:LastUpdate");
    }

    public String getLayerExtremeElevationsMin(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:ExtremeElevations/@min");
    }

    public String getLayerExtremeElevationsMax(Element layer)
    {
        return this.getText(layer, "ancestor-or-self::wms:Layer/wms:ExtremeElevations/@max");
    }

    // ********* Style Items ********* //

    protected HashMap<Element, Style> styleElements = new HashMap<Element, Style>();

    protected static class Style
    {
        protected final Layer layer;
        protected final Element element;
        protected String name;
        protected String title;

        public Style(Element element, Layer layer)
        {
            this.element = element;
            this.layer = layer;
        }
    }

    public String getStyleAbstract(Element styleElement)
    {
        return this.getText(styleElement, "wms:Abstract");
    }

    public String getStyleLegendFormat(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/wms:Format");
    }

    public String getStyleLegendHeight(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/@height");
    }

    public String getStyleLegendURL(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/wms:OnlineResource/@xlink:href");
    }

    public String getStyleLegendWidth(Element styleElement)
    {
        return this.getText(styleElement, "wms:LegendURL/@width");
    }

    public String getStyleName(Element styleElement)
    {
        Style style = this.styleElements.get(styleElement);
        return style != null && style.title != null ? style.title : this.getText(styleElement, "wms:Name");
    }

    public String getStyleName(Element layerElement, Element styleElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null || layer.styleElements == null)
            return this.getStyleName(layerElement, styleElement);

        Style style = layer.styleElements.get(styleElement);

        return style != null && style.name != null ? style.title : this.getText(styleElement, "wms:Name");
    }

    public String getStyleSheetURLFormat(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleSheetURL/wms:Format");
    }

    public String getStyleSheetURL(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleSheetURL/wms:OnlineResource/@xlink:href");
    }

    public String getStyleTitle(Element styleElement)
    {
        Style style = this.styleElements.get(styleElement);
        return style != null && style.title != null ? style.title : this.getText(styleElement, "wms:Title");
    }

    public String getStyleTitle(Element layerElement, Element styleElement)
    {
        Layer layer = this.namedLayerElements.get(layerElement);
        if (layer == null || layer.styleElements == null)
            return this.getStyleTitle(styleElement);

        Style style = this.styleElements.get(styleElement);
        return style != null && style.title != null ? style.title : this.getText(styleElement, "wms:Title");
    }

    public String getStyleURL(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleURL/wms:OnlineResource/@xlink:href");
    }

    public String getStyleURLFormat(Element styleElement)
    {
        return this.getText(styleElement, "wms:StyleURL/wms:Format");
    }

    // ********* Authority Items ********* //

    public String getAuthorityName(Element authority)
    {
        return this.getText(authority, "@wms:name");
    }

    public String getAuthorityURL(Element authority)
    {
        return this.getText(authority, "wms:OnlineResource/@xlink:href");
    }

    // ********* Identifier Items ********* //

    public String getIdentifier(Element identifier)
    {
        return this.getText(identifier, ".");
    }

    public String getIdentifierAuthority(Element identifier)
    {
        return this.getText(identifier, "@wms:authority");
    }

    // ********* Metadata Items ********* //

    public String getMetadataFormat(Element metadata)
    {
        return this.getText(metadata, "wms:Format");
    }

    public String getMetadataURL(Element metadata)
    {
        return this.getText(metadata, "wms:OnlineResource/@xlink:href");
    }

    public String getMetadataType(Element metadata)
    {
        return this.getText(metadata, "@wms:type");
    }

    // ********* EX_GeographicBoundingBox Items ********* //

    public String getWestBoundLongitude(Element bbox)
    {
        return this.getText(bbox, "wms:westBoundLongitude");
    }

    public String getEastBoundLongitude(Element bbox)
    {
        return this.getText(bbox, "wms:eastBoundLongitude");
    }

    public String getSouthBoundLatitude(Element bbox)
    {
        return this.getText(bbox, "wms:southBoundLatitude");
    }

    public String getNorthBoundLatitude(Element bbox)
    {
        return this.getText(bbox, "wms:northBoundLatitude");
    }

    // ********* BoundingBox Items ********* //

    public String getBoundingBoxCRS(Element bbox)
    {
        return this.getText(bbox, "@wms:CRS");
    }

    public String getBoundingBoxMinx(Element bbox)
    {
        return this.getText(bbox, "@wms:minx");
    }

    public String getBoundingBoxMiny(Element bbox)
    {
        return this.getText(bbox, "@wms:miny");
    }

    public String getBoundingBoxMaxx(Element bbox)
    {
        return this.getText(bbox, "@wms:maxx");
    }

    public String getBoundingBoxMaxy(Element bbox)
    {
        return this.getText(bbox, "@wms:maxy");
    }

    public String getBoundingBoxResx(Element bbox)
    {
        return this.getText(bbox, "@wms:resx");
    }

    public String getBoundingBoxResy(Element bbox)
    {
        return this.getText(bbox, "@wms:resy");
    }

    public String getBoundingBoxSRS(Element bbox)
    {
        return this.getText(bbox, "@wms:SRS");
    }

    // ********* Dimension Items ********* //

    public String getDimensionName(Element dimension)
    {
        return this.getText(dimension, "@wms:name");
    }

    public String getDimensionUnits(Element dimension)
    {
        return this.getText(dimension, "@wms:units");
    }

    public String getDimensionUnitSymbol(Element dimension)
    {
        return this.getText(dimension, "@wms:unitSymbol");
    }

    public String getDimensionDefault(Element dimension)
    {
        return this.getText(dimension, "@wms:default");
    }

    public String getDimensionMultipleValues(Element dimension)
    {
        return this.getText(dimension, "@wms:multipleValues");
    }

    public String getDimensionNearestValue(Element dimension)
    {
        return this.getText(dimension, "@wms:nearestValue");
    }

    public String getDimensionCurrent(Element dimension)
    {
        return this.getText(dimension, "@wms:current");
    }

    public String getDimensionExtent(Element dimension)
    {
        return this.getText(dimension, ".");
    }

    // ********* Extent Items, wms 1.1 only ********* //

    public String getExtentName(Element dimension)
    {
        return this.getText(dimension, "@wms:name");
    }

    public String getExtentDefault(Element dimension)
    {
        return this.getText(dimension, "@wms:default");
    }

    public String getExtentMultipleValues(Element dimension)
    {
        return this.getText(dimension, "@wms:multipleValues");
    }

    public String getExtentNearestValue(Element dimension)
    {
        return this.getText(dimension, "@wms:nearestValue");
    }

    public String getExtentCurrent(Element dimension)
    {
        return this.getText(dimension, "@wms:current");
    }

    public String getExtentText(Element dimension)
    {
        return this.getText(dimension, ".");
    }
//
//    public static void main(String[] args)
//    {
//        try
//        {
//            String server = "http://localhost:8080";
//            URI serverURI = new URI(server.trim());
//
//            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//            docBuilderFactory.setNamespaceAware(true);
//
//            if( Configuration.getJavaVersion() >= 1.6 )
//            {
//                try
//                {
//                    docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//                }
//                catch (ParserConfigurationException e)
//                {   // Note it and continue on. Some Java5 parsers don't support the feature.
//                    String message = Logging.getMessage("XML.NonvalidatingNotSupported");
//                    Logging.logger().finest(message);
//                }
//            }
//
//            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//            Document doc;
//
//            // Request the capabilities document from the server.
//            CapabilitiesRequest req = new CapabilitiesRequest(serverURI);
//            doc = docBuilder.parse(req.toString());
//
//            // Parse the DOM as a capabilities document.
//            Capabilities caps = Capabilities.parse(doc);
//
//            final Element[] namedLayerCaps = caps.getNamedLayers();
//            if (namedLayerCaps == null)
//                return;
//
//            for (Element layerCaps : namedLayerCaps)
//            {
//                String layerName = caps.getLayerName(layerCaps);
//                System.out.println("Layer name: " + (layerName != null ? layerName : "none"));
//                Element[] styles = caps.getLayerStyles(layerCaps);
//                if (styles != null)
//                {
//                    for (Element style : styles)
//                    {
//                        String styleName = caps.getStyleName(layerCaps, style);
//                        System.out.println("\tStyle name: " + (styleName != null ? styleName : "none"));
//                        String legendURL = caps.getStyleLegendURL(style);
//                        System.out.println("\tLegend URL: " + (legendURL != null ? legendURL : "none"));
//                    }
//                }
//            }
//        }
//        catch (URISyntaxException e)
//        {
//            e.printStackTrace();
//        }
//        catch (ParserConfigurationException e)
//        {
//            e.printStackTrace();
//        }
//        catch (SAXException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
