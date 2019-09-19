/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.util.VecBuffer;

import javax.xml.stream.*;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents a Shapefile record with a point shape type. Point shapes represent a single x,y coordinate pair.
 * <p>
 * Point shapes may have an optional z-coordinate or m-coordinate that accompanies the x,y coordinate pair. If a Point
 * has a z-coordinate, then <code>{@link #getZ()}</code> returns a non-<code>null</code> value. If a Point has an
 * m-coordinate, then <code>{@link #getM()}</code> returns a non-<code>null</code> value.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecordPoint.java 2303 2014-09-14 22:33:36Z dcollins $
 */
public class ShapefileRecordPoint extends ShapefileRecord
{
    protected Double z; // non-null only for Z types
    protected Double m; // non-null only for Measure types with measures specified

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
    public ShapefileRecordPoint(Shapefile shapeFile, ByteBuffer buffer)
    {
        super(shapeFile, buffer);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPointRecord()
    {
        return true;
    }

    /**
     * Get the point X and Y coordinates.
     *
     * @return the point X and Y coordinates.
     */
    public double[] getPoint()
    {
        VecBuffer vb = this.getPointBuffer(0);
        return vb.get(0, new double[vb.getCoordsPerVec()]);
    }

    /**
     * Returns the shape's Z value.
     *
     * @return the shape's Z value.
     */
    public Double getZ()
    {
        return this.z;
    }

    /**
     * Return the shape's optional measure value.
     *
     * @return the shape's measure, or null if no measure is in the record.
     */
    public Double getM()
    {
        return this.m;
    }

    /** {@inheritDoc} */
    @Override
    public double[] getBoundingRectangle()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected void doReadFromBuffer(Shapefile shapefile, ByteBuffer buffer)
    {
        // Specify that the record's points should be normalized if the shapefile itself is marked as needing
        // normalization.
        if (shapefile.isNormalizePoints())
            this.setNormalizePoints(true);

        // Store the number of parts and the number of points (always 1).
        this.numberOfParts = 1;
        this.numberOfPoints = 1;

        // Add the record's points to the Shapefile's point buffer, and record this record's part offset in the
        // Shapefile's point buffer.
        this.firstPartNumber = shapefile.addPoints(this, buffer, 1);

        // Read the optional Z value.
        if (this.isZType())
            this.readZ(buffer);

        // Read the optional measure value.
        if (this.isMeasureType())
            this.readOptionalMeasure(buffer);
    }

    /**
     * Read the record's Z value from the record buffer.
     *
     * @param buffer the record to read from.
     */
    protected void readZ(ByteBuffer buffer)
    {
        double[] zArray = ShapefileUtils.readDoubleArray(buffer, 1);
        this.z = zArray[0];
    }

    /**
     * Read any optional measure values from the record.
     *
     * @param buffer the record buffer to read from.
     */
    protected void readOptionalMeasure(ByteBuffer buffer)
    {
        // Measure values are optional.
        if (buffer.hasRemaining() && (buffer.limit() - buffer.position()) >= 8)
        {
            double[] mArray = ShapefileUtils.readDoubleArray(buffer, 1);
            this.m = mArray[0];
        }
    }

    /**
     * Export the record to KML as a {@code <Placemark>} element.
     *
     * @param xmlWriter XML writer to receive the generated KML.
     *
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @throws IOException        if an exception occurs while exporting the data.
     */
    @Override
    public void exportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        xmlWriter.writeStartElement("Placemark");
        xmlWriter.writeStartElement("name");
        xmlWriter.writeCharacters(Integer.toString(this.getRecordNumber()));
        xmlWriter.writeEndElement();

        // Write geometry
        xmlWriter.writeStartElement("Point");

        String altitudeMode = "absolute";
        double[] point = this.getPoint();
        Double z = this.getZ();

        if (z == null)
        {
            z = 0.0;
            altitudeMode = "clampToGround";
        }

        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        String coordString = String.format("%f,%f,%f", point[0], point[1], z);
        xmlWriter.writeStartElement("coordinates");
        xmlWriter.writeCharacters(coordString);
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement(); // Point
        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
    }
}
