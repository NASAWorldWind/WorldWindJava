/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100AxisDescription.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100AxisDescription extends AbstractXMLEventParser
{
    protected List<String> axisNames = new ArrayList<String>(2);
    protected List<String> offsetVectors = new ArrayList<String>(2);

    public WCS100AxisDescription(String namespaceURI)
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

    public WCS100Values getValues()
    {
        return (WCS100Values) this.getField("values");
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
