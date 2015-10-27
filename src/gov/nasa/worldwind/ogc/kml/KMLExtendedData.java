/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * Represents the KML <i>ExtendedData</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLExtendedData.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLExtendedData extends KMLAbstractObject
{
    protected List<KMLData> data = new ArrayList<KMLData>();
    protected List<KMLSchemaData> schemaData = new ArrayList<KMLSchemaData>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLExtendedData(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLData)
            this.addData((KMLData) o);
        else if (o instanceof KMLSchemaData)
            this.addSchemaData((KMLSchemaData) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    protected void addData(KMLData o)
    {
        this.data.add(o);
    }

    public List<KMLData> getData()
    {
        return this.data;
    }

    protected void addSchemaData(KMLSchemaData o)
    {
        this.schemaData.add(o);
    }

    public List<KMLSchemaData> getSchemaData()
    {
        return this.schemaData;
    }
}
