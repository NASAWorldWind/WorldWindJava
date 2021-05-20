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

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: GMLRectifiedGrid.java 2066 2014-06-20 20:41:46Z tgaskins $
 */
public class GMLRectifiedGrid extends GMLGrid
{
    protected List<String> axisNames = new ArrayList<String>(2);
    protected List<String> offsetVectors = new ArrayList<String>(2);

    public GMLRectifiedGrid(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getAxisNames()
    {
        return this.axisNames;
    }

    public List<String> getOffsetVectorStrings()
    {
        return this.offsetVectors;
    }

    public List<Vec4> getOffsetVectors()
    {
        List<Vec4> vectors = new ArrayList<Vec4>(this.offsetVectors.size());

        for (String s : this.offsetVectors)
        {
            double[] arr = new double[] {0, 0, 0, 0};
            String[] split = s.split(" ");
            for (int i = 0; i < Math.min(split.length, 4); i++)
            {
                try
                {
                    arr[i] = Double.parseDouble(split[i]);
                }
                catch (NumberFormatException e)
                {
                    String message = Logging.getMessage("generic.NumberFormatException");
                    Logging.logger().log(java.util.logging.Level.WARNING, message, e);
                    return Collections.emptyList();
                }
            }
            vectors.add(new Vec4(arr[0], arr[1], arr[2], arr[3]));
        }

        return vectors;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "axisName"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.axisNames.add(s);
        }
        else if (ctx.isStartElement(event, "offsetVector"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.offsetVectors.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
