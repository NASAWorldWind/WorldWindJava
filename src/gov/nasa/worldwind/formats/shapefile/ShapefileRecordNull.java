/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.shapefile;

import java.nio.ByteBuffer;

/**
 * Represents a Shapefile record with a <strong>null</strong> shape type. Null shape records may have attributes but
 * have no geometric data, and are therefore typically used as placeholders.
 *
 * @author tag
 * @version $Id: ShapefileRecordNull.java 2303 2014-09-14 22:33:36Z dcollins $
 */
public class ShapefileRecordNull extends ShapefileRecord
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
    public ShapefileRecordNull(Shapefile shapeFile, ByteBuffer buffer)
    {
        super(shapeFile, buffer);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNullRecord()
    {
        return true;
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
        this.numberOfParts = 0;
        this.numberOfPoints = 0;
    }
}
