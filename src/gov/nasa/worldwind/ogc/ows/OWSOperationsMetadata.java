/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
