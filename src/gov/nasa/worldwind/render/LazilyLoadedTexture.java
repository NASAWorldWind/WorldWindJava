/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.image.*;
import java.beans.*;
import java.net.URL;

/**
 * Represents a texture derived from a lazily loaded image source such as an image file or a {@link
 * java.awt.image.BufferedImage}.
 * <p/>
 * The interface contains a method, {@link #isTextureInitializationFailed()} to determine whether the instance failed to
 * convert an image source to a texture. If such a failure occurs, the method returns true and no further attempts are
 * made to create the texture.
 * <p/>
 * This class performs lazy retrieval and loading of an image source, attempting to retrieve and load the image source
 * only when the {@link #bind(DrawContext)} or {@link #applyInternalTransform(DrawContext)} methods are called. If the
 * image source is a {@link BufferedImage} the associated {@link Texture} object is created and available immediately
 * when <code>bind</code> or <code>applyInternalTransform</code> are called. If the image source is a local file or
 * remote stream (URL), retrieval and loading is performed on a separate thread from the EDT.
 *
 * @author tag
 * @version $Id: LazilyLoadedTexture.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LazilyLoadedTexture extends AVListImpl implements WWTexture
{
    /** The original image source specified at construction. */
    protected Object imageSource;
    /** The mip-map flag specified at construction. */
    protected boolean useMipMaps;
    /** The current anisotropy flag. */
    protected boolean useAnisotropy = true;

    /** The texture width, if the width is known. Otherwise it's -1. */
    protected Integer width;
    /** The texture height, if the height is known. Otherwise it's -1. */
    protected Integer height;
    /** The texture's texture coordinates, as determined by JOGL when the texture is created. */
    protected TextureCoords texCoords;
    /**
     * The texture data created as the image source is read. It's removed - set to null - once the textures is fully
     * created. This intermediate texture data is necessary because the image source is read in a non-EDT thread. This
     * field is <code>volatile</code> in order to synchronize atomic access among threads. This field is not used if the
     * image source is <code>BufferedImage</code>.
     */
    protected volatile TextureData textureData; // if non-null, then must be converted to a Texture
    /** Indicates that texture initialization failed. This texture should not be used if true. */
    protected boolean textureInitializationFailed = false;
    /** Indicates whether the image read from the image source has mip-map data. */
    protected boolean hasMipmapData = false;
    /** Identifies the {@link gov.nasa.worldwind.cache.FileStore} of the supporting file cache for this model. */
    protected FileStore fileStore = WorldWind.getDataFileStore();
    /** Provides a semaphore to synchronize access to the texture file if duplicate request tasks are active. */
    protected final Object fileLock = new Object();

    /**
     * The object to notify when an image is eventually loaded in memory. The current layer at the time the image source
     * is requested is assigned to this field.
     */
    protected PropertyChangeListener listener;

    /**
     * Constructs a texture object for a specified image source. Requests that mip-maps be used.
     *
     * @param imageSource the source of the image, either a file path {@link String} or a {@link
     *                    java.awt.image.BufferedImage}.
     *
     * @throws IllegalArgumentException if the <code>imageSource</code> is null.
     */
    public LazilyLoadedTexture(Object imageSource)
    {
        this(imageSource, true);
    }

    /**
     * Constructs a texture object for a specified image source.
     *
     * @param imageSource the source of the image, either a file path {@link String} or a {@link
     *                    java.awt.image.BufferedImage}.
     * @param useMipMaps  Indicates whether to generate and use mip-maps for the image.
     *
     * @throws IllegalArgumentException if the <code>imageSource</code> is null.
     */
    public LazilyLoadedTexture(Object imageSource, boolean useMipMaps)
    {
        initialize(imageSource, useMipMaps, null);
    }

    /**
     * Initializes this object's fields during construction.
     *
     * @param imageSource the image source.
     * @param useMipMaps  the mip-map flag.
     * @param listener    the change listener.
     *
     * @throws IllegalArgumentException if the image source is null.
     */
    protected void initialize(Object imageSource, boolean useMipMaps, PropertyChangeListener listener)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.imageSource = imageSource;
        this.useMipMaps = useMipMaps;

        if (listener != null)
            this.addPropertyChangeListener(listener);
    }

    public Object getImageSource()
    {
        return this.imageSource;
    }

    /**
     * Indicates whether the image source is a <code>BufferedImage</code>
     *
     * @return true if the image source is a <code>BufferedImage</code>, otherwise false.
     */
    protected boolean isBufferedImageSource()
    {
        return this.getImageSource() instanceof BufferedImage;
    }

    /**
     * Indicates the texture's width, which is the same as the image source's width. The width is known only after the
     * image source has been retrieved from disk or network and read as <code>TextureData</code>. It's value is -1 until
     * then.
     *
     * @return the texture's width if the texture has been retrieved, otherwise -1.
     */
    public int getWidth()
    {
        return this.width != null ? this.width : -1;
    }

    /**
     * Indicates the texture's height, which is the same as the image source's height. The height is known only after
     * the image source has been retrieved from disk or network and read as <code>TextureData</code>. It's value is -1
     * until then.
     *
     * @return the texture's height if the texture has been retrieved, otherwise -1.
     */
    public int getHeight()
    {
        return this.height != null ? this.height : -1;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This method behaves identically to {@link #getWidth()}. The <code>DrawContext</code> argument is not used.
     *
     * @param dc this parameter is not used by this class.
     *
     * @return the texture's width if the texture has been retrieved, otherwise -1.
     */
    public int getWidth(DrawContext dc)
    {
        return this.getWidth();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This method behaves identically to {@link #getHeight()}. The <code>DrawContext</code> argument is not used.
     *
     * @param dc this parameter is not used by this class.
     *
     * @return the texture's height if the texture has been retrieved, otherwise -1.
     */
    public int getHeight(DrawContext dc)
    {
        return this.getHeight();
    }

    /**
     * Indicates whether the texture should use mip-maps. If they are not available in the source image they are
     * created.
     *
     * @return true if mip-maps are used, false if  not.
     */
    public boolean isUseMipMaps()
    {
        return this.useMipMaps;
    }

    public TextureCoords getTexCoords()
    {
        return this.texCoords;
    }

    public boolean isTextureCurrent(DrawContext dc)
    {
        return this.getTexture(dc) != null;
    }

    /**
     * Indicates whether texture anisotropy is applied to the texture when rendered.
     *
     * @return useAnisotropy true if anisotropy is to be applied, otherwise false.
     */
    public boolean isUseAnisotropy()
    {
        return this.useAnisotropy;
    }

    /**
     * Specifies whether texture anisotropy is applied to the texture when rendered.
     *
     * @param useAnisotropy true if anisotropy is to be applied, otherwise false.
     */
    public void setUseAnisotropy(boolean useAnisotropy)
    {
        this.useAnisotropy = useAnisotropy;
    }

    /**
     * Indicates whether an attempt was made to retrieve and read the texture but it failed. If this flag is true, this
     * texture should not be used.
     *
     * @return true if texture retrieval or creation failed, otherwise true, even if the image source has not yet been
     *         retrieved.
     */
    public boolean isTextureInitializationFailed()
    {
        return this.textureInitializationFailed;
    }

    /**
     * Returns the {@link Texture} associated with this instance.
     *
     * @param dc the current draw context.
     *
     * @return this instance's texture, or null if the texture does not currently exist.
     */
    protected Texture getTexture(DrawContext dc)
    {
        if (this.getImageSource() == null)
            return null;

        Texture texture = dc.getTextureCache().getTexture(this.getImageSource());

        if (this.width == null && texture != null)
        {
            this.width = texture.getWidth();
            this.height = texture.getHeight();
            this.texCoords = texture.getImageTexCoords();
        }

        return texture;
    }

    /**
     * Returns this texture's texture data if it has been retrieved but a <code>Texture</code> has not yet been created
     * for it.
     * <p/>
     * If this object's texture data field is non-null, a new texture is created from the texture data when the tile is
     * next bound or otherwise initialized. This object's texture data field is then set to null.
     *
     * @return the texture data, which may be null indicating that the image source has not been read or that a texture
     *         has been created.
     */
    protected TextureData getTextureData()
    {
        return this.textureData;
    }

    /**
     * Specifies texture data for the tile. If texture data is non-null, a new texture is created from the texture data
     * when the tile is next bound.
     * <p/>
     * When a texture is created from the texture data, the texture data field is set to null to indicate that the data
     * has been converted to a texture and its resources may be released.
     *
     * @param textureData the texture data, which may be null.
     */
    protected void setTextureData(TextureData textureData)
    {
        this.textureData = textureData;
        if (textureData != null && textureData.getMipmapData() != null)
            this.hasMipmapData = true;
    }

    /**
     * Binds this instance's {@link Texture} to the <code>GLContext</code> if the texture has been created, otherwise
     * initiates image source retrieval and texture creation in a separate thread.
     *
     * @param dc the current draw context.
     *
     * @return true if the texture was bound, otherwise false.
     */
    public boolean bind(DrawContext dc)
    {
        if (this.isTextureInitializationFailed())
            return false;

        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Texture texture = this.getTexture(dc);
        if (texture == null)
            texture = this.requestTexture(dc);

        if (texture != null)
        {
            texture.bind(dc.getGL());
            return true;
        }
        else
        {
            return false;
        }
    }

    public void applyInternalTransform(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Texture texture = this.getTexture(dc);
        if (texture == null)
            texture = this.requestTexture(dc);

        if (texture == null)
            return;

        if (texture.getMustFlipVertically())
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glMatrixMode(GL2.GL_TEXTURE);
            gl.glLoadIdentity();
            gl.glScaled(1, -1, 1);
            gl.glTranslated(0, -1, 0);
        }
    }

    /**
     * If this instance's image source is a <code>BufferedImage</code>, creates and returns the texture, otherwise
     * creates a task in a separate thread to retrieve it from its local or remote location.
     *
     * @param dc the current draw context.
     *
     * @return the new texture, or null if the texture is not yet available.
     */
    protected Texture requestTexture(DrawContext dc)
    {
        if (this.isBufferedImageSource())
            return this.makeBufferedImageTexture(dc);

        if (this.getTextureData() != null && this.getTexture(dc) == null)
            return this.makeTextureFromTextureData(dc);

        if (WorldWind.getTaskService().isFull())
            return null;

        Runnable task = this.createRequestTask();
        if (WorldWind.getTaskService().contains(task))
            return null;

        // Use either the current layer or the layer list as the listener to notify when the request completes. The
        // latter is used when the image source is requested during ordered rendering and the current layer is null.
        this.listener = dc.getCurrentLayer() != null ? dc.getCurrentLayer() : dc.getLayers();

        WorldWind.getTaskService().addTask(task);

        return null;
    }

    /**
     * Returns an object that implements the Runnable interface, and who's <code>run</code> method retrieves and loads
     * this texture's image source.
     *
     * @return a new request task that retrieves and loads this texture's image source.
     */
    protected Runnable createRequestTask()
    {
        return new RequestTask(this);
    }

    /**
     * Creates this instance's {@link Texture} if the image source is a <code>BufferedImage<code>.
     *
     * @param dc the current draw context.
     *
     * @return the newly created texture, or null if the texture was not created.
     *
     * @throws IllegalStateException if the image source is null or not a <code>BufferedImage</code>.
     */
    protected Texture makeBufferedImageTexture(DrawContext dc)
    {
        if (this.getImageSource() == null || !(this.getImageSource() instanceof BufferedImage))
        {
            String message = Logging.getMessage("generic.NotABufferedImage");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        try
        {
            TextureData td = AWTTextureIO.newTextureData(Configuration.getMaxCompatibleGLProfile(),
                (BufferedImage) this.getImageSource(), this.isUseMipMaps());
            if (td == null)
                return null;

            this.setTextureData(td);

            return this.makeTextureFromTextureData(dc);
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("generic.IOExceptionDuringTextureInitialization");
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            this.textureInitializationFailed = true;
            return null;
        }
    }

    /**
     * Creates a {@link Texture} from this instance's {@link TextureData} if the <code>TextureData</code> exists.
     *
     * @param dc the current draw context.
     *
     * @return the newly created texture, or null if this instance has no current <code>TextureData</code> or if texture
     *         creation failed.
     */
    protected Texture makeTextureFromTextureData(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (this.getTextureData() == null) // texture not in cache yet texture data is null, can't initialize
        {
            String msg = Logging.getMessage("nullValue.TextureDataIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        try
        {
            Texture texture = TextureIO.newTexture(this.getTextureData());
            if (texture == null)
            {
                this.textureInitializationFailed = true;
                return null;
            }

            this.width = texture.getWidth();
            this.height = texture.getHeight();
            this.texCoords = texture.getImageTexCoords();

            this.setTextureParameters(dc, texture);

            // Cache the texture and release the texture data.
            dc.getTextureCache().put(this.getImageSource(), texture);
            this.setTextureData(null);

            return texture;
        }
        catch (Exception e)
        {
            String name = this.isBufferedImageSource() ? "BufferedImage" : this.getImageSource().toString();
            String msg = Logging.getMessage("generic.ExceptionAttemptingToCreateTexture", name);
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            return null;
        }
    }

    /**
     * Sets a specified texture's OpenGL <code>Texture</code> parameters.
     *
     * @param dc      the current draw context.
     * @param texture the texture whose parameters to set.
     */
    protected void setTextureParameters(DrawContext dc, Texture texture)
    {
        // Enable the appropriate mip-mapping texture filters if the caller has specified that mip-mapping should be
        // enabled, and the texture itself supports mip-mapping.
        boolean useMipMapFilter = this.useMipMaps && (this.getTextureData().getMipmapData() != null
            || texture.isUsingAutoMipmapGeneration());

        GL gl = dc.getGL();
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
            useMipMapFilter ? GL.GL_LINEAR_MIPMAP_LINEAR : GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

        if (this.isUseAnisotropy() && useMipMapFilter)
        {
            double maxAnisotropy = dc.getGLRuntimeCapabilities().getMaxTextureAnisotropy();
            if (dc.getGLRuntimeCapabilities().isUseAnisotropicTextureFilter() && maxAnisotropy >= 2.0)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) maxAnisotropy);
            }
        }
    }

    protected void notifyTextureLoaded()
    {
        if (this.listener != null)
        {
            this.listener.propertyChange(new PropertyChangeEvent(this, AVKey.TEXTURE, null, this));
            this.listener = null; // forget the listener to avoid dangling references
        }
    }

    /** Attempts to find this texture's image file locally, and if that fails attempts to find it remotely. */
    protected static class RequestTask implements Runnable
    {
        /** The BasicWWTexture associated with this request. */
        protected final LazilyLoadedTexture wwTexture;

        /**
         * Construct a request task for a specified BasicWWTexture.
         *
         * @param wwTexture the texture object for which to construct the request task.
         */
        protected RequestTask(LazilyLoadedTexture wwTexture)
        {
            if (wwTexture == null)
            {
                String message = Logging.getMessage("nullValue.TextureIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.wwTexture = wwTexture;
        }

        public void run()
        {
            if (Thread.currentThread().isInterrupted())
                return; // the task was cancelled because it's a duplicate or for some other reason

            URL fileUrl = this.wwTexture.fileStore.requestFile(this.wwTexture.getImageSource().toString());

            if (fileUrl != null)
            {
                if (this.wwTexture.loadTextureData(fileUrl))
                {
                    this.wwTexture.notifyTextureLoaded();
                }
            }
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final RequestTask that = (RequestTask) o;

            return !(this.wwTexture != null ? !this.wwTexture.equals(that.wwTexture) : that.wwTexture != null);
        }

        public int hashCode()
        {
            return (this.wwTexture != null ? this.wwTexture.hashCode() : 0);
        }

        public String toString()
        {
            return this.wwTexture.getImageSource().toString();
        }
    }

    /**
     * Loads the image from disk into memory. If successful, texture data is created and available via {@link
     * #getTextureData()}.
     *
     * @param fileUrl the URL of the image file.
     *
     * @return true if the image was successfully loaded, otherwise false.
     */
    protected boolean loadTextureData(URL fileUrl)
    {
        TextureData td;

        synchronized (this.fileLock)
        {
            td = readImage(fileUrl);

            if (td != null)
                this.setTextureData(td);
        }

        return this.getTextureData() != null;
    }

    /**
     * Reads and returns a {@link TextureData} for an image from a specified file URL.
     *
     * @param fileUrl the URL of the image file to read.
     *
     * @return a <code>TextureData</code> instance for the image.
     */
    protected TextureData readImage(URL fileUrl)
    {
        try
        {
            return OGLUtil.newTextureData(Configuration.getMaxCompatibleGLProfile(), fileUrl, this.isUseMipMaps());
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile",
                this.getImageSource());
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            this.textureInitializationFailed = true;
            return null;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LazilyLoadedTexture that = (LazilyLoadedTexture) o;

        //noinspection RedundantIfStatement
        if (imageSource != null ? !imageSource.equals(that.imageSource) : that.imageSource != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return imageSource != null ? imageSource.hashCode() : 0;
    }
}
