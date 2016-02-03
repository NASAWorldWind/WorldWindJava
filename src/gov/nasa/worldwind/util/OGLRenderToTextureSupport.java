/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import com.jogamp.opengl.util.texture.Texture;
import gov.nasa.worldwind.render.DrawContext;

import com.jogamp.opengl.*;

/**
 * OGLRenderToTextureSupport encapsulates the pattern of rendering GL commands to a destination texture. Currently only
 * the color pixel values are written to the destination texture, but other values (depth, stencil) should be possible
 * with modification or extension.
 * <p/>
 * OGLRenderToTextureSupport is compatible with GL version 1.1 or greater, but it attempts to use more recent features
 * whenever possible. Different GL feature sets result in different approaches to rendering to texture, therefore the
 * caller cannot depend on the mechanism by which OGLRenderToTextureSupport will write pixel values to the destination
 * texture. For this reason, OGLRenderToTextureSupport must be used when the contents of the windowing system buffer
 * (likely the back framebuffer) can be freely modified by OGLRenderToTextureSupport. The World Wind pre-render stage is
 * a good example of when it is appropriate to use OGLRenderToTextureSupport. Fore more information on the pre-render
 * stage, see {@link gov.nasa.worldwind.render.PreRenderable} and {@link gov.nasa.worldwind.layers.Layer#preRender(gov.nasa.worldwind.render.DrawContext)}.
 * <b>Note:</b> In order to achieve consistent results across all platforms, it is essential to clear the texture's
 * contents before rendering anything into the texture. Do this by invoking {@link
 * #clear(gov.nasa.worldwind.render.DrawContext, java.awt.Color)} immediately after any call to {@link
 * #beginRendering(gov.nasa.worldwind.render.DrawContext, int, int, int, int)}.
 * <p/>
 * The common usage pattern for OGLRenderToTextureSupport is as follows: <br/><code> DrawContext dc = ...; // Typically
 * passed in as an argument to the containing method.<br/> Texture texture = TextureIO.newTexture(new
 * TextureData(...);<br/> <br/> // Setup the drawing rectangle to match the texture dimensions, and originate from the
 * texture's lower left corner.<br/> OGLRenderToTextureSupport rttSupport = new OGLRenderToTextureSupport();<br/>
 * rttSupport.beginRendering(dc, 0, 0, texture.getWidth(), texture.getHeight());<br/> try<br/> {<br/> // Bind the
 * texture as the destination for color pixel writes.<br/> rttSupport.setColorTarget(dc, texture);<br/> // Clear the
 * texture contents with transparent black.<br/> rttSupport.clear(dc, new Color(0, 0, 0, 0));<br/> // Invoke desired GL
 * rendering commands.<br/> }<br/> finally<br/> {<br/> rttSupport.endRendering(dc);<br/> }<br/> </code>
 *
 * @author dcollins
 * @version $Id: OGLRenderToTextureSupport.java 1676 2013-10-21 18:32:30Z dcollins $
 */
public class OGLRenderToTextureSupport
{
    protected boolean isFramebufferObjectEnabled;
    protected Texture colorTarget;
    protected java.awt.Rectangle drawRegion;
    protected OGLStackHandler stackHandler;
    protected int framebufferObject;

    /** Constructs a new OGLRenderToTextureSupport, but otherwise does nothing. */
    public OGLRenderToTextureSupport()
    {
        this.isFramebufferObjectEnabled = true;
        this.stackHandler = new OGLStackHandler();
    }

    /**
     * Returns true if framebuffer objects are enabled for use (only applicable if the feature is available in the
     * current GL runtime)
     *
     * @return true if framebuffer objects are enabled, and false otherwise.
     */
    public boolean isEnableFramebufferObject()
    {
        return this.isFramebufferObjectEnabled;
    }

    /**
     * Specifies if framebuffer objects should be used if they are available in the current GL runtime.
     *
     * @param enable true to enable framebuffer objects, false to disable them.
     */
    public void setEnableFramebufferObject(boolean enable)
    {
        this.isFramebufferObjectEnabled = enable;
    }

    /**
     * Returns the texture currently set as the color buffer target, or null if no texture is currently bound as the
     * color buffer target.
     *
     * @return the Texture currently set as the color buffer target, or null if none exists.
     */
    public Texture getColorTarget()
    {
        return this.colorTarget;
    }

    /**
     * Sets the specified texture as the color buffer target. This texture receives the output of all GL commands
     * affecting the color buffer. Binding a null texture specifies that no texture should receive color values. If the
     * current color target texture is the same reference as the specified texture, this does nothing. Otherwise this
     * flushes any buffered pixel values to the current color target, and assigns the specified texture as the new color
     * target.
     * <p/>
     * If {@link #isEnableFramebufferObject()} is false, the supported texture formats for the color target are limited
     * only by the OpenGL implementation's supported formats. If {@link #isEnableFramebufferObject()} is true and the
     * DrawContext supports OpenGL framebuffer objects, the supported texture formats for the color target are as
     * follows: <ul> <li>RGB</li> <li>RGBA</li> <li>FLOAT_R_NV (on NVidia hardware)</li> <li>FLOAT_RG_NV (on NVidia
     * hardware)</li> <li>FLOAT_RGB_NV (on NVidia hardware)</li> <li>FLOAT_RGBA_NV (on NVidia hardware)</li> </ul>
     *
     * @param dc      the current DrawContext.
     * @param texture the Texture to use as the destination for GL commands affecting the color buffer. A null value is
     *                permitted.
     *
     * @throws IllegalArgumentException if the DrawContext is null.
     */
    public void setColorTarget(DrawContext dc, Texture texture)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.colorTarget == texture)
            return;

        // If we have a texture target, then write the current GL color buffer state to the current texture target
        // before binding a new target.
        if (this.colorTarget != null)
        {
            this.flushColor(dc);
        }

        // If framebuffer objects are enabled, then bind the target texture as the current framebuffer's color
        // attachment, and GL rendering commands then affect the target texture. Otherwise, GL rendering commands affect
        // the windowing system's write buffer (likely the onscreen back buffer), and are explicitly copied to the
        // texture in flush() or endRendering().
        if (this.useFramebufferObject(dc))
        {
            this.bindFramebufferColorAttachment(dc, texture);
        }

        this.colorTarget = texture;
    }

    /**
     * Clears the current texture target's pixels with the specified RGBA clear color. If the current color texture
     * target is null, this does nothing.
     *
     * @param dc    the current DrawContext.
     * @param color the RGBA clear color to write to the current color texture target.
     *
     * @throws IllegalArgumentException if either the DrawContext or the color is null.
     */
    public void clear(DrawContext dc, java.awt.Color color)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.colorTarget == null)
            return;

        float[] compArray = new float[4];
        color.getRGBComponents(compArray);
        // Premultiply color components by the alpha component.
        compArray[0] *= compArray[3];
        compArray[1] *= compArray[3];
        compArray[2] *= compArray[3];

        GL gl = dc.getGL();
        gl.glClearColor(compArray[0], compArray[1], compArray[2], compArray[3]);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Flushes any buffered pixel values to the appropriate target textures.
     *
     * @param dc the current DrawContext.
     *
     * @throws IllegalArgumentException if the DrawContext is null.
     */
    public void flush(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.flushColor(dc);
    }

    /**
     * Configures the GL attached to the specified DrawContext for rendering a 2D model to a texture. The modelview
     * matrix is set to the identity, the projection matrix is set to an orthographic projection aligned with the
     * specified draw rectangle (x, y, width, height), the viewport and scissor boxes are set to the specified draw
     * rectangle, and the depth test and depth write flags are disabled. Because the viewport and scissor boxes are set
     * to the draw rectangle, only the texels intersecting the specified drawing rectangle (x, y, width, height) are
     * affected by GL commands. Once rendering is complete, this should always be followed with a call to {@link
     * #endRendering(gov.nasa.worldwind.render.DrawContext)}.
     *
     * @param dc     the current DrawContext.
     * @param x      the x-coordinate of the draw region's lower left corner.
     * @param y      the y-coordinate of the draw region's lower left corner.
     * @param width  the draw region width.
     * @param height the draw region height.
     *
     * @throws IllegalArgumentException if the DrawContext is null.
     */
    public void beginRendering(DrawContext dc, int x, int y, int width, int height)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.drawRegion = new java.awt.Rectangle(x, y, width, height);

        // Note: there is no attribute bit for framebuffer objects. The default framebuffer object state (object ID 0
        // is bound as the current fbo) is restored in endRendering().
        this.stackHandler.pushAttrib(gl,
            GL2.GL_COLOR_BUFFER_BIT  // For clear color.
                | GL2.GL_DEPTH_BUFFER_BIT // For depth test and depth mask.
                | GL2.GL_SCISSOR_BIT      // For scissor test and scissor box.
                | GL2.GL_TRANSFORM_BIT    // For matrix mode.
                | GL2.GL_VIEWPORT_BIT);   // For viewport state.

        this.stackHandler.pushTextureIdentity(gl);
        this.stackHandler.pushProjectionIdentity(gl);
        gl.glOrtho(x, x + width, y, y + height, -1, 1);
        this.stackHandler.pushModelviewIdentity(gl);

        // Disable the depth test and writing to the depth buffer. This provides consistent render to texture behavior
        // regardless of whether we are using copy-to-texture or framebuffer objects. For copy-to-texture, the depth
        // test and depth writing are explicitly disabled. For fbos there is no depth buffer components, so the depth
        // dest is implicitly disabled.
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);
        // Enable the scissor test and set both the scissor box and the viewport to the specified region. This enables
        // the caller to set up rendering to a subset of the texture. Note that the scissor box defines the region
        // affected by a call to glClear().
        gl.glEnable(GL.GL_SCISSOR_TEST);
        gl.glScissor(x, y, width, height);
        gl.glViewport(x, y, width, height);

        if (this.useFramebufferObject(dc))
        {
            this.beginFramebufferObjectRendering(dc);
        }
    }

    /**
     * Flushes any buffered pixel values to the appropriate texure targets, then restores the GL state to its previous
     * configuration before {@link #beginRendering(gov.nasa.worldwind.render.DrawContext, int, int, int, int)} was
     * called. Finally, all texture targets associated with this OGLRenderToTextureSupport are unbound.
     *
     * @param dc the current DrawContext.
     *
     * @throws IllegalArgumentException if the DrawContext is null.
     */
    public void endRendering(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.flush(dc);

        if (this.useFramebufferObject(dc))
        {
            if (this.colorTarget != null)
            {
                this.bindFramebufferColorAttachment(dc, null);
            }

            this.endFramebufferObjectRendering(dc);
        }

        this.stackHandler.pop(gl);
        this.drawRegion = null;
        this.colorTarget = null;
    }

    protected void flushColor(DrawContext dc)
    {
        // If framebuffer objects are enabled, then texture contents are already affected by the any GL rendering
        // commands.
        if (this.useFramebufferObject(dc))
        {
            if (this.colorTarget != null)
            {
                // If the color target is attempting to use automatic mipmap generation, then we must manually update
                // its mipmap chain. Automatic mipmap generation is invoked when the GL client explicitly modifies the
                // texture contents by calling one of glTexImage or glTexSubImage. However when we render directly to
                // the texture using framebuffer objects, automatic mipmap generation is not invoked, and the texture's
                // mipmap chain contents are undefined until we explicitly update them.
                if (this.colorTarget.isUsingAutoMipmapGeneration())
                    this.updateMipmaps(dc, this.colorTarget);
            }
        }
        // If framebuffer objects are not enabled, then we've been rendering into the read buffer associated with the
        // windowing system (likely the onscreen back buffer). Explicitly copy the read buffer contents to the texture.
        else
        {
            if (this.colorTarget != null)
            {
                this.copyScreenPixelsToTexture(dc, this.drawRegion.x, this.drawRegion.y,
                    this.drawRegion.width, this.drawRegion.height, this.colorTarget);
            }
        }
    }

    protected void copyScreenPixelsToTexture(DrawContext dc, int x, int y, int width, int height, Texture texture)
    {
        int w = width;
        int h = height;

        // If the lower left corner of the region to copy is outside of the texture bounds, then exit and do nothing.
        if (x >= texture.getWidth() || y >= texture.getHeight())
            return;

        // Limit the dimensions of the region to copy so they fit into the texture's dimensions.
        if (w > texture.getWidth())
            w = texture.getWidth();
        if (h > texture.getHeight())
            h = texture.getHeight();

        GL gl = dc.getGL();

        try
        {
            // We want to copy the contents of the current GL read buffer to the specified texture target. However we do
            // not want to change any of the texture creation parameters (e.g. dimensions, internal format, border).
            // Therefore we use glCopyTexSubImage2D() to copy a region of the read buffer to a region of the texture.
            // glCopyTexSubImage2D() has two key features:
            // 1. Does not change the texture creation parameters. We want to upload a new region of data without
            //    changing the textures' defining parameters.
            // 2. Enables specification of a destination (x, y) offset in texels. This offset corresponds to the
            //    viewport (x, y) specified by the caller in beginRendering().
            texture.enable(gl);
            texture.bind(gl);
            gl.glCopyTexSubImage2D(
                texture.getTarget(), // target
                0,                   // level
                x, y,                // xoffset, yoffset
                x, y, w, h);         // x, y, width, height
        }
        finally
        {
            texture.disable(gl);
        }
    }

    protected void updateMipmaps(DrawContext dc, Texture texture)
    {
        GL gl = dc.getGL();

        try
        {
            texture.enable(gl);
            texture.bind(gl);
            gl.glGenerateMipmap(texture.getTarget());
        }
        finally
        {
            texture.disable(gl);
        }
    }

    protected boolean useFramebufferObject(DrawContext dc)
    {
        return this.isEnableFramebufferObject() && dc.getGLRuntimeCapabilities().isUseFramebufferObject();
    }

    protected void beginFramebufferObjectRendering(DrawContext dc)
    {
        // Binding a framebuffer object causes all GL operations to operate on the attached textures and renderbuffers
        // (if any).

        int[] framebuffers = new int[1];

        GL gl = dc.getGL();
        gl.glGenFramebuffers(1, framebuffers, 0);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, framebuffers[0]);

        this.framebufferObject = framebuffers[0];
        if (this.framebufferObject == 0)
        {
            throw new IllegalStateException("Frame Buffer Object not created.");
        }
    }

    protected void endFramebufferObjectRendering(DrawContext dc)
    {
        // Binding framebuffer object 0 (the default) causes GL operations to operate on the window system attached
        // framebuffer.

        int[] framebuffers = new int[] {this.framebufferObject};

        GL gl = dc.getGL();
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
        gl.glDeleteFramebuffers(1, framebuffers, 0);

        this.framebufferObject = 0;
    }

    protected void bindFramebufferColorAttachment(DrawContext dc, Texture texture)
    {
        GL gl = dc.getGL();

        // Attach the texture as color attachment 0 to the framebuffer.
        if (texture != null)
        {
            gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D,
                texture.getTextureObject(gl), 0);
            this.checkFramebufferStatus(dc);
        }
        // If the texture is null, detach color attachment 0 from the framebuffer.
        else
        {
            gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, 0, 0);
        }
    }

    protected void checkFramebufferStatus(DrawContext dc)
    {
        int status = dc.getGL().glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);

        switch (status)
        {
            // Framebuffer is configured correctly and supported on this hardware.
            case GL.GL_FRAMEBUFFER_COMPLETE:
                break;
            // Framebuffer is configured correctly, but not supported on this hardware.
            case GL.GL_FRAMEBUFFER_UNSUPPORTED:
                throw new IllegalStateException(getFramebufferStatusString(status));
                // Framebuffer is configured incorrectly. This should never happen, but we check anyway.
            default:
                throw new IllegalStateException(getFramebufferStatusString(status));
        }
    }

    protected static String getFramebufferStatusString(int status)
    {
        switch (status)
        {
            case GL.GL_FRAMEBUFFER_COMPLETE:
                return Logging.getMessage("OGL.FramebufferComplete");
            case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                return Logging.getMessage("OGL.FramebufferIncompleteAttachment");
            case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                return Logging.getMessage("OGL.FramebufferIncompleteDimensions");
            case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                return Logging.getMessage("OGL.FramebufferIncompleteDrawBuffer");
            case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                return Logging.getMessage("OGL.FramebufferIncompleteFormats");
            case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                return Logging.getMessage("OGL.FramebufferIncompleteMissingAttachment");
            case GL2.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                return Logging.getMessage("OGL.FramebufferIncompleteMultisample");
            case GL2.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                return Logging.getMessage("OGL.FramebufferIncompleteReadBuffer");
            case GL.GL_FRAMEBUFFER_UNSUPPORTED:
                return Logging.getMessage("OGL.FramebufferUnsupported");
            default:
                return null;
        }
    }
}
