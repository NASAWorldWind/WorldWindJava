/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import java.util.*;

/**
 * Represents the COLLADA <i>technique_common</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaTechniqueCommon.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaTechniqueCommon extends ColladaAbstractObject
{
    /** Materials contained by this technique. */
    protected List<ColladaInstanceMaterial> materials = new ArrayList<ColladaInstanceMaterial>();

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaTechniqueCommon(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the materials contained by this technique.
     *
     * @return List of materials. May return an empty list, but never returns null.
     */
    public List<ColladaInstanceMaterial> getMaterials()
    {
        return this.materials;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("instance_material"))
        {
            this.materials.add((ColladaInstanceMaterial) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
