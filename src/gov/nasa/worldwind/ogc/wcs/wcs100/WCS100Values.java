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

import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100Values.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100Values extends AbstractXMLEventParser
{
    protected List<WCS100SingleValue> singleValues = new ArrayList<WCS100SingleValue>(1);
    private List<WCS100Interval> intervals = new ArrayList<WCS100Interval>(1);

    public WCS100Values(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<WCS100SingleValue> getSingleValues()
    {
        return this.singleValues;
    }

    public String getDefault()
    {
        return (String) this.getField("default");
    }

    public String getType()
    {
        return (String) this.getField("type");
    }

    public String getSemantic()
    {
        return (String) this.getField("semantic");
    }

    public List<WCS100Interval> getIntervals()
    {
        return this.intervals;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "singleValue"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCS100SingleValue)
                    this.singleValues.add((WCS100SingleValue) o);
            }
        }
        else if (ctx.isStartElement(event, "interval"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCS100Interval)
                    this.intervals.add((WCS100Interval) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
