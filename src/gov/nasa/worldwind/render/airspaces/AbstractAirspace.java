/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.nio.Buffer;
import java.util.*;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: AbstractAirspace.java 3138 2015-06-02 19:13:16Z tgaskins $
 */
public abstract class AbstractAirspace extends WWObjectImpl
    implements Airspace, OrderedRenderable, PreRenderable, Movable, Movable2, Draggable
{
    protected static final String ARC_SLICES = "ArcSlices";
    protected static final String DISABLE_TERRAIN_CONFORMANCE = "DisableTerrainConformance";
    protected static final String EXPIRY_TIME = "ExpiryTime";
    protected static final String GEOMETRY_CACHE_NAME = "Airspace Geometry";
    protected static final String GEOMETRY_CACHE_KEY = Geometry.class.getName();
    protected static final String GLOBE_KEY = "GlobeKey";
    protected static final String LENGTH_SLICES = "LengthSlices";
    protected static final String LOOPS = "Loops";
    protected static final String PILLARS = "Pillars";
    protected static final String SLICES = "Slices";
    protected static final String SPLIT_THRESHOLD = "SplitThreshold";
    protected static final String STACKS = "Stacks";
    protected static final String SUBDIVISIONS = "Subdivisions";
    protected static final String VERTICAL_EXAGGERATION = "VerticalExaggeration";

    private static final long DEFAULT_GEOMETRY_CACHE_SIZE = 16777216L; // 16 megabytes
    /** The default outline pick width. */
    protected static final int DEFAULT_OUTLINE_PICK_WIDTH = 10;

    /** The default interior color. */
    protected static final Material DEFAULT_INTERIOR_MATERIAL = Material.LIGHT_GRAY;
    /** The default outline color. */
    protected static final Material DEFAULT_OUTLINE_MATERIAL = Material.DARK_GRAY;
    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;

    /** The attributes used if attributes are not specified. */
    protected static AirspaceAttributes defaultAttributes;

    static
    {
        // Create and populate the default attributes.
        defaultAttributes = new BasicAirspaceAttributes();
        defaultAttributes.setInteriorMaterial(DEFAULT_INTERIOR_MATERIAL);
        defaultAttributes.setOutlineMaterial(DEFAULT_OUTLINE_MATERIAL);
    }

    // Airspace properties.
    protected boolean visible = true;
    protected boolean highlighted;
    protected boolean dragEnabled = true;
    protected DraggableSupport draggableSupport = null;
    protected AirspaceAttributes attributes;
    protected AirspaceAttributes highlightAttributes;
    protected AirspaceAttributes activeAttributes = new BasicAirspaceAttributes(); // re-determined each frame
    protected double lowerAltitude = 0.0;
    protected double upperAltitude = 1.0;
    protected boolean lowerTerrainConforming = false;
    protected boolean upperTerrainConforming = false;
    protected String lowerAltitudeDatum = AVKey.ABOVE_MEAN_SEA_LEVEL;
    protected String upperAltitudeDatum = AVKey.ABOVE_MEAN_SEA_LEVEL;
    protected LatLon groundReference;
    protected boolean enableLevelOfDetail = true;
    protected Collection<DetailLevel> detailLevels = new TreeSet<DetailLevel>();
    // Rendering properties.
    protected boolean enableBatchRendering = true;
    protected boolean enableBatchPicking = true;
    protected boolean enableDepthOffset;
    protected int outlinePickWidth = DEFAULT_OUTLINE_PICK_WIDTH;
    protected Object delegateOwner;
    protected SurfaceShape surfaceShape;
    protected boolean mustRegenerateSurfaceShape;
    protected boolean drawSurfaceShape;
    protected long frameTimeStamp;
    protected boolean alwaysOnTop = false;
    // Geometry computation and rendering support.
    protected AirspaceInfo currentInfo;
    protected Layer pickLayer;
    protected PickSupport pickSupport = new PickSupport();
    protected GeometryBuilder geometryBuilder = new GeometryBuilder();
    // Geometry update support.
    protected long expiryTime = -1L;
    protected long minExpiryTime = 2000L;
    protected long maxExpiryTime = 6000L;
    protected static Random rand = new Random();
    // Elevation lookup map.
    protected Map<LatLon, Double> elevationMap = new HashMap<LatLon, Double>();
    // Implements the the interface used by the draw context's outlined-shape renderer.
    protected OutlinedShape outlineShapeRenderer = new OutlinedShape()
    {
        public boolean isDrawOutline(DrawContext dc, Object shape)
        {
            return ((AbstractAirspace) shape).mustDrawOutline(dc);
        }

        public boolean isDrawInterior(DrawContext dc, Object shape)
        {
            return ((AbstractAirspace) shape).mustDrawInterior(dc);
        }

        public void drawOutline(DrawContext dc, Object shape)
        {
            ((AbstractAirspace) shape).drawOutline(dc);
        }

        public void drawInterior(DrawContext dc, Object shape)
        {
            ((AbstractAirspace) shape).drawInterior(dc);
        }

        public boolean isEnableDepthOffset(DrawContext dc, Object shape)
        {
            return ((AbstractAirspace) shape).isEnableDepthOffset();
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

    // Airspaces perform about 5% better if their extent is cached, so do that here.

    protected static class AirspaceInfo
    {
        // The extent depends on the state of the globe used to compute it, and the vertical exaggeration.
        protected Extent extent;
        protected double eyeDistance;
        protected List<Vec4> minimalGeometry;
        protected double verticalExaggeration;
        protected Object globeStateKey;

        public AirspaceInfo(DrawContext dc, Extent extent, List<Vec4> minimalGeometry)
        {
            this.extent = extent;
            this.minimalGeometry = minimalGeometry;
            this.verticalExaggeration = dc.getVerticalExaggeration();
            this.globeStateKey = dc.getGlobe().getStateKey(dc);
        }

        public double getEyeDistance()
        {
            return this.eyeDistance;
        }

        public void setEyeDistance(double eyeDistance)
        {
            this.eyeDistance = eyeDistance;
        }

        public boolean isValid(DrawContext dc)
        {
            return this.verticalExaggeration == dc.getVerticalExaggeration()
                && (this.globeStateKey != null && this.globeStateKey.equals(dc.getGlobe().getStateKey(dc)));
        }
    }

    // usually only 1, but few at most
    protected HashMap<GlobeStateKey, AirspaceInfo> airspaceInfo = new HashMap<GlobeStateKey, AirspaceInfo>(2);

    public AbstractAirspace(AirspaceAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AirspaceAttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attributes = attributes;

        if (!WorldWind.getMemoryCacheSet().containsCache(GEOMETRY_CACHE_KEY))
        {
            long size = Configuration.getLongValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE, DEFAULT_GEOMETRY_CACHE_SIZE);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName(GEOMETRY_CACHE_NAME);
            WorldWind.getMemoryCacheSet().addCache(GEOMETRY_CACHE_KEY, cache);
        }
    }

    protected abstract Extent computeExtent(Globe globe, double verticalExaggeration);

    protected abstract List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration);

    public AbstractAirspace(AbstractAirspace source)
    {
        this(source.getAttributes());

        this.visible = source.visible;
        this.attributes = source.attributes;
        this.highlightAttributes = source.highlightAttributes;
        this.lowerAltitude = source.lowerAltitude;
        this.upperAltitude = source.upperAltitude;
        this.lowerAltitudeDatum = source.lowerAltitudeDatum;
        this.upperAltitudeDatum = source.upperAltitudeDatum;
        this.lowerTerrainConforming = source.lowerTerrainConforming;
        this.upperTerrainConforming = source.upperTerrainConforming;
        this.groundReference = source.groundReference;
        this.enableLevelOfDetail = source.enableLevelOfDetail;
        this.enableBatchPicking = source.enableBatchPicking;
        this.enableBatchRendering = source.enableBatchRendering;
        this.enableDepthOffset = source.enableDepthOffset;
        this.outlinePickWidth = source.outlinePickWidth;
        this.delegateOwner = source.delegateOwner;
        this.drawSurfaceShape = source.drawSurfaceShape;
    }

    public AbstractAirspace()
    {
        this(new BasicAirspaceAttributes());
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public AirspaceAttributes getAttributes()
    {
        return this.attributes;
    }

    public void setAttributes(AirspaceAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AirspaceAttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attributes = attributes;
    }

    @Override
    public void setAttributes(ShapeAttributes attributes)
    {
        this.setAttributes(new BasicAirspaceAttributes(attributes));
    }

    @Override
    public void setHighlightAttributes(ShapeAttributes highlightAttributes)
    {
        this.setHighlightAttributes(
            highlightAttributes != null ? new BasicAirspaceAttributes(highlightAttributes) : null);
    }

    @Override
    public AirspaceAttributes getHighlightAttributes()
    {
        return highlightAttributes;
    }

    @Override
    public void setHighlightAttributes(AirspaceAttributes highlightAttrs)
    {
        this.highlightAttributes = highlightAttrs;

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

    public double[] getAltitudes()
    {
        double[] array = new double[2];
        array[0] = this.lowerAltitude;
        array[1] = this.upperAltitude;
        return array;
    }

    protected double[] getAltitudes(double verticalExaggeration)
    {
        double[] array = this.getAltitudes();
        array[0] = array[0] * verticalExaggeration;
        array[1] = array[1] * verticalExaggeration;
        return array;
    }

    public void setAltitudes(double lowerAltitude, double upperAltitude)
    {
        this.lowerAltitude = lowerAltitude;
        this.upperAltitude = upperAltitude;
        this.invalidateAirspaceData();
    }

    public void setAltitude(double altitude)
    {
        this.setAltitudes(altitude, altitude);
    }

    public boolean[] isTerrainConforming()
    {
        // This method is here for backwards compatibility. The new scheme uses enumerations (in the form of Strings).

        boolean[] array = new boolean[2];
        array[0] = this.lowerTerrainConforming;
        array[1] = this.upperTerrainConforming;
        return array;
    }

    public void setTerrainConforming(boolean lowerTerrainConformant, boolean upperTerrainConformant)
    {
        // This method is here for backwards compatibility. The new scheme uses enumerations (in the form of Strings).

        this.lowerTerrainConforming = lowerTerrainConformant;
        this.upperTerrainConforming = upperTerrainConformant;

        this.lowerAltitudeDatum = this.lowerTerrainConforming ? AVKey.ABOVE_GROUND_LEVEL : AVKey.ABOVE_MEAN_SEA_LEVEL;
        this.upperAltitudeDatum = this.upperTerrainConforming ? AVKey.ABOVE_GROUND_LEVEL : AVKey.ABOVE_MEAN_SEA_LEVEL;

        this.invalidateAirspaceData();
    }

    public String[] getAltitudeDatum()
    {
        return new String[] {this.lowerAltitudeDatum, this.upperAltitudeDatum};
    }

    // TODO: The altitude datum logic is currently implemented only for Polygon. Implement it for the rest of them.

    public void setAltitudeDatum(String lowerAltitudeDatum, String upperAltitudeDatum)
    {
        if (lowerAltitudeDatum == null || upperAltitudeDatum == null)
        {
            String message = Logging.getMessage("nullValue.AltitudeDatumIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.lowerAltitudeDatum = lowerAltitudeDatum;
        this.upperAltitudeDatum = upperAltitudeDatum;

        if (lowerAltitudeDatum.equals(AVKey.ABOVE_GROUND_LEVEL) || lowerAltitudeDatum.equals(
            AVKey.ABOVE_GROUND_REFERENCE))
            this.lowerTerrainConforming = true;

        if (upperAltitudeDatum.equals(AVKey.ABOVE_GROUND_LEVEL) || upperAltitudeDatum.equals(
            AVKey.ABOVE_GROUND_REFERENCE))
            this.upperTerrainConforming = true;

        this.invalidateAirspaceData();
    }

    public LatLon getGroundReference()
    {
        return this.groundReference;
    }

    public void setGroundReference(LatLon groundReference)
    {
        this.groundReference = groundReference;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnableBatchRendering()
    {
        return this.enableBatchRendering;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableBatchRendering(boolean enableBatchRendering)
    {
        this.enableBatchRendering = enableBatchRendering;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnableBatchPicking()
    {
        return this.enableBatchPicking;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableBatchPicking(boolean enableBatchPicking)
    {
        this.enableBatchPicking = enableBatchPicking;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnableDepthOffset()
    {
        return this.enableDepthOffset;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableDepthOffset(boolean enableDepthOffset)
    {
        this.enableDepthOffset = enableDepthOffset;
    }

    /** {@inheritDoc} */
    @Override
    public int getOutlinePickWidth()
    {
        return this.outlinePickWidth;
    }

    /** {@inheritDoc} */
    @Override
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

    @Override
    public Object getDelegateOwner()
    {
        return this.delegateOwner;
    }

    @Override
    public void setDelegateOwner(Object delegateOwner)
    {
        this.delegateOwner = delegateOwner;
    }

    @Override
    public boolean isAlwaysOnTop()
    {
        return alwaysOnTop;
    }

    @Override
    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        this.alwaysOnTop = alwaysOnTop;
    }

    @Override
    public boolean isDrawSurfaceShape()
    {
        return drawSurfaceShape;
    }

    @Override
    public void setDrawSurfaceShape(boolean drawSurfaceShape)
    {
        this.drawSurfaceShape = drawSurfaceShape;
    }

    protected void adjustForGroundReference(DrawContext dc, boolean[] terrainConformant, double[] altitudes,
        LatLon groundRef)
    {
        if (groundRef == null)
            return; // Can't apply the datum without a reference point.

        for (int i = 0; i < 2; i++)
        {
            if (this.getAltitudeDatum()[i].equals(AVKey.ABOVE_GROUND_REFERENCE))
            {
                altitudes[i] += this.computeElevationAt(dc, groundRef.getLatitude(), groundRef.getLongitude());
                terrainConformant[i] = false;
            }
        }
    }

    public boolean isAirspaceCollapsed()
    {
        return this.lowerAltitude == this.upperAltitude && this.lowerTerrainConforming == this.upperTerrainConforming;
    }

    public void setTerrainConforming(boolean terrainConformant)
    {
        this.setTerrainConforming(terrainConformant, terrainConformant);
    }

    public boolean isEnableLevelOfDetail()
    {
        return this.enableLevelOfDetail;
    }

    public void setEnableLevelOfDetail(boolean enableLevelOfDetail)
    {
        this.enableLevelOfDetail = enableLevelOfDetail;
    }

    public Iterable<DetailLevel> getDetailLevels()
    {
        return this.detailLevels;
    }

    public void setDetailLevels(Collection<DetailLevel> detailLevels)
    {
        this.detailLevels.clear();
        this.addDetailLevels(detailLevels);
    }

    protected void addDetailLevels(Collection<DetailLevel> newDetailLevels)
    {
        if (newDetailLevels != null)
            for (DetailLevel level : newDetailLevels)
            {
                if (level != null)
                    this.detailLevels.add(level);
            }
    }

    /** {@inheritDoc} */
    public boolean isAirspaceVisible(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getView() == null)
        {
            String message = "nullValue.DrawingContextViewIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // A null extent indicates an airspace which has no geometry.
        Extent extent = this.getExtent(dc);
        if (extent == null)
            return false;

        // Test this airspace's extent against the pick frustum list.
        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(extent);

        // Test this airspace's extent against the viewing frustum.
        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.computeExtent(globe, verticalExaggeration);
    }

    public Extent getExtent(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.getAirspaceInfo(dc).extent;
    }

    protected AirspaceInfo getAirspaceInfo(DrawContext dc)
    {
        AirspaceInfo info = this.airspaceInfo.get(dc.getGlobe().getGlobeStateKey());

        if (info == null || !info.isValid(dc))
        {
            info = new AirspaceInfo(dc, this.computeExtent(dc), this.computeMinimalGeometry(dc));
            this.airspaceInfo.put(dc.getGlobe().getGlobeStateKey(), info);
        }

        return info;
    }

    protected Extent computeExtent(DrawContext dc)
    {
        return this.getExtent(dc.getGlobe(), dc.getVerticalExaggeration());
    }

    protected List<Vec4> computeMinimalGeometry(DrawContext dc)
    {
        return this.computeMinimalGeometry(dc.getGlobe(), dc.getVerticalExaggeration());
    }

    protected void invalidateAirspaceData()
    {
        this.airspaceInfo.clear(); // Doesn't hurt to remove all cached extents because re-creation is cheap
        this.mustRegenerateSurfaceShape = true;
    }

    @Override
    public double getDistanceFromEye()
    {
        return this.isAlwaysOnTop() ? 0 : this.currentInfo.getEyeDistance();
    }

    /**
     * Determines which attributes -- normal, highlight or default -- to use each frame. Places the result in this
     * shape's current active attributes.
     *
     * @see #getActiveAttributes()
     */
    protected void determineActiveAttributes(DrawContext dc)
    {
        if (this.frameTimeStamp == dc.getFrameTimeStamp())
            return;

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
     * #determineActiveAttributes(gov.nasa.worldwind.render.DrawContext)}. The active attributes are either the normal
     * or highlight attributes, depending on this shape's highlight flag, and incorporates default attributes for those
     * not specified in the applicable attribute set.
     *
     * @return this shape's currently active attributes.
     */
    public AirspaceAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    @Override
    public void preRender(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isVisible())
            return;

        if (dc.is2DGlobe() || this.isDrawSurfaceShape())
        {
            if (this.surfaceShape == null)
            {
                this.surfaceShape = this.createSurfaceShape();
                this.mustRegenerateSurfaceShape = true;
                if (this.surfaceShape == null)
                    return;
            }

            if (this.mustRegenerateSurfaceShape)
            {
                this.regenerateSurfaceShape(dc, this.surfaceShape);
                this.mustRegenerateSurfaceShape = false;
            }

            this.updateSurfaceShape(dc, this.surfaceShape);
            this.surfaceShape.preRender(dc);
        }
    }

    /**
     * Returns a {@link SurfaceShape} that corresponds to this Airspace and is used for drawing on 2D globes.
     *
     * @return The surface shape to represent this Airspace on a 2D globe.
     */
    protected SurfaceShape createSurfaceShape()
    {
        return null;
    }

    /**
     * Sets surface shape parameters prior to picking and rendering the 2D shape used to represent this Airspace on 2D
     * globes. Subclasses should override this method if they need to update more than the attributes and the delegate
     * owner.
     *
     * @param dc    the current drawing context.
     * @param shape the surface shape to update.
     */
    protected void updateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        this.determineActiveAttributes(dc);
        ShapeAttributes attrs = this.getActiveAttributes();
        if (shape.getAttributes() == null)
            shape.setAttributes(new BasicShapeAttributes(attrs));
        else
            shape.getAttributes().copy(attrs);

        Object o = this.getDelegateOwner();
        shape.setDelegateOwner(o != null ? o : this);

        boolean b = this.isEnableBatchPicking();
        shape.setEnableBatchPicking(b);
    }

    /**
     * Regenerates surface shape geometry prior to picking and rendering the 2D shape used to represent this Airspace on
     * 2D globes.
     *
     * @param dc    the current drawing context.
     * @param shape the surface shape to regenerate.
     */
    protected void regenerateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        // Intentionally left blank.
    }

    @Override
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

        if (!this.isVisible())
            return;

        if ((dc.is2DGlobe() || this.isDrawSurfaceShape()) && this.surfaceShape != null)
        {
            this.surfaceShape.render(dc);
            return;
        }

        this.currentInfo = this.getAirspaceInfo(dc);

        if (!this.isAirspaceVisible(dc))
            return;

        if (dc.isOrderedRenderingMode())
            this.drawOrderedRenderable(dc);
        else
            this.makeOrderedRenderable(dc);

        this.frameTimeStamp = dc.getFrameTimeStamp();
    }

    protected void makeOrderedRenderable(DrawContext dc)
    {
        this.determineActiveAttributes(dc);

        double eyeDistance = this.computeEyeDistance(dc);
        this.currentInfo.setEyeDistance(eyeDistance);

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();

        dc.addOrderedRenderable(this);
    }

    protected void drawOrderedRenderable(DrawContext dc)
    {
        this.beginRendering(dc);
        try
        {
            this.doDrawOrderedRenderable(dc, this.pickSupport);
            if (this.isEnableBatchRendering())
            {
                this.drawBatched(dc);
            }
        }
        finally
        {
            this.endRendering(dc);
        }
    }

    protected void drawBatched(DrawContext dc)
    {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode())
        {
            while (nextItem instanceof AbstractAirspace)
            {
                AbstractAirspace airspace = (AbstractAirspace) nextItem;
                if (!airspace.isEnableBatchRendering())
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                airspace.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
        else if (this.isEnableBatchPicking())
        {
            while (nextItem instanceof AbstractAirspace)
            {
                AbstractAirspace airspace = (AbstractAirspace) nextItem;
                if (!airspace.isEnableBatchRendering() || !airspace.isEnableBatchPicking())
                    break;

                if (airspace.pickLayer != this.pickLayer) // batch pick only within a single layer
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                airspace.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
    }

    protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates)
    {
        if (dc.isPickingMode())
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            Color pickColor = dc.getUniquePickColor();
            pickCandidates.addPickableObject(this.createPickedObject(pickColor.getRGB()));
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }

        dc.drawOutlinedShape(this.outlineShapeRenderer, this);
    }

    protected PickedObject createPickedObject(int colorCode)
    {
        return new PickedObject(colorCode, this.getDelegateOwner() != null ? this.getDelegateOwner() : this);
    }

    public void move(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position referencePos = this.getReferencePosition();
        if (referencePos == null)
            return;

        this.moveTo(referencePos.add(position));
    }

    @Override
    public void moveTo(Globe globe, Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position oldRef = this.getReferencePosition();
        if (oldRef == null)
            return;

        //noinspection UnnecessaryLocalVariable
        Position newRef = position;
        this.doMoveTo(globe, oldRef, newRef);
    }

    @Override
    public boolean isDragEnabled()
    {
        return this.dragEnabled;
    }

    @Override
    public void setDragEnabled(boolean enabled)
    {
        this.dragEnabled = true;
    }

    @Override
    public void drag(DragContext dragContext)
    {
        if (!this.dragEnabled)
            return;

        if (this.draggableSupport == null)
            this.draggableSupport = new DraggableSupport(this, this.isTerrainConforming()[0]
                ? WorldWind.RELATIVE_TO_GROUND : WorldWind.ABSOLUTE);

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragGlobeSizeConstant(dragContext);
    }

    protected void doMoveTo(Globe globe, Position oldRef, Position newRef)
    {
        this.doMoveTo(oldRef, newRef);
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position oldRef = this.getReferencePosition();
        if (oldRef == null)
            return;

        //noinspection UnnecessaryLocalVariable
        Position newRef = position;
        this.doMoveTo(oldRef, newRef);
    }

    protected void doMoveTo(Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] altitudes = this.getAltitudes();
        double elevDelta = newRef.getElevation() - oldRef.getElevation();
        this.setAltitudes(altitudes[0] + elevDelta, altitudes[1] + elevDelta);
    }

    protected Position computeReferencePosition(List<? extends LatLon> locations, double[] altitudes)
    {
        if (locations == null)
        {
            String message = "nullValue.LocationsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (altitudes == null)
        {
            String message = "nullValue.AltitudesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int count = locations.size();
        if (count == 0)
            return null;

        LatLon ll;
        if (count < 3)
            ll = locations.get(0);
        else
            ll = locations.get(count / 2);

        return new Position(ll, altitudes[0]);
    }

    protected double computeEyeDistance(DrawContext dc)
    {
        AirspaceInfo info = this.currentInfo;
        if (info == null || info.minimalGeometry == null || info.minimalGeometry.isEmpty())
            return 0.0;

        double minDistanceSquared = Double.MAX_VALUE;
        Vec4 eyePoint = dc.getView().getEyePoint();

        for (Vec4 point : info.minimalGeometry)
        {
            double d = point.distanceToSquared3(eyePoint);

            if (d < minDistanceSquared)
                minDistanceSquared = d;
        }

        return Math.sqrt(minDistanceSquared);
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    protected abstract void doRenderGeometry(DrawContext dc, String drawStyle);

    protected void beginRendering(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

        if (!dc.isPickingMode())
        {
            int attribMask = GL2.GL_COLOR_BUFFER_BIT  // For color write mask, blending src and func, alpha func.
                | GL2.GL_CURRENT_BIT // For current color.
                | GL2.GL_LINE_BIT // For line width, line smoothing, line stipple.
                | GL2.GL_POLYGON_BIT // For polygon offset.
                | GL2.GL_TRANSFORM_BIT; // For matrix mode.
            gl.glPushAttrib(attribMask);

            // Setup blending for non-premultiplied colors.
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);

            // Setup standard lighting by default. This must be disabled by airspaces that don't enable lighting.
            dc.beginStandardLighting();
        }
        else
        {
            int attribMask = GL2.GL_CURRENT_BIT // For current color.
                | GL2.GL_LINE_BIT; // For line width.
            gl.glPushAttrib(attribMask);
        }
    }

    protected void endRendering(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

        if (!dc.isPickingMode())
        {
            dc.endStandardLighting();
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY); // may have been enabled during rendering
        }

        gl.glPopAttrib();
    }

    @SuppressWarnings("UnusedParameters")
    protected boolean mustDrawInterior(DrawContext dc)
    {
        return this.getActiveAttributes().isDrawInterior();
    }

    protected void drawInterior(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        AirspaceAttributes attrs = this.getActiveAttributes();

        if (!dc.isPickingMode())
        {
            if (attrs.isEnableLighting()) // Enable GL lighting state and set the current GL material state.
            {
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
                attrs.getInteriorMaterial().apply(gl, GL2.GL_FRONT_AND_BACK, (float) attrs.getInteriorOpacity());
            }
            else // Disable GL lighting state and set the current GL color state.
            {
                gl.glDisable(GL2.GL_LIGHTING);
                gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
                Color sc = attrs.getInteriorMaterial().getDiffuse();
                double opacity = attrs.getInteriorOpacity();
                gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                    (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));
            }
        }

        this.doRenderGeometry(dc, Airspace.DRAW_STYLE_FILL);
    }

    @SuppressWarnings("UnusedParameters")
    protected boolean mustDrawOutline(DrawContext dc)
    {
        return this.getActiveAttributes().isDrawOutline();
    }

    protected void drawOutline(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        AirspaceAttributes attrs = this.getActiveAttributes();

        if (!dc.isPickingMode())
        {
            // Airspace outlines do not apply lighting.
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
            Color sc = attrs.getOutlineMaterial().getDiffuse();
            double opacity = attrs.getOutlineOpacity();
            gl.glColor4ub((byte) sc.getRed(), (byte) sc.getGreen(), (byte) sc.getBlue(),
                (byte) (opacity < 1 ? (int) (opacity * 255 + 0.5) : 255));

            if (attrs.isEnableAntialiasing())
            {
                gl.glEnable(GL.GL_LINE_SMOOTH);
            }
            else
            {
                gl.glDisable(GL.GL_LINE_SMOOTH);
            }
        }

        if (dc.isPickingMode() && attrs.getOutlineWidth() < this.getOutlinePickWidth())
            gl.glLineWidth(this.getOutlinePickWidth());
        else
            gl.glLineWidth((float) attrs.getOutlineWidth());

        if (attrs.getOutlineStippleFactor() > 0)
        {
            gl.glEnable(GL2.GL_LINE_STIPPLE);
            gl.glLineStipple(attrs.getOutlineStippleFactor(), attrs.getOutlineStipplePattern());
        }
        else
        {
            gl.glDisable(GL2.GL_LINE_STIPPLE);
        }

        this.doRenderGeometry(dc, Airspace.DRAW_STYLE_OUTLINE);
    }

    protected void drawGeometry(DrawContext dc, Geometry indices, Geometry vertices)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        AirspaceAttributes attrs = this.getActiveAttributes();

        int size = vertices.getSize(Geometry.VERTEX);
        int type = vertices.getGLType(Geometry.VERTEX);
        int stride = vertices.getStride(Geometry.VERTEX);
        Buffer buffer = vertices.getBuffer(Geometry.VERTEX);
        gl.glVertexPointer(size, type, stride, buffer);

        if (!dc.isPickingMode() && attrs.isEnableLighting())
        {
            type = vertices.getGLType(Geometry.NORMAL);
            stride = vertices.getStride(Geometry.NORMAL);
            buffer = vertices.getBuffer(Geometry.NORMAL);
            gl.glNormalPointer(type, stride, buffer);
        }

        // On some hardware, using glDrawRangeElements allows vertex data to be prefetched. We know the minimum and
        // maximum index values that are valid in elementBuffer (they are 0 and vertexCount-1), so it's harmless
        // to use this approach and allow the hardware to optimize.
        int mode = indices.getMode(Geometry.ELEMENT);
        int count = indices.getCount(Geometry.ELEMENT);
        type = indices.getGLType(Geometry.ELEMENT);
        int minElementIndex = 0;
        int maxElementIndex = vertices.getCount(Geometry.VERTEX) - 1;
        buffer = indices.getBuffer(Geometry.ELEMENT);
        gl.glDrawRangeElements(mode, minElementIndex, maxElementIndex, count, type, buffer);
    }

    protected GeometryBuilder getGeometryBuilder()
    {
        return this.geometryBuilder;
    }

    protected void setGeometryBuilder(GeometryBuilder gb)
    {
        if (gb == null)
        {
            String message = "nullValue.GeometryBuilderIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.geometryBuilder = gb;
    }

    protected DetailLevel computeDetailLevel(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterable<DetailLevel> detailLevels = this.getDetailLevels();
        if (detailLevels == null)
            return null;

        Iterator<DetailLevel> iter = detailLevels.iterator();
        if (!iter.hasNext())
            return null;

        // Find the first detail level that meets rendering criteria.
        DetailLevel level = iter.next();
        while (iter.hasNext() && !level.meetsCriteria(dc, this))
        {
            level = iter.next();
        }

        return level;
    }

    protected MemoryCache getGeometryCache()
    {
        return WorldWind.getMemoryCache(GEOMETRY_CACHE_KEY);
    }

    protected boolean isExpired(DrawContext dc, Geometry geom)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (geom == null)
        {
            String message = "nullValue.AirspaceGeometryIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object o = geom.getValue(EXPIRY_TIME);
        if (o != null && o instanceof Long && dc.getFrameTimeStamp() > (Long) o)
            return true;

        o = geom.getValue(GLOBE_KEY);
        //noinspection RedundantIfStatement
        if (o != null && !dc.getGlobe().getStateKey(dc).equals(o))
            return true;

        return false;
    }

    protected void updateExpiryCriteria(DrawContext dc, Geometry geom)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        long expiryTime = this.getExpiryTime();
        geom.setValue(EXPIRY_TIME, (expiryTime >= 0L) ? expiryTime : null);
        geom.setValue(GLOBE_KEY, dc.getGlobe().getStateKey(dc));
    }

    protected long getExpiryTime()
    {
        return this.expiryTime;
    }

    protected void setExpiryTime(long timeMillis)
    {
        this.expiryTime = timeMillis;
    }

    protected long[] getExpiryRange()
    {
        long[] array = new long[2];
        array[0] = this.minExpiryTime;
        array[1] = this.maxExpiryTime;
        return array;
    }

    protected void setExpiryRange(long minTimeMillis, long maxTimeMillis)
    {
        this.minExpiryTime = minTimeMillis;
        this.maxExpiryTime = maxTimeMillis;
    }

    protected long nextExpiryTime(DrawContext dc, boolean[] terrainConformance)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        long expiryTime;
        if (terrainConformance[0] || terrainConformance[1])
        {
            long time = nextLong(this.minExpiryTime, this.maxExpiryTime);
            expiryTime = dc.getFrameTimeStamp() + time;
        }
        else
        {
            expiryTime = -1L;
        }
        return expiryTime;
    }

    private static long nextLong(long lo, long hi)
    {
        long n = hi - lo + 1;
        long i = rand.nextLong() % n;
        return lo + ((i < 0) ? -i : i);
    }

    protected void clearElevationMap()
    {
        this.elevationMap.clear();
    }

    public Vec4 computePointFromPosition(DrawContext dc, Angle latitude, Angle longitude, double elevation,
        boolean terrainConformant)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double newElevation = elevation;

        if (terrainConformant)
        {
            newElevation += this.computeElevationAt(dc, latitude, longitude);
        }

        return dc.getGlobe().computePointFromPosition(latitude, longitude, newElevation);
    }

    protected double computeElevationAt(DrawContext dc, Angle latitude, Angle longitude)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Globe globe;
        LatLon latlon;
        Vec4 surfacePoint;
        Position surfacePos;
        Double elevation;

        latlon = new LatLon(latitude, longitude);
        elevation = this.elevationMap.get(latlon);

        if (elevation == null)
        {
            globe = dc.getGlobe();
            elevation = 0.0;

            surfacePoint = dc.getPointOnTerrain(latitude, longitude);
            if (surfacePoint != null)
            {
                surfacePos = globe.computePositionFromPoint(surfacePoint);
                elevation += surfacePos.getElevation();
            }
            else
            {
                elevation += dc.getVerticalExaggeration() * globe.getElevation(latitude, longitude);
            }

            this.elevationMap.put(latlon, elevation);
        }

        return elevation;
    }

    protected void makeExtremePoints(Globe globe, double verticalExaggeration, Iterable<? extends LatLon> locations,
        List<Vec4> extremePoints)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (locations == null)
        {
            String message = "nullValue.LocationsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] altitudes = this.getAltitudes();
        boolean[] terrainConformant = this.isTerrainConforming();

        // If terrain conformance is enabled, add the minimum or maximum elevations around the locations to the
        // airspace's altitudes.
        if (terrainConformant[0] || terrainConformant[1])
        {
            double[] extremeElevations = new double[2];

            if (LatLon.locationsCrossDateLine(locations))
            {
                Sector[] splitSector = Sector.splitBoundingSectors(locations);
                double[] a = globe.getMinAndMaxElevations(splitSector[0]);
                double[] b = globe.getMinAndMaxElevations(splitSector[1]);
                extremeElevations[0] = Math.min(a[0], b[0]); // Take the smallest min elevation.
                extremeElevations[1] = Math.max(a[1], b[1]); // Take the largest max elevation.
            }
            else
            {
                Sector sector = Sector.boundingSector(locations);
                extremeElevations = globe.getMinAndMaxElevations(sector);
            }

            if (terrainConformant[0])
                altitudes[0] += extremeElevations[0];

            if (terrainConformant[1])
                altitudes[1] += extremeElevations[1];
        }

        // Get the points corresponding to the given locations at the lower and upper altitudes.
        for (LatLon ll : locations)
        {
            extremePoints.add(globe.computePointFromPosition(ll.getLatitude(), ll.getLongitude(),
                verticalExaggeration * altitudes[0]));
            extremePoints.add(globe.computePointFromPosition(ll.getLatitude(), ll.getLongitude(),
                verticalExaggeration * altitudes[1]));
        }
    }

    //**************************************************************//
    //******************** END Geometry Rendering  *****************//
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
        rs.addStateValueAsBoolean(context, "visible", this.isVisible());
        rs.addStateValueAsBoolean(context, "highlighted", this.isHighlighted());
        rs.addStateValueAsDouble(context, "lowerAltitude", this.getAltitudes()[0]);
        rs.addStateValueAsDouble(context, "upperAltitude", this.getAltitudes()[1]);
        rs.addStateValueAsBoolean(context, "lowerTerrainConforming", this.isTerrainConforming()[0]);
        rs.addStateValueAsBoolean(context, "upperTerrainConforming", this.isTerrainConforming()[1]);
        rs.addStateValueAsString(context, "lowerAltitudeDatum", this.getAltitudeDatum()[0]);
        rs.addStateValueAsString(context, "upperAltitudeDatum", this.getAltitudeDatum()[1]);
        if (this.getGroundReference() != null)
            rs.addStateValueAsLatLon(context, "groundReference", this.getGroundReference());
        rs.addStateValueAsBoolean(context, "enableBatchRendering", this.isEnableBatchRendering());
        rs.addStateValueAsBoolean(context, "enableBatchPicking", this.isEnableBatchPicking());
        rs.addStateValueAsBoolean(context, "enableDepthOffset", this.isEnableDepthOffset());
        rs.addStateValueAsInteger(context, "outlinePickWidth", this.getOutlinePickWidth());
        rs.addStateValueAsBoolean(context, "alwaysOnTop", this.isAlwaysOnTop());
        rs.addStateValueAsBoolean(context, "drawSurfaceShape", this.isDrawSurfaceShape());
        rs.addStateValueAsBoolean(context, "enableLevelOfDetail", this.isEnableLevelOfDetail());

        if (this.attributes != null)
            this.attributes.getRestorableState(rs, rs.addStateObject(context, "attributes"));

        if (this.highlightAttributes != null)
            this.highlightAttributes.getRestorableState(rs, rs.addStateObject(context, "highlightAttributes"));
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
        Boolean booleanState = rs.getStateValueAsBoolean(context, "visible");
        if (booleanState != null)
            this.setVisible(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "highlighted");
        if (booleanState != null)
            this.setHighlighted(booleanState);

        Double lo = rs.getStateValueAsDouble(context, "lowerAltitude");
        if (lo == null)
            lo = this.getAltitudes()[0];

        Double hi = rs.getStateValueAsDouble(context, "upperAltitude");
        if (hi == null)
            hi = this.getAltitudes()[1];

        this.setAltitudes(lo, hi);

        Boolean loConform = rs.getStateValueAsBoolean(context, "lowerTerrainConforming");
        if (loConform == null)
            loConform = this.isTerrainConforming()[0];

        Boolean hiConform = rs.getStateValueAsBoolean(context, "upperTerrainConforming");
        if (hiConform == null)
            hiConform = this.isTerrainConforming()[1];

        this.setTerrainConforming(loConform, hiConform);

        String lowerDatum = rs.getStateValueAsString(context, "lowerAltitudeDatum");
        if (lowerDatum == null)
            lowerDatum = this.getAltitudeDatum()[0];

        String upperDatum = rs.getStateValueAsString(context, "upperAltitudeDatum");
        if (upperDatum == null)
            upperDatum = this.getAltitudeDatum()[1];

        this.setAltitudeDatum(lowerDatum, upperDatum);

        LatLon groundRef = rs.getStateValueAsLatLon(context, "groundReference");
        if (groundRef != null)
            this.setGroundReference(groundRef);

        booleanState = rs.getStateValueAsBoolean(context, "enableBatchRendering");
        if (booleanState != null)
            this.setEnableBatchRendering(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "enableBatchPicking");
        if (booleanState != null)
            this.setEnableBatchPicking(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "enableDepthOffset");
        if (booleanState != null)
            this.setEnableDepthOffset(booleanState);

        Integer intState = rs.getStateValueAsInteger(context, "outlinePickWidth");
        if (intState != null)
            this.setOutlinePickWidth(intState);

        booleanState = rs.getStateValueAsBoolean(context, "alwaysOnTop");
        if (booleanState != null)
            this.setAlwaysOnTop(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "drawSurfaceShape");
        if (booleanState != null)
            this.setDrawSurfaceShape(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "enableLevelOfDetail");
        if (booleanState != null)
            this.setEnableLevelOfDetail(booleanState);

        RestorableSupport.StateObject so = rs.getStateObject(context, "attributes");
        if (so != null)
            this.getAttributes().restoreState(rs, so);

        so = rs.getStateObject(context, "highlightAttributes");
        if (so != null)
            this.getHighlightAttributes().restoreState(rs, so);
    }
}
