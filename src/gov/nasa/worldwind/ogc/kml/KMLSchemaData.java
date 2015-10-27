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
 * Represents the KML <i>SchemaData</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLSchemaData.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLSchemaData extends KMLAbstractObject
{
    protected List<KMLSimpleData> simpleData = new ArrayList<KMLSimpleData>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLSchemaData(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLSimpleData)
            this.addSimpleData((KMLSimpleData) o);
    }

    public String getSchemaUrl()
    {
        return (String) this.getField("schemaUrl");
    }

    protected void addSimpleData(KMLSimpleData o)
    {
        this.simpleData.add(o);
    }

    public List<KMLSimpleData> getSimpleData()
    {
        return this.simpleData;
    }
}
