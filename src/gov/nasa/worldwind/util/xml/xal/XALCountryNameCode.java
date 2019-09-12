/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml.xal;

/**
 * @author tag
 * @version $Id: XALCountryNameCode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class XALCountryNameCode extends XALAbstractObject
{
    public XALCountryNameCode(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getScheme()
    {
        return (String) this.getField("Scheme");
    }
}
