/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.render.airspaces.Box;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.render.markers.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;

/**
 * Provides a user interface for editing a shape and performs editing. Depending on the shape type, the shape is shown
 * with control points for vertex locations and size. All shapes are shown with a handle that provides rotation.
 * <p/>
 * Left-drag on the shape's body moves the whole shape. Left-drag on a control point performs the action associated with
 * that control point. The editor provides vertex insertion and removal for airspace Polygon, Curtain, Route and Track
 * shapes, and SurfacePolygon and SurfacePolyline. Shift-left-click when the cursor is over the shape inserts a control
 * point at the cursor's position. Alt-left-click when the cursor is over a control point removes that control point.
 * Control points are added to the ends of airspace Polygon, Curtain, Route and Track, and SurfacePolyline by
 * shift-left-click on the first or last control point of the shape.
 * <p/>
 * This editor supports airspaces other than Cake and all surface shapes except SurfaceMultiPolygon and SurfaceImage.
 *
 * @author tag
 * @version $Id: ShapeEditor.java 3423 2015-09-23 20:59:03Z tgaskins $
 */
public class ShapeEditor implements SelectListener, PropertyChangeListener
{
    // Control point purposes
    /**
     * Indicates that a control point is associated with annotation.
     */
    protected static String ANNOTATION = "gov.nasa.worldwind.shapeEditor.Annotation";
    /**
     * Indicates a control point is associated with a location.
     */
    protected static String LOCATION = "gov.nasa.worldwind.shapeEditor.Location";
    /**
     * Indicates that a control point is associated with whole-shape rotation.
     */
    protected static String ROTATION = "gov.nasa.worldwind.shapeEditor.Rotation";
    /**
     * Indicates that a control point is associated with width change.
     */
    protected static String WIDTH = "gov.nasa.worldwind.shapeEditor.Width";
    /**
     * Indicates that a control point is associated with height change.
     */
    protected static String HEIGHT = "gov.nasa.worldwind.shapeEditor.Height";
    /**
     * Indicates that a control point is associated with the left width of a shape.
     */
    protected static String LEFT_WIDTH = "gov.nasa.worldwind.shapeEditor.LeftWidth";
    /**
     * Indicates that a control point is associated with the right width of a shape.
     */
    protected static String RIGHT_WIDTH = "gov.nasa.worldwind.shapeEditor.RightWidth";
    /**
     * Indicates that a control point is associated with the inner radius of a shape.
     */
    protected static String INNER_RADIUS = "gov.nasa.worldwind.shapeEditor.InnerRadius";
    /**
     * Indicates that a control point is associated with the outer radius of a shape.
     */
    protected static String OUTER_RADIUS = "gov.nasa.worldwind.shapeEditor.OuterRadius";
    /**
     * Indicates that a control point is associated with the inner minor radius of a shape.
     */
    protected static String INNER_MINOR_RADIUS = "gov.nasa.worldwind.shapeEditor.InnerMinorRadius";
    /**
     * Indicates that a control point is associated with the outer minor radius of a shape.
     */
    protected static String OUTER_MINOR_RADIUS = "gov.nasa.worldwind.shapeEditor.OuterMinorRadius";
    /**
     * Indicates that a control point is associated with the inner major radius of a shape.
     */
    protected static String INNER_MAJOR_RADIUS = "gov.nasa.worldwind.shapeEditor.InnerMajorRadius";
    /**
     * Indicates that a control point is associated with the outer major radius of a shape.
     */
    protected static String OUTER_MAJOR_RADIUS = "gov.nasa.worldwind.shapeEditor.OuterMajorRadius";
    /**
     * Indicates that a control point is associated with the left azimuth of a shape.
     */
    protected static String LEFT_AZIMUTH = "gov.nasa.worldwind.shapeEditor.LeftAzimuth";
    /**
     * Indicates that a control point is associated with the right azimuth of a shape.
     */
    protected static String RIGHT_AZIMUTH = "gov.nasa.worldwind.shapeEditor.RightAzimuth";

    /**
     * Represents editor control points.
     */
    protected static class ControlPointMarker extends BasicMarker
    {
        /**
         * The control point's ID, which is typically its list index when the shape has a list of locations.
         */
        protected int id;
        /**
         * Identifies individual track boxes.
         */
        protected int leg;
        /**
         * Indicates the feature the control point affects.
         */
        protected String purpose; // indicates the feature the control point affects
        /**
         * Indicates size (in meters) if this control point affects a size of the shape, otherwise null.
         */
        protected Double size;
        /**
         * Indicates angle if this control point affects an angle associated with the shape, otherwise null.
         */
        protected Angle rotation;

        public ControlPointMarker(Position position, MarkerAttributes attrs, int id, String purpose)
        {
            super(position, attrs);
            this.id = id;
            this.purpose = purpose;
        }

        public ControlPointMarker(Position position, MarkerAttributes attrs, int id, int leg, String purpose)
        {
            this(position, attrs, id, purpose);

            this.leg = leg;
        }

        public int getId()
        {
            return this.id;
        }

        public int getLeg()
        {
            return leg;
        }

        public String getPurpose()
        {
            return this.purpose;
        }

        public void setSize(double size)
        {
            this.size = size;
        }

        public Double getSize()
        {
            return size;
        }

        public void setRotation(Angle rotation)
        {
            this.rotation = rotation;
        }

        public Angle getRotation()
        {
            return rotation;
        }
    }

    /**
     * Editor state indicating that the shape is not being resized or moved.
     */
    protected static final int NONE = 0;
    /**
     * Editor state indicating that the shape is being moved.
     */
    protected static final int MOVING = 1;
    /**
     * Editor state indicating that the shape is being sized or otherwise respecified.
     */
    protected static final int SIZING = 2;

    /**
     * The {@link gov.nasa.worldwind.WorldWindow} associated with the shape.
     */
    protected final WorldWindow wwd;
    /**
     * The shape associated with the editor. Specified at construction and not subsequently modifiable.
     */
    protected Renderable shape;
    /**
     * The layer holding the editor's control points.
     */
    protected MarkerLayer controlPointLayer;
    /**
     * The layer holding the rotation line and perhaps other affordances.
     */
    protected RenderableLayer accessoryLayer;
    /**
     * The layer holding the control point's annotation.
     */
    protected RenderableLayer annotationLayer;
    /**
     * The layer holding a shadow copy of the shape while the shape is being moved or sized.
     */
    protected RenderableLayer shadowLayer;
    /**
     * The control point annotation.
     */
    protected EditorAnnotation annotation;
    /**
     * The units formatter to use when creating control point annotations.
     */
    protected UnitsFormat unitsFormat;

    /**
     * Indicates whether the editor is ready for editing.
     */
    protected boolean armed;
    /**
     * Indicates whether the editor is in the midst of an editing operation.
     */
    protected boolean active;
    /**
     * Indicates the current editing operation, one of NONE, MOVING or SIZING.
     */
    protected int activeOperation = NONE;
    /**
     * The terrain position associated with the cursor during the just previous drag event.
     */
    protected Position previousPosition = null;
    /**
     * The control point associated with the current sizing operation.
     */
    protected ControlPointMarker currentSizingMarker;
    /**
     * The attributes associated with the shape when the editor is constructed. These are swapped out during editing
     * operations in order to make the shape semi-transparent.
     */
    protected ShapeAttributes originalAttributes;
    /**
     * The highlight attributes associated with the shape when the editor is constructed. These are swapped out during
     * editing operations in order to make the shape semi-transparent.
     */
    protected ShapeAttributes originalHighlightAttributes;

    /**
     * The event most recently recieved by the selection method.
     */
    protected SelectEvent currentEvent;

    /**
     * For shapes without an inherent heading, the current heading established by the editor for the shape.
     */
    protected Angle currentHeading = Angle.ZERO;
    /**
     * Indicates track legs that are adjacent to their previous leg in the track.
     */
    protected List<Box> trackAdjacencyList;

    /**
     * Attributes used to represent shape vertices.
     */
    protected MarkerAttributes locationControlPointAttributes;
    /**
     * Attributes used to represent shape size.
     */
    protected MarkerAttributes sizeControlPointAttributes;
    /**
     * Attributes used to represent shape rotation and other angular features.
     */
    protected MarkerAttributes angleControlPointAttributes;

    /**
     * Indicates whether shapes with sub-segments such as Route and Track may be edited to add and remove legs.
     */
    protected boolean extensionEnabled = true;

    /**
     * Constructs an editor for a specified shape. Once constructed, the editor must be armed to operate. See {@link
     * #setArmed(boolean)}.
     *
     * @param wwd           the {@link gov.nasa.worldwind.WorldWindow} associated with the specified shape.
     * @param originalShape the shape to edit.
     *
     * @throws java.lang.IllegalArgumentException if either the specified world window or shape is null.
     */
    public ShapeEditor(WorldWindow wwd, Renderable originalShape)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().log(java.util.logging.Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (originalShape == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().log(java.util.logging.Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (!(originalShape instanceof Movable2))
        {
            String msg = Logging.getMessage("generic.Movable2NotSupported");
            Logging.logger().log(java.util.logging.Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (!(originalShape instanceof Attributable))
        {
            String msg = Logging.getMessage("generic.AttributableNotSupported");
            Logging.logger().log(java.util.logging.Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.shape = originalShape;
        this.originalAttributes = ((Attributable) this.getShape()).getAttributes();

        // Create a layer to hold the control points.
        this.controlPointLayer = new MarkerLayer();
        this.controlPointLayer.setKeepSeparated(false);
        this.controlPointLayer.setValue(AVKey.IGNORE, true); // means "Don't show this layer in the layer manager."
        if (this.shape instanceof SurfaceShape
            || (this.shape instanceof Airspace && ((Airspace) this.shape).isDrawSurfaceShape()))
        {
            // This ensures that control points are always placed on the terrain for surface shapes.
            this.controlPointLayer.setOverrideMarkerElevation(true);
            this.controlPointLayer.setElevation(0);
        }

        // Create a layer to hold the rotation line and any other affordances.
        this.accessoryLayer = new RenderableLayer();
        this.accessoryLayer.setPickEnabled(false);
        this.accessoryLayer.setValue(AVKey.IGNORE, true);

        // Set up the Path for the rotation line.
        ShapeAttributes lineAttrs = new BasicShapeAttributes();
        lineAttrs.setOutlineMaterial(Material.GREEN);
        lineAttrs.setOutlineWidth(2);
        java.util.List<Position> lineLocations = new ArrayList<Position>(2);
        lineLocations.add(Position.ZERO);
        lineLocations.add(Position.ZERO);
        Path rotationLine = new Path(lineLocations);
        rotationLine.setFollowTerrain(true);
        rotationLine.setPathType(AVKey.GREAT_CIRCLE);
        rotationLine.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        rotationLine.setAttributes(lineAttrs);
        this.accessoryLayer.addRenderable(rotationLine);

        // Create a layer to hold the editing annotations.
        this.annotationLayer = new RenderableLayer();
        this.annotationLayer.setPickEnabled(false);
        this.annotationLayer.setValue(AVKey.IGNORE, true);

        // Create the annotation.
        this.annotation = new EditorAnnotation("");
        this.annotationLayer.addRenderable(this.annotation);

        // Create a layer to hold the shadow shape, the shape that shows the state before an editing operation.
        this.shadowLayer = new RenderableLayer();
        this.shadowLayer.setPickEnabled(false);
        this.shadowLayer.setValue(AVKey.IGNORE, true);

        // Create a units formatter for the annotations.
        this.unitsFormat = new UnitsFormat();
        this.unitsFormat.setFormat(UnitsFormat.FORMAT_LENGTH, " %,12.3f %s");

        // Create the attributes assigned to the control points.
        this.makeControlPointAttributes();
    }

    protected void makeControlPointAttributes()
    {
        // Each attribute has color, marker type, opacity, size in pixels, and minimum size in meters (0 indicates that
        // the minimum size is not considered.
        this.locationControlPointAttributes = new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.SPHERE, 0.7, 10,
            0);
        this.sizeControlPointAttributes = new BasicMarkerAttributes(Material.CYAN, BasicMarkerShape.SPHERE, 0.7, 10, 0);
        this.angleControlPointAttributes = new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.SPHERE, 0.7, 10,
            0);
    }

    /**
     * Indicates the units formatter associated with this editor.
     *
     * @return the units formatter associated with this editor.
     */
    public UnitsFormat getUnitsFormat()
    {
        return unitsFormat;
    }

    /**
     * Specifies the units formatter to use when creating editor annotations.
     *
     * @param unitsFormat the units formatter to use. A default is created if null is specified.
     */
    public void setUnitsFormat(UnitsFormat unitsFormat)
    {
        this.unitsFormat = unitsFormat != null ? unitsFormat : new UnitsFormat();
    }

    /**
     * Indicates the World Window associated with this editor.
     *
     * @return the World Window associated with this editor.
     */
    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    /**
     * Indicates the shape associated with this editor.
     *
     * @return the shape associated with this editor.
     */
    public Renderable getShape()
    {
        return this.shape;
    }

    /**
     * Indicates the control point layer used by this editor.
     *
     * @return the control point layer used by this editor.
     */
    public MarkerLayer getControlPointLayer()
    {
        return controlPointLayer;
    }

    /**
     * Indicates the accessory layer used by this editor.
     *
     * @return the accessory layer used by this editor.
     */
    public RenderableLayer getAccessoryLayer()
    {
        return accessoryLayer;
    }

    /**
     * Indicates the annotation layer used by this editor.
     *
     * @return the annotation layer used by this editor.
     */
    public RenderableLayer getAnnotationLayer()
    {
        return annotationLayer;
    }

    /**
     * Indicates the shadow layer used by this editor.
     *
     * @return the shadow layer used by this editor.
     */
    public RenderableLayer getShadowLayer()
    {
        return shadowLayer;
    }

    /**
     * Indicates the annotation used to show locations and  measurements.
     *
     * @return the annotation used to show shape locations and measurements.
     */
    public EditorAnnotation getAnnotation()
    {
        return annotation;
    }

    /**
     * Indicates whether an editing operation is currently underway. Operations are SIZING and MOVING.
     *
     * @return true if an operation is underway, otherwise false.
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * Indicates the current operation being performed, either SIZING, MOVING or NONE.
     *
     * @return the current operation underway.
     */
    public int getActiveOperation()
    {
        return activeOperation;
    }

    /**
     * Returns the geographic position associated with the previous select event.
     *
     * @return the geographic position associated with the previous select event.
     */
    public Position getPreviousPosition()
    {
        return previousPosition;
    }

    /**
     * Indicates the control point currently used in the operation underway.
     *
     * @return the control point used in the operation currently underway.
     */
    public ControlPointMarker getCurrentSizingMarker()
    {
        return currentSizingMarker;
    }

    /**
     * Indicates the attributes associated with the shape when the editor was created.
     *
     * @return the attributes associated with the shape when the editor was created.
     */
    public ShapeAttributes getOriginalAttributes()
    {
        return originalAttributes;
    }

    /**
     * Indicates the highlight attributes associated with the shape prior to their being changed to achieve shape
     * transparency.
     *
     * @return the original highlight attributes.
     */
    public ShapeAttributes getOriginalHighlightAttributes()
    {
        return originalHighlightAttributes;
    }

    /**
     * Indicates the current rotation heading. This is updated as shapes are rotated.
     *
     * @return the current rotation heading.
     */
    public Angle getCurrentHeading()
    {
        return currentHeading;
    }

    /**
     * Indicates the attributes associated with location control points.
     *
     * @return the attributes associated with location control points.
     */
    public MarkerAttributes getLocationControlPointAttributes()
    {
        return locationControlPointAttributes;
    }

    /**
     * Indicates the attributes associated with size control points.
     *
     * @return the attributes associated with size control points.
     */
    public MarkerAttributes getSizeControlPointAttributes()
    {
        return sizeControlPointAttributes;
    }

    /**
     * Indicates the attributes associated with angle control points.
     *
     * @return the attributes associated with angle control points.
     */
    public MarkerAttributes getAngleControlPointAttributes()
    {
        return angleControlPointAttributes;
    }

    /**
     * Indicates whether multi-segment shapes such as Route and Track may be edited to add or remove segments.
     * @return true if segment addition and deletion are enabled, otherwise false. The default is true.
     */
    public boolean isExtensionEnabled()
    {
        return extensionEnabled;
    }

    /**
     * Specifies whether multi-segment shapes such as Route and Track may be edited to add or remove segments.
     *
     * @param extensionEnabled true to allow segment addition and removal, otherwise false.
     */
    public void setExtensionEnabled(boolean extensionEnabled)
    {
        this.extensionEnabled = extensionEnabled;
    }

    /**
     * Indicates the event most recently passed to the select handler.
     *
     * @return the event most recently passed to the select handler.
     */
    public SelectEvent getCurrentEvent()
    {
        return currentEvent;
    }

    /**
     * Indicates whether this editor is armed.
     *
     * @return <code>true</code> if the editor is armed, otherwise <code>false</code>.
     */
    public boolean isArmed()
    {
        return this.armed;
    }

    /**
     * Arms or disarms the editor. When armed, the editor's shape is displayed with control points and other affordances
     * that indicate possible editing operations.
     *
     * @param armed <code>true</code> to arm the editor, <code>false</code> to disarm it and remove the control points
     *              and other affordances. This method must be called when the editor is no longer needed so that the
     *              editor may remove the resources it created when it was armed.
     */
    public void setArmed(boolean armed)
    {
        if (!this.isArmed() && armed)
        {
            this.enable();
        }
        else if (this.isArmed() && !armed)
        {
            this.disable();
        }

        this.armed = armed;
    }

    /**
     * Called by {@link #setArmed(boolean)} to initialize this editor.
     */
    protected void enable()
    {
        LayerList layers = this.getWwd().getModel().getLayers();

        if (!layers.contains(this.getControlPointLayer()))
            layers.add(this.getControlPointLayer());

        if (!this.getControlPointLayer().isEnabled())
            this.getControlPointLayer().setEnabled(true);

        if (!layers.contains(this.getAccessoryLayer()))
            layers.add(this.getAccessoryLayer());

        if (!this.getAccessoryLayer().isEnabled())
            this.getAccessoryLayer().setEnabled(true);

        if (!layers.contains(this.getAnnotationLayer()))
            layers.add(this.getAnnotationLayer());

        if (!layers.contains(this.getShadowLayer()))
            layers.add(0, this.getShadowLayer());
        this.getShadowLayer().setEnabled(true);

        if (this.getShape() instanceof TrackAirspace)
            this.determineTrackAdjacency();

        this.updateControlPoints();

        this.getWwd().addSelectListener(this);
        this.getWwd().getSceneController().addPropertyChangeListener(this);
    }

    /**
     * Called by {@link #setArmed(boolean)} to remove resources no longer needed after editing.
     */
    protected void disable()
    {
        LayerList layers = this.getWwd().getModel().getLayers();

        layers.remove(this.getControlPointLayer());
        layers.remove(this.getAccessoryLayer());
        layers.remove(this.getAnnotationLayer());
        layers.remove(this.getShadowLayer());

        getWwd().removeSelectListener(this);
        getWwd().getSceneController().removePropertyChangeListener(this);

        ((Component) this.getWwd()).setCursor(null);
    }

    /**
     * Determines and stores internally the adjacency of successive track legs. Called during editor arming.
     */
    protected void determineTrackAdjacency()
    {
        if (this.trackAdjacencyList == null)
            this.trackAdjacencyList = new ArrayList<Box>();
        else
            this.trackAdjacencyList.clear();

        TrackAirspace track = (TrackAirspace) this.getShape();
        List<Box> legs = track.getLegs();
        for (int i = 1; i < legs.size(); i++)
        {
            boolean adjacent = legs.get(i - 1).getLocations()[1].equals(legs.get(i).getLocations()[0]);
            if (adjacent)
                this.trackAdjacencyList.add(legs.get(i));
        }
    }

    /**
     * The select handler, the method called when the user selects (rolls over, left clicks, etc.) the shape or a
     * control point. Does not necessarily indicate the shape associated with this editor.
     *
     * @param event the select event indicating what was selected and the geographic location under the cursor.
     */
    public void selected(SelectEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        this.currentEvent = event;

        if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            this.active = false;
            this.activeOperation = NONE;
            this.previousPosition = null;
            ((Component) this.getWwd()).setCursor(null);
            this.removeShadowShape();
            this.updateAnnotation(null);
        }
        else if (event.getEventAction().equals(SelectEvent.ROLLOVER))
        {
            if (!(this.getWwd() instanceof Component))
                return;

            // Update the cursor.
            Cursor cursor = null;
            if (this.activeOperation == MOVING)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            else if (this.getActiveOperation() == SIZING)
                cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            else if (event.getTopObject() != null && event.getTopObject() == this.getShape())
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            else if (event.getTopObject() != null && event.getTopObject() instanceof Marker)
                cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

            ((Component) this.getWwd()).setCursor(cursor);

            // Update the shape or control point annotation.
            if (this.getActiveOperation() == MOVING && event.getTopObject() == this.getShape())
                this.updateShapeAnnotation();
            else if (this.getActiveOperation() == SIZING)
                this.updateAnnotation(this.getCurrentSizingMarker());
            else if (event.getTopObject() != null && event.getTopObject() == this.getShape())
                this.updateShapeAnnotation();
            else if (event.getTopObject() != null && event.getTopObject() instanceof ControlPointMarker)
                this.updateAnnotation((ControlPointMarker) event.getTopObject());
            else
                this.updateAnnotation(null);
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_PRESS))
        {
            // Prepare for a drag.
            this.active = true;
            PickedObjectList objectsUnderCursor = this.getWwd().getObjectsAtCurrentPosition();
            if (objectsUnderCursor != null)
            {
                PickedObject terrainObject = objectsUnderCursor.getTerrainObject();
                if (terrainObject != null)
                    this.previousPosition = terrainObject.getPosition();
            }
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            Object topObject = event.getTopObject();
            if (topObject == null)
                return;

            // Add and delete control points.
            if (event.getTopPickedObject().getParentLayer() == this.getControlPointLayer())
            {
                this.reshapeShape((ControlPointMarker) topObject);
                this.updateControlPoints();
                this.updateAnnotation(this.getCurrentSizingMarker());
                event.consume();
            }
            else if ((event.getTopObject() == this.getShape()) &&
                (this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0)
            {
                this.reshapeShape(null);
                this.updateControlPoints();
                event.consume();
            }
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            if (!this.isActive())
                return;

            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null)
                return;

            if (this.getActiveOperation() == NONE) // drag is starting
                this.makeShadowShape();

            if (topObject == this.getShape() || this.getActiveOperation() == MOVING)
            {
                // Move the whole shape.
                this.activeOperation = MOVING;
                this.dragWholeShape(dragEvent);
                this.updateControlPoints();
                this.updateShapeAnnotation();
                event.consume();
            }
            else if (dragEvent.getTopPickedObject().getParentLayer() == this.getControlPointLayer()
                || this.getActiveOperation() == SIZING)
            {
                // Perform the editing operation associated with the selected control point.
                this.activeOperation = SIZING;
                this.reshapeShape((ControlPointMarker) topObject);
                this.updateControlPoints();
                this.updateAnnotation(this.getCurrentSizingMarker());
                event.consume();
            }

            this.getWwd().redraw(); // update the display
        }
    }

    /**
     * The property change listener, the method called when a property of the Scene Controller changes
     * (vertical exaggeration, etc.). Does not necessarily indicate a property associated with this editor.
     *
     * @param event the property change event indicating the property name and its associated value.
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (event.getPropertyName().equals(AVKey.VERTICAL_EXAGGERATION))
        {
            // The orientation line altitudes depend on the vertical exaggeration.
            this.updateControlPoints();
        }
    }

    /**
     * Creates the shape that will remain at the same location and is the same size as the shape to be edited.
     */
    protected void makeShadowShape()
    {
        Renderable shadowShape = this.doMakeShadowShape();
        if (shadowShape == null)
            return;

        if (this.getShape() instanceof Airspace)
            ((Airspace) this.getShape()).setAlwaysOnTop(true);

        // Reduce the opacity of an opaque current shape so that the shadow shape is visible while editing
        // is performed.

        this.originalAttributes = ((Attributable) this.getShape()).getAttributes();
        this.originalHighlightAttributes = ((Attributable) this.getShape()).getHighlightAttributes();

        ShapeAttributes editingAttributes = new BasicShapeAttributes(this.originalAttributes);
        if (editingAttributes.getInteriorOpacity() == 1)
            editingAttributes.setInteriorOpacity(0.7);

        ((Attributable) this.getShape()).setAttributes(editingAttributes);
        ((Attributable) this.getShape()).setHighlightAttributes(editingAttributes);

        this.getShadowLayer().addRenderable(shadowShape);

        if (this.getShape() instanceof Airspace)
        {
            double[] altitudes = ((Airspace) shadowShape).getAltitudes();
            ((Airspace) shadowShape).setAltitudes(altitudes[0], 0.95 * altitudes[1]);
//
//            // Show only the outline of the shadow shape.
//            AirspaceAttributes shadowAttributes = new BasicAirspaceAttributes(this.originalAttributes);
//            shadowAttributes.setDrawInterior(false);
//            ((Airspace)shadowShape).setAttributes(shadowAttributes);
//            ((Airspace)shadowShape).setHighlightAttributes(shadowAttributes);
        }
    }

    /**
     * Remove the shadow shape.
     */
    protected void removeShadowShape()
    {
        this.getShadowLayer().removeAllRenderables();
        if (this.getShape() instanceof AbstractAirspace)
            ((AbstractAirspace) this.getShape()).setAlwaysOnTop(false);

        // Restore the original attributes.
        if (this.getOriginalAttributes() != null)
        {
            ((Attributable) this.getShape()).setAttributes(this.getOriginalAttributes());
            ((Attributable) this.getShape()).setHighlightAttributes(this.getOriginalHighlightAttributes());
        }

        this.getWwd().redraw();
    }

    /**
     * Creates and returns the stationary shape displayed during editing operations. Subclasses should override this
     * method to create shadow shapes appropriate to the editor's shape.
     *
     * @return the new shadow shape created, or null if the shape type is not recognized.
     */
    protected Renderable doMakeShadowShape()
    {
        if (this.getShape() instanceof Polygon)
            return new Polygon((Polygon) this.getShape());
        else if (this.getShape() instanceof PartialCappedCylinder)
            return new PartialCappedCylinder((PartialCappedCylinder) this.getShape());
        else if (this.getShape() instanceof CappedCylinder)
            return new CappedCylinder((CappedCylinder) this.getShape());
        else if (this.getShape() instanceof CappedEllipticalCylinder)
            return new CappedEllipticalCylinder((CappedEllipticalCylinder) this.getShape());
        else if (this.getShape() instanceof Orbit)
            return new Orbit((Orbit) this.getShape());
        else if (this.getShape() instanceof Route)
            return new Route((Route) this.getShape());
        else if (this.getShape() instanceof Curtain)
            return new Curtain((Curtain) this.getShape());
        else if (this.getShape() instanceof SphereAirspace)
            return new SphereAirspace((SphereAirspace) this.getShape());
        else if (this.getShape() instanceof TrackAirspace)
            return new TrackAirspace((TrackAirspace) this.getShape());
        else if (this.getShape() instanceof SurfaceSquare)
            return new SurfaceSquare((SurfaceSquare) this.getShape());
        else if (this.getShape() instanceof SurfaceQuad)
            return new SurfaceQuad((SurfaceQuad) this.getShape());
        else if (this.getShape() instanceof SurfaceCircle)
            return new SurfaceCircle((SurfaceCircle) this.getShape());
        else if (this.getShape() instanceof SurfaceEllipse)
            return new SurfaceEllipse((SurfaceEllipse) this.getShape());
        else if (this.getShape() instanceof SurfacePolyline)
            return new SurfacePolyline((SurfacePolyline) this.getShape());
        else if (this.getShape() instanceof SurfacePolygon)
            return new SurfacePolygon((SurfacePolygon) this.getShape());

        return null;
    }

    /**
     * Performs shape-specific minor modifications to shapes after editing operation are performed. Some editing
     * operations cause positions that are originally identical to become slightly different and thereby disrupt the
     * original connectivity of the shape. This is the case for track-airspace legs, for instance. This method is called
     * just after editing operations are performed in order to give the editor a chance to reform connectivity or
     * otherwise modify the shape to retain its original properties. Subclasses should override this method if they are
     * aware of shapes other than those recognized by default and those shapes need such adjustment during editing.
     */
    protected void adjustShape()
    {
        if (this.getShape() instanceof TrackAirspace)
            this.adjustTrackShape();
    }

    /**
     * Restores adjacency of {@link gov.nasa.worldwind.render.airspaces.TrackAirspace} shapes. Called by {@link
     * #adjustShape()}.
     */
    protected void adjustTrackShape()
    {
        TrackAirspace track = (TrackAirspace) this.getShape();

        List<Box> legs = track.getLegs();
        if (legs == null)
            return;

        // Start with the second leg and restore coincidence of the first leg position with that of the previous leg.
        for (int i = 1; i < legs.size(); i++)
        {
            Box leg = legs.get(i);

            if (this.trackAdjacencyList.contains(legs.get(i)))
            {
                leg.setLocations(legs.get(i - 1).getLocations()[1], leg.getLocations()[1]);
            }
        }
    }

    /**
     * Moves the entire shape according to a specified drag event.
     *
     * @param dragEvent the event initiating the move.
     */
    protected void dragWholeShape(DragSelectEvent dragEvent)
    {
        Movable2 dragObject = (Movable2) this.getShape();

        View view = getWwd().getView();
        Globe globe = getWwd().getModel().getGlobe();

        // Compute ref-point position in screen coordinates.
        Position refPos = dragObject.getReferencePosition();
        if (refPos == null)
            return;

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
            dragObject.moveTo(getWwd().getModel().getGlobe(), new Position(p,
                ((Movable2) this.getShape()).getReferencePosition().getAltitude()));
        }

        this.adjustShape();
    }

    /**
     * Modifies the shape's locations, size or rotation. This method is called when a control point is dragged.
     *
     * @param controlPoint the control point selected.
     */
    protected void reshapeShape(ControlPointMarker controlPoint)
    {
        this.currentSizingMarker = controlPoint;

        // If the terrain beneath the control point is null, then the user is attempting to drag the handle off the
        // globe. This is not a valid state, so we ignore this action but keep the drag operation in effect.
        PickedObjectList objectsUnderCursor = this.getWwd().getObjectsAtCurrentPosition();
        if (objectsUnderCursor == null)
            return;

        PickedObject terrainObject = this.getWwd().getObjectsAtCurrentPosition().getTerrainObject();
        if (terrainObject == null)
            return;

        if (this.getPreviousPosition() == null)
        {
            this.previousPosition = terrainObject.getPosition();
            return;
        }

        // Perform the editing operation.
        this.doReshapeShape(controlPoint, terrainObject.getPosition());

        this.previousPosition = terrainObject.getPosition();

        this.adjustShape();
    }

    /**
     * Called by {@link #reshapeShape(ShapeEditor.ControlPointMarker)} to perform the actual shape modification.
     * Subclasses should override this method if they provide editing for shapes other than those supported by the basic
     * editor.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void doReshapeShape(ControlPointMarker controlPoint, Position terrainPosition)
    {
        if (this.getShape() instanceof Airspace)
        {
            if (this.getShape() instanceof Polygon || this.getShape() instanceof Curtain)
                this.reshapePolygonAirspace(terrainPosition, controlPoint);
            else if (this.getShape() instanceof CappedCylinder)
                this.reshapeCappedCylinder(terrainPosition, controlPoint);
            else if (this.getShape() instanceof CappedEllipticalCylinder)
                this.reshapeCappedEllipticalCylinder(terrainPosition, controlPoint);
            else if (this.getShape() instanceof Orbit)
                this.reshapeOrbit(terrainPosition, controlPoint);
            else if (this.getShape() instanceof Route)
                this.reshapeRoute(terrainPosition, controlPoint);
            else if (this.getShape() instanceof SphereAirspace)
                this.reshapeSphere(terrainPosition, controlPoint);
            else if (this.getShape() instanceof TrackAirspace)
                this.reshapeTrack(terrainPosition, controlPoint);
        }
        else if (this.getShape() instanceof SurfaceShape)
        {
            if (this.getShape() instanceof SurfacePolygon)
                this.reshapeSurfacePolygon(terrainPosition, controlPoint);
            else if (this.getShape() instanceof SurfacePolyline)
                this.reshapeSurfacePolygon(terrainPosition, controlPoint);
            else if (this.getShape() instanceof SurfaceCircle)
                this.reshapeSurfaceCircle(terrainPosition, controlPoint);
            else if (this.getShape() instanceof SurfaceSquare)
                this.reshapeSurfaceSquare(terrainPosition, controlPoint);
            else if (this.getShape() instanceof SurfaceQuad)
                this.reshapeSurfaceQuad(terrainPosition, controlPoint);
            else if (this.getShape() instanceof SurfaceEllipse)
                this.reshapeSurfaceEllipse(terrainPosition, controlPoint);
        }
    }

    /**
     * Updates the control points to the locations of the currently edited shape. Called each time a modification to the
     * shape is made. Subclasses should override this method to handle shape types not supported by the basic editor.
     */
    protected void updateControlPoints()
    {
        if (this.getShape() instanceof Airspace)
        {
            if (this.getShape() instanceof Polygon || this.getShape() instanceof Curtain)
                this.updatePolygonAirspaceControlPoints();
            else if (this.getShape() instanceof PartialCappedCylinder)
                this.updatePartialCappedCylinderControlPoints();
            else if (this.getShape() instanceof CappedCylinder)
                this.updateCappedCylinderControlPoints();
            else if (this.getShape() instanceof CappedEllipticalCylinder)
                this.updateCappedEllipticalCylinderControlPoints();
            else if (this.getShape() instanceof Orbit)
                this.updateOrbitControlPoints();
            else if (this.getShape() instanceof Route)
                this.updateRouteControlPoints();
            else if (this.getShape() instanceof SphereAirspace)
                this.updateSphereControlPoints();
            else if (this.getShape() instanceof TrackAirspace)
                this.updateTrackControlPoints();
        }
        else if (this.getShape() instanceof SurfaceShape)
        {
            if (this.getShape() instanceof SurfacePolygon || this.getShape() instanceof SurfacePolyline)
                this.updateSurfacePolygonControlPoints();
            else if (this.getShape() instanceof SurfaceCircle)
                this.updateSurfaceCircleControlPoints();
            else if (this.getShape() instanceof SurfaceSquare)
                this.updateSurfaceSquareControlPoints();
            else if (this.getShape() instanceof SurfaceQuad)
                this.updateSurfaceQuadControlPoints();
            else if (this.getShape() instanceof SurfaceEllipse)
                this.updateSurfaceEllipseControlPoints();
        }
    }

    /**
     * Updates the annotation indicating the edited shape's center. If the shape has no designated center, this method
     * prevents the annotation from displaying.
     */
    protected void updateShapeAnnotation()
    {
        LatLon center = this.getShapeCenter();

        if (center != null)
        {
            ControlPointMarker dummyMarker = this.makeControlPoint(new Position(center, 0), new BasicMarkerAttributes(),
                0, ANNOTATION);
            this.updateAnnotation(dummyMarker);
        }
        else
        {
            this.updateAnnotation(null);
        }
    }

    /**
     * Returns the shape's center location, or null if it has no designated center.
     *
     * @return the shape's center location, or null if the shape has no designated center.
     */
    protected LatLon getShapeCenter()
    {
        LatLon center = null;

        if (this.getShape() instanceof CappedCylinder)
            center = ((CappedCylinder) this.getShape()).getCenter();
        else if (this.getShape() instanceof CappedEllipticalCylinder)
            center = ((CappedEllipticalCylinder) this.getShape()).getCenter();
        else if (this.getShape() instanceof SphereAirspace)
            center = ((SphereAirspace) this.getShape()).getLocation();
        else if (this.getShape() instanceof SurfaceEllipse)
            center = ((SurfaceEllipse) this.getShape()).getCenter();
        else if (this.getShape() instanceof SurfaceQuad)
            center = ((SurfaceQuad) this.getShape()).getCenter();

        return center;
    }

    /**
     * Updates the annotation associated with a specified control point.
     *
     * @param controlPoint the control point.
     */
    protected void updateAnnotation(ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
        {
            this.getAnnotationLayer().setEnabled(false);
            return;
        }

        this.getAnnotationLayer().setEnabled(true);
        this.getAnnotation().setPosition(controlPoint.getPosition());

        String annotationText;
        if (controlPoint.size != null)
            annotationText = this.unitsFormat.length(null, controlPoint.size);
        else if (controlPoint.rotation != null)
            annotationText = this.unitsFormat.angle(null, controlPoint.rotation);
        else
            annotationText = this.unitsFormat.latLon2(controlPoint.getPosition());

        this.getAnnotation().setText(annotationText);
    }

    /**
     * Updates the line designating the shape's central axis.
     *
     * @param centerPosition the shape's center location and altitude at which to place one of the line's end points.
     * @param controlPoint   the shape orientation control point.
     */
    protected void updateOrientationLine(Position centerPosition, Position controlPoint)
    {
        Path rotationLine = (Path) this.getAccessoryLayer().getRenderables().iterator().next();

        double cAltitude = centerPosition.getAltitude();
        double rAltitude = controlPoint.getAltitude();
        if (this.getShapeAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
        {
            rotationLine.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            rotationLine.setFollowTerrain(true);

            // Set the line's altitude relative to the ground.
            cAltitude = centerPosition.getAltitude() - this.getWwd().getModel().getGlobe().getElevation(
                centerPosition.getLatitude(), centerPosition.getLongitude());
            rAltitude = controlPoint.getAltitude() - this.getWwd().getModel().getGlobe().getElevation(
                controlPoint.getLatitude(), controlPoint.getLongitude());
            // Path does not incorporate vertical exaggeration, but airspace shapes do. Compensate for that difference here.
            cAltitude *= this.getWwd().getSceneController().getVerticalExaggeration();
            rAltitude *= this.getWwd().getSceneController().getVerticalExaggeration();
            // Add a little altitude so that the line isn't lost during depth buffering.
            cAltitude += 100;
            rAltitude += 100;
        }
        else if (this.getShapeAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
        {
            rotationLine.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            rotationLine.setFollowTerrain(true);
        }
        else
        {
            rotationLine.setAltitudeMode(WorldWind.ABSOLUTE);
            rotationLine.setFollowTerrain(false);
        }

        java.util.List<Position> linePositions = new ArrayList<Position>(2);
        linePositions.add(new Position(centerPosition, cAltitude));
        linePositions.add(new Position(controlPoint, rAltitude));
        rotationLine.setPositions(linePositions);
    }

    /**
     * Computes the appropriate absolute altitude at which to place a control point at a specified location.
     *
     * @param location the location of the control point.
     *
     * @return the appropriate altitude at which to place the control point.
     */
    protected double getControlPointAltitude(LatLon location)
    {
        return this.doGetControlPointAltitude(location, this.getShape());
    }

    protected double doGetControlPointAltitude(LatLon location, Renderable shape)
    {
        double altitude = 0;

        if (shape instanceof Airspace && !((Airspace) shape).isDrawSurfaceShape())
        {
            Airspace airspace = (Airspace) shape;

            altitude = airspace.getAltitudes()[1];

            if (airspace.getAltitudeDatum()[1].equals(AVKey.ABOVE_GROUND_LEVEL))
            {
                LatLon refPos = airspace.getGroundReference();
                if (refPos == null)
                    refPos = location;
                altitude += getWwd().getModel().getGlobe().getElevation(refPos.getLatitude(), refPos.getLongitude());
            }
        }
        else if (shape instanceof Path)
        {
            for (Position position : ((Path) shape).getPositions())
            {
                if (new LatLon(position).equals(location))
                {
                    if (((Path) shape).getAltitudeMode() == WorldWind.ABSOLUTE)
                        altitude = position.getAltitude();
                    else if (((Path) shape).getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
                        altitude = position.getAltitude() + this.getWwd().getModel().getGlobe().getElevation(
                            location.getLatitude(), location.getLongitude());
                }
            }
        }
        else if (shape instanceof gov.nasa.worldwind.render.Polygon)
        {
            for (Position position : ((gov.nasa.worldwind.render.Polygon) shape).outerBoundary())
            {
                if (new LatLon(position).equals(location))
                {
                    if (((gov.nasa.worldwind.render.Polygon) shape).getAltitudeMode() == WorldWind.ABSOLUTE)
                        altitude = position.getAltitude();
                    else if (((gov.nasa.worldwind.render.Polygon) shape).getAltitudeMode()
                        == WorldWind.RELATIVE_TO_GROUND)
                        altitude = position.getAltitude() + this.getWwd().getModel().getGlobe().getElevation(
                            location.getLatitude(), location.getLongitude());
                }
            }
        }

        return altitude;
    }

    /**
     * Indicates the current shape's altitude mode if the shape has one.
     *
     * @return the shape's altitude mode if it has one, otherwise <code>WorldWind.ABSOLUTE</code>.
     */
    protected int getShapeAltitudeMode()
    {
        int altitudeMode = WorldWind.ABSOLUTE;

        if (this.getShape() instanceof Airspace && ((Airspace) this.getShape()).isDrawSurfaceShape())
        {
            altitudeMode = WorldWind.CLAMP_TO_GROUND;
        }
        else if (this.getShape() instanceof Airspace)
        {
            if (((Airspace) this.getShape()).getAltitudeDatum()[1].equals(AVKey.ABOVE_GROUND_LEVEL))
                altitudeMode = WorldWind.RELATIVE_TO_GROUND;
        }
        else if (this.getShape() instanceof SurfaceShape)
        {
            altitudeMode = WorldWind.CLAMP_TO_GROUND;
        }

        return altitudeMode;
    }

    /**
     * Creates a control point.
     *
     * @param position   the control point position.
     * @param attributes the control point attributes.
     * @param id         the control point ID.
     * @param purpose    the control point purpose.
     *
     * @return the new control point.
     */
    protected ControlPointMarker makeControlPoint(Position position, MarkerAttributes attributes, int id,
        String purpose)
    {
        return new ControlPointMarker(position, attributes, id, purpose);
    }

    /**
     * Creates a control point.
     *
     * @param position   the control point position.
     * @param attributes the control point attributes.
     * @param id         the control point ID.
     * @param leg        the control point leg.
     * @param purpose    the control point purpose.
     *
     * @return the new control point.
     */
    protected ControlPointMarker makeControlPoint(Position position, MarkerAttributes attributes, int id,
        int leg, String purpose)
    {
        return new ControlPointMarker(position, attributes, id, leg, purpose);
    }

    /**
     * Computes the Cartesian difference between two control points.
     *
     * @param previousLocation the location nof the previous control point.
     * @param currentLocation  the location of the current control point.
     *
     * @return the Cartesian difference between the two control points.
     */
    protected Vec4 computeControlPointDelta(LatLon previousLocation, LatLon currentLocation)
    {
        // Compute how much the specified control point moved.
        Vec4 terrainPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(currentLocation);
        Vec4 previousPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(previousLocation);

        return terrainPoint.subtract3(previousPoint);
    }

    /**
     * Add a specified increment to an angle and normalize the result to be between 0 and 360 degrees.
     *
     * @param originalHeading the base angle.
     * @param deltaHeading    the increment to add prior to normalizing.
     *
     * @return the normalized angle.
     */
    protected Angle normalizedHeading(Angle originalHeading, Angle deltaHeading)
    {
        final double twoPI = 2 * Math.PI;

        double newHeading = originalHeading.getRadians() + deltaHeading.getRadians();

        if (Math.abs(newHeading) > twoPI)
            newHeading = newHeading % twoPI;

        return Angle.fromRadians(newHeading >= 0 ? newHeading : newHeading + twoPI);
    }

    /**
     * Computes a control point location at the edge of a shape.
     *
     * @param center   the shape's center.
     * @param location a location that forms a line from the shape's center along the shape's axis. The returned
     *                 location is on the edge indicated by the cross product of a vector normal to the surface at the
     *                 specified center and a vector from the center to this location.
     * @param length   the distance of the edge from the shape's center.
     *
     * @return a location at the shape's edge at the same location along the shape's axis as the specified center
     * location.
     */
    protected Position computeEdgeLocation(LatLon center, LatLon location, double length)
    {
        Vec4 centerPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(center);
        Vec4 surfaceNormal = getWwd().getModel().getGlobe().computeEllipsoidalNormalAtLocation(
            center.getLatitude(), center.getLongitude());

        Vec4 point1 = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(location);
        Vec4 vecToLocation = point1.subtract3(centerPoint).normalize3();
        Vec4 vecToEdge = surfaceNormal.cross3(vecToLocation).normalize3().multiply3(length);

        LatLon edgeLocation = getWwd().getModel().getGlobe().computePositionFromEllipsoidalPoint(
            vecToEdge.add3(centerPoint));
        double edgeAltitude = this.getControlPointAltitude(edgeLocation);

        return new Position(edgeLocation, edgeAltitude);
    }

    /**
     * Computes a control point location at the edge of a rectangular shape.
     *
     * @param begin the beginning of the shape's center.
     * @param end   the end of the shape's center.
     * @param width the distance of the edge from the great circle arc between begin and end.
     *
     * @return a location centered along the edge parallel to the great circle arc between begin and end.
     */
    protected Position computeRectangularEdgeLocation(LatLon begin, LatLon end, double width)
    {
        LatLon center = LatLon.interpolateGreatCircle(0.5, begin, end);
        Angle edgeAzimuth = LatLon.greatCircleAzimuth(center, end).add(Angle.POS90);
        Angle edgeLength = Angle.fromRadians(width / this.getWwd().getModel().getGlobe().getRadius());

        LatLon edgeLocation = LatLon.greatCircleEndPosition(center, edgeAzimuth, edgeLength);
        double edgeAltitude = this.getControlPointAltitude(edgeLocation);

        return new Position(edgeLocation, edgeAltitude);
    }

    /**
     * Computes the point on a specified line segment that is nearest a specified point.
     *
     * @param p1    the line's first point.
     * @param p2    the line's second point.
     * @param point the point for which to determine a nearest point on the line segment.
     *
     * @return the nearest point on the line segment.
     */
    protected Vec4 nearestPointOnSegment(Vec4 p1, Vec4 p2, Vec4 point)
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

    /**
     * Inserts the location nearest to a specified position on an edge of a specified list of locations into the
     * appropriate place in that list.
     *
     * @param terrainPosition the position to find a nearest point for.
     * @param altitude        the altitude to use when determining the nearest point. Can be approximate and is not
     *                        necessarily the altitude of the terrain position.
     * @param locations       the list of locations. This list is modified by this method to contain the new location on
     *                        an edge nearest the specified terrain position.
     *
     * @return the index at which the new location was inserted into the list.
     */
    protected int addNearestLocation(Position terrainPosition, double altitude, List<LatLon> locations)
    {
        Globe globe = this.getWwd().getModel().getGlobe();

        // Find the nearest edge to the picked point and insert a new position on that edge.
        Vec4 pointPicked = globe.computeEllipsoidalPointFromPosition(terrainPosition.getLatitude(),
            terrainPosition.getLongitude(), altitude);

        Vec4 nearestPoint = null;
        int nearestSegmentIndex = 0;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 1; i <= locations.size(); i++) // <= is intentional, to handle the closing segment
        {
            // Skip the closing segment if the shape is not a polygon.
            if (!(this.getShape() instanceof Polygon || this.getShape() instanceof SurfacePolygon)
                && i == locations.size())
                continue;

            LatLon locationA = locations.get(i - 1);
            LatLon locationB = locations.get(i == locations.size() ? 0 : i);

            Vec4 pointA = globe.computeEllipsoidalPointFromPosition(locationA.getLatitude(),
                locationA.getLongitude(), altitude);
            Vec4 pointB = globe.computeEllipsoidalPointFromPosition(locationB.getLatitude(),
                locationB.getLongitude(), altitude);

            Vec4 pointOnEdge = this.nearestPointOnSegment(pointA, pointB, pointPicked);
            double distance = pointOnEdge.distanceTo3(pointPicked);
            if (distance < nearestDistance)
            {
                nearestPoint = pointOnEdge;
                nearestSegmentIndex = i;
                nearestDistance = distance;
            }
        }

        if (nearestPoint != null)
        {
            // Compute the location of the nearest point and add it to the shape.
            LatLon nearestLocation = globe.computePositionFromEllipsoidalPoint(nearestPoint);
            if (nearestSegmentIndex == locations.size())
                locations.add(nearestLocation);
            else
                locations.add(nearestSegmentIndex, nearestLocation);
            this.getControlPointLayer().setMarkers(null);

            return nearestSegmentIndex;
        }

        return -1;
    }

    /**
     * Adds a location to either the beginning or the end of a specified list of locations. Which end to add to is
     * determined by a specified control point.
     *
     * @param controlPoint the control point of the shape's end. If the control point's ID is 0 the new location is
     *                     inserted to the beginning of the list. If the control point ID corresponds to the last
     *                     location in the list then the new location is appended to the list. Otherwise no operation
     *                     occurs.
     * @param locations    the shape's locations. This list is modified upon return to include the new location.
     */
    protected void appendLocation(ControlPointMarker controlPoint, List<LatLon> locations)
    {
        // Add a control point to the beginning or end of the shape.
        Globe globe = this.getWwd().getModel().getGlobe();

        if (controlPoint.getId() == 0) // beginning of list
        {
            Vec4 pointA = globe.computeEllipsoidalPointFromLocation(locations.get(0));
            Vec4 pointB = globe.computeEllipsoidalPointFromLocation(locations.get(1));
            Vec4 newPoint = pointA.add3(pointA.subtract3(pointB).multiply3(0.1));
            locations.add(0, globe.computePositionFromEllipsoidalPoint(newPoint));
        }
        else if (controlPoint.getId() == locations.size() - 1) // end of list
        {
            Vec4 pointA = globe.computeEllipsoidalPointFromLocation(locations.get(locations.size() - 2));
            Vec4 pointB = globe.computeEllipsoidalPointFromLocation(locations.get(locations.size() - 1));
            Vec4 newPoint = pointB.add3(pointB.subtract3(pointA).multiply3(0.1));
            locations.add(globe.computePositionFromEllipsoidalPoint(newPoint));
        }
    }

    /**
     * Moves a control point location.
     *
     * @param controlPoint    the control point being moved.
     * @param terrainPosition the position selected by the user.
     * @param locations       the list of locations for the shape.
     */
    protected void moveLocation(ControlPointMarker controlPoint, Position terrainPosition, List<LatLon> locations)
    {
        // Compute the new location for the polygon location associated with the incoming control point.
        Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
        Vec4 markerPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            new Position(controlPoint.getPosition(), 0));
        Position markerPosition = getWwd().getModel().getGlobe().computePositionFromEllipsoidalPoint(
            markerPoint.add3(delta));

        // Update the polygon's locations.
        locations.set(controlPoint.getId(), markerPosition);
    }

    /**
     * Rotates a shape's locations.
     *
     * @param terrainPosition the position selected by the user.
     * @param locations       the list of locations for the shape.
     */
    protected void rotateLocations(Position terrainPosition, List<LatLon> locations)
    {
        // Rotate the positions.
        LatLon center = LatLon.getCenter(this.getWwd().getModel().getGlobe(), locations); // rotation axis
        Angle previousHeading = LatLon.greatCircleAzimuth(center, this.getPreviousPosition());
        Angle deltaHeading = LatLon.greatCircleAzimuth(center, terrainPosition).subtract(previousHeading);
        this.currentHeading = this.normalizedHeading(this.getCurrentHeading(), deltaHeading);

        // Rotate the polygon's locations by the heading delta angle.
        for (int i = 0; i < locations.size(); i++)
        {
            LatLon location = locations.get(i);

            Angle heading = LatLon.greatCircleAzimuth(center, location);
            Angle distance = LatLon.greatCircleDistance(center, location);
            LatLon newLocation = LatLon.greatCircleEndPosition(center, heading.add(deltaHeading), distance);
            locations.set(i, newLocation);
        }
    }

    /**
     * Performs an edit for {@link gov.nasa.worldwind.render.airspaces.Polygon} shapes.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void reshapePolygonAirspace(Position terrainPosition, ControlPointMarker controlPoint)
    {
        Iterable<? extends LatLon> currentLocations = null;

        if (this.getShape() instanceof Polygon)
            currentLocations = ((Polygon) this.getShape()).getLocations();
        else if (this.getShape() instanceof Curtain)
            currentLocations = ((Curtain) this.getShape()).getLocations();

        if (currentLocations == null)
            return;

        // Assemble a local copy of the polygon's locations.
        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon location : currentLocations)
        {
            locations.add(location);
        }

        if (controlPoint != null && controlPoint.getPurpose().equals(ROTATION))
        {
            // Rotate the polygon.
            this.rotateLocations(terrainPosition, locations);
        }
        else if (controlPoint != null) // location change or add/delete control point
        {
            if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0
                && this.isExtensionEnabled())
            {
                int minSize = this.getShape() instanceof Polygon ? 3 : 2;
                if (locations.size() > minSize)
                {
                    // Delete the control point.
                    locations.remove(controlPoint.getId());
                    this.getControlPointLayer().setMarkers(null);
                }
            }
            else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
                && this.isExtensionEnabled()
                && this.getShape() instanceof Curtain)
            {
                // Add a new control point.
                this.appendLocation(controlPoint, locations);
                this.getControlPointLayer().setMarkers(null);
            }
            else // control point location change
            {
                this.moveLocation(controlPoint, terrainPosition, locations);
            }
        }
        else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
            && this.isExtensionEnabled())
        {
            // Insert a new location along an edge of the polygon.
            double altitude = ((Airspace) this.getShape()).getAltitudes()[1];
            this.addNearestLocation(terrainPosition, altitude, locations);
        }

        // Update the shape's locations.
        if (this.getShape() instanceof Polygon)
            ((Polygon) this.getShape()).setLocations(locations);
        else if (this.getShape() instanceof Curtain)
            ((Curtain) this.getShape()).setLocations(locations);
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.Polygon} shapes.
     */
    protected void updatePolygonAirspaceControlPoints()
    {
        Iterable<? extends LatLon> currentLocations = null;

        if (this.getShape() instanceof Polygon)
            currentLocations = ((Polygon) this.getShape()).getLocations();
        else if (this.getShape() instanceof Curtain)
            currentLocations = ((Curtain) this.getShape()).getLocations();

        if (currentLocations == null)
            return;

        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon location : currentLocations)
        {
            locations.add(location);
        }

        if (locations.size() < 2)
            return;

        Globe globe = this.getWwd().getModel().getGlobe();

        LatLon polygonCenter = LatLon.getCenter(globe, locations);
        double centerAltitude = this.getControlPointAltitude(polygonCenter);

        // Compute the shape's heading and the rotation control location.
        Angle shapeRadius = LatLon.getAverageDistance(globe, polygonCenter, locations);
        shapeRadius = shapeRadius.multiply(1.2);
        Angle heading = this.getCurrentHeading();
        LatLon rotationControlLocation = LatLon.greatCircleEndPosition(polygonCenter, heading, shapeRadius);
        double rotationControlAltitude = this.getControlPointAltitude(rotationControlLocation);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            // Create control points for the polygon locations.
            ArrayList<Marker> controlPoints = new ArrayList<Marker>();
            int i = 0;
            for (LatLon location : locations)
            {
                double altitude = this.getControlPointAltitude(location);
                Position cpPosition = new Position(location, altitude);
                controlPoints.add(
                    this.makeControlPoint(cpPosition, this.getLocationControlPointAttributes(), i++, LOCATION));
            }

            // Create a control point for the rotation control.
            Position cpPosition = new Position(rotationControlLocation, rotationControlAltitude);
            controlPoints.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), i, ROTATION));

            this.getControlPointLayer().setMarkers(controlPoints);
        }
        else
        {
            // Update the polygon's location control points.
            Iterator<Marker> markerIterator = markers.iterator();
            for (LatLon location : locations)
            {
                double altitude = this.getControlPointAltitude(location);
                markerIterator.next().setPosition(new Position(location, altitude));
            }

            // Update the polygon's rotation control point.
            markerIterator.next().setPosition(new Position(rotationControlLocation, rotationControlAltitude));
        }

        // Update the heading annotation.
        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        for (LatLon ignored : locations)
        {
            markerIterator.next();
        }
        ((ControlPointMarker) markerIterator.next()).rotation = heading;

        // Update the rotation orientation line.
        this.updateOrientationLine(new Position(polygonCenter, centerAltitude),
            new Position(rotationControlLocation, rotationControlAltitude));
    }

    /**
     * Performs an edit for {@link gov.nasa.worldwind.render.airspaces.CappedCylinder} shapes.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void reshapeCappedCylinder(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        CappedCylinder cylinder = (CappedCylinder) this.getShape();
        double[] radii = cylinder.getRadii();

        Vec4 centerPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(cylinder.getCenter());
        Vec4 markerPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
        if (controlPoint.getPurpose().equals(OUTER_RADIUS))
            radii[1] += delta.dot3(vMarker);
        else if (controlPoint.getPurpose().equals(INNER_RADIUS))
            radii[0] += delta.dot3(vMarker);

        if (radii[0] >= 0 && radii[1] > 0 && radii[0] < radii[1])
            cylinder.setRadii(radii[0], radii[1]);

        if (this.getShape() instanceof PartialCappedCylinder)
        {
            Angle oldHeading = LatLon.greatCircleAzimuth(cylinder.getCenter(), this.getPreviousPosition());
            Angle deltaHeading = LatLon.greatCircleAzimuth(cylinder.getCenter(), terrainPosition).subtract(oldHeading);

            Angle[] azimuths = ((PartialCappedCylinder) cylinder).getAzimuths();
            if (controlPoint.getPurpose().equals(LEFT_AZIMUTH))
                azimuths[0] = this.normalizedHeading(azimuths[0], deltaHeading);
            else if (controlPoint.getPurpose().equals(RIGHT_AZIMUTH))
                azimuths[1] = this.normalizedHeading(azimuths[1], deltaHeading);
            else if (controlPoint.getPurpose().equals(ROTATION))
            {
                this.currentHeading = this.normalizedHeading(this.getCurrentHeading(), deltaHeading);
                azimuths[0] = this.normalizedHeading(azimuths[0], deltaHeading);
                azimuths[1] = this.normalizedHeading(azimuths[1], deltaHeading);
            }

            ((PartialCappedCylinder) cylinder).setAzimuths(azimuths[0], azimuths[1]);
        }
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.CappedCylinder}
     * shapes.
     */
    protected void updateCappedCylinderControlPoints()
    {
        CappedCylinder cylinder = (CappedCylinder) this.getShape();
        double[] radii = cylinder.getRadii();
        boolean hasInnerRadius = radii[0] > 0;

        LatLon outerRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), Angle.fromDegrees(90),
            Angle.fromRadians(radii[1] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        LatLon innerRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), Angle.fromDegrees(90),
            Angle.fromRadians(radii[0] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        double outerRadiusAltitude = this.getControlPointAltitude(outerRadiusLocation);
        double innerRadiusAltitude = this.getControlPointAltitude(innerRadiusLocation);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            Position cpPosition = new Position(outerRadiusLocation, outerRadiusAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, OUTER_RADIUS));
            if (hasInnerRadius)
            {
                cpPosition = new Position(innerRadiusLocation, innerRadiusAltitude);
                markerList.add(
                    this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 1, INNER_RADIUS));
            }
            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(outerRadiusLocation, outerRadiusAltitude));
            if (hasInnerRadius)
                markerIterator.next().setPosition(new Position(innerRadiusLocation, innerRadiusAltitude));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = radii[1];
        if (hasInnerRadius)
            ((ControlPointMarker) markerIterator.next()).size = radii[0];
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.PartialCappedCylinder}
     * shapes.
     */
    protected void updatePartialCappedCylinderControlPoints()
    {
        PartialCappedCylinder cylinder = (PartialCappedCylinder) this.getShape();

        double[] radii = cylinder.getRadii();
        boolean hasInnerRadius = radii[0] > 0;
        double averageRadius = 0.5 * (radii[0] + radii[1]);

        Angle[] azimuths = cylinder.getAzimuths();

        LatLon outerRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), azimuths[1],
            Angle.fromRadians(radii[1] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        LatLon innerRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), azimuths[1],
            Angle.fromRadians(radii[0] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        LatLon leftAzimuthLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), azimuths[0],
            Angle.fromRadians(averageRadius / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        LatLon rightAzimuthLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), azimuths[1],
            Angle.fromRadians(averageRadius / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        double outerRadiusAltitude = this.getControlPointAltitude(outerRadiusLocation);
        double innerRadiusAltitude = this.getControlPointAltitude(innerRadiusLocation);
        double rightAzimuthAltitude = this.getControlPointAltitude(rightAzimuthLocation);
        double leftAzimuthAltitude = this.getControlPointAltitude(leftAzimuthLocation);

        LatLon rotationControlLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), this.getCurrentHeading(),
            Angle.fromRadians(1.2 * radii[1] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        double rotationControlAltitude = this.getControlPointAltitude(rotationControlLocation);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            Position cpPosition = new Position(outerRadiusLocation, outerRadiusAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, OUTER_RADIUS));
            if (hasInnerRadius)
            {
                cpPosition = new Position(innerRadiusLocation, innerRadiusAltitude);
                markerList.add(
                    this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 1, INNER_RADIUS));
            }

            cpPosition = new Position(leftAzimuthLocation, leftAzimuthAltitude);
            markerList.add(
                this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 2, LEFT_AZIMUTH));
            cpPosition = new Position(rightAzimuthLocation, rightAzimuthAltitude);
            markerList.add(
                this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 3, RIGHT_AZIMUTH));

            cpPosition = new Position(rotationControlLocation, rotationControlAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 4, ROTATION));

            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(outerRadiusLocation, outerRadiusAltitude));
            if (hasInnerRadius)
                markerIterator.next().setPosition(new Position(innerRadiusLocation, rightAzimuthAltitude));
            markerIterator.next().setPosition(new Position(leftAzimuthLocation, leftAzimuthAltitude));
            markerIterator.next().setPosition(new Position(rightAzimuthLocation, rightAzimuthAltitude));

            markerIterator.next().setPosition(new Position(rotationControlLocation, rotationControlAltitude));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = radii[1];
        if (hasInnerRadius)
            ((ControlPointMarker) markerIterator.next()).size = radii[0];

        ((ControlPointMarker) markerIterator.next()).rotation = azimuths[0];
        ((ControlPointMarker) markerIterator.next()).rotation = azimuths[1];

        ((ControlPointMarker) markerIterator.next()).rotation = this.getCurrentHeading();

        // Update the rotation orientation line.
        double centerAltitude = this.getControlPointAltitude(cylinder.getCenter());
        this.updateOrientationLine(new Position(cylinder.getCenter(), centerAltitude),
            new Position(rotationControlLocation, rotationControlAltitude));
    }

    /**
     * Performs an edit for {@link gov.nasa.worldwind.render.airspaces.CappedCylinder} shapes.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void reshapeCappedEllipticalCylinder(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        CappedEllipticalCylinder cylinder = (CappedEllipticalCylinder) this.getShape();
        double[] radii = cylinder.getRadii();

        Vec4 centerPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(cylinder.getCenter());
        Vec4 markerPoint = getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
        if (controlPoint.getPurpose().equals(INNER_MINOR_RADIUS))
            radii[0] += delta.dot3(vMarker);
        else if (controlPoint.getPurpose().equals(INNER_MAJOR_RADIUS))
            radii[1] += delta.dot3(vMarker);
        else if (controlPoint.getPurpose().equals(OUTER_MINOR_RADIUS))
            radii[2] += delta.dot3(vMarker);
        else if (controlPoint.getPurpose().equals(OUTER_MAJOR_RADIUS))
            radii[3] += delta.dot3(vMarker);
        else if (controlPoint.getPurpose().equals(ROTATION)) {
            Angle oldHeading = LatLon.greatCircleAzimuth(cylinder.getCenter(), this.getPreviousPosition());
            Angle deltaHeading = LatLon.greatCircleAzimuth(cylinder.getCenter(), terrainPosition).subtract(oldHeading);
            cylinder.setHeading(this.normalizedHeading(cylinder.getHeading(), deltaHeading));
            this.currentHeading = this.normalizedHeading(this.getCurrentHeading(), deltaHeading);
        }

        if (isRadiiValid(radii[0], radii[2]) && isRadiiValid(radii[1], radii[3]))
            cylinder.setRadii(radii[0], radii[1], radii[2], radii[3]);
    }

    protected static boolean isRadiiValid(double innerRadius, double outerRadius)
    {
        return innerRadius >= 0 && innerRadius < outerRadius;
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.CappedCylinder}
     * shapes.
     */
    protected void updateCappedEllipticalCylinderControlPoints()
    {
        CappedEllipticalCylinder cylinder = (CappedEllipticalCylinder) this.getShape();
        double[] radii = cylinder.getRadii();
        boolean hasInnerRadius = radii[0] > 0 && radii[1] > 0;
        Angle heading = cylinder.getHeading();

        LatLon innerMinorRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(),
            Angle.fromDegrees(90).add(heading),
            Angle.fromRadians(radii[0] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        LatLon innerMajorRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(),
            Angle.fromDegrees(0).add(heading),
            Angle.fromRadians(radii[1] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        LatLon outerMinorRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(),
            Angle.fromDegrees(90).add(heading),
            Angle.fromRadians(radii[2] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        LatLon outerMajorRadiusLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(),
            Angle.fromDegrees(0).add(heading),
            Angle.fromRadians(radii[3] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        LatLon rotationControlLocation = LatLon.greatCircleEndPosition(cylinder.getCenter(), this.getCurrentHeading(),
            Angle.fromRadians(1.4 * radii[3] / this.getWwd().getModel().getGlobe().getEquatorialRadius()));
        double rotationControlAltitude = this.getControlPointAltitude(rotationControlLocation);

        double innerMinorRadiusAltitude = this.getControlPointAltitude(innerMinorRadiusLocation);
        double innerMajorRadiusAltitude = this.getControlPointAltitude(innerMajorRadiusLocation);
        double outerMinorRadiusAltitude = this.getControlPointAltitude(outerMinorRadiusLocation);
        double outerMajorRadiusAltitude = this.getControlPointAltitude(outerMajorRadiusLocation);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(2);
            Position cpPosition = new Position(outerMinorRadiusLocation, outerMinorRadiusAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 2, OUTER_MINOR_RADIUS));
            cpPosition = new Position(outerMajorRadiusLocation, outerMajorRadiusAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 3, OUTER_MAJOR_RADIUS));
            if (hasInnerRadius)
            {
                cpPosition = new Position(innerMinorRadiusLocation, innerMinorRadiusAltitude);
                markerList.add(
                    this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, INNER_MINOR_RADIUS));
                cpPosition = new Position(innerMajorRadiusLocation, innerMajorRadiusAltitude);
                markerList.add(
                    this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 1, INNER_MAJOR_RADIUS));
            }

            cpPosition = new Position(rotationControlLocation, rotationControlAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 4, ROTATION));

            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(outerMinorRadiusLocation, outerMinorRadiusAltitude));
            markerIterator.next().setPosition(new Position(outerMajorRadiusLocation, outerMajorRadiusAltitude));
            if (hasInnerRadius)
            {
                markerIterator.next().setPosition(new Position(innerMinorRadiusLocation, innerMinorRadiusAltitude));
                markerIterator.next().setPosition(new Position(innerMajorRadiusLocation, innerMajorRadiusAltitude));
            }

            markerIterator.next().setPosition(new Position(rotationControlLocation, rotationControlAltitude));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = radii[2];
        ((ControlPointMarker) markerIterator.next()).size = radii[3];
        if (hasInnerRadius)
        {
            ((ControlPointMarker) markerIterator.next()).size = radii[0];
            ((ControlPointMarker) markerIterator.next()).size = radii[1];
        }

        ((ControlPointMarker) markerIterator.next()).rotation = this.getCurrentHeading();

        // Update the rotation orientation line.
        double centerAltitude = this.getControlPointAltitude(cylinder.getCenter());
        this.updateOrientationLine(new Position(cylinder.getCenter(), centerAltitude),
            new Position(rotationControlLocation, rotationControlAltitude));
    }

    /**
     * Performs an edit for {@link gov.nasa.worldwind.render.airspaces.SphereAirspace} shapes.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void reshapeSphere(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        SphereAirspace sphere = (SphereAirspace) this.getShape();
        double radius = sphere.getRadius();

        Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            sphere.getLocation());
        Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
        if (controlPoint.getPurpose().equals(OUTER_RADIUS))
            radius += delta.dot3(vMarker);

        if (radius > 0)
            sphere.setRadius(radius);
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.SphereAirspace}
     * shapes.
     */
    protected void updateSphereControlPoints()
    {
        SphereAirspace sphere = (SphereAirspace) this.getShape();
        double radius = sphere.getRadius();

        LatLon radiusLocation = LatLon.greatCircleEndPosition(sphere.getLocation(), Angle.fromDegrees(90),
            Angle.fromRadians(radius / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        double radiusAltitude = this.getControlPointAltitude(radiusLocation);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            Position cpPosition = new Position(radiusLocation, radiusAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, OUTER_RADIUS));
            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(radiusLocation, radiusAltitude));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = radius;
    }

    /**
     * Performs an edit for {@link gov.nasa.worldwind.render.airspaces.Orbit} shapes.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void reshapeOrbit(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        Orbit orbit = (Orbit) this.getShape();
        LatLon[] locations = orbit.getLocations();
        double width = orbit.getWidth();

        LatLon center = LatLon.interpolateGreatCircle(0.5, locations[0], locations[1]);
        Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(center);

        Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            new Position(controlPoint.getPosition(), 0));

        if (controlPoint.getPurpose().equals(RIGHT_WIDTH))
        {
            Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
            Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();
            double newWidth = width + delta.dot3(vMarker);
            if (newWidth > 0)
                orbit.setWidth(width + delta.dot3(vMarker));
        }
        else if (controlPoint.getPurpose().equals(ROTATION))
        {
            Angle oldHeading = LatLon.greatCircleAzimuth(center, this.getPreviousPosition());
            Angle deltaHeading = LatLon.greatCircleAzimuth(center, terrainPosition).subtract(oldHeading);

            for (int i = 0; i < 2; i++)
            {
                Angle heading = LatLon.greatCircleAzimuth(center, locations[i]);
                Angle distance = LatLon.greatCircleDistance(center, locations[i]);
                locations[i] = LatLon.greatCircleEndPosition(center, heading.add(deltaHeading), distance);
            }
            orbit.setLocations(locations[0], locations[1]);
        }
        else // location change
        {
            Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
            Position markerPosition = this.getWwd().getModel().getGlobe().computePositionFromEllipsoidalPoint(
                markerPoint.add3(delta));
            locations[controlPoint.getId()] = markerPosition;
            orbit.setLocations(locations[0], locations[1]);
        }
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.Orbit} shapes.
     */
    protected void updateOrbitControlPoints()
    {
        Orbit orbit = (Orbit) this.getShape();
        LatLon[] locations = orbit.getLocations();
        double width = orbit.getWidth();

        double location0Altitude = this.getControlPointAltitude(locations[0]);
        double location1Altitude = this.getControlPointAltitude(locations[1]);

        Angle orbitHeading = LatLon.greatCircleAzimuth(locations[0], locations[1]);

        LatLon center = LatLon.interpolateGreatCircle(0.5, locations[0], locations[1]);
        double centerAltitude = this.getControlPointAltitude(center);
        Position widthPosition = this.computeRectangularEdgeLocation(locations[0], locations[1], 0.5 * width);

        Globe globe = this.getWwd().getModel().getGlobe();
        Vec4 centerPoint = globe.computeEllipsoidalPointFromLocation(center);
        Vec4 point0 = globe.computeEllipsoidalPointFromLocation(locations[1]);
        Vec4 vec = point0.subtract3(centerPoint);
        vec = vec.multiply3(1 + width / vec.getLength3());
        LatLon rotationControlLocation = globe.computePositionFromEllipsoidalPoint(vec.add3(centerPoint));
        double rotationControlAltitude = this.getControlPointAltitude(rotationControlLocation);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            Position cpPosition = new Position(locations[0], location0Altitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getLocationControlPointAttributes(), 0, LOCATION));
            cpPosition = new Position(locations[1], location1Altitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getLocationControlPointAttributes(), 1, LOCATION));

            cpPosition = new Position(widthPosition, widthPosition.getAltitude());
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 2, RIGHT_WIDTH));

            cpPosition = new Position(rotationControlLocation, rotationControlAltitude);
            markerList.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 3, ROTATION));

            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(locations[0], location0Altitude));
            markerIterator.next().setPosition(new Position(locations[1], location1Altitude));
            markerIterator.next().setPosition(new Position(widthPosition, widthPosition.getAltitude()));
            markerIterator.next().setPosition(new Position(rotationControlLocation, rotationControlAltitude));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        markerIterator.next();
        markerIterator.next();
        ((ControlPointMarker) markerIterator.next()).size = width;
        ((ControlPointMarker) markerIterator.next()).rotation = this.normalizedHeading(orbitHeading, Angle.ZERO);

        this.updateOrientationLine(new Position(center, centerAltitude),
            new Position(rotationControlLocation, rotationControlAltitude));
    }

    /**
     * Performs an edit for {@link gov.nasa.worldwind.render.airspaces.Route} shapes.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void reshapeRoute(Position terrainPosition, ControlPointMarker controlPoint)
    {
        Route route = (Route) this.getShape();

        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon ll : route.getLocations())
        {
            locations.add(ll);
        }

        if (controlPoint != null && controlPoint.getPurpose().equals(ROTATION))
        {
            this.rotateLocations(terrainPosition, locations);
            route.setLocations(locations);
        }
        else if (controlPoint != null
            && (controlPoint.getPurpose().equals(LEFT_WIDTH) || controlPoint.getPurpose().equals(RIGHT_WIDTH)))
        {
            LatLon legCenter = LatLon.interpolateGreatCircle(0.5, locations.get(0), locations.get(1));
            Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(legCenter);
            Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
                new Position(controlPoint.getPosition(), 0));
            Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();
            Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
            double newWidth = route.getWidth() + delta.dot3(vMarker);
            if (newWidth >= 0)
                route.setWidth(newWidth);
        }
        else if (controlPoint != null) // location change or add/delete control point
        {
            if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0
                && this.isExtensionEnabled())
            {
                if (locations.size() > 2)
                {
                    // Delete the control point.
                    locations.remove(controlPoint.getId());
                    this.getControlPointLayer().setMarkers(null);
                }
            }
            else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
                && this.isExtensionEnabled())
            {
                this.appendLocation(controlPoint, locations);
                this.getControlPointLayer().setMarkers(null);
            }
            else // control point location change
            {
                this.moveLocation(controlPoint, terrainPosition, locations);
            }

            route.setLocations(locations);
        }
        else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
            && this.isExtensionEnabled())
        {
            // Insert a new position into the shape.
            double altitude = ((Airspace) this.getShape()).getAltitudes()[1];
            this.addNearestLocation(terrainPosition, altitude, locations);
            route.setLocations(locations);
        }
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.Route} shapes.
     */
    protected void updateRouteControlPoints()
    {
        Route route = (Route) this.getShape();

        if (route.getLocations() == null)
            return;

        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon location : route.getLocations())
        {
            locations.add(location);
        }

        if (locations.size() < 2)
            return;

        Globe globe = this.getWwd().getModel().getGlobe();
        double width = route.getWidth();
        Position leftWidthPosition = this.computeRectangularEdgeLocation(locations.get(0), locations.get(1),
            -0.5 * width);
        Position rightWidthPosition = this.computeRectangularEdgeLocation(locations.get(0), locations.get(1),
            0.5 * width);

        LatLon routeCenter = LatLon.getCenter(globe, locations);
        double centerAltitude = this.getControlPointAltitude(routeCenter);

        // Compute the shape's heading and the rotation control location.
        Angle shapeRadius = LatLon.greatCircleDistance(routeCenter, locations.get(1));
        shapeRadius = shapeRadius.add(Angle.fromRadians(route.getWidth() / globe.getEquatorialRadius()));
        Angle heading = this.getCurrentHeading();
        LatLon rotationControlLocation = LatLon.greatCircleEndPosition(routeCenter, heading, shapeRadius);
        double rotationControlAltitude = this.getControlPointAltitude(rotationControlLocation);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            ArrayList<Marker> controlPoints = new ArrayList<Marker>();
            int i = 0;
            for (LatLon cpPosition : locations)
            {
                double altitude = this.getControlPointAltitude(cpPosition);
                Position position = new Position(cpPosition, altitude);
                controlPoints.add(
                    this.makeControlPoint(position, this.getLocationControlPointAttributes(), i++, LOCATION));
            }

            Position position = new Position(leftWidthPosition, leftWidthPosition.getAltitude());
            controlPoints.add(this.makeControlPoint(position, this.getSizeControlPointAttributes(), i++, RIGHT_WIDTH));
            position = new Position(rightWidthPosition, rightWidthPosition.getAltitude());
            controlPoints.add(this.makeControlPoint(position, this.getSizeControlPointAttributes(), i++, LEFT_WIDTH));

            position = new Position(rotationControlLocation, rotationControlAltitude);
            controlPoints.add(this.makeControlPoint(position, this.getAngleControlPointAttributes(), i, ROTATION));

            this.getControlPointLayer().setMarkers(controlPoints);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            for (LatLon cpPosition : locations)
            {
                double altitude = this.getControlPointAltitude(cpPosition);
                markerIterator.next().setPosition(new Position(cpPosition, altitude));
            }

            markerIterator.next().setPosition(new Position(leftWidthPosition, leftWidthPosition.getAltitude()));
            markerIterator.next().setPosition(new Position(rightWidthPosition, rightWidthPosition.getAltitude()));
            markerIterator.next().setPosition(new Position(rotationControlLocation, rotationControlAltitude));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        for (LatLon ignored : locations) // skip over the locations to get to the width and rotation control points
        {
            markerIterator.next();
        }
        ((ControlPointMarker) markerIterator.next()).size = route.getWidth();
        ((ControlPointMarker) markerIterator.next()).size = route.getWidth();
        ((ControlPointMarker) markerIterator.next()).rotation = heading;

        this.updateOrientationLine(new Position(routeCenter, centerAltitude),
            new Position(rotationControlLocation, rotationControlAltitude));
    }

    /**
     * Performs an edit for {@link gov.nasa.worldwind.render.airspaces.TrackAirspace} shapes.
     *
     * @param controlPoint    the control point selected.
     * @param terrainPosition the terrain position under the cursor.
     */
    protected void reshapeTrack(Position terrainPosition, ControlPointMarker controlPoint)
    {
        TrackAirspace track = (TrackAirspace) this.getShape();
        List<Box> legs = track.getLegs();

        if (controlPoint != null && controlPoint.getPurpose().equals(ROTATION))
        {
            List<LatLon> trackLocations = new ArrayList<LatLon>();
            for (Box leg : legs)
            {
                trackLocations.add(leg.getLocations()[0]);
                trackLocations.add(leg.getLocations()[1]);
            }
            LatLon center = LatLon.getCenter(this.getWwd().getModel().getGlobe(), trackLocations);
            Angle previousHeading = LatLon.greatCircleAzimuth(center, this.getPreviousPosition());
            Angle deltaHeading = LatLon.greatCircleAzimuth(center, terrainPosition).subtract(previousHeading);
            this.currentHeading = this.normalizedHeading(this.getCurrentHeading(), deltaHeading);

            // Rotate all the legs.
            for (Box leg : legs)
            {
                LatLon[] locations = leg.getLocations();

                Angle heading = LatLon.greatCircleAzimuth(center, locations[0]);
                Angle distance = LatLon.greatCircleDistance(center, locations[0]);
                locations[0] = LatLon.greatCircleEndPosition(center, heading.add(deltaHeading), distance);

                heading = LatLon.greatCircleAzimuth(center, locations[1]);
                distance = LatLon.greatCircleDistance(center, locations[1]);
                locations[1] = LatLon.greatCircleEndPosition(center, heading.add(deltaHeading), distance);

                leg.setLocations(locations[0], locations[1]);
            }

            track.setLegs(new ArrayList<Box>(track.getLegs()));
        }
        else if (controlPoint != null
            && (controlPoint.getPurpose().equals(LEFT_WIDTH) || controlPoint.getPurpose().equals(RIGHT_WIDTH)))
        {
            Box leg = legs.get(controlPoint.getLeg());
            LatLon[] legLocations = leg.getLocations();

            LatLon legCenter = LatLon.interpolateGreatCircle(0.5, legLocations[0], legLocations[1]);
            Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(legCenter);
            Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
                new Position(controlPoint.getPosition(), 0));
            Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

            double[] widths = leg.getWidths();
            double[] newWidths = new double[] {widths[0], widths[1]};
            Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
            if (controlPoint.getPurpose().equals(LEFT_WIDTH))
                newWidths[0] += delta.dot3(vMarker);
            else
                newWidths[1] += delta.dot3(vMarker);

            if (newWidths[0] >= 0 && newWidths[1] >= 0)
            {
                leg.setWidths(newWidths[0], newWidths[1]);
            }

            track.setLegs(new ArrayList<Box>(track.getLegs()));
        }
        else if (controlPoint != null)
        {
            // Make a modifiable copy of the legs list.
            legs = new ArrayList<Box>(legs);

            if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0
                && this.isExtensionEnabled())
            {
                // Remove a control point.

                if (legs.size() < 2) // Can't remove a control point from a single-leg track.
                    return;

                if (controlPoint.getLeg() == 0 && controlPoint.getId() == 0)
                {
                    legs.remove(0);
                }
                else if (controlPoint.getLeg() == legs.size() - 1 && controlPoint.getId() == 1)
                {
                    legs.remove(legs.size() - 1);
                }
                else
                {
                    if (controlPoint.getLeg() == 0) // need to treat the second control point of leg 0 specially
                    {
                        legs.get(0).setLocations(legs.get(0).getLocations()[0], legs.get(1).getLocations()[1]);
                        legs.remove(1);
                    }
                    else // remove an internal control point
                    {
                        Box leftLeg = controlPoint.getLeg() == 0 ? legs.get(0) : legs.get(controlPoint.getLeg() - 1);
                        Box rightLeg = legs.get(controlPoint.getLeg() + 1);
                        rightLeg.setLocations(leftLeg.getLocations()[1], rightLeg.getLocations()[1]);
                        legs.remove(controlPoint.getLeg());
                    }
                }

                track.setLegs(legs);
                this.determineTrackAdjacency();
                this.getControlPointLayer().setMarkers(null);
            }
            else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
                && this.isExtensionEnabled())
            {
                // Append a location to the beginning or end of the track.

                Globe globe = this.getWwd().getModel().getGlobe();

                if (controlPoint.getLeg() == 0 && controlPoint.getId() == 0) // first control point
                {
                    Vec4 pointA = globe.computeEllipsoidalPointFromLocation(legs.get(0).getLocations()[0]);
                    Vec4 pointB = globe.computeEllipsoidalPointFromLocation(legs.get(0).getLocations()[1]);
                    Vec4 newPoint = pointA.add3(pointA.subtract3(pointB).multiply3(0.1));
                    LatLon newLocation = globe.computePositionFromEllipsoidalPoint(newPoint);

                    Box newLeg = new Box(legs.get(0));
                    newLeg.setLocations(newLocation, legs.get(0).getLocations()[0]);
                    legs.add(0, newLeg);
                }
                else if (controlPoint.getLeg() == legs.size() - 1 && controlPoint.getId() == 1) // last control point
                {
                    Box lastLeg = legs.get(legs.size() - 1);
                    Vec4 pointA = globe.computeEllipsoidalPointFromLocation(lastLeg.getLocations()[1]);
                    Vec4 pointB = globe.computeEllipsoidalPointFromLocation(lastLeg.getLocations()[0]);
                    Vec4 newPoint = pointA.add3(pointA.subtract3(pointB).multiply3(0.1));
                    LatLon newLocation = globe.computePositionFromEllipsoidalPoint(newPoint);

                    Box newLeg = new Box(lastLeg);
                    newLeg.setLocations(lastLeg.getLocations()[1], newLocation);
                    legs.add(newLeg);
                }
                else
                {
                    return; // the point is internal rather than at the end
                }

                track.setLegs(legs);
                this.determineTrackAdjacency();
                this.getControlPointLayer().setMarkers(null);
            }
            else // control point location change
            {
                Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
                Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
                    new Position(controlPoint.getPosition(), 0));
                Position markerPosition = this.getWwd().getModel().getGlobe().computePositionFromEllipsoidalPoint(
                    markerPoint.add3(delta));

                Box leg = track.getLegs().get(controlPoint.getLeg());
                if (controlPoint.getId() == 0)
                    leg.setLocations(markerPosition, leg.getLocations()[1]);
                else
                    leg.setLocations(leg.getLocations()[0], markerPosition);

                track.setLegs(new ArrayList<Box>(track.getLegs()));
            }
        }
        else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
            && this.isExtensionEnabled())
        {
            // Make a modifiable copy of the legs list.
            legs = new ArrayList<Box>(legs);

            List<LatLon> locations = new ArrayList<LatLon>();
            for (Box leg : legs)
            {
                locations.add(leg.getLocations()[0]);
            }
            locations.add(legs.get(legs.size() - 1).getLocations()[1]); // add the last point of the last leg.

            int segmentIndex = this.addNearestLocation(terrainPosition, track.getAltitudes()[1], locations);
            LatLon newLocation = locations.get(segmentIndex);

            int legIndex = segmentIndex - 1;
            Box leg = legs.get(legIndex);
            Box newLeg = new Box(leg);

            if (legIndex > 0)
            {
                newLeg.setLocations(leg.getLocations()[0], newLocation);
                leg.setLocations(newLocation, leg.getLocations()[1]);
                legs.add(legIndex, newLeg);
            }
            else
            {
                newLeg.setLocations(newLocation, leg.getLocations()[1]);
                leg.setLocations(leg.getLocations()[0], newLocation);
                legs.add(1, newLeg);
            }

            track.setLegs(new ArrayList<Box>(legs));
            this.determineTrackAdjacency();
            this.getControlPointLayer().setMarkers(null);
        }
    }

    /**
     * Updates the control points and affordances for {@link gov.nasa.worldwind.render.airspaces.TrackAirspace} shapes.
     */
    protected void updateTrackControlPoints()
    {
        TrackAirspace track = (TrackAirspace) this.getShape();

        List<Box> legs = track.getLegs();
        if (legs == null)
            return;

        // Update the location control points.
        ArrayList<Marker> controlPoints = new ArrayList<Marker>();
        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        Iterator<Marker> markerIterator = markers != null ? markers.iterator() : null;
        for (int i = 0; i < legs.size(); i++)
        {
            Box leg = legs.get(i);
            LatLon[] legLocations = leg.getLocations();

            double altitude;

            if (markers == null)
            {
                if (!this.trackAdjacencyList.contains(leg))
                {
                    altitude = this.getControlPointAltitude(legLocations[0]);
                    ControlPointMarker cp = this.makeControlPoint(new Position(legLocations[0], altitude),
                        this.getLocationControlPointAttributes(), 0, i, LOCATION);
                    controlPoints.add(cp);
                }

                altitude = this.getControlPointAltitude(legLocations[1]);
                ControlPointMarker cp = this.makeControlPoint(new Position(legLocations[1], altitude),
                    this.getLocationControlPointAttributes(), 1, i, LOCATION);
                controlPoints.add(cp);
            }
            else
            {
                if (!this.trackAdjacencyList.contains(leg))
                {
                    altitude = this.getControlPointAltitude(legLocations[0]);
                    markerIterator.next().setPosition(new Position(legLocations[0], altitude));
                }

                altitude = this.getControlPointAltitude(legLocations[1]);
                markerIterator.next().setPosition(new Position(legLocations[1], altitude));
            }
        }

        // Update the width control points.
        for (int i = 0; i < legs.size(); i++)
        {
            Box leg = legs.get(i);
            LatLon[] legLocations = leg.getLocations();
            double[] widths = leg.getWidths();

            Position cwLPosition = this.computeRectangularEdgeLocation(legLocations[0], legLocations[1], -widths[0]);
            Position cwRPosition = this.computeRectangularEdgeLocation(legLocations[0], legLocations[1], widths[1]);

            if (markers == null)
            {
                Position cpPosition = new Position(cwLPosition, cwLPosition.getAltitude());
                controlPoints.add(
                    this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 2, i, LEFT_WIDTH));
                cpPosition = new Position(cwRPosition, cwRPosition.getAltitude());
                controlPoints.add(
                    this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 3, i, RIGHT_WIDTH));
            }
            else
            {
                //noinspection ConstantConditions
                markerIterator.next().setPosition(new Position(cwLPosition, cwLPosition.getAltitude()));
                markerIterator.next().setPosition(new Position(cwRPosition, cwRPosition.getAltitude()));
            }
        }

        // Update the rotation control points.
        List<LatLon> trackLocations = new ArrayList<LatLon>();
        for (Box leg : legs)
        {
            trackLocations.add(leg.getLocations()[0]);
            trackLocations.add(leg.getLocations()[1]);
        }

        Globe globe = this.getWwd().getModel().getGlobe();
        LatLon trackCenter = LatLon.getCenter(globe, trackLocations);
        double trackCenterAltitude = this.getControlPointAltitude(trackCenter);
        Angle trackRadius = LatLon.getAverageDistance(globe, trackCenter, trackLocations);
        double[] widths = legs.get(0).getWidths();
        trackRadius = trackRadius.addRadians((widths[0] + widths[1]) / globe.getEquatorialRadius());

        Angle heading = this.getCurrentHeading();
        LatLon rotationLocation = LatLon.greatCircleEndPosition(trackCenter, heading, trackRadius);
        double rotationAltitude = this.getControlPointAltitude(rotationLocation);

        if (markers == null)
        {
            Position cpPosition = new Position(rotationLocation, rotationAltitude);
            controlPoints.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 4, ROTATION));
        }
        else
        {
            //noinspection ConstantConditions
            markerIterator.next().setPosition(new Position(rotationLocation, rotationAltitude));
        }

        if (markers == null)
            this.getControlPointLayer().setMarkers(controlPoints);

        this.updateOrientationLine(new Position(trackCenter, trackCenterAltitude),
            new Position(rotationLocation, rotationAltitude));

        markers = this.getControlPointLayer().getMarkers();
        for (Marker marker : markers)
        {
            ControlPointMarker cp = (ControlPointMarker) marker;

            if (cp.getId() == 2)
                cp.size = legs.get(cp.getLeg()).getWidths()[0];
            else if (cp.getId() == 3)
                cp.size = legs.get(cp.getLeg()).getWidths()[1];
            else if (cp.getId() == 4)
                cp.rotation = heading;
        }
    }

    protected void reshapeSurfacePolygon(Position terrainPosition, ControlPointMarker controlPoint)
    {
        Iterable<? extends LatLon> corners = this.getShape() instanceof SurfacePolygon
            ? ((SurfacePolygon) this.getShape()).getLocations() : ((SurfacePolyline) this.getShape()).getLocations();

        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon ll : corners)
        {
            locations.add(ll);
        }

        if (controlPoint != null && controlPoint.getPurpose().equals(ROTATION))
        {
            // Rotate the polygon.
            this.rotateLocations(terrainPosition, locations);
        }
        else if (controlPoint != null) // control point location change or add/delete a control point
        {
            if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0
                && this.isExtensionEnabled())
            {
                int minSize = this.getShape() instanceof SurfacePolygon ? 3 : 2;
                if (locations.size() > minSize)
                {
                    // Delete the control point.
                    locations.remove(controlPoint.getId());
                    this.getControlPointLayer().setMarkers(null);
                }
            }
            else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
                && this.isExtensionEnabled()
                && this.getShape() instanceof SurfacePolyline)
            {
                this.appendLocation(controlPoint, locations);
                this.getControlPointLayer().setMarkers(null);
            }
            else // location change
            {
                this.moveLocation(controlPoint, terrainPosition, locations);
            }
        }
        else if ((this.getCurrentEvent().getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0
            && this.isExtensionEnabled())
        {
            this.addNearestLocation(terrainPosition, 0, locations);
        }

        if (this.getShape() instanceof SurfacePolygon)
            ((SurfacePolygon) this.getShape()).setLocations(locations);
        else
            ((SurfacePolyline) this.getShape()).setLocations(locations);
    }

    protected void updateSurfacePolygonControlPoints()
    {
        Iterable<? extends LatLon> locationsIterable = null;

        if (this.getShape() instanceof SurfacePolygon)
            locationsIterable = ((SurfacePolygon) this.getShape()).getLocations();
        else if (this.getShape() instanceof SurfacePolyline)
            locationsIterable = ((SurfacePolyline) this.getShape()).getLocations();

        if (locationsIterable == null)
            return;

        java.util.List<LatLon> locations = new ArrayList<LatLon>();
        for (LatLon location : locationsIterable)
        {
            locations.add(location);
        }

        if (locations.size() < 2)
            return;

        Globe globe = this.getWwd().getModel().getGlobe();

        LatLon polygonCenter = LatLon.getCenter(globe, locations);
        Angle shapeRadius = LatLon.getAverageDistance(globe, polygonCenter, locations);
        shapeRadius = shapeRadius.multiply(1.2);
        Angle heading = this.getCurrentHeading();
        LatLon rotationControlLocation = LatLon.greatCircleEndPosition(polygonCenter, heading, shapeRadius);

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            ArrayList<Marker> controlPoints = new ArrayList<Marker>();
            int i = 0;
            for (LatLon corner : locations)
            {
                Position cpPosition = new Position(corner, 0);
                controlPoints.add(
                    this.makeControlPoint(cpPosition, this.getLocationControlPointAttributes(), i++, LOCATION));
            }

            // Create a control point for the rotation control.
            Position cpPosition = new Position(rotationControlLocation, 0);
            controlPoints.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), i, ROTATION));

            this.getControlPointLayer().setMarkers(controlPoints);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            for (LatLon cpPosition : locations)
            {
                markerIterator.next().setPosition(new Position(cpPosition, 0));
            }

            // Update the polygon's rotation control point.
            markerIterator.next().setPosition(new Position(rotationControlLocation, 0));
        }

        // Update the heading annotation.
        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        for (LatLon ignored : locations)
        {
            markerIterator.next();
        }
        ((ControlPointMarker) markerIterator.next()).rotation = heading;

        // Update the rotation orientation line.
        this.updateOrientationLine(new Position(polygonCenter, 0),
            new Position(rotationControlLocation, 0));
    }

    protected void reshapeSurfaceCircle(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        SurfaceCircle circle = (SurfaceCircle) this.getShape();

        Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);

        Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(circle.getCenter());
        Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        double radius = circle.getRadius() + delta.dot3(vMarker);
        if (radius > 0)
            circle.setRadius(radius);
    }

    protected void updateSurfaceCircleControlPoints()
    {
        SurfaceCircle circle = (SurfaceCircle) this.getShape();

        LatLon radiusLocation = LatLon.greatCircleEndPosition(circle.getCenter(), Angle.fromDegrees(90),
            Angle.fromRadians(circle.getRadius() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            Position cpPosition = new Position(radiusLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, OUTER_RADIUS));
            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            markers.iterator().next().setPosition(new Position(radiusLocation, 0));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = circle.getRadius();
    }

    protected void reshapeSurfaceSquare(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        SurfaceSquare square = (SurfaceSquare) this.getShape();

        Vec4 terrainPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            this.getPreviousPosition());
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(square.getCenter());
        Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint);

        if (controlPoint.getPurpose().equals(RIGHT_WIDTH))
        {
            double size = square.getSize() + delta.dot3(vMarker.normalize3());
            if (size > 0)
                square.setSize(size);
        }
        else // rotation
        {
            Angle oldHeading = LatLon.greatCircleAzimuth(square.getCenter(), this.getPreviousPosition());
            Angle deltaHeading = LatLon.greatCircleAzimuth(square.getCenter(), terrainPosition).subtract(oldHeading);
            square.setHeading(this.normalizedHeading(square.getHeading(), deltaHeading));
        }
    }

    protected void updateSurfaceSquareControlPoints()
    {
        SurfaceSquare square = (SurfaceSquare) this.getShape();

        LatLon sizeLocation = LatLon.greatCircleEndPosition(square.getCenter(),
            Angle.fromDegrees(90 + square.getHeading().degrees),
            Angle.fromRadians(0.5 * square.getSize() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        LatLon rotationLocation = LatLon.greatCircleEndPosition(square.getCenter(),
            Angle.fromDegrees(square.getHeading().degrees),
            Angle.fromRadians(0.7 * square.getSize() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(1);
            Position cpPosition = new Position(sizeLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, RIGHT_WIDTH));

            cpPosition = new Position(rotationLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 1, ROTATION));

            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(sizeLocation, 0));
            markerIterator.next().setPosition(new Position(rotationLocation, 0));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = square.getSize();
        ((ControlPointMarker) markerIterator.next()).rotation = square.getHeading();

        this.updateOrientationLine(new Position(square.getCenter(), 0), new Position(rotationLocation, 0));
    }

    protected void reshapeSurfaceQuad(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        SurfaceQuad quad = (SurfaceQuad) this.getShape();

        Vec4 terrainPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            this.getPreviousPosition());
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(quad.getCenter());
        Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        if (controlPoint.getPurpose().equals(WIDTH) || controlPoint.getPurpose().equals(HEIGHT))
        {
            double width = quad.getWidth() + (controlPoint.getId() == 0 ? delta.dot3(vMarker) : 0);
            double height = quad.getHeight() + (controlPoint.getId() == 1 ? delta.dot3(vMarker) : 0);
            if (width > 0 && height > 0)
                quad.setSize(width, height);
        }
        else
        {
            Angle oldHeading = LatLon.greatCircleAzimuth(quad.getCenter(), this.getPreviousPosition());
            Angle deltaHeading = LatLon.greatCircleAzimuth(quad.getCenter(), terrainPosition).subtract(oldHeading);
            quad.setHeading(this.normalizedHeading(quad.getHeading(), deltaHeading));
        }
    }

    protected void updateSurfaceQuadControlPoints()
    {
        SurfaceQuad quad = (SurfaceQuad) this.getShape();

        LatLon widthLocation = LatLon.greatCircleEndPosition(quad.getCenter(),
            Angle.fromDegrees(90 + quad.getHeading().degrees),
            Angle.fromRadians(0.5 * quad.getWidth() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        LatLon heightLocation = LatLon.greatCircleEndPosition(quad.getCenter(),
            Angle.fromDegrees(quad.getHeading().degrees),
            Angle.fromRadians(0.5 * quad.getHeight() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        LatLon rotationLocation = LatLon.greatCircleEndPosition(quad.getCenter(),
            Angle.fromDegrees(quad.getHeading().degrees),
            Angle.fromRadians(0.7 * quad.getHeight() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(2);
            Position cpPosition = new Position(widthLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, WIDTH));
            cpPosition = new Position(heightLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 1, HEIGHT));

            cpPosition = new Position(rotationLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 2, ROTATION));

            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(widthLocation, 0));
            markerIterator.next().setPosition(new Position(heightLocation, 0));
            markerIterator.next().setPosition(new Position(rotationLocation, 0));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = quad.getWidth();
        ((ControlPointMarker) markerIterator.next()).size = quad.getHeight();
        ((ControlPointMarker) markerIterator.next()).rotation = quad.getHeading();

        this.updateOrientationLine(new Position(quad.getCenter(), 0), new Position(rotationLocation, 0));
    }

    protected void reshapeSurfaceEllipse(Position terrainPosition, ControlPointMarker controlPoint)
    {
        if (controlPoint == null)
            return; // Cannot add locations to this shape.

        SurfaceEllipse ellipse = (SurfaceEllipse) this.getShape();

        Vec4 terrainPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(terrainPosition);
        Vec4 previousPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            this.getPreviousPosition());
        Vec4 delta = terrainPoint.subtract3(previousPoint);

        Vec4 centerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(ellipse.getCenter());
        Vec4 markerPoint = this.getWwd().getModel().getGlobe().computeEllipsoidalPointFromLocation(
            controlPoint.getPosition());
        Vec4 vMarker = markerPoint.subtract3(centerPoint).normalize3();

        if (controlPoint.getPurpose().equals(WIDTH) || controlPoint.getPurpose().equals(HEIGHT))
        {
            double majorRadius = ellipse.getMajorRadius() + (controlPoint.getId() == 0 ? delta.dot3(vMarker) : 0);
            double minorRadius = ellipse.getMinorRadius() + (controlPoint.getId() == 1 ? delta.dot3(vMarker) : 0);
            if (majorRadius > 0 && minorRadius > 0)
                ellipse.setRadii(majorRadius, minorRadius);
        }
        else
        {
            Angle oldHeading = LatLon.greatCircleAzimuth(ellipse.getCenter(), this.getPreviousPosition());
            Angle deltaHeading = LatLon.greatCircleAzimuth(ellipse.getCenter(), terrainPosition).subtract(oldHeading);
            ellipse.setHeading(this.normalizedHeading(ellipse.getHeading(), deltaHeading));
        }

        this.updateAnnotation(controlPoint);
    }

    protected void updateSurfaceEllipseControlPoints()
    {
        SurfaceEllipse ellipse = (SurfaceEllipse) this.getShape();

        LatLon majorLocation = LatLon.greatCircleEndPosition(ellipse.getCenter(),
            Angle.fromDegrees(90 + ellipse.getHeading().degrees),
            Angle.fromRadians(ellipse.getMajorRadius() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        LatLon minorLocation = LatLon.greatCircleEndPosition(ellipse.getCenter(),
            Angle.fromDegrees(ellipse.getHeading().degrees),
            Angle.fromRadians(ellipse.getMinorRadius() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        LatLon rotationLocation = LatLon.greatCircleEndPosition(ellipse.getCenter(),
            Angle.fromDegrees(ellipse.getHeading().degrees),
            Angle.fromRadians(
                1.15 * ellipse.getMinorRadius() / this.getWwd().getModel().getGlobe().getEquatorialRadius()));

        Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
        if (markers == null)
        {
            java.util.List<Marker> markerList = new ArrayList<Marker>(2);
            Position cpPosition = new Position(majorLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 0, WIDTH));
            this.getControlPointLayer().setMarkers(markerList);
            cpPosition = new Position(minorLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getSizeControlPointAttributes(), 1, HEIGHT));

            cpPosition = new Position(rotationLocation, 0);
            markerList.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 2, ROTATION));

            this.getControlPointLayer().setMarkers(markerList);
        }
        else
        {
            Iterator<Marker> markerIterator = markers.iterator();
            markerIterator.next().setPosition(new Position(majorLocation, 0));
            markerIterator.next().setPosition(new Position(minorLocation, 0));
            markerIterator.next().setPosition(new Position(rotationLocation, 0));
        }

        Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
        ((ControlPointMarker) markerIterator.next()).size = ellipse.getMajorRadius();
        ((ControlPointMarker) markerIterator.next()).size = ellipse.getMinorRadius();
        ((ControlPointMarker) markerIterator.next()).rotation = ellipse.getHeading();

        this.updateOrientationLine(new Position(ellipse.getCenter(), 0), new Position(rotationLocation, 0));
    }
}
