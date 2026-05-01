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

import gov.nasa.worldwind.ogc.ows.*;
import gov.nasa.worldwind.ogc.wcs.wcs100.*;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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

        assertNotNull(caps.getVersion(), "Version is null");
        assertEquals("1.0.0", caps.getVersion(), "Incorrect version number");
        assertEquals("2013-06-28T16:26:00Z", caps.getUpdateSequence(), "Incorrect update sequence");

        WCS100Service service = caps.getService();
        assertNotNull(service, "Service is null");

        WCS100MetadataLink metadataLink = service.getMetadataLink();
        assertNotNull(metadataLink, "MetadataLink is null");
        assertEquals("http://worldwind26.arc.nasa.gov", metadataLink.getHref(), "Incorrect metadataLink href");
        assertEquals("simple", metadataLink.getType(), "Incorrect type value");
        assertEquals("TC211", metadataLink.getMetadataType(), "Incorrect metadataType value");

        String description = service.getDescription();
        assertNotNull(description, "Service description is null");
        assertTrue(description.startsWith("WorldWind MapServer Elevation test"), "Incorrect description");

        assertNotNull(service.getName(), "Service name is null");
        assertEquals("MapServer WCS", service.getName(), "Incorrect service name");

        assertNotNull(service.getLabel(), "Service label is null");
        assertEquals("WorldWind MapServer Elevation", service.getLabel(), "Incorrect service label");

        List<String> keywords = service.getKeywords();
        assertTrue(keywords != null, "Keywords is null");
        assertEquals(5, keywords.size(), "Incorrect keyword count");
        assertTrue(keywords.contains("wcs"), "Missing keyword");
        assertTrue(keywords.contains("test"), "Missing keyword");
        assertTrue(keywords.contains("FAA"), "Missing keyword");
        assertTrue(keywords.contains("charts"), "Missing keyword");
        assertTrue(keywords.contains("aeronautical"), "Missing keyword");

        WCS100ResponsibleParty responsibleParty = service.getResponsibleParty();
        assertNotNull(responsibleParty, "ResponsibleParty is null");
        assertNotNull(responsibleParty.getIndividualName(), "IndividualName is null");
        assertEquals("Randolph Kim", responsibleParty.getIndividualName(), "Incorrect individualName");
        assertNotNull(responsibleParty.getOrganisationName(), "OrganisationName is null");
        assertEquals("NASA", responsibleParty.getOrganisationName(), "Incorrect organisationName");
        assertNotNull(responsibleParty.getPositionName(), "PostionName is null");
        assertEquals("manager", responsibleParty.getPositionName(), "Incorrect positionName");
        OWSContactInfo contactInfo = responsibleParty.getContactInfo();
        assertNotNull(contactInfo, "ContactInfo is null");
        OWSAddress address = contactInfo.getAddress();
        assertNotNull(address, "Address is null");
        assertNotNull(address.getCity(), "City is null");
        assertEquals("Moffett Field", address.getCity(), "Incorrect city");
        assertNotNull(address.getCountries().get(0), "Country is null");
        assertEquals("USA", address.getCountries().get(0), "Incorrect country");
        assertNotNull(address.getElectronicMailAddresses().get(0), "ElectronicMailAddress is null");
        assertEquals("none@nasa.gov", address.getElectronicMailAddresses().get(0), "Incorrect electronicMailAddress");
        assertNotNull(address.getDeliveryPoints().get(0), "DeliveryPoint is null");
        assertEquals("NASA Ames Research Center", address.getDeliveryPoints().get(0), "Incorrect deliveryPoint");
        assertNotNull(address.getAdministrativeArea(), "AdministrativeArea is null");
        assertEquals("CA", address.getAdministrativeArea(), "Incorrect deliveryPoint");
        OWSPhone phone = contactInfo.getPhone();
        assertNotNull(phone, "Phone is null");
        assertNotNull(phone.getVoices().get(0), "Voice is null");
        assertEquals("000-000-0000", phone.getVoices().get(0), "Incorrect voice");
        assertNotNull(phone.getFacsimiles().get(0), "Facsimile is null");
        assertEquals("000-000-0000", phone.getFacsimiles().get(0), "Incorrect facsimile");
        assertEquals("http://worldwind26.arc.nasa.gov/wms2?", contactInfo.getOnlineResource(), "Incorrect contactInfo onlineResource href");

        assertNotNull(service.getFees(), "Fees is null");
        assertEquals("none", service.getFees(), "Incorrect country");

        List<String> accessConstraints = service.getAccessConstraints();
        assertNotNull(accessConstraints, "AccessConstraints is null");
        assertEquals(1, accessConstraints.size(), "Incorrect number of access constraints");
        assertEquals("none", accessConstraints.iterator().next(), "Incorrect accessConstraint");

        WCS100Capability capability = caps.getCapability();
        assertNotNull(capability, "Capability is null");

        WCS100Request request = capability.getRequest();
        assertNotNull(request, "Request is null");
        assertNotNull(request.getRequests(), "Request descriptions is null");
        assertEquals(3, request.getRequests().size(), "Incorrect request description count");
        assertNotNull(request.getRequest("GetCapabilities"), "GetCapabilities request description is null");
        assertNotNull(request.getRequest("DescribeCoverage"), "DescribeCoverage request description is null");
        assertNotNull(request.getRequest("GetCoverage"), "GetCoverage request description is null");
        checkRequestDescription(request.getRequest("GetCapabilities"), "http://worldwind26.arc.nasa.gov/wms2?");
        checkRequestDescription(request.getRequest("DescribeCoverage"), "http://worldwind26.arc.nasa.gov/wms2?");
        checkRequestDescription(request.getRequest("GetCoverage"), "http://worldwind26.arc.nasa.gov/wms2?");

        WCS100Exception exception = capability.getException();
        assertNotNull(exception, "Exception is null");
        assertNotNull(exception.getFormats(), "Exception Formats is null");
        assertEquals(1, exception.getFormats().size(), "Incorrect exception format count");
        Iterator<String> iterator = exception.getFormats().iterator();
        assertEquals("application/vnd.ogc.se_xml", iterator.next(), "Incorrect exception format");

        assertNotNull(caps.getContentMetadata(), "ContentMetadata is null");
        List<WCS100CoverageOfferingBrief> coverages = caps.getContentMetadata().getCoverageOfferings();
        assertNotNull(coverages, "CoverageOfferingBriefs is null");
        assertEquals(6, coverages.size(), "Incorrect CoverageOfferingBrief description count");

        WCS100CoverageOfferingBrief coverage = coverages.get(0);
        assertNotNull(coverage, "CoverageOfferingBrief 0 is null");
        assertNotNull(coverage.getName(), "CoverageOfferingBrief 0 name is null");
        assertEquals("aster_v2", coverage.getName(), "Incorrect CoverageOfferingBrief 0 name");
        assertNotNull(coverage.getLabel(), "CoverageOfferingBrief 0 label is null");
        assertEquals("ASTER version 2", coverage.getLabel(), "Incorrect CoverageOfferingBrief 0 label");
        WCS100LonLatEnvelope envelope = coverage.getLonLatEnvelope();
        assertNotNull(envelope, "LonLatEnvelope 0 is null");
        assertNotNull(envelope.getPositions(), "LonLatEnvelope 0 positions is null");
        assertEquals("urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName(), "Incorrect LonLatEnvelope 0 SRS");
        assertEquals(2, envelope.getPositions().size(), "Incorrect LonLatEnvelope 0 position count");
        assertEquals("-180 -83", envelope.getPositions().get(0).getPosString(), "Incorrect LonLatEnvelope 0 position 0");
        assertEquals("180 83", envelope.getPositions().get(1).getPosString(), "Incorrect LonLatEnvelope 0 position 1");

        coverage = coverages.get(1);
        assertNotNull(coverage, "CoverageOfferingBrief 1 is null");
        assertNotNull(coverage.getName(), "CoverageOfferingBrief 1 name is null");
        assertEquals("USGS-NED", coverage.getName(), "Incorrect CoverageOfferingBrief 1 name");
        assertNotNull(coverage.getLabel(), "CoverageOfferingBrief 1 label is null");
        assertEquals("USGS NED", coverage.getLabel(), "Incorrect CoverageOfferingBrief 1 label");
        envelope = coverage.getLonLatEnvelope();
        assertNotNull(envelope, "LonLatEnvelope 1 is null");
        assertNotNull(envelope.getPositions(), "LonLatEnvelope 1 positions is null");
        assertEquals("urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName(), "Incorrect LonLatEnvelope 1 SRS");
        assertEquals(2, envelope.getPositions().size(), "Incorrect LonLatEnvelope 1 position count");
        assertEquals("-125 25", envelope.getPositions().get(0).getPosString(), "Incorrect LonLatEnvelope 1 position 0");
        assertEquals("-65.5 50", envelope.getPositions().get(1).getPosString(), "Incorrect LonLatEnvelope 1 position 1");

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

        assertNotNull(caps.getVersion(), "Version is null");
        assertEquals("1.0.0", caps.getVersion(), "Incorrect version number");
        assertEquals("105", caps.getUpdateSequence(), "Incorrect update sequence");

        WCS100Service service = caps.getService();
        assertNotNull(service, "Service is null");

        WCS100MetadataLink metadataLink = service.getMetadataLink();
        assertNotNull(metadataLink, "MetadataLink is null");
        assertEquals("http://geoserver.sourceforge.net/html/index.php", metadataLink.getField("about"), "Incorrect metadataLink about value");
        assertEquals("simple", metadataLink.getField("type"), "Incorrect metadataLink type value");
        assertEquals("other", metadataLink.getField("metadataType"), "Incorrect metadataLink metadataType value");

        String description = service.getDescription();
        assertNotNull(description, "Service description is null");
        assertTrue(description.startsWith("This server implements the WCS specification 1.0"), "Incorrect description");

        assertNotNull(service.getName(), "Service name is null");
        assertEquals("WCS", service.getName(), "Incorrect service name");

        assertNotNull(service.getLabel(), "Service label is null");
        assertEquals("Web Coverage Service", service.getLabel(), "Incorrect service label");

        List<String> keywords = service.getKeywords();
        assertTrue(keywords != null, "Keywords is null");
        assertEquals(3, keywords.size(), "Incorrect keyword count");
        assertTrue(keywords.contains("WCS"), "Missing keyword");
        assertTrue(keywords.contains("WMS"), "Missing keyword");
        assertTrue(keywords.contains("GEOSERVER"), "Missing keyword");

        WCS100ResponsibleParty responsibleParty = service.getResponsibleParty();
        assertNotNull(responsibleParty, "ResponsibleParty is null");
        assertNotNull(responsibleParty.getIndividualName(), "IndividualName is null");
        assertEquals("Claudius Ptolomaeus", responsibleParty.getIndividualName(), "Incorrect individualName");
        assertNotNull(responsibleParty.getOrganisationName(), "OrganisationName is null");
        assertEquals("The ancient geographes INC", responsibleParty.getOrganisationName(), "Incorrect organisationName");
        assertNotNull(responsibleParty.getPositionName(), "PostionName is null");
        assertEquals("Chief geographer", responsibleParty.getPositionName(), "Incorrect positionName");
        OWSContactInfo contactInfo = responsibleParty.getContactInfo();
        assertNotNull(contactInfo, "ContactInfo is null");
        OWSAddress address = contactInfo.getAddress();
        assertNotNull(address, "Address is null");
        assertNotNull(address.getCity(), "City is null");
        assertEquals("Alexandria", address.getCity(), "Incorrect city");
        assertNotNull(address.getCountries(), "Country is null");
        assertEquals("Egypt", address.getCountries().get(0), "Incorrect country");
        assertNotNull(address.getElectronicMailAddresses(), "ElectronicMailAddress is null");
        assertEquals("claudius.ptolomaeus@gmail.com", address.getElectronicMailAddresses().get(0), "Incorrect electronicMailAddress");

        assertNotNull(service.getFees(), "Fees is null");
        assertEquals("NONE", service.getFees(), "Incorrect country");

        List<String> accessConstraints = service.getAccessConstraints();
        assertNotNull(accessConstraints, "AccessConstraints is null");
        assertEquals(1, accessConstraints.size(), "Incorrect number of access constraints");
        assertEquals("NONE", accessConstraints.iterator().next(), "Incorrect accessConstraint");

        WCS100Capability capability = caps.getCapability();
        assertNotNull(capability, "Capability is null");

        WCS100Request request = capability.getRequest();
        assertNotNull(request, "Request is null");
        assertNotNull(request.getRequests(), "Request descriptions is null");
        assertEquals(3, request.getRequests().size(), "Incorrect request description count");
        assertNotNull(request.getRequest("GetCapabilities"), "GetCapabilities request description is null");
        assertNotNull(request.getRequest("DescribeCoverage"), "DescribeCoverage request description is null");
        assertNotNull(request.getRequest("GetCoverage"), "GetCoverage request description is null");
        checkRequestDescription(request.getRequest("GetCapabilities"), "http://10.0.1.198:8080/geoserver/wcs?");
        checkRequestDescription(request.getRequest("DescribeCoverage"), "http://10.0.1.198:8080/geoserver/wcs?");
        checkRequestDescription(request.getRequest("GetCoverage"), "http://10.0.1.198:8080/geoserver/wcs?");

        WCS100Exception exception = capability.getException();
        assertNotNull(exception, "Exception is null");
        assertNotNull(exception.getFormats(), "Exception Formats is null");
        assertEquals(1, exception.getFormats().size(), "Incorrect exception format count");
        Iterator<String> iterator = exception.getFormats().iterator();
        assertEquals("application/vnd.ogc.se_xml", iterator.next(), "Incorrect exception format");

        assertNotNull(caps.getContentMetadata(), "ContentMetadata is null");
        List<WCS100CoverageOfferingBrief> coverages = caps.getContentMetadata().getCoverageOfferings();
        assertNotNull(coverages, "CoverageOfferingBriefs is null");
        assertEquals(7, coverages.size(), "Incorrect CoverageOfferingBrief description count");

        WCS100CoverageOfferingBrief coverage = coverages.get(0);
        assertNotNull(coverage, "CoverageOfferingBrief 0 is null");
        assertNotNull(coverage.getDescription(), "CoverageOfferingBrief 0 description is null");
        assertEquals("Generated from arcGridSample", coverage.getDescription(), "Incorrect CoverageOfferingBrief 0 description");
        assertNotNull(coverage.getName(), "CoverageOfferingBrief 0 name is null");
        assertEquals("nurc:Arc_Sample", coverage.getName(), "Incorrect CoverageOfferingBrief 0 name");
        assertNotNull(coverage.getLabel(), "CoverageOfferingBrief 0 label is null");
        assertEquals("A sample ArcGrid file", coverage.getLabel(), "Incorrect CoverageOfferingBrief 0 label");
        WCS100LonLatEnvelope envelope = coverage.getLonLatEnvelope();
        assertNotNull(envelope, "LonLatEnvelope 0 is null");
        assertNotNull(envelope.getPositions(), "LonLatEnvelope 0 positions is null");
        assertEquals("urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName(), "Incorrect LonLatEnvelope 0 SRS");
        assertEquals(2, envelope.getPositions().size(), "Incorrect LonLatEnvelope 0 position count");
        assertEquals("-180.0 -90.0", envelope.getPositions().get(0).getPosString(), "Incorrect LonLatEnvelope 0 position 0");
        assertEquals("180.0 90.0", envelope.getPositions().get(1).getPosString(), "Incorrect LonLatEnvelope 0 position 1");
        keywords = coverage.getKeywords();
        assertTrue(keywords != null, "Keywords is null for CoverageOfferingBrief 0");
        assertEquals(3, keywords.size(), "Incorrect keyword count for CoverageOfferingBrief 0");
        assertTrue(keywords.contains("WCS"), "Missing keyword for CoverageOfferingBrief 0");
        assertTrue(keywords.contains("arcGridSample"), "Missing keyword for CoverageOfferingBrief 0");
        assertTrue(keywords.contains("arcGridSample_Coverage"), "Missing keyword for CoverageOfferingBrief 0");

        coverage = coverages.get(1);
        assertNotNull(coverage, "CoverageOfferingBrief 1 is null");
        assertNotNull(coverage.getDescription(), "CoverageOfferingBrief 1 description is null");
        assertEquals("Generated from ImageMosaic", coverage.getDescription(), "Incorrect CoverageOfferingBrief 1 description");
        assertNotNull(coverage.getName(), "CoverageOfferingBrief 1 name is null");
        assertEquals("WW:aster_v2", coverage.getName(), "Incorrect CoverageOfferingBrief 1 name");
        assertNotNull(coverage.getLabel(), "CoverageOfferingBrief 1 label is null");
        assertEquals("ASTER", coverage.getLabel(), "Incorrect CoverageOfferingBrief 1 label");
        envelope = coverage.getLonLatEnvelope();
        assertNotNull(envelope, "LonLatEnvelope 1 is null");
        assertNotNull(envelope.getPositions(), "LonLatEnvelope 1 positions is null");
        assertEquals("urn:ogc:def:crs:OGC:1.3:CRS84", envelope.getSRSName(), "Incorrect LonLatEnvelope 1 SRS");
        assertEquals(2, envelope.getPositions().size(), "Incorrect LonLatEnvelope 1 position count");
        assertEquals("-180.0001388888889 -83.0001388888889", envelope.getPositions().get(0).getPosString(), "Incorrect LonLatEnvelope 1 position 0");
        assertEquals("180.00013888888887 83.00013888888888", envelope.getPositions().get(1).getPosString(), "Incorrect LonLatEnvelope 1 position 1");
        keywords = coverage.getKeywords();
        assertTrue(keywords != null, "Keywords is null for CoverageOfferingBrief 1");
        assertEquals(3, keywords.size(), "Incorrect keyword count for CoverageOfferingBrief 1");
        assertTrue(keywords.contains("WCS"), "Missing keyword for CoverageOfferingBrief 1");
        assertTrue(keywords.contains("ImageMosaic"), "Missing keyword for CoverageOfferingBrief 1");
        assertTrue(keywords.contains("ASTER"), "Missing keyword for CoverageOfferingBrief 1");

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

        assertNotNull(caps.getVersion(), "Version is null");
        assertEquals("1.1.1", caps.getVersion(), "Incorrect version number");
        assertEquals("99", caps.getUpdateSequence(), "Incorrect update sequence");

        OWSServiceIdentification serviceIdentification = caps.getServiceIdentification();
        assertNotNull(serviceIdentification, "Service Identification is null");
        assertEquals("NONE", serviceIdentification.getFees(), "Incorrect Fees");
        assertEquals("WCS", serviceIdentification.getServiceType(), "Incorrect ServiceType");

        List<String> titles = serviceIdentification.getTitles();
        assertTrue(titles != null, "Titles is null");
        assertEquals(1, titles.size(), "Incorrect Title count");
        for (String title : titles)
        {
            assertEquals("Web Coverage Service", title, "Incorrect Title");
        }

        List<String> abstracts = serviceIdentification.getAbstracts();
        assertTrue(abstracts != null, "Abstracts is null");
        assertEquals(1, abstracts.size(), "Incorrect Abstract count");
        for (String abs : abstracts)
        {
            assertTrue(abs.startsWith("This server implements"), "Incorrect Abstract start");
            assertTrue(abs.endsWith("available on WMS also."), "Incorrect Abstract end");
        }

        List<String> keywords = serviceIdentification.getKeywords();
        assertTrue(keywords != null, "Keywords is null");
        assertEquals(3, keywords.size(), "Incorrect Keyword count");
        assertTrue(keywords.contains("WCS"), "Missing Keyword");
        assertTrue(keywords.contains("WMS"), "Missing Keyword");
        assertTrue(keywords.contains("GEOSERVER"), "Missing Keyword");

        List<String> serviceTypeVersions = serviceIdentification.getServiceTypeVersions();
        assertTrue(serviceTypeVersions != null, "ServiceTypeVersions is null");
        assertEquals(2, serviceTypeVersions.size(), "Incorrect ServiceTypeVersion count");
        assertTrue(serviceTypeVersions.contains("1.1.0"), "Missing Keyword");
        assertTrue(serviceTypeVersions.contains("1.1.1"), "Missing Keyword");

        List<String> accessConstraints = serviceIdentification.getAccessConstraints();
        assertTrue(accessConstraints != null, "AccessConstraints is null");
        assertEquals(1, abstracts.size(), "Incorrect AccessConstraints count");
        for (String abs : accessConstraints)
        {
            assertEquals("NONE", abs, "Incorrect AccessConstraint");
        }

        OWSServiceProvider serviceProvider = caps.getServiceProvider();
        assertTrue(serviceProvider != null, "ServiceProvider is null");
        assertEquals("The ancient geographes INC", serviceProvider.getProviderName(), "ProviderName is incorrect");
        assertEquals("http://geoserver.org", serviceProvider.getProviderSite(), "ProviderSite is incorrect");

        OWSServiceContact serviceContact = serviceProvider.getServiceContact();
        assertTrue(serviceContact != null, "ServiceContact is null");
        assertEquals("Claudius Ptolomaeus", serviceContact.getIndividualName(), "IndividualName is incorrect");
        assertEquals("Chief geographer", serviceContact.getPositionName(), "PositionName is incorrect");

        OWSContactInfo contactInfo = serviceContact.getContactInfo();
        assertTrue(contactInfo != null, "ContactInfo is null");
        assertEquals("http://geoserver.org", contactInfo.getOnlineResource(), "OnlineResource is incorrect");

        OWSPhone phone = contactInfo.getPhone();
        assertTrue(phone != null, "Phone is null");

        OWSAddress address = contactInfo.getAddress();
        assertTrue(address != null, "Address is null");
        assertEquals("Alexandria", address.getCity(), "City is incorrect");

        List<String> countries = address.getCountries();
        assertTrue(countries != null, "Countries is null");
        assertEquals(1, countries.size(), "Incorrect Country count");
        for (String country : countries)
        {
            assertEquals("Egypt", country, "Incorrect Country");
        }

        List<String> emails = address.getElectronicMailAddresses();
        assertTrue(emails != null, "ElectronicMailAddress is null");
        assertEquals(1, emails.size(), "Incorrect ElectronicMailAddress count");
        for (String email : emails)
        {
            assertEquals("claudius.ptolomaeus@gmail.com", email, "Incorrect ElectronicMailAddress");
        }

        OWSOperationsMetadata operationsMetadata = caps.getOperationsMetadata();
        assertTrue(operationsMetadata != null, "OperationsMetadata is null");

        List<OWSOperation> operations = operationsMetadata.getOperations();
        assertTrue(operations != null, "Operations is null");
        assertEquals(3, operations.size(), "Incorrect Operation count");
        Set<String> operationNames = new HashSet<String>(3);
        for (OWSOperation operation : operations)
        {
            operationNames.add(operation.getName());
        }
        assertTrue(operationNames.contains("GetCapabilities"), "Missing Operation");
        assertTrue(operationNames.contains("DescribeCoverage"), "Missing Operation");
        assertTrue(operationNames.contains("GetCoverage"), "Missing Operation");

        for (OWSOperation operation : operations)
        {
            List<OWSDCP> dcps = operation.getDCPs();
            assertTrue(dcps != null, "DCPs is null");
            assertEquals(2, dcps.size(), "Incorrect DCP count");

            for (OWSDCP dcp : dcps)
            {
                assertTrue(dcp.getHTTP() != null, "DCP HTTP is null");
            }
        }

        String url = operationsMetadata.getGetOperationAddress("Get", "GetCapabilities");
        assertTrue(url != null, "Get operation address is null");
        assertEquals("http://10.0.1.198:8080/geoserver/wcs?", url, "Incorrect HTTP address");
        url = operationsMetadata.getGetOperationAddress("Post", "GetCapabilities");
        assertTrue(url != null, "Get operation address is null");
        assertEquals("http://10.0.1.198:8080/geoserver/wcs?", url, "Incorrect HTTP address");

        url = operationsMetadata.getGetOperationAddress("Get", "DescribeCoverage");
        assertTrue(url != null, "Get operation address is null");
        assertEquals("http://10.0.1.198:8080/geoserver/wcs?", url, "Incorrect HTTP address");
        url = operationsMetadata.getGetOperationAddress("Post", "DescribeCoverage");
        assertTrue(url != null, "Get operation address is null");
        assertEquals("http://10.0.1.198:8080/geoserver/wcs?", url, "Incorrect HTTP address");

        url = operationsMetadata.getGetOperationAddress("Get", "GetCoverage");
        assertTrue(url != null, "Get operation address is null");
        assertEquals("http://10.0.1.198:8080/geoserver/wcs?", url, "Incorrect HTTP address");
        url = operationsMetadata.getGetOperationAddress("Post", "GetCoverage");
        assertTrue(url != null, "Get operation address is null");
        assertEquals("http://10.0.1.198:8080/geoserver/wcs?", url, "Incorrect HTTP address");

        OWSOperation coverageOp = operationsMetadata.getOperation("GetCoverage");
        List<OWSParameter> parameters = coverageOp.getParameters();
        assertTrue(parameters != null, "Operation Parameters is null");
        assertEquals(1, parameters.size(), "Operation Parameter count is incorrect");
        for (OWSParameter parameter : parameters)
        {
            assertTrue(parameter.getName() != null, "Store parameter is missing");
            assertEquals("store", parameter.getName(), "Incorrect store value");

            List<OWSAllowedValues> allowedValues = parameter.getAllowedValues();
            assertTrue(allowedValues != null, "AllowedValues is null");
            assertEquals(1, allowedValues.size(), "AllowedValues count is incorrect");
            for (OWSAllowedValues avs : allowedValues)
            {
                List<String> avals = avs.getValues();
                assertTrue(avals != null, "AllowedValues values is null");
                assertEquals(2, avals.size(), "Allowed Values values count is incorrect");
                assertTrue(avals.contains("True"), "Missing allowed value");
                assertTrue(avals.contains("False"), "Missing allowed value");
            }
        }

        List<OWSConstraint> constraints = operationsMetadata.getConstraints();
        assertTrue(constraints != null, "Constraints is null");
        assertEquals(1, constraints.size(), "Incorrect Constraint count");
        for (OWSConstraint constraint : constraints)
        {
            assertEquals("PostEncoding", constraint.getName(), "Incorrect Constraint");

            List<OWSAllowedValues> allowedValues = constraint.getAllowedValues();
            assertTrue(allowedValues != null, "AllowedValues is null");
            assertEquals(1, allowedValues.size(), "AllowedValues count is incorrect");
            for (OWSAllowedValues avs : allowedValues)
            {
                List<String> avals = avs.getValues();
                assertTrue(avals != null, "AllowedValues values is null");
                assertEquals(1, avals.size(), "Allowed Values values count is incorrect");
                assertTrue(avals.contains("XML"), "Missing allowed value");
            }
        }

        WCSContents contents = caps.getContents();
        assertTrue(contents != null, "WCS Contents is missing");

        List<WCSCoverageSummary> coverageSummaries = contents.getCoverageSummaries();
        assertTrue(coverageSummaries != null, "WCS CoverageSummarys are missing");
        assertEquals(7, coverageSummaries.size(), "WCS CoverageSummarys count is incorrect");

        Set<String> identifiers = new HashSet<String>(coverageSummaries.size());
        for (WCSCoverageSummary summary : coverageSummaries)
        {
            identifiers.add(summary.getIdentifier());
        }
        assertTrue(identifiers.contains("Arc_Sample"), "Missing CoverageSummary Identifier");
        assertTrue(identifiers.contains("aster_v2"), "Missing CoverageSummary Identifier");
        assertTrue(identifiers.contains("FAAChartsCroppedReprojected"), "Missing CoverageSummary Identifier");
        assertTrue(identifiers.contains("NASA_SRTM30_900m_Tiled"), "Missing CoverageSummary Identifier");
        assertTrue(identifiers.contains("Img_Sample"), "Missing CoverageSummary Identifier");
        assertTrue(identifiers.contains("mosaic"), "Missing CoverageSummary Identifier");
        assertTrue(identifiers.contains("sfdem"), "Missing CoverageSummary Identifier");

        for (WCSCoverageSummary summary : coverageSummaries)
        {
            if (summary.getIdentifier().equals("Arc_Sample"))
            {
                assertEquals("A sample ArcGrid file", summary.getTitle(), "CoverageSummary Title is incorrect");
                assertEquals("Generated from arcGridSample", summary.getAbstract(), "CoverageSummary Abstract is incorrect");

                keywords = summary.getKeywords();
                assertTrue(keywords != null, "Keywords is null");
                assertEquals(3, keywords.size(), "Incorrect Keyword count");
                assertTrue(keywords.contains("WCS"), "Missing Keyword");
                assertTrue(keywords.contains("arcGridSample"), "Missing Keyword");
                assertTrue(keywords.contains("arcGridSample_Coverage"), "Missing Keyword");

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue(bbox != null, "BoundingBox is null");
                assertEquals("-180.0 -90.0", bbox.getLowerCorner(), "LowerCorner is incorrect");
                assertEquals("180.0 90.0", bbox.getUpperCorner(), "UpperCorner is incorrect");
            }
            else if (summary.getIdentifier().equals("aster_v2"))
            {
                assertEquals("ASTER", summary.getTitle(), "CoverageSummary Title is incorrect");
                assertEquals("Generated from ImageMosaic", summary.getAbstract(), "CoverageSummary Abstract is incorrect");

                keywords = summary.getKeywords();
                assertTrue(keywords != null, "Keywords is null");
                assertEquals(3, keywords.size(), "Incorrect Keyword count");
                assertTrue(keywords.contains("WCS"), "Missing Keyword");
                assertTrue(keywords.contains("ImageMosaic"), "Missing Keyword");
                assertTrue(keywords.contains("ASTER"), "Missing Keyword");

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue(bbox != null, "BoundingBox is null");
                assertEquals("-180.0001388888889 -83.0001388888889", bbox.getLowerCorner(), "LowerCorner is incorrect");
                assertEquals("180.00013888888887 83.00013888888888", bbox.getUpperCorner(), "UpperCorner is incorrect");
            }
            else if (summary.getIdentifier().equals("FAAChartsCroppedReprojected"))
            {
                assertEquals("FAAChartsCroppedReprojected", summary.getTitle(), "CoverageSummary Title is incorrect");
                assertEquals("Generated from ImageMosaic", summary.getAbstract(), "CoverageSummary Abstract is incorrect");

                keywords = summary.getKeywords();
                assertTrue(keywords != null, "Keywords is null");
                assertEquals(3, keywords.size(), "Incorrect Keyword count");
                assertTrue(keywords.contains("WCS"), "Missing Keyword");
                assertTrue(keywords.contains("ImageMosaic"), "Missing Keyword");
                assertTrue(keywords.contains("FAAChartsCroppedReprojected"), "Missing Keyword");

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue(bbox != null, "BoundingBox is null");
                assertEquals("-173.4897609604564 50.896520942672375", bbox.getLowerCorner(), "LowerCorner is incorrect");
                assertEquals("178.65474058869506 72.33574978977076", bbox.getUpperCorner(), "UpperCorner is incorrect");
            }
            else if (summary.getIdentifier().equals("NASA_SRTM30_900m_Tiled"))
            {
                assertEquals("NASA_SRTM30_900m_Tiled", summary.getTitle(), "CoverageSummary Title is incorrect");
                assertEquals("Generated from ImageMosaic", summary.getAbstract(), "CoverageSummary Abstract is incorrect");

                keywords = summary.getKeywords();
                assertTrue(keywords != null, "Keywords is null");
                assertEquals(3, keywords.size(), "Incorrect Keyword count");
                assertTrue(keywords.contains("WCS"), "Missing Keyword");
                assertTrue(keywords.contains("ImageMosaic"), "Missing Keyword");
                assertTrue(keywords.contains("NASA_SRTM30_900m_Tiled"), "Missing Keyword");

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue(bbox != null, "BoundingBox is null");
                assertEquals("-180.0 -90.0", bbox.getLowerCorner(), "LowerCorner is incorrect");
                assertEquals("180.0 90.0", bbox.getUpperCorner(), "UpperCorner is incorrect");
            }
            else if (summary.getIdentifier().equals("Img_Sample"))
            {
                assertEquals("North America sample imagery", summary.getTitle(), "CoverageSummary Title is incorrect");
                assertEquals("A very rough imagery of North America", summary.getAbstract(), "CoverageSummary Abstract is incorrect");

                keywords = summary.getKeywords();
                assertTrue(keywords != null, "Keywords is null");
                assertEquals(3, keywords.size(), "Incorrect Keyword count");
                assertTrue(keywords.contains("WCS"), "Missing Keyword");
                assertTrue(keywords.contains("worldImageSample"), "Missing Keyword");
                assertTrue(keywords.contains("worldImageSample_Coverage"), "Missing Keyword");

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue(bbox != null, "BoundingBox is null");
                assertEquals("-130.85168 20.7052", bbox.getLowerCorner(), "LowerCorner is incorrect");
                assertEquals("-62.0054 54.1141", bbox.getUpperCorner(), "UpperCorner is incorrect");
            }
            else if (summary.getIdentifier().equals("mosaic"))
            {
                assertEquals("mosaic", summary.getTitle(), "CoverageSummary Title is incorrect");
                assertEquals("Generated from ImageMosaic", summary.getAbstract(), "CoverageSummary Abstract is incorrect");

                keywords = summary.getKeywords();
                assertTrue(keywords != null, "Keywords is null");
                assertEquals(3, keywords.size(), "Incorrect Keyword count");
                assertTrue(keywords.contains("WCS"), "Missing Keyword");
                assertTrue(keywords.contains("ImageMosaic"), "Missing Keyword");
                assertTrue(keywords.contains("mosaic"), "Missing Keyword");

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue(bbox != null, "BoundingBox is null");
                assertEquals("6.346 36.492", bbox.getLowerCorner(), "LowerCorner is incorrect");
                assertEquals("20.83 46.591", bbox.getUpperCorner(), "UpperCorner is incorrect");
            }
            else if (summary.getIdentifier().equals("sfdem"))
            {
                assertEquals("sfdem is a Tagged Image File Format with Geographic information", summary.getTitle(), "CoverageSummary Title is incorrect");
                assertEquals("Generated from sfdem", summary.getAbstract(), "CoverageSummary Abstract is incorrect");

                keywords = summary.getKeywords();
                assertTrue(keywords != null, "Keywords is null");
                assertEquals(3, keywords.size(), "Incorrect Keyword count");
                assertTrue(keywords.contains("WCS"), "Missing Keyword");
                assertTrue(keywords.contains("sfdem"), "Missing Keyword");

                OWSWGS84BoundingBox bbox = summary.getBoundingBox();
                assertTrue(bbox != null, "BoundingBox is null");
                assertEquals("-103.87108701853181 44.370187074132616", bbox.getLowerCorner(), "LowerCorner is incorrect");
                assertEquals("-103.62940739432703 44.5016011535299", bbox.getUpperCorner(), "UpperCorner is incorrect");
            }
            else
            {
                assertTrue(false, "Unrecognized WCS CoverageSummary");
            }
        }
    }

    private static void checkRequestDescription(WCS100RequestDescription requestDescription, String url)
    {
        List<WCS100DCPType> dcpTypes = requestDescription.getDCPTypes();

        assertNotNull(dcpTypes, "DCPTypes is null for " + requestDescription.getRequestName());
        assertEquals(2, dcpTypes.size(), "Incorrect DCPTypes count for " + requestDescription.getRequestName());

        String get = null;
        String post = null;
        for (WCS100DCPType dcpType : dcpTypes)
        {
            WCS100HTTP http = dcpType.getHTTP();
            assertNotNull(http, "HTTP is null for request name " + requestDescription.getRequestName());
            if (http.getGetAddress() != null)
                get = http.getGetAddress();
            if (http.getPostAddress() != null)
                post = http.getPostAddress();
        }

        assertNotNull(get, "Get address is null for request name " + requestDescription.getRequestName());
        assertNotNull(post, "Post address is null for request name " + requestDescription.getRequestName());

        assertEquals(url, get, "Get address is incorrect for " + requestDescription.getRequestName());
        assertEquals(url, post, "Post address is incorrect for " + requestDescription.getRequestName());
    }
}
