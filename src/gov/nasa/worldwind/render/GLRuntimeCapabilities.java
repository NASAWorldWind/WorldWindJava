/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;

/**
 * GLRuntimeCapabilities describes the GL capabilities supported by the current GL runtime. It provides the caller with
 * the current GL version, with information about which GL features are available, and with properties defining the
 * capabilities of those features.
 * <p>
 * For each GL feature, there are three key pieces of information available through GLRuntimeCapabilities: <ul> <li>The
 * property <code>is[Feature]Available</code> defines whether or not the feature is supported by the current GL runtime.
 * This is an attribute of the GL runtime, and is typically configured automatically by a call to {@link
 * #initialize(com.jogamp.opengl.GLContext)}.</li> <li>The property <code>is[Feature]Enabled</code> defines whether or
 * not this feature should be used, and must be configured by the caller. </li> <li>The convenience method
 * <code>isUse[Feature]()</code>. This returns whether or not the feature is available and is enabled for use (it is
 * simply a conjunction of the "available" and "enabled" properties).</li> </ul>
 * <p>
 * GLRuntimeCapabilities is designed to automatically configure itself with information about the current GL runtime. To
 * invoke this behavior, call {@link #initialize(com.jogamp.opengl.GLContext)} with a valid GLContext at the beginning
 * of each rendering pass.
 *
 * @author dcollins
 * @version $Id: GLRuntimeCapabilities.java 1933 2014-04-14 22:54:19Z dcollins $
 */
public class GLRuntimeCapabilities
{
    protected static final String GL_EXT_FRAMEBUFFER_OBJECT_STRING = "GL_EXT_framebuffer_object";
    protected static final String GL_EXT_TEXTURE_FILTER_ANISOTROPIC_STRING = "GL_EXT_texture_filter_anisotropic";

    protected double glVersion;
    protected boolean isVMwareSVGA3D;
    protected boolean isAnisotropicTextureFilterAvailable;
    protected boolean isAnisotropicTextureFilterEnabled;
    protected boolean isFramebufferObjectAvailable;
    protected boolean isFramebufferObjectEnabled;
    protected boolean isVertexBufferObjectAvailable;
    protected boolean isVertexBufferObjectEnabled;
    protected int depthBits;
    protected double maxTextureAnisotropy;
    protected int maxTextureSize;
    protected int numTextureUnits;

    /**
     * Constructs a new GLAtttributes, enabling framebuffer objects, anisotropic texture filtering, and vertex buffer
     * objects. Note that these properties are marked as enabled, but they are not known to be available yet. All other
     * properties are set to default values which may be set explicitly by the caller, or implicitly by calling {@link
     * #initialize(com.jogamp.opengl.GLContext)}.
     * <p>
     * Note: The default vertex-buffer usage flag can be set via {@link gov.nasa.worldwind.Configuration} using the key
     * "gov.nasa.worldwind.avkey.VBOUsage". If that key is not specified in the configuration then vertex-buffer usage
     * defaults to <code>true</code>.
     */
    public GLRuntimeCapabilities()
    {
        this.isAnisotropicTextureFilterEnabled = true;
        this.isFramebufferObjectEnabled = true;
        this.isVertexBufferObjectEnabled = Configuration.getBooleanValue(AVKey.VBO_USAGE, true);
        this.maxTextureAnisotropy = -1d;
    }

    /**
     * Initialize this GLRuntimeCapabilities from the specified {@link com.jogamp.opengl.GLContext}. The context's
     * runtime GL capabilities are examined, and the properties of this GLRuntimeCapabilities are modified accordingly.
     * Invoking initialize() may change any property of this GLRuntimeCapabilities, except the caller specified enable
     * flags: is[Feature]Enabled.
     *
     * @param glContext the GLContext from which to initialize GL runtime capabilities.
     *
     * @throws IllegalArgumentException if the glContext is null.
     */
    public void initialize(GLContext glContext)
    {
        if (glContext == null)
        {
            String message = Logging.getMessage("nullValue.GLContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL gl = glContext.getGL();

        if (this.glVersion < 1.0)
        {
            String s = gl.glGetString(GL.GL_VERSION);
            if (s != null)
            {
                s = s.substring(0, 3);
                Double d = WWUtil.convertStringToDouble(s);
                if (d != null)
                    this.glVersion = d;
            }
        }

        // Determine whether or not the OpenGL implementation is provided by the VMware SVGA 3D driver. This flag is
        // used to work around bugs and unusual behavior in the VMware SVGA 3D driver. The VMware drivers tested on
        // 7 August 2013 report the following strings for GL_VENDOR and GL_RENDERER:
        // - GL_VENDOR: "VMware, Inc."
        // - GL_RENDERER: "Gallium 0.4 on SVGA3D; build: RELEASE;"
        String glVendor = gl.glGetString(GL.GL_VENDOR);
        String glRenderer = gl.glGetString(GL.GL_RENDERER);
        if (glVendor != null && glVendor.toLowerCase().contains("vmware")
            && glRenderer != null && glRenderer.toLowerCase().contains("svga3d"))
        {
            this.isVMwareSVGA3D = true;
        }

        this.isAnisotropicTextureFilterAvailable = gl.isExtensionAvailable(GL_EXT_TEXTURE_FILTER_ANISOTROPIC_STRING);
        this.isFramebufferObjectAvailable = gl.isExtensionAvailable(GL_EXT_FRAMEBUFFER_OBJECT_STRING);
        // Vertex Buffer Objects are supported in version 1.5 or greater only.
        this.isVertexBufferObjectAvailable = this.glVersion >= 1.5;

        if (this.depthBits == 0)
        {
            int[] params = new int[1];
            gl.glGetIntegerv(GL.GL_DEPTH_BITS, params, 0);
            this.depthBits = params[0];
        }

        // Texture max anisotropy defaults to -1. A value less than 2.0 indicates that this graphics context does not
        // support texture anisotropy.
        if (this.maxTextureAnisotropy < 0)
        {
            // Documentation on the anisotropic texture filter is available at
            // http://www.opengl.org/registry/specs/EXT/texture_filter_anisotropic.txt
            if (this.isAnisotropicTextureFilterAvailable)
            {
                // The maxAnisotropy value can be any real value. A value less than 2.0 indicates that the graphics
                // context does not support texture anisotropy.
                float[] params = new float[1];
                gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, params, 0);
                this.maxTextureAnisotropy = params[0];
            }
        }

        if (this.numTextureUnits == 0)
        {
            int[] params = new int[1];
            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, params, 0);
            this.numTextureUnits = params[0];
        }

        if (this.maxTextureSize == 0)
        {
            int[] params = new int[1];
            gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, params, 0);
            this.maxTextureSize = params[0];
        }
    }

    /**
     * Returns the current GL runtime version as a real number. For example, if the GL version is 1.5, this returns the
     * floating point number equivalent to 1.5.
     *
     * @return GL version as a number.
     */
    public double getGLVersion()
    {
        return this.glVersion;
    }

    /**
     * Sets the current GL runtime version as a real number. For example, to set a GL version of 1.5, specify the
     * floating point number 1.5.
     *
     * @param version the GL version as a number.
     */
    public void setGLVersion(double version)
    {
        this.glVersion = version;
    }

    /**
     * Returns true if the OpenGL implementation is provided by the VMware SVGA 3D driver. Otherwise this returns
     * false.
     * <p>
     * This flag is used to work around bugs and unusual behavior in the VMware SVGA 3D driver. For details on VMware
     * graphics drivers, see <a href="http://www.vmware.com/files/pdf/techpaper/vmware-horizon-view-graphics-acceleration-deployment.pdf">http://www.vmware.com/files/pdf/techpaper/vmware-horizon-view-graphics-acceleration-deployment.pdf</a>.
     *
     * @return true if the OpenGL implementation is VMware SVGA 3D, and false otherwise.
     */
    public boolean isVMwareSVGA3D()
    {
        return this.isVMwareSVGA3D;
    }

    /**
     * Returns true if anisotropic texture filtering is available in the current GL runtime, and is enabled. Otherwise
     * this returns false. For details on GL anisotropic texture filtering, see <a href="http://www.opengl.org/registry/specs/EXT/texture_filter_anisotropic.txt">http://www.opengl.org/registry/specs/EXT/texture_filter_anisotropic.txt</a>.
     *
     * @return true if anisotropic texture filtering is available and enabled, and false otherwise.
     */
    public boolean isUseAnisotropicTextureFilter()
    {
        return this.isAnisotropicTextureFilterAvailable && this.isAnisotropicTextureFilterEnabled;
    }

    /**
     * Returns true if framebuffer objects are available in the current GL runtime, and are enabled. Otherwise this
     * returns false. For details on GL framebuffer objects, see <a href="http://www.opengl.org/registry/specs/EXT/framebuffer_object.txt">http://www.opengl.org/registry/specs/EXT/framebuffer_object.txt</a>.
     *
     * @return true if framebuffer objects are available and enabled, and false otherwise.
     */
    public boolean isUseFramebufferObject()
    {
        return this.isFramebufferObjectAvailable && this.isFramebufferObjectEnabled;
    }

    /**
     * Returns true if vertex buffer objects are available in the current GL runtime, and are enabled. Otherwise this
     * returns false. For details on GL vertex buffer objects, see <a href="http://www.opengl.org/registry/specs/ARB/vertex_buffer_object.txt">http://www.opengl.org/registry/specs/ARB/vertex_buffer_object.txt</a>.
     *
     * @return true if vertex buffer objects are available and enabled, and false otherwise.
     */
    public boolean isUseVertexBufferObject()
    {
        return this.isVertexBufferObjectAvailable && this.isVertexBufferObjectEnabled;
    }

    /**
     * Returns true if anisotropic filtering is available in the current GL runtime.
     *
     * @return true if anisotropic texture filtering is available, and false otherwise.
     */
    public boolean isAnisotropicTextureFilterAvailable()
    {
        return this.isAnisotropicTextureFilterAvailable;
    }

    /**
     * Sets whether or not anisotropic filtering is available in the current GL runtime.
     *
     * @param available true to flag anisotropic texture filtering as available, and false otherwise.
     */
    public void setAnisotropicTextureFilterAvailable(boolean available)
    {
        this.isAnisotropicTextureFilterAvailable = available;
    }

    /**
     * Returns true if anisotropic texture filtering is enabled for use (only applicable if the feature is available in
     * the current GL runtime)
     *
     * @return true if anisotropic texture filtering is enabled, and false otherwise.
     */
    public boolean isAnisotropicTextureFilterEnabled()
    {
        return this.isAnisotropicTextureFilterEnabled;
    }

    /**
     * Sets whether or not anisotropic texture filtering should be used if it is available in the current GL runtime.
     *
     * @param enable true to enable anisotropic texture filtering, false to disable it.
     */
    public void setAnisotropicTextureFilterEnabled(boolean enable)
    {
        this.isAnisotropicTextureFilterEnabled = enable;
    }

    /**
     * Returns true if framebuffer objects are available in the current GL runtime.
     *
     * @return true if framebuffer objects are available, and false otherwise.
     */
    public boolean isFramebufferObjectAvailable()
    {
        return this.isFramebufferObjectAvailable;
    }

    /**
     * Sets whether or not framebuffer objects are available in the current GL runtime.
     *
     * @param available true to flag framebuffer objects as available, and false otherwise.
     */
    public void setFramebufferObjectAvailable(boolean available)
    {
        this.isFramebufferObjectAvailable = available;
    }

    /**
     * Returns true if framebuffer objects are enabled for use (only applicable if the feature is available in the
     * current GL runtime)
     *
     * @return true if framebuffer objects are enabled, and false otherwise.
     */
    public boolean isFramebufferObjectEnabled()
    {
        return this.isFramebufferObjectEnabled;
    }

    /**
     * Sets whether or not framebuffer objects should be used if they are available in the current GL runtime.
     *
     * @param enable true to enable framebuffer objects, false to disable them.
     */
    public void setFramebufferObjectEnabled(boolean enable)
    {
        this.isFramebufferObjectEnabled = enable;
    }

    /**
     * Returns true if vertex buffer objects are available in the current GL runtime.
     *
     * @return true if vertex buffer objects are available, and false otherwise.
     */
    public boolean isVertexBufferObjectAvailable()
    {
        return this.isVertexBufferObjectAvailable;
    }

    /**
     * Sets whether or not vertext buffer objects are available in the current GL runtime.
     *
     * @param available true to flag vertex buffer objects as available, and false otherwise.
     */
    public void setVertexBufferObjectAvailable(boolean available)
    {
        this.isVertexBufferObjectAvailable = available;
    }

    /**
     * Returns true if anisotropic vertex buffer objects are enabled for use (only applicable if the feature is
     * available in the current GL runtime).
     *
     * @return true if anisotropic vertex buffer objects are, and false otherwise.
     */
    public boolean isVertexBufferObjectEnabled()
    {
        return this.isVertexBufferObjectEnabled;
    }

    /**
     * Sets whether or not vertex buffer objects should be used if they are available in the current GL runtime.
     *
     * @param enable true to enable vertex buffer objects, false to disable them.
     */
    public void setVertexBufferObjectEnabled(boolean enable)
    {
        this.isVertexBufferObjectEnabled = enable;
    }

    /**
     * Returns the number of bitplanes in the current GL depth buffer. The number of bitplanes is directly proportional
     * to the accuracy of the GL renderer's hidden surface removal. The returned value is typically 16, 24 or 32. For
     * more information on OpenGL depth buffering, see <a href="http://www.opengl.org/archives/resources/faq/technical/depthbuffer.htm"
     * target="_blank">http://www.opengl.org/archives/resources/faq/technical/depthbuffer.htm</a>.
     *
     * @return the number of bitplanes in the current GL depth buffer.
     */
    public int getDepthBits()
    {
        return this.depthBits;
    }

    /**
     * Sets the number of bitplanes in the current GL depth buffer. The specified value is typically 16, 24 or 32.
     *
     * @param depthBits the number of bitplanes in the current GL depth buffer.
     *
     * @throws IllegalArgumentException if depthBits is less than one.
     */
    public void setDepthBits(int depthBits)
    {
        if (maxTextureSize < 1)
        {
            String message = Logging.getMessage("generic.DepthBitsLessThanOne");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.depthBits = depthBits;
    }

    /**
     * Returns a real number defining the maximum degree of texture anisotropy supported by the current GL runtime. This
     * defines the maximum ratio of the anisotropic texture filter. So 2.0 would define a maximum ratio of 2:1. If the
     * degree is less than 2, then the anisotropic texture filter is not supported by the current GL runtime.
     *
     * @return the maximum degree of texture anisotropy supported.
     */
    public double getMaxTextureAnisotropy()
    {
        return this.maxTextureAnisotropy;
    }

    /**
     * Sets the maximum degree of texture anisotropy supported by the current GL runtime.  This value defines the
     * maximum ratio of the an anisotropic texture filter. So 2.0 would define a maximum ratio of 2:1. A valueless than
     * 2 denotes that the anisotropic texture filter is not supported by the current GL runtime.
     *
     * @param maxAnisotropy the maximum degree of texture anisotropy supported.
     */
    public void setMaxTextureAnisotropy(double maxAnisotropy)
    {
        this.maxTextureAnisotropy = maxAnisotropy;
    }

    /**
     * Returns the maximum texture size in texels supported by the current GL runtime. For a 1D texture, this defines
     * the maximum texture width, for a 2D texture, this defines the maximum texture width and height, and for a 3D
     * texture, this defines the maximum width, height, and depth.
     *
     * @return the maximum texture size supported, in texels.
     */
    public int getMaxTextureSize()
    {
        return this.maxTextureSize;
    }

    /**
     * Sets the maximum texture size in texels supported by the current GL runtime. For a 1D texture, this defines the
     * maximum texture width, for a 2D texture, this defines the maximum texture width and height, and for a 3D texture,
     * this defines the maximum width, height, and depth.
     *
     * @param maxTextureSize the maximum texture size supported, in texels.
     *
     * @throws IllegalArgumentException if the size is less than one.
     */
    public void setMaxTextureSize(int maxTextureSize)
    {
        if (maxTextureSize < 1)
        {
            String message = Logging.getMessage("generic.MaxTextureSizeLessThanOne");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxTextureSize = maxTextureSize;
    }

    /**
     * Returns the number of texture units supported by the current GL runtime.
     *
     * @return the number of texture units supported.
     */
    public int getNumTextureUnits()
    {
        return this.numTextureUnits;
    }

    /**
     * Sets the number of texture units supported by the current GL runtime.
     *
     * @param numTextureUnits the number texture units supported.
     *
     * @throws IllegalArgumentException if the number of texture units is less than one.
     */
    public void setNumTextureUnits(int numTextureUnits)
    {
        if (numTextureUnits < 1)
        {
            String message = Logging.getMessage("generic.NumTextureUnitsLessThanOne");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.numTextureUnits = numTextureUnits;
    }
}
