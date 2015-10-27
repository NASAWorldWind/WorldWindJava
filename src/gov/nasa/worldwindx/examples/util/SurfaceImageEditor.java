/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id: SurfaceImageEditor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceImageEditor implements SelectListener
{
    protected static final int NONE = 0;
    protected static final int MOVING = 1;
    protected static final int SIZING = 2;

    protected final WorldWindow wwd;
    protected SurfaceImage shape;
    protected MarkerLayer controlPointLayer;
    protected boolean armed;

    protected boolean active;
    protected int activeOperation = NONE;
    protected Position previousPosition = null;

    protected static class ControlPointMarker extends BasicMarker
    {
        private int index;

        public ControlPointMarker(Position position, MarkerAttributes attrs, int index)
        {
            super(position, attrs);
            this.index = index;
        }

        public int getIndex()
        {
            return this.index;
        }
    }

    public SurfaceImageEditor(WorldWindow wwd, SurfaceImage shape)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }
        if (shape == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.shape = shape;

        this.controlPointLayer = new MarkerLayer();
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public SurfaceImage getSurfaceImage()
    {
        return this.shape;
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    public void setArmed(boolean armed)
    {
        if (!this.armed && armed)
        {
            this.enable();
        }
        else if (this.armed && !armed)
        {
            this.disable();
        }

        this.armed = armed;
    }

    protected void enable()
    {
        LayerList layers = this.wwd.getModel().getLayers();

        if (!layers.contains(this.controlPointLayer))
            layers.add(this.controlPointLayer);

        if (!this.controlPointLayer.isEnabled())
            this.controlPointLayer.setEnabled(true);

        this.updateAffordances();

        this.wwd.addSelectListener(this);
    }

    protected void disable()
    {
        LayerList layers = this.wwd.getModel().getLayers();

        layers.remove(this.controlPointLayer);

        wwd.removeSelectListener(this);
    }

    public void selected(SelectEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (event.getTopObject() != null && !(event.getTopObject() == this.shape
            || event.getTopPickedObject().getParentLayer() == this.controlPointLayer))
        {
            ((Component) this.wwd).setCursor(null);
            return;
        }

        if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            this.active = false;
            this.activeOperation = NONE;
            this.previousPosition = null;
        }
        else if (event.getEventAction().equals(SelectEvent.ROLLOVER))
        {
            if (!(this.wwd instanceof Component))
                return;

            if (event.getTopObject() == null || event.getTopPickedObject().isTerrain())
            {
                ((Component) this.wwd).setCursor(null);
                return;
            }

            Cursor cursor;
            if (event.getTopObject() instanceof SurfaceImage)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            else if (event.getTopObject() instanceof Marker)
                cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            else
                cursor = null;

            ((Component) this.wwd).setCursor(cursor);
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_PRESS))
        {
            this.active = true;
            this.previousPosition = this.wwd.getCurrentPosition();
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            if (!this.active)
                return;

            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null)
                return;

            if (topObject == this.shape || this.activeOperation == MOVING)
            {
                this.activeOperation = MOVING;
                this.dragWholeShape(dragEvent, topObject);
                this.updateAffordances();
                event.consume();
            }
            else if (dragEvent.getTopPickedObject().getParentLayer() == this.controlPointLayer
                || this.activeOperation == SIZING)
            {
                this.activeOperation = SIZING;
                this.resizeShape(topObject);
                this.updateAffordances();
                event.consume();
            }
        }
    }

    protected void dragWholeShape(DragSelectEvent dragEvent, Object topObject)
    {
        if (!(topObject instanceof Movable))
            return;

        Movable dragObject = (Movable) topObject;

        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();

        // Compute ref-point position in screen coordinates. Since the SufaceShape is implicitly follows the surface
        // geometry, we will override the reference elevation with the current surface elevation. This will improve
        // cursor tracking in areas where the elevations are far from zero.
        Position refPos = dragObject.getReferencePosition();
        if (refPos == null)
            return;

        double refElevation = this.computeSurfaceElevation(wwd, refPos);
        refPos = new Position(refPos, refElevation);
        Vec4 refPoint = globe.computePointFromPosition(refPos);
        Vec4 screenRefPoint = view.project(refPoint);

        // Compute screen-coord delta since last event.
        int dx = dragEvent.getPickPoint().x - dragEvent.getPreviousPickPoint().x;
        int dy = dragEvent.getPickPoint().y - dragEvent.getPreviousPickPoint().y;

        // Find intersection of screen coord ref-point with globe.
        double x = screenRefPoint.x + dx;
        double y = dragEvent.getMouseEvent().getComponent().getSize().height - screenRefPoint.y + dy - 1;
        Line ray = view.computeRayFromScreenPoint(x, y);
        Intersection inters[] = globe.intersect(ray, refPos.getElevation());

        if (inters != null)
        {
            // Intersection with globe. Move reference point to the intersection point.
            Position p = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
            dragObject.moveTo(p);
        }
    }

    protected void resizeShape(Object topObject)
    {
        if (!(topObject instanceof ControlPointMarker))
            return;

        // If the terrain beneath the control point is null, then the user is attempting to drag the handle off the
        // globe. This is not a valid state for SurfaceImage, so we will ignore this action but keep the drag operation
        // in effect.
        PickedObject terrainObject = this.wwd.getObjectsAtCurrentPosition().getTerrainObject();
        if (terrainObject == null)
            return;

        Position p = terrainObject.getPosition();

        Angle dLat = p.getLatitude().subtract(this.previousPosition.getLatitude());
        Angle dLon = p.getLongitude().subtract(this.previousPosition.getLongitude());
        LatLon delta = new LatLon(dLat, dLon);

        this.previousPosition = p;

        java.util.List<LatLon> corners = new ArrayList<LatLon>(this.shape.getCorners());
        ControlPointMarker marker = (ControlPointMarker) topObject;
        corners.set(marker.getIndex(), corners.get(marker.getIndex()).add(delta));
        this.shape.setCorners(corners);
    }

    protected void updateAffordances()
    {
        java.util.List<LatLon> corners = this.shape.getCorners();

        double d = LatLon.getAverageDistance(corners).radians * wwd.getModel().getGlobe().getRadius();

        MarkerAttributes markerAttrs =
            new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 0.7, 10, 0.1, d / 30);

        ArrayList<LatLon> handlePositions = new ArrayList<LatLon>(8);
        for (LatLon corner : corners)
        {
            handlePositions.add(corner);
        }

        ArrayList<Marker> handles = new ArrayList<Marker>(handlePositions.size());
        for (int i = 0; i < handlePositions.size(); i++)
        {
            handles.add(new ControlPointMarker(new Position(handlePositions.get(i), 0), markerAttrs, i));
        }

        this.controlPointLayer.setOverrideMarkerElevation(true);
        this.controlPointLayer.setElevation(0);
        this.controlPointLayer.setKeepSeparated(false);
        this.controlPointLayer.setMarkers(handles);
    }

    protected double computeSurfaceElevation(WorldWindow wwd, LatLon latLon)
    {
        SectorGeometryList sgl = wwd.getSceneController().getTerrain();
        if (sgl != null)
        {
            Vec4 point = sgl.getSurfacePoint(latLon.getLatitude(), latLon.getLongitude(), 0.0);
            if (point != null)
            {
                Position pos = wwd.getModel().getGlobe().computePositionFromPoint(point);
                return pos.getElevation();
            }
        }

        return wwd.getModel().getGlobe().getElevation(latLon.getLatitude(), latLon.getLongitude());
    }
}
