/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>source</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaSource.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaSource extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaSource(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the <i>accessor</i> field of the <i>technique_common</i> field.
     *
     * @return The value of the <i>accessor</i> field, or null if either the <i>accessor</i> or <i>technique_common</i>
     *         is not set.
     */
    public ColladaAccessor getAccessor()
    {
        // Handles only the COLLADA Common profile
        ColladaTechniqueCommon technique = (ColladaTechniqueCommon) this.getField("technique_common");
        if (technique == null)
            return null;

        return (ColladaAccessor) technique.getField("accessor");
    }
}
