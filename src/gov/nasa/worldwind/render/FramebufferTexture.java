/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: FramebufferTexture.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FramebufferTexture implements WWTexture
{
    protected WWTexture sourceTexture;
    protected Sector sector;
    protected List<LatLon> corners;

    protected int width;
    protected int height;
    protected TextureCoords textureCoords = new TextureCoords(0f, 0f, 1f, 1f);
    /** The density of explicit texture coordinates to specify for the quadrilateral the texture's applied to. */
    protected int tessellationDensity;

    /** The default density of texture coordinates to specify for the quadrilateral the texture's applied to. */
    protected static final int DEFAULT_TESSELLATION_DENSITY = 32;

    public FramebufferTexture(WWTexture imageSource, Sector sector, List<LatLon> corners)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sourceTexture = imageSource;
        this.sector = sector;
        this.corners = corners;

        this.tessellationDensity = DEFAULT_TESSELLATION_DENSITY;
    }

    public int getWidth(DrawContext dc)
    {
        return width;
    }

    public int getHeight(DrawContext dc)
    {
        return height;
    }

    public Sector getSector()
    {
        return sector;
    }

    public List<LatLon> getCorners()
    {
        return corners;
    }

    public boolean isTextureCurrent(DrawContext dc)
    {
        return dc.getTextureCache().getTexture(this) != null;
    }

    public Object getImageSource()
    {
        return this.sourceTexture;
    }

    public TextureCoords getTexCoords()
    {
        return this.textureCoords;
    }

    public boolean isTextureInitializationFailed()
    {
        return this.sourceTexture != null && this.sourceTexture.isTextureInitializationFailed();
    }

    public boolean bind(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Texture t = dc.getTextureCache().getTexture(this);

        if (t == null)
            t = this.initializeTexture(dc);

        if (t != null)
            t.bind(dc.getGL());

        return t != null;
    }

    public void applyInternalTransform(DrawContext dc)
    {
        // Framebuffer textures don't have an internal transform.
    }

    protected int getTessellationDensity()
    {
        return this.tessellationDensity;
    }

    protected Texture initializeTexture(DrawContext dc)
    {
        // The frame buffer can be used only during pre-rendering.
        if (!dc.isPreRenderMode())
            return null;

        // Bind actually binds the source texture only if the image source is available, otherwise it initiates image
        // source retrieval. If bind returns false, the image source is not yet available.
        if (this.sourceTexture == null || !this.sourceTexture.bind(dc))
            return null;

        // Ensure that the source texture size is available so that the FBO can be sized to match the source image.
        if (this.sourceTexture.getWidth(dc) < 1 || this.sourceTexture.getHeight(dc) < 1)
            return null;

        int potSourceWidth = WWMath.powerOfTwoCeiling(this.sourceTexture.getWidth(dc));
        int potSourceHeight = WWMath.powerOfTwoCeiling(this.sourceTexture.getHeight(dc));

        this.width = Math.min(potSourceWidth, dc.getView().getViewport().width);
        this.height = Math.min(potSourceHeight, dc.getView().getViewport().height);

        if (!this.generateTexture(dc, this.width, this.height))
            return null;

        GL gl = dc.getGL();

        TextureData td = new TextureData(gl.getGLProfile(), GL.GL_RGBA, this.width, this.height, 0, GL.GL_RGBA,
            GL.GL_UNSIGNED_BYTE, false, false, false, null, null);
        Texture t = TextureIO.newTexture(td);
        t.bind(gl); // must do this after generating texture because another texture is bound then

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

        gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, td.getInternalFormat(), 0, 0, td.getWidth(), td.getHeight(),
            td.getBorder());

        dc.getTextureCache().put(this, t);

        return t;
    }

    protected boolean generateTexture(DrawContext dc, int width, int height)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();

        Matrix geoToCartesian = this.computeGeographicToCartesianTransform(this.sector);

        try
        {
            ogsh.pushAttrib(gl, GL2.GL_COLOR_BUFFER_BIT
                | GL2.GL_ENABLE_BIT
                | GL2.GL_TRANSFORM_BIT
                | GL2.GL_VIEWPORT_BIT);

            // Fill the frame buffer with transparent black.
            gl.glClearColor(0f, 0f, 0f, 0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT);

            gl.glDisable(GL.GL_BLEND);
            gl.glDisable(GL.GL_CULL_FACE);
            gl.glDisable(GL.GL_DEPTH_TEST);

            // Setup a viewport with the dimensions of the texture, and a projection matrix of dimension 2.0 (along
            // each axis) centered at the origin. Using a projection matrix with these dimensions ensures that incoming
            // vertices are rasterized without any rounding error.
            ogsh.pushProjectionIdentity(gl);
            gl.glViewport(0, 0, width, height);
            gl.glOrtho(-1d, 1d, -1d, 1d, -1d, 1d);

            ogsh.pushModelviewIdentity(gl);
            ogsh.pushTextureIdentity(gl);

            if (this.sourceTexture != null)
            {
                try
                {
                    gl.glEnable(GL.GL_TEXTURE_2D);
                    if (!this.sourceTexture.bind(dc))
                        return false;

                    this.sourceTexture.applyInternalTransform(dc);

                    // Setup the texture to replace the fragment color at each pixel.
                    gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);

                    int tessellationDensity = this.getTessellationDensity();
                    this.drawQuad(dc, geoToCartesian, tessellationDensity, tessellationDensity);
                }
                finally
                {
                    gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE);
                    gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
                }
            }
        }
        finally
        {
            ogsh.pop(gl);
        }

        return true;
    }

    protected Matrix computeGeographicToCartesianTransform(Sector sector)
    {
        // Compute a transform that will map the geographic region defined by sector onto a cartesian region of width
        // and height 2.0 centered at the origin.

        double sx = 2.0 / sector.getDeltaLonDegrees();
        double sy = 2.0 / sector.getDeltaLatDegrees();

        double tx = -sector.getMinLongitude().degrees;
        double ty = -sector.getMinLatitude().degrees;

        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(Matrix.fromTranslation(-1.0, -1.0, 0.0));
        transform = transform.multiply(Matrix.fromScale(sx, sy, 1.0));
        transform = transform.multiply(Matrix.fromTranslation(tx, ty, 0.0));

        return transform;
    }

    protected Vec4 transformToQuadCoordinates(Matrix geoToCartesian, LatLon latLon)
    {
        return new Vec4(latLon.getLongitude().degrees, latLon.getLatitude().degrees, 0.0).transformBy4(geoToCartesian);
    }

    protected void drawQuad(DrawContext dc, Matrix geoToCartesian, int slices, int stacks)
    {
        Vec4 ll = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(0));
        Vec4 lr = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(1));
        Vec4 ur = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(2));
        Vec4 ul = this.transformToQuadCoordinates(geoToCartesian, this.corners.get(3));
        BilinearInterpolator interp = new BilinearInterpolator(ll, lr, ur, ul);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glBegin(GL2.GL_TRIANGLE_STRIP);
        try
        {
            this.drawQuad(dc, interp, slices, stacks);
        }
        finally
        {
            gl.glEnd();
        }
    }

    protected void drawQuad(DrawContext dc, BilinearInterpolator interp, int slices, int stacks)
    {
        double[] compArray = new double[4];
        double du = 1.0f / (float) slices;
        double dv = 1.0f / (float) stacks;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        for (int vi = 0; vi < stacks; vi++)
        {
            double v = vi * dv;
            double vn = (vi + 1) * dv;

            if (vi != 0)
            {
                interp.interpolate(slices * du, v, compArray);
                gl.glTexCoord2d(slices * du, v);
                gl.glVertex3dv(compArray, 0);

                interp.interpolate(0, v, compArray);
                gl.glTexCoord2d(0, v);
                gl.glVertex3dv(compArray, 0);
            }

            for (int ui = 0; ui <= slices; ui++)
            {
                double u = ui * du;

                interp.interpolate(u, v, compArray);
                gl.glTexCoord2d(u, v);
                gl.glVertex3dv(compArray, 0);

                interp.interpolate(u, vn, compArray);
                gl.glTexCoord2d(u, vn);
                gl.glVertex3dv(compArray, 0);
            }
        }
    }
}
