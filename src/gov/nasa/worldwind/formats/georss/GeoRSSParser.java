/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.georss;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: GeoRSSParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoRSSParser {
    
    public static final String GEORSS_URI = "http://www.georss.org/georss";
    public static final String GML_URI = "http://www.opengis.net/gml";
    
    public static List<Renderable> parseFragment(String fragmentString, NamespaceContext nsc) {
        return parseShapes(fixNamespaceQualification(fragmentString));
    }
    
    public static List<Renderable> parseShapes(String docString) {
        if (docString == null) {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        if (docString.length() < 1) { // avoid empty strings
            return null;
        }
        
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(docString)));
            
            List<Renderable> shapes = parseShapes(doc);
            
            if (shapes == null || shapes.size() < 1) {
                Logging.logger().log(Level.WARNING, "GeoRSS.NoShapes", docString);
                return null;
            }
            
            return shapes;
        } catch (ParserConfigurationException e) {
            String message = Logging.getMessage("GeoRSS.ParserConfigurationException");
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        } catch (IOException | SAXException e) {
            String message = Logging.getMessage("GeoRSS.IOExceptionParsing", docString);
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }
    
    private static String fixNamespaceQualification(String xmlString) {
        String lcaseString = xmlString.toLowerCase();
        StringBuffer qualifiers = new StringBuffer();
        
        if (lcaseString.contains("georss:") && !lcaseString.contains(GEORSS_URI)) {
            qualifiers.append(" xmlns:georss=\"");
            qualifiers.append(GEORSS_URI);
            qualifiers.append("\"");
        }
        
        if (lcaseString.contains("gml:") && !lcaseString.contains(GML_URI)) {
            qualifiers.append(" xmlns:gml=\"");
            qualifiers.append(GML_URI);
            qualifiers.append("\"");
        }
        
        if (qualifiers.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("<wwdummyelement");
            sb.append(qualifiers);
            sb.append(">");
            sb.append(xmlString);
            sb.append("</wwdummyelement>");
            
            return sb.toString();
        }
        
        return xmlString;
    }
    
    public static List<Renderable> parseShapes(File file) {
        if (file == null) {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(false);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            
            List<Renderable> shapes = parseShapes(doc);
            
            if (shapes == null || shapes.size() < 1) {
                Logging.logger().log(Level.WARNING, "GeoRSS.NoShapes", file.getPath());
                return null;
            }
            
            return shapes;
        } catch (ParserConfigurationException e) {
            String message = Logging.getMessage("GeoRSS.ParserConfigurationException");
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        } catch (IOException | SAXException e) {
            String message = Logging.getMessage("GeoRSS.IOExceptionParsing", file.getPath());
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }
    
    public static List<Renderable> parseShapes(Document xmlDoc) {
        if (xmlDoc == null) {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        ArrayList<Node> shapeNodes = new ArrayList<>();
        ArrayList<Node> attributeNodes = new ArrayList<>();

        // Shapes
        NodeList nodes = xmlDoc.getElementsByTagNameNS(GEORSS_URI, "where");
        if (nodes != null && nodes.getLength() > 0) {
            addNodes(shapeNodes, nodes);
        }
        
        nodes = xmlDoc.getElementsByTagNameNS(GEORSS_URI, "point");
        if (nodes != null && nodes.getLength() > 0) {
            addNodes(shapeNodes, nodes);
        }
        
        nodes = xmlDoc.getElementsByTagNameNS(GEORSS_URI, "line");
        if (nodes != null && nodes.getLength() > 0) {
            addNodes(shapeNodes, nodes);
        }
        
        nodes = xmlDoc.getElementsByTagNameNS(GEORSS_URI, "polygon");
        if (nodes != null && nodes.getLength() > 0) {
            addNodes(shapeNodes, nodes);
        }
        
        nodes = xmlDoc.getElementsByTagNameNS(GEORSS_URI, "box");
        if (nodes != null && nodes.getLength() > 0) {
            addNodes(shapeNodes, nodes);
        }

        // Attributes
        nodes = xmlDoc.getElementsByTagNameNS(GEORSS_URI, "radius");
        if (nodes != null && nodes.getLength() > 0) {
            addNodes(attributeNodes, nodes);
        }
        
        nodes = xmlDoc.getElementsByTagNameNS(GEORSS_URI, "elev");
        if (nodes != null && nodes.getLength() > 0) {
            addNodes(attributeNodes, nodes);
        }
        
        ArrayList<Renderable> shapes = new ArrayList<>();
        
        if (shapeNodes.size() < 1) {
            return null; // No warning here. Let the calling method inform of this case.
        }
        for (Node node : shapeNodes) {
            Renderable shape = null;
            String localName = node.getLocalName();
            
            switch (localName) {
                case "point":
                    shape = makePointShape(node, attributeNodes);
                    break;
                case "where":
                    shape = makeWhereShape(node);
                    break;
                case "line":
                    shape = makeLineShape(node, attributeNodes);
                    break;
                case "polygon":
                    shape = makePolygonShape(node, attributeNodes);
                    break;
                case "box":
                    shape = makeBoxShape(node, attributeNodes);
                    break;
                default:
                    break;
            }
            
            if (shape != null) {
                shapes.add(shape);
            }
        }
        
        return shapes;
    }
    
    private static void addNodes(ArrayList<Node> nodeList, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            nodeList.add(nodes.item(i));
        }
    }
    
    private static Renderable makeWhereShape(Node node) {
        Node typeNode = findChildByLocalName(node, "Polygon");
        if (typeNode != null) {
            return makeGMLPolygonShape(typeNode);
        }
        
        typeNode = findChildByLocalName(node, "Envelope");
        if (typeNode != null) {
            return makeGMLEnvelopeShape(typeNode);
        }
        
        typeNode = findChildByLocalName(node, "LineString");
        if (typeNode != null) {
            return makeGMLineStringShape(typeNode);
        }
        
        typeNode = findChildByLocalName(node, "Point");
        if (typeNode != null) {
            return makeGMLPointShape(typeNode);
        }
        
        Logging.logger().log(Level.WARNING, "GeoRSS.MissingElementContent", "where");
        return null;
    }
    
    private static Renderable makeGMLPolygonShape(Node node) {
        Node n = findChildByLocalName(node, "exterior");
        if (n == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.MissingElement", "exterior");
            return null;
        }
        
        n = findChildByLocalName(n, "LinearRing");
        if (n == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.MissingElement", "LinearRing");
            return null;
        }
        
        return makePolygonShape(n, null);
    }
    
    private static Renderable makePolygonShape(Node node, Iterable<Node> attrs) {
        String valuesString = node.getTextContent();
        if (valuesString == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.NoCoordinates", node.getLocalName());
            return null;
        }
        
        ArrayList<Double> values = getDoubleValues(valuesString);
        if (values.size() < 8 || values.size() % 2 != 0) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", node.getLocalName());
            return null;
        }
        
        ArrayList<LatLon> positions = new ArrayList<>();
        for (int i = 0; i < values.size(); i += 2) {
            positions.add(LatLon.fromDegrees(values.get(i), values.get(i + 1)));
        }
        
        double elevation = attrs != null ? getElevation(node, attrs) : 0d;
        if (elevation != 0) {
            Path path = new Path(positions, elevation);
            path.setAttributes(new BasicShapeAttributes());
            path.getAttributes().setOutlineMaterial(Material.WHITE);
            path.setPathType(AVKey.GREAT_CIRCLE);
            return path;
        } else {
            return new SurfacePolygon(positions);
        }
    }
    
    private static Renderable makeGMLEnvelopeShape(Node node) {
        Node n = findChildByLocalName(node, "lowerCorner");
        if (n == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.MissingElement", " lowerCorner");
            return null;
        }
        
        String lowerCornerString = n.getTextContent();
        if (lowerCornerString == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", " lowerCorner");
            return null;
        }
        
        n = findChildByLocalName(node, "upperCorner");
        if (n == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", " upperCorner");
            return null;
        }
        
        String upperCornerString = n.getTextContent();
        if (upperCornerString == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", " upperCorner");
            return null;
        }
        
        ArrayList<Double> lv = getDoubleValues(lowerCornerString);
        if (lv.size() != 2) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", " lowerCorner");
            return null;
        }
        
        ArrayList<Double> uv = getDoubleValues(upperCornerString);
        if (uv.size() != 2) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", " upperCorner");
            return null;
        }
        
        return new SurfaceSector(Sector.fromDegrees(lv.get(0), uv.get(0), lv.get(1), uv.get(1)));
    }
    
    private static Renderable makeBoxShape(Node node, Iterable<Node> attrs) {
        String valuesString = node.getTextContent();
        if (valuesString == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.NoCoordinates", node.getLocalName());
            return null;
        }
        
        ArrayList<Double> p = getDoubleValues(valuesString);
        if (p.size() != 4) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", node.getLocalName());
            return null;
        }
        
        double elevation = getElevation(node, attrs);
        if (elevation != 0) {
            return new Quadrilateral(LatLon.fromDegrees(p.get(0), p.get(1)),
                    LatLon.fromDegrees(p.get(2), p.get(3)), elevation);
        } else {
            return new SurfaceSector(Sector.fromDegrees(p.get(0), p.get(2), p.get(1), p.get(3)));
        }
    }
    
    private static Renderable makeGMLineStringShape(Node node) {
        Node n = findChildByLocalName(node, "posList");
        if (n == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.MissingElement", "posList");
            return null;
        }
        
        return makeLineShape(n, null);
    }
    
    private static Renderable makeLineShape(Node node, Iterable<Node> attrs) {
        String valuesString = node.getTextContent();
        if (valuesString == null) {
            Logging.logger().log(Level.WARNING, "GeoRSS.NoCoordinates", node.getLocalName());
            return null;
        }
        
        ArrayList<Double> values = getDoubleValues(valuesString);
        if (values.size() < 4) {
            Logging.logger().log(Level.WARNING, "GeoRSS.InvalidCoordinateCount", node.getLocalName());
            return null;
        }
        
        ArrayList<LatLon> positions = new ArrayList<>();
        for (int i = 0; i < values.size(); i += 2) {
            positions.add(LatLon.fromDegrees(values.get(i), values.get(i + 1)));
        }
        
        double elevation = attrs != null ? getElevation(node, attrs) : 0d;
        Path path;
        if (elevation != 0) {
            path = new Path(positions, elevation);
        } else {
            path = new Path(positions, 0);
        }
        path.setAttributes(new BasicShapeAttributes());
        path.getAttributes().setOutlineMaterial(Material.WHITE);
        path.setPathType(AVKey.GREAT_CIRCLE);
        return path;
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    private static Renderable makeGMLPointShape(Node node) {
        return null; // No shape provided for points. Expect app to use icons.
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    private static Renderable makePointShape(Node node, Iterable<Node> attrs) {
        return null; // No shape provided for points. Expect app to use icons.
    }
    
    private static Node findChildByLocalName(Node parent, String localName) {
        NodeList children = parent.getChildNodes();
        if (children == null || children.getLength() < 1) {
            return null;
        }
        
        for (int i = 0; i < children.getLength(); i++) {
            String ln = children.item(i).getLocalName();
            if (ln != null && ln.equals(localName)) {
                return children.item(i);
            }
        }
        
        return null;
    }
    
    private static Node findSiblingAttribute(String attrName, Iterable<Node> attribs, Node shapeNode) {
        for (Node attrib : attribs) {
            if (!attrib.getLocalName().equals(attrName)) {
                continue;
            }
            
            if (attrib.getParentNode().equals(shapeNode.getParentNode())) {
                return attrib;
            }
        }
        
        return null;
    }
    
    private static ArrayList<Double> getDoubleValues(String stringValues) {
        String[] tokens = stringValues.trim().split("[ ,\n]");
        if (tokens.length < 1) {
            return null;
        }
        
        ArrayList<Double> arl = new ArrayList<>();
        for (String s : tokens) {
            if (s == null || s.length() < 1) {
                continue;
            }
            
            double d;
            try {
                d = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                Logging.logger().log(Level.SEVERE, "GeoRSS.NumberFormatException", s);
                continue;
            }
            
            arl.add(d);
        }
        
        return arl;
    }
    
    private static double getElevation(Node shapeNode, Iterable<Node> attrs) {
        double elevation = 0d;
        
        Node elevNode = findSiblingAttribute("elev", attrs, shapeNode);
        if (elevNode != null) {
            ArrayList<Double> ev = getDoubleValues(elevNode.getTextContent());
            if (ev != null && ev.size() > 0) {
                elevation = ev.get(0);
            } else {
                Logging.logger().log(Level.WARNING, "GeoRSS.MissingElementContent", "elev");
            }
        }
        
        return elevation;
    }
}
