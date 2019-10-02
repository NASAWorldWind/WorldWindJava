/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.formats.georss.GeoRSSParser;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

/**
 * Illustrates how to create a shape from a GeoRSS document. This example creates two shapes from hard-coded example
 * GeoRSS documents.
 *
 * @author dcollins
 * @version $Id: GeoRSS.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class GeoRSS extends ApplicationTemplate {

    private static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false);

            RenderableLayer layer = this.buildGeoRSSLayer();
            layer.setName("GeoRSS Shapes");
            insertBeforePlacenames(this.getWwd(), layer);
        }

        private RenderableLayer buildGeoRSSLayer() {
            RenderableLayer layer = new RenderableLayer();
            java.util.List<Renderable> shapes;

            shapes = GeoRSSParser.parseShapes(GeoRSS_DOCSTRING_A);
            if (shapes != null) {
                addRenderables(layer, shapes);
            }

            shapes = GeoRSSParser.parseShapes(GeoRSS_DOCSTRING_B);
            if (shapes != null) {
                addRenderables(layer, shapes);
            }

            shapes = GeoRSSParser.parseShapes(GeoRSS_DOCSTRING_C);
            if (shapes != null) {
                addRenderables(layer, shapes);
            }

            return layer;
        }

        private void addRenderables(RenderableLayer layer, Iterable<Renderable> renderables) {
            for (Renderable r : renderables) {
                layer.addRenderable(r);
            }
        }
    }

    private static final String GeoRSS_DOCSTRING_A
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
            + "    <georss:where>"
            + "      <gml:Polygon>"
            + "        <gml:exterior>"
            + "          <gml:LinearRing>"
            + "            <gml:posList>"
            + "              45.256 -110.45 46.46 -109.48 43.84 -109.86 45.256 -110.45"
            + "            </gml:posList>"
            + "          </gml:LinearRing>"
            + "        </gml:exterior>"
            + "      </gml:Polygon>"
            + "    </georss:where>"
            + "  </entry>"
            + "</feed>";

    private static final String GeoRSS_DOCSTRING_B
            = "<feed xmlns=\"http://www.w3.org/2005/Atom\""
            + "              xmlns:georss=\"http://www.georss.org/georss\">"
            + "              <title>scribble</title>"
            + "              <id>http://example.com/atom</id>"
            + "              <author><name>Christopher Schmidt</name></author>"
            + "<entry>"
            + "  <id>http://example.com/19.atom</id>"
            + "  <link href=\"http://example.com/19.html\"/>"
            + "  <title>Feature #19</title>"
            + "  <content type=\"html\">Some content.</content>"
            + "  <georss:line>"
            + "    23.1811523438 -159.609375 "
            + "    22.5 -161.564941406 "
            + "    20.654296875 -160.422363281 "
            + "    18.4350585938 -156.247558594 "
            + "    18.3471679688 -154.731445312 "
            + "    19.951171875 -153.588867188 "
            + "    21.8188476562 -155.983886719"
            + "    23.02734375 -158.994140625"
            + "    23.0932617188 -159.631347656"
            + "  </georss:line>"
            + "</entry>"
            + "</feed>";

    private static final String GeoRSS_DOCSTRING_C
            = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<feed xmlns=\"http://www.w3.org/2005/Atom\""
            + "      xmlns:georss=\"http://www.georss.org/georss\""
            + "      xmlns:gml=\"http://www.opengis.net/gml\">"
            + "  <title>An X</title>"
            + "    <subtitle>Line test</subtitle>"
            + "    <link href=\"http://example.org/\"/>"
            + "    <updated>2005-12-13T18:30:02Z</updated>"
            + "    <author>"
            + "      <name>NASA</name>"
            + "      <email>nasa@nasa.gov</email>"
            + "    </author>"
            + "    <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>"
            + "  <entry>"
            + "    <title>An X</title>"
            + "    <link href=\"http://example.org/2005/09/09/atom01\"/>"
            + "    <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>"
            + "    <updated>2005-08-17T07:02:32Z</updated>"
            + "    <summary>Test</summary>"
            + "    <georss:line>45 -95 44 -94</georss:line>"
            + "    <georss:line>45 -94 44 -95</georss:line>"
            + "    <georss:elev>1000</georss:elev>"
            + "  </entry>"
            + "</feed>";

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind GeoRSS", AppFrame.class);
    }
}
