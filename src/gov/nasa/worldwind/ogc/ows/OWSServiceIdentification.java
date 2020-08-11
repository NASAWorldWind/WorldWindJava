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

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: OWSServiceIdentification.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSServiceIdentification extends AbstractXMLEventParser
{
    protected List<String> abstracts = new ArrayList<String>(1);
    protected List<String> accessConstraints = new ArrayList<String>(1);
    protected List<String> profiles = new ArrayList<String>(1);
    protected List<String> titles = new ArrayList<String>(1);
    protected List<String> serviceTypeVersions = new ArrayList<String>(1);

    public OWSServiceIdentification(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getTitles()
    {
        return this.titles;
    }

    public List<String> getAbstracts()
    {
        return this.abstracts;
    }

    public List<String> getKeywords()
    {
        return ((StringListXMLEventParser) this.getField("Keywords")).getStrings();
    }

    public String getServiceType()
    {
        return (String) this.getField("ServiceType");
    }

    public List<String> getServiceTypeVersions()
    {
        return this.serviceTypeVersions;
    }

    public String getFees()
    {
        return (String) this.getField("Fees");
    }

    public List<String> getAccessConstraints()
    {
        return this.accessConstraints;
    }

    public List<String> getProfiles()
    {
        return this.profiles;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "ServiceTypeVersion"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.serviceTypeVersions.add(s);
        }
        else if (ctx.isStartElement(event, "Abstract"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.abstracts.add(s);
        }
        else if (ctx.isStartElement(event, "AccessConstraints"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.accessConstraints.add(s);
        }
        else if (ctx.isStartElement(event, "Title"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.titles.add(s);
        }
        else if (ctx.isStartElement(event, "Profile"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.profiles.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
