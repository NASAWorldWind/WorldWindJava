/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.nitfs;
/**
 * @author Lado Garakanidze
 * @version $Id: NITFSSegmentType.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public enum NITFSSegmentType
{
    IMAGE_SEGMENT               (6, 10),
    SYMBOL_SEGMENT              (4, 6),
    LABEL_SEGMENT               (4, 3),
    TEXT_SEGMENT                (4, 5),
    DATA_EXTENSION_SEGMENT      (4, 9),
    RESERVED_EXTENSION_SEGMENT  (4, 7),
    USER_DEFINED_HEADER_SEGMENT (0, 0),
    EXTENDED_HEADER_SEGMENT     (0, 0);

    private final int fieldHeaderLengthSize;
    private final int fieldDataLengthSize;

    private NITFSSegmentType(int fieldHeaderLengthSize, int fieldDataLengthSize)
    {
        this.fieldHeaderLengthSize = fieldHeaderLengthSize;
        this.fieldDataLengthSize = fieldDataLengthSize;
    }

    public int getHeaderLengthSize() { return fieldHeaderLengthSize; }
    public int getDataLengthSize() { return fieldDataLengthSize; }
}
