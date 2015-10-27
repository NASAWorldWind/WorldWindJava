/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * Parses a WMS layer LogoURL element. Also used for WMS layer LegendURL elements.
 *
 * @author tag
 * @version $Id: WMSLogoURL.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSLogoURL extends WMSLayerInfoURL
{
    protected Integer width;
    protected Integer height;

    public WMSLogoURL(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args)
    {
        super.doParseEventAttributes(ctx, event, args);

        Iterator iter = event.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();

            if (attr.getName().getLocalPart().equals("width") && attr.getValue() != null)
            {
                Integer i = WWUtil.convertStringToInteger(attr.getValue());
                if (i != null)
                    this.setWidth(i);
            }

            if (attr.getName().getLocalPart().equals("height") && attr.getValue() != null)
            {
                Integer i = WWUtil.convertStringToInteger(attr.getValue());
                if (i != null)
                    this.setHeight(i);
            }
        }
    }

    public Integer getWidth()
    {
        return width;
    }

    protected void setWidth(Integer width)
    {
        this.width = width;
    }

    public Integer getHeight()
    {
        return height;
    }

    protected void setHeight(Integer height)
    {
        this.height = height;
    }
}
