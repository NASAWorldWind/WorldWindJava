/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * @author tag
 * @version $Id: PolyArc.java 2450 2014-11-20 21:41:54Z dcollins $
 */
public class PolyArc extends Polygon
{
    protected static final int DEFAULT_SLICES = 32;
    protected static final int MINIMAL_GEOMETRY_SLICES = 8;

    private double radius = 1.0;
    private Angle leftAzimuth = Angle.ZERO;
    private Angle rightAzimuth = Angle.ZERO;
    // Geometry.
    private int slices = DEFAULT_SLICES;
    private ArrayList<LatLon> polyArcLocations = new ArrayList<LatLon>();
    private ArrayList<Boolean> edgeFlags = new ArrayList<Boolean>();

    public PolyArc(List<? extends LatLon> locations, double radius, Angle leftAzimuth, Angle rightAzimuth)
    {
        super(locations);

        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius=" + radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
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

        this.radius = radius;
        this.leftAzimuth = leftAzimuth;
        this.rightAzimuth = rightAzimuth;
        this.makeDefaultDetailLevels();
    }

    public PolyArc(List<? extends LatLon> locations)
    {
        super(locations);
        this.makeDefaultDetailLevels();
    }

    public PolyArc(AirspaceAttributes attributes)
    {
        super(attributes);
        this.makeDefaultDetailLevels();
    }

    public PolyArc()
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
        level.setValue(SUBDIVISIONS, 3);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[1], "Detail-Level-1");
        level.setValue(SLICES, 26);
        level.setValue(SUBDIVISIONS, 3);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[2], "Detail-Level-2");
        level.setValue(SLICES, 20);
        level.setValue(SUBDIVISIONS, 2);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[3], "Detail-Level-3");
        level.setValue(SLICES, 14);
        level.setValue(SUBDIVISIONS, 1);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[4], "Detail-Level-4");
        level.setValue(SLICES, 8);
        level.setValue(SUBDIVISIONS, 0);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, true);
        levels.add(level);

        this.setDetailLevels(levels);
    }

    public double getRadius()
    {
        return this.radius;
    }

    public void setRadius(double radius)
    {
        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius=" + radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.radius = radius;
        this.invalidateAirspaceData();
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

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        List<LatLon> locations = this.getLocationList();
        if (locations == null || locations.isEmpty())
            return null;

        ArrayList<LatLon> arcLocations = new ArrayList<LatLon>();
        ArrayList<Boolean> arcFlags = new ArrayList<Boolean>();
        this.makePolyArcLocations(globe, locations, 8, arcLocations, arcFlags);

        ArrayList<LatLon> tessellatedLocations = new ArrayList<LatLon>();
        this.makeTessellatedLocations(globe, MINIMAL_GEOMETRY_SUBDIVISIONS, arcLocations, tessellatedLocations);

        ArrayList<Vec4> points = new ArrayList<Vec4>();
        this.makeExtremePoints(globe, verticalExaggeration, tessellatedLocations, points);

        return points;
    }

    @Override
    protected void regenerateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        ArrayList<LatLon> arcLocations = new ArrayList<LatLon>();
        ArrayList<Boolean> arcFlags = new ArrayList<Boolean>();
        this.makePolyArcLocations(dc.getGlobe(), this.getLocationList(), this.slices, arcLocations, arcFlags);

        ((SurfacePolygon) shape).setOuterBoundary(arcLocations);
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    protected double[] computeAngles()
    {
        Angle startAngle = this.normalizedAzimuth(this.leftAzimuth);
        Angle stopAngle = this.normalizedAzimuth(this.rightAzimuth);
        Angle sweepAngle;
        if (startAngle.compareTo(stopAngle) <= 0)
            sweepAngle = stopAngle.subtract(startAngle);
        else
            sweepAngle = Angle.POS360.subtract(startAngle).add(stopAngle);

        double[] array = new double[3];
        array[0] = startAngle.radians;
        array[1] = stopAngle.radians;
        array[2] = sweepAngle.radians;
        return array;
    }

    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        int slices = this.slices;

        if (this.isEnableLevelOfDetail())
        {
            DetailLevel level = this.computeDetailLevel(dc);

            Object o = level.getValue(SLICES);
            if (o != null && o instanceof Integer)
                slices = (Integer) o;
        }

        this.polyArcLocations.clear();
        this.edgeFlags.clear();
        this.makePolyArcLocations(dc.getGlobe(), this.getLocationList(), slices, this.polyArcLocations, this.edgeFlags);

        this.doRenderGeometry(dc, drawStyle, this.polyArcLocations, this.edgeFlags);
    }

    private void makePolyArcLocations(Globe globe, List<? extends LatLon> locations, int slices,
        List<LatLon> polyArcLocations, List<Boolean> edgeFlags)
    {
        int locationCount = locations.size();
        if (locationCount > 0)
        {
            // Create arc locations. These are guaranteed to be in clockwise order about the first location.
            double[] angles = this.computeAngles();
            double radius = this.radius;
            LatLon first = locations.get(0);
            LatLon[] arcLocations = this.makeArc(globe, first, radius, slices, angles[0], angles[2]);

            for (LatLon ll : arcLocations)
            {
                polyArcLocations.add(ll);
                edgeFlags.add(false);
            }

            // Enable edge flags for the first and last poly arc locations.
            if (edgeFlags.size() > 1)
            {
                edgeFlags.set(0, true);
                edgeFlags.set(edgeFlags.size() - 1, true);
            }

            // Add the remaining polygon locations (skipping the first). These winding order of these locations is
            // checked, then they are added in counter-clockwise order about the first location.
            if (locationCount > 1)
            {
                GeometryBuilder gb = this.getGeometryBuilder();
                Vec4[] polyPoints = new Vec4[locationCount + 1];
                Matrix[] polyTransform = new Matrix[1];
                int polyCount = this.computeEllipsoidalPolygon(globe, locations, null, polyPoints, null, polyTransform);
                int polyWinding = gb.computePolygonWindingOrder2(0, polyCount, polyPoints);

                if (polyWinding == GeometryBuilder.COUNTER_CLOCKWISE)
                {
                    for (int i = 1; i < locationCount; i++)
                    {
                        polyArcLocations.add(locations.get(i));
                        edgeFlags.add(true);
                    }
                }
                else // (polyWinding == GeometryBuilder.CLOCKWISE)
                {
                    for (int i = locationCount - 1; i >= 1; i--)
                    {
                        polyArcLocations.add(locations.get(i));
                        edgeFlags.add(true);
                    }
                }
            }
        }
    }

    private LatLon[] makeArc(Globe globe, LatLon center, double radius, int slices, double start, double sweep)
    {
        double da = sweep / slices;
        double r = radius / globe.getRadius();
        LatLon[] locations = new LatLon[slices + 1];

        for (int i = 0; i <= slices; i++)
        {
            double a = i * da + start;
            locations[i] = LatLon.greatCircleEndPosition(center, a, r);
        }

        return locations;
    }

    private Angle normalizedAzimuth(Angle azimuth)
    {
        double degrees = azimuth.degrees;
        double normalizedDegrees = degrees < 0.0 ? degrees + 360.0 : (degrees >= 360.0 ? degrees - 360.0 : degrees);
        return Angle.fromDegrees(normalizedDegrees);
    }

    //**************************************************************//
    //******************** END Geometry Rendering  *****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsDouble(context, "leftAzimuthDegrees", this.leftAzimuth.degrees);
        rs.addStateValueAsDouble(context, "rightAzimuthDegrees", this.rightAzimuth.degrees);
        rs.addStateValueAsDouble(context, "radius", this.radius);
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Double d = rs.getStateValueAsDouble(context, "radius");
        if (d != null)
            this.setRadius(d);

        Double la = rs.getStateValueAsDouble(context, "leftAzimuthDegrees");
        if (la == null)
            la = this.leftAzimuth.degrees;

        Double ra = rs.getStateValueAsDouble(context, "rightAzimuthDegrees");
        if (ra == null)
            ra = this.rightAzimuth.degrees;

        this.setAzimuths(Angle.fromDegrees(la), Angle.fromDegrees(ra));
    }
}
