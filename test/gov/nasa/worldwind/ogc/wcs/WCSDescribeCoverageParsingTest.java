/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.ogc.gml.*;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
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
        assertNotNull("CoverageOfferings is null", coverageOfferings);
        assertEquals("Incorrect coverage offering count", 1, coverageOfferings.size());

        WCS100CoverageOffering coverage = coverageOfferings.get(0);
        assertNotNull("CoverageOffering is null", coverage);
        assertNotNull("CoverageOffering name is null", coverage.getName());
        assertEquals("Incorrect CoverageOffering name", "WW:NASA_SRTM30_900m_Tiled", coverage.getName());
        assertNotNull("CoverageOffering label is null", coverage.getLabel());
        assertEquals("Incorrect CoverageOffering label", "NASA_SRTM30_900m_Tiled", coverage.getLabel());

        WCS100LonLatEnvelope lonLatEnvelope = coverage.getLonLatEnvelope();
        assertNotNull("LonLatEnvelope is null", lonLatEnvelope);
        assertNotNull("LonLatEnvelope positions is null", lonLatEnvelope.getPositions());
        assertEquals("Incorrect LonLatEnvelope SRS", "urn:ogc:def:crs:OGC:1.3:CRS84", lonLatEnvelope.getSRSName());
        assertEquals("Incorrect LonLatEnvelope position count", 2, lonLatEnvelope.getPositions().size());
        assertEquals("Incorrect LonLatEnvelope position 0", "-180.0 -90.0",
            lonLatEnvelope.getPositions().get(0).getPosString());
        assertEquals("Incorrect LonLatEnvelope position 1", "180.0 90.0",
            lonLatEnvelope.getPositions().get(1).getPosString());

        List<String> keywords = coverage.getKeywords();
        assertTrue("Keywords is null", keywords != null);
        assertEquals("Incorrect keyword count", 3, keywords.size());
        assertTrue("Missing keyword", keywords.contains("WCS"));
        assertTrue("Missing keyword", keywords.contains("ImageMosaic"));
        assertTrue("Missing keyword", keywords.contains("NASA_SRTM30_900m_Tiled"));

        WCS100DomainSet domainSet = coverage.getDomainSet();
        assertNotNull("DomainSet is null", domainSet);

        WCS100SpatialDomain spatialDomain = domainSet.getSpatialDomain();
        assertNotNull("SpatialDomain is null", spatialDomain);

        List<GMLEnvelope> envelopes = spatialDomain.getEnvelopes();
        assertNotNull("Envelope is null", envelopes);
        assertEquals("Incorrect envelope count", 1, envelopes.size());
        GMLEnvelope envelope = envelopes.get(0);
        assertEquals("Envelope position 0 is incorrect", "-180.0 -90.0",
            envelope.getPositions().get(0).getPosString());
        assertEquals("Envelope position 1 is incorrect", "180.0 90.0",
            envelope.getPositions().get(1).getPosString());

        List<GMLRectifiedGrid> rectifiedGrids = spatialDomain.getRectifiedGrids();
        assertNotNull("RectifiedGrid is null", rectifiedGrids);
        assertEquals("Incorrect RectifiedGrid count", 1, rectifiedGrids.size());
        GMLRectifiedGrid rGrid = rectifiedGrids.get(0);
        GMLLimits limits = rGrid.getLimits();
        assertNotNull("Limits is null", limits);
        List<GMLGridEnvelope> gridEnvelopes = limits.getGridEnvelopes();
        assertNotNull("GridEnvelope is null", gridEnvelopes);
        assertEquals("Incorrect GridEnvelope count", 1, gridEnvelopes.size());
        assertEquals("Low limit is incorrect", "0 0", gridEnvelopes.get(0).getLow());
        assertEquals("High limit is incorrect", "43199 21599", gridEnvelopes.get(0).getHigh());
        List<String> axisNames = rGrid.getAxisNames();
        assertNotNull("AxisNames is null", axisNames);
        assertEquals("Incorrect AxisNames count", 2, axisNames.size());
        assertEquals("Incorrect first axis name 0", "x", axisNames.get(0));
        assertEquals("Incorrect second axis name 0", "y", axisNames.get(1));
        GMLOrigin origin = rGrid.getOrigin();
        assertNotNull("Origin is null", origin);
        assertEquals("Incorrect origin values", "-179.99583333333334 89.99583333333334",
            origin.getPos().getPosString());
        List<String> offsetVectors = rGrid.getOffsetVectorStrings();
        assertNotNull("OffsetVectors is null", offsetVectors);
        assertEquals("Incorrect offsetVector count", 2, offsetVectors.size());
        assertEquals("Incorrect first offset vector", "0.008333333333333333 0.0", offsetVectors.get(0));
        assertEquals("Incorrect second offset vector", "0.0 -0.008333333333333333", offsetVectors.get(1));

        WCS100RangeSetHolder rangeSetHolder = coverage.getRangeSet();
        assertNotNull("RangeSetHolder is null", rangeSetHolder);
        WCS100RangeSet rangeSet = rangeSetHolder.getRangeSet();
        assertNotNull("RangeSet is null", rangeSet);
        assertEquals("RangeSet name is incorrect", "NASA_SRTM30_900m_Tiled", rangeSet.getName());
        assertEquals("RangeSet label is incorrect", "NASA_SRTM30_900m_Tiled", rangeSet.getLabel());
        List<WCS100AxisDescriptionHolder> axisDescriptionHolders = rangeSet.getAxisDescriptions();
        assertNotNull("axisDescription is null", axisDescriptionHolders);
        assertEquals("axisDescription count incorrect", 1, axisDescriptionHolders.size());
        WCS100AxisDescription axisDescription = axisDescriptionHolders.get(0).getAxisDescription();
        assertNotNull("AxisDescription is null", axisDescription);
        assertEquals("AxisDescription name is incorrect", "Band", axisDescription.getName());
        assertEquals("AxisDescription label is incorrect", "Band", axisDescription.getLabel());
        WCS100Values values = axisDescription.getValues();
        assertNotNull("Values is null", values);
        List<WCS100SingleValue> singleValues = values.getSingleValues();
        assertNotNull("SingleValues is null", singleValues);
        assertEquals("Incorrect singleValues count", 1, singleValues.size());
        assertEquals("Incorrect singleValue", 1.0, singleValues.get(0).getSingleValue(), 0.0);

        WCS100SupportedFormats supportedFormats = coverage.getSupportedFormats();
        assertNotNull("SuppotedFormats is null", supportedFormats);
        assertEquals("SupportedFormats count is incorrect", 8, supportedFormats.getStrings().size());
        assertTrue("Missing format", supportedFormats.getStrings().contains("ArcGrid"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("GeoTIFF"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("GIF"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("Gtopo30"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("ImageMosaic"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("JPEG"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("PNG"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("TIFF"));
        assertEquals("Supported formats nativeFormat is incorrect", "ImageMosaic",
            supportedFormats.getNativeFormat());

        WCS100SupportedCRSs supportedCRSs = coverage.getSupportedCRSs();
        assertNotNull("SupportedCRSs is null", supportedCRSs);
        assertNotNull("SupportedCRSs requestResponses is null", supportedCRSs.getRequestResponseCRSs());
        assertEquals("SupportedCRSs requestResponse count is incorrect", 1,
            supportedCRSs.getRequestResponseCRSs().size());
        assertEquals("RequestResponse value is incorrect", "EPSG:4326",
            supportedCRSs.getRequestResponseCRSs().get(0));

        WCS100SupportedInterpolations supportedInterpolations = coverage.getSupportedInterpolations();
        assertNotNull("SupportedInterpolations is null", supportedInterpolations);
        assertEquals("SupportedInterpolations count is incorrect", 3, supportedInterpolations.getStrings().size());
        assertTrue("Missing interpolation", supportedInterpolations.getStrings().contains("nearest neighbor"));
        assertTrue("Missing interpolation", supportedInterpolations.getStrings().contains("bilinear"));
        assertTrue("Missing interpolation", supportedInterpolations.getStrings().contains("bicubic"));
        assertEquals("Supported Interpolations default is incorrect", "nearest neighbor",
            supportedInterpolations.getDefault());
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
        assertNotNull("CoverageOfferings is null", coverageOfferings);
        assertEquals("Incorrect coverage offering count", 1, coverageOfferings.size());

        WCS100CoverageOffering coverage = coverageOfferings.get(0);
        assertNotNull("CoverageOffering is null", coverage);
        assertNotNull("CoverageOffering name is null", coverage.getName());
        assertEquals("Incorrect CoverageOffering name", "1", coverage.getName());
        assertNotNull("CoverageOffering label is null", coverage.getLabel());
        assertEquals("Incorrect CoverageOffering label", "dted0_1", coverage.getLabel());

        WCS100LonLatEnvelope lonLatEnvelope = coverage.getLonLatEnvelope();
        assertNotNull("LonLatEnvelope is null", lonLatEnvelope);
        assertNotNull("LonLatEnvelope positions is null", lonLatEnvelope.getPositions());
        assertEquals("Incorrect LonLatEnvelope SRS", "WGS84(DD)", lonLatEnvelope.getSRSName());
        assertEquals("Incorrect LonLatEnvelope position count", 2, lonLatEnvelope.getPositions().size());
        assertEquals("Incorrect LonLatEnvelope position 0", "-179.99999999999991 -89.999999999999943",
            lonLatEnvelope.getPositions().get(0).getPosString());
        assertEquals("Incorrect LonLatEnvelope position 1", "180.00000000000003 84.00416666700005",
            lonLatEnvelope.getPositions().get(1).getPosString());

        WCS100DomainSet domainSet = coverage.getDomainSet();
        assertNotNull("DomainSet is null", domainSet);

        WCS100SpatialDomain spatialDomain = domainSet.getSpatialDomain();
        assertNotNull("SpatialDomain is null", spatialDomain);

        List<GMLEnvelope> envelopes = spatialDomain.getEnvelopes();
        assertNotNull("Envelope is null", envelopes);
        assertEquals("Incorrect envelope count", 1, envelopes.size());
        GMLEnvelope envelope = envelopes.get(0);
        assertNotNull("Envelope srsName is null", envelope.getSRSName());
        assertEquals("Envelope srsName is incorrect", "EPSG:4326", envelope.getSRSName());
        assertEquals("Envelope position 0 is incorrect", "-179.99999999999991 -89.999999999999943",
            envelope.getPositions().get(0).getPosString());
        assertEquals("Envelope position 1 is incorrect", "180.00000000000003 84.00416666700005",
            envelope.getPositions().get(1).getPosString());
        assertNotNull("Envelope position 0 dimension missing", envelope.getPositions().get(0).getDimension());
        assertNotNull("Envelope position 1 dimension missing", envelope.getPositions().get(1).getDimension());
        assertEquals("Envelope position 0 dimension is incorrect", "2",
            envelope.getPositions().get(0).getDimension());
        assertEquals("Envelope position 1 dimension is incorrect", "2",
            envelope.getPositions().get(1).getDimension());

        List<GMLRectifiedGrid> rectifiedGrids = spatialDomain.getRectifiedGrids();
        assertNotNull("RectifiedGrid is null", rectifiedGrids);
        assertEquals("Incorrect RectifiedGrid count", 1, rectifiedGrids.size());
        GMLRectifiedGrid rGrid = rectifiedGrids.get(0);
        GMLLimits limits = rGrid.getLimits();
        assertNotNull("Limits is null", limits);
        List<GMLGridEnvelope> gridEnvelopes = limits.getGridEnvelopes();
        assertNotNull("GridEnvelope is null", gridEnvelopes);
        assertEquals("Incorrect GridEnvelope count", 1, gridEnvelopes.size());
        assertEquals("Low limit is incorrect", "0 0", gridEnvelopes.get(0).getLow());
        assertEquals("High limit is incorrect", "43199 20880", gridEnvelopes.get(0).getHigh());
        List<String> axisNames = rGrid.getAxisNames();
        assertNotNull("AxisNames is null", axisNames);
        assertEquals("Incorrect AxisNames count", 2, axisNames.size());
        assertEquals("Incorrect first axis name 0", "Raster_Pixel_Columns(X-axis)", axisNames.get(0));
        assertEquals("Incorrect second axis name 0", "Raster_Pixel_Rows(Y-axis)", axisNames.get(1));
        GMLOrigin origin = rGrid.getOrigin();
        assertNotNull("Origin is null", origin);
        assertEquals("Incorrect origin values", "-179.99583333333325 84.000000100105098",
            origin.getPos().getPosString());
        List<String> offsetVectors = rGrid.getOffsetVectorStrings();
        assertNotNull("OffsetVectors is null", offsetVectors);
        assertEquals("Incorrect offsetVector count", 2, offsetVectors.size());
        assertEquals("Incorrect first offset vector", "0.0083333333333333315 0", offsetVectors.get(0));
        assertEquals("Incorrect second offset vector", "0 -0.0083331337899046985", offsetVectors.get(1));

        WCS100RangeSetHolder rangeSetHolder = coverage.getRangeSet();
        assertNotNull("RangeSetHolder is null", rangeSetHolder);
        WCS100RangeSet rangeSet = rangeSetHolder.getRangeSet();
        assertNotNull("RangeSet is null", rangeSet);
        assertEquals("RangeSet name is incorrect", "RangeSet_1", rangeSet.getName());
        assertEquals("RangeSet label is incorrect", "dted0_1 RangeSet", rangeSet.getLabel());
        List<WCS100AxisDescriptionHolder> axisDescriptionHolders = rangeSet.getAxisDescriptions();
        assertNotNull("axisDescription is null", axisDescriptionHolders);
        assertEquals("axisDescription count incorrect", 1, axisDescriptionHolders.size());
        WCS100AxisDescription axisDescription = axisDescriptionHolders.get(0).getAxisDescription();
        assertNotNull("AxisDescription is null", axisDescription);
        assertEquals("AxisDescription name is incorrect", "Band", axisDescription.getName());
        assertEquals("AxisDescription label is incorrect", "Band Numbers", axisDescription.getLabel());
        WCS100Values values = axisDescription.getValues();
        assertNotNull("Values is null", values);
        List<WCS100SingleValue> singleValues = values.getSingleValues();
        assertNotNull("SingleValues is null", singleValues);
        assertEquals("Incorrect singleValues count", 1, singleValues.size());
        assertEquals("Incorrect singleValue", 1.0, singleValues.get(0).getSingleValue(), 0.0);

        WCS100Values nullValues = rangeSet.getNullValues();
        assertNotNull("NullValues is null", nullValues);
        singleValues = nullValues.getSingleValues();
        assertNotNull("NullValues SingleValues is null", nullValues);
        assertEquals("NullValues Incorrect singleValues count", 1, singleValues.size());
        assertEquals("NullValues Incorrect singleValue", 32767.0, singleValues.get(0).getSingleValue(), 0.0);

        WCS100SupportedFormats supportedFormats = coverage.getSupportedFormats();
        assertNotNull("SuppotedFormats is null", supportedFormats);
        assertEquals("SupportedFormats count is incorrect", 4, supportedFormats.getStrings().size());
        assertTrue("Missing format", supportedFormats.getStrings().contains("GeoTIFF"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("NITF"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("HDF"));
        assertTrue("Missing format", supportedFormats.getStrings().contains("JPEG2000"));
        assertEquals("Supported formats nativeFormat is incorrect", "GeoTIFF",
            supportedFormats.getNativeFormat());

        WCS100SupportedCRSs supportedCRSs = coverage.getSupportedCRSs();
        assertNotNull("SupportedCRSs is null", supportedCRSs);
        assertNotNull("SupportedCRSs requestResponses is null", supportedCRSs.getRequestResponseCRSs());
        assertEquals("SupportedCRSs requestResponse count is incorrect", 1,
            supportedCRSs.getRequestResponseCRSs().size());
        assertEquals("RequestResponse value is incorrect", "EPSG:4326",
            supportedCRSs.getRequestResponseCRSs().get(0));
        assertNotNull("SupportedCRSs nativeCRSs is null", supportedCRSs.getNativeCRSs());
        assertEquals("SupportedCRSs nativeCRSs count is incorrect", 1,
            supportedCRSs.getRequestResponseCRSs().size());
        assertEquals("NativeCRSs value is incorrect", "EPSG:4326",
            supportedCRSs.getRequestResponseCRSs().get(0));

        WCS100SupportedInterpolations supportedInterpolations = coverage.getSupportedInterpolations();
        assertNotNull("SupportedInterpolations is null", supportedInterpolations);
        assertEquals("SupportedInterpolations count is incorrect", 3, supportedInterpolations.getStrings().size());
        assertTrue("Missing interpolation", supportedInterpolations.getStrings().contains("nearest neighbor"));
        assertTrue("Missing interpolation", supportedInterpolations.getStrings().contains("bilinear"));
        assertTrue("Missing interpolation", supportedInterpolations.getStrings().contains("bicubic"));
        assertEquals("Supported Interpolations default is incorrect", "nearest neighbor",
            supportedInterpolations.getDefault());
    }
}
