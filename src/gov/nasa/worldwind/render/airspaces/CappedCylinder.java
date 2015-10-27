/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.util.*;

/**
 * A cylinder defined by a geographic position, a radius in meters, and minimum and maximum altitudes.
 *
 * @author tag
 * @version $Id: CappedCylinder.java 2446 2014-11-20 21:15:11Z dcollins $
 */
public class CappedCylinder extends AbstractAirspace
{
    protected static final int DEFAULT_SLICES = 32;
    protected static final int DEFAULT_STACKS = 1;
    protected static final int DEFAULT_LOOPS = 8;
    protected static final int MINIMAL_GEOMETRY_SLICES = 8;
    protected static final int MINIMAL_GEOMETRY_LOOPS = 4;

    private LatLon center = LatLon.ZERO;
    private double innerRadius = 0.0;
    private double outerRadius = 1.0;
    private boolean enableCaps = true;
    // Geometry.
    private int slices = DEFAULT_SLICES;
    private final int stacks = DEFAULT_STACKS;
    private int loops = DEFAULT_LOOPS;

    public CappedCylinder(LatLon location, double radius)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius=" + radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = location;
        this.outerRadius = radius;
        this.makeDefaultDetailLevels();
    }

    public CappedCylinder(CappedCylinder source)
    {
        super(source);

        this.center = source.center;
        this.innerRadius = source.innerRadius;
        this.outerRadius = source.outerRadius;
        this.enableCaps = source.enableCaps;
        this.slices = source.slices;
        this.loops = source.loops;

        this.makeDefaultDetailLevels();
    }

    public CappedCylinder(AirspaceAttributes attributes)
    {
        super(attributes);
        this.makeDefaultDetailLevels();
    }

    public CappedCylinder()
    {
        this.makeDefaultDetailLevels();
    }

    private void makeDefaultDetailLevels()
    {
        List<DetailLevel> levels = new ArrayList<DetailLevel>();
        double[] ramp = ScreenSizeDetailLevel.computeDefaultScreenSizeRamp(5);

        DetailLevel level;
        level = new ScreenSizeDetailLevel(ramp[0], "Detail-Level-0");
        level.setValue(SLICES, 32);
        level.setValue(STACKS, 1);
        level.setValue(LOOPS, 8);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[1], "Detail-Level-1");
        level.setValue(SLICES, 26);
        level.setValue(STACKS, 1);
        level.setValue(LOOPS, 6);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[2], "Detail-Level-2");
        level.setValue(SLICES, 20);
        level.setValue(STACKS, 1);
        level.setValue(LOOPS, 4);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[3], "Detail-Level-3");
        level.setValue(SLICES, 14);
        level.setValue(STACKS, 1);
        level.setValue(LOOPS, 2);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[4], "Detail-Level-4");
        level.setValue(SLICES, 8);
        level.setValue(STACKS, 1);
        level.setValue(LOOPS, 1);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, true);
        levels.add(level);

        this.setDetailLevels(levels);
    }

    /**
     * Returns the geographic location of the cylinder's center.
     *
     * @return the cylinder's center
     */
    public LatLon getCenter()
    {
        return this.center;
    }

    /**
     * Sets the cylinder's center.
     *
     * @param location the geographic position (latitude and longitude) of the cylinder's center.
     *
     * @throws IllegalArgumentException if the location is null.
     */
    public void setCenter(LatLon location)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = location;
        this.invalidateAirspaceData();
    }

    /**
     * Returns the cylinder's inner and outer radius, in meters.
     *
     * @return the cylinder's inner and outer radius, in meters.
     */
    public double[] getRadii()
    {
        double[] array = new double[2];
        array[0] = this.innerRadius;
        array[1] = this.outerRadius;
        return array;
    }

    /**
     * Sets the cylinder's inner and outer radius.
     *
     * @param innerRadius the cylinder's inner radius, in meters.
     * @param outerRadius the cylinder's inner radius, in meters.
     *
     * @throws IllegalArgumentException if either radius is less than zero.
     */
    public void setRadii(double innerRadius, double outerRadius)
    {
        if (innerRadius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "innerRadius=" + innerRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (outerRadius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "outerRadius=" + outerRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.invalidateAirspaceData();
    }

    /**
     * Sets the cylinder's radius.
     *
     * @param radius the cylinder's radius, in meters.
     *
     * @throws IllegalArgumentException if the radius is less than zero.
     */
    public void setRadius(double radius)
    {
        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius=" + radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setRadii(0.0, radius);
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
        double[] altitudes = this.getAltitudes();
        return new Position(this.center, altitudes[0]);
    }

    protected Extent computeExtent(Globe globe, double verticalExaggeration)
    {
        List<Vec4> points = this.computeMinimalGeometry(globe, verticalExaggeration);
        if (points == null || points.isEmpty())
            return null;

        Vec4 centerPoint = globe.computePointFromLocation(this.getCenter());
        Vec4 cylinderAxis = globe.computeSurfaceNormalAtPoint(centerPoint);

        double minProj = Double.MAX_VALUE;
        double maxProj = -Double.MAX_VALUE;
        double maxPerp = -Double.MAX_VALUE;

        for (Vec4 vec : points)
        {
            Vec4 v = vec.subtract3(centerPoint);
            double proj = v.dot3(cylinderAxis);
            double perp = v.perpendicularTo3(cylinderAxis).getLengthSquared3();

            if (minProj > proj)
                minProj = proj;

            if (maxProj < proj)
                maxProj = proj;

            if (maxPerp < perp)
                maxPerp = perp;
        }

        if (minProj != maxProj && maxPerp > 0.0)
        {
            Vec4 bottomCenter = centerPoint.add3(cylinderAxis.multiply3(minProj));
            Vec4 topCenter = centerPoint.add3(cylinderAxis.multiply3(maxProj));
            double radius = Math.sqrt(maxPerp);
            return new Cylinder(bottomCenter, topCenter, radius);
        }
        else
        {
            return Box.computeBoundingBox(points);
        }
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        GeometryBuilder gb = new GeometryBuilder();
        LatLon[] locations = gb.makeDiskLocations(globe, this.center, this.innerRadius, this.outerRadius,
            MINIMAL_GEOMETRY_SLICES, MINIMAL_GEOMETRY_LOOPS);

        ArrayList<Vec4> points = new ArrayList<Vec4>();
        this.makeExtremePoints(globe, verticalExaggeration, Arrays.asList(locations), points);

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

        List<LatLon> oldLocations = new ArrayList<LatLon>(1);
        oldLocations.add(this.getCenter());
        List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldRef, newRef, oldLocations);
        this.setCenter(newLocations.get(0));

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

        LatLon center = this.getCenter();
        double distance = LatLon.greatCircleDistance(oldRef, center).radians;
        double azimuth = LatLon.greatCircleAzimuth(oldRef, center).radians;
        this.setCenter(LatLon.greatCircleEndPosition(newRef, azimuth, distance));
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
        GeometryBuilder gb = new GeometryBuilder();
        LatLon[] locations = gb.makeCylinderLocations(dc.getGlobe(), this.center, this.outerRadius, this.slices);
        ((SurfacePolygon) shape).getBoundaries().clear();
        ((SurfacePolygon) shape).setOuterBoundary(Arrays.asList(locations));

        if (this.innerRadius > 0)
        {
            locations = gb.makeCylinderLocations(dc.getGlobe(), this.center, this.innerRadius, this.slices);
            ((SurfacePolygon) shape).addInnerBoundary(Arrays.asList(locations));
        }
    }

    protected int getSlices()
    {
        return this.slices;
    }

    protected void setSlices(int slices)
    {
        if (slices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.slices = slices;
    }

    protected int getStacks()
    {
        return this.stacks;
    }

    protected int getLoops()
    {
        return this.loops;
    }

    protected void setLoops(int loops)
    {
        if (loops < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.loops = loops;
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    protected Vec4 computeReferenceCenter(DrawContext dc)
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

        double[] altitudes = this.getAltitudes(dc.getVerticalExaggeration());
        return dc.getGlobe().computePointFromPosition(this.center.getLatitude(), this.center.getLongitude(),
            altitudes[0]); // model-coordinate reference center
    }

    protected Matrix computeEllipsoidalTransform(Globe globe, double verticalExaggeration)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] altitudes = this.getAltitudes(verticalExaggeration);
        return globe.computeEllipsoidalOrientationAtPosition(this.center.latitude, this.center.longitude, altitudes[0]);
    }

    protected void doRenderGeometry(DrawContext dc, String drawStyle)
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

        LatLon center = this.getCenter();
        double[] altitudes = this.getAltitudes(dc.getVerticalExaggeration());
        boolean[] terrainConformant = this.isTerrainConforming();
        double[] radii = this.getRadii();
        int slices = this.slices;
        int stacks = this.stacks;
        int loops = this.loops;

        if (this.isEnableLevelOfDetail())
        {
            DetailLevel level = this.computeDetailLevel(dc);

            Object o = level.getValue(SLICES);
            if (o != null && o instanceof Integer)
                slices = (Integer) o;

            o = level.getValue(STACKS);
            if (o != null && o instanceof Integer)
                stacks = (Integer) o;

            o = level.getValue(LOOPS);
            if (o != null && o instanceof Integer)
                loops = (Integer) o;

            o = level.getValue(DISABLE_TERRAIN_CONFORMANCE);
            if (o != null && o instanceof Boolean && ((Boolean) o))
                terrainConformant[0] = terrainConformant[1] = false;
        }

        Vec4 refCenter = this.computeReferenceCenter(dc);
        this.setExpiryTime(this.nextExpiryTime(dc, terrainConformant));
        this.clearElevationMap();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        try
        {
            dc.getView().pushReferenceCenter(dc, refCenter);

            if (Airspace.DRAW_STYLE_OUTLINE.equals(drawStyle))
            {
                // Outer cylinder isn't rendered if outer radius is zero.
                if (radii[1] != 0.0)
                {
                    this.drawCylinderOutline(dc, center, radii[1], altitudes, terrainConformant, slices, stacks,
                        GeometryBuilder.OUTSIDE, refCenter);
                }
                // Inner cylinder isn't rendered if inner radius is zero.
                if (radii[0] != 0.0)
                {
                    this.drawCylinderOutline(dc, center, radii[0], altitudes, terrainConformant, slices, stacks,
                        GeometryBuilder.INSIDE, refCenter);
                }
            }
            else if (Airspace.DRAW_STYLE_FILL.equals(drawStyle))
            {
                if (this.enableCaps)
                {
                    ogsh.pushAttrib(gl, GL2.GL_POLYGON_BIT);
                    gl.glEnable(GL.GL_CULL_FACE);
                    gl.glFrontFace(GL.GL_CCW);
                }

                if (this.enableCaps)
                {
                    // Caps aren't rendered if radii are equal.
                    if (radii[0] != radii[1])
                    {
                        this.drawDisk(dc, center, radii, altitudes[1], terrainConformant[1], slices, loops,
                            GeometryBuilder.OUTSIDE, refCenter);
                        // Bottom cap isn't rendered if airspace is collapsed
                        if (!this.isAirspaceCollapsed())
                        {
                            this.drawDisk(dc, center, radii, altitudes[0], terrainConformant[0], slices, loops,
                                GeometryBuilder.INSIDE, refCenter);
                        }
                    }
                }

                // Cylinders aren't rendered if airspace is collapsed
                if (!this.isAirspaceCollapsed())
                {
                    // Outer cylinder isn't rendered if outer radius is zero.
                    if (radii[1] != 0.0)
                    {
                        this.drawCylinder(dc, center, radii[1], altitudes, terrainConformant, slices, stacks,
                            GeometryBuilder.OUTSIDE, refCenter);
                    }
                    // Inner cylinder isn't rendered if inner radius is zero.
                    if (radii[0] != 0.0)
                    {
                        this.drawCylinder(dc, center, radii[0], altitudes, terrainConformant, slices, stacks,
                            GeometryBuilder.INSIDE, refCenter);
                    }
                }
            }
        }
        finally
        {
            dc.getView().popReferenceCenter(dc);
            ogsh.pop(gl);
        }
    }

    //**************************************************************//
    //********************  Cylinder            ********************//
    //**************************************************************//

    private void drawCylinder(DrawContext dc, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, int orientation, Vec4 referenceCenter)
    {
        Geometry vertexGeom = this.createCylinderVertexGeometry(dc, center, radius, altitudes, terrainConformant,
            slices, stacks, orientation, referenceCenter);

        Object cacheKey = new Geometry.CacheKey(this.getClass(), "Cylinder.Indices", slices, stacks, orientation);
        Geometry indexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (indexGeom == null)
        {
            indexGeom = new Geometry();
            this.makeCylinderIndices(slices, stacks, orientation, indexGeom);
            this.getGeometryCache().add(cacheKey, indexGeom);
        }

        this.drawGeometry(dc, indexGeom, vertexGeom);
    }

    private void drawCylinderOutline(DrawContext dc, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, int orientation, Vec4 referenceCenter)
    {
        Geometry vertexGeom = this.createCylinderVertexGeometry(dc, center, radius, altitudes, terrainConformant,
            slices, stacks, orientation, referenceCenter);

        Object cacheKey = new Geometry.CacheKey(this.getClass(), "Cylinder.OutlineIndices", slices, stacks,
            orientation);
        Geometry outlineIndexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (outlineIndexGeom == null)
        {
            outlineIndexGeom = new Geometry();
            this.makeCylinderOutlineIndices(slices, stacks, orientation, outlineIndexGeom);
            this.getGeometryCache().add(cacheKey, outlineIndexGeom);
        }

        this.drawGeometry(dc, outlineIndexGeom, vertexGeom);
    }

    private Geometry createCylinderVertexGeometry(DrawContext dc, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, int orientation, Vec4 referenceCenter)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "Cylinder.Vertices", center, radius,
            altitudes[0], altitudes[1], terrainConformant[0], terrainConformant[1], slices, stacks, orientation,
            referenceCenter);
        Geometry vertexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (vertexGeom == null || this.isExpired(dc, vertexGeom))
        {
            if (vertexGeom == null)
                vertexGeom = new Geometry();
            this.makeCylinder(dc, center, radius, altitudes, terrainConformant, slices, stacks, orientation,
                referenceCenter, vertexGeom);
            this.updateExpiryCriteria(dc, vertexGeom);
            this.getGeometryCache().add(cacheKey, vertexGeom);
        }

        return vertexGeom;
    }

    private void makeCylinder(DrawContext dc, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, int orientation, Vec4 referenceCenter, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int count = gb.getCylinderVertexCount(slices, stacks);
        float[] verts = new float[3 * count];
        float[] norms = new float[3 * count];
        gb.makeCylinderVertices(dc.getTerrain(), center, radius, altitudes, terrainConformant, slices, stacks,
            referenceCenter, verts);
        gb.makeCylinderNormals(slices, stacks, norms);

        dest.setVertexData(count, verts);
        dest.setNormalData(count, norms);
    }

    private void makeCylinderIndices(int slices, int stacks, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getCylinderDrawMode();
        int count = gb.getCylinderIndexCount(slices, stacks);
        int[] indices = new int[count];
        gb.makeCylinderIndices(slices, stacks, indices);

        dest.setElementData(mode, count, indices);
    }

    private void makeCylinderOutlineIndices(int slices, int stacks, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getCylinderOutlineDrawMode();
        int count = gb.getCylinderOutlineIndexCount(slices, stacks);
        int[] indices = new int[count];
        gb.makeCylinderOutlineIndices(slices, stacks, indices);

        dest.setElementData(mode, count, indices);
    }

    //**************************************************************//
    //********************  Disk                ********************//
    //**************************************************************//

    private void drawDisk(DrawContext dc, LatLon center, double[] radii, double altitude, boolean terrainConformant,
        int slices, int loops, int orientation, Vec4 referenceCenter)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "Disk.Vertices",
            center, radii[0], radii[1], altitude, terrainConformant,
            slices, loops, orientation, referenceCenter);
        Geometry vertexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (vertexGeom == null || this.isExpired(dc, vertexGeom))
        {
            if (vertexGeom == null)
                vertexGeom = new Geometry();
            this.makeDisk(dc, center, radii, altitude, terrainConformant,
                slices, loops, orientation, referenceCenter, vertexGeom);
            this.updateExpiryCriteria(dc, vertexGeom);
            this.getGeometryCache().add(cacheKey, vertexGeom);
        }

        cacheKey = new Geometry.CacheKey(this.getClass(), "Disk.Indices", slices, loops, orientation);
        Geometry indexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (indexGeom == null)
        {
            indexGeom = new Geometry();
            this.makeDiskIndices(slices, loops, orientation, indexGeom);
            this.getGeometryCache().add(cacheKey, indexGeom);
        }

        this.drawGeometry(dc, indexGeom, vertexGeom);
    }

    private void makeDisk(DrawContext dc, LatLon center, double[] radii, double altitude, boolean terrainConformant,
        int slices, int loops, int orientation, Vec4 referenceCenter, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int count = gb.getDiskVertexCount(slices, loops);
        float[] verts = new float[3 * count];
        float[] norms = new float[3 * count];
        gb.makeDiskVertices(dc.getTerrain(), center, radii[0], radii[1], altitude, terrainConformant, slices, loops,
            referenceCenter, verts);
        gb.makeDiskVertexNormals((float) radii[0], (float) radii[1], slices, loops, verts, norms);

        dest.setVertexData(count, verts);
        dest.setNormalData(count, norms);
    }

    private void makeDiskIndices(int slices, int loops, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getCylinderDrawMode();
        int count = gb.getDiskIndexCount(slices, loops);
        int[] indices = new int[count];
        gb.makeDiskIndices(slices, loops, indices);

        dest.setElementData(mode, count, indices);
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsBoolean(context, "capsVisible", this.isEnableCaps());
        rs.addStateValueAsLatLon(context, "center", this.getCenter());
        rs.addStateValueAsDouble(context, "innerRadius", this.getRadii()[0]);
        rs.addStateValueAsDouble(context, "outerRadius", this.getRadii()[1]);
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Boolean booleanState = rs.getStateValueAsBoolean(context, "capsVisible");
        if (booleanState != null)
            this.setEnableCaps(booleanState);

        LatLon ll = rs.getStateValueAsLatLon(context, "center");
        if (ll != null)
            this.setCenter(ll);

        Double ir = rs.getStateValueAsDouble(context, "innerRadius");
        if (ir == null)
            ir = this.getRadii()[0];

        Double or = rs.getStateValueAsDouble(context, "outerRadius");
        if (or == null)
            or = this.getRadii()[1];

        this.setRadii(ir, or);
    }
}
