/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.segmentplane;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceEditorUtil;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: SegmentPlaneEditor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SegmentPlaneEditor extends AbstractLayer
{
    protected static final int SEGMENT_BEGIN_INDEX = 0;
    protected static final int SEGMENT_END_INDEX = 1;

    private boolean armed;
    private boolean snapToGrid;
    private SegmentPlane segmentPlane; // Can be null.
    private SegmentPlaneRenderer renderer; // Can be null.

    public SegmentPlaneEditor()
    {
        this.armed = false;
        this.snapToGrid = true;
        this.renderer = new SegmentPlaneRenderer();
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    public void setArmed(boolean armed)
    {
        this.armed = armed;
    }

    public boolean isSnapToGrid()
    {
        return this.snapToGrid;
    }

    public void setSnapToGrid(boolean snapToGrid)
    {
        this.snapToGrid = snapToGrid;
    }

    public SegmentPlane getSegmentPlane()
    {
        return this.segmentPlane;
    }

    public void setSegmentPlane(SegmentPlane segmentPlane)
    {
        this.segmentPlane = segmentPlane;
    }

    public SegmentPlaneRenderer getSegmentPlaneRenderer()
    {
        return this.renderer;
    }

    public void setSegmentPlaneRenderer(SegmentPlaneRenderer renderer)
    {
        this.renderer = renderer;
    }

    protected void doRender(DrawContext dc)
    {
        if (!this.isArmed())
            return;

        if (this.getSegmentPlane() == null || this.getSegmentPlaneRenderer() == null)
            return;

        this.getSegmentPlaneRenderer().render(dc, this.getSegmentPlane());
    }

    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        if (!this.isArmed())
            return;

        if (this.getSegmentPlane() == null || this.getSegmentPlaneRenderer() == null)
            return;

        this.getSegmentPlaneRenderer().pick(dc, this.getSegmentPlane(), pickPoint, this);
    }

    public void moveControlPoint(WorldWindow wwd, PickedObject pickedObject, Point mousePoint, Point previousMousePoint)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (pickedObject == null)
        {
            String message = Logging.getMessage("nullValue.PickedObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Include this test to ensure any derived implementation performs it.
        if (this.getSegmentPlane() == null)
        {
            return;
        }

        if (!(pickedObject.getObject() instanceof SegmentPlane.ControlPoint))
        {
            return;
        }

        SegmentPlane.ControlPoint controlPoint = (SegmentPlane.ControlPoint) pickedObject.getObject();
        if (this.getSegmentPlane() != controlPoint.getOwner())
        {
            return;
        }

        this.doMoveControlPoint(wwd, pickedObject, mousePoint, previousMousePoint);
    }

    protected void doMoveControlPoint(WorldWindow wwd, PickedObject pickedObject,
        Point mousePoint, Point previousMousePoint)
    {
        SegmentPlane.ControlPoint controlPoint = (SegmentPlane.ControlPoint) pickedObject.getObject();

        Object key = controlPoint.getKey();
        if (key == null)
        {
            return;
        }

        if (key.equals(SegmentPlane.SEGMENT_BEGIN) || key.equals(SegmentPlane.SEGMENT_END))
        {
            this.doMoveSegmentPoint(wwd, pickedObject, mousePoint, previousMousePoint);
        }
        else if (key.equals(SegmentPlane.CONTROL_POINT_LOWER_LEFT))
        {
            this.doMoveSegmentPlane(wwd, pickedObject, mousePoint, previousMousePoint);
        }
        else if (key.equals(SegmentPlane.CONTROL_POINT_LOWER_RIGHT) 
            || key.equals(SegmentPlane.CONTROL_POINT_UPPER_RIGHT))
        {
            this.doMoveLateralControlPoint(wwd, pickedObject, mousePoint, previousMousePoint);
        }
        else if (key.equals(SegmentPlane.CONTROL_POINT_TOP_EDGE))
        {
            this.doMoveVerticalControlPoint(wwd, pickedObject, mousePoint, previousMousePoint);
        }
        else if (key.equals(SegmentPlane.CONTROL_POINT_LEADING_EDGE))
        {
            this.doMoveHorizontalControlPoint(wwd, pickedObject, mousePoint, previousMousePoint);
        }
    }

    //**************************************************************//
    //********************  Segment Plane Movement  ****************//
    //**************************************************************//

    protected void doMoveSegmentPlane(WorldWindow wwd, PickedObject pickedObject,
        Point mousePoint, Point previousMousePoint)
    {
        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();
        LatLon[] locations = this.getSegmentPlane().getPlaneLocations();

        Position pickedPos = pickedObject.getPosition();

        Position refPos = new Position(locations[0], pickedPos.getElevation());
        Vec4 refPoint = globe.computePointFromPosition(refPos);
        Vec4 screenRefPoint = view.project(refPoint);

        // Compute screen-coord delta since last event.
        int dx = mousePoint.x - previousMousePoint.x;
        int dy = mousePoint.y - previousMousePoint.y;

        // Find intersection of screen coord ref-point with globe.
        double x = screenRefPoint.x + dx;
        double y = screenRefPoint.y + dy;
        if (wwd instanceof Component)
        {
            y = ((Component) wwd).getSize().height - screenRefPoint.y + dy - 1;
        }

        Line ray = view.computeRayFromScreenPoint(x, y);
        Intersection[] intersections = globe.intersect(ray, refPos.getElevation());
        if (intersections == null || intersections.length == 0)
            return;

        Position newPos = globe.computePositionFromPoint(intersections[0].getIntersectionPoint());

        Angle heading = LatLon.greatCircleAzimuth(refPos, newPos);
        Angle distance = LatLon.greatCircleDistance(refPos, newPos);

        locations[0] = LatLon.greatCircleEndPosition(locations[0], heading, distance);
        locations[1] = LatLon.greatCircleEndPosition(locations[1], heading, distance);

        this.moveSegmentLocationWithPlane(locations, SEGMENT_BEGIN_INDEX);
        this.moveSegmentLocationWithPlane(locations, SEGMENT_END_INDEX);
        this.getSegmentPlane().setPlaneLocations(locations[0], locations[1]);
    }

    //**************************************************************//
    //********************  Segment Point Actions  *****************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doMoveSegmentPoint(WorldWindow wwd, PickedObject pickedObject,
        Point mousePoint, Point previousMousePoint)
    {
        Position oldPosition = pickedObject.getPosition();
        Position newPosition = this.computeNewPositionFromPlaneGeometry(wwd);

        // If the mouse point is not on the plane geometry, we compute an intersection with the infinite plane
        // defined by the SegmentPlane's corners.
        if (newPosition == null)
        {
            newPosition  = this.computeNewPositionFromPlaneIntersection(wwd, mousePoint);
            if (newPosition != null)
            {
                newPosition = this.resizeSegmentPlaneToFitPosition(wwd, newPosition);
            }
        }

        if (newPosition == null)
        {
            return;
        }

        newPosition = this.computePositionOnOrAboveSurface(wwd, newPosition);

        Position[] positions = this.getSegmentPlane().getSegmentPositions();

        Object endpointId = pickedObject.getValue(AVKey.PICKED_OBJECT_ID);
        if (endpointId.equals(SegmentPlane.SEGMENT_BEGIN))
        {
            positions[0] = new Position(oldPosition, newPosition.getElevation());
        }
        else if (endpointId.equals(SegmentPlane.SEGMENT_END))
        {
            positions[1] = newPosition;
        }

        this.getSegmentPlane().setSegmentPositions(positions[0], positions[1]);
    }

    protected Position computeNewPositionFromPlaneGeometry(WorldWindow wwd)
    {
        if (this.isSnapToGrid())
        {
            PickedObject gridObject = this.getPickedSegmentPlaneObject(wwd, SegmentPlane.PLANE_GRID);
            if (gridObject != null)
            {
                return gridObject.getPosition();
            }
        }

        PickedObject planeObject = this.getPickedSegmentPlaneObject(wwd, SegmentPlane.PLANE_BACKGROUND);
        if (planeObject != null)
        {
            return planeObject.getPosition();
        }

        return null;
    }

    protected Position computeNewPositionFromPlaneIntersection(WorldWindow wwd, Point mousePoint)
    {
        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();
        
        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Plane plane = this.getSegmentPlane().computeInfinitePlane(globe);
        if (plane == null)
        {
            return null;
        }

        Vec4 newPoint = plane.intersect(ray);
        if (newPoint == null)
        {
            return null;
        }

        return globe.computePositionFromPoint(newPoint);
    }

    protected Position resizeSegmentPlaneToFitPosition(WorldWindow wwd, Position position)
    {
        Globe globe = wwd.getModel().getGlobe();
        double[] altitudes = this.getSegmentPlane().getPlaneAltitudes();
        double[] gridSizes = this.getSegmentPlane().getGridCellDimensions();
        LatLon[] locations = this.getSegmentPlane().getPlaneLocations();

        if (position.getElevation() < altitudes[0])
        {
            altitudes[0] = altitudes[0] + this.getNextGridStep(position.getElevation(), altitudes[0], gridSizes[1]);
        }
        if (position.getElevation() > altitudes[1])
        {
            altitudes[1] = altitudes[0] + this.getNextGridStep(position.getElevation(), altitudes[0], gridSizes[1]);
        }

        Vec4[] segment = new Vec4[2];
        segment[0] = globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(), altitudes[0]);
        segment[1] = globe.computePointFromPosition(locations[1].getLatitude(), locations[1].getLongitude(), altitudes[0]);
        Vec4 n = segment[1].subtract3(segment[0]).normalize3();
        double length = segment[0].distanceTo3(segment[1]);

        Vec4 point = globe.computePointFromPosition(position);
        Vec4 p = point.subtract3(segment[0]);
        double dot = p.dot3(n);

        // Resize only in the positive direction.
        if (dot > length)
        {
            double nextLength = this.getNextGridStep(dot, 0.0, gridSizes[0]);
            Vec4 nextPoint = segment[0].add3(n.multiply3(nextLength));
            locations[1] = new LatLon(globe.computePositionFromPoint(nextPoint));
        }
        if (dot < 0.0)
        {
            position = new Position(locations[0], position.getElevation());
        }
        
        this.getSegmentPlane().setPlaneAltitudes(altitudes[0], altitudes[1]);
        this.getSegmentPlane().setPlaneLocations(locations[0], locations[1]);

        return position;
    }

    //**************************************************************//
    //********************  Segment Plane Orientation/Length  ******//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doMoveLateralControlPoint(WorldWindow wwd, PickedObject pickedObject,
        Point mousePoint, Point previousMousePoint)
    {
        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();
        double[] altitudes = this.getSegmentPlane().getPlaneAltitudes();
        LatLon[] locations = this.getSegmentPlane().getPlaneLocations();

        Position pos = pickedObject.getPosition();
        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Intersection[] intersection = globe.intersect(ray, pos.getElevation());
        if (intersection == null || intersection.length < 0)
            return;

        Vec4 newPoint = intersection[0].getIntersectionPoint();
        LatLon newLatLon = new LatLon(globe.computePositionFromPoint(newPoint));

        Object id = pickedObject.getValue(AVKey.PICKED_OBJECT_ID);
        if (id.equals(SegmentPlane.CONTROL_POINT_LOWER_RIGHT)
            ||id.equals(SegmentPlane.CONTROL_POINT_UPPER_RIGHT))
        {
            locations[1] = newLatLon;
            this.moveSegmentLocationWithPlane(locations, SEGMENT_END_INDEX);
        }

        this.getSegmentPlane().setPlaneLocations(locations[0], locations[1]);
    }

    //**************************************************************//
    //********************  Segment Plane Height  ******************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doMoveVerticalControlPoint(WorldWindow wwd, PickedObject pickedObject,
        Point mousePoint, Point previousMousePoint)
    {
        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();
        double[] altitudes = this.getSegmentPlane().getPlaneAltitudes();
        Position[] segmentPositions = this.getSegmentPlane().getSegmentPositions();

        Position pos = pickedObject.getPosition();
        Vec4 point = globe.computePointFromPosition(pos);

        Vec4 surfaceNormal = globe.computeSurfaceNormalAtPoint(point);
        Line verticalRay = new Line(point, surfaceNormal);
        Line screenRay = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());

        Vec4 pointOnLine = AirspaceEditorUtil.nearestPointOnLine(verticalRay, screenRay);
        Position newPos = globe.computePositionFromPoint(pointOnLine);

        altitudes[1] = newPos.getElevation();

        if (altitudes[1] < altitudes[0])
            altitudes[1] = altitudes[0];

        for (int i = 0; i < 2; i++)
        {
            if (altitudes[1] < segmentPositions[i].getElevation())
                altitudes[1] = segmentPositions[i].getElevation();
        }

        this.getSegmentPlane().setPlaneAltitudes(altitudes[0], altitudes[1]);
    }

    //**************************************************************//
    //********************  Segment Plane Length  ******************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doMoveHorizontalControlPoint(WorldWindow wwd, PickedObject pickedObject, 
        Point mousePoint, Point previousMousePoint)
    {
        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();
        LatLon[] locations = this.getSegmentPlane().getPlaneLocations();
        Position[] segmentPositions = this.getSegmentPlane().getSegmentPositions();

        Position pos = pickedObject.getPosition();
        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Intersection[] intersection = globe.intersect(ray, pos.getElevation());
        if (intersection == null || intersection.length < 0)
            return;

        Vec4 newPoint = intersection[0].getIntersectionPoint();
        LatLon newLatLon = new LatLon(globe.computePositionFromPoint(newPoint));

        Angle heading = LatLon.rhumbAzimuth(locations[0], locations[1]);
        Angle distance = LatLon.rhumbDistance(locations[0], newLatLon);

        Angle minDistance = LatLon.rhumbDistance(locations[0], segmentPositions[1]);
        if (distance.compareTo(minDistance) < 0)
            distance = minDistance;

        locations[1] = LatLon.rhumbEndPosition(locations[0], heading, distance);

        this.getSegmentPlane().setPlaneLocations(locations[0], locations[1]);
    }

    //**************************************************************//
    //********************  Utility Methods  ***********************//
    //**************************************************************//

    protected Position moveSegmentAltitudeWithPlane(Position position, double[] minAndMaxElevation)
    {
        double elevation = position.getElevation();
        if (elevation >= minAndMaxElevation[0] && elevation <= minAndMaxElevation[1])
        {
            return null;
        }

        if (elevation < minAndMaxElevation[0])
            elevation = minAndMaxElevation[0];
        if (elevation > minAndMaxElevation[1])
            elevation = minAndMaxElevation[1];

        return new Position(position, elevation);
    }
    
    protected void moveSegmentLocationWithPlane(LatLon[] newPlaneLocations, int segmentPositionIndex)
    {
        LatLon[] planeLocations = this.getSegmentPlane().getPlaneLocations();
        Position segmentPosition = this.getSegmentPlane().getSegmentPositions()[segmentPositionIndex];

        if (segmentPositionIndex == SEGMENT_BEGIN_INDEX)
        {
            Position newSegmentPosition = new Position(newPlaneLocations[0], segmentPosition.getElevation());
            this.getSegmentPlane().setSegmentBeginPosition(newSegmentPosition);
        }
        else if (segmentPositionIndex == SEGMENT_END_INDEX)
        {
            Angle newHeading = LatLon.rhumbAzimuth(newPlaneLocations[0], newPlaneLocations[1]);

            Angle distance = LatLon.rhumbDistance(planeLocations[0], segmentPosition);
            Angle maxDistance = LatLon.rhumbDistance(newPlaneLocations[0], newPlaneLocations[1]);
            if (distance.compareTo(maxDistance) > 0)
                distance = maxDistance;

            LatLon newLatLon = LatLon.rhumbEndPosition(newPlaneLocations[0], newHeading, distance);
            Position newSegmentPosition = new Position(newLatLon, segmentPosition.getElevation());

            this.getSegmentPlane().setSegmentEndPosition(newSegmentPosition);
        }
    }

    protected PickedObject getPickedSegmentPlaneObject(WorldWindow wwd, Object pickedObjectId)
    {
        if (wwd.getSceneController().getPickedObjectList() == null)
        {
            return null;
        }

        for (PickedObject po : wwd.getSceneController().getPickedObjectList())
        {
            if (po != null && po.getObject() == this.getSegmentPlane())
            {
                Object id = po.getValue(AVKey.PICKED_OBJECT_ID);
                if (id == pickedObjectId)
                {
                    return po;
                }
            }
        }

        return null;
    }

    protected Position computePositionOnOrAboveSurface(WorldWindow wwd, Position position)
    {
        if (wwd.getSceneController().getTerrain() != null)
        {
            Vec4 point = wwd.getSceneController().getTerrain().getSurfacePoint(
                position.getLatitude(), position.getLongitude());
            if (point != null)
            {
                Position pos = wwd.getModel().getGlobe().computePositionFromPoint(point);
                if (position.getElevation() < pos.getElevation())
                    return new Position(position, pos.getElevation());
                return position;
            }
        }

        double elev = wwd.getModel().getGlobe().getElevation(position.getLatitude(), position.getLongitude());
        if (position.getElevation() < elev)
            return new Position(position, elev);

        return position;
    }

    protected double getNextGridStep(double value, double origin, double gridSize)
    {
        double x = Math.ceil((value - origin) / gridSize);
        return gridSize * x;
    }
}
