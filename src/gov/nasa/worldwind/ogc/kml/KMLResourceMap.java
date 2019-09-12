/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>ResourceMap</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLResourceMap.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLResourceMap extends KMLAbstractObject
{
    protected List<KMLAlias> aliases = new ArrayList<KMLAlias>();

    public KMLResourceMap(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAlias)
            this.addAlias((KMLAlias) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    protected void addAlias(KMLAlias o)
    {
        this.aliases.add(o);
    }

    public List<KMLAlias> getAliases()
    {
        return this.aliases;
    }
}
