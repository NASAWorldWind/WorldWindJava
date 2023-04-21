/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
