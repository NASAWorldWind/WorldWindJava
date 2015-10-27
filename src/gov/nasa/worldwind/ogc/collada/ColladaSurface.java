/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>surface</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaSurface.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaSurface extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaSurface(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the <i>init_from</i> field of this surface.
     *
     * @return The <i>init_from</i> field, or null if it is not set.
     */
    public String getInitFrom()
    {
        return (String) this.getField("init_from");
    }
}
