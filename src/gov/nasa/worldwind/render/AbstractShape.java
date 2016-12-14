/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.ShapeDataCache;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import javax.xml.stream.*;
import java.awt.*;
import java.io.*;

/**
 * Provides a base class form several geometric {@link gov.nasa.worldwind.render.Renderable}s. Implements common
 * attribute handling and rendering flow for outlined shapes. Provides common defaults and common export code.
 * <p/>
 * In order to support simultaneous use of this shape with multiple globes (windows), this shape maintains a cache of
 * data computed relative to each globe. During rendering, the data for the currently active globe, as indicated in the
 * draw context, is made current. Subsequently called methods rely on the existence of this current data cache entry.
 *
 * @author tag
 * @version $Id: AbstractShape.java 3306 2015-07-08 22:00:14Z tgaskins $
 */
public abstract class AbstractShape extends WWObjectImpl
    implements Highlightable, OrderedRenderable, Movable, Movable2, ExtentHolder, GeographicExtent, Exportable,
    Restorable, PreRenderable, Attributable, Draggable
{
    /** The default interior color. */
    protected static final Material DEFAULT_INTERIOR_MATERIAL = Material.LIGHT_GRAY;
    /** The default outline color. */
    protected static final Material DEFAULT_OUTLINE_MATERIAL = Material.DARK_GRAY;
    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;
    /** The default altitude mode. */
    protected static final int DEFAULT_ALTITUDE_MODE = WorldWind.ABSOLUTE;
    /** The default outline pick width. */
    protected static final int DEFAULT_OUTLINE_PICK_WIDTH = 10;
    /** The default geometry regeneration interval. */
    protected static final int DEFAULT_GEOMETRY_GENERATION_INTERVAL = 3000;
    /** Indicates the number of vertices that must be present in order for VBOs to be used to render this shape. */
    protected static final int VBO_THRESHOLD = Configuration.getIntegerValue(AVKey.VBO_THRESHOLD, 30);

    /** The attributes used if attributes are not specified. */
    protected static ShapeAttributes defaultAttributes;

    static
    {
        // Create and populate the default attributes.
        defaultAttributes = new BasicShapeAttributes();
        defaultAttributes.setInteriorMaterial(DEFAULT_INTERIOR_MATERIAL);
        defaultAttributes.setOutlineMaterial(DEFAULT_OUTLINE_MATERIAL);
    }

    /**
     * Compute the intersections of a specified line with this shape. If the shape's altitude mode is other than {@link
     * WorldWind#ABSOLUTE}, the shape's geometry is created relative to the specified terrain rather than the terrain
     * used during rendering, which may be at lower level of detail than required for accurate intersection
     * determination.
     *
     * @param line    the line to intersect.
     * @param terrain the {@link Terrain} to use when computing the shape's geometry.
     *
     * @return a list of intersections identifying where the line intersects the shape, or null if the line does not
     * intersect the shape.
     *
     * @throws InterruptedException if the operation is interrupted.
     * @see Terrain
     */
    abstract public java.util.List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException;

    /**
     * Called during construction to establish any subclass-specific state such as different default values than those
     * set by this class.
     */
    abstract protected void initialize();

    /**
     * Indicates whether texture should be applied to this shape. Called during rendering to determine whether texture
     * state should be established during preparation for interior drawing.
     * <p/>
     * Note: This method always returns false during the pick pass.
     *
     * @param dc the current draw context
     *
     * @return true if texture should be applied, otherwise false.
     */
    abstract protected boolean mustApplyTexture(DrawContext dc);

    /**
     * Produces the geometry and other state necessary to represent this shape as an ordered renderable. Places this
     * shape on the draw context's ordered renderable list for subsequent rendering. This method is called during {@link
     * #pick(DrawContext, java.awt.Point)} and {@link #render(DrawContext)} when it's been determined that the shape is
     * likely to be visible.
     *
     * @param dc the current draw context.
     *
     * @return true if the ordered renderable state was successfully computed, otherwise false, in which case the
     * current pick or render pass is terminated for this shape. Subclasses should return false if it is not possible to
     * create the ordered renderable state.
     *
     * @see #pick(DrawContext, java.awt.Point)
     * @see #render(DrawContext)
     */
    abstract protected boolean doMakeOrderedRenderable(DrawContext dc);

    /**
     * Determines whether this shape's ordered renderable state is valid and can be rendered. Called by {@link
     * #makeOrderedRenderable(DrawContext)}just prior to adding the shape to the ordered renderable list.
     *
     * @param dc the current draw context.
     *
     * @return true if this shape is ready to be rendered as an ordered renderable.
     */
    abstract protected boolean isOrderedRenderableValid(DrawContext dc);

    /**
     * Draws this shape's outline. Called immediately after calling {@link #prepareToDrawOutline(DrawContext,
     * ShapeAttributes, ShapeAttributes)}, which establishes OpenGL state for lighting, blending, pick color and line
     * attributes. Subclasses should execute the drawing commands specific to the type of shape.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    abstract protected void doDrawOutline(DrawContext dc);

    /**
     * Draws this shape's interior. Called immediately after calling {@link #prepareToDrawInterior(DrawContext,
     * ShapeAttributes, ShapeAttributes)}, which establishes OpenGL state for lighting, blending, pick color and
     * interior attributes. Subclasses should execute the drawing commands specific to the type of shape.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    abstract protected void doDrawInterior(DrawContext dc);

    /**
     * Fill this shape's vertex buffer objects. If the vertex buffer object resource IDs don't yet exist, create them.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    abstract protected void fillVBO(DrawContext dc);

    /**
     * Exports shape-specific fields.
     *
     * @param xmlWriter the export writer to write to.
     *
     * @throws IOException        if an IO error occurs while writing to the output destination.
     * @throws XMLStreamException if an exception occurs converting this shape's fields to XML.
     */
    abstract protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException;

    /**
     * Creates and returns a new cache entry specific to the subclass.
     *
     * @param dc the current draw context.
     *
     * @return a data cache entry for the state in the specified draw context.
     */
    protected abstract AbstractShapeData createCacheEntry(DrawContext dc);

    /** This shape's normal, non-highlighted attributes. */
    protected ShapeAttributes normalAttrs;
    /** This shape's highlighted attributes. */
    protected ShapeAttributes highlightAttrs;
    /**
     * The attributes active for a particular pick and render pass. These are determined according to the highlighting
     * mode.
     */
    protected ShapeAttributes activeAttributes = new BasicShapeAttributes(); // re-determined each frame

    protected boolean highlighted;
    protected boolean dragEnabled = true;
    protected boolean visible = true;
    protected int altitudeMode = DEFAULT_ALTITUDE_MODE;
    protected boolean enableBatchRendering = true;
    protected boolean enableBatchPicking = true;
    protected boolean enableDepthOffset;
    protected int outlinePickWidth = DEFAULT_OUTLINE_PICK_WIDTH;
    protected Sector sector; // the shape's bounding sector
    protected Position referencePosition; // the location/position to use as the shape's reference point
    protected Object delegateOwner; // for app use to identify an owner of this shape other than the current layer
    protected long maxExpiryTime = DEFAULT_GEOMETRY_GENERATION_INTERVAL;
    protected long minExpiryTime = Math.max(DEFAULT_GEOMETRY_GENERATION_INTERVAL - 500, 0);
    protected boolean viewDistanceExpiration = true;
    protected SurfaceShape surfaceShape;

    // Volatile values used only during frame generation.
    protected OGLStackHandler BEogsh = new OGLStackHandler(); // used for beginDrawing/endDrawing state
    protected Layer pickLayer;
    protected PickSupport pickSupport = new PickSupport();

    /** Holds globe-dependent computed data. One entry per globe encountered during {@link #render(DrawContext)}. */
    protected ShapeDataCache shapeDataCache = new ShapeDataCache(60000);

    // Additional drag context
    protected DraggableSupport draggableSupport = null;

    /**
     * Identifies the active globe-dependent data for the current invocation of {@link #render(DrawContext)}. The active
     * data is drawn from this shape's data cache at the beginning of the <code>render</code> method.
     */
    protected AbstractShapeData currentData;

    /**
     * Returns the data cache entry for the current rendering.
     *
     * @return the data cache entry for the current rendering.
     */
    protected AbstractShapeData getCurrentData()
    {
        return this.currentData;
    }

    /** Holds the globe-dependent data captured in this shape's data cache. */
    protected static class AbstractShapeData extends ShapeDataCache.ShapeDataCacheEntry
    {
        /** Identifies the frame used to calculate this entry's values. */
        protected long frameNumber = -1;
        /** This entry's reference point. */
        protected Vec4 referencePoint;
        /** A quick-to-compute metric to determine eye distance changes that invalidate this entry's geometry. */
        protected Double referenceDistance;
        /** The GPU-resource cache key to use for this entry's VBOs, if VBOs are used. */
        protected Object vboCacheKey = new Object();

        /**
         * Constructs a data cache entry and initializes its globe-dependent state key for the globe in the specified
         * draw context and capture the current vertical exaggeration. The entry becomes invalid when these values
         * change or when the entry's expiration timer expires.
         *
         * @param dc            the current draw context.
         * @param minExpiryTime the minimum number of milliseconds to use this shape before regenerating its geometry.
         * @param maxExpiryTime the maximum number of milliseconds to use this shape before regenerating its geometry.
         */
        protected AbstractShapeData(DrawContext dc, long minExpiryTime, long maxExpiryTime)
        {
            super(dc, minExpiryTime, maxExpiryTime);
        }

        public long getFrameNumber()
        {
            return frameNumber;
        }

        public void setFrameNumber(long frameNumber)
        {
            this.frameNumber = frameNumber;
        }

        public Vec4 getReferencePoint()
        {
            return referencePoint;
        }

        public void setReferencePoint(Vec4 referencePoint)
        {
            this.referencePoint = referencePoint;
        }

        public Object getVboCacheKey()
        {
            return vboCacheKey;
        }

        public void setVboCacheKey(Object vboCacheKey)
        {
            this.vboCacheKey = vboCacheKey;
        }

        public Double getReferenceDistance()
        {
            return referenceDistance;
        }

        public void setReferenceDistance(Double referenceDistance)
        {
            this.referenceDistance = referenceDistance;
        }
    }

    /** Outlined shapes are drawn as {@link gov.nasa.worldwind.render.OutlinedShape}s. */
    protected OutlinedShape outlineShapeRenderer = new OutlinedShape()
    {
        public boolean isDrawOutline(DrawContext dc, Object shape)
        {
            return ((AbstractShape) shape).mustDrawOutline();
        }

        public boolean isDrawInterior(DrawContext dc, Object shape)
        {
            return ((AbstractShape) shape).mustDrawInterior();
        }

        public boolean isEnableDepthOffset(DrawContext dc, Object shape)
        {
            return ((AbstractShape) shape).isEnableDepthOffset();
        }

        public void drawOutline(DrawContext dc, Object shape)
        {
            ((AbstractShape) shape).drawOutline(dc);
        }

        public void drawInterior(DrawContext dc, Object shape)
        {
            ((AbstractShape) shape).drawInterior(dc);
        }

        public Double getDepthOffsetFactor(DrawContext dc, Object shape)
        {
            return null;
        }

        public Double getDepthOffsetUnits(DrawContext dc, Object shape)
        {
            return null;
        }
    };

    /** Invokes {@link #initialize()} during construction and sets the data cache's expiration time to a default value. */
    protected AbstractShape()
    {
        this.initialize();
    }

    protected AbstractShape(AbstractShape source)
    {
        this.normalAttrs = new BasicShapeAttributes(source.normalAttrs);
        this.highlightAttrs = new BasicShapeAttributes(source.highlightAttrs);
        this.highlighted = source.highlighted;
        this.visible = source.visible;
        this.altitudeMode = source.altitudeMode;
        this.enableBatchRendering = source.enableBatchRendering;
        this.enableBatchPicking = source.enableBatchPicking;
        this.enableDepthOffset = source.enableDepthOffset;
        this.outlinePickWidth = source.outlinePickWidth;
        this.sector = source.sector;
        this.referencePosition = source.referencePosition;
        this.delegateOwner = source.delegateOwner;

        this.initialize();
    }

    /** Invalidates computed values. Called when this shape's contents or certain attributes change. */
    protected void reset()
    {
        this.shapeDataCache.removeAllEntries();
        this.sector = null;
        this.surfaceShape = null;
    }

    /**
     * Returns this shape's normal (as opposed to highlight) attributes.
     *
     * @return this shape's normal attributes. May be null.
     */
    public ShapeAttributes getAttributes()
    {
        return this.normalAttrs;
    }

    /**
     * Specifies this shape's normal (as opposed to highlight) attributes.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public void setAttributes(ShapeAttributes normalAttrs)
    {
        this.normalAttrs = normalAttrs;

        if (this.surfaceShape != null)
            this.surfaceShape.setAttributes(normalAttrs);
    }

    /**
     * Returns this shape's highlight attributes.
     *
     * @return this shape's highlight attributes. May be null.
     */
    public ShapeAttributes getHighlightAttributes()
    {
        return highlightAttrs;
    }

    /**
     * Specifies this shape's highlight attributes.
     *
     * @param highlightAttrs the highlight attributes. May be null, in which case default attributes are used.
     */
    public void setHighlightAttributes(ShapeAttributes highlightAttrs)
    {
        this.highlightAttrs = highlightAttrs;

        if (this.surfaceShape != null)
            this.surfaceShape.setHighlightAttributes(highlightAttrs);
    }

    public boolean isHighlighted()
    {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /**
     * Indicates whether this shape is drawn during rendering.
     *
     * @return true if this shape is drawn, otherwise false.
     *
     * @see #setVisible(boolean)
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Specifies whether this shape is drawn during rendering.
     *
     * @param visible true to draw this shape, otherwise false. The default value is true.
     *
     * @see #setAttributes(ShapeAttributes)
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * Returns this shape's altitude mode.
     *
     * @return this shape's altitude mode.
     *
     * @see #setAltitudeMode(int)
     */
    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    /**
     * Specifies this shape's altitude mode, one of {@link WorldWind#ABSOLUTE}, {@link WorldWind#RELATIVE_TO_GROUND} or
     * {@link WorldWind#CLAMP_TO_GROUND}.
     * <p/>
     * Note: If the altitude mode is unrecognized, {@link WorldWind#ABSOLUTE} is used.
     * <p/>
     * Note: Subclasses may recognize additional altitude modes or may not recognize the ones described above.
     *
     * @param altitudeMode the altitude mode. The default value is {@link WorldWind#ABSOLUTE}.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        if (this.altitudeMode == altitudeMode)
            return;

        this.altitudeMode = altitudeMode;
        this.reset();
    }

    public double getDistanceFromEye()
    {
        return this.getCurrentData() != null ? this.getCurrentData().getEyeDistance() : 0;
    }

    /**
     * Indicates whether batch rendering is enabled for the concrete shape type of this shape.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchRendering(boolean).
     */
    public boolean isEnableBatchRendering()
    {
        return enableBatchRendering;
    }

    /**
     * Specifies whether adjacent shapes of this shape's concrete type in the ordered renderable list may be rendered
     * together if they are contained in the same layer. This increases performance. There is seldom a reason to disable
     * it.
     *
     * @param enableBatchRendering true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchRendering(boolean enableBatchRendering)
    {
        this.enableBatchRendering = enableBatchRendering;
    }

    /**
     * Indicates whether batch picking is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchPicking(boolean).
     */
    public boolean isEnableBatchPicking()
    {
        return enableBatchPicking;
    }

    /**
     * Specifies whether adjacent shapes of this shape's concrete type in the ordered renderable list may be pick-tested
     * together if they are contained in the same layer. This increases performance but allows only the top-most of the
     * polygons to be reported in a {@link gov.nasa.worldwind.event.SelectEvent} even if several of the polygons are at
     * the pick position.
     * <p/>
     * Batch rendering ({@link #setEnableBatchRendering(boolean)}) must be enabled in order for batch picking to occur.
     *
     * @param enableBatchPicking true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchPicking(boolean enableBatchPicking)
    {
        this.enableBatchPicking = enableBatchPicking;
    }

    /**
     * Indicates the outline line width to use during picking. A larger width than normal typically makes the outline
     * easier to pick.
     *
     * @return the outline line width used during picking.
     */
    public int getOutlinePickWidth()
    {
        return this.outlinePickWidth;
    }

    /**
     * Specifies the outline line width to use during picking. A larger width than normal typically makes the outline
     * easier to pick.
     * <p/>
     * Note that the size of the pick aperture also affects the precision necessary to pick.
     *
     * @param outlinePickWidth the outline pick width. The default is 10.
     *
     * @throws IllegalArgumentException if the width is less than 0.
     */
    public void setOutlinePickWidth(int outlinePickWidth)
    {
        if (outlinePickWidth < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlinePickWidth = outlinePickWidth;
    }

    /**
     * Indicates whether the filled sides of this shape should be offset towards the viewer to help eliminate artifacts
     * when two or more faces of this or other filled shapes are coincident.
     *
     * @return true if depth offset is applied, otherwise false.
     */
    public boolean isEnableDepthOffset()
    {
        return this.enableDepthOffset;
    }

    /**
     * Specifies whether the filled sides of this shape should be offset towards the viewer to help eliminate artifacts
     * when two or more faces of this or other filled shapes are coincident. See {@link
     * gov.nasa.worldwind.render.Offset}.
     *
     * @param enableDepthOffset true if depth offset is applied, otherwise false.
     */
    public void setEnableDepthOffset(boolean enableDepthOffset)
    {
        this.enableDepthOffset = enableDepthOffset;
    }

    /**
     * Indicates the maximum length of time between geometry regenerations. See {@link
     * #setGeometryRegenerationInterval(int)} for the regeneration-interval's description.
     *
     * @return the geometry regeneration interval, in milliseconds.
     *
     * @see #setGeometryRegenerationInterval(int)
     */
    public long getGeometryRegenerationInterval()
    {
        return this.maxExpiryTime;
    }

    /**
     * Specifies the maximum length of time between geometry regenerations. The geometry is regenerated when this
     * shape's altitude mode is {@link WorldWind#CLAMP_TO_GROUND} or {@link WorldWind#RELATIVE_TO_GROUND} in order to
     * capture changes to the terrain. (The terrain changes when its resolution changes or when new elevation data is
     * returned from a server.) Decreasing this value causes the geometry to more quickly track terrain changes but at
     * the cost of performance. Increasing this value often does not have much effect because there are limiting factors
     * other than geometry regeneration.
     *
     * @param geometryRegenerationInterval the geometry regeneration interval, in milliseconds. The default is two
     *                                     seconds.
     */
    public void setGeometryRegenerationInterval(int geometryRegenerationInterval)
    {
        this.maxExpiryTime = Math.max(geometryRegenerationInterval, 0);
        this.minExpiryTime = (long) (0.6 * (double) this.maxExpiryTime);

        for (ShapeDataCache.ShapeDataCacheEntry shapeData : this.shapeDataCache)
        {
            if (shapeData != null)
                shapeData.getTimer().setExpiryTime(this.minExpiryTime, this.maxExpiryTime);
        }
    }

    /**
     * Specifies the position to use as a reference position for computed geometry. This value should typically left to
     * the default value of the first position in the polygon's outer boundary.
     *
     * @param referencePosition the reference position. May be null, in which case the first position of the outer
     *                          boundary is the reference position.
     */
    public void setReferencePosition(Position referencePosition)
    {
        this.referencePosition = referencePosition;
        this.reset();
    }

    public Object getDelegateOwner()
    {
        return delegateOwner;
    }

    public void setDelegateOwner(Object delegateOwner)
    {
        this.delegateOwner = delegateOwner;
    }

    /**
     * Returns this shape's extent in model coordinates.
     *
     * @return this shape's extent, or null if an extent has not been computed.
     */
    public Extent getExtent()
    {
        return this.getCurrentData().getExtent();
    }

    /**
     * Returns the Cartesian coordinates of this shape's reference position as computed during the most recent
     * rendering.
     *
     * @return the Cartesian coordinates corresponding to this shape's reference position, or null if the point has not
     * been computed.
     */
    public Vec4 getReferencePoint()
    {
        return this.currentData.getReferencePoint();
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
            return null;

        ShapeDataCache.ShapeDataCacheEntry entry = this.shapeDataCache.getEntry(globe);

        return (entry != null && !entry.isExpired(null) && entry.getExtent() != null) ? entry.getExtent() : null;
    }

    /**
     * Determines which attributes -- normal, highlight or default -- to use each frame. Places the result in this
     * shape's current active attributes.
     *
     * @see #getActiveAttributes()
     */
    protected void determineActiveAttributes()
    {
        if (this.isHighlighted())
        {
            if (this.getHighlightAttributes() != null)
                this.activeAttributes.copy(this.getHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                if (this.getAttributes() != null)
                    this.activeAttributes.copy(this.getAttributes());
                else
                    this.activeAttributes.copy(defaultAttributes);

                this.activeAttributes.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeAttributes.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
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
    }

    /**
     * Returns this shape's currently active attributes, as determined during the most recent call to {@link
     * #determineActiveAttributes()}. The active attributes are either the normal or highlight attributes, depending on
     * this shape's highlight flag, and incorporates default attributes for those not specified in the applicable
     * attribute set.
     *
     * @return this shape's currently active attributes.
     */
    public ShapeAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    /**
     * Indicates whether this shape's renderable geometry must be recomputed, either as a result of an attribute or
     * property change or the expiration of the geometry regeneration interval.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     *
     * @return true if this shape's geometry must be regenerated, otherwise false.
     */
    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        return this.getCurrentData().isExpired(dc) || !this.getCurrentData().isValid(dc);
    }

    /**
     * Indicates whether this shape should use OpenGL vertex buffer objects.
     *
     * @param dc the current draw context.
     *
     * @return true if this shape should use vertex buffer objects, otherwise false.
     */
    protected boolean shouldUseVBOs(DrawContext dc)
    {
        return dc.getGLRuntimeCapabilities().isUseVertexBufferObject();
    }

    /**
     * Indicates whether this shape's interior must be drawn.
     *
     * @return true if an interior must be drawn, otherwise false.
     */
    protected boolean mustDrawInterior()
    {
        return this.getActiveAttributes().isDrawInterior();
    }

    /**
     * Indicates whether this shape's outline must be drawn.
     *
     * @return true if the outline should be drawn, otherwise false.
     */
    protected boolean mustDrawOutline()
    {
        return this.getActiveAttributes().isDrawOutline();
    }

    /**
     * Indicates whether standard lighting must be applied by consulting the current active attributes. Calls {@link
     * #mustApplyLighting(DrawContext, ShapeAttributes)}, specifying null for the activeAttrs.
     *
     * @param dc the current draw context
     *
     * @return true if lighting must be applied, otherwise false.
     */
    protected boolean mustApplyLighting(DrawContext dc)
    {
        return this.mustApplyLighting(dc, null);
    }

    /**
     * Indicates whether standard lighting must be applied by consulting either the specified active attributes or the
     * current active attributes.
     *
     * @param dc          the current draw context
     * @param activeAttrs the attribute bundle to consider when determining whether lighting is applied. May be null, in
     *                    which case the current active attributes are used.
     *
     * @return true if lighting must be applied, otherwise false.
     */
    protected boolean mustApplyLighting(DrawContext dc, ShapeAttributes activeAttrs)
    {
        return activeAttrs != null ? activeAttrs.isEnableLighting() : this.activeAttributes.isEnableLighting();
    }

    /**
     * Indicates whether normal vectors must be computed by consulting the current active attributes. Calls {@link
     * #mustCreateNormals(DrawContext, ShapeAttributes)}, specifying null for the activeAttrs.
     *
     * @param dc the current draw context
     *
     * @return true if normal vectors must be computed, otherwise false.
     */
    protected boolean mustCreateNormals(DrawContext dc)
    {
        return this.mustCreateNormals(dc, null);
    }

    /**
     * Indicates whether normal vectors must be computed by consulting either the specified active attributes or the
     * current active attributes. Calls {@link #mustApplyLighting(DrawContext, ShapeAttributes)}, passing the specified
     * active attrs.
     *
     * @param dc          the current draw context
     * @param activeAttrs the attribute bundle to consider when determining whether normals should be computed. May be
     *                    null, in which case the current active attributes are used.
     *
     * @return true if normal vectors must be computed, otherwise false.
     */
    protected boolean mustCreateNormals(DrawContext dc, ShapeAttributes activeAttrs)
    {
        return this.mustApplyLighting(dc, activeAttrs);
    }

    /**
     * Creates a {@link WWTexture} for a specified image source.
     *
     * @param imageSource the image source for which to create the texture.
     *
     * @return the new <code>WWTexture</code>.
     *
     * @throws IllegalArgumentException if the image source is null.
     */
    protected WWTexture makeTexture(Object imageSource)
    {
        return new LazilyLoadedTexture(imageSource, true);
    }

    @Override
    public void preRender(DrawContext dc)
    {
        if (dc.getGlobe() instanceof Globe2D)
        {
            if (this.surfaceShape == null)
            {
                this.surfaceShape = this.createSurfaceShape();
                if (this.surfaceShape == null)
                    return;

                this.surfaceShape.setAttributes(this.getAttributes());
                this.surfaceShape.setHighlightAttributes(this.getHighlightAttributes());
            }

            this.updateSurfaceShape();
            this.surfaceShape.preRender(dc);
        }
    }

    /**
     * Returns a {@link SurfaceShape} that corresponds to this Path and is used for drawing on 2D globes.
     *
     * @return The surface shape to represent this Path on a 2D globe.
     */
    protected SurfaceShape createSurfaceShape()
    {
        return null;
    }

    /**
     * Sets surface shape parameters prior to picking and rendering the 2D shape used to represent this shape on 2D
     * globes. Subclasses should override this method if they need to update more than the highlighted state, visibility
     * state and delegate owner.
     */
    protected void updateSurfaceShape()
    {
        this.surfaceShape.setHighlighted(this.isHighlighted());
        this.surfaceShape.setVisible(this.isVisible());

        Object o = this.getDelegateOwner();
        this.surfaceShape.setDelegateOwner(o != null ? o : this);
    }

    public void pick(DrawContext dc, Point pickPoint)
    {
        // This method is called only when ordered renderables are being drawn.

        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.render(dc);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    public void render(DrawContext dc)
    {
        // This render method is called three times during frame generation. It's first called as a {@link Renderable}
        // during <code>Renderable</code> picking. It's called again during normal rendering. And it's called a third
        // time as an OrderedRenderable. The first two calls determine whether to add the shape to the ordered renderable
        // list during pick and render. The third call just draws the ordered renderable.

        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getGlobe() instanceof Globe2D && this.surfaceShape != null)
        {
            this.surfaceShape.render(dc);
            return;
        }

        // Retrieve the cached data for the current globe. If it doesn't yet exist, create it. Most code subsequently
        // executed depends on currentData being non-null.
        this.currentData = (AbstractShapeData) this.shapeDataCache.getEntry(dc.getGlobe());
        if (this.currentData == null)
        {
            this.currentData = this.createCacheEntry(dc);
            this.shapeDataCache.addEntry(this.currentData);
        }

        if (dc.getSurfaceGeometry() == null)
            return;

        if (!this.isVisible())
            return;

        if (this.isTerrainDependent())
            this.checkViewDistanceExpiration(dc);

        // Invalidate the extent if the vertical exaggeration has changed.
        if (this.currentData.getVerticalExaggeration() != dc.getVerticalExaggeration()) {
            this.currentData.setExtent(null);
        }

        if (this.getExtent() != null)
        {
            if (!this.intersectsFrustum(dc))
                return;

            // If the shape is less that a pixel in size, don't render it.
            if (dc.isSmall(this.getExtent(), 1))
                return;
        }

        if (dc.isOrderedRenderingMode())
            this.drawOrderedRenderable(dc);
        else
            this.makeOrderedRenderable(dc);
    }

    /**
     * Determines whether to add this shape to the draw context's ordered renderable list. Creates this shapes
     * renderable geometry.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Re-use values already calculated this frame.
        if (dc.getFrameTimeStamp() != this.getCurrentData().getFrameNumber())
        {
            this.determineActiveAttributes();
            if (this.getActiveAttributes() == null)
                return;

            // Regenerate the positions and shape at a specified frequency.
            if (this.mustRegenerateGeometry(dc))
            {
                if (!this.doMakeOrderedRenderable(dc))
                    return;

                if (this.shouldUseVBOs(dc))
                    this.fillVBO(dc);

                this.getCurrentData().restartTimer(dc);
            }

            this.getCurrentData().setFrameNumber(dc.getFrameTimeStamp());
        }

        if (!this.isOrderedRenderableValid(dc))
            return;

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();

        this.addOrderedRenderable(dc);
    }

    /**
     * Adds this shape to the draw context's ordered renderable list.
     *
     * @param dc the current draw context.
     */
    protected void addOrderedRenderable(DrawContext dc)
    {
        dc.addOrderedRenderable(this);
    }

    /**
     * Indicates whether this shape's geometry depends on the terrain.
     *
     * @return true if this shape's geometry depends on the terrain, otherwise false.
     */
    protected boolean isTerrainDependent()
    {
        return this.getAltitudeMode() != WorldWind.ABSOLUTE;
    }

    /**
     * Indicates whether this shape's terrain-dependent geometry is continually computed as its distance from the eye
     * point changes. This is often necessary to ensure that the shape is updated as the terrain precision changes. But
     * it's often not necessary as well, and can be disabled.
     *
     * @return true if the terrain dependent geometry is updated as the eye distance changes, otherwise false. The
     * default is true.
     */
    public boolean isViewDistanceExpiration()
    {
        return viewDistanceExpiration;
    }

    /**
     * Specifies whether this shape's terrain-dependent geometry is continually computed as its distance from the eye
     * point changes. This is often necessary to ensure that the shape is updated as the terrain precision changes. But
     * it's often not necessary as well, and can be disabled.
     *
     * @param viewDistanceExpiration true to enable view distance expiration, otherwise false.
     */
    public void setViewDistanceExpiration(boolean viewDistanceExpiration)
    {
        this.viewDistanceExpiration = viewDistanceExpiration;
    }

    /**
     * Determines whether this shape's geometry should be invalidated because the view distance changed, and if so,
     * invalidates the geometry.
     *
     * @param dc the current draw context.
     */
    protected void checkViewDistanceExpiration(DrawContext dc)
    {
        // Determine whether the distance of this shape from the eye has changed significantly. Invalidate the previous
        // extent and expire the shape geometry if it has. "Significantly" is considered a 10% difference.

        if (!this.isViewDistanceExpiration())
            return;

        Vec4 refPt = this.currentData.getReferencePoint();
        if (refPt == null)
            return;

        double newRefDistance = dc.getView().getEyePoint().distanceTo3(refPt);
        Double oldRefDistance = this.currentData.getReferenceDistance();
        if (oldRefDistance == null || Math.abs(newRefDistance - oldRefDistance) / oldRefDistance > 0.10)
        {
            this.currentData.setExpired(true);
            this.currentData.setExtent(null);
            this.currentData.setReferenceDistance(newRefDistance);
        }
    }

    /**
     * Determines whether this shape intersects the view frustum.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     *
     * @return true if this shape intersects the frustum, otherwise false.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        if (this.getExtent() == null)
            return true; // don't know the visibility, shape hasn't been computed yet

        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(this.getExtent());

        return dc.getView().getFrustumInModelCoordinates().intersects(this.getExtent());
    }

    /**
     * Draws this shape as an ordered renderable.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    protected void drawOrderedRenderable(DrawContext dc)
    {
        this.beginDrawing(dc, 0);
        try
        {
            this.doDrawOrderedRenderable(dc, this.pickSupport);

            if (this.isEnableBatchRendering())
                this.drawBatched(dc);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    /**
     * Draws this ordered renderable and all subsequent Path ordered renderables in the ordered renderable list. If the
     * current pick mode is true, only shapes within the same layer are drawn as a batch.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    protected void drawBatched(DrawContext dc)
    {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode())
        {
            while (nextItem != null && nextItem.getClass() == this.getClass())
            {
                AbstractShape shape = (AbstractShape) nextItem;
                if (!shape.isEnableBatchRendering())
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                shape.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
        else if (this.isEnableBatchPicking())
        {
            while (nextItem != null && nextItem.getClass() == this.getClass())
            {
                AbstractShape shape = (AbstractShape) nextItem;
                if (!shape.isEnableBatchRendering() || !shape.isEnableBatchPicking())
                    break;

                if (shape.pickLayer != this.pickLayer) // batch pick only within a single layer
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                shape.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
    }

    /**
     * Draw this shape as an ordered renderable. If in picking mode, add it to the picked object list of specified
     * {@link PickSupport}. The <code>PickSupport</code> may not be the one associated with this instance. During batch
     * picking the <code>PickSupport</code> of the instance initiating the batch picking is used so that all shapes
     * rendered in batch are added to the same pick list.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc             the current draw context.
     * @param pickCandidates a pick support holding the picked object list to add this shape to.
     */
    protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates)
    {
        this.currentData = (AbstractShapeData) this.shapeDataCache.getEntry(dc.getGlobe());

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        dc.getView().setReferenceCenter(dc, this.getCurrentData().getReferencePoint());

        if (dc.isPickingMode())
        {
            Color pickColor = dc.getUniquePickColor();
            pickCandidates.addPickableObject(this.createPickedObject(pickColor.getRGB()));
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }

        dc.drawOutlinedShape(this.outlineShapeRenderer, this);
    }

    /**
     * Creates a {@link gov.nasa.worldwind.pick.PickedObject} for this shape and the specified unique pick color. The
     * PickedObject returned by this method will be added to the pick list to represent the current shape.
     *
     * @param dc        the current draw context.
     * @param pickColor the unique color for this shape.
     *
     * @return a new picked object.
     *
     * @deprecated Use the more general {@link #createPickedObject(int)} instead.
     */
    @SuppressWarnings({"UnusedParameters"})
    protected PickedObject createPickedObject(DrawContext dc, Color pickColor)
    {
        return this.createPickedObject(pickColor.getRGB());
    }

    /**
     * Creates a {@link gov.nasa.worldwind.pick.PickedObject} for this shape and the specified unique pick color code.
     * The PickedObject returned by this method will be added to the pick list to represent the current shape.
     *
     * @param colorCode the unique color code for this shape.
     *
     * @return a new picked object.
     */
    protected PickedObject createPickedObject(int colorCode)
    {
        return new PickedObject(colorCode, this.getDelegateOwner() != null ? this.getDelegateOwner() : this);
    }

    /**
     * Establish the OpenGL state needed to draw this shape.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc       the current draw context.
     * @param attrMask an attribute mask indicating state the caller will set. This base class implementation sets
     *                 <code>GL_CURRENT_BIT, GL_LINE_BIT, GL_HINT_BIT, GL_POLYGON_BIT, GL_COLOR_BUFFER_BIT, and
     *                 GL_TRANSFORM_BIT</code>.
     *
     * @return the stack handler used to set the OpenGL state. Callers should use this to set additional state,
     * especially state indicated in the attribute mask argument.
     */
    protected OGLStackHandler beginDrawing(DrawContext dc, int attrMask)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.BEogsh.clear();

        // Note: While it's tempting to set each of these conditionally on whether the feature is actually enabled
        // for this shape, e.g. if (mustApplyBlending...), it doesn't work with batch rendering because subsequent
        // shapes in the batch may have the feature enabled.
        attrMask |= GL2.GL_CURRENT_BIT
            | GL2.GL_DEPTH_BUFFER_BIT
            | GL2.GL_LINE_BIT | GL2.GL_HINT_BIT // for outlines
            | GL2.GL_COLOR_BUFFER_BIT // for blending
            | GL2.GL_TRANSFORM_BIT // for texture
            | GL2.GL_POLYGON_BIT; // for culling

        this.BEogsh.pushAttrib(gl, attrMask);

        if (!dc.isPickingMode())
        {
            dc.beginStandardLighting();
            gl.glEnable(GL.GL_LINE_SMOOTH);
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
        }
        else
        {
            gl.glDisable(GL.GL_LINE_SMOOTH);
            gl.glDisable(GL.GL_BLEND);
        }

        gl.glDisable(GL.GL_CULL_FACE);

        this.BEogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY); // all drawing uses vertex arrays

        dc.getView().pushReferenceCenter(dc, this.getCurrentData().getReferencePoint());

        return this.BEogsh;
    }

    /**
     * Pop the state set in {@link #beginDrawing(DrawContext, int)}.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        dc.getView().popReferenceCenter(dc);

        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY); // explicitly disable normal array client state; fixes WWJ-450

        if (!dc.isPickingMode())
        {
            dc.endStandardLighting();
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }

        this.BEogsh.pop(gl);
    }

    /**
     * Draws this shape's outline.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    protected void drawOutline(DrawContext dc)
    {
        ShapeAttributes activeAttrs = this.getActiveAttributes();

        this.prepareToDrawOutline(dc, activeAttrs, defaultAttributes);

        this.doDrawOutline(dc);
    }

    /**
     * Establishes OpenGL state for drawing the outline, including setting the color/material, line smoothing, line
     * width and stipple. Disables texture.
     *
     * @param dc           the current draw context.
     * @param activeAttrs  the attributes indicating the state value to set.
     * @param defaultAttrs the attributes to use if <code>activeAttrs</code> does not contain a necessary value.
     */
    protected void prepareToDrawOutline(DrawContext dc, ShapeAttributes activeAttrs, ShapeAttributes defaultAttrs)
    {
        if (activeAttrs == null || !activeAttrs.isDrawOutline())
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode())
        {
            Material material = activeAttrs.getOutlineMaterial();
            if (material == null)
                material = defaultAttrs.getOutlineMaterial();

            if (this.mustApplyLighting(dc, activeAttrs))
            {
                material.apply(gl, GL2.GL_FRONT_AND_BACK, (float) activeAttrs.getOutlineOpacity());

                gl.glEnable(GL2.GL_LIGHTING);
                gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            }
            else
            {
                Color sc = material.getDiffuse();
                double opacity = activeAttrs.getOutlineOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));

                gl.glDisable(GL2.GL_LIGHTING);
                gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            }

            gl.glHint(GL.GL_LINE_SMOOTH_HINT, activeAttrs.isEnableAntialiasing() ? GL.GL_NICEST : GL.GL_DONT_CARE);
        }

        if (dc.isPickingMode() && activeAttrs.getOutlineWidth() < this.getOutlinePickWidth())
            gl.glLineWidth(this.getOutlinePickWidth());
        else
            gl.glLineWidth((float) activeAttrs.getOutlineWidth());

        if (activeAttrs.getOutlineStippleFactor() > 0)
        {
            gl.glEnable(GL2.GL_LINE_STIPPLE);
            gl.glLineStipple(activeAttrs.getOutlineStippleFactor(), activeAttrs.getOutlineStipplePattern());
        }
        else
        {
            gl.glDisable(GL2.GL_LINE_STIPPLE);
        }

        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    /**
     * Draws this shape's interior.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    protected void drawInterior(DrawContext dc)
    {
        this.prepareToDrawInterior(dc, this.getActiveAttributes(), defaultAttributes);

        this.doDrawInterior(dc);
    }

    /**
     * Establishes OpenGL state for drawing the interior, including setting the color/material. Enabling texture is left
     * to the subclass.
     *
     * @param dc           the current draw context.
     * @param activeAttrs  the attributes indicating the state value to set.
     * @param defaultAttrs the attributes to use if <code>activeAttrs</code> does not contain a necessary value.
     */
    protected void prepareToDrawInterior(DrawContext dc, ShapeAttributes activeAttrs, ShapeAttributes defaultAttrs)
    {
        if (!activeAttrs.isDrawInterior())
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode())
        {
            Material material = activeAttrs.getInteriorMaterial();
            if (material == null)
                material = defaultAttrs.getInteriorMaterial();

            if (this.mustApplyLighting(dc, activeAttrs))
            {
                material.apply(gl, GL2.GL_FRONT_AND_BACK, (float) activeAttrs.getInteriorOpacity());

                gl.glEnable(GL2.GL_LIGHTING);
                gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            }
            else
            {
                Color sc = material.getDiffuse();
                double opacity = activeAttrs.getInteriorOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));

                gl.glDisable(GL2.GL_LIGHTING);
                gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            }

            if (activeAttrs.getInteriorOpacity() < 1)
                gl.glDepthMask(false);
        }
    }

    /**
     * Computes a model-coordinate point from a position, applying this shape's altitude mode.
     *
     * @param terrain  the terrain to compute a point relative to the globe's surface.
     * @param position the position to compute a point for.
     *
     * @return the model-coordinate point corresponding to the position and this shape's shape type.
     */
    protected Vec4 computePoint(Terrain terrain, Position position)
    {
        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            return terrain.getSurfacePoint(position.getLatitude(), position.getLongitude(), 0d);
        else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            return terrain.getSurfacePoint(position);

        // Raise the shape to accommodate vertical exaggeration applied to the terrain.
        double height = position.getElevation() * terrain.getVerticalExaggeration();

        return terrain.getGlobe().computePointFromPosition(position, height);
    }

    /**
     * Computes a model-coordinate point from a position, applying this shape's altitude mode, and using
     * <code>CLAMP_TO_GROUND</code> if the current globe is 2D.
     *
     * @param dc       the current draw context.
     * @param terrain  the terrain to compute a point relative to the globe's surface.
     * @param position the position to compute a point for.
     *
     * @return the model-coordinate point corresponding to the position and this shape's shape type.
     */
    protected Vec4 computePoint(DrawContext dc, Terrain terrain, Position position)
    {
        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND || dc.is2DGlobe())
            return terrain.getSurfacePoint(position.getLatitude(), position.getLongitude(), 0d);
        else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            return terrain.getSurfacePoint(position);

        // Raise the shape to accommodate vertical exaggeration applied to the terrain.
        double height = position.getElevation() * terrain.getVerticalExaggeration();

        return terrain.getGlobe().computePointFromPosition(position, height);
    }

    /**
     * Computes this shape's approximate extent from its positions.
     *
     * @param globe                the globe to use to compute the extent.
     * @param verticalExaggeration the vertical exaggeration to apply to computed terrain points.
     * @param positions            the positions to compute the extent for.
     *
     * @return the extent, or null if an extent cannot be computed. Null is returned if either <code>globe</code> or
     * <code>positions</code> is null.
     */
    protected Extent computeExtentFromPositions(Globe globe, double verticalExaggeration,
        Iterable<? extends LatLon> positions)
    {
        if (globe == null || positions == null)
            return null;

        Sector mySector = this.getSector();
        if (mySector == null)
            return null;

        double[] extremes;
        double[] minAndMaxElevations = globe.getMinAndMaxElevations(mySector);
        if (this.getAltitudeMode() != WorldWind.CLAMP_TO_GROUND)
        {
            extremes = new double[] {Double.MAX_VALUE, -Double.MAX_VALUE};
            for (LatLon pos : positions)
            {
                double elevation = pos instanceof Position ? ((Position) pos).getElevation() : 0;
                if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
                    elevation += minAndMaxElevations[1];

                if (extremes[0] > elevation)
                    extremes[0] = elevation * verticalExaggeration; // min
                if (extremes[1] < elevation)
                    extremes[1] = elevation * verticalExaggeration; // max
            }
        }
        else
        {
            extremes = minAndMaxElevations;
        }

        return Sector.computeBoundingBox(globe, verticalExaggeration, mySector, extremes[0], extremes[1]);
    }

    /**
     * Get or create OpenGL resource IDs for the current data cache entry.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     *
     * @return an array containing the coordinate vertex buffer ID in the first position and the index vertex buffer ID
     * in the second position.
     */
    protected int[] getVboIds(DrawContext dc)
    {
        return (int[]) dc.getGpuResourceCache().get(this.getCurrentData().getVboCacheKey());
    }

    /**
     * Removes from the GPU resource cache the entry for the current data cache entry's VBOs.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    protected void clearCachedVbos(DrawContext dc)
    {
        dc.getGpuResourceCache().remove(this.getCurrentData().getVboCacheKey());
    }

    protected int countTriangleVertices(java.util.List<java.util.List<Integer>> prims,
        java.util.List<Integer> primTypes)
    {
        int numVertices = 0;

        for (int i = 0; i < prims.size(); i++)
        {
            switch (primTypes.get(i))
            {
                case GL.GL_TRIANGLES:
                    numVertices += prims.get(i).size();
                    break;

                case GL.GL_TRIANGLE_FAN:
                    numVertices += (prims.get(i).size() - 2) * 3; // N tris from N + 2 vertices
                    break;

                case GL.GL_TRIANGLE_STRIP:
                    numVertices += (prims.get(i).size() - 2) * 3; // N tris from N + 2 vertices
                    break;
            }
        }

        return numVertices;
    }

    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position refPos = this.getReferencePosition();

        // The reference position is null if this shape has no positions. In this case moving the shape by a
        // relative delta is meaningless because the shape has no geographic location. Therefore we fail softly by
        // exiting and doing nothing.
        if (refPos == null)
            return;

        this.moveTo(refPos.add(delta));
    }

    @Override
    public void moveTo(Globe globe, Position position)
    {
        this.moveTo(position); // TODO: Update all implementers of this method to use the Movable2 interface
    }

    @Override
    public boolean isDragEnabled()
    {
        return this.dragEnabled;
    }

    @Override
    public void setDragEnabled(boolean enabled)
    {
        this.dragEnabled = enabled;
    }

    @Override
    public void drag(DragContext dragContext)
    {
        if (!this.dragEnabled)
            return;

        if (this.draggableSupport == null)
            this.draggableSupport = new DraggableSupport(this, this.getAltitudeMode());

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragGlobeSizeConstant(dragContext);
    }

    public String isExportFormatSupported(String mimeType)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    public void export(String mimeType, Object output) throws IOException, UnsupportedOperationException
    {
        if (mimeType == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (output == null)
        {
            String message = Logging.getMessage("nullValue.OutputBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String supported = this.isExportFormatSupported(mimeType);
        if (FORMAT_NOT_SUPPORTED.equals(supported))
        {
            String message = Logging.getMessage("Export.UnsupportedFormat", mimeType);
            Logging.logger().warning(message);
            throw new UnsupportedOperationException(message);
        }

        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
        {
            try
            {
                exportAsKML(output);
            }
            catch (XMLStreamException e)
            {
                Logging.logger().throwing(getClass().getName(), "export", e);
                throw new IOException(e);
            }
        }
        else
        {
            String message = Logging.getMessage("Export.UnsupportedFormat", mimeType);
            Logging.logger().warning(message);
            throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Export the placemark to KML as a {@code <Placemark>} element. The {@code output} object will receive the data.
     * This object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @throws IOException        if an exception occurs while exporting the data.
     * @see #export(String, Object)
     */
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        }
        else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        }
        else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }

        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Placemark");

        String property = getStringValue(AVKey.DISPLAY_NAME);
        if (property != null)
        {
            xmlWriter.writeStartElement("name");
            xmlWriter.writeCharacters(property);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeStartElement("visibility");
        xmlWriter.writeCharacters(KMLExportUtil.kmlBoolean(this.isVisible()));
        xmlWriter.writeEndElement();

        String shortDescription = (String) getValue(AVKey.SHORT_DESCRIPTION);
        if (shortDescription != null)
        {
            xmlWriter.writeStartElement("Snippet");
            xmlWriter.writeCharacters(shortDescription);
            xmlWriter.writeEndElement();
        }

        String description = (String) getValue(AVKey.BALLOON_TEXT);
        if (description != null)
        {
            xmlWriter.writeStartElement("description");
            xmlWriter.writeCharacters(description);
            xmlWriter.writeEndElement();
        }

        // KML does not allow separate attributes for cap and side, so just use the cap attributes.
        final ShapeAttributes normalAttributes = getAttributes();
        final ShapeAttributes highlightAttributes = getHighlightAttributes();

        // Write style map
        if (normalAttributes != null || highlightAttributes != null)
        {
            xmlWriter.writeStartElement("StyleMap");
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.NORMAL, normalAttributes);
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.HIGHLIGHT, highlightAttributes);
            xmlWriter.writeEndElement(); // StyleMap
        }

        this.doExportAsKML(xmlWriter);

        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }

    //**************************************************************//
    //*********************       Restorable       *****************//
    //**************************************************************//

    public String getRestorableState()
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
    }

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Method is invoked by subclasses to have superclass add its state and only its state
        this.doMyGetRestorableState(rs, context);
    }

    private void doMyGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        rs.addStateValueAsBoolean(context, "highlighted", this.isHighlighted());
        rs.addStateValueAsBoolean(context, "visible", this.isVisible());
        rs.addStateValueAsInteger(context, "altitudeMode", this.getAltitudeMode());

        this.normalAttrs.getRestorableState(rs, rs.addStateObject(context, "attributes"));
        //this.highlightAttrs.getRestorableState(rs, rs.addStateObject(context, "highlightAttrs"));
    }

    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        this.doRestoreState(rs, null);
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Method is invoked by subclasses to have superclass add its state and only its state
        this.doMyRestoreState(rs, context);
    }

    private void doMyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {

        Boolean booleanState = rs.getStateValueAsBoolean(context, "highlighted");
        if (booleanState != null)
            this.setHighlighted(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "visible");
        if (booleanState != null)
            this.setVisible(booleanState);

        Integer integerState = rs.getStateValueAsInteger(context, "altitudeMode");
        if (integerState != null)
            this.setAltitudeMode(integerState);

        RestorableSupport.StateObject so = rs.getStateObject(context, "attributes");
        if (so != null)
        {
            ShapeAttributes attrs = (this.getAttributes() != null) ? this.getAttributes() : new BasicShapeAttributes();
            attrs.restoreState(rs, so);
            this.setAttributes(attrs);
        }

        /*
        so = rs.getStateObject(context, "highlightAttrs");
        if (so != null)
        {
            ShapeAttributes attrs = (this.getHighlightAttributes() != null) ? this.getHighlightAttributes()
                : new BasicShapeAttributes();
            attrs.restoreState(rs, so);
            this.setHighlightAttributes(attrs);
        }
        */
    }
}
