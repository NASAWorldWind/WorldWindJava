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

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.gx.GXConstants;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.util.xml.atom.AtomConstants;
import gov.nasa.worldwind.util.xml.xal.XALConstants;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
public class KMLTest
{
    @Test
    public void testRootElement()
    {
        StringBuilder sb = this.newDocument();
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);

        assertNotNull(root, "KML root is null");
        assertNull(root.getHint(), "KML root hint is not null");
    }

    @Test
    public void testRootHint()
    {
        StringBuilder sb = this.newDocument();
        sb = new StringBuilder(sb.toString().replace("<kml", "<kml hint=\"yes\""));
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);

        assertNotNull(root, "KML root is null");
        assertNotNull(root.getHint(), "KML root hint is null");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLDocument, "Root feature is not as expected");
        assertEquals(feature.getId(), ID, "Object ID not as expected");
        assertEquals(feature.getTargetId(), targetID, "Target ID not as expected");
    }

    @Test
    public void testUnassignedAbstractObjectAttributes()
    {
        StringBuilder sb = this.newDocument();
        sb.append("<Document>");
        sb.append("</Document>");
        this.endDocument(sb);

        KMLRoot root = this.newParsedRoot(sb);
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLDocument, "Root feature is not as expected");

        assertNull(feature.getId(), "ID not null");
        assertNull(feature.getTargetId(), "Target ID not null");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLDocument, "Root feature is not as expected");

        assertEquals(feature.getName(), name, "Name not as expected");
        assertEquals(feature.getVisibility(), visibility, "Visibility not as expected");
        assertEquals(feature.getOpen(), open, "Open not as expected");
        assertEquals(feature.getAddress(), address, "Address not as expected");
        assertEquals(feature.getPhoneNumber(), phoneNumber, "Phone number not as expected");
        assertEquals(feature.getSnippet(), snippet, "Snippet not as expected");
        assertEquals(feature.getDescription(), description, "Description not as expected");
        assertEquals(feature.getStyleUrl().getCharacters(), styleUrl, "Style URL not as expected");

        assertEquals(feature.getLink().getHref(), linkHref, "Link href not as expected");
        assertEquals(feature.getLink().getRel(), linkRel, "Link rel not as expected");
        assertEquals(feature.getLink().getType(), linkType, "Link type not as expected");
        assertEquals(feature.getLink().getHreflang(), linkHreflang, "Link hreflang not as expected");
        assertEquals(feature.getLink().getTitle(), linkTitle, "Link title not as expected");
        assertEquals(feature.getLink().getLength().intValue(), linkLength, "Link length not as expected");
        assertEquals(feature.getLink().getBase(), linkBase, "Link base not as expected");
        assertEquals(feature.getLink().getLang(), linkLang, "Link lang not as expected");

        assertEquals(feature.getAuthor().getName(), authorName, "Author name not as expected");
        assertEquals(feature.getAuthor().getEmail(), authorEmail, "Author email not as expected");
        assertEquals(feature.getAuthor().getUri(), authorUri, "Author URI not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLDocument, "Root feature is not as expected");

        assertNull(feature.getName(), "Name not null");
        assertNull(feature.getVisibility(), "Visibility not null");
        assertNull(feature.getOpen(), "Open not null");
        assertNull(feature.getAddress(), "Address not null");
        assertNull(feature.getPhoneNumber(), "Phone number not null");
        assertNull(feature.getSnippet(), "Snippet not null");
        assertNull(feature.getDescription(), "Description not null");
        assertNull(feature.getStyleUrl(), "Style URL not null");
        assertNull(feature.getView(), "View not null");
        assertNull(feature.getRegion(), "Region not null");
        assertNull(feature.getAuthor(), "Author not null");
        assertNull(feature.getLink(), "Link not null");
        assertNull(feature.getAddressDetails(), "Address details not null");
        assertEquals(0, feature.getStyleSelectors().size(), "Style selectors not empty");
        assertNull(feature.getTimePrimitive(), "Time not null");
        assertNull(feature.getExtendedData(), "Extended data not null");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLPoint, "Placemark geometry is not as expected");

        KMLPoint point = (KMLPoint) geometry;
        assertEquals(point.getAltitudeMode(), altitudeMode, "Altitude mode not as expected");
        assertEquals(point.isExtrude(), extrude, "Extrude not as expected");
        assertEquals(point.getCoordinates(), coords, "Coordinates not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLPoint, "Placemark geometry is not as expected");

        KMLPoint point = (KMLPoint) geometry;
        assertEquals(point.getAltitudeMode(), altitudeMode, "Altitude mode not as expected");
        assertEquals(point.isExtrude(), extrude, "Extrude not as expected");
        assertEquals(point.getCoordinates(), coords, "Coordinates not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLPoint, "Placemark geometry is not as expected");

        KMLPoint point = (KMLPoint) geometry;
        assertEquals(point.getAltitudeMode(), altitudeMode, "Altitude mode not as expected");
        assertEquals(point.isExtrude(), extrude, "Extrude not as expected");
        assertEquals(point.getCoordinates(), coords, "Coordinates not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLLinearRing, "Placemark geometry is not as expected");

        KMLLinearRing ring = (KMLLinearRing) geometry;
        assertEquals(ring.getAltitudeMode(), altitudeMode, "Altitude mode not as expected");
        assertEquals(ring.isExtrude(), extrude, "Extrude not as expected");
        assertEquals(ring.getTessellate(), tessellate, "Tessellate not as expected");
        assertEquals(ring.getCoordinates().list, coords, "Coordinates not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLLineString, "Placemark geometry is not as expected");

        KMLLineString ring = (KMLLineString) geometry;
        assertEquals(ring.getAltitudeMode(), altitudeMode, "Altitude mode not as expected");
        assertEquals(ring.isExtrude(), extrude, "Extrude not as expected");
        assertEquals(ring.getTessellate(), tessellate, "Tessellate not as expected");
        assertEquals(ring.getCoordinates().list, coords, "Coordinates not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLPolygon, "Placemark geometry is not as expected");

        KMLPolygon pgon = (KMLPolygon) geometry;
        assertEquals(pgon.getAltitudeMode(), altitudeMode, "Altitude mode not as expected");
        assertEquals(pgon.isExtrude(), extrude, "Extrude not as expected");
        assertEquals(pgon.getTessellate(), tessellate, "Tessellate not as expected");

        assertEquals(pgon.getOuterBoundary().getCoordinates().list, outerCoords, "Outer coordinates not as expected");
        assertEquals(pgon.getOuterBoundary().getAltitudeMode(), outerAltitudeMode, "Outer altitude mode not as expected");
        assertEquals(pgon.getOuterBoundary().isExtrude(), outerExtrude, "Outer extrude not as expected");
        assertEquals(pgon.getOuterBoundary().getTessellate(), outerTessellate, "Outer tessellate not as expected");

        Iterable<? extends KMLLinearRing> innerBoundaries = pgon.getInnerBoundaries();
        assertNotNull(innerBoundaries);
        assertTrue(innerBoundaries.iterator().hasNext());
        KMLLinearRing innerBoundary = innerBoundaries.iterator().next();
        assertNotNull(innerBoundary);
        assertEquals(innerBoundary.getCoordinates().list, innerCoords, "Inner coordinates not as expected");
        assertEquals(innerBoundary.getAltitudeMode(), innerAltitudeMode, "Inner altitude mode not as expected");
        assertEquals(innerBoundary.isExtrude(), innerExtrude, "Inner extrude not as expected");
        assertEquals(innerBoundary.getTessellate(), innerTessellate, "Inner tessellate not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLSimpleData dataItem = ((KMLPlacemark) feature).getSimpleData();
        assertNotNull(dataItem, "No SimpleData");
        assertEquals(dataItem.getName(), name, "SimpleData name not as expected");
        assertEquals(dataItem.getCharacters(), item, "SimpleData string not as expected");
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
        assertNotNull(root, "KML root is null");

        for (Map.Entry<String, Object> field : root.getFields().getEntries())
        {
            if (field.getKey().equals("Unrecognized") && field.getValue() instanceof UnrecognizedXMLEventParser)
            {
                UnrecognizedXMLEventParser uField = (UnrecognizedXMLEventParser) field.getValue();
                Object o = uField.getField("Placemark");
                assertNotNull(o, "No SimpleData");
                assertTrue(o instanceof KMLPlacemark, "Unrecognized object not as expected");

                KMLSimpleData dataItem = ((KMLPlacemark) o).getSimpleData();
                assertNotNull(dataItem, "No SimpleData");
                assertEquals(dataItem.getName(), name, "SimpleData name not as expected");
                assertEquals(dataItem.getCharacters(), item, "SimpleData string not as expected");

                return;
            }
        }
        assertTrue(true, "Unrecognized element not found");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLLinearRing, "Placemark geometry is not as expected");

        KMLLinearRing ring = (KMLLinearRing) geometry;
        assertEquals(ring.getCoordinates().list, coords, "Coordinates not as expected");
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

        assertEquals(coords, positions, "Coordinates not as expected");
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
        assertNotNull(root, "KML root is null");

        KMLAbstractFeature doc = root.getFeature();
        assertNotNull(doc, "Document is null");
        assertTrue(doc instanceof KMLDocument, "Unrecognized object not as expected");

        for (Map.Entry<String, Object> field : doc.getFields().getEntries())
        {
            if (field.getKey().equals("Unrecognized") && field.getValue() instanceof UnrecognizedXMLEventParser)
            {
                UnrecognizedXMLEventParser uField = (UnrecognizedXMLEventParser) field.getValue();
                Object o = uField.getField("Placemark");
                assertNotNull(o, "No SimpleData");
                assertTrue(o instanceof KMLPlacemark, "Unrecognized object not as expected");

                KMLSimpleData dataItem = ((KMLPlacemark) o).getSimpleData();
                assertNotNull(dataItem, "No SimpleData");
                assertEquals(dataItem.getName(), name, "SimpleData name not as expected");
                assertEquals(dataItem.getCharacters(), item, "SimpleData string not as expected");

                return;
            }
        }
        assertTrue(true, "Unrecognized element not found");
    }

    @Test
    public void testGoogleTutorialExample01()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample01.kml");

        KMLAbstractFeature feature = root.getFeature();
        assertTrue(feature instanceof KMLPlacemark, "Root feature is not as expected");
        assertEquals("Simple placemark", feature.getName(), "Incorrect name");
        assertEquals("Attached to the ground. Intelligently places itself\n"
                + "            at the height of the underlying terrain.", feature.getDescription(), "Incorrect description");

        KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
        assertTrue(geometry instanceof KMLPoint, "Geometry not a Point");

        Position coords = ((KMLPoint) geometry).getCoordinates();
        assertEquals(Angle.fromDegrees(37.42228990140251), coords.getLatitude(), "Incorrect latitude");
        assertEquals(Angle.fromDegrees(-122.0822035425683), coords.getLongitude(), "Incorrect longitude");
        assertEquals(0d, coords.getAltitude(), 0.0, "Incorrect altitude");
    }

    @Test
    public void testGoogleTutorialExample02()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample02.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue(document instanceof KMLDocument, "Root feature is not as expected");

        List<KMLAbstractFeature> features = ((KMLDocument) document).getFeatures();
        assertEquals(1, features.size(), "Incorrect number of features");
        assertTrue(features.get(0) instanceof KMLPlacemark, "Root feature is not as expected");

        KMLPlacemark placemark = (KMLPlacemark) features.get(0);
        assertEquals("CDATA example", placemark.getName(), "Incorrect name");
        String s =
            "\n"
                + "          <h1>CDATA Tags are useful!</h1>\n"
                + "          <p><font color=\"red\">Text is <i>more readable</i> and\n"
                + "          <b>easier to write</b> when you can avoid using entity\n"
                + "          references.</font></p>\n"
                + "        ";
        assertFalse(s.equals(placemark.getDescription()), "Description string not trimmed");
        assertEquals(s.trim(), placemark.getDescription(), "Incorrect description");

        KMLAbstractGeometry geometry = placemark.getGeometry();
        assertTrue(geometry instanceof KMLPoint, "Geometry not a Point");

        Position coords = ((KMLPoint) geometry).getCoordinates();
        assertEquals(Angle.fromDegrees(14.996729), coords.getLatitude(), "Incorrect latitude");
        assertEquals(Angle.fromDegrees(102.595626), coords.getLongitude(), "Incorrect longitude");
        assertEquals(0d, coords.getAltitude(), 0.0, "Incorrect altitude");
    }

    @Test
    public void testGoogleTutorialExample03()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample03.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue(document instanceof KMLDocument, "Root feature is not as expected");

        List<KMLAbstractFeature> features = ((KMLDocument) document).getFeatures();
        assertEquals(1, features.size(), "Incorrect number of features");
        assertTrue(features.get(0) instanceof KMLPlacemark, "Root feature is not as expected");

        KMLPlacemark placemark = (KMLPlacemark) features.get(0);
        assertEquals("Entity references example", placemark.getName(), "Incorrect name");
        assertEquals("<h1>Entity references are hard to type!</h1><p><font color=\"green\">Text\n                "
                + "is <i>more readable</i> and <b>easier to write</b> when you can avoid using\n                "
                + "entity references.</font></p>", placemark.getDescription(), "Incorrect description");

        KMLAbstractGeometry geometry = placemark.getGeometry();
        assertTrue(geometry instanceof KMLPoint, "Geometry not a Point");

        Position coords = ((KMLPoint) geometry).getCoordinates();
        assertEquals(Angle.fromDegrees(14.998518), coords.getLatitude(), "Incorrect latitude");
        assertEquals(Angle.fromDegrees(102.594411), coords.getLongitude(), "Incorrect longitude");
        assertEquals(0d, coords.getAltitude(), 0.0, "Incorrect altitude");
    }

    @Test
    public void testGoogleTutorialExample04()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/GoogleTutorialExample04.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue(document instanceof KMLFolder, "Root feature is not as expected");
        assertEquals("Ground Overlays", document.getName(), "Incorrect name");
        assertEquals("Examples of ground overlays", document.getDescription(), "Incorrect description");

        List<KMLAbstractFeature> features = ((KMLFolder) document).getFeatures();
        assertEquals(1, features.size(), "Incorrect number of features");
        assertTrue(features.get(0) instanceof KMLGroundOverlay, "Root feature is not as expected");

        KMLGroundOverlay overlay = (KMLGroundOverlay) features.get(0);
        assertEquals("Large-scale overlay on terrain", overlay.getName(), "Incorrect name");
        assertEquals("Overlay shows Mount Etna erupting\n"
                + "                on July 13th, 2001.", overlay.getDescription(), "Incorrect description");

        KMLIcon icon = overlay.getIcon();
        assertNotNull(icon, "Overlay icon is null");
        assertEquals("https://developers.google.com/kml/documentation/images/etna.jpg", icon.getHref(), "Incorrect icon href");

        KMLLatLonBox box = overlay.getLatLonBox();
        assertNotNull(box, "Overlay LatLonBox is null");
        assertEquals(37.91904192681665, box.getNorth(), 0.0, "Incorrect box north");
        assertEquals(37.46543388598137, box.getSouth(), 0.0, "Incorrect box south");
        assertEquals(15.35832653742206, box.getEast(), 0.0, "Incorrect box east");
        assertEquals(14.60128369746704, box.getWest(), 0.0, "Incorrect box west");
    }

    @Test
    public void testStyleReference()
    {
        KMLRoot root = this.openAndParseFile("testData/KML/StyleReferences.kml");

        KMLAbstractFeature document = root.getFeature();
        assertTrue(document instanceof KMLDocument, "Root feature is not as expected");

        List<KMLAbstractFeature> features = ((KMLDocument) document).getFeatures();
        assertEquals(1, features.size(), "Incorrect number of features");
        assertTrue(features.get(0) instanceof KMLPlacemark, "Document feature is not as expected");

        List<KMLAbstractStyleSelector> styles = document.getStyleSelectors();
        assertEquals(1, styles.size(), "Incorrect number of styles");
// // TODO: re-enable w/o relying on getStyleUrlResolved
//            KMLPlacemark placemark = (KMLPlacemark) features.get(0);
//            assertEquals("Building 41", placemark.getName(), "Incorrect name");
//            assertEquals("#transBluePoly", placemark.getStyleUrl().getCharacters(), "Incorrect styleUrl");
//            assertNotNull(placemark.getStyleUrlResolved(), "Style is  null");
//
//            assertTrue(placemark.getGeometry() instanceof KMLPolygon, "Placemark feature is not as expected");
//            KMLPolygon pgon = (KMLPolygon) placemark.getGeometry();
//            assertEquals((Boolean) true, pgon.getExtrude(), "Incorrect extrude value");
//            assertEquals("relativeToGround", pgon.getAltitudeMode(), "Incorrect altitude mode");
//
//            KMLStyle style = placemark.getStyleUrlResolved();
//            KMLLineStyle lineStyle = style.getLineStyle();
//            assertNotNull(lineStyle, "LineStyle is  null");
//            assertEquals(1.5, lineStyle.getWidth(), "Line style width is not as expected");
//
//            KMLPolyStyle polyStyle = style.getPolyStyle();
//            assertNotNull(polyStyle, "PolyStyle is  null");
//            assertEquals("7dff0000", polyStyle.getColor(), "Poly style color is not as expected");
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
                assertNotNull(is, "Support file not found in KMZ: " + name);
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

            assertNotNull(root, "KML root is null");
            assertTrue(parserMessage.length() == 0,
                "Parser notification occurred\n" + sourceDoc + ":" + parserMessage);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return root;
    }
}
