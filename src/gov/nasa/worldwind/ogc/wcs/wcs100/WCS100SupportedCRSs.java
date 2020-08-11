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

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: WCS100SupportedCRSs.java 2062 2014-06-19 20:10:41Z tgaskins $
 */
public class WCS100SupportedCRSs extends AbstractXMLEventParser
{
    protected List<String> requestResponseCRSs = new ArrayList<String>(1);
    protected List<String> requestCRSs = new ArrayList<String>(1);
    protected List<String> responseCRSs = new ArrayList<String>(1);
    protected List<String> nativeCRSs = new ArrayList<String>(1);

    public WCS100SupportedCRSs(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<String> getRequestResponseCRSs()
    {
        return requestResponseCRSs;
    }

    public List<String> getRequestCRSs()
    {
        return requestCRSs;
    }

    public List<String> getResponseCRSs()
    {
        return responseCRSs;
    }

    public List<String> getNativeCRSs()
    {
        return nativeCRSs;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "requestResponseCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.requestResponseCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "requestCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.requestCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "responseCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.responseCRSs.add(s);
        }
        else if (ctx.isStartElement(event, "nativeCRSs"))
        {
            String s = ctx.getStringParser().parseString(ctx, event);
            if (!WWUtil.isEmpty(s))
                this.nativeCRSs.add(s);
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
