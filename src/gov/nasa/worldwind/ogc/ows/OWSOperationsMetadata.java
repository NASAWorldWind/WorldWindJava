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

import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * @author tag
 * @version $Id: OWSOperationsMetadata.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class OWSOperationsMetadata extends AbstractXMLEventParser
{
    protected List<OWSOperation> operations = new ArrayList<OWSOperation>(2);
    protected List<OWSConstraint> constraints = new ArrayList<OWSConstraint>(1);

    public OWSOperationsMetadata(String namespaceURI)
    {
        super(namespaceURI);
    }

    public List<OWSOperation> getOperations()
    {
        return this.operations;
    }

    public List<OWSConstraint> getConstraints()
    {
        return this.constraints;
    }

    public OWSOperation getOperation(String opName)
    {
        for (OWSOperation op : this.getOperations())
        {
            if (op.getName().equals(opName))
                return op;
        }

        return null;
    }

    public String getGetOperationAddress(String opProtocol, String opName)
    {
        OWSOperation op = this.getOperation(opName);
        if (opName != null)
        {
            for (OWSDCP dcp : op.getDCPs())
            {
                OWSHTTP http = dcp.getHTTP();
                if (http != null)
                {
                    if (opProtocol.equals("Get") && http.getGetAddress() != null)
                        return http.getGetAddress();
                    else if (opProtocol.equals("Post") && http.getPostAddress() != null)
                        return http.getPostAddress();
                }
            }
        }

        return null;
    }

    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (ctx.isStartElement(event, "Operation"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OWSOperation)
                    this.operations.add((OWSOperation) o);
            }
        }
        else if (ctx.isStartElement(event, "Constraint"))
        {
            XMLEventParser parser = this.allocate(ctx, event);
            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o != null && o instanceof OWSConstraint)
                    this.constraints.add((OWSConstraint) o);
            }
        }
        else
        {
            super.doParseEventContent(ctx, event, args);
        }
    }
}
