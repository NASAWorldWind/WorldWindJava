/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.util.*;

/**
 * A spherical airspace shape defined by a center location and a radius. The sphere's center altitude and terrain
 * conformance attributes are taken from the lower altitude and lower terrain conformance. When terrain conformance is
 * disabled, the sphere's altitude behaves as a height above mean sea level. When terrain conformance is enabled, the
 * sphere's altitude will behave as a height offset above the terrain. Unlike other airspace shapes, the sphere's
 * geometry will not morph to the terrain beneath it.
 *
 * @author dcollins
 * @version $Id: SphereAirspace.java 2308 2014-09-16 19:27:22Z tgaskins $
 */
public class SphereAirspace extends AbstractAirspace
{
    protected static final int DEFAULT_SUBDIVISIONS = 3;

    private LatLon location = LatLon.ZERO;
    private double radius = 1.0;
    // Geometry.
    private int subdivisions = DEFAULT_SUBDIVISIONS;

    public SphereAirspace(LatLon location, double radius)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.location = location;
        this.radius = radius;
        this.makeDefaultDetailLevels();
    }

    public SphereAirspace(AirspaceAttributes attributes)
    {
        super(attributes);
        this.makeDefaultDetailLevels();
    }

    public SphereAirspace()
    {
        this.makeDefaultDetailLevels();
    }

    public SphereAirspace(SphereAirspace source)
    {
        super(source);

        this.location = source.location;
        this.radius = source.radius;
        this.subdivisions = source.subdivisions;

        this.makeDefaultDetailLevels();
    }

    private void makeDefaultDetailLevels()
    {
        List<DetailLevel> levels = new ArrayList<DetailLevel>();
        double[] ramp = ScreenSizeDetailLevel.computeLinearScreenSizeRamp(7, 10.0, 600.0);

        DetailLevel level;
        level = new ScreenSizeDetailLevel(ramp[0], "Detail-Level-0");
        level.setValue(SUBDIVISIONS, 6);
        //level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[1], "Detail-Level-1");
        level.setValue(SUBDIVISIONS, 5);
        //level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[2], "Detail-Level-2");
        level.setValue(SUBDIVISIONS, 4);
        //level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[3], "Detail-Level-3");
        level.setValue(SUBDIVISIONS, 3);
        //level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[4], "Detail-Level-4");
        level.setValue(SUBDIVISIONS, 2);
        //level.setValue(DISABLE_TERRAIN_CONFORMANCE, true);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[5], "Detail-Level-5");
        level.setValue(SUBDIVISIONS, 1);
        //level.setValue(DISABLE_TERRAIN_CONFORMANCE, true);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[6], "Detail-Level-6");
        level.setValue(SUBDIVISIONS, 0);
        //level.setValue(DISABLE_TERRAIN_CONFORMANCE, true);
        levels.add(level);

        this.setDetailLevels(levels);
    }

    /**
     * Returns the center location of the sphere.
     *
     * @return location of the sphere.
     */
    public LatLon getLocation()
    {
        return this.location;
    }

    /**
     * Sets the center location of the sphere.
     *
     * @param location the location of the sphere.
     *
     * @throws IllegalArgumentException if <code>location</code> is null
     */
    public void setLocation(LatLon location)
    {
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.location = location;
        this.invalidateAirspaceData();
    }

    /**
     * Returns the radius of the sphere in meters.
     *
     * @return radius of the sphere in meters.
     */
    public double getRadius()
    {
        return this.radius;
    }

    /**
     * Sets the radius of the sphere in meters. This will also set the altitude limits to match the new radius and
     * center elevation.
     *
     * @param radius the radius of the sphere.
     *
     * @throws IllegalArgumentException if <code>radius</code> is less than zero
     */
    public void setRadius(double radius)
    {
        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.radius = radius;
        this.invalidateAirspaceData();
    }

    public Position getReferencePosition()
    {
        double[] altitudes = this.getAltitudes();
        return new Position(this.location, altitudes[0]);
    }

    protected Extent computeExtent(Globe globe, double verticalExaggeration)
    {
        double altitude = this.getAltitudes(verticalExaggeration)[0];
        boolean terrainConformant = this.isTerrainConforming()[0];
        double radius = this.getRadius();

        if (terrainConformant)
        {
            double[] extremes = globe.getMinAndMaxElevations(this.location.getLatitude(), this.location.getLongitude());
            double minAltitude = verticalExaggeration * extremes[0] + altitude - radius;
            double maxAltitude = verticalExaggeration * extremes[1] + altitude + radius;
            Vec4 bottomCenter = globe.computePointFromPosition(this.location, minAltitude);
            Vec4 topCenter = globe.computePointFromPosition(this.location, maxAltitude);
            return new Cylinder(bottomCenter, topCenter, radius);
        }
        else
        {
            Vec4 centerPoint = globe.computePointFromPosition(this.location, altitude);
            return new Sphere(centerPoint, radius);
        }
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        return null; // Sphere has no need for a minimal geometry.
    }

    /**
     * Returns this SphereAirspace's {@link gov.nasa.worldwind.geom.Extent} for the specified DrawContext. This
     * overrides {@link gov.nasa.worldwind.render.airspaces.AbstractAirspace#getExtent(gov.nasa.worldwind.render.DrawContext)}
     * in order to bypass the superclass' extent caching. Unlike other Airspace's Extents, SphereAirspace's Extent is a
     * perfect fitting {@link gov.nasa.worldwind.geom.Sphere}, who's center point depends on the current surface
     * geometry. For this reason SphereAirspace's exact bounding volume can be easily computed, and should not be
     * cached.
     *
     * @param dc the current DrawContext.
     *
     * @return this SphereAirspace's Extent in model coordinates.
     *
     * @throws IllegalArgumentException if the DrawContext is null, or if the Globe held by the DrawContext is null.
     */
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

        return this.computeExtent(dc);
    }

    protected Sphere computeExtent(DrawContext dc)
    {
        double altitude = this.getAltitudes(dc.getVerticalExaggeration())[0];
        boolean terrainConformant = this.isTerrainConforming()[0];
        double radius = this.getRadius();

        this.clearElevationMap();

        Vec4 centerPoint = this.computePointFromPosition(dc, this.location.getLatitude(), this.location.getLongitude(),
            altitude, terrainConformant);
        return new Sphere(centerPoint, radius);
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

        List<LatLon> locations = new ArrayList<LatLon>(1);
        locations.add(this.getLocation());
        List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldRef, newRef, locations);
        this.setLocation(newLocations.get(0));

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

        this.setLocation(newRef);
    }

    protected double computeEyeDistance(DrawContext dc)
    {
        Sphere sphere = this.computeExtent(dc);
        Vec4 eyePoint = dc.getView().getEyePoint();

        // Return the distance to the sphere's edge. This provides a good distance for rendering order when the the eye
        // point is inside and outside of the sphere.
        double distance = sphere.getCenter().distanceTo3(eyePoint);
        return Math.abs(distance - sphere.getRadius());
    }

    @Override
    protected SurfaceShape createSurfaceShape()
    {
        return new SurfaceCircle();
    }

    @Override
    protected void updateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        super.updateSurfaceShape(dc, shape);

        shape.getAttributes().setDrawOutline(false); // suppress the surface shape's outline
    }

    @Override
    protected void regenerateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        ((SurfaceCircle) shape).setCenter(this.location);
        ((SurfaceCircle) shape).setRadius(this.radius);
    }

    protected int getSubdivisions()
    {
        return this.subdivisions;
    }

    protected void setSubdivisions(int subdivisions)
    {
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.subdivisions = subdivisions;
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

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

        this.clearElevationMap();

        if (Airspace.DRAW_STYLE_FILL.equals(drawStyle))
        {
            this.drawSphere(dc);
        }
        else if (Airspace.DRAW_STYLE_OUTLINE.equals(drawStyle))
        {
            // Sphere airspaces do not display an outline.
        }
    }

    protected void drawSphere(DrawContext dc)
    {
        double[] altitudes = this.getAltitudes(dc.getVerticalExaggeration());
        boolean[] terrainConformant = this.isTerrainConforming();
        int subdivisions = this.getSubdivisions();

        if (this.isEnableLevelOfDetail())
        {
            DetailLevel level = this.computeDetailLevel(dc);

            Object o = level.getValue(SUBDIVISIONS);
            if (o != null && o instanceof Integer)
                subdivisions = (Integer) o;

            //o = level.getValue(DISABLE_TERRAIN_CONFORMANCE);
            //if (o != null && o instanceof Boolean && (Boolean) o)
            //    terrainConformant[0] = terrainConformant[1] = false;
        }

        Vec4 centerPoint = this.computePointFromPosition(dc,
            this.location.getLatitude(), this.location.getLongitude(), altitudes[0], terrainConformant[0]);

        Matrix modelview = dc.getView().getModelviewMatrix();
        modelview = modelview.multiply(Matrix.fromTranslation(centerPoint));
        modelview = modelview.multiply(Matrix.fromScale(this.getRadius()));
        double[] matrixArray = new double[16];
        modelview.toArray(matrixArray, 0, false);

        this.setExpiryTime(-1L); // Sphere geometry never expires.

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glPushAttrib(GL2.GL_POLYGON_BIT | GL2.GL_TRANSFORM_BIT);
        try
        {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glFrontFace(GL.GL_CCW);

            // Were applying a scale transform on the modelview matrix, so the normal vectors must be re-normalized
            // before lighting is computed. In this case we're scaling by a constant factor, so GL_RESCALE_NORMAL
            // is sufficient and potentially less expensive than GL_NORMALIZE (or computing unique normal vectors
            // for each value of radius). GL_RESCALE_NORMAL was introduced in OpenGL version 1.2.
            gl.glEnable(GL2.GL_RESCALE_NORMAL);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            try
            {
                gl.glLoadMatrixd(matrixArray, 0);
                this.drawUnitSphere(dc, subdivisions);
            }
            finally
            {
                gl.glPopMatrix();
            }
        }
        finally
        {
            gl.glPopAttrib();
        }
    }

    protected void drawUnitSphere(DrawContext dc, int subdivisions)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "Sphere", subdivisions);
        Geometry geom = (Geometry) this.getGeometryCache().getObject(cacheKey);
        if (geom == null || this.isExpired(dc, geom))
        {
            if (geom == null)
                geom = new Geometry();
            this.makeSphere(1.0, subdivisions, geom);
            this.updateExpiryCriteria(dc, geom);
            this.getGeometryCache().add(cacheKey, geom);
        }

        this.drawGeometry(dc, geom, geom);
    }

    protected void makeSphere(double radius, int subdivisions, Geometry dest)
    {
        GeometryBuilder gb = this.getGeometryBuilder();
        gb.setOrientation(GeometryBuilder.OUTSIDE);

        GeometryBuilder.IndexedTriangleArray ita = gb.tessellateSphere((float) radius, subdivisions);
        float[] normalArray = new float[3 * ita.getVertexCount()];
        gb.makeIndexedTriangleArrayNormals(ita, normalArray);

        dest.setElementData(GL.GL_TRIANGLES, ita.getIndexCount(), ita.getIndices());
        dest.setVertexData(ita.getVertexCount(), ita.getVertices());
        dest.setNormalData(ita.getVertexCount(), normalArray);
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsLatLon(context, "location", this.getLocation());
        rs.addStateValueAsDouble(context, "radius", this.getRadius());
        rs.addStateValueAsInteger(context, "subdivisions", this.getSubdivisions());
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        LatLon ll = rs.getStateValueAsLatLon(context, "location");
        if (ll != null)
            this.setLocation(ll);

        Double d = rs.getStateValueAsDouble(context, "radius");
        if (d != null)
            this.setRadius(d);

        Integer i = rs.getStateValueAsInteger(context, "subdivisions");
        if (i != null)
            this.setSubdivisions(i);
    }
}
