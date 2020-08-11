/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
