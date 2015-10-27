/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: WCS100RangeSetHolder.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100RangeSetHolder extends AbstractXMLEventParser
{
    public WCS100RangeSetHolder(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getSemantic()
    {
        return (String) this.getField("semantic");
    }

    public String getRefSys()
    {
        return (String) this.getField("refSys");
    }

    public String getRefSysLabel()
    {
        return (String) this.getField("refSysLabel");
    }

    public WCS100RangeSet getRangeSet()
    {
        return (WCS100RangeSet) this.getField("RangeSet");
    }
}
