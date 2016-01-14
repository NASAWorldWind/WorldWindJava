/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.GL2;

/**
 * Provides a simple lighting model with one light. This model uses only OpenGL light 0.
 *
 * @author tag
 * @version $Id: BasicLightingModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicLightingModel implements LightingModel
{
    protected OGLStackHandler lightingStackHandler = new OGLStackHandler();
    protected Vec4 lightDirection = new Vec4(1.0, 0.5, 1.0);
    protected Material lightMaterial = Material.WHITE;
    protected long frameID;

    public void beginLighting(DrawContext dc)
    {
        if (this.lightingStackHandler.isActive())
            return; // lighting is already enabled

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.lightingStackHandler.pushAttrib(gl, GL2.GL_LIGHTING_BIT);

        this.apply(dc);
    }

    public void endLighting(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.lightingStackHandler.pop(gl);
        this.lightingStackHandler.clear();
    }

    /**
     * Returns the model's light direction.
     *
     * @return the model's light direction.
     */
    public Vec4 getLightDirection()
    {
        return lightDirection;
    }

    /**
     * Specifies the model's light direction.
     *
     * @param lightDirection the model's light direction.
     *
     * @throws IllegalArgumentException if the light direction is null.
     */
    public void setLightDirection(Vec4 lightDirection)
    {
        if (lightDirection == null)
        {
            String message = Logging.getMessage("nullValue.LightDirectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.lightDirection = lightDirection;
    }

    /**
     * Returns the model's light material.
     *
     * @return the model's light material.
     */
    public Material getLightMaterial()
    {
        return lightMaterial;
    }

    /**
     * Specifies the model's light direction.
     *
     * @param lightMaterial the model's light material.
     *
     * @throws IllegalArgumentException if the light material is null.
     */
    public void setLightMaterial(Material lightMaterial)
    {
        if (lightMaterial == null)
        {
            String message = Logging.getMessage("nullValue.LightMaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.lightMaterial = lightMaterial;
    }

    protected void apply(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glEnable(GL2.GL_LIGHTING);
        applyStandardLightModel(gl);
        applyStandardShadeModel(gl);

        gl.glEnable(GL2.GL_LIGHT0);
        applyStandardLightMaterial(gl, GL2.GL_LIGHT0, this.lightMaterial);
        applyStandardLightDirection(gl, GL2.GL_LIGHT0, this.lightDirection);
    }

    protected void applyStandardLightModel(GL2 gl)
    {
        float[] modelAmbient = new float[4];
        modelAmbient[0] = 1.0f;
        modelAmbient[1] = 1.0f;
        modelAmbient[2] = 1.0f;
        modelAmbient[3] = 0.0f;

        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, modelAmbient, 0);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
//        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_FALSE);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
    }

    protected void applyStandardShadeModel(GL2 gl)
    {
        gl.glShadeModel(GL2.GL_SMOOTH);
    }

    protected static void applyStandardLightMaterial(GL2 gl, int light, Material material)
    {
        // The alpha value at a vertex is taken only from the diffuse material's alpha channel, without any
        // lighting computations applied. Therefore we specify alpha=0 for all lighting ambient, specular and
        // emission values. This will have no effect on material alpha.

        float[] ambient = new float[4];
        float[] diffuse = new float[4];
        float[] specular = new float[4];
        material.getDiffuse().getRGBColorComponents(diffuse);
        material.getSpecular().getRGBColorComponents(specular);
        ambient[3] = diffuse[3] = specular[3] = 0.0f;

        gl.glLightfv(light, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(light, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(light, GL2.GL_SPECULAR, specular, 0);
    }

    protected void applyStandardLightDirection(GL2 gl, int light, Vec4 direction)
    {
        // Setup the light as a directional light coming from the viewpoint. This requires two state changes
        // (a) Set the light position as direction x, y, z, and set the w-component to 0, which tells OpenGL this is
        //     a directional light.
        // (b) Invoke the light position call with the identity matrix on the modelview stack. Since the position
        //     is transformed by the

        Vec4 vec = direction.normalize3();
        float[] params = new float[4];
        params[0] = (float) vec.x;
        params[1] = (float) vec.y;
        params[2] = (float) vec.z;
        params[3] = 0.0f;

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glLightfv(light, GL2.GL_POSITION, params, 0);

        gl.glPopMatrix();
    }
}
