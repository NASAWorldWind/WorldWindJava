/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.util.*;

/**
 * A cylinder defined by a geographic position, a radius in meters, and minimum and maximum altitudes.
 *
 * @author tag
 * @version $Id: PartialCappedCylinder.java 2447 2014-11-20 21:19:17Z dcollins $
 */
public class PartialCappedCylinder extends CappedCylinder
{
    private Angle leftAzimuth = Angle.ZERO;
    private Angle rightAzimuth = Angle.POS360;

    public PartialCappedCylinder(LatLon location, double radius, Angle leftAzimuth, Angle rightAzimuth)
    {
        super(location, radius);

        if (leftAzimuth == null)
        {
            String message = "nullValue.LeftAzimuthIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (rightAzimuth == null)
        {
            String message = "nullValue.RightAzimuthIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.leftAzimuth = leftAzimuth;
        this.rightAzimuth = rightAzimuth;
    }

    public PartialCappedCylinder(LatLon location, double radius)
    {
        super(location, radius);
    }

    public PartialCappedCylinder(AirspaceAttributes attributes)
    {
        super(attributes);
    }

    public PartialCappedCylinder()
    {
    }

    public PartialCappedCylinder(PartialCappedCylinder source)
    {
        super(source);

        this.leftAzimuth = source.leftAzimuth;
        this.rightAzimuth = source.rightAzimuth;
    }

    public Angle[] getAzimuths()
    {
        Angle[] array = new Angle[2];
        array[0] = this.leftAzimuth;
        array[1] = this.rightAzimuth;
        return array;
    }

    public void setAzimuths(Angle leftAzimuth, Angle rightAzimuth)
    {
        if (leftAzimuth == null)
        {
            String message = "nullValue.LeftAzimuthIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (rightAzimuth == null)
        {
            String message = "nullValue.RightAzimuthIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.leftAzimuth = leftAzimuth;
        this.rightAzimuth = rightAzimuth;
        this.invalidateAirspaceData();
    }

    protected Box computeExtent(Globe globe, double verticalExaggeration)
    {
        List<Vec4> points = this.computeMinimalGeometry(globe, verticalExaggeration);
        if (points == null || points.isEmpty())
            return null;

        // A bounding box typically provides a better fit for a partial capped cylinder than a bounding cylinder.
        return Box.computeBoundingBox(points);
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        double[] angles = this.computeAngles();
        // Angles are equal, fall back to building a closed cylinder.
        if (angles == null)
            return super.computeMinimalGeometry(globe, verticalExaggeration);

        GeometryBuilder gb = this.getGeometryBuilder();
        LatLon[] locations = gb.makePartialDiskLocations(globe, this.getCenter(), this.getRadii()[0],
            this.getRadii()[1], MINIMAL_GEOMETRY_SLICES, MINIMAL_GEOMETRY_LOOPS, angles[0], angles[2]);

        ArrayList<Vec4> points = new ArrayList<Vec4>();
        this.makeExtremePoints(globe, verticalExaggeration, Arrays.asList(locations), points);

        return points;
    }

    @Override
    protected void regenerateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        double[] angles = this.computeAngles();
        if (angles == null) // angles are equal, fall back to drawing a closed cylinder
        {
            super.regenerateSurfaceShape(dc, shape);
            return;
        }

        double[] radii = this.getRadii();
        GeometryBuilder gb = this.getGeometryBuilder();

        ArrayList<LatLon> locations = new ArrayList<LatLon>();

        if (radii[0] > 0) // inner radius is > 0; add inner loop
        {
            List<LatLon> innerLoop = Arrays.asList(gb.makePartialCylinderLocations(dc.getGlobe(), this.getCenter(),
                this.getRadii()[0], this.getSlices(), angles[0], angles[2]));
            locations.addAll(innerLoop);
        }
        else // inner radius == 0
        {
            locations.add(this.getCenter());
        }

        List<LatLon> outerLoop = Arrays.asList(gb.makePartialCylinderLocations(dc.getGlobe(), this.getCenter(),
            this.getRadii()[1], this.getSlices(), angles[0], angles[2]));
        Collections.reverse(outerLoop);
        locations.addAll(outerLoop); // outer loop in reverse

        ((SurfacePolygon) shape).getBoundaries().clear();
        ((SurfacePolygon) shape).setOuterBoundary(locations);
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    protected double[] computeAngles()
    {
        // Compute the start and sweep angles such that the partial cylinder shape tranverses a clockwise path from
        // the start angle to the stop angle.
        Angle startAngle, stopAngle, sweepAngle;
        startAngle = normalizedAzimuth(this.leftAzimuth);
        stopAngle = normalizedAzimuth(this.rightAzimuth);

        int i = startAngle.compareTo(stopAngle);
        // Angles are equal, fallback to building a closed cylinder.
        if (i == 0)
            return null;

        if (i < 0)
            sweepAngle = stopAngle.subtract(startAngle);
        else // (i > 0)
            sweepAngle = Angle.POS360.subtract(startAngle).add(stopAngle);

        double[] array = new double[3];
        array[0] = startAngle.radians;
        array[1] = stopAngle.radians;
        array[2] = sweepAngle.radians;
        return array;
    }

    protected Angle normalizedAzimuth(Angle azimuth)
    {
        if (azimuth == null)
        {
            String message = "nullValue.AzimuthIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double degrees = azimuth.degrees;
        double normalizedDegrees = degrees < 0.0 ? degrees + 360.0 : (degrees >= 360.0 ? degrees - 360.0 : degrees);
        return Angle.fromDegrees(normalizedDegrees);
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

        double[] angles = this.computeAngles();
        // Angles are equal, fallback to drawing a closed cylinder.
        if (angles == null)
        {
            super.doRenderGeometry(dc, drawStyle);
            return;
        }

        LatLon center = this.getCenter();
        double[] altitudes = this.getAltitudes(dc.getVerticalExaggeration());
        boolean[] terrainConformant = this.isTerrainConforming();
        double[] radii = this.getRadii();
        int slices = this.getSlices();
        int stacks = this.getStacks();
        int loops = this.getLoops();

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

        Vec4 referenceCenter = this.computeReferenceCenter(dc);
        this.setExpiryTime(this.nextExpiryTime(dc, terrainConformant));
        this.clearElevationMap();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        try
        {
            dc.getView().pushReferenceCenter(dc, referenceCenter);

            if (Airspace.DRAW_STYLE_OUTLINE.equals(drawStyle))
            {
                this.drawRadialWallOutline(dc, center, radii, angles[0], altitudes, terrainConformant, loops, stacks,
                    GeometryBuilder.INSIDE, referenceCenter);
                this.drawRadialWallOutline(dc, center, radii, angles[1], altitudes, terrainConformant, loops, stacks,
                    GeometryBuilder.OUTSIDE, referenceCenter);

                // Outer cylinder isn't rendered if outer radius is zero.
                if (radii[1] != 0.0)
                {
                    this.drawPartialCylinderOutline(dc, center, radii[1], altitudes, terrainConformant, slices, stacks,
                        GeometryBuilder.OUTSIDE, angles[0], angles[2], referenceCenter);
                }
                // Inner cylinder isn't rendered if inner radius is zero.
                if (radii[0] != 0.0)
                {
                    this.drawPartialCylinderOutline(dc, center, radii[0], altitudes, terrainConformant, slices, stacks,
                        GeometryBuilder.INSIDE, angles[0], angles[2], referenceCenter);
                }
            }
            else if (Airspace.DRAW_STYLE_FILL.equals(drawStyle))
            {
                if (this.isEnableCaps())
                {
                    ogsh.pushAttrib(gl, GL2.GL_POLYGON_BIT);
                    gl.glEnable(GL.GL_CULL_FACE);
                    gl.glFrontFace(GL.GL_CCW);
                }

                if (this.isEnableCaps())
                {
                    // Caps aren't rendered if radii are equal.
                    if (radii[0] != radii[1])
                    {
                        this.drawPartialDisk(dc, center, radii, altitudes[1], terrainConformant[1], slices, loops,
                            GeometryBuilder.OUTSIDE, angles[0], angles[2], referenceCenter);
                        // Bottom cap isn't rendered if airspace is collapsed.
                        if (!this.isAirspaceCollapsed())
                        {
                            this.drawPartialDisk(dc, center, radii, altitudes[0], terrainConformant[0], slices, loops,
                                GeometryBuilder.INSIDE, angles[0], angles[2], referenceCenter);
                        }
                    }
                }

                // Cylinders aren't rendered if airspace is collapsed.
                if (!this.isAirspaceCollapsed())
                {
                    this.drawRadialWall(dc, center, radii, angles[0], altitudes, terrainConformant, loops, stacks,
                        GeometryBuilder.INSIDE, referenceCenter);
                    this.drawRadialWall(dc, center, radii, angles[1], altitudes, terrainConformant, loops, stacks,
                        GeometryBuilder.OUTSIDE, referenceCenter);

                    // Outer cylinder isn't rendered if outer radius is zero.
                    if (radii[1] != 0.0)
                    {
                        this.drawPartialCylinder(dc, center, radii[1], altitudes, terrainConformant, slices, stacks,
                            GeometryBuilder.OUTSIDE, angles[0], angles[2], referenceCenter);
                    }
                    // Inner cylinder isn't rendered if inner radius is zero.
                    if (radii[0] != 0.0)
                    {
                        this.drawPartialCylinder(dc, center, radii[0], altitudes, terrainConformant, slices, stacks,
                            GeometryBuilder.INSIDE, angles[0], angles[2], referenceCenter);
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
    //********************  Partial Cylinder    ********************//
    //**************************************************************//

    private void drawPartialCylinder(DrawContext dc, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, int orientation, double start, double sweep,
        Vec4 referenceCenter)
    {
        Geometry vertexGeom = this.createPartialCylinderVertexGeometry(dc, center, radius, altitudes, terrainConformant,
            slices, stacks, orientation, start, sweep, referenceCenter);

        Object cacheKey = new Geometry.CacheKey(this.getClass(), "PartialCylinder.Indices", slices, stacks,
            orientation);
        Geometry indexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (indexGeom == null)
        {
            indexGeom = new Geometry();
            this.makePartialCylinderIndices(slices, stacks, orientation, indexGeom);
            this.getGeometryCache().add(cacheKey, indexGeom);
        }

        this.drawGeometry(dc, indexGeom, vertexGeom);
    }

    private void drawPartialCylinderOutline(DrawContext dc, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, int orientation, double start, double sweep,
        Vec4 referenceCenter)
    {
        Geometry vertexGeom = this.createPartialCylinderVertexGeometry(dc, center, radius, altitudes, terrainConformant,
            slices, stacks, orientation, start, sweep, referenceCenter);

        Object cacheKey = new Geometry.CacheKey(this.getClass(), "PartialCylinder.OutlineIndices", slices, stacks,
            orientation);
        Geometry outlineIndexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (outlineIndexGeom == null)
        {
            outlineIndexGeom = new Geometry();
            this.makePartialCylinderOutlineIndices(slices, stacks, orientation, outlineIndexGeom);
            this.getGeometryCache().add(cacheKey, outlineIndexGeom);
        }

        this.drawGeometry(dc, outlineIndexGeom, vertexGeom);
    }

    private Geometry createPartialCylinderVertexGeometry(DrawContext dc, LatLon center, double radius,
        double[] altitudes, boolean[] terrainConformant, int slices, int stacks, int orientation, double start,
        double sweep, Vec4 referenceCenter)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "PartialCylinder.Vertices", center,
            radius, altitudes[0], altitudes[1], terrainConformant[0], terrainConformant[1], slices, stacks, orientation,
            start, sweep, referenceCenter);
        Geometry vertexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (vertexGeom == null || this.isExpired(dc, vertexGeom))
        {
            if (vertexGeom == null)
                vertexGeom = new Geometry();
            this.makePartialCylinder(dc, center, radius, altitudes, terrainConformant, slices, stacks, orientation,
                start, sweep, referenceCenter, vertexGeom);
            this.updateExpiryCriteria(dc, vertexGeom);
            this.getGeometryCache().add(cacheKey, vertexGeom);
        }

        return vertexGeom;
    }

    private void makePartialCylinder(DrawContext dc, LatLon center, double radius, double[] altitudes,
        boolean[] terrainConformant, int slices, int stacks, int orientation, double start, double sweep,
        Vec4 referenceCenter, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);
        float height = (float) (altitudes[1] - altitudes[0]);

        int count = gb.getPartialCylinderVertexCount(slices, stacks);
        float[] verts = new float[3 * count];
        float[] norms = new float[3 * count];
        gb.makePartialCylinderVertices(dc.getTerrain(), center, radius, altitudes, terrainConformant, slices, stacks,
            start, sweep, referenceCenter, verts);
        gb.makePartialCylinderNormals((float) radius, height, slices, stacks, (float) start, (float) sweep, norms);

        dest.setVertexData(count, verts);
        dest.setNormalData(count, norms);
    }

    private void makePartialCylinderIndices(int slices, int stacks, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getPartialCylinderDrawMode();
        int count = gb.getPartialCylinderIndexCount(slices, stacks);
        int[] indices = new int[count];
        gb.makePartialCylinderIndices(slices, stacks, indices);

        dest.setElementData(mode, count, indices);
    }

    private void makePartialCylinderOutlineIndices(int slices, int stacks, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getPartialCylinderOutlineDrawMode();
        int count = gb.getPartialCylinderOutlineIndexCount(slices, stacks);
        int[] indices = new int[count];
        gb.makePartialCylinderOutlineIndices(slices, stacks, indices);

        dest.setElementData(mode, count, indices);
    }

    //**************************************************************//
    //********************  Partial Disk        ********************//
    //**************************************************************//

    private void drawPartialDisk(DrawContext dc, LatLon center, double[] radii, double altitude,
        boolean terrainConformant, int slices, int loops, int orientation, double start, double sweep,
        Vec4 referenceCenter)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "PartialDisk.Vertices", center,
            radii[0], radii[1], altitude, terrainConformant, slices, loops, orientation, start, sweep, referenceCenter);
        Geometry vertexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (vertexGeom == null || this.isExpired(dc, vertexGeom))
        {
            if (vertexGeom == null)
                vertexGeom = new Geometry();
            this.makePartialDisk(dc, center, radii, altitude, terrainConformant, slices, loops, orientation, start,
                sweep, referenceCenter, vertexGeom);
            this.updateExpiryCriteria(dc, vertexGeom);
            this.getGeometryCache().add(cacheKey, vertexGeom);
        }

        cacheKey = new Geometry.CacheKey(this.getClass(), "PartialDisk.Indices", slices, loops, orientation);
        Geometry indexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (indexGeom == null)
        {
            indexGeom = new Geometry();
            this.makePartialDiskIndices(slices, loops, orientation, indexGeom);
            this.getGeometryCache().add(cacheKey, indexGeom);
        }

        this.drawGeometry(dc, indexGeom, vertexGeom);
    }

    private void makePartialDisk(DrawContext dc, LatLon center, double[] radii, double altitude,
        boolean terrainConformant, int slices, int loops, int orientation, double start, double sweep,
        Vec4 referenceCenter, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int count = gb.getPartialDiskIndexCount(slices, loops);
        float[] verts = new float[3 * count];
        float[] norms = new float[3 * count];
        gb.makePartialDiskVertices(dc.getTerrain(), center, radii[0], radii[1], altitude, terrainConformant, slices,
            loops, start, sweep, referenceCenter, verts);
        gb.makePartialDiskVertexNormals((float) radii[0], (float) radii[1], slices, loops, (float) start, (float) sweep,
            verts, norms);

        dest.setVertexData(count, verts);
        dest.setNormalData(count, norms);
    }

    private void makePartialDiskIndices(int slices, int loops, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getPartialDiskDrawMode();
        int count = gb.getPartialDiskIndexCount(slices, loops);
        int[] indices = new int[count];
        gb.makePartialDiskIndices(slices, loops, indices);

        dest.setElementData(mode, count, indices);
    }

    //**************************************************************//
    //********************  Radial Wall         ********************//
    //**************************************************************//

    private void drawRadialWall(DrawContext dc, LatLon center, double[] radii, double angle, double[] altitudes,
        boolean[] terrainConformant, int pillars, int stacks, int orientation, Vec4 referenceCenter)
    {
        Geometry vertexGeom = this.createRadialWallVertexGeometry(dc, center, radii, angle, altitudes,
            terrainConformant, pillars, stacks, orientation, referenceCenter);

        Object cacheKey = new Geometry.CacheKey(this.getClass(), "RadialWall.Indices", pillars, stacks, orientation);
        Geometry indexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (indexGeom == null)
        {
            indexGeom = new Geometry();
            this.makeRadialWallIndices(pillars, stacks, orientation, indexGeom);
            this.getGeometryCache().add(cacheKey, indexGeom);
        }

        this.drawGeometry(dc, indexGeom, vertexGeom);
    }

    private void drawRadialWallOutline(DrawContext dc, LatLon center, double[] radii, double angle, double[] altitudes,
        boolean[] terrainConformant, int pillars, int stacks, int orientation, Vec4 referenceCenter)
    {
        Geometry vertexGeom = this.createRadialWallVertexGeometry(dc, center, radii, angle, altitudes,
            terrainConformant, pillars, stacks, orientation, referenceCenter);

        Object cacheKey = new Geometry.CacheKey(this.getClass(), "RadialWall.OutlineIndices", pillars, stacks,
            orientation);
        Geometry outlineIndexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (outlineIndexGeom == null)
        {
            outlineIndexGeom = new Geometry();
            this.makeRadialWallOutlineIndices(pillars, stacks, orientation, outlineIndexGeom);
            this.getGeometryCache().add(cacheKey, outlineIndexGeom);
        }

        this.drawGeometry(dc, outlineIndexGeom, vertexGeom);
    }

    private Geometry createRadialWallVertexGeometry(DrawContext dc, LatLon center, double[] radii, double angle,
        double[] altitudes, boolean[] terrainConformant, int pillars, int stacks, int orientation, Vec4 referenceCenter)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "RadialWall.Vertices", center, radii[0],
            radii[1], angle, altitudes[0], altitudes[1], terrainConformant[0], terrainConformant[1], pillars, stacks,
            orientation, referenceCenter);
        Geometry vertexGeom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (vertexGeom == null || this.isExpired(dc, vertexGeom))
        {
            if (vertexGeom == null)
                vertexGeom = new Geometry();
            this.makeRadialWall(dc, center, radii, angle, altitudes, terrainConformant, pillars, stacks, orientation,
                referenceCenter, vertexGeom);
            this.updateExpiryCriteria(dc, vertexGeom);
            this.getGeometryCache().add(cacheKey, vertexGeom);
        }

        return vertexGeom;
    }

    private void makeRadialWall(DrawContext dc, LatLon center, double[] radii, double angle, double[] altitudes,
        boolean[] terrainConformant, int pillars, int stacks, int orientation, Vec4 referenceCenter, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);
        float height = (float) (altitudes[1] - altitudes[0]);

        int count = gb.getRadialWallVertexCount(pillars, stacks);
        float[] verts = new float[3 * count];
        float[] norms = new float[3 * count];
        gb.makeRadialWallVertices(dc.getTerrain(), center, radii[0], radii[1], angle, altitudes, terrainConformant,
            pillars, stacks, referenceCenter, verts);
        gb.makeRadialWallNormals((float) radii[0], (float) radii[1], height, (float) angle, pillars, stacks, norms);

        dest.setVertexData(count, verts);
        dest.setNormalData(count, norms);
    }

    private void makeRadialWallIndices(int pillars, int stacks, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getRadialWallDrawMode();
        int count = gb.getRadialWallIndexCount(pillars, stacks);
        int[] indices = new int[count];
        gb.makeRadialWallIndices(pillars, stacks, indices);

        dest.setElementData(mode, count, indices);
    }

    private void makeRadialWallOutlineIndices(int pillars, int stacks, int orientation, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(orientation);

        int mode = gb.getRadialWallOutlineDrawMode();
        int count = gb.getRadialWallOutlineIndexCount(pillars, stacks);
        int[] indices = new int[count];
        gb.makeRadialWallOutlineIndices(pillars, stacks, indices);

        dest.setElementData(mode, count, indices);
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsDouble(context, "leftAzimuthDegrees", this.leftAzimuth.degrees);
        rs.addStateValueAsDouble(context, "rightAzimuthDegrees", this.rightAzimuth.degrees);
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Double la = rs.getStateValueAsDouble(context, "leftAzimuthDegrees");
        if (la == null)
            la = this.leftAzimuth.degrees;

        Double ra = rs.getStateValueAsDouble(context, "rightAzimuthDegrees");
        if (ra == null)
            ra = this.rightAzimuth.degrees;

        this.setAzimuths(Angle.fromDegrees(la), Angle.fromDegrees(ra));
    }
}
