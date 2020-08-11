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

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.geom.Angle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * @author tag
 * @version $Id: AngleXMLEventParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AngleXMLEventParser extends AbstractXMLEventParser
{
    protected QName elementName;

    public AngleXMLEventParser(QName elementName)
    {
        this.elementName = elementName;
    }

    public Object parse(XMLEventParserContext ctx, XMLEvent angleEvent, Object... args) throws XMLStreamException
    {
        Angle angle = null;

        for (XMLEvent event = ctx.nextEvent(); event != null; event = ctx.nextEvent())
        {
            if (ctx.isEndElement(event, angleEvent))
                return angle;

            if (ctx.isStartElement(event, this.elementName))
            {
                Double d = ctx.getDoubleParser().parseDouble(ctx, event);
                if (d != null)
                    angle = Angle.fromDegrees(d);
            }
        }

        return null;
    }

    public Angle parseAngle(XMLEventParserContext ctx, XMLEvent angleEvent, Object... args) throws XMLStreamException
    {
        return (Angle) this.parse(ctx, angleEvent, args);
    }
}
