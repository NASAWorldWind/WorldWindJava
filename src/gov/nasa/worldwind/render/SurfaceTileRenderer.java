/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: SurfaceTileRenderer.java 2288 2014-08-30 20:24:56Z dcollins $
 */
public abstract class SurfaceTileRenderer implements Disposable
{
    private static final int DEFAULT_ALPHA_TEXTURE_SIZE = 1024;

    protected Texture alphaTexture;
    protected Texture outlineTexture;
    protected boolean showImageTileOutlines = false;
    protected boolean useImageTilePickColors = false;

    /**
     * Free internal resources held by this surface tile renderer. A GL context must be current when this method is
     * called.
     *
     * @throws com.jogamp.opengl.GLException - If an OpenGL context is not current when this method is called.
     */
    public void dispose()
    {
        GLContext context = GLContext.getCurrent();
        if (context == null || context.getGL() == null)
            return;

        GL gl = context.getGL();

        if (this.alphaTexture != null)
            this.alphaTexture.destroy(gl);
        this.alphaTexture = null;
        if (this.outlineTexture != null)
            this.outlineTexture.destroy(gl);
        this.outlineTexture = null;
    }

    public boolean isShowImageTileOutlines()
    {
        return showImageTileOutlines;
    }

    public void setShowImageTileOutlines(boolean showImageTileOutlines)
    {
        this.showImageTileOutlines = showImageTileOutlines;
    }

    /**
     * Indicates how this SurfaceTileRenderer interprets image tile colors during picking. When true, image tile RGB
     * colors are drawn during picking. When false, image tile RGB colors are replaced with the current RGB color.
     * Initially false.
     *
     * @return true if image tile RGB colors are drawn during picking, false if image tile RGB colors are replaced by
     *         the current RGB color.
     */
    public boolean isUseImageTilePickColors()
    {
        return this.useImageTilePickColors;
    }

    /**
     * Specifies how this SurfaceTileRenderer interprets image tile colors during picking. When true, image tile RGB
     * colors are drawn during picking. When false, image tile RGB colors are replaced with the current RGB color.
     *
     * @param useImageTilePickColors true if image tile RGB colors should be drawn during picking, false if image tile
     *                               RGB colors should be replaced by the current RGB color.
     */
    public void setUseImageTilePickColors(boolean useImageTilePickColors)
    {
        this.useImageTilePickColors = useImageTilePickColors;
    }

    public void renderTile(DrawContext dc, SurfaceTile tile)
    {
        if (tile == null)
        {
            String message = Logging.getMessage("nullValue.TileIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        ArrayList<SurfaceTile> al = new ArrayList<SurfaceTile>(1);
        al.add(tile);
        this.renderTiles(dc, al);
        al.clear();
    }

    protected static class Transform
    {
        double HScale;
        double VScale;
        double HShift;
        double VShift;
        double rotationDegrees;
    }

    abstract protected void preComputeTextureTransform(DrawContext dc, SectorGeometry sg, Transform t);

    abstract protected void computeTextureTransform(DrawContext dc, SurfaceTile tile, Transform t);

    abstract protected Iterable<SurfaceTile> getIntersectingTiles(DrawContext dc, SectorGeometry sg,
        Iterable<? extends SurfaceTile> tiles);

    public void renderTiles(DrawContext dc, Iterable<? extends SurfaceTile> tiles)
    {
        if (tiles == null)
        {
            String message = Logging.getMessage("nullValue.TileIterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        int alphaTextureUnit = GL.GL_TEXTURE1;
        boolean showOutlines = this.showImageTileOutlines && dc.getGLRuntimeCapabilities().getNumTextureUnits() > 2;

        gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT // for alpha func
            | GL2.GL_ENABLE_BIT
            | GL2.GL_CURRENT_BIT
            | GL2.GL_DEPTH_BUFFER_BIT // for depth func
            | GL2.GL_TRANSFORM_BIT);

        try
        {
            this.alphaTexture = dc.getTextureCache().getTexture(this);
            if (this.alphaTexture == null)
            {
                this.initAlphaTexture(dc, DEFAULT_ALPHA_TEXTURE_SIZE); // TODO: choose size to match incoming tile size?
                dc.getTextureCache().put(this, this.alphaTexture);
            }

            if (showOutlines && this.outlineTexture == null)
                this.initOutlineTexture(dc, 128);

            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(GL.GL_LEQUAL);

            gl.glEnable(GL2.GL_ALPHA_TEST);
            gl.glAlphaFunc(GL2.GL_GREATER, 0.01f);

            gl.glActiveTexture(GL.GL_TEXTURE0);
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glMatrixMode(GL2.GL_TEXTURE);
            gl.glPushMatrix();
            if (!dc.isPickingMode()) // treat texture as an image; modulate RGBA with the current color
            {
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
            }
            else if (this.useImageTilePickColors) // treat texture as pick colors; use texture RGBA directly
            {
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
            }
            else // treat texture as a pick mask; replace RGB with the current pick color
            {
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);
            }

            int numTexUnitsUsed = 2;
            if (showOutlines)
            {
                numTexUnitsUsed = 3;
                alphaTextureUnit = GL.GL_TEXTURE2;
                gl.glActiveTexture(GL.GL_TEXTURE1);
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glMatrixMode(GL2.GL_TEXTURE);
                gl.glPushMatrix();
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_ADD);
            }

            gl.glActiveTexture(alphaTextureUnit);
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glMatrixMode(GL2.GL_TEXTURE);
            gl.glPushMatrix();
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

            dc.getSurfaceGeometry().beginRendering(dc);

            // For each current geometry tile, find the intersecting image tiles and render the geometry
            // tile once for each intersecting image tile.
            Transform transform = new Transform();
            for (SectorGeometry sg : dc.getSurfaceGeometry())
            {
                Iterable<SurfaceTile> tilesToRender = this.getIntersectingTiles(dc, sg, tiles);
                if (tilesToRender == null)
                    continue;

                sg.beginRendering(dc, numTexUnitsUsed); // TODO: wrap in try/catch in case of exception

                // Pre-load info to compute the texture transform
                this.preComputeTextureTransform(dc, sg, transform);

                // For each intersecting tile, establish the texture transform necessary to map the image tile
                // into the geometry tile's texture space. Use an alpha texture as a mask to prevent changing the
                // frame buffer where the image tile does not overlap the geometry tile. Render both the image and
                // alpha textures via multi-texture rendering.
                // TODO: Figure out how to apply multi-texture to more than one tile at a time. Use fragment shader?
                for (SurfaceTile tile : tilesToRender)
                {
                    gl.glActiveTexture(GL.GL_TEXTURE0);

                    if (tile.bind(dc))
                    {
                        gl.glMatrixMode(GL2.GL_TEXTURE);
                        gl.glLoadIdentity();
                        tile.applyInternalTransform(dc, true);

                        // Determine and apply texture transform to map image tile into geometry tile's texture space
                        this.computeTextureTransform(dc, tile, transform);
                        gl.glScaled(transform.HScale, transform.VScale, 1d);
                        gl.glTranslated(transform.HShift, transform.VShift, 0d);

                        if (showOutlines)
                        {
                            gl.glActiveTexture(GL.GL_TEXTURE1);
                            this.outlineTexture.bind(gl);

                            // Apply the same texture transform to the outline texture. The outline textures uses a
                            // different texture unit than the tile, so the transform made above does not carry over.
                            gl.glMatrixMode(GL2.GL_TEXTURE);
                            gl.glLoadIdentity();
                            gl.glScaled(transform.HScale, transform.VScale, 1d);
                            gl.glTranslated(transform.HShift, transform.VShift, 0d);
                        }

                        // Prepare the alpha texture to be used as a mask where texture coords are outside [0,1]
                        gl.glActiveTexture(alphaTextureUnit);
                        this.alphaTexture.bind(gl);

                        // Apply the same texture transform to the alpha texture. The alpha texture uses a
                        // different texture unit than the tile, so the transform made above does not carry over.
                        gl.glMatrixMode(GL2.GL_TEXTURE);
                        gl.glLoadIdentity();
                        gl.glScaled(transform.HScale, transform.VScale, 1d);
                        gl.glTranslated(transform.HShift, transform.VShift, 0d);

                        // Render the geometry tile
                        sg.renderMultiTexture(dc, numTexUnitsUsed);
                    }
                }

                sg.endRendering(dc);
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE,
                Logging.getMessage("generic.ExceptionWhileRenderingLayer", this.getClass().getName()), e);
        }
        finally
        {
            dc.getSurfaceGeometry().endRendering(dc);

            gl.glActiveTexture(alphaTextureUnit);
            gl.glMatrixMode(GL2.GL_TEXTURE);
            gl.glPopMatrix();
            gl.glDisable(GL.GL_TEXTURE_2D);

            if (showOutlines)
            {
                gl.glActiveTexture(GL.GL_TEXTURE1);
                gl.glMatrixMode(GL2.GL_TEXTURE);
                gl.glPopMatrix();
                gl.glDisable(GL.GL_TEXTURE_2D);
            }

            gl.glActiveTexture(GL.GL_TEXTURE0);
            gl.glMatrixMode(GL2.GL_TEXTURE);
            gl.glPopMatrix();
            gl.glDisable(GL.GL_TEXTURE_2D);

            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE);
            if (dc.isPickingMode())
            {
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, OGLUtil.DEFAULT_SRC0_RGB);
                gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, OGLUtil.DEFAULT_COMBINE_RGB);
            }

            gl.glPopAttrib();
        }
    }

    private static void fillByteBuffer(ByteBuffer buffer, byte value)
    {
        for (int i = 0; i < buffer.capacity(); i++)
        {
            buffer.put(value);
        }
    }

    protected void initAlphaTexture(DrawContext dc, int size)
    {
        ByteBuffer textureBytes = Buffers.newDirectByteBuffer(size * size);
        fillByteBuffer(textureBytes, (byte) 0xff);

        GL gl = dc.getGL();

        TextureData textureData = new TextureData(gl.getGLProfile(), GL.GL_ALPHA, size, size, 0, GL.GL_ALPHA,
            GL.GL_UNSIGNED_BYTE, false, false, false, textureBytes.rewind(), null);
        this.alphaTexture = TextureIO.newTexture(textureData);

        this.alphaTexture.bind(gl);
        this.alphaTexture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        this.alphaTexture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        this.alphaTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
        this.alphaTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
        // Assume the default border color of (0, 0, 0, 0).
    }

    protected void initOutlineTexture(DrawContext dc, int size)
    {
        ByteBuffer textureBytes = Buffers.newDirectByteBuffer(size * size);
        for (int row = 0; row < size; row++)
        {
            for (int col = 0; col < size; col++)
            {
                byte p;
                if (row == 0 || col == 0 || row == size - 1 || col == size - 1)
                    p = (byte) 0xff;
                else
                    p = (byte) 0;
                textureBytes.put(row * size + col, p);
            }
        }

        GL gl = dc.getGL();

        TextureData textureData = new TextureData(gl.getGLProfile(), GL.GL_LUMINANCE, size, size, 0, GL.GL_LUMINANCE,
            GL.GL_UNSIGNED_BYTE, false, false, false, textureBytes.rewind(), null);
        this.outlineTexture = TextureIO.newTexture(textureData);

        this.outlineTexture.bind(gl);
        this.outlineTexture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        this.outlineTexture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        this.outlineTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        this.outlineTexture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    }
}
