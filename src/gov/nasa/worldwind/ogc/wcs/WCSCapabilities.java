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

import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.ogc.ows.OWSCapabilities;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;

/**
 * @author tag
 * @version $Id: WCSCapabilities.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class WCSCapabilities extends OWSCapabilities
{
    public WCSCapabilities(Object docSource)
    {
        super(OGCConstants.WCS_1_1_1_NAMESPACE_URI, docSource);
    }

    public WCSContents getContents()
    {
        return (WCSContents) this.getField("Contents");
    }

    public String getOtherSource()
    {
        AttributesOnlyXMLEventParser parser = (AttributesOnlyXMLEventParser) this.getField("OtherSource");

        return parser != null ? (String) parser.getField("href") : null;
    }

    public String getDefaultNamespaceURI()
    {
        return this.namespaceURI;
    }

    public String getWCSNamespace()
    {
        return this.namespaceURI;
    }

    protected void determineNamespaces()
    {
        String version = this.getVersion();
        if (version == null)
        {
            this.setOWSNamespaceURI(OGCConstants.OWS_1_1_0_NAMESPACE_URI);
            this.setNamespaceURI(OGCConstants.WCS_1_1_1_NAMESPACE_URI);
        }
        else if (WWUtil.compareVersion(version, "1.1.1") == 0)
        {
            this.setOWSNamespaceURI(OGCConstants.OWS_1_1_0_NAMESPACE_URI);
            this.setNamespaceURI(OGCConstants.WCS_1_1_1_NAMESPACE_URI);
        }
        else
        {
            this.setOWSNamespaceURI(OGCConstants.OWS_1_1_0_NAMESPACE_URI);
            this.setNamespaceURI(OGCConstants.WCS_1_1_1_NAMESPACE_URI);
        }
    }

    @Override
    protected void registerParsers(XMLEventParserContext ctx)
    {
        super.registerParsers(ctx);

        ctx.addStringParsers(this.namespaceURI, new String[] {
            "Identifier",
            "SupportedCRS",
            "SupportedFormat"
        });

        ctx.registerParser(new QName(this.getWCSNamespace(), "Contents"),
            new WCSContents(this.namespaceURI));

        ctx.registerParser(new QName(this.getWCSNamespace(), "CoverageSummary"),
            new WCSCoverageSummary(this.namespaceURI));

        ctx.registerParser(new QName(this.getWCSNamespace(), "OtherSource"),
            new AttributesOnlyXMLEventParser(this.namespaceURI));
    }
}
