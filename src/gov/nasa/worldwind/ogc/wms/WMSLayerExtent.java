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

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * Parses a WMS layer Extent element. These elements are defined only in WMS 1.1.1.
 *
 * @author tag
 * @version $Id: WMSLayerExtent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSLayerExtent extends AbstractXMLEventParser
{
    protected String extent;
    protected String name;
    protected String defaultValue;
    protected Boolean nearestValue;

    public WMSLayerExtent(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args)
    {
        Iterator iter = event.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            Attribute attr = (Attribute) iter.next();
            if (attr.getName().getLocalPart().equals("name") && attr.getValue() != null)
                this.setName(attr.getValue());

            else if (attr.getName().getLocalPart().equals("default") && attr.getValue() != null)
                this.setDefaultValue(attr.getValue());

            else if (attr.getName().getLocalPart().equals("nearestValue") && attr.getValue() != null)
            {
                Boolean d = WWUtil.convertStringToBoolean(attr.getValue());
                if (d != null)
                    this.setNearestValue(d);
            }
        }
    }

    public String getExtent()
    {
        return this.getCharacters();
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    protected void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public Boolean isNearestValue()
    {
        return nearestValue;
    }

    protected void setNearestValue(Boolean nearestValue)
    {
        this.nearestValue = nearestValue;
    }
}
