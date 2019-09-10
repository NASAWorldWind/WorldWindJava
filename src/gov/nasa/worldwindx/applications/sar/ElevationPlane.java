/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.util.List;

/**
 * Renders a textured plane at a given elevation.
 *
 * @author Patrick Murris
 * @version $Id: ElevationPlane.java 2231 2014-08-15 19:03:12Z dcollins $
 */
public class ElevationPlane extends Polygon
{
    private Object imageSource;
    protected WWTexture texture;
    private double imageSize = 500; // meter

    protected OGLStackHandler osh = new OGLStackHandler();

    public ElevationPlane()
    {
        this.getAttributes().setEnableLighting(false);
    }

    /**
     * Get the source for the fill pattern image. Can be a file path to a local image or a {@link
     * java.awt.image.BufferedImage} reference.
     *
     * @return the source for the fill pattern image - can be null.
     */
    public Object getImageSource()
    {
        return this.imageSource;
    }

    /**
     * Set the source for the fill pattern image. Can be a file path to a local image or a {@link
     * java.awt.image.BufferedImage} reference.
     *
     * @param imageSource the source for the fill pattern image - can be null.
     */
    public void setImageSource(Object imageSource)
    {
        this.imageSource = imageSource;
        this.texture = null;
    }

    /**
     * Get the real world image size in meter. The image source is repeated so that one tile covers this distance.
     *
     * @return the real world image size in meter.
     */
    public double getImageSize()
    {
        return this.imageSize;
    }

    /**
     * Set the real world image size in meter. The image source will be repeated so that one tile will covers this
     * distance.
     *
     * @param sizeInMeter the real world image size in meter.
     */
    public void setImageSize(double sizeInMeter)
    {
        this.imageSize = sizeInMeter;
    }

    // Airspace Polygon overload
    protected void doRenderGeometry(DrawContext dc, String drawStyle, List<LatLon> locations, List<Boolean> edgeFlags)
    {
        this.beginRendering(dc);
        try
        {
            // Setup texture coordinates generation
            this.applyTextureState(dc);

            // Disable writing to depth buffer
            // TODO: let the application decide whether the plane should be translucent?
            dc.getGL().glDepthMask(false);

            // Draw
            super.doRenderGeometry(dc, drawStyle, locations, edgeFlags);
        }
        finally
        {
            this.unApplyTextureState(dc);
            this.endRendering(dc);
        }
    }

    protected void beginRendering(DrawContext dc)
    {
        // TODO: review attributes
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        osh.pushAttrib(gl, GL2.GL_COLOR_BUFFER_BIT // for alpha func
            | GL2.GL_ENABLE_BIT
            | GL2.GL_CURRENT_BIT
            | GL2.GL_DEPTH_BUFFER_BIT // for depth func
            | GL2.GL_TRANSFORM_BIT);
        osh.pushTextureIdentity(gl);
    }

    protected void endRendering(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        osh.pop(gl);
    }

    protected void applyTextureState(DrawContext dc)
    {
        WWTexture texture = getTexture();
        if (texture == null)
            return;

        if (!texture.bind(dc))
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        // Texture coordinates generation
        double[][] planes = this.computePlanes(dc);
        if (planes == null)
            return;

        gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
        gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
        gl.glTexGendv(GL2.GL_S, GL2.GL_OBJECT_PLANE, planes[0], 0);
        gl.glTexGendv(GL2.GL_T, GL2.GL_OBJECT_PLANE, planes[1], 0);
        gl.glEnable(GL2.GL_TEXTURE_GEN_S);
        gl.glEnable(GL2.GL_TEXTURE_GEN_T);
        // Pattern scaling
        gl.glMatrixMode(GL2.GL_TEXTURE_MATRIX);
        gl.glScaled(1 / this.imageSize, 1 / this.imageSize, 1f);
        // Texture setup
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        // TODO: factor in polygon opacity?
    }

    protected void unApplyTextureState(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, OGLUtil.DEFAULT_TEXTURE_GEN_MODE);
        gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, OGLUtil.DEFAULT_TEXTURE_GEN_MODE);
        gl.glTexGendv(GL2.GL_S, GL2.GL_OBJECT_PLANE, OGLUtil.DEFAULT_TEXTURE_GEN_S_OBJECT_PLANE, 0);
        gl.glTexGendv(GL2.GL_T, GL2.GL_OBJECT_PLANE, OGLUtil.DEFAULT_TEXTURE_GEN_T_OBJECT_PLANE, 0);

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    protected double[][] computePlanes(DrawContext dc)
    {
        double[][] planes = new double[2][4];
        // Compute two planes perpendicular to the polygon at its reference position.
        Position center = this.getReferencePosition();
        if (center == null)
            return null;

        Vec4 north = dc.getGlobe().computeNorthPointingTangentAtLocation(center.latitude, center.longitude);
        Vec4 normal = dc.getGlobe().computeSurfaceNormalAtLocation(center.latitude, center.longitude);
        Vec4 east = north.cross3(normal);
        north.toArray4(planes[0], 0); // texture coordinate s
        east.toArray4(planes[1], 0);  // texture coordinate t

        return planes;
    }

    protected WWTexture getTexture()
    {
        if (this.texture == null && this.imageSource != null)
            this.texture = new BasicWWTexture(this.imageSource);

        return this.texture;
    }
}
