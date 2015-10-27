/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
