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
 * Represents the KML <i>ListStyle</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLListStyle.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLListStyle extends KMLAbstractSubStyle
{
    protected List<KMLItemIcon> itemIcons = new ArrayList<KMLItemIcon>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLListStyle(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLItemIcon)
            this.addItemIcon((KMLItemIcon) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public String getListItemType()
    {
        return (String) this.getField("listItemType");
    }

    public String getBgColor()
    {
        return (String) this.getField("bgColor");
    }

    protected void addItemIcon(KMLItemIcon o)
    {
        this.itemIcons.add(o);
    }

    public List<KMLItemIcon> getItemIcons()
    {
        return this.itemIcons;
    }

    public Integer getMaxSnippetLines()
    {
        return (Integer) this.getField("maxSnippetLines");
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLListStyle))
        {
            String message = Logging.getMessage("KML.InvalidElementType", sourceValues.getClass().getName());
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        KMLListStyle sourceStyle = (KMLListStyle) sourceValues;

        if (sourceStyle.getItemIcons() != null && sourceStyle.getItemIcons().size() > 0)
            this.mergeItemIcons(sourceStyle);

        super.applyChange(sourceValues);
    }

    /**
     * Merge a list of incoming item icons with the current list. If an incoming item icon has the same ID as an
     * existing one, replace the existing one, otherwise just add the incoming one.
     *
     * @param sourceStyle the incoming item icons.
     */
    protected void mergeItemIcons(KMLListStyle sourceStyle)
    {
        // Make a copy of the existing list so we can modify it as we traverse the copy.
        List<KMLItemIcon> itemIconsCopy = new ArrayList<KMLItemIcon>(this.getItemIcons().size());
        Collections.copy(itemIconsCopy, this.getItemIcons());

        for (KMLItemIcon sourceItemIcon : sourceStyle.getItemIcons())
        {
            String id = sourceItemIcon.getId();
            if (!WWUtil.isEmpty(id))
            {
                for (KMLItemIcon existingItemIcon : itemIconsCopy)
                {
                    String currentId = existingItemIcon.getId();
                    if (!WWUtil.isEmpty(currentId) && currentId.equals(id))
                    {
                        this.getItemIcons().remove(existingItemIcon);
                    }
                }
            }

            this.getItemIcons().add(sourceItemIcon);
        }
    }
}
