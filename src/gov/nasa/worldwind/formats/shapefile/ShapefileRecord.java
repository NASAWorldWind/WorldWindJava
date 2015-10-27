/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.*;
import java.io.IOException;
import java.nio.*;
import java.util.*;

/**
 * Represents a single record of a shapefile.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecord.java 2303 2014-09-14 22:33:36Z dcollins $
 */
public abstract class ShapefileRecord
{
    protected Shapefile shapeFile;
    protected int recordNumber;
    protected int contentLengthInBytes;
    protected String shapeType;
    protected DBaseRecord attributes;
    protected int numberOfParts;
    protected int numberOfPoints;
    protected int firstPartNumber;
    /** Indicates if the record's point coordinates should be normalized. Defaults to false. */
    protected boolean normalizePoints;

    protected static final int RECORD_HEADER_LENGTH = 8;
    protected static List<String> measureTypes = new ArrayList<String>(Arrays.asList(
        Shapefile.SHAPE_POINT_M, Shapefile.SHAPE_POINT_Z,
        Shapefile.SHAPE_MULTI_POINT_M, Shapefile.SHAPE_MULTI_POINT_Z,
        Shapefile.SHAPE_POLYLINE_M, Shapefile.SHAPE_POLYLINE_Z,
        Shapefile.SHAPE_POLYGON_M, Shapefile.SHAPE_POLYGON_Z
    ));

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
    public ShapefileRecord(Shapefile shapeFile, ByteBuffer buffer)
    {
        if (shapeFile == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Save the buffer's current position.
        int pos = buffer.position();
        try
        {
            this.readFromBuffer(shapeFile, buffer);
        }
        finally
        {
            // Move to the end of the record.
            buffer.position(pos + this.contentLengthInBytes + RECORD_HEADER_LENGTH);
        }
    }

    /**
     * Returns the shapefile containing this record.
     *
     * @return the shapefile containing this record.
     */
    public Shapefile getShapeFile()
    {
        return this.shapeFile;
    }

    /**
     * Returns the zero-orgin ordinal position of the record in the shapefile.
     *
     * @return the record's ordinal position in the shapefile.
     */
    public int getRecordNumber()
    {
        return this.recordNumber;
    }

    /**
     * Returns the record's shape type.
     *
     * @return the record' shape type. See {@link Shapefile} for a list of the defined shape types.
     */
    public String getShapeType()
    {
        return this.shapeType;
    }

    /**
     * Returns the record's attributes.
     *
     * @return the record's attributes.
     */
    public DBaseRecord getAttributes()
    {
        return this.attributes;
    }

    /**
     * Specifies the shapefile's attributes.
     *
     * @param attributes the shapefile's attributes. May be null.
     */
    public void setAttributes(DBaseRecord attributes)
    {
        this.attributes = attributes;
    }

    /**
     * Returns the number of parts in the record.
     *
     * @return the number of parts in the record.
     */
    public int getNumberOfParts()
    {
        return this.numberOfParts;
    }

    /**
     * Returns the first part number in the record.
     *
     * @return the first part number in the record.
     */
    public int getFirstPartNumber()
    {
        return this.firstPartNumber;
    }

    /**
     * Returns the last part number in the record.
     *
     * @return the last part number in the record.
     */
    public int getLastPartNumber()
    {
        return this.firstPartNumber + this.numberOfParts - 1;
    }

    /**
     * Returns the number of points in the record.
     *
     * @return the number of points in the record.
     */
    public int getNumberOfPoints()
    {
        return this.numberOfPoints;
    }

    /**
     * Returns the number of points in a specified part of the record.
     *
     * @param partNumber the part number for which to return the number of points.
     *
     * @return the number of points in the specified part.
     */
    public int getNumberOfPoints(int partNumber)
    {
        if (partNumber < 0 || partNumber >= this.getNumberOfParts())
        {
            String message = Logging.getMessage("generic.indexOutOfRange", partNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int shapefilePartNumber = this.getFirstPartNumber() + partNumber;
        return this.getShapeFile().getPointBuffer().subBufferSize(shapefilePartNumber);
    }

    /**
     * Returns the {@link gov.nasa.worldwind.util.VecBuffer} holding the X and Y points of a specified part.
     *
     * @param partNumber the part for which to return the point buffer.
     *
     * @return the buffer holding the part's points. The points are ordered X0,Y0,X1,Y1,...Xn-1,Yn-1, where "n" is the
     *         number of points in the part.
     */
    public VecBuffer getPointBuffer(int partNumber)
    {
        if (partNumber < 0 || partNumber >= this.getNumberOfParts())
        {
            String message = Logging.getMessage("generic.indexOutOfRange", partNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int shapefilePartNumber = this.getFirstPartNumber() + partNumber;
        return this.getShapeFile().getPointBuffer().subBuffer(shapefilePartNumber);
    }

    /**
     * Returns the {@link gov.nasa.worldwind.util.CompoundVecBuffer} holding all the X and Y points for this record. The
     * returned buffer contains one sub-buffer for each of this record's parts. The coordinates for each part are
     * referenced by invoking {@link gov.nasa.worldwind.util.CompoundVecBuffer#subBuffer(int)}, where the index is one
     * of this record's part IDs, starting with 0 and ending with <code>{@link #getNumberOfParts()} - 1</code>
     * (inclusive).
     *
     * @return a CompoundVecBuffer that holds this record's coordinate data.
     */
    public CompoundVecBuffer getCompoundPointBuffer()
    {
        return this.getShapeFile().getPointBuffer().slice(this.getFirstPartNumber(), this.getLastPartNumber());
    }

    /**
     * Returns a four-element array containing this record's bounding rectangle, or null if this record has no bounding
     * rectangle.
     * <p/>
     * The returned array is ordered as follows: minimum Y, maximum Y, minimum X, and maximum X. If the Shapefile's
     * coordinate system is geographic, the elements can be interpreted as angular degrees in the order minimum
     * latitude, maximum latitude, minimum longitude, and maximum longitude.
     *
     * @return the record's bounding rectangle, or null to indicate that this record does not have a bounding
     *         rectangle.
     */
    public abstract double[] getBoundingRectangle();

    /**
     * Reads and parses subclass-specific contents of a shapefile record from a specified buffer. The buffer's current
     * position must be the start of the subclass' unique contents and will be the start of the next record when the
     * constructor returns.
     *
     * @param shapefile the containing {@link Shapefile}.
     * @param buffer    the shapefile record {@link java.nio.ByteBuffer} to read from.
     */
    protected abstract void doReadFromBuffer(Shapefile shapefile, ByteBuffer buffer);

    /**
     * Reads and parses the contents of a shapefile record from a specified buffer. The buffer's current position must
     * be the start of the record and will be the start of the next record when the constructor returns.
     *
     * @param shapefile the containing {@link Shapefile}.
     * @param buffer    the shapefile record {@link java.nio.ByteBuffer} to read from.
     */
    protected void readFromBuffer(Shapefile shapefile, ByteBuffer buffer)
    {
        // Read record number and record length - big endian.
        buffer.order(ByteOrder.BIG_ENDIAN);
        this.recordNumber = buffer.getInt();
        this.contentLengthInBytes = buffer.getInt() * 2;

        // Read shape type - little endian
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int type = buffer.getInt();
        String shapeType = shapefile.getShapeType(type);
        this.validateShapeType(shapefile, shapeType);

        this.shapeType = shapeType;
        this.shapeFile = shapefile;

        this.doReadFromBuffer(shapefile, buffer);
    }

    /**
     * Verifies that the record's shape type matches the expected one, typically that of the shapefile. All non-null
     * records in a Shapefile must be of the same type. Throws an exception if the types do not match and the shape type
     * is not <code>{@link Shapefile#SHAPE_NULL}</code>. Records of type <code>SHAPE_NULL</code> are always valid, and
     * may appear in any Shapefile.
     * <p/>
     * For details, see the ESRI Shapefile specification at <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf"/>,
     * pages 4 and 5.
     *
     * @param shapefile the shapefile.
     * @param shapeType the record's shape type.
     *
     * @throws WWRuntimeException       if the shape types do not match.
     * @throws IllegalArgumentException if the specified shape type is null.
     */
    protected void validateShapeType(Shapefile shapefile, String shapeType)
    {
        if (shapeType == null)
        {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!shapeType.equals(shapefile.getShapeType()) && !shapeType.equals(Shapefile.SHAPE_NULL))
        {
            String message = Logging.getMessage("SHP.UnsupportedShapeType", shapeType);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    /**
     * Indicates whether the record is a shape type capable of containing optional measure values. Does not indicate
     * whether the record actually contains measure values.
     *
     * @return true if the record may contain measure values.
     */
    protected boolean isMeasureType()
    {
        return Shapefile.isMeasureType(this.getShapeType());
    }

    /**
     * Indicates whether the record is a shape type containing Z values.
     *
     * @return true if the record is a type containing Z values.
     */
    protected boolean isZType()
    {
        return Shapefile.isZType(this.getShapeType());
    }

    /**
     * Indicates whether this is a null record. When true, this record may be cast to a ShapefileRecordNull by calling
     * {@link #asNullRecord()}.
     *
     * @return true if this is a null record, otherwise false.
     */
    public boolean isNullRecord()
    {
        return false;
    }

    /**
     * Indicates whether this is a point record. When true, this record may be cast to a ShapefileRecordPoint by calling
     * {@link #asPointRecord()}.
     *
     * @return true if this is a point record, otherwise false.
     */
    public boolean isPointRecord()
    {
        return false;
    }

    /**
     * Indicates whether this is a multi point record. When true, this record may be cast to a ShapefileRecordMultiPoint
     * by calling {@link #asPointRecord()}.
     *
     * @return true if this is a multi point record, otherwise false.
     */
    public boolean isMultiPointRecord()
    {
        return false;
    }

    /**
     * Indicates whether this is a polyline record. When true, this record may be cast to a ShapefileRecordPolyline by
     * calling {@link #asPolylineRecord()}.
     *
     * @return true if this is a polyline record, otherwise false.
     */
    public boolean isPolylineRecord()
    {
        return false;
    }

    /**
     * Indicates whether this is a polygon record. When true, this record may be cast to a ShapefileRecordPolygon by
     * calling {@link #asPolygonRecord()}.
     *
     * @return true if this is a polygon record, otherwise false.
     */
    public boolean isPolygonRecord()
    {
        return false;
    }

    /**
     * Returns this record as a ShapefileRecordNull. This results in a class cast exception if this is not a null
     * record. Check this record's type using {@link #isNullRecord()} prior to calling this method.
     *
     * @return this record cast as a ShapefileRecordNull.
     */
    public ShapefileRecordNull asNullRecord()
    {
        return (ShapefileRecordNull) this;
    }

    /**
     * Returns this record as a ShapefileRecordPoint. This results in a class cast exception if this is not a point
     * record. Check this record's type using {@link #isPointRecord()} prior to calling this method.
     *
     * @return this record cast as a ShapefileRecordPoint.
     */
    public ShapefileRecordPoint asPointRecord()
    {
        return (ShapefileRecordPoint) this;
    }

    /**
     * Returns this record as a ShapefileRecordMultiPoint. This results in a class cast exception if this is not a multi
     * point record. Check this record's type using {@link #isMultiPointRecord()} prior to calling this method.
     *
     * @return this record cast as a ShapefileRecordMultiPoint.
     */
    public ShapefileRecordMultiPoint asMultiPointRecord()
    {
        return (ShapefileRecordMultiPoint) this;
    }

    /**
     * Returns this record as a ShapefileRecordPolyline. This results in a class cast exception if this is not a
     * polyline record. Check this record's type using {@link #isPolylineRecord()} prior to calling this method.
     *
     * @return this record cast as a ShapefileRecordPolyline.
     */
    public ShapefileRecordPolyline asPolylineRecord()
    {
        return (ShapefileRecordPolyline) this;
    }

    /**
     * Returns this record as a ShapefileRecordPolygon. This results in a class cast exception if this is not a polygon
     * record. Check this record's type using {@link #isPolygonRecord()} prior to calling this method.
     *
     * @return this record cast as a ShapefileRecordPolygon.
     */
    public ShapefileRecordPolygon asPolygonRecord()
    {
        return (ShapefileRecordPolygon) this;
    }

    /**
     * Returns whether the record's point coordinates should be normalized.
     *
     * @return <code>true</code> if the record's points should be normalized; <code>false</code> otherwise.
     */
    public boolean isNormalizePoints()
    {
        return this.normalizePoints;
    }

    /**
     * Specifies if the record's point coordinates should be normalized. Defaults to <code>false</code>.
     *
     * @param normalizePoints <code>true</code> if the record's points should be normalized; <code>false</code>
     *                        otherwise.
     */
    public void setNormalizePoints(boolean normalizePoints)
    {
        this.normalizePoints = normalizePoints;
    }

    public void exportAsXML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Record");

        xmlWriter.writeAttribute("id", Integer.toString(this.getRecordNumber()));
        xmlWriter.writeAttribute("shape", this.getShapeType().substring(this.getShapeType().lastIndexOf("Shape") + 5));
        xmlWriter.writeAttribute("parts", Integer.toString(this.getNumberOfParts()));
        xmlWriter.writeAttribute("points", Integer.toString(this.getNumberOfPoints()));
        xmlWriter.writeCharacters("\n");

        for (Map.Entry<String, Object> a : this.getAttributes().getEntries())
        {
            xmlWriter.writeStartElement("Attribute");

            xmlWriter.writeAttribute("name", a.getKey() != null ? a.getKey().toString() : "");
            xmlWriter.writeAttribute("value", a.getValue() != null ? a.getValue().toString() : "");

            xmlWriter.writeEndElement(); // Attribute
            xmlWriter.writeCharacters("\n");
        }

        if (this.getNumberOfParts() > 0)
        {
            VecBuffer vb = this.getPointBuffer(0);
            for (LatLon ll : vb.getLocations())
            {
                xmlWriter.writeStartElement("Point");
                xmlWriter.writeAttribute("x", Double.toString(ll.getLatitude().degrees));
                xmlWriter.writeAttribute("y", Double.toString(ll.getLongitude().degrees));
                xmlWriter.writeEndElement(); // Point
                xmlWriter.writeCharacters("\n");
            }
        }

        // TODO: export record-type specific fields

        xmlWriter.writeEndElement(); // Record
    }

    /**
     * Export the record as KML. This implementation does nothing; subclasses may override this method to provide KML
     * export.
     *
     * @param xmlWriter Writer to receive KML.
     *
     * @throws IOException        If an exception occurs while writing the KML
     * @throws XMLStreamException If an exception occurs while exporting the data.
     */
    public void exportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
    }

    public void printInfo(boolean printCoordinates)
    {
        System.out.printf("%d, %s: %d parts, %d points", this.getRecordNumber(), this.getShapeType(),
            this.getNumberOfParts(), this.getNumberOfPoints());
        for (Map.Entry<String, Object> a : this.getAttributes().getEntries())
        {
            if (a.getKey() != null)
                System.out.printf(", %s", a.getKey());
            if (a.getValue() != null)
                System.out.printf(", %s", a.getValue());
        }
        System.out.println();

        System.out.print("\tAttributes: ");
        for (Map.Entry<String, Object> entry : this.getAttributes().getEntries())
        {
            System.out.printf("%s = %s, ", entry.getKey(), entry.getValue());
        }
        System.out.println();

        if (!printCoordinates)
            return;

        VecBuffer vb = this.getPointBuffer(0);
        for (LatLon ll : vb.getLocations())
        {
            System.out.printf("\t%f, %f\n", ll.getLatitude().degrees, ll.getLongitude().degrees);
        }
    }
}
