/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.airspaces.Airspace;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: AirspaceEditorUtil.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AirspaceEditorUtil
{
    // Airspace altitude constants.
    public static final int LOWER_ALTITUDE = 0;
    public static final int UPPER_ALTITUDE = 1;

    //**************************************************************//
    //********************  Airspace/Control Point Utilities  ******//
    //**************************************************************//

    public static double computeLowestHeightAboveSurface(WorldWindow wwd,
        Iterable<? extends AirspaceControlPoint> controlPoints, int altitudeIndex)
    {
        double minHeight = Double.MAX_VALUE;

        for (AirspaceControlPoint controlPoint : controlPoints)
        {
            if (altitudeIndex == controlPoint.getAltitudeIndex())
            {
                double height = computeHeightAboveSurface(wwd, controlPoint.getPoint());
                if (height < minHeight)
                {
                    minHeight = height;
                }
            }
        }

        return minHeight;
    }

    public static double computeHeightAboveSurface(WorldWindow wwd, Vec4 point)
    {
        Position pos = wwd.getModel().getGlobe().computePositionFromPoint(point);
        Vec4 surfacePoint = computeSurfacePoint(wwd, pos.getLatitude(), pos.getLongitude());
        Vec4 surfaceNormal = wwd.getModel().getGlobe().computeSurfaceNormalAtPoint(point);
        return point.subtract3(surfacePoint).dot3(surfaceNormal);
    }

    public static double computeMinimumDistanceBetweenAltitudes(int numLocations,
        Iterable<? extends AirspaceControlPoint> controlPoints)
    {
        // We cannot assume anything about the ordering of the control points handed to us, but we must be able to
        // access them by location index and altitude index. To achieve this we place them in a map that will be
        // indexable by location and altitude.

        double minDistance = Double.MAX_VALUE;

        HashMap<Object, AirspaceControlPoint> map = new HashMap<Object, AirspaceControlPoint>();
        for (AirspaceControlPoint p : controlPoints)
        {
            map.put(p.getKey(), p);
        }

        for (int locationIndex = 0; locationIndex < numLocations; locationIndex++)
        {
            Object lowerKey = BasicAirspaceControlPoint.keyFor(locationIndex, LOWER_ALTITUDE);
            Object upperKey = BasicAirspaceControlPoint.keyFor(locationIndex, UPPER_ALTITUDE);

            AirspaceControlPoint lowerControlPoint = map.get(lowerKey);
            AirspaceControlPoint upperControlPoint = map.get(upperKey);

            if (lowerControlPoint != null && upperControlPoint != null)
            {
                double distance = lowerControlPoint.getPoint().distanceTo3(upperControlPoint.getPoint());
                if (distance < minDistance)
                {
                    minDistance = distance;
                }
            }
        }

        return (minDistance == Double.MAX_VALUE) ? 0.0 : minDistance;
    }

    //**************************************************************//
    //********************  Control Point Edge  ********************//
    //**************************************************************//

    public static class EdgeInfo
    {
        int locationIndex;
        int nextLocationIndex;
        int altitudeIndex;
        Vec4 point1;
        Vec4 point2;

        public EdgeInfo(int locationIndex, int nextLocationIndex, int altitudeIndex, Vec4 point1, Vec4 point2)
        {
            this.locationIndex = locationIndex;
            this.nextLocationIndex = nextLocationIndex;
            this.altitudeIndex = altitudeIndex;
            this.point1 = point1;
            this.point2 = point2;
        }
    }

    public static AirspaceControlPoint createControlPointFor(WorldWindow wwd, Line ray,
        AirspaceEditor editor, Airspace airspace, EdgeInfo edge)
    {
        // If the nearest point occurs before the line segment, then insert the new point before the segment. If the
        // nearest point occurs after the line segment, then insert the new point after the segment. If the nearest
        // point occurs inside the line segment, then insert the new point in the segment.

        Vec4 newPoint = intersectAirspaceAltitudeAt(wwd, airspace, edge.altitudeIndex, ray);
        Vec4 pointOnEdge = nearestPointOnSegment(edge.point1, edge.point2, newPoint);

        int locationIndex;
        int altitudeIndex = edge.altitudeIndex;

        if (pointOnEdge == edge.point1)
        {
            locationIndex = edge.locationIndex;
        }
        else if (pointOnEdge == edge.point2)
        {
            locationIndex = edge.nextLocationIndex + 1;
        }
        else // (o == Orientation.INSIDE)
        {
            locationIndex = edge.nextLocationIndex;
        }

        return new BasicAirspaceControlPoint(editor, airspace, locationIndex, altitudeIndex, newPoint);
    }

    public static List<EdgeInfo> computeEdgeInfoFor(int numLocations,
        Iterable<? extends AirspaceControlPoint> controlPoints)
    {
        // Compute edge data structures for the segment between each successive pair of control points, including the
        // edge between the last and first control points. Do this for the upper and lower altitudes of the airspace.
        // We cannot assume anything about the ordering of the control points handed to us, but we must be able to
        // access them by location index and altitude index. To achieve this we place them in a map that will be
        // indexable by location and altitude.

        ArrayList<EdgeInfo> edgeInfoList = new ArrayList<EdgeInfo>();

        HashMap<Object, AirspaceControlPoint> map = new HashMap<Object, AirspaceControlPoint>();
        for (AirspaceControlPoint p : controlPoints)
        {
            map.put(p.getKey(), p);
        }

        for (int altitudeIndex = 0; altitudeIndex < 2; altitudeIndex++)
        {
            for (int locationIndex = 0; locationIndex < numLocations; locationIndex++)
            {
                int nextLocationIndex = (locationIndex < numLocations - 1) ? (locationIndex + 1) : 0;
                Object key = BasicAirspaceControlPoint.keyFor(locationIndex, altitudeIndex);
                Object nextKey = BasicAirspaceControlPoint.keyFor(nextLocationIndex, altitudeIndex);

                AirspaceControlPoint controlPoint = map.get(key);
                AirspaceControlPoint nextControlPoint = map.get(nextKey);

                if (controlPoint != null && nextControlPoint != null)
                {
                    edgeInfoList.add(new EdgeInfo(locationIndex, nextLocationIndex, altitudeIndex,
                        controlPoint.getPoint(), nextControlPoint.getPoint()));
                }
            }
        }

        return edgeInfoList;
    }

    public static EdgeInfo selectBestEdgeMatch(WorldWindow wwd, Line ray,
        Airspace airspace, List<? extends EdgeInfo> edgeInfoList)
    {
        // Try to find the edge that is closest to the given ray. This is used by the routine doAddNextLocation(),
        // which is trying to determine the user's intent as to which edge a new two control points should be added to.
        // Therefore consider the potential locations of a new control point on the ray: one for each of the lower
        // and upper airspace altitudes. We choose the edge that is closest to one of these points. We will ignore
        // an edge if its nearest point is behind the ray origin.

        Vec4[] pointOnLine = new Vec4[2];
        pointOnLine[LOWER_ALTITUDE] = intersectAirspaceAltitudeAt(wwd, airspace, LOWER_ALTITUDE, ray);
        pointOnLine[UPPER_ALTITUDE] = intersectAirspaceAltitudeAt(wwd, airspace, UPPER_ALTITUDE, ray);

        EdgeInfo bestEdge = null;
        double nearestDistance = Double.MAX_VALUE;

        for (EdgeInfo edge : edgeInfoList)
        {
            for (int index = 0; index < 2; index++)
            {
                Vec4 pointOnEdge = nearestPointOnSegment(edge.point1, edge.point2, pointOnLine[index]);
                if (!isPointBehindLineOrigin(ray, pointOnEdge))
                {
                    double d = pointOnEdge.distanceTo3(pointOnLine[index]);
                    if (d < nearestDistance)
                    {
                        bestEdge = edge;
                        nearestDistance = d;
                    }
                }
            }
        }

        return bestEdge;
    }

    //**************************************************************//
    //********************  Globe Utilities  ***********************//
    //**************************************************************//

    public static Vec4 intersectAirspaceAltitudeAt(WorldWindow wwd, Airspace airspace, int altitudeIndex, Line ray)
    {
        double elevation = airspace.getAltitudes()[altitudeIndex];

        boolean terrainConformant = airspace.isTerrainConforming()[altitudeIndex];
        if (terrainConformant)
        {
            Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(ray);
            if (intersections != null)
            {
                Vec4 point = nearestIntersectionPoint(ray, intersections);
                if (point != null)
                {
                    Position pos = wwd.getModel().getGlobe().computePositionFromPoint(point);
                    elevation += pos.getElevation();
                }
            }
        }

        return intersectGlobeAt(wwd, elevation, ray);
    }

    public static Vec4 intersectGlobeAt(WorldWindow wwd, double elevation, Line ray)
    {
        Intersection[] intersections = wwd.getModel().getGlobe().intersect(ray, elevation);
        if (intersections == null || intersections.length == 0)
        {
            return null;
        }

        return nearestIntersectionPoint(ray, intersections);
    }

    public static double surfaceElevationAt(WorldWindow wwd, Line ray)
    {
        // Try to find the surface elevation at the mouse point by intersecting a ray with the terrain.

        double surfaceElevation = 0.0;

        if (wwd.getSceneController().getTerrain() != null)
        {
            Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(ray);
            if (intersections != null)
            {
                Vec4 point = nearestIntersectionPoint(ray, intersections);
                if (point != null)
                {
                    Position pos = wwd.getModel().getGlobe().computePositionFromPoint(point);
                    surfaceElevation = pos.getElevation();
                }
            }
        }

        return surfaceElevation;
    }

    public static Vec4 computeSurfacePoint(WorldWindow wwd, Angle latitude, Angle longitude)
    {
        Vec4 point = wwd.getSceneController().getTerrain().getSurfacePoint(latitude, longitude);
        if (point != null)
            return point;

        return wwd.getModel().getGlobe().computePointFromPosition(latitude, longitude, 0.0);
    }

    //**************************************************************//
    //********************  Line Utilities  ************************//
    //**************************************************************//

    public static boolean isPointBehindLineOrigin(Line line, Vec4 point)
    {
        double dot = point.subtract3(line.getOrigin()).dot3(line.getDirection());
        return dot < 0.0;
    }

    public static Vec4 nearestPointOnLine(Line source, Line target)
    {
        // Compute the points on each ray that are closest to one another.
        // Taken from "Mathematics for 3D Game Programming..." by Eric Lengyel, Section 4.1.2.

        double dot_dir = source.getDirection().dot3(target.getDirection());
        double c = 1.0 / (dot_dir * dot_dir - 1.0);
        double a1 = target.getOrigin().subtract3(source.getOrigin()).dot3(source.getDirection());
        double a2 = target.getOrigin().subtract3(source.getOrigin()).dot3(target.getDirection());
        double t1 = c * (a2 * dot_dir - a1);

        return source.getPointAt(t1);
    }

    public static Vec4 nearestPointOnSegment(Vec4 p1, Vec4 p2, Vec4 point)
    {
        Vec4 segment = p2.subtract3(p1);
        Vec4 dir = segment.normalize3();

        double dot = point.subtract3(p1).dot3(dir);
        if (dot < 0.0)
        {
            return p1;
        }
        else if (dot > segment.getLength3())
        {
            return p2;
        }
        else
        {
            return Vec4.fromLine3(p1, dot, dir);
        }
    }

    public static Vec4 nearestIntersectionPoint(Line line, Intersection[] intersections)
    {
        Vec4 intersectionPoint = null;

        // Find the nearest intersection that's in front of the ray origin.
        double nearestDistance = Double.MAX_VALUE;
        for (Intersection intersection : intersections)
        {
            // Ignore any intersections behind the line origin.
            if (!isPointBehindLineOrigin(line, intersection.getIntersectionPoint()))
            {
                double d = intersection.getIntersectionPoint().distanceTo3(line.getOrigin());
                if (d < nearestDistance)
                {
                    intersectionPoint = intersection.getIntersectionPoint();
                    nearestDistance = d;
                }
            }
        }

        return intersectionPoint;
    }
}
