/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Abstract implementation of SurfaceObject that participates in the {@link gov.nasa.worldwind.SceneController}'s bulk
 * rendering of SurfaceObjects. The SceneControllers bulk renders all SurfaceObjects added to the {@link
 * gov.nasa.worldwind.render.DrawContext}'s ordered surface renderable queue during the preRendering pass. While
 * building the composite representation the SceneController invokes {@link #render(DrawContext)} in ordered rendering
 * mode. To avoid overloading the purpose of the render method, AbstractSurfaceObject does not add itself to the
 * DrawContext's ordered surface renderable queue during rendering.
 * <p>
 * Subclasses that do not wish to participate in this composite representation can override this behavior as follows:
 * <ol> <li>Override {@link #makeOrderedPreRenderable(DrawContext)}; do not add this object to the draw context's
 * ordered renderable queue. Perform any preRender processing necessary for the subclass to pick and render itself.</li>
 * <li>Override {@link #pickOrderedRenderable(DrawContext, PickSupport)}; draw the custom pick representation.</li>
 * <li>Override {@link #makeOrderedRenderable(DrawContext)}; add this object to the draw context's ordered renderable
 * queue. AbstractSurfaceObject does not add itself to this queue during rendering. render() is called from the
 * SceneController while building the composite representation because AbstractSurfaceObject adds itself to the ordered
 * surface renderable queue during preRendering.</li> <li>Override {@link #drawOrderedRenderable(DrawContext)}; draw the
 * custom representation. Unlike AbstractSurfaceObject subclasses should assume the modelview and projection matrices
 * are consistent with the current {@link gov.nasa.worldwind.View}.</li> </ol>
 *
 * @author dcollins
 * @version $Id: AbstractSurfaceObject.java 3240 2015-06-22 23:38:49Z tgaskins $
 */
public abstract class AbstractSurfaceObject extends WWObjectImpl implements SurfaceObject
{
    // Public interface properties.
    protected boolean visible;
    protected final long uniqueId;
    protected long lastModifiedTime;
    protected Object delegateOwner;
    protected boolean enableBatchPicking;
    protected boolean drawBoundingSectors;
    protected Map<Object, CacheEntry> extentCache = new HashMap<Object, CacheEntry>();
    // Picking properties.
    protected Layer pickLayer;
    protected PickSupport pickSupport = new PickSupport();
    /** Support class used to build surface tiles used to draw the pick representation. */
    protected SurfaceObjectTileBuilder pickTileBuilder;
    /* The next unique ID. This property is shared by all instances of AbstractSurfaceObject. */
    protected static long nextUniqueId = 1;

    /**
     * Creates a new AbstractSurfaceObject, assigning it a unique ID and initializing its last modified time to the
     * current system time.
     */
    public AbstractSurfaceObject()
    {
        this.visible = true;
        this.uniqueId = nextUniqueId();
        this.lastModifiedTime = System.currentTimeMillis();
        this.enableBatchPicking = true;
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public AbstractSurfaceObject(AbstractSurfaceObject source)
    {
        super(source);

        this.visible = source.visible;
        this.uniqueId = nextUniqueId();
        this.lastModifiedTime = System.currentTimeMillis();
        this.enableBatchPicking = source.enableBatchPicking;
    }

    /**
     * Returns the next unique integer associated with an AbstractSurfaceObject. This method is synchronized to ensure
     * that two threads calling simultaneously receive different values. Since this method is called from
     * AbstractSurfaceObject's constructor, this is critical to ensure that AbstractSurfaceObject can be safely
     * constructed on separate threads.
     *
     * @return the next unique integer.
     */
    protected static synchronized long nextUniqueId()
    {
        return nextUniqueId++;
    }

    /** {@inheritDoc} */
    public boolean isVisible()
    {
        return this.visible;
    }

    /** {@inheritDoc} */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
        this.updateModifiedTime();
    }

    /** {@inheritDoc} */
    public Object getStateKey(DrawContext dc)
    {
        return new SurfaceObjectStateKey(this.getUniqueId(), this.lastModifiedTime);
    }

    /** {@inheritDoc} */
    public double getDistanceFromEye()
    {
        return 0;
    }

    /** {@inheritDoc} */
    public Object getDelegateOwner()
    {
        return this.delegateOwner;
    }

    /** {@inheritDoc} */
    public void setDelegateOwner(Object owner)
    {
        this.delegateOwner = owner;
    }

    /**
     * Indicates whether the SurfaceObject draws its bounding sector.
     *
     * @return <code>true</code> if the shape draws its outline; <code>false</code> otherwise.
     *
     * @see #setDrawBoundingSectors(boolean)
     */
    public boolean isDrawBoundingSectors()
    {
        return this.drawBoundingSectors;
    }

    /**
     * Specifies if the SurfaceObject should draw its bounding sector. If <code>true</code>, the SurfaceObject draws an
     * outline of its bounding sector in green on top of its shape. The default value is <code>false</code>.
     *
     * @param draw <code>true</code> to draw the shape's outline; <code>false</code> otherwise.
     */
    public void setDrawBoundingSectors(boolean draw)
    {
        this.drawBoundingSectors = draw;
        // Update the modified time so the object's composite representation is updated. We draw the bounding sector
        // along with the object's composite representation.
        this.updateModifiedTime();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnableBatchPicking()
    {
        return this.enableBatchPicking;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableBatchPicking(boolean enable)
    {
        this.enableBatchPicking = enable;
    }

    /** {@inheritDoc} */
    public Extent getExtent(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        CacheEntry entry = this.extentCache.get(dc.getGlobe().getGlobeStateKey());
        if (entry != null)
        {
            return (Extent) entry.object;
        }
        else
        {
            entry = new CacheEntry(this.computeExtent(dc), dc);
            this.extentCache.put(dc.getGlobe().getGlobeStateKey(), entry);
            return (Extent) entry.object;
        }
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isVisible())
            return;

        // Ordered pre-rendering is a no-op for this object. The SceneController prepares a composite representation of
        // this object for rendering and calls this object's render method when doing so.
        if (!dc.isOrderedRenderingMode())
            this.makeOrderedPreRenderable(dc);
    }

    /** {@inheritDoc} */
    public void pick(DrawContext dc, Point pickPoint)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // This method is called only during ordered picking. Therefore we setup for picking and draw this object to the
        // framebuffer in a unique pick color. We invoke a separate path for picking because this object creates and
        // draws a separate representation of itself during picking. Using a separate call stack enables us to use
        // common rendering code to draw both the pick and render representations, by setting the draw context's 
        // isPickingMode flag to control which representation is drawn.

        if (!this.isVisible())
            return;

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.pickOrderedRenderable(dc, this.pickSupport);

            if (this.isEnableBatchPicking())
                this.pickBatched(dc, this.pickSupport);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isVisible())
            return;

        if (dc.isOrderedRenderingMode())
            this.drawOrderedRenderable(dc);
        else
            this.makeOrderedRenderable(dc);
    }

    /**
     * Returns an integer number uniquely identifying the surface object. This number is unique relative to other
     * instances of SurfaceObject, but is not necessarily globally unique.
     *
     * @return the surface object's unique identifier.
     */
    protected long getUniqueId()
    {
        return this.uniqueId;
    }

    /**
     * Sets the SurfaceObject's modified time to the current system time. This causes cached representations of this
     * SurfaceObject to be refreshed.
     */
    protected void updateModifiedTime()
    {
        this.lastModifiedTime = System.currentTimeMillis();
    }

    /** Clears this SurfaceObject's internal extent cache. */
    protected void clearCaches()
    {
        this.extentCache.clear();
    }

    /* Updates this SurfaceObject's modified time and clears its internal caches. */
    protected void onShapeChanged()
    {
        this.updateModifiedTime();
        this.clearCaches();
    }

    //**************************************************************//
    //********************  Extent  ********************************//
    //**************************************************************//

    /**
     * Computes the surface object's extent. Uses the sector list returned by {@link #getSectors(DrawContext)} to
     * compute the extent bounding this object's sectors on the draw context's surface. This returns null if the surface
     * object has no sectors.
     *
     * @param dc the draw context the extent relates to.
     *
     * @return the surface object's extent. Returns null if the surface object has no sectors.
     */
    protected Extent computeExtent(DrawContext dc)
    {
        List<Sector> sectors = this.getSectors(dc);
        if (sectors == null)
            return null;

        return this.computeExtent(dc.getGlobe(), dc.getVerticalExaggeration(), sectors);
    }

    /**
     * Computes an extent bounding the the specified sectors on the specified Globe's surface. If the list contains a
     * single sector this returns a box created by calling {@link gov.nasa.worldwind.geom.Sector#computeBoundingBox(gov.nasa.worldwind.globes.Globe,
     * double, gov.nasa.worldwind.geom.Sector)}. If the list contains more than one sector this returns a {@link
     * gov.nasa.worldwind.geom.Box} containing the corners of the boxes bounding each sector. This returns null if the
     * sector list is empty.
     *
     * @param globe                the globe the extent relates to.
     * @param verticalExaggeration the globe's vertical surface exaggeration.
     * @param sectors              the sectors to bound.
     *
     * @return an extent for the specified sectors on the specified Globe.
     */
    protected Extent computeExtent(Globe globe, double verticalExaggeration, List<Sector> sectors)
    {
        // This should never happen, but we check anyway.
        if (sectors.size() == 0)
        {
            return null;
        }
        // This surface shape does not cross the international dateline, and therefore has a single bounding sector.
        // Return the box which contains that sector.
        else if (sectors.size() == 1)
        {
            return Sector.computeBoundingBox(globe, verticalExaggeration, sectors.get(0));
        }
        // This surface crosses the international dateline, and its bounding sectors are split along the dateline.
        // Return a box which contains the corners of the boxes bounding each sector.
        else
        {
            ArrayList<Vec4> boxCorners = new ArrayList<Vec4>();

            for (Sector s : sectors)
            {
                Box box = Sector.computeBoundingBox(globe, verticalExaggeration, s);
                boxCorners.addAll(Arrays.asList(box.getCorners()));
            }

            return Box.computeBoundingBox(boxCorners);
        }
    }

    /**
     * Test if this SurfaceObject intersects the specified draw context's frustum. During picking mode, this tests
     * intersection against all of the draw context's pick frustums. During rendering mode, this tests intersection
     * against the draw context's viewing frustum.
     *
     * @param dc the draw context the SurfaceObject is related to.
     *
     * @return true if this SurfaceObject intersects the draw context's frustum; false otherwise.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        // A null extent indicates an object which has no location.
        Extent extent = this.getExtent(dc);
        if (extent == null)
            return false;

        // Test this object's extent against the pick frustum list
        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(extent);

        // Test this object's extent against the viewing frustum.
        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    /**
     * Test if this SurfaceObject intersects the specified draw context's pick frustums.
     *
     * @param dc the draw context the SurfaceObject is related to.
     *
     * @return true if this SurfaceObject intersects any of the draw context's pick frustums; false otherwise.
     */
    protected boolean intersectsPickFrustum(DrawContext dc)
    {
        // Test this object's extent against the pick frustum list. A null extent indicates the object has no location.
        Extent extent = this.getExtent(dc);
        return extent != null && dc.getPickFrustums().intersectsAny(extent);
    }

    /**
     * Test if this SurfaceObject intersects the specified draw context's visible sector. This returns false if the draw
     * context's visible sector is null.
     *
     * @param dc draw context the SurfaceObject is related to.
     *
     * @return true if this SurfaceObject intersects the draw context's visible sector; false otherwise.
     */
    protected boolean intersectsVisibleSector(DrawContext dc)
    {
        if (dc.getVisibleSector() == null)
            return false;

        List<Sector> sectors = this.getSectors(dc);
        if (sectors == null)
            return false;

        for (Sector s : sectors)
        {
            if (s.intersects(dc.getVisibleSector()))
                return true;
        }

        return false;
    }

    //**************************************************************//
    //********************  Rendering  *****************************//
    //**************************************************************//

    /**
     * Prepares the SurfaceObject as an {@link gov.nasa.worldwind.render.OrderedRenderable} and adds it to the
     * DrawContext's ordered surface renderable list. Additionally, this prepares the SurfaceObject's pickable
     * representation if the SurfaceObject's containing layer is enabled for picking and the SurfaceObject intersects
     * one of the DrawContext's picking frustums.
     * <p>
     * During ordered preRendering, the {@link gov.nasa.worldwind.SceneController} builds a composite representation of
     * this SurfaceObject and any other SurfaceObject on the DrawContext's ordered surface renderable list. The
     * SceneController causes each SurfaceObject's to draw itself into the composite representation by calling its
     * {@link #render(DrawContext)} method in ordered rendering mode.
     *
     * @param dc the DrawContext to add to.
     */
    protected void makeOrderedPreRenderable(DrawContext dc)
    {
        // Test for visibility against the draw context's visible sector prior to preparing this object for
        // preRendering.
        if (!this.intersectsVisibleSector(dc))
            return;

        // Create a representation of this object that can be used during picking. No need for a pickable representation
        // if this object's parent layer isn't pickable or if this object doesn't intersect the pick frustum. We do not
        // test visibility against the view frustum, because it's possible for the pick frustum to slightly exceed the
        // view frustum when the cursor is on the viewport edge.
        if ((dc.getCurrentLayer() == null || dc.getCurrentLayer().isPickEnabled()) && this.intersectsPickFrustum(dc))
            this.buildPickRepresentation(dc);

        // If this object is visible, add it to the draw context's ordered surface renderable queue. This queue is
        // processed by the SceneController during the preRender pass as follows: the SceneController builds a composite
        // representation of this object and any other SurfaceObject on the queue, and calls this object's preRender
        // method (we ignore this call with a conditional in preRender). While building a composite representation the
        // SceneController calls this object's render method in ordered rendering mode.
        if (this.intersectsFrustum(dc))
            dc.addOrderedSurfaceRenderable(this);
    }

    /**
     * Prepares the SurfaceObject as an {@link gov.nasa.worldwind.render.OrderedRenderable} and adds it to the
     * DrawContext's ordered surface renderable list. We ignore this call during rendering mode to suppress calls to
     * {@link #render(DrawContext)} during ordered rendering mode. The SceneController already invokes render during
     * ordered picking mode to build a composite representation of the SurfaceObjects.
     * <p>
     * During ordered picking, the {@link gov.nasa.worldwind.SceneController} invokes the SurfaceObject's {@link
     * #pick(DrawContext, java.awt.Point)} method.
     *
     * @param dc the DrawContext to add to.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Add this object to the draw context's ordered surface renderable queue only during picking mode. This queue
        // is processed by the SceneController during each rendering pass as follows:
        //
        // 1) Ordered picking - the SceneController calls this object's pick method.
        //
        // 2) Ordered rendering - the SceneController draws the composite representation of this object prepared during
        // ordered preRendering and calls this object's render method. Since we use the render method to draw a
        // composite representation during preRendering, we suppress this call by not adding to the ordered surface
        // renderable queue during rendering.

        if (!dc.isPickingMode())
            return;

        // Test for visibility prior to adding this object to the draw context's ordered renderable queue. Note that
        // there's no need to test again during ordered rendering mode.
        if (!this.intersectsVisibleSector(dc) || !this.intersectsFrustum(dc))
            return;

        this.pickLayer = dc.getCurrentLayer(); // Keep track of the object's parent layer for use during picking.
        dc.addOrderedSurfaceRenderable(this);
    }

    /**
     * Causes the SurfaceObject to draw itself in a unique pick color, and add itself as a pickable object to the
     * specified pickSupport.
     *
     * @param dc          the current DrawContext.
     * @param pickSupport the PickSupport to add the SurfaceObject to.
     */
    protected void pickOrderedRenderable(DrawContext dc, PickSupport pickSupport)
    {
        // Register a unique pick color with the PickSupport. We define the pickable object to be the caller specified
        // delegate owner, or this object if the delegate owner is null. We define the picked position to be the
        // terrain's picked position to maintain backwards compatibility with previous implementations of SurfaceObject.
        Color pickColor = dc.getUniquePickColor();
        pickSupport.addPickableObject(this.createPickedObject(dc, pickColor));

        // Draw an individual representation of this object in a unique pick color. This representation is created
        // during the preRender pass in makeOrderedPreRenderable().
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        this.drawPickRepresentation(dc);
    }

    /**
     * Create a {@link gov.nasa.worldwind.pick.PickedObject} for this surface object. The PickedObject created by this
     * method will be added to the pick list to represent the current surface object.
     *
     * @param dc        Active draw context.
     * @param pickColor Unique color for this PickedObject.
     *
     * @return A new picked object.
     */
    protected PickedObject createPickedObject(DrawContext dc, Color pickColor)
    {
        Object pickedObject = this.getDelegateOwner() != null ? this.getDelegateOwner() : this;
        Position pickedPos = dc.getPickedObjects().getTerrainObject() != null
            ? dc.getPickedObjects().getTerrainObject().getPosition() : null;

        return new PickedObject(pickColor.getRGB(), pickedObject, pickedPos, false);
    }

    /**
     * Causes adjacent SurfaceObjects in the DrawContext's ordered surface renderable list to draw themselves in in a
     * unique pick color, and adds themselves as pickable objects to the specified pickSupport. Adjacent SurfaceObjects
     * are removed from the DrawContext's list and processed until one is encountered that has a different containing
     * layer or is not enabled for batch picking.
     *
     * @param dc          the current DrawContext.
     * @param pickSupport the PickSupport to add the SurfaceObject to.
     */
    protected void pickBatched(DrawContext dc, PickSupport pickSupport)
    {
        // Draw as many as we can in a batch to save pick resolution.
        Object nextItem = dc.getOrderedSurfaceRenderables().peek();

        while (nextItem != null && nextItem instanceof AbstractSurfaceObject)
        {
            AbstractSurfaceObject so = (AbstractSurfaceObject) nextItem;

            // Batch pick only within a single layer, and for objects which are enabled for batch picking.
            if (so.pickLayer != this.pickLayer || !so.isEnableBatchPicking())
                break;

            dc.getOrderedSurfaceRenderables().poll(); // take it off the queue
            so.pickOrderedRenderable(dc, pickSupport);

            nextItem = dc.getOrderedSurfaceRenderables().peek();
        }
    }

    /**
     * Causes the SurfaceObject to render itself. SurfaceObjects are drawn in geographic coordinates into offscreen
     * surface tiles. This attempts to get a {@link gov.nasa.worldwind.util.SurfaceTileDrawContext} from the
     * DrawContext's AVList by querying the key {@link gov.nasa.worldwind.avlist.AVKey#SURFACE_TILE_DRAW_CONTEXT}. If
     * the DrawContext has a SurfaceTileDrawContext attached under that key, this calls {@link
     * #drawGeographic(DrawContext, gov.nasa.worldwind.util.SurfaceTileDrawContext)} with the SurfaceTileDrawContext.
     * Otherwise this logs a warning and returns.
     *
     * @param dc the current DrawContext.
     */
    protected void drawOrderedRenderable(DrawContext dc)
    {
        // This method is invoked by the SceneController during ordered rendering mode while building a composite
        // representation of the SurfaceObjects during ordered preRendering. Since we use this method to draw a
        // composite representation during preRendering, we prevent this method from being invoked during ordered
        // rendering. Note that this method is not invoked during ordered picking; pickOrderedRenderable is called
        // instead.

        SurfaceTileDrawContext sdc = (SurfaceTileDrawContext) dc.getValue(AVKey.SURFACE_TILE_DRAW_CONTEXT);
        if (sdc == null)
        {
            Logging.logger().warning(Logging.getMessage("nullValue.SurfaceTileDrawContextIsNull"));
            return;
        }

        this.drawGeographic(dc, sdc);

        // Draw the diagnostic bounding sectors during ordered rendering mode.
        if (this.isDrawBoundingSectors() && !dc.isPickingMode())
            this.drawBoundingSectors(dc, sdc);
    }

    /**
     * Causes the SurfaceObject to render itself to the specified region in geographic coordinates. The specified
     * viewport denotes the geographic region and its corresponding screen viewport.
     *
     * @param dc  the current draw context.
     * @param sdc the context containing a geographic region and screen viewport corresponding to a surface tile.
     */
    protected abstract void drawGeographic(DrawContext dc, SurfaceTileDrawContext sdc);

    //**************************************************************//
    //********************  Picking  *******************************//
    //**************************************************************//

    /**
     * Builds this AbstractSurfaceObject's pickable representation. This method is called during the preRender phase,
     * and is therefore free to modify the framebuffer contents to create the pickable representation.
     *
     * @param dc the draw context to build a representation for.
     */
    protected void buildPickRepresentation(DrawContext dc)
    {
        // Lazily create the support object used to build the pick representation.  We keep a reference to the
        // SurfaceObjectTileBuilder used to build the tiles because it acts as a cache key to the tiles and determines
        // when the tiles must be updated.
        if (this.pickTileBuilder == null)
            this.pickTileBuilder = this.createPickTileBuilder();

        // Build the pickable representation of this surface object as a list of surface tiles. Set the DrawContext into
        // ordered picking mode while the surface object's pickable representation is built. During ordered picking mode
        // the surface objects draws a pickable representation of itself in the surface tile's, and culls tiles against
        // the pick frustum.
        boolean prevPickingMode = dc.isPickingMode();
        boolean prevOrderedRenderingMode = dc.isOrderedRenderingMode();
        try
        {
            if (!prevPickingMode)
                dc.enablePickingMode();
            dc.setOrderedRenderingMode(true);

            // Build the pick representation as a list of surface tiles.
            this.pickTileBuilder.buildTiles(dc, Arrays.asList(this));
        }
        finally
        {
            // Restore the DrawContext's previous picking and ordered rendering modes.
            if (!prevPickingMode)
                dc.disablePickingMode();
            dc.setOrderedRenderingMode(prevOrderedRenderingMode);
        }
    }

    /**
     * Causes this SurfaceObject to draw a representation of itself suitable for use during picking.
     *
     * @param dc the current DrawContext.
     */
    protected void drawPickRepresentation(DrawContext dc)
    {
        // The pick representation is stored as a list of surface tiles. If the list is empty, then this surface object
        // was not picked. This method might be called when the list is null or empty because of an upstream
        // exception that prevented creation of the list.
        if (this.pickTileBuilder == null || this.pickTileBuilder.getTileCount(dc) == 0)
            return;

        // Draw the pickable representation of this surface object created during preRendering.
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushAttrib(gl, GL2.GL_POLYGON_BIT); // For cull face enable, cull face, polygon mode.
        try
        {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);

            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.pickTileBuilder.getTiles(dc));
        }
        finally
        {
            ogsh.pop(gl);
            // Clear the list of pick tiles to avoid retaining references to them in case we're never picked again.
            this.pickTileBuilder.clearTiles(dc);
        }
    }

    /**
     * Returns a {@link SurfaceObjectTileBuilder} appropriate for building and drawing the surface object's pickable
     * representation. The returned SurfaceObjectTileBuilder's is configured to create textures with the GL_ALPHA8
     * format, and to use GL_NEAREST filtering. This reduces a surface object's pick texture resources by a factor of 4,
     * and ensures that linear texture filtering and mip-mapping is disabled while drawing the pick tiles.
     *
     * @return a SurfaceObjectTileBuilder used for building and drawing the surface object's pickable representation.
     */
    protected SurfaceObjectTileBuilder createPickTileBuilder()
    {
        return new SurfaceObjectTileBuilder(new Dimension(512, 512), GL2.GL_ALPHA8, false, false);
    }

    //**************************************************************//
    //********************  Diagnostic Support  ********************//
    //**************************************************************//

    /**
     * Causes this SurfaceObject to render its bounding sectors to the specified region in geographic coordinates. The
     * specified viewport denotes the geographic region and its corresponding screen viewport.
     * <p>
     * The bounding sectors are rendered as a 1 pixel wide green outline.
     *
     * @param dc  the current DrawContext.
     * @param sdc the context containing a geographic region and screen viewport corresponding to a surface tile.
     *
     * @see #getSectors(DrawContext)
     */
    protected void drawBoundingSectors(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        List<Sector> sectors = this.getSectors(dc);
        if (sectors == null)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int attributeMask =
            GL2.GL_COLOR_BUFFER_BIT   // For alpha test enable, blend enable, alpha func, blend func.
                | GL2.GL_CURRENT_BIT  // For current color.
                | GL2.GL_LINE_BIT;    // For line smooth, line width.

        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushAttrib(gl, attributeMask);
        ogsh.pushModelview(gl);
        try
        {
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);

            gl.glDisable(GL.GL_LINE_SMOOTH);
            gl.glLineWidth(1f);

            gl.glColor4f(1f, 1f, 1f, 0.5f);

            // Set the model-view matrix to transform from geographic coordinates to viewport coordinates.
            Matrix matrix = sdc.getModelviewMatrix();
            gl.glMultMatrixd(matrix.toArray(new double[16], 0, false), 0);

            for (Sector s : sectors)
            {
                LatLon[] corners = s.getCorners();
                gl.glBegin(GL2.GL_LINE_LOOP);
                gl.glVertex2f((float) corners[0].getLongitude().degrees, (float) corners[0].getLatitude().degrees);
                gl.glVertex2f((float) corners[1].getLongitude().degrees, (float) corners[1].getLatitude().degrees);
                gl.glVertex2f((float) corners[2].getLongitude().degrees, (float) corners[2].getLatitude().degrees);
                gl.glVertex2f((float) corners[3].getLongitude().degrees, (float) corners[3].getLatitude().degrees);
                gl.glEnd();
            }
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    //**************************************************************//
    //********************  State Key  *****************************//
    //**************************************************************//

    /**
     * Represents a surface object's current state. StateKey uniquely identifies a surface object's current state as
     * follows: <ul> <li>The StateKey class distinguishes the key from other object types.</li> <li>The object's unique
     * ID distinguishes one surface object instances from another.</li> <li>The object's modified time distinguishes an
     * object's internal state from any of its previous states.</li> </ul> Using the unique ID to distinguish between objects
     * ensures that the StateKey does not store dangling references to the surface object itself. Should the StateKey
     * live longer than the surface object that created it, the StateKey does not prevent the object from being garbage
     * collected.
     */
    protected static class SurfaceObjectStateKey implements Cacheable
    {
        /** The SurfaceObject's unique ID. This is unique to all instances of SurfaceObject. */
        protected final long uniqueId;
        /** The SurfaceObject's modified time. */
        protected final long modifiedTime;

        /**
         * Constructs a new SurfaceObjectStateKey with the specified unique ID and modified time.
         *
         * @param uniqueId     the SurfaceObject's unique ID.
         * @param modifiedTime the SurfaceObject's modified time.
         */
        public SurfaceObjectStateKey(long uniqueId, long modifiedTime)
        {
            this.uniqueId = uniqueId;
            this.modifiedTime = modifiedTime;
        }

        @Override
        @SuppressWarnings({"SimplifiableIfStatement"})
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            SurfaceObjectStateKey that = (SurfaceObjectStateKey) o;
            return this.uniqueId == that.uniqueId && this.modifiedTime == that.modifiedTime;
        }

        @Override
        public int hashCode()
        {
            return 31 * (int) (this.uniqueId ^ (this.uniqueId >>> 32))
                + (int) (this.modifiedTime ^ (this.modifiedTime >>> 32));
        }

        /**
         * Returns the state key's size in bytes.
         *
         * @return the state key's size in bytes.
         */
        public long getSizeInBytes()
        {
            return 16; // Return the size of two long integers.
        }
    }

    //**************************************************************//
    //********************  Cache Entry  ***************************//
    //**************************************************************//

    /** Represents a globe dependent cache entry. */
    protected static class CacheEntry
    {
        public Object object;
        protected Object globeStateKey;

        public CacheEntry(Object object, DrawContext dc)
        {
            this.object = object;
            this.globeStateKey = dc.getGlobe().getStateKey(dc);
        }
    }
}
