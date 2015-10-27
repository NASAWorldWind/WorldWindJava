/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.shapebuilder;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.event.*;

/**
 * An abstract class defining common functionality and fields for editors used in the RigidShapeBuilder example.  These
 * include field variables and getters and setters for the shape's annotations that are displayed during editing (and
 * related labels), references to the current WorldWindow and mouse location, flags indicating whether the editor is
 * currently armed and whether annotations should be shown, as well as fields indicating the current action being
 * performed, the current editMode, and the current altitudeMode.
 * <p/>
 * In addition, the class contains several helper functions related to displaying annotations, which all editors should
 * be able to do.
 *
 * @author ccrick
 * @version $Id: AbstractShapeEditor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractShapeEditor extends AbstractLayer implements MouseListener, MouseMotionListener
{
    /**
     * Labels used in the annotations which are displayed during editing to show the current value of various shape
     * parameters.  Actual label values are retrieved from the World Wind message resource bundle.
     */
    public static final String ANGLE_LABEL = "MeasureTool.AngleLabel";
    public static final String AREA_LABEL = "MeasureTool.AreaLabel";
    public static final String LENGTH_LABEL = "MeasureTool.LengthLabel";
    public static final String PERIMETER_LABEL = "MeasureTool.PerimeterLabel";
    public static final String RADIUS_LABEL = "MeasureTool.RadiusLabel";
    public static final String HEIGHT_LABEL = "MeasureTool.HeightLabel";
    public static final String WIDTH_LABEL = "MeasureTool.WidthLabel";
    public static final String HEADING_LABEL = "MeasureTool.HeadingLabel";
    public static final String TILT_LABEL = "MeasureTool.TiltLabel";
    public static final String ROLL_LABEL = "MeasureTool.RollLabel";
    public static final String EAST_SKEW_LABEL = "MeasureTool.EastSkewLabel";
    public static final String NORTH_SKEW_LABEL = "MeasureTool.NorthSkewLabel";
    public static final String CENTER_LATITUDE_LABEL = "MeasureTool.CenterLatitudeLabel";
    public static final String CENTER_LONGITUDE_LABEL = "MeasureTool.CenterLongitudeLabel";
    public static final String CENTER_ALTITUDE_LABEL = "MeasureTool.CenterAltitudeLabel";
    public static final String LATITUDE_LABEL = "MeasureTool.LatitudeLabel";
    public static final String LONGITUDE_LABEL = "MeasureTool.LongitudeLabel";
    public static final String ALTITUDE_LABEL = "MeasureTool.AltitudeLabel";
    public static final String ACCUMULATED_LABEL = "MeasureTool.AccumulatedLabel";
    public static final String MAJOR_AXIS_LABEL = "MeasureTool.MajorAxisLabel";
    public static final String MINOR_AXIS_LABEL = "MeasureTool.MinorAxisLabel";

    protected WorldWindow wwd;
    protected Point mousePoint;

    protected ScreenAnnotation annotation;  // an annotation for displaying current values of various shape parameters
    protected AnnotationAttributes annotationAttributes; // attributes controlling the look-and-feel of the annotation
    protected UnitsFormat unitsFormat;  // class for uniformly formatting the units used in annotation values

    protected boolean armed;
    protected boolean showAnnotation = true;
    protected boolean aboveGround = false;

    protected long frameTimestamp = -1;
    protected String activeAction;

    protected String editMode;
    protected int altitudeMode = WorldWind.ABSOLUTE;

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
        }

        this.wwd = wwd;

        if (this.wwd != null)
        {
            this.wwd.getInputHandler().addMouseListener(this);
            this.wwd.getInputHandler().addMouseMotionListener(this);
        }
    }

    public boolean isArmed()
    {
        return this.armed;
    }

    public void setArmed(boolean armed)
    {
        this.armed = armed;
    }

    public int getAltitudeMode()
    {
        return this.altitudeMode;
    }

    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
    }

    public boolean isShowAnnotation()
    {
        return this.showAnnotation;
    }

    public void setShowAnnotation(boolean state)
    {
        this.showAnnotation = state;
    }

    public boolean isAboveGround()
    {
        return this.aboveGround;
    }

    public void setAboveGround(boolean state)
    {
        this.aboveGround = state;
    }

    public String getLabel(String labelName)
    {
        if (labelName == null)
        {
            String msg = Logging.getMessage("nullValue.LabelName");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String label = this.getStringValue(labelName);

        if (label != null)
            return label;
        else
        {
            if (this.unitsFormat == null)
                this.unitsFormat = new UnitsFormat();

            return this.unitsFormat.getStringValue(labelName);
        }
    }

    public void setLabel(String labelName, String label)
    {
        if (labelName != null && labelName.length() > 0)
            this.setValue(labelName, label);
    }

    protected void setAnnotationAttributes(AnnotationAttributes attributes)
    {
        this.annotationAttributes = attributes;
    }

    protected AnnotationAttributes getAnnotationAttributes()
    {
        return this.annotationAttributes;
    }

    abstract public void setShape(AbstractShape shape);

    abstract public String getEditMode();

    abstract public void setEditMode(String editMode);

    abstract public void updateAnnotation(Position pos);

    protected void initializeAnnotation()
    {
        // Annotation attributes
        this.setInitialLabels();

        this.annotationAttributes = new AnnotationAttributes();
        this.annotationAttributes.setFrameShape(AVKey.SHAPE_NONE);
        this.annotationAttributes.setInsets(new Insets(0, 0, 0, 0));
        this.annotationAttributes.setDrawOffset(new Point(0, 10));
        this.annotationAttributes.setTextAlign(AVKey.CENTER);
        this.annotationAttributes.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        this.annotationAttributes.setFont(Font.decode("Arial-Bold-14"));
        this.annotationAttributes.setTextColor(Color.WHITE);
        this.annotationAttributes.setBackgroundColor(Color.BLACK);
        this.annotationAttributes.setSize(new Dimension(220, 0));

        this.annotation = new ScreenAnnotation("", new Point(0, 0), this.annotationAttributes);
        this.annotation.getAttributes().setVisible(false);
        this.annotation.getAttributes().setDrawOffset(null); // use defaults
    }

    protected void setInitialLabels()
    {
        this.setLabel(ACCUMULATED_LABEL, Logging.getMessage(ACCUMULATED_LABEL));
        this.setLabel(ANGLE_LABEL, Logging.getMessage(ANGLE_LABEL));
        this.setLabel(AREA_LABEL, Logging.getMessage(AREA_LABEL));
        this.setLabel(CENTER_LATITUDE_LABEL, Logging.getMessage(CENTER_LATITUDE_LABEL));
        this.setLabel(CENTER_LONGITUDE_LABEL, Logging.getMessage(CENTER_LONGITUDE_LABEL));
        this.setLabel(CENTER_ALTITUDE_LABEL, Logging.getMessage(CENTER_ALTITUDE_LABEL));
        this.setLabel(HEADING_LABEL, Logging.getMessage(HEADING_LABEL));
        this.setLabel(TILT_LABEL, Logging.getMessage(TILT_LABEL));
        this.setLabel(ROLL_LABEL, Logging.getMessage(ROLL_LABEL));
        this.setLabel(EAST_SKEW_LABEL, Logging.getMessage(EAST_SKEW_LABEL));
        this.setLabel(NORTH_SKEW_LABEL, Logging.getMessage(NORTH_SKEW_LABEL));
        this.setLabel(HEIGHT_LABEL, Logging.getMessage(HEIGHT_LABEL));
        this.setLabel(LATITUDE_LABEL, Logging.getMessage(LATITUDE_LABEL));
        this.setLabel(LONGITUDE_LABEL, Logging.getMessage(LONGITUDE_LABEL));
        this.setLabel(ALTITUDE_LABEL, Logging.getMessage(ALTITUDE_LABEL));
        this.setLabel(LENGTH_LABEL, Logging.getMessage(LENGTH_LABEL));
        this.setLabel(MAJOR_AXIS_LABEL, Logging.getMessage(MAJOR_AXIS_LABEL));
        this.setLabel(MINOR_AXIS_LABEL, Logging.getMessage(MINOR_AXIS_LABEL));
        this.setLabel(PERIMETER_LABEL, Logging.getMessage(PERIMETER_LABEL));
        this.setLabel(RADIUS_LABEL, Logging.getMessage(RADIUS_LABEL));
        this.setLabel(WIDTH_LABEL, Logging.getMessage(WIDTH_LABEL));
    }

    protected boolean arePositionsRedundant(Position posA, Position posB)
    {
        if (posA == null || posB == null)
            return false;

        String aLat = this.unitsFormat.angleNL("", posA.getLatitude());
        String bLat = this.unitsFormat.angleNL("", posB.getLatitude());

        if (!aLat.equals(bLat))
            return false;

        String aLon = this.unitsFormat.angleNL("", posA.getLongitude());
        String bLon = this.unitsFormat.angleNL("", posB.getLongitude());

        if (!aLon.equals(bLon))
            return false;

        String aAlt = this.unitsFormat.lengthNL("", posA.getAltitude());
        String bAlt = this.unitsFormat.lengthNL("", posB.getAltitude());

        return aAlt.equals(bAlt);
    }
}
