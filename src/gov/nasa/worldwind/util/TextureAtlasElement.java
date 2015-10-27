/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.net.URL;

/**
 * Represents a texture defined by a sub-image within a {@link TextureAtlas}.
 * <p/>
 * TextureAtlasElement performs lazy retrieval and loading of its image source into its texture atlas. This loads the
 * image source and adds it to the atlas only when the {@link #load(gov.nasa.worldwind.render.DrawContext)} method is
 * called. If the image source is a {@link BufferedImage} it is added to the atlas immediately when <code>load</code> is
 * called. If the image source is a local file or a remote stream (URL), retrieval and loading is performed on a
 * separate thread from the EDT.
 *
 * @author dcollins
 * @version $Id: TextureAtlasElement.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TextureAtlasElement implements Disposable
{
    /** Indicates the texture atlas this element belongs to. Specified during construction. */
    protected TextureAtlas atlas;
    /** Indicates the original image source associated with this element. Specified during construction. */
    protected Object imageSource;
    /**
     * The BufferedImage created as the image source is read. This intermediate field is necessary because the image
     * source is read on a non-EDT thread, but changes to the texture atlas must be performed on the EDT. This is set to
     * <code>null</code> once the image is loaded into the texture atlas. This field is <code>volatile</code> in order
     * to synchronize atomic access among threads. This field is not used if the image source is
     * <code>BufferedImage</code>.
     */
    protected volatile BufferedImage image;
    /**
     * Indicates that image initialization failed. This element should not be used if <code>true</code>. Initially
     * <code>false</code>.
     */
    protected boolean imageInitializationFailed;
    /**
     * The object to notify when the image is eventually loaded in memory. This is either the current layer or the layer
     * list at the time the image source is requested. The latter is used when the image source is requested during
     * ordered rendering mode, in which case the current layer is <code>null</code>. This set to <code>null</code> once
     * the image is loaded into the texture atlas.
     */
    protected PropertyChangeListener listener;

    /**
     * Creates a new texture atlas element with the specified atlas and image source.
     *
     * @param atlas       the texture atlas this element belongs to.
     * @param imageSource a general image source. The source type may be one of the following: <ul> <li>a {@link
     *                    URL}</li> <li>an {@link java.io.InputStream}</li> <li>a {@link java.io.File}</li> <li>a {@link
     *                    String} containing a valid URL description or a file or resource name available on the
     *                    classpath.</li> </ul>
     *
     * @throws IllegalArgumentException if either the atlas or the image source is <code>null</code>.
     */
    public TextureAtlasElement(TextureAtlas atlas, Object imageSource)
    {
        if (atlas == null)
        {
            String msg = Logging.getMessage("nullValue.AtlasIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(imageSource))
        {
            String msg = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.atlas = atlas;
        this.imageSource = imageSource;
    }

    /**
     * Indicates the texture atlas this element belongs to.
     *
     * @return this element's texture atlas.
     */
    public TextureAtlas getTextureAtlas()
    {
        return this.atlas;
    }

    /**
     * Indicates the original image source associated with this element.
     *
     * @return this element's image source.
     */
    public Object getImageSource()
    {
        return this.imageSource;
    }

    /**
     * Indicates whether this element's image source is a BufferedImage.
     *
     * @return <code>true</code> if this element's image source is a BufferedImage, and <code>false</code> otherwise.
     */
    protected boolean isBufferedImageSource()
    {
        return this.getImageSource() instanceof BufferedImage;
    }

    /**
     * Indicates the image created as the image source is read.
     *
     * @return this element's image.
     *
     * @see #setImage(java.awt.image.BufferedImage)
     */
    protected BufferedImage getImage()
    {
        return this.image;
    }

    /**
     * Specifies the image created as the image source is read. This intermediate field is necessary because the image
     * source is read on a non-EDT thread, but changes to the texture atlas must be performed on the EDT. This field is
     * <code>volatile</code> in order to synchronize atomic access among threads.
     *
     * @param image this element's image.
     */
    protected void setImage(BufferedImage image)
    {
        this.image = image;
    }

    /**
     * Returns the image dimensions associated with this texture atlas element. Always call <code>load</code> before
     * calling this method to ensure that the element is loaded into its texture atlas.
     *
     * @return the image dimensions associated with this texture atlas element, or <code>null</code> if this texture
     *         atlas element has not yet loaded or has failed to load.
     *
     * @see #load(gov.nasa.worldwind.render.DrawContext)
     */
    public Dimension getSize()
    {
        return this.getTextureAtlas().getSize(this.getImageSource());
    }

    /**
     * Returns the OpenGL texture coordinates associated this texture atlas element. Always call <code>load</code>
     * before calling this method to ensure that the element is loaded into its texture atlas.
     * <p/>
     * The returned texture coordinates can change any time an element is added or removed from this element's texture
     * atlas, and therefore should not be cached unless the caller has explicit knowledge of when this element's texture
     * atlas has changed.
     *
     * @return the OpenGL texture coordinates corresponding this texture atlas element, or <code>null</code> if this
     *         texture atlas element has not yet loaded or has failed to load.
     *
     * @see #load(gov.nasa.worldwind.render.DrawContext)
     */
    public TextureCoords getTexCoords()
    {
        return this.getTextureAtlas().getTexCoords(this.getImageSource());
    }

    /**
     * Indicates whether this element's image failed to load. This element should not be used if <code>true</code>.
     *
     * @return <code>true</code> if this element's image failed to load, and <code>false</code> otherwise.
     */
    public boolean isImageInitializationFailed()
    {
        return this.imageInitializationFailed;
    }

    /**
     * Loads this element's image and adds it to the texture atlas. If the image is not yet loaded this initiates image
     * source retrieval in a separate thread. This does nothing if the texture atlas already contains this element, or
     * if this element's image failed to load in an earlier attempt.
     *
     * @param dc the current draw context. Used to generate a repaint event when the image source retrieval completes.
     *
     * @return <code>true</code> if this element's image is successfully loaded and added to the texture atlas,
     *         otherwise <code>false</code>.
     */
    public boolean load(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.isImageInitializationFailed())
            return false;

        // The atlas already contains an entry for this element then just return true.
        if (this.getTextureAtlas().contains(this.getImageSource()))
            return true;

        // The atlas does not contain an entry for this element. Issue a request for this element's image if it does not
        // exist, or load it into the atlas if it does. In this case we return true if this element was successfully
        // loaded into the atlas, and false otherwise.
        return this.requestImage(dc);
    }

    /**
     * Removes this element's image from its texture atlas and disposes of this element's image resource. This does
     * nothing if this element's image has not yet been loaded.
     */
    public void dispose()
    {
        if (this.getTextureAtlas().contains(this.getImageSource()))
            this.getTextureAtlas().remove(this.getImageSource());

        this.setImage(null);
    }

    /**
     * Indicates whether another texture atlas element is equivalent to this one. This tests equality using the image
     * source of each element.
     *
     * @param o the object to test.
     *
     * @return <code>true</code> if the specified object is a TextureAtlasElement and its image source is equivalent to
     *         this element's image source, otherwise <code>false</code>.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        TextureAtlasElement that = (TextureAtlasElement) o;
        return this.imageSource != null ? this.imageSource.equals(that.imageSource) : that.imageSource == null;
    }

    /** Returns the hash code for this texture atlas element's image source. */
    @Override
    public int hashCode()
    {
        return this.imageSource != null ? this.imageSource.hashCode() : 0;
    }

    /** Returns the string representation of this texture atlas element's image source. */
    @Override
    public String toString()
    {
        return this.imageSource != null ? this.imageSource.toString() : null;
    }

    /**
     * Requests that this element's image source be loaded into its texture atlas. If the image source is a
     * BufferedImage, this immediately loads it into the texture atlas and returns <code>true</code>. Otherwise, this
     * initiates the retrieval of this element's image source in a separate thread and returns <code>false</code>. Once
     * the image source is retrieved, a subsequent invocation of this method loads it into the texture atlas and returns
     * <code>true</code>.
     *
     * @param dc the current draw context. Used to generate a repaint event when the image source retrieval completes.
     *
     * @return <code>true</code> if this element's image is loaded into the texture atlas, and <code>false</code>
     *         otherwise.
     */
    protected boolean requestImage(DrawContext dc)
    {
        // If the image source is already a buffered image, assign it to this element's image and let the subsequent
        // logic in this method take care of adding it to the atlas.
        if (this.isBufferedImageSource())
            this.setImage((BufferedImage) this.getImageSource());

        if (this.getImage() != null && !this.getTextureAtlas().contains(this.getImageSource()))
            return this.addAtlasImage();

        if (WorldWind.getTaskService().isFull())
            return false;

        Runnable task = this.createRequestTask();
        if (WorldWind.getTaskService().contains(task))
            return false;

        // Use either the current layer or the layer list as the listener to notify when the request completes. The
        // latter is used when the image source is requested during ordered rendering mode, and the current layer is
        // null.
        this.listener = dc.getCurrentLayer() != null ? dc.getCurrentLayer() : dc.getLayers();

        WorldWind.getTaskService().addTask(task);

        return false;
    }

    /**
     * Adds this element's image source into its texture atlas. This throws an exception if this element's image source
     * is not loaded and stored in the <code>image</code> property.
     *
     * @return <code>true</code> if this element's image is successfully added to the texture atlas, and
     *         <code>false</code> otherwise.
     */
    protected boolean addAtlasImage()
    {
        if (this.getImage() == null)
        {
            String msg = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        try
        {
            // Place this element's image in the atlas, then release our reference to the image.
            this.getTextureAtlas().add(this.getImageSource(), this.getImage());
            this.setImage(null);
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("TextureAtlas.ExceptionAddingImage", this.getImageSource().toString());
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            this.imageInitializationFailed = true;
            return false;
        }

        return true;
    }

    /**
     * Returns an object that implements the Runnable interface, and who's <code>run</code> method retrieves and loads
     * this element's image source.
     *
     * @return a new request task that retrieves and loads this element's image source.
     */
    protected Runnable createRequestTask()
    {
        return new RequestTask(this);
    }

    /**
     * Loads this element's image source into its <code>image</code> property. If the image source is a remote resource,
     * this initiates a request for it and returns <code>null</code>.
     *
     * @return <code>true</code> if the image source has been loaded successfully, and <code>false</code> otherwise.
     */
    protected boolean loadImage()
    {
        URL fileUrl = WorldWind.getDataFileStore().requestFile(this.getImageSource().toString());
        if (fileUrl != null)
        {
            BufferedImage image = this.readImage(fileUrl);
            if (image != null)
                this.setImage(image);
        }

        return this.getImage() != null;
    }

    /**
     * Reads and returns the specified image URL as a BufferedImage.
     *
     * @param fileUrl the image URL to read.
     *
     * @return the image URL as a BufferedImage, or <code>null</code> if the image could not be read.
     */
    protected BufferedImage readImage(URL fileUrl)
    {
        try
        {
            return ImageIO.read(fileUrl);
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("generic.ExceptionAttemptingToReadImageFile",
                this.getImageSource().toString());
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            this.imageInitializationFailed = true;
            return null;
        }
    }

    /**
     * Notifies this texture atlas element's listener that image loading has completed. This does nothing if this
     * texture atlas element has no listener.
     */
    protected void notifyImageLoaded()
    {
        if (this.listener != null)
        {
            this.listener.propertyChange(new PropertyChangeEvent(this, AVKey.IMAGE, null, this));
            this.listener = null; // Forget the listener to avoid dangling references.
        }
    }

    /**
     * RequestTask is an implementation of the Runnable interface who's <code>run</code> method retrieves and loads this
     * element's image source.
     */
    protected static class RequestTask implements Runnable
    {
        /** The texture atlas element associated with this request task. Specified during construction. */
        protected TextureAtlasElement elem;

        /**
         * Constructs a new request task with the specified texture atlas element, but otherwise does nothing. Calling
         * the new request tasks <code>run</code> method causes this to retrieve and load the specified element's image
         * source.
         *
         * @param elem the texture atlas element who's image source is retrieved and loaded.
         *
         * @throws IllegalArgumentException if the element is <code>null</code>.
         */
        protected RequestTask(TextureAtlasElement elem)
        {
            if (elem == null)
            {
                String message = Logging.getMessage("nullValue.ElementIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.elem = elem;
        }

        /**
         * Retrieves and loads the image source from this request task's texture atlas element, and notifies the element
         * when the load completes. This does nothing if the current thread has been interrupted.
         */
        public void run()
        {
            if (Thread.currentThread().isInterrupted())
                return; // The task was cancelled because it's a duplicate or for some other reason.

            if (this.elem.loadImage())
                this.elem.notifyImageLoaded();
        }

        /**
         * Indicates whether another request task is equivalent to this one. This tests equality using the texture atlas
         * element source of each task.
         *
         * @param o the object to test.
         *
         * @return <code>true</code> if the specified object is a RequestTask, and its texture atlas element is
         *         equivalent to this task's texture atlas element.
         */
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            RequestTask that = (RequestTask) o;
            return this.elem.equals(that.elem);
        }

        /** Returns the hash code for this request task's texture atlas element. */
        @Override
        public int hashCode()
        {
            return this.elem.hashCode();
        }

        /** Returns the string representation of this request task's texture atlas element. */
        @Override
        public String toString()
        {
            return this.elem.toString();
        }
    }
}
