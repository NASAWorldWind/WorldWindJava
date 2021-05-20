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

package gov.nasa.worldwind.ogc.wcs.wcs100;

/**
 * @author tag
 * @version $Id: WCS100CoverageOffering.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100CoverageOffering extends WCS100CoverageOfferingBrief
{
    public WCS100CoverageOffering(String namespaceURI)
    {
        super(namespaceURI);
    }

    public WCS100LonLatEnvelope getLonLatEnvelope()
    {
        return (WCS100LonLatEnvelope) this.getField("lonLatEnvelope");
    }

    public WCS100DomainSet getDomainSet()
    {
        return (WCS100DomainSet) this.getField("domainSet");
    }

    public WCS100RangeSetHolder getRangeSet()
    {
        return (WCS100RangeSetHolder) this.getField("rangeSet");
    }

    public WCS100SupportedFormats getSupportedFormats()
    {
        return (WCS100SupportedFormats) this.getField("supportedFormats");
    }

    public WCS100SupportedCRSs getSupportedCRSs()
    {
        return (WCS100SupportedCRSs) this.getField("supportedCRSs");
    }

    public WCS100SupportedInterpolations getSupportedInterpolations()
    {
        return (WCS100SupportedInterpolations) this.getField("supportedInterpolations");
    }
}
