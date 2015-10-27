/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
