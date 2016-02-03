/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe2D;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.GL;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Builds a list of {@link gov.nasa.worldwind.render.SurfaceTile} instances who's content is defined by a specified set
 * of {@link gov.nasa.worldwind.render.SurfaceRenderable} instances. It's typically not necessary to use
 * SurfaceObjectTileBuilder directly. World Wind's default scene controller automatically batches instances of
 * SurfaceRenderable in a single SurfaceObjectTileBuilder. Applications that need to draw basic surface shapes should
 * use or extend {@link gov.nasa.worldwind.render.SurfaceShape} instead of using SurfaceObjectTileBuilder directly.
 * <p/>
 * Surface tiles are built by calling {@link #buildTiles(DrawContext, Iterable)} with an iterable of surface
 * renderables. This assembles a set of surface tiles that meet the resolution requirements for the specified draw
 * context, then draws the surface renderables into those offscreen surface tiles by calling render on each instance.
 * This process may temporarily use the framebuffer to perform offscreen rendering, and therefore should be called
 * during the preRender method of a World Wind layer. See the {@link gov.nasa.worldwind.render.PreRenderable} interface
 * for details. Once built, the surface tiles can be rendered by a {@link gov.nasa.worldwind.render.SurfaceTileRenderer}.
 * <p/>
 * By default, SurfaceObjectTileBuilder creates texture tiles with a width and height of 512 pixels, and with internal
 * format <code>GL_RGBA</code>. These parameters are configurable by calling {@link
 * #setTileDimension(java.awt.Dimension)} or {@link #setTileTextureFormat(int)}.
 * <p/>
 * The most common usage pattern for SurfaceObjectTileBuilder is to build the surface tiles from a set of surface
 * renderables during the preRender phase, then draw those surface tiles during the render phase. For example, a
 * renderable can use SurfaceObjectTileBuilder to draw a set of surface renderables as follows:
 * <p/>
 * <code>
 * <pre>
 * class MyRenderable implements Renderable, PreRenderable
 * {
 *     protected SurfaceObjectTileBuilder tileBuilder = new SurfaceObjectTileBuilder();
 *
 *     public void preRender(DrawContext dc)
 *     {
 *         List<?> surfaceRenderables = Arrays.asList(
 *             new SurfaceCircle(LatLon.fromDegrees(0, 100), 10000),
 *             new SurfaceSquare(LatLon.fromDegrees(0, 101), 10000));
 *         this.tileBuilder.buildSurfaceTiles(dc, surfaceRenderables);
 *     }
 *
 *     public void render(DrawContext dc)
 *     {
 *         dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.tileBuilder.getTiles(dc));
 *     }
 * }
 * </pre>
 * </code>
 *
 * @author dcollins
 * @version $Id: SurfaceObjectTileBuilder.java 3108 2015-05-26 19:07:06Z dcollins $
 */
public class SurfaceObjectTileBuilder
{
    /** The default surface tile texture dimension, in pixels. */
    protected static final int DEFAULT_TEXTURE_DIMENSION = 512;
    /** The default OpenGL internal format used to create surface tile textures. */
    protected static final int DEFAULT_TEXTURE_INTERNAL_FORMAT = GL.GL_RGBA8;
    /** The default OpenGL pixel format used to create surface tile textures. */
    protected static final int DEFAULT_TEXTURE_PIXEL_FORMAT = GL.GL_RGBA;
    /**
     * The default split scale. The split scale 2.9 has been empirically determined to render sharp lines and edges with
     * the SurfaceShapes such as SurfacePolyline and SurfacePolygon.
     */
    protected static final double DEFAULT_SPLIT_SCALE = 2.9;
    /** The default level zero tile delta used to construct a LevelSet. */
    protected static final LatLon DEFAULT_LEVEL_ZERO_TILE_DELTA = LatLon.fromDegrees(36, 36);
    /**
     * The default number of levels used to construct a LevelSet. Approximately 0.1 meters per pixel at the Earth's
     * equator.
     */
    protected static final int DEFAULT_NUM_LEVELS = 17;
    /** The next unique ID. This property is shared by all instances of SurfaceObjectTileBuilder. */
    protected static long nextUniqueId = 1;
    /**
     * Map associating a tile texture dimension to its corresponding LevelSet. This map is a class property in order to
     * share LevelSets across all instances of SurfaceObjectTileBuilder.
     */
    protected static Map<Dimension, LevelSet> levelSetMap = new HashMap<Dimension, LevelSet>();

    /**
     * Indicates the desired tile texture width and height, in pixels. Initially set to
     * <code>DEFAULT_TEXTURE_DIMENSION</code>.
     */
    protected Dimension tileDimension = new Dimension(DEFAULT_TEXTURE_DIMENSION, DEFAULT_TEXTURE_DIMENSION);
    /** The surface tile OpenGL texture format. 0 indicates the default format is used. */
    protected int tileTextureFormat;
    /** Controls if surface tiles are rendered using a linear filter or a nearest-neighbor filter. */
    protected boolean useLinearFilter = true;
    /** Controls if mip-maps are generated for surface tile textures. */
    protected boolean useMipmaps = true;
    /** Controls if tiles are forced to update during {@link #buildTiles(DrawContext, Iterable)}. */
    protected boolean forceTileUpdates;
    /** Controls the tile resolution as distance changes between the globe's surface and the eye point. */
    protected double splitScale = DEFAULT_SPLIT_SCALE;
    /**
     * List of currently assembled surface renderables. Valid only during the execution of {@link
     * #buildTiles(DrawContext, Iterable)}.
     */
    protected List<SurfaceRenderable> currentSurfaceObjects = new ArrayList<SurfaceRenderable>();
    /** List of currently assembled surface tiles. */
    protected Map<Object, TileInfo> tileInfoMap = new HashMap<Object, TileInfo>();
    /** The currently active TileInfo. Valid only during the execution of {@link #buildTiles(DrawContext, Iterable)}. */
    protected TileInfo currentInfo;
    /** Support class used to render to an offscreen surface tile. */
    protected OGLRenderToTextureSupport rttSupport = new OGLRenderToTextureSupport();

    /**
     * Constructs a new SurfaceObjectTileBuilder with a tile width and height of <code>512</code>, with the default tile
     * texture format, with linear filtering enabled, and with mip-mapping disabled.
     */
    public SurfaceObjectTileBuilder()
    {
    }

    /**
     * Constructs a new SurfaceObjectTileBuilder width the specified tile dimension, tile texture format, and flags
     * specifying if linear filtering and mip-mapping are enabled.
     *
     * @param tileTextureDimension the surface tile texture dimension, in pixels.
     * @param tileTextureFormat    the surface tile OpenGL texture format, or 0 to use the default format.
     * @param useLinearFilter      true to use linear filtering while rendering surface tiles; false to use
     *                             nearest-neighbor filtering.
     * @param useMipmaps           true to generate mip-maps for surface tile textures; false otherwise.
     *
     * @throws IllegalArgumentException if the tile dimension is null.
     */
    public SurfaceObjectTileBuilder(Dimension tileTextureDimension, int tileTextureFormat, boolean useLinearFilter,
        boolean useMipmaps)
    {
        if (tileTextureDimension == null)
        {
            String message = Logging.getMessage("nullValue.DimensionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setTileDimension(tileTextureDimension);
        this.setTileTextureFormat(tileTextureFormat);
        this.setUseLinearFilter(useLinearFilter);
        this.setUseMipmaps(useMipmaps);
    }

    /**
     * Returns the surface tile dimension.
     *
     * @return the surface tile dimension, in pixels.
     */
    public Dimension getTileDimension()
    {
        return this.tileDimension;
    }

    /**
     * Specifies the preferred surface tile texture dimension. If the dimension is larger than the viewport dimension,
     * this uses a dimension with width and height set to the largest power of two that is less than or equal to the
     * specified dimension and the viewport dimension.
     *
     * @param dimension the surface tile dimension, in pixels.
     *
     * @throws IllegalArgumentException if the dimension is null.
     */
    public void setTileDimension(Dimension dimension)
    {
        if (dimension == null)
        {
            String message = Logging.getMessage("nullValue.DimensionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.tileDimension = dimension;
    }

    /**
     * Returns the surface tile's OpenGL texture format, or 0 to indicate that the default format is used.
     *
     * @return the OpenGL texture format, or 0 if the default format is used.
     *
     * @see #setTileTextureFormat(int)
     */
    public int getTileTextureFormat()
    {
        return tileTextureFormat;
    }

    /**
     * Specifies the surface tile's OpenGL texture format. A value of 0 indicates that the default format should be
     * used. Otherwise, the texture format may be one of the following: <code> <ul> <li>GL_ALPHA</li> <li>GL_ALPHA4</li>
     * <li>GL_ALPHA8</li> <li>GL_ALPHA12</li> <li>GL_ALPHA16</li> <li>GL_COMPRESSED_ALPHA</li>
     * <li>GL_COMPRESSED_LUMINANCE</li> <li>GL_COMPRESSED_LUMINANCE_ALPHA</li> <li>GL_COMPRESSED_INTENSITY</li>
     * <li>GL_COMPRESSED_RGB</li> <li>GL_COMPRESSED_RGBA</li> <li>GL_DEPTH_COMPONENT</li> <li>GL_DEPTH_COMPONENT16</li>
     * <li>GL_DEPTH_COMPONENT24</li> <li>GL_DEPTH_COMPONENT32</li> <li>GL_LUMINANCE</li> <li>GL_LUMINANCE4</li>
     * <li>GL_LUMINANCE8</li> <li>GL_LUMINANCE12</li> <li>GL_LUMINANCE16</li> <li>GL_LUMINANCE_ALPHA</li>
     * <li>GL_LUMINANCE4_ALPHA4</li> <li>GL_LUMINANCE6_ALPHA2</li> <li>GL_LUMINANCE8_ALPHA8</li>
     * <li>GL_LUMINANCE12_ALPHA4</li> <li>GL_LUMINANCE12_ALPHA12</li> <li>GL_LUMINANCE16_ALPHA16</li>
     * <li>GL_INTENSITY</li> <li>GL_INTENSITY4</li> <li>GL_INTENSITY8</li> <li>GL_INTENSITY12</li>
     * <li>GL_INTENSITY16</li> <li>GL_R3_G3_B2</li> <li>GL_RGB</li> <li>GL_RGB4</li> <li>GL_RGB5</li> <li>GL_RGB8</li>
     * <li>GL_RGB10</li> <li>GL_RGB12</li> <li>GL_RGB16</li> <li>GL_RGBA</li> <li>GL_RGBA2</li> <li>GL_RGBA4</li>
     * <li>GL_RGB5_A1</li> <li>GL_RGBA8</li> <li>GL_RGB10_A2</li> <li>GL_RGBA12</li> <li>GL_RGBA16</li>
     * <li>GL_SLUMINANCE</li> <li>GL_SLUMINANCE8</li> <li>GL_SLUMINANCE_ALPHA</li> <li>GL_SLUMINANCE8_ALPHA8</li>
     * <li>GL_SRGB</li> <li>GL_SRGB8</li> <li>GL_SRGB_ALPHA</li> <li>GL_SRGB8_ALPHA8</li> </ul> </code>
     * <p/>
     * If the texture format is any of <code>GL_RGB, GL_RGB8, GL_RGBA, or GL_RGBA8</code>, the tile builder attempts to
     * use OpenGL framebuffer objects to render shapes to the texture tiles. Otherwise, this renders shapes to the
     * framebuffer and copies the framebuffer contents to the texture tiles.
     *
     * @param textureFormat the OpenGL texture format, or 0 to use the default format.
     */
    public void setTileTextureFormat(int textureFormat)
    {
        this.tileTextureFormat = textureFormat;
    }

    /**
     * Returns if linear filtering is used when rendering surface tiles.
     *
     * @return true if linear filtering is used; false if nearest-neighbor filtering is used.
     */
    public boolean isUseLinearFilter()
    {
        return useLinearFilter;
    }

    /**
     * Specifies if linear filtering should be used when rendering surface tiles.
     *
     * @param useLinearFilter true to use linear filtering; false to use nearest-neighbor filtering.
     */
    public void setUseLinearFilter(boolean useLinearFilter)
    {
        this.useLinearFilter = useLinearFilter;
    }

    /**
     * Returns if mip-maps are generated for surface tile textures.
     *
     * @return true if mip-maps are generated; false otherwise.
     */
    public boolean isUseMipmaps()
    {
        return this.useMipmaps;
    }

    /**
     * Specifies if mip-maps should be generated for surface tile textures.
     *
     * @param useMipmaps true to generate mip-maps; false otherwise.
     */
    public void setUseMipmaps(boolean useMipmaps)
    {
        this.useMipmaps = useMipmaps;
    }

    /**
     * Indicates whether or not tiles textures are forced to update during {@link #buildTiles(DrawContext, Iterable)}.
     * When true, tile textures always update their contents with the current surface renderables. When false, tile
     * textures only update their contents when the surface renderables change. Initially false.
     *
     * @return true if tile textures always update their contents, false if tile textures only update when the surface
     * renderables change.
     */
    public boolean isForceTileUpdates()
    {
        return this.forceTileUpdates;
    }

    /**
     * Specifies whether or not tiles textures are forced to update during {@link #buildTiles(DrawContext, Iterable)}.
     * When true, tile textures always update their contents with the current surface renderables. When false, tile
     * textures only update their contents when the surface renderables change.
     *
     * @param forceTileUpdates true if tile textures should always update their contents, false if tile textures should
     *                         only update when the surface renderables change.
     */
    public void setForceTileUpdates(boolean forceTileUpdates)
    {
        this.forceTileUpdates = forceTileUpdates;
    }

    /**
     * Sets the parameter controlling the tile resolution as distance changes between the globe's surface and the eye
     * point. Higher resolution is displayed as the split scale increases from 1.0. Lower resolution is displayed as the
     * split scale decreases from 1.0. The default value is 2.9.
     *
     * @param splitScale a value near 1.0 that controls the tile's surface texel resolution as the distance between the
     *                   globe's surface and the eye point change. Increasing values select higher resolution,
     *                   decreasing values select lower resolution. The default value is 2.9.
     */
    public void setSplitScale(double splitScale)
    {
        this.splitScale = splitScale;
    }

    /**
     * Returns the split scale value controlling the tile's surface texel resolution relative to the distance between
     * the globe's surface at the image position and the eye point.
     *
     * @return the current split scale.
     *
     * @see #setSplitScale(double)
     */
    public double getSplitScale()
    {
        return this.splitScale;
    }

    /**
     * Returns the number of SurfaceTiles assembled during the last call to {@link #buildTiles(DrawContext, Iterable)}.
     *
     * @param dc the draw context used to build tiles.
     *
     * @return a count of SurfaceTiles containing a composite representation of the specified surface renderables.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public int getTileCount(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object tileInfoKey = this.createTileInfoKey(dc);
        TileInfo tileInfo = this.tileInfoMap.get(tileInfoKey);
        return tileInfo != null ? tileInfo.tiles.size() : 0;
    }

    /**
     * Returns the list of SurfaceTiles assembled during the last call to {@link #buildTiles(DrawContext, Iterable)}.
     *
     * @param dc the draw context used to build tiles.
     *
     * @return a List of SurfaceTiles containing a composite representation of the specified surface renderables.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public Collection<? extends SurfaceTile> getTiles(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object tileInfoKey = this.createTileInfoKey(dc);
        TileInfo tileInfo = this.tileInfoMap.get(tileInfoKey);
        return tileInfo != null ? tileInfo.tiles : Collections.<SurfaceTile>emptyList();
    }

    /**
     * Assembles the surface tiles and draws any surface renderables in the iterable into those offscreen tiles. The
     * surface tiles are assembled to meet the necessary resolution of to the draw context's {@link
     * gov.nasa.worldwind.View}. This may temporarily use the framebuffer to perform offscreen rendering, and therefore
     * should be called during the preRender method of a World Wind {@link gov.nasa.worldwind.layers.Layer}.
     * <p/>
     * This does nothing if the specified iterable is null, is empty or contains no surface renderables.
     *
     * @param dc       the draw context to build tiles for.
     * @param iterable the iterable to gather surface renderables from.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public void buildTiles(DrawContext dc, Iterable<?> iterable)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        TileInfoKey tileInfoKey = this.createTileInfoKey(dc);
        this.currentInfo = this.tileInfoMap.get(tileInfoKey);
        if (this.currentInfo == null)
        {
            this.currentInfo = this.createTileInfo(dc);
            this.tileInfoMap.put(tileInfoKey, this.currentInfo);
        }

        this.currentSurfaceObjects.clear();
        this.currentInfo.tiles.clear();

        if (iterable == null)
            return;

        // Assemble the list of current surface renderables from the specified iterable.
        this.assembleSurfaceObjects(iterable);

        // We've cleared any tile assembly state from the last rendering pass. Determine if we can assemble and update
        // the tiles. If not, we're done.
        if (this.currentSurfaceObjects.isEmpty() || !this.canAssembleTiles(dc))
            return;

        // Assemble the current visible tiles and update their associated textures if necessary.
        this.assembleTiles(dc);
        this.updateTiles(dc);

        // Clear references to surface renderables to avoid dangling references. The surface renderable list is no
        // longer needed, no are the lists held by each tile.
        this.currentSurfaceObjects.clear();
        for (SurfaceObjectTile tile : this.currentInfo.tiles)
        {
            tile.clearObjectList();
        }
    }

    /**
     * Removes all entries from the list of SurfaceTiles assembled during the last call to {@link
     * #buildTiles(DrawContext, Iterable)}.
     *
     * @param dc the draw context used to build tiles.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public void clearTiles(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object tileInfoKey = this.createTileInfoKey(dc);
        TileInfo tileInfo = this.tileInfoMap.get(tileInfoKey);
        if (tileInfo != null)
        {
            tileInfo.tiles.clear();
        }
    }

    /**
     * Returns the list of pickable object candidates associated with the SurfaceTiles assembled during the last call to
     * {@link #buildTiles(DrawContext, Iterable)}.
     *
     * @param dc the draw context used to build tiles.
     *
     * @return the pick candidates associated with the list of SurfaceTiles.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public Collection<PickedObject> getPickCandidates(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object tileInfoKey = this.createTileInfoKey(dc);
        TileInfo tileInfo = this.tileInfoMap.get(tileInfoKey);
        return tileInfo != null ? tileInfo.pickCandidates : Collections.<PickedObject>emptyList();
    }

    /**
     * Removes all entries from the list of pickable object candidates assembled during the last call to {@link
     * #buildTiles(DrawContext, Iterable)}.
     *
     * @param dc the draw context used to build tiles.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public void clearPickCandidates(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object tileInfoKey = this.createTileInfoKey(dc);
        TileInfo tileInfo = this.tileInfoMap.get(tileInfoKey);
        if (tileInfo != null)
        {
            tileInfo.pickCandidates.clear();
        }
    }

    //**************************************************************//
    //********************  Tile Updating  *************************//
    //**************************************************************//

    /**
     * Updates each {@link SurfaceObjectTileBuilder.SurfaceObjectTile} in the {@link #currentInfo}. This is typically
     * called after {@link #assembleTiles(DrawContext)} to update the assembled tiles.
     * <p/>
     * This method does nothing if <code>currentTiles</code> is empty.
     *
     * @param dc the draw context the tiles relate to.
     */
    protected void updateTiles(DrawContext dc)
    {
        if (this.currentInfo.tiles.isEmpty())
            return;

        // The tile drawing rectangle has the same dimension as the current tile viewport, but it's lower left corner
        // is placed at the origin. This is because the orthographic projection setup by OGLRenderToTextureSupport
        // maps (0, 0) to the lower left corner of the drawing region, therefore we can drop the (x, y) offset when
        // drawing pixels to the texture, as (0, 0) is automatically mapped to (x, y). Since we've created the tiles
        // from a LevelSet where each level has equivalent dimension, we assume that tiles in the current tile list
        // have equivalent dimension.

        // The OpenGL framebuffer object extension used by RenderToTextureSupport works only for texture formats
        // GL_RGB and GL_RGBA. Disable framebuffer objects if the tile builder has been configured with a different
        // format.
        this.rttSupport.setEnableFramebufferObject(
            this.tileTextureFormat == 0 || // Default format is GL_RGB8.
                this.tileTextureFormat == GL.GL_RGB ||
                this.tileTextureFormat == GL.GL_RGB8 ||
                this.tileTextureFormat == GL.GL_RGBA ||
                this.tileTextureFormat == GL.GL_RGBA8);

        this.rttSupport.beginRendering(dc, 0, 0, this.currentInfo.tileWidth, this.currentInfo.tileHeight);
        try
        {
            for (SurfaceObjectTile tile : this.currentInfo.tiles)
            {
                this.updateTile(dc, tile);
            }
        }
        finally
        {
            this.rttSupport.endRendering(dc);
        }
    }

    /**
     * Draws the current list of surface renderables into the specified surface tile. The surface tiles is updated only
     * when necessary. The tile keeps track of the list of surface renderables rendered into it, and the state keys
     * those objects. The tile is updated if the list changes, if any of the state keys change, or if the tile has no
     * texture. Otherwise the tile is left unchanged and the update is skipped.
     *
     * @param dc   the draw context the tile relates to.
     * @param tile the tile to update.
     */
    protected void updateTile(DrawContext dc, SurfaceObjectTile tile)
    {
        // Get the tile's texture from the draw context's texture cache. If null we create a new texture and update the
        // texture cache below.
        Texture texture = tile.getTexture(dc.getTextureCache());

        // If force tile updates is off, compare the previous tile state against the currently computed state to
        // determine if the tile needs to be updated. The tile needs to be updated if any the following conditions are
        // true:
        // * The tile has no texture.
        // * The tile has no state.
        // * The list of intersecting objects has changed.
        // * An intersecting object's state key is different than one stored in the tile's previous state key.
        if (!this.isForceTileUpdates())
        {
            Object tileStateKey = tile.getStateKey(dc);
            if (texture != null && tileStateKey.equals(tile.lastUpdateStateKey))
                return;

            // If the tile needs to be updated, then assign its lastUpdateStateKey before its texture is created. This
            // ensures that the lastUpdateStateKey is current when the tile is added to the cache.
            tile.lastUpdateStateKey = tileStateKey;
        }

        if (texture == null) // Create the tile's texture if it doesn't already have one.
        {
            texture = this.createTileTexture(dc, tile.getWidth(), tile.getHeight());
            tile.setTexture(dc.getTextureCache(), texture);
        }

        if (texture == null) // This should never happen, but we check anyway.
        {
            Logging.logger().warning(Logging.getMessage("nullValue.TextureIsNull"));
            return;
        }

        try
        {
            // Surface renderables expect the SurfaceTileDrawContext to be attached to the draw context's AVList. Create
            // a SurfaceTileDrawContext with the tile's Sector and viewport. The Sector defines the context's geographic
            // extent, and the viewport defines the context's corresponding viewport in pixels.
            dc.setValue(AVKey.SURFACE_TILE_DRAW_CONTEXT, this.createSurfaceTileDrawContext(tile));

            this.rttSupport.setColorTarget(dc, texture);
            this.rttSupport.clear(dc, new Color(0, 0, 0, 0)); // Set all texture pixels to transparent black.

            if (tile.hasObjects())
            {
                for (SurfaceRenderable so : tile.getObjectList())
                {
                    so.render(dc);
                }
            }
        }
        finally
        {
            this.rttSupport.setColorTarget(dc, null);

            dc.removeKey(AVKey.SURFACE_TILE_DRAW_CONTEXT);
        }
    }

    /**
     * Returns a new surface tile texture for use on the specified draw context with the specified width and height.
     * <p/>
     * The returned texture's internal format is specified by <code>tilePixelFormat</code>. If
     * <code>tilePixelFormat</code> is zero, this returns a texture with internal format <code>GL_RGBA8</code>.
     * <p/>
     * The returned texture's parameters are configured as follows: <table> <tr><th>Parameter
     * Name</th><th>Value</th></tr> <tr><td><code>GL.GL_TEXTURE_MIN_FILTER</code></td><td><code>GL_LINEAR_MIPMAP_LINEAR</code>
     * if <code>useLinearFilter</code> and <code>useMipmaps</code> are both true, <code>GL_LINEAR</code> if
     * <code>useLinearFilter</code> is true and <code>useMipmaps</code> is false, and <code>GL_NEAREST</code> if
     * <code>useLinearFilter</code> is false.</td></tr> <tr><td><code>GL.GL_TEXTURE_MAG_FILTER</code></td><td><code>GL_LINEAR</code>
     * if <code>useLinearFilter</code> is true, <code>GL_NEAREST</code> if <code>useLinearFilter</code> is
     * false.</td></tr> <tr><td><code>GL.GL_TEXTURE_WRAP_S</code></td><td><code>GL_CLAMP_TO_EDGE</code></td></tr>
     * <tr><td><code>GL.GL_TEXTURE_WRAP_T</code></td><td><code>GL_CLAMP_TO_EDGE</code></td></tr>
     *
     * @param dc     the draw context to create a texture for.
     * @param width  the texture's width, in pixels.
     * @param height the texture's height, in pixels.
     *
     * @return a new texture with the specified width and height.
     */
    protected Texture createTileTexture(DrawContext dc, int width, int height)
    {
        int internalFormat = this.tileTextureFormat;
        if (internalFormat == 0)
            internalFormat = DEFAULT_TEXTURE_INTERNAL_FORMAT;

        int pixelFormat = OGLUtil.computeTexturePixelFormat(internalFormat);
        if (pixelFormat == 0)
            pixelFormat = DEFAULT_TEXTURE_PIXEL_FORMAT;

        Texture t;
        GL gl = dc.getGL();

        TextureData td = new TextureData(
            gl.getGLProfile(),    // GL profile
            internalFormat,       // internal format
            width, height,        // dimension
            0,                    // border
            pixelFormat,          // pixel format
            GL.GL_UNSIGNED_BYTE,  // pixel type
            this.isUseMipmaps(),  // mipmap
            false, false,         // dataIsCompressed, mustFlipVertically
            null, null)           // buffer, flusher
        {
            /**
             * Overridden to return a non-zero size. TextureData does not compute an estimated memory size if the buffer
             * is null. Therefore we override getEstimatedMemorySize() to return the appropriate size in bytes of a
             * texture with the common pixel formats.
             */
            @Override
            public int getEstimatedMemorySize()
            {
                int sizeInBytes = OGLUtil.estimateTextureMemorySize(this.getInternalFormat(), this.getWidth(),
                    this.getHeight(), this.getMipmap());
                if (sizeInBytes > 0)
                    return sizeInBytes;

                return super.getEstimatedMemorySize();
            }
        };

        t = TextureIO.newTexture(td);
        t.bind(gl);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, this.isUseLinearFilter() ?
            (this.isUseMipmaps() ? GL.GL_LINEAR_MIPMAP_LINEAR : GL.GL_LINEAR) : GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, this.isUseLinearFilter() ?
            GL.GL_LINEAR : GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

        if (this.isUseMipmaps())
        {
            double maxAnisotropy = dc.getGLRuntimeCapabilities().getMaxTextureAnisotropy();
            if (dc.getGLRuntimeCapabilities().isUseAnisotropicTextureFilter() && maxAnisotropy >= 2.0)
            {
                gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) maxAnisotropy);
            }
        }

        return t;
    }

    /**
     * Returns a new Object representing the drawing context for the specified tile. The returned object should
     * represent the tile's sector and it's corresponding viewport in pixels.
     *
     * @param tile The tile to create a context for.
     *
     * @return a new drawing context for the specified tile.
     */
    protected Object createSurfaceTileDrawContext(SurfaceObjectTile tile)
    {
        return new SurfaceTileDrawContext(tile, this.currentInfo.pickCandidates);
    }

    //**************************************************************//
    //********************  Surface Renderable Assembly  ***********//
    //**************************************************************//

    /**
     * Adds any SurfaceRenderables in the specified Iterable to the tile builder's {@link #currentSurfaceObjects} list.
     *
     * @param iterable the Iterable to gather SurfaceRenderables from.
     */
    protected void assembleSurfaceObjects(Iterable<?> iterable)
    {
        // Gather up all the SurfaceRenderables, ignoring null references and non SurfaceRenderables.
        for (Object o : iterable)
        {
            if (o instanceof SurfaceRenderable)
                this.currentSurfaceObjects.add((SurfaceRenderable) o);
        }
    }

    //**************************************************************//
    //********************  LevelSet Assembly  *********************//
    //**************************************************************//

    /**
     * Returns a shared <code>LevelSet</code> for the specified <code>tileDimension</code>. All instances of
     * <code>SurfaceObjectTileBuilder</code> share common LevelSets to determine which tiles are visible, but create
     * unique tile instances and uses a unique tile cache name. Since all instances use the same tile structure to
     * determine visible tiles, this saves memory while ensuring that each instance stores its own tiles in the cache.
     * <p/>
     * The returned LevelSet's cache name and dataset name are dummy values, and should not be used. Use this tile
     * builder's cache name for the specified <code>tileDimension</code> instead.
     * <p/>
     * In practice, there are at most 10 dimensions we use: 512, 256, 128, 64, 32, 16, 8, 4, 2, 1. Therefore keeping the
     * <code>LevelSet</code>s in a map requires little memory overhead, and ensures each <code>LevelSet</code> is
     * retained once constructed. Retaining references to the <code>LevelSet</code>s means we're able to re-use the
     * texture resources associated with each <code>LevelSet</code> in the <code>DrawContext</code>'s texture cache.
     * <p/>
     * Subsequent calls are guaranteed to return the same <code>LevelSet</code> for the same
     * <code>tileDimension</code>.
     *
     * @param tileWidth  the tile width, in pixels.
     * @param tileHeight the tile height, in pixels.
     *
     * @return a LevelSet with the specified tile dimensions.
     */
    protected LevelSet getLevelSet(int tileWidth, int tileHeight)
    {
        // If we already have a LevelSet for the dimension, just return it. Otherwise create it and put it in a map for
        // use during subsequent calls.
        Dimension key = new Dimension(tileWidth, tileHeight);
        LevelSet levelSet = levelSetMap.get(key);
        if (levelSet == null)
        {
            levelSet = createLevelSet(tileWidth, tileHeight);
            levelSetMap.put(key, levelSet);
        }

        return levelSet;
    }

    /**
     * Returns a new LevelSet with the specified tile width and height. The LevelSet overs the full sphere, has a level
     * zero tile delta of {@link #DEFAULT_LEVEL_ZERO_TILE_DELTA}, has number of levels equal to {@link
     * #DEFAULT_NUM_LEVELS} (with no empty levels). The LevelSets' cache name and dataset name dummy values, and should
     * not be used.
     *
     * @param tileWidth  the LevelSet's tile width, in pixels.
     * @param tileHeight the LevelSet's tile height, in pixels.
     *
     * @return a new LevelSet configured to with
     */
    protected static LevelSet createLevelSet(int tileWidth, int tileHeight)
    {
        AVList params = new AVListImpl();
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, DEFAULT_LEVEL_ZERO_TILE_DELTA);
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        params.setValue(AVKey.NUM_LEVELS, DEFAULT_NUM_LEVELS);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.TILE_WIDTH, tileWidth);
        params.setValue(AVKey.TILE_HEIGHT, tileHeight);
        // This is a shared LevelSet, so just supply a dummy cache name and dataset name.
        params.setValue(AVKey.DATA_CACHE_NAME, SurfaceObjectTileBuilder.class.getName());
        params.setValue(AVKey.DATASET_NAME, SurfaceObjectTileBuilder.class.getName());
        // We won't use any tile resource paths, so just supply a dummy format suffix.
        params.setValue(AVKey.FORMAT_SUFFIX, SurfaceObjectTileBuilder.class.getName());

        return new LevelSet(params);
    }

    //**************************************************************//
    //********************  Tile Assembly  *************************//
    //**************************************************************//

    /**
     * Returns true if the draw context's viewport width and height are greater than zero.
     *
     * @param dc the DrawContext to test.
     *
     * @return true if the DrawContext's has a non-zero viewport; false otherwise.
     */
    protected boolean canAssembleTiles(DrawContext dc)
    {
        Rectangle viewport = dc.getView().getViewport();
        return viewport.getWidth() > 0 && viewport.getHeight() > 0;
    }

    /**
     * Assembles a set of surface tiles that are visible in the specified DrawContext and meet the tile builder's
     * resolution criteria. Tiles are culled against the current surface renderable list, against the DrawContext's view
     * frustum during rendering mode, and against the DrawContext's pick frustums during picking mode. If a tile does
     * not meet the tile builder's resolution criteria, it's split into four sub-tiles and the process recursively
     * repeated on the sub-tiles. Visible leaf tiles are added to the {@link #currentInfo}.
     * <p/>
     * During assembly each surface renderable in {@link #currentSurfaceObjects} is sorted into the tiles they
     * intersect. The top level tiles are used as an index to quickly determine which tiles each renderable intersects.
     * Surface renderables are sorted into sub-tiles by simple intersection tests, and are added to each tile's surface
     * renderable list at most once. See {@link SurfaceObjectTileBuilder.SurfaceObjectTile#addSurfaceObject(SurfaceRenderable,
     * gov.nasa.worldwind.geom.Sector)}. Tiles that don't intersect any surface renderables are discarded.
     *
     * @param dc the DrawContext to assemble tiles for.
     */
    protected void assembleTiles(DrawContext dc)
    {
        LevelSet levelSet = this.currentInfo.levelSet;
        String tileCacheName = this.currentInfo.cacheName;

        Level level = levelSet.getFirstLevel();
        Angle dLat = level.getTileDelta().getLatitude();
        Angle dLon = level.getTileDelta().getLongitude();
        Angle latOrigin = levelSet.getTileOrigin().getLatitude();
        Angle lonOrigin = levelSet.getTileOrigin().getLongitude();

        // Store the top level tiles in a set to ensure that each top level tile is added only once. Store the tiles
        // that intersect each surface renderable in a set to ensure that each object is added to a tile at most once.
        Set<SurfaceObjectTile> topLevelTiles = new HashSet<SurfaceObjectTile>();
        Set<Object> intersectingTileKeys = new HashSet<Object>();

        // Iterate over the current surface renderables, adding each surface renderable to the top level tiles that it
        // intersects. This produces a set of top level tiles containing the surface renderables that intersect each
        // tile. We use the tile structure as an index to quickly determine the tiles a surface renderable intersects,
        // and add object to those tiles. This has the effect of quickly sorting the objects into the top level tiles.
        // We collect the top level tiles in a HashSet to ensure there are no duplicates when multiple objects intersect
        // the same top level tiles.
        for (SurfaceRenderable so : this.currentSurfaceObjects)
        {
            List<Sector> sectors = so.getSectors(dc);
            if (sectors == null)
                continue;

            for (Sector s : sectors)
            {
                // Use the LevelSets tiling scheme to index the surface renderable's sector into the top level tiles.
                // This index operation is faster than computing an intersection test between each tile and the list of
                // surface renderables.
                int firstRow = Tile.computeRow(dLat, s.getMinLatitude(), latOrigin);
                int firstCol = Tile.computeColumn(dLon, s.getMinLongitude(), lonOrigin);
                int lastRow = Tile.computeRow(dLat, s.getMaxLatitude(), latOrigin);
                int lastCol = Tile.computeColumn(dLon, s.getMaxLongitude(), lonOrigin);

                Angle p1 = Tile.computeRowLatitude(firstRow, dLat, latOrigin);
                for (int row = firstRow; row <= lastRow; row++)
                {
                    Angle p2;
                    p2 = p1.add(dLat);

                    Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
                    for (int col = firstCol; col <= lastCol; col++)
                    {
                        Angle t2;
                        t2 = t1.add(dLon);

                        Object tileKey = this.createTileKey(level, row, col, tileCacheName);

                        // Ignore this tile if the surface renderable has already been added to it. This handles
                        // dateline spanning surface renderables which have two sectors that share a common boundary.
                        if (intersectingTileKeys.contains(tileKey))
                            continue;

                        SurfaceObjectTile tile = (SurfaceObjectTile) TextureTile.getMemoryCache().getObject(tileKey);
                        if (tile == null)
                        {
                            tile = this.createTile(new Sector(p1, p2, t1, t2), level, row, col, tileCacheName);
                            TextureTile.getMemoryCache().add(tileKey, tile);
                        }

                        intersectingTileKeys.add(tileKey); // Set of intersecting tile keys ensure no duplicate objects.
                        topLevelTiles.add(tile); // Set of top level tiles ensures no duplicates tiles.
                        tile.addSurfaceObject(so, s);

                        t1 = t2;
                    }
                    p1 = p2;
                }
            }

            intersectingTileKeys.clear(); // Clear the intersecting tile keys for the next surface renderable.
        }

        // Add each top level tile or its descendants to the current tile list.
        for (SurfaceObjectTile tile : topLevelTiles)
        {
            this.addTileOrDescendants(dc, levelSet, null, tile);
        }
    }

    /**
     * Potentially adds the specified tile or its descendants to the tile builder's {@link #currentInfo}. The tile and
     * its descendants are discarded if the tile is not visible or does not intersect any surface renderables in the
     * parent's surface renderable list. See {@link SurfaceObjectTileBuilder.SurfaceObjectTile#getObjectList()}.
     * <p/>
     * If the tile meet the tile builder's resolution criteria it's added to the tile builder's
     * <code>currentTiles</code> list. Otherwise, it's split into four sub-tiles and each tile is recursively processed.
     * See {@link #meetsRenderCriteria(DrawContext, gov.nasa.worldwind.util.LevelSet, gov.nasa.worldwind.util.Tile)}.
     *
     * @param dc       the current DrawContext.
     * @param levelSet the tile's LevelSet.
     * @param parent   the tile's parent, or null if the tile is a top level tile.
     * @param tile     the tile to add.
     */
    protected void addTileOrDescendants(DrawContext dc, LevelSet levelSet, SurfaceObjectTile parent,
        SurfaceObjectTile tile)
    {
        // Ignore this tile if it falls completely outside the DrawContext's visible sector.
        if (!this.intersectsVisibleSector(dc, tile))
        {
            // This tile is not added to the current tile list, so we clear it's object list to prepare it for use
            // during the next frame.
            tile.clearObjectList();
            return;
        }

        // Ignore this tile if it falls completely outside the frustum. This may be the viewing frustum or the pick
        // frustum, depending on the implementation.
        if (!this.intersectsFrustum(dc, tile))
        {
            // This tile is not added to the current tile list, so we clear it's object list to prepare it for use
            // during the next frame.
            tile.clearObjectList();
            return;
        }

        // If the parent tile is not null, add any parent surface renderables that intersect this tile.
        if (parent != null)
            this.addIntersectingObjects(dc, parent, tile);

        // Ignore tiles that do not intersect any surface renderables.
        if (!tile.hasObjects())
            return;

        // If this tile meets the current rendering criteria, add it to the current tile list. This tile's object list
        // is cleared after the tile update operation.
        if (this.meetsRenderCriteria(dc, levelSet, tile))
        {
            this.addTile(tile);
            return;
        }

        Level nextLevel = levelSet.getLevel(tile.getLevelNumber() + 1);
        for (TextureTile subTile : tile.createSubTiles(nextLevel))
        {
            this.addTileOrDescendants(dc, levelSet, tile, (SurfaceObjectTile) subTile);
        }

        // This tile is not added to the current tile list, so we clear it's object list to prepare it for use during
        // the next frame.
        tile.clearObjectList();
    }

    /**
     * Adds surface renderables from the parent's object list to the specified tile's object list. If the tile's sector
     * does not intersect the sector bounding the parent's object list, this does nothing. Otherwise, this adds any of
     * the parent's surface renderables that intersect the tile's sector to the tile's object list.
     *
     * @param dc     the current DrawContext.
     * @param parent the tile's parent.
     * @param tile   the tile to add intersecting surface renderables to.
     */
    protected void addIntersectingObjects(DrawContext dc, SurfaceObjectTile parent, SurfaceObjectTile tile)
    {
        // If the parent has no objects, then there's nothing to add to this tile and we exit immediately.
        if (!parent.hasObjects())
            return;

        // If this tile does not intersect the parent's object bounding sector, then none of the parent's objects
        // intersect this tile. Therefore we exit immediately, and do not add any objects to this tile.
        if (!tile.getSector().intersects(parent.getObjectSector()))
            return;

        // If this tile contains the parent's object bounding sector, then all of the parent's objects intersect this
        // tile. Therefore we just add all of the parent's objects to this tile. Additionally, the parent's object
        // bounding sector becomes this tile's object bounding sector.
        if (tile.getSector().contains(parent.getObjectSector()))
        {
            tile.addAllSurfaceObjects(parent.getObjectList(), parent.getObjectSector());
        }
        // Otherwise, the tile may intersect some of the parent's object list. Compute which objects intersect this
        // tile, and compute this tile's bounding sector as the union of those object's sectors.
        else
        {
            for (SurfaceRenderable so : parent.getObjectList())
            {
                List<Sector> sectors = so.getSectors(dc);
                if (sectors == null)
                    continue;

                // Test intersection against each of the surface renderable's sectors. We break after finding an
                // intersection to avoid adding the same object to the tile more than once.
                for (Sector s : sectors)
                {
                    if (tile.getSector().intersects(s))
                    {
                        tile.addSurfaceObject(so, s);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Adds the specified tile to this tile builder's {@link #currentInfo} and the TextureTile memory cache.
     *
     * @param tile the tile to add.
     */
    protected void addTile(SurfaceObjectTile tile)
    {
        this.currentInfo.tiles.add(tile);
        TextureTile.getMemoryCache().add(tile.getTileKey(), tile);
    }

    /**
     * Test if the tile intersects the specified draw context's frustum. During picking mode, this tests intersection
     * against all of the draw context's pick frustums. During rendering mode, this tests intersection against the draw
     * context's viewing frustum.
     *
     * @param dc   the draw context the surface renderable is related to.
     * @param tile the tile to test for intersection.
     *
     * @return true if the tile intersects the draw context's frustum; false otherwise.
     */
    protected boolean intersectsFrustum(DrawContext dc, TextureTile tile)
    {
        Extent extent = tile.getExtent(dc);
        if (extent == null)
            return false;

        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(extent);

        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    /**
     * Test if the specified tile intersects the draw context's visible sector. This returns false if the draw context's
     * visible sector is null.
     *
     * @param dc   the current draw context.
     * @param tile the tile to test for intersection.
     *
     * @return true if the tile intersects the draw context's visible sector; false otherwise.
     */
    protected boolean intersectsVisibleSector(DrawContext dc, TextureTile tile)
    {
        return dc.getVisibleSector() != null && dc.getVisibleSector().intersects(tile.getSector());
    }

    /**
     * Tests if the specified tile meets the rendering criteria on the specified draw context. This returns true if the
     * tile is from the level set's final level, or if the tile achieves the desired resolution on the draw context.
     *
     * @param dc       the current draw context.
     * @param levelSet the level set the tile belongs to.
     * @param tile     the tile to test.
     *
     * @return true if the tile meets the rendering criteria; false otherwise.
     */
    protected boolean meetsRenderCriteria(DrawContext dc, LevelSet levelSet, Tile tile)
    {
        return levelSet.isFinalLevel(tile.getLevel().getLevelNumber()) || !this.needToSplit(dc, tile);
    }

    /**
     * Tests if the specified tile must be split to meets the desired resolution on the specified draw context. This
     * compares the distance form the eye point to the tile to determine if the tile meets the desired resolution for
     * the {@link gov.nasa.worldwind.View} attached to the draw context.
     *
     * @param dc   the current draw context.
     * @param tile the tile to test.
     *
     * @return true if the tile must be split; false otherwise.
     */
    protected boolean needToSplit(DrawContext dc, Tile tile)
    {
        // Compute the height in meters of a texel from the specified tile. Take care to convert from the radians to
        // meters by multiplying by the globe's radius, not the length of a Cartesian point. Using the length of a
        // Cartesian point is incorrect when the globe is flat.
        double texelSizeRadians = tile.getLevel().getTexelSize();
        double texelSizeMeters = dc.getGlobe().getRadius() * texelSizeRadians;

        // Compute the level of detail scale and the field of view scale. These scales are multiplied by the eye
        // distance to derive a scaled distance that is then compared to the texel size. The level of detail scale is
        // specified as a power of 10. For example, a detail factor of 3 means split when the cell size becomes more
        // than one thousandth of the eye distance. The field of view scale is specified as a ratio between the current
        // field of view and a the default field of view. In a perspective projection, decreasing the field of view by
        // 50% has the same effect on object size as decreasing the distance between the eye and the object by 50%.
        // The detail hint is reduced for tiles above 75 degrees north and below 75 degrees south.
        double s = this.getSplitScale();
        if (tile.getSector().getMinLatitude().degrees >= 75 || tile.getSector().getMaxLatitude().degrees <= -75)
            s *= 0.85;
        double detailScale = Math.pow(10, -s);
        double fieldOfViewScale = dc.getView().getFieldOfView().tanHalfAngle() / Angle.fromDegrees(45).tanHalfAngle();
        fieldOfViewScale = WWMath.clamp(fieldOfViewScale, 0, 1);

        // Compute the distance between the eye point and the sector in meters, and compute a fraction of that distance
        // by multiplying the actual distance by the level of detail scale and the field of view scale.
        double eyeDistanceMeters = tile.getSector().distanceTo(dc, dc.getView().getEyePoint());
        double scaledEyeDistanceMeters = eyeDistanceMeters * detailScale * fieldOfViewScale;

        // Split when the texel size in meters becomes greater than the specified fraction of the eye distance, also in
        // meters. Another way to say it is, use the current tile if its texel size is less than the specified fraction
        // of the eye distance.
        //
        // NOTE: It's tempting to instead compare a screen pixel size to the texel size, but that calculation is
        // window-size dependent and results in selecting an excessive number of tiles when the window is large.
        return texelSizeMeters > scaledEyeDistanceMeters;
    }

    //**************************************************************//
    //********************  Tile Info  *****************************//
    //**************************************************************//

    /**
     * Creates a key to address the tile information associated with the specified draw context. Each key is unique to
     * this instance, the tile dimensions that fit in the draw context's viewport, and the globe offset when a 2D globe
     * is in use. Using a unique set of tile information ensures that
     * <p/>
     * In practices, there are at most 10 dimensions we'll use (512, 256, 128, 64, 32, 16, 8, 4, 2, 1) and 3 globe
     * offsets (-1, 0, 1). Therefore there are at most 30 sets of tile information for each instance of
     * SurfaceObjectTileBuilder.
     *
     * @param dc the draw context to create the tile info key for.
     *
     * @return the tile info key for the specified draw context.
     */
    protected TileInfoKey createTileInfoKey(DrawContext dc)
    {
        Dimension tileDimension = this.computeTextureTileDimension(dc);
        return new TileInfoKey(dc, tileDimension.width, tileDimension.height);
    }

    /**
     * Creates a tile info associated with the specified draw context.
     *
     * @param dc the draw context to create the tile info for.
     *
     * @return the tile info for the specified draw context.
     */
    protected TileInfo createTileInfo(DrawContext dc)
    {
        // Use a LevelSet shared by all instances of this class to save memory and prevent a conflict in the tile and
        // texture caches. Use a cache name unique to this tile info instance.
        Dimension tileDimension = this.computeTextureTileDimension(dc);
        LevelSet levelSet = this.getLevelSet(tileDimension.width, tileDimension.height);
        String cacheName = this.uniqueCacheName();
        return new TileInfo(levelSet, cacheName, tileDimension.width, tileDimension.height);
    }

    /**
     * Returns the tile dimension used to create the tile textures for the specified <code>DrawContext</code>. This
     * attempts to use this tile builder's {@link #tileDimension}, but always returns a dimension that is is a power of
     * two, is square, and fits in the <code>DrawContext</code>'s viewport.
     *
     * @param dc the <code>DrawContext</code> to compute a texture tile dimension for.
     *
     * @return a texture tile dimension appropriate for the specified <code>DrawContext</code>.
     */
    protected Dimension computeTextureTileDimension(DrawContext dc)
    {
        // Force a square dimension by using the maximum of the tile builder's tileWidth and tileHeight.
        int maxSize = Math.max(this.tileDimension.width, this.tileDimension.height);

        // The viewport may be smaller than the desired dimension. For that reason, we constrain the desired tile
        // dimension by the viewport width and height.
        Rectangle viewport = dc.getView().getViewport();
        if (maxSize > viewport.width)
            maxSize = viewport.width;
        if (maxSize > viewport.height)
            maxSize = viewport.height;

        // The final dimension used to render all surface tiles will be the power of two which is less than or equal to
        // the preferred dimension, and which fits into the viewport.

        int potSize = WWMath.powerOfTwoFloor(maxSize);
        return new Dimension(potSize, potSize);
    }

    /**
     * Returns a unique name appropriate for use as part of a cache name.
     *
     * @return a unique cache name.
     */
    protected String uniqueCacheName()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append("/");
        sb.append(nextUniqueId++);

        return sb.toString();
    }

    protected static class TileInfoKey
    {
        public final int globeOffset;
        public final int tileWidth;
        public final int tileHeight;

        public TileInfoKey(DrawContext dc, int tileWidth, int tileHeight)
        {
            this.globeOffset = (dc.getGlobe() instanceof Globe2D) ? ((Globe2D) dc.getGlobe()).getOffset() : 0;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            TileInfoKey that = (TileInfoKey) o;
            return this.globeOffset == that.globeOffset
                && this.tileWidth == that.tileWidth
                && this.tileHeight == that.tileHeight;
        }

        @Override
        public int hashCode()
        {
            int result = this.globeOffset;
            result = 31 * result + this.tileWidth;
            result = 31 * result + this.tileHeight;
            return result;
        }
    }

    protected static class TileInfo
    {
        public ArrayList<SurfaceObjectTile> tiles = new ArrayList<SurfaceObjectTile>();
        public ArrayList<PickedObject> pickCandidates = new ArrayList<PickedObject>();
        public LevelSet levelSet;
        public String cacheName;
        public int tileWidth;
        public int tileHeight;

        public TileInfo(LevelSet levelSet, String cacheName, int tileWidth, int tileHeight)
        {
            this.levelSet = levelSet;
            this.cacheName = cacheName;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
        }
    }

    //**************************************************************//
    //********************  Surface Object Tile  *******************//
    //**************************************************************//

    /**
     * Returns a new SurfaceObjectTile corresponding to the specified {@code sector}, {@code level}, {@code row}, {@code
     * column}, and {@code cacheName}.
     *
     * @param sector    The tile's Sector.
     * @param level     The tile's Level in a {@link LevelSet}.
     * @param row       The tile's row in the Level, starting from 0 and increasing to the right.
     * @param column    The tile's column in the Level, starting from 0 and increasing upward.
     * @param cacheName Tile tile's cache name.
     *
     * @return a new SurfaceObjectTile.
     */
    protected SurfaceObjectTile createTile(Sector sector, Level level, int row, int column, String cacheName)
    {
        return new SurfaceObjectTile(sector, level, row, column, cacheName);
    }

    /**
     * Returns a new tile key corresponding to the tile with the specified {@code level}, {@code row}, {@code column},
     * and {@code cacheName}.
     *
     * @param level     The tile's Level in a {@link LevelSet}.
     * @param row       The tile's row in the Level, starting from 0 and increasing to the right.
     * @param column    The tile's column in the Level, starting from 0 and increasing upward.
     * @param cacheName Tile tile's cache name.
     *
     * @return a tile key.
     */
    protected Object createTileKey(Level level, int row, int column, String cacheName)
    {
        return new TileKey(level.getLevelNumber(), row, column, cacheName);
    }

    /**
     * Represents a {@link gov.nasa.worldwind.layers.TextureTile} who's contents is constructed by a set of surface
     * objects. The tile maintains a collection of surface renderables that intersect the tile, and provides methods for
     * to modify and retrieve that collection. Additionally, the method {@link #getStateKey(DrawContext)} provides a
     * mechanism to uniquely identify the tile's current state, including the state of each intersecting surface
     * object.
     */
    protected static class SurfaceObjectTile extends TextureTile
    {
        /** The sector that bounds the surface renderables intersecting the tile. */
        protected Sector objectSector;
        /** List of surface renderables intersecting the tile. */
        protected List<SurfaceRenderable> intersectingObjects;
        /** The state key that was valid when the tile was last updated. */
        protected Object lastUpdateStateKey;

        /**
         * Constructs a tile for a given sector, level, row and column of the tile's containing tile set.
         *
         * @param sector    The sector corresponding with the tile.
         * @param level     The tile's level within a containing level set.
         * @param row       The row index (0 origin) of the tile within the indicated level.
         * @param column    The column index (0 origin) of the tile within the indicated level.
         * @param cacheName The tile's cache name. Overrides the Level's cache name to associates the tile with it's
         *                  tile builder in a global cache.
         *
         * @throws IllegalArgumentException if any of the {@code sector}, {@code level}, or {@code cacheName } are
         *                                  {@code null}.
         */
        public SurfaceObjectTile(Sector sector, Level level, int row, int column, String cacheName)
        {
            super(sector, level, row, column, cacheName);
        }

        /**
         * Returns the tile's size in bytes. Overridden to append the size of the {@link #cacheName} and the {@link
         * #lastUpdateStateKey} to the superclass' computed size.
         *
         * @return The tile's size in bytes.
         */
        @Override
        public long getSizeInBytes()
        {
            long size = super.getSizeInBytes();

            if (this.lastUpdateStateKey instanceof Cacheable)
                size += ((Cacheable) this.lastUpdateStateKey).getSizeInBytes();
            else if (this.lastUpdateStateKey != null)
                size += 4; // If the object doesn't implement Cacheable, just account for the reference to it.

            return size;
        }

        /**
         * Returns an object that uniquely identifies the tile's state on the specified draw context. This object is
         * guaranteed to be globally unique; an equality test with a state key from another always returns false.
         *
         * @param dc the draw context the state key relates to.
         *
         * @return an object representing surface renderable's current state.
         */
        public Object getStateKey(DrawContext dc)
        {
            return new SurfaceObjectTileStateKey(dc, this);
        }

        /**
         * Returns a sector that bounds the surface renderables intersecting the tile. This returns null if no surface
         * objects intersect the tile.
         *
         * @return a sector bounding the tile's intersecting objects.
         */
        public Sector getObjectSector()
        {
            return this.objectSector;
        }

        /**
         * Returns whether list of surface renderables intersecting this tile has elements.
         *
         * @return {@code true} if the list of surface renderables intersecting this tile has elements, and {@code
         * false} otherwise.
         */
        public boolean hasObjects()
        {
            return this.intersectingObjects != null && !this.intersectingObjects.isEmpty();
        }

        /**
         * Returns a list of surface renderables intersecting the tile.
         *
         * @return a tile's intersecting objects.
         */
        public List<SurfaceRenderable> getObjectList()
        {
            return this.intersectingObjects;
        }

        /**
         * Clears the tile's list of intersecting objects. {@link #getObjectSector()} returns null after calling this
         * method.
         */
        public void clearObjectList()
        {
            this.intersectingObjects = null;
            this.objectSector = null;
        }

        /**
         * Adds the specified surface renderable to the tile's list of intersecting objects.
         *
         * @param so     the surface renderable to add.
         * @param sector the sector bounding the specified surface renderable.
         */
        public void addSurfaceObject(SurfaceRenderable so, Sector sector)
        {
            if (this.intersectingObjects == null)
                this.intersectingObjects = new ArrayList<SurfaceRenderable>();

            this.intersectingObjects.add(so);
            this.objectSector = (this.objectSector != null) ? this.objectSector.union(sector) : sector;
        }

        /**
         * Adds the specified collection of surface renderables to the tile's list of intersecting objects.
         *
         * @param c      the collection of surface renderables to add.
         * @param sector the sector bounding the specified surface renderable collection.
         */
        public void addAllSurfaceObjects(List<SurfaceRenderable> c, Sector sector)
        {
            if (this.intersectingObjects == null)
                this.intersectingObjects = new ArrayList<SurfaceRenderable>();

            this.intersectingObjects.addAll(c);
            this.objectSector = (this.objectSector != null) ? this.objectSector.union(sector) : sector;
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Overridden to return a new SurfaceObjectTile. The returned tile is created with the same cache name as this
         * tile.
         */
        @Override
        protected TextureTile createSubTile(Sector sector, Level level, int row, int col)
        {
            return new SurfaceObjectTile(sector, level, row, col, this.getCacheName());
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Overridden to return a TileKey with the same cache name as this tile.
         */
        @Override
        protected TileKey createSubTileKey(Level level, int row, int col)
        {
            return new TileKey(level.getLevelNumber(), row, col, this.getCacheName());
        }
    }

    /**
     * Represents a surface renderable tile's current state. TileStateKey distinguishes the tile's state by comparing
     * the individual state keys of the surface renderables intersecting the tile. This does not retain any references
     * to the surface renderables themselves. Should the tile state key live longer than the surface renderables, the
     * state key does not prevent those objects from being reclaimed by the garbage collector.
     */
    protected static class SurfaceObjectTileStateKey implements Cacheable
    {
        protected final TileKey tileKey;
        protected final Object[] intersectingObjectKeys;

        /**
         * Construsts a tile state key for the specified surface renderable tile.
         *
         * @param dc   the draw context the state key is related to.
         * @param tile the tile to construct a state key for.
         */
        public SurfaceObjectTileStateKey(DrawContext dc, SurfaceObjectTile tile)
        {
            if (tile != null && tile.hasObjects())
            {
                this.tileKey = tile.getTileKey();
                this.intersectingObjectKeys = new Object[tile.getObjectList().size()];

                int index = 0;
                for (SurfaceRenderable so : tile.getObjectList())
                {
                    this.intersectingObjectKeys[index++] = so.getStateKey(dc);
                }
            }
            else
            {
                this.tileKey = null;
                this.intersectingObjectKeys = null;
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            // Compare the tile keys and each state key in the array. The state keys are equal if the tile keys are
            // equal, the arrays equivalent length, and each array element is equivalent. Arrays.equals() correctly
            // handles null references.
            SurfaceObjectTileStateKey that = (SurfaceObjectTileStateKey) o;
            return (this.tileKey != null ? this.tileKey.equals(that.tileKey) : that.tileKey == null)
                && Arrays.equals(this.intersectingObjectKeys, that.intersectingObjectKeys);
        }

        @Override
        public int hashCode()
        {
            int result = this.tileKey != null ? this.tileKey.hashCode() : 0;
            result = 31 * result + Arrays.hashCode(this.intersectingObjectKeys); // Correctly handles a null reference.
            return result;
        }

        /**
         * Returns the tile state key's size in bytes. The total size of the intersecting object keys, plus the size of
         * the array itself. The tileKey is owned by the SurfaceObjectTile, so we don't include it in the state key's
         * size.
         *
         * @return The state key's size in bytes.
         */
        public long getSizeInBytes()
        {
            if (this.intersectingObjectKeys == null)
                return 0;

            long size = 4 * this.intersectingObjectKeys.length; // For the array references.

            for (Object o : this.intersectingObjectKeys)
            {
                if (o instanceof Cacheable)
                    size += ((Cacheable) o).getSizeInBytes();
                else if (o != null)
                    size += 4; // If the object doesn't implement Cacheable, just account for the reference to it.
            }

            return size;
        }
    }
}
