/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.WWUtil;

/**
 * Represents the COLLADA <i>Unit</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaUnit.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaUnit extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaUnit(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the "meter" attribute of the unit. This attribute is a scaling factor that converts length
     * units in the COLLADA document to meters (1.0 for meters, 1000 for kilometers, etc.) See COLLADA spec pg. 5-18.
     *
     * @return The scaling factor, or null if none is defined.
     */
    public Double getMeter()
    {
        String s = (String) this.getField("meter");
        return WWUtil.makeDouble(s);
    }
}
