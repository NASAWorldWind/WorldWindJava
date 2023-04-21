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

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCSContents.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCSContents extends AbstractXMLEventParser
{
    protected List<WCSCoverageSummary> coverageSummaries = new ArrayList<WCSCoverageSummary>(1);
    protected List<AttributesOnlyXMLEventParser> otherSources = new ArrayList<AttributesOnlyXMLEventParser>(1);
    protected List<String> supportedCRSs = new ArrayList<String>(1);
    protected List<String> supportedFormats = new ArrayList<String>(1);

    public WCSContents(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<WCSCoverageSummary> getCoverageSummaries()
    {
        return this.coverageSummaries;
    }

    public List<String> getSupportedCRSs()
    {
        return this.supportedCRSs;
    }

    public List<String> getSupportedFormats()
    {
        return this.supportedFormats;
    }

    public List<String> getOtherSources()
    {
        List<String> strings = new ArrayList<String>(1);

        for (AttributesOnlyXMLEventParser parser : this.otherSources)
        {
            String url = (String) parser.getField("href");
            if (url != null)
                strings.add(url);
        }

        return strings.size() > 0 ? strings : null;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "CoverageSummary"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCSCoverageSummary)
                    this.coverageSummaries.add((WCSCoverageSummary) o);
            }
        }
        else if (ctx.isStartElement(event, "OtherSource"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof AttributesOnlyXMLEventParser)
                    this.otherSources.add((AttributesOnlyXMLEventParser) o);
            }
        }
        else if (ctx.isStartElement(event, "SupportedCRS"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.supportedCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "SupportedFormat"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.supportedFormats.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
