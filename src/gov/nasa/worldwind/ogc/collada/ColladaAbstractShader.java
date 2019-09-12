/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Base class for COLLADA shaders.
 *
 * @author pabercrombie
 * @version $Id: ColladaAbstractShader.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaAbstractShader extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected ColladaAbstractShader(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Indicates the shader's emission parameter.
     *
     * @return The emission parameter, or null if none is set.
     */
    public ColladaTextureOrColor getEmission()
    {
        return (ColladaTextureOrColor) this.getField("emission");
    }

    /**
     * Indicates the shader's ambient parameter.
     *
     * @return The ambient parameter, or null if none is set.
     */
    public ColladaTextureOrColor getAmbient()
    {
        return (ColladaTextureOrColor) this.getField("ambient");
    }

    /**
     * Indicates the shader's diffuse parameter.
     *
     * @return The diffuse parameter, or null if none is set.
     */
    public ColladaTextureOrColor getDiffuse()
    {
        return (ColladaTextureOrColor) this.getField("diffuse");
    }

    /**
     * Indicates the shader's specular parameter.
     *
     * @return The specular parameter, or null if none is set.
     */
    public ColladaTextureOrColor getSpecular()
    {
        return (ColladaTextureOrColor) this.getField("specular");
    }
}
