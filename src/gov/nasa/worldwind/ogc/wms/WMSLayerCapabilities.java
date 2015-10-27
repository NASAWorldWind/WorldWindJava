/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.OGCBoundingBox;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.util.*;

/**
 * Parses a WMS Layer element.
 *
 * @author tag
 * @version $Id: WMSLayerCapabilities.java 1931 2014-04-14 21:31:43Z tgaskins $
 */
public class WMSLayerCapabilities extends AbstractXMLEventParser
{
    protected QName ABSTRACT;
    protected QName ATTRIBUTION;
    protected QName AUTHORITY_URL;
    protected QName BOUNDING_BOX;
    protected QName CRS;
    protected QName DATA_URL;
    protected QName DIMENSION;
    protected QName EXTENT;
    protected QName EXTREME_ELEVATIONS;
    protected QName FEATURE_LIST_URL;
    protected QName GEOGRAPHIC_BOUNDING_BOX;
    protected QName IDENTIFIER;
    protected QName KEYWORD_LIST;
    protected QName KEYWORD;
    protected QName LAST_UPDATE;
    protected QName LAT_LON_BOUNDING_BOX; // 1.1.1
    protected QName LAYER;
    protected QName MAX_SCALE_DENOMINATOR;
    protected QName METADATA_URL;
    protected QName MIN_SCALE_DENOMINATOR;
    protected QName NAME;
    protected QName SCALE_HINT;
    protected QName SRS;
    protected QName STYLE;
    protected QName TITLE;

    protected Set<WMSLayerAttribution> attributions;
    protected Set<WMSAuthorityURL> authorityURLs;
    protected Set<OGCBoundingBox> boundingBoxes;
    protected Boolean cascaded;
    protected Set<String> crs;
    protected Set<WMSLayerInfoURL> dataURLs;
    protected Set<WMSLayerDimension> dimensions;
    protected Set<WMSLayerExtent> extents; // 1.1.1
    protected Double extremeElevationMin;
    protected Double extremeElevationMax;
    protected Set<WMSLayerInfoURL> featureListURLs;
    protected Integer fixedHeight;
    protected Integer fixedWidth;
    protected Sector geographicBoundingBox;
    protected Set<WMSLayerIdentifier> identifiers;
    protected Set<String> keywords;
    protected String lastUpdate;
    protected String layerAbstract;
    protected List<WMSLayerCapabilities> layers;
    protected Double maxScaleDenominator;
    protected Double maxScaleHint;
    protected Set<WMSLayerInfoURL> metadataURLs;
    protected Double minScaleDenominator;
    protected Double minScaleHint;
    protected String name;
    protected Boolean noSubsets;
    protected Boolean opaque;
    protected Boolean queryable;
    protected Set<String> srs; // 1.1.1
    protected Set<WMSLayerStyle> styles;
    protected String title;
    protected WMSLayerCapabilities parent;
    protected WMSCapabilityInformation enclosingCapabilityInformation;

    public WMSLayerCapabilities(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        ABSTRACT = new QName(this.getNamespaceURI(), "Abstract");
        ATTRIBUTION = new QName(this.getNamespaceURI(), "Attribution");
        AUTHORITY_URL = new QName(this.getNamespaceURI(), "AuthorityURL");
        BOUNDING_BOX = new QName(this.getNamespaceURI(), "BoundingBox");
        CRS = new QName(this.getNamespaceURI(), "CRS");
        DATA_URL = new QName(this.getNamespaceURI(), "DataURL");
        DIMENSION = new QName(this.getNamespaceURI(), "Dimension");
        EXTENT = new QName(this.getNamespaceURI(), "Extent");
        EXTREME_ELEVATIONS = new QName(this.getNamespaceURI(), "ExtremeElevations");
        FEATURE_LIST_URL = new QName(this.getNamespaceURI(), "FeatureListURL");
        GEOGRAPHIC_BOUNDING_BOX = new QName(this.getNamespaceURI(), "EX_GeographicBoundingBox");
        IDENTIFIER = new QName(this.getNamespaceURI(), "Identifier");
        KEYWORD_LIST = new QName(this.getNamespaceURI(), "KeywordList");
        KEYWORD = new QName(this.getNamespaceURI(), "Keyword");
        LAST_UPDATE = new QName(this.getNamespaceURI(), "LastUpdate");
        LAT_LON_BOUNDING_BOX = new QName(this.getNamespaceURI(), "LatLonBoundingBox");
        LAYER = new QName(this.getNamespaceURI(), "Layer");
        MAX_SCALE_DENOMINATOR = new QName(this.getNamespaceURI(), "MaxScaleDenominator");
        METADATA_URL = new QName(this.getNamespaceURI(), "MetadataURL");
        MIN_SCALE_DENOMINATOR = new QName(this.getNamespaceURI(), "MinScaleDenominator");
        NAME = new QName(this.getNamespaceURI(), "Name");
        SCALE_HINT = new QName(this.getNamespaceURI(), "ScaleHint");
        SRS = new QName(this.getNamespaceURI(), "SRS");
        STYLE = new QName(this.getNamespaceURI(), "Style");
        TITLE = new QName(this.getNamespaceURI(), "Title");
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        XMLEventParser parser = super.allocate(ctx, event);
        if (parser != null)
            return parser;

        if (ctx.isStartElement(event, LAYER))
            defaultParser = new WMSLayerCapabilities(this.getNamespaceURI());
        else if (ctx.isStartElement(event, STYLE))
            defaultParser = new WMSLayerStyle(this.getNamespaceURI());
        else if (ctx.isStartElement(event, KEYWORD_LIST))
            defaultParser = new StringSetXMLEventParser(this.getNamespaceURI(), KEYWORD);
        else if (ctx.isStartElement(event, BOUNDING_BOX))
            defaultParser = new OGCBoundingBox(this.getNamespaceURI());
        else if (ctx.isStartElement(event, ATTRIBUTION))
            defaultParser = new WMSLayerAttribution(this.getNamespaceURI());
        else if (ctx.isStartElement(event, IDENTIFIER))
            defaultParser = new WMSLayerIdentifier(this.getNamespaceURI());
        else if (ctx.isStartElement(event, DIMENSION))
            defaultParser = new WMSLayerDimension(this.getNamespaceURI());
        else if (ctx.isStartElement(event, EXTENT))
            defaultParser = new WMSLayerExtent(this.getNamespaceURI());
        else if (ctx.isStartElement(event, AUTHORITY_URL))
            defaultParser = new WMSAuthorityURL(this.getNamespaceURI());
        else if (ctx.isStartElement(event, DATA_URL))
            defaultParser = new WMSLayerInfoURL(this.getNamespaceURI());
        else if (ctx.isStartElement(event, FEATURE_LIST_URL))
            defaultParser = new WMSLayerInfoURL(this.getNamespaceURI());
        else if (ctx.isStartElement(event, METADATA_URL))
            defaultParser = new WMSLayerInfoURL(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    public boolean isLeaf()
    {
        return this.getLayers().size() == 0;
    }

    public void setEnclosingCapabilityInformation(WMSCapabilityInformation caps)
    {
        this.enclosingCapabilityInformation = caps;

        // Resolve inherited attributes for children.
        for (WMSLayerCapabilities lc : this.getLayers())
        {
            lc.setEnclosingCapabilityInformation(caps);
        }
    }

    public WMSCapabilityInformation getEnclosingCapabilityInformation()
    {
        return enclosingCapabilityInformation;
    }

    public void resolveAttributes(WMSLayerCapabilities parentLayer)
    {
        this.parent = parentLayer;

        // The following are inherited from parent if not specified in child, otherwise they're assigned a default.
        if (this.getCascaded() == null)
            this.setCascaded(this.parent != null ? this.parent.getCascaded() : false);

        if (this.queryable == null)
            this.setQueryable(this.parent != null ? this.parent.isQueryable() : false);

        if (this.noSubsets == null)
            this.setNoSubsets(this.parent != null ? this.parent.isNoSubsets() : false);

        if (this.opaque == null)
            this.setOpaque(this.parent != null ? this.parent.isOpaque() : false);

        if (this.getFixedWidth() == null)
            this.setFixedWidth(this.parent != null ? this.parent.getFixedWidth() : 0);

        if (this.getFixedHeight() == null)
            this.setFixedHeight(this.parent != null ? this.parent.getFixedHeight() : 0);

        // The rest have add or replace inheritance and no default.
        if (this.parent != null)
        {
            if (this.getGeographicBoundingBox() == null) // geo box inherited from parent if not specified in child
                this.setGeographicBoundingBox(this.parent.getGeographicBoundingBox());

            if (this.getMinScaleDenominator() == null) // scales are inherited from parent if not specified in child
                this.setMinScaleDenominator(this.parent.getMinScaleDenominator());

            if (this.getMaxScaleDenominator() == null) // scales are inherited from parent if not specified in child
                this.setMaxScaleDenominator(this.parent.getMaxScaleDenominator());

            if (this.getExtremeElevationMin() == null) // extremes are inherited from parent if not specified in child
                this.setExtremeElevationMin(this.parent.getExtremeElevationMin());

            if (this.getExtremeElevationMax() == null) // extremes are inherited from parent if not specified in child
                this.setExtremeElevationMax(this.parent.getExtremeElevationMax());

            // The following are additive.
            this.addStyles(this.parent.getStyles());
            this.addCRS(this.parent.getCRS());
            this.addAuthorityURLs(this.parent.getAuthorityURLs());
            this.addBoundingBoxes(this.parent.getBoundingBoxes());
            this.addDimensions(this.parent.getDimensions());
            this.addAttributions(this.parent.getAttributions());
        }

        // Resolve inherited attributes for children.
        for (WMSLayerCapabilities caps : this.getLayers())
        {
            caps.resolveAttributes(this);
        }
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, LAYER))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerCapabilities)
                    this.addLayer(((WMSLayerCapabilities) o));
            }
        }
        else if (ctx.isStartElement(event, TITLE))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setTitle(s);
        }
        else if (ctx.isStartElement(event, NAME))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setName(s);
        }
        else if (ctx.isStartElement(event, STYLE))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerStyle)
                    this.addStyle(((WMSLayerStyle) o));
            }
        }
        else if (ctx.isStartElement(event, SRS))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.addSRS(s);
        }
        else if (ctx.isStartElement(event, CRS))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.addCRS(s);
        }
        else if (ctx.isStartElement(event, GEOGRAPHIC_BOUNDING_BOX))
        {
            this.parseGeographicBoundingBox(ctx, event);
        }
        else if (ctx.isStartElement(event, LAT_LON_BOUNDING_BOX))
        {
            this.parseGeographicBoundingBoxV111(ctx, event);
        }
        else if (ctx.isStartElement(event, ABSTRACT))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setLayerAbstract(s);
        }
        else if (ctx.isStartElement(event, LAST_UPDATE))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.setLastUpdate(s);
        }
        else if (ctx.isStartElement(event, MAX_SCALE_DENOMINATOR))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.setMaxScaleDenominator(d);
        }
        else if (ctx.isStartElement(event, MIN_SCALE_DENOMINATOR))
        {
            Double d = ctx.getDoubleParser().parseDouble(ctx, event);
            if (d != null)
                this.setMinScaleDenominator(d);
        }
        else if (ctx.isStartElement(event, EXTREME_ELEVATIONS))
        {
            this.parseExtremeElevations(ctx, event);
        }
        else if (ctx.isStartElement(event, SCALE_HINT))
        {
            this.parseScaleHint(ctx, event);
        }
        else if (ctx.isStartElement(event, BOUNDING_BOX))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCBoundingBox)
                    this.addBoundingBox((OGCBoundingBox) o);
            }
        }
        else if (ctx.isStartElement(event, ATTRIBUTION))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerAttribution)
                    this.addAttribution((WMSLayerAttribution) o);
            }
        }
        else if (ctx.isStartElement(event, KEYWORD_LIST))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof StringSetXMLEventParser)
                    this.setKeywords(((StringSetXMLEventParser) o).getStrings());
            }
        }
        else if (ctx.isStartElement(event, IDENTIFIER))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerIdentifier)
                    this.addIdentifer((WMSLayerIdentifier) o);
            }
        }
        else if (ctx.isStartElement(event, DIMENSION))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerDimension)
                    this.addDimension((WMSLayerDimension) o);
            }
        }
        else if (ctx.isStartElement(event, EXTENT))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerExtent)
                    this.addExtent((WMSLayerExtent) o);
            }
        }
        else if (ctx.isStartElement(event, AUTHORITY_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSAuthorityURL)
                    this.addAuthorityURL((WMSAuthorityURL) o);
            }
        }
        else if (ctx.isStartElement(event, DATA_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerInfoURL)
                    this.addDataURL((WMSLayerInfoURL) o);
            }
        }
        else if (ctx.isStartElement(event, FEATURE_LIST_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerInfoURL)
                    this.addFeatureListURL((WMSLayerInfoURL) o);
            }
        }
        else if (ctx.isStartElement(event, METADATA_URL))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WMSLayerInfoURL)
                    this.addMetadataURL((WMSLayerInfoURL) o);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent layerEvent, Object... args)
    {
        Iterator iter = layerEvent.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (attr.getName().getLocalPart().equals("queryable") && attr.getValue() != null)
            {
                Boolean b = this.parseBooleanLayerAttribute(attr.getValue());
                if (b != null)
                    this.setQueryable(b);
            }
            else if (attr.getName().getLocalPart().equals("cascaded") && attr.getValue() != null)
            {
                Boolean b = this.parseBooleanLayerAttribute(attr.getValue());
                if (b != null)
                    this.setCascaded(b);
            }
            else if (attr.getName().getLocalPart().equals("opaque") && attr.getValue() != null)
            {
                Boolean b = this.parseBooleanLayerAttribute(attr.getValue());
                if (b != null)
                    this.setOpaque(b);
            }
            else if (attr.getName().getLocalPart().equals("noSubsets") && attr.getValue() != null)
            {
                Boolean b = this.parseBooleanLayerAttribute(attr.getValue());
                if (b != null)
                    this.setNoSubsets(b);
            }
            else if (attr.getName().getLocalPart().equals("fixedWidth") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    this.setFixedWidth(d.intValue());
            }
            else if (attr.getName().getLocalPart().equals("fixedHeight") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    this.setFixedHeight(d.intValue());
            }
        }
    }

    protected Boolean parseBooleanLayerAttribute(String s)
    {
        if (WWUtil.isEmpty(s))
            return false;

        if (s.equalsIgnoreCase("false"))
            return false;
        else if (s.equalsIgnoreCase("true"))
            return true;

        Boolean d = WWUtil.convertStringToBoolean(s);
        return (d != null && d);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void parseExtremeElevations(XMLEventParserContext ctx, XMLEvent layerEvent)
    {
        Iterator iter = layerEvent.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (attr.getName().getLocalPart().equals("min") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    this.setExtremeElevationMin(d);
            }
            else if (attr.getName().getLocalPart().equals("max") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    this.setExtremeElevationMax(d);
            }
        }
    }

    protected void parseGeographicBoundingBox(XMLEventParserContext ctx, XMLEvent bboxEvent) throws XMLStreamException
    {
        Double minLat = null;
        Double minLon = null;
        Double maxLat = null;
        Double maxLon = null;

        for (XMLEvent event = ctx.nextEvent(); event != null; event = ctx.nextEvent())
        {
            if (ctx.isEndElement(event, bboxEvent))
            {
                if (minLat != null && minLon != null && maxLat != null && maxLon != null)
                    this.setGeographicBoundingBox(Sector.fromDegreesAndClamp(minLat, maxLat, minLon, maxLon));
                return;
            }
            else if (event.isStartElement())
            {
                if (event.asStartElement().getName().getLocalPart().equals("westBoundLongitude"))
                {
                    Double d = ctx.getDoubleParser().parseDouble(ctx, event);
                    if (d != null)
                        minLon = d;
                }
                else if (event.asStartElement().getName().getLocalPart().equals("eastBoundLongitude"))
                {
                    Double d = ctx.getDoubleParser().parseDouble(ctx, event);
                    if (d != null)
                        maxLon = d;
                }
                else if (event.asStartElement().getName().getLocalPart().equals("southBoundLatitude"))
                {
                    Double d = ctx.getDoubleParser().parseDouble(ctx, event);
                    if (d != null)
                        minLat = d;
                }
                else if (event.asStartElement().getName().getLocalPart().equals("northBoundLatitude"))
                {
                    Double d = ctx.getDoubleParser().parseDouble(ctx, event);
                    if (d != null)
                        maxLat = d;
                }
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void parseGeographicBoundingBoxV111(XMLEventParserContext ctx, XMLEvent bboxEvent)
        throws XMLStreamException
    {
        Double minLat = null;
        Double minLon = null;
        Double maxLat = null;
        Double maxLon = null;

        Iterator iter = bboxEvent.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (attr.getName().getLocalPart().equals("minx") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    minLon = d;
            }
            else if (attr.getName().getLocalPart().equals("miny") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    minLat = d;
            }
            else if (attr.getName().getLocalPart().equals("maxx") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    maxLon = d;
            }
            else if (attr.getName().getLocalPart().equals("maxy") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    maxLat = d;
            }
        }

        if (minLat != null && minLon != null && maxLat != null && maxLon != null)
            this.setGeographicBoundingBox(Sector.fromDegrees(minLat, maxLat, minLon, maxLon));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void parseScaleHint(XMLEventParserContext ctx, XMLEvent bboxEvent) throws XMLStreamException
    {
        Iterator iter = bboxEvent.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (attr.getName().getLocalPart().equals("min") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    this.setMinScaleHint(d);
            }
            else if (attr.getName().getLocalPart().equals("max") && attr.getValue() != null)
            {
                Double d = WWUtil.convertStringToDouble(attr.getValue());
                if (d != null)
                    this.setMaxScaleHint(d);
            }
        }
    }

    public List<WMSLayerCapabilities> getNamedLayers()
    {
        List<WMSLayerCapabilities> namedLayers = new ArrayList<WMSLayerCapabilities>();

        if (this.getName() != null)
            namedLayers.add(this);

        for (WMSLayerCapabilities layer : this.getLayers())
        {
            namedLayers.addAll(layer.getNamedLayers());
        }

        return namedLayers;
    }

    public WMSLayerCapabilities getLayerByName(String name)
    {
        if (WWUtil.isEmpty(name))
            return null;

        if (this.getName() != null && this.getName().equals(name))
            return this;

        for (WMSLayerCapabilities lc : this.getLayers())
        {
            if (lc.getName() != null && lc.getName().equals(name))
                return lc;
        }

        return null;
    }

    public WMSLayerStyle getStyleByName(String name)
    {
        if (WWUtil.isEmpty(name))
            return null;

        for (WMSLayerStyle style : this.getStyles())
        {
            if (style.getName().equals(name))
                return style;
        }

        return null;
    }

    public Double getExtremeElevationMin()
    {
        return extremeElevationMin;
    }

    protected void setExtremeElevationMin(Double extremeElevationMin)
    {
        this.extremeElevationMin = extremeElevationMin;
    }

    public Double getExtremeElevationMax()
    {
        return extremeElevationMax;
    }

    protected void setExtremeElevationMax(Double extremeElevationMax)
    {
        this.extremeElevationMax = extremeElevationMax;
    }

    public String getLastUpdate()
    {
        return lastUpdate;
    }

    protected void setLastUpdate(String lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }

    public Double getMinScaleHint()
    {
        return this.minScaleHint;
    }

    protected void setMinScaleHint(Double scaleHint)
    {
        this.minScaleHint = scaleHint;
    }

    public Double getMaxScaleHint()
    {
        return this.maxScaleHint;
    }

    protected void setMaxScaleHint(Double scaleHint)
    {
        this.maxScaleHint = scaleHint;
    }

    public Set<WMSLayerDimension> getDimensions()
    {
        if (this.dimensions != null)
            return this.dimensions;
        else
            return Collections.emptySet();
    }

    protected void addDimension(WMSLayerDimension dimension)
    {
        if (this.dimensions == null)
            this.dimensions = new HashSet<WMSLayerDimension>();

        this.getDimensions().add(dimension);
    }

    protected void addDimensions(Set<WMSLayerDimension> dims)
    {
        if (dims.size() == 0)
            return;

        for (WMSLayerDimension dim : dims)
        {
            this.addDimension(dim);
        }
    }

    protected void setDimensions(Set<WMSLayerDimension> dimensions)
    {
        this.dimensions = dimensions;
    }

    public Set<WMSLayerExtent> getExtents()
    {
        if (this.extents != null)
            return this.extents;
        else
            return Collections.emptySet();
    }

    protected void addExtent(WMSLayerExtent extent)
    {
        if (this.extents == null)
            this.extents = new HashSet<WMSLayerExtent>();

        this.getExtents().add(extent);
    }

    protected void addExtents(Set<WMSLayerExtent> inExtents)
    {
        if (inExtents.size() == 0)
            return;

        for (WMSLayerExtent extent : inExtents)
        {
            this.addExtent(extent);
        }
    }

    public Boolean getCascaded()
    {
        return cascaded;
    }

    protected void setCascaded(Boolean cascaded)
    {
        this.cascaded = cascaded;
    }

    public Integer getFixedHeight()
    {
        return fixedHeight;
    }

    protected void setFixedHeight(Integer height)
    {
        this.fixedHeight = height;
    }

    public Integer getFixedWidth()
    {
        return fixedWidth;
    }

    protected void setFixedWidth(Integer width)
    {
        this.fixedWidth = width;
    }

    public Boolean isNoSubsets()
    {
        return noSubsets;
    }

    protected void setNoSubsets(Boolean noSubsets)
    {
        this.noSubsets = noSubsets;
    }

    public Boolean isOpaque()
    {
        return opaque;
    }

    protected void setOpaque(Boolean opaque)
    {
        this.opaque = opaque;
    }

    public Boolean isQueryable()
    {
        return queryable;
    }

    protected void setQueryable(Boolean queryable)
    {
        this.queryable = queryable;
    }

    public Set<WMSLayerAttribution> getAttributions()
    {
        if (this.attributions != null)
            return attributions;
        else
            return Collections.emptySet();
    }

    protected void setAttributions(Set<WMSLayerAttribution> attributions)
    {
        this.attributions = attributions;
    }

    protected void addAttribution(WMSLayerAttribution attribution)
    {
        if (this.attributions == null)
            this.attributions = new HashSet<WMSLayerAttribution>();

        this.getAttributions().add(attribution);
    }

    protected void addAttributions(Set<WMSLayerAttribution> attribs)
    {
        for (WMSLayerAttribution attrib : attribs)
        {
            this.addAttribution(attrib);
        }
    }

    public Set<WMSAuthorityURL> getAuthorityURLs()
    {
        if (this.authorityURLs != null)
            return this.authorityURLs;
        else
            return Collections.emptySet();
    }

    protected void setAuthorityURLs(Set<WMSAuthorityURL> urls)
    {
        this.authorityURLs = urls;
    }

    protected void addAuthorityURL(WMSAuthorityURL authorityURL)
    {
        if (this.authorityURLs == null)
            this.authorityURLs = new HashSet<WMSAuthorityURL>();

        this.getAuthorityURLs().add(authorityURL);
    }

    protected void addAuthorityURLs(Set<WMSAuthorityURL> urls)
    {
        for (WMSAuthorityURL url : urls)
        {
            this.addAuthorityURL(url);
        }
    }

    public Set<WMSLayerIdentifier> getIdentifiers()
    {
        if (this.identifiers != null)
            return this.identifiers;
        else
            return Collections.emptySet();
    }

    protected void addIdentifer(WMSLayerIdentifier identifier)
    {
        if (this.identifiers == null)
            this.identifiers = new HashSet<WMSLayerIdentifier>();

        this.getIdentifiers().add(identifier);
    }

    protected void addIdentifiers(Set<WMSLayerIdentifier> ids)
    {
        for (WMSLayerIdentifier id : ids)
        {
            this.addIdentifer(id);
        }
    }

    public Set<WMSLayerInfoURL> getMetadataURLs()
    {
        if (this.metadataURLs != null)
            return this.metadataURLs;
        else
            return Collections.emptySet();
    }

    protected void addMetadataURL(WMSLayerInfoURL url)
    {
        if (this.metadataURLs == null)
            this.metadataURLs = new HashSet<WMSLayerInfoURL>();

        this.getMetadataURLs().add(url);
    }

    protected void addMetadataURLs(Set<WMSLayerInfoURL> urls)
    {
        for (WMSLayerInfoURL url : urls)
        {
            this.addMetadataURL(url);
        }
    }

    public Set<WMSLayerInfoURL> getFeatureListURLs()
    {
        if (this.featureListURLs != null)
            return this.featureListURLs;
        else
            return Collections.emptySet();
    }

    protected void addFeatureListURL(WMSLayerInfoURL url)
    {
        if (this.featureListURLs == null)
            this.featureListURLs = new HashSet<WMSLayerInfoURL>();

        this.getFeatureListURLs().add(url);
    }

    protected void addFeatureListURLs(Set<WMSLayerInfoURL> urls)
    {
        for (WMSLayerInfoURL url : urls)
        {
            this.addFeatureListURL(url);
        }
    }

    public Set<WMSLayerInfoURL> getDataURLs()
    {
        if (this.dataURLs != null)
            return this.dataURLs;
        else
            return Collections.emptySet();
    }

    protected void addDataURL(WMSLayerInfoURL url)
    {
        if (this.dataURLs == null)
            this.dataURLs = new HashSet<WMSLayerInfoURL>();

        this.getDataURLs().add(url);
    }

    protected void addDataURLs(Set<WMSLayerInfoURL> urls)
    {
        for (WMSLayerInfoURL url : urls)
        {
            this.addDataURL(url);
        }
    }

    public List<WMSLayerCapabilities> getLayers()
    {
        if (this.layers != null)
            return this.layers;
        else
            return Collections.emptyList();
    }

    protected void addLayer(WMSLayerCapabilities layer)
    {
        if (this.layers == null)
            this.layers = new ArrayList<WMSLayerCapabilities>();

        this.getLayers().add(layer);
    }

    protected void addLayers(Set<WMSLayerCapabilities> inLayers)
    {
        for (WMSLayerCapabilities layer : inLayers)
        {
            this.addLayer(layer);
        }
    }

    public Set<WMSLayerStyle> getStyles()
    {
        if (this.styles != null)
            return this.styles;
        else
            return Collections.emptySet();
    }

    protected void setStyles(Set<WMSLayerStyle> styles)
    {
        this.styles = styles;
    }

    protected void addStyle(WMSLayerStyle style)
    {
        if (this.styles == null)
            this.styles = new HashSet<WMSLayerStyle>();

        this.getStyles().add(style);
    }

    protected void addStyles(Set<WMSLayerStyle> inStyles)
    {
        for (WMSLayerStyle style : inStyles)
        {
            this.addStyle(style);
        }
    }

    public Set<OGCBoundingBox> getBoundingBoxes()
    {
        if (this.boundingBoxes != null)
            return this.boundingBoxes;
        else
            return Collections.emptySet();
    }

    protected void setBoundingBoxes(Set<OGCBoundingBox> boxes)
    {
        this.boundingBoxes = boxes;
    }

    protected void addBoundingBox(OGCBoundingBox box)
    {
        if (this.boundingBoxes == null)
            this.boundingBoxes = new HashSet<OGCBoundingBox>();

        this.getBoundingBoxes().add(box);
    }

    protected void addBoundingBoxes(Set<OGCBoundingBox> boxes)
    {
        for (OGCBoundingBox bbox : boxes)
        {
            this.addBoundingBox(bbox);
        }
    }

    public Sector getGeographicBoundingBox()
    {
        return geographicBoundingBox;
    }

    protected void setGeographicBoundingBox(Sector geographicBoundingBox)
    {
        this.geographicBoundingBox = geographicBoundingBox;
    }

    public Set<String> getKeywords()
    {
        if (this.keywords != null)
            return this.keywords;
        else
            return Collections.emptySet();
    }

    protected void setKeywords(Set<String> keywords)
    {
        this.keywords = keywords;
    }

    public String getLayerAbstract()
    {
        return layerAbstract;
    }

    protected void setLayerAbstract(String layerAbstract)
    {
        this.layerAbstract = layerAbstract;
    }

    public Double getMaxScaleDenominator()
    {
        return maxScaleDenominator;
    }

    protected void setMaxScaleDenominator(Double maxScaleDenominator)
    {
        this.maxScaleDenominator = maxScaleDenominator;
    }

    public Double getMinScaleDenominator()
    {
        return minScaleDenominator;
    }

    protected void setMinScaleDenominator(Double minScaleDenominator)
    {
        this.minScaleDenominator = minScaleDenominator;
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }

    public String getTitle()
    {
        return title;
    }

    protected void setTitle(String title)
    {
        this.title = title;
    }

    public Set<String> getSRS()
    {
        if (this.srs != null)
            return this.srs;
        else
            return Collections.emptySet();
    }

    protected void setSRS(Set<String> srs)
    {
        this.srs = srs;
    }

    protected void addSRS(String srs)
    {
        if (this.srs == null)
            this.srs = new HashSet<String>();

        this.srs.add(srs);
    }

    protected void addSRS(Set<String> srss)
    {
        for (String c : srss)
        {
            this.addSRS(c);
        }
    }

    public Set<String> getCRS()
    {
        if (this.crs != null)
            return this.crs;
        else
            return Collections.emptySet();
    }

    protected void setCRS(Set<String> crs)
    {
        this.crs = crs;
    }

    protected void addCRS(String crs)
    {
        if (this.crs == null)
            this.crs = new HashSet<String>();

        this.crs.add(crs);
    }

    protected void addCRS(Set<String> crss)
    {
        for (String c : crss)
        {
            this.addCRS(c);
        }
    }

    public boolean hasCoordinateSystem(String coordSys)
    {
        if (coordSys == null)
            return false;

        Collection<String> coordSystems = this.crs != null ? this.crs : this.srs;
        if (coordSystems == null)
            return false;

        for (String s : coordSystems)
        {
            if (coordSys.equals(s))
                return true;
        }

        return false;
    }

    @Override
    public String toString() // TODO: Complete this method
    {
        StringBuilder sb = new StringBuilder("LAYER ");

        if (!WWUtil.isEmpty(this.getName()))
            sb.append(this.getName()).append(": ");
        sb.append("queryable = ").append(this.isQueryable());

        return sb.toString();
    }
}
