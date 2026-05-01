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

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.ogc.gml.*;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class WCSDescribeCoverageParsingTest
{
    @Test
    public void testParsing001()
    {
        WCS100DescribeCoverage caps = new WCS100DescribeCoverage("testData/WCS/WCSDescribeCoverage001.xml");

        try
        {
            caps.parse();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        List<WCS100CoverageOffering> coverageOfferings = caps.getCoverageOfferings();
        assertNotNull(coverageOfferings, "CoverageOfferings is null");
        assertEquals(1, coverageOfferings.size(), "Incorrect coverage offering count");

        WCS100CoverageOffering coverage = coverageOfferings.get(0);
        assertNotNull(coverage, "CoverageOffering is null");
        assertNotNull(coverage.getName(), "CoverageOffering name is null");
        assertEquals("WW:NASA_SRTM30_900m_Tiled", coverage.getName(), "Incorrect CoverageOffering name");
        assertNotNull(coverage.getLabel(), "CoverageOffering label is null");
        assertEquals("NASA_SRTM30_900m_Tiled", coverage.getLabel(), "Incorrect CoverageOffering label");

        WCS100LonLatEnvelope lonLatEnvelope = coverage.getLonLatEnvelope();
        assertNotNull(lonLatEnvelope, "LonLatEnvelope is null");
        assertNotNull(lonLatEnvelope.getPositions(), "LonLatEnvelope positions is null");
        assertEquals("urn:ogc:def:crs:OGC:1.3:CRS84", lonLatEnvelope.getSRSName(), "Incorrect LonLatEnvelope SRS");
        assertEquals(2, lonLatEnvelope.getPositions().size(), "Incorrect LonLatEnvelope position count");
        assertEquals("-180.0 -90.0", lonLatEnvelope.getPositions().get(0).getPosString(), "Incorrect LonLatEnvelope position 0");
        assertEquals("180.0 90.0", lonLatEnvelope.getPositions().get(1).getPosString(), "Incorrect LonLatEnvelope position 1");

        List<String> keywords = coverage.getKeywords();
        assertTrue(keywords != null, "Keywords is null");
        assertEquals(3, keywords.size(), "Incorrect keyword count");
        assertTrue(keywords.contains("WCS"), "Missing keyword");
        assertTrue(keywords.contains("ImageMosaic"), "Missing keyword");
        assertTrue(keywords.contains("NASA_SRTM30_900m_Tiled"), "Missing keyword");

        WCS100DomainSet domainSet = coverage.getDomainSet();
        assertNotNull(domainSet, "DomainSet is null");

        WCS100SpatialDomain spatialDomain = domainSet.getSpatialDomain();
        assertNotNull(spatialDomain, "SpatialDomain is null");

        List<GMLEnvelope> envelopes = spatialDomain.getEnvelopes();
        assertNotNull(envelopes, "Envelope is null");
        assertEquals(1, envelopes.size(), "Incorrect envelope count");
        GMLEnvelope envelope = envelopes.get(0);
        assertEquals("-180.0 -90.0", envelope.getPositions().get(0).getPosString(), "Envelope position 0 is incorrect");
        assertEquals("180.0 90.0", envelope.getPositions().get(1).getPosString(), "Envelope position 1 is incorrect");

        List<GMLRectifiedGrid> rectifiedGrids = spatialDomain.getRectifiedGrids();
        assertNotNull(rectifiedGrids, "RectifiedGrid is null");
        assertEquals(1, rectifiedGrids.size(), "Incorrect RectifiedGrid count");
        GMLRectifiedGrid rGrid = rectifiedGrids.get(0);
        GMLLimits limits = rGrid.getLimits();
        assertNotNull(limits, "Limits is null");
        List<GMLGridEnvelope> gridEnvelopes = limits.getGridEnvelopes();
        assertNotNull(gridEnvelopes, "GridEnvelope is null");
        assertEquals(1, gridEnvelopes.size(), "Incorrect GridEnvelope count");
        assertEquals("0 0", gridEnvelopes.get(0).getLow(), "Low limit is incorrect");
        assertEquals("43199 21599", gridEnvelopes.get(0).getHigh(), "High limit is incorrect");
        List<String> axisNames = rGrid.getAxisNames();
        assertNotNull(axisNames, "AxisNames is null");
        assertEquals(2, axisNames.size(), "Incorrect AxisNames count");
        assertEquals("x", axisNames.get(0), "Incorrect first axis name 0");
        assertEquals("y", axisNames.get(1), "Incorrect second axis name 0");
        GMLOrigin origin = rGrid.getOrigin();
        assertNotNull(origin, "Origin is null");
        assertEquals("-179.99583333333334 89.99583333333334", origin.getPos().getPosString(), "Incorrect origin values");
        List<String> offsetVectors = rGrid.getOffsetVectorStrings();
        assertNotNull(offsetVectors, "OffsetVectors is null");
        assertEquals(2, offsetVectors.size(), "Incorrect offsetVector count");
        assertEquals("0.008333333333333333 0.0", offsetVectors.get(0), "Incorrect first offset vector");
        assertEquals("0.0 -0.008333333333333333", offsetVectors.get(1), "Incorrect second offset vector");

        WCS100RangeSetHolder rangeSetHolder = coverage.getRangeSet();
        assertNotNull(rangeSetHolder, "RangeSetHolder is null");
        WCS100RangeSet rangeSet = rangeSetHolder.getRangeSet();
        assertNotNull(rangeSet, "RangeSet is null");
        assertEquals("NASA_SRTM30_900m_Tiled", rangeSet.getName(), "RangeSet name is incorrect");
        assertEquals("NASA_SRTM30_900m_Tiled", rangeSet.getLabel(), "RangeSet label is incorrect");
        List<WCS100AxisDescriptionHolder> axisDescriptionHolders = rangeSet.getAxisDescriptions();
        assertNotNull(axisDescriptionHolders, "axisDescription is null");
        assertEquals(1, axisDescriptionHolders.size(), "axisDescription count incorrect");
        WCS100AxisDescription axisDescription = axisDescriptionHolders.get(0).getAxisDescription();
        assertNotNull(axisDescription, "AxisDescription is null");
        assertEquals("Band", axisDescription.getName(), "AxisDescription name is incorrect");
        assertEquals("Band", axisDescription.getLabel(), "AxisDescription label is incorrect");
        WCS100Values values = axisDescription.getValues();
        assertNotNull(values, "Values is null");
        List<WCS100SingleValue> singleValues = values.getSingleValues();
        assertNotNull(singleValues, "SingleValues is null");
        assertEquals(1, singleValues.size(), "Incorrect singleValues count");
        assertEquals(1.0, singleValues.get(0).getSingleValue(), 0.0, "Incorrect singleValue");

        WCS100SupportedFormats supportedFormats = coverage.getSupportedFormats();
        assertNotNull(supportedFormats, "SuppotedFormats is null");
        assertEquals(8, supportedFormats.getStrings().size(), "SupportedFormats count is incorrect");
        assertTrue(supportedFormats.getStrings().contains("ArcGrid"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("GeoTIFF"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("GIF"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("Gtopo30"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("ImageMosaic"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("JPEG"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("PNG"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("TIFF"), "Missing format");
        assertEquals("ImageMosaic", supportedFormats.getNativeFormat(), "Supported formats nativeFormat is incorrect");

        WCS100SupportedCRSs supportedCRSs = coverage.getSupportedCRSs();
        assertNotNull(supportedCRSs, "SupportedCRSs is null");
        assertNotNull(supportedCRSs.getRequestResponseCRSs(), "SupportedCRSs requestResponses is null");
        assertEquals(1, supportedCRSs.getRequestResponseCRSs().size(), "SupportedCRSs requestResponse count is incorrect");
        assertEquals("EPSG:4326", supportedCRSs.getRequestResponseCRSs().get(0), "RequestResponse value is incorrect");

        WCS100SupportedInterpolations supportedInterpolations = coverage.getSupportedInterpolations();
        assertNotNull(supportedInterpolations, "SupportedInterpolations is null");
        assertEquals(3, supportedInterpolations.getStrings().size(), "SupportedInterpolations count is incorrect");
        assertTrue(supportedInterpolations.getStrings().contains("nearest neighbor"), "Missing interpolation");
        assertTrue(supportedInterpolations.getStrings().contains("bilinear"), "Missing interpolation");
        assertTrue(supportedInterpolations.getStrings().contains("bicubic"), "Missing interpolation");
        assertEquals("nearest neighbor", supportedInterpolations.getDefault(), "Supported Interpolations default is incorrect");
    }

    @Test
    public void testParsing002()
    {
        WCS100DescribeCoverage caps = new WCS100DescribeCoverage("testData/WCS/WCSDescribeCoverage002.xml");

        try
        {
            caps.parse();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        List<WCS100CoverageOffering> coverageOfferings = caps.getCoverageOfferings();
        assertNotNull(coverageOfferings, "CoverageOfferings is null");
        assertEquals(1, coverageOfferings.size(), "Incorrect coverage offering count");

        WCS100CoverageOffering coverage = coverageOfferings.get(0);
        assertNotNull(coverage, "CoverageOffering is null");
        assertNotNull(coverage.getName(), "CoverageOffering name is null");
        assertEquals("1", coverage.getName(), "Incorrect CoverageOffering name");
        assertNotNull(coverage.getLabel(), "CoverageOffering label is null");
        assertEquals("dted0_1", coverage.getLabel(), "Incorrect CoverageOffering label");

        WCS100LonLatEnvelope lonLatEnvelope = coverage.getLonLatEnvelope();
        assertNotNull(lonLatEnvelope, "LonLatEnvelope is null");
        assertNotNull(lonLatEnvelope.getPositions(), "LonLatEnvelope positions is null");
        assertEquals("WGS84(DD)", lonLatEnvelope.getSRSName(), "Incorrect LonLatEnvelope SRS");
        assertEquals(2, lonLatEnvelope.getPositions().size(), "Incorrect LonLatEnvelope position count");
        assertEquals("-179.99999999999991 -89.999999999999943", lonLatEnvelope.getPositions().get(0).getPosString(), "Incorrect LonLatEnvelope position 0");
        assertEquals("180.00000000000003 84.00416666700005", lonLatEnvelope.getPositions().get(1).getPosString(), "Incorrect LonLatEnvelope position 1");

        WCS100DomainSet domainSet = coverage.getDomainSet();
        assertNotNull(domainSet, "DomainSet is null");

        WCS100SpatialDomain spatialDomain = domainSet.getSpatialDomain();
        assertNotNull(spatialDomain, "SpatialDomain is null");

        List<GMLEnvelope> envelopes = spatialDomain.getEnvelopes();
        assertNotNull(envelopes, "Envelope is null");
        assertEquals(1, envelopes.size(), "Incorrect envelope count");
        GMLEnvelope envelope = envelopes.get(0);
        assertNotNull(envelope.getSRSName(), "Envelope srsName is null");
        assertEquals("EPSG:4326", envelope.getSRSName(), "Envelope srsName is incorrect");
        assertEquals("-179.99999999999991 -89.999999999999943", envelope.getPositions().get(0).getPosString(), "Envelope position 0 is incorrect");
        assertEquals("180.00000000000003 84.00416666700005", envelope.getPositions().get(1).getPosString(), "Envelope position 1 is incorrect");
        assertNotNull(envelope.getPositions().get(0).getDimension(), "Envelope position 0 dimension missing");
        assertNotNull(envelope.getPositions().get(1).getDimension(), "Envelope position 1 dimension missing");
        assertEquals("2", envelope.getPositions().get(0).getDimension(), "Envelope position 0 dimension is incorrect");
        assertEquals("2", envelope.getPositions().get(1).getDimension(), "Envelope position 1 dimension is incorrect");

        List<GMLRectifiedGrid> rectifiedGrids = spatialDomain.getRectifiedGrids();
        assertNotNull(rectifiedGrids, "RectifiedGrid is null");
        assertEquals(1, rectifiedGrids.size(), "Incorrect RectifiedGrid count");
        GMLRectifiedGrid rGrid = rectifiedGrids.get(0);
        GMLLimits limits = rGrid.getLimits();
        assertNotNull(limits, "Limits is null");
        List<GMLGridEnvelope> gridEnvelopes = limits.getGridEnvelopes();
        assertNotNull(gridEnvelopes, "GridEnvelope is null");
        assertEquals(1, gridEnvelopes.size(), "Incorrect GridEnvelope count");
        assertEquals("0 0", gridEnvelopes.get(0).getLow(), "Low limit is incorrect");
        assertEquals("43199 20880", gridEnvelopes.get(0).getHigh(), "High limit is incorrect");
        List<String> axisNames = rGrid.getAxisNames();
        assertNotNull(axisNames, "AxisNames is null");
        assertEquals(2, axisNames.size(), "Incorrect AxisNames count");
        assertEquals("Raster_Pixel_Columns(X-axis)", axisNames.get(0), "Incorrect first axis name 0");
        assertEquals("Raster_Pixel_Rows(Y-axis)", axisNames.get(1), "Incorrect second axis name 0");
        GMLOrigin origin = rGrid.getOrigin();
        assertNotNull(origin, "Origin is null");
        assertEquals("-179.99583333333325 84.000000100105098", origin.getPos().getPosString(), "Incorrect origin values");
        List<String> offsetVectors = rGrid.getOffsetVectorStrings();
        assertNotNull(offsetVectors, "OffsetVectors is null");
        assertEquals(2, offsetVectors.size(), "Incorrect offsetVector count");
        assertEquals("0.0083333333333333315 0", offsetVectors.get(0), "Incorrect first offset vector");
        assertEquals("0 -0.0083331337899046985", offsetVectors.get(1), "Incorrect second offset vector");

        WCS100RangeSetHolder rangeSetHolder = coverage.getRangeSet();
        assertNotNull(rangeSetHolder, "RangeSetHolder is null");
        WCS100RangeSet rangeSet = rangeSetHolder.getRangeSet();
        assertNotNull(rangeSet, "RangeSet is null");
        assertEquals("RangeSet_1", rangeSet.getName(), "RangeSet name is incorrect");
        assertEquals("dted0_1 RangeSet", rangeSet.getLabel(), "RangeSet label is incorrect");
        List<WCS100AxisDescriptionHolder> axisDescriptionHolders = rangeSet.getAxisDescriptions();
        assertNotNull(axisDescriptionHolders, "axisDescription is null");
        assertEquals(1, axisDescriptionHolders.size(), "axisDescription count incorrect");
        WCS100AxisDescription axisDescription = axisDescriptionHolders.get(0).getAxisDescription();
        assertNotNull(axisDescription, "AxisDescription is null");
        assertEquals("Band", axisDescription.getName(), "AxisDescription name is incorrect");
        assertEquals("Band Numbers", axisDescription.getLabel(), "AxisDescription label is incorrect");
        WCS100Values values = axisDescription.getValues();
        assertNotNull(values, "Values is null");
        List<WCS100SingleValue> singleValues = values.getSingleValues();
        assertNotNull(singleValues, "SingleValues is null");
        assertEquals(1, singleValues.size(), "Incorrect singleValues count");
        assertEquals(1.0, singleValues.get(0).getSingleValue(), 0.0, "Incorrect singleValue");

        WCS100Values nullValues = rangeSet.getNullValues();
        assertNotNull(nullValues, "NullValues is null");
        singleValues = nullValues.getSingleValues();
        assertNotNull(nullValues, "NullValues SingleValues is null");
        assertEquals(1, singleValues.size(), "NullValues Incorrect singleValues count");
        assertEquals(32767.0, singleValues.get(0).getSingleValue(), 0.0, "NullValues Incorrect singleValue");

        WCS100SupportedFormats supportedFormats = coverage.getSupportedFormats();
        assertNotNull(supportedFormats, "SuppotedFormats is null");
        assertEquals(4, supportedFormats.getStrings().size(), "SupportedFormats count is incorrect");
        assertTrue(supportedFormats.getStrings().contains("GeoTIFF"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("NITF"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("HDF"), "Missing format");
        assertTrue(supportedFormats.getStrings().contains("JPEG2000"), "Missing format");
        assertEquals("GeoTIFF", supportedFormats.getNativeFormat(), "Supported formats nativeFormat is incorrect");

        WCS100SupportedCRSs supportedCRSs = coverage.getSupportedCRSs();
        assertNotNull(supportedCRSs, "SupportedCRSs is null");
        assertNotNull(supportedCRSs.getRequestResponseCRSs(), "SupportedCRSs requestResponses is null");
        assertEquals(1, supportedCRSs.getRequestResponseCRSs().size(), "SupportedCRSs requestResponse count is incorrect");
        assertEquals("EPSG:4326", supportedCRSs.getRequestResponseCRSs().get(0), "RequestResponse value is incorrect");
        assertNotNull(supportedCRSs.getNativeCRSs(), "SupportedCRSs nativeCRSs is null");
        assertEquals(1, supportedCRSs.getRequestResponseCRSs().size(), "SupportedCRSs nativeCRSs count is incorrect");
        assertEquals("EPSG:4326", supportedCRSs.getRequestResponseCRSs().get(0), "NativeCRSs value is incorrect");

        WCS100SupportedInterpolations supportedInterpolations = coverage.getSupportedInterpolations();
        assertNotNull(supportedInterpolations, "SupportedInterpolations is null");
        assertEquals(3, supportedInterpolations.getStrings().size(), "SupportedInterpolations count is incorrect");
        assertTrue(supportedInterpolations.getStrings().contains("nearest neighbor"), "Missing interpolation");
        assertTrue(supportedInterpolations.getStrings().contains("bilinear"), "Missing interpolation");
        assertTrue(supportedInterpolations.getStrings().contains("bicubic"), "Missing interpolation");
        assertEquals("nearest neighbor", supportedInterpolations.getDefault(), "Supported Interpolations default is incorrect");
    }
}
