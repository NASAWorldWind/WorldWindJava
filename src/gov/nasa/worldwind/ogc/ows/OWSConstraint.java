/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: OWSConstraint.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSConstraint extends AbstractXMLEventParser
{
    protected List<OWSAllowedValues> allowedValues = new ArrayList<OWSAllowedValues>(1);

    public OWSConstraint(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    public List<OWSAllowedValues> getAllowedValues()
    {
        return this.allowedValues;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "AllowedValues"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OWSAllowedValues)
                    this.allowedValues.add((OWSAllowedValues) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
