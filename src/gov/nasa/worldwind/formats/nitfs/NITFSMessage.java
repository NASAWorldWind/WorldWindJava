/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.nitfs;

import gov.nasa.worldwind.formats.rpf.RPFUserDefinedHeaderSegment;
import gov.nasa.worldwind.util.Logging;

import java.io.IOException;

/**
 * @author Lado Garakanidze
 * @version $Id: NITFSMessage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NITFSMessage
{
    private     java.nio.ByteBuffer     buffer;
    private NITFSFileHeader fileHeader;
    private     java.util.ArrayList<NITFSSegment> segments = new java.util.ArrayList<NITFSSegment>();


    public NITFSSegment getSegment( NITFSSegmentType segmentType )
    {
        for(NITFSSegment seg : segments)
        {
            if(null != seg && seg.segmentType.equals(segmentType))
                return seg;
        }
        return null;
    }

    public NITFSFileHeader getNITFSFileHeader()
    {
        return this.fileHeader;
    }

    private NITFSMessage(java.nio.ByteBuffer buffer)
    {
        this.buffer = buffer;
        this.fileHeader = new NITFSFileHeader(buffer);

        // read ALL description groups and segments
        this.readSegments();
    }

    private void readSegments()
    {
        int saveOffset = this.buffer.position();
        int nextSegmentOffset = this.fileHeader.getHeaderLength();

        // parse Image Description Group
        nextSegmentOffset = parseSegment(NITFSSegmentType.IMAGE_SEGMENT, nextSegmentOffset);
        // parse Graphic/Symbol Description Group
        nextSegmentOffset = parseSegment(NITFSSegmentType.SYMBOL_SEGMENT, nextSegmentOffset);
        // parse Label Description Group
        nextSegmentOffset = parseSegment(NITFSSegmentType.LABEL_SEGMENT, nextSegmentOffset);
        // parse Text Description Group
        nextSegmentOffset = parseSegment(NITFSSegmentType.TEXT_SEGMENT, nextSegmentOffset);
        // parse Data Extension Description Group
        nextSegmentOffset = parseSegment(NITFSSegmentType.DATA_EXTENSION_SEGMENT, nextSegmentOffset);
        // parse Reserved Extension Description Group
        nextSegmentOffset = parseSegment(NITFSSegmentType.RESERVED_EXTENSION_SEGMENT, nextSegmentOffset);
        // parse User Defined Header Description (UDHD) Group
        NITFSUserDefinedHeaderSegment userHeaderSeg = new RPFUserDefinedHeaderSegment(this.buffer);
        this.segments.add( userHeaderSeg );
        nextSegmentOffset += userHeaderSeg.headerLength + userHeaderSeg.dataLength;
        // parse Extended Header Description Group
        nextSegmentOffset = parseSegment(NITFSSegmentType.EXTENDED_HEADER_SEGMENT, nextSegmentOffset);

        // let's read each header
        for(NITFSSegment segment : segments)
        {

//
//            String segId = NITFSUtil.getString(buffer, segment.headerStartOffset, 2);
//            System.out.println("Segment type=" + segment.segmentType + ", id=" + segId);
        }
    }

    private int parseSegment(NITFSSegmentType segType, int nextSegmentOffset)
    {
        int headerLengthSize = segType.getHeaderLengthSize();
        int dataLengthSize = segType.getDataLengthSize();

        int numOfSegments = Integer.parseInt(NITFSUtil.getString(this.buffer, 3));
        for (int i = 0; i < numOfSegments; i++)
        {
            int segHeaderLength = Integer.parseInt(NITFSUtil.getString(this.buffer, headerLengthSize));
            int seqDataLength = Integer.parseInt(NITFSUtil.getString(this.buffer, dataLengthSize));

            int saveOffset = this.buffer.position(); // pass buffer to NITFSSegment to parse their headers' contents
            NITFSSegment segment;
            switch (segType)
            {
                case IMAGE_SEGMENT:
                    segment = new NITFSImageSegment(this.buffer, nextSegmentOffset, segHeaderLength,
                                        nextSegmentOffset + segHeaderLength, seqDataLength);
                    break;
                case SYMBOL_SEGMENT:
                    segment = new NITFSSymbolSegment(this.buffer, nextSegmentOffset, segHeaderLength,
                                        nextSegmentOffset + segHeaderLength, seqDataLength);
                    break;
                case LABEL_SEGMENT:
                    segment = new NITFSLabelSegment(this.buffer, nextSegmentOffset, segHeaderLength,
                                        nextSegmentOffset + segHeaderLength, seqDataLength);
                    break;
                case TEXT_SEGMENT:
                    segment = new NITFSTextSegment(this.buffer, nextSegmentOffset, segHeaderLength,
                                        nextSegmentOffset + segHeaderLength, seqDataLength);
                    break;
                case DATA_EXTENSION_SEGMENT:
                    segment = new NITFSDataExtensionSegment(this.buffer, nextSegmentOffset, segHeaderLength,
                                        nextSegmentOffset + segHeaderLength, seqDataLength);
                    break;
                case RESERVED_EXTENSION_SEGMENT:
                    segment = new NITFSReservedExtensionSegment(this.buffer, nextSegmentOffset, segHeaderLength,
                                        nextSegmentOffset + segHeaderLength, seqDataLength);
                    break;
                case USER_DEFINED_HEADER_SEGMENT:
                    segment = new RPFUserDefinedHeaderSegment(this.buffer);
                    break;
                case EXTENDED_HEADER_SEGMENT:    // // throw exception - wrong parser for EXTENDED_HEADER_SEGMENT
                    segment = new NITFSExtendedHeaderSegment(this.buffer, nextSegmentOffset, segHeaderLength,
                                        nextSegmentOffset + segHeaderLength, seqDataLength);
                    break;

                default:
                    throw new NITFSRuntimeException("NITFSReader.UnknownOrUnsupportedSegment", segType.toString());

            }
            this.segments.add(segment);

            nextSegmentOffset += segHeaderLength + seqDataLength;
            buffer.position(saveOffset); // restore offset
        }
        return nextSegmentOffset;
    }

    public static NITFSMessage load(java.io.File file) throws java.io.IOException
    {
        validateImageFile(file);

        java.nio.ByteBuffer roBuffer = NITFSUtil.readEntireFile(file).asReadOnlyBuffer();

        // check if it is a NITFS format file (NITF or NSIF - for NATO Secondary Imagery Format)
        String fmtId = NITFSUtil.getString(roBuffer, 0, 4);
        if( 0 != "NITF".compareTo(fmtId) && 0 != "NSIF".compareTo(fmtId))
        {
            throw new NITFSRuntimeException("NITFSReader.UnknownOrUnsupportedNITFSFormat", file.getCanonicalPath());
        }

        return new NITFSMessage(roBuffer);
    }

    private static void validateImageFile(java.io.File file)
        throws IOException, IllegalArgumentException, NITFSRuntimeException {
        if (null == file)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!file.exists() || !file.canRead())
        {
            throw new NITFSRuntimeException("NITFSReader.NoFileOrNoPermission", file.getCanonicalPath());
        }
    }
}
