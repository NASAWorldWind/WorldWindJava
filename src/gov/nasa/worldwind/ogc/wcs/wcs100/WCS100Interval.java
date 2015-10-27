/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs.wcs100;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * @author tag
 * @version $Id: WCS100Interval.java 2061 2014-06-19 19:59:40Z tgaskins $
 */
public class WCS100Interval extends AbstractXMLEventParser
{
    public WCS100Interval(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getType()
    {
        return (String) this.getField("type");
    }

    public String getSemantic()
    {
        return (String) this.getField("semantic");
    }

    public String getAtomic()
    {
        return (String) this.getField("atomic");
    }

    public WCS100Min getMin()
    {
        return (WCS100Min) this.getField("min");
    }

    public WCS100Max getMax()
    {
        return (WCS100Max) this.getField("max");
    }

    public String getRes()
    {
        return (String) this.getField("res");
    }
}
