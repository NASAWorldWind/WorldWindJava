/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.util.*;
import java.util.List;

/**
 * A frame that can scroll its contents. The frame can be interactively resized by dragging the border, and be moved by
 * dragging the frame or title bar. The frame can be minimized. The frame displays scroll bars if the size of the
 * content exceeds the size of the frame, and optionally displays a title bar with a text string and an icon.
 * <p/>
 * The frame renders its contents into a texture, and then draws the texture when the frame is rendered. This provides
 * good performance for content that is expensive to draw, and changes infrequently. If the frame is sized so large that
 * the visible portion of the contents cannot be rendered into a single texture, then the contents will be drawn
 * directly when the frame is rendered.
 *
 * @author pabercrombie
 * @version $Id: ScrollFrame.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class ScrollFrame extends DragControl implements PreRenderable, Renderable
{
    /** Default dimension of tiles in the backing texture. */
    protected static final int DEFAULT_TEXTURE_TILE_DIMENSION = 512;

    /** Default height of the frame title bar. */
    protected static final int DEFAULT_TITLE_BAR_HEIGHT = 25;
    /** Default size of the minimize button. */
    protected static final int DEFAULT_BUTTON_SIZE = 18;
    /** Default width of the scroll bars. */
    protected static final int DEFAULT_SCROLL_BAR_SIZE = 15;
    /** Default width of the frame border. */
    protected static final int DEFAULT_FRAME_BORDER_WIDTH = 3;
    /** Default width of the pickable frame border. */
    protected static final int DEFAULT_FRAME_BORDER_PICK_WIDTH = 10;
    /** Default width of lines used to draw the frame. */
    protected static final int DEFAULT_LINE_WIDTH = 1;
    /** Default delay (in milliseconds) between frame when the frame is animating. */
    protected static final int DEFAULT_ANIMATION_DELAY = 5;
    /** Default size of the maximized frame. */
    protected static final Size DEFAULT_MAXIMIZED_SIZE = Size.fromPixels(265, 300);

    /** Attributes to use when the frame is not highlighted. */
    protected FrameAttributes normalAttributes;
    /** Attributes to use when the frame is highlighted. */
    protected FrameAttributes highlightAttributes;
    /** Active attributes, either normal or highlight. */
    protected FrameAttributes activeAttributes = new BasicFrameAttributes(); // re-determined each frame

    /**
     * The full frame title. This title may be displayed in a truncated form if the frame is too small to accommodate
     * the full title.
     */
    protected String frameTitle;

    /** The contents of the frame. */
    protected Scrollable contents;

    /** Indicates the location of the upper left corner of the frame. */
    protected Offset screenLocation;

    /** Indicates whether or not to draw a title bar in the frame. Default is true. */
    protected boolean drawTitleBar = true;
    /** Indicates whether or not the user can resize the frame by dragging the edge. Default is true. */
    protected boolean enableResize = true;
    /** Indicates whether or not the user can move the frame by dragging with the mouse. Default is true. */
    protected boolean enableMove = true;

    /** The height, in pixels, of the frame title bar. */
    protected int titleBarHeight = DEFAULT_TITLE_BAR_HEIGHT;
    /** The size, in pixels, of the frame's minimize button. */
    protected int buttonSize = DEFAULT_BUTTON_SIZE;
    /** The width of the frame scroll bar. */
    protected int scrollBarSize = DEFAULT_SCROLL_BAR_SIZE;
    /** The width of the frame border. */
    protected int frameBorder = DEFAULT_FRAME_BORDER_WIDTH;
    /** The width of lines used to draw the frame. */
    protected int frameLineWidth = DEFAULT_LINE_WIDTH;

    /** Support for setting up and restoring OpenGL state during rendering. */
    protected OGLStackHandler BEogsh = new OGLStackHandler();
    /** Support for setting up and restoring picking state, and resolving the picked object. */
    protected PickSupport pickSupport = new PickSupport();

    /** Scroll bar to control vertical scrolling. */
    protected ScrollBar verticalScrollBar;
    /** Scroll bar to control horizontal scrolling. */
    protected ScrollBar horizontalScrollBar;

    /** Indicates whether or not the frame is minimized. */
    protected boolean minimized = false;

    /** The size of the maximized frame. */
    protected Size maximizedSize = DEFAULT_MAXIMIZED_SIZE;
    /** The size of the minimized frame. */
    protected Size minimizedSize;
    /** The size of the active frame, minimized or maximized. */
    protected Size activeSize;
    /** The maximum size of the frame. This is a constraint applied to the frame's size. */
    protected Size maxSize;

    /** Image source for the icon drawn in the upper left corner of the frame. */
    protected Object iconImageSource;
    /** Texture for the icon displayed in the frame title bar. Loaded from {@link #iconImageSource}. */
    protected BasicWWTexture texture;

    /** An animation to play when the frame is minimized or maximized. */
    protected Animation minimizeAnimation;
    /** The active animation that is currently playing. */
    protected Animation animation;
    /** Delay in milliseconds between frames of an animation. */
    protected int animationDelay = DEFAULT_ANIMATION_DELAY;

    // UI controls
    /** HotSpot to handle user input on the minimize button. */
    protected HotSpot minimizeButton;
    /** Control to handle resizing the frame. */
    protected FrameResizeControl frameResizeControl;
    /** Width of the pickable frame border. */
    protected int borderPickWidth = DEFAULT_FRAME_BORDER_PICK_WIDTH;

    /** The frame geometry vertices passed to OpenGL. */
    protected DoubleBuffer vertexBuffer;

    /** Support class used to render to an offscreen texture. */
    protected OGLRenderToTextureSupport rttSupport = new OGLRenderToTextureSupport();
    protected List<ContentTile> tiles = new ArrayList<ContentTile>();
    /**
     * Indicates whether the contents should be rendered into a texture and cached, or rendered directly. The frame
     * renders into a texture if the frame size can be accommodated by a single texture. If the size is too large to
     * render using a single texture, the contents are drawn directly.
     */
    protected boolean renderToTexture;

    /** Cache key used to locate the rendering texture in the DrawContext's texture cache. */
    protected final Object textureCacheKey = new Object();

    /**
     * Map that associates logical tiles in the scrollable content with allocated tiles in the texture used to render
     * the content.
     */
    protected Map<ContentTile, TextureTile> textureTileMap = new HashMap<ContentTile, TextureTile>();
    /** List to manage sub-tiles of the rendering texture. */
    protected List<TextureTile> textureTiles = new ArrayList<TextureTile>();
    /** Dimension of the texture used to render the scrollable content. Must be a power of two. */
    protected int textureDimension;
    /**
     * Dimension of a sub-tile in the rendering texture. Also the dimension of logical tiles in the scrollable content
     * that are rendered into the texture tiles.
     */
    protected int textureTileDimension = DEFAULT_TEXTURE_TILE_DIMENSION;

    // Frame title fields. These fields are recomputed when the frame size changes, the title text changes, or the
    // title font changes.
    /**
     * Frame title that is actually drawn in the title bar. If the frame is too narrow to display the entire title, the
     * title will be truncated. This title must be regenerated if the frame size or the title font change.
     */
    protected String shortTitle;
    /** Width of the frame title area. */
    protected int frameTitleWidth;
    /** Font used to generate {@link #shortTitle}. */
    protected Font shortFrameTitleFont;
    /** Text bounds of the {@link #shortTitle}. */
    protected Rectangle2D shortTitleBounds;

    // Computed each frame
    protected long frameNumber = -1;
    /** Indicates that the frame must be regenerated because the size or attributes have changed. */
    protected boolean mustRecomputeFrameGeometry = true;
    /**
     * Indicates the location of the upper left corner of the frame, in AWT coordinates (origin at the upper left corner
     * of the screen.
     */
    protected Point2D awtScreenPoint;
    /** Bounds of the full frame. */
    protected Rectangle frameBounds;
    /** Bounds of the frame inside the frame border. */
    protected Rectangle innerBounds;
    /** Bounds of the content part of the frame. */
    protected Rectangle contentBounds;
    protected Rectangle scrollContentBounds;
    /** Bounds of the pickable area. */
    protected Rectangle pickBounds;
    /**
     * Total size of the frame content. This size may exceed the size of the frame, in which case scroll bars will be
     * displayed.
     */
    protected Dimension contentSize;
    /** Size of the frame. */
    protected Dimension frameSize;
    /** Indicates whether or not the frame is highlighted. */
    protected boolean highlighted;
    /** Indicates whether or not the vertical scroll bar must be drawn. */
    protected boolean showVerticalScrollbar;
    /** Indicates whether or not the horizontal scroll bar must be drawn. */
    protected boolean showHorizontalScrollbar;

    /** The attributes used if attributes are not specified. */
    protected static final FrameAttributes defaultAttributes;

    static
    {
        defaultAttributes = new BasicFrameAttributes();
    }

    /** Create a new scroll frame. */
    public ScrollFrame()
    {
        super(null);
        this.initializeUIControls();
    }

    /**
     * Create a scroll frame with a position.
     *
     * @param x x coordinate of the upper left corner of the frame, in AWT screen coordinates (origin upper left corner
     *          of the screen).
     * @param y y coordinate of the upper left corner of the frame, in AWT screen coordinates (origin upper left corner
     *          of the screen).
     */
    public ScrollFrame(int x, int y)
    {
        this(new Offset((double) x, (double) y, AVKey.PIXELS, AVKey.INSET_PIXELS));
    }

    /**
     * Create a scroll positioned with an Offset.
     *
     * @param screenLocation initial location of the upper left corner of the frame.
     */
    public ScrollFrame(Offset screenLocation)
    {
        super(null);
        this.setScreenLocation(screenLocation);
        this.initializeUIControls();
    }

    /**
     * Indicates the frame contents.
     *
     * @return the contents of the frame.
     */
    public Scrollable getContents()
    {
        return contents;
    }

    /**
     * Specifies the frame contents.
     *
     * @param contents new frame contents.
     */
    public void setContents(Scrollable contents)
    {
        this.contents = contents;
    }

    /**
     * Indicates if the frame is minimized. If the frame is not minimized, it is either maximized or animating toward a
     * maximized state.
     *
     * @return {@code true} if the frame is minimized.
     */
    public boolean isMinimized()
    {
        return this.minimized;
    }

    /**
     * Sets the frame to its minimized or maximized state. If the frame is not already in the desired state it will
     * animate to the desired state.
     *
     * @param minimized {@code true} if the frame must be minimized. {@code false} if the frame must not be minimized.
     */
    public void setMinimized(boolean minimized)
    {
        if (minimized != this.isMinimized())
        {
            this.minimized = minimized;
            if (this.minimizeAnimation != null)
            {
                this.animation = this.minimizeAnimation;
                this.animation.reset();
            }
        }
    }

    /**
     * Indicates whether or not the frame is highlighted.
     *
     * @return {@code true} if the frame is highlighted, otherwise {@code false}.
     */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /**
     * Sets the frame its highlighted or not highlighted state.
     *
     * @param highlighted {@code true} if the frame is now highlighted.
     */
    public void setHighlighted(boolean highlighted)
    {
        if (this.highlighted != highlighted)
        {
            this.highlighted = highlighted;

            this.contents.setHighlighted(highlighted);
        }
    }

    /**
     * Get the title of the tree frame.
     *
     * @return The frame title.
     *
     * @see #setFrameTitle(String)
     */
    public String getFrameTitle()
    {
        return this.frameTitle;
    }

    /**
     * Set the title of the tree frame.
     *
     * @param frameTitle New frame title.
     *
     * @see #getFrameTitle()
     */
    public void setFrameTitle(String frameTitle)
    {
        this.frameTitle = frameTitle;

        // Invalidate the computed title for display. It will be regenerated the next time the frame is rendered.
        this.shortTitle = null;
    }

    /**
     * Get the size of tree frame. The size determines the size of the frame when the frame is maximized.
     *
     * @return the size of the tree frame.
     *
     * @see #getMinimizedSize()
     */
    public Size getSize()
    {
        return this.maximizedSize;
    }

    /**
     * Set the size of the frame. The size determines the size of the frame when the frame is maximized.
     *
     * @param size New size.
     *
     * @see #setMinimizedSize(gov.nasa.worldwind.render.Size)
     */
    public void setSize(Size size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.SizeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maximizedSize = size;
        this.mustRecomputeFrameGeometry = true;

        if (!this.isAnimating())
            this.forceTileUpdate();
    }

    /**
     * Indicates the size of the minimized tree frame. This size is used when the tree is minimized or animating.
     *
     * @return the size of the minimized frame. {@code null} indicates that there is no minimized size, in which case
     *         the normal maximized frame size is used in the minimized state.
     */
    public Size getMinimizedSize()
    {
        return this.minimizedSize;
    }

    /**
     * Specifies the size of the minimized tree frame. This size is used when the tree is minimized or animating. An
     * animation can use this field to manipulate the size of the frame as the animation runs without interfering with
     * the frame's normal maximized size.
     *
     * @param size the size of the minimized frame. Set {@code null} to use the same size in maximized and minimized
     *             states.
     */
    public void setMinimizedSize(Size size)
    {
        this.minimizedSize = size;
        this.mustRecomputeFrameGeometry = true;

        if (!this.isAnimating())
            this.forceTileUpdate();
    }

    /**
     * Indicates the maximum size of frame. This size is applied as a constraint to the tree's normal size (indicated by
     * {@link #getSize()}).
     *
     * @return the maximum size of the frame. {@code null} indicates no maximum.
     */
    public Size getMaxSize()
    {
        return this.maxSize;
    }

    /**
     * Specifies the maximum size of the frame. This size is applied as a constraint to the tree's normal size
     * (indicated by {@link #getSize()}).
     *
     * @param size the maximum size of the minimized frame. Set {@code null} for no maximum.
     */
    public void setMaxSize(Size size)
    {
        this.maxSize = size;
        this.mustRecomputeFrameGeometry = true;

        if (!this.isAnimating())
            this.forceTileUpdate();
    }

    /**
     * Return the amount of screen that space that the frame is currently using. The frame size may change due to a
     * window resize, or an animation.
     *
     * @return The size of the frame on screen, in pixels. This method will return null until the frame has been
     *         rendered at least once.
     */
    public Dimension getCurrentSize()
    {
        return this.frameSize;
    }

    /**
     * Indicates the height, in pixels, of the frame title bar.
     *
     * @return height of the title bar.
     *
     * @see #isDrawTitleBar()
     */
    public int getTitleBarHeight()
    {
        return this.titleBarHeight;
    }

    /**
     * Specifies the height, in pixels, of the frame title bar.
     *
     * @param titleBarHeight new height, in pixels.
     */
    public void setTitleBarHeight(int titleBarHeight)
    {
        if (titleBarHeight < 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", titleBarHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.titleBarHeight = titleBarHeight;
    }

    /**
     * Does the frame have a title bar?
     *
     * @return True if the frame will draw a title bar.
     */
    public boolean isDrawTitleBar()
    {
        return this.drawTitleBar;
    }

    /**
     * Set whether the frame has a title bar.
     *
     * @param drawTitleBar True if the frame will draw a title bar.
     *
     * @see #setTitleBarHeight(int)
     */
    public void setDrawTitleBar(boolean drawTitleBar)
    {
        this.drawTitleBar = drawTitleBar;
    }

    /**
     * Indicates whether or not the user can resize the frame by dragging the frame border.
     *
     * @return {@code true} if the user can resize the frame by dragging.
     */
    public boolean isEnableResizeControl()
    {
        return this.enableResize;
    }

    /**
     * Specifies whether the user can resize the frame by dragging the border.
     *
     * @param enable {@code true} to allow the user to resize the frame by dragging the border.
     */
    public void setEnableResizeControl(boolean enable)
    {
        this.enableResize = enable;
    }

    /**
     * Specifies whether the user can move the frame by dragging the title bar.
     *
     * @return {@code true} if the user can allowed to move the frame by dragging the title bar.
     */
    public boolean isEnableMove()
    {
        return this.enableMove;
    }

    /**
     * Specifies whether the user can move the frame by dragging the title bar.
     *
     * @param enable {@code true} if the user is allowed to move the frame by dragging.
     */
    public void setEnableMove(boolean enable)
    {
        this.enableMove = enable;
    }

    /**
     * Get the animation that is played when the tree frame is minimized.
     *
     * @return Animation played when the frame is minimized.
     *
     * @see #setMinimizeAnimation(Animation)
     */
    public Animation getMinimizeAnimation()
    {
        return minimizeAnimation;
    }

    /**
     * Set the animation that is played when the tree frame is minimized.
     *
     * @param minimizeAnimation New minimize animation.
     *
     * @see #getMinimizeAnimation()
     */
    public void setMinimizeAnimation(Animation minimizeAnimation)
    {
        this.minimizeAnimation = minimizeAnimation;
    }

    /**
     * Get the image source for the frame icon.
     *
     * @return The icon image source, or null if no image source has been set.
     *
     * @see #setIconImageSource(Object)
     */
    public Object getIconImageSource()
    {
        return this.iconImageSource;
    }

    /**
     * Set the image source of the frame icon. This icon is drawn in the upper right hand corner of the tree frame.
     *
     * @param imageSource New image source. May be a String, URL, or BufferedImage.
     */
    public void setIconImageSource(Object imageSource)
    {
        this.iconImageSource = imageSource;
    }

    /**
     * Get the bounds of the tree frame.
     *
     * @param dc Draw context
     *
     * @return The bounds of the tree frame on screen, in screen coordinates (origin at upper left).
     */
    public Rectangle getBounds(DrawContext dc)
    {
        this.updateBounds(dc);

        return new Rectangle((int) this.awtScreenPoint.getX(), (int) this.awtScreenPoint.getY(), this.frameSize.width,
            this.frameSize.height);
    }

    /**
     * Get the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @return Screen location, measured in pixels from the upper left corner of the screen.
     */
    public Offset getScreenLocation()
    {
        return this.screenLocation;
    }

    /**
     * Set the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @param screenLocation New screen location.
     */
    public void setScreenLocation(Offset screenLocation)
    {
        this.screenLocation = screenLocation;
    }

    /**
     * Get the location of the upper left corner of the frame, measured from the upper left corner of the screen.
     *
     * @return The location of the upper left corner of the frame. This method will return null until the has been
     *         rendered.
     */
    protected Point2D getScreenPoint()
    {
        return this.awtScreenPoint;
    }

    /**
     * Indicates the frame attributes used to draw the frame when it is not highlighted.
     *
     * @return normal frame attributes.
     */
    public FrameAttributes getAttributes()
    {
        return this.normalAttributes;
    }

    /**
     * Specifies the frame attributes used to draw the frame when it is not highlighted.
     *
     * @param attributes new attributes bundle for normal state.
     */
    public void setAttributes(FrameAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.normalAttributes = attributes;
    }

    /**
     * Indicates the frame attributes used to draw the frame when it is highlighted.
     *
     * @return highlight frame attributes.
     */
    public FrameAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    /**
     * Specifies the frame attributes used to draw the frame when it is highlighted.
     *
     * @param attributes new attributes bundle for highlight state.
     */
    public void setHighlightAttributes(FrameAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.highlightAttributes = attributes;
    }

    //**************************************************************//
    //********************  Tile Updating  *************************//
    //**************************************************************//

    /**
     * Build the list of ContentTile that represents the logical tiles in the frame contents.
     *
     * @param rows    Number of rows of tiles in the contents.
     * @param columns Number of columns of tiles.
     */
    protected void assembleTiles(int rows, int columns)
    {
        this.tiles.clear();

        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < columns; j++)
            {
                ContentTile newTile = new ContentTile(i, j);
                this.tiles.add(newTile);
            }
        }
    }

    /**
     * Indicates whether or not any of the the rendered tiles must be updated.
     *
     * @param dc Current draw context.
     *
     * @return {@code true} if any of the tiles need to be updated.
     */
    protected boolean mustUpdateTiles(DrawContext dc)
    {
        // Tiles are not visible if the frame is minimized, so no reason to update
        if (this.isMinimized())
            return false;

        // Make sure that our texture is available. If the texture has been evicted it will need to be regenerated.
        if (dc.getTextureCache().getTexture(this.textureCacheKey) == null)
            return true;

        if (tiles.isEmpty())
            return true;

        long contentUpdateTime = this.contents.getUpdateTime();

        for (ContentTile tile : this.tiles)
        {
            if (this.mustUpdateTile(tile, contentUpdateTime))
                return true;
        }
        return false;
    }

    /**
     * Determine if a tile in the content layout needs to be updated.
     *
     * @param tile              Tile to test.
     * @param contentUpdateTime Time at which the content was last updated.
     *
     * @return {@code true} if the tile needs to be updated. Always returns {@code false} if the tile is not visible in
     *         the current frame bounds.
     */
    protected boolean mustUpdateTile(ContentTile tile, long contentUpdateTime)
    {
        Rectangle tileBounds = this.getContentTileBounds(tile.row, tile.column);
        if (this.contentBounds.intersects(tileBounds))
        {
            if (tile.updateTime != contentUpdateTime)
                return true;

            TextureTile textureTile = this.getTextureTile(tile);
            if (textureTile == null)
                return true;
        }

        return false;
    }

    /**
     * Update content tiles that have been rendered to a texture.
     *
     * @param dc Current draw context.
     */
    protected void updateTiles(DrawContext dc)
    {
        // The OpenGL framebuffer object extension used by RenderToTextureSupport works only for texture formats
        // GL_RGB and GL_RGBA.
        this.rttSupport.setEnableFramebufferObject(true);

        Texture texture = dc.getTextureCache().getTexture(this.textureCacheKey);

        // Determine how large of a texture is required to render the full frame bounds.
        int dim = this.computeTileTextureDimension(this.contentBounds.getSize(), this.contentSize);

        // Check the texture dimension against the maximum supported by the hardware
        int maxTexture = dc.getGLRuntimeCapabilities().getMaxTextureSize();
        this.renderToTexture = (dim <= maxTexture);

        // If the frame is too big to render into a texture don't bother building tiles
        if (!this.renderToTexture)
        {
            return;
        }

        // If we don't have a texture, or if we need a different size of texture, allocate a new one
        if (texture == null || this.textureDimension != dim)
        {
            texture = this.createTileTexture(dc, dim, dim);
            dc.getTextureCache().put(this.textureCacheKey, texture);
            this.textureDimension = dim;

            int numTiles = dim / this.textureTileDimension;

            // Create entries for the sub-tiles in the texture. Each sub-tile will be used to render a piece of the
            // frame contents.
            this.textureTiles.clear();
            for (int i = 0; i < numTiles; i++)
            {
                for (int j = 0; j < numTiles; j++)
                {
                    this.textureTiles.add(new TextureTile(i, j));
                }
            }

            // Clear the texture tile map. Any previous tile allocations are now invalid.
            this.textureTileMap.clear();
        }

        if (texture == null) // This should never happen, but we check anyway.
        {
            Logging.logger().warning(Logging.getMessage("nullValue.TextureIsNull"));
            return;
        }

        int rows = (int) Math.ceil((double) this.contentSize.height / this.textureTileDimension);
        int columns = (int) Math.ceil((double) this.contentSize.width / this.textureTileDimension);
        if (tiles.size() != rows * columns)
        {
            this.assembleTiles(rows, columns);
        }

        long contentUpdateTime = this.contents.getUpdateTime();

        // Update each tile that needs updating
        for (ContentTile tile : this.tiles)
        {
            if (this.mustUpdateTile(tile, contentUpdateTime))
            {
                TextureTile textureTile = this.getTextureTile(tile);
                if (textureTile == null)
                {
                    textureTile = this.allocateTextureTile(tile);
                }

                int x = textureTile.column * this.textureTileDimension;
                int y = textureTile.row * this.textureTileDimension;

                Rectangle tileBounds = new Rectangle(x, y, this.textureTileDimension, this.textureTileDimension);

                this.rttSupport.beginRendering(dc, tileBounds.x, tileBounds.y, tileBounds.width, tileBounds.height);
                try
                {
                    this.updateTile(dc, tile, tileBounds);

                    tile.updateTime = contentUpdateTime;
                    textureTile.lastUsed = dc.getFrameTimeStamp();
                }
                finally
                {
                    this.rttSupport.endRendering(dc);
                }
            }
        }
    }

    /**
     * Get the texture tile allocated for a ContentTile in the frame content.
     *
     * @param tile ContentTile for which to get a texture tile.
     *
     * @return TextureTile allocated for the given ContentTile, or {@code null} if no TextureTile has been allocated.
     */
    protected TextureTile getTextureTile(ContentTile tile)
    {
        TextureTile textureTile = this.textureTileMap.get(tile);
        if (textureTile != null && textureTile.currentTile.equals(tile))
        {
            return textureTile;
        }

        return null;
    }

    /**
     * Allocate a texture tile for a ContentTile. There are a limited number of texture tiles. If no tiles are free, the
     * least recently used texture tile will be reallocated.
     *
     * @param tile ScrollableTile for which to allocate a texture tile.
     *
     * @return TextureTile allocated for the ScrollableTile.
     */
    protected TextureTile allocateTextureTile(ContentTile tile)
    {
        // Sort the list of texture tiles so that we can find the one that is the least recently used.
        TextureTile[] timeOrderedEntries = new TextureTile[this.textureTiles.size()];
        Arrays.sort(this.textureTiles.toArray(timeOrderedEntries));

        // Grab the least recently used tile
        TextureTile textureTile = timeOrderedEntries[0];

        textureTile.currentTile = tile;
        this.textureTileMap.put(tile, textureTile);

        return textureTile;
    }

    /**
     * Draws the current list of ScrollableTiles into the texture tiles. The tiles are updated when necessary.
     *
     * @param dc         the draw context the tile relates to.
     * @param tile       the tile to update. A new texture tile will be allocated for the tile, if the tile does not
     *                   have a texture.
     * @param tileBounds bounds of the tile being updated, within the larger texture.
     */
    protected void updateTile(DrawContext dc, ContentTile tile, Rectangle tileBounds)
    {
        int x = tileBounds.x - tile.column * this.textureTileDimension;
        int y = tileBounds.y - this.contentSize.height + this.textureTileDimension * (tile.row + 1);
        Rectangle scrollBounds = new Rectangle(x, y, this.contentBounds.width, this.textureTileDimension);

        try
        {
            Texture texture = dc.getTextureCache().getTexture(this.textureCacheKey);

            this.rttSupport.setColorTarget(dc, texture);
            this.rttSupport.clear(dc, new Color(0, 0, 0, 0)); // Set all texture pixels to transparent black.

            this.contents.renderScrollable(dc, scrollBounds.getLocation(), scrollBounds.getSize(), tileBounds);
        }
        finally
        {
            this.rttSupport.setColorTarget(dc, null);
        }
    }

    /** Force all tiles to update on the next frame. */
    protected void forceTileUpdate()
    {
        for (ContentTile tile : tiles)
        {
            tile.updateTime = -1;
        }
    }

    /**
     * Returns a new tile texture with the specified width and height.
     * <p/>
     * The returned texture's internal format is RGBA8.
     *
     * @param width  the texture's width, in pixels.
     * @param height the texture's height, in pixels.
     *
     * @return a new texture with the specified width and height.
     */
    protected Texture createTileTexture(DrawContext dc, int width, int height)
    {
        GL gl = dc.getGL();

        TextureData td = new TextureData(
            gl.getGLProfile(),    // GL profile
            GL.GL_RGBA8,          // internal format
            width, height,        // dimension
            0,                    // border
            GL.GL_RGBA,           // pixel format
            GL.GL_UNSIGNED_BYTE,  // pixel type
            false,                // mipmap
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

        Texture t = TextureIO.newTexture(td);
        t.bind(gl);

        return t;
    }

    /**
     * Compute the dimension of a texture large enough to represent the amount of the contents visible in the frame.
     *
     * @param frameSize   Size of the frame content area.
     * @param contentSize Size of the frame content.
     *
     * @return Dimension of a texture large enough to render the full frame content area. This method always returns a
     *         power of two dimension.
     */
    protected int computeTileTextureDimension(Dimension frameSize, Dimension contentSize)
    {
        int width = Math.min(frameSize.width, contentSize.width);
        int height = Math.min(frameSize.height, contentSize.height);

        int area = width * height;

        return WWMath.powerOfTwoCeiling((int) Math.sqrt(area) + this.textureTileDimension);
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        Offset screenLocation = this.getScreenLocation();
        if (screenLocation == null)
            return;

        this.stepAnimation(dc);

        this.updateBounds(dc);

        // Highlight the frame if the pick point is within the frame's pickable bounds.
        Point pickPoint = dc.getPickPoint();
        if (pickPoint != null)
        {
            int glY = dc.getView().getViewport().height - pickPoint.y;
            this.setHighlighted(this.pickBounds.contains(new Point(pickPoint.x, glY)));
        }

        this.determineActiveAttributes();

        if (this.intersectsFrustum(dc) && this.mustUpdateTiles(dc))
        {
            try
            {
                this.beginDrawing(dc);
                this.updateTiles(dc);
            }
            finally
            {
                this.endDrawing(dc);
            }
        }
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        Offset screenLocation = this.getScreenLocation();
        if (screenLocation == null || this.frameBounds == null)
            return;

        if (this.mustRecomputeFrameGeometry)
        {
            this.computeFrameGeometry();
            this.mustRecomputeFrameGeometry = false;
        }

        if (this.intersectsFrustum(dc))
        {
            try
            {
                this.beginDrawing(dc);

                // While the tree is animated toward a minimized state, draw it as if it were maximized,
                // with the contents and scroll bars
                if (this.isDrawMinimized())
                    this.drawMinimized(dc);
                else
                    this.drawMaximized(dc);
            }
            finally
            {
                this.endDrawing(dc);
            }
        }
    }

    /** Initialize controls to resizing the frame, minimizing the frame, etc. */
    protected void initializeUIControls()
    {
        this.minimizeAnimation = new WindowShadeAnimation(this);
        this.frameResizeControl = new FrameResizeControl(this);

        this.minimizeButton = new TreeHotSpot(this)
        {
            @Override
            public void selected(SelectEvent event)
            {
                if (event == null || this.isConsumed(event))
                    return;

                if (event.isLeftClick())
                {
                    ScrollFrame.this.setMinimized(!ScrollFrame.this.isMinimized());
                    event.consume();
                }
                else
                {
                    super.selected(event);
                }
            }
        };

        this.verticalScrollBar = new ScrollBar(this, AVKey.VERTICAL);
        this.horizontalScrollBar = new ScrollBar(this, AVKey.HORIZONTAL);
    }

    /**
     * Determines whether the frame intersects the view frustum.
     *
     * @param dc the current draw context.
     *
     * @return {@code true} If the frame intersects the frustum, otherwise {@code false}.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(this.pickBounds);
        else
            return dc.getView().getViewport().intersects(this.frameBounds);
    }

    /**
     * Increment the active animation. Has no effect if there is no active animation. This method must be called once
     * per frame.
     *
     * @param dc Current draw context.
     */
    protected void stepAnimation(DrawContext dc)
    {
        if (this.isAnimating())
        {
            this.animation.step();

            if (this.animation.hasNext())
                dc.setRedrawRequested(this.animationDelay);
            else
                this.animation = null;
        }
    }

    /**
     * Get the bounds of a tile in the frame content.
     *
     * @param row    Row of the tile to get the bounds of.
     * @param column Column of the tile to get the bounds of.
     *
     * @return Bounds of the desired tile, relative to the lower left corner of {#link contentBounds}.
     */
    protected Rectangle getContentTileBounds(int row, int column)
    {
        int xScroll = this.horizontalScrollBar.getValue();
        int yScroll = this.verticalScrollBar.getValue();

        int tileScreenX = (int) this.contentBounds.getMinX() + this.textureTileDimension * column - xScroll;
        int tileScreenY = (int) this.contentBounds.getMaxY() - this.textureTileDimension * (row + 1) + yScroll;

        return new Rectangle(tileScreenX, tileScreenY, this.textureTileDimension, this.textureTileDimension);
    }

    /**
     * Compute the bounds of the content frame, and the extents of the scroll range. Calling this method updates the
     * frame and scroll model to match the size of the scrollable content. Bounds are only computed once per frame.
     * Multiple calls in the same frame will not change the computed bounds.
     *
     * @param dc Current draw context.
     */
    public void updateBounds(DrawContext dc)
    {
        if (dc.getFrameTimeStamp() == this.frameNumber)
            return;

        this.determineSize();

        Rectangle viewport = dc.getView().getViewport();

        Dimension contentSize = null;
        Dimension previousFrameSize = this.frameSize;

        Size size = this.getActiveSize();

        // If the frame size is relative to the content size, compute the content size and then set the frame size.
        if (this.isRelativeSize(size))
        {
            // Pass null for the frame bounds because the frame size depends on the content size.
            contentSize = this.contents.getSize(dc, null);
            Dimension frameSizeForContentSize = this.computeFrameRectForContentRect(contentSize);

            this.frameSize = size.compute(frameSizeForContentSize.width, frameSizeForContentSize.height,
                viewport.width, viewport.height);
        }
        else
        {
            // Otherwise just compute the frame size. The content size will be computed after the frame size has been
            // determined.
            this.frameSize = size.compute(0, 0, viewport.width, viewport.height);
        }

        // Apply the maximum size constraint
        if (this.getMaxSize() != null)
        {
            Dimension max = this.getMaxSize().compute(this.frameSize.width, this.frameSize.height, viewport.width,
                viewport.height);
            this.frameSize.width = Math.min(this.frameSize.width, max.width);
            this.frameSize.height = Math.min(this.frameSize.height, max.height);
        }

        // If the frame size has changed, the frame geometry must be regenerated
        if (!this.frameSize.equals(previousFrameSize))
            this.mustRecomputeFrameGeometry = true;

        // Compute point in OpenGL coordinates
        Point2D upperLeft = this.screenLocation.computeOffset(viewport.width, viewport.height, 1.0, 1.0);

        this.awtScreenPoint = new Point((int) upperLeft.getX(), (int) (viewport.height - upperLeft.getY()));

        this.frameBounds = new Rectangle((int) upperLeft.getX(), (int) upperLeft.getY() - this.frameSize.height,
            this.frameSize.width, this.frameSize.height);

        // Compute the pickable screen extent as the frame extent, plus the width of the frame's pickable outline.
        // This extent is used during picking to ensure that the frame's outline is pickable when it exceeds the
        // frame's screen extent.
        this.pickBounds = new Rectangle(
            this.frameBounds.x - this.borderPickWidth / 2,
            this.frameBounds.y - this.borderPickWidth / 2,
            this.frameBounds.width + this.borderPickWidth,
            this.frameBounds.height + this.borderPickWidth);

        this.innerBounds = new Rectangle((int) upperLeft.getX() + this.frameBorder,
            (int) upperLeft.getY() - frameSize.height + this.frameBorder, frameSize.width - this.frameBorder * 2,
            frameSize.height - this.frameBorder * 2);

        // If the content size has yet not been computed, compute it now.
        if (contentSize == null)
        {
            // Compute the bounds as if both scroll bars are visible. This saves us from having to compute the size
            // multiple times if scroll bars are required. If scroll bars are not required it may leave a little bit of
            // extra padding on the edges of the frame.
            contentSize = this.contents.getSize(dc, this.computeBounds(true, true).getSize());
        }

        // Computing the bounds of the content area of the frame requires computing the bounds with no scroll bars,
        // and determining if scroll bars are required given the size of the scrollable content.

        // Try laying out the frame without scroll bars
        this.contentBounds = this.computeBounds(false, false);

        // Determine if we need a vertical scroll bar
        boolean showVerticalScrollbar = this.mustShowVerticalScrollbar(contentSize);

        // If we need a vertical scroll bar, recompute the bounds because the scrollbar consumes horizontal space
        if (showVerticalScrollbar)
        {
            this.contentBounds = this.computeBounds(true, false);
        }

        // Determine if we need a horizontal scroll bar
        boolean showHorizontalScrollbar = this.mustShowHorizontalScrollbar(contentSize);

        // If we need a horizontal scroll bar, recompute the bounds because the horizontal scroll bar consumes vertical
        // space and a vertical scroll bar may now be required
        if (showHorizontalScrollbar && !showVerticalScrollbar)
        {
            this.contentBounds = this.computeBounds(showVerticalScrollbar, showHorizontalScrollbar);

            // Determine if we now need a vertical scroll bar
            showVerticalScrollbar = this.mustShowVerticalScrollbar(contentSize);
        }

        // Final bounds computation now that all the scroll bars are established
        this.contentBounds = this.computeBounds(showVerticalScrollbar, showHorizontalScrollbar);

        // If the scroll bars were visible and are now hidden, reset the scroll position to zero. Otherwise, the
        // scroll bar will already be scrolled to a certain position when it reappears, which is probably not what
        // the user expects.
        if (this.showVerticalScrollbar && !showVerticalScrollbar)
            this.verticalScrollBar.setValue(0);
        if (this.showHorizontalScrollbar && !showHorizontalScrollbar)
            this.horizontalScrollBar.setValue(0);

        this.showVerticalScrollbar = showVerticalScrollbar;
        this.showHorizontalScrollbar = showHorizontalScrollbar;

        // Set scroll bar ranges
        this.verticalScrollBar.setMaxValue(contentSize.height);
        this.verticalScrollBar.setExtent(this.contentBounds.height);

        this.horizontalScrollBar.setMaxValue(contentSize.width);
        this.horizontalScrollBar.setExtent(this.contentBounds.width);

        // Compute the bounds of the content based on the scroll position
        this.scrollContentBounds = new Rectangle(this.contentBounds);
        this.scrollContentBounds.x -= this.horizontalScrollBar.getValue();
        this.scrollContentBounds.y += this.verticalScrollBar.getValue();
        this.scrollContentBounds.y = this.scrollContentBounds.y - (contentSize.height - this.contentBounds.height);

        this.contentSize = contentSize;

        this.frameNumber = dc.getFrameTimeStamp();
    }

    /**
     * Compute the size of the frame rectangle required to accommodate a given content size without displaying scroll
     * bars.
     *
     * @param contentSize Size of the frame content.
     *
     * @return Frame size required to display the content without scrollbars.
     */
    protected Dimension computeFrameRectForContentRect(Dimension contentSize)
    {
        int frameWidth = contentSize.width + this.frameBorder * 2 + 4 * this.frameLineWidth + this.scrollBarSize;
        int frameHeight = contentSize.height + this.frameBorder * 2 + this.getTitleBarHeight()
            + 2 * this.frameLineWidth;

        return new Dimension(frameWidth, frameHeight);
    }

    /**
     * Determines if the frame size is relative to the size of the scrollable content (as opposed to an absolute pixel
     * value, or a fraction of the viewport).
     *
     * @param size Size to test.
     *
     * @return {@code true} if the absolute size of {@code size} depends on the size of the frame content.
     */
    // TODO try to eliminate this dependence on size modes. This would break if an app subclassed Size and implemented
    // TODO different modes.
    protected boolean isRelativeSize(Size size)
    {
        String heightMode = size.getHeightMode();
        String widthMode = size.getWidthMode();

        return Size.NATIVE_DIMENSION.equals(heightMode)
            || Size.MAINTAIN_ASPECT_RATIO.equals(heightMode)
            || Size.NATIVE_DIMENSION.equals(widthMode)
            || Size.MAINTAIN_ASPECT_RATIO.equals(widthMode);
    }

    /**
     * Determine if the vertical scrollbar should be displayed.
     *
     * @param contentSize The total size, in pixels, of the scrollable content.
     *
     * @return {@code true} if the vertical scrollbar should be displayed, otherwise {@code false}.
     */
    protected boolean mustShowVerticalScrollbar(Dimension contentSize)
    {
        // If the frame is not minimized or in the middle of an animation, compare the content size to the visible
        // bounds.
        if ((!this.isMinimized() && !this.isAnimating()))
        {
            return contentSize.height > this.contentBounds.height;
        }
        else
        {
            // Otherwise, return the previous scrollbar setting, do not recompute it. While the frame is animating, we want
            // the scrollbar decision to be based on its maximized size. If the frame would have scrollbars when maximized will
            // have scrollbars while it animates, but a frame that would not have scrollbars when maximized will not have
            // scrollbars while animating.
            return this.showVerticalScrollbar;
        }
    }

    /**
     * Determine if the horizontal scrollbar should be displayed.
     *
     * @param contentSize The total size, in pixels, of the scrollable content.
     *
     * @return {@code true} if the horizontal scrollbar should be displayed, otherwise {@code false}.
     */
    protected boolean mustShowHorizontalScrollbar(Dimension contentSize)
    {
        // Show a scroll bar if the content is large enough to require a scroll bar, and there is enough space to
        // draw the scroll bar.
        return contentSize.width > this.contentBounds.width
            && this.innerBounds.height > this.titleBarHeight + this.scrollBarSize;
    }

    /**
     * Determines if the frame is currently animating.
     *
     * @return {@code true} if an animation is in progress, otherwise {@code false}.
     */
    protected boolean isAnimating()
    {
        return this.animation != null;
    }

    /**
     * Compute the content bounds, taking into account the frame size and the presence of scroll bars.
     *
     * @param showVerticalScrollBar   True if the frame will have a vertical scroll bar. A vertical scroll bar will make
     *                                the content frame narrower.
     * @param showHorizontalScrollBar True if the frame will have a horizontal scroll bar. A horizontal scroll bar will
     *                                make the content frame shorter.
     *
     * @return The bounds of the content frame.
     */
    protected Rectangle computeBounds(boolean showVerticalScrollBar, boolean showHorizontalScrollBar)
    {
        int hScrollBarSize = (showHorizontalScrollBar ? this.scrollBarSize : 0);
        int vScrollBarSize = (showVerticalScrollBar ? this.scrollBarSize : 0);

        int titleBarHeight = this.isDrawTitleBar() ? this.titleBarHeight : 0;

        int inset = 2 * this.frameLineWidth;

        return new Rectangle(this.innerBounds.x + inset,
            this.innerBounds.y + hScrollBarSize + inset,
            this.innerBounds.width - vScrollBarSize - inset * 2,
            this.innerBounds.height - titleBarHeight - hScrollBarSize - inset);
    }

    /** Updates the frame's screen-coordinate geometry in {@link #vertexBuffer} according to the current screen bounds. */
    protected void computeFrameGeometry()
    {
        if (this.frameBounds == null)
            return;

        FrameAttributes attributes = this.getActiveAttributes();

        this.vertexBuffer = FrameFactory.createShapeBuffer(AVKey.SHAPE_RECTANGLE, this.frameBounds.width,
            this.frameBounds.height, attributes.getCornerRadius(), this.vertexBuffer);
    }

    /**
     * Get the smallest dimension that the frame can draw itself. This user is not allowed to resize the frame to be
     * smaller than this dimension.
     *
     * @return The frame's minimum size.
     */
    protected Dimension getMinimumSize()
    {
        // Reserve enough space to draw the border, both scroll bars, and the title bar
        int minWidth = this.frameBorder * 2 + this.scrollBarSize * 3; // left scroll arrow + right + vertical scroll bar
        int minHeight = this.frameBorder * 2 + this.scrollBarSize * 3
            + this.titleBarHeight; // Up arrow + down arrow + horizontal scroll bar
        return new Dimension(minWidth, minHeight);
    }

    /**
     * Determines if the frame should draw in its minimized form.
     *
     * @return {@code true} if the frame should draw minimized, otherwise {@code false}.
     */
    protected boolean isDrawMinimized()
    {
        // Draw minimized when the frame is minimized, but not while animating toward the minimized state
        return this.isMinimized() && !this.isAnimating();
    }

    /**
     * Draw the frame in its maximized state.
     *
     * @param dc Current draw context.
     */
    protected void drawMaximized(DrawContext dc)
    {
        this.drawFrame(dc);

        // Draw the contents using the cached texture, if we've rendered to a texture. Otherwise, just draw the
        // contents directly. Always draw the contents directly in picking mode because unique pick colors can't be
        // cached in a texture.
        if (this.renderToTexture && !dc.isPickingMode())
        {
            this.drawContentTiles(dc);
        }
        else
        {
            this.drawContentDirect(dc);
        }
    }

    /**
     * Draw the frame contents directly (not using previously generated tiles).
     *
     * @param dc Current draw context.
     */
    protected void drawContentDirect(DrawContext dc)
    {
        GL gl = dc.getGL();
        try
        {
            gl.glEnable(GL.GL_SCISSOR_TEST);
            gl.glScissor(this.contentBounds.x, this.contentBounds.y - 1, this.contentBounds.width + 1,
                this.contentBounds.height);

            this.contents.renderScrollable(dc, this.scrollContentBounds.getLocation(),
                this.scrollContentBounds.getSize(), this.contentBounds);
        }
        finally
        {
            gl.glDisable(GL.GL_SCISSOR_TEST);
        }
    }

    /**
     * Draw the frame contents using previously build texture tiles.
     *
     * @param dc Current draw context.
     */
    protected void drawContentTiles(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        try
        {
            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            gl.glEnable(GL.GL_TEXTURE_2D);

            // Set up blending with pre-multiplied colors
            OGLUtil.applyBlending(gl, true);

            Texture texture = dc.getTextureCache().getTexture(this.textureCacheKey);
            if (texture == null)
                return;

            texture.bind(gl);

            for (ContentTile tile : tiles)
            {
                TextureTile textureTile = this.getTextureTile(tile);
                if (textureTile == null)
                {
                    continue;
                }

                int tileX = textureTile.column * this.textureTileDimension;
                int tileY = textureTile.row * this.textureTileDimension;

                Rectangle tileScreenBounds = this.getContentTileBounds(tile.row, tile.column);
                Rectangle clippedTileBounds = tileScreenBounds.intersection(this.contentBounds);

                // If the tile is not visible in the content area, don't bother drawing it.
                if (clippedTileBounds.isEmpty())
                {
                    continue;
                }

                Rectangle subTileBounds = new Rectangle(tileX + clippedTileBounds.x - tileScreenBounds.x,
                    tileY + clippedTileBounds.y - tileScreenBounds.y, clippedTileBounds.width,
                    clippedTileBounds.height);

                gl.glPushMatrix();
                try
                {
                    gl.glTranslated(clippedTileBounds.x, clippedTileBounds.y, 0.0f);

                    gl.glColor4f(1, 1, 1, (float) this.getActiveAttributes().getForegroundOpacity());

                    TextureCoords texCoords = texture.getSubImageTexCoords((int) subTileBounds.getMinX(),
                        (int) subTileBounds.getMinY(), (int) subTileBounds.getMaxX(), (int) subTileBounds.getMaxY());
                    gl.glScaled(subTileBounds.width, subTileBounds.height, 1d);
                    dc.drawUnitQuad(texCoords);
                }
                finally
                {
                    gl.glPopMatrix();
                }
            }
        }
        finally
        {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
    }

    /**
     * Draw the frame in its minimized state.
     *
     * @param dc Current draw context.
     */
    protected void drawMinimized(DrawContext dc)
    {
        this.drawFrame(dc);
    }

    /**
     * Draw the frame, scroll bars, and title bar.
     *
     * @param dc Current draw context.
     */
    protected void drawFrame(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushModelviewIdentity(gl);

            FrameAttributes attributes = this.getActiveAttributes();

            gl.glTranslated(this.frameBounds.x, this.frameBounds.y, 0.0);

            boolean drawHorizontalScrollbar = this.showHorizontalScrollbar;
            boolean drawVerticalScrollbar = this.showVerticalScrollbar;

            if (!dc.isPickingMode())
            {
                Color[] color = attributes.getBackgroundColor();

                try
                {
                    gl.glEnable(GL.GL_LINE_SMOOTH);

                    OGLUtil.applyColor(gl, color[0], 1.0, false);
                    gl.glLineWidth(this.frameLineWidth);
                    FrameFactory.drawBuffer(dc, GL.GL_LINE_STRIP, this.vertexBuffer);
                }
                finally
                {
                    gl.glDisable(GL.GL_LINE_SMOOTH);
                }

                gl.glLoadIdentity();
                gl.glTranslated(this.innerBounds.x, this.innerBounds.y, 0.0); // Translate back inner frame

                TreeUtil.drawRectWithGradient(gl, new Rectangle(0, 0, this.innerBounds.width, this.innerBounds.height),
                    color[0], color[1], attributes.getBackgroundOpacity(), AVKey.VERTICAL);
            }
            else
            {
                int frameHeight = this.frameBounds.height;
                int frameWidth = this.frameBounds.width;

                // Draw draggable frame
                TreeUtil.drawPickableRect(dc, this.pickSupport, this, new Rectangle(0, 0, frameWidth, frameHeight));

                if (this.isEnableResizeControl() && !this.isDrawMinimized())
                {
                    Color color = dc.getUniquePickColor();
                    int colorCode = color.getRGB();
                    this.pickSupport.addPickableObject(colorCode, this.frameResizeControl);
                    gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                    // Draw the resize control as a pickable line on the frame border
                    gl.glLineWidth(this.borderPickWidth);
                    FrameFactory.drawBuffer(dc, GL.GL_LINE_STRIP, this.vertexBuffer);
                }

                gl.glLoadIdentity();
                gl.glTranslated(this.innerBounds.x, this.innerBounds.y, 0.0); // Translate back inner frame

                // If both scroll bars are visible, draw the empty square in the lower right hand corner as a part of
                // the resize control, pickable area.
                if (drawVerticalScrollbar && drawHorizontalScrollbar && !this.isDrawMinimized())
                {
                    gl.glRecti(this.innerBounds.width - this.scrollBarSize, 0,
                        this.innerBounds.width, this.scrollBarSize);
                }
            }

            if (!this.isDrawMinimized())
                this.drawScrollBars(dc);

            // Draw title bar
            if (this.isDrawTitleBar())
            {
                gl.glTranslated(0, this.innerBounds.height - this.titleBarHeight, 0);
                this.drawTitleBar(dc);
            }

            // Draw a thin border outlining the filled rectangle that is the frame background.
            if (!dc.isPickingMode())
            {
                gl.glLoadIdentity();

                int minX = (int) this.innerBounds.getMinX();
                int minY = (int) this.innerBounds.getMinY();
                int maxX = (int) this.innerBounds.getMaxX();
                int maxY = (int) this.innerBounds.getMaxY();

                OGLUtil.applyColor(gl, attributes.getForegroundColor(), false);

                // Do not draw the outline on the edges with scroll bars because the scrollbar draws its own border. On
                // some devices the scroll bar border draws next to the frame border, resulting in a double width border.
                gl.glBegin(GL2.GL_LINE_STRIP);
                try
                {
                    if (!drawVerticalScrollbar)
                        gl.glVertex2f(maxX, minY + 0.5f);

                    gl.glVertex2f(maxX, maxY);
                    gl.glVertex2f(minX + 0.5f, maxY);
                    gl.glVertex2f(minX + 0.5f, minY + 0.5f);

                    if (!drawHorizontalScrollbar)
                        gl.glVertex2f(maxX, minY + 0.5f);
                }
                finally
                {
                    gl.glEnd();
                }
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    /**
     * Draw visible scroll bars for the frame. Scroll bars that are not visible will not be drawn.
     *
     * @param dc Current draw context.
     */
    protected void drawScrollBars(DrawContext dc)
    {
        // Draw a vertical scroll bar if the tree extends beyond the visible bounds
        if (this.showVerticalScrollbar)
        {
            int x1 = this.innerBounds.width - this.scrollBarSize;
            int y1 = 1;
            if (this.showHorizontalScrollbar)
                y1 += this.scrollBarSize;

            Rectangle scrollBarBounds = new Rectangle(x1, y1, this.scrollBarSize, this.contentBounds.height + 1);

            this.verticalScrollBar.setBounds(scrollBarBounds);
            this.verticalScrollBar.render(dc);
        }

        // Draw a horizontal scroll bar if the tree extends beyond the visible bounds
        if (this.showHorizontalScrollbar)
        {
            int x1 = 1;
            int y1 = 1;
            int width = this.innerBounds.width - 1;
            if (this.showVerticalScrollbar)
                width -= this.scrollBarSize;

            Rectangle scrollBarBounds = new Rectangle(x1, y1, width, this.scrollBarSize);

            this.horizontalScrollBar.setBounds(scrollBarBounds);
            this.horizontalScrollBar.render(dc);
        }
    }

    /**
     * Draw the title bar.
     *
     * @param dc Draw context
     */
    protected void drawTitleBar(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        FrameAttributes attributes = this.getActiveAttributes();

        if (!dc.isPickingMode())
        {
            // Draw title bar as a rectangle with gradient
            Color[] color = attributes.getTitleBarColor();
            TreeUtil.drawRectWithGradient(gl, new Rectangle(0, 0, this.innerBounds.width, this.getTitleBarHeight()),
                color[0], color[1], attributes.getBackgroundOpacity(), AVKey.VERTICAL);

            OGLUtil.applyColor(gl, attributes.getForegroundColor(), 1.0, false);

            if (!this.isDrawMinimized())
            {
                // Draw a line to separate the title bar from the frame
                gl.glBegin(GL2.GL_LINES);
                try
                {
                    gl.glVertex2f(0, 0);
                    gl.glVertex2f(this.innerBounds.width, 0);
                }
                finally
                {
                    gl.glEnd();
                }
            }

            Point drawPoint = new Point(0, 0);

            this.drawIcon(dc, drawPoint);
            this.drawTitleText(dc, drawPoint);
        }

        this.drawMinimizeButton(dc);
    }

    /**
     * Draw an icon in the upper left corner of the title bar. This method takes a point relative to lower left corner
     * of the title bar. This point is modified to indicate how much horizontal space is consumed by the icon.
     *
     * @param dc        Draw context
     * @param drawPoint Point at which to draw the icon. This point is relative to the lower left corner of the title
     *                  bar. This point will be modified to indicate how much horizontal space was consumed by drawing
     *                  the icon. After drawing the icon, the x value with point to the first available space to the
     *                  right of the icon.
     */
    protected void drawIcon(DrawContext dc, Point drawPoint)
    {
        // This method is never called during picked, so picking mode is not handled here

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        FrameAttributes attributes = this.getActiveAttributes();

        int iconSpace = attributes.getIconSpace();

        // Draw icon in upper left corner
        BasicWWTexture texture = this.getTexture();
        if (texture == null)
        {
            drawPoint.x += iconSpace;
            return;
        }

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            if (texture.bind(dc))
            {
                gl.glEnable(GL.GL_TEXTURE_2D);

                Dimension iconSize = attributes.getIconSize();

                oglStack.pushModelview(gl);

                gl.glColor4d(1.0, 1.0, 1.0, 1.0);

                double vertAdjust = (this.titleBarHeight - iconSize.height) / 2;
                TextureCoords texCoords = texture.getTexCoords();
                gl.glTranslated(drawPoint.x + iconSpace, drawPoint.y + vertAdjust + 1, 1.0);
                gl.glScaled((double) iconSize.width, (double) iconSize.height, 1d);
                dc.drawUnitQuad(texCoords);

                drawPoint.x += iconSize.getWidth() + iconSpace * 2;
            }
        }
        finally
        {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            oglStack.pop(gl);
        }
    }

    /**
     * Draw text in the frame title.
     *
     * @param dc        Draw context
     * @param drawPoint Point at which to draw text. This point is relative to the lower left corner of the title bar.
     */
    protected void drawTitleText(DrawContext dc, Point drawPoint)
    {
        // This method is never called during picked, so picking mode is not handled here

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        FrameAttributes attributes = this.getActiveAttributes();

        String frameTitle = this.getFrameTitle();
        if (frameTitle == null)
            return;

        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            attributes.getFont());

        // Determine if the shortened frame title needs to be regenerated. If so, generate it now.
        int titleAreaWidth = this.innerBounds.width - this.buttonSize - drawPoint.x - attributes.getIconSpace();
        if (this.mustGenerateShortTitle(attributes.getFont(), titleAreaWidth))
        {
            this.generateShortTitle(dc, frameTitle, titleAreaWidth, "...");
        }

        if (this.shortTitle == null)
            return;

        try
        {
            textRenderer.begin3DRendering();
            OGLUtil.applyColor(gl, attributes.getTextColor(), 1.0, false);

            double vertAdjust = (this.titleBarHeight - Math.abs(this.shortTitleBounds.getY())) / 2;
            textRenderer.draw(this.shortTitle, drawPoint.x, (int) (drawPoint.y + vertAdjust) + 1);
        }
        finally
        {
            textRenderer.end3DRendering();
        }
    }

    /**
     * Determine if a the shortened frame title needs to be regenerated.
     *
     * @param titleFont      Title font.
     * @param titleAreaWidth Width in pixels of the frame title area.
     *
     * @return {@code true} if the shortened title needs to be regenerated, otherwise {@code false}.
     */
    protected boolean mustGenerateShortTitle(Font titleFont, int titleAreaWidth)
    {
        return this.shortTitle == null
            || !titleFont.equals(this.shortFrameTitleFont)
            || titleAreaWidth != this.frameTitleWidth;
    }

    /**
     * Generate a shortened version of the frame title that will fit in the frame title area. If the frame is wide
     * enough to accommodate the full title, then the short title will be the same as the full title. The method sets
     * the fields {@link #shortTitle}, {@link #shortFrameTitleFont}, {@link #frameTitleWidth}.
     *
     * @param dc         Current draw context.
     * @param frameTitle Full frame title.
     * @param width      Width in pixels of the frame title area.
     * @param cutOff     String to append to title to indicate that text has been cut off due to a small frame. For
     *                   example, if the cut off string is "...", the string "Frame Title" might be shortened to "Frame
     *                   T...".
     */
    protected void generateShortTitle(DrawContext dc, String frameTitle, int width, String cutOff)
    {
        Font font = this.getActiveAttributes().getFont();

        // Keep track of the font and width used to generate the title so that we can invalidate the generated
        // title if the font or size change.
        this.shortFrameTitleFont = font;
        this.frameTitleWidth = width;

        if (frameTitle == null)
        {
            this.shortTitle = null;
            return;
        }

        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);

        // Check to see if the frame is wide enough to display the entire title
        Rectangle2D size = textRenderer.getBounds(frameTitle);
        if (size.getWidth() < width)
        {
            this.shortTitle = frameTitle; // No truncation required
            this.shortTitleBounds = size;
            return;
        }

        // Check to see if the frame is too small to display even the continuation
        Rectangle2D ellipseSize = textRenderer.getBounds(cutOff);
        if (width < ellipseSize.getWidth())
        {
            this.shortTitle = null; // Frame too small
            this.shortTitleBounds = null;
            return;
        }

        // Starting at the end of the string, remove characters until the string fits
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < frameTitle.length(); i++)
        {
            sb.append(frameTitle.charAt(i));
            Rectangle2D bounds = textRenderer.getBounds(sb);
            if (bounds.getWidth() + ellipseSize.getWidth() > width)
            {
                sb.deleteCharAt(sb.length() - 1);
                sb.append("...");
                break;
            }
        }

        // Make sure that the computed string contains at least one character of the original title. If not, don't
        // show any text.
        if (sb.length() > cutOff.length())
        {
            this.shortTitle = sb.toString();
            this.shortTitleBounds = textRenderer.getBounds(sb);
        }
        else
        {
            this.shortTitle = null;
            this.shortTitleBounds = null;
        }
    }

    /**
     * Draw the minimize/maximize button in the frame title bar.
     *
     * @param dc Current draw context.
     */
    protected void drawMinimizeButton(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        OGLStackHandler oglStack = new OGLStackHandler();
        try
        {
            oglStack.pushModelviewIdentity(gl);

            int x = (int) this.innerBounds.getMaxX() - this.getActiveAttributes().getIconSpace() - this.buttonSize;
            int y = (int) this.innerBounds.getMaxY() - (this.titleBarHeight - this.buttonSize) / 2 - this.buttonSize;
            gl.glTranslated(x, y, 0.0);

            if (!dc.isPickingMode())
            {
                Color color = this.getActiveAttributes().getMinimizeButtonColor();

                FrameAttributes attributes = this.getActiveAttributes();
                OGLUtil.applyColor(gl, color, attributes.getForegroundOpacity(), false);
                gl.glRectf(0, 0, buttonSize, buttonSize);

                OGLUtil.applyColor(gl, attributes.getForegroundColor(), false);

                gl.glBegin(GL2.GL_LINE_LOOP);
                try
                {
                    gl.glVertex2f(0f, 0f);
                    gl.glVertex2f(0.5f, buttonSize + 0.5f);
                    gl.glVertex2f(buttonSize, buttonSize + 0.5f);
                    gl.glVertex2f(buttonSize, 0);
                }
                finally
                {
                    gl.glEnd();
                }

                gl.glBegin(GL2.GL_LINES);
                try
                {
                    // Draw a horizontal line. If the frame is maximized, this will be a minus sign. If the tree is
                    // minimized, this will be part of a plus sign.
                    gl.glVertex2f(buttonSize / 4f, buttonSize / 2f);
                    gl.glVertex2f(buttonSize - buttonSize / 4f, buttonSize / 2f);

                    // Draw a vertical line to complete the plus sign if the frame is minimized.
                    if (this.isMinimized())
                    {
                        gl.glVertex2f(buttonSize / 2f, buttonSize / 4f);
                        gl.glVertex2f(buttonSize / 2f, buttonSize - buttonSize / 4f);
                    }
                }
                finally
                {
                    gl.glEnd();
                }
            }
            else
            {
                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();

                this.pickSupport.addPickableObject(colorCode, this.minimizeButton, null, false);
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                gl.glScaled(buttonSize, buttonSize, 1d);
                dc.drawUnitQuad();
            }
        }
        finally
        {
            oglStack.pop(gl);
        }
    }

    protected void beginDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        GLU glu = dc.getGLU();

        this.BEogsh.pushAttrib(gl, GL2.GL_DEPTH_BUFFER_BIT
            | GL2.GL_COLOR_BUFFER_BIT
            | GL2.GL_ENABLE_BIT
            | GL2.GL_CURRENT_BIT
            | GL2.GL_POLYGON_BIT     // For polygon mode
            | GL2.GL_LINE_BIT        // For line width
            | GL2.GL_TRANSFORM_BIT
            | GL2.GL_SCISSOR_BIT);

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL.GL_DEPTH_TEST);

        // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
        // into the GL projection matrix.
        this.BEogsh.pushProjectionIdentity(gl);

        java.awt.Rectangle viewport = dc.getView().getViewport();

        glu.gluOrtho2D(0d, viewport.width, 0d, viewport.height);
        this.BEogsh.pushModelviewIdentity(gl);

        if (dc.isPickingMode())
        {
            this.pickSupport.clearPickList();
            this.pickSupport.beginPicking(dc);
        }
    }

    protected void endDrawing(DrawContext dc)
    {
        if (dc.isPickingMode())
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, dc.getPickPoint(), dc.getCurrentLayer());
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.BEogsh.pop(gl);
    }

    /**
     * Get the currently active frame attributes.
     *
     * @return attributes that are active for this frame.
     */
    protected FrameAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    /** Determines which attributes -- normal, highlight or default -- to use each frame. */
    protected void determineActiveAttributes()
    {
        if (this.isHighlighted())
        {
            if (this.getHighlightAttributes() != null)
                this.activeAttributes.copy(this.getHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we will use the normal attributes.
                if (this.getAttributes() != null)
                    this.activeAttributes.copy(this.getAttributes());
                else
                    this.activeAttributes.copy(defaultAttributes);
            }
        }
        else if (this.getAttributes() != null)
        {
            this.activeAttributes.copy(this.getAttributes());
        }
        else
        {
            this.activeAttributes.copy(defaultAttributes);
        }

        this.determineScrollbarAttributes();
    }

    /** Update the attributes of the scroll bars to match the frame's highlight state. */
    protected void determineScrollbarAttributes()
    {
        this.verticalScrollBar.setLineColor(this.activeAttributes.getForegroundColor());
        this.verticalScrollBar.setOpacity(this.activeAttributes.getBackgroundOpacity());
        this.horizontalScrollBar.setLineColor(this.activeAttributes.getForegroundColor());
        this.horizontalScrollBar.setOpacity(this.activeAttributes.getBackgroundOpacity());

        Color[] scrollBarColor = this.activeAttributes.getScrollBarColor();
        this.horizontalScrollBar.setKnobColor(scrollBarColor[0], scrollBarColor[1]);
        this.verticalScrollBar.setKnobColor(scrollBarColor[0], scrollBarColor[1]);
    }

    /**
     * Indicates the size that applies to the frame for this frame, either the maximumed or the minimized size.
     *
     * @return the frame size for the duration of this frame.
     */
    protected Size getActiveSize()
    {
        return this.activeSize;
    }

    /** Determine the frame size to use for the current frame. */
    protected void determineSize()
    {
        // Use the minimized size if the frame is minimized or animating to or from the minimized state.
        if ((this.isMinimized() || this.isAnimating()) && this.minimizedSize != null)
        {
            this.activeSize = this.minimizedSize;
        }
        else
        {
            this.activeSize = this.maximizedSize;
        }
    }

    /**
     * Get the texture loaded for the icon image source. If the texture has not been loaded, this method will attempt to
     * load it in the background.
     *
     * @return The icon texture, or no image source has been set, or if the icon has not been loaded yet.
     */
    protected BasicWWTexture getTexture()
    {
        if (this.texture != null)
            return this.texture;
        else
            return this.initializeTexture();
    }

    /**
     * Create and initialize the texture from the image source. If the image is not in memory this method will request
     * that it be loaded and return null.
     *
     * @return The texture, or null if the texture is not yet available.
     */
    protected BasicWWTexture initializeTexture()
    {
        Object imageSource = this.getIconImageSource();
        if (imageSource instanceof String || imageSource instanceof URL)
        {
            URL imageURL = WorldWind.getDataFileStore().requestFile(imageSource.toString());
            if (imageURL != null)
            {
                this.texture = new BasicWWTexture(imageURL, true);
            }
        }
        else if (imageSource != null)
        {
            this.texture = new BasicWWTexture(imageSource, true);
            return this.texture;
        }

        return null;
    }

    /**
     * Get a reference to one of the frame's scroll bars.
     *
     * @param direction Determines which scroll bar to get. Either {@link AVKey#VERTICAL} or {@link AVKey#HORIZONTAL}.
     *
     * @return The horizontal or vertical scroll bar.
     */
    public ScrollBar getScrollBar(String direction)
    {
        if (AVKey.VERTICAL.equals(direction))
            return this.verticalScrollBar;
        else
            return this.horizontalScrollBar;
    }

    @Override
    protected void beginDrag(Point point)
    {
        if (this.isEnableMove())
        {
            Point2D location = this.awtScreenPoint;
            this.dragRefPoint = new Point((int) location.getX() - point.x, (int) location.getY() - point.y);
        }
    }

    public void drag(Point point)
    {
        if (this.isEnableMove())
        {
            double x = point.x + this.dragRefPoint.x;
            double y = point.y + this.dragRefPoint.y;
            this.setScreenLocation(new Offset(x, y, AVKey.PIXELS, AVKey.INSET_PIXELS));
        }
    }

    @Override
    public void selected(SelectEvent event)
    {
        if (event == null || this.isConsumed(event))
            return;

        super.selected(event);

        // Minimize the frame if the title bar was double clicked.
        Rectangle titleBarBounds = new Rectangle((int) this.awtScreenPoint.getX() + this.frameBorder,
            (int) this.awtScreenPoint.getY() + this.frameBorder * 2, this.innerBounds.width, this.titleBarHeight);

        if (event.isLeftDoubleClick())
        {
            Point pickPoint = event.getPickPoint();
            if (pickPoint != null && titleBarBounds.contains(event.getPickPoint()))
            {
                this.setMinimized(!this.isMinimized());
                event.consume();
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (e == null || e.isConsumed())
            return;

        // Java on Mac OS X implements support for horizontal scrolling by sending a Shift+ScrollWheel event. This is
        // not the case for Java for other platforms, so we handle the scrolling logic for Mac OS X
        if (Configuration.isMacOS())
        {
            this.doScrollMacOS(e);
        }
        else
        {
            this.doScroll(e);
        }

        e.consume();

        // Fire a property change to trigger a repaint
        this.firePropertyChange(AVKey.REPAINT, null, this);
    }

    /**
     * Handle a mouse wheel event.
     *
     * @param e Mouse event that triggered the scroll.
     */
    protected void doScroll(MouseWheelEvent e)
    {
        // Determine whether to scroll horizontally or vertically by giving priority to the vertical scroll bar. Scroll
        // vertically if only both scroll bars are active or only the vertical scroll bar is active. Scroll horizontally
        // if only the horizontal scroll bar is active.
        if (this.showVerticalScrollbar)
        {
            this.verticalScrollBar.scroll(e.getUnitsToScroll() * this.getMouseWheelScrollUnit(AVKey.VERTICAL));
        }
        else if (this.showHorizontalScrollbar)
        {
            this.horizontalScrollBar.scroll(e.getUnitsToScroll() * this.getMouseWheelScrollUnit(AVKey.HORIZONTAL));
        }
    }

    /**
     * Handle a mouse wheel event on Mac OS X. This method correctly handles the Magic Mouse and Magic Trackpad devices,
     * which support horizontal scrolling.
     *
     * @param e Mouse event that triggered the scroll.
     */
    protected void doScrollMacOS(MouseWheelEvent e)
    {
        // On Mac OS X, Java always scrolls horizontally when the Shift key is down. This policy is used to support the
        // Magic Mouse and Magic Trackpad devices. When the user scroll horizontally on either of these devices, Java
        // automatically sends a Shift+ScrollWheel event, regardless of whether the Shift key is actually down. See
        // Radar #4631846 in
        // http://developer.apple.com/library/mac/#releasenotes/Java/JavaLeopardRN/ResolvedIssues/ResolvedIssues.html.
        if (e.isShiftDown())
        {
            this.horizontalScrollBar.scroll(e.getUnitsToScroll() * this.getMouseWheelScrollUnit(AVKey.HORIZONTAL));
        }
        // If the Shift key is not down, Java Mac OS X implements the standard scrolling policy used by Java on other 
        // operating systems.
        else
        {
            this.doScroll(e);
        }
    }

    /**
     * Get the scroll unit that the mouse wheel scrolls by.
     *
     * @param direction Direction of scroll.
     *
     * @return The scroll unit that will be applied when the mouse wheel is moved.
     */
    protected int getMouseWheelScrollUnit(String direction)
    {
        return (int) (this.getScrollBar(direction).getBlockIncrement() * 0.25);
    }

    /**
     * A tile in the frame content. This class represents one tile in the frame contents, and the time at which that
     * tile was last drawn to texture tile.
     */
    class ContentTile
    {
        /** Row in the frame content. */
        int row;
        /** Column in the frame content. */
        int column;
        /** Time at which this content tile was last drawn to a texture tile. */
        long updateTime;

        /**
         * Create new content tile.
         *
         * @param row    Row of this tile in the frame content.
         * @param column Column of this tile in the frame content.
         */
        ContentTile(int row, int column)
        {
            this.row = row;
            this.column = column;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            ContentTile that = (ContentTile) o;

            if (this.row != that.row)
                return false;
            //noinspection RedundantIfStatement
            if (this.column != that.column)
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result;

            result = this.row;
            result = 31 * result + this.column;

            return result;
        }
    }

    /** A region of the backing texture used to render one tile of the scrollable content. */
    class TextureTile implements Comparable<TextureTile>
    {
        /** Row of this tile in the frame's backing texture. */
        int row;
        /** Column of this tile in the frame's backing texture. */
        int column;
        /** The content tile currently rendered in this texture tile. */
        ContentTile currentTile;
        /** The last time that this tile was accessed. Used to implement an LRU tile replacement scheme. */
        long lastUsed;

        /**
         * Create a new texture tile.
         *
         * @param row    Row of the tile in the frame's backing texture.
         * @param column Column of the tile in the frame's backing texture.
         */
        TextureTile(int row, int column)
        {
            this.row = row;
            this.column = column;
        }

        /**
         * Compare two TextureTiles by the time that the tiles were last accessed.
         *
         * @param that Tile to compare with.
         *
         * @return -1 if this tile was accessed less recently than that tile, 0 if the access times are the same, or 1
         *         if this tile was accessed more recently.
         */
        public int compareTo(TextureTile that)
        {
            if (that == null)
            {
                String msg = Logging.getMessage("nullValue.CacheEntryIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            return this.lastUsed < that.lastUsed ? -1 : this.lastUsed == that.lastUsed ? 0 : 1;
        }
    }
}
