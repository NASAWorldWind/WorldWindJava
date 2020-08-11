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
package gov.nasa.worldwind.formats.georss;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@SuppressWarnings("deprecation")
@RunWith(JUnit4.class)
public class GeoRSSParserTest {
    //////////////////////////////////////////////////////////
    // GeoRSS-Simple Parsing Tests
    //////////////////////////////////////////////////////////

    @Test
    public void testSimple_Point() {
        String xmlString = createExampleGeoRSS(
                "<georss:point>45.256 -71.92</georss:point>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // <georss:point> is not translated to any renderable shape.
        assertNull("", shapes);
    }

    @Test
    public void testSimple_PointWithElevation() {
        String xmlString = createExampleGeoRSS(
                "<georss:point>45.256 -71.92</georss:point>"
                + "<georss:elev>313</georss:elev>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // <georss:point> is not translated to any renderable shape.
        assertNull("", shapes);
    }

    @Test
    public void testSimple_Line() {
        String xmlString = createExampleGeoRSS(
                "<georss:line>45.256 -110.45 46.46 -109.48 43.84 -109.86</georss:line>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:line> is translated to a WWJ Path.
        assertTrue("", shapes.get(0) instanceof Path);

        Path shape = (Path) shapes.get(0);
        java.util.List<Position> positions = (java.util.List<Position>) shape.getPositions();
        assertNotNull("", positions);
        assertEquals("", 3, positions.size());
        assertEquals("", Position.fromDegrees(45.256, -110.45, 0.0), positions.get(0));
        assertEquals("", Position.fromDegrees(46.46, -109.48, 0.0), positions.get(1));
        assertEquals("", Position.fromDegrees(43.84, -109.86, 0.0), positions.get(2));
    }

    @Test
    public void testSimple_LineWithElevation() {
        String xmlString = createExampleGeoRSS(
                "<georss:line>45.256 -110.45 46.46 -109.48 43.84 -109.86</georss:line>"
                + "<georss:elev>313</georss:elev>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:line> is translated to a WWJ Path.
        assertTrue("", shapes.get(0) instanceof Path);

        Path shape = (Path) shapes.get(0);
        java.util.List<Position> positions = (java.util.List<Position>) shape.getPositions();
        assertNotNull("", positions);
        assertEquals("", 3, positions.size());
        assertEquals("", Position.fromDegrees(45.256, -110.45, 313.0), positions.get(0));
        assertEquals("", Position.fromDegrees(46.46, -109.48, 313.0), positions.get(1));
        assertEquals("", Position.fromDegrees(43.84, -109.86, 313.0), positions.get(2));
    }

    @Test
    public void testSimple_Polygon() {
        String xmlString = createExampleGeoRSS(
                "<georss:polygon>45.256 -110.45 46.46 -109.48 43.84 -109.86 45.256 -110.45</georss:polygon>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:polygon> is translated to a WWJ SurfacePolygon when no elevation is specified.
        assertTrue("", shapes.get(0) instanceof SurfacePolygon);

        SurfacePolygon shape = (SurfacePolygon) shapes.get(0);
        java.util.List positions = (java.util.List) shape.getLocations();
        assertNotNull("", positions);
        assertEquals("", 4, positions.size());
        assertEquals("", LatLon.fromDegrees(45.256, -110.45), positions.get(0));
        assertEquals("", LatLon.fromDegrees(46.46, -109.48), positions.get(1));
        assertEquals("", LatLon.fromDegrees(43.84, -109.86), positions.get(2));
        assertEquals("", LatLon.fromDegrees(45.256, -110.45), positions.get(3));
    }

    @Test
    public void testSimple_PolygonWithElevation() {
        String xmlString = createExampleGeoRSS(
                "<georss:polygon>45.256 -110.45 46.46 -109.48 43.84 -109.86 45.256 -110.45</georss:polygon>"
                + "<georss:elev>313</georss:elev>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:polygon> is translated to a WWJ Path when an elevation is specified.
        assertTrue("", shapes.get(0) instanceof Path);

        Path shape = (Path) shapes.get(0);
        java.util.List<Position> positions = (java.util.List<Position>) shape.getPositions();
        assertNotNull("", positions);
        assertEquals("", 4, positions.size());
        assertEquals("", Position.fromDegrees(45.256, -110.45, 313.0), positions.get(0));
        assertEquals("", Position.fromDegrees(46.46, -109.48, 313.0), positions.get(1));
        assertEquals("", Position.fromDegrees(43.84, -109.86, 313.0), positions.get(2));
        assertEquals("", Position.fromDegrees(45.256, -110.45, 313.0), positions.get(3));
    }

    @Test
    public void testSimple_Box() {
        String xmlString = createExampleGeoRSS(
                "<georss:box>42.943 -71.032 43.039 -69.856</georss:box>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:box> is translated to a WWJ SurfaceSector when no elevation is specified.
        assertTrue("", shapes.get(0) instanceof SurfaceSector);

        SurfaceSector shape = (SurfaceSector) shapes.get(0);
        java.util.List<LatLon> positions = shape.getSector().asList();
        assertNotNull("", positions);
        assertEquals("", 4, positions.size());
        assertEquals("", LatLon.fromDegrees(42.943, -71.032), positions.get(0));
        assertEquals("", LatLon.fromDegrees(42.943, -69.856), positions.get(1));
        assertEquals("", LatLon.fromDegrees(43.039, -69.856), positions.get(2));
        assertEquals("", LatLon.fromDegrees(43.039, -71.032), positions.get(3));
    }

    @Test
    public void testSimple_BoxWithElevation() {
        String xmlString = createExampleGeoRSS(
                "<georss:box>42.943 -71.032 43.039 -69.856</georss:box>"
                + "<georss:elev>313</georss:elev>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:box> is translated to a WWJ Quadrilateral when an elevation is specified.
        assertTrue("", shapes.get(0) instanceof Quadrilateral);

        Quadrilateral shape = (Quadrilateral) shapes.get(0);
        LatLon[] positions = shape.getCorners();
        assertNotNull("", positions);
        assertEquals("", 2, positions.length);
        assertEquals("", LatLon.fromDegrees(42.943, -71.032), positions[0]);
        assertEquals("", LatLon.fromDegrees(43.039, -69.856), positions[1]);
        assertEquals("", 313.0, shape.getElevation(), 0.0);
    }

    //////////////////////////////////////////////////////////
    // GeoRSS-GML Parsing Tests
    //////////////////////////////////////////////////////////
    @Test
    public void testGML_Point() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:Point>"
                + "    <gml:pos>45.256 -71.92</gml:pos>"
                + "  </gml:Point>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // <gml:Point> is not translated to any renderable shape.
        assertNull("", shapes);
    }

    @Test
    public void testGML_Line() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:LineString>"
                + "    <gml:posList>"
                + "      45.256 -110.45 46.46 -109.48 43.84 -109.86"
                + "    </gml:posList>"
                + "  </gml:LineString>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <gml:LineString> is translated to a WWJ Path.
        assertTrue("", shapes.get(0) instanceof Path);

        Path shape = (Path) shapes.get(0);
        java.util.List<Position> positions = (java.util.List<Position>) shape.getPositions();
        assertNotNull("", positions);
        assertEquals("", 3, positions.size());
        assertEquals("", Position.fromDegrees(45.256, -110.45, 0.0), positions.get(0));
        assertEquals("", Position.fromDegrees(46.46, -109.48, 0.0), positions.get(1));
        assertEquals("", Position.fromDegrees(43.84, -109.86, 0.0), positions.get(2));
    }

    @Test
    public void testGML_Polygon() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:Polygon>"
                + "    <gml:exterior>"
                + "      <gml:LinearRing>"
                + "        <gml:posList>"
                + "          45.256 -110.45 46.46 -109.48 43.84 -109.86 45.256 -110.45"
                + "        </gml:posList>"
                + "      </gml:LinearRing>"
                + "    </gml:exterior>"
                + "  </gml:Polygon>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <gml:Polygon> is translated to a WWJ SurfacePolygon.
        assertTrue("", shapes.get(0) instanceof SurfacePolygon);

        SurfacePolygon shape = (SurfacePolygon) shapes.get(0);
        java.util.List positions = (java.util.List) shape.getLocations();
        assertNotNull("", positions);
        assertEquals("", 4, positions.size());
        assertEquals("", LatLon.fromDegrees(45.256, -110.45), positions.get(0));
        assertEquals("", LatLon.fromDegrees(46.46, -109.48), positions.get(1));
        assertEquals("", LatLon.fromDegrees(43.84, -109.86), positions.get(2));
        assertEquals("", LatLon.fromDegrees(45.256, -110.45), positions.get(3));
    }

    @Test
    public void testGML_Box() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:Envelope>"
                + "    <gml:lowerCorner>42.943 -71.032</gml:lowerCorner>"
                + "    <gml:upperCorner>43.039 -69.856</gml:upperCorner>"
                + "  </gml:Envelope>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:box> is translated to a WWJ SurfaceSector when no elevation is specified.
        assertTrue("", shapes.get(0) instanceof SurfaceSector);

        SurfaceSector shape = (SurfaceSector) shapes.get(0);
        java.util.List<LatLon> positions = shape.getSector().asList();
        assertNotNull("", positions);
        assertEquals("", 4, positions.size());
        assertEquals("", LatLon.fromDegrees(42.943, -71.032), positions.get(0));
        assertEquals("", LatLon.fromDegrees(42.943, -69.856), positions.get(1));
        assertEquals("", LatLon.fromDegrees(43.039, -69.856), positions.get(2));
        assertEquals("", LatLon.fromDegrees(43.039, -71.032), positions.get(3));
    }

    //////////////////////////////////////////////////////////
    // Exceptional Condition Tests
    //////////////////////////////////////////////////////////
    @Test
    public void testSimple_PointNotEnoughPairs() {
        String xmlString = createExampleGeoRSS(
                "<georss:point></georss:point>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void testSimple_LineNotEnoughPairs() {
        String xmlString = createExampleGeoRSS(
                "<georss:line>45.256 -110.45</georss:line>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void testSimple_PolygonNotEnoughPairs() {
        String xmlString = createExampleGeoRSS(
                "<georss:polygon>45.256 -110.45 46.46 -109.48 43.84 -109.86</georss:polygon>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void testSimple_BoxNotEnoughPairs() {
        String xmlString = createExampleGeoRSS(
                "<georss:box>42.943 -71.032</georss:box>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void testGML_PointNotEnoughPairs() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:Point>"
                + "    <gml:pos></gml:pos>"
                + "  </gml:Point>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void testGML_LineNotEnoughPairs() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:LineString>"
                + "    <gml:posList>"
                + "      45.256 -110.45"
                + "    </gml:posList>"
                + "  </gml:LineString>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void testGML_PolygonNotEnoughPairs() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:Polygon>"
                + "    <gml:exterior>"
                + "      <gml:LinearRing>"
                + "        <gml:posList>"
                + "          45.256 -110.45 46.46 -109.48 43.84 -109.86"
                + "        </gml:posList>"
                + "      </gml:LinearRing>"
                + "    </gml:exterior>"
                + "  </gml:Polygon>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void testGML_BoxMissingElement() {
        String xmlString = createExampleGeoRSS(
                "<georss:where>"
                + "  <gml:Envelope>"
                + "    <gml:lowerCorner>42.943 -71.032</gml:lowerCorner>"
                + // The next line would normally be included.
                // "    <gml:upperCorner>43.039 -69.856</gml:upperCorner>" +
                "  </gml:Envelope>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void test_NoShapes() {
        String xmlString = createExampleGeoRSS("");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        assertNull("", shapes);
    }

    @Test
    public void test_MultipleShapes() {
        String xmlString = createExampleGeoRSS(
                "<georss:line>45.256 -110.45 46.46 -109.48 43.84 -109.86</georss:line>"
                + "<georss:where>"
                + "  <gml:LineString>"
                + "    <gml:posList>"
                + "      45.256 -110.45 46.46 -109.48 43.84 -109.86"
                + "    </gml:posList>"
                + "  </gml:LineString>"
                + "</georss:where>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() >= 2);
        assertNotNull("", shapes.get(0));
        assertNotNull("", shapes.get(1));
        // <georss:line> is translated to a WWJ Path.
        assertTrue("", shapes.get(0) instanceof Path);
        // <georss:polygon> is translated to a WWJ Path when an elevation is specified.
        assertTrue("", shapes.get(1) instanceof Path);

        Path shape;
        java.util.List<Position> positions;

        shape = (Path) shapes.get(0);
        positions = (java.util.List<Position>) shape.getPositions();
        assertNotNull("", positions);
        assertEquals("", 3, positions.size());
        assertEquals("", Position.fromDegrees(45.256, -110.45, 0.0), positions.get(0));
        assertEquals("", Position.fromDegrees(46.46, -109.48, 0.0), positions.get(1));
        assertEquals("", Position.fromDegrees(43.84, -109.86, 0.0), positions.get(2));

        shape = (Path) shapes.get(1);
        positions = (java.util.List<Position>) shape.getPositions();
        assertNotNull("", positions);
        assertEquals("", 3, positions.size());
        assertEquals("", Position.fromDegrees(45.256, -110.45, 0.0), positions.get(0));
        assertEquals("", Position.fromDegrees(46.46, -109.48, 0.0), positions.get(1));
        assertEquals("", Position.fromDegrees(43.84, -109.86, 0.0), positions.get(2));
    }

    @Test
    public void test_CommaDelimitedCoordinates() {
        String xmlString = createExampleGeoRSS(
                "<georss:line>45.256, -110.45, 46.46, -109.48, 43.84, -109.86</georss:line>");
        java.util.List<Renderable> shapes = GeoRSSParser.parseShapes(xmlString);

        // Parsed shapes list should have at least one non-null element.
        assertNotNull("", shapes);
        assertTrue("", shapes.size() != 0);
        assertNotNull("", shapes.get(0));
        // <georss:line> is translated to a WWJ Path.
        assertTrue("", shapes.get(0) instanceof Path);

        Path shape = (Path) shapes.get(0);
        java.util.List<Position> positions = (java.util.List<Position>) shape.getPositions();
        assertNotNull("", positions);
        assertEquals("", 3, positions.size());
        assertEquals("", Position.fromDegrees(45.256, -110.45, 0.0), positions.get(0));
        assertEquals("", Position.fromDegrees(46.46, -109.48, 0.0), positions.get(1));
        assertEquals("", Position.fromDegrees(43.84, -109.86, 0.0), positions.get(2));
    }

    //////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////
    private static String createExampleGeoRSS(String georssXml) {
        String xmlString
                = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<feed xmlns=\"http://www.w3.org/2005/Atom\""
                + "      xmlns:georss=\"http://www.georss.org/georss\""
                + "      xmlns:gml=\"http://www.opengis.net/gml\">"
                + "  <title>Earthquakes</title>"
                + "    <subtitle>International earthquake observation labs</subtitle>"
                + "    <link href=\"http://example.org/\"/>"
                + "    <updated>2005-12-13T18:30:02Z</updated>"
                + "    <author>"
                + "      <name>Dr. Thaddeus Remor</name>"
                + "      <email>tremor@quakelab.edu</email>"
                + "    </author>"
                + "    <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>"
                + "  <entry>"
                + "    <title>M 3.2, Mona Passage</title>"
                + "    <link href=\"http://example.org/2005/09/09/atom01\"/>"
                + "    <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>"
                + "    <updated>2005-08-17T07:02:32Z</updated>"
                + "    <summary>We just had a big one.</summary>"
                + "    {0}"
                + "  </entry>"
                + "</feed>";
        return java.text.MessageFormat.format(xmlString, georssXml);
    }
}
