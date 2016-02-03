/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.ShapeDataCache;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import javax.xml.stream.*;
import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * A multi-sided 3D shell formed by a base polygon in latitude and longitude extruded from the terrain to either a
 * specified height or an independent height per location. The base polygon may be complex with multiple internal but
 * not intersecting contours.
 * <p/>
 * Extruded polygon boundaries may be specified using either {@link LatLon} locations or {@link Position} positions, but
 * all the shape's boundary vertices must be the same type.
 * <p/>
 * Extruded polygons may optionally be textured. Textures may be applied to both the faces of the outer and inner
 * boundaries or just the outer boundaries. Texture can also be applied independently to the cap. Standard lighting is
 * optionally applied. Texture source images are lazily retrieved and loaded. This can cause a brief period in which the
 * texture is not displayed while it is retrieved from disk or network.
 * <p/>
 * <code>ExtrudedPolygon</code> side faces and cap have independent attributes for both normal and highlighted drawing.
 * <p/>
 * When specifying a single height (altitude mode {@link WorldWind#CONSTANT}, the height is relative to a reference
 * location designated by one of the specified polygon locations in the outer boundary. The default reference location
 * is the first one in the polygon's outer boundary. An alternative location may be specified by calling {@link
 * #setReferenceLocation(LatLon)}. The extruded polygon is capped with a plane at the specified height and tangent to
 * the ellipsoid at the reference location. Since locations other than the reference location may resolve to points at
 * elevations other than that at the reference location, the distances from those points to the cap are adjusted so that
 * the adjacent sides precisely meet the cap. When specifying polygons using a single height, only the latitudes and
 * longitudes of polygon boundary positions must be specified.
 * <p/>
 * Independent per-location heights may be specified via the <i>altitude</i> field of {@link Position}s defining the
 * polygon's inner and outer boundaries. Depending on the specified altitude mode, the position altitudes may be
 * interpreted as altitudes relative to mean sea level or altitudes above the ground at the associated latitude and
 * longitude locations.
 * <p/>
 * Boundaries are required to be closed, their first location must be equal to their last location. Boundaries that are
 * not closed are explicitly closed by this shape when they are specified.
 * <p/>
 * Extruded polygons are safe to share among World Windows. They should not be shared among layers in the same World
 * Window.
 * <p/>
 * In order to support simultaneous use of this shape with multiple globes (windows), this shape maintains a cache of
 * data computed relative to each globe. During rendering, the data for the currently active globe, as indicated in the
 * draw context, is made current. Subsequently called methods rely on the existence of this current data cache entry.
 * <p/>
 * When drawn on a 2D globe, this shape uses a {@link SurfacePolygon} to represent itself. Cap texture is not supported
 * in this case.
 *
 * @author tag
 * @version $Id: ExtrudedPolygon.java 2111 2014-06-30 18:09:45Z tgaskins $
 */
public class ExtrudedPolygon extends AbstractShape
{
    /** The default interior color for sides. */
    protected static final Material DEFAULT_SIDES_INTERIOR_MATERIAL = Material.LIGHT_GRAY;
    /** The default altitude mode. */
    protected static final int DEFAULT_ALTITUDE_MODE = WorldWind.CONSTANT;

    /** The attributes used if attributes are not specified. */
    protected static final ShapeAttributes defaultSideAttributes;

    protected double baseDepth;

    static
    {
        defaultSideAttributes = new BasicShapeAttributes();
        defaultSideAttributes.setInteriorMaterial(DEFAULT_SIDES_INTERIOR_MATERIAL);
    }

    /** The <code>ShapeData</code> class holds globe-specific data for this shape. */
    protected static class ShapeData extends AbstractShapeData implements Iterable<ExtrudedBoundaryInfo>
    {
        /** The boundary locations of the associated shape. Copied from that shape during construction. */
        protected List<ExtrudedBoundaryInfo> boundaries = new ArrayList<ExtrudedBoundaryInfo>();
        /** A buffer holding the Cartesian cap vertices of all the shape's boundaries. */
        protected FloatBuffer capVertexBuffer;
        /** A buffer holding the cap normals of all the shape's boundaries. */
        protected FloatBuffer capNormalBuffer;
        /** A buffer holding the Cartesian vertices of all the shape's side vertices. */
        protected FloatBuffer sideVertexBuffer;
        /** A buffer holding the side normals of all the shape's boundaries. */
        protected FloatBuffer sideNormalBuffer;
        /** A buffer holding the texture coordinates of all the shape's faces. Non-null only when texture is applied. */
        protected FloatBuffer sideTextureCoordsBuffer;

        // Tessellation fields
        /** This shape's tessellation. */
        protected GLUTessellatorSupport.CollectIndexListsCallback cb;
        /**
         * The indices identifying the cap vertices in a shape data's vertex buffer. Determined when this shape is
         * tessellated, which occurs only once unless the shape's boundaries are re-specified.
         */
        protected IntBuffer capFillIndices;
        /** Slices of <code>capFillIndices</code>, one per boundary. */
        protected List<IntBuffer> capFillIndexBuffers;
        /** Indicates whether a tessellation error occurred. No more attempts to tessellate will be made if set to true. */
        protected boolean tessellationError = false;

        /**
         * Constructs an instance using the boundaries of a specified extruded polygon.
         *
         * @param dc    the current draw context.
         * @param shape this shape.
         */
        public ShapeData(DrawContext dc, ExtrudedPolygon shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);

            if (shape.boundaries.size() < 1)
            {
                // add a placeholder for the outer boundary
                this.boundaries.add(new ExtrudedBoundaryInfo(new ArrayList<LatLon>()));
                return;
            }

            // Copy the shape's boundaries.
            for (List<? extends LatLon> boundary : shape.boundaries)
            {
                this.boundaries.add(new ExtrudedBoundaryInfo(boundary));
            }

            // Copy the shape's side texture references.
            this.copySideTextureReferences(shape);
        }

        protected void copySideTextureReferences(ExtrudedPolygon shape)
        {
            if (shape.sideTextures != null)
            {
                for (int i = 0; i < this.boundaries.size() && i < shape.sideTextures.size(); i++)
                {
                    ExtrudedBoundaryInfo ebi = this.boundaries.get(i);
                    if (ebi != null)
                        this.boundaries.get(i).sideTextures = shape.sideTextures.get(i);
                }
            }
        }

        /**
         * Returns the outer boundary information for this shape data.
         *
         * @return this shape data's outer boundary information.
         */
        protected ExtrudedBoundaryInfo getOuterBoundaryInfo()
        {
            return this.boundaries.get(0);
        }

        /**
         * Iterates over the boundary information of this shape data.
         *
         * @return an iterator over this shape data's boundary info.
         */
        public Iterator<ExtrudedBoundaryInfo> iterator()
        {
            return this.boundaries.iterator();
        }
    }

    @Override
    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new ShapeData(dc, this);
    }

    /**
     * Indicates the currently active shape data.
     *
     * @return the currently active shape data.
     */
    protected ShapeData getCurrent()
    {
        return (ShapeData) this.getCurrentData();
    }

    /**
     * Holds globe-specific information for each contour of the polygon. This class is meant only to be used as a way to
     * group per-boundary information in globe-specific <code>ShapeData</code>.
     */
    protected static class ExtrudedBoundaryInfo
    {
        /** The boundary vertices. This is merely a reference to the paren't shape's boundaries. */
        protected List<? extends LatLon> locations;
        /** The number of faces in the boundary. (The number of positions - 1.) */
        protected int faceCount;

        /** The vertices defining the boundary's cap. */
        protected Vec4[] capVertices;
        /** The vertices defining the boundary's base. These are always on the terrain. */
        protected Vec4[] baseVertices;

        /** Indices identifying the cap vertices in the vertex buffer. */
        protected IntBuffer capFillIndices;
        /** Indices identifying the cap edges in the vertex buffer. */
        protected IntBuffer capEdgeIndices;
        /** A buffer holding the vertices defining the boundary's cap. */
        protected FloatBuffer capVertexBuffer;
        /** A buffer holding the boundary cap's vertex normals. Non-null only when lighting is applied. */
        protected FloatBuffer capNormalBuffer;

        /** The indices identifying the boundary's side faces in the side-vertex buffer. */
        protected IntBuffer sideIndices;
        /** The indices identifying the boundary's edge indices in the side-vertex buffer. */
        protected IntBuffer sideEdgeIndices;
        /** A buffer holding the side vertices. These are passed to OpenGL. */
        protected FloatBuffer sideVertexBuffer;
        /** A buffer holding per-vertex normals. Non-null only when lighting is applied. */
        protected FloatBuffer sideNormalBuffer;
        /** The textures to apply to this boundary, one per face. */
        protected List<WWTexture> sideTextures;
        /**
         * The texture coordinates to use when applying side textures, a coordinate pair for each of 4 corners. These
         * are globe-specific because they account for the varying positions of the base on the terrain. (The cap
         * texture coordinates are not globe-specific.)
         */
        protected FloatBuffer sideTextureCoords;

        /**
         * Constructs a boundary info instance for a specified boundary.
         *
         * @param locations the boundary locations. Only this reference is kept; the boundaries are not copied.
         */
        public ExtrudedBoundaryInfo(List<? extends LatLon> locations)
        {
            this.locations = locations;
            this.faceCount = locations.size() - 1;
        }
    }

    // This static hash map holds the vertex indices that define the shape geometry. Their contents depend only on the
    // number of locations in the source polygon, so they can be reused by all shapes with the same location count.
    protected static HashMap<Integer, IntBuffer> capEdgeIndexBuffers = new HashMap<Integer, IntBuffer>();
    protected static HashMap<Integer, IntBuffer> sideFillIndexBuffers = new HashMap<Integer, IntBuffer>();
    protected static HashMap<Integer, IntBuffer> sideEdgeIndexBuffers = new HashMap<Integer, IntBuffer>();

    /** Indicates the number of vertices that must be present in order for VBOs to be used to render this shape. */
    protected static final int VBO_THRESHOLD = Configuration.getIntegerValue(AVKey.VBO_THRESHOLD, 30);

    /**
     * The location of each vertex in this shape's boundaries. There is one list per boundary. There is always an entry
     * for the outer boundary, but its list is empty if an outer boundary has not been specified.
     */
    protected List<List<? extends LatLon>> boundaries;
    /** The total number of locations in all boundaries. */
    protected int totalNumLocations;
    /** The total number of faces in all this shape's boundaries. */
    protected int totalFaceCount;

    /** This shape's height. Default is 1. */
    protected double height = 1;

    /** The attributes to use when drawing this shape's sides. */
    protected ShapeAttributes sideAttributes;
    /** The attributes to use when drawing this shape's sides in highlight mode. */
    protected ShapeAttributes sideHighlightAttributes;
    /** The currently active side attributes, derived from the specified attributes. Current only during rendering. */
    protected ShapeAttributes activeSideAttributes = new BasicShapeAttributes();
    /** This shape's side textures. */
    protected List<List<WWTexture>> sideTextures;
    /** This shape's cap texture. */
    protected WWTexture capTexture;
    /** This shape's cap texture coordinates. */
    protected FloatBuffer capTextureCoords;
    /** Indicates whether the cap should be drawn. */
    protected boolean enableCap = true;
    /** Indicates whether the sides should be drawn. */
    protected boolean enableSides = true;

    // Intersection fields
    /** The terrain used in the most recent intersection calculations. */
    protected Terrain previousIntersectionTerrain;
    /** The globe state key for the globe used in the most recent intersection calculation. */
    protected Object previousIntersectionGlobeStateKey;
    /** The shape data used for the previous intersection calculation. */
    protected ShapeData previousIntersectionShapeData;

    /** Constructs an extruded polygon with an empty outer boundary and a default height of 1 meter. */
    public ExtrudedPolygon()
    {
        this.boundaries = new ArrayList<List<? extends LatLon>>();
        this.boundaries.add(new ArrayList<LatLon>()); // placeholder for outer boundary
    }

    /**
     * Constructs an extruded polygon of a specified height and an empty outer boundary.
     *
     * @param height the shape height, in meters. May be null, in which case a height of 1 is used. The height is used
     *               only when the altitude mode is {@link WorldWind#CONSTANT}, which is the default for this shape.
     */
    public ExtrudedPolygon(Double height)
    {
        this(); // to initialize the instance

        this.setHeight(height);
    }

    /**
     * Constructs an extruded polygon for a specified list of outer boundary locations and a height.
     *
     * @param corners the list of locations defining this extruded polygon's outer boundary.
     * @param height  the shape height, in meters. May be null, in which case a height of 1 is used. The height is used
     *                only when the altitude mode is {@link WorldWind#CONSTANT}, which is the default for this shape.
     *
     * @throws IllegalArgumentException if the location list is null or the height is specified but less than or equal
     *                                  to zero.
     */
    public ExtrudedPolygon(Iterable<? extends LatLon> corners, Double height)
    {
        this(); // to initialize the instance

        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height != null && height <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "height <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(corners, height);
    }

    /**
     * Constructs an extruded polygon from an outer boundary, a height, and images for its outer faces.
     *
     * @param corners      the list of locations defining this polygon's outer boundary.
     * @param height       the shape height, in meters. May be null, in which case a height of 1 is used. The height is
     *                     used only when the altitude mode is {@link WorldWind#CONSTANT}, which is the default for this
     *                     shape.
     * @param imageSources images to apply to the polygon's outer faces. One image for each face must be included. May
     *                     also be null.
     *
     * @throws IllegalArgumentException if the location list is null or the height is specified but less than or equal
     *                                  to zero.
     */
    public ExtrudedPolygon(Iterable<? extends LatLon> corners, double height, Iterable<?> imageSources)
    {
        this(corners, height);

        if (imageSources != null)
        {
            this.sideTextures = new ArrayList<List<WWTexture>>();
            this.sideTextures.add(this.fillImageList(imageSources));
        }
    }

    /**
     * Constructs an extruded polygon from an outer boundary.
     *
     * @param corners the list of outer boundary positions -- latitude longitude and altitude. The altitude mode
     *                determines whether the positions are considered relative to mean sea level (they are "absolute")
     *                or the ground elevation at the associated latitude and longitude.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public ExtrudedPolygon(Iterable<? extends Position> corners)
    {
        this(corners, 1d); // the height field is ignored when positions are specified, so any value will do
    }

    /**
     * Constructs an extruded polygon from an outer boundary specified with position heights.
     *
     * @param corners the list of positions -- latitude longitude and altitude -- defining the polygon's outer boundary.
     *                The altitude mode determines whether the positions are considered relative to mean sea level (they
     *                are "absolute") or the ground elevation at the associated latitude and longitude.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public ExtrudedPolygon(Position.PositionList corners)
    {
        this(corners.list, 1d); // the height field is ignored when positions are specified, so any value will do
    }

    /**
     * Constructs an extruded polygon from an outer boundary and apply specified textures to its outer faces.
     *
     * @param corners      the list of positions -- latitude longitude and altitude -- defining the polygon. The
     *                     altitude mode determines whether the positions are considered relative to mean sea level
     *                     (they are "absolute") or the ground elevation at the associated latitude and longitude.
     * @param imageSources textures to apply to the polygon's outer faces. One texture for each face must be included.
     *                     May also be null.
     *
     * @throws IllegalArgumentException if the position list is null.
     */
    public ExtrudedPolygon(Iterable<? extends Position> corners, Iterable<?> imageSources)
    {
        this(corners);

        if (imageSources != null)
        {
            this.sideTextures = new ArrayList<List<WWTexture>>();
            this.sideTextures.add(this.fillImageList(imageSources));
        }
    }

    protected void initialize()
    {
        // Overridden to specify a default altitude mode unique to extruded polygons.
        this.altitudeMode = DEFAULT_ALTITUDE_MODE;
    }

    protected void reset()
    {
        // Assumes that the boundary lists have already been established.

        for (List<? extends LatLon> locations : this.boundaries)
        {
            if (locations == null || locations.size() < 3)
                continue;

            if (!WWMath.computeWindingOrderOfLocations(locations).equals(AVKey.COUNTER_CLOCKWISE))
                Collections.reverse(locations);
        }

        this.totalNumLocations = this.countLocations();

        this.previousIntersectionShapeData = null;
        this.previousIntersectionTerrain = null;
        this.previousIntersectionGlobeStateKey = null;

        super.reset(); // removes all shape-data cache entries
    }

    /**
     * Counts the total number of locations in this polygon's boundaries, not including positions introduced by
     * extrusion.
     *
     * @return the number of locations in the polygon boundaries.
     */
    protected int countLocations()
    {
        int count = 0;

        for (List<? extends LatLon> locations : this.boundaries)
        {
            count += locations.size();
        }

        this.totalFaceCount = count - this.boundaries.size();

        return count;
    }

    /**
     * Returns the list of locations or positions defining this polygon's outer boundary.
     *
     * @return this polygon's positions.
     */
    public Iterable<? extends LatLon> getOuterBoundary()
    {
        return this.outerBoundary();
    }

    /**
     * Returns a reference to the outer boundary of this polygon.
     *
     * @return this polygon's outer boundary.
     */
    protected List<? extends LatLon> outerBoundary()
    {
        return this.boundaries.get(0);
    }

    /**
     * Indicates whether this shape's outer boundary exists and has more than two points.
     *
     * @return true if the outer boundary is valid, otherwise false.s
     */
    protected boolean isOuterBoundaryValid()
    {
        return this.boundaries.size() > 0 && this.boundaries.get(0).size() > 2;
    }

    /**
     * Specifies the latitude and longitude of the locations defining the outer boundary of this polygon. To specify
     * altitudes, pass {@link Position}s rather than {@link LatLon}s. The shape's height is not modified.
     *
     * @param corners the outer boundary locations.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void setOuterBoundary(Iterable<? extends LatLon> corners)
    {
        this.setOuterBoundary(corners, this.getHeight());
    }

    /**
     * Specifies the latitudes, longitudes and outer-boundary images for the outer boundary of this polygon. To specify
     * altitudes, pass {@link Position}s rather than {@link LatLon}s.
     *
     * @param corners      the polygon locations.
     * @param imageSources images to apply to the outer faces. One image must be specified for each face. May be null.
     *
     * @throws IllegalArgumentException if the location list is null.
     */
    public void setOuterBoundary(Iterable<? extends LatLon> corners, Iterable<?> imageSources)
    {
        this.setOuterBoundary(corners);

        if (imageSources == null && this.sideTextures == null)
            return;

        // Must install or replace the side textures

        if (this.sideTextures == null)
            this.sideTextures = new ArrayList<List<WWTexture>>();

        // Add or replace the first element, the outer boundary's element, in the list of side textures.
        List<WWTexture> textures = this.fillImageList(imageSources);
        this.sideTextures.set(0, textures);

        // Update the shape cache
        for (ShapeDataCache.ShapeDataCacheEntry entry : this.shapeDataCache)
        {
            ShapeData sd = (ShapeData) entry;
            if (sd.boundaries != null)
                sd.copySideTextureReferences(this);
        }
    }

    /**
     * Specifies the latitude and longitude of the outer boundary locations defining this polygon, and optionally the
     * extruded polygon's height. To specify altitudes for each boundary location, pass {@link Position}s rather than
     * {@link LatLon}s, but those altitudes are used only when the shape's altitude mode is {@link WorldWind#ABSOLUTE}.
     *
     * @param corners the outer boundary locations.
     * @param height  the shape height, in meters.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void setOuterBoundary(Iterable<? extends LatLon> corners, Double height)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.getBoundaries().set(0, this.fillBoundary(corners));

        if (height != null)
            this.height = height;

        this.reset();
    }

    protected List<? extends LatLon> fillBoundary(Iterable<? extends LatLon> corners)
    {
        ArrayList<LatLon> list = new ArrayList<LatLon>();
        for (LatLon corner : corners)
        {
            if (corner != null)
                list.add(corner);
        }

        if (list.size() < 3)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Close the list if not already closed.
        if (list.size() > 0 && !list.get(0).equals(list.get(list.size() - 1)))
            list.add(list.get(0));

        list.trimToSize();

        return list;
    }

    /**
     * Add an inner boundary to this polygon.
     *
     * @param corners the boundary's locations.
     *
     * @throws IllegalArgumentException if the location list is null or contains fewer than three locations.
     */
    public void addInnerBoundary(Iterable<? extends LatLon> corners)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationInListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.getBoundaries().add(this.fillBoundary(corners));

        this.reset();
    }

    /**
     * Add an inner boundary to this polygon and specify images to apply to each of the boundary's faces. Specify {@link
     * LatLon}s to use the polygon's single height, or {@link Position}s, to include individual altitudes.
     *
     * @param corners      the boundary's locations.
     * @param imageSources images to apply to the boundary's faces. One image must be specified for each face. May be
     *                     null.
     *
     * @throws IllegalArgumentException if the location list is null.
     */
    public void addInnerBoundary(Iterable<? extends LatLon> corners, Iterable<?> imageSources)
    {
        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationInListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.getBoundaries().add(this.fillBoundary(corners));

        if (imageSources != null)
        {
            if (this.sideTextures == null)
            {
                this.sideTextures = new ArrayList<List<WWTexture>>();
                this.sideTextures.add(new ArrayList<WWTexture>()); // placeholder for outer boundary
            }

            this.sideTextures.add(this.fillImageList(imageSources));
        }

        this.reset();
    }

    /**
     * Returns this shape's boundaries.
     *
     * @return this shape's boundaries.
     */
    protected List<List<? extends LatLon>> getBoundaries()
    {
        return this.boundaries;
    }

    /**
     * Creates texture object for a boundary's image sources.
     *
     * @param imageSources the images to apply for this boundary.
     *
     * @return the list of texture objects, or null if the <code>imageSources</code> argument is null.
     */
    protected List<WWTexture> fillImageList(Iterable<?> imageSources)
    {
        if (imageSources == null)
            return null;

        ArrayList<WWTexture> textures = new ArrayList<WWTexture>();

        for (Object source : imageSources)
        {
            if (source != null)
                textures.add(this.makeTexture(source));
            else
                textures.add(null);
        }

        textures.trimToSize();

        return textures;
    }

    /**
     * Returns this extruded polygon's cap image.
     *
     * @return the texture image source, or null if no source has been specified.
     */
    public Object getCapImageSource()
    {
        return this.capTexture != null ? this.capTexture.getImageSource() : null;
    }

    /**
     * Specifies the image to apply to this extruded polygon's cap.
     *
     * @param imageSource   the image source. May be a {@link String} identifying a file path or URL, a {@link File}, or
     *                      a {@link java.net.URL}.
     * @param texCoords     the (s, t) texture coordinates aligning the image to the polygon. There must be one texture
     *                      coordinate pair, (s, t), for each location in the polygon's outer boundary.
     * @param texCoordCount the number of texture coordinates, (s, v) pairs, specified.
     *
     * @throws IllegalArgumentException if the image source is not null and either the texture coordinates are null or
     *                                  inconsistent with the specified texture-coordinate count, or there are fewer
     *                                  than three texture coordinate pairs.
     */
    public void setCapImageSource(Object imageSource, float[] texCoords, int texCoordCount)
    {
        if (imageSource == null)
        {
            this.capTexture = null;
            this.capTextureCoords = null;
            return;
        }

        if (texCoords == null)
        {
            String message = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (texCoordCount < 3 || texCoords.length < 2 * texCoordCount)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.capTexture = this.makeTexture(imageSource);

        // Determine whether the tex-coord list needs to be closed.
        boolean closeIt = texCoords[0] != texCoords[texCoordCount - 2] || texCoords[1] != texCoords[texCoordCount - 1];

        this.capTextureCoords = Buffers.newDirectFloatBuffer(2 * (texCoordCount + (closeIt ? 1 : 0)));
        for (int i = 0; i < 2 * texCoordCount; i++)
        {
            this.capTextureCoords.put(texCoords[i]);
        }

        if (closeIt)
        {
            this.capTextureCoords.put(this.capTextureCoords.get(0));
            this.capTextureCoords.put(this.capTextureCoords.get(1));
        }
    }

    /**
     * Returns the texture coordinates for this polygon's cap.
     *
     * @return the texture coordinates, or null if no cap texture coordinates have been specified.
     */
    public float[] getTextureCoords()
    {
        if (this.capTextureCoords == null)
            return null;

        float[] retCoords = new float[this.capTextureCoords.limit()];
        this.capTextureCoords.get(retCoords, 0, retCoords.length);

        return retCoords;
    }

    /**
     * Get the texture applied to this extruded polygon's cap.
     *
     * @return The texture, or null if there is no texture or the texture is not yet available.
     */
    protected WWTexture getCapTexture()
    {
        return this.capTexture;
    }

    /**
     * Returns the height of this extruded polygon.
     *
     * @return the height originally specified, in meters.
     */
    public double getHeight()
    {
        return height;
    }

    /**
     * Specifies the height of this extruded polygon.
     *
     * @param height the height, in meters.
     *
     * @throws IllegalArgumentException if the height is less than or equal to zero.
     */
    public void setHeight(double height)
    {
        if (this.height == height)
            return;

        if (height <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "height <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.height = height;
        this.reset();
    }

    /**
     * Indicates whether the cap of this extruded polygon is drawn.
     *
     * @return true to draw the cap, otherwise false.
     */
    public boolean isEnableCap()
    {
        return enableCap;
    }

    /**
     * Specifies whether the cap of this extruded polygon is drawn.
     *
     * @param enableCap true to draw the cap, otherwise false.
     */
    public void setEnableCap(boolean enableCap)
    {
        this.enableCap = enableCap;
    }

    /**
     * Indicates whether the sides of this extruded polygon are drawn.
     *
     * @return true to draw the sides, otherwise false.
     */
    public boolean isEnableSides()
    {
        return enableSides;
    }

    /**
     * Specifies whether to draw the sides of this extruded polygon.
     *
     * @param enableSides true to draw the sides, otherwise false.
     */
    public void setEnableSides(boolean enableSides)
    {
        this.enableSides = enableSides;
    }

    /**
     * Returns the attributes applied to this polygon's side faces.
     *
     * @return this polygon's side attributes.
     */
    public ShapeAttributes getSideAttributes()
    {
        return this.sideAttributes;
    }

    /**
     * Specifies the attributes applied to this polygon's side faces.
     *
     * @param attributes this polygon's side attributes.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is null.
     */
    public void setSideAttributes(ShapeAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sideAttributes = attributes;
    }

    /**
     * Returns the attributes applied to this polygon's cap.
     *
     * @return this polygon's cap attributes.
     */
    public ShapeAttributes getCapAttributes()
    {
        return this.getAttributes();
    }

    /**
     * Specifies the attributes applied to this polygon's cap.
     *
     * @param attributes this polygon's cap attributes.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is null.
     */
    public void setCapAttributes(ShapeAttributes attributes)
    {
        this.setAttributes(attributes);
    }

    /**
     * Returns the highlight attributes applied to this polygon's side faces.
     *
     * @return this polygon's side highlight attributes.
     */
    public ShapeAttributes getSideHighlightAttributes()
    {
        return sideHighlightAttributes;
    }

    /**
     * Specifies the highlight attributes applied to this polygon's side faces.
     *
     * @param attributes this polygon's side highlight attributes.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is null.
     */
    public void setSideHighlightAttributes(ShapeAttributes attributes)
    {
        if (attributes == null)
        {
            String message = "nullValue.AttributesIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sideHighlightAttributes = attributes;
    }

    /**
     * Returns the highlight attributes applied to this polygon's cap.
     *
     * @return this polygon's cap highlight attributes.
     */
    public ShapeAttributes getCapHighlightAttributes()
    {
        return this.getHighlightAttributes();
    }

    /**
     * Specifies the highlight attributes applied to this polygon's cap.
     *
     * @param attributes this polygon's cap highlight attributes.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is null.
     */
    public void setCapHighlightAttributes(ShapeAttributes attributes)
    {
        this.setHighlightAttributes(attributes);
    }

    /**
     * Each time this polygon is rendered the appropriate attributes for the current mode are determined. This method
     * returns the resolved attributes.
     *
     * @return the currently active attributes for this polygon's side faces.
     */
    protected ShapeAttributes getActiveSideAttributes()
    {
        return this.activeSideAttributes;
    }

    /**
     * Each time this polygon is rendered the appropriate attributes for the current mode are determined. This method
     * returns the resolved attributes.
     *
     * @return the currently active attributes for this polygon's cap.
     */
    protected ShapeAttributes getActiveCapAttributes()
    {
        return this.getActiveAttributes();
    }

    public Sector getSector()
    {
        if (this.sector == null && this.outerBoundary().size() > 2)
            this.sector = Sector.boundingSector(this.getOuterBoundary());

        return this.sector;
    }

    /**
     * Indicates the location to use as a reference location for computed geometry.
     *
     * @return the reference location, or null if no reference location has been specified.
     *
     * @see #getReferencePosition()
     */
    public LatLon getReferenceLocation()
    {
        return this.getReferencePosition();
    }

    /**
     * Specifies the location to use as a reference location for computed geometry. This value should typically be left
     * to the default, which is the first location in the extruded polygon's outer boundary.
     *
     * @param referenceLocation the reference location. May be null, in which case the first location of the outer
     *                          boundary is the reference location.
     *
     * @see #setReferencePosition(gov.nasa.worldwind.geom.Position)
     */
    public void setReferenceLocation(LatLon referenceLocation)
    {
        if (referenceLocation == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.referencePosition = new Position(referenceLocation, 0);
    }

    /**
     * Indicates the position used as a reference position for this extruded polygon's computed geometry.
     *
     * @return the reference position, or null if no reference position has been specified.
     */
    public Position getReferencePosition()
    {
        if (this.referencePosition != null)
            return this.referencePosition;

        if (this.boundaries.size() > 0 && this.outerBoundary().size() > 0)
        {
            if (this.outerBoundary().get(0) instanceof Position)
                this.referencePosition = (Position) this.outerBoundary().get(0);
            else
                this.referencePosition = new Position(this.outerBoundary().get(0), 0);
        }

        return this.referencePosition;
    }

    /**
     * Returns this object's base depth.
     *
     * @return This object's base depth, in meters.
     *
     * @see #setBaseDepth(double)
     */
    public double getBaseDepth()
    {
        return baseDepth;
    }

    /**
     * Specifies a depth below the terrain at which to place this extruded polygon's base vertices. This value does not
     * affect the height of the extruded polygon nor the position of its cap. It positions the base vertices such that
     * they are the specified distance below the terrain. The default value is zero, therefore the base vertices are
     * position on the terrain.
     *
     * @param baseDepth the depth in meters to position the base vertices below the terrain. Specify positive values to
     *                  position the base vertices below the terrain. (Negative values position the base vertices above
     *                  the terrain.)
     */
    public void setBaseDepth(double baseDepth)
    {
        this.baseDepth = baseDepth;
    }

    /**
     * Returns this extruded polygon's side images.
     *
     * @return a collection of lists each identifying the image sources for the associated outer or inner polygon
     *         boundary.
     */
    public List<List<Object>> getImageSources()
    {
        if (this.sideTextures == null)
            return null;

        boolean hasTextures = false;
        for (List<WWTexture> textures : this.sideTextures)
        {
            if (textures != null && textures.size() > 0)
            {
                hasTextures = true;
                break;
            }
        }

        if (!hasTextures)
            return null;

        List<List<Object>> imageSources = new ArrayList<List<Object>>(this.getBoundaries().size());

        for (List<WWTexture> textures : this.sideTextures)
        {
            if (textures == null)
            {
                imageSources.add(null);
            }
            else
            {
                ArrayList<Object> images = new ArrayList<Object>(textures.size());
                imageSources.add(images);

                for (WWTexture image : textures)
                {
                    images.add(image.getImageSource());
                }
            }
        }

        return imageSources;
    }

    /**
     * Indicates whether side images have been specified for this extruded polygon.
     *
     * @return true if side textures have been specified, otherwise false.
     */
    public boolean hasSideTextures()
    {
        if (this.sideTextures == null)
            return false;

        for (List<WWTexture> textures : this.sideTextures)
        {
            if (textures != null && textures.size() > 0)
                return true;
        }

        return false;
    }

    @Override
    protected boolean mustApplyTexture(DrawContext dc)
    {
        if (this.getCapTexture() != null && this.capTextureCoords != null)
            return true;

        return this.mustApplySideTextures();
    }

    @Override
    protected boolean isTerrainDependent()
    {
        return true; // this shape is terrain dependent regardless of the altitude mode
    }

    protected boolean mustApplySideTextures()
    {
        return this.hasSideTextures();
    }

    /**
     * Indicates whether the interior of either the sides or cap must be drawn.
     *
     * @return true if an interior must be drawn, otherwise false.
     */
    protected boolean mustDrawInterior()
    {
        return super.mustDrawInterior() || this.getActiveSideAttributes().isDrawInterior();
    }

    /**
     * Indicates whether the polygon's outline should be drawn.
     *
     * @return true if the outline should be drawn, otherwise false.
     */
    protected boolean mustDrawOutline()
    {
        return super.mustDrawOutline() || this.getActiveSideAttributes().isDrawOutline();
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        if (shapeData.capVertexBuffer == null || shapeData.sideVertexBuffer == null)
            return true;

        if (dc.getVerticalExaggeration() != shapeData.getVerticalExaggeration())
            return true;

        if ((this.mustApplyLighting(dc, this.getActiveCapAttributes()) && shapeData.capNormalBuffer == null)
            || (this.mustApplyLighting(dc, this.getActiveSideAttributes()) && shapeData.sideNormalBuffer == null))
            return true;

        return super.mustRegenerateGeometry(dc);
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        // See if we've cached an extent associated with the globe.
        Extent extent = super.getExtent(globe, verticalExaggeration);
        if (extent != null)
            return extent;

        return super.computeExtentFromPositions(globe, verticalExaggeration, this.getOuterBoundary());
    }

    /**
     * Computes this shapes extent. If a reference point is specified, the extent is translated to that reference
     * point.
     *
     * @param outerBoundary the shape's outer boundary.
     * @param refPoint      a reference point to which the extent is translated. May be null.
     *
     * @return the computed extent, or null if the extent cannot be computed.
     */
    protected Extent computeExtent(ExtrudedBoundaryInfo outerBoundary, Vec4 refPoint)
    {
        if (outerBoundary == null || outerBoundary.capVertices == null || outerBoundary.baseVertices == null)
            return null;

        Vec4[] topVertices = outerBoundary.capVertices;
        Vec4[] botVertices = outerBoundary.baseVertices;

        ArrayList<Vec4> allVertices = new ArrayList<Vec4>(2 * topVertices.length);
        allVertices.addAll(Arrays.asList(topVertices));
        allVertices.addAll(Arrays.asList(botVertices));

        Box boundingBox = Box.computeBoundingBox(allVertices);

        // The bounding box is computed relative to the polygon's reference point, so it needs to be translated to
        // model coordinates in order to indicate its model-coordinate extent.
        return boundingBox != null ? boundingBox.translate(refPoint) : null;
    }

    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes(); // determine active cap attributes in super class

        if (this.isHighlighted())
        {
            if (this.getSideHighlightAttributes() != null)
                this.activeSideAttributes.copy(this.getSideHighlightAttributes());
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                if (this.getSideAttributes() != null)
                    this.activeSideAttributes.copy(this.getSideAttributes());
                else
                    this.activeSideAttributes.copy(defaultSideAttributes);

                this.activeSideAttributes.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeSideAttributes.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
            }
        }
        else
        {
            if (this.getSideAttributes() != null)
                this.activeSideAttributes.copy(this.getSideAttributes());
            else
                this.activeSideAttributes.copy(defaultSideAttributes);
        }
    }

    @Override
    protected SurfaceShape createSurfaceShape()
    {
        SurfacePolygon polygon = new SurfacePolygon();
        this.setSurfacePolygonBoundaries(polygon);

        return polygon;
    }

    protected void setSurfacePolygonBoundaries(SurfaceShape shape)
    {
        SurfacePolygon polygon = (SurfacePolygon) shape;

        polygon.setLocations(this.getOuterBoundary());

        List<List<? extends LatLon>> bounds = this.getBoundaries();
        for (int i = 1; i < bounds.size(); i++)
        {
            polygon.addInnerBoundary(bounds.get(i));
        }
    }

    public void render(DrawContext dc)
    {
        if (!this.isOuterBoundaryValid())
            return;

        super.render(dc);
    }

    @Override
    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        return this.getCurrent().capVertexBuffer != null || this.getCurrent().sideVertexBuffer != null;
    }

    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        if (dc.getSurfaceGeometry() == null || !this.isOuterBoundaryValid())
            return false;

        this.createMinimalGeometry(dc, this.getCurrent());

        // If the shape is less that a pixel in size, don't render it.
        if (this.getExtent() == null || dc.isSmall(this.getExtent(), 1))
            return false;

        if (!this.intersectsFrustum(dc))
            return false;

        this.createFullGeometry(dc, dc.getTerrain(), this.getCurrent(), true);

        return true;
    }

    protected OGLStackHandler beginDrawing(DrawContext dc, int attrMask)
    {
        OGLStackHandler ogsh = super.beginDrawing(dc, attrMask);

        if (!dc.isPickingMode())
        {
            // Push an identity texture matrix. This prevents drawSides() from leaking GL texture matrix state. The
            // texture matrix stack is popped from OGLStackHandler.pop() within {@link #endRendering}.
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            ogsh.pushTextureIdentity(gl);
        }

        return ogsh;
    }

    @Override
    public void drawOutline(DrawContext dc)
    {
        if (this.isEnableSides() && getActiveSideAttributes().isDrawOutline())
            this.drawSideOutline(dc, this.getCurrent());

        if (this.isEnableCap() && getActiveCapAttributes().isDrawOutline())
            this.drawCapOutline(dc, this.getCurrent());
    }

    @Override
    public void drawInterior(DrawContext dc)
    {
        if (this.isEnableSides() && getActiveSideAttributes().isDrawInterior())
            this.drawSideInteriors(dc, this.getCurrent());

        if (this.isEnableCap() && getActiveCapAttributes().isDrawInterior())
            this.drawCapInterior(dc, this.getCurrent());
    }

    /**
     * Not used by this class, which overrides {@link #drawOutline(DrawContext)}.
     *
     * @param dc not used.
     */
    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        return; // this class overrides super.drawOutline so doesn't use this "do" method
    }

    /**
     * Draws the cap's outline.
     * <p/>
     * This base implementation draws the outline of the basic polygon. Subclasses should override it to draw their
     * outline or an alternate outline of the basic polygon.
     *
     * @param dc        the draw context.
     * @param shapeData the current shape data.
     */
    public void drawCapOutline(DrawContext dc, ShapeData shapeData)
    {
        this.prepareToDrawOutline(dc, this.getActiveCapAttributes(), defaultAttributes);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            if (!dc.isPickingMode() && this.mustApplyLighting(dc, this.getActiveCapAttributes()))
                gl.glNormalPointer(GL.GL_FLOAT, 0, boundary.capNormalBuffer.rewind());

            IntBuffer indices = boundary.capEdgeIndices;
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, boundary.capVertexBuffer.rewind());
            gl.glDrawElements(GL.GL_LINES, indices.limit(), GL.GL_UNSIGNED_INT, indices.rewind());
        }
//
//        // Diagnostic to show the normal vectors.
//        if (this.mustApplyLighting(dc))
//            dc.drawNormals(1000, this.vertexBuffer, this.normalBuffer);
    }

    /**
     * Draws this extruded polygon's side outline.
     *
     * @param dc        the draw context.
     * @param shapeData the current shape data.
     */
    protected void drawSideOutline(DrawContext dc, ShapeData shapeData)
    {
        this.prepareToDrawOutline(dc, this.getActiveSideAttributes(), defaultSideAttributes);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            if (!dc.isPickingMode() && this.mustApplyLighting(dc, this.getActiveSideAttributes()))
                gl.glNormalPointer(GL.GL_FLOAT, 0, boundary.sideNormalBuffer.rewind());

            IntBuffer indices = boundary.sideEdgeIndices;
            indices.rewind();

            // Don't draw the top outline if the cap will draw it.
            if (this.isEnableCap() && this.getActiveCapAttributes().isDrawOutline())
            {
                indices = indices.slice();
                indices.position(2 * boundary.faceCount);
            }

            gl.glVertexPointer(3, GL.GL_FLOAT, 0, boundary.sideVertexBuffer.rewind());
            gl.glDrawElements(GL.GL_LINES, indices.remaining(), GL.GL_UNSIGNED_INT, indices);
        }
    }

    /**
     * Not used by this class.
     *
     * @param dc not used.
     */
    @Override
    protected void doDrawInterior(DrawContext dc)
    {
        return;
    }

    /**
     * Draws the filled interior of this shape's cap.
     *
     * @param dc        the draw context.
     * @param shapeData this shape's current globe-specific data.
     */
    public void drawCapInterior(DrawContext dc, ShapeData shapeData)
    {
        super.prepareToDrawInterior(dc, this.getActiveCapAttributes(), defaultAttributes);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, this.getActiveCapAttributes()))
            gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.capNormalBuffer.rewind());

        WWTexture texture = this.getCapTexture();
        if (!dc.isPickingMode() && texture != null && this.capTextureCoords != null)
        {
            texture.bind(dc);
            texture.applyInternalTransform(dc);

            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, this.capTextureCoords.rewind());
            dc.getGL().glEnable(GL.GL_TEXTURE_2D);
            gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        }
        else
        {
            dc.getGL().glDisable(GL.GL_TEXTURE_2D);
            gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        }

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.capVertexBuffer.rewind());

        for (int i = 0; i < shapeData.cb.getPrimTypes().size(); i++)
        {
            IntBuffer ib = shapeData.capFillIndexBuffers.get(i);
            gl.glDrawElements(shapeData.cb.getPrimTypes().get(i), ib.limit(), GL.GL_UNSIGNED_INT, ib.rewind());
        }
    }

    /**
     * Draws this shape's sides.
     *
     * @param dc        the draw context.
     * @param shapeData this shape's current globe-specific data.
     */
    protected void drawSideInteriors(DrawContext dc, ShapeData shapeData)
    {
        super.prepareToDrawInterior(dc, this.getActiveSideAttributes(), defaultSideAttributes);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            if (!dc.isPickingMode() && this.mustApplyLighting(dc, this.getActiveSideAttributes()))
                gl.glNormalPointer(GL.GL_FLOAT, 0, boundary.sideNormalBuffer.rewind());

            if (!dc.isPickingMode() && boundary.sideTextureCoords != null)
            {
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
                gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, boundary.sideTextureCoords.rewind());
            }
            else
            {
                gl.glDisable(GL.GL_TEXTURE_2D);
                gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            }

            gl.glVertexPointer(3, GL.GL_FLOAT, 0, boundary.sideVertexBuffer.rewind());

            boundary.sideIndices.rewind();
            for (int j = 0; j < boundary.faceCount; j++)
            {
                if (!dc.isPickingMode() && boundary.sideTextureCoords != null)
                {
                    if (!boundary.sideTextures.get(j).bind(dc))
                        continue;

                    boundary.sideTextures.get(j).applyInternalTransform(dc);
                }

                boundary.sideIndices.position(4 * j);
                boundary.sideIndices.limit(4 * (j + 1));
                gl.glDrawElements(GL.GL_TRIANGLE_STRIP, 4, GL.GL_UNSIGNED_INT, boundary.sideIndices);
            }
        }
    }

    /**
     * Computes the information necessary to determine this extruded polygon's extent.
     *
     * @param dc        the current draw context.
     * @param shapeData this shape's current globe-specific data.
     */
    protected void createMinimalGeometry(DrawContext dc, ShapeData shapeData)
    {
        this.computeReferencePoint(dc.getTerrain(), shapeData);
        if (shapeData.getReferencePoint() == null)
            return;

        this.computeBoundaryVertices(dc.getTerrain(), shapeData.getOuterBoundaryInfo(), shapeData.getReferencePoint());

        if (this.getExtent() == null || this.getAltitudeMode() != WorldWind.ABSOLUTE)
            shapeData.setExtent(this.computeExtent(shapeData.getOuterBoundaryInfo(), shapeData.getReferencePoint()));

        shapeData.setEyeDistance(this.computeEyeDistance(dc, shapeData));
        shapeData.setGlobeStateKey(dc.getGlobe().getGlobeStateKey(dc));
        shapeData.setVerticalExaggeration(dc.getVerticalExaggeration());
    }

    /**
     * Computes the minimum distance between this shape and the eye point.
     *
     * @param dc        the draw context.
     * @param shapeData this shape's current globe-specific data.
     *
     * @return the minimum distance from the shape to the eye point.
     */
    protected double computeEyeDistance(DrawContext dc, ShapeData shapeData)
    {
        double minDistance = Double.MAX_VALUE;
        Vec4 eyePoint = dc.getView().getEyePoint();

        for (Vec4 point : shapeData.getOuterBoundaryInfo().capVertices)
        {
            double d = point.add3(shapeData.getReferencePoint()).distanceTo3(eyePoint);
            if (d < minDistance)
                minDistance = d;
        }

        return minDistance;
    }

    /**
     * Computes and sets this shape's reference point, the Cartesian position corresponding to its geographic location.
     *
     * @param terrain   the terrain to use when computing the reference point. The reference point is always on the
     *                  terrain.
     * @param shapeData the current shape data.
     */
    protected void computeReferencePoint(Terrain terrain, ShapeData shapeData)
    {
        LatLon refPos = this.getReferencePosition();
        if (refPos == null)
            return;

        shapeData.setReferencePoint(terrain.getSurfacePoint(refPos.getLatitude(), refPos.getLongitude(), 0));
    }

    /**
     * Computes a boundary set's full geometry.
     *
     * @param dc                the current draw context.
     * @param terrain           the terrain to use when computing the geometry.
     * @param shapeData         the boundary set to compute the geometry for.
     * @param skipOuterBoundary true if outer boundaries vertices do not need to be calculated, otherwise false.
     */
    protected void createFullGeometry(DrawContext dc, Terrain terrain, ShapeData shapeData,
        boolean skipOuterBoundary)
    {
        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            boundary.capEdgeIndices = this.getCapEdgeIndices(boundary.locations.size());
            boundary.sideIndices = this.getSideIndices(boundary.locations.size());
            boundary.sideEdgeIndices = this.getSideEdgeIndices(boundary.locations.size());
        }

        if (this.isEnableSides() || this.isEnableCap())
            this.createVertices(terrain, shapeData, skipOuterBoundary);

        if (this.isEnableSides())
        {
            this.createSideGeometry(shapeData);

            if (this.mustApplyLighting(dc, this.getActiveSideAttributes()))
                this.createSideNormals(shapeData);

            if (!dc.isPickingMode() && this.mustApplySideTextures())
                this.createSideTextureCoords(shapeData);
        }

        if (this.isEnableCap())
        {
            this.createCapGeometry(dc, shapeData);

            if (this.mustApplyLighting(dc, this.getActiveCapAttributes()))
                this.createCapNormals(shapeData);
        }
    }

    /**
     * Creates this shape's Cartesian vertices.
     *
     * @param terrain           the terrain to use when computing the vertices.
     * @param shapeData         the current shape data.
     * @param skipOuterBoundary if true, do not compute the outer boundary's vertices because they have already been
     *                          computed.
     */
    protected void createVertices(Terrain terrain, ShapeData shapeData, boolean skipOuterBoundary)
    {
        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            if (boundary != shapeData.getOuterBoundaryInfo() || !skipOuterBoundary)
                this.computeBoundaryVertices(terrain, boundary, shapeData.getReferencePoint());
        }
    }

    /**
     * Compute and set the Cartesian vertices for one specified boundary of this shape.
     *
     * @param terrain  the terrain to use when computing the vertices.
     * @param boundary the boundary for which to compute the vertices.
     * @param refPoint the reference point specifying the coordinate origin of the vertices.
     */
    protected void computeBoundaryVertices(Terrain terrain, ExtrudedBoundaryInfo boundary, Vec4 refPoint)
    {
        Vec4[] topVertices = boundary.capVertices;
        if (topVertices == null || topVertices.length < boundary.locations.size())
            topVertices = new Vec4[boundary.locations.size()];

        Vec4[] bottomVertices = boundary.baseVertices;
        if (bottomVertices == null || bottomVertices.length < boundary.locations.size())
            bottomVertices = new Vec4[boundary.locations.size()];

        // These variables are used to compute the independent length of each cap vertex for CONSTANT altitude mode.
        Vec4 N = null;
        Vec4 vaa = null;
        double vaaLength = 0;
        double vaLength = 0;

        boundary.faceCount = boundary.locations.size() - 1;
        for (int i = 0; i < boundary.faceCount; i++)
        {
            // The order for both top and bottom is CCW as one looks down from space onto the base polygon. For a
            // 4-sided polygon (defined by 5 lat/lon locations) the vertex order is 0-1-2-3-4.

            LatLon location = boundary.locations.get(i);

            // Compute the bottom point, which is on the terrain.
            Vec4 vert = terrain.getSurfacePoint(location.getLatitude(), location.getLongitude(), 0);

            if (this.getBaseDepth() == 0)
            {
                // Place the base vertex on the terrain.
                bottomVertices[i] = vert.subtract3(refPoint);
            }
            else
            {
                // Place the base vertex below the terrain (if base depth is positive).
                double length = vert.getLength3();
                bottomVertices[i] = vert.multiply3((length - this.getBaseDepth()) / length).subtract3(refPoint);
            }

            // Compute the top/cap point.
            if (this.getAltitudeMode() == WorldWind.CONSTANT || !(location instanceof Position))
            {
                // The shape's base is on the terrain, and all the cap vertices are at a common altitude
                // relative to the reference position. This means that the distance between the cap and base vertices
                // at the boundary locations varies. Further, the vector between the base vertex and cap vertex at each
                // boundary location is parallel to that at the reference position. The calculation below first
                // determines that reference-position vector, then applies scaled versions of it to determine the cap
                // vertex at all the other boundary positions.
                if (vaa == null)
                {
                    Position refPos = this.getReferencePosition();
                    N = terrain.getGlobe().computeSurfaceNormalAtLocation(refPos.getLatitude(), refPos.getLongitude());
                    vaa = N.multiply3(this.getHeight());
                    vaaLength = vaa.getLength3();
                    vaLength = refPoint.dot3(N);
                }

                double delta = vert.dot3(N) - vaLength;
                vert = vert.add3(vaa.multiply3(1d - delta / vaaLength));
            }
            else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            {
                vert = terrain.getSurfacePoint(location.getLatitude(), location.getLongitude(),
                    ((Position) location).getAltitude());
            }
            else // WorldWind.ABSOLUTE
            {
                vert = terrain.getGlobe().computePointFromPosition(location.getLatitude(), location.getLongitude(),
                    ((Position) location).getAltitude() * terrain.getVerticalExaggeration());
            }

            topVertices[i] = vert.subtract3(refPoint);
        }

        topVertices[boundary.locations.size() - 1] = topVertices[0];
        bottomVertices[boundary.locations.size() - 1] = bottomVertices[0];

        boundary.capVertices = topVertices;
        boundary.baseVertices = bottomVertices;
    }

    /**
     * Constructs the Cartesian geometry of this shape's sides and sets it in the specified shape data.
     *
     * @param shapeData the current shape data.
     */
    protected void createSideGeometry(ShapeData shapeData)
    {
        // The side vertex buffer requires 4 vertices of x,y,z for each polygon face.
        int vertexCoordCount = this.totalFaceCount * 4 * 3; // 4 vertices of x,y,z per face

        if (shapeData.sideVertexBuffer != null && shapeData.sideVertexBuffer.capacity() >= vertexCoordCount)
            shapeData.sideVertexBuffer.clear();
        else
            shapeData.sideVertexBuffer = Buffers.newDirectFloatBuffer(vertexCoordCount);

        // Create individual buffer slices for each boundary.
        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            boundary.sideVertexBuffer = this.fillSideVertexBuffer(boundary.capVertices, boundary.baseVertices,
                shapeData.sideVertexBuffer.slice());
            shapeData.sideVertexBuffer.position(
                shapeData.sideVertexBuffer.position() + boundary.sideVertexBuffer.limit());
        }
    }

    protected void createSideNormals(ShapeData shapeData)
    {
        int vertexCoordCount = this.totalFaceCount * 4 * 3; // 4 vertices of x,y,z per face

        if (shapeData.sideNormalBuffer != null && shapeData.sideNormalBuffer.capacity() >= vertexCoordCount)
            shapeData.sideNormalBuffer.clear();
        else
            shapeData.sideNormalBuffer = Buffers.newDirectFloatBuffer(vertexCoordCount);

        // Create individual buffer slices for each boundary.
        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            boundary.sideNormalBuffer = this.fillSideNormalBuffer(boundary.capVertices, boundary.baseVertices,
                shapeData.sideNormalBuffer.slice());
            shapeData.sideNormalBuffer.position(
                shapeData.sideNormalBuffer.position() + boundary.sideNormalBuffer.limit());
        }
    }

    /**
     * Creates the texture coordinates for this shape's sides.
     *
     * @param shapeData the current shape data.
     */
    protected void createSideTextureCoords(ShapeData shapeData)
    {
        // Create individual buffer slices for each boundary.
        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            boolean applyTextureToThisBoundary = this.hasSideTextures()
                && boundary.sideTextures != null && boundary.sideTextures.size() == boundary.faceCount;

            if (applyTextureToThisBoundary)
            {
                int texCoordSize = boundary.faceCount * 4 * 2; // n sides of 4 verts w/s,t
                if (boundary.sideTextureCoords != null && boundary.sideTextureCoords.capacity() >= texCoordSize)
                    boundary.sideTextureCoords.clear();
                else
                    boundary.sideTextureCoords = Buffers.newDirectFloatBuffer(texCoordSize);

                this.fillSideTexCoordBuffer(boundary.capVertices, boundary.baseVertices,
                    boundary.sideTextureCoords);
            }
        }
    }

    /**
     * Compute the cap geometry.
     *
     * @param dc        the current draw context.
     * @param shapeData boundary vertices are calculated during {@link #createMinimalGeometry(DrawContext,
     *                  gov.nasa.worldwind.render.ExtrudedPolygon.ShapeData)}).
     */
    protected void createCapGeometry(DrawContext dc, ShapeData shapeData)
    {
        if (shapeData.capVertexBuffer != null
            && shapeData.capVertexBuffer.capacity() >= this.totalNumLocations * 3)
            shapeData.capVertexBuffer.clear();
        else
            shapeData.capVertexBuffer = Buffers.newDirectFloatBuffer(this.totalNumLocations * 3);

        // Fill the vertex buffer. Simultaneously create individual buffer slices for each boundary. These are used to
        // draw the outline.
        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            boundary.capVertexBuffer = WWBufferUtil.copyArrayToBuffer(boundary.capVertices,
                shapeData.capVertexBuffer.slice());
            shapeData.capVertexBuffer.position(
                shapeData.capVertexBuffer.position() + boundary.capVertexBuffer.limit());
        }

        if (shapeData.cb == null) // need to tessellate only once
            this.createTessllationGeometry(dc, shapeData);

        this.generateCapInteriorIndices(shapeData);
    }

    protected void createCapNormals(ShapeData shapeData)
    {
        if (shapeData.capNormalBuffer != null
            && shapeData.capNormalBuffer.capacity() >= this.totalNumLocations * 3)
            shapeData.capNormalBuffer.clear();
        else
            shapeData.capNormalBuffer = Buffers.newDirectFloatBuffer(shapeData.capVertexBuffer.capacity());

        for (ExtrudedBoundaryInfo boundary : shapeData)
        {
            boundary.capNormalBuffer = this.computeCapNormals(boundary, shapeData.capNormalBuffer.slice());
            shapeData.capNormalBuffer.position(
                shapeData.capNormalBuffer.position() + boundary.capNormalBuffer.limit());
        }
    }

    /**
     * Compute normal vectors for an extruded polygon's cap vertices.
     *
     * @param boundary the boundary to compute normals for.
     * @param nBuf     the buffer in which to place the computed normals. Must have enough remaining space to hold the
     *                 normals.
     *
     * @return the buffer specified as input, with its limit incremented by the number of vertices copied, and its
     *         position set to 0.
     */
    protected FloatBuffer computeCapNormals(ExtrudedBoundaryInfo boundary, FloatBuffer nBuf)
    {
        int nVerts = boundary.locations.size();
        Vec4[] verts = boundary.capVertices;
        double avgX, avgY, avgZ;

        // Compute normal for first point of boundary.
        Vec4 va = verts[1].subtract3(verts[0]);
        Vec4 vb = verts[nVerts - 2].subtract3(verts[0]); // nverts - 2 because last and first are same
        avgX = (va.y * vb.z) - (va.z * vb.y);
        avgY = (va.z * vb.x) - (va.x * vb.z);
        avgZ = (va.x * vb.y) - (va.y * vb.x);

        // Compute normals for interior boundary points.
        for (int i = 1; i < nVerts - 1; i++)
        {
            va = verts[i + 1].subtract3(verts[i]);
            vb = verts[i - 1].subtract3(verts[i]);
            avgX += (va.y * vb.z) - (va.z * vb.y);
            avgY += (va.z * vb.x) - (va.x * vb.z);
            avgZ += (va.x * vb.y) - (va.y * vb.x);
        }

        avgX /= nVerts - 1;
        avgY /= nVerts - 1;
        avgZ /= nVerts - 1;
        double length = Math.sqrt(avgX * avgX + avgY * avgY + avgZ * avgZ);

        for (int i = 0; i < nVerts; i++)
        {
            nBuf.put((float) (avgX / length)).put((float) (avgY / length)).put((float) (avgZ / length));
        }

        nBuf.flip();

        return nBuf;
    }

    protected FloatBuffer fillSideVertexBuffer(Vec4[] topVerts, Vec4[] bottomVerts, FloatBuffer vBuf)
    {
        // Forms the polygon's faces from its vertices, simultaneously copying the Cartesian coordinates from a Vec4
        // array to a FloatBuffer.

        int faceCount = topVerts.length - 1;
        int size = faceCount * 4 * 3; // n sides of 4 verts of x,y,z
        vBuf.limit(size);

        // Fill the vertex buffer with coordinates for each independent face -- 4 vertices per face. Vertices need to be
        // independent in order to have different texture coordinates and normals per face.
        // For an n-sided polygon the vertex order is b0-b1-t1-t0, b1-b2-t2-t1, ... Note the counter-clockwise ordering.
        for (int i = 0; i < faceCount; i++)
        {
            int v = i;
            vBuf.put((float) bottomVerts[v].x).put((float) bottomVerts[v].y).put((float) bottomVerts[v].z);
            v = i + 1;
            vBuf.put((float) bottomVerts[v].x).put((float) bottomVerts[v].y).put((float) bottomVerts[v].z);
            v = i + 1;
            vBuf.put((float) topVerts[v].x).put((float) topVerts[v].y).put((float) topVerts[v].z);
            v = i;
            vBuf.put((float) topVerts[v].x).put((float) topVerts[v].y).put((float) topVerts[v].z);
        }

        vBuf.flip();

        return vBuf;
    }

    protected FloatBuffer fillSideNormalBuffer(Vec4[] topVerts, Vec4[] bottomVerts, FloatBuffer nBuf)
    {
        // This method parallels fillVertexBuffer. The normals are stored in exactly the same order.

        int faceCount = topVerts.length - 1;
        int size = faceCount * 4 * 3; // n sides of 4 verts of x,y,z
        nBuf.limit(size);

        for (int i = 0; i < faceCount; i++)
        {
            Vec4 va = topVerts[i + 1].subtract3(bottomVerts[i]);
            Vec4 vb = topVerts[i].subtract3(bottomVerts[i + 1]);
            Vec4 normal = va.cross3(vb).normalize3();

            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
            nBuf.put((float) normal.x).put((float) normal.y).put((float) normal.z);
        }

        nBuf.flip();

        return nBuf;
    }

    /**
     * Computes the texture coordinates for a boundary of this shape.
     *
     * @param topVerts    the boundary's top Cartesian coordinates.
     * @param bottomVerts the boundary's bottom Cartesian coordinates.
     * @param tBuf        the buffer in which to place the computed texture coordinates.
     */
    protected void fillSideTexCoordBuffer(Vec4[] topVerts, Vec4[] bottomVerts, FloatBuffer tBuf)
    {
        int faceCount = topVerts.length - 1;
        double lengths[] = new double[faceCount + 1];

        // Find the top-to-bottom lengths of the corners in order to determine their relative lengths.
        for (int i = 0; i < faceCount; i++)
        {
            lengths[i] = bottomVerts[i].distanceTo3(topVerts[i]);
        }
        lengths[faceCount] = lengths[0]; // duplicate the first length to ease iteration below

        // Fill the vertex buffer with texture coordinates for each independent face in the same order as the vertices
        // in the vertex buffer.
        int b = 0;
        for (int i = 0; i < faceCount; i++)
        {
            // Set the base texture coord to 0 for the longer side and a proportional value for the shorter side.
            if (lengths[i] > lengths[i + 1])
            {
                tBuf.put(b++, 0).put(b++, 0);
                tBuf.put(b++, 1).put(b++, (float) (1d - lengths[i + 1] / lengths[i]));
            }
            else
            {
                tBuf.put(b++, 0).put(b++, (float) (1d - lengths[i] / lengths[i + 1]));
                tBuf.put(b++, 1).put(b++, 0);
            }
            tBuf.put(b++, 1).put(b++, 1);
            tBuf.put(b++, 0).put(b++, 1);
        }
    }

    /**
     * Returns the indices defining the cap vertices.
     *
     * @param n the number of positions in the polygon.
     *
     * @return a buffer of indices that can be passed to OpenGL to draw all the shape's edges.
     */
    protected IntBuffer getCapEdgeIndices(int n)
    {
        IntBuffer ib = capEdgeIndexBuffers.get(n);
        if (ib != null)
            return ib;

        // The edges are two-point lines connecting vertex pairs.
        ib = Buffers.newDirectIntBuffer(2 * (n - 1) * 3);
        for (int i = 0; i < n - 1; i++)
        {
            ib.put(i).put(i + 1);
        }

        capEdgeIndexBuffers.put(n, ib);

        return ib;
    }

    /**
     * Returns the indices defining the vertices of each face of this extruded polygon.
     *
     * @param n the number of positions in this extruded polygon.
     *
     * @return a buffer of indices that can be passed to OpenGL to draw all face of the shape.
     */
    protected IntBuffer getSideIndices(int n)
    {
        IntBuffer ib = sideFillIndexBuffers.get(n);
        if (ib != null)
            return ib;

        // Compute them if not already computed. Each side is two triangles defined by one triangle strip. All edges
        // can't be combined into one tri-strip because each side may have its own texture and therefore different
        // texture coordinates.
        ib = Buffers.newDirectIntBuffer(n * 4);
        for (int i = 0; i < n; i++)
        {
            ib.put(4 * i + 3).put(4 * i).put(4 * i + 2).put(4 * i + 1);
        }

        sideFillIndexBuffers.put(n, ib);

        return ib;
    }

    /**
     * Returns the indices defining the vertices of a boundary's face edges.
     *
     * @param n the number of positions in the boundary.
     *
     * @return a buffer of indices that can be passed to OpenGL to draw all the boundary's edges.
     */
    protected IntBuffer getSideEdgeIndices(int n)
    {
        IntBuffer ib = sideEdgeIndexBuffers.get(n);
        if (ib != null)
            return ib;

        int nn = n - 1; // the boundary is closed so don't add an edge for the redundant position.

        // The edges are two-point lines connecting vertex pairs.

        ib = Buffers.newDirectIntBuffer((2 * nn) * 3); // 2n each for top, bottom and corners

        // Top. Keep this first so that the top edge can be turned off independently.
        for (int i = 0; i < nn; i++)
        {
            ib.put(4 * i + 2).put(4 * i + 3);
        }

        // Bottom
        for (int i = 0; i < nn; i++)
        {
            ib.put(4 * i).put(4 * i + 1);
        }

        // Corners
        for (int i = 0; i < nn; i++)
        {
            ib.put(4 * i).put(4 * i + 3);
        }

        sideEdgeIndexBuffers.put(n, ib);

        return ib;
    }

    @Override
    protected void fillVBO(DrawContext dc)
    {
        // VBOs are not used by this shape type
    }

    /**
     * Tessellates this extruded polygon's cap. This method catches {@link OutOfMemoryError} exceptions and if the draw
     * context is not null passes the exception to the rendering exception listener (see {@link
     * WorldWindow#addRenderingExceptionListener(gov.nasa.worldwind.event.RenderingExceptionListener)}).
     *
     * @param dc        the draw context.
     * @param shapeData the boundary set to tessellate
     */
    protected void createTessllationGeometry(DrawContext dc, ShapeData shapeData)
    {
        // Wrap polygon tessellation in a try/catch block. We do this to catch and handle OutOfMemoryErrors caused during
        // tessellation of the polygon vertices.
        try
        {
            Vec4 normal = this.computePolygonNormal(shapeData);

            // There's a fallback for non-computable normal in computePolygonNormal, but test here in case the fallback
            // doesn't work either.
            if (normal == null)
            {
                String message = Logging.getMessage("Geom.ShapeNormalVectorNotComputable", this);
                Logging.logger().log(java.util.logging.Level.SEVERE, message);
                shapeData.tessellationError = true;
                return;
            }

            this.tessellatePolygon(shapeData, normal.normalize3());
        }
        catch (OutOfMemoryError e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileTessellating", this);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);

            shapeData.tessellationError = true;

            if (dc != null)
            {
                //noinspection ThrowableInstanceNeverThrown
                dc.addRenderingException(new WWRuntimeException(message, e));
            }
        }
    }

    protected Vec4 computePolygonNormal(ShapeData shapeData)
    {
        Vec4 normal = WWMath.computeBufferNormal(shapeData.capVertexBuffer, 0);

        // The normal vector is null if this is a degenerate polygon representing a line or a single point. We fall
        // back to using the globe's surface normal at the reference point. This allows the tessellator to process
        // the degenerate polygon without generating an exception.
        if (normal == null)
        {
            Globe globe = shapeData.getGlobeStateKey().getGlobe();
            if (globe != null)
                normal = globe.computeSurfaceNormalAtLocation(
                    this.getReferencePosition().getLatitude(), this.getReferencePosition().getLongitude());
        }

        return normal;
    }

    /**
     * Tessellates the polygon from its vertices.
     *
     * @param shapeData the polygon boundaries.
     * @param normal    a unit normal vector for the plane containing the polygon vertices. Even though the the vertices
     *                  might not be coplanar, only one representative normal is used for tessellation.
     */
    protected void tessellatePolygon(ShapeData shapeData, Vec4 normal)
    {
        GLUTessellatorSupport glts = new GLUTessellatorSupport();
        shapeData.cb = new GLUTessellatorSupport.CollectIndexListsCallback();

        glts.beginTessellation(shapeData.cb, normal);
        try
        {
            double[] coords = new double[3];

            GLU.gluTessBeginPolygon(glts.getGLUtessellator(), null);

            int k = 0;
            for (ExtrudedBoundaryInfo boundary : shapeData)
            {
                GLU.gluTessBeginContour(glts.getGLUtessellator());
                FloatBuffer vBuf = boundary.capVertexBuffer;
                for (int i = 0; i < boundary.locations.size(); i++)
                {
                    coords[0] = vBuf.get(i * 3);
                    coords[1] = vBuf.get(i * 3 + 1);
                    coords[2] = vBuf.get(i * 3 + 2);

                    GLU.gluTessVertex(glts.getGLUtessellator(), coords, 0, k++);
                }
                GLU.gluTessEndContour(glts.getGLUtessellator());
            }

            GLU.gluTessEndPolygon(glts.getGLUtessellator());
        }
        finally
        {
            // Free any heap memory used for tessellation immediately. If tessellation has consumed all available
            // heap memory, we must free memory used by tessellation immediately or subsequent operations such as
            // message logging will fail.
            glts.endTessellation();
        }
    }

    /**
     * Construct the lists of indices that identify the tessellated shape's vertices in the vertex buffer.
     *
     * @param shapeData the current shape data.
     */
    protected void generateCapInteriorIndices(ShapeData shapeData)
    {
        GLUTessellatorSupport.CollectIndexListsCallback cb = shapeData.cb;

        if (shapeData.capFillIndices == null || shapeData.capFillIndices.capacity() < cb.getNumIndices())
            shapeData.capFillIndices = Buffers.newDirectIntBuffer(cb.getNumIndices());
        else
            shapeData.capFillIndices.clear();

        if (shapeData.capFillIndexBuffers == null || shapeData.capFillIndexBuffers.size() < cb.getPrimTypes().size())
            shapeData.capFillIndexBuffers = new ArrayList<IntBuffer>(cb.getPrimTypes().size());
        else
            shapeData.capFillIndexBuffers.clear();

        for (List<Integer> prim : cb.getPrims())
        {
            IntBuffer ib = shapeData.capFillIndices.slice();
            for (Integer i : prim)
            {
                ib.put(i);
            }
            ib.flip();
            shapeData.capFillIndexBuffers.add(ib);
            shapeData.capFillIndices.position(shapeData.capFillIndices.position() + ib.limit());
        }
    }

    protected boolean isSameAsPreviousTerrain(Terrain terrain)
    {
        if (terrain == null || terrain != this.previousIntersectionTerrain)
            return false;

        //noinspection SimplifiableIfStatement
        if (terrain.getVerticalExaggeration() != this.previousIntersectionTerrain.getVerticalExaggeration())
            return false;

        return this.previousIntersectionGlobeStateKey != null &&
            terrain.getGlobe().getGlobeStateKey().equals(this.previousIntersectionGlobeStateKey);
    }

    /**
     * Compute the intersections of a specified line with this extruded polygon. If the polygon's altitude mode is other
     * than {@link WorldWind#ABSOLUTE}, the extruded polygon's geometry is created relative to the specified terrain
     * rather than the terrain used during rendering, which may be at lower level of detail than required for accurate
     * intersection determination.
     *
     * @param line    the line to intersect.
     * @param terrain the {@link Terrain} to use when computing the extruded polygon's geometry.
     *
     * @return a list of intersections identifying where the line intersects the extruded polygon, or null if the line
     *         does not intersect the extruded polygon.
     *
     * @throws InterruptedException if the operation is interrupted.
     * @see Terrain
     */
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        if (!this.isEnableSides() && !this.isEnableCap())
            return null;

        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return null;

        if (!this.isOuterBoundaryValid())
            return null;

        // Reuse the previously computed high-res boundary set if the terrain is the same.
        ShapeData highResShapeData = this.isSameAsPreviousTerrain(terrain) ? this.previousIntersectionShapeData
            : null;

        if (highResShapeData == null)
        {
            highResShapeData = this.createIntersectionGeometry(terrain);
            if (highResShapeData == null)
                return null;

            this.previousIntersectionShapeData = highResShapeData;
            this.previousIntersectionTerrain = terrain;
            this.previousIntersectionGlobeStateKey = terrain.getGlobe().getGlobeStateKey();
        }

        if (highResShapeData.getExtent() != null && highResShapeData.getExtent().intersect(line) == null)
            return null;

        final Line localLine = new Line(line.getOrigin().subtract3(highResShapeData.getReferencePoint()),
            line.getDirection());
        List<Intersection> intersections = new ArrayList<Intersection>();

        for (ExtrudedBoundaryInfo boundary : highResShapeData)
        {
            List<Intersection> boundaryIntersections = this.intersectBoundarySides(localLine, boundary);

            if (boundaryIntersections != null && boundaryIntersections.size() > 0)
                intersections.addAll(boundaryIntersections);
        }

        if (this.isEnableCap())
            this.intersectCap(localLine, highResShapeData, intersections);

        if (intersections.size() == 0)
            return null;

        for (Intersection intersection : intersections)
        {
            Vec4 pt = intersection.getIntersectionPoint().add3(highResShapeData.getReferencePoint());
            intersection.setIntersectionPoint(pt);

            // Compute intersection position relative to ground.
            Position pos = terrain.getGlobe().computePositionFromPoint(pt);
            Vec4 gp = terrain.getSurfacePoint(pos.getLatitude(), pos.getLongitude(), 0);
            double dist = Math.sqrt(pt.dotSelf3()) - Math.sqrt(gp.dotSelf3());
            intersection.setIntersectionPosition(new Position(pos, dist));

            intersection.setObject(this);
        }

        return intersections;
    }

    protected ShapeData createIntersectionGeometry(Terrain terrain)
    {
        ShapeData shapeData = new ShapeData(null, this);
        shapeData.setGlobeStateKey(terrain.getGlobe().getGlobeStateKey());

        this.computeReferencePoint(terrain, shapeData);
        if (shapeData.getReferencePoint() == null)
            return null;

        // Compute the boundary vertices first.
        this.createVertices(terrain, shapeData, false);

        if (this.isEnableSides())
            this.createSideGeometry(shapeData);

        if (this.isEnableCap())
            this.createCapGeometry(null, shapeData);

        shapeData.setExtent(this.computeExtent(shapeData.getOuterBoundaryInfo(), shapeData.getReferencePoint()));

        return shapeData;
    }

    /**
     * Intersects a line with the sides of an individual boundary.
     *
     * @param line     the line to intersect.
     * @param boundary the boundary to intersect.
     *
     * @return the computed intersections, or null if there are no intersections.
     *
     * @throws InterruptedException if the operation is interrupted.
     */
    protected List<Intersection> intersectBoundarySides(Line line, ExtrudedBoundaryInfo boundary)
        throws InterruptedException
    {
        List<Intersection> intersections = new ArrayList<Intersection>();
        Vec4[] topVertices = boundary.capVertices;
        Vec4[] bottomVertices = boundary.baseVertices;

        for (int i = 0; i < boundary.baseVertices.length - 1; i++)
        {
            Vec4 va = bottomVertices[i];
            Vec4 vb = topVertices[i + 1];
            Vec4 vc = topVertices[i];

            Intersection intersection = Triangle.intersect(line, va, vb, vc);
            if (intersection != null)
                intersections.add(intersection);

            vc = bottomVertices[i + 1];

            intersection = Triangle.intersect(line, va, vb, vc);
            if (intersection != null)
                intersections.add(intersection);
        }

        return intersections.size() > 0 ? intersections : null;
    }

    protected void intersectCap(Line line, ShapeData shapeData, List<Intersection> intersections)
        throws InterruptedException
    {
        if (shapeData.cb.getPrimTypes() == null)
            return;

        for (int i = 0; i < shapeData.cb.getPrimTypes().size(); i++)
        {
            IntBuffer ib = shapeData.capFillIndexBuffers.get(i);
            ib.rewind();
            List<Intersection> ti = Triangle.intersectTriangleTypes(line, shapeData.capVertexBuffer, ib,
                shapeData.cb.getPrimTypes().get(i));

            if (ti != null && ti.size() > 0)
                intersections.addAll(ti);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Note that this method overwrites the boundary locations lists, and therefore no longer refer to the originally
     * specified boundary lists.
     *
     * @param position the new position of the shape's reference position.
     */
    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isOuterBoundaryValid())
            return;

        Position oldPosition = this.getReferencePosition();
        if (oldPosition == null)
            return;

        List<List<? extends LatLon>> newLocations = new ArrayList<List<? extends LatLon>>(this.boundaries.size());

        for (List<? extends LatLon> boundary : this.boundaries)
        {
            if (boundary == null || boundary.size() == 0)
                continue;

            List<LatLon> newList = LatLon.computeShiftedLocations(oldPosition, position, boundary);
            if (newList == null)
                continue;

            // Must convert the new locations to positions if the old ones were positions.
            for (int i = 0; i < boundary.size(); i++)
            {
                if (boundary.get(i) instanceof Position)
                    newList.set(i, new Position(newList.get(i), ((Position) boundary.get(i)).getAltitude()));
            }

            newLocations.add(newList);
        }

        this.boundaries = newLocations;
        this.setReferencePosition(position);
        this.reset();
    }

    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        // Write geometry
        xmlWriter.writeStartElement("Polygon");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters("1");
        xmlWriter.writeEndElement();

        final String altitudeMode = KMLExportUtil.kmlAltitudeMode(getAltitudeMode());
        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        this.writeKMLBoundaries(xmlWriter);

        xmlWriter.writeEndElement(); // Polygon
    }

    protected void writeKMLBoundaries(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        // Outer boundary
        Iterable<? extends LatLon> outerBoundary = this.getOuterBoundary();
        if (outerBoundary != null)
        {
            xmlWriter.writeStartElement("outerBoundaryIs");
            if (outerBoundary.iterator().hasNext() && outerBoundary.iterator().next() instanceof Position)
                this.exportBoundaryAsLinearRing(xmlWriter, outerBoundary);
            else
                KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, outerBoundary, getHeight());
            xmlWriter.writeEndElement(); // outerBoundaryIs
        }

        // Inner boundaries
        Iterator<List<? extends LatLon>> boundaryIterator = this.boundaries.iterator();
        if (boundaryIterator.hasNext())
            boundaryIterator.next(); // Skip outer boundary, we already dealt with it above

        while (boundaryIterator.hasNext())
        {
            List<? extends LatLon> boundary = boundaryIterator.next();

            xmlWriter.writeStartElement("innerBoundaryIs");
            if (boundary.iterator().hasNext() && boundary.iterator().next() instanceof Position)
                this.exportBoundaryAsLinearRing(xmlWriter, outerBoundary);
            else
                KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, boundary, getHeight());
            xmlWriter.writeEndElement(); // innerBoundaryIs
        }
    }

    /**
     * Writes the boundary in KML as either a list of lat, lon, altitude tuples or lat, lon tuples, depending on the
     * type originally specified.
     *
     * @param xmlWriter the XML writer.
     * @param boundary  the boundary to write.
     *
     * @throws XMLStreamException if an error occurs during writing.
     */
    protected void exportBoundaryAsLinearRing(XMLStreamWriter xmlWriter, Iterable<? extends LatLon> boundary)
        throws XMLStreamException
    {
        xmlWriter.writeStartElement("LinearRing");
        xmlWriter.writeStartElement("coordinates");
        for (LatLon location : boundary)
        {
            if (location instanceof Position)
            {
                xmlWriter.writeCharacters(String.format(Locale.US, "%f,%f,%f ",
                    location.getLongitude().getDegrees(),
                    location.getLatitude().getDegrees(),
                    ((Position) location).getAltitude()));
            }
            else
            {
                xmlWriter.writeCharacters(String.format(Locale.US, "%f,%f ",
                    location.getLongitude().getDegrees(),
                    location.getLatitude().getDegrees()));
            }
        }
        xmlWriter.writeEndElement(); // coordinates
        xmlWriter.writeEndElement(); // LinearRing
    }
}
