/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Represents a Shapefile record with a polygon shape type. Polygon shapes represent an connected sequence of four or
 * more x,y coordinate pairs that form a closed loop. Polygon shapes may contain multiple rings, where each ring is a
 * closed loop of four or more points. Rings defining a filled part of a polygon have a clockwise winding order, while
 * rings defining holes in the polygon have a counter-clockwise winding order.
 * <p>
 * Polygons may have optional z-coordinates or m-coordinates that accompany each coordinate pair. If a Polygon has
 * z-coordinates, then <code>{@link #getZValues()}</code> returns a non-<code>null</code> array of values.  If a Polygon
 * has m-coordinates, then <code>{@link #getMValues()}</code> returns a non-<code>null</code> array of values.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecordPolygon.java 2303 2014-09-14 22:33:36Z dcollins $
 */
public class ShapefileRecordPolygon extends ShapefileRecordPolyline
{
    /**
     * Constructs a record instance from the given {@link java.nio.ByteBuffer}. The buffer's current position must be
     * the start of the record, and will be the start of the next record when the constructor returns.
     *
     * @param shapeFile the parent {@link Shapefile}.
     * @param buffer    the shapefile record {@link java.nio.ByteBuffer} to read from.
     *
     * @throws IllegalArgumentException if any argument is null or otherwise invalid.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if the record's shape type does not match that of the shapefile.
     */
    public ShapefileRecordPolygon(Shapefile shapeFile, ByteBuffer buffer)
    {
        super(shapeFile, buffer);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPolygonRecord()
    {
        return true;
    }

    /**
     * Export the record to KML as a {@code <Placemark>} element. If the polygon has a "height" attribute it will be
     * exported as an extruded polygon.
     *
     * @param xmlWriter XML writer to receive the generated KML.
     *
     * @throws javax.xml.stream.XMLStreamException
     *                             If an exception occurs while writing the KML
     * @throws java.io.IOException If an exception occurs while exporting the data.
     */
    @Override
    public void exportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        Iterable<? extends LatLon> outerBoundary = null;
        List<Iterable<? extends LatLon>> innerBoundaries = new ArrayList<Iterable<? extends LatLon>>();

        // If the polygon has a "height" attribute, export as an extruded polygon.
        Double height = ShapefileUtils.extractHeightAttribute(this);

        for (int i = 0; i < this.getNumberOfParts(); i++)
        {
            // Although the shapefile spec says that inner and outer boundaries can be listed in any order, it's
            // assumed here that inner boundaries are at least listed adjacent to their outer boundary, either
            // before or after it. The below code accumulates inner boundaries into the polygon until an
            // outer boundary comes along. If the outer boundary comes before the inner boundaries, the inner
            // boundaries are added to the polygon until another outer boundary comes along, at which point a new
            // polygon is started.

            VecBuffer buffer = this.getCompoundPointBuffer().subBuffer(i);
            if (WWMath.computeWindingOrderOfLocations(buffer.getLocations()).equals(AVKey.CLOCKWISE))
            {
                if (outerBoundary == null)
                {
                    outerBoundary = buffer.getLocations();
                }
                else
                {
                    this.exportPolygonAsKML(xmlWriter, outerBoundary, innerBoundaries, height);

                    outerBoundary = this.getCompoundPointBuffer().getLocations();
                    innerBoundaries.clear();
                }
            }
            else
            {
                innerBoundaries.add(buffer.getLocations());
            }
        }

        if (outerBoundary != null && outerBoundary.iterator().hasNext())
        {
            this.exportPolygonAsKML(xmlWriter, outerBoundary, innerBoundaries, height);
        }
    }

    protected void exportPolygonAsKML(XMLStreamWriter xmlWriter, Iterable<? extends LatLon> outerBoundary,
        List<Iterable<? extends LatLon>> innerBoundaries, Double height) throws IOException, XMLStreamException
    {
        xmlWriter.writeStartElement("Placemark");
        xmlWriter.writeStartElement("name");
        xmlWriter.writeCharacters(Integer.toString(this.getRecordNumber()));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("Polygon");

        String altitudeMode;
        if (height != null)
        {
            xmlWriter.writeStartElement("extrude");
            xmlWriter.writeCharacters("1");
            xmlWriter.writeEndElement();

            altitudeMode = "absolute";
        }
        else
        {
            altitudeMode = "clampToGround";
            height = 0.0;
        }

        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("outerBoundaryIs");
        KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, outerBoundary, height);
        xmlWriter.writeEndElement(); // outerBoundaryIs

        for (Iterable<? extends LatLon> innerBoundary : innerBoundaries)
        {
            xmlWriter.writeStartElement("innerBoundaryIs");
            KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, innerBoundary, height);
            xmlWriter.writeEndElement(); // innerBoundaryIs
        }

        xmlWriter.writeEndElement(); // Polygon
        xmlWriter.writeEndElement(); // Placemark
        xmlWriter.flush();
    }
}
