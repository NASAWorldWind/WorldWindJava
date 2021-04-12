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

import java.util.*;

/**
 * @author Lado Garakanidze
 * @version $Id: RPFFrameFileIndexSection.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFFrameFileIndexSection
{
    // [ frame file index section subheader ]
    private String highestSecurityClassification;
    private long frameFileIndexTableOffset;
    private long numOfFrameFileIndexRecords;
    private int numOfPathnameRecords;
    private int frameFileIndexRecordLength;

    // [ frame file index subsection ]

    //      [ frame file index table ]
    private ArrayList<RPFFrameFileIndexRecord> frameFileIndexTable = new ArrayList<RPFFrameFileIndexRecord>();
    //      [ pathname table ]
    // private ArrayList<String> pathnameTable = new ArrayList<String>();

    public String getHighestSecurityClassification()
    {
        return highestSecurityClassification;
    }

    public long getFrameFileIndexTableOffset()
    {
        return frameFileIndexTableOffset;
    }

    public long getNumOfFrameFileIndexRecords()
    {
        return numOfFrameFileIndexRecords;
    }

    public int getNumOfPathnameRecords()
    {
        return numOfPathnameRecords;
    }

    public int getFrameFileIndexRecordLength()
    {
        return frameFileIndexRecordLength;
    }

    public List<RPFFrameFileIndexRecord> getFrameFileIndexTable()
    {
        return frameFileIndexTable;
    }

//    public ArrayList<String> getPathnameTable()
//    {
//        return pathnameTable;
//    }

    public RPFFrameFileIndexSection(java.nio.ByteBuffer buffer)
    {
        // [ frame file index section subheader ]
        this.highestSecurityClassification = NITFSUtil.getString(buffer, 1);
        this.frameFileIndexTableOffset = NITFSUtil.getUInt(buffer);
        this.numOfFrameFileIndexRecords = NITFSUtil.getUInt(buffer);
        this.numOfPathnameRecords = NITFSUtil.getUShort(buffer);
        this.frameFileIndexRecordLength = NITFSUtil.getUShort(buffer);

        this.parseFrameFileIndexAndPathnameTables(buffer);
    }

    private void parseFrameFileIndexAndPathnameTables(java.nio.ByteBuffer buffer)
    {
        int theSectionOffset = buffer.position();
        Hashtable<Integer, String> pathnames = new Hashtable<Integer, String>();

        for (int i = 0; i < this.numOfFrameFileIndexRecords; i++)
        {
            this.frameFileIndexTable.add(new RPFFrameFileIndexRecord(buffer));
        }

        for (int i = 0; i < this.numOfPathnameRecords; i++)
        {
            int relOffset = buffer.position() - theSectionOffset;
            int len = NITFSUtil.getUShort(buffer);
            pathnames.put(relOffset, NITFSUtil.getString(buffer, len));
        }

        if (0 < this.frameFileIndexTable.size() && 0 < pathnames.size())
        { // update pathname field in every RPFFrameFileIndexRecord
            for (RPFFrameFileIndexRecord rec : this.frameFileIndexTable)
            {
                int offset = (int) rec.getPathnameRecordOffset();
                if (pathnames.containsKey(offset))
                    rec.setPathname(pathnames.get(offset));
                else
                    throw new NITFSRuntimeException("NITFSReader.CorrespondingPathnameWasNotFound");
            }
        }
    }

    public class RPFFrameFileIndexRecord
    {
        public int getBoundaryRectangleRecordNumber()
        {
            return boundaryRectangleRecordNumber;
        }

        public int getFrameLocationRowNumber()
        {
            return frameLocationRowNumber;
        }

        public int getFrameLocationColumnNumber()
        {
            return frameLocationColumnNumber;
        }

        public String getFrameFileName()
        {
            return frameFileName;
        }

        public String getGeoLocation()
        {
            return geoLocation;
        }

        public String getSecurityClass()
        {
            return securityClass;
        }

        public String getSecurityCountryCode()
        {
            return securityCountryCode;
        }

        public String getSecurityReleaseMark()
        {
            return securityReleaseMark;
        }

        public long getPathnameRecordOffset()
        {
            return pathnameRecordOffset;
        }

        public String getPathname()
        {
            return pathname;
        }

        public void setPathname(String pathname)
        {
            this.pathname = pathname;
        }

        private int boundaryRectangleRecordNumber;
        private int frameLocationRowNumber;
        private int frameLocationColumnNumber;
        private long pathnameRecordOffset;
        private String frameFileName;
        private String geoLocation;
        private String securityClass;
        private String securityCountryCode;
        private String securityReleaseMark;
        private String pathname;   // this field is not part of the NITFS spec

        public RPFFrameFileIndexRecord(java.nio.ByteBuffer buffer)
        {
            this.boundaryRectangleRecordNumber = NITFSUtil.getUShort(buffer);
            this.frameLocationRowNumber = NITFSUtil.getUShort(buffer);
            this.frameLocationColumnNumber = NITFSUtil.getUShort(buffer);
            this.pathnameRecordOffset = NITFSUtil.getUInt(buffer);
            this.frameFileName = NITFSUtil.getString(buffer, 12);
            this.geoLocation = NITFSUtil.getString(buffer, 6);
            this.securityClass = NITFSUtil.getString(buffer, 1);
            this.securityCountryCode = NITFSUtil.getString(buffer, 2);
            this.securityReleaseMark = NITFSUtil.getString(buffer, 2);
            this.pathname = "";
        }
    }
}
