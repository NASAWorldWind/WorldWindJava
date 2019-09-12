/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.ogc.kml.gx.GXParserContext;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.util.xml.atom.AtomParserContext;
import gov.nasa.worldwind.util.xml.xal.XALParserContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import java.util.Map;

/**
 * The parser context for KML and KMZ docuements. Specifies the classes used to parse each type of KML element.
 *
 * @author tag
 * @version $Id: KMLParserContext.java 1528 2013-07-31 01:00:32Z pabercrombie $
 */
public class KMLParserContext extends BasicXMLEventParserContext
{
    protected KMLCoordinatesParser coordinatesParser;

    /** The key used to identify the coordinates parser in the parser context's parser map. */
    protected static QName COORDINATES = new QName("Coordinates");

    /** The names of elements that contain merely string data and can be parsed by a generic string parser. */
    protected static final String[] StringFields = new String[]
        {
            // Only element names, not attribute names, are needed here.
            "address",
            "altitudeMode",
            "begin",
            "bgColor",
            "color",
            "colorMode",
            "cookie",
            "description",
            "displayMode",
            "displayName",
            "end",
            "expires",
            "gridOrigin",
            "href",
            "httpQuery",
            "key",
            "labelColor",
            "linkDescription",
            "linkName",
            "listItemType",
            "message",
            "name",
            "phoneNumber",
            "refreshMode",
            "schemaUrl",
            "shape",
            "snippet",
            "sourceHref",
            "state",
            "targetHref",
            "text",
            "textColor",
            "type",
            "value",
            "viewFormat",
            "viewRefreshMode",
            "when",
            "DocumentSource"
        };

    /** The names of elements that contain merely double data and can be parsed by a generic double parser. */
    protected static final String[] DoubleFields = new String[]
        {
            "altitude",
            "bottomFov",
            "east",
            "heading",
            "latitude",
            "leftFov",
            "longitude",
            "maxAltitude",
            "minAltitude",
            "minFadeExtent",
            "maxFadeExtent",
            "minLodPixels",
            "maxLodPixels",
            "minRefreshPeriod",
            "maxSessionLength",
            "near",
            "north",
            "range",
            "refreshInterval",
            "rightFov",
            "roll",
            "rotation",
            "scale",
            "south",
            "tilt",
            "topFov",
            "viewRefreshTime",
            "viewBoundScale",
            "west",
            "width",
            "x",
            "y",
            "z",
        };

    /** The names of elements that contain merely integer data and can be parsed by a generic integer parser. */
    protected static final String[] IntegerFields = new String[]
        {
            "drawOrder",
            "maxHeight",
            "maxLines",
            "maxSnippetLines",
            "maxWidth",
            "tileSize",
        };

    /**
     * The names of elements that contain merely boolean integer (0 or 1) data and can be parsed by a generic boolean
     * integer parser.
     */
    protected static final String[] BooleanFields = new String[]
        {
            "extrude",
            "fill",
            "flyToView",
            "open",
            "outline",
            "refreshVisibility",
            "tessellate",
            "visibility",
        };

    /**
     * Creates a parser context instance.
     *
     * @param defaultNamespace the default namespace. If null, {@link gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}
     *                         is used.
     */
    public KMLParserContext(String defaultNamespace)
    {
        this(null, defaultNamespace);
    }

    /**
     * Creates a parser context instance.
     *
     * @param eventReader      the event reader from which to read events.
     * @param defaultNamespace the default namespace. If null, {@link gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}
     *                         is used.
     */
    public KMLParserContext(XMLEventReader eventReader, String defaultNamespace)
    {
        super(eventReader, defaultNamespace != null ? defaultNamespace : KMLConstants.KML_NAMESPACE);
    }

    public KMLParserContext(KMLParserContext ctx)
    {
        super(ctx);
    }

    /**
     * Loads the parser map with the parser to use for each element type. The parser may be changed by calling {@link
     * #registerParser(javax.xml.namespace.QName, gov.nasa.worldwind.util.xml.XMLEventParser)}.
     */
    protected void initializeParsers()
    {
        super.initializeParsers();

        this.parsers.put(COORDINATES, new KMLCoordinatesParser());

        this.initializeVersion2dot2Parsers();
        this.initializeCompanionParsers();

        // Set up to handle old versions.
        this.initializeVersion2dot1Parsers();
        this.initializeVersion2dot0Parsers();
    }

    protected void initializeVersion2dot2Parsers()
    {
        this.initializeParsers(KMLConstants.KML_2dot2_NAMESPACE);
        this.initializeParsers(KMLConstants.KML_GOOGLE_2dot2_NAMESPACE);
    }

    protected void initializeParsers(String ns)
    {
        this.parsers.put(new QName(ns, "Alias"), new KMLAlias(ns));
        this.parsers.put(new QName(ns, "BalloonStyle"), new KMLBalloonStyle(ns));
        this.parsers.put(new QName(ns, "Camera"), new KMLCamera(ns));
        this.parsers.put(new QName(ns, "Change"), new KMLChange(ns));
        this.parsers.put(new QName(ns, "coordinates"), this.getCoordinatesParser());
        this.parsers.put(new QName(ns, "Create"), new KMLCreate(ns));
        this.parsers.put(new QName(ns, "Data"), new KMLData(ns));
        this.parsers.put(new QName(ns, "Delete"), new KMLDelete(ns));
        this.parsers.put(new QName(ns, "Document"), new KMLDocument(ns));
        this.parsers.put(new QName(ns, "ExtendedData"), new KMLExtendedData(ns));
        this.parsers.put(new QName(ns, "Folder"), new KMLFolder(ns));
        this.parsers.put(new QName(ns, "GroundOverlay"), new KMLGroundOverlay(ns));
        this.parsers.put(new QName(ns, "Icon"), new KMLIcon(ns));
        this.parsers.put(new QName(ns, "IconStyle"), new KMLIconStyle(ns));
        this.parsers.put(new QName(ns, "ImagePyramid"), new KMLImagePyramid(ns));
        this.parsers.put(new QName(ns, "innerBoundaryIs"), new KMLBoundary(ns));
        this.parsers.put(new QName(ns, "ItemIcon"), new KMLItemIcon(ns));
        this.parsers.put(new QName(ns, "hotSpot"), new KMLVec2(ns));
        this.parsers.put(new QName(ns, "LabelStyle"), new KMLLabelStyle(ns));
        this.parsers.put(new QName(ns, "LatLonBox"), new KMLLatLonBox(ns));
        this.parsers.put(new QName(ns, "LatLonAltBox"), new KMLLatLonAltBox(ns));
        this.parsers.put(new QName(ns, "LinearRing"), new KMLLinearRing(ns));
        this.parsers.put(new QName(ns, "LineString"), new KMLLineString(ns));
        this.parsers.put(new QName(ns, "LineStyle"), new KMLLineStyle(ns));
        this.parsers.put(new QName(ns, "Link"), new KMLLink(ns));
        this.parsers.put(new QName(ns, "linkSnippet"), new KMLSnippet(ns));
        this.parsers.put(new QName(ns, "ListStyle"), new KMLListStyle(ns));
        this.parsers.put(new QName(ns, "Location"), new KMLLocation(ns));
        this.parsers.put(new QName(ns, "Lod"), new KMLLod(ns));
        this.parsers.put(new QName(ns, "LookAt"), new KMLLookAt(ns));
        this.parsers.put(new QName(ns, "Model"), new KMLModel(ns));
        this.parsers.put(new QName(ns, "MultiGeometry"), new KMLMultiGeometry(ns));
        this.parsers.put(new QName(ns, "NetworkLink"), new KMLNetworkLink(ns));
        this.parsers.put(new QName(ns, "NetworkLinkControl"), new KMLNetworkLinkControl(ns));
        this.parsers.put(new QName(ns, "Orientation"), new KMLOrientation(ns));
        this.parsers.put(new QName(ns, "outerBoundaryIs"), new KMLBoundary(ns));
        this.parsers.put(new QName(ns, "overlayXY"), new KMLVec2(ns));
        this.parsers.put(new QName(ns, "Pair"), new KMLPair(ns));
        this.parsers.put(new QName(ns, "PhotoOverlay"), new KMLPhotoOverlay(ns));
        this.parsers.put(new QName(ns, "Placemark"), new KMLPlacemark(ns));
        this.parsers.put(new QName(ns, "Point"), new KMLPoint(ns));
        this.parsers.put(new QName(ns, "Polygon"), new KMLPolygon(ns));
        this.parsers.put(new QName(ns, "PolyStyle"), new KMLPolyStyle(ns));
        this.parsers.put(new QName(ns, "Region"), new KMLRegion(ns));
        this.parsers.put(new QName(ns, "ResourceMap"), new KMLResourceMap(ns));
        this.parsers.put(new QName(ns, "rotationXY"), new KMLVec2(ns));
        this.parsers.put(new QName(ns, "Scale"), new KMLScale(ns));
        this.parsers.put(new QName(ns, "Schema"), new KMLSchema(ns));
        this.parsers.put(new QName(ns, "SchemaData"), new KMLSchemaData(ns));
        this.parsers.put(new QName(ns, "ScreenOverlay"), new KMLScreenOverlay(ns));
        this.parsers.put(new QName(ns, "screenXY"), new KMLVec2(ns));
        this.parsers.put(new QName(ns, "SimpleData"), new KMLSimpleData(ns));
        this.parsers.put(new QName(ns, "SimpleField"), new KMLSimpleField(ns));
        this.parsers.put(new QName(ns, "size"), new KMLVec2(ns));
        this.parsers.put(new QName(ns, "Snippet"), new KMLSnippet(ns));
        this.parsers.put(new QName(ns, "Style"), new KMLStyle(ns));
        this.parsers.put(new QName(ns, "StyleMap"), new KMLStyleMap(ns));
        this.parsers.put(new QName(ns, "styleUrl"), new KMLStyleUrl(ns));
        this.parsers.put(new QName(ns, "TimeSpan"), new KMLTimeSpan(ns));
        this.parsers.put(new QName(ns, "TimeStamp"), new KMLTimeStamp(ns));
        this.parsers.put(new QName(ns, "Update"), new KMLUpdate(ns));
        this.parsers.put(new QName(ns, "Url"), new KMLLink(ns)); // Deprecated in KML 2.1. Still used by NetworkLink.
        this.parsers.put(new QName(ns, "ViewVolume"), new KMLViewVolume(ns));

        this.addStringParsers(ns, StringFields);
        this.addDoubleParsers(ns, DoubleFields);
        this.addIntegerParsers(ns, IntegerFields);
        this.addBooleanParsers(ns, BooleanFields);
    }

    protected void initializeVersion2dot1Parsers()
    {
        // Just add all the default parsers. // TODO: Check for differences between 2.0 and 2.1
        this.initializeParsers(KMLConstants.KML_2dot1_NAMESPACE);
    }

    protected void initializeVersion2dot0Parsers()
    {
        String ns = KMLConstants.KML_2dot0_NAMESPACE;

        // Just add all the default parsers. // TODO: Check for differences between 2.0 and 2.1
        for (Map.Entry<QName, XMLEventParser> entry : this.parsers.entrySet())
        {
            this.parsers.put(new QName(ns, entry.getKey().getLocalPart()), entry.getValue());
        }
    }

    protected void initializeCompanionParsers()
    {
        this.parsers.putAll(GXParserContext.getDefaultParsers());
        this.parsers.putAll(AtomParserContext.getDefaultParsers());
        this.parsers.putAll(XALParserContext.getDefaultParsers());
    }

    /**
     * Get the default coordinates parser.
     *
     * @return the default coordinates parser.
     */
    public KMLCoordinatesParser getCoordinatesParser()
    {
        if (this.coordinatesParser == null)
            this.coordinatesParser = (KMLCoordinatesParser) this.getParser(COORDINATES);

        return this.coordinatesParser;
    }
}
