/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.xml.stream.*;
import java.io.IOException;

/**
 * Collection of utilities methods for generating KML.
 *
 * @author tag
 * @version $Id: KMLExportUtil.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLExportUtil
{
    /**
     * Convert a WorldWind altitude mode to a KML altitude mode.
     *
     * @param altitudeMode Altitude mode to convert.
     *
     * @return The KML altitude mode that corresponds to {@code altitudeMode}.
     *
     * @throws IllegalArgumentException If {@code altitudeMode} is not a valid WorldWind altitude mode.
     */
    public static String kmlAltitudeMode(int altitudeMode)
    {
        final String kmlAltitude;
        switch (altitudeMode)
        {
            case WorldWind.CLAMP_TO_GROUND:
                kmlAltitude = "clampToGround";
                break;
            case WorldWind.RELATIVE_TO_GROUND:
                kmlAltitude = "relativeToGround";
                break;
            case WorldWind.ABSOLUTE:
            case WorldWind.CONSTANT:
                kmlAltitude = "absolute";
                break;
            default:
                String message = Logging.getMessage("generic.InvalidAltitudeMode", altitudeMode);
                Logging.logger().warning(message);
                throw new IllegalArgumentException(message);
        }

        return kmlAltitude;
    }

    /**
     * Export ShapeAttributes as KML Pair element in a StyleMap. This method assumes that the StyleMap tag has already
     * been written; it writes the Pair tag.
     *
     * @param xmlWriter  Writer to receive the Style element.
     * @param styleType  The type of style: normal or highlight. Value should match either {@link KMLConstants#NORMAL}
     *                   or {@link KMLConstants#HIGHLIGHT}
     * @param attributes Attributes to export. The method takes no action if this parameter is null.
     *
     * @throws javax.xml.stream.XMLStreamException
     *                             if exception occurs writing XML.
     * @throws java.io.IOException if exception occurs exporting data.
     */
    public static void exportAttributesAsKML(XMLStreamWriter xmlWriter, String styleType, ShapeAttributes attributes)
        throws XMLStreamException, IOException
    {
        if (attributes != null)
        {
            xmlWriter.writeStartElement("Pair");
            xmlWriter.writeStartElement("key");
            xmlWriter.writeCharacters(styleType);
            xmlWriter.writeEndElement();

            attributes.export(KMLConstants.KML_MIME_TYPE, xmlWriter);
            xmlWriter.writeEndElement(); // Pair
        }
    }

    /**
     * Export an {@link Offset} as a KML element.
     *
     * @param xmlWriter Writer to receive the Style element.
     * @param offset    The offset to export. If {@code offset} is null, nothing is written to the stream.
     * @param tagName   The name of the KML tag to create.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if exception occurs writing XML.
     */
    public static void exportOffset(XMLStreamWriter xmlWriter, Offset offset, String tagName) throws XMLStreamException
    {
        if (offset != null)
        {
            xmlWriter.writeStartElement(tagName);
            xmlWriter.writeAttribute("x", Double.toString(offset.getX()));
            xmlWriter.writeAttribute("y", Double.toString(offset.getY()));
            xmlWriter.writeAttribute("xunits", KMLUtil.wwUnitsToKMLUnits(offset.getXUnits()));
            xmlWriter.writeAttribute("yunits", KMLUtil.wwUnitsToKMLUnits(offset.getYUnits()));
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export a {@link Size} as a KML element.
     *
     * @param xmlWriter Writer to receive the Style element.
     * @param dimension The dimension to export. If {@code dimension} is null, nothing is written to the stream.
     * @param tagName   The name of the KML tag to create.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if exception occurs writing XML.
     */
    public static void exportDimension(XMLStreamWriter xmlWriter, Size dimension, String tagName)
        throws XMLStreamException
    {
        if (dimension != null)
        {
            xmlWriter.writeStartElement(tagName);
            exportDimensionAttributes("x", xmlWriter, dimension.getWidthMode(), dimension.getWidth(),
                dimension.getWidthUnits());
            exportDimensionAttributes("y", xmlWriter, dimension.getHeightMode(), dimension.getHeight(),
                dimension.getHeightUnits());
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the attributes of a Size. This method assumes that the dimension start tag has already been
     * written to the stream.
     *
     * @param axes      "x" or "y".
     * @param xmlWriter Writer that will received exported data.
     * @param sizeMode  Size mode for this dimension.
     * @param size      The size of the dimension.
     * @param units     Units of {@code size}.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if exception occurs writing XML.
     */
    private static void exportDimensionAttributes(String axes, XMLStreamWriter xmlWriter, String sizeMode, double size,
        String units)
        throws XMLStreamException
    {
        if (Size.NATIVE_DIMENSION.equals(sizeMode))
        {
            xmlWriter.writeAttribute(axes, "-1");
        }
        else if (Size.MAINTAIN_ASPECT_RATIO.equals(sizeMode))
            xmlWriter.writeAttribute(axes, "0");
        else if (Size.EXPLICIT_DIMENSION.equals(sizeMode))
        {
            xmlWriter.writeAttribute(axes, Double.toString(size));
            xmlWriter.writeAttribute(axes + "units", KMLUtil.wwUnitsToKMLUnits(units));
        }
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnknownSizeMode", sizeMode));
        }
    }

    /**
     * Strip the "0X" prefix from a hex string.
     *
     * @param hexString String to manipulate.
     *
     * @return The portion of {@code hexString} after the 0X. For example: "0X00FF00" => "00FF00". If the string does
     *         not begin with 0X, {@code hexString} is returned. The comparison is not case sensitive.
     */
    public static String stripHexPrefix(String hexString)
    {
        if (hexString.startsWith("0x") || hexString.startsWith("0X"))
            return hexString.substring(2);
        else
            return hexString;
    }

    /**
     * Export the boundary of a polygon as a KML LinearRing.
     *
     * @param xmlWriter Writer to receive generated XML.
     * @param boundary  Boundary to export.
     * @param altitude  Altitude of the points in the ring.
     *
     * @throws XMLStreamException if exception occurs writing XML.
     */
    public static void exportBoundaryAsLinearRing(XMLStreamWriter xmlWriter, Iterable<? extends LatLon> boundary,
        Double altitude)
        throws XMLStreamException
    {
        String altitudeString = null;
        if (altitude != null)
        {
            altitudeString = Double.toString(altitude);
        }

        xmlWriter.writeStartElement("LinearRing");
        xmlWriter.writeStartElement("coordinates");
        for (LatLon location : boundary)
        {
            xmlWriter.writeCharacters(Double.toString(location.getLongitude().getDegrees()));
            xmlWriter.writeCharacters(",");
            xmlWriter.writeCharacters(Double.toString(location.getLatitude().getDegrees()));

            if (altitudeString != null)
            {
                xmlWriter.writeCharacters(",");
                xmlWriter.writeCharacters(altitudeString);
            }

            xmlWriter.writeCharacters(" ");
        }
        xmlWriter.writeEndElement();
        xmlWriter.writeEndElement(); // LinearRing
    }

    /**
     * Convert a boolean to binary string "1" or "0".
     *
     * @param value Value to convert.
     *
     * @return "1" if {@code value} is true, otherwise "0".
     */
    public static String kmlBoolean(boolean value)
    {
        return value ? "1" : "0";
    }
}
