/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.*;
import gov.nasa.worldwindx.applications.sar.segmentplane.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;

/**
 * @author dcollins
 * @version $Id: SARSegmentPlane.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARSegmentPlane extends WWObjectImpl
{
    private String angleFormat;
    private String elevationUnit;
    private WorldWindow wwd; // Can be null.
    // Segment plane components.
    private SegmentPlane segmentPlane;
    private SegmentPlaneEditor segmentPlaneEditor;
    private SegmentPlaneController segmentPlaneController;
    private boolean modifiedSinceLastArm = false;
    private boolean ignorePlaneChangeEvents = false;

    public SARSegmentPlane()
    {
        this.segmentPlane = new SegmentPlane();
        this.segmentPlaneEditor = new SegmentPlaneEditor();
        this.segmentPlaneController = new SegmentPlaneController(null);
        this.segmentPlane.setVisible(false);
        this.segmentPlane.addPropertyChangeListener(this);
        this.segmentPlaneEditor.setSegmentPlane(this.segmentPlane);
        this.segmentPlaneController.setEditor(this.segmentPlaneEditor);
        this.initSegmentPlane();
    }

    public boolean isVisible()
    {
        return this.segmentPlane.isVisible();
    }

    public void setVisible(boolean visible)
    {
        this.segmentPlane.setVisible(visible);
    }

    public boolean isArmed()
    {
        return this.segmentPlaneEditor.isArmed();
    }

    public void setArmed(boolean armed)
    {
        if (armed && !(this.segmentPlaneEditor.isArmed()))
        {
            this.modifiedSinceLastArm = false;
        }

        this.segmentPlaneEditor.setArmed(armed);
    }

    public boolean isSnapToGrid()
    {
        return this.segmentPlaneEditor.isSnapToGrid();
    }

    public void setSnapToGrid(boolean snapToGrid)
    {
        this.segmentPlaneEditor.setSnapToGrid(snapToGrid);
    }

    public double[] getGridCellDimensions()
    {
        return this.segmentPlane.getGridCellDimensions();
    }

    public void setGridCellDimensions(double width, double height)
    {
        this.segmentPlane.setGridCellDimensions(width, height);
    }

    public String getAngleFormat()
    {
        return this.angleFormat;
    }

    public void setAngleFormat(String angleFormat)
    {
        this.angleFormat = angleFormat;
    }

    public String getElevationUnit()
    {
        return this.elevationUnit;
    }

    public void setElevationUnit(String elevationUnit)
    {
        this.elevationUnit = elevationUnit;
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
            this.wwd.removePropertyChangeListener(this);

            if (this.wwd.getModel().getLayers().contains(this.segmentPlaneEditor))
            {
                this.wwd.getModel().getLayers().remove(this.segmentPlaneEditor);
            }
        }

        this.wwd = wwd;
        this.segmentPlaneController.setWorldWindow(wwd);

        if (this.wwd != null)
        {
            this.wwd.addPropertyChangeListener(this);

            if (!this.wwd.getModel().getLayers().contains(this.segmentPlaneEditor))
            {
                this.wwd.getModel().getLayers().add(this.segmentPlaneEditor);
            }
        }
    }

    @SuppressWarnings({"StringEquality"})
    public void propertyChange(PropertyChangeEvent e)
    {
        String propertyName = e.getPropertyName();

        if (e.getSource() == this.segmentPlane)
        {
            this.modifiedSinceLastArm = true;
        }

        if (propertyName == SegmentPlane.SEGMENT_BEGIN || propertyName == SegmentPlane.SEGMENT_END)
        {
            if (!this.ignorePlaneChangeEvents)
            {
                super.propertyChange(e);
            }
        }
        else if (propertyName == SARKey.ANGLE_FORMAT)
        {
            if (e.getNewValue() != null)
            {
                this.setAngleFormat(e.getNewValue().toString());
                super.propertyChange(e);
            }
        }
        else if (propertyName == SARKey.ELEVATION_UNIT)
        {
            if (e.getNewValue() != null)
            {
                this.setElevationUnit(e.getNewValue().toString());
                super.propertyChange(e);
            }
        }
    }

    public Position[] getSegmentPositions()
    {
        return this.segmentPlane.getSegmentPositions();
    }

    public void setSegmentPositions(Position position1, Position position2)
    {
        this.ignorePlaneChangeEvents = true;
        try
        {
            this.segmentPlane.setSegmentPositions(position1, position2);
        }
        finally
        {
            this.ignorePlaneChangeEvents = false;
        }
    }

    public double[] getPlaneAltitudes()
    {
        return this.segmentPlane.getPlaneAltitudes();
    }

    public void setPlaneAltitudes(double lowerAltitude, double upperAltitude)
    {
        this.ignorePlaneChangeEvents = true;
        try
        {
            this.segmentPlane.setPlaneAltitudes(lowerAltitude, upperAltitude);
        }
        finally
        {
            this.ignorePlaneChangeEvents = false;
        }
    }

    public LatLon[] getPlaneLocations()
    {
        return this.segmentPlane.getPlaneLocations();
    }

    public void setPlaneLocations(LatLon location1, LatLon location2)
    {
        this.ignorePlaneChangeEvents = true;
        try
        {
            this.segmentPlane.setPlaneLocations(location1, location2);
        }
        finally
        {
            this.ignorePlaneChangeEvents = false;
        }
    }

    public SegmentPlaneAttributes getAttributes()
    {
        return this.segmentPlane.getAttributes();
    }

    public void setAttributes(SegmentPlaneAttributes attributes)
    {
        this.segmentPlane.setAttributes(attributes);
    }

    public void setObjectVisible(String key, boolean geometryVisible, boolean labelVisible)
    {
        SegmentPlaneAttributes.GeometryAttributes geometryAttributes =
            this.segmentPlane.getAttributes().getGeometryAttributes(key);
        if (geometryAttributes != null)
        {
            geometryAttributes.setVisible(geometryVisible);
        }

        SegmentPlaneAttributes.LabelAttributes labelAttributes =
            this.segmentPlane.getAttributes().getLabelAttributes(key);
        if (labelAttributes != null)
        {
            labelAttributes.setVisible(labelVisible);
        }
    }

    public double[] computeAltitudesToFitPositions(Iterable<? extends Position> positions)
    {
        if (this.wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return computeAltitudesToFitPositions(this.wwd, this.segmentPlane, positions, this.modifiedSinceLastArm);
    }

    public LatLon[] computeLocationsToFitPositions(Position position1, Position position2)
    {
        if (this.wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return computeLocationsToFitPositions(this.wwd, this.segmentPlane, position1, position2,
            this.modifiedSinceLastArm);
    }

    public Position getIntersectionPosition(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Globe globe = this.wwd.getModel().getGlobe();

        Vec4 point = this.segmentPlaneEditor.getSegmentPlaneRenderer().intersect(globe, line, this.segmentPlane);
        if (point == null)
        {
            return null;
        }

        return globe.computePositionFromPoint(point);
    }

    public double getObjectSize(String key, Vec4 point)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        View view = this.wwd.getView();
        Globe globe = this.wwd.getModel().getGlobe();

        return this.segmentPlaneEditor.getSegmentPlaneRenderer().computeObjectSize(view, globe, this.segmentPlane,
            key, point);
    }

    //**************************************************************//
    //********************  Segment Plane initialization  **********//
    //**************************************************************//

    protected void initSegmentPlane()
    {
        double gridSize = SAR2.feetToMeters(1000);
        this.segmentPlane.setGridCellDimensions(gridSize, gridSize);
        this.segmentPlane.setPlaneOutlineMask(SegmentPlane.TOP);
        this.segmentPlane.setBorderMask(SegmentPlane.LEFT);

        Color foregroundColor = new Color(90, 146, 200);
        Color backgroundColor = new Color(163, 191, 222);
        Color segmentPointColor = Color.YELLOW;
        Color hAxisLabelColor = Color.RED;
        Color vAxisLabelColor = Color.YELLOW;
        Color moveControlPointColor = Color.GREEN;
        Color resizeControlPointColor = Color.MAGENTA;
        double maxAxisLabelActiveDistance = Double.MAX_VALUE;
        double maxSecondaryControlPointActiveDistance = Double.MAX_VALUE;
        double maxPrimaryControlPointActiveDistance = Double.MAX_VALUE;

        SegmentPlaneAttributes attributes = new SegmentPlaneAttributes();
        this.segmentPlane.setAttributes(attributes);

        //**************************************************************//
        //********************  Plane Attributes  **********************//
        //**************************************************************//

        SegmentPlaneAttributes.GeometryAttributes background = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(backgroundColor), 0.6);
        SegmentPlaneAttributes.GeometryAttributes outline = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(foregroundColor), 1.0);
        SegmentPlaneAttributes.GeometryAttributes grid = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(foregroundColor), 1.0);
        SegmentPlaneAttributes.GeometryAttributes border = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(foregroundColor), 1.0);
        grid.setSize(1);
        grid.setPickSize(10);
        outline.setSize(3);
        border.setSize(3);

        attributes.setGeometryAttributes(SegmentPlane.PLANE_BACKGROUND, background);
        attributes.setGeometryAttributes(SegmentPlane.PLANE_OUTLINE, outline);
        attributes.setGeometryAttributes(SegmentPlane.PLANE_GRID, grid);
        attributes.setGeometryAttributes(SegmentPlane.PLANE_BORDER, border);

        //**************************************************************//
        //********************  Segment Altimeter Attributes  **********//
        //**************************************************************//

        SegmentPlaneAttributes.GeometryAttributes altimeterGeometry = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(segmentPointColor), 1.0);
        AxisLabelAttributes altimeterLabel = new AxisLabelAttributes(
            vAxisLabelColor, Font.decode("Arial-12"), AVKey.LEFT, AVKey.CENTER, this);
        altimeterGeometry.setSize(1);
        altimeterLabel.setVisible(false);
        altimeterLabel.setMaxActiveDistance(maxAxisLabelActiveDistance);

        attributes.setGeometryAttributes(SegmentPlane.ALTIMETER, altimeterGeometry);
        attributes.setLabelAttributes(SegmentPlane.ALTIMETER, altimeterLabel);

        //**************************************************************//
        //********************  Segment Control Point Attributes  ******//
        //**************************************************************//

        SegmentPlaneAttributes.GeometryAttributes segmentBeginPointGeom = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(segmentPointColor), 1.0);
        SegmentPlaneAttributes.GeometryAttributes segmentEndPointGeom = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(segmentPointColor), 1.0);
        ControlPointLabelAttributes segmentBeginPointLabel = new ControlPointLabelAttributes(
            segmentPointColor, Font.decode("Arial-12"), AVKey.RIGHT, AVKey.CENTER, this);
        ControlPointLabelAttributes segmentEndPointLabel = new ControlPointLabelAttributes(
            segmentPointColor, Font.decode("Arial-12"), AVKey.RIGHT, AVKey.CENTER, this);
        segmentBeginPointGeom.setEnablePicking(false);
        segmentBeginPointGeom.setSize(8);
        segmentBeginPointGeom.setPickSize(10);
        segmentEndPointGeom.setSize(8);
        segmentEndPointGeom.setPickSize(10);
        segmentBeginPointLabel.setMaxActiveDistance(maxSecondaryControlPointActiveDistance);
        segmentBeginPointLabel.setShowAltitude(true);
        segmentBeginPointLabel.setShowHeightAboveSurface(true);
        segmentBeginPointLabel.setOffset(new Vec4(-10, 0, 0));
        segmentEndPointLabel.setPrefix("Aircraft");
        segmentEndPointLabel.setMaxActiveDistance(maxPrimaryControlPointActiveDistance);
        segmentEndPointLabel.setShowAltitude(true);
        segmentEndPointLabel.setShowHeightAboveSurface(true);
        segmentEndPointLabel.setOffset(new Vec4(-10, 0, 0));

        attributes.setGeometryAttributes(SegmentPlane.SEGMENT_BEGIN, segmentBeginPointGeom.copy());
        attributes.setGeometryAttributes(SegmentPlane.SEGMENT_END, segmentEndPointGeom.copy());
        attributes.setLabelAttributes(SegmentPlane.SEGMENT_BEGIN, segmentBeginPointLabel);
        attributes.setLabelAttributes(SegmentPlane.SEGMENT_END, segmentEndPointLabel);

        //**************************************************************//
        //********************  Axis Label Attributes  *****************//
        //**************************************************************//

        AxisLabelAttributes horizontalAxisLabels = new AxisLabelAttributes(
            hAxisLabelColor, Font.decode("Arial-10"), AVKey.CENTER, AVKey.BOTTOM, this);
        AltitudeLabelAttributes verticalAxisLabels = new AltitudeLabelAttributes(
            vAxisLabelColor, Font.decode("Arial-10"), AVKey.RIGHT, AVKey.BOTTOM, this);
        horizontalAxisLabels.setMaxActiveDistance(maxAxisLabelActiveDistance);
        verticalAxisLabels.setMaxActiveDistance(maxAxisLabelActiveDistance);

        attributes.setLabelAttributes(SegmentPlane.HORIZONTAL_AXIS_LABELS, horizontalAxisLabels);
        attributes.setLabelAttributes(SegmentPlane.VERTICAL_AXIS_LABELS, verticalAxisLabels);

        //**************************************************************//
        //********************  Plane Move Control Point Attributes  ***//
        //**************************************************************//

        SegmentPlaneAttributes.GeometryAttributes moveControlPointLRGeom =
            new SegmentPlaneAttributes.GeometryAttributes(new Material(moveControlPointColor), 1.0);
        SegmentPlaneAttributes.GeometryAttributes moveControlPointURGeom =
            new SegmentPlaneAttributes.GeometryAttributes(new Material(moveControlPointColor), 1.0);
        ControlPointLabelAttributes moveControlPointLRLabel = new ControlPointLabelAttributes(
            moveControlPointColor, Font.decode("Arial-12"), AVKey.LEFT, AVKey.CENTER, this);
        ControlPointLabelAttributes moveControlPointURLabel = new ControlPointLabelAttributes(
            moveControlPointColor, Font.decode("Arial-12"), AVKey.LEFT, AVKey.CENTER, this);
        moveControlPointLRGeom.setSize(7);
        moveControlPointLRGeom.setPickSize(10);
        moveControlPointLRGeom.setOffset(new Vec4(0, 0, 7));
        moveControlPointURGeom.setSize(7);
        moveControlPointURGeom.setPickSize(10);
        moveControlPointLRLabel.setVisible(false);
        moveControlPointLRLabel.setMaxActiveDistance(maxSecondaryControlPointActiveDistance);
        moveControlPointURLabel.setShowLocation(false);
        moveControlPointURLabel.setShowSegmentHeading(true);
        moveControlPointURLabel.setMaxActiveDistance(maxSecondaryControlPointActiveDistance);
        moveControlPointURLabel.setOffset(new Vec4(15, 0, 0));

        attributes.setGeometryAttributes(SegmentPlane.CONTROL_POINT_LOWER_RIGHT, moveControlPointLRGeom.copy());
        attributes.setGeometryAttributes(SegmentPlane.CONTROL_POINT_UPPER_RIGHT, moveControlPointURGeom.copy());
        attributes.setLabelAttributes(SegmentPlane.CONTROL_POINT_LOWER_RIGHT, moveControlPointLRLabel);
        attributes.setLabelAttributes(SegmentPlane.CONTROL_POINT_UPPER_RIGHT, moveControlPointURLabel);

        //**************************************************************//
        //********************  Plane Resize Control Point Attributes  *//
        //**************************************************************//

        SegmentPlaneAttributes.GeometryAttributes resizeControlPointGeom
            = new SegmentPlaneAttributes.GeometryAttributes(
            new Material(resizeControlPointColor), 1.0);
        ControlPointLabelAttributes resizeControlPointLabel = new ControlPointLabelAttributes(
            resizeControlPointColor, Font.decode("Arial-10"), AVKey.LEFT, AVKey.CENTER, this);
        resizeControlPointGeom.setSize(7);
        resizeControlPointGeom.setPickSize(10);
        resizeControlPointLabel.setVisible(false);
        resizeControlPointLabel.setMaxActiveDistance(maxSecondaryControlPointActiveDistance);

        attributes.setGeometryAttributes(SegmentPlane.CONTROL_POINT_LEADING_EDGE, resizeControlPointGeom.copy());
        attributes.setGeometryAttributes(SegmentPlane.CONTROL_POINT_TOP_EDGE, resizeControlPointGeom.copy());
        attributes.setLabelAttributes(SegmentPlane.CONTROL_POINT_LEADING_EDGE, resizeControlPointLabel.copy());
        attributes.setLabelAttributes(SegmentPlane.CONTROL_POINT_TOP_EDGE, resizeControlPointLabel.copy());
    }

    //**************************************************************//
    //********************  Control Point Label Attributes  ********//
    //**************************************************************//

    public static class SARLabelAttributes extends SegmentPlaneAttributes.LabelAttributes
    {
        private SARSegmentPlane context;

        public SARLabelAttributes(Color color, Font font, String horizontalAlignment, String verticalAlignment,
            SARSegmentPlane context)
        {
            super(color, font, horizontalAlignment, verticalAlignment);
            this.context = context;
        }

        public SARLabelAttributes()
        {
        }

        public SARSegmentPlane getContext()
        {
            return this.context;
        }

        public void setContext(SARSegmentPlane context)
        {
            this.context = context;
        }

        public SegmentPlaneAttributes.LabelAttributes copy()
        {
            return this.copyTo(new SARLabelAttributes());
        }

        protected SegmentPlaneAttributes.LabelAttributes copyTo(SegmentPlaneAttributes.LabelAttributes copy)
        {
            super.copyTo(copy);

            if (copy instanceof SARLabelAttributes)
            {
                ((SARLabelAttributes) copy).setContext(this.getContext());
            }

            return copy;
        }

        protected String formatAngle(Angle angle)
        {
            return SARSegmentPlane.formatAngle(this.context.getAngleFormat(), angle);
        }

        protected String formatElevation(double value)
        {
            return SARSegmentPlane.formatElevation(this.context.getElevationUnit(), value);
        }
    }

    public static class ControlPointLabelAttributes extends SARLabelAttributes
    {
        private String prefix;
        private boolean showLocation = true;
        private boolean showAltitude = false;
        private boolean showHeightAboveSurface = false;
        private boolean showSegmentHeading = false;

        public ControlPointLabelAttributes(Color color, Font font, String horizontalAlignment,
            String verticalAlignment, SARSegmentPlane context)
        {
            super(color, font, horizontalAlignment, verticalAlignment, context);
        }

        public ControlPointLabelAttributes()
        {
        }

        public String getPrefix()
        {
            return this.prefix;
        }

        public void setPrefix(String prefix)
        {
            this.prefix = prefix;
        }

        public boolean isShowLocation()
        {
            return this.showLocation;
        }

        public void setShowLocation(boolean showLocation)
        {
            this.showLocation = showLocation;
        }

        public boolean isShowAltitude()
        {
            return this.showAltitude;
        }

        public void setShowAltitude(boolean show)
        {
            this.showAltitude = show;
        }

        public boolean isShowSegmentHeading()
        {
            return this.showSegmentHeading;
        }

        public void setShowSegmentHeading(boolean show)
        {
            this.showSegmentHeading = show;
        }

        public boolean isShowHeightAboveSurface()
        {
            return this.showHeightAboveSurface;
        }

        public void setShowHeightAboveSurface(boolean show)
        {
            this.showHeightAboveSurface = show;
        }

        public SegmentPlaneAttributes.LabelAttributes copy()
        {
            return this.copyTo(new ControlPointLabelAttributes());
        }

        protected SegmentPlaneAttributes.LabelAttributes copyTo(SegmentPlaneAttributes.LabelAttributes copy)
        {
            super.copyTo(copy);

            if (copy instanceof ControlPointLabelAttributes)
            {
                ((ControlPointLabelAttributes) copy).setPrefix(this.getPrefix());
                ((ControlPointLabelAttributes) copy).setShowLocation(this.isShowLocation());
                ((ControlPointLabelAttributes) copy).setShowAltitude(this.isShowAltitude());
                ((ControlPointLabelAttributes) copy).setShowHeightAboveSurface(this.isShowHeightAboveSurface());
                ((ControlPointLabelAttributes) copy).setShowSegmentHeading(this.isShowSegmentHeading());
            }

            return copy;
        }

        public String getText(SegmentPlane segmentPlane, Position position, AVList values)
        {
            StringBuilder sb = new StringBuilder();

            if (this.getPrefix() != null)
            {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(this.getPrefix());
            }

            if (this.isShowLocation())
            {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append("(");
                sb.append(this.formatAngle(position.getLatitude()));
                sb.append(", ");
                sb.append(this.formatAngle(position.getLongitude()));
                sb.append(")");
            }

            if (this.isShowSegmentHeading())
            {
                LatLon[] locations = segmentPlane.getPlaneLocations();
                Angle heading = LatLon.rhumbAzimuth(locations[0], locations[1]);

                if (sb.length() > 0)
                    sb.append("\n");
                sb.append("Heading: ").append(heading.toDecimalDegreesString(0));
            }

            if (this.isShowAltitude())
            {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append("Alt: ").append(this.formatElevation(position.getElevation()));
            }

            if (this.isShowHeightAboveSurface())
            {
                if (values != null)
                {
                    Double height = AVListImpl.getDoubleValue(values, AVKey.HEIGHT);
                    if (height != null)
                    {
                        if (sb.length() > 0)
                            sb.append("\n");
                        sb.append("AGL: ").append(this.formatElevation(height));
                    }
                }
            }

            return sb.toString();
        }
    }

    public static class AltitudeLabelAttributes extends SARLabelAttributes
    {
        public AltitudeLabelAttributes(Color color, Font font, String horizontalAlignment, String verticalAlignment,
            SARSegmentPlane context)
        {
            super(color, font, horizontalAlignment, verticalAlignment, context);
        }

        public AltitudeLabelAttributes()
        {
        }

        public SegmentPlaneAttributes.LabelAttributes copy()
        {
            return this.copyTo(new AltitudeLabelAttributes());
        }

        public String getText(SegmentPlane segmentPlane, Position position, AVList values)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(this.formatElevation(position.getElevation()));

            return sb.toString();
        }
    }

    public static class AxisLabelAttributes extends SARLabelAttributes
    {
        public AxisLabelAttributes(Color color, Font font, String horizontalAlignment,
            String verticalAlignment, SARSegmentPlane context)
        {
            super(color, font, horizontalAlignment, verticalAlignment, context);
        }

        public AxisLabelAttributes()
        {
        }

        public SegmentPlaneAttributes.LabelAttributes copy()
        {
            return this.copyTo(new AxisLabelAttributes());
        }

        public String getText(SegmentPlane segmentPlane, Position position, AVList values)
        {
            StringBuilder sb = new StringBuilder();

            if (values != null)
            {
                Double width = AVListImpl.getDoubleValue(values, AVKey.WIDTH);
                Double height = AVListImpl.getDoubleValue(values, AVKey.HEIGHT);
                boolean haveTuple = (width != null && height != null);

                if (haveTuple)
                    sb.append("(");

                if (width != null)
                    sb.append(this.formatElevation(width));

                if (haveTuple)
                    sb.append(", ");

                if (height != null)
                    sb.append(this.formatElevation(height));

                if (haveTuple)
                    sb.append(")");
            }

            if (sb.length() == 0)
                return null;

            return sb.toString();
        }
    }

    public static class MessageLabelAttributes extends SegmentPlaneAttributes.LabelAttributes
    {
        private String message;

        public MessageLabelAttributes(Color color, Font font, String horizontalAlignment, String verticalAlignment,
            String message)
        {
            super(color, font, horizontalAlignment, verticalAlignment);
            this.message = message;
        }

        public MessageLabelAttributes()
        {
        }

        public String getMessage()
        {
            return this.message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public SegmentPlaneAttributes.LabelAttributes copy()
        {
            return this.copyTo(new MessageLabelAttributes());
        }

        protected SegmentPlaneAttributes.LabelAttributes copyTo(SegmentPlaneAttributes.LabelAttributes copy)
        {
            super.copyTo(copy);

            if (copy instanceof MessageLabelAttributes)
            {
                ((MessageLabelAttributes) copy).setMessage(this.getMessage());
            }

            return copy;
        }

        public String getText(SegmentPlane segmentPlane, Position position, AVList values)
        {
            return this.getMessage();
        }
    }

    protected static String formatAngle(String format, Angle angle)
    {
        if (Angle.ANGLE_FORMAT_DMS.equals(format))
        {
            return angle.toDMSString();
        }
        else
        {
            return angle.toDecimalDegreesString(4);
        }
    }

    protected static String formatElevation(String elevationFormat, double elevation)
    {
        if (SAR2.UNIT_IMPERIAL.equals(elevationFormat))
        {
            return String.format("%.0f ft", WWMath.convertMetersToFeet(elevation));
        }
        else // Default to metric units.
        {
            return String.format("%.0f m", elevation);
        }
    }

    //**************************************************************//
    //********************  Utility Methods  ***********************//
    //**************************************************************//

    protected static double getSurfaceElevationAt(WorldWindow wwd, Angle latitude, Angle longitude)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Globe globe = wwd.getModel().getGlobe();
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        SectorGeometryList sgl = wwd.getSceneController().getTerrain();
        if (sgl != null)
        {
            Vec4 point = sgl.getSurfacePoint(latitude, longitude);
            if (point != null)
            {
                Position pos = globe.computePositionFromPoint(point);
                return pos.getElevation();
            }
        }

        return globe.getElevation(latitude, longitude);
    }

    protected static double[] computeAltitudesToFitPositions(WorldWindow wwd, SegmentPlane segmentPlane,
        Iterable<? extends Position> positions, boolean recallUserDefinedVGap)
    {
        Globe globe = wwd.getModel().getGlobe();
        double[] altitudes = segmentPlane.getPlaneAltitudes();
        double[] gridSizes = segmentPlane.getGridCellDimensions();
        Position[] segmentPositions = segmentPlane.getSegmentPositions();

        double oldMaxSegmentAltitude = Math.max(segmentPositions[0].getElevation(), segmentPositions[1].getElevation());
        double[] minAndMaxElevation = globe.getMinAndMaxElevations(Sector.boundingSector(positions));

        double newMaxSegmentAltitude = -Double.MAX_VALUE;
        for (Position pos : positions)
        {
            if (newMaxSegmentAltitude < pos.getElevation())
                newMaxSegmentAltitude = pos.getElevation();
        }

        double segmentVGap = altitudes[1] - oldMaxSegmentAltitude;
        if (!recallUserDefinedVGap || segmentVGap < 0)
        {
            segmentVGap = computeInitialVerticalGap(wwd, segmentPlane, positions);
        }

        return new double[]
            {
                gridSizes[1] * Math.floor(minAndMaxElevation[0] / gridSizes[1]),
                newMaxSegmentAltitude + segmentVGap
            };
    }

    protected static LatLon[] computeLocationsToFitPositions(WorldWindow wwd, SegmentPlane segmentPlane,
        Position position1, Position position2, boolean recallUserDefinedHGap)
    {
        LatLon[] locations = segmentPlane.getPlaneLocations();
        Position[] segmentPositions = segmentPlane.getSegmentPositions();

        Angle segmentHGap = LatLon.rhumbDistance(segmentPositions[1], locations[1]);
        if (!recallUserDefinedHGap || segmentHGap.compareTo(Angle.ZERO) < 0)
        {
            segmentHGap = computeInitialHorizontalGap(wwd, segmentPlane, position1, position2);
        }

        Angle newSegmentHeading = LatLon.rhumbAzimuth(position1, position2);
        Angle newSegmentLength = LatLon.rhumbDistance(position1, position2).add(segmentHGap);

        return new LatLon[]
            {
                new LatLon(position1),
                LatLon.rhumbEndPosition(position1, newSegmentHeading, newSegmentLength)
            };
    }

    protected static double computeInitialVerticalGap(WorldWindow wwd, SegmentPlane segmentPlane,
        Iterable<? extends Position> positions)
    {
        double[] gridCellDimensions = segmentPlane.getGridCellDimensions();

        double maxHeightAboveSurface = -Double.MAX_VALUE;
        for (Position pos : positions)
        {
            double heightAboveSurface = pos.getElevation() - getSurfaceElevationAt(wwd,
                pos.getLatitude(), pos.getLongitude());
            if (heightAboveSurface > maxHeightAboveSurface)
            {
                maxHeightAboveSurface = heightAboveSurface;
            }
        }

        return Math.max(2 * gridCellDimensions[1], maxHeightAboveSurface / 2.0);
    }

    protected static Angle computeInitialHorizontalGap(WorldWindow wwd, SegmentPlane segmentPlane,
        Position position1, Position position2)
    {
        double[] gridCellDimensions = segmentPlane.getGridCellDimensions();

        double gridWidthRadians = gridCellDimensions[0] / wwd.getModel().getGlobe().getRadius();
        double segmentDistanceRadians = LatLon.rhumbDistance(position1, position2).radians;

        return Angle.fromRadians(Math.max(2 * gridWidthRadians, segmentDistanceRadians / 2.0));
    }
}
