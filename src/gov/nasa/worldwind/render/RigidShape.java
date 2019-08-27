/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.airspaces.Geometry;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.nio.*;
import java.util.*;

/**
 * A general rigid volume defined by a center position and the three axis radii. If A is the radius in the north-south
 * direction, and b is the radius in the east-west direction, and c is the radius in the vertical direction (increasing
 * altitude), then A == B == C defines a unit shape, A == B > C defines a vertically flattened shape (disk-shaped), A ==
 * B < C defines a vertically stretched shape.
 *
 * @author ccrick
 * @version $Id: RigidShape.java 2990 2015-04-07 19:06:15Z tgaskins $
 */
public abstract class RigidShape extends AbstractShape
{
    /**
     * Maintains globe-dependent computed data such as Cartesian vertices and extents. One entry exists for each
     * distinct globe that this shape encounters in calls to {@link AbstractShape#render(DrawContext)}. See {@link
     * AbstractShape}.
     */
    protected static class ShapeData extends AbstractShapeData
    {
        /** Holds the computed tessellation of the shape in model coordinates. */
        protected List<Geometry> meshes = new ArrayList<Geometry>();
        /** The GPU-resource cache keys to use for this entry's VBOs (one for eack LOD), if VBOs are used. */
        protected Map<Integer, Object> vboCacheKeys = new HashMap<Integer, Object>();

        /** Indicates whether the index buffer needs to be filled because a new buffer is used or some other reason. */
        protected boolean refillIndexBuffer = true; // set to true if the index buffer needs to be refilled
        /** Indicates whether the index buffer's VBO needs to be filled because a new buffer is used or other reason. */
        protected boolean refillIndexVBO = true; // set to true if the index VBO needs to be refilled

        public ShapeData(DrawContext dc, RigidShape shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);
            //super(dc, 0, 0); // specify 0 as expiry time since only size/position transform changes with time
        }

        public Geometry getMesh()
        {
            return meshes.get(0);
        }

        public Geometry getMesh(int index)
        {
            return meshes.get(index);
        }

        public List<Geometry> getMeshes()
        {
            return meshes;
        }

        public void setMesh(Geometry mesh)
        {
            this.addMesh(0, mesh);
        }

        public void setMeshes(List<Geometry> meshes)
        {
            this.meshes = meshes;
        }

        public void addMesh(Geometry mesh)
        {
            this.addMesh(this.meshes.size(), mesh);
        }

        public void addMesh(int index, Geometry mesh)
        {
            this.meshes.add(index, mesh);
        }

        public Object getVboCacheKey(int index)
        {
            return vboCacheKeys.get(index);
        }

        public void setVboCacheKey(int index, Object vboCacheKey)
        {
            this.vboCacheKeys.put(index, vboCacheKey);
        }

        public int getVboCacheSize()
        {
            return this.vboCacheKeys.size();
        }
    }

    /**
     * Returns the current shape data cache entry.
     *
     * @return the current data cache entry.
     */
    protected ShapeData getCurrentShapeData()
    {
        return (ShapeData) this.getCurrentData();
    }

    public class Offsets
    {
        protected Map<Integer, float[]> offsets;

        public Offsets()
        {
            offsets = new HashMap<Integer, float[]>();

            // set default values to zero offset
            float[] zeroOffset = {0.0f, 0.0f};
            for (int i = 0; i < 4; i++)
            {
                offsets.put(i, zeroOffset);
            }
        }

        public float[] getOffset(int index)
        {
            return offsets.get(index);
        }

        public void setOffset(int index, float uOffset, float vOffset)
        {
            float[] offsetPair = {uOffset, vOffset};
            offsets.put(index, offsetPair);
        }
    }

    protected int faceCount = 1;   // number of separate geometric face that comprise this shape

    protected Position centerPosition = Position.ZERO;
    protected double northSouthRadius = 1; // radius in the north-south (latitudinal) direction
    protected double verticalRadius = 1; // radius in the vertical direction
    protected double eastWestRadius = 1; // radius in the east-west (longitudinal) direction

    protected Angle heading; // rotation about vertical axis, positive counter-clockwise
    protected Angle tilt; // rotation about east-west axis, positive counter-clockwise
    protected Angle roll; // rotation about north-south axis, positive counter-clockwise

    protected Angle skewNorthSouth = Angle.POS90;
    protected Angle skewEastWest = Angle.POS90;

    protected boolean renderExtent = false;

    // Geometry
    protected double detailHint = 0;
    protected GeometryBuilder geometryBuilder = new GeometryBuilder();

    // Key values for Level of Detail-related key-value pairs
    protected static final String GEOMETRY_CACHE_KEY = Geometry.class.getName();
    protected static final long DEFAULT_GEOMETRY_CACHE_SIZE = 16777216L; // 16 megabytes
    protected static final String GEOMETRY_CACHE_NAME = "Airspace Geometry"; // use same cache as Airspaces

    /** The image source of the shape's texture. */
    protected Map<Integer, Object> imageSources = new HashMap<Integer, Object>();
    // image sources for the textures for each piece of geometry
    /** The {@link WWTexture} created for the image source, if any. */
    protected Map<Integer, WWTexture> textures = new HashMap<Integer, WWTexture>();
    // optional textures for each piece of geometry

    // texture coordinate offsets for each piece
    //protected Map<Integer, Offsets> offsets = new HashMap<Integer, Offsets>();
    protected Map<Integer, OffsetsList> offsets = new HashMap<Integer, OffsetsList>();
    // texture coordinates for each piece       // TODO: is this necessary?
    protected Map<Integer, FloatBuffer> offsetTextureCoords = new HashMap<Integer, FloatBuffer>();

    // Fields used in intersection calculations
    /** The terrain used in the most recent intersection calculations. */
    protected Terrain previousIntersectionTerrain;
    /** The globe state key for the globe used in the most recent intersection calculation. */
    protected Object previousIntersectionGlobeStateKey;
    /** The shape data used for the previous intersection calculation. */
    protected ShapeData previousIntersectionShapeData;

    @Override
    protected void reset()
    {
        this.previousIntersectionShapeData = null;
        this.previousIntersectionTerrain = null;
        this.previousIntersectionGlobeStateKey = null;

        super.reset();
    }

    /**
     * Returns the texture applied to this shape's #index piece of geometry. This method returns null if this piece of
     * geometry's image source has not yet been retrieved.
     *
     * @param index the index of the piece of geometry whose texture will be returned.
     *
     * @return the texture, or null if there is no texture or the texture is not yet available.
     */
    protected WWTexture getTexture(int index)
    {
        return textures.get(index);
    }

    /**
     * Establishes the texture for this piece of the shape's geometry.
     *
     * @param index   the index of the piece of geometry for which we are setting the texture.
     * @param texture the texture for this shape.
     */
    protected void setTexture(int index, WWTexture texture)
    {
        this.textures.put(index, texture);
    }

    /**
     * Indicates the image source for this shape's optional texture for the #index piece of geometry.
     *
     * @param index the index of the piece of geometry for which we are retrieving the texture.
     *
     * @return the image source of #index texture.
     */
    public Object getImageSource(int index)
    {
        return this.imageSources.get(index);
    }

    /**
     * Specifies the single image source for this shape's optional texture, to be placed on every face.
     *
     * @param imageSource the texture image source. May be a {@link java.io.File}, file path, a stream, a URL or a
     *                    {@link java.awt.image.BufferedImage}.
     */

    public void setImageSources(Object imageSource)
    {
        if (imageSource != null)
        {
            for (int i = 0; i < getFaceCount(); i++)
            {
                setImageSource(i, imageSource);
            }
        }
    }

    /**
     * Specifies the image sources for this shape's optional textures.
     *
     * @param imageSources the list of texture image sources. May be {@link java.io.File}, file path, a stream, a URL or
     *                     a {@link java.awt.image.BufferedImage}.
     */
    public void setImageSources(Iterable imageSources)
    {
        if (imageSources != null)
        {
            int index = 0;
            for (Object imageSource : imageSources)
            {
                setImageSource(index, imageSource);
                index++;
            }
        }
    }

    /**
     * Specifies the image source for this shape's #index optional texture.
     *
     * @param index       the index of the piece of geometry for which we are setting the imageSource.
     * @param imageSource the texture image source. May be a {@link java.io.File}, file path, a stream, a URL or a
     *                    {@link java.awt.image.BufferedImage}.
     */
    public void setImageSource(int index, Object imageSource)
    {
        // check if a texture has already been created for this imageSource before creating one
        if (imageSource != null)
        {
            WWTexture newTexture = null;
            for (Integer key : imageSources.keySet())
            {
                if (imageSource.equals(imageSources.get(key)))
                {
                    newTexture = textures.get(key);
                    break;
                }
            }

            this.setTexture(index, newTexture == null ? this.makeTexture(imageSource) : newTexture);
            this.imageSources.put(index, imageSource);
        }
    }

    /**
     * Returns the number of separate faces that comprise this shape.
     *
     * @return number of faces
     */
    public int getFaceCount()
    {
        return this.faceCount;
    }

    /**
     * Sets the number of separate faces that comprise this shape.
     *
     * @param faces integer indicating how many different faces this shape has
     */
    protected void setFaceCount(int faces)
    {
        this.faceCount = faces;
    }

    public abstract int getSubdivisions();

    /**
     * Returns the pair of texture coordinate offsets corresponding to the shape face and texture coordinate specified
     * by the faceIndex and offsetIndex.
     *
     * @param faceIndex   the shape face from which to retrieve the offset pair
     * @param offsetIndex the index of the specific texture coordinate on the face whose offsets we wish to retrieve
     *
     * @return the specified texture offset pair
     */
    public float[] getOffsets(int faceIndex, int offsetIndex)
    {
        return this.offsets.get(faceIndex) != null ? this.offsets.get(faceIndex).getOffset(offsetIndex) : null;
    }

    /**
     * Sets the u and v texture coordinate offsets for the specified texture coordinate on the specified shape face.
     *
     * @param faceIndex   the shape face on which we would like to set the texture coordinate offsets
     * @param offsetIndex the index of the particular texture coordinate we would like to set offsets for
     * @param uOffset     the offset in the u direction
     * @param vOffset     the offset in the v direction
     */
    public void setOffset(int faceIndex, int offsetIndex, float uOffset, float vOffset)
    {
        if (this.offsets.get(faceIndex) != null)
        {
            this.offsets.get(faceIndex).setOffset(offsetIndex, uOffset, vOffset);
        }
    }

    /**
     * Indicates this shape's center position.
     *
     * @return this shape's center position.
     */
    public Position getCenterPosition()
    {
        return centerPosition;
    }

    /**
     * Specifies this shape's center position.
     *
     * @param centerPosition this shape's center position.
     */
    public void setCenterPosition(Position centerPosition)
    {
        if (centerPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.centerPosition = centerPosition;

        reset();
    }

    /**
     * Returns the shape's referencePosition, which is its centerPosition
     *
     * @return the centerPosition of the shape
     */
    public Position getReferencePosition()
    {
        return this.centerPosition;
    }

    /**
     * Indicates the radius of this shape's axis in the north-south (latitudinal) direction.
     *
     * @return this shape's radius in the north-south direction.
     */
    public double getNorthSouthRadius()
    {
        return northSouthRadius;
    }

    /**
     * Specifies this shape's radius in meters in the north-south (latitudinal) direction. The radius must be greater
     * than 0.
     *
     * @param northSouthRadius the shape radius in the north-south direction. Must be greater than 0.
     *
     * @throws IllegalArgumentException if the radius is not greater than 0.
     */
    public void setNorthSouthRadius(double northSouthRadius)
    {
        if (northSouthRadius <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "northSouthRadius <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.northSouthRadius = northSouthRadius;

        reset();
    }

    /**
     * Indicates the radius of this shape's axis in the east-west (longitudinal) direction.
     *
     * @return this shape's radius in the east-west direction.
     */
    public double getEastWestRadius()
    {
        return eastWestRadius;
    }

    /**
     * Specifies this shape's radius in meters in the east-west (longitudinal) direction. The radius must be greater
     * than 0.
     *
     * @param eastWestRadius the shape radius in the east-west direction. Must be greater than 0.
     *
     * @throws IllegalArgumentException if the radius is not greater than 0.
     */
    public void setEastWestRadius(double eastWestRadius)
    {
        if (eastWestRadius <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "eastWestRadius <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.eastWestRadius = eastWestRadius;

        reset();
    }

    /**
     * Indicates the radius of this shape's axis in the vertical (altitudinal) direction.
     *
     * @return this shape's radius in the vertical direction.
     */
    public double getVerticalRadius()
    {
        return verticalRadius;
    }

    /**
     * Specifies this shape's radius in meters in the vertical (altitudinal) direction. The radius must be greater than
     * 0.
     *
     * @param verticalRadius the shape radius in the vertical direction. Must be greater than 0.
     *
     * @throws IllegalArgumentException if the radius is not greater than 0.
     */
    public void setVerticalRadius(double verticalRadius)
    {
        if (verticalRadius <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "verticalRadius <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.verticalRadius = verticalRadius;

        reset();
    }

    /**
     * Indicates this shape's azimuth, its rotation about its vertical axis. North corresponds to an azimuth of 0.
     * Angles are in degrees and positive clockwise.
     *
     * @return this shape's azimuth.
     */
    public Angle getHeading()
    {
        return this.heading;
    }

    /**
     * Specifies this shape's azimuth, its rotation about its vertical axis. North corresponds to an azimuth of 0.
     * Angles are in degrees and positive clockwise.
     *
     * @param heading the shape's azimuth, in degrees.
     */
    public void setHeading(Angle heading)
    {
        // constrain values to 0 to 360 (aka 0 to 2PI) for compatibility with KML
        double degrees = heading.getDegrees();
        while (degrees < 0)
        {
            degrees += 360;
        }
        while (degrees > 360)
        {
            degrees -= 360;
        }

        this.heading = Angle.fromDegrees(degrees);

        reset();
    }

    /**
     * Indicates this shape's pitch, its rotation about its east-west axis. Angles are positive clockwise.
     *
     * @return this shape's azimuth.
     */
    public Angle getTilt()
    {
        return this.tilt;
    }

    /**
     * Specifies this shape's pitch, its rotation about its east-west axis. Angles are positive clockwise.
     *
     * @param tilt the shape's pitch, in degrees.
     */
    public void setTilt(Angle tilt)
    {
        // constrain values to 0 to 360 (aka 0 to 2PI) for compatibility with KML
        double degrees = tilt.getDegrees();
        while (degrees < 0)
        {
            degrees += 360;
        }
        while (degrees > 360)
        {
            degrees -= 360;
        }

        this.tilt = Angle.fromDegrees(degrees);

        reset();
    }

    /**
     * Indicates this shape's roll, its rotation about its north-south axis. Angles are positive clockwise.
     *
     * @return this shape's azimuth.
     */
    public Angle getRoll()
    {
        return this.roll;
    }

    /**
     * Specifies this shape's roll, its rotation about its north-south axis. Angles are in degrees and positive
     * clockwise.
     *
     * @param roll the shape's roll, in degrees.
     */
    public void setRoll(Angle roll)
    {
        // constrain values to 0 to 360 (aka 0 to 2PI) for compatibility with KML
        double degrees = roll.getDegrees();
        while (degrees < 0)
        {
            degrees += 360;
        }
        while (degrees > 360)
        {
            degrees -= 360;
        }

        this.roll = Angle.fromDegrees(degrees);

        reset();
    }

    /**
     * Indicates this shape's skew, its shearing along its north-south axis. Angles are positive counter-clockwise.
     *
     * @return this shape's North-South skew.
     */
    public Angle getSkewNorthSouth()
    {
        return skewNorthSouth;
    }

    /**
     * Specifies this shape's skew, its shearing along its north-south axis. Angles are in degrees and positive
     * counter-clockwise.
     *
     * @param skew the shape's skew in the North-South direction.
     */
    public void setSkewNorthSouth(Angle skew)
    {
        if (skew.compareTo(Angle.POS180) >= 0 || skew.compareTo(Angle.ZERO) <= 0)
        {
            String message = Logging.getMessage("generic.AngleOutOfRange",
                "skew >= 180 degrees or skew <= 0 degrees");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.skewNorthSouth = skew;

        reset();
    }

    /**
     * Indicates this shape's skew, its shearing along its east-west axis. Angles are positive counter-clockwise.
     *
     * @return this shape's East-West skew.
     */
    public Angle getSkewEastWest()
    {
        return skewEastWest;
    }

    /**
     * Specifies this shape's skew, its shearing along its east-west axis. Angles are in degrees and positive
     * counter-clockwise.
     *
     * @param skew the shape's skew in the East-West direction.
     */
    public void setSkewEastWest(Angle skew)
    {
        if (skew.compareTo(Angle.POS180) >= 0 || skew.compareTo(Angle.ZERO) <= 0)
        {
            String message = Logging.getMessage("generic.AngleOutOfRange",
                "skew >= 180 degrees or skew <= 0 degrees");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.skewEastWest = skew;

        reset();
    }

    @Override
    protected void initialize()
    {
        // Nothing to override
    }

    /**
     * Indicates the shape's detail hint, which is described in {@link #setDetailHint(double)}.
     *
     * @return the detail hint
     *
     * @see #setDetailHint(double)
     */
    public double getDetailHint()
    {
        return this.detailHint;
    }

    /**
     * Modifies the relationship of the shape's tessellation resolution to its distance from the eye. Values greater
     * than 0 cause higher resolution tessellation, but at an increased performance cost. Values less than 0 decrease
     * the default tessellation resolution at any given distance from the eye. The default value is 0. Values typically
     * range between -0.5 and 0.5.
     *
     * @param detailHint the degree to modify the default tessellation resolution of the shape. Values greater than 1
     *                   increase the resolution. Values less than zero decrease the resolution. The default value is
     *                   0.
     */
    public void setDetailHint(double detailHint)
    {
        this.detailHint = detailHint;

        reset();
    }

    /** Create the geometry cache supporting the Level of Detail system. */
    protected void setUpGeometryCache()
    {
        if (!WorldWind.getMemoryCacheSet().containsCache(GEOMETRY_CACHE_KEY))
        {
            long size = Configuration.getLongValue(AVKey.AIRSPACE_GEOMETRY_CACHE_SIZE, DEFAULT_GEOMETRY_CACHE_SIZE);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName(GEOMETRY_CACHE_NAME);
            WorldWind.getMemoryCacheSet().addCache(GEOMETRY_CACHE_KEY, cache);
        }
    }

    /**
     * Retrieve the geometry cache supporting the Level of Detail system.
     *
     * @return the geometry cache.
     */
    protected MemoryCache getGeometryCache()
    {
        return WorldWind.getMemoryCache(GEOMETRY_CACHE_KEY);
    }

    @Override
    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new ShapeData(dc, this);
    }

    @Override
    protected boolean mustApplyTexture(DrawContext dc)
    {
        boolean applyTexture = false;

        for (int i = 0; i < getFaceCount(); i++)
        {
            // TODO add error checking here?
            if (this.getTexture(i) != null && this.getCurrentShapeData().getMesh(i).getBuffer(Geometry.TEXTURE) != null)
            {
                applyTexture = true;
                break;
            }
        }
        return applyTexture;
    }

    protected boolean mustApplyTexture(int index)
    {
        return this.getTexture(index) != null &&
            this.getCurrentShapeData().getMesh(index).getBuffer(Geometry.TEXTURE) != null;
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        ShapeData shapedata = this.getCurrentShapeData();

        if (shapedata == null || shapedata.getMeshes() == null
            || shapedata.getMeshes().size() < 1)
            return true;

        if (shapedata.getMesh(0) == null
            || shapedata.getMesh(0).getBuffer(Geometry.VERTEX) == null)
            return true;

        if (dc.getVerticalExaggeration() != shapedata.getVerticalExaggeration())
            return true;

        //noinspection SimplifiableIfStatement
        if (this.getAltitudeMode() == WorldWind.ABSOLUTE
            && shapedata.getGlobeStateKey() != null
            && shapedata.getGlobeStateKey().equals(dc.getGlobe().getGlobeStateKey(dc)))
            return false;

        return super.mustRegenerateGeometry(dc);
    }

    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrentShapeData();

        Vec4 refPt = this.computeReferencePoint(dc);
        if (refPt == null)
            return false;
        shapeData.setReferencePoint(refPt);

        shapeData.setEyeDistance(dc.getView().getEyePoint().distanceTo3(refPt));
        shapeData.setGlobeStateKey(dc.getGlobe().getGlobeStateKey(dc));
        shapeData.setVerticalExaggeration(dc.getVerticalExaggeration());

        // The extent must be computed before the computeSubdivisions method is called (below) because that
        // calculation depends on the extent. Normally we couldn't compute the extent before generating the
        // geometry, but rigid shapes use the shape's fields rather than the computed geometry to compute their
        // extent. See WWJ-482 for a description of the situation that caused the extent calculation to be moved
        // to here.
        shapeData.setExtent(this.computeExtent(dc));

        // determine with how many subdivisions the geometry should be tessellated
        computeSubdivisions(dc, shapeData);

        // Recompute tessellated positions because the geometry or view may have changed.
        this.makeGeometry(shapeData);

        // If the shape is less that a pixel in size, don't render it.
        if (shapeData.getExtent() == null || dc.isSmall(shapeData.getExtent(), 1))
            return false;

        //noinspection SimplifiableIfStatement
        if (!this.intersectsFrustum(dc))
            return false;

        return !(shapeData.getMesh(0) == null || shapeData.getMesh(0).getBuffer(Geometry.VERTEX) == null
            || shapeData.getMesh(0).getCount(Geometry.VERTEX) < 2);
    }

    @Override
    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        return this.getCurrentShapeData().getMesh(0).getBuffer(Geometry.VERTEX) != null;
    }

    @Override
    protected OGLStackHandler beginDrawing(DrawContext dc, int attrMask)
    {
        OGLStackHandler ogsh = super.beginDrawing(dc, attrMask);

        if (!dc.isPickingMode())
        {
            // Push an identity texture matrix. This prevents drawGeometry() from leaking GL texture matrix state. The
            // texture matrix stack is popped from OGLStackHandler.pop().
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            ogsh.pushTextureIdentity(gl);
        }

        return ogsh;
    }

    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        for (int i = 0; i < getFaceCount(); i++)
        {
            Geometry mesh = this.getCurrentShapeData().getMesh(i);

            // set to draw using GL_LINES
            mesh.setMode(Geometry.ELEMENT, GL.GL_LINES);

            // set up MODELVIEW matrix to properly position, orient and scale this shape
            setModelViewMatrix(dc);

            this.drawGeometry(dc, this.getCurrentShapeData(), i);

            // return render mode to GL_TRIANGLE
            mesh.setMode(Geometry.ELEMENT, GL.GL_TRIANGLES);
        }
    }

    @Override
    protected void doDrawInterior(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // render extent if specified
        if (this.renderExtent)
        {
            Box extent = (Box) this.getCurrentShapeData().getExtent();
            extent.render(dc);
        }

        // set up MODELVIEW matrix to properly position, orient and scale this shape
        setModelViewMatrix(dc);

        for (int i = 0; i < getFaceCount(); i++)
        {
            // set up the texture if one exists
            if (!dc.isPickingMode() && mustApplyTexture(i) && this.getTexture(i).bind(
                dc)) // bind initiates retrieval
            {
                this.getTexture(i).applyInternalTransform(dc);

                Geometry mesh = this.getCurrentShapeData().getMesh(i);

                if (getOffsets(i, 0) != null)   // if texture offsets exist
                {
                    // apply offsets to texture coords to properly position and warp the texture
                    // TODO: should only do this when offsets have changed, e.g. during editing!
                    int bufferSize = mesh.getBuffer(Geometry.TEXTURE).limit();
                    FloatBuffer texCoords = (FloatBuffer) mesh.getBuffer(Geometry.TEXTURE);
                    FloatBuffer offsetCoords = Buffers.newDirectFloatBuffer(bufferSize);

                    for (int j = 0; j < bufferSize; j += 2)
                    {
                        float u = texCoords.get(j);
                        float v = texCoords.get(j + 1);

                        // bilinear interpolation of the uv corner offsets
                        float uOffset = -getOffsets(i, 0)[0] * (1 - u) * (v)
                            - getOffsets(i, 1)[0] * (u) * (v)
                            - getOffsets(i, 2)[0] * (1 - u) * (1 - v)
                            - getOffsets(i, 3)[0] * (u) * (1 - v)
                            + u;

                        float vOffset = -getOffsets(i, 0)[1] * (1 - u) * (v)
                            - getOffsets(i, 1)[1] * (u) * (v)
                            - getOffsets(i, 2)[1] * (1 - u) * (1 - v)
                            - getOffsets(i, 3)[1] * (u) * (1 - v)
                            + v;

                        offsetCoords.put(j, uOffset);
                        offsetCoords.put(j + 1, vOffset);
                    }

                    gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, offsetCoords.rewind());
                }
                else
                {
                    gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, mesh.getBuffer(Geometry.TEXTURE).rewind());
                }
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            }
            else
            {
                gl.glDisable(GL.GL_TEXTURE_2D);
                gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            }

            drawGeometry(dc, this.getCurrentShapeData(), i);
        }
    }

    /**
     * Computes the shape's extent using a bounding box.
     *
     * Code within this class assumes that this shape's extent is computed from the shape's fields rather than its
     * computed geometry. If you override this method, be sure that this lack of dependency on computed geometry is
     * adhered to by the overriding method. See doMakeOrderedRenderable above for a description of why this assumption
     * is made. Also see WWJ-482.
     *
     * @param dc the current drawContext
     *
     * @return the computed extent.
     */
    protected Extent computeExtent(DrawContext dc)
    {
        Matrix matrix = computeRenderMatrix(dc);

        // create a list of vertices representing the extrema of the unit sphere
        Vector<Vec4> extrema = new Vector<Vec4>(4);
        // transform the extrema by the render matrix to get their final positions
        Vec4 point = matrix.transformBy3(matrix, -1, 1, -1);   // far upper left
        extrema.add(point);
        point = matrix.transformBy3(matrix, 1, 1, 1);   // near upper right
        extrema.add(point);
        point = matrix.transformBy3(matrix, 1, -1, -1);   // near lower left
        extrema.add(point);
        point = matrix.transformBy3(matrix, -1, -1, 1);   // far lower right
        extrema.add(point);
        gov.nasa.worldwind.geom.Box boundingBox = gov.nasa.worldwind.geom.Box.computeBoundingBox(extrema);

        Vec4 centerPoint = getCurrentData().getReferencePoint();

        return boundingBox != null ? boundingBox.translate(centerPoint) : null;
    }

    /**
     * Computes the shape's extent using a bounding box.
     *
     * @param globe                the current globe
     * @param verticalExaggeration the current vertical exaggeration
     *
     * @return the computed extent.
     *
     * @throws IllegalArgumentException if the globe is null.
     */
    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // compute a bounding box for vertices transformed to their world coordinates
        Matrix matrix = computeRenderMatrix(globe, verticalExaggeration);

        // create a list of vertices representing the extrema of the unit sphere
        Vector<Vec4> extrema = new Vector<Vec4>(4);
        // transform the extrema by the render matrix to get their final positions
        Vec4 point = matrix.transformBy3(matrix, -1, 1, -1);   // far upper left
        extrema.add(point);
        point = matrix.transformBy3(matrix, 1, 1, 1);   // near upper right
        extrema.add(point);
        point = matrix.transformBy3(matrix, 1, -1, -1);   // near lower left
        extrema.add(point);
        point = matrix.transformBy3(matrix, -1, -1, 1);   // far lower right
        extrema.add(point);
        gov.nasa.worldwind.geom.Box boundingBox = Box.computeBoundingBox(extrema);

        // get - or compute - the center point, in global coordinates
        Position pos = this.getCenterPosition();
        if (pos == null)
            return null;

        Vec4 centerPoint = this.computeReferencePoint(globe, verticalExaggeration);

        // translate the bounding box to its correct location
        return boundingBox != null ? boundingBox.translate(centerPoint) : null;
    }

    /**
     * Computes the shape's sector.  Not currently supported.
     *
     * @return the bounding sector for this shape
     */
    public Sector getSector()
    {
        return null;
    }

    /**
     * Transform all vertices with the provided matrix
     *
     * @param vertices    the buffer of vertices to transform
     * @param numVertices the number of distinct vertices in the buffer (assume 3-space)
     * @param matrix      the matrix for transforming the vertices
     *
     * @return the transformed vertices.
     */
    protected FloatBuffer computeTransformedVertices(FloatBuffer vertices, int numVertices, Matrix matrix)
    {
        int size = numVertices * 3;
        FloatBuffer newVertices = Buffers.newDirectFloatBuffer(size);

        // transform all vertices by the render matrix
        for (int i = 0; i < numVertices; i++)
        {
            Vec4 point = matrix.transformBy3(matrix, vertices.get(3 * i), vertices.get(3 * i + 1),
                vertices.get(3 * i + 2));
            newVertices.put((float) point.getX()).put((float) point.getY()).put((float) point.getZ());
        }

        newVertices.rewind();

        return newVertices;
    }

    /**
     * Sets the shape's referencePoint, which is essentially its centerPosition in Cartesian coordinates.
     *
     * @param dc the current DrawContext
     *
     * @return the computed reference point relative to the globe associated with the draw context.
     */
    public Vec4 computeReferencePoint(DrawContext dc)
    {
        Position pos = this.getCenterPosition();
        if (pos == null)
            return null;

        return computePoint(dc.getTerrain(), pos);
    }

    /**
     * Sets the shape's referencePoint, which is essentially its centerPosition in Cartesian coordinates.
     *
     * @param globe                the current globe
     * @param verticalExaggeration the current vertical exaggeration
     *
     * @return the computed reference point, or null if the point could not be computed.
     */
    protected Vec4 computeReferencePoint(Globe globe, double verticalExaggeration)
    {
        Position pos = this.getCenterPosition();
        if (pos == null)
            return null;

        double elevation = globe.getElevation(pos.latitude, pos.longitude);

        double height;
        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            height = 0d + elevation * verticalExaggeration;
        else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            height = pos.getAltitude() + elevation * verticalExaggeration;
        else    // ABSOLUTE elevation mode
        {
            // Raise the shape to accommodate vertical exaggeration applied to the terrain.
            height = pos.getAltitude() * verticalExaggeration;
        }

        return globe.computePointFromPosition(pos, height);
    }

    /**
     * Computes the transform to use during rendering to convert the unit sphere geometry representation of this shape
     * to its correct shape location, orientation and scale
     *
     * @param globe                the current globe
     * @param verticalExaggeration the current vertical exaggeration
     *
     * @return the modelview transform for this shape
     *
     * @throws IllegalArgumentException if globe is null
     */
    public Matrix computeRenderMatrix(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix matrix = Matrix.IDENTITY;

        // translate and orient
        matrix = matrix.multiply(globe.computeSurfaceOrientationAtPosition(this.getCenterPosition()));

        // now apply the user-specified heading/tilt/roll:
        // order corresponds to KML rotations (YXZ, positive clockwise)

        // roll
        if (roll != null)
            matrix = matrix.multiply(Matrix.fromRotationY(Angle.POS360.subtract(this.roll)));
        // tilt
        if (tilt != null)
            matrix = matrix.multiply(Matrix.fromRotationX(Angle.POS360.subtract(this.tilt)));
        // heading
        if (heading != null)
            matrix = matrix.multiply(Matrix.fromRotationZ(Angle.POS360.subtract(this.heading)));
        //matrix = matrix.multiply(Matrix.fromRotationZ(this.heading));

        // apply skew (aka shear) matrix
        matrix = matrix.multiply(Matrix.fromSkew(this.skewEastWest, this.skewNorthSouth));

        // finally, scale it along each of the axis
        matrix = matrix.multiply(Matrix.fromScale(this.getEastWestRadius(),
            this.getNorthSouthRadius(), this.getVerticalRadius()));

        return matrix;
    }

    /**
     * Computes the transform to use during rendering to convert the unit sphere geometry representation of this shape
     * to its correct shape location, orientation and scale
     *
     * @param dc the current draw context
     *
     * @return the modelview transform for this shape
     *
     * @throws IllegalArgumentException if draw context is null or the referencePoint is null
     */
    public Matrix computeRenderMatrix(DrawContext dc)
    {
        Matrix matrix = Matrix.IDENTITY;

        // translate and orient, accounting for altitude mode
        Position refPosition = dc.getGlobe().computePositionFromPoint(this.computeReferencePoint(dc));
        matrix = matrix.multiply(dc.getGlobe().computeSurfaceOrientationAtPosition(refPosition));

        // now apply the user-specified heading/tilt/roll
        // order corresponds to KML rotations (YXZ, positive clockwise)

        // roll
        if (roll != null)
            matrix = matrix.multiply(Matrix.fromRotationY(Angle.POS360.subtract(this.roll)));
        // tilt
        if (tilt != null)
            matrix = matrix.multiply(Matrix.fromRotationX(Angle.POS360.subtract(this.tilt)));
        // heading
        if (heading != null)
            matrix = matrix.multiply(Matrix.fromRotationZ(Angle.POS360.subtract(this.heading)));
        //matrix = matrix.multiply(Matrix.fromRotationZ(this.heading));

        // apply skew (aka shear) matrix
        matrix = matrix.multiply(Matrix.fromSkew(this.skewEastWest, this.skewNorthSouth));

        // finally, scale it along each of the axis
        matrix = matrix.multiply(Matrix.fromScale(this.getEastWestRadius(),
            this.getNorthSouthRadius(), this.getVerticalRadius()));

        return matrix;
    }

    /**
     * Computes the inverse of the transform to use during rendering to convert the unit geometry representation of this
     * shape to its correct shape location, orientation and scale, essentially bringing the shape back into local
     * coordinate space.
     *
     * @param globe                the current globe
     * @param verticalExaggeration the current vertical exaggeration
     *
     * @return the inverse of the modelview transform for this shape
     *
     * @throws IllegalArgumentException if draw context is null or the referencePoint is null
     */
    public Matrix computeRenderMatrixInverse(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix matrix = Matrix.IDENTITY;

        // inverse scale along each of the axis
        matrix = matrix.multiply(Matrix.fromScale(1 / this.getEastWestRadius(),
            1 / this.getNorthSouthRadius(), 1 / this.getVerticalRadius()));

        // apply inverse skew (aka shear) matrix
        matrix = matrix.multiply(Matrix.fromSkew(Angle.POS180.subtract(this.skewEastWest),
            Angle.POS180.subtract(this.skewNorthSouth)));

        // inverse heading
        if (heading != null)
            matrix = matrix.multiply(Matrix.fromRotationZ(this.heading));
        //matrix = matrix.multiply(Matrix.fromRotationZ(this.heading));
        // inverse tilt
        if (tilt != null)
            matrix = matrix.multiply(Matrix.fromRotationX(this.tilt));
        // roll
        if (roll != null)
            matrix = matrix.multiply(Matrix.fromRotationY(this.roll));

        // translate and orient
        Position refPosition = globe.computePositionFromPoint(this.computeReferencePoint(globe, verticalExaggeration));
        matrix = matrix.multiply(globe.computeSurfaceOrientationAtPosition(refPosition).getInverse());

        /*
        Vec4 point = ellipsoidalGlobe.geodeticToCartesian(refPosition.getLatitude(), refPosition.getLongitude(),
                                                            refPosition.getElevation());
        // Rotate the coordinate system to match the latitude.
        // Latitude is treated clockwise as rotation about the X-axis. We don't flip the latitude value so as
        // to get the inverse
        matrix = matrix.multiply(Matrix.fromRotationX(refPosition.getLatitude()));

        // Rotate the coordinate system to match the longitude.
        // Longitude is treated as counter-clockwise rotation about the Y-axis.
        matrix = matrix.multiply(Matrix.fromRotationY(refPosition.getLongitude().multiply(-1.0)));
        // Transform to the cartesian coordinates of (latitude, longitude, metersElevation).
        matrix = matrix.multiply(Matrix.fromTranslation(point.multiply3(-1.0)));
        */

        return matrix;
    }

    /**
     * Called during drawing to set the modelview matrix to apply the correct position, scale and orientation for this
     * shape.
     *
     * @param dc the current DrawContext
     *
     * @throws IllegalArgumentException if draw context is null or the draw context GL is null
     */
    protected void setModelViewMatrix(DrawContext dc)
    {
        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Matrix matrix = dc.getView().getModelviewMatrix();
        matrix = matrix.multiply(computeRenderMatrix(dc));

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Were applying a scale transform on the modelview matrix, so the normal vectors must be re-normalized
        // before lighting is computed.
        gl.glEnable(GL2.GL_NORMALIZE);

        gl.glMatrixMode(GL2.GL_MODELVIEW);

        double[] matrixArray = new double[16];
        matrix.toArray(matrixArray, 0, false);
        gl.glLoadMatrixd(matrixArray, 0);
    }

    /**
     * Move the Rigid Shape to the specified destination.
     *
     * @param position the position to move the shape to
     */
    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position oldPosition = this.getReferencePosition();
        if (oldPosition == null)
            return;

        setCenterPosition(position);

        this.reset();
    }

    /**
     * Computes the number of subdivisions necessary to achieve the expected Level of Detail given the shape's
     * relationship to the viewer.
     *
     * @param dc        the current drawContext.
     * @param shapeData the current globe-specific shape data
     */
    abstract protected void computeSubdivisions(DrawContext dc, ShapeData shapeData);

    //*****************************************************************//
    //***********************  Geometry Rendering  ********************//
    //*****************************************************************//

    /**
     * Sets the Geometry mesh for this shape, either by pulling it from the geometryCache, or by creating it anew if the
     * appropriate geometry does not yet exist in the cache.
     *
     * @param shapeData the current shape data.
     */
    abstract protected void makeGeometry(ShapeData shapeData);

    protected GeometryBuilder getGeometryBuilder()
    {
        return this.geometryBuilder;
    }

    /**
     * Renders the Rigid Shape
     *
     * @param dc        the current draw context
     * @param shapeData the current shape data
     * @param index     the index of the shape face to render
     *
     * @throws IllegalArgumentException if the draw context is null or the element buffer is null
     */
    protected void drawGeometry(DrawContext dc, ShapeData shapeData, int index)
    {
        Geometry mesh = shapeData.getMesh(index);

        if (mesh.getBuffer(Geometry.ELEMENT) == null)
        {
            String message = "nullValue.ElementBufferIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int mode, count, type;
        Buffer elementBuffer;

        mode = mesh.getMode(Geometry.ELEMENT);
        count = mesh.getCount(Geometry.ELEMENT);
        type = mesh.getGLType(Geometry.ELEMENT);
        elementBuffer = mesh.getBuffer(Geometry.ELEMENT);

        this.drawGeometry(dc, mode, count, type, elementBuffer, shapeData, index);
    }

    /**
     * Renders the shape, using data from the provided buffer and the given parameters.
     *
     * @param dc            the current draw context
     * @param mode          the render mode
     * @param count         the number of elements to be drawn
     * @param type          the data type of the elements to be drawn
     * @param elementBuffer the buffer containing the list of elements to be drawn
     * @param shapeData     this shape's current globe-specific shape data
     * @param index         the index of the shape face to render
     */
    abstract protected void drawGeometry(DrawContext dc, int mode, int count, int type, Buffer elementBuffer,
        ShapeData shapeData, int index);

    //*****************************************************************//
    //*********************        VBOs               *****************//
    //*****************************************************************//

    /**
     * Get or create OpenGL resource IDs for the current data cache entry.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param index the index of the LOD whose VboID's will be retrieved.
     * @param dc    the current draw context.
     *
     * @return an array containing the coordinate vertex buffer ID in the first position and the index vertex buffer ID
     *         in the second position.
     */
    protected int[] getVboIds(int index, DrawContext dc)
    {
        ShapeData data = ((ShapeData) this.getCurrentData());
        if (data != null)
        {
            Object key = data.getVboCacheKey(index);
            if (key != null)
                return (int[]) dc.getGpuResourceCache().get(key);
        }

        return null;
    }

    /**
     * Removes from the GPU resource cache the entry for the current data cache entry's VBOs.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */
    @Override
    protected void clearCachedVbos(DrawContext dc)
    {
        for (Integer key : ((ShapeData) this.getCurrentData()).vboCacheKeys.keySet())
        {
            dc.getGpuResourceCache().remove(((ShapeData) this.getCurrentData()).getVboCacheKey(key));
        }
    }

    /**
     * Fill this shape's vertex buffer objects. If the vertex buffer object resource IDs don't yet exist, create them.
     *
     * @param dc the current draw context.
     */
    protected void fillVBO(DrawContext dc)
    {
        GL gl = dc.getGL();
        ShapeData shapeData = this.getCurrentShapeData();
        List<Geometry> meshes = shapeData.getMeshes();

        // create the cacheKey for this LOD if it doesn't yet exist
        if (shapeData.getVboCacheKey(getSubdivisions()) == null)
        {
            shapeData.setVboCacheKey(getSubdivisions(), this.getClass().toString() + getSubdivisions());
        }
        int[] vboIds = (int[]) dc.getGpuResourceCache().get(shapeData.getVboCacheKey(getSubdivisions()));
        if (vboIds == null)
        {
            int size = 0;
            for (int face = 0; face < getFaceCount(); face++)
            {
                size += meshes.get(face).getBuffer(Geometry.VERTEX).limit() * Buffers.SIZEOF_FLOAT;
                size += meshes.get(face).getBuffer(Geometry.ELEMENT).limit() * Buffers.SIZEOF_FLOAT;
            }

            vboIds = new int[2 * getFaceCount()];
            gl.glGenBuffers(vboIds.length, vboIds, 0);
            dc.getGpuResourceCache().put(shapeData.getVboCacheKey(getSubdivisions()), vboIds,
                GpuResourceCache.VBO_BUFFERS, size);

            shapeData.refillIndexVBO = true;
        }

        if (shapeData.refillIndexVBO)
        {
            try
            {
                for (int face = 0; face < getFaceCount(); face++)
                {
                    IntBuffer ib = (IntBuffer) meshes.get(face).getBuffer(Geometry.ELEMENT);
                    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[2 * face + 1]);
                    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, ib.limit() * Buffers.SIZEOF_FLOAT, ib.rewind(),
                        GL.GL_DYNAMIC_DRAW);
                }

                shapeData.refillIndexVBO = false;
            }
            finally
            {
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }

        try
        {
            for (int face = 0; face < getFaceCount(); face++)
            {
                FloatBuffer vb = (FloatBuffer) meshes.get(face).getBuffer(Geometry.VERTEX);
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[2 * face]);
                gl.glBufferData(GL.GL_ARRAY_BUFFER, vb.limit() * Buffers.SIZEOF_FLOAT, vb.rewind(),
                    GL.GL_STATIC_DRAW);
            }
        }
        finally
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    //*****************************************************************//
    //*********************       Intersections       *****************//
    //*****************************************************************//

    protected boolean isSameAsPreviousTerrain(Terrain terrain)
    {
        if (terrain == null || this.previousIntersectionTerrain == null || terrain != this.previousIntersectionTerrain)
            return false;

        //noinspection SimplifiableIfStatement
        if (terrain.getVerticalExaggeration() != this.previousIntersectionTerrain.getVerticalExaggeration())
            return false;

        return this.previousIntersectionGlobeStateKey != null &&
            terrain.getGlobe().getGlobeStateKey().equals(this.previousIntersectionGlobeStateKey);
    }

    public void clearIntersectionGeometry()
    {
        this.previousIntersectionGlobeStateKey = null;
        this.previousIntersectionShapeData = null;
        this.previousIntersectionTerrain = null;
    }

    /**
     * Compute the intersections of a specified line with this shape. If the shape's altitude mode is other than {@link
     * WorldWind#ABSOLUTE}, the shape's geometry is created relative to the specified terrain rather than the terrain
     * used during rendering, which may be at lower level of detail than required for accurate intersection
     * determination.
     *
     * @param line    the line to intersect.
     * @param terrain the {@link Terrain} to use when computing the wedge's geometry.
     *
     * @return a list of intersections identifying where the line intersects the shape, or null if the line does not
     *         intersect the shape.
     *
     * @throws InterruptedException if the operation is interrupted.
     * @see Terrain
     */
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        List<Intersection> shapeIntersections = new ArrayList<Intersection>();
        List<Intersection> faceIntersections;

        for (int i = 0; i < getFaceCount(); i++)
        {
            faceIntersections = intersect(line, terrain, i);
            if (faceIntersections != null)
                shapeIntersections.addAll(faceIntersections);
        }
        return shapeIntersections;
    }

    public List<Intersection> intersect(Line line, Terrain terrain, int index) throws InterruptedException
    {
        Position refPos = this.getReferencePosition();
        if (refPos == null)
            return null;

        // check that the geometry exists
        if (this.getCurrentShapeData().getMesh(index).getBuffer(Geometry.VERTEX) == null)
            return null;

        // Reuse the previously computed high-res shape data if the terrain is the same.
        ShapeData highResShapeData = this.isSameAsPreviousTerrain(terrain) ? this.previousIntersectionShapeData
            : null;

        if (highResShapeData == null)
        {
            highResShapeData = this.createIntersectionGeometry(terrain);

            if (highResShapeData.getMesh(index) == null)
                return null;

            this.previousIntersectionShapeData = highResShapeData;
            this.previousIntersectionTerrain = terrain;
            this.previousIntersectionGlobeStateKey = terrain.getGlobe().getGlobeStateKey();
        }

        if (highResShapeData.getExtent() != null && highResShapeData.getExtent().intersect(line) == null)
            return null;

        final Line localLine = new Line(line.getOrigin().subtract3(highResShapeData.getReferencePoint()),
            line.getDirection());

        List<Intersection> shapeIntersections = new ArrayList<Intersection>();

        for (int i = 0; i < getFaceCount(); i++)
        {
            List<Intersection> intersections = new ArrayList<Intersection>();
            this.intersect(localLine, highResShapeData, intersections, index);

            if (intersections.size() == 0)
                continue;

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

            if (intersections.size() > 0)
                shapeIntersections.addAll(intersections);
        }
        return shapeIntersections;
    }

    protected void intersect(Line line, ShapeData shapeData, List<Intersection> intersections, int index)
        throws InterruptedException
    {

        IntBuffer indices = (IntBuffer) shapeData.getMesh(index).getBuffer(Geometry.ELEMENT);
        indices.rewind();
        FloatBuffer vertices = (FloatBuffer) shapeData.getMesh(index).getBuffer(Geometry.VERTEX);
        vertices.rewind();

        List<Intersection> ti = Triangle.intersectTriangleTypes(line, vertices, indices,
            GL.GL_TRIANGLES);

        if (ti != null && ti.size() > 0)
            intersections.addAll(ti);
    }

    abstract protected ShapeData createIntersectionGeometry(Terrain terrain);

    /**
     * Returns intersections of line with the ith face of this shape,  Assumes we already know the line intersects the
     * shape somewhere (but perhaps not on this face)
     *
     * @param line         the line to intersect.
     * @param index        the index of the face to test for intersections
     * @param renderMatrix the current renderMatrix
     *
     * @return a list of intersections identifying where the line intersects this shape face, or null if the line does
     *         not intersect this face.
     *
     * @throws InterruptedException if the operation is interrupted.
     */
    public List<Intersection> intersectFace(Line line, int index, Matrix renderMatrix) throws InterruptedException
    {
        final Line localLine = new Line(line.getOrigin().subtract3(this.getCurrentShapeData().getReferencePoint()),
            line.getDirection());

        Geometry mesh = this.getCurrentShapeData().getMesh(index);
        // transform the vertices from local to world coords
        FloatBuffer vertices = computeTransformedVertices((FloatBuffer) mesh.getBuffer(Geometry.VERTEX),
            mesh.getCount(Geometry.VERTEX), renderMatrix);

        List<Intersection> intersections = Triangle.intersectTriangleTypes(localLine, vertices,
            (IntBuffer) mesh.getBuffer(Geometry.ELEMENT), GL.GL_TRIANGLES);

        if (intersections == null || intersections.size() == 0)
            return null;

        for (Intersection intersection : intersections)
        {
            // translate the intersection to world coordinates
            Vec4 pt = intersection.getIntersectionPoint().add3(this.getCurrentShapeData().getReferencePoint());
            intersection.setIntersectionPoint(pt);

            intersection.setObject(this);
        }

        return intersections;
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
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsPosition(context, "centerPosition", this.getCenterPosition());
        rs.addStateValueAsDouble(context, "northSouthRadius", this.getNorthSouthRadius());
        rs.addStateValueAsDouble(context, "eastWestRadius", this.getEastWestRadius());
        rs.addStateValueAsDouble(context, "verticalRadius", this.getVerticalRadius());
        rs.addStateValueAsDouble(context, "heading", this.getHeading().degrees);
        rs.addStateValueAsDouble(context, "tilt", this.getTilt().degrees);
        rs.addStateValueAsDouble(context, "roll", this.getRoll().degrees);
        rs.addStateValueAsDouble(context, "skewNorthSouth", this.getSkewNorthSouth().degrees);
        rs.addStateValueAsDouble(context, "skewEastWest", this.getSkewEastWest().degrees);
        rs.addStateValueAsOffsetsList(context, "offsets", this.offsets);
        rs.addStateValueAsImageSourceList(context, "imageSources", this.imageSources, getFaceCount());
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
        super.doRestoreState(rs, context);

        Position positionState = rs.getStateValueAsPosition(context, "centerPosition");
        if (positionState != null)
            this.setCenterPosition(positionState);

        Double doubleState = rs.getStateValueAsDouble(context, "northSouthRadius");
        if (doubleState != null)
            this.setNorthSouthRadius(doubleState);

        doubleState = rs.getStateValueAsDouble(context, "eastWestRadius");
        if (doubleState != null)
            this.setEastWestRadius(doubleState);

        doubleState = rs.getStateValueAsDouble(context, "verticalRadius");
        if (doubleState != null)
            this.setVerticalRadius(doubleState);

        doubleState = rs.getStateValueAsDouble(context, "heading");
        if (doubleState != null)
            this.setHeading(Angle.fromDegrees(doubleState));

        doubleState = rs.getStateValueAsDouble(context, "tilt");
        if (doubleState != null)
            this.setTilt(Angle.fromDegrees(doubleState));

        doubleState = rs.getStateValueAsDouble(context, "roll");
        if (doubleState != null)
            this.setRoll(Angle.fromDegrees(doubleState));

        doubleState = rs.getStateValueAsDouble(context, "skewNorthSouth");
        if (doubleState != null)
            this.setSkewNorthSouth(Angle.fromDegrees(doubleState));

        doubleState = rs.getStateValueAsDouble(context, "skewEastWest");
        if (doubleState != null)
            this.setSkewEastWest(Angle.fromDegrees(doubleState));

        HashMap<Integer, OffsetsList> offsetsListState = rs.getStateValueAsOffsetsList(context, "offsets");
        if (offsetsListState != null)
            this.offsets = offsetsListState;

        HashMap<Integer, Object> imageSourceListState = rs.getStateValueAsImageSourceList(context, "imageSources");
        if (imageSourceListState != null)
        {
            for (int i = 0; i < imageSourceListState.size(); i++)
            {
                setImageSource(i, imageSourceListState.get(i));
            }
        }
    }
}
