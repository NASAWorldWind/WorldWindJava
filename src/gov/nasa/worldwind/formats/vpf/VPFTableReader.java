/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.nio.*;
import java.util.ArrayList;

/**
 * DIGEST Part 2, Annex C.2.2.1.2 and C.2.3.1.1
 *
 * @author dcollins
 * @version $Id: VPFTableReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFTableReader
{
    public VPFTableReader()
    {
    }

    public VPFBufferedRecordData read(File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            ByteBuffer buffer = this.readFileToBuffer(file);
            return this.doRead(file, buffer);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("VPF.ExceptionAttemptingToReadTable", file.getPath());
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    protected ByteBuffer readFileToBuffer(File file) throws IOException
    {
        ByteBuffer buffer = WWIO.readFileToBuffer(file, true); // Read VPF table to a direct ByteBuffer.
        buffer.order(ByteOrder.LITTLE_ENDIAN); // Default to least significant byte first order.
        return buffer;
    }

    protected VPFBufferedRecordData doRead(File file, ByteBuffer buffer)
    {
        // Read the table header.
        Header header = this.readHeader(buffer);
        // Set the byte ordering to the ordering specified by the table header.
        buffer.order(header.byteOrder);

        RecordIndex recordIndex = null;
        // Attempt to find a variable-length record index according to the file naming convention in
        // DIGEST Part 2 Annex C.2.3.1.2
        File recordIndexFile = new File(file.getParent(), getRecordIndexFilename(file.getName()));
        if (recordIndexFile.exists())
            recordIndex = this.readRecordIndex(recordIndexFile);
        // If the record index is null, then attempt to compute it from the header's column definitions.
        if (recordIndex == null)
            recordIndex = this.computeRecordIndex(buffer, header);
        // If the record index is still null, then we the column definitions are variable length, and there is no
        // variable-length record index associated with this table. In this case, we cannot read the table body.
        if (recordIndex == null)
        {
            String message = Logging.getMessage("VPF.VariableLengthIndexFileMissing");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        // Read the table record data.
        return this.readRecordData(buffer, header.columns, recordIndex);
    }

    //**************************************************************//
    //********************  Header  ********************************//
    //**************************************************************//

    /** MIL-STD-2407, section 5.4.1.1 */
    protected static class Header
    {
        public int length;
        public ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN; // Default to least significant byte first order.
        public String description;
        public String narrativeTableName;
        public Column[] columns;

        public Header()
        {
        }
    }

    /** MIL-STD-2407, section 5.4.1.1 */
    public class Column
    {
        public final String name;
        public String dataType;
        public int numElements;
        public String keyType;
        public String description;
        public String valueDescriptionTableName;
        public String thematicIndexName;
        public String narrativeTableName;
        public VPFDataBuffer dataBuffer;

        public Column(String name)
        {
            this.name = name;
        }

        public int getFieldLength()
        {
            if (this.isVariableLengthField())
                return -1;

            VPFDataType type = VPFDataType.fromTypeName(this.dataType);
            return this.numElements * type.getFieldLength();
        }

        public boolean isVariableLengthField()
        {
            VPFDataType type = VPFDataType.fromTypeName(this.dataType);
            return (this.numElements < 0) || type.isVariableLength();
        }

        public boolean isPrimaryKey()
        {
            return this.keyType.equals(VPFConstants.PRIMARY_KEY);
        }

        public boolean isUniqueKey()
        {
            return this.keyType.equals(VPFConstants.UNIQUE_KEY);
        }

        public boolean isNonUniqueKey()
        {
            return this.keyType.equals(VPFConstants.NON_UNIQUE_KEY);
        }
    }

    protected Header readHeader(ByteBuffer buffer)
    {
        int offset = buffer.position();
        int length = buffer.getInt();

        Header header = new Header();
        header.length = length;
        header.byteOrder = ByteOrder.LITTLE_ENDIAN; // Default to least significant byte first order.

        if (length == 0)
        {
            return header;
        }

        // Read the byte order character.
        String s = VPFUtils.readDelimitedText(buffer, ';');
        if (s != null && s.equalsIgnoreCase("M"))
            header.byteOrder = ByteOrder.BIG_ENDIAN;

        // Read the table description string.
        s = VPFUtils.readDelimitedText(buffer, ';');
        if (s != null)
            header.description = s.trim();

        // Read the narrative table name.
        s = VPFUtils.readDelimitedText(buffer, ';');
        if (s != null && s.charAt(0) != '-')
            header.narrativeTableName = s.trim();

        ArrayList<Column> columnList = new ArrayList<Column>();

        while (buffer.position() < (offset + length))
        {
            Column col = this.readColumnDescription(buffer);
            columnList.add(col);
        }

        header.columns = new Column[columnList.size()];
        columnList.toArray(header.columns);

        // Consume any remaining text, up to the column component delimiter ';'.
        VPFUtils.readDelimitedText(buffer, ';');

        return header;
    }

    protected Column readColumnDescription(ByteBuffer buffer)
    {
        String s = VPFUtils.readDelimitedText(buffer, '=');
        if (s == null)
        {
            String message = Logging.getMessage("VPF.MissingColumnName");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        Column col = new Column(s);

        s = VPFUtils.readDelimitedText(buffer, ',');
        if (s != null)
            col.dataType = s;

        s = VPFUtils.readDelimitedText(buffer, ',');
        if (s != null)
            col.numElements = parseNumElements(s);

        s = VPFUtils.readDelimitedText(buffer, ',');
        if (s != null)
            col.keyType = s;

        s = VPFUtils.readDelimitedText(buffer, ',');
        if (s != null)
            col.description = s;

        s = VPFUtils.readDelimitedText(buffer, ',');
        if (s != null)
            col.valueDescriptionTableName = s;

        s = VPFUtils.readDelimitedText(buffer, ',');
        if (s != null)
            col.thematicIndexName = s;

        // Consume any remaining text, up to the sub column delimiter ':'.
        s = VPFUtils.readDelimitedText(buffer, ':');
        if (s != null)
        {
            int pos = s.indexOf(",");
            if (pos >= 0)
            {
                s = s.substring(0, pos);
                col.narrativeTableName = s;
            }
        }

        return col;
    }

    protected static int parseNumElements(String numElements)
    {
        // "*" denotes a field with variable length.
        if (numElements == null || numElements.equals("*"))
            return -1;

        Integer i = WWUtil.convertStringToInteger(numElements);
        return (i != null) ? i : -1;
    }

    //**************************************************************//
    //********************  Record Data  ***************************//
    //**************************************************************//

    protected interface RecordDataReader
    {
        VPFDataBuffer getDataBuffer();

        void read(ByteBuffer byteBuffer);
    }

    protected abstract static class AbstractDataReader implements RecordDataReader
    {
        protected VPFDataBuffer dataBuffer;

        public AbstractDataReader(VPFDataBuffer dataBuffer)
        {
            this.dataBuffer = dataBuffer;
        }

        public VPFDataBuffer getDataBuffer()
        {
            return this.dataBuffer;
        }
    }

    protected static class FixedLengthDataReader extends AbstractDataReader
    {
        protected int numElements;

        public FixedLengthDataReader(VPFDataBuffer dataBuffer, int numElements)
        {
            super(dataBuffer);
            this.numElements = numElements;
        }

        public void read(ByteBuffer byteBuffer)
        {
            this.dataBuffer.read(byteBuffer, this.numElements);
        }
    }

    protected static class VariableLengthDataReader extends AbstractDataReader
    {
        public VariableLengthDataReader(VPFDataBuffer dataBuffer)
        {
            super(dataBuffer);
        }

        public void read(ByteBuffer byteBuffer)
        {
            this.dataBuffer.read(byteBuffer);
        }
    }

    protected VPFBufferedRecordData readRecordData(ByteBuffer byteBuffer, Column[] columns, RecordIndex recordIndex)
    {
        int numRows = recordIndex.numEntries;
        int numColumns = columns.length;

        // Create data readers for each column.
        RecordDataReader[] readers = new RecordDataReader[numColumns];
        for (int col = 0; col < numColumns; col++)
        {
            VPFDataType type = VPFDataType.fromTypeName(columns[col].dataType);
            VPFDataBuffer dataBuffer = type.createDataBuffer(numRows, columns[col].numElements);
            readers[col] = columns[col].isVariableLengthField() ?
                new VariableLengthDataReader(dataBuffer)
                : new FixedLengthDataReader(dataBuffer, columns[col].numElements);
        }

        // Read the column data associated with each row.
        for (int row = 0; row < numRows; row++)
        {
            byteBuffer.position(recordIndex.entries[row].offset);

            for (int col = 0; col < numColumns; col++)
            {
                readers[col].read(byteBuffer);
            }
        }

        VPFBufferedRecordData recordData = new VPFBufferedRecordData();
        recordData.setNumRecords(numRows);

        // Set the record data buffer associated with each column.
        for (int col = 0; col < numColumns; col++)
        {
            recordData.setRecordData(columns[col].name, readers[col].getDataBuffer());

            // Compute an index for any columns which are identified as primary keys or unique keys.
            if (!columns[col].name.equals(VPFConstants.ID) &&
                (columns[col].name.equals(VPFConstants.PRIMARY_KEY) ||
                    columns[col].name.equals(VPFConstants.UNIQUE_KEY)))
            {
                recordData.buildRecordIndex(columns[col].name);
            }
        }

        return recordData;
    }

    //**************************************************************//
    //********************  Record Index  **************************//
    //**************************************************************//

    public static class RecordIndex
    {
        public static class Entry
        {
            public int offset;
            public int length;

            public Entry(int offset, int length)
            {
                this.offset = offset;
                this.length = length;
            }
        }

        public int numEntries;
        public int headerLength;
        public Entry[] entries;

        public RecordIndex()
        {
        }
    }

    /**
     * Returns the name of the Variable-length Index File associated with a specified table name. Note that this does
     * not determine whether or not the index exists, it simply returns the abstract file name of the index. <br> See
     * MIL-STD-2407, section 5.3.1.2 DIGEST Part 2, Annex C.2.3.1.2
     *
     * @param tableName the table name to return an index name for.
     *
     * @return the name of a variable-length index file associated with the table name.,
     */
    protected static String getRecordIndexFilename(String tableName)
    {
        boolean isFcs = tableName.equalsIgnoreCase(VPFConstants.FEATURE_CLASS_SCHEMA_TABLE);

        StringBuilder sb = new StringBuilder();

        int len = tableName.length();
        sb.append(tableName, 0, (len > 0) ? (len - 1) : len);
        sb.append(isFcs ? "z" : "x");

        return sb.toString();
    }

    protected RecordIndex readRecordIndex(File file)
    {
        try
        {
            ByteBuffer buffer = this.readFileToBuffer(file);
            buffer.order(ByteOrder.LITTLE_ENDIAN); // Default to least significant byte first order.

            RecordIndex index = new RecordIndex();
            index.numEntries = buffer.getInt();
            index.headerLength = buffer.getInt();
            index.entries = new RecordIndex.Entry[index.numEntries];

            for (int i = 0; i < index.numEntries; i++)
            {
                int recordOffset = buffer.getInt();
                int recordLength = buffer.getInt();
                index.entries[i] = new RecordIndex.Entry(recordOffset, recordLength);
            }

            return index;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("VPF.ExceptionAttemptingToReadRecordIndex", file.getPath());
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    protected RecordIndex computeRecordIndex(ByteBuffer buffer, Header header)
    {
        // Compute a fixed length record size by summing the sizes of individual columns. Assume that the bytes of row
        // values are tightly packed.
        int recordLength = 0;
        for (Column col : header.columns)
        {
            // If any column contains a variable length field, then we cannot compute a record size for this table.
            if (col.isVariableLengthField())
            {
                return null;
            }

            recordLength += col.getFieldLength();
        }

        // Body offset is size of header length field (4 bytes) plus the length of the header content.
        // Body length is remaining bytes in file minus the body offset.
        // Number of records is the number of times a record with a fixed length can appear in the body.
        int bodyOffset = 4 + header.length;
        int bodyLength = buffer.limit() - bodyOffset;
        int numRecords = bodyLength / recordLength;

        RecordIndex index = new RecordIndex();
        index.headerLength = header.length;
        index.numEntries = numRecords;
        index.entries = new RecordIndex.Entry[numRecords];

        int offset = bodyOffset;
        for (int i = 0; i < numRecords; i++)
        {
            index.entries[i] = new RecordIndex.Entry(offset, recordLength);
            offset += index.entries[i].length;
        }

        return index;
    }
}
