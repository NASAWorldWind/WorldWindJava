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
import gov.nasa.worldwind.geom.LatLon;

/**
 * @author lado
 * @version $Id: RPFFrameFileComponents.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFFrameFileComponents
{
    public static final String DATA_TAG = "RPFIMG";

    // [ rpf location section ]
    public RPFLocationSection componentLocationTable;

    // [ rpf coverage section ]
    public LatLon nwUpperLeft, swLowerleft, neUpperRight, seLowerRight;
    public double verticalResolutionNorthSouth;
    public double horizontalResolutionEastWest;
    public double verticalIntervalLatitude;
    public double horizontalIntervalLongitude;

    // [ color / grayscale section ]
    public RPFColorMap[] rpfColorMaps;

    // [ rpf color / grayscale section ]
    public short numOfColorGrayscaleOffsetRecords;
    public short numOfColorConverterOffsetRecords;
    public String externalColorGrayscaleFilename;
    // [ rpf colormap subsection ]
    public long colormapOffsetTableOffset;
    public int colormapGrayscaleOffsetRecordLength;

    // [ rpf color converter subsection ]

    // [ rpf image description subheader ]
    public int numOfSpectralGroups;
    public int numOfSubframeTables;
    public int numOfSpectralBandTables;
    public int numOfSpectralBandLinesPerImageRow;
    public int numOfSubframesInEastWestDirection;
    public int numOfSubframesInNorthSouthDirection;
    public long numOfOutputColumnsPerSubframe;
    public long numOfOutputRowsPerSubframe;
    public long subframeMaskTableOffset;
    public long transparencyMaskTableOffset;

    // [ rpf related images section ]
    public RelatedImagesSection relatedImagesSection = null;

    public RPFFrameFileComponents(java.nio.ByteBuffer buffer)
    {
        this.componentLocationTable = new RPFLocationSection(buffer);

        if (0 < this.componentLocationTable.getCoverageSectionSubheaderLength())
            this.parseRPFCoverageSection(buffer);

        if (0 < this.componentLocationTable.getColorGrayscaleSectionSubheaderLength())
            this.parseColorGrayscaleSection(buffer);

        if (0 < this.componentLocationTable.getColormapSubsectionLength())
            this.parseColormapSubSection(buffer);

        if (0 < this.componentLocationTable.getColorConverterSubsectionLength())
            this.parseColorConverterSubsection(buffer);

        if (0 < this.componentLocationTable.getImageDescriptionSubheaderLength())
        {
            buffer.position(this.componentLocationTable.getImageDescriptionSubheaderLocation());
            this.parseImageDescriptionSubheader(buffer);
        }
        if (0 < this.componentLocationTable.getRelatedImagesSectionSubheaderLength())
        {
            buffer.position(this.componentLocationTable.getRelatedImagesSectionSubheaderLocation());
            this.relatedImagesSection = new RelatedImagesSection(buffer);
        }
    }

    private void parseImageDescriptionSubheader(java.nio.ByteBuffer buffer)
    {
        this.numOfSpectralGroups = NITFSUtil.getUShort(buffer);
        this.numOfSubframeTables = NITFSUtil.getUShort(buffer);
        this.numOfSpectralBandTables = NITFSUtil.getUShort(buffer);
        this.numOfSpectralBandLinesPerImageRow = NITFSUtil.getUShort(buffer);
        this.numOfSubframesInEastWestDirection = NITFSUtil.getUShort(buffer);
        this.numOfSubframesInNorthSouthDirection = NITFSUtil.getUShort(buffer);
        this.numOfOutputColumnsPerSubframe = NITFSUtil.getUInt(buffer);
        this.numOfOutputRowsPerSubframe = NITFSUtil.getUInt(buffer);
        this.subframeMaskTableOffset = NITFSUtil.getUInt(buffer);
        this.transparencyMaskTableOffset = NITFSUtil.getUInt(buffer);
    }

    private void parseColorConverterSubsection(java.nio.ByteBuffer buffer)
    {
        buffer.position(this.componentLocationTable.getColorConverterSubsectionLocation());
//        if (0 < this.numOfColorConverterOffsetRecords)
//            throw new NITFSRuntimeException("NITFSReader.NotImplemented.ColorConvertorSubsectionReader");
    }

    private void parseColormapSubSection(java.nio.ByteBuffer buffer)
    {
        buffer.position(this.componentLocationTable.getColormapSubsectionLocation());

        this.colormapOffsetTableOffset = NITFSUtil.getUInt(buffer);
        this.colormapGrayscaleOffsetRecordLength = NITFSUtil.getUShort(buffer);
        // read color / grayscale AND histogram records; builds a ColorMap (LUT)
        if (0 < this.numOfColorGrayscaleOffsetRecords)
        {
            rpfColorMaps = new RPFColorMap[this.numOfColorGrayscaleOffsetRecords];
            for (int i = 0; i < this.numOfColorGrayscaleOffsetRecords; i++)
            {
                rpfColorMaps[i] = new RPFColorMap(buffer, this.componentLocationTable.getColormapSubsectionLocation());
            }
        }
        else
            throw new NITFSRuntimeException("NITFSReader.InvalidNumberOfRPFColorGrayscaleRecords");
    }

    private void parseColorGrayscaleSection(java.nio.ByteBuffer buffer)
    {
        buffer.position(this.componentLocationTable.getColorGrayscaleSectionSubheaderLocation());

        this.numOfColorGrayscaleOffsetRecords = NITFSUtil.getByteAsShort(buffer);
        this.numOfColorConverterOffsetRecords = NITFSUtil.getByteAsShort(buffer);
        this.externalColorGrayscaleFilename = NITFSUtil.getString(buffer, 12);
    }

    private void parseRPFCoverageSection(java.nio.ByteBuffer buffer)
    {
        buffer.position(this.componentLocationTable.getCoverageSectionSubheaderLocation());

        this.nwUpperLeft = LatLon.fromDegrees(buffer.getDouble(), buffer.getDouble());
        this.swLowerleft = LatLon.fromDegrees(buffer.getDouble(), buffer.getDouble());
        this.neUpperRight = LatLon.fromDegrees(buffer.getDouble(), buffer.getDouble());
        this.seLowerRight = LatLon.fromDegrees(buffer.getDouble(), buffer.getDouble());

        this.verticalResolutionNorthSouth = buffer.getDouble();
        this.horizontalResolutionEastWest = buffer.getDouble();
        this.verticalIntervalLatitude = buffer.getDouble();
        this.horizontalIntervalLongitude = buffer.getDouble();
    }

    public class RelatedImagesSection
    {
        // [ rpf related images section subheader ]
        public long relatedImageDescriptionTableOffset;
        public int numOfRelatedImageDescriptionRecords;
        public int relatedImageDescriptionRecordLength;

        public RelatedImagesSection(java.nio.ByteBuffer buffer)
        {
            this.relatedImageDescriptionTableOffset = NITFSUtil.getUInt(buffer);
            this.numOfRelatedImageDescriptionRecords = NITFSUtil.getUShort(buffer);
            this.relatedImageDescriptionRecordLength = NITFSUtil.getUShort(buffer);
        }
    }
}
