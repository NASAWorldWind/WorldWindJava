/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id$
 */
public class WCS100DCPType extends AbstractXMLEventParser
{
    public WCS100DCPType(String namespaceURI)
    {
        super(namespaceURI);
    }

    public WCS100HTTP getHTTP()
    {
        return (WCS100HTTP) this.getField("HTTP");
    }
}
