/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import javax.xml.stream.*;
import java.awt.*;
import java.io.IOException;
import java.nio.*;
import java.util.*;
import java.util.List;

import static gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil.kmlBoolean;

// TODO: Measurement (getLength), Texture, lighting

/**
 * Displays a line or curve between positions. The path is drawn between input positions to achieve a specified path
 * type, e.g., {@link AVKey#GREAT_CIRCLE}. It can also conform to the underlying terrain. A curtain may be formed by
 * extruding the path to the ground.
 * <p/>
 * Altitudes within the path's positions are interpreted according to the path's altitude mode. If the altitude mode is
 * {@link WorldWind#ABSOLUTE}, the altitudes are considered as height above the ellipsoid. If the altitude mode is
 * {@link WorldWind#RELATIVE_TO_GROUND}, the altitudes are added to the elevation of the terrain at the position. If the
 * altitude mode is {@link WorldWind#CLAMP_TO_GROUND} the altitudes are ignored.
 * <p/>
 * Between the specified positions the path is drawn along a curve specified by the path's path type, either {@link
 * AVKey#GREAT_CIRCLE}, {@link AVKey#RHUMB_LINE} or {@link AVKey#LINEAR}. (See {@link #setPathType(String)}.)
 * <p/>
 * Paths have separate attributes for normal display and highlighted display. If no attributes are specified, default
 * attributes are used. See {@link #DEFAULT_INTERIOR_MATERIAL}, {@link #DEFAULT_OUTLINE_MATERIAL}, and {@link
 * #DEFAULT_HIGHLIGHT_MATERIAL}.
 * <p/>
 * When the path type is <code>LINEAR</code> the path conforms to terrain only if the follow-terrain property is true.
 * Otherwise the path control points will be connected by straight line segments.
 * <p/>
 * The terrain conformance of <code>GREAT_CIRCLE</code> or <code>RHUMB_LINE</code> paths is determined by the path's
 * follow-terrain and terrain-conformance properties. When the follow-terrain property is true, terrain conformance
 * adapts as the view moves relative to the path; the terrain-conformance property governs the precision of conformance,
 * and the number of intermediate positions computed varies. See {@link #setFollowTerrain(boolean)} and {@link
 * #setTerrainConformance(double)}. If the follow-terrain property is false, the view position is not considered and the
 * number of intermediate positions between specified positions is the constant value specified by the num-subsegments
 * property (see {@link #setNumSubsegments(int)}). The latter case may produce higher performance than the former.
 * <p/>
 * The path positions may be shown by calling {@link #setShowPositions(boolean)} with an argument of <code>true</code>.
 * This causes dots to be drawn at each originally specified path position. Dots are not drawn at tessellated path
 * positions. The size of the dots may be specified via {@link #setShowPositionsScale(double)}. The dots are drawn only
 * when the Path is within a threshold distance from the eye point. The threshold may be specified by calling {@link
 * #setShowPositionsThreshold(double)}. The dots are drawn in the path's outline material colors by default.
 * <p/>
 * The path's line and the path's position dots may be drawn in unique RGBA colors by configuring the path with a {@link
 * PositionColors} (see {@link #setPositionColors(gov.nasa.worldwind.render.Path.PositionColors)}).
 * <p/>
 * Path picking includes information about which position dots are picked, in addition to the path itself. A position
 * dot under the cursor is returned as an Integer object in the PickedObject's AVList under they key AVKey.ORDINAL.
 * Position dots intersecting the pick rectangle are returned as a List of Integer objects in the PickedObject's AVList
 * under the key AVKey.ORDINAL_LIST.
 * <p/>
 * When drawn on a 2D globe, this shape uses a {@link SurfacePolyline} to represent itself. The following features are
 * not provided in this case: display of path positions, extrusion, outline pick width, and identification of path
 * position picked.
 *
 * @author tag
 * @version $Id: Path.java 3032 2015-04-17 17:53:35Z dcollins $
 */
public class Path extends AbstractShape
{
    /** The default interior color. */
    protected static final Material DEFAULT_INTERIOR_MATERIAL = Material.PINK;
    /** The default outline color. */
    protected static final Material DEFAULT_OUTLINE_MATERIAL = Material.RED;
    /** The default path type. */
    protected static final String DEFAULT_PATH_TYPE = AVKey.LINEAR;
    /**
     * The offset applied to a terrain following Path's depth values to to ensure it shows over the terrain: 0.99.
     * Values less than 1.0 pull the path in front of the terrain, values greater than 1.0 push the path behind the
     * terrain.
     */
    protected static final double SURFACE_PATH_DEPTH_OFFSET = 0.99;
    /** The default number of tessellation points between the specified path positions. */
    protected static final int DEFAULT_NUM_SUBSEGMENTS = 10;
    /** The default terrain conformance target. */
    protected static final double DEFAULT_TERRAIN_CONFORMANCE = 10;
    /** The default distance from the eye beyond which positions dots are not drawn. */
    protected static final double DEFAULT_DRAW_POSITIONS_THRESHOLD = 1e6;
    /** The default scale for position dots. The scale is applied to the current outline width to produce the dot size. */
    protected static final double DEFAULT_DRAW_POSITIONS_SCALE = 10;

    /** The PositionColors interface defines an RGBA color for each of a path's original positions. */
    public static interface PositionColors
    {
        /**
         * Returns an RGBA color corresponding to the specified position and ordinal. This returns <code>null</code> if
         * a color cannot be determined for the specified position and ordinal. The specified <code>position</code> is
         * guaranteed to be one of the same Position references passed to a path at construction or in a call to {@link
         * Path#setPositions(Iterable)}.
         * <p/>
         * The specified <code>ordinal</code> denotes the position's ordinal number as it appears in the position list
         * passed to the path. Ordinal numbers start with 0 and increase by 1 for every originally specified position.
         * For example, the first three path positions have ordinal values 0, 1, 2.
         * <p/>
         * The returned color's RGB components must <em>not</em> be premultiplied by its Alpha component.
         *
         * @param position the path position the color corresponds to.
         * @param ordinal  the ordinal number of the specified position.
         *
         * @return an RGBA color corresponding to the position and ordinal, or <code>null</code> if a color cannot be
         * determined.
         */
        Color getColor(Position position, int ordinal);
    }

    /**
     * Maintains globe-dependent computed data such as Cartesian vertices and extents. One entry exists for each
     * distinct globe that this shape encounters in calls to {@link AbstractShape#render(DrawContext)}. See {@link
     * AbstractShape}.
     */
    protected static class PathData extends AbstractShapeData
    {
        /** The positions formed from applying path type and terrain conformance. */
        protected ArrayList<Position> tessellatedPositions;
        /**
         * The colors corresponding to each tessellated position, or <code>null</code> if the path's
         * <code>positionColors</code> is <code>null</code>.
         */
        protected ArrayList<Color> tessellatedColors;
        /**
         * The model coordinate vertices to render, all relative to this shape data's reference center. If the path is
         * extruded, the base vertices are interleaved: Vcap, Vbase, Vcap, Vbase, ...
         */
        protected FloatBuffer renderedPath;
        /**
         * Indices to the <code>renderedPath</code> identifying the vertices of the originally specified boundary
         * positions and their corresponding terrain point. This is used to draw vertical lines at those positions when
         * the path is extruded.
         */
        protected IntBuffer polePositions; // identifies original positions and corresponding ground points
        /**
         * Indices to the <code>renderedPath</code> identifying the vertices of the originally specified boundary
         * positions. (Not their terrain points as well, as <code>polePositions</code> does.)
         */
        protected IntBuffer positionPoints; // identifies the original positions in the rendered path.
        /**
         * Indices of tessellated path lines when using 2D globe and the path crosses the dateline.
         */
        protected IntBuffer path2DIndices;
        /**
         * Indices of the tessellated positions at which new lines must be formed rather than continuing the previous
         * line. Used only when the path's positions span the dateline and a 2D globe is being used.
         */
        protected ArrayList<Integer> splitPositions;
        /** Indicates whether the rendered path has extrusion points in addition to path points. */
        protected boolean hasExtrusionPoints; // true when the rendered path contains extrusion points
        /**
         * Indicates the offset in number of floats to the first RGBA color tuple in <code>renderedPath</code>. This is
         * <code>0</code> if <code>renderedPath</code> has no RGBA color tuples.
         */
        protected int colorOffset;
        /**
         * Indicates the stride in number of floats between the first element of consecutive vertices in
         * <code>renderedPath</code>.
         */
        protected int vertexStride;
        /** Indicates the number of vertices represented by <code>renderedPath</code>. */
        protected int vertexCount;

        public PathData(DrawContext dc, Path shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);
        }

        /**
         * The positions resulting from tessellating this path. If the path's attributes don't cause tessellation, then
         * the positions returned are those originally specified.
         *
         * @return the positions computed by path tessellation.
         */
        public List<Position> getTessellatedPositions()
        {
            return this.tessellatedPositions;
        }

        public void setTessellatedPositions(ArrayList<Position> tessellatedPositions)
        {
            this.tessellatedPositions = tessellatedPositions;
        }

        /**
         * Indicates the colors corresponding to each position in <code>tessellatedPositions</code>, or
         * <code>null</code> if the path does not have per-position colors.
         *
         * @return the colors corresponding to each path position, or <code>null</code> if the path does not have
         * per-position colors.
         */
        public List<Color> getTessellatedColors()
        {
            return this.tessellatedColors;
        }

        /**
         * Specifies the colors corresponding to each position in <code>tessellatedPositions</code>, or
         * <code>null</code> to specify that the path does not have per-position colors. The entries in the specified
         * list must have a one-to-one correspondence with the entries in <code>tessellatedPositions</code>.
         *
         * @param tessellatedColors the colors corresponding to each path position, or <code>null</code> if the path
         *                          does not have per-position colors.
         */
        public void setTessellatedColors(ArrayList<Color> tessellatedColors)
        {
            this.tessellatedColors = tessellatedColors;
        }

        /**
         * The Cartesian coordinates of the tessellated positions. If path verticals are enabled, this path also
         * contains the ground points corresponding to the path positions.
         *
         * @return the Cartesian coordinates of the tessellated positions.
         */
        public FloatBuffer getRenderedPath()
        {
            return this.renderedPath;
        }

        public void setRenderedPath(FloatBuffer renderedPath)
        {
            this.renderedPath = renderedPath;
        }

        /**
         * Returns a buffer of indices into the rendered path ({@link #renderedPath} that identify the originally
         * specified positions that remain after tessellation. These positions are those of the position dots, if
         * drawn.
         *
         * @return the path's originally specified positions that survived tessellation.
         */
        public IntBuffer getPositionPoints()
        {
            return this.positionPoints;
        }

        public void setPositionPoints(IntBuffer posPoints)
        {
            this.positionPoints = posPoints;
        }

        /**
         * Returns a buffer of indices into the rendered path ({@link #renderedPath} that identify the top and bottom
         * vertices of this path's vertical line segments.
         *
         * @return the path's pole positions.
         */
        public IntBuffer getPolePositions()
        {
            return this.polePositions;
        }

        public void setPolePositions(IntBuffer polePositions)
        {
            this.polePositions = polePositions;
        }

        /**
         * Indicates whether this path is extruded and the extrusion points have been computed.
         *
         * @return true if the path is extruded and the extrusion points are computed, otherwise false.
         */
        public boolean isHasExtrusionPoints()
        {
            return this.hasExtrusionPoints;
        }

        public void setHasExtrusionPoints(boolean hasExtrusionPoints)
        {
            this.hasExtrusionPoints = hasExtrusionPoints;
        }

        /**
         * Indicates the offset in number of floats to the first RGBA color tuple in <code>renderedPath</code>. This
         * returns <code>0</code> if <code>renderedPath</code> has no RGBA color tuples.
         *
         * @return the offset in number of floats to the first RGBA color tuple in <code>renderedPath</code>.
         */
        public int getColorOffset()
        {
            return this.colorOffset;
        }

        /**
         * Specifies the offset in number of floats to the first RGBA color tuple in <code>renderedPath</code>. Specify
         * 0 if <code>renderedPath</code> has no RGBA color tuples.
         *
         * @param offset the offset in number of floats to the first RGBA color tuple in <code>renderedPath</code>.
         */
        public void setColorOffset(int offset)
        {
            this.colorOffset = offset;
        }

        /**
         * Indicates the stride in number of floats between the first element of consecutive vertices in
         * <code>renderedPath</code>.
         *
         * @return the stride in number of floats between vertices in in <code>renderedPath</code>.
         */
        public int getVertexStride()
        {
            return this.vertexStride;
        }

        /**
         * Specifies the stride in number of floats between the first element of consecutive vertices in
         * <code>renderedPath</code>.
         *
         * @param stride the stride in number of floats between vertices in in <code>renderedPath</code>.
         */
        public void setVertexStride(int stride)
        {
            this.vertexStride = stride;
        }

        /**
         * Indicates the number of vertices in <code>renderedPath</code>.
         *
         * @return the the number of verices in <code>renderedPath</code>.
         */
        public int getVertexCount()
        {
            return this.vertexCount;
        }

        /**
         * Specifies the number of vertices in <code>renderedPath</code>. Specify 0 if <code>renderedPath</code>
         * contains no vertices.
         *
         * @param count the the number of vertices in <code>renderedPath</code>.
         */
        public void setVertexCount(int count)
        {
            this.vertexCount = count;
        }
    }

    @Override
    protected SurfaceShape createSurfaceShape()
    {
        SurfacePolyline polyline = new SurfacePolyline();
        if (this.getPositions() != null)
            polyline.setLocations(this.getPositions());

        return polyline;
    }

    @Override
    protected void updateSurfaceShape()
    {
        super.updateSurfaceShape();

        this.surfaceShape.setPathType(this.getPathType());
    }

    /**
     * PickablePositions associates a range of pick color codes with a Path. The color codes represent the range of pick
     * colors that the Path's position points are drawn in. The color codes represent ARGB colors packed into a 32-bit
     * integer.
     */
    protected static class PickablePositions
    {
        // TODO: Replace this class with usage of PickSupport.addPickableObjectRange.

        /** The minimum color code, inclusive. */
        public final int minColorCode;
        /** The maximum color code, inclusive. */
        public final int maxColorCode;
        /** The Path who's position points are associated with the specified color code range. */
        public final Path path;

        /**
         * Creates a new PickablePositions with the specified color code range and Path. See the PickablePositions
         * class-level documentation for more information.
         *
         * @param minColorCode the minimum color code, inclusive.
         * @param maxColorCode the maximum color code, inclusive.
         * @param path         the Path who's position points are associated with the specified color code range.
         */
        public PickablePositions(int minColorCode, int maxColorCode, Path path)
        {
            this.minColorCode = minColorCode;
            this.maxColorCode = maxColorCode;
            this.path = path;
        }
    }

    /**
     * Subclass of PickSupport that adds the capability to resolve a Path's picked position point. Path position points
     * are registered with PathPickSupport by calling {@link #addPickablePositions(int, int, Path)} with the minimum and
     * maximum color codes that the Path's position points are drawn in.
     * <p/>
     * The resolution of the picked position point is integrated with the resolution of the picked Path. Either an
     * entire Path or one of its position points may be picked. In either case, resolvePick and getTopObject return a
     * PickedObject that specifies the picked Path. If a position point is picked, the PickedObject's AVList contains
     * the position and ordinal number of the picked position.
     */
    protected static class PathPickSupport extends PickSupport
    {
        // TODO: Replace this subclass with usage of PickSupport.addPickableObjectRange.
        // TODO: Take care to retain the behavior in doResolvePick below that merges multiple picks from a single path.

        /**
         * The list of Path pickable positions that this PathPickSupport is currently tracking. This list maps a range
         * of color codes to a Path, where the color codes represent the range of pick colors that the Path's position
         * points are drawn in.
         */
        protected List<PickablePositions> pickablePositions = new ArrayList<PickablePositions>();
        /**
         * A map that associates each path with a picked object. Used to during box picking to consolidate the
         * information about what parts of each path are picked into a single picked object. Path's positions are drawn
         * in unique colors and are therefore separately pickable during box picking.
         */
        protected Map<Object, PickedObject> pathPickedObjects = new HashMap<Object, PickedObject>();

        /**
         * {@inheritDoc}
         * <p/>
         * Overridden to clear the list of pickable positions.
         */
        @Override
        public void clearPickList()
        {
            super.clearPickList();
            this.pickablePositions.clear();
        }

        /**
         * Indicates the list of Path pickable positions that this PathPickSupport is currently tracking. This list maps
         * a range of color codes to a Path, where the color codes represent the range of pick colors that the Path's
         * position points are drawn in. The returned list is empty if addPickablePositions has not been called since
         * the last call to clearPickList.
         *
         * @return the list of Path pickable positions.
         */
        public List<PickablePositions> getPickablePositions()
        {
            return this.pickablePositions;
        }

        /**
         * Registers a range of unique pick color codes with a Path, representing the range of pick colors that the
         * Path's position points are drawn in. The color codes represent ARGB colors packed into a 32-bit integer.
         *
         * @param minColorCode the minimum color code, inclusive.
         * @param maxColorCode the maximum color code, inclusive.
         * @param path         the Path who's position points are associated with the specified color code range.
         *
         * @throws IllegalArgumentException if the path is null.
         */
        public void addPickablePositions(int minColorCode, int maxColorCode, Path path)
        {
            if (path == null)
            {
                String message = Logging.getMessage("nullValue.PathIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.pickablePositions.add(new PickablePositions(minColorCode, maxColorCode, path));

            // Incorporate the Path position's min and max color codes into this PickSupport's minimum and maximum color
            // codes.
            this.adjustExtremeColorCodes(minColorCode);
            this.adjustExtremeColorCodes(maxColorCode);
        }

        /**
         * Computes and returns the top object at the specified pick point. This either resolves a pick of an entire
         * Path or one of its position points. In either case, this returns a PickedObject that specifies the picked
         * Path. If a position point is picked, the PickedObject's AVList contains the picked position's geographic
         * position in the key AVKey.POSITION and its ordinal number in the key AVKey.ORDINAL.
         * <p/>
         * This returns null if the pickPoint is null, or if there is no Path or Path position point at the specified
         * pick point.
         *
         * @param dc        the current draw context.
         * @param pickPoint the screen-coordinate point in question.
         *
         * @return a new picked object instances indicating the Path or Path position point at the specified pick point,
         * or null if no Path is at the specified pick point.
         *
         * @throws IllegalArgumentException if the draw context is null.
         */
        @Override
        public PickedObject getTopObject(DrawContext dc, Point pickPoint)
        {
            if (dc == null)
            {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (this.getPickableObjects().isEmpty() && this.getPickablePositions().isEmpty())
                return null;

            int colorCode = this.getTopColor(dc, pickPoint);
            if (colorCode == dc.getClearColor().getRGB())
                return null;

            PickedObject pickedObject = this.getPickableObjects().get(colorCode);
            if (pickedObject != null)
                return pickedObject;

            for (PickablePositions positions : this.getPickablePositions())
            {
                if (colorCode >= positions.minColorCode && colorCode <= positions.maxColorCode)
                {
                    // If the top color code matches a Path's position color, convert the color code to a position index
                    // and delegate to the Path to resolve the index to a PickedObject. minColorCode corresponds to
                    // index 0, and minColorCode+i corresponds to index i.
                    int ordinal = colorCode - positions.minColorCode;
                    return positions.path.resolvePickedPosition(colorCode, ordinal);
                }
            }

            return null;
        }

        /**
         * Adds all picked paths that are registered with this PickSupport and intersect the specified rectangle in AWT
         * screen coordinates (if any) to the draw context's list of picked objects. Each picked object includes the
         * picked path and the ordinal numbers of positions that intersect the specified rectangle, if any. If any
         * positions intersect the rectangle, the picked object's AVList contains the ordinal numbers in the key
         * AVKey.ORDINAL_LIST.
         *
         * @param dc       the draw context which receives the picked objects.
         * @param pickRect the rectangle in AWT screen coordinates.
         * @param layer    the layer associated with the picked objects.
         */
        @SuppressWarnings({"unchecked"})
        @Override
        protected void doResolvePick(DrawContext dc, Rectangle pickRect, Layer layer)
        {
            if (this.pickableObjects.isEmpty() && this.pickablePositions.isEmpty())
            {
                // There's nothing to do if both the pickable objects and pickable positions are empty.
                return;
            }
            else if (this.pickablePositions.isEmpty())
            {
                // Fall back to the superclass version of this method if we have pickable objects but no pickable
                // positions. This avoids the additional overhead of consolidating multiple objects picked from the same
                // path.
                super.doResolvePick(dc, pickRect, layer);
                return;
            }

            // Get the unique pick colors in the specified screen rectangle. Use the minimum and maximum color codes to
            // cull the number of colors that the draw context must consider with identifying the unique pick colors in
            // the specified rectangle.
            int[] colorCodes = dc.getPickColorsInRectangle(pickRect, this.minAndMaxColorCodes);
            if (colorCodes == null || colorCodes.length == 0)
                return;

            // Lookup the pickable object (if any) for each unique color code appearing in the pick rectangle. Each
            // picked object that corresponds to a picked color is added to the draw context. Since the
            for (int colorCode : colorCodes)
            {
                if (colorCode == 0) // This should never happen, but we check anyway.
                    continue;

                PickedObject po = this.pickableObjects.get(colorCode);
                if (po != null)
                {
                    // The color code corresponds to a path's line, so we add the path and its picked object to the map
                    // of picked objects if one doesn't already exist. If one already exists, then this picked object
                    // provides no additional information and we just ignore it. Note that if multiple parts of a path
                    // are picked, we use the pick color of the first part we encounter.
                    if (!this.pathPickedObjects.containsKey(po.getObject()))
                        this.pathPickedObjects.put(po.getObject(), po);
                }
                else
                {
                    for (PickablePositions positions : this.getPickablePositions())
                    {
                        if (colorCode >= positions.minColorCode && colorCode <= positions.maxColorCode)
                        {
                            Path path = positions.path;

                            // The color code corresponds to a path's position, so we incorporate that position's
                            // ordinal into the picked object. Note that if multiple parts of a path are picked, we use
                            // the pick color of the first part we encounter.
                            po = this.pathPickedObjects.get(path);
                            if (po == null)
                                this.pathPickedObjects.put(path, po = path.createPickedObject(colorCode));

                            // Convert the color code to a position index and delegate to the Path to resolve the
                            // ordinal. minColorCode corresponds to position index 0, and minColorCode+i corresponds to
                            // position index i.
                            int ordinal = path.getOrdinal(colorCode - positions.minColorCode);

                            // Add the ordinal to the list of picked ordinals on the path's picked object.
                            List ordinalList = (List) po.getValue(AVKey.ORDINAL_LIST);
                            if (ordinalList == null)
                                po.setValue(AVKey.ORDINAL_LIST, ordinalList = new ArrayList<Integer>());
                            ordinalList.add(ordinal);

                            break; // No need to check the remaining paths.
                        }
                    }
                }
            }

            // We've consolidated the information about what parts of each path are picked into a map of picked objects.
            // The values in this map contain all the information we need, so we just add them to the draw context.
            for (PickedObject po : this.pathPickedObjects.values())
            {
                if (layer != null)
                    po.setParentLayer(layer);

                dc.addObjectInPickRectangle(po);
            }

            // Clear the map of path's to corresponding picked objects to ensure that the picked objects from this call
            // do not interfere with the next call.
            this.pathPickedObjects.clear();
        }
    }

    @Override
    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new PathData(dc, this);
    }

    protected PathData getCurrentPathData()
    {
        return (PathData) this.getCurrentData();
    }

    protected Iterable<? extends Position> positions; // the positions as provided by the application
    protected int numPositions; // the number of positions in the positions field.
    protected PositionColors positionColors; // defines a color at each application-provided position.
    protected static ByteBuffer pickPositionColors; // defines the colors used to resolve position point picking.

    protected String pathType = DEFAULT_PATH_TYPE;
    protected boolean followTerrain; // true if altitude mode indicates terrain following
    protected boolean extrude;
    protected double terrainConformance = DEFAULT_TERRAIN_CONFORMANCE;
    protected int numSubsegments = DEFAULT_NUM_SUBSEGMENTS;
    protected boolean drawVerticals = true;
    protected boolean showPositions = false;
    protected double showPositionsThreshold = DEFAULT_DRAW_POSITIONS_THRESHOLD;
    protected double showPositionsScale = DEFAULT_DRAW_POSITIONS_SCALE;
    protected boolean positionsSpanDateline;

    /** Creates a path with no positions. */
    public Path()
    {
    }

    public Path(Path source)
    {
        super(source);

        List<Position> copiedPositions = new ArrayList<Position>();
        for (Position position : source.positions)
        {
            copiedPositions.add(position);
        }
        this.setPositions(copiedPositions);
        this.positionColors = source.positionColors;
        this.pathType = source.pathType;
        this.followTerrain = source.followTerrain;
        this.extrude = source.extrude;
        this.terrainConformance = source.terrainConformance;
        this.numSubsegments = source.numSubsegments;
        this.drawVerticals = source.drawVerticals;
        this.showPositions = source.showPositions;
        this.showPositionsThreshold = source.showPositionsThreshold;
        this.showPositionsScale = source.showPositionsScale;
    }

    /**
     * Creates a path with specified positions.
     * <p/>
     * Note: If fewer than two positions is specified, no path is drawn.
     *
     * @param positions the path positions. This reference is retained by this shape; the positions are not copied. If
     *                  any positions in the set change, {@link #setPositions(Iterable)} must be called to inform this
     *                  shape of the change.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public Path(Iterable<? extends Position> positions)
    {
        this.setPositions(positions);
    }

    /**
     * Creates a path with positions specified via a generic list.
     * <p/>
     * Note: If fewer than two positions is specified, the path is not drawn.
     *
     * @param positions the path positions. This reference is retained by this shape; the positions are not copied. If
     *                  any positions in the set change, {@link #setPositions(Iterable)} must be called to inform this
     *                  shape of the change.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public Path(Position.PositionList positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setPositions(positions.list);
    }

    /**
     * Creates a path between two positions.
     *
     * @param posA the first position.
     * @param posB the second position.
     *
     * @throws IllegalArgumentException if either position is null.
     */
    public Path(Position posA, Position posB)
    {
        if (posA == null || posB == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<Position> endPoints = new ArrayList<Position>(2);
        endPoints.add(posA);
        endPoints.add(posB);
        this.setPositions(endPoints);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to assign this Path's pickSupport property to a new PathPickSupport instance.
     */
    @Override
    protected void initialize()
    {
        this.pickSupport = new PathPickSupport();
    }

    @Override
    protected void reset()
    {
        for (ShapeDataCache.ShapeDataCacheEntry entry : this.shapeDataCache)
        {
            ((PathData) entry).tessellatedPositions = null;
            ((PathData) entry).tessellatedColors = null;
        }

        super.reset();
    }

    /**
     * Returns this path's positions.
     *
     * @return this path's positions. Will be null if no positions have been specified.
     */
    public Iterable<? extends Position> getPositions()
    {
        return this.positions;
    }

    /**
     * Specifies this path's positions, which replace this path's current positions, if any.
     * <p/>
     * Note: If fewer than two positions is specified, this path is not drawn.
     *
     * @param positions this path's positions.
     *
     * @throws IllegalArgumentException if positions is null.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.positions = positions;
        this.computePositionCount();
        this.positionsSpanDateline = LatLon.locationsCrossDateLine(this.positions);

        this.reset();
    }

    /**
     * Indicates the PositionColors that defines the RGBA color for each of this path's positions. A return value of
     * <code>null</code> is valid and indicates that this path's positions are colored according to its
     * ShapeAttributes.
     *
     * @return this Path's PositionColors, or <code>null</code> if this path is colored according to its
     * ShapeAttributes.
     *
     * @see #setPositionColors(gov.nasa.worldwind.render.Path.PositionColors)
     */
    public PositionColors getPositionColors()
    {
        return this.positionColors;
    }

    /**
     * Specifies the position colors used to define an RGBA color for each of this path's positions. When
     * non-<code>null</code>, the specified <code>positionColors</code> is called during rendering to define a color at
     * each of this path's positions specified during construction or in a call to {@link #setPositions(Iterable)}. The
     * returned colors are applied to this path's line and the optional dots drawn at each position when
     * <code>showPositions</code> is true, and override the ShapeAttributes' outline color and outline opacity for both
     * normal display and highlighted display. The specified <code>positionColors</code> do not affect this path's
     * filled interior or vertical drop lines displayed when this path is extruded.
     * <p/>
     * If this path is configured to tessellate itself by creating additional positions between the originally specified
     * positions, an interpolated color is assigned to each tessellated position by computing a weighted linear
     * combination of the colors at the originally specified positions.
     * <p/>
     * Specify <code>null</code> to disable position colors and draw this path's line and optional position dots
     * according to its ShapeAttributes. This path's position colors reference is <code>null</code> by default.
     *
     * @param positionColors the PositionColors that defines an RGBA color for each of this path's positions, or
     *                       <code>null</code> to color this path's positions according to its ShapeAttributes.
     *
     * @see #getPositionColors()
     * @see PositionColors
     */
    public void setPositionColors(PositionColors positionColors)
    {
        this.positionColors = positionColors;
        this.reset();
    }

    /**
     * Indicates whether to extrude this path. Extruding the path extends a filled interior from the path to the
     * terrain.
     *
     * @return true to extrude this path, otherwise false.
     *
     * @see #setExtrude(boolean)
     */
    public boolean isExtrude()
    {
        return extrude;
    }

    /**
     * Specifies whether to extrude this path. Extruding the path extends a filled interior from the path to the
     * terrain.
     *
     * @param extrude true to extrude this path, otherwise false. The default value is false.
     */
    public void setExtrude(boolean extrude)
    {
        this.extrude = extrude;
        this.reset();
    }

    /**
     * Indicates whether this path is terrain following.
     *
     * @return true if terrain following, otherwise false.
     *
     * @see #setFollowTerrain(boolean)
     */
    public boolean isFollowTerrain()
    {
        return this.followTerrain;
    }

    /**
     * Specifies whether this path is terrain following.
     *
     * @param followTerrain true if terrain following, otherwise false. The default value is false.
     */
    public void setFollowTerrain(boolean followTerrain)
    {
        if (this.followTerrain == followTerrain)
            return;

        this.followTerrain = followTerrain;
        this.reset();
    }

    /**
     * Indicates the number of segments used between specified positions to achieve this path's path type. Higher values
     * cause the path to conform more closely to the path type but decrease performance.
     * <p/>
     * Note: The sub-segments number is ignored when the path follows terrain or when the path type is {@link
     * AVKey#LINEAR}.
     *
     * @return the number of sub-segments.
     *
     * @see #setNumSubsegments(int)
     */
    public int getNumSubsegments()
    {
        return numSubsegments;
    }

    /**
     * Specifies the number of segments used between specified positions to achieve this path's path type. Higher values
     * cause the path to conform more closely to the path type but decrease performance.
     * <p/>
     * Note: The sub-segments number is ignored when the path follows terrain or when the path type is {@link
     * AVKey#LINEAR}.
     *
     * @param numSubsegments the number of sub-segments. The default is 10.
     */
    public void setNumSubsegments(int numSubsegments)
    {
        this.numSubsegments = numSubsegments;
        this.reset();
    }

    /**
     * Indicates the terrain conformance target when this path follows the terrain. The value indicates the maximum
     * number of pixels between which intermediate positions of a path segment -- the path portion between two specified
     * positions -- are computed.
     *
     * @return the terrain conformance, in pixels.
     *
     * @see #setTerrainConformance(double)
     */
    public double getTerrainConformance()
    {
        return terrainConformance;
    }

    /**
     * Specifies how accurately this path must adhere to the terrain when the path is terrain following. The value
     * specifies the maximum number of pixels between tessellation points. Lower values increase accuracy but decrease
     * performance.
     *
     * @param terrainConformance the number of pixels between tessellation points.
     */
    public void setTerrainConformance(double terrainConformance)
    {
        this.terrainConformance = terrainConformance;
        this.reset();
    }

    /**
     * Indicates this paths path type.
     *
     * @return the path type.
     *
     * @see #setPathType(String)
     */
    public String getPathType()
    {
        return pathType;
    }

    /**
     * Specifies this path's path type. Recognized values are {@link AVKey#GREAT_CIRCLE}, {@link AVKey#RHUMB_LINE} and
     * {@link AVKey#LINEAR}.
     *
     * @param pathType the current path type. The default value is {@link AVKey#LINEAR}.
     *
     * @see <a href="{@docRoot}/overview-summary.html#path-types">Path Types</a>
     */
    public void setPathType(String pathType)
    {
        this.pathType = pathType;
        this.reset();
    }

    /**
     * Indicates whether to draw at each specified path position when this path is extruded.
     *
     * @return true to draw the lines, otherwise false.
     *
     * @see #setDrawVerticals(boolean)
     */
    public boolean isDrawVerticals()
    {
        return drawVerticals;
    }

    /**
     * Specifies whether to draw vertical lines at each specified path position when this path is extruded.
     *
     * @param drawVerticals true to draw the lines, otherwise false. The default value is true.
     */
    public void setDrawVerticals(boolean drawVerticals)
    {
        this.drawVerticals = drawVerticals;
        this.reset();
    }

    /**
     * Indicates whether dots are drawn at the Path's original positions.
     *
     * @return true if dots are drawn, otherwise false.
     */
    public boolean isShowPositions()
    {
        return showPositions;
    }

    /**
     * Specifies whether to draw dots at the original positions of the Path. The dot color and size are controlled by
     * the Path's outline material and scale attributes.
     *
     * @param showPositions true if dots are drawn at each original (not tessellated) position, otherwise false.
     */
    public void setShowPositions(boolean showPositions)
    {
        this.showPositions = showPositions;
    }

    /**
     * Indicates the scale factor controlling the size of dots drawn at this path's specified positions. The scale is
     * multiplied by the outline width given in this path's {@link ShapeAttributes} to determine the actual size of the
     * dots, in pixels. See {@link ShapeAttributes#setOutlineWidth(double)}.
     *
     * @return the shape's draw-position scale. The default scale is 10.
     */
    public double getShowPositionsScale()
    {
        return showPositionsScale;
    }

    /**
     * Specifies the scale factor controlling the size of dots drawn at this path's specified positions. The scale is
     * multiplied by the outline width given in this path's {@link ShapeAttributes} to determine the actual size of the
     * dots, in pixels. See {@link ShapeAttributes#setOutlineWidth(double)}.
     *
     * @param showPositionsScale the new draw-position scale.
     */
    public void setShowPositionsScale(double showPositionsScale)
    {
        this.showPositionsScale = showPositionsScale;
    }

    /**
     * Indicates the eye distance from this shape's center beyond which position dots are not drawn.
     *
     * @return the eye distance at which to enable or disable position dot drawing. The default is 1e6 meters, which
     * typically causes the dots to always be drawn.
     */
    public double getShowPositionsThreshold()
    {
        return showPositionsThreshold;
    }

    /**
     * Specifies the eye distance from this shape's center beyond which position dots are not drawn.
     *
     * @param showPositionsThreshold the eye distance at which to enable or disable position dot drawing.
     */
    public void setShowPositionsThreshold(double showPositionsThreshold)
    {
        this.showPositionsThreshold = showPositionsThreshold;
    }

    public Sector getSector()
    {
        if (this.sector == null && this.positions != null)
            this.sector = Sector.boundingSector(this.positions);

        return this.sector;
    }

    @Override
    protected boolean mustDrawInterior()
    {
        return super.mustDrawInterior() && this.getCurrentPathData().hasExtrusionPoints;
    }

    @Override
    protected boolean mustApplyLighting(DrawContext dc, ShapeAttributes activeAttrs)
    {
        return false; // TODO: Lighting; need to compute normals
    }

    @Override
    protected boolean mustApplyTexture(DrawContext dc)
    {
        return false;
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        if (this.getCurrentPathData() == null || this.getCurrentPathData().renderedPath == null)
            return true;

        if (this.getCurrentPathData().tessellatedPositions == null)
            return true;

        if (dc.getVerticalExaggeration() != this.getCurrentPathData().getVerticalExaggeration())
            return true;

        //noinspection SimplifiableIfStatement
//        if (this.getAltitudeMode() == WorldWind.ABSOLUTE
//            && this.getCurrentPathData().getGlobeStateKey() != null
//            && this.getCurrentPathData().getGlobeStateKey().equals(dc.getGlobe().getGlobeStateKey(dc)))
//            return false;

        return super.mustRegenerateGeometry(dc);
    }

    protected boolean shouldUseVBOs(DrawContext dc)
    {
        return this.getCurrentPathData().tessellatedPositions.size() > VBO_THRESHOLD && super.shouldUseVBOs(dc);
    }

    /**
     * Indicates whether this Path's defining positions and the positions in between are located on the underlying
     * terrain. This returns <code>true</code> if this Path's altitude mode is <code>WorldWind.CLAMP_TO_GROUND</code>
     * and the follow-terrain property is <code>true</code>. Otherwise this returns <code>false</code>.
     *
     * @return <code>true</code> if this Path's positions and the positions in between are located on the underlying
     * terrain, and <code>false</code> otherwise.
     */
    protected boolean isSurfacePath(DrawContext dc)
    {
        return (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND && this.isFollowTerrain()) || dc.is2DGlobe();
    }

    @Override
    protected void determineActiveAttributes()
    {
        // When the interior is drawn the vertex buffer has a different layout, so it may need to be rebuilt.
        boolean isDrawInterior = this.activeAttributes != null && this.activeAttributes.isDrawInterior();

        super.determineActiveAttributes();

        if (this.activeAttributes != null && this.activeAttributes.isDrawInterior() != isDrawInterior)
            this.getCurrentData().setExpired(true);
    }

    /** Counts the number of positions in this path's specified positions. */
    protected void computePositionCount()
    {
        this.numPositions = 0;

        if (this.positions != null)
        {
            //noinspection UnusedDeclaration
            for (Position pos : this.positions)
            {
                ++this.numPositions;
            }
        }
    }

    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        // currentData must be set prior to calling this method
        PathData pathData = this.getCurrentPathData();

        pathData.setReferencePoint(this.computeReferenceCenter(dc));
        if (pathData.getReferencePoint() == null)
            return false;

        // Recompute tessellated positions because the geometry or view may have changed.
        this.makeTessellatedPositions(dc, pathData);
        if (pathData.tessellatedPositions == null || pathData.tessellatedPositions.size() < 2)
            return false;

        // Create the rendered Cartesian points.
        int previousSize = pathData.renderedPath != null ? pathData.renderedPath.limit() : 0;
        this.computePath(dc, pathData.tessellatedPositions, pathData);
        if (pathData.renderedPath == null || pathData.renderedPath.limit() < 6)
            return false;

        if (pathData.renderedPath.limit() > previousSize && this.shouldUseVBOs(dc))
            this.clearCachedVbos(dc);

        pathData.setExtent(this.computeExtent(pathData));

        // If the shape is less that a pixel in size, don't render it.
        if (this.getExtent() == null || dc.isSmall(this.getExtent(), 1))
            return false;

        if (!this.intersectsFrustum(dc))
            return false;

        pathData.setEyeDistance(this.computeEyeDistance(dc, pathData));
        pathData.setGlobeStateKey(dc.getGlobe().getGlobeStateKey(dc));
        pathData.setVerticalExaggeration(dc.getVerticalExaggeration());

        return true;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to add this Path's pickable positions to the pick candidates.
     */
    @Override
    protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates)
    {
        if (dc.isPickingMode())
        {
            // Add the pickable objects used to resolve picks against individual position points. This must be done
            // before we call super.doDrawOrderedRenderable in order to populate the pickPositionColors buffer before
            // outline rendering.
            this.addPickablePositions(dc, pickCandidates);
        }

        super.doDrawOrderedRenderable(dc, pickCandidates);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to place this Path behind other ordered renderables when this Path is entirely located on the
     * underlying terrain. In this case this Path must be drawn first to ensure that other ordered renderables are
     * correctly drawn on top of it and are not affected by this Path's depth offset. If two paths are both located on
     * the terrain, they are drawn with respect to their layer ordering.
     */
    @Override
    protected void addOrderedRenderable(DrawContext dc)
    {
        if (this.isSurfacePath(dc))
        {
            dc.addOrderedRenderable(this, true); // Specify that this Path is behind other renderables.
        }
        else
        {
            super.addOrderedRenderable(dc);
        }
    }

    @Override
    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        return this.getCurrentPathData().renderedPath != null && this.getCurrentPathData().vertexCount >= 2;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If this Path is entirely located on the terrain, this applies an offset to the Path's depth values to to ensure
     * it shows over the terrain. This does not apply a depth offset in any other case to avoid incorrectly drawing the
     * path over objects it should be behind, including the terrain. In addition to applying a depth offset, this
     * disables writing to the depth buffer to avoid causing subsequently drawn ordered renderables to incorrectly fail
     * the depth test. Since this Path is located on the terrain, the terrain already provides the necessary depth
     * values and we can be certain that other ordered renderables should appear on top of it.
     */
    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        boolean projectionOffsetPushed = false; // keep track for error recovery

        try
        {
            if (this.isSurfacePath(dc))
            {
                // Pull the line forward just a bit to ensure it shows over the terrain.
                dc.pushProjectionOffest(SURFACE_PATH_DEPTH_OFFSET);
                dc.getGL().glDepthMask(false);
                projectionOffsetPushed = true;
            }

            if (this.shouldUseVBOs(dc))
            {
                int[] vboIds = this.getVboIds(dc);
                if (vboIds != null)
                    this.doDrawOutlineVBO(dc, vboIds, this.getCurrentPathData());
                else
                    this.doDrawOutlineVA(dc, this.getCurrentPathData());
            }
            else
            {
                this.doDrawOutlineVA(dc, this.getCurrentPathData());
            }
        }
        finally
        {
            if (projectionOffsetPushed)
            {
                dc.popProjectionOffest();
                dc.getGL().glDepthMask(true);
            }
        }
    }

    protected void doDrawOutlineVBO(DrawContext dc, int[] vboIds, PathData pathData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        try
        {
            int stride = pathData.hasExtrusionPoints ? 2 * pathData.vertexStride : pathData.vertexStride;
            int count = pathData.hasExtrusionPoints ? pathData.vertexCount / 2 : pathData.vertexCount;
            boolean useVertexColors = !dc.isPickingMode() && pathData.tessellatedColors != null;

            // Convert stride from number of elements to number of bytes.
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
            gl.glVertexPointer(3, GL.GL_FLOAT, 4 * stride, 0);

            // Apply this path's per-position colors if we're in normal rendering mode (not picking) and this path's
            // positionColors is non-null.
            if (useVertexColors)
            {
                // Convert stride and offset from number of elements to number of bytes.
                gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
                gl.glColorPointer(4, GL.GL_FLOAT, 4 * stride, 4 * pathData.colorOffset);
            }

            if (this.positionsSpanDateline && dc.is2DGlobe())
            {
                gl.glDrawElements(GL.GL_LINES, pathData.path2DIndices.limit(), GL.GL_UNSIGNED_INT,
                    pathData.path2DIndices.rewind());
            }
            else
            {
                gl.glDrawArrays(GL.GL_LINE_STRIP, 0, count);
            }

            if (useVertexColors)
                gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

            if (pathData.hasExtrusionPoints && this.isDrawVerticals())
                this.drawVerticalOutlineVBO(dc, vboIds, pathData);

            if (this.isShowPositions())
                this.drawPointsVBO(dc, vboIds, pathData);
        }
        finally
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    protected void doDrawOutlineVA(DrawContext dc, PathData pathData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        int stride = pathData.hasExtrusionPoints ? 2 * pathData.vertexStride : pathData.vertexStride;
        int count = pathData.hasExtrusionPoints ? pathData.vertexCount / 2 : pathData.vertexCount;
        boolean useVertexColors = !dc.isPickingMode() && pathData.tessellatedColors != null;

        // Convert stride from number of elements to number of bytes.
        gl.glVertexPointer(3, GL.GL_FLOAT, 4 * stride, pathData.renderedPath.rewind());

        // Apply this path's per-position colors if we're in normal rendering mode (not picking) and this path's
        // positionColors is non-null.
        if (useVertexColors)
        {
            // Convert stride from number of elements to number of bytes, and position the vertex buffer at the first
            // color.
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 4 * stride, pathData.renderedPath.position(pathData.colorOffset));
            pathData.renderedPath.rewind();
        }

        if (this.positionsSpanDateline && dc.is2DGlobe())
        {
            gl.glDrawElements(GL.GL_LINES, pathData.path2DIndices.limit(), GL.GL_UNSIGNED_INT,
                pathData.path2DIndices.rewind());
        }
        else
        {
            gl.glDrawArrays(GL.GL_LINE_STRIP, 0, count);
        }

        if (useVertexColors)
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

        if (pathData.hasExtrusionPoints && this.isDrawVerticals())
            this.drawVerticalOutlineVA(dc, pathData);

        if (this.isShowPositions())
            this.drawPointsVA(dc, pathData);
    }

    protected void drawVerticalOutlineVBO(DrawContext dc, int[] vboIds, PathData pathData)
    {
        IntBuffer polePositions = pathData.polePositions;
        if (polePositions == null || polePositions.limit() < 1)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Convert stride from number of elements to number of bytes.
        gl.glVertexPointer(3, GL.GL_FLOAT, 4 * pathData.vertexStride, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[1]);
        gl.glDrawElements(GL.GL_LINES, polePositions.limit(), GL.GL_UNSIGNED_INT, 0);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Draws vertical lines at this path's specified positions.
     *
     * @param dc       the current draw context.
     * @param pathData the current globe-specific path data.
     */
    protected void drawVerticalOutlineVA(DrawContext dc, PathData pathData)
    {
        IntBuffer polePositions = pathData.polePositions;
        if (polePositions == null || polePositions.limit() < 1)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Convert stride from number of elements to number of bytes.
        gl.glVertexPointer(3, GL.GL_FLOAT, 4 * pathData.vertexStride, pathData.renderedPath.rewind());
        gl.glDrawElements(GL.GL_LINES, polePositions.limit(), GL.GL_UNSIGNED_INT, polePositions.rewind());
    }

    /**
     * Draws vertical lines at this path's specified positions.
     *
     * @param dc       the current draw context.
     * @param pathData the current globe-specific path data.
     */
    protected void drawPointsVA(DrawContext dc, PathData pathData)
    {
        double d = this.getDistanceMetric(dc, pathData);
        if (d > this.getShowPositionsThreshold())
            return;

        IntBuffer posPoints = pathData.positionPoints;
        if (posPoints == null || posPoints.limit() < 1)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Convert stride from number of elements to number of bytes.
        gl.glVertexPointer(3, GL.GL_FLOAT, 4 * pathData.vertexStride, pathData.renderedPath.rewind());

        if (dc.isPickingMode())
        {
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, pickPositionColors);
        }
        else if (pathData.tessellatedColors != null)
        {
            // Apply this path's per-position colors if we're in normal rendering mode (not picking) and this path's
            // positionColors is non-null. Convert stride from number of elements to number of bytes, and position the
            // vertex buffer at the first color.
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 4 * pathData.vertexStride,
                pathData.renderedPath.position(pathData.colorOffset));
        }

        this.prepareToDrawPoints(dc);
        gl.glDrawElements(GL.GL_POINTS, posPoints.limit(), GL.GL_UNSIGNED_INT, posPoints.rewind());

        // Restore gl state
        gl.glPointSize(1f);
        gl.glDisable(GL2.GL_POINT_SMOOTH);

        if (dc.isPickingMode() || pathData.tessellatedColors != null)
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
    }

    /**
     * Draws points at this path's specified positions.
     * <p/>
     * Note: when the draw context is in picking mode, this binds the current GL_ARRAY_BUFFER to 0 after using the
     * currently bound GL_ARRAY_BUFFER to specify the vertex pointer. This does not restore GL_ARRAY_BUFFER to the its
     * previous state. If the caller intends to use that buffer after this method returns, the caller must bind the
     * buffer again.
     *
     * @param dc       the current draw context.
     * @param vboIds   the ids of this shapes buffers.
     * @param pathData the current globe-specific path data.
     */
    protected void drawPointsVBO(DrawContext dc, int[] vboIds, PathData pathData)
    {
        double d = this.getDistanceMetric(dc, pathData);
        if (d > this.getShowPositionsThreshold())
            return;

        IntBuffer posPoints = pathData.positionPoints;
        if (posPoints == null || posPoints.limit() < 1)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Convert stride from number of elements to number of bytes.
        gl.glVertexPointer(3, GL.GL_FLOAT, 4 * pathData.vertexStride, 0);

        if (dc.isPickingMode())
        {
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, pickPositionColors);
        }
        else if (pathData.tessellatedColors != null)
        {
            // Apply this path's per-position colors if we're in normal rendering mode (not picking) and this path's
            // positionColors is non-null. Convert the stride and offset from number of elements to number of bytes.
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glColorPointer(4, GL.GL_FLOAT, 4 * pathData.vertexStride, 4 * pathData.colorOffset);
        }

        this.prepareToDrawPoints(dc);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
        gl.glDrawElements(GL.GL_POINTS, posPoints.limit(), GL.GL_UNSIGNED_INT, 0);

        // Restore the previous GL point state.
        gl.glPointSize(1f);
        gl.glDisable(GL2.GL_POINT_SMOOTH);

        // Restore the previous GL color array state.
        if (dc.isPickingMode() || pathData.tessellatedColors != null)
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
    }

    protected void prepareToDrawPoints(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (dc.isPickingMode())
        {
            // During picking, compute the GL point size as the product of the active outline width and the show
            // positions scale, plus the positive difference (if any) between the outline pick width and the outline
            // width. During picking, the outline width is set to the larger of the outline width and the outline pick
            // width. We need to adjust the point size accordingly to ensure that the points are not covered by the
            // larger outline width. We add the difference between the normal and pick widths rather than scaling the
            // pick width by the show positions scale, because the latter produces point sizes that are too large, and
            // obscure the other nearby points.
            ShapeAttributes activeAttrs = this.getActiveAttributes();
            double deltaWidth = activeAttrs.getOutlineWidth() < this.getOutlinePickWidth()
                ? this.getOutlinePickWidth() - activeAttrs.getOutlineWidth() : 0;
            gl.glPointSize((float) (this.getShowPositionsScale() * activeAttrs.getOutlineWidth() + deltaWidth));
        }
        else
        {
            // During normal rendering mode, compute the GL point size as the product of the active outline width and
            // the show positions scale. This computation is consistent with the documentation for the methods
            // setShowPositionsScale and getShowPositionsScale.
            gl.glPointSize((float) (this.getShowPositionsScale() * this.getActiveAttributes().getOutlineWidth()));
        }

        // Enable point smoothing both in picking mode and normal rendering mode. Normally, we do not enable smoothing
        // during picking, because GL uses semi-transparent fragments to give the point a round and anti-aliased
        // appearance, and semi-transparent pixels do not correspond to this Path's pickable color. Since blending is
        // not enabled during picking but the alpha test is, this has the effect of producing a rounded point in the
        // pick buffer that has a sharp transition between the Path's pick color and the other fragment colors. Without
        // this state enabled, position points display as squares in the pick buffer.
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
    }

    /**
     * Draws this path's interior when the path is extruded.
     *
     * @param dc the current draw context.
     */
    protected void doDrawInterior(DrawContext dc)
    {
        if (this.shouldUseVBOs(dc))
        {
            int[] vboIds = this.getVboIds(dc);
            if (vboIds != null)
                this.doDrawInteriorVBO(dc, vboIds, this.getCurrentPathData());
            else
                this.doDrawInteriorVA(dc, this.getCurrentPathData());
        }
        else
        {
            this.doDrawInteriorVA(dc, this.getCurrentPathData());
        }
    }

    protected void doDrawInteriorVBO(DrawContext dc, int[] vboIds, PathData pathData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Convert stride from number of elements to number of bytes.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 4 * pathData.vertexStride, 0);
        gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, pathData.vertexCount);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    protected void doDrawInteriorVA(DrawContext dc, PathData pathData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Convert stride from number of elements to number of bytes.
        gl.glVertexPointer(3, GL.GL_FLOAT, 4 * pathData.vertexStride, pathData.renderedPath.rewind());
        gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, pathData.vertexCount);
    }

    /**
     * Computes the shape's model-coordinate path from a list of positions. Applies the path's terrain-conformance
     * settings. Adds extrusion points -- those on the ground -- when the path is extruded.
     *
     * @param dc        the current draw context.
     * @param positions the positions to create a path for.
     * @param pathData  the current globe-specific path data.
     */
    protected void computePath(DrawContext dc, List<Position> positions, PathData pathData)
    {
        pathData.hasExtrusionPoints = false;

        FloatBuffer path = pathData.renderedPath;

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND || dc.is2DGlobe())
            path = this.computePointsRelativeToTerrain(dc, positions, 0d, path, pathData);
        else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            path = this.computePointsRelativeToTerrain(dc, positions, null, path, pathData);
        else
            path = this.computeAbsolutePoints(dc, positions, path, pathData);

        path.flip(); // since the path is reused the limit might not be the same as the previous usage

        pathData.renderedPath = path;
        pathData.vertexCount = path.limit() / pathData.vertexStride;
    }

    /**
     * Computes a terrain-conforming, model-coordinate path from a list of positions, using either a specified altitude
     * or the altitudes in the specified positions. Adds extrusion points -- those on the ground -- when the path is
     * extruded and the specified single altitude is not 0.
     *
     * @param dc        the current draw context.
     * @param positions the positions to create a path for.
     * @param altitude  if non-null, the height above the terrain to use for all positions. If null, each position's
     *                  altitude is used as the height above the terrain.
     * @param path      a buffer in which to store the computed points. May be null. The buffer is not used if it is
     *                  null or tool small for the required number of points. A new buffer is created in that case and
     *                  returned by this method. This method modifies the buffer,s position and limit fields.
     * @param pathData  the current globe-specific path data.
     *
     * @return the buffer in which to place the computed points.
     */
    protected FloatBuffer computePointsRelativeToTerrain(DrawContext dc, List<Position> positions,
        Double altitude, FloatBuffer path, PathData pathData)
    {
        boolean extrudeIt = this.isExtrude() && !(altitude != null && altitude == 0);
        int numPoints = extrudeIt ? 2 * positions.size() : positions.size();
        int elemsPerPoint = (pathData.tessellatedColors != null ? 7 : 3);
        Iterator<Color> colorIter = (pathData.tessellatedColors != null ? pathData.tessellatedColors.iterator() : null);
        float[] color = (pathData.tessellatedColors != null ? new float[4] : null);

        if (path == null || path.capacity() < elemsPerPoint * numPoints)
            path = Buffers.newDirectFloatBuffer(elemsPerPoint * numPoints);

        path.clear();

        for (Position pos : positions)
        {
            double height = altitude != null ? altitude : pos.getAltitude();
            Vec4 referencePoint = pathData.getReferencePoint();
            Vec4 pt = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), height);
            path.put((float) (pt.x - referencePoint.x));
            path.put((float) (pt.y - referencePoint.y));
            path.put((float) (pt.z - referencePoint.z));

            if (colorIter != null && colorIter.hasNext())
            {
                colorIter.next().getRGBComponents(color);
                path.put(color);
            }

            if (extrudeIt)
                this.appendTerrainPoint(dc, pos, color, path, pathData);
        }

        pathData.colorOffset = (pathData.tessellatedColors != null ? 3 : 0);
        pathData.vertexStride = elemsPerPoint;

        return path;
    }

    /**
     * Computes a model-coordinate path from a list of positions, using the altitudes in the specified positions. Adds
     * extrusion points -- those on the ground -- when the path is extruded and the specified single altitude is not 0.
     *
     * @param dc        the current draw context.
     * @param positions the positions to create a path for.
     * @param path      a buffer in which to store the computed points. May be null. The buffer is not used if it is
     *                  null or tool small for the required number of points. A new buffer is created in that case and
     *                  returned by this method. This method modifies the buffer,s position and limit fields.
     * @param pathData  the current globe-specific path data.
     *
     * @return the buffer in which to place the computed points.
     */
    protected FloatBuffer computeAbsolutePoints(DrawContext dc, List<Position> positions, FloatBuffer path,
        PathData pathData)
    {
        int numPoints = this.isExtrude() ? 2 * positions.size() : positions.size();
        int elemsPerPoint = (pathData.tessellatedColors != null ? 7 : 3);
        Iterator<Color> colorIter = (pathData.tessellatedColors != null ? pathData.tessellatedColors.iterator() : null);
        float[] color = (pathData.tessellatedColors != null ? new float[4] : null);

        if (path == null || path.capacity() < elemsPerPoint * numPoints)
            path = Buffers.newDirectFloatBuffer(elemsPerPoint * numPoints);

        path.clear();

        Globe globe = dc.getGlobe();
        Vec4 referencePoint = pathData.getReferencePoint();

        if (dc.getVerticalExaggeration() != 1)
        {
            double ve = dc.getVerticalExaggeration();
            for (Position pos : positions)
            {
                Vec4 pt = globe.computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                    ve * (pos.getAltitude()));
                path.put((float) (pt.x - referencePoint.x));
                path.put((float) (pt.y - referencePoint.y));
                path.put((float) (pt.z - referencePoint.z));

                if (colorIter != null && colorIter.hasNext())
                {
                    colorIter.next().getRGBComponents(color);
                    path.put(color);
                }

                if (this.isExtrude())
                    this.appendTerrainPoint(dc, pos, color, path, pathData);
            }
        }
        else
        {
            for (Position pos : positions)
            {
                Vec4 pt = globe.computePointFromPosition(pos);
                path.put((float) (pt.x - referencePoint.x));
                path.put((float) (pt.y - referencePoint.y));
                path.put((float) (pt.z - referencePoint.z));

                if (colorIter != null && colorIter.hasNext())
                {
                    colorIter.next().getRGBComponents(color);
                    path.put(color);
                }

                if (this.isExtrude())
                    this.appendTerrainPoint(dc, pos, color, path, pathData);
            }
        }

        pathData.colorOffset = (pathData.tessellatedColors != null ? 3 : 0);
        pathData.vertexStride = elemsPerPoint;

        return path;
    }

    /**
     * Computes a point on a path and adds it to the renderable geometry. Used to generate extrusion vertices.
     *
     * @param dc       the current draw context.
     * @param position the path position.
     * @param color    an array of length 4 containing the position's corresponding color as RGBA values in the range
     *                 [0, 1], or <code>null</code> if the position has no associated color.
     * @param path     the path to append to. Assumes that the path has adequate capacity.
     * @param pathData the current globe-specific path data.
     */
    protected void appendTerrainPoint(DrawContext dc, Position position, float[] color, FloatBuffer path,
        PathData pathData)
    {
        Vec4 referencePoint = pathData.getReferencePoint();
        Vec4 pt = dc.computeTerrainPoint(position.getLatitude(), position.getLongitude(), 0d);
        path.put((float) (pt.x - referencePoint.x));
        path.put((float) (pt.y - referencePoint.y));
        path.put((float) (pt.z - referencePoint.z));

        if (color != null)
            path.put(color);

        pathData.hasExtrusionPoints = true;
    }

    /**
     * Registers this Path's pickable position color codes with the specified pickCandidates. The pickCandidates must be
     * an instance of PathPickSupport. This does nothing if this Path's position points are not drawn.
     *
     * @param dc             the current draw context.
     * @param pickCandidates the PickSupport to register with. Must be an instance of PathPickSupport.
     */
    protected void addPickablePositions(DrawContext dc, PickSupport pickCandidates)
    {
        if (!this.isShowPositions())
            return;

        PathData pathData = this.getCurrentPathData();
        double d = this.getDistanceMetric(dc, pathData);
        if (d > this.getShowPositionsThreshold())
            return;

        IntBuffer posPoints = pathData.positionPoints;
        if (posPoints == null || posPoints.limit() < 1)
            return;

        if (pickPositionColors == null || pickPositionColors.capacity() < 3 * pathData.vertexCount)
            pickPositionColors = ByteBuffer.allocateDirect(3 * pathData.vertexCount);
        pickPositionColors.clear();

        posPoints.rewind(); // Rewind the position points buffer before use to ensure it starts at position 0.
        posPoints.get(); // Skip the first position index; this is always 0.
        int nextPosition = posPoints.get();
        Color pickColor = dc.getUniquePickColor();
        int minColorCode = pickColor.getRGB();
        int maxColorCode = minColorCode;

        for (int i = 0; i < pathData.vertexCount; i++)
        {
            if (i == nextPosition)
            {
                if (posPoints.remaining() > 0) // Don't advance beyond the last position index.
                    nextPosition = posPoints.get();
                pickColor = dc.getUniquePickColor();
                maxColorCode = pickColor.getRGB();
            }

            pickPositionColors.put((byte) pickColor.getRed()).put((byte) pickColor.getGreen()).put(
                (byte) pickColor.getBlue());
        }

        pickPositionColors.flip(); // Since this buffer is shared, the limit will likely be different each use.
        posPoints.rewind(); // Rewind the position points buffer after use.

        ((PathPickSupport) pickCandidates).addPickablePositions(minColorCode, maxColorCode, this);
    }

    /**
     * Returns a new PickedObject corresponding to this Path's position point at the specified index. The returned
     * PickedObject's AVList contains the picked position's geographic position in the key AVKey.POSITION and its
     * ordinal number in the key AVKey.ORDINAL.
     *
     * @param colorCode     the color code corresponding to the picked position point.
     * @param positionIndex the position point's index.
     *
     * @return a PickedObject corresponding to the position point at the specified index.
     */
    protected PickedObject resolvePickedPosition(int colorCode, int positionIndex)
    {
        PickedObject po = this.createPickedObject(colorCode);

        Position pos = this.getPosition(positionIndex);
        if (pos != null)
            po.setPosition(pos);

        Integer ordinal = this.getOrdinal(positionIndex);
        if (ordinal != null)
            po.setValue(AVKey.ORDINAL, ordinal);

        return po;
    }

    /**
     * Generates positions defining this path with path type and terrain-conforming properties applied. Builds the
     * path's <code>tessellatedPositions</code> and <code>polePositions</code> fields.
     *
     * @param pathData the current globe-specific path data.
     * @param dc       the current draw context.
     */
    protected void makeTessellatedPositions(DrawContext dc, PathData pathData)
    {
        if (this.numPositions < 2)
            return;

        if (pathData.tessellatedPositions == null || pathData.tessellatedPositions.size() < this.numPositions)
        {
            int size = (this.numSubsegments * (this.numPositions - 1) + 1) * (this.isExtrude() ? 2 : 1);
            pathData.tessellatedPositions = new ArrayList<Position>(size);
            pathData.tessellatedColors = (this.positionColors != null) ? new ArrayList<Color>(size) : null;
        }
        else
        {
            pathData.tessellatedPositions.clear();

            if (pathData.tessellatedColors != null)
                pathData.tessellatedColors.clear();
        }

        if (pathData.polePositions == null || pathData.polePositions.capacity() < this.numPositions * 2)
            pathData.polePositions = Buffers.newDirectIntBuffer(this.numPositions * 2);
        else
            pathData.polePositions.clear();

        if (pathData.positionPoints == null || pathData.positionPoints.capacity() < this.numPositions)
            pathData.positionPoints = Buffers.newDirectIntBuffer(this.numPositions);
        else
            pathData.positionPoints.clear();

        this.makePositions(dc, pathData);

        pathData.tessellatedPositions.trimToSize();
        pathData.polePositions.flip();
        pathData.positionPoints.flip();

        if (pathData.tessellatedColors != null)
            pathData.tessellatedColors.trimToSize();
    }

    /**
     * Computes this Path's distance from the eye point, for use in determining when to show positions points. The value
     * returned is only an approximation because the eye distance varies along the path.
     *
     * @param dc       the current draw context.
     * @param pathData this path's current shape data.
     *
     * @return the distance of the shape from the eye point. If the eye distance cannot be computed, the eye position's
     * elevation is returned instead.
     */
    protected double getDistanceMetric(DrawContext dc, PathData pathData)
    {
        return pathData.getExtent() != null
            ? WWMath.computeDistanceFromEye(dc, pathData.getExtent())
            : dc.getView().getEyePosition().getElevation();
    }

    protected void makePositions(DrawContext dc, PathData pathData)
    {
        if (pathData.splitPositions != null)
            pathData.splitPositions.clear();

        Iterator<? extends Position> iter = this.positions.iterator();
        Position posA = iter.next();
        int ordinalA = 0;
        Color colorA = this.getColor(posA, ordinalA);

        this.addTessellatedPosition(posA, colorA, ordinalA, pathData); // add the first position of the path

        // Tessellate each segment of the path.
        Vec4 ptA = this.computePoint(dc.getTerrain(), posA);

        while (iter.hasNext())
        {
            Position posB = iter.next();
            int ordinalB = ordinalA + 1;
            Color colorB = this.getColor(posB, ordinalB);
            Vec4 ptB = this.computePoint(dc.getTerrain(), posB);

            if (this.positionsSpanDateline && dc.is2DGlobe()
                && posA.getLongitude().degrees != posB.getLongitude().degrees
                && LatLon.locationsCrossDateline(posA, posB))
            {
                // Introduce two points at the dateline that cause the rendered path to break, with one side positive
                // longitude and the other side negative longitude. This break causes the rendered path to break into
                // separate lines during rendering.

                // Compute the split position on the dateline.
                LatLon splitLocation = LatLon.intersectionWithMeridian(posA, posB, Angle.POS180, dc.getGlobe());
                Position splitPosition = Position.fromDegrees(splitLocation.getLatitude().degrees,
                    180 * Math.signum(posA.getLongitude().degrees), posA.getAltitude());
                Vec4 splitPoint = this.computePoint(dc.getTerrain(), splitPosition);

                // Compute the color at the split position.
                Color splitColor = null;
                if (colorA != null && colorB != null)
                {
                    double originalSegmentLength = this.computeSegmentLength(dc, posA, posB);
                    double truncatedSegmentLength = this.computeSegmentLength(dc, posA, splitPosition);
                    double s = truncatedSegmentLength / originalSegmentLength;
                    splitColor = s > 0 ? WWUtil.interpolateColor(s, colorA, colorB) : colorA;
                }

                // Create the tessellated-positions segment from the beginning position to the split position.
                this.makeSegment(dc, posA, splitPosition, ptA, splitPoint, colorA, splitColor, ordinalA, -1, pathData);

                // Mark where the split position is so a new line is started there during rendering.
                if (pathData.splitPositions == null)
                    pathData.splitPositions = new ArrayList<Integer>(1);
                pathData.splitPositions.add(pathData.tessellatedPositions.size());

                // Make the corresponding split position on the dateline side with opposite sign of the first split
                // position.
                splitPosition = Position.fromDegrees(splitPosition.getLatitude().degrees,
                    -1 * splitPosition.getLongitude().degrees, splitPosition.getAltitude());
                splitPoint = this.computePoint(dc.getTerrain(), splitPosition);

                // Create the tessellated-positions segment from the split position to the end position.
                this.addTessellatedPosition(splitPosition, splitColor, -1, pathData);
                this.makeSegment(dc, splitPosition, posB, splitPoint, ptB, splitColor, colorB, -1, ordinalB, pathData);
            }
            else if (this.isSmall(dc, ptA, ptB, 8) || !this.isSegmentVisible(dc, posA, posB, ptA, ptB))
            {
                // If the segment is very small or not visible, don't tessellate, just add the segment's end position.
                this.addTessellatedPosition(posB, colorB, ordinalB, pathData);
            }
            else
            {
                this.makeSegment(dc, posA, posB, ptA, ptB, colorA, colorB, ordinalA, ordinalB, pathData);
            }

            posA = posB;
            ptA = ptB;
            ordinalA = ordinalB;
            colorA = colorB;
        }

        if (this.positionsSpanDateline && dc.is2DGlobe())
            this.makePath2DIndices(pathData);
    }

    /**
     * Adds a position to this path's <code>tessellatedPositions</code> list. If the specified color is not
     * <code>null</code>, this adds the color to this path's <code>tessellatedColors</code> list. If the specified
     * ordinal is not <code>null</code>, this adds the position's index to the <code>polePositions</code> and
     * <code>positionPoints</code> index buffers.
     *
     * @param pos      the position to add.
     * @param color    the color corresponding to the position. May be <code>null</code> to indicate that the position
     *                 has no associated color.
     * @param ordinal  the ordinal number corresponding to the position's location in the original position list. May be
     *                 <code>null</code> to indicate that the position is not one of the originally specified
     *                 positions.
     * @param pathData the current globe-specific path data.
     */
    protected void addTessellatedPosition(Position pos, Color color, Integer ordinal, PathData pathData)
    {
        if (ordinal != null && ordinal >= 0)
        {
            // NOTE: Assign these indices before adding the new position to the tessellatedPositions list.
            int index = pathData.tessellatedPositions.size() * 2;
            pathData.polePositions.put(index).put(index + 1);

            if (pathData.hasExtrusionPoints)
                pathData.positionPoints.put(index);
            else
                pathData.positionPoints.put(pathData.tessellatedPositions.size());
        }

        pathData.tessellatedPositions.add(pos); // be sure to do the add after the pole position is set

        if (color != null)
            pathData.tessellatedColors.add(color);
    }

    protected void makePath2DIndices(PathData pathData)
    {
        int size = pathData.tessellatedPositions.size() * 2 - 2;
        if (pathData.path2DIndices == null || pathData.path2DIndices.capacity() != size)
            pathData.path2DIndices = Buffers.newDirectIntBuffer(size);

        int currentIndex = 0;
        if (pathData.splitPositions != null)
        {
            for (Integer splitIndex : pathData.splitPositions)
            {
                for (int i = currentIndex; i < splitIndex - 1; i++)
                {
                    pathData.path2DIndices.put(i).put(i + 1);
                }
                pathData.path2DIndices.put(splitIndex).put(splitIndex + 1);
                currentIndex = splitIndex + 1;
            }
        }

        for (int i = currentIndex; i < pathData.tessellatedPositions.size() - 1; i++)
        {
            pathData.path2DIndices.put(i).put(i + 1);
        }

        pathData.path2DIndices.flip();
    }

    /**
     * Returns the Path position corresponding index. This returns null if the index does not correspond to an original
     * position.
     *
     * @param positionIndex the position's index.
     *
     * @return the Position corresponding to the specified index.
     */
    protected Position getPosition(int positionIndex)
    {
        PathData pathData = this.getCurrentPathData();
        // Get an index into the tessellatedPositions list.
        int index = pathData.positionPoints.get(positionIndex);
        // Return the originally specified position, which is stored in the tessellatedPositions list.
        return (index >= 0 && index < pathData.tessellatedPositions.size()) ?
            pathData.tessellatedPositions.get(index) : null;
    }

    /**
     * Returns the ordinal number corresponding to the position. This returns null if the position index does not
     * correspond to an original position.
     *
     * @param positionIndex the position's index.
     *
     * @return the ordinal number corresponding to the specified position index.
     */
    protected Integer getOrdinal(int positionIndex)
    {
        return positionIndex;
    }

    /**
     * Returns an RGBA color corresponding to the specified position from the original position list and its
     * corresponding ordinal number by delegating the call to this path's positionColors. This returns <code>null</code>
     * if this path's positionColors property is <code>null</code>. This returns white if a color cannot be determined
     * for the specified position and ordinal.
     *
     * @param pos     the path position the color corresponds to.
     * @param ordinal the ordinal number of the specified position.
     *
     * @return an RGBA color corresponding to the position and ordinal, or <code>null</code> if this path's
     * positionColors property is <code>null</code>.
     */
    protected Color getColor(Position pos, Integer ordinal)
    {
        if (this.positionColors == null)
            return null;

        Color color = this.positionColors.getColor(pos, ordinal);
        return color != null ? color : Color.WHITE;
    }

    /**
     * Determines whether the segment between two path positions is visible.
     *
     * @param dc   the current draw context.
     * @param posA the segment's first position.
     * @param posB the segment's second position.
     * @param ptA  the model-coordinate point corresponding to the segment's first position.
     * @param ptB  the model-coordinate point corresponding to the segment's second position.
     *
     * @return true if the segment is visible relative to the current view frustum, otherwise false.
     */
    protected boolean isSegmentVisible(DrawContext dc, Position posA, Position posB, Vec4 ptA, Vec4 ptB)
    {
        Frustum f = dc.getView().getFrustumInModelCoordinates();

        if (f.contains(ptA))
            return true;

        if (f.contains(ptB))
            return true;

        if (ptA.equals(ptB))
            return false;

        Position posC = Position.interpolateRhumb(0.5, posA, posB);
        Vec4 ptC = this.computePoint(dc.getTerrain(), posC);
        if (f.contains(ptC))
            return true;

        double r = Line.distanceToSegment(ptA, ptB, ptC);
        Cylinder cyl = new Cylinder(ptA, ptB, r == 0 ? 1 : r);
        return cyl.intersects(dc.getView().getFrustumInModelCoordinates());
    }

    /**
     * Creates the interior segment positions to adhere to the current path type and terrain-following settings.
     *
     * @param dc       the current draw context.
     * @param posA     the segment's first position.
     * @param posB     the segment's second position.
     * @param ptA      the model-coordinate point corresponding to the segment's first position.
     * @param ptB      the model-coordinate point corresponding to the segment's second position.
     * @param colorA   the color corresponding to the segment's first position, or <code>null</code> if the first
     *                 position has no associated color.
     * @param colorB   the color corresponding to the segment's second position, or <code>null</code> if the first
     *                 position has no associated color.
     * @param ordinalA the ordinal number corresponding to the segment's first position in the original position list.
     * @param ordinalB the ordinal number corresponding to the segment's second position in the original position list.
     * @param pathData the current globe-specific path data.
     */
    @SuppressWarnings({"StringEquality", "UnusedParameters"})
    protected void makeSegment(DrawContext dc, Position posA, Position posB, Vec4 ptA, Vec4 ptB, Color colorA,
        Color colorB, int ordinalA, int ordinalB, PathData pathData)
    {
        // This method does not add the first position of the segment to the position list. It adds only the
        // subsequent positions, including the segment's last position.

        boolean straightLine = this.getPathType() == AVKey.LINEAR && !this.isSurfacePath(dc);

        double arcLength;
        if (straightLine)
            arcLength = ptA.distanceTo3(ptB);
        else
            arcLength = this.computeSegmentLength(dc, posA, posB);

        if (arcLength <= 0 || straightLine)
        {
            if (!ptA.equals(ptB) || (this.positionsSpanDateline && dc.is2DGlobe()))
                this.addTessellatedPosition(posB, colorB, ordinalB, pathData);
            return;
        }

        // Variables for great circle and rhumb computation.
        Angle segmentAzimuth = null;
        Angle segmentDistance = null;

        for (double s = 0, p = 0; s < 1; )
        {
            if (this.isFollowTerrain() || dc.is2DGlobe())
                p += this.terrainConformance * dc.getView().computePixelSizeAtDistance(
                    ptA.distanceTo3(dc.getView().getEyePoint()));
            else
                p += arcLength / this.numSubsegments;

            if (arcLength < p || arcLength - p < 1e-9)
                break; // position is either beyond the arc length or the remaining distance is in millimeters on Earth

            Position pos;
            Color color;
            s = p / arcLength;

            if (this.pathType == AVKey.LINEAR)
            {
                if (segmentAzimuth == null)
                {
                    segmentAzimuth = LatLon.linearAzimuth(posA, posB);
                    segmentDistance = LatLon.linearDistance(posA, posB);
                }
                Angle distance = Angle.fromRadians(s * segmentDistance.radians);
                LatLon latLon = LatLon.linearEndPosition(posA, segmentAzimuth, distance);
                pos = new Position(latLon, (1 - s) * posA.getElevation() + s * posB.getElevation());
                color = (colorA != null && colorB != null) ? WWUtil.interpolateColor(s, colorA, colorB) : null;
            }
            else if (this.pathType == AVKey.RHUMB_LINE || this.pathType == AVKey.LOXODROME)
            {
                if (segmentAzimuth == null)
                {
                    segmentAzimuth = LatLon.rhumbAzimuth(posA, posB);
                    segmentDistance = LatLon.rhumbDistance(posA, posB);
                }
                Angle distance = Angle.fromRadians(s * segmentDistance.radians);
                LatLon latLon = LatLon.rhumbEndPosition(posA, segmentAzimuth, distance);
                pos = new Position(latLon, (1 - s) * posA.getElevation() + s * posB.getElevation());
                color = (colorA != null && colorB != null) ? WWUtil.interpolateColor(s, colorA, colorB) : null;
            }
            else // GREAT_CIRCLE
            {
                if (segmentAzimuth == null)
                {
                    segmentAzimuth = LatLon.greatCircleAzimuth(posA, posB);
                    segmentDistance = LatLon.greatCircleDistance(posA, posB);
                }
                Angle distance = Angle.fromRadians(s * segmentDistance.radians);
                LatLon latLon = LatLon.greatCircleEndPosition(posA, segmentAzimuth, distance);
                pos = new Position(latLon, (1 - s) * posA.getElevation() + s * posB.getElevation());
                color = (colorA != null && colorB != null) ? WWUtil.interpolateColor(s, colorA, colorB) : null;
            }

            this.addTessellatedPosition(pos, color, null, pathData);

            ptA = ptB;
        }

        this.addTessellatedPosition(posB, colorB, ordinalB, pathData);
    }

    /**
     * Computes the approximate model-coordinate, path length between two positions. The length of the path depends on
     * the path type: great circle, rhumb, or linear.
     *
     * @param dc   the current draw context.
     * @param posA the first position.
     * @param posB the second position.
     *
     * @return the distance between the positions.
     */
    @SuppressWarnings({"StringEquality"})
    protected double computeSegmentLength(DrawContext dc, Position posA, Position posB)
    {
        LatLon llA = new LatLon(posA.getLatitude(), posA.getLongitude());
        LatLon llB = new LatLon(posB.getLatitude(), posB.getLongitude());

        Angle ang;
        String pathType = this.getPathType();
        if (pathType == AVKey.LINEAR)
            ang = LatLon.linearDistance(llA, llB);
        else if (pathType == AVKey.RHUMB_LINE || pathType == AVKey.LOXODROME)
            ang = LatLon.rhumbDistance(llA, llB);
        else // Great circle
            ang = LatLon.greatCircleDistance(llA, llB);

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            return ang.radians * (dc.getGlobe().getRadius());

        double height = 0.5 * (posA.getElevation() + posB.getElevation());
        return ang.radians * (dc.getGlobe().getRadius() + height * dc.getVerticalExaggeration());
    }

    /**
     * Computes this path's reference center.
     *
     * @param dc the current draw context.
     *
     * @return the computed reference center, or null if it cannot be computed.
     */
    protected Vec4 computeReferenceCenter(DrawContext dc)
    {
        if (this.positions == null)
            return null;

        Position pos = this.getReferencePosition();
        if (pos == null)
            return null;

        return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
            dc.getVerticalExaggeration() * pos.getAltitude());
    }

    /**
     * Computes the minimum distance between this Path and the eye point.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc       the draw context.
     * @param pathData the current shape data for this shape.
     *
     * @return the minimum distance from the shape to the eye point.
     */
    protected double computeEyeDistance(DrawContext dc, PathData pathData)
    {
        double minDistanceSquared = Double.MAX_VALUE;
        Vec4 eyePoint = dc.getView().getEyePoint();
        Vec4 refPt = pathData.getReferencePoint();

        pathData.renderedPath.rewind();
        while (pathData.renderedPath.hasRemaining())
        {
            double x = eyePoint.x - (pathData.renderedPath.get() + refPt.x);
            double y = eyePoint.y - (pathData.renderedPath.get() + refPt.y);
            double z = eyePoint.z - (pathData.renderedPath.get() + refPt.z);

            double d = x * x + y * y + z * z;
            if (d < minDistanceSquared)
                minDistanceSquared = d;

            // If the renderedPath contains RGBA color tuples in between each XYZ coordinate tuple, advance the
            // renderedPath's position to the next XYZ coordinate tuple.
            if (pathData.vertexStride > 3)
                pathData.renderedPath.position(pathData.renderedPath.position() + pathData.vertexStride - 3);
        }

        return Math.sqrt(minDistanceSquared);
    }

    /**
     * Computes the path's bounding box from the current rendering path. Assumes the rendering path is up-to-date.
     *
     * @param current the current data for this shape.
     *
     * @return the computed extent.
     */
    protected Extent computeExtent(PathData current)
    {
        if (current.renderedPath == null)
            return null;

        current.renderedPath.rewind();
        Box box = Box.computeBoundingBox(new BufferWrapper.FloatBufferWrapper(current.renderedPath),
            current.vertexStride);

        // The path points are relative to the reference center, so translate the extent to the reference center.
        box = box.translate(current.getReferencePoint());

        return box;
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        // See if we've cached an extent associated with the globe.
        Extent extent = super.getExtent(globe, verticalExaggeration);
        if (extent != null)
            return extent;

        PathData current = (PathData) this.shapeDataCache.getEntry(globe);
        if (current == null)
            return null;

        // Use the tessellated positions if they exist because they best represent the actual shape.
        Iterable<? extends Position> posits = current.tessellatedPositions != null
            ? current.tessellatedPositions : this.getPositions();
        if (posits == null)
            return null;

        return super.computeExtentFromPositions(globe, verticalExaggeration, posits);
    }

    /**
     * Computes the path's reference position. The position returned is the center-most ordinal position in the path's
     * specified positions.
     *
     * @return the computed reference position.
     */
    public Position getReferencePosition()
    {
        return this.numPositions < 1 ? null : this.positions.iterator().next(); // use the first position
    }

    protected void fillVBO(DrawContext dc)
    {
        PathData pathData = this.getCurrentPathData();
        int numIds = this.isShowPositions() ? 3 : pathData.hasExtrusionPoints && this.isDrawVerticals() ? 2 : 1;

        int[] vboIds = (int[]) dc.getGpuResourceCache().get(pathData.getVboCacheKey());
        if (vboIds != null && vboIds.length != numIds)
        {
            this.clearCachedVbos(dc);
            vboIds = null;
        }

        GL gl = dc.getGL();

        int vSize = pathData.renderedPath.limit() * 4;
        int iSize = pathData.hasExtrusionPoints
            && this.isDrawVerticals() ? pathData.tessellatedPositions.size() * 2 * 4 : 0;
        if (this.isShowPositions())
            iSize += pathData.tessellatedPositions.size();

        if (vboIds == null)
        {
            vboIds = new int[numIds];
            gl.glGenBuffers(vboIds.length, vboIds, 0);
            dc.getGpuResourceCache().put(pathData.getVboCacheKey(), vboIds, GpuResourceCache.VBO_BUFFERS,
                vSize + iSize);
        }

        try
        {
            FloatBuffer vb = pathData.renderedPath;
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, vb.limit() * 4, vb.rewind(), GL.GL_STATIC_DRAW);

            if (pathData.hasExtrusionPoints && this.isDrawVerticals())
            {
                IntBuffer ib = pathData.polePositions;
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[1]);
                gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, ib.limit() * 4, ib.rewind(), GL.GL_STATIC_DRAW);
            }

            if (this.isShowPositions())
            {
                IntBuffer ib = pathData.positionPoints;
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[2]);
                gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, ib.limit() * 4, ib.rewind(), GL.GL_STATIC_DRAW);
            }
        }
        finally
        {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    @Override
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException // TODO
    {
        return null;
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

        // The reference position is null if this Path has no positions. In this case moving the Path by a
        // relative delta is meaningless because the Path has no geographic location. Therefore we fail softly by
        // exiting and doing nothing.
        if (refPos == null)
            return;

        this.moveTo(refPos.add(delta));
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.numPositions == 0)
            return;

        Position oldPosition = this.getReferencePosition();

        // The reference position is null if this Path has no positions. In this case moving the Path to a new
        // reference position is meaningless because the Path has no geographic location. Therefore we fail softly
        // by exiting and doing nothing.
        if (oldPosition == null)
            return;

        List<Position> newPositions = Position.computeShiftedPositions(oldPosition, position, this.positions);

        if (newPositions != null)
            this.setPositions(newPositions);
    }

    @Override
    public void moveTo(Globe globe, Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.numPositions == 0)
            return;

        Position oldPosition = this.getReferencePosition();

        // The reference position is null if this Path has no positions. In this case moving the Path to a new
        // reference position is meaningless because the Path has no geographic location. Therefore we fail softly
        // by exiting and doing nothing.
        if (oldPosition == null)
            return;

        List<Position> newPositions = Position.computeShiftedPositions(globe, oldPosition, position, this.positions);

        if (newPositions != null)
        {
            this.setPositions(newPositions);
        }
    }

    protected boolean isSmall(DrawContext dc, Vec4 ptA, Vec4 ptB, int numPixels)
    {
        return ptA.distanceTo3(ptB) <= numPixels * dc.getView().computePixelSizeAtDistance(
            dc.getView().getEyePoint().distanceTo3(ptA));
    }

    /** {@inheritDoc} */
    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        // Write geometry
        xmlWriter.writeStartElement("LineString");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters(kmlBoolean(isExtrude()));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("tessellate");
        xmlWriter.writeCharacters(kmlBoolean(isFollowTerrain()));
        xmlWriter.writeEndElement();

        final String altitudeMode = KMLExportUtil.kmlAltitudeMode(getAltitudeMode());
        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("coordinates");
        for (Position position : this.positions)
        {
            xmlWriter.writeCharacters(String.format(Locale.US, "%f,%f,%f ",
                position.getLongitude().getDegrees(),
                position.getLatitude().getDegrees(),
                position.getElevation()));
        }
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement(); // LineString
    }
}
