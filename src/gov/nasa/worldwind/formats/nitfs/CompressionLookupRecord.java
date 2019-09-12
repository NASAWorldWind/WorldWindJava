/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.nitfs;

import gov.nasa.worldwind.formats.rpf.RPFColorMap;

/**
 * @author Lado Garakanidze
 * @version $Id: CompressionLookupRecord.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class CompressionLookupRecord
{
    public int getTableID()
    {
        return this.tableID;
    }

    public int getNumOfRecords()
    {
        return this.numOfRecords;
    }

    public int getNumOfValuesPerRecord()
    {
        return this.numOfValuesPerRecord;
    }

    public int getValueBitLength()
    {
        return this.valueBitLength;
    }

    public short getBytesPerRecord()
    {
        return this.bytesPerRecord;
    }

    public byte[] copyValues(byte [] dest, int destOffset, int idx, int len)
    {
        if(len != this.bytesPerRecord)
            throw new NITFSRuntimeException("NITFSReader.AttemptToCopyWithInvalidSizeOfRecord");
        if(idx  >= this.numOfRecords)
            throw new NITFSRuntimeException("NITFSReader.AttemptToCopyOutOfBoundsAtSource");
        if(null == dest)
            throw new NITFSRuntimeException("NITFSReader.AttemptCopyToIvalidDestination");
        if(dest.length < destOffset + len)
            throw new NITFSRuntimeException("NITFSReader.AttemptToCopyOutOfBoundsAtDestination");

        System.arraycopy(lut, idx * this.bytesPerRecord, dest, destOffset, this.bytesPerRecord);

        return dest;
    }


    private int     tableID;
    private int     numOfRecords;
    private int     numOfValuesPerRecord;
    private int     valueBitLength;
    private int     tableLocation;
    private short   bytesPerRecord;

    private byte[]  lut;

    public CompressionLookupRecord(java.nio.ByteBuffer buffer,
        int compressionLookupSubsectionLocation,
        RPFColorMap[] colormaps // TODO update LUT with the color mapped values to gain performance
    )

    {
        this.tableID = NITFSUtil.getUShort(buffer);
        this.numOfRecords = (int) NITFSUtil.getUInt(buffer);
        this.numOfValuesPerRecord = NITFSUtil.getUShort(buffer);
        this.valueBitLength = NITFSUtil.getUShort(buffer);
        this.tableLocation = (int) (NITFSUtil.getUInt(buffer) + compressionLookupSubsectionLocation);
        int saveOffset = buffer.position();

        this.bytesPerRecord = (short) (this.numOfValuesPerRecord * this.valueBitLength/8L);
        this.lut = new byte[ this.numOfRecords * this.bytesPerRecord ];

        buffer.position(this.tableLocation);
        buffer.get(this.lut, 0, this.numOfRecords * this.bytesPerRecord);

        buffer.position(saveOffset);
    }
}
