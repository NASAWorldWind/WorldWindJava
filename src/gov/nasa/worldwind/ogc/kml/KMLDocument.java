/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>Document</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLDocument.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLDocument extends KMLAbstractContainer
{
    protected List<KMLSchema> schemas = new ArrayList<KMLSchema>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLDocument(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLSchema)
            this.addSchema((KMLSchema) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    protected void addSchema(KMLSchema o)
    {
        this.schemas.add(o);
    }

    public List<KMLSchema> getSchemas()
    {
        return this.schemas;
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLDocument))
        {
            String message = Logging.getMessage("KML.InvalidElementType", sourceValues.getClass().getName());
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        super.applyChange(sourceValues);

        KMLDocument sourceDocument = (KMLDocument) sourceValues;

        if (sourceDocument.getSchemas() != null && sourceDocument.getSchemas().size() > 0)
            this.mergeSchemas(sourceDocument);
    }

    /**
     * Merge a list of incoming schemas with the current list. If an incoming schema has the same ID as an existing
     * one, replace the existing one, otherwise just add the incoming one.
     *
     * @param sourceDocument the incoming document.
     */
    protected void mergeSchemas(KMLDocument sourceDocument)
    {
        // Make a copy of the existing list so we can modify it as we traverse the copy.
        List<KMLSchema> schemaListCopy = new ArrayList<KMLSchema>(this.getSchemas().size());
        Collections.copy(schemaListCopy, this.getSchemas());

        for (KMLSchema sourceSchema : sourceDocument.getSchemas())
        {
            String id = sourceSchema.getId();
            if (!WWUtil.isEmpty(id))
            {
                for (KMLSchema existingSchema : schemaListCopy)
                {
                    String currentId = existingSchema.getId();
                    if (!WWUtil.isEmpty(currentId) && currentId.equals(id))
                        this.getSchemas().remove(existingSchema);
                }
            }

            this.getSchemas().add(sourceSchema);
        }
    }
}
