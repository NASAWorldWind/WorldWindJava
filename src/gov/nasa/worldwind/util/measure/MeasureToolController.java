/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.measure;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Generic controller for the <code>MeasureTool</code>.
 *
 * @author Patrick Murris
 * @version $Id: MeasureToolController.java 3090 2015-05-14 21:10:41Z dcollins $
 * @see MeasureTool
 */
public class MeasureToolController extends MouseAdapter
        implements MouseListener, MouseMotionListener, SelectListener, PositionListener, RenderingListener
{
    protected MeasureTool measureTool;

    protected boolean armed = false;
    protected boolean active = false;
    protected boolean moving = false;
    protected boolean useRubberBand = true;
    protected boolean freeHand = false;
    protected double freeHandMinSpacing = 100;

    protected MeasureTool.ControlPoint rubberBandTarget;
    protected MeasureTool.ControlPoint movingTarget;
    protected MeasureTool.ControlPoint lastPickedObject;
    protected BasicDragger dragger;

    /**
     * Set the <code>MeasureTool</code> that this controller will be operating on.
     *
     * @param measureTool the <code>MeasureTool</code> that this controller will be operating on.
     */
    public void setMeasureTool(MeasureTool measureTool)
    {
        if (measureTool == null)
        {
            String msg = Logging.getMessage("nullValue.MeasureToolIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.measureTool = measureTool;
    }

    /**
     * Get the <code>MeasureTool</code> that this controller is operating on.
     *
     * @return the <code>MeasureTool</code> that this controller is operating on.
     */

    public MeasureTool getMeasureTool()
    {
        return this.measureTool;
    }

    /**
     * Returns true if this controller is using rubber band during shape creation. When using rubber band, new control
     * points are added by first pressing the left mouse button, then dragging the mouse toward the proper position,
     * then releasing the mouse button. Otherwise new control point are added for each new click of the mouse.
     *
     * @return true if this controller is using rubber band during shape creation.
     */
    public boolean isUseRubberBand()
    {
        return this.useRubberBand;
    }

    /**
     * Set whether this controller should use rubber band during shape creation. When using rubber band, new control
     * points are added by first pressing the left mouse button, then dragging the mouse toward the proper position,
     * then releasing the mouse button. Otherwise new control point are added for each new click of the mouse.
     *
     * @param state true if this controller should use rubber band during shape creation.
     */
    public void setUseRubberBand(boolean state)
    {
        this.useRubberBand = state;
    }

    /**
     * Get whether this controller allows free hand drawing of path and polygons while using rubber band mode.
     *
     * @return true if free hand drawing of path and polygons in rubber band mode.
     */
    public boolean isFreeHand()
    {
        return this.freeHand;
    }

    /**
     * Set whether this controller allows free hand drawing of path and polygons while using rubber band mode.
     *
     * @param state true to allow free hand drawing of path and polygons in rubber band mode.
     */
    public void setFreeHand(boolean state)
    {
        this.freeHand = state;
    }

    /**
     * Get the minimum distance in meters between two control points for free hand drawing.
     *
     * @return the minimum distance in meters between two control points for free hand drawing.
     */
    public double getFreeHandMinSpacing()
    {
        return this.freeHandMinSpacing;
    }

    /**
     * Set the minimum distance in meters between two control points for free hand drawing.
     *
     * @param distance the minimum distance in meters between two control points for free hand drawing.
     */
    public void setFreeHandMinSpacing(double distance)
    {
        this.freeHandMinSpacing = distance;
    }

    /**
     * Identifies whether the measure tool controller is armed.
     *
     * @return true if armed, false if not armed.
     */
    public boolean isArmed()
    {
        return this.armed;
    }

    /**
     * Arms and disarms the measure tool controller. When armed, the controller monitors user input and builds the
     * shape in response to user actions. When disarmed, the controller ignores all user input.
     *
     * @param armed true to arm the controller, false to disarm it.
     */
    public void setArmed(boolean armed)
    {
        if (this.armed != armed)
        {
            this.armed = armed;
            this.measureTool.firePropertyChange(MeasureTool.EVENT_ARMED, !armed, armed);
        }
    }

    /**
     * Returns true if the controller is in the middle of a rubber band operation.
     *
     * @return true if the controller is in the middle of a rubber band operation.
     */
    public boolean isActive()
    {
        return this.active;
    }

    protected void setActive(boolean state)
    {
        this.active = state;
    }

    /**
     * Returns true if the controller is moving the measure shape as a whole.
     *
     * @return true if the controller is moving the measure shape as a whole.
     */
    public boolean isMoving()
    {
        return this.moving;
    }

    protected void setMoving(boolean state)
    {
        this.moving = state;
    }

    // Handle mouse actions
    public void mousePressed(MouseEvent mouseEvent)
    {
        if (this.isArmed() && this.isUseRubberBand() && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            if ((mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
            {
                if (!mouseEvent.isControlDown())
                {
                    this.setActive(true);

                    if (measureTool.addControlPoint() != null) // null when the cursor is off the globe
                    {
                        if (measureTool.getControlPoints().size() == 1)
                        {
                            measureTool.addControlPoint(); // Simulate a second click
                        }
                        // Set the rubber band target to the last control point or the relevant control for regular shapes.
                        if (measureTool.isRegularShape())
                        {
                            String initControl =
                                measureTool.getShapeInitialControl(measureTool.getWwd().getCurrentPosition());
                            rubberBandTarget = measureTool.getControlPoint(initControl);
                        }
                        else
                        {
                            rubberBandTarget = (MeasureTool.ControlPoint) measureTool.getControlPoints().get(
                                measureTool.getControlPoints().size() - 1);
                        }
                        measureTool.firePropertyChange(MeasureTool.EVENT_RUBBERBAND_START, null, null);
                    }
                }
            }
            mouseEvent.consume();
        }
        else if(!this.isArmed() && mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.isAltDown())
        {
            if (!this.measureTool.isRegularShape())
            {
                this.setMoving(true);
                this.movingTarget = this.lastPickedObject;
            }
            mouseEvent.consume();
        }
    }

    public void mouseReleased(MouseEvent mouseEvent)
    {
        if (this.isArmed() && this.isUseRubberBand() && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            if (measureTool.getPositions().size() == 1)
                measureTool.removeControlPoint();
            this.setActive(false);
            rubberBandTarget = null;
            // Disarm after second control point of a line or regular shape
            autoDisarm();
            mouseEvent.consume();
            measureTool.firePropertyChange(MeasureTool.EVENT_RUBBERBAND_STOP, null, null);
        }
        else if (this.isMoving()  && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            this.setMoving(false);
            this.movingTarget = null;
            mouseEvent.consume();
        }
    }

    // Handle single click for removing control points
    public void mouseClicked(MouseEvent mouseEvent)
    {
        if (measureTool == null)
            return;

        if (this.isArmed() && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            if (mouseEvent.isControlDown())
                measureTool.removeControlPoint();
            else if (!this.isUseRubberBand())
            {
                // Disarm after second control point of a line or regular shape
                if (measureTool.addControlPoint() != null)
                {
                    autoDisarm();
                }
            }
            mouseEvent.consume();
        }
    }


    // Handle mouse motion
    public void mouseDragged(MouseEvent mouseEvent)
    {
        if (measureTool == null)
            return;

        if (this.isActive() && this.isArmed() && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
        {
            // Don't update the control point here because the wwd current cursor position will not
            // have been updated to reflect the current mouse position. Wait to update in the
            // position listener, but consume the event so the view doesn't respond to it.
            mouseEvent.consume();
        }
        else if (!this.isArmed() && this.isMoving()
            && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0
            && mouseEvent.isAltDown())
        {
            // Consume the ALT+Drag mouse event to ensure the View does not respond to it. Don't update the control
            // point here because the wwd current cursor position will not have been updated to reflect the current
            // mouse position. Wait to update in the position listener, but consume the event so the view doesn't
            // respond to it.
            mouseEvent.consume();
        }
    }

    public void mouseMoved(MouseEvent mouseEvent)
    {

    }

    // Handle cursor position change for rubber band
    public void moved(PositionEvent event)
    {
        if (measureTool == null || (!this.active && !this.moving))
            return;

        this.doMoved(event);
    }

    // Handle dragging of control points
    public void selected(SelectEvent event)
    {
        // Ignore select events if the tools is armed, or in a move/rotate action. In either case we don't
        // want to change the currently selected or hightlighted control point.
        if (measureTool == null || (this.isArmed() && this.isUseRubberBand()) || this.isMoving())
            return;

        if (dragger == null)
            dragger = new BasicDragger(measureTool.getWwd());

        // Have rollover events highlight the rolled-over object.
        if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !dragger.isDragging())
        {
            this.highlight(event.getTopObject());
            this.measureTool.getWwd().redraw();
        }

        this.doSelected(event);

        // We missed any roll-over events while dragging, so highlight any under the cursor now,
        // or de-highlight the dragged control point if it's no longer under the cursor.
        if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            PickedObjectList pol = this.measureTool.getWwd().getObjectsAtCurrentPosition();
            if (pol != null)
            {
                this.highlight(pol.getTopObject());
                this.measureTool.getWwd().redraw();
            }
        }
    }

    // Wait for end of rendering to update metrics - length, area...
    public void stageChanged(RenderingEvent event)
    {
        if (measureTool == null)
            return;

        if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP))
        {
            measureTool.firePropertyChange(MeasureTool.EVENT_METRIC_CHANGED, null, null);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doMoved(PositionEvent event)
    {
        if (this.active && rubberBandTarget != null && this.measureTool.getWwd().getObjectsAtCurrentPosition() != null
            && this.measureTool.getWwd().getObjectsAtCurrentPosition().getTerrainObject() != null)
        {
            if (!isFreeHand() || (!measureTool.getMeasureShapeType().equals(MeasureTool.SHAPE_PATH)
                    && !measureTool.getMeasureShapeType().equals(MeasureTool.SHAPE_POLYGON)))
            {
                // Rubber band - Move control point and update shape
                Position lastPosition = rubberBandTarget.getPosition();
                PickedObjectList pol = measureTool.getWwd().getObjectsAtCurrentPosition();
                PickedObject to = pol.getTerrainObject();
                rubberBandTarget.setPosition(new Position(to.getPosition(), 0));
                measureTool.moveControlPoint(rubberBandTarget);
                measureTool.firePropertyChange(MeasureTool.EVENT_POSITION_REPLACE,
                    lastPosition, rubberBandTarget.getPosition());
                measureTool.getWwd().redraw();
            }
            else
            {
                // Free hand - Compute distance from current control point (rubber band target)
                Position lastPosition = rubberBandTarget.getPosition();
                Position newPosition = measureTool.getWwd().getCurrentPosition();
                double distance = LatLon.greatCircleDistance(lastPosition, newPosition).radians
                        * measureTool.getWwd().getModel().getGlobe().getRadius();
                if (distance >= freeHandMinSpacing)
                {
                    // Add new control point
                    if (measureTool.addControlPoint() != null) // null when the cursor is off the globe
                    {
                        rubberBandTarget = (MeasureTool.ControlPoint) getMeasureTool().getControlPoints().get(
                            getMeasureTool().getControlPoints().size() - 1);
                    }
                }
            }
        }
        else if (this.moving && movingTarget != null && measureTool.getWwd().getCurrentPosition() != null)
        {
            // Moving the whole shape
            Position lastPosition = movingTarget.getPosition();
            Position newPosition = measureTool.getWwd().getCurrentPosition();
            this.moveToPosition(lastPosition, newPosition);

            // Update the tool tip to follow the shape as it moves.
            if (measureTool.isShowAnnotation())
                measureTool.updateAnnotation(movingTarget.getPosition());

            measureTool.getWwd().redraw();
        }
    }

    /**
     * Move the shape to the specified new position
     * @param oldPosition Previous position of shape
     * @param newPosition New position for shape
     */
    protected void moveToPosition(Position oldPosition, Position newPosition)
    {
        Angle distanceAngle = LatLon.greatCircleDistance(oldPosition, newPosition);
        Angle azimuthAngle = LatLon.greatCircleAzimuth(oldPosition, newPosition);
        measureTool.moveMeasureShape(azimuthAngle, distanceAngle);
        measureTool.firePropertyChange(MeasureTool.EVENT_POSITION_REPLACE, oldPosition, newPosition);
    }

    protected void doSelected(SelectEvent event)
    {
        if (this.movingTarget != null)
            return;

        if (!event.getEventAction().equals(SelectEvent.DRAG) && !event.getEventAction().equals(SelectEvent.DRAG_END))
            return;

        if (event.getTopObject() == null || !(event.getTopObject() instanceof MeasureTool.ControlPoint) ||
            ((MeasureTool.ControlPoint) event.getTopObject()).getParent() != measureTool)
            return;

        // Have drag events drag the selected object.
        this.dragSelected(event);
    }

    protected void dragSelected(SelectEvent event)
    {
        MeasureTool.ControlPoint point = (MeasureTool.ControlPoint)event.getTopObject();

        LatLon lastPosition = point.getPosition();
        if (point.getValue(MeasureTool.CONTROL_TYPE_LOCATION_INDEX) != null)
            lastPosition = measureTool.getPositions().get((Integer)point.getValue(MeasureTool.CONTROL_TYPE_LOCATION_INDEX));

        // Delegate dragging computations to a dragger.
        this.dragger.selected(event);

        measureTool.moveControlPoint(point);
        if (measureTool.isShowAnnotation())
            measureTool.updateAnnotation(point.getPosition());
        measureTool.firePropertyChange(MeasureTool.EVENT_POSITION_REPLACE,
                lastPosition, point.getPosition());
        measureTool.getWwd().redraw();
    }

    protected void highlight(Object o)
    {
        // Manage highlighting of control points
        if (this.lastPickedObject == o)
            return; // Same thing selected

        // Turn off highlight if on.
        if (this.lastPickedObject != null)
        {
            this.lastPickedObject.getAttributes().setHighlighted(false);
            this.lastPickedObject.getAttributes().setBackgroundColor(null); // use default
            this.lastPickedObject = null;
            if (measureTool.isShowAnnotation())
                measureTool.updateAnnotation(null);
            this.setCursor(null);
        }

        // Turn on highlight if object selected is a control point and belongs to this controller's MeasureTool.
        if (this.lastPickedObject == null && o instanceof MeasureTool.ControlPoint &&
            ((MeasureTool.ControlPoint) o).getParent() == measureTool)
        {
            this.lastPickedObject = (MeasureTool.ControlPoint) o;
            this.lastPickedObject.getAttributes().setHighlighted(true);
            // Highlite using text color
            this.lastPickedObject.getAttributes().setBackgroundColor(
                    this.lastPickedObject.getAttributes().getTextColor());
            if (measureTool.isShowAnnotation())
                measureTool.updateAnnotation(this.lastPickedObject.getPosition());
            this.setCursor(this.lastPickedObject);
        }
    }

    protected void setCursor(MeasureTool.ControlPoint controlPoint)
    {
        // TODO: handle 'rotating' mode cursor is this.isRotating() - when using Alt key on regular shapes
        if (controlPoint == null)
        {
            setComponentCursor(null);
        }
        else
        {
            if (this.measureTool.isRegularShape())
            {
                if (this.measureTool.isCornerControl(controlPoint))
                {
                    Angle azimuth = LatLon.greatCircleAzimuth(controlPoint.getPosition(),
                        this.measureTool.getCenterPosition());
                    // Account for view heading in cursor selection
                    azimuth = azimuth.subtract(this.measureTool.getWwd().getView().getHeading());
                    setComponentCursor(selectResizeCursor(azimuth));
                }
                else if (this.measureTool.isCenterControl(controlPoint))
                {
                    setComponentCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            else
            {
                // Line, path and polygon
                setComponentCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        }
    }

    protected Cursor selectResizeCursor(Angle azimuth)
    {
        while (azimuth.degrees < 0)
            azimuth = azimuth.addDegrees(360);

        if (azimuth.degrees < 22.5)
            return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
        else if (azimuth.degrees < 67.5)
            return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
        else if (azimuth.degrees < 112.5)
            return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
        else if (azimuth.degrees < 157.5)
            return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
        else if (azimuth.degrees < 202.5)
            return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
        else if (azimuth.degrees < 247.5)
            return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
        else if (azimuth.degrees < 292.5)
            return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
        else if (azimuth.degrees < 337.5)
            return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
        else // if (azimuth.degrees < 360)
            return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
    }

    protected void setComponentCursor(Cursor cursor)
    {
        ((Component) this.measureTool.getWwd()).setCursor(cursor != null ? cursor : Cursor.getDefaultCursor());
    }


    protected void autoDisarm()
    {
        // Disarm after second control point of a line or regular shape
        if (measureTool.isRegularShape() || measureTool.getMeasureShapeType().equals(MeasureTool.SHAPE_LINE))
            if (measureTool.getControlPoints().size() > 1)
                this.setArmed(false);
    }

}
