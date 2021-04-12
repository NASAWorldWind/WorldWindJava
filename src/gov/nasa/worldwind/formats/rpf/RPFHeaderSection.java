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

import gov.nasa.worldwind.formats.nitfs.NITFSUtil;

/**
 * @author Lado Garakanidze
 * @version $Id: RPFHeaderSection.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class RPFHeaderSection
{
    public static final String DATA_TAG = "RPFHDR";

    public boolean  endianIndicator;
    public short    headerLength;
    public String   filename;
    public short    updateIndicator; // new | replacement | update
    public String   govSpecNumber;
    public String   govSpecDate;
    public String   securityClass;
    public String   securityCountryCode;
    public String   securityReleaseMark;
    public int      locationSectionLocation;

    public RPFHeaderSection(java.nio.ByteBuffer buffer)
    {
        this.endianIndicator = ((byte) 0 != buffer.get());         // reads 1 byte, 0 for big endian
        this.headerLength = buffer.getShort();                     // reads 2 bytes
        this.filename = NITFSUtil.getString(buffer, 12);
        this.updateIndicator = NITFSUtil.getByteAsShort(buffer);       // reads 1 byte (short)
        this.govSpecNumber = NITFSUtil.getString(buffer, 15);
        this.govSpecDate = NITFSUtil.getString(buffer, 8);
        this.securityClass = NITFSUtil.getString(buffer, 1);
        this.securityCountryCode = NITFSUtil.getString(buffer, 2);
        this.securityReleaseMark = NITFSUtil.getString(buffer, 2);
        this.locationSectionLocation = buffer.getInt();            // read 4 bytes (int)
    }
}
