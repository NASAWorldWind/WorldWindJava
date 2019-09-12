/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.util.measuretool;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author tag
 * @version $Id: WWOMeasureDisplay.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWOMeasureDisplay implements WWOMeasureTool.MeasureDisplay
{
    protected WWOMeasureTool measureTool;
    protected ScreenAnnotation annotation;
    protected AnnotationAttributes annotationAttributes;
    protected AVListImpl avList = new AVListImpl();
    protected UnitsFormat unitsFormat = new UnitsFormat();

    public WWOMeasureDisplay(WWOMeasureTool measureTool)
    {
        this.measureTool = measureTool;

        this.annotationAttributes = new AnnotationAttributes();
        this.annotationAttributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        this.annotationAttributes.setInsets(new Insets(10, 10, 10, 10));
        this.annotationAttributes.setDrawOffset(new Point(0, 10));
        this.annotationAttributes.setTextAlign(AVKey.CENTER);
        this.annotationAttributes.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        this.annotationAttributes.setFont(Font.decode("Arial-Bold-14"));
        this.annotationAttributes.setTextColor(Color.WHITE);
        this.annotationAttributes.setBackgroundColor(new Color(0, 0, 0, 180));
        this.annotationAttributes.setSize(new Dimension(220, 0));

        this.annotation = new ScreenAnnotation("", new Point(0, 0), this.annotationAttributes);
        this.annotation.getAttributes().setVisible(false);
        this.annotation.getAttributes().setDrawOffset(null); // use defaults

        this.setInitialLabels();
    }

    protected void setInitialLabels()
    {
        this.setLabel(ACCUMULATED_LABEL, "Accumulated");
        this.setLabel(ANGLE_LABEL, "Angle");
        this.setLabel(AREA_LABEL, "Area");
        this.setLabel(CENTER_LATITUDE_LABEL, "Center Lat");
        this.setLabel(CENTER_LONGITUDE_LABEL, "Center Lon");
        this.setLabel(HEADING_LABEL, "Heading");
        this.setLabel(HEIGHT_LABEL, "Height");
        this.setLabel(LATITUDE_LABEL, "Lat");
        this.setLabel(LONGITUDE_LABEL, "Lon");
        this.setLabel(LENGTH_LABEL, "Length");
        this.setLabel(MAJOR_AXIS_LABEL, "Major");
        this.setLabel(MINOR_AXIS_LABEL, "Minor");
        this.setLabel(PERIMETER_LABEL, "Perimeter");
        this.setLabel(RADIUS_LABEL, "Radius");
        this.setLabel(WIDTH_LABEL, "Width");
    }

    public void addToLayer(RenderableLayer layer)
    {
        layer.addRenderable(this.annotation);
    }

    public void removeFromLayer(RenderableLayer layer)
    {
        layer.removeRenderable(this.annotation);
    }

    public void setLabel(String labelName, String label)
    {
        if (labelName != null && labelName.length() > 0)
            this.avList.setValue(labelName, label);
    }

    public String getLabel(String labelName)
    {
        if (labelName == null)
        {
            String msg = Logging.getMessage("nullValue.LabelName");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String label = this.avList.getStringValue(labelName);

        return label != null ? label : this.measureTool.getUnitsFormat().getStringValue(labelName);
    }

    public boolean isAnnotation(Object o)
    {
        return o == this.annotation;
    }

    public void updateMeasureDisplay(Position position)
    {
        if (position == null)
        {
            this.annotation.getAttributes().setVisible(false);
            return;
        }

        String displayString = this.getDisplayString(position, this.measureTool);

        if (displayString == null)
        {
            this.annotation.getAttributes().setVisible(false);
            return;
        }

        this.annotation.setText(displayString);

        Vec4 screenPoint = this.computeAnnotationPosition(position, this.measureTool);
        if (screenPoint != null)
            this.annotation.setScreenPoint(new Point((int) screenPoint.x, (int) screenPoint.y));

        this.annotation.getAttributes().setVisible(true);
    }

    protected String getDisplayString(Position pos, WWOMeasureTool mt)
    {
        String displayString = null;
        String shapeType = this.measureTool.getMeasureShapeType();
        Rectangle2D.Double shapeRectangle = this.measureTool.getShapeRectangle();

        if (pos != null)
        {
            if (shapeType.equals(AVKey.SHAPE_CIRCLE) && shapeRectangle != null)
            {
                displayString = this.formatCircleMeasurements(pos, mt);
            }
            else if (shapeType.equals(AVKey.SHAPE_SQUARE) && shapeRectangle != null)
            {
                displayString = this.formatSquareMeasurements(pos, mt);
            }
            else if (shapeType.equals(AVKey.SHAPE_QUAD) && shapeRectangle != null)
            {
                displayString = this.formatQuadMeasurements(pos, mt);
            }
            else if (shapeType.equals(AVKey.SHAPE_ELLIPSE) && shapeRectangle != null)
            {
                displayString = this.formatEllipseMeasurements(pos, mt);
            }
            else if (shapeType.equals(AVKey.SHAPE_LINE) || shapeType.equals(AVKey.SHAPE_PATH))
            {
                displayString = this.formatLineMeasurements(pos, mt);
            }
            else if (shapeType.equals(AVKey.SHAPE_POLYGON))
            {
                displayString = this.formatPolygonMeasurements(pos, mt);
            }
        }

        return displayString;
    }

    protected Vec4 computeAnnotationPosition(Position pos, WWOMeasureTool mt)
    {
        Vec4 surfacePoint = mt.getWwd().getSceneController().getTerrain().getSurfacePoint(
            pos.getLatitude(), pos.getLongitude());
        if (surfacePoint == null)
        {
            Globe globe = mt.getWwd().getModel().getGlobe();
            surfacePoint = globe.computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                globe.getElevation(pos.getLatitude(), pos.getLongitude()));
        }

        return mt.getWwd().getView().project(surfacePoint);
    }

    protected String formatCircleMeasurements(Position pos, WWOMeasureTool mt)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(mt.getUnitsFormat().areaNL(mt.getLabel(AREA_LABEL), mt.getArea()));
        sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(PERIMETER_LABEL), mt.getLength()));

        if (mt.getShapeRectangle() != null)
            sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(RADIUS_LABEL), mt.getShapeRectangle().width / 2d));

        this.formatControlPoints(pos, mt, sb);

        return sb.toString();
    }

    protected String formatEllipseMeasurements(Position pos, WWOMeasureTool mt)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(mt.getUnitsFormat().areaNL(mt.getLabel(AREA_LABEL), mt.getArea()));
        sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(PERIMETER_LABEL), mt.getLength()));

        if (mt.getShapeRectangle() != null)
        {
            sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(MAJOR_AXIS_LABEL), mt.getShapeRectangle().width));
            sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(MINOR_AXIS_LABEL), mt.getShapeRectangle().height));
        }

        if (mt.getOrientation() != null)
            sb.append(mt.getUnitsFormat().angleNL(mt.getLabel(HEADING_LABEL), mt.getOrientation()));

        this.formatControlPoints(pos, mt, sb);

        return sb.toString();
    }

    protected String formatSquareMeasurements(Position pos, WWOMeasureTool mt)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(mt.getUnitsFormat().areaNL(mt.getLabel(AREA_LABEL), mt.getArea()));
        sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(PERIMETER_LABEL), mt.getLength()));

        if (mt.getShapeRectangle() != null)
            sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(WIDTH_LABEL), mt.getShapeRectangle().width));

        if (mt.getOrientation() != null)
            sb.append(mt.getUnitsFormat().angleNL(mt.getLabel(HEADING_LABEL), mt.getOrientation()));

        this.formatControlPoints(pos, mt, sb);

        return sb.toString();
    }

    protected String formatQuadMeasurements(Position pos, WWOMeasureTool mt)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(mt.getUnitsFormat().areaNL(mt.getLabel(AREA_LABEL), mt.getArea()));
        sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(PERIMETER_LABEL), mt.getLength()));

        if (mt.getShapeRectangle() != null)
        {
            sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(WIDTH_LABEL), mt.getShapeRectangle().width));
            sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(HEIGHT_LABEL), mt.getShapeRectangle().height));
        }

        if (mt.getOrientation() != null)
            sb.append(mt.getUnitsFormat().angleNL(mt.getLabel(HEADING_LABEL), mt.getOrientation()));

        this.formatControlPoints(pos, mt, sb);

        return sb.toString();
    }

    protected String formatPolygonMeasurements(Position pos, WWOMeasureTool mt)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(mt.getUnitsFormat().areaNL(mt.getLabel(AREA_LABEL), mt.getArea()));
        sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(PERIMETER_LABEL), mt.getLength()));

        this.formatControlPoints(pos, mt, sb);

        return sb.toString();
    }

    protected String formatLineMeasurements(Position pos, WWOMeasureTool mt)
    {
        // TODO: Compute the heading of individual path segments
        StringBuilder sb = new StringBuilder();

        sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(LENGTH_LABEL), mt.getLength()));

        Double accumLength = this.computeAccumulatedLength(pos, mt);
        if (accumLength != null && accumLength >= 1 && !lengthsEssentiallyEqual(mt.getLength(), accumLength))
            sb.append(mt.getUnitsFormat().lengthNL(mt.getLabel(ACCUMULATED_LABEL), accumLength));

        if (mt.getOrientation() != null)
            sb.append(mt.getUnitsFormat().angleNL(mt.getLabel(HEADING_LABEL), mt.getOrientation()));

        this.formatControlPoints(pos, mt, sb);

        return sb.toString();
    }

    protected void formatControlPoints(Position pos, WWOMeasureTool mt, StringBuilder sb)
    {
        if (mt.getCenterPosition() != null && areLocationsRedundant(mt.getCenterPosition(), pos, mt.getUnitsFormat()))
        {
            sb.append(
                mt.getUnitsFormat().angleNL(mt.getLabel(CENTER_LATITUDE_LABEL), mt.getCenterPosition().getLatitude()));
            sb.append(mt.getUnitsFormat().angleNL(mt.getLabel(CENTER_LONGITUDE_LABEL),
                mt.getCenterPosition().getLongitude()));
        }
        else
        {   // See if it's a control point and show it if it is
            for (int i = 0; i < mt.getControlPoints().size(); i++)
            {
                if (this.areLocationsRedundant(pos, mt.getControlPoints().get(i).getPosition(), mt.getUnitsFormat()))
                {
                    sb.append(mt.getUnitsFormat().angleNL(mt.getLabel(LATITUDE_LABEL), pos.getLatitude()));
                    sb.append(mt.getUnitsFormat().angleNL(mt.getLabel(LONGITUDE_LABEL), pos.getLongitude()));
                }
            }
        }
    }

    protected Double computeAccumulatedLength(LatLon pos, WWOMeasureTool mt)
    {
        if (mt.getPositions().size() < 2)
            return null;

        double radius = mt.getWwd().getModel().getGlobe().getRadius();
        double distanceFromStart = 0;
        int segmentIndex = 0;
        LatLon pos1 = mt.getPositions().get(segmentIndex);
        for (int i = 1; i < mt.getPositions().size(); i++)
        {
            LatLon pos2 = mt.getPositions().get(i);
            double segmentLength = LatLon.greatCircleDistance(pos1, pos2).radians * radius;

            // Check whether the position is inside the segment
            double length1 = LatLon.greatCircleDistance(pos1, pos).radians * radius;
            double length2 = LatLon.greatCircleDistance(pos2, pos).radians * radius;
            if (length1 <= segmentLength && length2 <= segmentLength)
            {
                // Compute portion of segment length
                distanceFromStart += length1 / (length1 + length2) * segmentLength;
                break;
            }
            else
                distanceFromStart += segmentLength;
            pos1 = pos2;
        }

        double gcPathLength = this.computePathLength(mt);

        return distanceFromStart < gcPathLength ? mt.getLength() * (distanceFromStart / gcPathLength) : null;
    }

    protected double computePathLength(WWOMeasureTool mt)
    {
        double pathLengthRadians = 0;

        LatLon pos1 = null;
        for (LatLon pos2 : mt.getPositions())
        {
            if (pos1 != null)
                pathLengthRadians += LatLon.greatCircleDistance(pos1, pos2).radians;
            pos1 = pos2;
        }

        return pathLengthRadians * mt.getWwd().getModel().getGlobe().getRadius();
    }

    protected Angle computeAngleBetween(LatLon a, LatLon b, LatLon c)
    {
        Vec4 v0 = new Vec4(
            b.getLatitude().radians - a.getLatitude().radians,
            b.getLongitude().radians - a.getLongitude().radians, 0);

        Vec4 v1 = new Vec4(
            c.getLatitude().radians - b.getLatitude().radians,
            c.getLongitude().radians - b.getLongitude().radians, 0);

        return v0.angleBetween3(v1);
    }

    protected boolean lengthsEssentiallyEqual(double l1, double l2)
    {
        return Math.abs(l1 - l2) < 0.01; // equal to within a centimeter
    }

    protected boolean areLocationsRedundant(LatLon locA, LatLon locB, UnitsFormat units)
    {
        if (locA == null || locB == null)
            return false;

        String aLat = units.angleNL("", locA.getLatitude());
        String bLat = units.angleNL("", locB.getLatitude());

        if (!aLat.equals(bLat))
            return false;

        String aLon = units.angleNL("", locA.getLongitude());
        String bLon = units.angleNL("", locB.getLongitude());

        return aLon.equals(bLon);
    }
}
