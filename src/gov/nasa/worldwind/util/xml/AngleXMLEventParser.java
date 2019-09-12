/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
