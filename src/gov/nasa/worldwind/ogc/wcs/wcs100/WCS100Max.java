/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: WCS100Max.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100Max extends AbstractXMLEventParser
{
    public WCS100Max(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getClosure()
    {
        return (String) this.getField("closure");
    }

    public String getMax()
    {
        return (String) this.getField("CharactersContent");
    }
}
