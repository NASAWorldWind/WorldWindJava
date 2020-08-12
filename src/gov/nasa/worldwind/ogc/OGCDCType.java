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

import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Parses an OGC DCPType element.
 *
 * @author tag
 * @version $Id: OGCDCType.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGCDCType extends AbstractXMLEventParser
{
    protected QName GET;
    protected QName POST;
    protected QName HTTP;
    protected QName ONLINE_RESOURCE;

    public static class DCPInfo
    {
        protected String protocol;
        protected String method;
        protected OGCOnlineResource onlineResource;

        public DCPInfo(String protocol)
        {
            this.protocol = protocol;
        }
    }

    protected List<DCPInfo> protocols = new ArrayList<DCPInfo>(1);

    public OGCDCType(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    @Override
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        XMLEventParser defaultParser = null;

        if (ctx.isStartElement(event, ONLINE_RESOURCE))
            defaultParser = new OGCOnlineResource(this.getNamespaceURI());

        return ctx.allocate(event, defaultParser);
    }

    private void initialize()
    {
        GET = new QName(this.getNamespaceURI(), "Get");
        POST = new QName(this.getNamespaceURI(), "Post");
        HTTP = new QName(this.getNamespaceURI(), "HTTP");
        ONLINE_RESOURCE = new QName(this.getNamespaceURI(), "OnlineResource");
    }

    @Override
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, HTTP))
        {
            this.addProtocol(event.asStartElement().getName().getLocalPart());
        }
        else if (ctx.isStartElement(event, GET) || ctx.isStartElement(event, POST))
        {
            this.addRequestMethod(event.asStartElement().getName().getLocalPart());
        }
        else if (ctx.isStartElement(event, ONLINE_RESOURCE))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OGCOnlineResource)
                    this.addOnlineResource((OGCOnlineResource) o);
            }
        }
    }

    public List<DCPInfo> getDCPInfos()
    {
        return this.protocols;
    }

    protected void addProtocol(String protocol)
    {
        this.protocols.add(new DCPInfo(protocol));
    }

    protected void addRequestMethod(String requestMethod)
    {
        DCPInfo dcpi = this.protocols.get(this.protocols.size() - 1);

        if (dcpi.method != null)
        {
            dcpi = new DCPInfo(dcpi.protocol);
            this.protocols.add(dcpi);
        }

        dcpi.method = requestMethod;
    }

    protected void addOnlineResource(OGCOnlineResource onlineResource)
    {
        DCPInfo dcpi = this.protocols.get(this.protocols.size() - 1);

        dcpi.onlineResource = onlineResource;
    }

    public OGCOnlineResource getOnlineResouce(String protocol, String requestMethod)
    {
        for (DCPInfo dcpi : this.getDCPInfos())
        {
            if (!dcpi.protocol.equalsIgnoreCase(protocol))
                continue;

            if (dcpi.method.equalsIgnoreCase(requestMethod))
                return dcpi.onlineResource;
        }

        return null;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (DCPInfo dcpi : this.getDCPInfos())
        {
            sb.append(dcpi.protocol).append(", ");
            sb.append(dcpi.method).append(", ");
            sb.append(dcpi.onlineResource.toString());
        }

        return sb.toString();
    }
}
