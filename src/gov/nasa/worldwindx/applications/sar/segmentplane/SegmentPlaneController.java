/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.segmentplane;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.Material;

import java.awt.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: SegmentPlaneController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SegmentPlaneController implements MouseListener, MouseMotionListener, PositionListener
{
    // TODO:
    // the structure of this controller class is very similar to methods in AirspaceEditorController. Consolidate
    // editor controller functionality in a general place.

    private boolean active;
    private SegmentPlaneEditor editor; // Can be null.
    private SegmentPlaneAttributes lastAttributes;
    private WorldWindow wwd; // Can be null.
    // Current selection and device state.
    protected Point mousePoint;
    protected Point lastMousePoint;
    protected PickedObject activePickedObject;

    public SegmentPlaneController(WorldWindow wwd)
    {
        this.active = false;
        this.setWorldWindow(wwd);
    }

    public boolean isActive()
    {
        return this.active;
    }

    protected void setActive(boolean active)
    {
        this.active = active;
    }

    public SegmentPlaneEditor getEditor()
    {
        return this.editor;
    }

    public void setEditor(SegmentPlaneEditor editor)
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
            this.wwd.getInputHandler().removeMouseListener(this);
            this.wwd.getInputHandler().removeMouseMotionListener(this);
            this.wwd.removePositionListener(this);
        }

        this.wwd = wwd;

        if (this.wwd != null)
        {
            this.wwd.getInputHandler().addMouseListener(this);
            this.wwd.getInputHandler().addMouseMotionListener(this);
            this.wwd.addPositionListener(this);
        }
    }

    protected PickedObject getTopOwnedControlPoint()
    {
        if (this.getEditor() == null)
        {
            return null;
        }

        if (this.getWorldWindow().getSceneController().getPickedObjectList() == null)
        {
            return null;
        }

        PickedObject topObject = this.getWorldWindow().getSceneController().getPickedObjectList().getTopPickedObject();
        if (topObject == null || !(topObject.getObject() instanceof SegmentPlane.ControlPoint))
        {
            return null;
        }

        SegmentPlane.ControlPoint controlPoint = (SegmentPlane.ControlPoint) topObject.getObject();
        if (controlPoint.getOwner() != this.getEditor().getSegmentPlane())
        {
            return null;
        }

        return topObject;
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

        this.updateCursor();
        this.updateAttributes();
    }

    public void mousePressed(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }

        PickedObject pickedObject = this.getTopOwnedControlPoint();

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (pickedObject != null)
            {
                this.active = true;
                this.activePickedObject = pickedObject;
                e.consume();
            }
        }

        this.updateCursor();
        this.updateAttributes();
    }

    public void mouseReleased(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (this.active)
            {
                this.active = false;
                this.activePickedObject = null;
                e.consume();
            }
        }

        this.updateCursor();
        this.updateAttributes();
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
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

        this.lastMousePoint = this.mousePoint;
        this.mousePoint = new Point(e.getPoint()); // copy to insulate us from changes by the caller

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }

        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
        {
            if (this.active)
            {
                // Don't update the segment plane here because the World Window current cursor position will not have
                // been updated to reflect the current mouse position. Wait to update in the position listener, but
                // consume the event so the View doesn't respond to it.
                e.consume();
            }
        }

        this.updateCursor();
        this.updateAttributes();
    }

    public void mouseMoved(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.lastMousePoint = this.mousePoint;
        this.mousePoint = new Point(e.getPoint()); // copy to insulate us from changes by the caller

        this.updateCursor();
        this.updateAttributes();
    }

    //**************************************************************//
    //********************  Position Events  ***********************//
    //**************************************************************//

    public void moved(PositionEvent e)
    {
        if (e == null)
        {
            return;
        }

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return;
        }

        if (this.activePickedObject != null)
        {
            this.handleObjectMoved(this.activePickedObject, e.getScreenPoint(), this.lastMousePoint);
        }

        this.updateCursor();
        this.updateAttributes();
    }

    protected void handleObjectMoved(PickedObject object, Point mousePoint, Point lastMousePoint)
    {
        this.getEditor().moveControlPoint(this.getWorldWindow(), object, mousePoint, lastMousePoint);
    }

    //**************************************************************//
    //********************  Action/Cursor Pairing  *****************//
    //**************************************************************//

    protected void updateAttributes()
    {
        if (this.getEditor() == null)
        {
            return;
        }

        if (this.lastAttributes == null)
        {
            this.lastAttributes = this.getEditor().getSegmentPlane().getAttributes();
        }

        SegmentPlaneAttributes actionAttributes = this.getAttributesFor(this.lastAttributes);

        if (actionAttributes != null)
        {
            this.getEditor().getSegmentPlane().setAttributes(actionAttributes);
        }
        else
        {
            this.getEditor().getSegmentPlane().setAttributes(this.lastAttributes);
            this.lastAttributes = null;
        }
    }

    protected void updateCursor()
    {
        Cursor cursor = this.getCursor();

        if (this.wwd instanceof Component)
        {
            ((Component) this.wwd).setCursor(cursor);
        }
    }

    protected SegmentPlaneAttributes getAttributesFor(SegmentPlaneAttributes attributes)
    {
        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return null;
        }

        PickedObject pickedObject = this.isActive() ? this.activePickedObject : this.getTopOwnedControlPoint();
        if (pickedObject == null)
        {
            return null;
        }

        SegmentPlane.ControlPoint controlPoint = (SegmentPlane.ControlPoint) pickedObject.getObject();

        SegmentPlaneAttributes newAttributes = attributes.copy();
        SegmentPlaneAttributes.GeometryAttributes geometryAttributes = newAttributes.getGeometryAttributes(controlPoint.getKey());
        SegmentPlaneAttributes.LabelAttributes labelAttributes = newAttributes.getLabelAttributes(controlPoint.getKey());

        if (geometryAttributes != null)
        {
            Color actionColor = makeBrighter(geometryAttributes.getMaterial().getDiffuse());
            geometryAttributes.setMaterial(new Material(actionColor));
            geometryAttributes.setSize(1.2 * geometryAttributes.getSize());
            geometryAttributes.setPickSize(1.2 * geometryAttributes.getPicksize());
        }

        if (labelAttributes != null)
        {
            Font actionFont = labelAttributes.getFont().deriveFont(Font.BOLD);
            labelAttributes.setFont(actionFont);
        }

        return newAttributes;
    }

    protected Cursor getCursor()
    {
        // If we're actively engaged in some action, then return the cursor associated with that action. Otherwise
        // return the cursor representing the action that would be invoked (if the user pressed the mouse) given the
        // curent modifiers and pick list.

        // Include this test to ensure any derived implementation performs it.
        if (this.getEditor() == null || !this.getEditor().isArmed())
        {
            return null;
        }

        if (this.active)
        {
            if (this.activePickedObject != null)
            {
                return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            }
        }
        else
        {
            if (this.getTopOwnedControlPoint() != null)
            {
                return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            }
        }

        return null;
    }

    private static Color makeBrighter(Color color)
    {
        float[] hsbComponents = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComponents);
        float hue = hsbComponents[0];
        float saturation = hsbComponents[1];
        float brightness = hsbComponents[2];

        saturation /= 3f;
        brightness *= 3f;

        if (saturation < 0f)
            saturation = 0f;

        if (brightness > 1f)
            brightness = 1f;

        int rgbInt = Color.HSBtoRGB(hue, saturation, brightness);

        return new Color(rgbInt);
    }
}
