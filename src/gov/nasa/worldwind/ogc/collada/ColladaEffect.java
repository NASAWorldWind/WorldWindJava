/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.render.Material;

import java.awt.*;

/**
 * Represents the COLLADA <i>effect</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaEffect.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaEffect extends ColladaAbstractParamContainer
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaEffect(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the <i>profile_COMMON</i> field.
     *
     * @return The value of the <i>profile_COMMON</i> field, or null if the field is not set.
     */
    public ColladaProfileCommon getProfileCommon()
    {
        return (ColladaProfileCommon) this.getField("profile_COMMON");
    }

    /**
     * Indicates the <i>texture</i> specified by this <i>effect</i>. The texture is specified by the <i>diffuse</i>
     * field of the shader specified by the <i>profile_COMMON</i> element.
     *
     * @return The texture specified by this effect, or null if the texture cannot be resolved.
     */
    public ColladaTexture getTexture()
    {
        ColladaProfileCommon profile = this.getProfileCommon();
        if (profile == null)
            return null;

        ColladaTechnique technique = profile.getTechnique();
        if (technique == null)
            return null;

        ColladaAbstractShader shader = technique.getShader();
        if (shader == null)
            return null;

        ColladaTextureOrColor diffuse = shader.getDiffuse();
        if (diffuse == null)
            return null;

        return diffuse.getTexture();
    }

    /**
     * Indicates the material specified by this effect. Material is specified by the shader in the <i>profile_COMMON</i>
     * element.
     *
     * @return The material for this effect, or null if the material cannot be resolved.
     */
    public Material getMaterial()
    {
        ColladaProfileCommon profile = this.getProfileCommon();
        if (profile == null)
            return null;

        ColladaTechnique technique = profile.getTechnique();
        if (technique == null)
            return null;

        ColladaAbstractShader shader = technique.getShader();
        if (shader == null)
            return null;

        Color emission = null;
        Color ambient = null;
        Color diffuse = null;
        Color specular = null;

        ColladaTextureOrColor textureOrColor = shader.getEmission();
        if (textureOrColor != null)
            emission = textureOrColor.getColor();

        textureOrColor = shader.getAmbient();
        if (textureOrColor != null)
            ambient = textureOrColor.getColor();

        textureOrColor = shader.getSpecular();
        if (textureOrColor != null)
            specular = textureOrColor.getColor();

        textureOrColor = shader.getDiffuse();
        if (textureOrColor != null)
            diffuse = textureOrColor.getColor();

        // TODO what should be we do with materials that don't have Diffuse?
        if (diffuse == null)
            return null;

        if (emission == null)
            emission = new Color(0, 0, 0, diffuse.getAlpha());
        if (ambient == null)
            ambient = diffuse;
        if (specular == null)
            specular = new Color(255, 255, 255, diffuse.getAlpha());

        return new Material(specular, diffuse, ambient, emission, 1f);
    }

    /** {@inheritDoc} */
    @Override
    public ColladaNewParam getParam(String sid)
    {
        ColladaNewParam param = super.getParam(sid);
        if (param != null)
            return param;

        ColladaProfileCommon profile = this.getProfileCommon();
        if (profile == null)
            return null;

        return profile.getParam(sid);
    }
}
