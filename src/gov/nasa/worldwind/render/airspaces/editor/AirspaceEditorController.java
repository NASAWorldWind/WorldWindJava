/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.airspaces.Airspace;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: AirspaceEditorController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AirspaceEditorController implements KeyListener, MouseListener, MouseMotionListener
{
    private boolean active;
    private String activeAction;
    private AirspaceEditor editor; // Can be null
    private WorldWindow wwd; // Can be null
    // Current selection and device state.
    private Point mousePoint;
    private Airspace activeAirspace;
    private AirspaceControlPoint activeControlPoint;
    // Action/Cursor pairings.
    private Map<String, Cursor> actionCursorMap = new HashMap<String, Cursor>();

    protected static final String MOVE_AIRSPACE_LATERALLY = "AirspaceEdiorController.MoveAirspaceLaterally";
    protected static final String MOVE_AIRSPACE_VERTICALLY = "AirspaceEdiorController.MoveAirspaceVertically";
    protected static final String RESIZE_AIRSPACE = "AirspaceEdiorController.ResizeAirspace";
    protected static final String ADD_CONTROL_POINT = "AirspaceEdiorController.AddControlPoint";
    protected static final String REMOVE_CONTROL_POINT = "AirspaceEdiorController.RemoveControlPoint";
    protected static final String MOVE_CONTROL_POINT = "AirspaceEdiorController.MoveControlPoint";

    // TODO
    // enable/disable individual editor actions
    // 1. add control point
    // 2. remove control point
    // 3. move control point
    // 4. resize
    // 5. move airspace

    // TODO: allow the editor to define the action/behavior associated with a control point, so the correct cursor
    // will be displayed (or some future UI affordance). Currently the controller assumes that a control point implies
    // a move action. This really only affects the cursor display, since the editor ultimately decides what to do when
    // a control point is moved.

    public AirspaceEditorController(WorldWindow wwd)
    {
        this.active = false;
        this.setWorldWindow(wwd);
        this.setupActionCursorMap();
    }

    public AirspaceEditorController()
    {
        this(null);
    }

    public boolean isActive()
    {
        return this.active;
    }

    protected void setActive(boolean active)
    {
        this.active = active;
    }

    public String getActiveAction()
    {
        return activeAction;
    }

    protected void setActiveAction(String action)
    {
        this.activeAction = action;
    }

    public AirspaceEditor getEditor()
    {
        return this.editor;
    }

    public void setEditor(AirspaceEditor editor)
    {
        this.editor = editor;
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow wwd)
    {
        if (this.wwd == wwd)
            return;

        if (this.wwd != null)
        {
            this.wwd.getInputHandler().removeKeyListener(this);
            this.wwd.getInputHandler().removeMouseListener(this);
            this.wwd.getInputHandler().removeMouseMotionListener(this);
        }

        this.wwd = wwd;

        if (this.wwd != null)
        {
            this.wwd.getInputHandler().addKeyListener(this);
            this.wwd.getInputHandler().addMouseListener(this);
            this.wwd.getInputHandler().addMouseMotionListener(this);
        }
    }

    protected Point getMousePoint()
    {
        return this.mousePoint;
    }

    protected void setMousePoint(Point point)
    {
        this.mousePoint = point;
    }

    protected AirspaceControlPoint getActiveControlPoint()
    {
        return this.activeControlPoint;
    }

    protected void setActiveControlPoint(AirspaceControlPoint controlPoint)
    {
        this.activeControlPoint = controlPoint;
    }

    protected Airspace getActiveAirspace()
    {
        return activeAirspace;
    }

    protected void setActiveAirspace(Airspace airspace)
    {
        this.activeAirspace = airspace;
    }

    protected Airspace getTopOwnedAirspaceAtCurrentPosition()
    {
        // Without an editor, we cannot know if the airspace belongs to us.
        if (this.getEditor() == null)
            return null;

        Object obj = this.getTopPickedObject();
        // Airspace is compared by reference, because we're only concerned about the exact reference
        // an editor refers to, rather than an equivalent object.
        if (this.getEditor().getAirspace() != obj)
            return null;

        return (Airspace) obj;
    }

    protected AirspaceControlPoint getTopOwnedControlPointAtCurrentPosition()
    {
        // Without an editor, we cannot know if the airspace belongs to us.
        if (this.getEditor() == null)
            return null;

        Object obj = this.getTopPickedObject();
        if (!(obj instanceof AirspaceControlPoint))
            return null;

        // AirspaceEditor is compared by reference, because we're only concerned about the exact reference
        // a control point refers to, rather than an equivalent object.
        if (this.getEditor() != (((AirspaceControlPoint) obj).getEditor()))
            return null;

        return (AirspaceControlPoint) obj;
    }

    //protected AirspaceControlPoint getFirstOwnedControlPointAtCurrentPosition()
    //{
    //    // Without an editor, we cannot know if the control point belongs to us.
    //    if (this.getEditor() == null)
    //        return null;
    //
    //    PickedObjectList pickedObjects = this.getWorldWindow().getObjectsAtCurrentPosition();
    //    if (pickedObjects == null)
    //        return null;
    //
    //    AirspaceControlPoint controlPoint = null;
    //
    //    for (int i = 0; i < pickedObjects.size() && controlPoint == null; i++)
    //    {
    //        PickedObject po = pickedObjects.get(i);
    //        if (!po.isTerrain() && po.getObject() instanceof AirspaceControlPoint)
    //        {
    //            AirspaceControlPoint pickedPoint = (AirspaceControlPoint) po.getObject();
    //            // Editor is compared by reference, because we're only concerned about the exact reference
    //            // a control point refers to, rather than an equivalent object.
    //            if (this.getEditor() == pickedPoint.getEditor())
    //            {
    //                controlPoint = pickedPoint;
    //            }
    //        }
    //    }
    //
    //    return controlPoint;
    //}
    
    protected Object getTopPickedObject()
    {
        if (this.getWorldWindow() == null)
            return null;

        PickedObjectList pickedObjects = this.getWorldWindow().getObjectsAtCurrentPosition();
        if (pickedObjects == null || pickedObjects.getTopPickedObject() == null
            || pickedObjects.getTopPickedObject().isTerrain())
        {
            return null;
        }

        return pickedObjects.getTopPickedObject().getObject();
    }

    protected Map<String, Cursor> getActionCursorMap()
    {
        return this.actionCursorMap;
    }

    //**************************************************************//
    //********************  Key Events  ****************************//
    //**************************************************************//

    public void keyTyped(KeyEvent e)
    {
    }

    public void keyPressed(KeyEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.updateCursor(e);

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public void keyReleased(KeyEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.updateCursor(e);
        
        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    //**************************************************************//
    //********************  Mouse Events  **************************//
    //**************************************************************//

    public void mouseClicked(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.updateCursor(e);

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }

        AirspaceControlPoint topControlPoint = this.getTopOwnedControlPointAtCurrentPosition();

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (e.isControlDown())
            {
                if (topControlPoint != null)
                {
                    this.handleControlPointRemoved(topControlPoint, e);
                }
                e.consume();
            }
            //else if (e.isAltDown())
            //{
            //    // Actual logic is handled in mousePressed, but we consume the event here to keep the any other
            //    // system from receiving it.
            //    e.consume();
            //}
        }
    }

    public void mousePressed(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.setMousePoint(new Point(e.getPoint())); // copy to insulate us from changes by the caller
        this.updateCursor(e);

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }

        Airspace topAirspace = this.getTopOwnedAirspaceAtCurrentPosition();        
        AirspaceControlPoint topControlPoint = this.getTopOwnedControlPointAtCurrentPosition();

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (e.isControlDown())
            {
                // Actual logic is handled in mouseClicked, but we consume the event here to keep the any other
                // system from receiving it.
                this.setActive(true);
                this.setActiveAction(REMOVE_CONTROL_POINT);
                e.consume();
            }
            else if (e.isAltDown())
            {
                this.setActive(true);
                this.setActiveAction(ADD_CONTROL_POINT);
                if (topControlPoint == null)
                {
                    AirspaceControlPoint p = this.handleControlPointAdded(this.getEditor().getAirspace(), e);
                    if (p != null)
                    {
                        this.setActiveControlPoint(p);
                    }
                }                
                e.consume();
            }
            else
            {
                if (topControlPoint != null)
                {
                    this.setActive(true);
                    this.setActiveAction(null); // Don't know what action we'll perform until mouseDragged().
                    this.setActiveControlPoint(topControlPoint);
                    e.consume();
                }
                else if (topAirspace != null)
                {
                    this.setActive(true);
                    this.setActiveAction(null); // Don't know what action we'll perform until mouseDragged().
                    this.setActiveAirspace(topAirspace);
                    e.consume();
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.setMousePoint(new Point(e.getPoint())); // copy to insulate us from changes by the caller
        this.updateCursor(e);

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (this.isActive())
            {
                this.setActive(false);
                this.setActiveAction(null);
                this.setActiveAirspace(null);
                this.setActiveControlPoint(null);
                e.consume();
            }
        }
    }

    public void mouseEntered(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public void mouseExited(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    protected AirspaceControlPoint handleControlPointAdded(Airspace airspace, MouseEvent mouseEvent)
    {
        AirspaceControlPoint controlPoint = this.getEditor().addControlPoint(this.getWorldWindow(), airspace,
            mouseEvent.getPoint());
        this.getWorldWindow().redraw();

        return controlPoint;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void handleControlPointRemoved(AirspaceControlPoint controlPoint, MouseEvent mouseEvent)
    {
        this.getEditor().removeControlPoint(this.getWorldWindow(), controlPoint);
    }

    //**************************************************************//
    //********************  Mouse Motion Events  *******************//
    //**************************************************************//

    public void mouseDragged(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        Point lastMousePoint = this.getMousePoint();
        this.setMousePoint(new Point(e.getPoint())); // copy to insulate us from changes by the caller
        this.updateCursor(e);

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }
        
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
        {
            if (this.isActive())
            {
                if (this.getActiveControlPoint() != null)
                {
                    this.handleControlPointDragged(this.getActiveControlPoint(), e, lastMousePoint);
                }
                else if (this.getActiveAirspace() != null)
                {
                    this.handleAirspaceDragged(this.getActiveAirspace(), e, lastMousePoint);
                }
                e.consume();
            }
        }
    }

    public void mouseMoved(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.setMousePoint(new Point(e.getPoint())); // copy to insulate us from changes by the caller
        this.updateCursor(e);

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    protected void handleControlPointDragged(AirspaceControlPoint controlPoint, MouseEvent e, Point lastMousePoint)
    {
        if (e.isShiftDown())
        {
            this.setActiveAction(RESIZE_AIRSPACE);
            this.getEditor().resizeAtControlPoint(this.getWorldWindow(), controlPoint, e.getPoint(), lastMousePoint);
        }
        else
        {
            this.setActiveAction(MOVE_CONTROL_POINT);
            this.getEditor().moveControlPoint(this.getWorldWindow(), controlPoint, e.getPoint(), lastMousePoint);
        }
    }

    protected void handleAirspaceDragged(Airspace airspace, MouseEvent e, Point lastMousePoint)
    {
        if (e.isShiftDown())
        {
            this.setActiveAction(MOVE_AIRSPACE_VERTICALLY);
            this.getEditor().moveAirspaceVertically(this.getWorldWindow(), airspace, e.getPoint(), lastMousePoint);
        }
        else
        {
            this.setActiveAction(MOVE_AIRSPACE_LATERALLY);
            this.getEditor().moveAirspaceLaterally(this.getWorldWindow(), airspace, e.getPoint(), lastMousePoint);
        }
    }

    //**************************************************************//
    //********************  Action/Cursor Pairing  *****************//
    //**************************************************************//

    protected void setupActionCursorMap()
    {
        // TODO: find more suitable cursors for the remove control point action, and the move vertically action.
        this.getActionCursorMap().put(MOVE_AIRSPACE_LATERALLY, Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        this.getActionCursorMap().put(MOVE_AIRSPACE_VERTICALLY, Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        this.getActionCursorMap().put(RESIZE_AIRSPACE, Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        this.getActionCursorMap().put(ADD_CONTROL_POINT, Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.getActionCursorMap().put(REMOVE_CONTROL_POINT, Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.getActionCursorMap().put(MOVE_CONTROL_POINT, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    protected void updateCursor(InputEvent e)
    {
        // Include this test to ensure any derived implementation performs it.
        if (e == null || e.getComponent() == null)
        {
            return;
        }

        Cursor cursor = this.getCursorFor(e);
        e.getComponent().setCursor(cursor);
        e.getComponent().repaint();
    }

    protected Cursor getCursorFor(InputEvent e)
    {
        // If we're actively engaged in some action, then return the cursor associated with that action. Otherwise
        // return the cursor representing the action that would be invoked (if the user pressed the mouse) given the
        // current modifiers and pick list.

        if (e == null)
        {
            return null;
        }

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return null;
        }

        String action = this.isActive() ? this.getActiveAction() : this.getPotentialActionFor(e);
        return this.getActionCursorMap().get(action);
    }

    protected String getPotentialActionFor(InputEvent e)
    {
        Airspace topAirspace = this.getTopOwnedAirspaceAtCurrentPosition();
        AirspaceControlPoint topControlPoint = this.getTopOwnedControlPointAtCurrentPosition();

        if (e.isAltDown())
        {
            if (topControlPoint == null)
            {
                return ADD_CONTROL_POINT;
            }
        }
        else if (e.isControlDown())
        {
            if (topControlPoint != null)
            {
                return REMOVE_CONTROL_POINT;
            }
        }
        else if (e.isShiftDown())
        {
            if (topControlPoint != null)
            {
                return RESIZE_AIRSPACE;
            }
            else if (topAirspace != null)
            {
                return MOVE_AIRSPACE_VERTICALLY;
            }
        }
        else
        {
            if (topControlPoint != null)
            {
                return MOVE_CONTROL_POINT;
            }
            else if (topAirspace != null)
            {
                return MOVE_AIRSPACE_LATERALLY;
            }
        }

        return null;
    }
}
