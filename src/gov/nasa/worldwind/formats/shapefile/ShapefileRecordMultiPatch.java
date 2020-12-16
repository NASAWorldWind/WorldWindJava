/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import javax.xml.stream.*;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents a Shapefile record with a multi patch shape type. Multi-patch shapes represent a set of x,y coordinate
 * pairs.
 * <p>
 * Multi-patchs may have optional z-coordinates or m-coordinates that accompany each x,y coordinate pair. If a
 * Multi-patch has z-coordinates, then <code>{@link #getZValues()}</code> returns a non-<code>null</code> array of
 * values. If a Multi-patch has m-coordinates, then <code>{@link #getMValues()}</code> returns a non-<code>null</code>
 * array of values.
 *
 * @author tag
 * @version $Id: ShapefileRecordMultiPatch.java 2303 2014-09-14 22:33:36Z dcollins $
 */
public class ShapefileRecordMultiPatch extends ShapefileRecord {

    public enum PartType {
        TriangleStrip, TriangleFan, OuterRing, InnerRing, FirstRing, Ring
    };
    protected double[] boundingRectangle;
    protected double[] zRange; // non-null only for Z types
    protected double[][] partZValues; // non-null only for Z types
    protected double[] mRange; // will be null if no measures
    protected double[] mValues; // will be null if no measures
    // Multipatch shapefiles need a specialized type for each part which can be:

    // VALUE  PART TYPE
    // 0      Triangle Strip
    // 1      Triangle Fan
    // 2      Outer Ring
    // 3      Inner Ring
    // 4      First Ring
    // 5      Ring
    protected PartType[] partTypes;

    /**
     * Constructs a record instance from the given {@link java.nio.ByteBuffer}. The buffer's current position must be
     * the start of the record, and will be the start of the next record when the constructor returns.
     *
     * @param shapeFile the parent {@link Shapefile}.
     * @param buffer the shapefile record {@link java.nio.ByteBuffer} to read from.
     *
     * @throws IllegalArgumentException if any argument is null or otherwise invalid.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException if the record's shape type does not match that of the
     * shapefile.
     */
    public ShapefileRecordMultiPatch(Shapefile shapeFile, ByteBuffer buffer) {
        super(shapeFile, buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMultiPatchRecord() {
        return true;
    }

    /**
     * Returns an iterator over all the points X and Y coordinates for a specified part of this record. Part numbers
     * start at zero.
     *
     * @param partNumber the number of the part of this record - zero based.
     *
     * @return an {@link Iterable} over the points X and Y coordinates.
     */
    public Iterable<double[]> getPoints(int partNumber) {
        return this.getPointBuffer(partNumber).getCoords();
    }

    /**
     * Returns the shape's Z range.
     *
     * @return the shape's Z range. The range minimum is at index 0, the maximum at index 1.
     */
    public double[] getZRange() {
        return this.zRange;
    }

    /**
     * Returns the shape's Z values.
     *
     * @param partNo the number of the part of this record - zero based.
     * @return the shape's Z values.
     */
    public double[] getZValues(int partNo) {
        return this.partZValues[partNo];
    }

    public PartType[] getPartTypes() {
        return this.partTypes;
    }

    /**
     * Returns the shape's optional measure range.
     *
     * @return the shape's measure range, or null if no measures are in the record. The range minimum is at index 0, the
     * maximum at index 1.
     */
    public double[] getMRange() {
        return this.mRange;
    }

    /**
     * Returns the shape's optional measure values.
     *
     * @return the shape's measure values, or null if no measures are in the record.
     */
    public double[] getMValues() {
        return this.mValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getBoundingRectangle() {
        return this.boundingRectangle != null ? this.boundingRectangle : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doReadFromBuffer(Shapefile shapefile, ByteBuffer buffer) {
        // Read the bounding rectangle.
        Shapefile.BoundingRectangle rect = shapefile.readBoundingRectangle(buffer);
        this.boundingRectangle = rect.coords;

        // Specify that the record's points should be normalized if the bounding rectangle is normalized. Ignore the
        // shapefile's normalizePoints property to avoid normalizing records that don't need it.
        if (rect.isNormalized) {
            this.setNormalizePoints(true);
        }

        // Read the number of parts and the number of points.
        this.numberOfParts = buffer.getInt();
        this.numberOfPoints = buffer.getInt();
        this.firstPartNumber = -1;

        if (this.numberOfParts > 0 && this.numberOfPoints > 0) {
            // Read the part positions.
            int[] partPositions = ShapefileUtils.readIntArray(buffer, this.numberOfParts);
            int[] rawPartTypes = ShapefileUtils.readIntArray(buffer, this.numberOfParts);
            this.partTypes = new PartType[rawPartTypes.length];
            for (int i = 0; i < rawPartTypes.length; i++) {
                this.partTypes[i] = PartType.values()[rawPartTypes[i]];
            }
            this.partZValues = new double[this.numberOfParts][];
            for (int i = 0; i < this.numberOfParts; i++) {
                int length = (i == this.numberOfParts - 1) ? this.numberOfPoints - partPositions[i]
                        : partPositions[i + 1] - partPositions[i];
                this.partZValues[i] = new double[length];
                // Add the record's points to the Shapefile's point buffer, and record this record's part offset in the
                // Shapefile's point buffer.
                int offset = shapefile.addPoints(this, buffer, length);

                if (this.firstPartNumber < 0) {
                    this.firstPartNumber = offset;
                }
            }
        }

        double[] zValues = this.readZ(buffer);
        int zPartIndex = 0;
        for (int i = 0; i < this.numberOfParts; i++) {
            System.arraycopy(zValues, zPartIndex, this.partZValues[i], 0, this.partZValues[i].length);
            zPartIndex += this.partZValues[i].length;
        }

        // Read the optional measure value.
        if (this.isMeasureType()) {
            this.readOptionalMeasures(buffer);
        }
    }

    /**
     * Read's the shape's Z values from the record buffer.
     *
     * @param buffer the record buffer to read from.
     */
    protected double[] readZ(ByteBuffer buffer) {
        this.zRange = ShapefileUtils.readDoubleArray(buffer, 2);
        return ShapefileUtils.readDoubleArray(buffer, this.getNumberOfPoints());
    }

    /**
     * Reads any optional measure values from the record buffer.
     *
     * @param buffer the record buffer to read from.
     */
    protected void readOptionalMeasures(ByteBuffer buffer) {
        // Measure values are optional.
        if (buffer.hasRemaining() && (buffer.limit() - buffer.position()) >= (this.getNumberOfPoints() * 8)) {
            this.mRange = ShapefileUtils.readDoubleArray(buffer, 2);
            this.mValues = ShapefileUtils.readDoubleArray(buffer, this.getNumberOfPoints());
        }
    }

    /**
     * Export the record to KML as a {@code <Placemark>} element.
     *
     * @param xmlWriter XML writer to receive the generated KML.
     *
     * @throws javax.xml.stream.XMLStreamException If an exception occurs while writing the KML
     * @throws java.io.IOException If an exception occurs while exporting the data.
     */
    @Override
    public void exportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException {
        // TODO
//        xmlWriter.writeStartElement("Placemark");
//        xmlWriter.writeStartElement("name");
//        xmlWriter.writeCharacters(Integer.toString(this.getRecordNumber()));
//        xmlWriter.writeEndElement();
//
//        // Write geometry
//        xmlWriter.writeStartElement("MultiGeometry");
//
//        String altitudeMode = this.isZType() ? "absolute" : "clampToGround";
//
//        Iterable<double[]> points = this.getPoints(0);
//        double[] zValues = this.getZValues();
//
//        int index = 0;
//        for (double[] point : points) {
//            xmlWriter.writeStartElement("Point");
//
//            double z = 0.0;
//            if (zValues != null && index < zValues.length) {
//                z = zValues[index];
//            }
//
//            xmlWriter.writeStartElement("altitudeMode");
//            xmlWriter.writeCharacters(altitudeMode);
//            xmlWriter.writeEndElement();
//
//            String coordString = String.format("%f,%f,%f", point[0], point[1], z);
//            xmlWriter.writeStartElement("coordinates");
//            xmlWriter.writeCharacters(coordString);
//            xmlWriter.writeEndElement();
//
//            xmlWriter.writeEndElement(); // Point
//            index++;
//        }
//
//        xmlWriter.writeEndElement(); // MultiGeometry
//        xmlWriter.writeEndElement(); // Placemark
//
//        xmlWriter.flush();
    }
}
