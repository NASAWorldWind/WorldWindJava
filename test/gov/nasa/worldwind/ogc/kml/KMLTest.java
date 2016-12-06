/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.gx.GXConstants;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.util.xml.atom.AtomConstants;
import gov.nasa.worldwind.util.xml.xal.XALConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class KMLTest
{
    @Test
    public void testRootElement()
    {
        StringBuilder sb = this.newDocument();
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);

        assertNotNull("KML root is null", root);
        assertNull("KML root hint is not null", root.getHint());
    }

    @Test
    public void testRootHint()
    {
        StringBuilder sb = this.newDocument();
        sb = new StringBuilder(sb.toString().replace("<kml", "<kml hint=\"yes\""));
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);

        assertNotNull("KML root is null", root);
        assertNotNull("KML root hint is null", root.getHint());
    }

    @Test
    public void testAbstractObjectAttributes()
    {
        String ID = "ABC123";
        String targetID = "DEF456";

        StringBuilder sb = this.newDocument();
        sb.append("<Document id=\"").append(ID).append("\" targetId=\"").append(targetID).append("\"></Document>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLDocument);
        assertEquals("Object ID not as expected", feature.getId(), ID);
        assertEquals("Target ID not as expected", feature.getTargetId(), targetID);
    }

    @Test
    public void testUnassignedAbstractObjectAttributes()
    {
        StringBuilder sb = this.newDocument();
        sb.append("<Document>");
        sb.append("</Document>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLDocument);

        assertNull("ID not null", feature.getId());
        assertNull("Target ID not null", feature.getTargetId());
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testAbstractFeatureAttributes()
    {
        String name = "XXXYYYZZZ";
        boolean visibility = true;
        boolean open = false;
        String address = "100 LALA LANE";
        String phoneNumber = "1-800-888-999";
        String snippet = "5";
        String description = "This is a test";
        String styleUrl = "http://worldwind.arc.nasa.gov";

        // TODO view
        // TODO region
        // TODO xal:address details
        // TODO style selector
        // TODO time
        // TODO extended data

        StringBuilder sb = this.newDocument();
        sb.append("<Document>");
        sb.append("<name>").append(name).append("</name>");
        sb.append("<visibility>").append(visibility ? "1" : "0").append("</visibility>");
        sb.append("<open>").append(open ? "1" : "0").append("</open>");
        sb.append("<address>").append(address).append("</address>");
        sb.append("<phoneNumber>").append(phoneNumber).append("</phoneNumber>");
        sb.append("<snippet>").append(snippet).append("</snippet>");
        sb.append("<description>").append(description).append("</description>");
        sb.append("<styleUrl>").append(styleUrl).append("</styleUrl>");

        String linkHref = "http://worldwind.arc.nasa.gov";
        String linkRel = "thisIsLinkRel";
        String linkType = "thisIsLinkType";
        String linkHreflang = "thisIsLinkHrefLang";
        String linkTitle = "this is Link Title";
        int linkLength = 5;
        String linkBase = "thisIsLinkBase";
        String linkLang = "thisIsLinkLang";

        sb.append("<atom:link");
        sb.append(" href=\"").append(linkHref).append("\"");
        sb.append(" rel=\"").append(linkRel).append("\"");
        sb.append(" type=\"").append(linkType).append("\"");
        sb.append(" hreflang=\"").append(linkHreflang).append("\"");
        sb.append(" title=\"").append(linkTitle).append("\"");
        sb.append(" length=\"").append(linkLength).append("\"");
        sb.append(" base=\"").append(linkBase).append("\"");
        sb.append(" lang=\"").append(linkLang).append("\"");
        sb.append("></atom:link>");

        String authorName = "Author C. Bookwriter";
        String authorEmail = "author@book.com";
        String authorUri = "http://worldwind.arc.nasa.gov";

        sb.append("<atom:author>");
        sb.append("<atom:name>").append(authorName).append("</atom:name>");
        sb.append("<atom:email>").append(authorEmail).append("</atom:email>");
        sb.append("<atom:uri>").append(authorUri).append("</atom:uri>");
        sb.append("</atom:author>");

        sb.append("</Document>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLDocument);

        assertEquals("Name not as expected", feature.getName(), name);
        assertEquals("Visibility not as expected", feature.getVisibility(), visibility);
        assertEquals("Open not as expected", feature.getOpen(), open);
        assertEquals("Address not as expected", feature.getAddress(), address);
        assertEquals("Phone number not as expected", feature.getPhoneNumber(), phoneNumber);
        assertEquals("Snippet not as expected", feature.getSnippet(), snippet);
        assertEquals("Description not as expected", feature.getDescription(), description);
        assertEquals("Style URL not as expected", feature.getStyleUrl().getCharacters(), styleUrl);

        assertEquals("Link href not as expected", feature.getLink().getHref(), linkHref);
        assertEquals("Link rel not as expected", feature.getLink().getRel(), linkRel);
        assertEquals("Link type not as expected", feature.getLink().getType(), linkType);
        assertEquals("Link hreflang not as expected", feature.getLink().getHreflang(), linkHreflang);
        assertEquals("Link title not as expected", feature.getLink().getTitle(), linkTitle);
        assertEquals("Link length not as expected", feature.getLink().getLength().intValue(), linkLength);
        assertEquals("Link base not as expected", feature.getLink().getBase(), linkBase);
        assertEquals("Link lang not as expected", feature.getLink().getLang(), linkLang);

        assertEquals("Author name not as expected", feature.getAuthor().getName(), authorName);
        assertEquals("Author email not as expected", feature.getAuthor().getEmail(), authorEmail);
        assertEquals("Author URI not as expected", feature.getAuthor().getUri(), authorUri);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testUnassignedAbstractFeatureAttributes()
    {
        StringBuilder sb = this.newDocument();
        sb.append("<Document>");
        sb.append("</Document>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLDocument);

        assertNull("Name not null", feature.getName());
        assertNull("Visibility not null", feature.getVisibility());
        assertNull("Open not null", feature.getOpen());
        assertNull("Address not null", feature.getAddress());
        assertNull("Phone number not null", feature.getPhoneNumber());
        assertNull("Snippet not null", feature.getSnippet());
        assertNull("Description not null", feature.getDescription());
        assertNull("Style URL not null", feature.getStyleUrl());
        assertNull("View not null", feature.getView());
        assertNull("Region not null", feature.getRegion());
        assertNull("Author not null", feature.getAuthor());
        assertNull("Link not null", feature.getLink());
        assertNull("Address details not null", feature.getAddressDetails());
        assertEquals("Style selectors not empty", 0, feature.getStyleSelectors().size());
        assertNull("Time not null", feature.getTimePrimitive());
        assertNull("Extended data not null", feature.getExtendedData());
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testPrefixUsage()
    {
        String altitudeMode = "absolute";
        boolean extrude = true;
        Position coords = Position.fromDegrees(23.56, -18.3, 9);

        StringBuilder sb = this.newPrefixedDocument();
        sb.append("<kml:Placemark>");
        sb.append("<kml:Point>");
        sb.append("<kml:extrude>").append(extrude ? "1" : "0").append("</kml:extrude>");
        sb.append("<kml:altitudeMode>").append(altitudeMode).append("</kml:altitudeMode>");
        sb.append("<kml:coordinates>");
        sb.append(coords.getLongitude().degrees).append(",");
        sb.append(coords.getLatitude().degrees).append(",");
        sb.append(coords.getElevation());
        sb.append("</kml:coordinates>");
        sb.append("</kml:Point>");
        sb.append("</kml:Placemark>");
        this.endPrefixedDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Placemark geometry is not as expected", geometry instanceof KMLPoint);

        KMLPoint point = (KMLPoint) geometry;
        assertEquals("Altitude mode not as expected", point.getAltitudeMode(), altitudeMode);
        assertEquals("Extrude not as expected", point.isExtrude(), extrude);
        assertEquals("Coordinates not as expected", point.getCoordinates(), coords);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testNoDefaultNamespace()
    {
        String altitudeMode = "absolute";
        boolean extrude = true;
        Position coords = Position.fromDegrees(23.56, -18.3, 9);

        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<kml>");
        sb.append("<Placemark>");
        sb.append("<Point>");
        sb.append("<extrude>").append(extrude ? "1" : "0").append("</extrude>");
        sb.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");
        sb.append("<coordinates>");
        sb.append(coords.getLongitude().degrees).append(",");
        sb.append(coords.getLatitude().degrees).append(",");
        sb.append(coords.getElevation());
        sb.append("</coordinates>");
        sb.append("</Point>");
        sb.append("</Placemark>");
        sb.append("</kml>");

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Placemark geometry is not as expected", geometry instanceof KMLPoint);

        KMLPoint point = (KMLPoint) geometry;
        assertEquals("Altitude mode not as expected", point.getAltitudeMode(), altitudeMode);
        assertEquals("Extrude not as expected", point.isExtrude(), extrude);
        assertEquals("Coordinates not as expected", point.getCoordinates(), coords);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testPoint()
    {
        String altitudeMode = "absolute";
        boolean extrude = true;
        Position coords = Position.fromDegrees(23.56, -18.3, 9);

        StringBuilder sb = this.newDocument();
        sb.append("<Placemark>");
        sb.append("<Point>");
        sb.append("<extrude>").append(extrude ? "1" : "0").append("</extrude>");
        sb.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");
        sb.append("<coordinates>");
        sb.append(coords.getLongitude().degrees).append(",");
        sb.append(coords.getLatitude().degrees).append(",");
        sb.append(coords.getElevation());
        sb.append("</coordinates>");
        sb.append("</Point>");
        sb.append("</Placemark>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Placemark geometry is not as expected", geometry instanceof KMLPoint);

        KMLPoint point = (KMLPoint) geometry;
        assertEquals("Altitude mode not as expected", point.getAltitudeMode(), altitudeMode);
        assertEquals("Extrude not as expected", point.isExtrude(), extrude);
        assertEquals("Coordinates not as expected", point.getCoordinates(), coords);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testLinearRing()
    {
        String altitudeMode = "clampToGround";
        boolean extrude = true;
        boolean tessellate = false;

        List<Position> coords = new ArrayList<Position>();
        coords.add(Position.fromDegrees(23.56, -18.3, 9));
        coords.add(Position.fromDegrees(24.56, -19.3, 8));
        coords.add(Position.fromDegrees(25.56, -17.3, 99));

        StringBuilder sb = this.newDocument();
        sb.append("<Placemark>");
        sb.append("<LinearRing>");
        sb.append("<extrude>").append(extrude ? "1" : "0").append("</extrude>");
        sb.append("<tessellate>").append(tessellate ? "1" : "0").append("</tessellate>");
        sb.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");
        sb.append("<coordinates>");
        for (Position p : coords)
        {
            sb.append(p.getLongitude().degrees).append(",");
            sb.append(p.getLatitude().degrees).append(",");
            sb.append(p.getElevation()).append(" ");
        }
        sb.append("</coordinates>");
        sb.append("</LinearRing>");
        sb.append("</Placemark>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Placemark geometry is not as expected", geometry instanceof KMLLinearRing);

        KMLLinearRing ring = (KMLLinearRing) geometry;
        assertEquals("Altitude mode not as expected", ring.getAltitudeMode(), altitudeMode);
        assertEquals("Extrude not as expected", ring.isExtrude(), extrude);
        assertEquals("Tessellate not as expected", ring.getTessellate(), tessellate);
        assertEquals("Coordinates not as expected", ring.getCoordinates().list, coords);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testLineString()
    {
        String altitudeMode = "clampToGround";
        boolean extrude = false;
        boolean tessellate = true;

        List<Position> coords = new ArrayList<Position>();
        coords.add(Position.fromDegrees(23.56, -18.3, 9));
        coords.add(Position.fromDegrees(24.56, -19.3, 8));
        coords.add(Position.fromDegrees(25.56, -17.3, 99));

        StringBuilder sb = this.newDocument();
        sb.append("<Placemark>");
        sb.append("<LineString>");
        sb.append("<extrude>").append(extrude ? "1" : "0").append("</extrude>");
        sb.append("<tessellate>").append(tessellate ? "1" : "0").append("</tessellate>");
        sb.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");
        sb.append("<coordinates>");
        for (Position p : coords)
        {
            sb.append(p.getLongitude().degrees).append(",");
            sb.append(p.getLatitude().degrees).append(",");
            sb.append(p.getElevation()).append(" ");
        }
        sb.append("</coordinates>");
        sb.append("</LineString>");
        sb.append("</Placemark>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Placemark geometry is not as expected", geometry instanceof KMLLineString);

        KMLLineString ring = (KMLLineString) geometry;
        assertEquals("Altitude mode not as expected", ring.getAltitudeMode(), altitudeMode);
        assertEquals("Extrude not as expected", ring.isExtrude(), extrude);
        assertEquals("Tessellate not as expected", ring.getTessellate(), tessellate);
        assertEquals("Coordinates not as expected", ring.getCoordinates().list, coords);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testPolygon()
    {
        String altitudeMode = "clampToGround";
        boolean extrude = false;
        boolean tessellate = true;

        String outerAltitudeMode = "clampToSeaFloor";
        boolean outerExtrude = true;
        boolean outerTessellate = false;

        String innerAltitudeMode = "absolute";
        boolean innerExtrude = true;
        boolean innerTessellate = true;

        List<Position> outerCoords = new ArrayList<Position>();
        outerCoords.add(Position.fromDegrees(23.56, -18.3, 9));
        outerCoords.add(Position.fromDegrees(24.56, -19.3, 8));
        outerCoords.add(Position.fromDegrees(25.56, -17.3, 99));

        List<Position> innerCoords = new ArrayList<Position>();
        innerCoords.add(Position.fromDegrees(22.56, -18.3, 1));
        innerCoords.add(Position.fromDegrees(21.56, -19.3, 2));
        innerCoords.add(Position.fromDegrees(20.56, -17.3, 3));

        StringBuilder sb = this.newDocument();
        sb.append("<Placemark>");
        sb.append("<Polygon>");
        sb.append("<extrude>").append(extrude ? "1" : "0").append("</extrude>");
        sb.append("<tessellate>").append(tessellate ? "1" : "0").append("</tessellate>");
        sb.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");

        sb.append("<outerBoundaryIs>");
        sb.append("<LinearRing>");
        sb.append("<extrude>").append(outerExtrude ? "1" : "0").append("</extrude>");
        sb.append("<tessellate>").append(outerTessellate ? "1" : "0").append("</tessellate>");
        sb.append("<altitudeMode>").append(outerAltitudeMode).append("</altitudeMode>");
        sb.append("<coordinates>");
        for (Position p : outerCoords)
        {
            sb.append(p.getLongitude().degrees).append(",");
            sb.append(p.getLatitude().degrees).append(",");
            sb.append(p.getElevation()).append(" ");
        }
        sb.append("</coordinates>");
        sb.append("</LinearRing>");
        sb.append("</outerBoundaryIs>");

        sb.append("<innerBoundaryIs>");
        sb.append("<LinearRing>");
        sb.append("<extrude>").append(innerExtrude ? "1" : "0").append("</extrude>");
        sb.append("<tessellate>").append(innerTessellate ? "1" : "0").append("</tessellate>");
        sb.append("<altitudeMode>").append(innerAltitudeMode).append("</altitudeMode>");
        sb.append("<coordinates>");
        for (Position p : innerCoords)
        {
            sb.append(p.getLongitude().degrees).append(",");
            sb.append(p.getLatitude().degrees).append(",");
            sb.append(p.getElevation()).append(" ");
        }
        sb.append("</coordinates>");
        sb.append("</LinearRing>");
        sb.append("</innerBoundaryIs>");

        sb.append("</Polygon>");
        sb.append("</Placemark>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Placemark geometry is not as expected", geometry instanceof KMLPolygon);

        KMLPolygon pgon = (KMLPolygon) geometry;
        assertEquals("Altitude mode not as expected", pgon.getAltitudeMode(), altitudeMode);
        assertEquals("Extrude not as expected", pgon.isExtrude(), extrude);
        assertEquals("Tessellate not as expected", pgon.getTessellate(), tessellate);

        assertEquals("Outer coordinates not as expected", pgon.getOuterBoundary().getCoordinates().list,
            outerCoords);
        assertEquals("Outer altitude mode not as expected", pgon.getOuterBoundary().getAltitudeMode(),
            outerAltitudeMode);
        assertEquals("Outer extrude not as expected", pgon.getOuterBoundary().isExtrude(),
            outerExtrude);
        assertEquals("Outer tessellate not as expected", pgon.getOuterBoundary().getTessellate(), outerTessellate);

        Iterable<? extends KMLLinearRing> innerBoundaries = pgon.getInnerBoundaries();
        assertNotNull(innerBoundaries);
        assertTrue(innerBoundaries.iterator().hasNext());
        KMLLinearRing innerBoundary = innerBoundaries.iterator().next();
        assertNotNull(innerBoundary);
        assertEquals("Inner coordinates not as expected", innerBoundary.getCoordinates().list,
            innerCoords);
        assertEquals("Inner altitude mode not as expected", innerBoundary.getAltitudeMode(),
            innerAltitudeMode);
        assertEquals("Inner extrude not as expected", innerBoundary.isExtrude(),
            innerExtrude);
        assertEquals("Inner tessellate not as expected", innerBoundary.getTessellate(), innerTessellate);
    }

    @Test
    public void testSimpleDataType()
    {
        String item = "Test a String";
        String name = "SimpleData Name";

        StringBuilder sb = this.newDocument();
        sb.append("<Placemark>");
        sb.append("<SimpleData name=\"").append(name).append("\">").append(item).append("</SimpleData>");
        sb.append("</Placemark>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLSimpleData dataItem = ((KMLPlacemark) feature).getSimpleData();
        assertNotNull("No SimpleData", dataItem);
        assertEquals("SimpleData name not as expected", dataItem.getName(), name);
        assertEquals("SimpleData string not as expected", dataItem.getCharacters(), item);
    }

    @Test
    public void testUnrecognizedElement()
    {
        String item = "Test a String";
        String name = "SimpleData Name";

        StringBuilder sb = this.newDocument();
        sb.append("<Unrecognized>");
        sb.append("<Placemark>");
        sb.append("<SimpleData name=\"").append(name).append("\">").append(item).append("</SimpleData>");
        sb.append("</Placemark>");
        sb.append("</Unrecognized>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb, true);
        assertNotNull("KML root is null", root);

        for (Map.Entry<String, Object> field : root.getFields().getEntries())
        {
            if (field.getKey().equals("Unrecognized") && field.getValue() instanceof UnrecognizedXMLEventParser)
            {
                UnrecognizedXMLEventParser uField = (UnrecognizedXMLEventParser) field.getValue();
                Object o = uField.getField("Placemark");
                assertNotNull("No SimpleData", o);
                assertTrue("Unrecognized object not as expected", o instanceof KMLPlacemark);

                KMLSimpleData dataItem = ((KMLPlacemark) o).getSimpleData();
                assertNotNull("No SimpleData", dataItem);
                assertEquals("SimpleData name not as expected", dataItem.getName(), name);
                assertEquals("SimpleData string not as expected", dataItem.getCharacters(), item);

                return;
            }
        }
        assertTrue("Unrecognized element not found", true);
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void testCoordinatesParser()
    {
        // Test parsing coordinates separated by newline and tab characters instead of just spaces
        List<String> separators = Arrays.asList("\n", "\n\r", "\t");

        List<Position> coords = new ArrayList<Position>();
        coords.add(Position.fromDegrees(23.56, -18.3, 9));
        coords.add(Position.fromDegrees(24.56, -19.3, 8));
        coords.add(Position.fromDegrees(25.56, -17.3, 99));

        StringBuilder sb = this.newDocument();
        sb.append("<Placemark>");
        sb.append("<LinearRing>");
        sb.append("<coordinates>");

        Iterator<String> separator = separators.iterator();
        for (Position p : coords)
        {
            sb.append(p.getLongitude().degrees).append(",");
            sb.append(p.getLatitude().degrees).append(",");
            sb.append(p.getElevation()).append(
                separator.next());   // Separate coordinate tuple with newline instead of space
        }
        sb.append("</coordinates>");
        sb.append("</LinearRing>");
        sb.append("</Placemark>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Placemark geometry is not as expected", geometry instanceof KMLLinearRing);

        KMLLinearRing ring = (KMLLinearRing) geometry;
        assertEquals("Coordinates not as expected", ring.getCoordinates().list, coords);
    }

    /** Test coordinate tokenizer with a mix of well formed and not so well formed input. */
    @Test
    public void testCoordinatesTokenizer()
    {
        List<Position> coords = new ArrayList<Position>();
        coords.add(Position.fromDegrees(23.56, -18.3, 9));
        coords.add(Position.fromDegrees(56.0, 34.9, 2));
        coords.add(Position.fromDegrees(19, 56.9));
        coords.add(Position.fromDegrees(23.9, 90, 44));
        coords.add(Position.fromDegrees(18, 12.3, 8));
        coords.add(Position.fromDegrees(57, 3.3, -110.9));
        coords.add(Position.fromDegrees(80.1, 50, -23.1));

        // Test with well formed coordinate tuples, and also tuples with spaces to ensure that the tokenizer
        // is able to handle input that is not well formed.
        String coordString = "-18.3,23.56,9     34.9, 56.0, 2     \t56.9, 19     90.0,23.9,44   "
            + " 12.3,18,8,3.3,57,-110.9,50,80.1,-23.1";

        KMLCoordinateTokenizer tokenizer = new KMLCoordinateTokenizer(coordString);

        List<Position> positions = new ArrayList<Position>();
        while (tokenizer.hasMoreTokens())
        {
            positions.add(tokenizer.nextPosition());
        }

        assertEquals("Coordinates not as expected", coords, positions);
    }

    @Test
    public void testNestedUnrecognizedElement()
    {
        String item = "Test a String";
        String name = "SimpleData Name";

        StringBuilder sb = this.newDocument();
        sb.append("<Document>");
        sb.append("<Unrecognized>");
        sb.append("<Placemark>");
        sb.append("<SimpleData name=\"").append(name).append("\">").append(item).append("</SimpleData>");
        sb.append("</Placemark>");
        sb.append("</Unrecognized>");
        sb.append("</Document>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb, true);
        assertNotNull("KML root is null", root);

        KMLAbstractFeature doc = root.getFeature();
        assertNotNull("Document is null", doc);
        assertTrue("Unrecognized object not as expected", doc instanceof KMLDocument);

        for (Map.Entry<String, Object> field : doc.getFields().getEntries())
        {
            if (field.getKey().equals("Unrecognized") && field.getValue() instanceof UnrecognizedXMLEventParser)
            {
                UnrecognizedXMLEventParser uField = (UnrecognizedXMLEventParser) field.getValue();
                Object o = uField.getField("Placemark");
                assertNotNull("No SimpleData", o);
                assertTrue("Unrecognized object not as expected", o instanceof KMLPlacemark);

                KMLSimpleData dataItem = ((KMLPlacemark) o).getSimpleData();
                assertNotNull("No SimpleData", dataItem);
                assertEquals("SimpleData name not as expected", dataItem.getName(), name);
                assertEquals("SimpleData string not as expected", dataItem.getCharacters(), item);

                return;
            }
        }
        assertTrue("Unrecognized element not found", true);
    }

    @Test
    public void testGoogleTutorialExample01()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample01.kml");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue("Root feature is not as expected", feature instanceof KMLPlacemark);
        assertEquals("Incorrect name", "Simple placemark", feature.getName());
        assertEquals("Incorrect description",
            "Attached to the ground. Intelligently places itself\n"
                + "            at the height of the underlying terrain.",
            feature.getDescription());

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue("Geometry not a Point", geometry instanceof KMLPoint);

        Position coords = ((KMLPoint) geometry).getCoordinates();
        assertEquals("Incorrect latitude", Angle.fromDegrees(37.42228990140251), coords.getLatitude());
        assertEquals("Incorrect longitude", Angle.fromDegrees(-122.0822035425683), coords.getLongitude());
        assertEquals("Incorrect altitude", 0d, coords.getAltitude(), 0.0);
    }

    @Test
    public void testGoogleTutorialExample02()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample02.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue("Root feature is not as expected", document instanceof KMLDocument);

        List<KMLAbstractFeature> features = ((KMLDocument) document).getFeatures();
        assertEquals("Incorrect number of features", 1, features.size());
        assertTrue("Root feature is not as expected", features.get(0) instanceof KMLPlacemark);

        KMLPlacemark placemark = (KMLPlacemark) features.get(0);
        assertEquals("Incorrect name", "CDATA example", placemark.getName());
        String s =
            "\n"
                + "          <h1>CDATA Tags are useful!</h1>\n"
                + "          <p><font color=\"red\">Text is <i>more readable</i> and\n"
                + "          <b>easier to write</b> when you can avoid using entity\n"
                + "          references.</font></p>\n"
                + "        ";
        assertFalse("Description string not trimmed", s.equals(placemark.getDescription()));
        assertEquals("Incorrect description", s.trim(), placemark.getDescription());

        KMLAbstractGeometry geometry = placemark.getGeometry();
        assertTrue("Geometry not a Point", geometry instanceof KMLPoint);

        Position coords = ((KMLPoint) geometry).getCoordinates();
        assertEquals("Incorrect latitude", Angle.fromDegrees(14.996729), coords.getLatitude());
        assertEquals("Incorrect longitude", Angle.fromDegrees(102.595626), coords.getLongitude());
        assertEquals("Incorrect altitude", 0d, coords.getAltitude(), 0.0);
    }

    @Test
    public void testGoogleTutorialExample03()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample03.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue("Root feature is not as expected", document instanceof KMLDocument);

        List<KMLAbstractFeature> features = ((KMLDocument) document).getFeatures();
        assertEquals("Incorrect number of features", 1, features.size());
        assertTrue("Root feature is not as expected", features.get(0) instanceof KMLPlacemark);

        KMLPlacemark placemark = (KMLPlacemark) features.get(0);
        assertEquals("Incorrect name", "Entity references example", placemark.getName());
        assertEquals("Incorrect description",
            "<h1>Entity references are hard to type!</h1><p><font color=\"green\">Text\n                "
                + "is <i>more readable</i> and <b>easier to write</b> when you can avoid using\n                "
                + "entity references.</font></p>",
            placemark.getDescription());

        KMLAbstractGeometry geometry = placemark.getGeometry();
        assertTrue("Geometry not a Point", geometry instanceof KMLPoint);

        Position coords = ((KMLPoint) geometry).getCoordinates();
        assertEquals("Incorrect latitude", Angle.fromDegrees(14.998518), coords.getLatitude());
        assertEquals("Incorrect longitude", Angle.fromDegrees(102.594411), coords.getLongitude());
        assertEquals("Incorrect altitude", 0d, coords.getAltitude(), 0.0);
    }

    @Test
    public void testGoogleTutorialExample04()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample04.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue("Root feature is not as expected", document instanceof KMLFolder);
        assertEquals("Incorrect name", "Ground Overlays", document.getName());
        assertEquals("Incorrect description", "Examples of ground overlays", document.getDescription());

        List<KMLAbstractFeature> features = ((KMLFolder) document).getFeatures();
        assertEquals("Incorrect number of features", 1, features.size());
        assertTrue("Root feature is not as expected", features.get(0) instanceof KMLGroundOverlay);

        KMLGroundOverlay overlay = (KMLGroundOverlay) features.get(0);
        assertEquals("Incorrect name", "Large-scale overlay on terrain", overlay.getName());
        assertEquals("Incorrect description",
            "Overlay shows Mount Etna erupting\n"
                + "                on July 13th, 2001.", overlay.getDescription());

        KMLIcon icon = overlay.getIcon();
        assertNotNull("Overlay icon is null", icon);
        assertEquals("Incorrect icon href", "https://developers.google.com/kml/documentation/images/etna.jpg",
            icon.getHref());

        KMLLatLonBox box = overlay.getLatLonBox();
        assertNotNull("Overlay LatLonBox is null", box);
        assertEquals("Incorrect box north", 37.91904192681665, box.getNorth(), 0.0);
        assertEquals("Incorrect box south", 37.46543388598137, box.getSouth(), 0.0);
        assertEquals("Incorrect box east", 15.35832653742206, box.getEast(), 0.0);
        assertEquals("Incorrect box west", 14.60128369746704, box.getWest(), 0.0);
    }

    @Test
    public void testStyleReference()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/StyleReferences.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue("Root feature is not as expected", document instanceof KMLDocument);

        List<KMLAbstractFeature> features = ((KMLDocument) document).getFeatures();
        assertEquals("Incorrect number of features", 1, features.size());
        assertTrue("Document feature is not as expected", features.get(0) instanceof KMLPlacemark);

        List<KMLAbstractStyleSelector> styles = document.getStyleSelectors();
        assertEquals("Incorrect number of styles", 1, styles.size());
// // TODO: re-enable w/o relying on getStyleUrlResolved
//            KMLPlacemark placemark = (KMLPlacemark) features.get(0);
//            assertEquals("Incorrect name", "Building 41", placemark.getName());
//            assertEquals("Incorrect styleUrl", "#transBluePoly", placemark.getStyleUrl().getCharacters());
//            assertNotNull("Style is  null", placemark.getStyleUrlResolved());
//
//            assertTrue("Placemark feature is not as expected", placemark.getGeometry() instanceof KMLPolygon);
//            KMLPolygon pgon = (KMLPolygon) placemark.getGeometry();
//            assertEquals("Incorrect extrude value", (Boolean) true, pgon.getExtrude());
//            assertEquals("Incorrect altitude mode", "relativeToGround", pgon.getAltitudeMode());
//
//            KMLStyle style = placemark.getStyleUrlResolved();
//            KMLLineStyle lineStyle = style.getLineStyle();
//            assertNotNull("LineStyle is  null", lineStyle);
//            assertEquals("Line style width is not as expected", 1.5, lineStyle.getWidth());
//
//            KMLPolyStyle polyStyle = style.getPolyStyle();
//            assertNotNull("PolyStyle is  null", polyStyle);
//            assertEquals("Poly style color is not as expected", "7dff0000", polyStyle.getColor());
    }

    @Test
    public void testKMZFromFileURL()
    {
        try
        {
            File file = new File("testData/KML/kmztest01.kmz");
            KMLRoot root = KMLRoot.create(new URL("file:///" + file.getAbsolutePath().replace(" ", "%20")));
            root.parse();

            String[] fileNames = new String[]
                {
                    "files/BurjOverlay.png",
                    "files/CNOverlay.png",
                    "files/EmpireOverlay.png",
                    "files/PetronasOverlay.png",
                    "files/SearsOverlay.png",
                    "files/ShanghaiOverlay.png",
                    "files/TaipeiOverlay.png",
                    "files/TurningOverlay.png",
                    "files/ContinueOverlay.png",
                    "files/camera_mode.png",
                    "files/3DBuildingsLayer3.png",
                };

            for (String name : fileNames)
            {
                InputStream is = root.getKMLDoc().getSupportFileStream(name);
                assertNotNull("Support file not found in KMZ: " + name, is);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new WWRuntimeException();
        }
    }

    private StringBuilder newDocument()
    {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<kml");
        sb.append(" xmlns=\"").append(KMLConstants.KML_NAMESPACE).append("\"");
        sb.append(" xmlns:atom=\"").append(AtomConstants.ATOM_NAMESPACE).append("\"");
        sb.append(" xmlns:xal=\"").append(XALConstants.XAL_NAMESPACE).append("\"");
        sb.append(" xmlns:gx=\"").append(GXConstants.GX_NAMESPACE).append("\"");
        sb.append(">");

        return sb;
    }

    private StringBuilder newPrefixedDocument()
    {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<kml:kml");
        sb.append(" xmlns:kml=\"").append(KMLConstants.KML_NAMESPACE).append("\"");
        sb.append(" xmlns:atom=\"").append(AtomConstants.ATOM_NAMESPACE).append("\"");
        sb.append(" xmlns:xal=\"").append(XALConstants.XAL_NAMESPACE).append("\"");
        sb.append(" xmlns:gx=\"").append(GXConstants.GX_NAMESPACE).append("\"");
        sb.append(">");

        return sb;
    }

    private void endDocument(StringBuilder sb)
    {
        sb.append("</kml>");
    }

    private void endPrefixedDocument(StringBuilder sb)
    {
        sb.append("</kml:kml>");
    }

    private KMLRoot newParsedRoot(StringBuilder sb)
    {
        KMLRoot root;
        try
        {
            root = new KMLRoot(WWIO.getInputStreamFromString(sb.toString()), KMLConstants.KML_MIME_TYPE);
            return root.parse();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private KMLRoot newParsedRoot(StringBuilder sb, boolean suppressLogging)
    {
        KMLRoot root;
        try
        {
            root = new KMLRoot(WWIO.getInputStreamFromString(sb.toString()), KMLConstants.KML_MIME_TYPE);

            if (suppressLogging)
            {
                root.setNotificationListener(new XMLParserNotificationListener()
                {
                    public void notify(XMLParserNotification notification)
                    {
                        // Do nothing. This prevents logging of notification messages.
                    }
                });
            }

            return root.parse();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private KMLRoot openAndParseFile(String sourceDoc)
    {
        KMLRoot root;
        final StringBuilder parserMessage = new StringBuilder();

        try
        {
            root = new KMLRoot(new File(sourceDoc));
            root.setNotificationListener(new XMLParserNotificationListener()
            {
                public void notify(XMLParserNotification notificationEvent)
                {
                    if (parserMessage.length() != 0)
                        parserMessage.append(", ");

                    parserMessage.append(notificationEvent.toString());
                }
            });
            root.parse();

            assertNotNull("KML root is null", root);
            assertTrue("Parser notification occurred\n" + sourceDoc + ":" + parserMessage,
                parserMessage.length() == 0);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return root;
    }
}
