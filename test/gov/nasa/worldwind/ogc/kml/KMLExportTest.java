/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.kml.KMLDocumentBuilder;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.XMLConstants;
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import java.io.*;
import java.util.*;

/**
 * Test export of KML by writing shapes to KML and validating the resulting document against the KML schema.
 *
 * @author pabercrombie
 * @version $Id: KMLExportTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@RunWith(Parameterized.class)
public class KMLExportTest
{
    protected static ShapeAttributes normalShapeAttributes;
    protected static ShapeAttributes highlightShapeAttributes;

    protected List<Exportable> objectsToExport;

    public KMLExportTest(List<Exportable> objectsToExport)
    {
        this.objectsToExport = objectsToExport;
    }

    /**
     * Method to create parametrized data to drive the test.
     *
     * @return Collection of object[]. Each object[] holds parameters for one invocation of the test.
     */
    @SuppressWarnings("unchecked")
    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        normalShapeAttributes = new BasicShapeAttributes();
        normalShapeAttributes.setInteriorMaterial(Material.BLUE);
        normalShapeAttributes.setOutlineMaterial(Material.BLACK);

        highlightShapeAttributes = new BasicShapeAttributes();
        highlightShapeAttributes.setInteriorMaterial(Material.RED);
        highlightShapeAttributes.setOutlineMaterial(Material.BLACK);

        // Export a single instance of each type of shape to its own document to test the shape exporters in isolation.
        return Arrays.asList(new Object[][] {
            {Arrays.asList(makePointPlacemark())},
            {Arrays.asList(makePath())},
            {Arrays.asList(makePolygon())},
            {Arrays.asList(makeExtrudedPolygon())},
            {Arrays.asList(makeSurfacePolygon())},
            {Arrays.asList(makeScreenImage())},
            {Arrays.asList(makeSurfaceSector())},
            {Arrays.asList(makeSurfacePolyline())},
            {Arrays.asList(makeSurfaceImage())},
            {Arrays.asList(makeSurfaceImageWithLatLonQuad())},

            // Finally, test exporting all of the shapes to the same document.
            {Arrays.asList(makePointPlacemark(),
                makePath(),
                makePolygon(),
                makeExtrudedPolygon(),
                makeSurfacePolygon(),
                makeScreenImage(),
                makeSurfaceSector(),
                makeSurfacePolyline(),
                makeSurfaceImage(),
                makeSurfaceImageWithLatLonQuad())}
        });
    }

    @Test
    public void testExport() throws XMLStreamException, IOException
    {
        Writer stringWriter = new StringWriter();
        KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(stringWriter);

        for (Exportable e : this.objectsToExport)
        {
            kmlBuilder.writeObject(e);
        }
        kmlBuilder.close();

        String xmlString = stringWriter.toString();
        boolean docValid = validateDocument(xmlString);

        Assert.assertTrue("Exported document failed to validate", docValid);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKmlNotSupported() throws XMLStreamException, IOException
    {
        Pyramid pyramid = new Pyramid(Position.ZERO, 100, 100);
        pyramid.export(KMLConstants.KML_MIME_TYPE, new StringWriter());
    }

    public boolean validateDocument(String doc)
    {
        try
        {
            Source source = new StreamSource(new StringReader(doc));

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load the KML GX schema, which extends the OGC KML schema. This allows us to validate documents that use
            // the gx extensions.
            Source schemaFile = new StreamSource(new File("schemas/kml22gx.xsd"));
            Schema schema = factory.newSchema(schemaFile);

            Validator validator = schema.newValidator();
            validator.validate(source);

            return true;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("XML.ValidationFailed", e.getLocalizedMessage());
            Logging.logger().warning(message);
            return false;
        }
    }

    //////////////////////////////////////////////////////////
    // Methods to build test shapes
    //////////////////////////////////////////////////////////

    protected static PointPlacemark makePointPlacemark()
    {
        PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(37.824713, -122.370028, 0.0));

        placemark.setLabelText("Treasure Island");
        placemark.setValue(AVKey.SHORT_DESCRIPTION, "Sample placemark");
        placemark.setValue(AVKey.BALLOON_TEXT, "This is a <b>Point Placemark</b>");

        placemark.setLineEnabled(false);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        return placemark;
    }

    protected static Path makePath()
    {
        Path path = new Path();

        List<Position> positions = Arrays.asList(
            Position.fromDegrees(37.8304, -122.3720, 0),
            Position.fromDegrees(37.8293, -122.3679, 50),
            Position.fromDegrees(37.8282, -122.3710, 100));

        path.setPositions(positions);
        path.setExtrude(true);

        path.setAttributes(normalShapeAttributes);
        path.setHighlightAttributes(highlightShapeAttributes);

        path.setValue(AVKey.SHORT_DESCRIPTION, "Short description of Path");
        path.setValue(AVKey.BALLOON_TEXT, "This is a Path.");

        return path;
    }

    protected static Polygon makePolygon()
    {
        Polygon poly = new Polygon();

        List<Position> outerBoundary = Arrays.asList(
            Position.fromDegrees(37.8224479345424, -122.3739784354151, 50.0),
            Position.fromDegrees(37.82239261906633, -122.3740285701554, 50.0),
            Position.fromDegrees(37.82240608112512, -122.3744696934806, 50.0),
            Position.fromDegrees(37.82228167878964, -122.3744693163394, 50.0),
            Position.fromDegrees(37.82226619249474, -122.3739902862858, 50.0),
            Position.fromDegrees(37.82219810227204, -122.3739510452131, 50.0),
            Position.fromDegrees(37.82191990027978, -122.3742004406226, 50.0),
            Position.fromDegrees(37.82186185177756, -122.3740740264531, 50.0),
            Position.fromDegrees(37.82213350487949, -122.3738377669854, 50.0),
            Position.fromDegrees(37.82213842777661, -122.3737599855226, 50.0),
            Position.fromDegrees(37.82184815805735, -122.3735538230499, 50.0),
            Position.fromDegrees(37.82188747252212, -122.3734202823307, 50.0),
            Position.fromDegrees(37.82220302338508, -122.37362176179, 50.0),
            Position.fromDegrees(37.8222686063349, -122.3735762207482, 50.0),
            Position.fromDegrees(37.82224254303025, -122.3731468984375, 50.0),
            Position.fromDegrees(37.82237319467147, -122.3731303943743, 50.0),
            Position.fromDegrees(37.82238194814573, -122.3735637823936, 50.0),
            Position.fromDegrees(37.82244505243797, -122.3736008458059, 50.0),
            Position.fromDegrees(37.82274355652806, -122.3734009024945, 50.0),
            Position.fromDegrees(37.82280084508153, -122.3735091430554, 50.0),
            Position.fromDegrees(37.82251198652374, -122.3737489159765, 50.0),
            Position.fromDegrees(37.82251207172572, -122.3738269699774, 50.0),
            Position.fromDegrees(37.82280161524027, -122.3740332968739, 50.0),
            Position.fromDegrees(37.82275318071796, -122.3741825267907, 50.0),
            Position.fromDegrees(37.8224479345424, -122.3739784354151, 50.0));

        List<Position> innerBoundary = Arrays.asList(
            Position.fromDegrees(37.82237624346899, -122.3739179072036, 50.0),
            Position.fromDegrees(37.82226147323489, -122.3739053159649, 50.0),
            Position.fromDegrees(37.82221834573171, -122.3737889140025, 50.0),
            Position.fromDegrees(37.82226275093125, -122.3736772434448, 50.0),
            Position.fromDegrees(37.82237889526623, -122.3736727730745, 50.0),
            Position.fromDegrees(37.82243486851886, -122.3737811526564, 50.0),
            Position.fromDegrees(37.82237624346899, -122.3739179072036, 50.0));

        poly.setOuterBoundary(outerBoundary);
        poly.addInnerBoundary(innerBoundary);
        poly.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        poly.setAttributes(normalShapeAttributes);
        poly.setHighlightAttributes(highlightShapeAttributes);

        poly.setValue(AVKey.SHORT_DESCRIPTION, "Short description of Polygon");
        poly.setValue(AVKey.BALLOON_TEXT, "This is a Polygon.");

        return poly;
    }

    protected static ExtrudedPolygon makeExtrudedPolygon()
    {
        List<LatLon> outerBoundary = Arrays.asList(
            LatLon.fromDegrees(37.82149354446911, -122.3733560304957),
            LatLon.fromDegrees(37.82117090585719, -122.373137984389),
            LatLon.fromDegrees(37.82112935430661, -122.3731700720207),
            LatLon.fromDegrees(37.82113506282489, -122.3735841514635),
            LatLon.fromDegrees(37.82101164837017, -122.3735817496288),
            LatLon.fromDegrees(37.82099914948776, -122.3731815096979),
            LatLon.fromDegrees(37.82093774387397, -122.3731333692387),
            LatLon.fromDegrees(37.82064954769766, -122.3733605302921),
            LatLon.fromDegrees(37.82057406490976, -122.3732525187856),
            LatLon.fromDegrees(37.82086416575975, -122.3729848018693),
            LatLon.fromDegrees(37.82086255206195, -122.3729324258244),
            LatLon.fromDegrees(37.82057205724941, -122.3727416785599),
            LatLon.fromDegrees(37.82061568890014, -122.3725671569858),
            LatLon.fromDegrees(37.82093295537437, -122.3727709788506),
            LatLon.fromDegrees(37.82098871578818, -122.3727255708088),
            LatLon.fromDegrees(37.82097808162479, -122.3723060217839),
            LatLon.fromDegrees(37.82110007219308, -122.3722968671965),
            LatLon.fromDegrees(37.82110373259643, -122.3726983367773),
            LatLon.fromDegrees(37.82117566788379, -122.3727596225336),
            LatLon.fromDegrees(37.82145563874553, -122.3725326523252),
            LatLon.fromDegrees(37.82151892072805, -122.3726641448363),
            LatLon.fromDegrees(37.82124069262472, -122.3728982274088),
            LatLon.fromDegrees(37.82124096659101, -122.3729893447623),
            LatLon.fromDegrees(37.82153635589877, -122.3731815613555),
            LatLon.fromDegrees(37.82149354446911, -122.3733560304957));

        List<LatLon> innerBoundary = Arrays.asList(
            LatLon.fromDegrees(37.82112031091372, -122.373064499392),
            LatLon.fromDegrees(37.82100759559802, -122.3730689911348),
            LatLon.fromDegrees(37.820948040709, -122.3729462525036),
            LatLon.fromDegrees(37.8209989651238, -122.3728227939659),
            LatLon.fromDegrees(37.8211120257155, -122.3728156874424),
            LatLon.fromDegrees(37.82117285576511, -122.3729373105723),
            LatLon.fromDegrees(37.82112031091372, -122.373064499392));

        ExtrudedPolygon poly = new ExtrudedPolygon();

        poly.setOuterBoundary(outerBoundary, 50d);
        poly.addInnerBoundary(innerBoundary);
        poly.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);

        poly.setCapAttributes(normalShapeAttributes);
        poly.setSideAttributes(normalShapeAttributes);
        poly.setCapHighlightAttributes(highlightShapeAttributes);
        poly.setSideHighlightAttributes(highlightShapeAttributes);

        poly.setValue(AVKey.SHORT_DESCRIPTION, "Short description of Extruded Polygon");
        poly.setValue(AVKey.BALLOON_TEXT, "This is an Extruded Polygon.");

        return poly;
    }

    protected static SurfacePolygon makeSurfacePolygon()
    {
        List<LatLon> positions = Arrays.asList(
            LatLon.fromDegrees(37.8117, -122.3688),
            LatLon.fromDegrees(37.8098, -122.3622),
            LatLon.fromDegrees(37.8083, -122.3670),
            LatLon.fromDegrees(37.8117, -122.3688));

        SurfacePolygon poly = new SurfacePolygon();

        poly.setOuterBoundary(positions);
        poly.setAttributes(normalShapeAttributes);
        poly.setHighlightAttributes(highlightShapeAttributes);

        poly.setValue(AVKey.SHORT_DESCRIPTION, "Short description of Surface Polygon");
        poly.setValue(AVKey.BALLOON_TEXT, "This is a Surface Polygon.");

        return poly;
    }

    protected static ScreenImage makeScreenImage()
    {
        ScreenImage sc = new ScreenImage();

        sc.setImageSource("images/pushpins/plain-yellow.png");

        sc.setScreenOffset(new Offset(0.0, 0.0, AVKey.FRACTION, AVKey.FRACTION));
        sc.setImageOffset(new Offset(0.0, 0.0, AVKey.FRACTION, AVKey.FRACTION));

        Size size = new Size();
        size.setWidth(Size.EXPLICIT_DIMENSION, 100.0, AVKey.PIXELS);
        size.setHeight(Size.MAINTAIN_ASPECT_RATIO, 0.0, null);
        sc.setSize(size);

        return sc;
    }

    protected static SurfaceImage makeSurfaceImage()
    {
        String url = "http://code.google.com/apis/kml/documentation/Images/rectangle.gif";
        Sector sector = Sector.fromDegrees(60.0, 80.0, -60.0, 60.0);

        return new SurfaceImage(url, sector);
    }

    protected static SurfaceImage makeSurfaceImageWithLatLonQuad()
    {
        String url = "http://code.google.com/apis/kml/documentation/Images/rectangle.gif";

        List<LatLon> corners = Arrays.asList(
            LatLon.fromDegrees(44.160723, 81.601884),
            LatLon.fromDegrees(43.665148, 83.529902),
            LatLon.fromDegrees(44.248831, 82.947737),
            LatLon.fromDegrees(44.321015, 81.509322));

        return new SurfaceImage(url, corners);
    }

    protected static SurfaceSector makeSurfaceSector()
    {
        SurfaceSector sector = new SurfaceSector(Sector.fromDegrees(60, 80, -90, -70));
        sector.setAttributes(normalShapeAttributes);
        sector.setHighlightAttributes(highlightShapeAttributes);
        sector.setValue(AVKey.DISPLAY_NAME, "Surface Sector");
        sector.setValue(AVKey.SHORT_DESCRIPTION, "Short description of Surface Sector");
        sector.setValue(AVKey.BALLOON_TEXT, "This is a Surface Sector.");
        return sector;
    }

    protected static SurfacePolyline makeSurfacePolyline()
    {
        SurfacePolyline polyline = new SurfacePolyline();

        List<LatLon> positions = Arrays.asList(
            LatLon.fromDegrees(37.83, -122.37),
            LatLon.fromDegrees(37.82, -122.36),
            LatLon.fromDegrees(37.82, -122.37));

        polyline.setLocations(positions);

        polyline.setAttributes(normalShapeAttributes);
        polyline.setHighlightAttributes(highlightShapeAttributes);

        polyline.setValue(AVKey.DISPLAY_NAME, "Surface Polyline");
        polyline.setValue(AVKey.SHORT_DESCRIPTION, "Short description of Surface Polyline");
        polyline.setValue(AVKey.BALLOON_TEXT, "This is a Surface Polyline.");

        return polyline;
    }
}
