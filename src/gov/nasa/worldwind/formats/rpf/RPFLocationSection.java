/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.formats.nitfs.*;

/**
 * @author Lado Garakanidze
 * @version $Id: RPFLocationSection.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFLocationSection
{
    private java.util.Hashtable<Integer, ComponentLocationRecord> table =
            new java.util.Hashtable<Integer, ComponentLocationRecord>();

        public int getHeaderComponentLocation()
        {
            return this.getLocation(128);
        }
        public int getHeaderComponentLength()
        {
            return this.getLength(128);
        }
        public int getLocationComponentLocation()
        {
            return this.getLocation(129);
        }
        public int getLocationComponentLength()
        {
            return this.getLength(129);
        }
        public int getCoverageSectionSubheaderLocation()
        {
            return this.getLocation(130);
        }
        public int getCoverageSectionSubheaderLength()
        {
            return this.getLength(130);
        }
        public int getCompressionSectionSubheaderLocation()
        {
            return this.getLocation(131);
        }
        public int getCompressionSectionSubheaderLength()
        {
            return this.getLength(131);
        }
        public int getCompressionLookupSubsectionLocation()
        {
            return this.getLocation(132);
        }
        public int getCompressionLookupSubsectionLength()
        {
            return this.getLength(132);
        }
        public int getCompressionParameterSubsectionLocation()
        {
            return this.getLocation(133);
        }
        public int getCompressionParameterSubsectionLength()
        {
            return this.getLength(133);
        }
        public int getColorGrayscaleSectionSubheaderLocation()
        {
            return this.getLocation(134);
        }
        public int getColorGrayscaleSectionSubheaderLength()
        {
            return this.getLength(134);
        }
        public int getColormapSubsectionLocation()
        {
            return this.getLocation(135);
        }
        public int getColormapSubsectionLength()
        {
            return this.getLength(135);
        }
        public int getImageDescriptionSubheaderLocation()
        {
            return this.getLocation(136);
        }
        public int getImageDescriptionSubheaderLength()
        {
            return this.getLength(136);
        }
        public int getImageDisplayParametersSubheaderLocation()
        {
            return this.getLocation(137);
        }
        public int getImageDisplayParametersSubheaderLength()
        {
            return this.getLength(137);
        }
        public int getMaskSubsectionLocation()
        {
            return this.getLocation(138);
        }
        public int getMaskSubsectionLength()
        {
            return this.getLength(138);
        }
        public int getColorConverterSubsectionLocation()
        {
            return this.getLocation(139);
        }
        public int getColorConverterSubsectionLength()
        {
            return this.getLength(139);
        }

        public int getSpatialDataSubsectionLocation()
        {
            return this.getLocation(140);
        }
        public int getSpatialDataSubsectionLength()
        {
            return this.getLength(140);
        }
        public int getAttributeSectionSubheaderLocation()
        {
            return this.getLocation(141);
        }
        public int getAttributeSectionSubheaderLength()
        {
            return this.getLength(141);
        }
        public int getAttributeSubsectionLocation()
        {
            return this.getLocation(142);
        }
        public int getAttributeSubsectionLength()
        {
            return this.getLength(142);
        }
        public int getExplicitArealCoverageTableLocation()
        {
            return this.getLocation(143);
        }
        public int getExplicitArealCoverageTableLength()
        {
            return this.getLength(143);
        }
        public int getRelatedImagesSectionSubheaderLocation()
        {
            return this.getLocation(144);
        }
        public int getRelatedImagesSectionSubheaderLength()
        {
            return this.getLength(144);
        }
        public int getRelatedImagesSubsectionLocation()
        {
            return this.getLocation(145);
        }
        public int getRelatedImagesSubsectionLength()
        {
            return this.getLength(145);
        }
        public int getReplaceUpdateSectionSubheaderLocation()
        {
            return this.getLocation(146);
        }
        public int getReplaceUpdateSectionSubheaderLength()
        {
            return this.getLength(146);
        }
        public int getReplaceUpdateTableLocation()
        {
            return this.getLocation(147);
        }
        public int getReplaceUpdateTableLength()
        {
            return this.getLength(147);
        }
        public int getBoundaryRectangleSectionSubheaderLocation()
        {
            return this.getLocation(148);
        }
        public int getBoundaryRectangleSectionSubheaderLength()
        {
            return this.getLength(148);
        }
        public int getBoundaryRectangleTableLocation()
        {
            return this.getLocation(149);
        }
        public int getBoundaryRectangleTableLength()
        {
            return this.getLength(149);
        }
        public int getFrameFileIndexSectionSubheaderLocation()
        {
            return this.getLocation(150);
        }
        public int getFrameFileIndexSectionSubheaderLength()
        {
            return this.getLength(150);
        }
        public int getFrameFileIndexSubsectionLocation()
        {
            return this.getLocation(151);
        }
        public int getFrameFileIndexSubsectionLength()
        {
            return this.getLength(151);
        }
        public int getColorTableIndexSectionSubheaderLocation()
        {
            return this.getLocation(152);
        }
        public int getColorTableIndexSectionSubheaderLength()
        {
            return this.getLength(152);
        }
        public int getColorTableIndexRecordLocation()
        {
            return this.getLocation(153);
        }
        public int getColorTableIndexRecordLength()
        {
            return this.getLength(153);
        }
        // because of lack of "unsigned" in java, we store UINT as int, and UINT as long
        public int      locationSectionLength;
        public long     componentLocationTableOffset;
        public int      numOfComponentLocationRecords;
        public int      componentLocationRecordLength;
        public long     componentAggregateLength;

        public RPFLocationSection(java.nio.ByteBuffer buffer)
        {
            this.locationSectionLength = NITFSUtil.getUShort(buffer);
            this.componentLocationTableOffset = NITFSUtil.getUInt(buffer);
            this.numOfComponentLocationRecords = NITFSUtil.getUShort(buffer);
            this.componentLocationRecordLength = NITFSUtil.getUShort(buffer);
            this.componentAggregateLength = NITFSUtil.getUInt(buffer);

            if (this.numOfComponentLocationRecords < 2)
                throw new NITFSRuntimeException("NITFSReader:InvalidNumberOfComponentLocationRecords");

            for (int i = 0; i < this.numOfComponentLocationRecords; i++)
            {
                int id = NITFSUtil.getUShort(buffer);
                table.put(id, new ComponentLocationRecord(id,
                    NITFSUtil.getUInt(buffer),      // read uint:4 as "length"
                    NITFSUtil.getUInt(buffer)       // read uint:4 as "location"
                ));
            }
        }

        private int getLocation(int componentID)
        {
            ComponentLocationRecord rec = this.getRecord(componentID);
            return (int) ((null != rec) ? (0xFFFFFFFFL & rec.getLocation()) : 0);
        }
        private int getLength(int componentID)
        {
            ComponentLocationRecord rec = this.getRecord(componentID);
            return (int) ((null != rec) ? (0xFFFFFFFFL & rec.getLength()) : 0);
        }
        private ComponentLocationRecord getRecord(int componentID)
        {
            if(table.containsKey(componentID))
                return table.get(componentID);
            return null;
        }

        public class ComponentLocationRecord
        {
            private int  id;
            private long length;
            private long location;

            public int getId()
            {
                return id;
            }

            public long getLength()
            {
                return length;
            }

            public long getLocation()
            {
                return location;
            }

            public ComponentLocationRecord(int id, long length, long location)
            {
                this.id = id;
                this.length = length;
                this.location = location;
            }
        }

}
