/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.util.Logging;

import javax.swing.event.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id: AbstractAirspaceEditor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractAirspaceEditor extends AbstractLayer implements AirspaceEditor
{
    private boolean armed;
    private boolean useRubberBand;
    private boolean keepControlPointsAboveTerrain;
    private AirspaceControlPointRenderer controlPointRenderer;
    private EventListenerList eventListeners = new EventListenerList();
    // List of control points from the last call to draw().
    private ArrayList<AirspaceControlPoint> currentControlPoints = new ArrayList<AirspaceControlPoint>();

    // Airspace altitude constants.
    protected static final int LOWER_ALTITUDE = AirspaceEditorUtil.LOWER_ALTITUDE;
    protected static final int UPPER_ALTITUDE = AirspaceEditorUtil.UPPER_ALTITUDE;

    public AbstractAirspaceEditor(AirspaceControlPointRenderer renderer)
    {
        if (renderer == null)
        {
            String message = Logging.getMessage("nullValue.RendererIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.armed = false;
        this.useRubberBand = true;
        this.keepControlPointsAboveTerrain = false;
        this.controlPointRenderer = renderer;
    }

    public AbstractAirspaceEditor()
    {
        this(new BasicAirspaceControlPointRenderer());
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    public void setArmed(boolean armed)
    {
        this.armed = armed;
    }

    public boolean isUseRubberBand()
    {
        return this.useRubberBand;
    }

    public void setUseRubberBand(boolean state)
    {
        this.useRubberBand = state;
    }

    public boolean isKeepControlPointsAboveTerrain()
    {
        return this.keepControlPointsAboveTerrain;
    }

    public void setKeepControlPointsAboveTerrain(boolean state)
    {
        this.keepControlPointsAboveTerrain = state;
    }

    public AirspaceControlPointRenderer getControlPointRenderer()
    {
        return this.controlPointRenderer;
    }

    public void setControlPointRenderer(AirspaceControlPointRenderer renderer)
    {
        if (renderer == null)
        {
            String message = Logging.getMessage("nullValue.RendererIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.controlPointRenderer = renderer;
    }

    public AirspaceEditListener[] getEditListeners()
    {
        return this.eventListeners.getListeners(AirspaceEditListener.class);
    }

    public void addEditListener(AirspaceEditListener listener)
    {
        this.eventListeners.add(AirspaceEditListener.class, listener);
    }

    public void removeEditListener(AirspaceEditListener listener)
    {
        this.eventListeners.remove(AirspaceEditListener.class, listener);
    }

    //**************************************************************//
    //********************  Control Point Rendering  ***************//
    //**************************************************************//

    protected void doRender(DrawContext dc)
    {
        if (!this.isArmed())
            return;

        this.draw(dc, null);
    }

    protected void doPick(DrawContext dc, Point point)
    {
        if (!this.isArmed())
            return;

        this.draw(dc, point);
    }

    protected void draw(DrawContext dc, Point pickPoint)
    {
        this.getCurrentControlPoints().clear();
        this.assembleControlPoints(dc);

        if (dc.isPickingMode())
        {
            this.getControlPointRenderer().pick(dc, this.getCurrentControlPoints(), pickPoint, this);
        }
        else
        {
            this.getControlPointRenderer().render(dc, this.getCurrentControlPoints());
        }
    }

    protected java.util.List<AirspaceControlPoint> getCurrentControlPoints()
    {
        return this.currentControlPoints;
    }

    protected void setCurrentControlPoints(java.util.List<? extends AirspaceControlPoint> controlPointList)
    {
        this.currentControlPoints.clear();
        this.currentControlPoints.addAll(controlPointList);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void addControlPoint(DrawContext dc, AirspaceControlPoint controlPoint)
    {
        this.currentControlPoints.add(controlPoint);
    }

    protected abstract void assembleControlPoints(DrawContext dc);

    //**************************************************************//
    //********************  Control Point Events  ******************//
    //**************************************************************//

    public void moveAirspaceLaterally(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint)
    {
        // Include this test to ensure any derived implementation performs it.
        if (this.getAirspace() == null || this.getAirspace() != airspace)
        {
            return;
        }

        this.doMoveAirspaceLaterally(wwd, airspace, mousePoint, previousMousePoint);
    }

    public void moveAirspaceVertically(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint)
    {
        // Include this test to ensure any derived implementation performs it.
        if (this.getAirspace() == null || this.getAirspace() != airspace)
        {
            return;
        }

        this.doMoveAirspaceVertically(wwd, airspace, mousePoint, previousMousePoint);
    }

    public AirspaceControlPoint addControlPoint(WorldWindow wwd, Airspace airspace,
        Point mousePoint)
    {
        // Include this test to ensure any derived implementation performs it.
        if (this.getAirspace() == null || this.getAirspace() != airspace)
        {
            return null;
        }

        if (wwd == null || mousePoint == null)
        {
            return null;
        }

        return this.doAddControlPoint(wwd, airspace, mousePoint);
    }

    public void removeControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint)
    {
        // Include this test to ensure any derived implementation performs it.
        if (this.getAirspace() == null)
        {
            return;
        }

        if (wwd == null || controlPoint == null)
        {
            return;
        }

        if (this != controlPoint.getEditor() || this.getAirspace() != controlPoint.getAirspace())
        {
            return;
        }

        this.doRemoveControlPoint(wwd, controlPoint);
    }

    public void moveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint)
    {
        // Include this test to ensure any derived implementation performs it.
        if (this.getAirspace() == null)
        {
            return;
        }

        if (this != controlPoint.getEditor() || this.getAirspace() != controlPoint.getAirspace())
        {
            return;
        }

        this.doMoveControlPoint(wwd, controlPoint, mousePoint, previousMousePoint);
    }

    public void resizeAtControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint)
    {
        // Include this test to ensure any derived implementation performs it.
        if (this.getAirspace() == null)
        {
            return;
        }

        if (this != controlPoint.getEditor() || this.getAirspace() != controlPoint.getAirspace())
        {
            return;
        }

        this.doResizeAtControlPoint(wwd, controlPoint, mousePoint, previousMousePoint);
    }

    protected void fireAirspaceMoved(AirspaceEditEvent e)
    {
        // Iterate over the listener list in reverse order. This has the effect of notifying the listeners in the
        // order they were added.
        AirspaceEditListener[] listeners = this.eventListeners.getListeners(AirspaceEditListener.class);
        for (int i = listeners.length - 1; i >= 0; i--)
        {
            listeners[i].airspaceMoved(e);
        }
    }

    protected void fireAirspaceResized(AirspaceEditEvent e)
    {
        // Iterate over the listener list in reverse order. This has the effect of notifying the listeners in the
        // order they were added.
        AirspaceEditListener[] listeners = this.eventListeners.getListeners(AirspaceEditListener.class);
        for (int i = listeners.length - 1; i >= 0; i--)
        {
            listeners[i].airspaceResized(e);
        }
    }

    protected void fireControlPointAdded(AirspaceEditEvent e)
    {
        // Iterate over the listener list in reverse order. This has the effect of notifying the listeners in the
        // order they were added.
        AirspaceEditListener[] listeners = this.eventListeners.getListeners(AirspaceEditListener.class);
        for (int i = listeners.length - 1; i >= 0; i--)
        {
            listeners[i].controlPointAdded(e);
        }
    }

    protected void fireControlPointRemoved(AirspaceEditEvent e)
    {
        // Iterate over the listener list in reverse order. This has the effect of notifying the listeners in the
        // order they were added.
        AirspaceEditListener[] listeners = this.eventListeners.getListeners(AirspaceEditListener.class);
        for (int i = listeners.length - 1; i >= 0; i--)
        {
            listeners[i].controlPointRemoved(e);
        }
    }

    protected void fireControlPointChanged(AirspaceEditEvent e)
    {
        // Iterate over the listener list in reverse order. This has the effect of notifying the listeners in the
        // order they were added.
        AirspaceEditListener[] listeners = this.eventListeners.getListeners(AirspaceEditListener.class);
        for (int i = listeners.length - 1; i >= 0; i--)
        {
            listeners[i].controlPointChanged(e);
        }
    }

    protected abstract AirspaceControlPoint doAddControlPoint(WorldWindow wwd, Airspace airspace,
        Point mousePoint);

    protected abstract void doRemoveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint);

    protected abstract void doMoveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint);

    protected abstract void doResizeAtControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint);

    //**************************************************************//
    //********************  Default Event Handling  ****************//
    //**************************************************************//

    protected void doMoveAirspaceLaterally(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint)
    {
        // Intersect a ray throuh each mouse point, with a geoid passing through the reference elevation. Since
        // most airspace control points follow a fixed altitude, this will track close to the intended mouse position.
        // If either ray fails to intersect the geoid, then ignore this event. Use the difference between the two
        // intersected positions to move the control point's location.

        if (!(airspace instanceof Movable))
        {
            return;
        }

        Movable movable = (Movable) airspace;
        View view = wwd.getView();
        Globe globe = wwd.getModel().getGlobe();

        Position refPos = movable.getReferencePosition();
        if (refPos == null)
            return;

        // Convert the reference position into a cartesian point. This assumes that the reference elevation is defined
        // by the airspace's lower altitude.
        Vec4 refPoint = null;
        if (airspace.isTerrainConforming()[LOWER_ALTITUDE])
            refPoint = wwd.getSceneController().getTerrain().getSurfacePoint(refPos);
        if (refPoint == null)
            refPoint = globe.computePointFromPosition(refPos);

        // Convert back to a position.
        refPos = globe.computePositionFromPoint(refPoint);

        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousRay = view.computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());

        Vec4 vec = AirspaceEditorUtil.intersectGlobeAt(wwd, refPos.getElevation(), ray);
        Vec4 previousVec = AirspaceEditorUtil.intersectGlobeAt(wwd, refPos.getElevation(), previousRay);

        if (vec == null || previousVec == null)
        {
            return;
        }

        Position pos = globe.computePositionFromPoint(vec);
        Position previousPos = globe.computePositionFromPoint(previousVec);
        LatLon change = pos.subtract(previousPos);

        movable.move(new Position(change.getLatitude(), change.getLongitude(), 0.0));

        this.fireAirspaceMoved(new AirspaceEditEvent(wwd, airspace, this));
    }

    protected void doMoveAirspaceVertically(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint)
    {
        // Find the closest points between the rays through each screen point, and the ray from the control point
        // and in the direction of the globe's surface normal. Compute the elevation difference between these two
        // points, and use that as the change in airspace altitude.
        //
        // If the state keepControlPointsAboveTerrain is set, we prevent the control point from passing any lower than
        // the terrain elevation beneath it.

        if (!(airspace instanceof Movable))
        {
            return;
        }

        Movable movable = (Movable) airspace;
        Position referencePos = movable.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        Vec4 surfaceNormal = wwd.getModel().getGlobe().computeSurfaceNormalAtLocation(referencePos.getLatitude(),
            referencePos.getLongitude());
        Line verticalRay = new Line(referencePoint, surfaceNormal);
        Line screenRay = wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());
        Line previousScreenRay = wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());

        Vec4 pointOnLine = AirspaceEditorUtil.nearestPointOnLine(verticalRay, screenRay);
        Vec4 previousPointOnLine = AirspaceEditorUtil.nearestPointOnLine(verticalRay, previousScreenRay);

        Position pos = wwd.getModel().getGlobe().computePositionFromPoint(pointOnLine);
        Position previousPos = wwd.getModel().getGlobe().computePositionFromPoint(previousPointOnLine);
        double elevationChange = previousPos.getElevation() - pos.getElevation();

        double[] altitudes = this.getAirspace().getAltitudes();
        boolean[] terrainConformance = this.getAirspace().isTerrainConforming();

        if (this.isKeepControlPointsAboveTerrain())
        {
            if (terrainConformance[LOWER_ALTITUDE])
            {
                if (altitudes[LOWER_ALTITUDE] + elevationChange < 0.0)
                    elevationChange = 0.0 - altitudes[LOWER_ALTITUDE];
            }
            else
            {
                double height = AirspaceEditorUtil.computeLowestHeightAboveSurface(
                    wwd, this.getCurrentControlPoints(), LOWER_ALTITUDE);
                if (elevationChange <= -height)
                {
                    elevationChange = -height;
                }
            }
        }

        altitudes[LOWER_ALTITUDE] += elevationChange;
        altitudes[UPPER_ALTITUDE] += elevationChange;
        this.getAirspace().setAltitudes(altitudes[LOWER_ALTITUDE], altitudes[UPPER_ALTITUDE]);

        this.fireAirspaceMoved(new AirspaceEditEvent(wwd, airspace, this));
    }
}
