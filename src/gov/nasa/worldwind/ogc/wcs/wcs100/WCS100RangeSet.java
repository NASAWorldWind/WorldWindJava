/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100RangeSet.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100RangeSet extends AbstractXMLEventParser
{
    protected List<WCS100AxisDescriptionHolder> axisDescriptions = new ArrayList<WCS100AxisDescriptionHolder>(1);

    public WCS100RangeSet(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    public String getLabel()
    {
        return (String) this.getField("label");
    }

    public String getDescription()
    {
        return (String) this.getField("description");
    }

    public WCS100MetadataLink getMetadataLink()
    {
        return (WCS100MetadataLink) this.getField("metadataLink");
    }

    public WCS100Values getNullValues()
    {
        return (WCS100Values) this.getField("nullValues");
    }

    public List<WCS100AxisDescriptionHolder> getAxisDescriptions()
    {
        return this.axisDescriptions;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "axisDescription"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof WCS100AxisDescriptionHolder)
                    this.axisDescriptions.add((WCS100AxisDescriptionHolder) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }

}
