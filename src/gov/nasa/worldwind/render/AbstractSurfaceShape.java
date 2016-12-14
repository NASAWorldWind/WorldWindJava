/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.combine.*;
import gov.nasa.worldwind.util.measure.AreaMeasurer;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;

/**
 * Common superclass for surface conforming shapes such as {@link gov.nasa.worldwind.render.SurfacePolygon}, {@link
 * gov.nasa.worldwind.render.SurfacePolyline}, {@link gov.nasa.worldwind.render.SurfaceEllipse}, {@link
 * gov.nasa.worldwind.render.SurfaceQuad}, and {@link gov.nasa.worldwind.render.SurfaceSector}.
 * <p/>
 * SurfaceShapes have separate attributes for normal display and highlighted display. If no attributes are specified,
 * default attributes are used. See {@link #DEFAULT_INTERIOR_MATERIAL}, {@link #DEFAULT_OUTLINE_MATERIAL}, and {@link
 * #DEFAULT_HIGHLIGHT_MATERIAL}.
 * <p/>
 * AbstractSurfaceShape extends from {@link gov.nasa.worldwind.render.AbstractSurfaceObject}, and therefore inherits
 * AbstractSurfaceObject's batch rendering capabilities.
 *
 * @author dcollins
 * @version $Id: AbstractSurfaceShape.java 3240 2015-06-22 23:38:49Z tgaskins $
 */
public abstract class AbstractSurfaceShape extends AbstractSurfaceObject implements SurfaceShape, Movable, Movable2,
    Combinable, Draggable
{
    /** The default interior color. */
    protected static final Material DEFAULT_INTERIOR_MATERIAL = Material.LIGHT_GRAY;
    /** The default outline color. */
    protected static final Material DEFAULT_OUTLINE_MATERIAL = Material.DARK_GRAY;
    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;
    /** The default path type. */
    protected static final String DEFAULT_PATH_TYPE = AVKey.GREAT_CIRCLE;
    /** The default number of texels per shape edge interval. */
    protected static final int DEFAULT_TEXELS_PER_EDGE_INTERVAL = 50;
    /** The default minimum number of shape edge intervals. */
    protected static final int DEFAULT_MIN_EDGE_INTERVALS = 0;
    /** The default maximum number of shape edge intervals. */
    protected static final int DEFAULT_MAX_EDGE_INTERVALS = 100;
    /** The attributes used if attributes are not specified. */
    protected static final ShapeAttributes defaultAttrs;

    static
    {
        defaultAttrs = new BasicShapeAttributes();
        defaultAttrs.setInteriorMaterial(DEFAULT_INTERIOR_MATERIAL);
        defaultAttrs.setOutlineMaterial(DEFAULT_OUTLINE_MATERIAL);
    }

    // Public interface properties.
    protected boolean highlighted;
    protected boolean dragEnabled = true;
    protected DraggableSupport draggableSupport = null;
    protected ShapeAttributes normalAttrs;
    protected ShapeAttributes highlightAttrs;
    protected ShapeAttributes activeAttrs = this.createActiveAttributes(); // re-determined each frame
    protected String pathType = DEFAULT_PATH_TYPE;
    protected double texelsPerEdgeInterval = DEFAULT_TEXELS_PER_EDGE_INTERVAL;
    protected int minEdgeIntervals = DEFAULT_MIN_EDGE_INTERVALS;
    protected int maxEdgeIntervals = DEFAULT_MAX_EDGE_INTERVALS;
    // Rendering properties.
    protected List<List<LatLon>> activeGeometry = new ArrayList<List<LatLon>>(); // re-determined each frame
    protected List<List<LatLon>> activeOutlineGeometry = new ArrayList<List<LatLon>>(); // re-determined each frame
    protected WWTexture texture; // An optional texture.
    protected Map<Object, CacheEntry> sectorCache = new HashMap<Object, CacheEntry>();
    protected Map<Object, CacheEntry> geometryCache = new HashMap<Object, CacheEntry>();
    protected OGLStackHandler stackHandler = new OGLStackHandler();
    protected static FloatBuffer vertexBuffer;
    // Measurement properties.
    protected AreaMeasurer areaMeasurer;
    protected long areaMeasurerLastModifiedTime;

    /** Constructs a new surface shape with the default attributes. */
    public AbstractSurfaceShape()
    {
    }

    /**
     * Constructs a new surface shape with the specified normal (as opposed to highlight) attributes. Modifying the
     * attribute reference after calling this constructor causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public AbstractSurfaceShape(ShapeAttributes normalAttrs)
    {
        this.setAttributes(normalAttrs);
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public AbstractSurfaceShape(AbstractSurfaceShape source)
    {
        super(source);

        this.highlighted = source.highlighted;
        this.normalAttrs = source.normalAttrs;
        this.highlightAttrs = source.highlightAttrs;
        this.pathType = source.pathType;
        this.texelsPerEdgeInterval = source.texelsPerEdgeInterval;
        this.minEdgeIntervals = source.minEdgeIntervals;
        this.maxEdgeIntervals = source.maxEdgeIntervals;
    }

    /** {@inheritDoc} */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /** {@inheritDoc} */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
        this.updateModifiedTime();
    }

    /** {@inheritDoc} */
    public ShapeAttributes getAttributes()
    {
        return this.normalAttrs;
    }

    /** {@inheritDoc} */
    public void setAttributes(ShapeAttributes normalAttrs)
    {
        this.normalAttrs = normalAttrs;
        this.updateModifiedTime();
    }

    /** {@inheritDoc} */
    public ShapeAttributes getHighlightAttributes()
    {
        return highlightAttrs;
    }

    /** {@inheritDoc} */
    public void setHighlightAttributes(ShapeAttributes highlightAttrs)
    {
        this.highlightAttrs = highlightAttrs;
        this.updateModifiedTime();
    }

    public String getPathType()
    {
        return this.pathType;
    }

    public void setPathType(String pathType)
    {
        if (pathType == null)
        {
            String message = Logging.getMessage("nullValue.PathTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pathType = pathType;
        this.onShapeChanged();
    }

    public double getTexelsPerEdgeInterval()
    {
        return this.texelsPerEdgeInterval;
    }

    public void setTexelsPerEdgeInterval(double texelsPerEdgeInterval)
    {
        if (texelsPerEdgeInterval <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "texelsPerEdgeInterval <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.texelsPerEdgeInterval = texelsPerEdgeInterval;
        this.onShapeChanged();
    }

    public int[] getMinAndMaxEdgeIntervals()
    {
        return new int[] {this.minEdgeIntervals, this.maxEdgeIntervals};
    }

    public void setMinAndMaxEdgeIntervals(int minEdgeIntervals, int maxEdgeIntervals)
    {
        if (minEdgeIntervals < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "minEdgeIntervals < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (maxEdgeIntervals < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "maxEdgeIntervals < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minEdgeIntervals = minEdgeIntervals;
        this.maxEdgeIntervals = maxEdgeIntervals;
        this.onShapeChanged();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The returned state key is constructed the SurfaceShape's unique ID, last modified time, and its active
     * attributes. The returned state key has no dependency on the {@link gov.nasa.worldwind.globes.Globe}. Subclasses
     * that depend on the Globe should return a state key that include the globe's state key.
     */
    @Override
    public Object getStateKey(DrawContext dc)
    {
        // Store a copy of the active attributes to insulate the key from changes made to the shape's active attributes.
        // Use a null globe state key because SurfaceShape does not depend on the globe by default.
        return new SurfaceShapeStateKey(this.getUniqueId(), this.lastModifiedTime, this.getActiveAttributes().copy(),
            null);
    }

    @SuppressWarnings({"unchecked"})
    public List<Sector> getSectors(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        CacheEntry entry = this.sectorCache.get(dc.getGlobe().getGlobeStateKey());
        if (entry != null)
        {
            return (List<Sector>) entry.object;
        }
        else
        {
            entry = new CacheEntry(this.computeSectors(dc), dc);
            this.sectorCache.put(dc.getGlobe().getGlobeStateKey(), entry);
            return (List<Sector>) entry.object;
        }
    }

    /**
     * Computes the bounding sectors for the shape. There will be more than one if the shape crosses the date line, but
     * does not enclose a pole.
     *
     * @param dc Current draw context.
     *
     * @return Bounding sectors for the shape.
     */
    protected List<Sector> computeSectors(DrawContext dc)
    {
        return this.computeSectors(dc.getGlobe());
    }

    /**
     * Computes the bounding sectors for the shape. There will be more than one if the shape crosses the date line, but
     * does not enclose a pole.
     *
     * @param globe Current globe.
     *
     * @return Bounding sectors for the shape.
     */
    protected List<Sector> computeSectors(Globe globe)
    {
        Iterable<? extends LatLon> locations = this.getLocations(globe);
        if (locations == null)
            return null;

        List<Sector> sectors = null;

        String pole = this.containsPole(locations);
        if (pole != null)
        {
            // If the shape contains a pole, then the bounding sector is defined by the shape's extreme latitude, the
            // latitude of the pole, and the full range of longitude.
            Sector s = Sector.boundingSector(locations);
            if (AVKey.NORTH.equals(pole))
                s = new Sector(s.getMinLatitude(), Angle.POS90, Angle.NEG180, Angle.POS180);
            else
                s = new Sector(Angle.NEG90, s.getMaxLatitude(), Angle.NEG180, Angle.POS180);

            sectors = Arrays.asList(s);
        }
        else if (LatLon.locationsCrossDateLine(locations))
        {
            Sector[] array = Sector.splitBoundingSectors(locations);
            if (array != null && array.length == 2 && !isSectorEmpty(array[0]) && !isSectorEmpty(array[1]))
                sectors = Arrays.asList(array);
        }
        else
        {
            Sector s = Sector.boundingSector(locations);
            if (!isSectorEmpty(s))
                sectors = Arrays.asList(s);
        }

        if (sectors == null)
            return null;

        // Great circle paths between two latitudes may result in a latitude which is greater or smaller than either of
        // the two latitudes. All other path types are bounded by the defining locations.
        if (AVKey.GREAT_CIRCLE.equals(this.getPathType()))
        {
            for (int i = 0; i < sectors.size(); i++)
            {
                Sector s = sectors.get(i);

                LatLon[] extremes = LatLon.greatCircleArcExtremeLocations(locations);

                double minLatDegrees = s.getMinLatitude().degrees;
                double maxLatDegrees = s.getMaxLatitude().degrees;

                if (minLatDegrees > extremes[0].getLatitude().degrees)
                    minLatDegrees = extremes[0].getLatitude().degrees;
                if (maxLatDegrees < extremes[1].getLatitude().degrees)
                    maxLatDegrees = extremes[1].getLatitude().degrees;

                Angle minLat = Angle.fromDegreesLatitude(minLatDegrees);
                Angle maxLat = Angle.fromDegreesLatitude(maxLatDegrees);

                sectors.set(i, new Sector(minLat, maxLat, s.getMinLongitude(), s.getMaxLongitude()));
            }
        }

        return sectors;
    }

    protected static boolean isSectorEmpty(Sector sector)
    {
        if (sector == null)
            return true;

        //noinspection SimplifiableIfStatement
        if (sector.equals(Sector.EMPTY_SECTOR))
            return true;

        return sector.getMinLatitude().equals(sector.getMaxLatitude())
            && sector.getMinLongitude().equals(sector.getMaxLongitude());
    }

    /**
     * Returns this SurfaceShape's enclosing volume as an {@link gov.nasa.worldwind.geom.Extent} in model coordinates,
     * given a specified {@link gov.nasa.worldwind.globes.Globe} and vertical exaggeration (see {@link
     * gov.nasa.worldwind.SceneController#getVerticalExaggeration()}.
     *
     * @param globe                the Globe this SurfaceShape is related to.
     * @param verticalExaggeration the vertical exaggeration of the scene containing this SurfaceShape.
     *
     * @return this SurfaceShape's Extent in model coordinates.
     *
     * @throws IllegalArgumentException if the Globe is null.
     */
    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<Sector> sectors = this.computeSectors(globe);
        if (sectors == null)
            return null;

        return this.computeExtent(globe, verticalExaggeration, sectors);
    }

    public String getRestorableState()
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
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

    public double getArea(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        AreaMeasurer areaMeasurer = this.setupAreaMeasurer(globe);
        return areaMeasurer.getArea(globe);
    }

    public double getArea(Globe globe, boolean terrainConformant)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        AreaMeasurer areaMeasurer = this.setupAreaMeasurer(globe);
        areaMeasurer.setFollowTerrain(terrainConformant);
        return areaMeasurer.getArea(globe);
    }

    public double getPerimeter(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        AreaMeasurer areaMeasurer = this.setupAreaMeasurer(globe);
        return areaMeasurer.getPerimeter(globe);
    }

    public double getWidth(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        AreaMeasurer areaMeasurer = this.setupAreaMeasurer(globe);
        return areaMeasurer.getWidth(globe);
    }

    public double getHeight(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        AreaMeasurer areaMeasurer = this.setupAreaMeasurer(globe);
        return areaMeasurer.getHeight(globe);
    }

    public double getLength(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        AreaMeasurer areaMeasurer = this.setupAreaMeasurer(globe);
        return areaMeasurer.getLength(globe);
    }

    public void move(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position referencePosition = this.getReferencePosition();
        if (referencePosition == null)
            return;

        this.moveTo(referencePosition.add(position));
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position oldReferencePosition = this.getReferencePosition();
        if (oldReferencePosition == null)
            return;

        this.doMoveTo(oldReferencePosition, position);
    }

    public void moveTo(Globe globe, Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position oldReferencePosition = this.getReferencePosition();
        if (oldReferencePosition == null)
            return;

        this.doMoveTo(globe, oldReferencePosition, position);
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
            this.draggableSupport = new DraggableSupport(this, WorldWind.CLAMP_TO_GROUND);

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragGlobeSizeConstant(dragContext);
    }

    /** {@inheritDoc} */
    @Override
    public void combine(CombineContext cc)
    {
        if (cc == null)
        {
            String msg = Logging.getMessage("nullValue.CombineContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (cc.isBoundingSectorMode())
            this.combineBounds(cc);
        else
            this.combineContours(cc);
    }

    public abstract Position getReferencePosition();

    protected abstract void doMoveTo(Position oldReferencePosition, Position newReferencePosition);
    protected abstract void doMoveTo(Globe globe, Position oldReferencePosition, Position newReferencePosition);

    protected void onShapeChanged()
    {
        this.updateModifiedTime();
        this.clearCaches();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to clear this SurfaceShape's internal sector and geometry caches.
     */
    @Override
    protected void clearCaches()
    {
        super.clearCaches();
        this.sectorCache.clear();
        this.geometryCache.clear();
    }

    //**************************************************************//
    //********************  Rendering  *****************************//
    //**************************************************************//

    /**
     * Overridden to determine the shape's active attributes during preRendering, prior to building the shape's pickable
     * representation and the SceneController's composite representation.
     *
     * @param dc the current draw context.
     */
    @Override
    protected void makeOrderedPreRenderable(DrawContext dc)
    {
        this.determineActiveAttributes();
        super.makeOrderedPreRenderable(dc);
    }

    protected void drawGeographic(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sdc == null)
        {
            String message = Logging.getMessage("nullValue.SurfaceTileDrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.beginDrawing(dc, sdc);
        try
        {
            this.doDrawGeographic(dc, sdc);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    protected void beginDrawing(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.stackHandler.pushAttrib(gl,
            GL2.GL_COLOR_BUFFER_BIT      // For alpha test func and ref, blend func
                | GL2.GL_CURRENT_BIT     // For current color.
                | GL2.GL_ENABLE_BIT      // For disable depth test.
                | GL2.GL_LINE_BIT        // For line width, line smooth, line stipple.
                | GL2.GL_POLYGON_BIT     // For cull enable and cull face.
                | GL2.GL_TRANSFORM_BIT); // For matrix mode.

        this.stackHandler.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);

        this.stackHandler.pushTextureIdentity(gl);
        this.stackHandler.pushProjection(gl);
        this.stackHandler.pushModelview(gl);

        // Enable the alpha test.
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.0f);

        // Disable the depth test.
        gl.glDisable(GL.GL_DEPTH_TEST);

        // Enable backface culling.
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);

        // Enable client vertex arrays.
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

        // Enable blending.
        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_BLEND);
        }

        this.applyModelviewTransform(dc, sdc);
    }

    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (texture != null && !dc.isPickingMode())
        {
            gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, OGLUtil.DEFAULT_TEXTURE_GEN_MODE);
            gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, OGLUtil.DEFAULT_TEXTURE_GEN_MODE);
            gl.glTexGendv(GL2.GL_S, GL2.GL_OBJECT_PLANE, OGLUtil.DEFAULT_TEXTURE_GEN_S_OBJECT_PLANE, 0);
            gl.glTexGendv(GL2.GL_T, GL2.GL_OBJECT_PLANE, OGLUtil.DEFAULT_TEXTURE_GEN_T_OBJECT_PLANE, 0);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }

        this.stackHandler.pop(gl);
    }

    protected void doDrawGeographic(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        this.determineActiveGeometry(dc, sdc);

        if (this.getActiveAttributes().isDrawInterior() && this.getActiveAttributes().getInteriorOpacity() > 0)
            this.drawInterior(dc, sdc);

        if (this.getActiveAttributes().isDrawOutline() && this.getActiveAttributes().getOutlineOpacity() > 0)
            this.drawOutline(dc, sdc);
    }

    protected void applyModelviewTransform(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Apply the geographic to surface tile coordinate transform.
        Matrix modelview = sdc.getModelviewMatrix();

        // If the SurfaceShape has a non-null reference position, transform to the local coordinate system that has its
        // origin at the reference position.
        Position refPos = this.getReferencePosition();
        if (refPos != null)
        {
            Matrix refMatrix = Matrix.fromTranslation(refPos.getLongitude().degrees, refPos.getLatitude().degrees, 0);
            modelview = modelview.multiply(refMatrix);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glMultMatrixd(modelview.toArray(new double[16], 0, false), 0);
    }

    /** Determines which attributes -- normal, highlight or default -- to use each frame. */
    protected void determineActiveAttributes()
    {
        if (this.isHighlighted())
        {
            if (this.getHighlightAttributes() != null)
                this.activeAttrs.copy(this.getHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                if (this.getAttributes() != null)
                    this.activeAttrs.copy(this.getAttributes());

                this.activeAttrs.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeAttrs.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeAttrs.setOutlineOpacity(1);
                this.activeAttrs.setInteriorOpacity(1);
            }
        }
        else if (this.getAttributes() != null)
        {
            this.activeAttrs.copy(this.getAttributes());
        }
        else
        {
            this.activeAttrs.copy(defaultAttrs);
        }
    }

    protected ShapeAttributes createActiveAttributes()
    {
        return new BasicShapeAttributes();
    }

    protected ShapeAttributes getActiveAttributes()
    {
        return this.activeAttrs;
    }

    protected void determineActiveGeometry(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        this.activeGeometry.clear();
        this.activeOutlineGeometry.clear();

        List<List<LatLon>> geom = this.getCachedGeometry(dc, sdc);
        if (geom == null)
            return;

        for (List<LatLon> locations : geom)
        {
            List<LatLon> drawLocations = new ArrayList<LatLon>(locations);

            String pole = this.containsPole(drawLocations);
            if (pole != null)
            {
                // Wrap the shape interior around the pole and along the anti-meridian. See WWJ-284.
                List<LatLon> poleLocations = this.cutAlongDateLine(drawLocations, pole, dc.getGlobe());
                this.activeGeometry.add(poleLocations);
                // The outline need only compensate for dateline crossing. See WWJ-452.
                List<List<LatLon>> datelineLocations = this.repeatAroundDateline(drawLocations);
                this.activeOutlineGeometry.addAll(datelineLocations);
            }
            else if (LatLon.locationsCrossDateLine(drawLocations))
            {
                List<List<LatLon>> datelineLocations = this.repeatAroundDateline(drawLocations);
                this.activeGeometry.addAll(datelineLocations);
                this.activeOutlineGeometry.addAll(datelineLocations);
            }
            else
            {
                this.activeGeometry.add(drawLocations);
                this.activeOutlineGeometry.add(drawLocations);
            }
        }
    }

    /**
     * Indicates whether the shape is a closed polygon that can enclose a pole, or an open path that cannot. This makes
     * a difference when computing the bounding sector for a shape. For example, consider the positions (-100, 85), (0,
     * 80), (100, 80). If these positions are treated as a closed polygon (a triangle over the North Pole) then the
     * bounding sector is 80 to 90 lat, -180 to 180 lon. But if they are treated as an open path (a line wrapping
     * partway around the pole) then the bounding sector is 80 to 85 lat, -100 to 100 lon.
     *
     * @return True if the shape is a closed polygon that can contain a pole, or false if it is treated as an open path
     *         that cannot contain a pole.
     */
    protected boolean canContainPole()
    {
        return true;
    }

    /**
     * Determine if a list of geographic locations encloses either the North or South pole. The list is treated as a
     * closed loop. (If the first and last positions are not equal the loop will be closed for purposes of this
     * computation.)
     *
     * @param locations Locations to test.
     *
     * @return AVKey.NORTH if the North Pole is enclosed, AVKey.SOUTH if the South Pole is enclosed, or null if neither
     *         pole is enclosed. Always returns null if {@link #canContainPole()} returns false.
     */
    // TODO handle a shape that contains both poles.
    protected String containsPole(Iterable<? extends LatLon> locations)
    {
        if (!this.canContainPole())
            return null;

        return LatLon.locationsContainPole(locations);
    }

    /**
     * Divide a list of locations that encloses a pole along the international date line. This method determines where
     * the locations cross the date line, and inserts locations to the pole, and then back to the intersection position.
     * This allows the shape to be "unrolled" when projected in a lat-lon projection.
     *
     * @param locations Locations to cut at date line. This list is not modified.
     * @param pole      Pole contained by locations, either AVKey.NORTH or AVKey.SOUTH.
     * @param globe     Current globe.
     *
     * @return New location list with locations added to correctly handle date line intersection.
     */
    protected List<LatLon> cutAlongDateLine(List<LatLon> locations, String pole, Globe globe)
    {
        // If the locations do not contain a pole, then there's nothing to do.
        if (pole == null)
            return locations;

        return LatLon.cutLocationsAlongDateLine(locations, pole, globe);
    }

    /**
     * Returns a list containing two copies of the specified list of locations crossing the dateline: one that extends
     * across the -180 longitude  boundary and one that extends across the +180 longitude boundary. If the list of
     * locations does not cross the dateline this returns a list containing a copy of the original list.
     *
     * @param locations Locations to repeat. This is list not modified.
     *
     * @return A list containing two new location lists, one copy for either side of the date line.
     */
    protected List<List<LatLon>> repeatAroundDateline(List<LatLon> locations)
    {
        return LatLon.repeatLocationsAroundDateline(locations);
    }

    protected List<List<LatLon>> getActiveGeometry()
    {
        return this.activeGeometry;
    }

    protected void drawInterior(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        if (this.getActiveGeometry().isEmpty())
            return;

        this.applyInteriorState(dc, sdc, this.getActiveAttributes(), this.getInteriorTexture(),
            this.getReferencePosition());

        // Tessellate and draw the interior, making no assumptions about the nature or structure of the shape's
        // vertices. The interior is treated as a potentially complex polygon, and this code will do its best to
        // rasterize that polygon. The outline is treated as a simple line loop, regardless of whether the shape's
        // vertices actually define a closed path.
        this.tessellateInterior(dc);
    }

    protected void drawOutline(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        if (this.activeOutlineGeometry.isEmpty())
            return;

        this.applyOutlineState(dc, this.getActiveAttributes());

        for (List<LatLon> drawLocations : this.activeOutlineGeometry)
        {
            this.drawLineStrip(dc, drawLocations);
        }
    }

    protected void drawLineStrip(DrawContext dc, List<LatLon> locations)
    {
        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return;

        if (vertexBuffer == null || vertexBuffer.capacity() < 2 * locations.size())
            vertexBuffer = Buffers.newDirectFloatBuffer(2 * locations.size());
        vertexBuffer.clear();

        for (LatLon ll : locations)
        {
            vertexBuffer.put((float) (ll.getLongitude().degrees - refPos.getLongitude().degrees));
            vertexBuffer.put((float) (ll.getLatitude().degrees - refPos.getLatitude().degrees));
        }
        vertexBuffer.flip();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertexBuffer);
        gl.glDrawArrays(GL.GL_LINE_STRIP, 0, locations.size());
    }

    protected WWTexture getInteriorTexture()
    {
        if (this.getActiveAttributes().getImageSource() == null)
        {
            this.texture = null;
        }
        else if (this.texture == null
            || this.texture.getImageSource() != this.getActiveAttributes().getImageSource())
        {
            this.texture = new BasicWWTexture(this.getActiveAttributes().getImageSource(), true);
        }

        return this.texture;
    }

    @SuppressWarnings({"unchecked"})
    protected List<List<LatLon>> getCachedGeometry(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object key = this.createGeometryKey(dc, sdc);
        CacheEntry entry = this.geometryCache.get(key);
        if (entry != null)
        {
            return (List<List<LatLon>>) entry.object;
        }
        else
        {
            entry = new CacheEntry(this.createGeometry(dc.getGlobe(), sdc), dc);
            this.geometryCache.put(key, entry);
            return (List<List<LatLon>>) entry.object;
        }
    }

    protected List<List<LatLon>> createGeometry(Globe globe, SurfaceTileDrawContext sdc)
    {
        double edgeIntervalsPerDegree = this.computeEdgeIntervalsPerDegree(sdc);
        return this.createGeometry(globe, edgeIntervalsPerDegree);
    }

    protected abstract List<List<LatLon>> createGeometry(Globe globe, double edgeIntervalsPerDegree);

    protected Object createGeometryKey(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        return new GeometryKey(dc, this.computeEdgeIntervalsPerDegree(sdc));
    }

    protected double computeEdgeIntervalsPerDegree(SurfaceTileDrawContext sdc)
    {
        double texelsPerDegree = Math.max(
            sdc.getViewport().width / sdc.getSector().getDeltaLonDegrees(),
            sdc.getViewport().getHeight() / sdc.getSector().getDeltaLatDegrees());
        double intervalsPerTexel = 1.0 / this.getTexelsPerEdgeInterval();

        return intervalsPerTexel * texelsPerDegree;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    protected double computeEdgeIntervalsPerDegree(double resolution)
    {
        double degreesPerInterval = resolution * 180.0 / Math.PI;
        double intervalsPerDegree = 1.0 / degreesPerInterval;

        return intervalsPerDegree;
    }

    //**************************************************************//
    //********************  Combinable  ****************************//
    //**************************************************************//

    protected void combineBounds(CombineContext cc)
    {
        List<Sector> sectorList = this.computeSectors(cc.getGlobe());
        if (sectorList == null)
            return; // no caller specified locations to bound

        cc.addBoundingSector(Sector.union(sectorList));
    }

    protected void combineContours(CombineContext cc)
    {
        List<Sector> sectorList = this.computeSectors(cc.getGlobe());
        if (sectorList == null)
            return; // no caller specified locations to draw

        if (!cc.getSector().intersectsAny(sectorList))
            return; // this shape does not intersect the region of interest

        this.doCombineContours(cc);
    }

    protected void doCombineContours(CombineContext cc)
    {
        double edgeIntervalsPerDegree = this.computeEdgeIntervalsPerDegree(cc.getResolution());
        List<List<LatLon>> contours = this.createGeometry(cc.getGlobe(), edgeIntervalsPerDegree);
        if (contours == null)
            return; // shape has no caller specified data

        for (List<LatLon> contour : contours)
        {
            String pole = this.containsPole(contour);
            if (pole != null) // Wrap the contour around the pole and along the anti-meridian. See WWJ-284.
            {
                List<LatLon> poleContour = this.cutAlongDateLine(contour, pole, cc.getGlobe());
                this.doCombineContour(cc, poleContour);
            }
            else if (LatLon.locationsCrossDateLine(contour)) // Split the contour along the anti-meridian.
            {
                List<List<LatLon>> datelineContours = this.repeatAroundDateline(contour);
                this.doCombineContour(cc, datelineContours.get(0));
                this.doCombineContour(cc, datelineContours.get(1));
            }
            else
            {
                this.doCombineContour(cc, contour);
            }
        }
    }

    protected void doCombineContour(CombineContext cc, Iterable<? extends LatLon> contour)
    {
        GLUtessellator tess = cc.getTessellator();

        try
        {
            GLU.gluTessBeginContour(tess);

            for (LatLon location : contour)
            {
                double[] vertex = {location.longitude.degrees, location.latitude.degrees, 0};
                GLU.gluTessVertex(tess, vertex, 0, vertex);
            }
        }
        finally
        {
            GLU.gluTessEndContour(tess);
        }
    }

    //**************************************************************//
    //********************  Rendering State  ***********************//
    //**************************************************************//

    protected void applyInteriorState(DrawContext dc, SurfaceTileDrawContext sdc, ShapeAttributes attributes,
        WWTexture texture, LatLon refLocation)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (texture != null && !dc.isPickingMode())
        {
            this.applyInteriorTextureState(dc, sdc, attributes, texture, refLocation);
        }
        else
        {
            if (!dc.isPickingMode())
            {
                // Apply blending in non-premultiplied color mode.
                OGLUtil.applyBlending(gl, false);
                // Set the current RGBA color to the outline color and opacity. Convert the floating point opacity from the
                // range [0, 1] to the unsigned byte range [0, 255].
                Color color = attributes.getInteriorMaterial().getDiffuse();
                int alpha = (int) (255 * attributes.getInteriorOpacity() + 0.5);
                gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) alpha);
            }

            // Disable textures.
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glDisable(GL2.GL_TEXTURE_GEN_S);
            gl.glDisable(GL2.GL_TEXTURE_GEN_T);
        }
    }

    protected void applyOutlineState(DrawContext dc, ShapeAttributes attributes)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Apply line width state
        double lineWidth = attributes.getOutlineWidth();
        if (dc.isPickingMode() && !attributes.isDrawInterior())
        {
            if (lineWidth != 0)
                lineWidth += 5;
        }
        gl.glLineWidth((float) lineWidth);

        // Apply line smooth state
        if (!dc.isPickingMode() && attributes.isEnableAntialiasing())
        {
            gl.glEnable(GL.GL_LINE_SMOOTH);
        }
        else
        {
            gl.glDisable(GL.GL_LINE_SMOOTH);
        }

        // Apply line stipple state.
        if (dc.isPickingMode() || (attributes.getOutlineStippleFactor() <= 0))
        {
            gl.glDisable(GL2.GL_LINE_STIPPLE);
        }
        else
        {
            gl.glEnable(GL2.GL_LINE_STIPPLE);
            gl.glLineStipple(
                attributes.getOutlineStippleFactor(),
                attributes.getOutlineStipplePattern());
        }

        if (!dc.isPickingMode())
        {
            // Apply blending in non-premultiplied color mode.
            OGLUtil.applyBlending(gl, false);
            // Set the current RGBA color to the outline color and opacity. Convert the floating point opacity from the
            // range [0, 1] to the unsigned byte range [0, 255].
            Color color = attributes.getOutlineMaterial().getDiffuse();
            int alpha = (int) (255 * attributes.getOutlineOpacity() + 0.5);
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) alpha);
        }

        // Disable textures.
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_TEXTURE_GEN_S);
        gl.glDisable(GL2.GL_TEXTURE_GEN_T);
    }

    protected void applyInteriorTextureState(DrawContext dc, SurfaceTileDrawContext sdc, ShapeAttributes attributes,
        WWTexture texture, LatLon refLocation)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!texture.bind(dc))
            return;

        if (!dc.isPickingMode())
        {
            // Apply blending in premultiplied color mode, and set the current RGBA color to white, with the specified
            // opacity.
            OGLUtil.applyBlending(gl, true);
            OGLUtil.applyColor(gl, Color.WHITE, attributes.getInteriorOpacity(), true);
        }

        // Apply texture coordinate generation.
        double[] planeS = new double[] {1, 0, 0, 1};
        double[] planeT = new double[] {0, 1, 0, 1};
        gl.glEnable(GL2.GL_TEXTURE_GEN_S);
        gl.glEnable(GL2.GL_TEXTURE_GEN_T);
        gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
        gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
        gl.glTexGendv(GL2.GL_S, GL2.GL_OBJECT_PLANE, planeS, 0);
        gl.glTexGendv(GL2.GL_T, GL2.GL_OBJECT_PLANE, planeT, 0);

        // Apply texture transform.
        Matrix transform = Matrix.IDENTITY;
        // Translate geographic coordinates to the reference location.
        if (refLocation != null)
        {
            double refLatDegrees = refLocation.getLatitude().degrees;
            double refLonDegrees = refLocation.getLongitude().degrees;
            transform = Matrix.fromTranslation(refLonDegrees, refLatDegrees, 0d).multiply(transform);
        }
        // Premultiply pattern scaling and cos latitude to compensate latitude distortion on x
        double cosLat = refLocation != null ? refLocation.getLatitude().cos() : 1d;
        double scale = attributes.getImageScale();
        transform = Matrix.fromScale(cosLat / scale, 1d / scale, 1d).multiply(transform);
        // To maintain the pattern apparent size, we scale it so that one texture pixel match one draw tile pixel.
        double regionPixelSize = dc.getGlobe().getRadius() * sdc.getSector().getDeltaLatRadians()
            / sdc.getViewport().height;
        double texturePixelSize = dc.getGlobe().getRadius() * Angle.fromDegrees(1).radians / texture.getHeight(dc);
        double drawScale = texturePixelSize / regionPixelSize;
        transform = Matrix.fromScale(drawScale, drawScale, 1d).multiply(transform); // Pre multiply
        // Apply texture coordinates transform
        double[] matrixArray = transform.toArray(new double[16], 0, false);
        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glLoadIdentity();
        texture.applyInternalTransform(dc);
        gl.glMultMatrixd(matrixArray, 0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        // Apply texture environment and parameters.
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
    }

    //**************************************************************//
    //********************  Intermediate Locations  ****************//
    //**************************************************************//

    protected void generateIntermediateLocations(Iterable<? extends LatLon> iterable, double edgeIntervalsPerDegree,
        boolean makeClosedPath, List<LatLon> locations)
    {
        LatLon firstLocation = null;
        LatLon lastLocation = null;

        for (LatLon ll : iterable)
        {
            if (firstLocation == null)
            {
                firstLocation = ll;
            }

            if (lastLocation != null)
            {
                this.addIntermediateLocations(lastLocation, ll, edgeIntervalsPerDegree, locations);
            }

            locations.add(ll);
            lastLocation = ll;
        }

        // If the caller has instructed us to generate locations for a closed path, then check to see if the specified
        // locations define a closed path. If not, then we need to generate intermediate locations between the last
        // and first locations, then close the path by repeating the first location.
        if (makeClosedPath)
        {
            if (firstLocation != null && lastLocation != null && !firstLocation.equals(lastLocation))
            {
                this.addIntermediateLocations(lastLocation, firstLocation, edgeIntervalsPerDegree, locations);
                locations.add(firstLocation);
            }
        }
    }

    @SuppressWarnings({"StringEquality"})
    protected void addIntermediateLocations(LatLon a, LatLon b, double edgeIntervalsPerDegree, List<LatLon> locations)
    {
        if (this.pathType != null && this.pathType == AVKey.GREAT_CIRCLE)
        {
            Angle pathLength = LatLon.greatCircleDistance(a, b);

            double edgeIntervals = WWMath.clamp(edgeIntervalsPerDegree * pathLength.degrees,
                this.minEdgeIntervals, this.maxEdgeIntervals);
            int numEdgeIntervals = (int) Math.ceil(edgeIntervals);

            if (numEdgeIntervals > 1)
            {
                double headingRadians = LatLon.greatCircleAzimuth(a, b).radians;
                double stepSizeRadians = pathLength.radians / (numEdgeIntervals + 1);

                for (int i = 1; i <= numEdgeIntervals; i++)
                {
                    locations.add(LatLon.greatCircleEndPosition(a, headingRadians, i * stepSizeRadians));
                }
            }
        }
        else if (this.pathType != null && (this.pathType == AVKey.RHUMB_LINE || this.pathType == AVKey.LOXODROME))
        {
            Angle pathLength = LatLon.rhumbDistance(a, b);

            double edgeIntervals = WWMath.clamp(edgeIntervalsPerDegree * pathLength.degrees,
                this.minEdgeIntervals, this.maxEdgeIntervals);
            int numEdgeIntervals = (int) Math.ceil(edgeIntervals);

            if (numEdgeIntervals > 1)
            {
                double headingRadians = LatLon.rhumbAzimuth(a, b).radians;
                double stepSizeRadians = pathLength.radians / (numEdgeIntervals + 1);

                for (int i = 1; i <= numEdgeIntervals; i++)
                {
                    locations.add(LatLon.rhumbEndPosition(a, headingRadians, i * stepSizeRadians));
                }
            }
        }
        else // Default to linear interpolation in latitude and longitude.
        {
            // Linear interpolation between 2D coordinates is already performed by GL during shape rasterization.
            // There is no need to duplicate that effort here.
        }
    }

    //**************************************************************//
    //********************  Interior Tessellation  *****************//
    //**************************************************************//

    protected Integer tessellateInterior(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            return this.doTessellateInterior(dc);
        }
        catch (OutOfMemoryError e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileTessellating", this);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);

            //noinspection ThrowableInstanceNeverThrown
            dc.addRenderingException(new WWRuntimeException(message, e));

            this.handleUnsuccessfulInteriorTessellation(dc);

            return null;
        }
    }

    protected Integer doTessellateInterior(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        GLUtessellatorCallback cb = GLUTessellatorSupport.createOGLDrawPrimitivesCallback(gl);

        // Create a tessellator with the default winding rule: GLU_TESS_WINDING_ODD. This winding rule produces the
        // expected tessellation when the shape's contours all have a counter-clockwise winding.
        GLUTessellatorSupport glts = new GLUTessellatorSupport();
        glts.beginTessellation(cb, new Vec4(0, 0, 1));
        try
        {
            return this.tessellateInteriorVertices(glts.getGLUtessellator());
        }
        finally
        {
            // Free any heap memory used for tessellation immediately. If tessellation has consumed all available heap
            // memory, we must free memory used by tessellation immediately or subsequent operations such as message
            // logging will fail.
            glts.endTessellation();
        }
    }

    protected Integer tessellateInteriorVertices(GLUtessellator tess)
    {
        if (this.getActiveGeometry().isEmpty())
            return null;

        Position referencePos = this.getReferencePosition();
        if (referencePos == null)
            return null;

        int numBytes = 0;
        GLU.gluTessBeginPolygon(tess, null);

        for (List<LatLon> drawLocations : this.getActiveGeometry())
        {
            GLU.gluTessBeginContour(tess);
            for (LatLon ll : drawLocations)
            {
                double[] vertex = new double[3];
                vertex[0] = ll.getLongitude().degrees - referencePos.getLongitude().degrees;
                vertex[1] = ll.getLatitude().degrees - referencePos.getLatitude().degrees;
                GLU.gluTessVertex(tess, vertex, 0, vertex);
                numBytes += 3 * 8; // 3 coords of 8 bytes each
            }
            GLU.gluTessEndContour(tess);
        }

        GLU.gluTessEndPolygon(tess);

        return numBytes;
    }

    protected void handleUnsuccessfulInteriorTessellation(DrawContext dc)
    {
    }

    //**************************************************************//
    //********************  Measurement  ***************************//
    //**************************************************************//

    protected AreaMeasurer setupAreaMeasurer(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.areaMeasurer == null)
        {
            this.areaMeasurer = new AreaMeasurer();
        }

        // Try to use the currently cached locations. If the AreaMeasurer is out of sync with this shape's state,
        // then update the AreaMeasurer's internal location list.
        if (this.areaMeasurerLastModifiedTime < this.lastModifiedTime)
        {
            // The AreaMeasurer requires an ArrayList reference, but SurfaceShapes use an opaque iterable. Copy the
            // iterable contents into an ArrayList to satisfy AreaMeasurer without compromising the generality of the
            // shape's iterator.
            ArrayList<LatLon> arrayList = new ArrayList<LatLon>();

            Iterable<? extends LatLon> locations = this.getLocations(globe);
            if (locations != null)
            {
                for (LatLon ll : locations)
                {
                    arrayList.add(ll);
                }

                if (arrayList.size() > 1 && !arrayList.get(0).equals(arrayList.get(arrayList.size() - 1)))
                    arrayList.add(arrayList.get(0));
            }

            this.areaMeasurer.setPositions(arrayList, 0);
            this.areaMeasurerLastModifiedTime = this.lastModifiedTime;
        }

        // Surface shapes follow the terrain by definition.
        this.areaMeasurer.setFollowTerrain(true);

        return this.areaMeasurer;
    }

    //**************************************************************//
    //******************** Restorable State  ***********************//
    //**************************************************************//

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Note: drawBoundingSectors is a diagnostic flag, therefore it is not saved or restored.

        rs.addStateValueAsBoolean(context, "visible", this.isVisible());
        rs.addStateValueAsBoolean(context, "highlighted", this.isHighlighted());
        rs.addStateValueAsString(context, "pathType", this.getPathType());
        rs.addStateValueAsDouble(context, "texelsPerEdgeInterval", this.getTexelsPerEdgeInterval());

        int[] minAndMaxEdgeIntervals = this.getMinAndMaxEdgeIntervals();
        rs.addStateValueAsInteger(context, "minEdgeIntervals", minAndMaxEdgeIntervals[0]);
        rs.addStateValueAsInteger(context, "maxEdgeIntervals", minAndMaxEdgeIntervals[1]);

        if (this.getAttributes() != null)
            this.getAttributes().getRestorableState(rs, rs.addStateObject(context, "attributes"));

        if (this.getHighlightAttributes() != null)
            this.getHighlightAttributes().getRestorableState(rs, rs.addStateObject(context, "highlightAttrs"));

        RestorableSupport.StateObject so = rs.addStateObject(null, "avlist");
        for (Map.Entry<String, Object> avp : this.getEntries())
        {
            this.getRestorableStateForAVPair(avp.getKey(), avp.getValue() != null ? avp.getValue() : "", rs, so);
        }
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Invoke the legacy restore functionality. This will enable the shape to recognize state XML elements
        // from the previous version of SurfaceShape.
        this.legacyRestoreState(rs, context);

        // Note: drawBoundingSectors is a diagnostic flag, therefore it is not saved or restored.

        Boolean b = rs.getStateValueAsBoolean(context, "visible");
        if (b != null)
            this.setVisible(b);

        b = rs.getStateValueAsBoolean(context, "highlighted");
        if (b != null)
            this.setHighlighted(b);

        String s = rs.getStateValueAsString(context, "pathType");
        if (s != null)
        {
            String pathType = this.pathTypeFromString(s);
            if (pathType != null)
                this.setPathType(pathType);
        }

        Double d = rs.getStateValueAsDouble(context, "texelsPerEdgeInterval");
        if (d != null)
            this.setTexelsPerEdgeInterval(d);

        int[] minAndMaxEdgeIntervals = this.getMinAndMaxEdgeIntervals();

        Integer minEdgeIntervals = rs.getStateValueAsInteger(context, "minEdgeIntervals");
        if (minEdgeIntervals != null)
            minAndMaxEdgeIntervals[0] = minEdgeIntervals;

        Integer maxEdgeIntervals = rs.getStateValueAsInteger(context, "maxEdgeIntervals");
        if (maxEdgeIntervals != null)
            minAndMaxEdgeIntervals[1] = maxEdgeIntervals;

        if (minEdgeIntervals != null || maxEdgeIntervals != null)
            this.setMinAndMaxEdgeIntervals(minAndMaxEdgeIntervals[0], minAndMaxEdgeIntervals[1]);

        RestorableSupport.StateObject so = rs.getStateObject(context, "attributes");
        if (so != null)
        {
            ShapeAttributes attrs = (this.getAttributes() != null) ? this.getAttributes() : new BasicShapeAttributes();
            attrs.restoreState(rs, so);
            this.setAttributes(attrs);
        }

        so = rs.getStateObject(context, "highlightAttrs");
        if (so != null)
        {
            ShapeAttributes attrs = (this.getHighlightAttributes() != null) ? this.getHighlightAttributes()
                : new BasicShapeAttributes();
            attrs.restoreState(rs, so);
            this.setHighlightAttributes(attrs);
        }

        so = rs.getStateObject(null, "avlist");
        if (so != null)
        {
            RestorableSupport.StateObject[] avpairs = rs.getAllStateObjects(so, "");
            if (avpairs != null)
            {
                for (RestorableSupport.StateObject avp : avpairs)
                {
                    if (avp != null)
                        this.setValue(avp.getName(), avp.getValue());
                }
            }
        }

        // We've potentially modified the shapes attributes in either legacyRestoreState(), or in
        // attributes.restoreState(). Flag that the shape has changed in order to ensure that any cached data associated
        // with the shape is invalidated.
        this.onShapeChanged();
    }

    /**
     * Restores state values from previous versions of the SurfaceShape state XML. These values are stored or named
     * differently than the current implementation. Those values which have not changed are ignored here, and will
     * restored in {@link #doRestoreState(gov.nasa.worldwind.util.RestorableSupport,
     * gov.nasa.worldwind.util.RestorableSupport.StateObject)}.
     *
     * @param rs      RestorableSupport object which contains the state value properties.
     * @param context active context in the RestorableSupport to read state from.
     */
    protected void legacyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Ignore texture width and height parameters, they're no longer used.

        //Integer width = rs.getStateValueAsInteger(context, "textureWidth");
        //Integer height = rs.getStateValueAsInteger(context, "textureHeight");
        //if (width != null && height != null)
        //    this.setTextureSize(new Dimension(width, height));

        ShapeAttributes attrs = this.getAttributes();

        java.awt.Color color = rs.getStateValueAsColor(context, "color");
        if (color != null)
            (attrs != null ? attrs : (attrs = new BasicShapeAttributes())).setInteriorMaterial(new Material(color));

        color = rs.getStateValueAsColor(context, "borderColor");
        if (color != null)
            (attrs != null ? attrs : (attrs = new BasicShapeAttributes())).setOutlineMaterial(new Material(color));

        Double dub = rs.getStateValueAsDouble(context, "lineWidth");
        if (dub != null)
            (attrs != null ? attrs : (attrs = new BasicShapeAttributes())).setOutlineWidth(dub);

        // Ignore numEdgeIntervalsPerDegree, since it's no longer used.
        //Double intervals = rs.getStateValueAsDouble(context, "numEdgeIntervalsPerDegree");
        //if (intervals != null)
        //    this.setEdgeIntervalsPerDegree(intervals.intValue());

        Boolean booleanState = rs.getStateValueAsBoolean(context, "drawBorder");
        if (booleanState != null)
            (attrs != null ? attrs : (attrs = new BasicShapeAttributes())).setDrawOutline(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "drawInterior");
        if (booleanState != null)
            (attrs != null ? attrs : (attrs = new BasicShapeAttributes())).setDrawInterior(booleanState);

        booleanState = rs.getStateValueAsBoolean(context, "antialias");
        if (booleanState != null)
            (attrs != null ? attrs : (attrs = new BasicShapeAttributes())).setEnableAntialiasing(booleanState);

        if (attrs != null)
            this.setAttributes(attrs);

        // Positions data is a per object property now. This value is recognized by SurfacePolygon, SurfacePolyline, and
        // SurfaceSector. Other shapes ignore this property.

        //ArrayList<LatLon> locations = rs.getStateValueAsLatLonList(context, "locations");
        //if (locations != null)
        //    this.positions = locations;
    }

    protected String pathTypeFromString(String s)
    {
        if (s == null)
            return null;

        if (s.equals(AVKey.GREAT_CIRCLE))
        {
            return AVKey.GREAT_CIRCLE;
        }
        else if (s.equals(AVKey.LINEAR))
        {
            return AVKey.LINEAR;
        }
        else if (s.equals(AVKey.LOXODROME))
        {
            return AVKey.LOXODROME;
        }
        else if (s.equals(AVKey.RHUMB_LINE))
        {
            return AVKey.RHUMB_LINE;
        }

        return null;
    }

    //**************************************************************//
    //********************  State Key  *****************************//
    //**************************************************************//

    /**
     * Represents a surface shapes's current state. SurfaceShapeStateKey extends {@link
     * gov.nasa.worldwind.render.AbstractSurfaceObject.SurfaceObjectStateKey} by adding the shape's current {@link
     * gov.nasa.worldwind.render.ShapeAttributes} and the globe's state key.
     * <p/>
     * SurfaceShapeStateKey uniquely identifies a surface shapes's current state exactly as SurfaceObjectStateKey does,
     * but also distinguishes the shape's active ShapeAttributes from any previous attributes, and distinguishes between
     * different globes via the globe state key.
     */
    protected static class SurfaceShapeStateKey extends SurfaceObjectStateKey
    {
        /** The SurfaceShape's attributes. May be null if the shape has no attributes. */
        protected final ShapeAttributes attributes;
        /** The Globe's state key. May be null if the shape's state does not depend on the globe. */
        protected final Object globeStateKey;

        /**
         * Constructs a new SurfaceShapeStateKey with the specified unique ID, modified time, attributes, and globe
         * state key. The globe state key should be null if the surface shape does not depend on the globe.
         *
         * @param uniqueID      the SurfaceShape's unique ID.
         * @param modifiedTime  the SurfaceShape's modified time.
         * @param attributes    the SurfaceShape's attributes, or null if the shape has no attributes.
         * @param globeStateKey the globe's state key, or null if the shape does not depend on the globe.
         *
         * @see gov.nasa.worldwind.globes.Globe#getStateKey(DrawContext)
         */
        public SurfaceShapeStateKey(long uniqueID, long modifiedTime, ShapeAttributes attributes, Object globeStateKey)
        {
            super(uniqueID, modifiedTime);

            this.attributes = attributes;
            this.globeStateKey = globeStateKey;
        }

        @Override
        @SuppressWarnings({"SimplifiableIfStatement"})
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            SurfaceShapeStateKey that = (SurfaceShapeStateKey) o;
            return super.equals(o)
                && (this.attributes != null ? this.attributes.equals(that.attributes) : that.attributes == null)
                && (this.globeStateKey != null ? this.globeStateKey.equals(that.globeStateKey)
                : that.globeStateKey == null);
        }

        @Override
        public int hashCode()
        {
            int result = super.hashCode();
            result = 31 * result + (this.attributes != null ? this.attributes.hashCode() : 0);
            result = 31 * result + (this.globeStateKey != null ? this.globeStateKey.hashCode() : 0);
            return result;
        }

        /**
         * Returns the state key's size in bytes. Overridden to include the attributes and the reference to the globe
         * state key.
         *
         * @return The state key's size in bytes.
         */
        @Override
        public long getSizeInBytes()
        {
            return super.getSizeInBytes() + 64; // Add the shape attributes and the references.
        }
    }

    //**************************************************************//
    //********************  Cache Key, Cache Entry  ****************//
    //**************************************************************//

    protected static class GeometryKey
    {
        protected Globe globe;
        protected double edgeIntervalsPerDegree;

        public GeometryKey(DrawContext dc, double edgeIntervalsPerDegree)
        {
            this.globe = dc.getGlobe();
            this.edgeIntervalsPerDegree = edgeIntervalsPerDegree;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            GeometryKey that = (GeometryKey) o;
            return this.globe.equals(that.globe) && this.edgeIntervalsPerDegree == that.edgeIntervalsPerDegree;
        }

        @Override
        public int hashCode()
        {
            int hash = this.globe.hashCode();
            long temp = this.edgeIntervalsPerDegree != +0.0d ? Double.doubleToLongBits(this.edgeIntervalsPerDegree)
                : 0L;
            return 31 * hash + (int) (temp ^ (temp >>> 32));
        }
    }

    /**
     * Does this object support a certain export format?
     *
     * @param format Mime type for the export format.
     *
     * @return One of {@link Exportable#FORMAT_SUPPORTED}, {@link Exportable#FORMAT_NOT_SUPPORTED}, or {@link
     *         Exportable#FORMAT_PARTIALLY_SUPPORTED}.
     *
     * @see #export(String, Object)
     */
    public String isExportFormatSupported(String format)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(format))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    /**
     * Export the Polygon. The {@code output} object will receive the exported data. The type of this object depends on
     * the export format. The formats and object types supported by this class are:
     * <p/>
     * <pre>
     * Format                                         Supported output object types
     * ================================================================================
     * KML (application/vnd.google-earth.kml+xml)     java.io.Writer
     *                                                java.io.OutputStream
     *                                                javax.xml.stream.XMLStreamWriter
     * </pre>
     *
     * @param mimeType MIME type of desired export format.
     * @param output   An object that will receive the exported data. The type of this object depends on the export
     *                 format (see above).
     *
     * @throws java.io.IOException           If an exception occurs writing to the output object.
     * @throws UnsupportedOperationException if the format is not supported by this object, or if the {@code output}
     *                                       argument is not of a supported type.
     */
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

    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        // This is a dummy method, here to enable a call to it above. It's expected to be overridden by subclasses.
    }
}
