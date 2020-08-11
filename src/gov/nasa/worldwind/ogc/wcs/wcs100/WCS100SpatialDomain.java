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

import gov.nasa.worldwind.ogc.gml.*;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100SpatialDomain.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100SpatialDomain extends AbstractXMLEventParser
{
    protected List<GMLEnvelope> envelopes = new ArrayList<GMLEnvelope>(1);
    protected List<GMLRectifiedGrid> rectifiedGrids = new ArrayList<GMLRectifiedGrid>(1);
    protected List<GMLGrid> grids = new ArrayList<GMLGrid>(1);

    public WCS100SpatialDomain(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<GMLEnvelope> getEnvelopes()
    {
        return this.envelopes;
    }

    public List<GMLRectifiedGrid> getRectifiedGrids()
    {
        return this.rectifiedGrids;
    }

    public List<GMLGrid> getGrids()
    {
        return this.grids;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "Envelope") || ctx.isStartElement(event, "EnvelopeWithTimePeriod"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof GMLEnvelope)
                    this.envelopes.add((GMLEnvelope) o);
            }
        }
        else if (ctx.isStartElement(event, "RectifiedGrid"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof GMLRectifiedGrid)
                    this.rectifiedGrids.add((GMLRectifiedGrid) o);
            }
        }
        else if (ctx.isStartElement(event, "Grid"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof GMLGrid)
                    this.grids.add((GMLGrid) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
