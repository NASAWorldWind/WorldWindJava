/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: Polygon.java 2309 2014-09-17 00:04:08Z tgaskins $
 */
public class Polygon extends AbstractAirspace
{
    protected static final int DEFAULT_SUBDIVISIONS = 3;
    protected static final int MINIMAL_GEOMETRY_SUBDIVISIONS = 2;

    private List<LatLon> locations = new ArrayList<LatLon>();
    private boolean enableCaps = true;
    private int subdivisions = DEFAULT_SUBDIVISIONS;

    public Polygon(Polygon source)
    {
        super(source);

        this.enableCaps = source.enableCaps;
        this.subdivisions = source.subdivisions;

        this.addLocations(source.locations);
        this.makeDefaultDetailLevels();
    }

    public Polygon(Iterable<? extends LatLon> locations)
    {
        this.addLocations(locations);
        this.makeDefaultDetailLevels();
    }

    public Polygon(AirspaceAttributes attributes)
    {
        super(attributes);
        this.makeDefaultDetailLevels();
    }

    public Polygon()
    {
        this.makeDefaultDetailLevels();
    }

    private void makeDefaultDetailLevels()
    {
        List<DetailLevel> levels = new ArrayList<DetailLevel>();
        double[] ramp = ScreenSizeDetailLevel.computeDefaultScreenSizeRamp(5);

        DetailLevel level;
        level = new ScreenSizeDetailLevel(ramp[0], "Detail-Level-0");
        level.setValue(SUBDIVISIONS, 4);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[1], "Detail-Level-1");
        level.setValue(SUBDIVISIONS, 3);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[2], "Detail-Level-2");
        level.setValue(SUBDIVISIONS, 2);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[3], "Detail-Level-3");
        level.setValue(SUBDIVISIONS, 1);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[4], "Detail-Level-4");
        level.setValue(SUBDIVISIONS, 0);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, true);
        levels.add(level);

        this.setDetailLevels(levels);
    }

    public List<LatLon> getLocations()
    {
        return Collections.unmodifiableList(this.locations);
    }

    public void setLocations(Iterable<? extends LatLon> locations)
    {
        this.locations.clear();
        this.addLocations(locations);
    }

    protected List<LatLon> getLocationList()
    {
        return this.locations;
    }

    protected void addLocations(Iterable<? extends LatLon> newLocations)
    {
        if (newLocations != null)
        {
            for (LatLon ll : newLocations)
            {
                if (ll != null)
                    this.locations.add(ll);
            }
        }

        this.invalidateAirspaceData();
    }

    public boolean isEnableCaps()
    {
        return this.enableCaps;
    }

    public void setEnableCaps(boolean enable)
    {
        this.enableCaps = enable;
    }

    public Position getReferencePosition()
    {
        return this.computeReferencePosition(this.locations, this.getAltitudes());
    }

    protected Extent computeExtent(Globe globe, double verticalExaggeration)
    {
        List<Vec4> points = this.computeMinimalGeometry(globe, verticalExaggeration);
        if (points == null || points.isEmpty())
            return null;

        // Add a point at the center of this polygon to the points used to compute its extent. The center point captures
        // the curvature of the globe when the polygon's minimal geometry only contain any points near the polygon's
        // edges.
        Vec4 centerPoint = Vec4.computeAveragePoint(points);
        LatLon centerLocation = globe.computePositionFromPoint(centerPoint);
        this.makeExtremePoints(globe, verticalExaggeration, Arrays.asList(centerLocation), points);

        return Box.computeBoundingBox(points);
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        List<LatLon> locations = this.getLocations();
        if (locations == null || locations.isEmpty())
            return null;

        ArrayList<LatLon> copyOfLocations = new ArrayList<LatLon>(locations);
        ArrayList<LatLon> tessellatedLocations = new ArrayList<LatLon>();
        this.makeTessellatedLocations(globe, MINIMAL_GEOMETRY_SUBDIVISIONS, copyOfLocations, tessellatedLocations);

        ArrayList<Vec4> points = new ArrayList<Vec4>();
        this.makeExtremePoints(globe, verticalExaggeration, tessellatedLocations, points);

        return points;
    }

    protected void doMoveTo(Globe globe, Position oldRef, Position newRef)
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

        List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldRef, newRef, this.getLocations());
        this.setLocations(newLocations);

        super.doMoveTo(oldRef, newRef);
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

        super.doMoveTo(oldRef, newRef);

        int count = this.locations.size();
        LatLon[] newLocations = new LatLon[count];
        for (int i = 0; i < count; i++)
        {
            LatLon ll = this.locations.get(i);
            double distance = LatLon.greatCircleDistance(oldRef, ll).radians;
            double azimuth = LatLon.greatCircleAzimuth(oldRef, ll).radians;
            newLocations[i] = LatLon.greatCircleEndPosition(newRef, azimuth, distance);
        }
        this.setLocations(Arrays.asList(newLocations));
    }

    @Override
    protected SurfaceShape createSurfaceShape()
    {
        return new SurfacePolygon();
    }

    @Override
    protected void updateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        super.updateSurfaceShape(dc, shape);

        boolean mustDrawInterior = this.getActiveAttributes().isDrawInterior() && this.isEnableCaps();
        shape.getAttributes().setDrawInterior(mustDrawInterior); // suppress the shape interior when caps are disabled
    }

    @Override
    protected void regenerateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        ((SurfacePolygon) shape).setOuterBoundary(this.locations);
    }

    protected int getSubdivisions()
    {
        return this.subdivisions;
    }

    protected void setSubdivisions(int subdivisions)
    {
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions=" + subdivisions);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.subdivisions = subdivisions;
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    protected Vec4 computeReferenceCenter(DrawContext dc)
    {
        Extent extent = this.getExtent(dc);
        return extent != null ? extent.getCenter() : null;
    }

    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        this.doRenderGeometry(dc, drawStyle, this.locations, null);
    }

    protected void doRenderGeometry(DrawContext dc, String drawStyle, List<LatLon> locations, List<Boolean> edgeFlags)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (locations == null)
        {
            String message = "nullValue.LocationsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (locations.size() == 0)
            return;

        double[] altitudes = this.getAltitudes(dc.getVerticalExaggeration());
        boolean[] terrainConformant = this.isTerrainConforming();
        boolean enableCaps = this.isEnableCaps();
        int subdivisions = this.subdivisions;

        if (this.getAltitudeDatum()[0].equals(AVKey.ABOVE_GROUND_REFERENCE)
            || this.getAltitudeDatum()[1].equals(AVKey.ABOVE_GROUND_REFERENCE))
        {
            this.adjustForGroundReference(dc, terrainConformant, altitudes);
        }

        if (this.isEnableLevelOfDetail())
        {
            DetailLevel level = this.computeDetailLevel(dc);

            Object o = level.getValue(SUBDIVISIONS);
            if (o != null && o instanceof Integer)
                subdivisions = (Integer) o;

            o = level.getValue(DISABLE_TERRAIN_CONFORMANCE);
            if (o != null && o instanceof Boolean && (Boolean) o)
                terrainConformant[0] = terrainConformant[1] = false;
        }

        Vec4 referenceCenter = this.computeReferenceCenter(dc);
        this.setExpiryTime(this.nextExpiryTime(dc, terrainConformant));
        this.clearElevationMap();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        try
        {
            dc.getView().pushReferenceCenter(dc, referenceCenter);

            if (Airspace.DRAW_STYLE_FILL.equals(drawStyle))
            {
                if (enableCaps && !this.isAirspaceCollapsed())
                {
                    ogsh.pushAttrib(gl, GL2.GL_POLYGON_BIT);
                    gl.glEnable(GL.GL_CULL_FACE);
                    gl.glFrontFace(GL.GL_CCW);
                }

                this.drawPolygonFill(dc, locations, edgeFlags, altitudes, terrainConformant, enableCaps, subdivisions,
                    referenceCenter);
            }
            else if (Airspace.DRAW_STYLE_OUTLINE.equals(drawStyle))
            {
                this.drawPolygonOutline(dc, locations, edgeFlags, altitudes, terrainConformant, enableCaps,
                    subdivisions, referenceCenter);
            }
        }
        finally
        {
            dc.getView().popReferenceCenter(dc);
            ogsh.pop(gl);
        }
    }

    protected void adjustForGroundReference(DrawContext dc, boolean[] terrainConformant, double[] altitudes)
    {
        LatLon groundRef = this.getGroundReference();

        if (groundRef == null && this.getLocationList().size() > 0)
            groundRef = this.getLocationList().get(0);

        this.adjustForGroundReference(dc, terrainConformant, altitudes, groundRef); // no-op if groudRef is null
    }

    protected int computeEllipsoidalPolygon(Globe globe, List<? extends LatLon> locations, List<Boolean> edgeFlags,
        Vec4[] points, Boolean[] edgeFlagArray, Matrix[] transform)
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
        if (points == null)
        {
            String message = "nullValue.LocationsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points.length < (1 + locations.size()))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength",
                "points.length < " + (1 + locations.size()));
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (transform == null)
        {
            String message = "nullValue.TransformIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (transform.length < 1)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength",
                "transform.length < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Allocate space to hold the list of locations and location vertices.
        int locationCount = locations.size();

        // Compute the cartesian points for each location.
        for (int i = 0; i < locationCount; i++)
        {
            LatLon ll = locations.get(i);
            points[i] = globe.computeEllipsoidalPointFromPosition(ll.getLatitude(), ll.getLongitude(), 0.0);

            if (edgeFlagArray != null)
                edgeFlagArray[i] = (edgeFlags != null) ? edgeFlags.get(i) : true;
        }

        // Compute the average of the cartesian points.
        Vec4 centerPoint = Vec4.computeAveragePoint(Arrays.asList(points));

        // Test whether the polygon is closed. If it is not closed, repeat the first vertex.
        if (!points[0].equals(points[locationCount - 1]))
        {
            points[locationCount] = points[0];
            if (edgeFlagArray != null)
                edgeFlagArray[locationCount] = edgeFlagArray[0];

            locationCount++;
        }

        // Compute a transform that will map the cartesian points to a local coordinate system centered at the average
        // of the points and oriented with the globe surface.
        Position centerPos = globe.computePositionFromEllipsoidalPoint(centerPoint);
        Matrix tx = globe.computeEllipsoidalOrientationAtPosition(centerPos.latitude, centerPos.longitude,
            centerPos.elevation);
        Matrix txInv = tx.getInverse();
        // Map the cartesian points to a local coordinate space.
        for (int i = 0; i < locationCount; i++)
        {
            points[i] = points[i].transformBy4(txInv);
        }

        transform[0] = tx;

        return locationCount;
    }

    private void makePolygonVertices(int count, Vec4[] points, float[] vertices)
    {
        for (int i = 0; i < count; i++)
        {
            int index = 3 * i;
            vertices[index] = (float) points[i].x;
            vertices[index + 1] = (float) points[i].y;
            vertices[index + 2] = (float) points[i].z;
        }
    }

    //**************************************************************//
    //********************  Polygon  ******************//
    //**************************************************************//

    protected static class PolygonGeometry implements Cacheable
    {
        private Geometry fillIndexGeometry;
        private Geometry outlineIndexGeometry;
        private Geometry vertexGeometry;

        public PolygonGeometry()
        {
            this.fillIndexGeometry = new Geometry();
            this.outlineIndexGeometry = new Geometry();
            this.vertexGeometry = new Geometry();
        }

        public Geometry getFillIndexGeometry()
        {
            return this.fillIndexGeometry;
        }

        public Geometry getOutlineIndexGeometry()
        {
            return this.outlineIndexGeometry;
        }

        public Geometry getVertexGeometry()
        {
            return this.vertexGeometry;
        }

        public long getSizeInBytes()
        {
            long sizeInBytes = 0L;
            sizeInBytes += (this.fillIndexGeometry != null) ? this.fillIndexGeometry.getSizeInBytes() : 0L;
            sizeInBytes += (this.outlineIndexGeometry != null) ? this.outlineIndexGeometry.getSizeInBytes() : 0L;
            sizeInBytes += (this.vertexGeometry != null) ? this.vertexGeometry.getSizeInBytes() : 0L;

            return sizeInBytes;
        }
    }

    private PolygonGeometry getPolygonGeometry(DrawContext dc, List<LatLon> locations, List<Boolean> edgeFlags,
        double[] altitudes, boolean[] terrainConformant,
        boolean enableCaps, int subdivisions,
        Vec4 referenceCenter)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "Polygon",
            locations, edgeFlags, altitudes[0], altitudes[1], terrainConformant[0], terrainConformant[1],
            enableCaps, subdivisions, referenceCenter);

        // Wrap geometry creation in a try/catch block. We do this to catch and handle OutOfMemoryErrors caused during
        // tessellation of the polygon vertices. If the polygon cannot be tessellated, we replace the polygon's
        // locations with an empty list to prevent subsequent tessellation attempts, and to avoid rendering a misleading
        // representation by omitting any part of the geometry.
        try
        {
            PolygonGeometry geom = (PolygonGeometry) this.getGeometryCache().getObject(cacheKey);
            if (geom == null || this.isExpired(dc, geom.getVertexGeometry()))
            {
                if (geom == null)
                    geom = new PolygonGeometry();
                this.makePolygon(dc, locations, edgeFlags, altitudes, terrainConformant, enableCaps, subdivisions,
                    referenceCenter, geom);
                this.updateExpiryCriteria(dc, geom.getVertexGeometry());
                this.getGeometryCache().add(cacheKey, geom);
            }

            return geom;
        }
        catch (OutOfMemoryError e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileTessellating", this);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);

            //noinspection ThrowableInstanceNeverThrown
            dc.addRenderingException(new WWRuntimeException(message, e));

            this.handleUnsuccessfulGeometryCreation();
            return null;
        }
    }

    protected void handleUnsuccessfulGeometryCreation()
    {
        // If creating the polygon geometry was unsuccessful, we modify the polygon to avoid any additional creation
        // attempts, and free any resources that the polygon won't use. This is done to gracefully handle
        // OutOfMemoryErrors throws while tessellating the polygon geometry.

        // Replace the polygon's locations with an empty list. This ensures that any rendering code won't attempt to
        // re-create the polygon's geometry.
        this.locations = Collections.emptyList();
        // Reinitialize the polygon, since we've replaced its locations with an empty list.
        this.invalidateAirspaceData();
    }

    private void drawPolygonFill(DrawContext dc, List<LatLon> locations, List<Boolean> edgeFlags,
        double[] altitudes, boolean[] terrainConformant,
        boolean enableCaps, int subdivisions,
        Vec4 referenceCenter)
    {
        PolygonGeometry geom = this.getPolygonGeometry(dc, locations, edgeFlags, altitudes, terrainConformant,
            enableCaps, subdivisions, referenceCenter);
        if (geom != null)
            this.drawGeometry(dc, geom.getFillIndexGeometry(), geom.getVertexGeometry());
    }

    private void drawPolygonOutline(DrawContext dc, List<LatLon> locations, List<Boolean> edgeFlags,
        double[] altitudes, boolean[] terrainConformant,
        boolean enableCaps, int subdivisions,
        Vec4 referenceCenter)
    {
        PolygonGeometry geom = this.getPolygonGeometry(dc, locations, edgeFlags, altitudes, terrainConformant,
            enableCaps, subdivisions, referenceCenter);
        if (geom != null)
            this.drawGeometry(dc, geom.getOutlineIndexGeometry(), geom.getVertexGeometry());
    }

    private void makePolygon(DrawContext dc, List<LatLon> locations, List<Boolean> edgeFlags,
        double[] altitudes, boolean[] terrainConformant,
        boolean enableCaps, int subdivisions,
        Vec4 referenceCenter,
        PolygonGeometry dest)
    {
        if (locations.size() == 0)
            return;

        GeometryBuilder gb = this.getGeometryBuilder();

        Vec4[] polyPoints = new Vec4[locations.size() + 1];
        Boolean[] polyEdgeFlags = new Boolean[locations.size() + 1];
        Matrix[] polyTransform = new Matrix[1];
        int polyCount = this.computeEllipsoidalPolygon(dc.getGlobe(), locations, edgeFlags, polyPoints, polyEdgeFlags,
            polyTransform);

        // Compute the winding order of the planar cartesian points. If the order is not counter-clockwise, then
        // reverse the locations and points ordering.
        int winding = gb.computePolygonWindingOrder2(0, polyCount, polyPoints);
        if (winding != GeometryBuilder.COUNTER_CLOCKWISE)
        {
            gb.reversePoints(0, polyCount, polyPoints);
            gb.reversePoints(0, polyCount, polyEdgeFlags);
        }

        float[] polyVertices = new float[3 * polyCount];
        this.makePolygonVertices(polyCount, polyPoints, polyVertices);

        int fillDrawMode = GL.GL_TRIANGLES;
        int outlineDrawMode = GL.GL_LINES;

        int fillIndexCount = 0;
        int outlineIndexCount = 0;
        int vertexCount = 0;

        GeometryBuilder.IndexedTriangleArray ita = null;

        fillIndexCount += this.getEdgeFillIndexCount(polyCount, subdivisions);
        outlineIndexCount += this.getEdgeOutlineIndexCount(polyCount, subdivisions, polyEdgeFlags);
        vertexCount += this.getEdgeVertexCount(polyCount, subdivisions);

        if (enableCaps)
        {
            ita = gb.tessellatePolygon2(0, polyCount, polyVertices);
            for (int i = 0; i < subdivisions; i++)
            {
                gb.subdivideIndexedTriangleArray(ita);
            }

            fillIndexCount += ita.getIndexCount();
            vertexCount += ita.getVertexCount();
            // Bottom cap isn't drawn if airspace is collapsed.
            if (!this.isAirspaceCollapsed())
            {
                fillIndexCount += ita.getIndexCount();
                vertexCount += ita.getVertexCount();
            }
        }

        int[] fillIndices = new int[fillIndexCount];
        int[] outlineIndices = new int[outlineIndexCount];
        float[] vertices = new float[3 * vertexCount];
        float[] normals = new float[3 * vertexCount];

        int fillIndexPos = 0;
        int outlineIndexPos = 0;
        int vertexPos = 0;

        this.makeEdge(dc, polyCount, polyVertices, polyEdgeFlags, altitudes, terrainConformant, subdivisions,
            GeometryBuilder.OUTSIDE, polyTransform[0], referenceCenter,
            fillIndexPos, fillIndices, outlineIndexPos, outlineIndices, vertexPos, vertices, normals);
        fillIndexPos += this.getEdgeFillIndexCount(polyCount, subdivisions);
        outlineIndexPos += this.getEdgeOutlineIndexCount(polyCount, subdivisions, polyEdgeFlags);
        vertexPos += this.getEdgeVertexCount(polyCount, subdivisions);

        if (enableCaps)
        {
            this.makeCap(dc, ita, altitudes[1], terrainConformant[1], GeometryBuilder.OUTSIDE, polyTransform[0],
                referenceCenter, fillIndexPos, fillIndices, vertexPos, vertices, normals);
            fillIndexPos += ita.getIndexCount();
            vertexPos += ita.getVertexCount();
            // Bottom cap isn't drawn if airspace is collapsed.
            if (!this.isAirspaceCollapsed())
            {
                this.makeCap(dc, ita, altitudes[0], terrainConformant[0], GeometryBuilder.INSIDE, polyTransform[0],
                    referenceCenter, fillIndexPos, fillIndices, vertexPos, vertices, normals);
                fillIndexPos += ita.getIndexCount();
                vertexPos += ita.getVertexCount();
            }
        }

        dest.getFillIndexGeometry().setElementData(fillDrawMode, fillIndexCount, fillIndices);
        dest.getOutlineIndexGeometry().setElementData(outlineDrawMode, outlineIndexCount, outlineIndices);
        dest.getVertexGeometry().setVertexData(vertexCount, vertices);
        dest.getVertexGeometry().setNormalData(vertexCount, normals);
    }

    protected void makeTessellatedLocations(Globe globe, int subdivisions, List<LatLon> locations,
        List<LatLon> tessellatedLocations)
    {
        ArrayList<Vec4> points = new ArrayList<Vec4>();
        for (LatLon ll : locations)
        {
            points.add(globe.computeEllipsoidalPointFromPosition(ll.latitude, ll.longitude, 0));
        }

        //noinspection StringEquality
        if (WWMath.computeWindingOrderOfLocations(locations) != AVKey.COUNTER_CLOCKWISE)
            Collections.reverse(locations);

        Vec4 centerPoint = Vec4.computeAveragePoint(points);
        Position centerPos = globe.computePositionFromEllipsoidalPoint(centerPoint);
        Vec4 surfaceNormal = globe.computeEllipsoidalNormalAtLocation(centerPos.latitude, centerPos.longitude);

        int numPoints = points.size();
        float[] coords = new float[3 * numPoints];
        for (int i = 0; i < numPoints; i++)
        {
            points.get(i).toFloatArray(coords, 3 * i, 3);
        }

        GeometryBuilder gb = new GeometryBuilder();
        GeometryBuilder.IndexedTriangleArray tessellatedPoints = gb.tessellatePolygon(0, numPoints, coords,
            surfaceNormal);

        for (int i = 0; i < subdivisions; i++)
        {
            gb.subdivideIndexedTriangleArray(tessellatedPoints);
        }

        for (int i = 0; i < tessellatedPoints.getVertexCount(); i++)
        {
            Vec4 v = Vec4.fromFloatArray(tessellatedPoints.getVertices(), 3 * i, 3);
            tessellatedLocations.add(globe.computePositionFromEllipsoidalPoint(v));
        }
    }

    //**************************************************************//
    //********************  Polygon Edge        ********************//
    //**************************************************************//

    private int getEdgeFillIndexCount(int count, int subdivisions)
    {
        return (count - 1) * this.getSectionFillIndexCount(subdivisions);
    }

    private int getEdgeOutlineIndexCount(int count, int subdivisions, Boolean[] edgeFlags)
    {
        int sum = 0;
        for (int i = 0; i < count - 1; i++)
        {
            sum += this.getSectionOutlineIndexCount(subdivisions, edgeFlags[i], edgeFlags[i + 1]);
        }

        return sum;
    }

    private int getEdgeVertexCount(int count, int subdivisions)
    {
        return (count - 1) * this.getSectionVertexCount(subdivisions);
    }

    private int getSectionFillIndexCount(int subdivisions)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        return 6 * (gb.getSubdivisionPointsVertexCount(subdivisions) - 1);
    }

    private int getSectionOutlineIndexCount(int subdivisions, boolean beginEdgeFlag, boolean endEdgeFlag)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        int count = 4 * (gb.getSubdivisionPointsVertexCount(subdivisions) - 1);
        if (beginEdgeFlag)
            count += 2;
        if (endEdgeFlag)
            count += 2;

        return count;
    }

    private int getSectionVertexCount(int subdivisions)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        return 2 * gb.getSubdivisionPointsVertexCount(subdivisions);
    }

    private void makeEdge(DrawContext dc, int count, float[] locations, Boolean[] edgeFlags,
        double[] altitudes, boolean[] terrainConformant,
        int subdivisions, int orientation,
        Matrix locationTransform,
        Vec4 referenceCenter,
        int fillIndexPos, int[] fillIndices,
        int outlineIndexPos, int[] outlineIndices,
        int vertexPos, float[] vertices, float[] normals)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int sectionFillIndexCount = this.getSectionFillIndexCount(subdivisions);
        int sectionVertexCount = this.getSectionVertexCount(subdivisions);

        for (int i = 0; i < count - 1; i++)
        {
            boolean beginEdgeFlag = edgeFlags[i];
            boolean endEdgeFlag = edgeFlags[i + 1];

            this.makeSectionFillIndices(subdivisions, vertexPos, fillIndexPos, fillIndices);
            this.makeSectionOutlineIndices(subdivisions, vertexPos, outlineIndexPos, outlineIndices,
                beginEdgeFlag, endEdgeFlag);
            this.makeSectionVertices(dc, i, locations, altitudes, terrainConformant, subdivisions,
                locationTransform, referenceCenter, vertexPos, vertices);
            gb.makeIndexedTriangleArrayNormals(fillIndexPos, sectionFillIndexCount, fillIndices,
                vertexPos, sectionVertexCount, vertices, normals);

            fillIndexPos += sectionFillIndexCount;
            outlineIndexPos += this.getSectionOutlineIndexCount(subdivisions, beginEdgeFlag, endEdgeFlag);
            vertexPos += sectionVertexCount;
        }
    }

    private void makeSectionFillIndices(int subdivisions, int vertexPos, int indexPos, int[] indices)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        int count = gb.getSubdivisionPointsVertexCount(subdivisions);

        int index = indexPos;
        int pos, nextPos;
        for (int i = 0; i < count - 1; i++)
        {
            pos = vertexPos + 2 * i;
            nextPos = vertexPos + 2 * (i + 1);
            indices[index++] = pos + 1;
            indices[index++] = pos;
            indices[index++] = nextPos + 1;
            indices[index++] = nextPos + 1;
            indices[index++] = pos;
            indices[index++] = nextPos;
        }
    }

    private void makeSectionOutlineIndices(int subdivisions, int vertexPos, int indexPos, int[] indices,
        boolean beginEdgeFlag, boolean endEdgeFlag)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        int count = gb.getSubdivisionPointsVertexCount(subdivisions);

        int index = indexPos;
        int pos, nextPos;

        if (beginEdgeFlag)
        {
            pos = vertexPos;
            indices[index++] = pos;
            indices[index++] = pos + 1;
        }

        for (int i = 0; i < count - 1; i++)
        {
            pos = vertexPos + 2 * i;
            nextPos = vertexPos + 2 * (i + 1);
            indices[index++] = pos;
            indices[index++] = nextPos;
            indices[index++] = pos + 1;
            indices[index++] = nextPos + 1;
        }

        if (endEdgeFlag)
        {
            pos = vertexPos + 2 * (count - 1);
            indices[index++] = pos;
            indices[index] = pos + 1;
        }
    }

    private void makeSectionVertices(DrawContext dc, int locationPos, float[] locations,
        double[] altitude, boolean[] terrainConformant,
        int subdivisions,
        Matrix locationTransform,
        Vec4 referenceCenter,
        int vertexPos, float[] vertices)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        int numPoints = gb.getSubdivisionPointsVertexCount(subdivisions);

        Globe globe = dc.getGlobe();
        int index1 = 3 * locationPos;
        int index2 = 3 * (locationPos + 1);

        float[] locationVerts = new float[3 * numPoints];
        gb.makeSubdivisionPoints(
            locations[index1], locations[index1 + 1], locations[index1 + 2],
            locations[index2], locations[index2 + 1], locations[index2 + 2],
            subdivisions, locationVerts);

        for (int i = 0; i < numPoints; i++)
        {
            int index = 3 * i;
            Vec4 vec = new Vec4(locationVerts[index], locationVerts[index + 1], locationVerts[index + 2]);
            vec = vec.transformBy4(locationTransform);
            Position pos = globe.computePositionFromEllipsoidalPoint(vec); // ellipsoidal-coordinate point and transform

            for (int j = 0; j < 2; j++)
            {
                vec = this.computePointFromPosition(dc, pos.getLatitude(), pos.getLongitude(), altitude[j],
                    terrainConformant[j]); // final model-coordinate point

                index = 2 * i + j;
                index = 3 * (vertexPos + index);
                vertices[index] = (float) (vec.x - referenceCenter.x);
                vertices[index + 1] = (float) (vec.y - referenceCenter.y);
                vertices[index + 2] = (float) (vec.z - referenceCenter.z);
            }
        }
    }

    //**************************************************************//
    //********************  Polygon Cap         ********************//
    //**************************************************************//

    private void makeCap(DrawContext dc, GeometryBuilder.IndexedTriangleArray ita,
        double altitude, boolean terrainConformant,
        int orientation,
        Matrix locationTransform,
        Vec4 referenceCenter,
        int indexPos, int[] indices,
        int vertexPos, float[] vertices, float[] normals)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        Globe globe = dc.getGlobe();

        int indexCount = ita.getIndexCount();
        int vertexCount = ita.getVertexCount();
        int[] locationIndices = ita.getIndices();
        float[] locationVerts = ita.getVertices();

        this.copyIndexArray(indexCount, (orientation == GeometryBuilder.INSIDE), locationIndices,
            vertexPos, indexPos, indices);

        for (int i = 0; i < vertexCount; i++)
        {
            int index = 3 * i;
            Vec4 vec = new Vec4(locationVerts[index], locationVerts[index + 1], locationVerts[index + 2]);
            vec = vec.transformBy4(locationTransform);

            Position pos = globe.computePositionFromEllipsoidalPoint(vec); // ellipsoidal-coordinate point and transform
            vec = this.computePointFromPosition(dc, pos.getLatitude(), pos.getLongitude(), altitude,
                terrainConformant); // final model-coordinate point

            index = 3 * (vertexPos + i);
            vertices[index] = (float) (vec.x - referenceCenter.x);
            vertices[index + 1] = (float) (vec.y - referenceCenter.y);
            vertices[index + 2] = (float) (vec.z - referenceCenter.z);
        }

        gb.makeIndexedTriangleArrayNormals(indexPos, indexCount, indices, vertexPos, vertexCount, vertices,
            normals);
    }

    private void copyIndexArray(int indexCount, boolean reverseWinding, int[] indices,
        int destVertexPos, int destIndexPos, int[] dest)
    {
        for (int i = 0; i < indexCount; i += 3)
        {
            if (reverseWinding)
            {
                dest[destIndexPos + i] = destVertexPos + indices[i + 2];
                dest[destIndexPos + i + 1] = destVertexPos + indices[i + 1];
                dest[destIndexPos + i + 2] = destVertexPos + indices[i];
            }
            else
            {
                dest[destIndexPos + i] = destVertexPos + indices[i];
                dest[destIndexPos + i + 1] = destVertexPos + indices[i + 1];
                dest[destIndexPos + i + 2] = destVertexPos + indices[i + 2];
            }
        }
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsBoolean(context, "enableCaps", this.enableCaps);

        if (this.locations != null)
            rs.addStateValueAsLatLonList(context, "locations", this.locations);
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Boolean booleanState = rs.getStateValueAsBoolean(context, "enableCaps");
        if (booleanState != null)
            this.setEnableCaps(booleanState);

        List<LatLon> locations = rs.getStateValueAsLatLonList(context, "locations");
        if (locations != null)
            this.setLocations(locations);
    }
}
