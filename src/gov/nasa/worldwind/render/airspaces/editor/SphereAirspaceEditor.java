/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: SphereAirspaceEditor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SphereAirspaceEditor extends AbstractAirspaceEditor
{
    private SphereAirspace sphere = null; // Can be null
    private double minRadius = 1.0;
    private double maxRadius = Double.MAX_VALUE;
    private boolean alwaysShowRadiusControl = false;
    private double radiusControlDrawDistance = 14;

    public static final int RADIUS_CONTROL_ID = 1024;

    public SphereAirspaceEditor(AirspaceControlPointRenderer renderer)
    {
        super(renderer);
    }

    public SphereAirspaceEditor()
    {
        this(getDefaultRenderer());
    }

    public static AirspaceControlPointRenderer getDefaultRenderer()
    {
        BasicAirspaceControlPointRenderer renderer = new BasicAirspaceControlPointRenderer();
        renderer.setControlPointMarker(createDefaultMarker());
        renderer.setEnableDepthTest(false);
        return renderer;
    }

    public static Marker createDefaultMarker()
    {
        // Create an opaque blue sphere. By default the sphere has a 12 pixel radius, but its radius must be at least
        // 0.1 meters .
        MarkerAttributes attributes = new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 1.0, 12, 0.1);
        return new BasicMarker(null, attributes, null);
    }

    public Airspace getAirspace()
    {
        return this.getSphere();
    }

    public SphereAirspace getSphere()
    {
        return this.sphere;
    }

    public void setSphere(SphereAirspace sphere)
    {
        this.sphere = sphere;
    }

    public double getMinRadius()
    {
        return this.minRadius;
    }

    public void setMinRadius(double radius)
    {
        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minRadius = radius;
    }

    public double getMaxRadius()
    {
        return this.maxRadius;
    }

    public void setMaxRadius(double radius)
    {
        if (radius < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxRadius = radius;
    }

    public boolean isAlwaysShowRadiusControl()
    {
        return this.alwaysShowRadiusControl;
    }

    public void setAlwaysShowRadiusControl(boolean alwaysShow)
    {
        this.alwaysShowRadiusControl = alwaysShow;
    }

    public double getRadiusControlDrawDistance()
    {
        return radiusControlDrawDistance;
    }

    public void setRadiusControlDrawDistance(double distance)
    {
        if (distance < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "distance < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.radiusControlDrawDistance = distance;
    }

    //**************************************************************//
    //********************  Control Point Assembly  ****************//
    //**************************************************************//

    protected void assembleControlPoints(DrawContext dc)
    {
        // If the cursor passes near the edge of the sphere, draw a tangent control point that can be used to
        // adjust the sphere's radius.

        if (this.getSphere() == null)
            return;

        Extent bounds = this.getSphere().getExtent(dc);
        if (bounds == null)
            return;

        Point pickPoint = dc.getPickPoint();
        if (pickPoint == null)
            return;

        Line pickRay = dc.getView().computeRayFromScreenPoint(pickPoint.getX(), pickPoint.getY());

        Vec4 centerPoint = bounds.getCenter();
        double radius = bounds.getRadius();

        Vec4 nearestPointOnLine = pickRay.nearestPointTo(centerPoint);
        Vec4 normalToNearest = nearestPointOnLine.subtract3(centerPoint).normalize3();
        Vec4 nearestPointOnSphere = normalToNearest.multiply3(radius).add3(centerPoint);

        Vec4 nearestScreenPointOnLine = dc.getView().project(nearestPointOnLine);
        Vec4 nearestScreenPointOnSphere = dc.getView().project(nearestPointOnSphere);

        double distance = nearestScreenPointOnLine.distanceTo3(nearestScreenPointOnSphere);
        if (this.isAlwaysShowRadiusControl() || distance < this.getRadiusControlDrawDistance())
        {
            AirspaceControlPoint controlPoint = new BasicAirspaceControlPoint(this, this.getSphere(),
                RADIUS_CONTROL_ID, RADIUS_CONTROL_ID, nearestPointOnSphere);
            this.addControlPoint(dc, controlPoint);
        }
    }

    protected Vec4 getCenterPoint(WorldWindow wwd, Airspace airspace)
    {
        if (!(airspace instanceof SphereAirspace))
        {
            return null;
        }

        SphereAirspace sphere = (SphereAirspace) airspace;
        LatLon location = sphere.getLocation();
        double altitude = sphere.getAltitudes()[LOWER_ALTITUDE];
        boolean terrainConforming = sphere.isTerrainConforming()[LOWER_ALTITUDE];

        Vec4 point;
        if (terrainConforming)
        {
            if (wwd.getSceneController().getTerrain() != null)
            {
                point = wwd.getSceneController().getTerrain().getSurfacePoint(
                    location.getLatitude(), location.getLongitude(), altitude);
            }
            else
            {
                double elevation = wwd.getModel().getGlobe().getElevation(
                    location.getLatitude(), location.getLongitude());
                point = wwd.getModel().getGlobe().computePointFromPosition(
                    location.getLatitude(), location.getLongitude(), elevation + altitude);
            }
        }
        else
        {
            point = wwd.getModel().getGlobe().computePointFromPosition(
                location.getLatitude(), location.getLongitude(), altitude);
        }

        return point;
    }

    //**************************************************************//
    //********************  Control Point Events  ******************//
    //**************************************************************//

    protected void doMoveAirspaceVertically(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint)
    {
        // Find the closest points between the rays through each screen point, and the ray from the control point
        // and in the direction of the globe's surface normal. Compute the elevation difference between these two
        // points, and use that as the change in airspace altitude.
        //
        // If the state keepControlPointsAboveTerrain is set, we prevent the control point from passing any lower than
        // the terrain elevation beneath it.

        double altitude = this.getAirspace().getAltitudes()[LOWER_ALTITUDE];
        boolean terrainConforming = this.getAirspace().isTerrainConforming()[LOWER_ALTITUDE];
        Vec4 centerPoint = this.getCenterPoint(wwd, airspace);

        Vec4 surfaceNormal = wwd.getModel().getGlobe().computeSurfaceNormalAtPoint(centerPoint);
        Line verticalRay = new Line(centerPoint, surfaceNormal);
        Line screenRay = wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Line previousScreenRay = wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());

        Vec4 pointOnLine = AirspaceEditorUtil.nearestPointOnLine(verticalRay, screenRay);
        Vec4 previousPointOnLine = AirspaceEditorUtil.nearestPointOnLine(verticalRay, previousScreenRay);

        Position pos = wwd.getModel().getGlobe().computePositionFromPoint(pointOnLine);
        Position previousPos = wwd.getModel().getGlobe().computePositionFromPoint(previousPointOnLine);
        double elevationChange = previousPos.getElevation() - pos.getElevation();

        if (this.isKeepControlPointsAboveTerrain())
        {
            if (terrainConforming)
            {
                if (altitude + elevationChange < 0.0)
                    elevationChange = -altitude;
            }
            else
            {
                double height = AirspaceEditorUtil.computeHeightAboveSurface(wwd, centerPoint);
                if (elevationChange <= -height)
                    elevationChange = -height;
            }
        }

        double newElevation = altitude + elevationChange;
        this.getAirspace().setAltitude(newElevation);

        this.fireAirspaceMoved(new AirspaceEditEvent(wwd, airspace, this));
    }

    protected AirspaceControlPoint doAddControlPoint(WorldWindow wwd, Airspace airspace,
        Point mousePoint)
    {
        return null;
    }

    protected void doRemoveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint)
    {
    }

    protected void doMoveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint)
    {
        if (controlPoint.getLocationIndex() == RADIUS_CONTROL_ID)
        {
            this.doMoveRadiusControlPoint(wwd, controlPoint, mousePoint, previousMousePoint);
        }
    }

    protected void doResizeAtControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint)
    {
    }

    protected void doMoveRadiusControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint)
    {
        // Find the closest points between the rays through each screen point, and the ray from the sphere center to the
        // control point. Compute the signed difference between these two points, and use that as the change in radius.

        Vec4 centerPoint = this.getCenterPoint(wwd, this.getSphere());

        Line screenRay = wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Line previousScreenRay = wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(centerPoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(centerPoint);

        double distance = nearestPointOnLine.distanceTo3(centerPoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(centerPoint);
        double radiusChange = previousDistance - distance;

        double radius = this.getSphere().getRadius() + radiusChange;
        if (radius < this.getMinRadius())
            radius = this.getMinRadius();
        if (radius > this.getMaxRadius())
            radius = this.getMaxRadius();

        this.getSphere().setRadius(radius);

        AirspaceEditEvent editEvent = new AirspaceEditEvent(wwd, this.getSphere(), this, controlPoint);
        this.fireControlPointChanged(editEvent);
        this.fireAirspaceResized(editEvent);
    }
}
