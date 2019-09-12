/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.xml.XMLEventParserContext;
import gov.nasa.worldwind.util.WWUtil;

import javax.xml.stream.events.*;
import javax.xml.stream.XMLStreamException;

/**
 * Represents the KML <i>Snippet</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLSnippet.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLSnippet extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLSnippet(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventAttribute(Attribute attr, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if ("maxLines".equals(attr.getName().getLocalPart()))
            this.setMaxLines(WWUtil.makeInteger(attr.getValue()));
        else
            super.doAddEventAttribute(attr, ctx, event, args);
    }

    public Integer getMaxLines()
    {
        return (Integer) this.getField("maxLines");
    }

    public void setMaxLines(Integer o)
    {
        this.setField("maxLines", o);
    }
}
