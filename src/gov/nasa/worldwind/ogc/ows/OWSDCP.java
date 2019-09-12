/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: OWSDCP.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class OWSDCP extends AbstractXMLEventParser
{
    public OWSDCP(String namespaceURI)
    {
        super(namespaceURI);
    }

    public OWSHTTP getHTTP()
    {
        return (OWSHTTP) this.getField("HTTP");
    }
}
