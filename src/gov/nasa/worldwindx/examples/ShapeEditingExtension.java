/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.util.ShapeEditor;

import java.awt.event.*;
import java.util.*;

/**
 * Shows how to extend {@link gov.nasa.worldwind.util.ShapeEditor} to operate on a custom Renderable. This example
 * defines a custom renderable and an extension of <code>ShapeEditor</code> that knows how to edit it.
 *
 * @author tag
 * @version $Id: ShapeEditingExtension.java 2421 2014-11-09 00:30:19Z tgaskins $
 */
public class ShapeEditingExtension extends ApplicationTemplate
{
    /**
     * Defines a custom Renderable that we'll use to illustrate editing extension.
     */
    public static class Arrow implements Renderable, Movable2, Highlightable, Attributable, Draggable
    {
        protected Path shaft;
        protected Path head;
        protected boolean dragEnabled = true;
        protected DraggableSupport draggableSupport = null;

        public Arrow(LatLon location0, LatLon location1, double altitude)
        {
            this.shaft = new Path(new Position(location0, altitude), new Position(location1, altitude));
            this.head = new Path();

            this.shaft.setDelegateOwner(this);
            this.head.setDelegateOwner(this);

            this.shaft.setPathType(AVKey.GREAT_CIRCLE);
            this.head.setPathType(AVKey.GREAT_CIRCLE);
        }

        public Arrow(Arrow source)
        {
            this.shaft = new Path(source.shaft);
            this.head = new Path(source.head);
        }

        public void setLocations(LatLon location0, LatLon location1)
        {
            double altitude = this.getAltitude();
            List<Position> positions = new ArrayList<Position>(2);
            positions.add(new Position(location0, altitude));
            positions.add(new Position(location1, altitude));
            this.shaft.setPositions(positions);
            this.head.setPositions(new ArrayList<Position>(0)); // causes arrowhead to be rebuilt
        }

        public LatLon[] getLocations()
        {
            Iterable<? extends Position> positions = this.shaft.getPositions();
            Iterator<? extends Position> iterator = positions.iterator();

            return new LatLon[] {new LatLon(iterator.next()), new LatLon(iterator.next())};
        }

        public double getAltitude()
        {
            return this.shaft.getPositions().iterator().next().getAltitude();
        }

        public int getAltitudeMode()
        {
            return this.shaft.getAltitudeMode();
        }

        public void setAttributes(ShapeAttributes attributes)
        {
            this.shaft.setAttributes(attributes);
            this.head.setAttributes(attributes);
        }

        public ShapeAttributes getAttributes()
        {
            return this.shaft.getAttributes();
        }

        public void setHighlightAttributes(ShapeAttributes attributes)
        {
            this.shaft.setHighlightAttributes(attributes);
            this.head.setHighlightAttributes(attributes);
        }

        public ShapeAttributes getHighlightAttributes()
        {
            return this.shaft.getHighlightAttributes();
        }

        @Override
        public boolean isHighlighted()
        {
            return this.shaft.isHighlighted();
        }

        @Override
        public void setHighlighted(boolean highlighted)
        {
            this.shaft.setHighlighted(highlighted);
            this.head.setHighlighted(highlighted);
        }

        @Override
        public void render(DrawContext dc)
        {
            this.shaft.render(dc);

            // Make the arrowhead if it's moved since we last made it.
            if (this.head.getPositions() == null || !this.head.getPositions().iterator().hasNext())
                this.makeArrowhead(dc);

            this.head.render(dc);
        }

        protected void makeArrowhead(DrawContext dc)
        {
            Globe globe = dc.getGlobe();

            Iterable<? extends Position> positions = this.shaft.getPositions();
            Iterator<? extends Position> iterator = positions.iterator();
            Position position0 = iterator.next();
            Position position1 = iterator.next();

            Angle shaftDistance = LatLon.rhumbDistance(position0, position1);
            double shaftLength = shaftDistance.radians * globe.getEquatorialRadius();
            double headLength = 0.1 * shaftLength;
            double headWidth = headLength * Math.tan(Math.PI / 4);

            Vec4 point0 = globe.computeEllipsoidalPointFromLocation(position0);
            Vec4 point1 = globe.computeEllipsoidalPointFromLocation(position1);
            Vec4 shaftVec = point1.subtract3(point0).multiply3(0.9);

            Vec4 surfaceNormal = globe.computeEllipsoidalNormalAtLocation(position1.getLatitude(),
                position1.getLongitude());

            Vec4 perpendicularVec = surfaceNormal.cross3(shaftVec).normalize3().multiply3(headWidth);
            Vec4 leftHeadPoint = point0.add3(shaftVec).add3(perpendicularVec);
            LatLon leftHeadLocation = globe.computePositionFromEllipsoidalPoint(leftHeadPoint);

            perpendicularVec = shaftVec.cross3(surfaceNormal).normalize3().multiply3(headWidth);
            Vec4 rightHeadPoint = point0.add3(shaftVec).add3(perpendicularVec);
            LatLon rightHeadLocation = globe.computePositionFromEllipsoidalPoint(rightHeadPoint);

            List<Position> newPositions = new ArrayList<Position>(2);
            newPositions.add(new Position(leftHeadLocation, this.getAltitude()));
            newPositions.add(position1);
            newPositions.add(new Position(rightHeadLocation, this.getAltitude()));
            this.head.setPositions(newPositions);
        }

        @Override
        public Position getReferencePosition()
        {
            return this.shaft.getReferencePosition();
        }

        @Override
        public void moveTo(Globe globe, Position position)
        {
            this.shaft.moveTo(globe, position);
            this.head.setPositions(new ArrayList<Position>(0));
        }

        @Override
        public boolean isDragEnabled()
        {
            return this.dragEnabled;
        }

        @Override
        public void setDragEnabled(boolean enabled)
        {
            this.dragEnabled = enabled;
        }

        @Override
        public void drag(DragContext dragContext)
        {
            if (!this.dragEnabled)
                return;

            if (this.draggableSupport == null)
                this.draggableSupport = new DraggableSupport(this, this.getAltitudeMode());

            this.doDrag(dragContext);
        }

        protected void doDrag(DragContext dragContext)
        {
            this.draggableSupport.dragGlobeSizeConstant(dragContext);
        }
    }

    /**
     * Defines an extension to {@link gov.nasa.worldwind.util.ShapeEditor} that knows how to operate on the custom shape
     * defined above.
     */
    public static class ShapeEditorExtension extends ShapeEditor
    {
        public ShapeEditorExtension(WorldWindow wwd, Renderable shape)
        {
            super(wwd, shape);
        }

        protected int getShapeAltitudeMode()
        {
            if (this.getShape() instanceof Arrow)
                return ((Arrow) this.getShape()).getAltitudeMode();
            else
                return super.getShapeAltitudeMode();
        }

        protected double doGetControlPointAltitude(LatLon location, Renderable shape)
        {
            if (shape instanceof Arrow)
                return super.doGetControlPointAltitude(location, ((Arrow) shape).shaft);
            else
                return super.doGetControlPointAltitude(location, shape);
        }

        /**
         * Creates the shape that's shown in the original position while the original shape is edited.
         *
         * @return a copy of the original shape.
         */
        protected Renderable doMakeShadowShape()
        {
            // Call the superclass to let it take care of shapes it knows about.
            Renderable shadow = super.doMakeShadowShape();
            if (shadow != null)
                return shadow;

            /**
             * It doesn't know about the current shape, so it must be the custom shape.
             */
            if (this.getShape() instanceof Arrow)
                return new Arrow((Arrow) this.getShape());

            return null;
        }

        /**
         * Called during editing when a control point is moved. (Not when the whole object is moved.)
         *
         * @param controlPoint    the control point selected.
         * @param terrainPosition the terrain position under the cursor.
         */
        protected void doReshapeShape(ControlPointMarker controlPoint, Position terrainPosition)
        {
            // First see if it's the custom shape. If not, defer to the superclass.
            if (this.getShape() instanceof Arrow)
                this.reshapeArrow(controlPoint, terrainPosition);
            else
                super.doReshapeShape(controlPoint, terrainPosition);
        }

        /**
         * Called during editing and moving to reposition the control points. This method should only compute and set
         * the new control point and other affordances. It should not attempt to edit the shape.
         */
        protected void updateControlPoints()
        {
            // First see if it's the custom shape. If not, defer to the superclass.
            if (this.getShape() instanceof Arrow)
                this.updateArrowControlPoints();
            else
                super.updateControlPoints();
        }

        /**
         * Edit the arrow according to the control point that is being moved. In the case of the custom shape, there is
         * only one control point, and that's for rotation.
         *
         * @param controlPoint    the control point being moved by the user.
         * @param terrainPosition the terrain position under the cursor.
         */
        protected void reshapeArrow(ControlPointMarker controlPoint, Position terrainPosition)
        {
            if (controlPoint == null)
                return; // this shape does not support control point insertion.

            Arrow arrow = (Arrow) this.getShape();

            // Compute the new location for the arrowhead.
            Globe globe = this.getWwd().getModel().getGlobe();
            Vec4 delta = this.computeControlPointDelta(this.getPreviousPosition(), terrainPosition);
            Vec4 markerPoint = globe.computeEllipsoidalPointFromLocation(controlPoint.getPosition());
            Position markerPosition = globe.computePositionFromEllipsoidalPoint(markerPoint.add3(delta));
            arrow.setLocations(arrow.getLocations()[0], markerPosition);
        }

        /**
         * Modify the control points to conform to the current state of the shape being edited.
         */
        protected void updateArrowControlPoints()
        {
            Arrow arrow = (Arrow) this.getShape();

            LatLon[] locations = arrow.getLocations();

            // Get a handle on the current control points. If the handle is null, then the control points must be
            // created.
            Iterable<Marker> markers = this.getControlPointLayer().getMarkers();
            if (markers == null)
            {
                // There is only one control point. Compute its location, create it and add it to the list.
                java.util.List<Marker> markerList = new ArrayList<Marker>(1);
                double altitude = this.getControlPointAltitude(locations[1]);
                Position cpPosition = new Position(locations[1], altitude);
                markerList.add(this.makeControlPoint(cpPosition, this.getAngleControlPointAttributes(), 0, ROTATION));

                this.getControlPointLayer().setMarkers(markerList);
            }
            else
            {
                // The control point exists but must be updated to the new end position of the shape.
                Iterator<Marker> markerIterator = markers.iterator();
                double altitude = this.getControlPointAltitude(locations[1]);
                markerIterator.next().setPosition(new Position(locations[1], altitude));
            }

            // Update the control point field that indicates the current heading of the shape.
            Angle arrowHeading = LatLon.greatCircleAzimuth(locations[0], locations[1]);
            Iterator<Marker> markerIterator = this.getControlPointLayer().getMarkers().iterator();
            ((ControlPointMarker) markerIterator.next()).setRotation(this.normalizedHeading(arrowHeading, Angle.ZERO));
        }

        protected void updateAnnotation(ControlPointMarker controlPoint)
        {
            super.updateAnnotation(controlPoint);

            if (controlPoint != null)
            {
                // Add the arrowhead location to the readout, which by default for this control point shows only the
                // rotation angle.
                String text = this.getAnnotation().getText();
                text += "\n" + this.getUnitsFormat().latLon2(controlPoint.getPosition());
                this.getAnnotation().setText(text);
            }
        }
    }

    /**
     * This is the app that instantiates the custom shape and the extended shape editor.
     */
    public static class AppFrame extends ApplicationTemplate.AppFrame implements SelectListener
    {
        protected ShapeEditor editor;
        protected ShapeAttributes lastAttrs;

        public AppFrame()
        {
            // Receive selection event to determine when to place the shape in editing mode.
            this.getWwd().addSelectListener(this);

            // Create the custom shape, add it to a layer and add the layer to the World Window's layer list.
            RenderableLayer layer = new RenderableLayer();

            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.BLUE);
            attrs.setOutlineWidth(2);
            attrs.setEnableAntialiasing(true);

            AirspaceAttributes highlightAttrs = new BasicAirspaceAttributes(attrs);
            highlightAttrs.setOutlineMaterial(Material.RED);

            SurfaceEllipse ellipse = new SurfaceEllipse(attrs, LatLon.fromDegrees(40, -115), 1e5, 1.5e5);
            ellipse.setHeading(Angle.fromDegrees(30));
            ellipse.setHighlightAttributes(highlightAttrs);
            layer.addRenderable(ellipse);

            Arrow arrow = new Arrow(LatLon.fromDegrees(40, -115), LatLon.fromDegrees(41, -115), 4e4);
            arrow.setAttributes(attrs);
            arrow.setHighlightAttributes(highlightAttrs);
            layer.addRenderable(arrow);

            insertBeforePlacenames(getWwd(), layer);
        }

        @Override
        public void selected(SelectEvent event)
        {
            PickedObject topObject = event.getTopPickedObject();

            // Enable and disable editing via left click.
            if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
            {
                if (topObject != null && topObject.getObject() instanceof Renderable)
                {
                    if (this.editor == null)
                    {
                        // Enable editing of the selected shape.
                        this.editor = new ShapeEditorExtension(getWwd(), (Renderable) topObject.getObject());
                        this.editor.setArmed(true);
                        this.keepShapeHighlighted(true);
                        event.consume();
                    }
                    else if (this.editor.getShape() != event.getTopObject())
                    {
                        // Switch editor to a different shape.
                        this.keepShapeHighlighted(false);
                        this.editor.setArmed(false);
                        this.editor = new ShapeEditorExtension(getWwd(), (Renderable) topObject.getObject());
                        this.editor.setArmed(true);
                        this.keepShapeHighlighted(true);
                        event.consume();
                    }
                    else if ((event.getMouseEvent().getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == 0
                        && (event.getMouseEvent().getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == 0)
                    {
                        // Disable editing of the current shape. Shift and Alt are used by the editor, so ignore
                        // events with those buttons down.
                        this.editor.setArmed(false);
                        this.keepShapeHighlighted(false);
                        this.editor = null;
                        event.consume();
                    }
                }
            }
        }

        /**
         * Keeps the shape in what appears to be its highlighted state for the duration of its time being edited.
         *
         * @param tf <code>true</code> to enable constant highlighting, otherwise false.
         */
        protected void keepShapeHighlighted(boolean tf)
        {
            if (tf)
            {
                // Set the shape's normal attributes to its highlighted attributes.
                this.lastAttrs = ((Attributable) this.editor.getShape()).getAttributes();
                ((Attributable) this.editor.getShape()).setAttributes(
                    ((Attributable) this.editor.getShape()).getHighlightAttributes());
            }
            else
            {
                // restore the shape's original normal attributes.
                ((Attributable) this.editor.getShape()).setAttributes(this.lastAttrs);
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Shape Editing Extension", ShapeEditingExtension.AppFrame.class);
    }
}
