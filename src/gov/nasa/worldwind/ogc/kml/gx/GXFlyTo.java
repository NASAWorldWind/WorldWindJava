/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.util.xml.XMLEventParserContext;
import gov.nasa.worldwind.ogc.kml.*;

import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLStreamException;

/**
 * @author tag
 * @version $Id: GXFlyTo.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GXFlyTo extends GXAbstractTourPrimitive
{
    public GXFlyTo(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAbstractView)
            this.setView((KMLAbstractView) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public Double getDuration()
    {
        return (Double) this.getField("duration");
    }

    public String getFlyToMode()
    {
        return (String) this.getField("flyToMode");
    }

    public KMLAbstractView getView()
    {
        return (KMLAbstractView) this.getField("AbstractView");
    }

    protected void setView(KMLAbstractView o)
    {
        this.setField("AbstractView", o);
    }
}
