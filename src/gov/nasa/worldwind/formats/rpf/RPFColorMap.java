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

package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.formats.nitfs.*;

/**
 * @author lado
 * @version $Id: RPFColorMap.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFColorMap
{
    public int getTableID()
    {
        return tableID;
    }

    public int getHistogramRecordLength()
    {
        return histogramRecordLength;
    }

    public int getHistogramTableOffset()
    {
        return (int)(0xFFFFFFFFL & histogramTableOffset);
    }

    public int getNumOfColorRecords()
    {
        return (int)(0xFFFFFFFFL & numOfColorRecords);
    }

    public int getColorElementLength()
    {
        return (int)(0xFFFFFFFFL & colorElementLength);
    }

    public byte getColor(int colorRec, int bytePosition)
    {
        long idx = colorRec * this.getNumOfColorRecords() * getColorElementLength() + bytePosition ;
        return this.colorMap[(int)idx];
    }



    public byte[] getColorMap()
    {
        return this.colorMap;
    }

    private byte[]  colorMap;
    // private byte[]  histogramMap;

    private int     tableID;
    private long    numOfColorRecords;

    private short   colorElementLength;
    private int     histogramRecordLength;
    private long    colorTableOffset;
    private long    histogramTableOffset;

    public RPFColorMap(java.nio.ByteBuffer buffer, int colormapSubsectionOffset)
    {
        this.parseRPFColorOffsetRecord(buffer);
        // now let's load color map and histogram
        int saveOffset = buffer.position();
        this.loadColorMaps(buffer, colormapSubsectionOffset);
        // ok, we can skip histogram for now
        // this.loadHistogram(buffer, colormapSubsectionOffset);
        buffer.position(saveOffset);
    }

    private void parseRPFColorOffsetRecord(java.nio.ByteBuffer buffer)
    {
        this.tableID                = NITFSUtil.getUShort(buffer);
        this.numOfColorRecords      = NITFSUtil.getUInt(buffer);
        this.colorElementLength     = NITFSUtil.getByteAsShort(buffer);
        this.histogramRecordLength  = NITFSUtil.getUShort(buffer);
        this.colorTableOffset       = NITFSUtil.getUInt(buffer);
        this.histogramTableOffset   = NITFSUtil.getUInt(buffer);
    }

    private void loadColorMaps(java.nio.ByteBuffer buffer, int colormapSubsectionOffset)
    {
        if (0 == this.numOfColorRecords)
            throw new NITFSRuntimeException("NITFSReader.InvalidNumberOfColorRecords");
        if (0 == this.colorElementLength)
            throw new NITFSRuntimeException("NITFSReader.InvalidLengthOfColorRecordElement");

        buffer.position((int) (colormapSubsectionOffset + this.colorTableOffset));
        int mapLength = (int)(this.numOfColorRecords * this.colorElementLength);
        this.colorMap = new byte[mapLength];
        buffer.get(this.colorMap, 0, mapLength);
    }

    private void loadHistogram(java.nio.ByteBuffer buffer, int colormapSubsectionOffset)
    {
        if (0 == this.numOfColorRecords)
            throw new NITFSRuntimeException("NITFSReader.InvalidNumberOfColorRecords");
        if (0 == this.histogramRecordLength)
            throw new NITFSRuntimeException("NITFSReader.InvalidLengthOfHistogramRecordElement");
        // skip the loading of the histogram table, just increment a position in the buffer
        buffer.position((int) (colormapSubsectionOffset + this.histogramTableOffset
            + (this.numOfColorRecords * this.histogramRecordLength)));
    }
}
