/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>param</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaParam.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ColladaParam extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaParam(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the <i>name</i> field.
     *
     * @return The value of the <i>name</i> field, or null if the field is not set.
     */
    public String getName()
    {
        return (String) this.getField("name");
    }
}
