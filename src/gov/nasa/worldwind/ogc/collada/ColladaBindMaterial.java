/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>bind_material</i> element, and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaBindMaterial.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaBindMaterial extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaBindMaterial(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the <i>technique_common</i> element of the bind material.
     *
     * @return Technique common element, or null if none is set.
     */
    public ColladaTechniqueCommon getTechniqueCommon()
    {
        return (ColladaTechniqueCommon) this.getField("technique_common");
    }
}
