/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import com.jogamp.opengl.util.packrect.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.Queue;

/**
 * Represents a texture composed of multiple independent images. The independent images are referred to as
 * <i>elements</i>, and are packed into non-overlapping sub-rectangles within the texture atlas. The following NVIDIA
 * document describes this technique: <a title="Improve Batching Using Texture Atlases" target="blank_"
 * href="ftp://download.nvidia.com/developer/NVTextureSuite/Atlas_Tools/Texture_Atlas_Whitepaper.pdf">Improve Batching
 * Using Texture Atlases</a>
 *
 * @author dcollins
 * @version $Id: TextureAtlas.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TextureAtlas
{
    /**
     * Implementation of the JOGL BackingStoreManager interface for texture atlas. This is used by the JOGL {@link
     * RectanglePacker}, and delegates calls from a JOGL rectangle packer to methods in this texture atlas.
     */
    protected class AtlasBackingStore implements BackingStoreManager
    {
        /**
         * {@inheritDoc}
         * <p/>
         * Calls {@link TextureAtlas#createBackingImage(int, int)} with the specified width and height.
         */
        public Object allocateBackingStore(int w, int h)
        {
            return createBackingImage(w, h);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Calls {@link TextureAtlas#disposeBackingImage()}.
         */
        public void deleteBackingStore(Object backingStore)
        {
            disposeBackingImage();
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Returns <code>true</code>. The texture atlas can always attempt to expand or compact.
         */
        public boolean canCompact()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Returns <code>false</code>, indicating that the rectangle packer should just expand. When configured to do
         * so, texture atlas evicts old elements in <code>additionFailed</code> if this texture atlas is full and the
         * addition would otherwise fail.
         */
        public boolean preExpand(Rect cause, int attemptNumber)
        {
            return false;
        }

        /**
         * {@inheritDoc}
         * <p/>
         * If this texture atlas is configured to evicts old elements, this attempts to remove the oldest one then
         * exits, allowing the caller to attempt the addition again. This throws a WWRuntimeException if this texture
         * atlas is not configured to evict old elements, or if there are no more elements to evict.
         *
         * @throws WWRuntimeException if this backing store cannot fit the rectangle in its layout.
         */
        public boolean additionFailed(Rect cause, int attemptNumber)
        {
            if (!isEvictOldElements() || !removeLeastRecentlyUsedEntry())
                throw new WWRuntimeException(Logging.getMessage("TextureAtlas.AtlasIsFull"));
            else
                return true;
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Calls {@link TextureAtlas#beginMoveEntries(java.awt.image.BufferedImage, java.awt.image.BufferedImage)},
         * casting the specified backing stores to BufferedImages.
         */
        public void beginMovement(Object oldBackingStore, Object newBackingStore)
        {
            beginMoveEntries((BufferedImage) oldBackingStore, (BufferedImage) newBackingStore);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Calls {@link TextureAtlas#moveEntry(java.awt.image.BufferedImage, com.jogamp.opengl.util.packrect.Rect,
         * java.awt.image.BufferedImage, com.jogamp.opengl.util.packrect.Rect)}, casting the specified backing stores to
         * BufferedImages.
         */
        public void move(Object oldBackingStore, Rect oldLocation, Object newBackingStore, Rect newLocation)
        {
            moveEntry((BufferedImage) oldBackingStore, oldLocation, (BufferedImage) newBackingStore, newLocation);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Calls {@link TextureAtlas#endMoveEntries(java.awt.image.BufferedImage, java.awt.image.BufferedImage)},
         * casting the specified backing stores to BufferedImages.
         */
        public void endMovement(Object oldBackingStore, Object newBackingStore)
        {
            endMoveEntries((BufferedImage) oldBackingStore, (BufferedImage) newBackingStore);
        }
    }

    /**
     * Represents an image element in a texture atlas. Each entry indicates the element's key, the image's rectangle in
     * the backing image, the actual image's offset within that rectangle, the image's actual width and height, and a
     * timestamp indicating the last time the element was used. Implements the {@link Comparable} interface by comparing
     * the lastUsed timestamp, ordered from least recently used to most recently used.
     */
    protected static class Entry implements Comparable<Entry>
    {
        /** Indicates the element's key. Initialized during construction. */
        public final Object key;
        /** Indicates the element's bounding rectangle within the texture atlas. Initialized during construction. */
        public Rect rect;
        /** Indicates the element's image X offset withing the bounding rectangle. Initialized during construction. */
        public int imageOffsetX;
        /** Indicates the element's image Y offset withing the bounding rectangle. Initialized during construction. */
        public int imageOffsetY;
        /**
         * Indicates the element's image width. May be smaller than the bounding rectangle's width. Initialized during
         * construction.
         */
        public int imageWidth;
        /**
         * Indicates the element's image height. May be smaller than the bounding rectangle's height. Initialized during
         * construction.
         */
        public int imageHeight;
        /** Indicates the last time this entry was used. */
        public long lastUsed;

        /**
         * Constructs a texture atlas entry corresponding with a texture atlas element with the specified key, bounding
         * rectangle, and image offsets within the bounding rectangle.
         *
         * @param key          the element's key.
         * @param rect         the element's bounding rectangle within the texture atlas.
         * @param imageOffsetX the element's image X offset withing the bounding rectangle.
         * @param imageOffsetY the element's image Y offset withing the bounding rectangle.
         * @param imageWidth   the element's image width. May be smaller than the bounding rectangle's width.
         * @param imageHeight  the element's image height. May be smaller than the bounding rectangle's height.
         */
        public Entry(Object key, Rect rect, int imageOffsetX, int imageOffsetY, int imageWidth, int imageHeight)
        {
            this.key = key;
            this.rect = rect;
            this.imageOffsetX = imageOffsetX;
            this.imageOffsetY = imageOffsetY;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }

        /**
         * Compares this texture atlas entry's last used timestamp to that of the specified entry. This returns -1 if
         * this entry's last used time is earlier than the specified entry's, 0 if the two entries have the same last
         * used time, and 1 if this entry's last used time is later than the specified entry's.
         *
         * @param that the texture atlas entry this entry is compared to.
         *
         * @return -1, 0, or 1 if this entry's last used time is earlier than, the same as, or later than the specified
         *         entry's last used time.
         *
         * @throws IllegalArgumentException if the specified entry is <code>null</code>.
         */
        public int compareTo(Entry that)
        {
            if (that == null)
            {
                String msg = Logging.getMessage("nullValue.EntryIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            return this.lastUsed < that.lastUsed ? -1 : this.lastUsed == that.lastUsed ? 0 : 1;
        }
    }

    /** The texture atlas' default setting for the useMipMaps property: <code>true</code>. */
    protected static final boolean DEFAULT_USE_MIP_MAPS = true;
    /** The texture atlas' default setting for the useAnisotropy property: <code>true</code>. */
    protected static final boolean DEFAULT_USE_ANISOTROPY = true;
    /** The texture atlas' default maximum vertical fragmentation: 0.7. */
    protected static final double DEFAULT_MAX_VERTICAL_FRAGMENTATION = 0.7;

    /**
     * Indicates this texture atlas' maximum total width, in pixels. This is specified during construction and is used
     * to determine when an image is too large to fit in this texture atlas.
     */
    protected int maxWidth;
    /**
     * Indicates this texture atlas' maximum total height, in pixels. This is specified during construction and is used
     * to determine when an image is too large to fit in this texture atlas.
     */
    protected int maxHeight;
    /**
     * Indicates whether this texture atlas generates mip-maps for each atlas element. <code>true</code> if this texture
     * atlas generates mip-maps, and <code>false</code> otherwise. Specified during construction.
     */
    protected boolean useMipMaps;
    /**
     * Indicates whether this texture atlas applies an anisotropic filter to each atlas element. <code>true</code> if
     * this texture atlas applies an anisotropic filter, and <code>false</code> otherwise. This has no effect if
     * useMipMaps is <code>false</code>. Specified during construction.
     */
    protected boolean useAnisotropy;
    /**
     * Indicates whether this texture atlas evicts old elements in order to make room for a new element when the atlas
     * is full. <code>true</code> if this atlas evicts elements to make room for new elements, and <code>false</code>
     * otherwise. Initially code <code>false</code>.
     */
    protected boolean evictOldElements;
    /**
     * Indicates the maximum amount of vertical fragmentation this texture atlas allows before compacting its elements.
     * Initialized to DEFAULT_MAX_VERTICAL_FRAGMENTATION.
     */
    protected double maxVerticalFragmentation = DEFAULT_MAX_VERTICAL_FRAGMENTATION;
    /**
     * The JOGL rectangle packer used by this texture atlas to determine how to pack the elements within this texture
     * atlas' backing image. Initialized during construction.
     */
    protected RectanglePacker rectPacker;
    /**
     * Maps element keys to their corresponding entry. This enables the texture atlas to access the information about
     * each element in constant time using its key. Initialized to a new HashMap.
     */
    protected Map<Object, Entry> entryMap = new HashMap<Object, Entry>();
    /**
     * Indicates the rectangle within this texture atlas' backing image that is currently out-of-sync with its
     * corresponding OpenGL texture. The dirty rectangle is <code>null</code> when this texture atlas' backing image is
     * synchronized with its OpenGL texture. Initially <code>null</code>.
     */
    protected Rectangle dirtyRect;
    /**
     * Indicates the color used to fill regions of this texture atlas that do not contain a sub-image element. Initially
     * transparent black (R=0, G=0, B=0, A=0).
     */
    protected Color clearColor = new Color(0, 0, 0, 0);
    /**
     * Temporary AWT graphics instance used to move image elements during a beginMovement/endMovement block. This
     * property is assigned in beginMovement, used in move, then cleared in endMovement. Initially <code>null</code>.
     */
    protected Graphics2D g;
    /**
     * Indicates the current key corresponding to this texture atlas' OpenGL texture in the GPU resource cache. This key
     * is assigned to a new instance whenever this texture atlas creates new backing image. Initialized to a new
     * Object.
     */
    protected Object textureKey = new Object();
    /**
     * Queue of texture keys corresponding to disposed backing images. These keys are disposed during the next call to
     * <code>bind</code>. While disposed backing textures would eventually be evicted by the GPU resource cache,
     * explicitly disposing them avoids polluting the GPU resource cache with orphaned textures that are used only by
     * this texture atlas.
     */
    protected Queue<Object> disposedTextureKeys = new ArrayDeque<Object>();

    /**
     * Constructs a texture atlas with the specified initial and maximum dimensions. All dimensions must be greater than
     * zero, and the maximum dimensions must be greater than or equal to the initial dimensions. The constructed texture
     * atlas generates mip-maps and applies an anisotropic filter to each element.
     *
     * @param initialWidth  the texture atlas' initial width, in pixels. Must be greater than zero.
     * @param initialHeight the texture atlas' initial height, in pixels. Must be greater than zero.
     * @param maxWidth      the texture atlas' maximum width, in pixels. Must be greater than or equal to initialWidth.
     * @param maxHeight     the texture atlas' maximum height, in pixels. Must be greater than or equal to
     *                      initialHeight.
     *
     * @throws IllegalArgumentException if any of initialWidth, initialHeight, maxWidth, or maxHeight are less than or
     *                                  equal to zero, if maxWidth is less than initialWidth, or if maxHeight is less
     *                                  than initialHeight.
     */
    public TextureAtlas(int initialWidth, int initialHeight, int maxWidth, int maxHeight)
    {
        this(initialWidth, initialHeight, maxWidth, maxHeight, DEFAULT_USE_MIP_MAPS, DEFAULT_USE_ANISOTROPY);
    }

    /**
     * Constructs a texture atlas with the specified initial and maximum dimensions. All dimensions must be greater than
     * zero, and the maximum dimensions must be greater than or equal to the initial dimensions. This constructor
     * enables specification of whether the texture atlas generates mip-maps and applies an anisotropic filter to each
     * element.
     *
     * @param initialWidth  the texture atlas' initial width, in pixels. Must be greater than zero.
     * @param initialHeight the texture atlas' initial height, in pixels. Must be greater than zero.
     * @param maxWidth      the texture atlas' maximum width, in pixels. Must be greater than or equal to initialWidth.
     * @param maxHeight     the texture atlas' maximum height, in pixels. Must be greater than or equal to
     *                      initialHeight.
     * @param useMipMaps    whether to generate mip-maps for each atlas element. <code>true</code> to generate mip-maps,
     *                      and <code>false</code> otherwise.
     * @param useAnisotropy whether to apply an anisotropic filter to each atlas element. <code>true</code> to apply an
     *                      anisotropic filter, and <code>false</code> otherwise. This has no effect if useMipMaps is
     *                      <code>false</code>.
     *
     * @throws IllegalArgumentException if any of initialWidth, initialHeight, maxWidth, or maxHeight are less than or
     *                                  equal to zero, if maxWidth is less than initialWidth, or if maxHeight is less
     *                                  than initialHeight.
     */
    public TextureAtlas(int initialWidth, int initialHeight, int maxWidth, int maxHeight, boolean useMipMaps,
        boolean useAnisotropy)
    {
        if (initialWidth < 1)
        {
            String msg = Logging.getMessage("TextureAtlas.InitialWidthInvalid", initialWidth);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (initialHeight < 1)
        {
            String msg = Logging.getMessage("TextureAtlas.InitialHeightInvalid", initialHeight);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (maxWidth < initialWidth)
        {
            String msg = Logging.getMessage("TextureAtlas.MaxWidthInvalid", maxWidth);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (maxHeight < initialHeight)
        {
            String msg = Logging.getMessage("TextureAtlas.MaxWidthInvalid", maxHeight);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Create a JOGL rectangle packer with the specified initial and maximum dimensions. The rectangle packer
        // determines the placement of each image within this texture atlas, and determines when to expand the atlas.
        this.rectPacker = this.createRectanglePacker(initialWidth, initialHeight);
        this.rectPacker.setMaxSize(maxWidth, maxHeight);

        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.useMipMaps = useMipMaps;
        this.useAnisotropy = useAnisotropy;
    }

    /**
     * Returns a new JOGL rectangle packer that is used by this texture atlas to determine how to pack the elements
     * within this texture atlas' backing image. By default, this returns a rectangle packer with an {@link
     * AtlasBackingStore} as the backing store manager. Called during construction.
     *
     * @param initialWidth  this texture atlas' initial width.
     * @param initialHeight this texture atlas' initial height.
     *
     * @return a new JOGL rectangle packer with the specified initial dimensions.
     */
    protected RectanglePacker createRectanglePacker(int initialWidth, int initialHeight)
    {
        return new RectanglePacker(new AtlasBackingStore(), initialWidth, initialHeight);
    }

    /**
     * Indicates this texture atlas' current width, in pixels.
     *
     * @return this texture atlas' current width.
     */
    public int getWidth()
    {
        return ((BufferedImage) this.rectPacker.getBackingStore()).getWidth();
    }

    /**
     * Indicates this texture atlas' current height, in pixels.
     *
     * @return this texture atlas' current height.
     */
    public int getHeight()
    {
        return ((BufferedImage) this.rectPacker.getBackingStore()).getHeight();
    }

    /**
     * Indicates this texture atlas' maximum width, in pixels.
     *
     * @return this texture atlas' maximum width.
     */
    public int getMaxWidth()
    {
        return this.maxWidth;
    }

    /**
     * Indicates this texture atlas' maximum height, in pixels.
     *
     * @return this texture atlas' maximum height.
     */
    public int getMaxHeight()
    {
        return this.maxHeight;
    }

    /**
     * Indicates whether this texture atlas generates mip-maps for each atlas element. Specified during construction.
     *
     * @return <code>true</code> if this texture atlas generates mip-maps, and <code>false</code> otherwise.
     */
    public boolean isUseMipMaps()
    {
        return this.useMipMaps;
    }

    /**
     * Indicates whether this texture atlas applies an anisotropic filter to each atlas element. This has no effect if
     * useMipMaps is <code>false</code>. Specified during construction.
     *
     * @return <code>true</code> if this texture atlas applies an anisotropic filter, and <code>false</code> otherwise.
     */
    public boolean isUseAnisotropy()
    {
        return this.useAnisotropy;
    }

    /**
     * Indicates whether this texture atlas evicts the oldest elements in order to make room for a new element when the
     * atlas is full.
     *
     * @return <code>true</code> if this atlas evicts old elements to make room for new elements, and <code>false</code>
     *         otherwise.
     *
     * @see #setEvictOldElements(boolean)
     */
    public boolean isEvictOldElements()
    {
        return this.evictOldElements;
    }

    /**
     * Specifies whether this texture atlas should evict the oldest elements in order to make room for a new element
     * when the atlas is full. When disabled, calling <code>add</code> with an element that does not fit in the current
     * atlas layout causes this texture atlas to throw an exception if the atlas cannot be expanded. When enabled, the
     * oldest elements are evicted until there is enough space to fit the element in the layout.
     *
     * @param evictOldElements <code>true</code> if this atlas should evict old elements to make room for new elements,
     *                         and <code>false</code> otherwise.
     */
    public void setEvictOldElements(boolean evictOldElements)
    {
        this.evictOldElements = evictOldElements;
    }

    /**
     * Returns the number of elements currently in this texture atlas.
     *
     * @return the number of elements in this texture atlas, or 0 if this atlas does not contain any elements.
     */
    public int getNumElements()
    {
        return this.entryMap.size();
    }

    /**
     * Indicates whether this texture atlas contains any elements.
     *
     * @return <code>true</code> if this texture atlas contains at least one element, and <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return this.entryMap.isEmpty();
    }

    /**
     * Adds a new element to this texture atlas with the specified key and image. The image's dimensions must be less
     * than or equal to this texture atlas' maximum dimensions. If this texture atlas is not configured to evict old
     * entries, this throws an exception if the image does not fit in the current atlas layout and the atlas cannot be
     * expanded.
     * <p/>
     * This adds a one pixel border around the specified image in this texture atlas' backing image by copying the
     * image's outer pixels into a border surrounding the original image. This border avoids sampling pixels from
     * neighboring atlas elements when an OpenGL box filter is applied to this image. This means that the atlas actually
     * requires space for an image with dimensions (width + 2, height + 2), where width and height are the image's
     * original dimensions.
     *
     * @param key   an object used to reference the image.
     * @param image the image to add.
     *
     * @throws IllegalArgumentException if either the key or image is <code>null</code>, or if the image dimensions are
     *                                  greater than this texture atlas' maximum dimensions.
     * @throws WWRuntimeException       if this texture atlas is too full to fit the image in its layout.
     */
    public void add(Object key, BufferedImage image)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (image == null)
        {
            String msg = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Add two to account for the 1 pixel border we add to the image.
        if (image.getWidth() + 2 > this.maxWidth || image.getHeight() + 2 > this.maxHeight)
        {
            String msg = Logging.getMessage("TextureAtlas.ImageTooLarge", key);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        try
        {
            this.doAdd(key, image);
        }
        catch (Exception e)
        {
            // doAdd throws a WWRuntimeException when the rectangle packer cannot fit the specified image into the
            // backing store.
            String msg = Logging.getMessage("TextureAtlas.AtlasIsFull", key);
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }
    }

    /**
     * Adds a new element to this texture atlas with the specified key and image.
     *
     * @param key   an object used to reference the image.
     * @param image the image to add.
     *
     * @throws WWRuntimeException if this texture atlas is too full to fit the image in its layout.
     */
    protected void doAdd(Object key, BufferedImage image)
    {
        // Remove any existing entry and add it to the list of unused entries before attempting to add one with the same
        // key. This ensures that the old entry is not orphaned in the rectangle packer's list of rectangles.
        Entry entry = this.entryMap.remove(key);
        if (entry != null)
        {
            this.doRemove(entry);
        }

        // Create a rectangle for the element with enough additional space to provide a 1 pixel border around the image.
        Rect rect = new Rect(0, 0, image.getWidth() + 2, image.getHeight() + 2, null);

        // Add an entry to the entryMap to provide constant time access to the entry's rectangle and attributes, and
        // mark the entry as used at the current time. We offset the image by 1 pixel within its rectangle to provide a
        // 1 pixel border around the image
        entry = new Entry(key, rect, 1, 1, image.getWidth(), image.getHeight());
        this.markUsed(entry);
        this.entryMap.put(key, entry);

        // Add the element's rectangle to the rectangle packer, expanding or rearranging the existing elements as needed
        // to incorporate the new element. This call sets the new rectangle's x and y coordinates to the rectangle's
        // location within the backing image.
        this.rectPacker.add(rect);

        // Copy the image's pixels into the rectangle packer's backing image at point determined by the rectangle
        // packer, replacing backing store pixels with those of the image. Note that the rectangle's x and y coordinates
        // are assigned upon adding it to the rectangle packer above. We draw a 1 pixel border around the image in order
        // to avoid sampling pixels from neighboring atlas elements when an OpenGL box filter is applied to this image.
        int imageX = rect.x() + entry.imageOffsetX;
        int imageY = rect.y() + entry.imageOffsetY;
        this.drawImage((BufferedImage) this.rectPacker.getBackingStore(), image, imageX, imageY, true);

        // Mark the rectangle associated with this entry as dirty so the OpenGL texture is synchronized with the backing
        // image upon the next call to bind.
        this.markDirty(rect.x(), rect.y(), rect.w(), rect.h());
    }

    /**
     * Removes the element with the specified key from this texture atlas, freeing the space it occupied to be used by
     * other elements. If this texture atlas does not contain an element with the specified key, this returns
     * <code>false</code> but otherwise does nothing.
     *
     * @param key an object used to reference the element to remove.
     *
     * @return <code>true</code> if this texture atlas contained an element with the specified key, and
     *         <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if the key is <code>null</code>.
     */
    public boolean remove(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Entry entry = this.entryMap.remove(key);
        if (entry != null)
        {
            this.doRemove(entry);
        }

        return entry != null;
    }

    /**
     * Removes the element corresponding to the specified entry from this texture atlas, freeing the space it occupied
     * to be used by other elements.
     *
     * @param entry the entry to remove.
     */
    protected void doRemove(Entry entry)
    {
        Rect rect = entry.rect;

        // Remove the element's rectangle from the JOGL rectangle packer. This frees space for the
        this.rectPacker.remove(rect);

        // Fill the element's rectangle in the backing image with the clear color, then mark the rectangle as dirty
        // so the OpenGL texture is synchronized with the backing image during the next call to bind.
        this.clearRect((BufferedImage) this.rectPacker.getBackingStore(), rect.x(), rect.y(), rect.w(), rect.h());
        this.markDirty(rect.x(), rect.y(), rect.w(), rect.h());

        // Compact the remaining entries if the vertical fragmentation ratio is larger than this texture atlas'
        // configured threshold. This avoids wasting texture space when many elements of different sizes are
        // subsequently added and removed.
        if (this.rectPacker.verticalFragmentationRatio() > this.maxVerticalFragmentation)
            this.rectPacker.compact();
    }

    /**
     * Indicates whether this texture atlas contains an element with the specified key.
     *
     * @param key the key which the element is referenced by.
     *
     * @return <code>true</code> if this texture atlas contains an element with the specified key, and
     *         <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if the key is <code>null</code>.
     */
    public boolean contains(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.entryMap.containsKey(key);
    }

    /**
     * Returns the image dimensions associated with an element in this texture atlas. This returns <code>null</code>
     * this texture atlas does not contain the element.
     *
     * @param key the key which the element is referenced by.
     *
     * @return the image dimensions corresponding to the specified element, or <code>null</code> if this texture atlas
     *         does not contain the element.
     */
    public Dimension getSize(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Entry entry = this.entryMap.get(key);
        if (entry == null)
            return null;

        // Mark that the entry has been used at the current time.
        this.markUsed(entry);

        return new Dimension(entry.imageWidth, entry.imageHeight);
    }

    /**
     * Returns the OpenGL texture coordinates associated with an element in this texture atlas. This returns
     * <code>null</code> if this texture atlas does not contain the element.
     * <p/>
     * The returned texture coordinates can change any time an element is added or removed from this texture atlas, and
     * therefore should not be cached unless the caller has explicit knowledge of when this texture atlas has changed.
     *
     * @param key the key which the element is referenced by.
     *
     * @return the OpenGL texture coordinates corresponding to the specified element, or <code>null</code> if this
     *         texture atlas does not contain the element.
     */
    public TextureCoords getTexCoords(Object key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Entry entry = this.entryMap.get(key);
        if (entry == null)
            return null;

        // Mark that the entry has been used at the current time.
        this.markUsed(entry);

        // Compute the lower-left and upper-right pixels corresponding to the element's image. We use the image width
        // and height instead of the rectangle's width and height because the image may be smaller than its rectangle.
        float x1 = entry.rect.x() + entry.imageOffsetX;
        float y1 = entry.rect.y() + entry.imageOffsetY;
        float x2 = x1 + entry.imageWidth;
        float y2 = y1 + entry.imageHeight;

        // Compute the lower-left and upper-right OpenGL texture coordinates corresponding to the element's image. This
        // step converts pixel locations in the range [0, width] or [0, height] to the range [0, 1].
        BufferedImage backingImage = (BufferedImage) this.rectPacker.getBackingStore();
        float tx1 = x1 / (float) backingImage.getWidth();
        float tx2 = x2 / (float) backingImage.getWidth();
        float ty1 = y1 / (float) backingImage.getHeight();
        float ty2 = y2 / (float) backingImage.getHeight();

        // Note that we flip the element's y coordinates. The backing image uses AWT coordinates which places its origin
        // in the upper-left corner, while the OpenGL texture expects the origin to be in the lower-left corner.
        return new TextureCoords(tx1, ty2, tx2, ty1);
    }

    /** Removes all elements from this texture atlas. The backing image retains its current dimensions after this call. */
    public void clear()
    {
        this.rectPacker.clear();
        this.entryMap.clear();

        // We've removed all entries from this texture atlas, so mark the entire backing image as dirty so the OpenGL
        // texture is synchronized with the backing image during the next call to bind.
        BufferedImage backingImage = (BufferedImage) this.rectPacker.getBackingStore();
        this.markDirty(0, 0, backingImage.getWidth(), backingImage.getHeight());
    }

    /**
     * Binds this texture atlas' OpenGL texture to the GLContext attached to the draw context. Before binding, this
     * updates the OpenGL texture as necessary to reflect changes in this texture atlas since the last call to
     * <code>bind</code>.
     *
     * @param dc the current draw context.
     *
     * @return <code>true</code> if the texture is bound, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if the draw context is <code>null</code>.
     */
    public boolean bind(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Remove textures corresponding to this texture atlas' disposed backing images from the draw context's GPU
        // resource cache before synchronizing the OpenGL texture. We do this before synchronizing to ensure that this
        // texture atlas does not cause unnecessary cache thrashing.
        this.disposeOldTextures(dc);

        // Synchronize the OpenGL texture with the backing image, creating OpenGL texture as necessary.
        Texture texture = this.syncTexture(dc);
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

    /**
     * Creates and returns a BufferedImage with the specified dimensions to use as this texture atlas' backing image.
     * The returned image must be at least as large as the specified dimensions, but may be larger. By default, this
     * always returns an image with power-of-two dimensions in order to maximize compatibility with different graphics
     * cards.
     *
     * @param width  the backing image's minimum width, in pixels.
     * @param height the backing image's minimum height, in pixels.
     *
     * @return a new BufferedImage to use as this texture atlas' backing image.
     */
    protected BufferedImage createBackingImage(int width, int height)
    {
        // Create an image with power-of-two dimensions in order to maximize compatibility with different graphics
        // cards. This eliminates the need for the individual images to have power-of-two dimensions.
        int potWidth = WWMath.powerOfTwoCeiling(width);
        int potHeight = WWMath.powerOfTwoCeiling(height);

        // Create a buffered image with the type 4BYTE_ABGR_PRE and fill it with the clear color. We use this image type
        // in order to avoid creating an extra copy when the JOGL TextureIO utility creates a texture from this image's
        // data.
        BufferedImage bi = new BufferedImage(potWidth, potHeight, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        this.clearRect(bi, 0, 0, potWidth, potHeight);

        return bi;
    }

    /** Disposes of this texture atlas' current backing image. */
    protected void disposeBackingImage()
    {
        // The rectangle packer is expanding or compacting the backing image, so we need to dispose of the current
        // backing image and its associated texture. We dispose of the texture by generating a new texture key and
        // adding the old key to the list of disposed texture keys. The current key may not be associated with any
        // texture, in which case it is ignored when processing the disposed texture keys. We do nothing to dispose of
        // the backing image, because this texture atlas does not retain any references to it.
        Object oldKey = this.textureKey;
        this.textureKey = new Object();
        this.disposedTextureKeys.add(oldKey);
    }

    /**
     * Fills the specified rectangle with the clear color in the backing image.
     *
     * @param backingImage the destination backing image to fill with the clear color.
     * @param x            the X coordinate of the rectangle's upper-left corner, in pixels.
     * @param y            the Y coordinates of the rectangle's upper-left corner, in pixels.
     * @param width        the rectangle's width, in pixels.
     * @param height       the rectangle's height, in pixels.
     */
    protected void clearRect(BufferedImage backingImage, int x, int y, int width, int height)
    {
        Graphics2D g = backingImage.createGraphics();
        try
        {
            g.setComposite(AlphaComposite.Src); // Replace destination pixels with the clear color (disables blending).
            g.setColor(this.clearColor);
            g.fillRect(x, y, width, height);
        }
        finally
        {
            g.dispose();
        }
    }

    /**
     * Draws the specified image in the backing image at the specified (x, y) location. If drawBorder is
     * <code>true</code>, this copies the image's outer pixels into 1 pixel border surrounding the original image. This
     * border avoids sampling pixels from neighboring atlas elements when an OpenGL box filter is applied to this
     * image.
     *
     * @param backingImage the destination backing image to draw into.
     * @param image        the source image to draw.
     * @param x            the X coordinate of the image's upper-left corner, in pixels.
     * @param y            the Y coordinates of the image's upper-left corner, in pixels.
     * @param drawBorder   <code>true</code> this copy the image's outer pixels into 1 pixel border surrounding the
     *                     original image, or <code>false</code> to draw only the image.
     */
    protected void drawImage(BufferedImage backingImage, BufferedImage image, int x, int y, boolean drawBorder)
    {
        int w = image.getWidth();
        int h = image.getHeight();

        Graphics2D g = backingImage.createGraphics();
        try
        {
            // Replace destination pixels with source pixels (disables blending).
            g.setComposite(AlphaComposite.Src);

            // Copy the entire image to (x, y).
            g.drawImage(image, x, y, null);

            if (drawBorder)
            {
                // Copy the image's top left corner to (x - 1, y - 1).
                g.drawImage(image,
                    x - 1, y - 1, x, y, // dstX1, dstY1, dstX2, dstY2
                    0, 0, 1, 1, // srcX1, srcY1, srcX2, srcY2
                    null);

                // Copy the image's top row to (x, y - 1).
                g.drawImage(image,
                    x, y - 1, x + w, y, // dstX1, dstY1, dstX2, dstY2
                    0, 0, w, 1, // srcX1, srcY1, srcX2, srcY2
                    null);

                // Copy the image's top right corner to (x + w, y - 1).
                g.drawImage(image,
                    x + w, y - 1, x + w + 1, y, // dstX1, dstY1, dstX2, dstY2
                    w - 1, 0, w, 1, // srcX1, srcY1, srcX2, srcY2
                    null);

                // Copy the image's right column to (x + w, y).
                g.drawImage(image,
                    x + w, y, x + w + 1, y + h, // dstX1, dstY1, dstX2, dstY2
                    w - 1, 0, w, h, // srcX1, srcY1, srcX2, srcY2
                    null);

                // Copy the image's bottom right corner to (x + w, y + h).
                g.drawImage(image,
                    x + w, y + h, x + w + 1, y + h + 1, // dstX1, dstY1, dstX2, dstY2
                    w - 1, h - 1, w, h, // srcX1, srcY1, srcX2, srcY2
                    null);

                // Copy the image's bottom row to (x, y + h).
                g.drawImage(image,
                    x, y + h, x + w, y + h + 1, // dstX1, dstY1, dstX2, dstY2
                    0, h - 1, w, h, // srcX1, srcY1, srcX2, srcY2
                    null);

                // Copy the image's bottom left corner to (x - 1, y + h).
                g.drawImage(image,
                    x - 1, y + h, x, y + h + 1, // dstX1, dstY1, dstX2, dstY2
                    0, h - 1, 1, h, // srcX1, srcY1, srcX2, srcY2
                    null);

                // Copy the image's left column to (x - 1, y).
                g.drawImage(image,
                    x - 1, y, x, y + h, // dstX1, dstY1, dstX2, dstY2
                    0, 0, 1, h, // srcX1, srcY1, srcX2, srcY2
                    null);
            }
        }
        finally
        {
            g.dispose();
        }
    }

    /**
     * Called when the atlas is performing a full re-layout of its elements, just before the layout begins. If this
     * texture atlas' dimensions are changing, the specified backing images refer to separate instances with different
     * dimensions. If this texture atlas is performing a re-layout in place, the specified backing images refer to the
     * same instance.
     *
     * @param oldBackingImage the backing image corresponding to the previous layout.
     * @param newBackingImage the backing image corresponding to the new layout.
     */
    @SuppressWarnings({"UnusedParameters"})
    protected void beginMoveEntries(BufferedImage oldBackingImage, BufferedImage newBackingImage)
    {
        if (this.g != null) // This should never happen, but we check anyway.
            this.g.dispose();

        this.g = newBackingImage.createGraphics();
        this.g.setComposite(AlphaComposite.Src); // Replace destination pixels with source pixels.
    }

    /**
     * Called when the atlas is performing a full re-layout of its elements, just after the layout ends. If this texture
     * atlas' dimensions have changed, the specified backing images refer to separate instances with different
     * dimensions. If this texture atlas has performed a re-layout in place, the specified backing images refer to the
     * same instance.
     *
     * @param oldBackingImage the backing image corresponding to the previous layout.
     * @param newBackingImage the backing image corresponding to the new layout.
     */
    @SuppressWarnings({"UnusedParameters"})
    protected void endMoveEntries(BufferedImage oldBackingImage, BufferedImage newBackingImage)
    {
        if (this.g != null) // This should never happen, but we check anyway.
        {
            this.g.dispose();
            this.g = null;
        }

        // We've removed all entries from this texture atlas, so mark the entire backing image as dirty so it's
        // synchronized with the OpenGL texture during the next call to bind.
        this.markDirty(0, 0, newBackingImage.getWidth(), newBackingImage.getHeight());
    }

    /**
     * Called for each atlas element when the atlas is performing a full re-layout of its elements. If this texture
     * atlas' dimensions are changing, the specified backing images refer to separate instances with different
     * dimensions. If this texture atlas is performing a re-layout in place, the specified backing images refer to the
     * same instance. In either case, the specified rectangles correspond to the element's location in the old backing
     * image and new backing image.
     *
     * @param oldBackingImage the backing image corresponding to the previous layout.
     * @param oldRect         the element's location in oldBackingImage.
     * @param newBackingImage the backing image corresponding to the new layout.
     * @param newRect         the element's location in newBackingImage.
     */
    protected void moveEntry(BufferedImage oldBackingImage, Rect oldRect, BufferedImage newBackingImage, Rect newRect)
    {
        // Note that there is no need to update the rectangle instance associated with the entry for this rectangle. The
        // JOGL rectangle packer automatically takes care of updating the rectangle for us.

        this.g.setComposite(AlphaComposite.Src); // Replace destination pixels with the clear color (disables blending).

        if (oldBackingImage == newBackingImage)
        {
            // The backing image has not changed. Move the entry's rectangle from its old location to its new location.
            this.g.copyArea(oldRect.x(), oldRect.y(), oldRect.w(), oldRect.h(), // x, y, width, height
                newRect.x() - oldRect.x(), newRect.y() - oldRect.y()); // dx, dy
        }
        else
        {
            // The backing image is changing. Copy the entry from its location in the old backing images to its location
            // in the new backing image.
            this.g.drawImage(oldBackingImage,
                // dstX1, dstY1, dstX2, dstY2
                newRect.x(), newRect.y(), newRect.x() + newRect.w(), newRect.y() + newRect.h(),
                // srcX1, srcY1, srcX2, srcY2
                oldRect.x(), oldRect.y(), oldRect.x() + oldRect.w(), oldRect.y() + oldRect.h(),
                null);
        }
    }

    /**
     * Marks the specified entry as used by setting its last used time to the current time in nanoseconds.
     *
     * @param entry the entry who's last used time is marked.
     */
    protected void markUsed(Entry entry)
    {
        entry.lastUsed = System.nanoTime();
    }

    /**
     * Removes the oldest entry from this texture atlas. This does nothing if this texture atlas is empty.
     *
     * @return <code>true</code> if this removed an entry, and <code>false</code> if there are no entries to remove.
     */
    protected boolean removeLeastRecentlyUsedEntry()
    {
        if (this.entryMap.isEmpty())
            return false;

        Entry[] timeOrderedEntries = new Entry[this.entryMap.size()];
        Arrays.sort(this.entryMap.values().toArray(timeOrderedEntries));

        Entry entryToRemove = timeOrderedEntries[0];
        this.entryMap.remove(entryToRemove.key);
        this.doRemove(entryToRemove);

        return true;
    }

    /**
     * Returns the region of this texture atlas' backing image that is not currently synchronized with the OpenGL
     * texture.
     *
     * @return the region of this texture atlas that must be synchronized.
     */
    protected Rectangle getDirtyRect()
    {
        return this.dirtyRect;
    }

    /**
     * Marks a region of this texture atlas' backing image as needing to be synchronized with the OpenGL texture. If
     * there is already a dirty region, the final dirty region is the union of the two.
     *
     * @param x      the X coordinate of the region's upper-left corner, in pixels.
     * @param y      the Y coordinate of the region's upper-left corner, in pixels.
     * @param width  the region's width, in pixels.
     * @param height the region's height, in pixels.
     */
    protected void markDirty(int x, int y, int width, int height)
    {
        Rectangle rect = new Rectangle(x, y, width, height);

        if (this.dirtyRect == null)
            this.dirtyRect = rect;
        else
            this.dirtyRect.add(rect);
    }

    /**
     * Removes any regions in this texture atlas' backing image previously marked as needing to be synchronized with the
     * OpenGL texture.
     */
    protected void clearDirtyRect()
    {
        this.dirtyRect = null;
    }

    /**
     * Indicates the OpenGL {@link Texture} associated with this texture atlas.
     *
     * @param dc the current draw context.
     *
     * @return this instance's OpenGL texture, or <code>null</code> if the texture does not currently exist.
     */
    protected Texture getTexture(DrawContext dc)
    {
        return dc.getTextureCache().getTexture(this.textureKey);
    }

    /**
     * Specifies the OpenGL {@link Texture} associated with this texture atlas.
     *
     * @param dc      the current draw context.
     * @param texture this instance's OpenGL texture, or <code>null</code> to specify that this texture atlas has no
     *                texture.
     */
    protected void setTexture(DrawContext dc, Texture texture)
    {
        dc.getTextureCache().put(this.textureKey, texture);
    }

    /**
     * Removes textures corresponding to this texture atlas' disposed backing images from the draw context's GPU
     * resource cache. While disposed backing textures would eventually be evicted by the GPU resource cache, explicitly
     * removing them avoids polluting the GPU resource cache with orphaned textures that are used only by this texture
     * atlas.
     *
     * @param dc the draw context containing the GPU resource cache to remove textures from.
     */
    protected void disposeOldTextures(DrawContext dc)
    {
        // Process each key in the disposedTextureKeys queue. Since TextureAtlas keys are unique to each instance, the
        // texture keys are not shared with any other object, and therefore are orphaned once they're unused. We
        // explicitly remove them from the texture cache to ensure that this texture atlas uses a minimal amount of
        // texture memory.
        Object key;
        while ((key = this.disposedTextureKeys.poll()) != null)
        {
            // The key may never have been be associated with a texture if this texture atlas was expanded or contracted
            // more than once between calls to bind. In this case we just ignore the disposed key and continue.
            if (dc.getTextureCache().contains(key))
                dc.getTextureCache().remove(key);
        }
    }

    /**
     * Synchronizes this texture atlas's backing image with its OpenGL texture, creating an OpenGL texture as necessary.
     * This attempts to minimize transfer between Java and OpenGL by loading the smallest possible portion of the
     * backing image into the OpenGL texture.
     *
     * @param dc the current draw context.
     *
     * @return this texture atlas' OpenGL texture.
     */
    protected Texture syncTexture(DrawContext dc)
    {
        Texture texture = this.getTexture(dc);

        if (texture == null)
        {
            // This texture atlas' OpenGL texture does not exist on the specified draw context. Load the entire backing
            // image into a new texture and use that as this texture atlas' OpenGL texture.
            texture = this.makeTextureWithBackingImage(dc);
        }
        else if (this.getDirtyRect() != null)
        {
            // A region of this texture atlas' OpenGL texture is out-of-sync; load only the necessary portion of the
            // backing image into the texture.
            texture = this.updateTextureWithSubImage(dc, this.getDirtyRect());
        }

        // Clear the dirty rectangle to indicate that this texture atlas' backing image and texture are synchronized.
        this.clearDirtyRect();

        return texture;
    }

    /**
     * Creates an OpenGL texture by loading this texture atlas's backing image into a new texture with the same
     * dimensions.
     *
     * @param dc the current draw context.
     *
     * @return a new OpenGL texture containing the data from this texture atlas' backing image.
     */
    protected Texture makeTextureWithBackingImage(DrawContext dc)
    {
        BufferedImage backingImage = (BufferedImage) this.rectPacker.getBackingStore();
        Texture texture = AWTTextureIO.newTexture(dc.getGL().getGLProfile(), backingImage, this.isUseMipMaps());

        this.setTexture(dc, texture);
        this.setTextureParameters(dc);

        return texture;
    }

    /**
     * Loads a sub-region of this texture atlas' backing image into its OpenGL texture. This does nothing and returns
     * code <code>null</code> if this texture atlas' does not have an OpenGL texture.
     *
     * @param dc   the current draw context.
     * @param rect the rectangle to load.
     *
     * @return this texture atlas' OpenGL texture, or <code>null</code> if this texture atlas' does not have an OpenGL
     *         texture.
     */
    protected Texture updateTextureWithSubImage(DrawContext dc, Rectangle rect)
    {
        Texture texture = this.getTexture(dc);
        if (texture == null) // This should never happen, but we check anyway.
        {
            String msg = Logging.getMessage("nullValue.TextureIsNull");
            Logging.logger().warning(msg);
            return null;
        }

        if (!this.isUseMipMaps() || texture.isUsingAutoMipmapGeneration())
        {
            // If we're either not using mip-maps or we have automatic mip-map generation, then load the sub-image
            // corresponding to the specified rectangle into the OpenGL texture. Note that the x and y coordinates of
            // the dirty region do not need to be translated because the image and texture share the same coordinate
            // system.
            BufferedImage backingImage = (BufferedImage) this.rectPacker.getBackingStore();
            BufferedImage subImage = backingImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
            GL gl = dc.getGL();
            TextureData subTextureData = AWTTextureIO.newTextureData(gl.getGLProfile(), subImage, false);
            texture.updateSubImage(gl, subTextureData, 0, rect.x, rect.y);
        }
        else
        {
            // If we're using mip-maps but do not have automatic mip-map generation, we must load the entire image into
            // the texture in order to force JOGL to recompute the mip-map data for all levels in Java. We must also
            // respecify the texture parameters, because Texture.updateImage overwrites the texture parameters with
            // default values.
            BufferedImage backingImage = (BufferedImage) this.rectPacker.getBackingStore();
            GL gl = dc.getGL();
            texture.updateImage(gl, AWTTextureIO.newTextureData(gl.getGLProfile(), backingImage, this.isUseMipMaps()));
            this.setTextureParameters(dc);
        }

        return texture;
    }

    /**
     * Specifies the OpenGL texture parameters associated with this texture atlas' OpenGL texture. Called after updating
     * this texture atlas' OpenGL texture, when the OpenGL texture is bound to the draw context's
     * <code>GLContext</code>.
     *
     * @param dc the current draw context.
     */
    protected void setTextureParameters(DrawContext dc)
    {
        GL gl = dc.getGL();

        // The JOGL Texture class specifies appropriate default values for the following OpenGL texture parameters:
        // - GL_TEXTURE_MIN_FILTER
        // - GL_TEXTURE_MAG_FILTER
        // - GL_TEXTURE_WRAP_S
        // - GL_TEXTURE_WRAP_T

        if (this.isUseMipMaps() && this.isUseAnisotropy())
        {
            double maxAnisotropy = dc.getGLRuntimeCapabilities().getMaxTextureAnisotropy();
            if (dc.getGLRuntimeCapabilities().isUseAnisotropicTextureFilter() && maxAnisotropy >= 2.0)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) maxAnisotropy);
            }
        }
    }
}
