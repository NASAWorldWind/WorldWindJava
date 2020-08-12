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

import gov.nasa.worldwind.formats.nitfs.NITFSRuntimeException;
import gov.nasa.worldwind.formats.nitfs.NITFSSegmentType;

import java.io.IOException;

/**
 * @author Lado Garakanidze
 * @version $Id: RPFTOCFile.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFTOCFile extends RPFFile
{
    private RPFFileComponents rpfFileComponents;

    public RPFHeaderSection getHeaderSection()
    {
        return (null != this.rpfFileComponents) ? this.rpfFileComponents.getRPFHeaderSection() : null;
    }

    public RPFFrameFileIndexSection getFrameFileIndexSection()
    {
        return (null != this.rpfFileComponents) ? this.rpfFileComponents.getRPFFrameFileIndexSection() : null;
    }

    public RPFFileComponents getRPFFileComponents()
    {
        return this.rpfFileComponents;
    }

    protected RPFTOCFile(java.io.File rpfFile) throws IOException, NITFSRuntimeException {
        super(rpfFile);

        RPFUserDefinedHeaderSegment segment =
            (RPFUserDefinedHeaderSegment)this.getNITFSSegment( NITFSSegmentType.USER_DEFINED_HEADER_SEGMENT);

        if(null ==  segment)
            throw new NITFSRuntimeException("NITFSReader.UserDefinedHeaderSegmentWasNotFound");

        this.rpfFileComponents = segment.getRPFFileComponents();
        if(null == this.rpfFileComponents)
            throw new NITFSRuntimeException("NITFSReader.RPFFileComponents.Were.Not.Found.In.UserDefinedHeaderSegment");
    }

    public static RPFTOCFile load(java.io.File tocFile) throws java.io.IOException
    {
        return new RPFTOCFile(tocFile);
    }
}
