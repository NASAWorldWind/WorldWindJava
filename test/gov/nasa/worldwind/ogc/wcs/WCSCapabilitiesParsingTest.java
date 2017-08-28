/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.ogc.ows.*;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class WCSCapabilitiesParsingTest
{
    @Test
    public void testParsing001()
    {
        WCS100Capabilities caps = new WCS100Capabilities("testData/WCS/WCSCapabilities003.xml");

        try
        {
            caps.parse();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        assertNotNull("Version is null", caps.getVersion());
        assertEquals("Incorrect version number", "1.0.0", caps.getVersion());
        assertEquals("Incorrect update sequence", "2013-06-28T16:26:00Z", caps.getUpdateSequence());

        WCS100Service service = caps.getService();
        assertNotNull("Service is null", service);

        WCS100MetadataLink metadataLink = service.getMetadataLink();
        assertNotNull("MetadataLink is null", metadataLink);
        assertEquals("Incorrect metadataLink href", "http://worldwind26.arc.nasa.gov", metadataLink.getHref());
        assertEquals("Incorrect type value", "simple", metadataLink.getType());
        assertEquals("Incorrect metadataType value", "TC211", metadataLink.getMetadataType());

        String description = service.getDescription();
        assertNotNull("Service description is null", description);
        assertTrue("Incorrect description", description.startsWith("WorldWind MapServer Elevation test"));

        assertNotNull("Service name is null", service.getName());
        assertEquals("Incorrect service name", "MapServer WCS", service.getName());

        assertNotNull("Service label is null", service.getLabel());
        assertEquals("Incorrect service label", "WorldWind MapServer Elevation", service.getLabel());

        List<String> keywords = service.getKeywords();
        assertTrue("Keywords is null", keywords != null);
        assertEquals("Incorrect keyword count", 5, keywords.size());
        assertTrue("Missing keyword", keywords.contains("wcs"));
        assertTrue("Missing keyword", keywords.contains("test"));
        assertTrue("Missing keyword", keywords.contains("FAA"));
        assertTrue("Missing keyword", keywords.contains("charts"));
        assertTrue("Missing keyword", keywords.contains("aeronautical"));

        WCS100ResponsibleParty responsibleParty = service.getResponsibleParty();
        assertNotNull("ResponsibleParty is null", responsibleParty);
        assertNotNull("IndividualName is null", responsibleParty.getIndividualName());
        assertEquals("Incorrect individualName", "Randolph Kim", responsibleParty.getIndividualName());
        assertNotNull("OrganisationName is null", responsibleParty.getOrganisationName());
        assertEquals("Incorrect organisationName", "NASA", responsibleParty.getOrganisationName());
        assertNotNull("PostionName is null", responsibleParty.getPositionName());
        assertEquals("Incorrect positionName", "manager", responsibleParty.getPositionName());
        OWSContactInfo contactInfo = responsibleParty.getContactInfo();
        assertNotNull("ContactInfo is null", contactInfo);
        OWSAddress address = contactInfo.getAddress();
        assertNotNull("Address is null", address);
        assertNotNull("City is null", address.getCity());
        assertEquals("Incorrect city", "Moffett Field", address.getCity());
        assertNotNull("Country is null", address.getCountries().get(0));
        assertEquals("Incorrect country", "USA", address.getCountries().get(0));
        assertNotNull("ElectronicMailAddress is null", address.getElectronicMailAddresses().get(0));
        assertEquals("Incorrect electronicMailAddress", "none@nasa.gov", address.getElectronicMailAddresses().get(0));
        assertNotNull("DeliveryPoint is null", address.getDeliveryPoints().get(0));
        assertEquals("Incorrect deliveryPoint", "NASA Ames Research Center", address.getDeliveryPoints().get(0));
        assertNotNull("AdministrativeArea is null", address.getAdministrativeArea());
        assertEquals("Incorrect deliveryPoint", "CA", address.getAdministrativeArea());
        OWSPhone phone = contactInfo.getPhone();
        assertNotNull("Phone is null", phone);
        assertNotNull("Voice is null", phone.getVoices().get(0));
        assertEquals("Incorrect voice", "000-000-0000", phone.getVoices().get(0));
        assertNotNull("Facsimile is null", phone.getFacsimiles().get(0));
        assertEquals("Incorrect facsimile", "000-000-0000", phone.getFacsimiles().get(0));
        assertEquals("Incorrect contactInfo onlineResource href", "http://worldwind26.arc.nasa.gov/wms2?",
            contactInfo.getOnlineResource());

        assertNotNull("Fees is null", service.getFees());
        assertEquals("Incorrect country", "none", service.getFees());

        List<String> accessConstraints = service.getAccessConstraints();
        assertNotNull("AccessConstraints is null", accessConstraints);
        assertEquals("Incorrect number of access constraints", 1, accessConstraints.size());
        assertEquals("Incorrect accessConstraint", "none", accessConstraints.iterator().next());

        WCS100Capability capability = caps.getCapability();
        assertNotNull("Capability is null", capability);

        WCS100Request request = capability.getRequest();
        assertNotNull("Request is null", request);
        assertNotNull("Request descriptions is null", request.getRequests());
        assertEquals("Incorrect request description count", 3, request.getRequests().size());
        assertNotNull("GetCapabilities request description is null", request.getRequest("GetCapabilities"));
        assertNotNull("DescribeCoverage request description is null", request.getRequest("DescribeCoverage"));
        assertNotNull("GetCoverage request description is null", request.getRequest("GetCoverage"));
        checkRequestDescription(request.getRequest("GetCapabilities"), "http://worldwind26.arc.nasa.gov/wms2?");
        checkRequestDescription(request.getRequest("DescribeCoverage"), "http://worldwind26.arc.nasa.gov/wms2?");
        checkRequestDescription(request.getRequest("GetCoverage"), "http://worldwind26.arc.nasa.gov/wms2?");

        WCS100Exception exception = capability.getException();
        assertNotNull("Exception is null", exception);
        assertNotNull("Exception Formats is null", exception.getFormats());
        assertEquals("Incorrect exception format count", 1, exception.getFormats().size());
        Iterator<String> iterator = exception.getFormats().iterator();
        assertEquals("Incorrect exception format", "application/vnd.ogc.se_xml", iterator.next());

        assertNotNull("ContentMetadata is null", caps.getContentMetadata());
        List<WCS100CoverageOfferingBrief> coverages = caps.getContentMetadata().getCoverageOfferings();
        assertNotNull("CoverageOfferingBriefs is null", coverages);
        assertEquals("Incorrect CoverageOfferingBrief description count", 6, coverages.size());

        WCS100CoverageOfferingBrief coverage = coverages.get(0);
        assertNotNull("CoverageOfferingBrief 0 is null", coverage);
        assertNotNull("CoverageOfferingBrief 0 name is null", coverage.getName());
        assertEquals("Incorrect CoverageOfferingBrief 0 name", "aster_v2", coverage.getName());
        assertNotNull("CoverageOfferingBrief 0 label is null", coverage.getLabel());
        assertEquals("Incorrect CoverageOfferingBrief 0 label", "ASTER version 2", coverage.getLabel());
        WCS100LonLatEnvelope envelope = coverage.getLonLatEnvelope();
        assertNotNull("LonLatEnvelope 0 is null", envelope);
        assertNotNull("LonLatEnvelope 0 positions is null", envelope.getPositions());
        assertEquals("Incorrect LonLatEnvelope 0 SRS", "urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName());
        assertEquals("Incorrect LonLatEnvelope 0 position count", 2, envelope.getPositions().size());
        assertEquals("Incorrect LonLatEnvelope 0 position 0", "-180 -83",
            envelope.getPositions().get(0).getPosString());
        assertEquals("Incorrect LonLatEnvelope 0 position 1", "180 83", envelope.getPositions().get(1).getPosString());

        coverage = coverages.get(1);
        assertNotNull("CoverageOfferingBrief 1 is null", coverage);
        assertNotNull("CoverageOfferingBrief 1 name is null", coverage.getName());
        assertEquals("Incorrect CoverageOfferingBrief 1 name", "USGS-NED", coverage.getName());
        assertNotNull("CoverageOfferingBrief 1 label is null", coverage.getLabel());
        assertEquals("Incorrect CoverageOfferingBrief 1 label", "USGS NED", coverage.getLabel());
        envelope = coverage.getLonLatEnvelope();
        assertNotNull("LonLatEnvelope 1 is null", envelope);
        assertNotNull("LonLatEnvelope 1 positions is null", envelope.getPositions());
        assertEquals("Incorrect LonLatEnvelope 1 SRS", "urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName());
        assertEquals("Incorrect LonLatEnvelope 1 position count", 2, envelope.getPositions().size());
        assertEquals("Incorrect LonLatEnvelope 1 position 0", "-125 25", envelope.getPositions().get(0).getPosString());
        assertEquals("Incorrect LonLatEnvelope 1 position 1", "-65.5 50",
            envelope.getPositions().get(1).getPosString());

        // There are more CoverageOfferingBrief elements in the file, but testing the two above is adequate.
    }

    @Test
    public void testParsing002()
    {
        WCS100Capabilities caps = new WCS100Capabilities("testData/WCS/WCSCapabilities002.xml");

        try
        {
            caps.parse();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        assertNotNull("Version is null", caps.getVersion());
        assertEquals("Incorrect version number", "1.0.0", caps.getVersion());
        assertEquals("Incorrect update sequence", "105", caps.getUpdateSequence());

        WCS100Service service = caps.getService();
        assertNotNull("Service is null", service);

        WCS100MetadataLink metadataLink = service.getMetadataLink();
        assertNotNull("MetadataLink is null", metadataLink);
        assertEquals("Incorrect metadataLink about value", "http://geoserver.sourceforge.net/html/index.php",
            metadataLink.getField("about"));
        assertEquals("Incorrect metadataLink type value", "simple", metadataLink.getField("type"));
        assertEquals("Incorrect metadataLink metadataType value", "other", metadataLink.getField("metadataType"));

        String description = service.getDescription();
        assertNotNull("Service description is null", description);
        assertTrue("Incorrect description",
            description.startsWith("This server implements the WCS specification 1.0"));

        assertNotNull("Service name is null", service.getName());
        assertEquals("Incorrect service name", "WCS", service.getName());

        assertNotNull("Service label is null", service.getLabel());
        assertEquals("Incorrect service label", "Web Coverage Service", service.getLabel());

        List<String> keywords = service.getKeywords();
        assertTrue("Keywords is null", keywords != null);
        assertEquals("Incorrect keyword count", 3, keywords.size());
        assertTrue("Missing keyword", keywords.contains("WCS"));
        assertTrue("Missing keyword", keywords.contains("WMS"));
        assertTrue("Missing keyword", keywords.contains("GEOSERVER"));

        WCS100ResponsibleParty responsibleParty = service.getResponsibleParty();
        assertNotNull("ResponsibleParty is null", responsibleParty);
        assertNotNull("IndividualName is null", responsibleParty.getIndividualName());
        assertEquals("Incorrect individualName", "Claudius Ptolomaeus", responsibleParty.getIndividualName());
        assertNotNull("OrganisationName is null", responsibleParty.getOrganisationName());
        assertEquals("Incorrect organisationName", "The ancient geographes INC",
            responsibleParty.getOrganisationName());
        assertNotNull("PostionName is null", responsibleParty.getPositionName());
        assertEquals("Incorrect positionName", "Chief geographer", responsibleParty.getPositionName());
        OWSContactInfo contactInfo = responsibleParty.getContactInfo();
        assertNotNull("ContactInfo is null", contactInfo);
        OWSAddress address = contactInfo.getAddress();
        assertNotNull("Address is null", address);
        assertNotNull("City is null", address.getCity());
        assertEquals("Incorrect city", "Alexandria", address.getCity());
        assertNotNull("Country is null", address.getCountries());
        assertEquals("Incorrect country", "Egypt", address.getCountries().get(0));
        assertNotNull("ElectronicMailAddress is null", address.getElectronicMailAddresses());
        assertEquals("Incorrect electronicMailAddress", "claudius.ptolomaeus@gmail.com",
            address.getElectronicMailAddresses().get(0));

        assertNotNull("Fees is null", service.getFees());
        assertEquals("Incorrect country", "NONE", service.getFees());

        List<String> accessConstraints = service.getAccessConstraints();
        assertNotNull("AccessConstraints is null", accessConstraints);
        assertEquals("Incorrect number of access constraints", 1, accessConstraints.size());
        assertEquals("Incorrect accessConstraint", "NONE", accessConstraints.iterator().next());

        WCS100Capability capability = caps.getCapability();
        assertNotNull("Capability is null", capability);

        WCS100Request request = capability.getRequest();
        assertNotNull("Request is null", request);
        assertNotNull("Request descriptions is null", request.getRequests());
        assertEquals("Incorrect request description count", 3, request.getRequests().size());
        assertNotNull("GetCapabilities request description is null", request.getRequest("GetCapabilities"));
        assertNotNull("DescribeCoverage request description is null", request.getRequest("DescribeCoverage"));
        assertNotNull("GetCoverage request description is null", request.getRequest("GetCoverage"));
        checkRequestDescription(request.getRequest("GetCapabilities"), "http://10.0.1.198:8080/geoserver/wcs?");
        checkRequestDescription(request.getRequest("DescribeCoverage"), "http://10.0.1.198:8080/geoserver/wcs?");
        checkRequestDescription(request.getRequest("GetCoverage"), "http://10.0.1.198:8080/geoserver/wcs?");

        WCS100Exception exception = capability.getException();
        assertNotNull("Exception is null", exception);
        assertNotNull("Exception Formats is null", exception.getFormats());
        assertEquals("Incorrect exception format count", 1, exception.getFormats().size());
        Iterator<String> iterator = exception.getFormats().iterator();
        assertEquals("Incorrect exception format", "application/vnd.ogc.se_xml", iterator.next());

        assertNotNull("ContentMetadata is null", caps.getContentMetadata());
        List<WCS100CoverageOfferingBrief> coverages = caps.getContentMetadata().getCoverageOfferings();
        assertNotNull("CoverageOfferingBriefs is null", coverages);
        assertEquals("Incorrect CoverageOfferingBrief description count", 7, coverages.size());

        WCS100CoverageOfferingBrief coverage = coverages.get(0);
        assertNotNull("CoverageOfferingBrief 0 is null", coverage);
        assertNotNull("CoverageOfferingBrief 0 description is null", coverage.getDescription());
        assertEquals("Incorrect CoverageOfferingBrief 0 description", "Generated from arcGridSample",
            coverage.getDescription());
        assertNotNull("CoverageOfferingBrief 0 name is null", coverage.getName());
        assertEquals("Incorrect CoverageOfferingBrief 0 name", "nurc:Arc_Sample",
            coverage.getName());
        assertNotNull("CoverageOfferingBrief 0 label is null", coverage.getLabel());
        assertEquals("Incorrect CoverageOfferingBrief 0 label", "A sample ArcGrid file",
            coverage.getLabel());
        WCS100LonLatEnvelope envelope = coverage.getLonLatEnvelope();
        assertNotNull("LonLatEnvelope 0 is null", envelope);
        assertNotNull("LonLatEnvelope 0 positions is null", envelope.getPositions());
        assertEquals("Incorrect LonLatEnvelope 0 SRS", "urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName());
        assertEquals("Incorrect LonLatEnvelope 0 position count", 2, envelope.getPositions().size());
        assertEquals("Incorrect LonLatEnvelope 0 position 0", "-180.0 -90.0",
            envelope.getPositions().get(0).getPosString());
        assertEquals("Incorrect LonLatEnvelope 0 position 1", "180.0 90.0",
            envelope.getPositions().get(1).getPosString());
        keywords = coverage.getKeywords();
        assertTrue("Keywords is null for CoverageOfferingBrief 0", keywords != null);
        assertEquals("Incorrect keyword count for CoverageOfferingBrief 0", 3, keywords.size());
        assertTrue("Missing keyword for CoverageOfferingBrief 0", keywords.contains("WCS"));
        assertTrue("Missing keyword for CoverageOfferingBrief 0", keywords.contains("arcGridSample"));
        assertTrue("Missing keyword for CoverageOfferingBrief 0", keywords.contains("arcGridSample_Coverage"));

        coverage = coverages.get(1);
        assertNotNull("CoverageOfferingBrief 1 is null", coverage);
        assertNotNull("CoverageOfferingBrief 1 description is null", coverage.getDescription());
        assertEquals("Incorrect CoverageOfferingBrief 1 description", "Generated from ImageMosaic",
            coverage.getDescription());
        assertNotNull("CoverageOfferingBrief 1 name is null", coverage.getName());
        assertEquals("Incorrect CoverageOfferingBrief 1 name", "WW:aster_v2",
            coverage.getName());
        assertNotNull("CoverageOfferingBrief 1 label is null", coverage.getLabel());
        assertEquals("Incorrect CoverageOfferingBrief 1 label", "ASTER",
            coverage.getLabel());
        envelope = coverage.getLonLatEnvelope();
        assertNotNull("LonLatEnvelope 1 is null", envelope);
        assertNotNull("LonLatEnvelope 1 positions is null", envelope.getPositions());
        assertEquals("Incorrect LonLatEnvelope 1 SRS", "urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName());
        assertEquals("Incorrect LonLatEnvelope 1 position count", 2, envelope.getPositions().size());
        assertEquals("Incorrect LonLatEnvelope 1 position 0", "-180.0001388888889 -83.0001388888889",
            envelope.getPositions().get(0).getPosString());
        assertEquals("Incorrect LonLatEnvelope 1 position 1", "180.00013888888887 83.00013888888888",
            envelope.getPositions().get(1).getPosString());
        keywords = coverage.getKeywords();
        assertTrue("Keywords is null for CoverageOfferingBrief 1", keywords != null);
        assertEquals("Incorrect keyword count for CoverageOfferingBrief 1", 3, keywords.size());
        assertTrue("Missing keyword for CoverageOfferingBrief 1", keywords.contains("WCS"));
        assertTrue("Missing keyword for CoverageOfferingBrief 1", keywords.contains("ImageMosaic"));
        assertTrue("Missing keyword for CoverageOfferingBrief 1", keywords.contains("ASTER"));

        // There are more CoverageOfferingBrief elements in the file, but testing the two above is adequate.
    }

    @Test
    public void testParsing003()
    {
        WCSCapabilities caps = new WCSCapabilities("testData/WCS/WCSCapabilities001.xml");

        try
        {
            caps.parse();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        assertNotNull("Version is null", caps.getVersion());
        assertEquals("Incorrect version number", "1.1.1", caps.getVersion());
        assertEquals("Incorrect update sequence", "99", caps.getUpdateSequence());

        OWSServiceIdentification serviceIdentification = caps.getServiceIdentification();
        assertNotNull("Service Identification is null", serviceIdentification);
        assertEquals("Incorrect Fees", "NONE", serviceIdentification.getFees());
        assertEquals("Incorrect ServiceType", "WCS", serviceIdentification.getServiceType());

        List<String> titles = serviceIdentification.getTitles();
        assertTrue("Titles is null", titles != null);
        assertEquals("Incorrect Title count", 1, titles.size());
        for (String title : titles)
        {
            assertEquals("Incorrect Title", "Web Coverage Service", title);
        }

        List<String> abstracts = serviceIdentification.getAbstracts();
        assertTrue("Abstracts is null", abstracts != null);
        assertEquals("Incorrect Abstract count", 1, abstracts.size());
        for (String abs : abstracts)
        {
            assertTrue("Incorrect Abstract start", abs.startsWith("This server implements"));
            assertTrue("Incorrect Abstract end", abs.endsWith("available on WMS also."));
        }

        List<String> keywords = serviceIdentification.getKeywords();
        assertTrue("Keywords is null", keywords != null);
        assertEquals("Incorrect Keyword count", 3, keywords.size());
        assertTrue("Missing Keyword", keywords.contains("WCS"));
        assertTrue("Missing Keyword", keywords.contains("WMS"));
        assertTrue("Missing Keyword", keywords.contains("GEOSERVER"));

        List<String> serviceTypeVersions = serviceIdentification.getServiceTypeVersions();
        assertTrue("ServiceTypeVersions is null", serviceTypeVersions != null);
        assertEquals("Incorrect ServiceTypeVersion count", 2, serviceTypeVersions.size());
        assertTrue("Missing Keyword", serviceTypeVersions.contains("1.1.0"));
        assertTrue("Missing Keyword", serviceTypeVersions.contains("1.1.1"));

        List<String> accessConstraints = serviceIdentification.getAccessConstraints();
        assertTrue("AccessConstraints is null", accessConstraints != null);
        assertEquals("Incorrect AccessConstraints count", 1, abstracts.size());
        for (String abs : accessConstraints)
        {
            assertEquals("Incorrect AccessConstraint", "NONE", abs);
        }

        OWSServiceProvider serviceProvider = caps.getServiceProvider();
        assertTrue("ServiceProvider is null", serviceProvider != null);
        assertEquals("ProviderName is incorrect", "The ancient geographes INC", serviceProvider.getProviderName());
        assertEquals("ProviderSite is incorrect", "http://geoserver.org", serviceProvider.getProviderSite());

        OWSServiceContact serviceContact = serviceProvider.getServiceContact();
        assertTrue("ServiceContact is null", serviceContact != null);
        assertEquals("IndividualName is incorrect", "Claudius Ptolomaeus", serviceContact.getIndividualName());
        assertEquals("PositionName is incorrect", "Chief geographer", serviceContact.getPositionName());

        OWSContactInfo contactInfo = serviceContact.getContactInfo();
        assertTrue("ContactInfo is null", contactInfo != null);
        assertEquals("OnlineResource is incorrect", "http://geoserver.org", contactInfo.getOnlineResource());

        OWSPhone phone = contactInfo.getPhone();
        assertTrue("Phone is null", phone != null);

        OWSAddress address = contactInfo.getAddress();
        assertTrue("Address is null", address != null);
        assertEquals("City is incorrect", "Alexandria", address.getCity());

        List<String> countries = address.getCountries();
        assertTrue("Countries is null", countries != null);
        assertEquals("Incorrect Country count", 1, countries.size());
        for (String country : countries)
        {
            assertEquals("Incorrect Country", "Egypt", country);
        }

        List<String> emails = address.getElectronicMailAddresses();
        assertTrue("ElectronicMailAddress is null", emails != null);
        assertEquals("Incorrect ElectronicMailAddress count", 1, emails.size());
        for (String email : emails)
        {
            assertEquals("Incorrect ElectronicMailAddress", "claudius.ptolomaeus@gmail.com", email);
        }

        OWSOperationsMetadata operationsMetadata = caps.getOperationsMetadata();
        assertTrue("OperationsMetadata is null", operationsMetadata != null);

        List<OWSOperation> operations = operationsMetadata.getOperations();
        assertTrue("Operations is null", operations != null);
        assertEquals("Incorrect Operation count", 3, operations.size());
        Set<String> operationNames = new HashSet<String>(3);
        for (OWSOperation operation : operations)
        {
            operationNames.add(operation.getName());
        }
        assertTrue("Missing Operation", operationNames.contains("GetCapabilities"));
        assertTrue("Missing Operation", operationNames.contains("DescribeCoverage"));
        assertTrue("Missing Operation", operationNames.contains("GetCoverage"));

        for (OWSOperation operation : operations)
        {
            List<OWSDCP> dcps = operation.getDCPs();
            assertTrue("DCPs is null", dcps != null);
            assertEquals("Incorrect DCP count", 2, dcps.size());

            for (OWSDCP dcp : dcps)
            {
                assertTrue("DCP HTTP is null", dcp.getHTTP() != null);
            }
        }

        String url = operationsMetadata.getGetOperationAddress("Get", "GetCapabilities");
        assertTrue("Get operation address is null", url != null);
        assertEquals("Incorrect HTTP address", "http://10.0.1.198:8080/geoserver/wcs?", url);
        url = operationsMetadata.getGetOperationAddress("Post", "GetCapabilities");
        assertTrue("Get operation address is null", url != null);
        assertEquals("Incorrect HTTP address", "http://10.0.1.198:8080/geoserver/wcs?", url);

        url = operationsMetadata.getGetOperationAddress("Get", "DescribeCoverage");
        assertTrue("Get operation address is null", url != null);
        assertEquals("Incorrect HTTP address", "http://10.0.1.198:8080/geoserver/wcs?", url);
        url = operationsMetadata.getGetOperationAddress("Post", "DescribeCoverage");
        assertTrue("Get operation address is null", url != null);
        assertEquals("Incorrect HTTP address", "http://10.0.1.198:8080/geoserver/wcs?", url);

        url = operationsMetadata.getGetOperationAddress("Get", "GetCoverage");
        assertTrue("Get operation address is null", url != null);
        assertEquals("Incorrect HTTP address", "http://10.0.1.198:8080/geoserver/wcs?", url);
        url = operationsMetadata.getGetOperationAddress("Post", "GetCoverage");
        assertTrue("Get operation address is null", url != null);
        assertEquals("Incorrect HTTP address", "http://10.0.1.198:8080/geoserver/wcs?", url);

        OWSOperation coverageOp = operationsMetadata.getOperation("GetCoverage");
        List<OWSParameter> parameters = coverageOp.getParameters();
        assertTrue("Operation Parameters is null", parameters != null);
        assertEquals("Operation Parameter count is incorrect", 1, parameters.size());
        for (OWSParameter parameter : parameters)
        {
            assertTrue("Store parameter is missing", parameter.getName() != null);
            assertEquals("Incorrect store value", "store", parameter.getName());

            List<OWSAllowedValues> allowedValues = parameter.getAllowedValues();
            assertTrue("AllowedValues is null", allowedValues != null);
            assertEquals("AllowedValues count is incorrect", 1, allowedValues.size());
            for (OWSAllowedValues avs : allowedValues)
            {
                List<String> avals = avs.getValues();
                assertTrue("AllowedValues values is null", avals != null);
                assertEquals("Allowed Values values count is incorrect", 2, avals.size());
                assertTrue("Missing allowed value", avals.contains("True"));
                assertTrue("Missing allowed value", avals.contains("False"));
            }
        }

        List<OWSConstraint> constraints = operationsMetadata.getConstraints();
        assertTrue("Constraints is null", constraints != null);
        assertEquals("Incorrect Constraint count", 1, constraints.size());
        for (OWSConstraint constraint : constraints)
        {
            assertEquals("Incorrect Constraint", "PostEncoding", constraint.getName());

            List<OWSAllowedValues> allowedValues = constraint.getAllowedValues();
            assertTrue("AllowedValues is null", allowedValues != null);
            assertEquals("AllowedValues count is incorrect", 1, allowedValues.size());
            for (OWSAllowedValues avs : allowedValues)
            {
                List<String> avals = avs.getValues();
                assertTrue("AllowedValues values is null", avals != null);
                assertEquals("Allowed Values values count is incorrect", 1, avals.size());
                assertTrue("Missing allowed value", avals.contains("XML"));
            }
        }

        WCSContents contents = caps.getContents();
        assertTrue("WCS Contents is missing", contents != null);

        List<WCSCoverageSummary> coverageSummaries = contents.getCoverageSummaries();
        assertTrue("WCS CoverageSummarys are missing", coverageSummaries != null);
        assertEquals("WCS CoverageSummarys count is incorrect", 7, coverageSummaries.size());

        Set<String> identifiers = new HashSet<String>(coverageSummaries.size());
        for (WCSCoverageSummary summary : coverageSummaries)
        {
            identifiers.add(summary.getIdentifier());
        }
        assertTrue("Missing CoverageSummary Identifier", identifiers.contains("Arc_Sample"));
        assertTrue("Missing CoverageSummary Identifier", identifiers.contains("aster_v2"));
        assertTrue("Missing CoverageSummary Identifier", identifiers.contains("FAAChartsCroppedReprojected"));
        assertTrue("Missing CoverageSummary Identifier", identifiers.contains("NASA_SRTM30_900m_Tiled"));
        assertTrue("Missing CoverageSummary Identifier", identifiers.contains("Img_Sample"));
        assertTrue("Missing CoverageSummary Identifier", identifiers.contains("mosaic"));
        assertTrue("Missing CoverageSummary Identifier", identifiers.contains("sfdem"));

        for (WCSCoverageSummary summary : coverageSummaries)
        {
            if (summary.getIdentifier().equals("Arc_Sample"))
            {
                assertEquals("CoverageSummary Title is incorrect", "A sample ArcGrid file", summary.getTitle());
                assertEquals("CoverageSummary Abstract is incorrect", "Generated from arcGridSample",
                    summary.getAbstract());

                keywords = summary.getKeywords();
                assertTrue("Keywords is null", keywords != null);
                assertEquals("Incorrect Keyword count", 3, keywords.size());
                assertTrue("Missing Keyword", keywords.contains("WCS"));
                assertTrue("Missing Keyword", keywords.contains("arcGridSample"));
                assertTrue("Missing Keyword", keywords.contains("arcGridSample_Coverage"));

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue("BoundingBox is null", bbox != null);
                assertEquals("LowerCorner is incorrect", "-180.0 -90.0", bbox.getLowerCorner());
                assertEquals("UpperCorner is incorrect", "180.0 90.0", bbox.getUpperCorner());
            }
            else if (summary.getIdentifier().equals("aster_v2"))
            {
                assertEquals("CoverageSummary Title is incorrect", "ASTER", summary.getTitle());
                assertEquals("CoverageSummary Abstract is incorrect", "Generated from ImageMosaic",
                    summary.getAbstract());

                keywords = summary.getKeywords();
                assertTrue("Keywords is null", keywords != null);
                assertEquals("Incorrect Keyword count", 3, keywords.size());
                assertTrue("Missing Keyword", keywords.contains("WCS"));
                assertTrue("Missing Keyword", keywords.contains("ImageMosaic"));
                assertTrue("Missing Keyword", keywords.contains("ASTER"));

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue("BoundingBox is null", bbox != null);
                assertEquals("LowerCorner is incorrect", "-180.0001388888889 -83.0001388888889",
                    bbox.getLowerCorner());
                assertEquals("UpperCorner is incorrect", "180.00013888888887 83.00013888888888",
                    bbox.getUpperCorner());
            }
            else if (summary.getIdentifier().equals("FAAChartsCroppedReprojected"))
            {
                assertEquals("CoverageSummary Title is incorrect", "FAAChartsCroppedReprojected",
                    summary.getTitle());
                assertEquals("CoverageSummary Abstract is incorrect", "Generated from ImageMosaic",
                    summary.getAbstract());

                keywords = summary.getKeywords();
                assertTrue("Keywords is null", keywords != null);
                assertEquals("Incorrect Keyword count", 3, keywords.size());
                assertTrue("Missing Keyword", keywords.contains("WCS"));
                assertTrue("Missing Keyword", keywords.contains("ImageMosaic"));
                assertTrue("Missing Keyword", keywords.contains("FAAChartsCroppedReprojected"));

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue("BoundingBox is null", bbox != null);
                assertEquals("LowerCorner is incorrect", "-173.4897609604564 50.896520942672375",
                    bbox.getLowerCorner());
                assertEquals("UpperCorner is incorrect", "178.65474058869506 72.33574978977076",
                    bbox.getUpperCorner());
            }
            else if (summary.getIdentifier().equals("NASA_SRTM30_900m_Tiled"))
            {
                assertEquals("CoverageSummary Title is incorrect", "NASA_SRTM30_900m_Tiled", summary.getTitle());
                assertEquals("CoverageSummary Abstract is incorrect", "Generated from ImageMosaic",
                    summary.getAbstract());

                keywords = summary.getKeywords();
                assertTrue("Keywords is null", keywords != null);
                assertEquals("Incorrect Keyword count", 3, keywords.size());
                assertTrue("Missing Keyword", keywords.contains("WCS"));
                assertTrue("Missing Keyword", keywords.contains("ImageMosaic"));
                assertTrue("Missing Keyword", keywords.contains("NASA_SRTM30_900m_Tiled"));

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue("BoundingBox is null", bbox != null);
                assertEquals("LowerCorner is incorrect", "-180.0 -90.0", bbox.getLowerCorner());
                assertEquals("UpperCorner is incorrect", "180.0 90.0", bbox.getUpperCorner());
            }
            else if (summary.getIdentifier().equals("Img_Sample"))
            {
                assertEquals("CoverageSummary Title is incorrect", "North America sample imagery",
                    summary.getTitle());
                assertEquals("CoverageSummary Abstract is incorrect", "A very rough imagery of North America",
                    summary.getAbstract());

                keywords = summary.getKeywords();
                assertTrue("Keywords is null", keywords != null);
                assertEquals("Incorrect Keyword count", 3, keywords.size());
                assertTrue("Missing Keyword", keywords.contains("WCS"));
                assertTrue("Missing Keyword", keywords.contains("worldImageSample"));
                assertTrue("Missing Keyword", keywords.contains("worldImageSample_Coverage"));

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue("BoundingBox is null", bbox != null);
                assertEquals("LowerCorner is incorrect", "-130.85168 20.7052", bbox.getLowerCorner());
                assertEquals("UpperCorner is incorrect", "-62.0054 54.1141", bbox.getUpperCorner());
            }
            else if (summary.getIdentifier().equals("mosaic"))
            {
                assertEquals("CoverageSummary Title is incorrect", "mosaic", summary.getTitle());
                assertEquals("CoverageSummary Abstract is incorrect", "Generated from ImageMosaic",
                    summary.getAbstract());

                keywords = summary.getKeywords();
                assertTrue("Keywords is null", keywords != null);
                assertEquals("Incorrect Keyword count", 3, keywords.size());
                assertTrue("Missing Keyword", keywords.contains("WCS"));
                assertTrue("Missing Keyword", keywords.contains("ImageMosaic"));
                assertTrue("Missing Keyword", keywords.contains("mosaic"));

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue("BoundingBox is null", bbox != null);
                assertEquals("LowerCorner is incorrect", "6.346 36.492", bbox.getLowerCorner());
                assertEquals("UpperCorner is incorrect", "20.83 46.591", bbox.getUpperCorner());
            }
            else if (summary.getIdentifier().equals("sfdem"))
            {
                assertEquals("CoverageSummary Title is incorrect",
                    "sfdem is a Tagged Image File Format with Geographic information", summary.getTitle());
                assertEquals("CoverageSummary Abstract is incorrect", "Generated from sfdem",
                    summary.getAbstract());

                keywords = summary.getKeywords();
                assertTrue("Keywords is null", keywords != null);
                assertEquals("Incorrect Keyword count", 3, keywords.size());
                assertTrue("Missing Keyword", keywords.contains("WCS"));
                assertTrue("Missing Keyword", keywords.contains("sfdem"));

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue("BoundingBox is null", bbox != null);
                assertEquals("LowerCorner is incorrect", "-103.87108701853181 44.370187074132616",
                    bbox.getLowerCorner());
                assertEquals("UpperCorner is incorrect", "-103.62940739432703 44.5016011535299",
                    bbox.getUpperCorner());
            }
            else
            {
                assertTrue("Unrecognized WCS CoverageSummary", false);
            }
        }
    }

    private static void checkRequestDescription(WCS100RequestDescription requestDescription, String url)
    {
        List<WCS100DCPType> dcpTypes = requestDescription.getDCPTypes();

        assertNotNull("DCPTypes is null for " + requestDescription.getRequestName(), dcpTypes);
        assertEquals("Incorrect DCPTypes count for " + requestDescription.getRequestName(), 2, dcpTypes.size());

        String get = null;
        String post = null;
        for (WCS100DCPType dcpType : dcpTypes)
        {
            WCS100HTTP http = dcpType.getHTTP();
            assertNotNull("HTTP is null for request name " + requestDescription.getRequestName(), http);
            if (http.getGetAddress() != null)
                get = http.getGetAddress();
            if (http.getPostAddress() != null)
                post = http.getPostAddress();
        }

        assertNotNull("Get address is null for request name " + requestDescription.getRequestName(), get);
        assertNotNull("Post address is null for request name " + requestDescription.getRequestName(), post);

        assertEquals("Get address is incorrect for " + requestDescription.getRequestName(), url, get);
        assertEquals("Post address is incorrect for " + requestDescription.getRequestName(), url, post);
    }
}
