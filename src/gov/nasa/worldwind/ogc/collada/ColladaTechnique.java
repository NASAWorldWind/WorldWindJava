/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>technique</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaTechnique.java 675 2012-07-02 18:47:47Z pabercrombie $
 */
public class ColladaTechnique extends ColladaAbstractParamContainer
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaTechnique(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the shader contained by the technique. Supported shaders are <i>lambert</i> and <i>phong</i>.
     *
     * @return The shader for this technique, or null if the shader is not set, or is not supported.
     */
    public ColladaAbstractShader getShader()
    {
        Object o = this.getField("lambert");
        if (o != null)
            return (ColladaAbstractShader) o;

        o = this.getField("phong");
        if (o != null)
            return (ColladaAbstractShader) o;

        // TODO handle other shaders
        return null;
    }

    /**
     * Indicates the value of the <i>profile</i> field.
     *
     * @return The value of the profile field, or null if the field is not set.
     */
    public String getProfile()
    {
        return (String) this.getField("profile");
    }
}
