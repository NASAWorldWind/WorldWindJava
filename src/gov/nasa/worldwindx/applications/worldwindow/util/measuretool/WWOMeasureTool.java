/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.util.measuretool;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: WWOMeasureTool.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWOMeasureTool extends AVListImpl
    implements Disposable, MouseListener, MouseMotionListener, SelectListener, PositionListener, RenderingListener
{
    public interface MeasureDisplay
    {
        public void updateMeasureDisplay(Position position);

        public void addToLayer(RenderableLayer layer);

        public void removeFromLayer(RenderableLayer layer);

        public void setLabel(String labelName, String label);

        public String getLabel(String labelName);

        public static final String ANGLE_LABEL = "MeasureTool.AngleLabel";
        public static final String AREA_LABEL = "MeasureTool.AreaLabel";
        public static final String LENGTH_LABEL = "MeasureTool.LengthLabel";
        public static final String PERIMETER_LABEL = "MeasureTool.PerimeterLabel";
        public static final String RADIUS_LABEL = "MeasureTool.RadiusLabel";
        public static final String HEIGHT_LABEL = "MeasureTool.HeightLabel";
        public static final String WIDTH_LABEL = "MeasureTool.WidthLabel";
        public static final String HEADING_LABEL = "MeasureTool.HeadingLabel";
        public static final String CENTER_LATITUDE_LABEL = "MeasureTool.CenterLatitudeLabel";
        public static final String CENTER_LONGITUDE_LABEL = "MeasureTool.CenterLongitudeLabel";
        public static final String LATITUDE_LABEL = "MeasureTool.LatitudeLabel";
        public static final String LONGITUDE_LABEL = "MeasureTool.LongitudeLabel";
        public static final String ACCUMULATED_LABEL = "MeasureTool.AccumulatedLabel";
        public static final String MAJOR_AXIS_LABEL = "MeasureTool.MajorAxisLabel";
        public static final String MINOR_AXIS_LABEL = "MeasureTool.MinorAxisLabel";

        boolean isAnnotation(Object o);
    }

    public interface ControlPoint
    {
        public WWOMeasureTool getParent();

        public Object setValue(String key, Object value);

        public Object getValue(String key);

        public void setPosition(Position position);

        public Position getPosition();

        public void highlight(boolean tf);
    }

    public interface ControlPointList
    {
        public int size();

        public ControlPoint createControlPoint(Position position);

        public ControlPoint get(int index);

        public void add(ControlPoint controlPoint);

        public void remove(ControlPoint controlPoint);

        public void remove(int index);

        public void clear();

        public void render(DrawContext dc);

        public void addToLayer(RenderableLayer layer);

        public void removeFromLayer(RenderableLayer layer);
    }

    public static final String EVENT_POSITION_ADD = "MeasureTool.AddPosition";
    public static final String EVENT_POSITION_REMOVE = "MeasureTool.RemovePosition";
    public static final String EVENT_POSITION_REPLACE = "MeasureTool.ReplacePosition";
    public static final String EVENT_METRIC_CHANGED = "MeasureTool.MetricChanged";
    public static final String EVENT_ARMED = "MeasureTool.Armed";
    public static final String EVENT_RUBBERBAND_START = "MeasureTool.RubberBandStart";
    public static final String EVENT_RUBBERBAND_STOP = "MeasureTool.RubberBandStop";

    protected final WorldWindow wwd;
    protected BasicDragger dragger;

    protected Renderable shape;
    protected String measureShapeType;
    protected boolean regularShape;

    protected ControlPointList controlPoints;
    protected RenderableLayer controlPointsLayer;
    protected boolean showControlPoints = true;

    protected MeasureDisplay measureDisplay;
    protected boolean showAnnotation = true;
    protected UnitsFormat unitsFormat = new UnitsFormat();

    protected ArrayList<Position> positions = new ArrayList<Position>();
    protected Rectangle2D.Double shapeRectangle = null;
    protected Position shapeCenterPosition = null;
    protected Angle shapeOrientation = null;
    protected int shapeIntervals = 64;

    protected boolean armed = false;
    protected boolean active = false;
    protected boolean moving = false;
    protected boolean useRubberBand = true;
    protected boolean freeHand = false;
    protected double freeHandMinSpacing = 100;

    protected WWOMeasureTool.ControlPoint rubberBandTarget;
    protected WWOMeasureTool.ControlPoint movingTarget;
    protected WWOMeasureTool.ControlPoint lastPickedObject;

    public WWOMeasureTool(final WorldWindow wwd, Renderable shape, String lineType, RenderableLayer controlPointsLayer)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (shape == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;

        this.controlPoints = this.createControlPoints();
        this.controlPointsLayer = controlPointsLayer; // new RenderableLayer();
        this.controlPoints.addToLayer(this.controlPointsLayer);

        this.measureDisplay = this.createMeasureDisplay();
        this.measureDisplay.addToLayer(this.controlPointsLayer);

        this.getWwd().getInputHandler().addMouseListener(this);
        this.getWwd().getInputHandler().addMouseMotionListener(this);
        this.getWwd().addPositionListener(this);
        this.getWwd().addSelectListener(this);
        this.getWwd().addRenderingListener(this);

        this.shape = shape;
        if (this.shape instanceof Polyline)
            this.setMeasureShape((Polyline) this.shape, lineType);
        else
            this.setMeasureShape((SurfaceShape) this.shape);
    }

    public void dispose()
    {
        this.getWwd().getInputHandler().removeMouseListener(this);
        this.getWwd().getInputHandler().removeMouseMotionListener(this);
        this.getWwd().removePositionListener(this);
        this.getWwd().removeSelectListener(this);
        this.getWwd().removeRenderingListener(this);

        this.controlPoints.removeFromLayer(this.controlPointsLayer);
        this.measureDisplay.removeFromLayer(this.controlPointsLayer);
        this.getControlPoints().clear();
    }

    protected ControlPointList createControlPoints()
    {
        return new WWOMeasureToolControlPoints(this);
    }

    protected MeasureDisplay createMeasureDisplay()
    {
        return new WWOMeasureDisplay(this);
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public void setUnitsFormat(UnitsFormat unitsFormat)
    {
        if (unitsFormat == null)
        {
            String msg = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.unitsFormat = unitsFormat;
    }

    public UnitsFormat getUnitsFormat()
    {
        return this.unitsFormat;
    }

    public void setLabel(String labelName, String label)
    {
        this.measureDisplay.setLabel(labelName, label);
    }

    public String getLabel(String labelName)
    {
        return this.measureDisplay.getLabel(labelName);
    }

    public Renderable getShape()
    {
        return this.shape;
    }

    public boolean isShowControlPoints()
    {
        return this.showControlPoints;
    }

    public void setShowControlPoints(boolean state)
    {
        this.showControlPoints = state;
        this.controlPointsLayer.setEnabled(state);
        this.getWwd().redraw();
    }

    public boolean isShowAnnotation()
    {
        return this.showAnnotation;
    }

    public void setShowAnnotation(boolean state)
    {
        this.showAnnotation = state;
    }

    public boolean isUseRubberBand()
    {
        return this.useRubberBand;
    }

    public void setUseRubberBand(boolean state)
    {
        this.useRubberBand = state;
    }

    public boolean isFreeHand()
    {
        return this.freeHand;
    }

    public void setFreeHand(boolean state)
    {
        this.freeHand = state;
    }

    public double getFreeHandMinSpacing()
    {
        return this.freeHandMinSpacing;
    }

    public void setFreeHandMinSpacing(double distance)
    {
        this.freeHandMinSpacing = distance;
    }

    public void clear()
    {
        while (this.positions.size() > 0 || this.getControlPoints().size() > 0)
        {
            this.removeControlPoint();
        }

        this.shapeCenterPosition = null;
        this.shapeOrientation = null;
        this.shapeRectangle = null;
    }

    public String getMeasureShapeType()
    {
        return this.measureShapeType;
    }

    public ArrayList<? extends Position> getPositions()
    {
        return this.positions;
    }

    protected ControlPointList getControlPoints()
    {
        return this.controlPoints;
    }

    protected ControlPoint createControlPoint(Position position)
    {
        return this.controlPoints.createControlPoint(position);
    }

    public Rectangle2D.Double getShapeRectangle()
    {
        return this.shapeRectangle;
    }

    protected void setMeasureShape(Polyline line, String shapeType)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.setArmed(false);
        this.clear();

        this.shape = line;
        this.regularShape = false;

        // Update position list and create control points
        int i = 0;
        for (Position pos : line.getPositions())
        {
            this.positions.add(pos);
            addControlPoint(pos, "PositionIndex", i++);
        }
        // Set proper measure shape type
        this.measureShapeType = (shapeType != null && shapeType.equals(AVKey.SHAPE_PATH))
            ? AVKey.SHAPE_PATH : AVKey.SHAPE_LINE;
        this.firePropertyChange(EVENT_POSITION_REPLACE, null, null);
        this.getWwd().redraw();
    }

    protected void setMeasureShape(SurfaceShape newShape)
    {
        if (newShape == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setArmed(false);
        this.clear();

        if (newShape instanceof SurfaceQuad)
        {
            this.shape = newShape;
            this.regularShape = true;
            this.measureShapeType = newShape instanceof SurfaceSquare ? AVKey.SHAPE_SQUARE : AVKey.SHAPE_QUAD;
            // Set regular shape properties
            SurfaceQuad shape = ((SurfaceQuad) newShape);
            this.shapeCenterPosition = new Position(shape.getCenter(), 0);
            this.shapeRectangle = new Rectangle2D.Double(0, 0, shape.getWidth(), shape.getHeight());
            this.shapeOrientation = shape.getHeading();
            // Create control points for regular shapes
            this.updateShapeControlPoints();
            // Extract positions from shape
            this.updatePositionsFromShape();
        }
        else if (newShape instanceof SurfaceEllipse)
        {
            // Set measure shape type
            this.shape = newShape;
            this.regularShape = true;
            this.measureShapeType = newShape instanceof SurfaceCircle ? AVKey.SHAPE_CIRCLE : AVKey.SHAPE_ELLIPSE;
            // Set regular shape properties
            SurfaceEllipse shape = ((SurfaceEllipse) newShape);
            this.shapeCenterPosition = new Position(shape.getCenter(), 0);
            this.shapeRectangle = new Rectangle2D.Double(0, 0, shape.getMajorRadius() * 2,
                shape.getMinorRadius() * 2);
            this.shapeOrientation = shape.getHeading();
            // Create control points for regular shapes
            this.updateShapeControlPoints();
            // Extract positions from shape
            this.updatePositionsFromShape();
        }
        else // SurfacePolygon, SurfacePolyline, SurfaceSector, or some custom shape
        {
            // Set measure shape type
            this.shape = newShape;
            this.measureShapeType = AVKey.SHAPE_POLYGON;
            // Extract positions from shape
            updatePositionsFromShape();
            // Create control points for each position except the last that is the same as the first
            for (int i = 0; i < this.positions.size() - 1; i++)
            {
                addControlPoint(this.positions.get(i), "PositionIndex", i);
            }
        }

        this.firePropertyChange(EVENT_POSITION_REPLACE, null, null);
        this.getWwd().redraw();
    }

    protected boolean isRegularShape()
    {
        return this.regularShape;
    }

    // *** Metric accessors ***

    public double getLength()
    {
        if (this.shape == null)
            return -1;

        if (this.shape instanceof Polyline)
            return ((Polyline) this.shape).getLength();
        else
            return ((SurfaceShape) this.shape).getPerimeter(this.getWwd().getModel().getGlobe());
    }

    public double getArea()
    {
        return this.shape != null && this.shape instanceof SurfaceShape ?
            ((SurfaceShape) this.shape).getArea(this.getWwd().getModel().getGlobe(), true) : -1;
    }

    public double getWidth()
    {
        return this.shapeRectangle != null ? this.shapeRectangle.width : -1;
    }

    public double getHeight()
    {
        return this.shapeRectangle != null ? this.shapeRectangle.height : -1;
    }

    public Angle getOrientation()
    {
        return this.shapeOrientation;
    }

    public Position getCenterPosition()
    {
        return this.shapeCenterPosition;
    }

    // *** Editing shapes ***

    /** Add a control point to the current measure shape at the cuurrent WorldWindow position. */
    public void addControlPoint()
    {
        Position curPos = this.getWwd().getCurrentPosition();
        if (curPos == null)
            return;

        if (this.isRegularShape())
        {
            // Regular shapes are defined in two steps: 1. center, 2. east point.
            if (this.shapeCenterPosition == null)
            {
                this.shapeCenterPosition = curPos;
                updateShapeControlPoints();
            }
            else if (this.shapeRectangle == null)
            {
                // Compute shape rectangle and heading, curPos being east of center
                updateShapeProperties("East", curPos);
                // Update or create control points
                updateShapeControlPoints();
            }
        }
        else
        {
            if (!this.measureShapeType.equals(AVKey.SHAPE_POLYGON) || this.positions.size() <= 1)
            {
                // Line, path or polygons with less then two points
                this.positions.add(curPos);
                addControlPoint(this.positions.get(this.positions.size() - 1), "PositionIndex",
                    this.positions.size() - 1);
                if (this.measureShapeType.equals(AVKey.SHAPE_POLYGON) && this.positions.size() == 2)
                {
                    // Once we have two points of a polygon, add an extra position
                    // to loop back to the first position and have a closed shape
                    this.positions.add(this.positions.get(0));
                }
                if (this.measureShapeType.equals(AVKey.SHAPE_LINE) && this.positions.size() > 1)
                {
                    // Two points on a line, update line heading info
                    this.shapeOrientation = LatLon.greatCircleAzimuth(this.positions.get(0), this.positions.get(1));
                }
            }
            else
            {
                // For polygons with more then 2 points, the last position is the same as the first, so insert before it
                this.positions.add(positions.size() - 1, curPos);
                addControlPoint(this.positions.get(this.positions.size() - 2), "PositionIndex",
                    this.positions.size() - 2);
            }
        }
        // Update screen shapes
        updateMeasureShape();
        firePropertyChange(EVENT_POSITION_ADD, null, curPos);
        this.getWwd().redraw();
    }

    /** Remove the last control point from the current measure shape. */
    public void removeControlPoint()
    {
        Position currentLastPosition = null;
        if (this.isRegularShape())
        {
            if (this.shapeRectangle != null)
            {
                this.shapeRectangle = null;
                this.shapeOrientation = null;
                this.positions.clear();
                // remove all control points except center which is first
                while (this.getControlPoints().size() > 1)
                {
                    this.getControlPoints().remove(1);
                }
            }
            else if (this.shapeCenterPosition != null)
            {
                this.shapeCenterPosition = null;
                this.getControlPoints().clear();
            }
        }
        else
        {
            if (this.positions.size() == 0)
                return;

            if (!this.measureShapeType.equals(AVKey.SHAPE_POLYGON) || this.positions.size() == 1)
            {
                currentLastPosition = this.positions.get(this.positions.size() - 1);
                this.positions.remove(this.positions.size() - 1);
            }
            else
            {
                // For polygons with more then 2 points, the last position is the same as the first, so remove before it
                currentLastPosition = this.positions.get(this.positions.size() - 2);
                this.positions.remove(this.positions.size() - 2);
                if (positions.size() == 2)
                    positions.remove(1); // remove last loop position when a polygon shrank to only two (same) positions
            }
            if (this.getControlPoints().size() > 0)
                this.getControlPoints().remove(this.getControlPoints().size() - 1);
        }

        // Update screen shapes
        updateMeasureShape();
        this.firePropertyChange(EVENT_POSITION_REMOVE, currentLastPosition, null);
        this.getWwd().redraw();
    }

    /**
     * Update the current measure shape according to a given control point position.
     *
     * @param point one of the shape control points.
     */
    public void moveControlPoint(ControlPoint point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (point.getValue("Control") != null)
        {
            // Update shape properties
            updateShapeProperties((String) point.getValue("Control"), point.getPosition());
            updateShapeControlPoints();
            //positions = makeShapePositions();
        }

        if (point.getValue("PositionIndex") != null)
        {
            int positionIndex = (Integer) point.getValue("PositionIndex");
            // Update positions
            Position surfacePosition = computeSurfacePosition(point.getPosition());
            positions.set(positionIndex, surfacePosition);
            // Update last pos too if polygon and first pos changed
            if (measureShapeType.equals(AVKey.SHAPE_POLYGON) && positions.size() > 2 && positionIndex == 0)
                positions.set(positions.size() - 1, surfacePosition);
            // Update heading for simple line
            if (measureShapeType.equals(AVKey.SHAPE_LINE) && positions.size() > 1)
                shapeOrientation = LatLon.greatCircleAzimuth(positions.get(0), positions.get(1));
        }

        // Update rendered shapes
        updateMeasureShape();
    }

    /**
     * Move the current measure shape along a great circle arc at a given azimuth <code>Angle</code> for a given
     * distance <code>Angle</code>.
     *
     * @param azimuth  the azimuth <code>Angle</code>.
     * @param distance the distance <code>Angle</code>.
     */
    public void moveMeasureShape(Angle azimuth, Angle distance)
    {
        if (distance == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (azimuth == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.isRegularShape())
        {
            // Move regular shape center
            if (getControlPoints().size() > 0)
            {
                ControlPoint point = getControlPoints().get(0);
                point.setPosition(
                    new Position(LatLon.greatCircleEndPosition(point.getPosition(), azimuth, distance), 0));
                moveControlPoint(point);
            }
        }
        else
        {
            // Move all positions and control points
            for (int i = 0; i < positions.size(); i++)
            {
                Position newPos = computeSurfacePosition(
                    LatLon.greatCircleEndPosition(positions.get(i), azimuth, distance));
                positions.set(i, newPos);
                if (!this.measureShapeType.equals(AVKey.SHAPE_POLYGON) || i < positions.size() - 1)
                    getControlPoints().get(i).setPosition(new Position(newPos, 0));
            }
            // Update heading for simple line
            if (measureShapeType.equals(AVKey.SHAPE_LINE) && positions.size() > 1)
                shapeOrientation = LatLon.greatCircleAzimuth(positions.get(0), positions.get(1));
            // Update rendered shapes
            updateMeasureShape();
        }
    }

    protected Position computeSurfacePosition(LatLon latLon)
    {
        Vec4 surfacePoint = getWwd().getSceneController().getTerrain().getSurfacePoint(latLon.getLatitude(),
            latLon.getLongitude());
        if (surfacePoint != null)
            return getWwd().getModel().getGlobe().computePositionFromPoint(surfacePoint);
        else
            return new Position(latLon, getWwd().getModel().getGlobe().getElevation(latLon.getLatitude(),
                latLon.getLongitude()));
    }

    protected void updateShapeProperties(String control, Position newPosition)
    {
        if (control.equals("Center"))
        {
            // Update center position
            this.shapeCenterPosition = newPosition;
        }
        else
        {
            // Compute shape rectangle and heading
            double headingOffset = control.equals("East") ? 90
                : control.equals("South") ? 180
                    : control.equals("West") ? 270
                        : 0;
            this.shapeOrientation = LatLon.greatCircleAzimuth(this.shapeCenterPosition, newPosition)
                .subtractDegrees(headingOffset);
            // Compute distance - have a minimal distance to avoid zero sized shape
            Angle distanceAngle = LatLon.greatCircleDistance(this.shapeCenterPosition, newPosition);
            double distance = Math.max(distanceAngle.radians * getWwd().getModel().getGlobe().getRadius(), .1);
            double width, height;
            if (control.equals("East") || control.equals("West"))
            {
                width = distance * 2;
                height = this.shapeRectangle != null ? this.shapeRectangle.height : width;
                if (this.measureShapeType.equals(AVKey.SHAPE_CIRCLE) || this.measureShapeType.equals(
                    AVKey.SHAPE_SQUARE))
                    //noinspection SuspiciousNameCombination
                    height = width;
                else if (this.isActive())
                    height = width * .6;   // during shape creation
            }
            else
            {
                height = distance * 2;
                width = this.shapeRectangle != null ? this.shapeRectangle.width : height;
                if (this.measureShapeType.equals(AVKey.SHAPE_CIRCLE) || this.measureShapeType.equals(
                    AVKey.SHAPE_SQUARE))
                    //noinspection SuspiciousNameCombination
                    width = height;
                else if (this.isActive())
                    width = height * 0.6;   // during shape creation
            }
            this.shapeRectangle = new Rectangle2D.Double(0, 0, width, height);
        }
    }

    protected void updateShapeControlPoints()
    {
        if (this.shapeCenterPosition != null && this.getControlPoints().size() < 1)
        {
            // Create center control point
            addControlPoint(Position.ZERO, "Control", "Center");
        }

        if (this.shapeCenterPosition != null)
        {
            // Update center control point position
            this.getControlPoints().get(0).setPosition(new Position(this.shapeCenterPosition, 0));
        }

        if (this.shapeRectangle != null && this.getControlPoints().size() < 5)
        {
            // Add control points in four directions
            addControlPoint(Position.ZERO, "Control", "North");
            addControlPoint(Position.ZERO, "Control", "East");
            addControlPoint(Position.ZERO, "Control", "South");
            addControlPoint(Position.ZERO, "Control", "West");
        }

        if (this.shapeRectangle != null)
        {
            Angle halfWidthAngle = Angle.fromRadians(this.shapeRectangle.width / 2
                / getWwd().getModel().getGlobe().getRadius());
            Angle halfHeightAngle = Angle.fromRadians(this.shapeRectangle.height / 2
                / getWwd().getModel().getGlobe().getRadius());
            // Update control points positions in four directions
            Position controlPos;
            // North
            controlPos = new Position(LatLon.greatCircleEndPosition(
                this.shapeCenterPosition, this.shapeOrientation, halfHeightAngle), 0);
            getControlPoints().get(1).setPosition(controlPos);
            // East
            controlPos = new Position(LatLon.greatCircleEndPosition(
                this.shapeCenterPosition, this.shapeOrientation.addDegrees(90), halfWidthAngle), 0);
            getControlPoints().get(2).setPosition(controlPos);
            // South
            controlPos = new Position(LatLon.greatCircleEndPosition(
                this.shapeCenterPosition, this.shapeOrientation.addDegrees(180), halfHeightAngle), 0);
            getControlPoints().get(3).setPosition(controlPos);
            // West
            controlPos = new Position(LatLon.greatCircleEndPosition(
                this.shapeCenterPosition, this.shapeOrientation.addDegrees(270), halfWidthAngle), 0);
            getControlPoints().get(4).setPosition(controlPos);
        }
    }

    protected void updateMeasureShape()
    {
        // Update line
        if (this.measureShapeType.equals(AVKey.SHAPE_LINE) || this.measureShapeType.equals(AVKey.SHAPE_PATH))
        {
            // Update current line
            if (this.positions.size() > 1 && this.shape != null)
                ((Polyline) this.shape).setPositions(this.positions);
        }
        // Update polygon
        else if (this.measureShapeType.equals(AVKey.SHAPE_POLYGON))
        {
            if (this.shape != null)
            {
                // Update current shape
                ((SurfacePolygon) this.shape).setLocations(this.positions);
            }
        }
        // Update regular shape
        else if (this.isRegularShape())
        {
            if (this.shape != null && this.shapeRectangle != null)
            {
                // Update current shape
                if (this.measureShapeType.equals(AVKey.SHAPE_QUAD) || this.measureShapeType.equals(AVKey.SHAPE_SQUARE))
                {
                    ((SurfaceQuad) this.shape).setCenter(this.shapeCenterPosition);
                    ((SurfaceQuad) this.shape).setSize(this.shapeRectangle.width, this.shapeRectangle.height);
                    ((SurfaceQuad) this.shape).setHeading(this.shapeOrientation);
                }
                if (this.measureShapeType.equals(AVKey.SHAPE_ELLIPSE) || this.measureShapeType.equals(
                    AVKey.SHAPE_CIRCLE))
                {
                    ((SurfaceEllipse) this.shape).setCenter(this.shapeCenterPosition);
                    ((SurfaceEllipse) this.shape).setRadii(this.shapeRectangle.width / 2,
                        this.shapeRectangle.height / 2);
                    ((SurfaceEllipse) this.shape).setHeading(this.shapeOrientation);
                }
                // Update position from shape list with zero elevation
                updatePositionsFromShape();
            }
        }
    }

    protected void updatePositionsFromShape()
    {
        Globe globe = this.getWwd().getModel().getGlobe();

        this.positions.clear();

        Iterable<? extends LatLon> locations = ((SurfaceShape) this.shape).getLocations(globe);
        if (locations != null)
        {
            for (LatLon latLon : locations)
            {
                this.positions.add(new Position(latLon, 0));
            }
        }
    }

    protected void addControlPoint(Position position, String key, Object value)
    {
        ControlPoint controlPoint = createControlPoint(new Position(position, 0));
        controlPoint.setValue(key, value);
        this.getControlPoints().add(controlPoint);
    }

    ///////////// Controller ///////////////

    protected boolean isActive()
    {
        return this.active;
    }

    protected void setActive(boolean state)
    {
        this.active = state;
    }

    protected boolean isMoving()
    {
        return this.moving;
    }

    protected void setMoving(boolean state)
    {
        this.moving = state;
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    /**
     * Arms and disarms the measure tool controller. When armed, the controller monitors user input and builds the shape
     * in response to user actions. When disarmed, the controller ignores all user input.
     *
     * @param armed true to arm the controller, false to disarm it.
     */
    public void setArmed(boolean armed)
    {
        if (this.armed != armed)
        {
            this.armed = armed;
            this.firePropertyChange(WWOMeasureTool.EVENT_ARMED, !armed, armed);
        }
    }

    // Handle mouse actions
    public void mousePressed(MouseEvent mouseEvent)
    {
        if (this.isArmed() && this.useRubberBand && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            if ((mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
            {
                if (!mouseEvent.isControlDown())
                {
                    this.setActive(true);
                    this.addControlPoint();
                    if (this.getControlPoints().size() == 1)
                    {
                        this.addControlPoint(); // Simulate a second click
                    }
                    // Set the rubber band target to the last control point or the east one for regular shapes.
                    rubberBandTarget = this.getControlPoints().get(
                        this.isRegularShape() ? 2 : this.getControlPoints().size() - 1);
                    this.firePropertyChange(WWOMeasureTool.EVENT_RUBBERBAND_START, null, null);
                }
            }
            mouseEvent.consume();
        }
        else if (!this.isArmed() && mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.isAltDown())
        {
            this.setMoving(true);
            this.movingTarget = this.lastPickedObject;
            mouseEvent.consume();
        }
    }

    public void mouseReleased(MouseEvent mouseEvent)
    {
        if (this.isArmed() && this.useRubberBand && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            if (this.useRubberBand && this.getPositions().size() == 1)
                this.removeControlPoint();
            this.setActive(false);
            rubberBandTarget = null;
            // Disarm after second control point of a line or regular shape
            autoDisarm();
            mouseEvent.consume();
            this.firePropertyChange(WWOMeasureTool.EVENT_RUBBERBAND_STOP, null, null);
        }
        else if (this.isMoving() && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            this.setMoving(false);
            this.movingTarget = null;
            mouseEvent.consume();
        }
    }

    // Handle single click for removing control points
    public void mouseClicked(MouseEvent mouseEvent)
    {
        if (this.isArmed() && mouseEvent.getButton() == MouseEvent.BUTTON1)
        {
            if (mouseEvent.isControlDown())
                this.removeControlPoint();
            else if (!this.useRubberBand)
            {
                this.addControlPoint();
                // Disarm after second control point of a line or regular shape
                autoDisarm();
            }
            mouseEvent.consume();
        }
    }

    // Handle mouse motion
    public void mouseDragged(MouseEvent mouseEvent)
    {
        if (this.isArmed() && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
        {
            // Don't update the control point here because the wwd current cursor position will not
            // have been updated to reflect the current mouse position. Wait to update in the
            // position listener, but consume the event so the view doesn't respond to it.
            mouseEvent.consume();
        }
    }

    public void mouseMoved(MouseEvent mouseEvent)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
    }// Handle cursor position change for rubber band

    public void mouseExited(MouseEvent e)
    {
    }

    public void moved(PositionEvent event)
    {
        if ((!this.active && !this.moving))
            return;

        this.doMoved();
    }

    // Handle dragging of control points
    public void selected(SelectEvent event)
    {
        if ((this.isArmed() && this.useRubberBand))
            return;

        if (dragger == null)
            dragger = new BasicDragger(this.getWwd());

        if (event.getEventAction().equals(SelectEvent.ROLLOVER))
            highlight(event.getTopObject());

        this.doSelected(event);
    }

    // Wait for end of rendering to update metrics - length, area...
    public void stageChanged(RenderingEvent event) // TODO: Why?
    {
//        if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP))
//        {
//            this.firePropertyChange(GPXMeasureTool.EVENT_METRIC_CHANGED, null, null);
//        }
    }

    protected void doMoved()
    {
        if (this.active && rubberBandTarget != null && this.getWwd().getCurrentPosition() != null)
        {
            if (!this.freeHand || (!this.getMeasureShapeType().equals(AVKey.SHAPE_PATH)
                && !this.getMeasureShapeType().equals(AVKey.SHAPE_POLYGON)))
            {
                // Rubber band - Move control point and update shape
                Position lastPosition = rubberBandTarget.getPosition();
                rubberBandTarget.setPosition(new Position(this.getWwd().getCurrentPosition(), 0));
                this.moveControlPoint(rubberBandTarget);
                this.firePropertyChange(WWOMeasureTool.EVENT_POSITION_REPLACE,
                    lastPosition, rubberBandTarget.getPosition());
                this.getWwd().redraw();
            }
            else
            {
                // Free hand - Compute distance from current control point (rubber band target)
                Position lastPosition = rubberBandTarget.getPosition();
                Position newPosition = this.getWwd().getCurrentPosition();
                double distance = LatLon.greatCircleDistance(lastPosition, newPosition).radians
                    * this.getWwd().getModel().getGlobe().getRadius();
                if (distance >= freeHandMinSpacing)
                {
                    // Add new control point
                    this.addControlPoint();
                    rubberBandTarget = this.getControlPoints().get(
                        this.getControlPoints().size() - 1);
                    this.getWwd().redraw();
                }
            }
        }
        else if (this.moving && movingTarget != null && this.getWwd().getCurrentPosition() != null)
        {
            // Moving the whole shape
            Position lastPosition = movingTarget.getPosition();
            Position newPosition = this.getWwd().getCurrentPosition();
            this.moveToPosition(lastPosition, newPosition);
            //measureTool.getWwd().redraw();
        }
    }

    protected void moveToPosition(Position oldPosition, Position newPosition)
    {
        Angle distanceAngle = LatLon.greatCircleDistance(oldPosition, newPosition);
        Angle azimuthAngle = LatLon.greatCircleAzimuth(oldPosition, newPosition);
        this.moveMeasureShape(azimuthAngle, distanceAngle);
        this.firePropertyChange(WWOMeasureTool.EVENT_POSITION_REPLACE, oldPosition, newPosition);
    }

    protected EventListenerList eventListeners = new EventListenerList();

    public void addSelectListener(SelectListener listener)
    {
        this.eventListeners.add(SelectListener.class, listener);
    }

    protected void callSelectListeners(final SelectEvent event)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                for (SelectListener listener : eventListeners.getListeners(SelectListener.class))
                {
                    listener.selected(event);
                }
            }
        });
    }

    protected void doSelected(SelectEvent event)
    {
        if (event.getTopObject() != null)
        {
            if (event.getTopObject() instanceof WWOMeasureTool.ControlPoint)
            {
                WWOMeasureTool.ControlPoint point = (WWOMeasureTool.ControlPoint) event.getTopObject();
                // Check whether this control point belongs to our measure tool
                if (point.getParent() != this)
                    return;

                // Have rollover events highlight the rolled-over object.
                if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
                {
                    this.highlight(point);
                }
                // Have drag events drag the selected object.
                else if (movingTarget == null && (event.getEventAction().equals(SelectEvent.DRAG_END)
                    || event.getEventAction().equals(SelectEvent.DRAG)))
                {
                    this.dragSelected(event);
                }
            }
            else if (measureDisplay.isAnnotation(event.getTopObject()))
            {
                Position pos = null;
                if (event.getObjects().getTerrainObject() != null)
                    pos = event.getObjects().getTerrainObject().getPosition();

                if (isShowAnnotation())
                    measureDisplay.updateMeasureDisplay(pos);
            }
            else if (event.getTopObject() == shape)
            {
                for (SelectListener listener : eventListeners.getListeners(SelectListener.class))
                {
                    listener.selected(event);
                }

                Position pos = null;
                if (event.getObjects().getTerrainObject() != null)
                    pos = event.getObjects().getTerrainObject().getPosition();

                if (isShowAnnotation())
                    measureDisplay.updateMeasureDisplay(pos);
            }
            else
            {
                if (isShowAnnotation())
                    measureDisplay.updateMeasureDisplay(null);
            }
        }
    }

    protected void dragSelected(SelectEvent event)
    {
        WWOMeasureTool.ControlPoint point = (WWOMeasureTool.ControlPoint) event.getTopObject();

        LatLon lastPosition = point.getPosition();
        if (point.getValue("PositionIndex") != null)
            lastPosition = this.getPositions().get((Integer) point.getValue("PositionIndex"));

        // Delegate dragging computations to a dragger.
        this.dragger.selected(event);

        this.moveControlPoint(point);
        if (this.isShowAnnotation())
            this.measureDisplay.updateMeasureDisplay(point.getPosition());
        this.firePropertyChange(WWOMeasureTool.EVENT_POSITION_REPLACE,
            lastPosition, point.getPosition());
        this.getWwd().redraw();
    }

    protected void highlight(Object o)
    {
        // Manage highlighting of control points
        if (this.lastPickedObject == o)
            return; // Same thing selected

        if (o != null && o instanceof WWOMeasureTool.ControlPoint
            && ((WWOMeasureTool.ControlPoint) o).getParent() != this)
            return; // Does not belong to this measure tool

        // Turn off highlight if on.
        if (this.lastPickedObject != null)
        {
            this.lastPickedObject.highlight(false);
            this.lastPickedObject = null;
            if (this.isShowAnnotation())
                this.measureDisplay.updateMeasureDisplay(null);
        }

        // Turn on highlight if object selected.
        if (o != null && o instanceof WWOMeasureTool.ControlPoint)
        {
            this.lastPickedObject = (WWOMeasureTool.ControlPoint) o;
            this.lastPickedObject.highlight(true);
            if (this.isShowAnnotation())
                this.measureDisplay.updateMeasureDisplay(this.lastPickedObject.getPosition());
        }
    }

    protected void autoDisarm()
    {
        // Disarm after second control point of a line or regular shape
        if (this.isRegularShape() || this.getMeasureShapeType().equals(AVKey.SHAPE_LINE))
            if (this.getControlPoints().size() > 1)
                this.setArmed(false);
    }
}
