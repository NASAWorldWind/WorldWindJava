/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * Represents the KML <i>Schema</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLSchema.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLSchema extends AbstractXMLEventParser
{
    protected List<KMLSimpleField> simpleFields = new ArrayList<KMLSimpleField>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLSchema(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLSimpleField)
            this.addSimpleField((KMLSimpleField) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public String getName()
    {
        return (String) this.getField("name");
    }

    public String getId()
    {
        return (String) this.getField("id");
    }

    protected void addSimpleField(KMLSimpleField o)
    {
        this.simpleFields.add(o);
    }

    public List<KMLSimpleField> getSimpleFields()
    {
        return this.simpleFields;
    }
}
