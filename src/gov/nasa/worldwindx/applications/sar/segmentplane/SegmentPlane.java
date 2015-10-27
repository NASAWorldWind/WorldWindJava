/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.segmentplane;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: SegmentPlane.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SegmentPlane extends WWObjectImpl
{
    public static class ControlPoint
    {
        private Object owner;
        private Object key;
        private double uCoordinate;
        private double vCoordinate;
        private boolean relativeToSurface;
        private String shapeType;

        public ControlPoint(Object owner, Object key, double uCoordinate, double vCoordinate,
            boolean relativeToSurface, String shapeType)
        {
            if (owner == null)
            {
                String message = Logging.getMessage("nullValue.OwnerIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (key == null)
            {
                String message = Logging.getMessage("nullValue.KeyIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.owner = owner;
            this.key = key;
            this.uCoordinate = uCoordinate;
            this.vCoordinate = vCoordinate;
            this.relativeToSurface = relativeToSurface;
            this.shapeType = shapeType;
        }

        public Object getOwner()
        {
            return this.owner;
        }

        public Object getKey()
        {
            return this.key;
        }

        public double[] getCoordinates()
        {
            return new double[] {this.uCoordinate, this.vCoordinate};
        }

        public boolean isRelativeToSurface()
        {
            return this.relativeToSurface;
        }

        public String getShapeType()
        {
            return this.shapeType;
        }
    }

    protected static class StateKey
    {
        private final SegmentPlane segmentPlane;
        private final long serialNumber;

        public StateKey(SegmentPlane segmentPlane, long serialNumber)
        {
            this.segmentPlane = segmentPlane;
            this.serialNumber = serialNumber;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            StateKey that = (StateKey) o;
            return this.segmentPlane.equals(that.segmentPlane) && (this.serialNumber == that.serialNumber);
        }

        public int hashCode()
        {
            int result = this.segmentPlane != null ? this.segmentPlane.hashCode() : 0;
            result = 31 * result + (int) (this.serialNumber ^ (this.serialNumber >>> 32));
            return result;
        }
    }

    public static final String ALTIMETER = "SegmentPlane.Altimeter";
    public static final String CONTROL_POINT_LOWER_LEFT = "SegmentPlane.ControlPointLowerLeft";
    public static final String CONTROL_POINT_LOWER_RIGHT = "SegmentPlane.ControlPointLowerRight";
    public static final String CONTROL_POINT_UPPER_RIGHT = "SegmentPlane.ControlPointUpperRight";
    public static final String CONTROL_POINT_TOP_EDGE = "SegmentPlane.ControlPointTopEdge";
    public static final String CONTROL_POINT_LEADING_EDGE = "SegmentPlane.ControlPointLeadingEdge";
    public static final String HORIZONTAL_AXIS_LABELS = "SegmentPlane.HorizontalAxisLabels";
    public static final String PLANE_ALTITUDES = "SegmentPlane.PlaneAltitudes";
    public static final String PLANE_BACKGROUND = "SegmentPlane.PlaneBackground";
    public static final String PLANE_BORDER = "SegmentPlane.PlaneBorder";
    public static final String PLANE_GRID = "SegmentPlane.PlaneGrid";
    public static final String PLANE_GRID_DIMENSIONS = "SegmentPlane.PlaneGridDimensions";
    public static final String PLANE_LOCATIONS = "SegmentPlane.PlaneLocations";
    public static final String PLANE_OUTLINE = "SegmentPlane.PlaneOutline";
    public static final String SEGMENT_BEGIN = "SegmentPlane.SegmentBegin";
    public static final String SEGMENT_END = "SegmentPlane.SegmentEnd";
    public static final String VERTICAL_AXIS_LABELS = "SegmentPlane.VerticalAxisLabels";

    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;

    private boolean visible;
    private SegmentPlaneAttributes attributes;
    private double planeLowerAltitude;
    private double planeUpperAltitude;
    private LatLon planeLocation1;
    private LatLon planeLocation2;
    private double gridCellWidth;
    private double gridCellHeight;
    private int planeOutlineMask;
    private int borderMask;
    private Position segmentBeginPosition;
    private Position segmentEndPosition;
    private List<ControlPoint> controlPointList;
    protected long serialNumber = 1;

    public SegmentPlane()
    {
        this.visible = true;
        this.attributes = new SegmentPlaneAttributes();
        this.planeLowerAltitude = 0.0;
        this.planeUpperAltitude = 0.0;
        this.planeLocation1 = LatLon.ZERO;
        this.planeLocation2 = LatLon.ZERO;
        this.gridCellWidth = 1.0;
        this.gridCellHeight = 1.0;
        this.planeOutlineMask = TOP | BOTTOM | LEFT | RIGHT;
        this.borderMask = TOP | BOTTOM | LEFT | RIGHT;
        this.segmentBeginPosition = Position.ZERO;
        this.segmentEndPosition = Position.ZERO;
        this.controlPointList = new ArrayList<ControlPoint>();

        this.addDefaultAttributes(PLANE_BACKGROUND);
        this.addDefaultAttributes(PLANE_GRID);
        this.addDefaultAttributes(PLANE_OUTLINE);
        this.addDefaultAttributes(PLANE_BORDER);
        this.addDefaultAttributes(SEGMENT_BEGIN);
        this.addDefaultAttributes(SEGMENT_END);
        this.addDefaultAttributes(ALTIMETER);

        this.addDefaultControlPoint(CONTROL_POINT_LOWER_RIGHT,  1.0, 0.0, true, BasicMarkerShape.SPHERE);
        this.addDefaultControlPoint(CONTROL_POINT_UPPER_RIGHT,  1.0, 1.0, false, BasicMarkerShape.SPHERE);
        this.addDefaultControlPoint(CONTROL_POINT_TOP_EDGE,     0.5, 1.0, false, BasicMarkerShape.SPHERE);
        this.addDefaultControlPoint(CONTROL_POINT_LEADING_EDGE, 1.0, 0.5, true, BasicMarkerShape.SPHERE);
    }

    protected void addDefaultAttributes(Object key)
    {
        this.getAttributes().setGeometryAttributes(key, new SegmentPlaneAttributes.GeometryAttributes());
        this.getAttributes().setLabelAttributes(key, new SegmentPlaneAttributes.LabelAttributes());
    }

    protected void addDefaultControlPoint(Object key, double u, double v, boolean relativeToSurface, String shapeType)
    {
        this.addControlPoint(new ControlPoint(this, key, u, v, relativeToSurface, shapeType));
        this.addDefaultAttributes(key);
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public SegmentPlaneAttributes getAttributes()
    {
        return attributes;
    }

    public void setAttributes(SegmentPlaneAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attributes = attributes;
    }

    public double[] getPlaneAltitudes()
    {
        return new double[] {this.planeLowerAltitude, this.planeUpperAltitude};
    }

    /**
     * Set the upper and lower altitude limits.
     *
     * @param lowerAltitude the lower altitude limit, in meters relative to mean sea level
     * @param upperAltitude the upper altitude limit, in meters relative to mean sea level
     */
    public void setPlaneAltitudes(double lowerAltitude, double upperAltitude)
    {
        double[] oldAltitudes = this.getPlaneAltitudes();

        this.planeLowerAltitude = lowerAltitude;
        this.planeUpperAltitude = upperAltitude;
        this.setStateExpired();

        this.firePropertyChange(PLANE_ALTITUDES, oldAltitudes, this.getPlaneAltitudes());
    }

    public LatLon[] getPlaneLocations()
    {
        return new LatLon[] {this.planeLocation1, this.planeLocation2};
    }

    public void setPlaneLocations(LatLon location1, LatLon location2)
    {
        if (location1 == null)
        {
            String message = "nullValue.Location1IsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (location2 == null)
        {
            String message = "nullValue.Location2IsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon[] oldLocations = this.getPlaneLocations();

        this.planeLocation1 = location1;
        this.planeLocation2 = location2;
        this.setStateExpired();

        this.firePropertyChange(PLANE_LOCATIONS, oldLocations, this.getPlaneLocations());
    }

    public double[] getGridCellDimensions()
    {
        return new double[] {this.gridCellWidth, this.gridCellHeight};
    }

    public void setGridCellDimensions(double width, double height)
    {
        if (width <= 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (height <= 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "height <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] oldGridDimensions = this.getGridCellDimensions();

        this.gridCellWidth = width;
        this.gridCellHeight = height;
        this.setStateExpired();

        this.firePropertyChange(PLANE_GRID_DIMENSIONS, oldGridDimensions, this.getGridCellDimensions());
    }

    public int getPlaneOutlineMask()
    {
        return this.planeOutlineMask;
    }

    public void setPlaneOutlineMask(int mask)
    {
        this.planeOutlineMask = mask;
        this.setStateExpired();
    }

    public int getBorderMask()
    {
        return this.borderMask;
    }

    public void setBorderMask(int mask)
    {
        this.borderMask = mask;
        this.setStateExpired();
    }

    public Position[] getSegmentPositions()
    {
        return new Position[] {this.segmentBeginPosition, this.segmentEndPosition};
    }

    public void setSegmentPositions(Position position1, Position position2)
    {
        if (position1 == null)
        {
            String message = "nullValue.Position1IsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (position2 == null)
        {
            String message = "nullValue.Position2IsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setSegmentBeginPosition(position1);
        this.setSegmentEndPosition(position2);
    }

    public void setSegmentBeginPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position oldPosition = this.segmentBeginPosition;
        this.segmentBeginPosition = position;

        this.firePropertyChange(SEGMENT_BEGIN, oldPosition, this.segmentBeginPosition);
    }

    public void setSegmentEndPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position oldPosition = this.segmentEndPosition;
        this.segmentEndPosition = position;

        this.firePropertyChange(SEGMENT_END, oldPosition, this.segmentEndPosition);
    }

    public List<ControlPoint> getControlPoints()
    {
        return Collections.unmodifiableList(this.controlPointList);
    }

    public void setControlPoints(Iterable<? extends ControlPoint> controlPoints)
    {
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.controlPointList.clear();
        for (ControlPoint p : controlPoints)
        {
            this.addControlPoint(p);
        }
        this.setStateExpired();
    }

    protected void addControlPoint(ControlPoint controlPoint)
    {
        if (controlPoint.getOwner() != this)
        {
            String message = Logging.getMessage("generic.OwnerIsInvalid", controlPoint.getOwner());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.controlPointList.add(controlPoint);
    }

    public Object getStateKey()
    {
        return new StateKey(this, this.serialNumber);
    }

    public Plane computeInfinitePlane(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BilinearInterpolator interp = this.createPlaneInterpolator(globe);
        Vec4[] corners = interp.getCorners();

        Vec4 a = corners[1].subtract3(corners[0]);
        Vec4 b = corners[3].subtract3(corners[0]);
        Vec4 n = a.cross3(b).normalize3();
        double d = -corners[0].dot3(n);

        if (n.equals(Vec4.ZERO))
        {
            return null;
        }

        return new Plane(n.x, n.y, n.z, d);
    }

    public BilinearInterpolator createPlaneInterpolator(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] altitudes = this.getPlaneAltitudes();
        LatLon[] locations = this.getPlaneLocations();

        Vec4 ll = globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(), altitudes[0]);
        Vec4 lr = globe.computePointFromPosition(locations[1].getLatitude(), locations[1].getLongitude(), altitudes[0]);
        Vec4 ur = globe.computePointFromPosition(locations[1].getLatitude(), locations[1].getLongitude(), altitudes[1]);
        Vec4 ul = globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(), altitudes[1]);

        return new BilinearInterpolator(ll, lr, ur, ul);
    }

    protected void setStateExpired()
    {
        this.serialNumber++;
    }
}
