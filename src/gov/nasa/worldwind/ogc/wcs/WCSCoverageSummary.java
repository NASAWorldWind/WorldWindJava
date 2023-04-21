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

import gov.nasa.worldwind.ogc.ows.OWSWGS84BoundingBox;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCSCoverageSummary.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCSCoverageSummary extends AbstractXMLEventParser
{
    // TODO: metadata

    protected List<String> abstracts = new ArrayList<String>(1);
    protected List<OWSWGS84BoundingBox> boundingBoxes = new ArrayList<OWSWGS84BoundingBox>(1);
    protected List<WCSCoverageSummary> coverageSummaries = new ArrayList<WCSCoverageSummary>(1);
    protected List<String> supportedCRSs = new ArrayList<String>(1);
    protected List<String> supportedFormats = new ArrayList<String>(1);
    protected List<String> titles = new ArrayList<String>(1);

    public WCSCoverageSummary(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getAbstracts()
    {
        return this.abstracts;
    }

    public String getAbstract()
    {
        Iterator<String> iter = this.abstracts.iterator();

        return iter.hasNext() ? iter.next() : null;
    }

    public List<OWSWGS84BoundingBox> getBoundingBoxes()
    {
        return this.boundingBoxes;
    }

    public OWSWGS84BoundingBox getBoundingBox()
    {
        Iterator<OWSWGS84BoundingBox> iter = this.boundingBoxes.iterator();

        return iter.hasNext() ? iter.next() : null;
    }

    public List<WCSCoverageSummary> getCoverageSummaries()
    {
        return this.coverageSummaries;
    }

    public String getIdentifier()
    {
        return (String) this.getField("Identifier");
    }

    public List<String> getKeywords()
    {
        return ((StringListXMLEventParser) this.getField("Keywords")).getStrings();
    }

    public List<String> getSupportedCRSs()
    {
        return this.supportedCRSs;
    }

    public List<String> getSupportedFormats()
    {
        return this.supportedFormats;
    }

    public List<String> getTitles()
    {
        return this.titles;
    }

    public String getTitle()
    {
        Iterator<String> iter = this.titles.iterator();

        return iter.hasNext() ? iter.next() : null;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "Abstract"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.abstracts.add(s);
        }
        else if (ctx.isStartElement(event, "WGS84BoundingBox"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OWSWGS84BoundingBox)
                    this.boundingBoxes.add((OWSWGS84BoundingBox) o);
            }
        }
        else if (ctx.isStartElement(event, "CoverageSummary"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCSCoverageSummary)
                    this.coverageSummaries.add((WCSCoverageSummary) o);
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
        else if (ctx.isStartElement(event, "Title"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.titles.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
