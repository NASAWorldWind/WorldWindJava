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

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * Parses an OGC OnlineResource element.
 *
 * @author tag
 * @version $Id: OGCOnlineResource.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGCOnlineResource extends AbstractXMLEventParser
{
    protected QName HREF;
    protected QName TYPE;

    protected String type;
    protected String href;

    public OGCOnlineResource(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        HREF = new QName(WWXML.XLINK_URI, "href");
        TYPE = new QName(WWXML.XLINK_URI, "type");
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
            if (ctx.isSameAttributeName(attr.getName(), HREF))
                this.setHref(attr.getValue());
            else if (ctx.isSameAttributeName(attr.getName(), TYPE))
                this.setType(attr.getValue());
        }
    }

    public String getType()
    {
        return type;
    }

    protected void setType(String type)
    {
        this.type = type;
    }

    public String getHref()
    {
        return href;
    }

    protected void setHref(String href)
    {
        this.href = href;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("href: ").append(this.href != null ? this.href : "null");
        sb.append(", type: ").append(this.type != null ? this.type : "null");

        return sb.toString();
    }
}
