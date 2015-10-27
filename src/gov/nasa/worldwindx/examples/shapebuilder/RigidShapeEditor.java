/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.shapebuilder;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.util.ShapeUtils;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.editor.AirspaceEditorUtil;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * @author ccrick
 * @version $Id: RigidShapeEditor.java 2215 2014-08-09 20:05:40Z tgaskins $
 */
public class RigidShapeEditor extends AbstractShapeEditor
{
    public static final String MOVE_VERTEX_ACTION = "gov.nasa.worldwind.RigidShapeEditor.MoveVertexAction";
    public static final String CHANGE_HEIGHT_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ChangeHeightAction";
    public static final String CHANGE_LATITUDE_ACTION = "gov.nasa.worldwind.RigidShapeEditor.MoveShapeLatitudeAction";
    public static final String CHANGE_LONGITUDE_ACTION = "gov.nasa.worldwind.RigidShapeEditor.MoveShapeLongitudeAction";
    public static final String MOVE_SHAPE_ACTION = "gov.nasa.worldwind.RigidShapeEditor.MoveShapeAction";

    public static final String SCALE_SHAPE_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleShapeAction";
    public static final String SCALE_NORTH_SOUTH_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleNorthSouthAction";
    public static final String SCALE_EAST_WEST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleEastWestAction";
    public static final String SCALE_VERTICAL_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleVerticalAction";
    public static final String SCALE_NORTH_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleNorthAction";
    public static final String SCALE_SOUTH_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleSouthAction";
    public static final String SCALE_EAST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleEastAction";
    public static final String SCALE_WEST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleWestAction";
    public static final String SCALE_UP_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleUpAction";
    public static final String SCALE_DOWN_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleDownAction";
    public static final String SCALE_RADIUS_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleRadiusAction";
    public static final String SCALE_ANGLE_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleAngleAction";

    public static final String SCALE_NORTHEAST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleNortheastAction";
    public static final String SCALE_SOUTHWEST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleSouthwestAction";
    public static final String SCALE_NORTHWEST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleNorthwestAction";
    public static final String SCALE_SOUTHEAST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ScaleSoutheastAction";

    public static final String CHANGE_HEADING_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ChangeHeadingAction";
    public static final String CHANGE_TILT_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ChangeTiltAction";
    public static final String CHANGE_ROLL_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ChangeRollAction";

    public static final String SKEW_NORTH_SOUTH_ACTION = "gov.nasa.worldwind.RigidShapeEditor.SkewNorthSouthAction";
    public static final String SKEW_EAST_WEST_ACTION = "gov.nasa.worldwind.RigidShapeEditor.SkewEastWestAction";
    public static final String CHANGE_SKEW_ACTION = "gov.nasa.worldwind.RigidShapeEditor.ChangeSkewAction";

    public static final String SET_TEXTURE_ACTION = "gov.nasa.worldwind.RigidShapeEditor.SetTextureAction";
    public static final String TEXTURE_MOVE_ACTION = "gov.nasa.worldwind.RigidShapeEditor.TextureMoveAction";

    public static final String TEXTURE_UPPER_LEFT_ACTION = "gov.nasa.worldwind.RigidShapeEditor.textureUpperLeftAction";
    public static final String TEXTURE_UPPER_RIGHT_ACTION
        = "gov.nasa.worldwind.RigidShapeEditor.textureUpperRightAction";
    public static final String TEXTURE_LOWER_LEFT_ACTION = "gov.nasa.worldwind.RigidShapeEditor.textureLowerLeftAction";
    public static final String TEXTURE_LOWER_RIGHT_ACTION
        = "gov.nasa.worldwind.RigidShapeEditor.textureLowerRightAction";

    public static final String TEXTURE_SCALE_RIGHT_ACTION
        = "gov.nasa.worldwind.RigidShapeEditor.textureScaleRightAction";
    public static final String TEXTURE_SCALE_LEFT_ACTION = "gov.nasa.worldwind.RigidShapeEditor.textureScaleLeftAction";
    public static final String TEXTURE_SCALE_UP_ACTION = "gov.nasa.worldwind.RigidShapeEditor.textureScaleUpAction";
    public static final String TEXTURE_SCALE_DOWN_ACTION = "gov.nasa.worldwind.RigidShapeEditor.textureScaleDownAction";

    public static final String TRANSLATION_MODE = "gov.nasa.worldwind.RigidShapeEditor.TranslationMode";
    public static final String SCALE_MODE = "gov.nasa.worldwind.RigidShapeEditor.ScaleMode";
    public static final String ROTATION_MODE = "gov.nasa.worldwind.RigidShapeEditor.RotationMode";
    public static final String SKEW_MODE = "gov.nasa.worldwind.RigidShapeEditor.SkewMode";
    public static final String TEXTURE_MODE = "gov.nasa.worldwind.RigidShapeEditor.TextureMode";

    public static enum Direction
    {
        RIGHT, LEFT, UP, DOWN
    }

    public static final Integer UPPER_LEFT_UV = 0;
    public static final Integer UPPER_RIGHT_UV = 1;
    public static final Integer LOWER_LEFT_UV = 2;
    public static final Integer LOWER_RIGHT_UV = 3;

    protected RigidShape shape;
    protected RigidShape activeControlPoint;
    protected int activeControlPointIndex;
    protected java.util.List<RigidShape> controlPoints;
    protected int selectedFace = -1;

    protected BasicMarkerAttributes vertexControlAttributes;

    protected ShapeAttributes translationControlAttributes;
    protected ShapeAttributes scaleControlAttributes;
    protected ShapeAttributes rotationControlAttributes;
    protected ShapeAttributes textureControlAttributes;
    protected ShapeAttributes heightControlAttributes;
    protected ShapeAttributes radiusControlAttributes;

    protected ShapeAttributes rollGuideAttributes;
    protected ShapeAttributes headingGuideAttributes;
    protected ShapeAttributes tiltGuideAttributes;

    protected ShapeAttributes translationRodAttributes;
    protected ShapeAttributes scaleRodAttributes;
    protected ShapeAttributes rotationRodAttributes;
    protected ShapeAttributes radiusRodAttributes;

    java.util.List<Path> controlPointRods;

    protected Thread intersectionsDispatchThread;

    protected Path tempPath;
    protected Path tempPath2;
    protected Path tempPath3;
    protected boolean firstPass = true;

    public RigidShapeEditor()
    {
        this.assembleControlPointAttributes();
        this.initializeAnnotation();
        this.unitsFormat = new UnitsFormat();
    }

    public RigidShape getShape()
    {
        return this.shape;
    }

    public void setShape(AbstractShape shape)
    {
        if (shape == null)
        {
            String message = "nullValue.Shape";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.shape = (RigidShape) shape;
    }

    public String getEditMode()
    {
        return this.editMode;
    }

    public void setEditMode(String editMode)
    {
        if (editMode.equalsIgnoreCase("move"))
            this.editMode = TRANSLATION_MODE;
        else if (editMode.equalsIgnoreCase("scale"))
            this.editMode = SCALE_MODE;
        else if (editMode.equalsIgnoreCase("rotate"))
            this.editMode = ROTATION_MODE;
        else if (editMode.equalsIgnoreCase("skew"))
            this.editMode = SKEW_MODE;
        else if (editMode.equalsIgnoreCase("texture"))
            this.editMode = TEXTURE_MODE;
    }

    public int getSelectedFace()
    {
        return this.selectedFace;
    }

    public void setSelectedFace(int selectedFace)
    {
        if (selectedFace >= this.shape.getFaceCount() || selectedFace < 0)
            this.selectedFace = 0;
        else
            this.selectedFace = selectedFace;
    }

    protected void assembleControlPoints(DrawContext dc)
    {
        // Control points are re-computed each frame
        this.controlPoints = new ArrayList<RigidShape>();
        this.controlPointRods = new ArrayList<Path>();

        // Depending on the current edit mode, assemble the appropriate control points
        if (this.editMode.equalsIgnoreCase(TRANSLATION_MODE))
            this.assembleTranslationControlPoints(dc);
        else if (this.editMode.equalsIgnoreCase(SCALE_MODE))
            this.assembleScaleControlPoints(dc);
        else if (this.editMode.equalsIgnoreCase(ROTATION_MODE))
            this.assembleRotationControlPoints(dc);
        else if (this.editMode.equalsIgnoreCase(SKEW_MODE))
            this.assembleSkewControlPoints(dc);
        else if (this.editMode.equalsIgnoreCase(TEXTURE_MODE))
            this.assembleTextureControlPoints(dc);
    }

    protected void assembleTranslationControlPoints(DrawContext dc)
    {
        RigidShape shape = this.getShape();
        double radiusScaleFactor;

        //Matrix matrix = shape.computeRenderMatrix(dc);
        Vec4 refPt = shape.computeReferencePoint(dc);
        Position refPos = shape.getReferencePosition();

        double radius = ShapeUtils.getViewportScaleFactor(wwd) / 12;

        if (controlPoints.size() > 0)
        {
            for (RigidShape controlPoint : controlPoints)
            {
                controlPoint.setEastWestRadius(radius);
                controlPoint.setNorthSouthRadius(radius);
                controlPoint.setVerticalRadius(radius);
            }
        }
        else
        {
            // get perpendicular vectors (relative to earth)
            Vec4 upVec = this.wwd.getModel().getGlobe().computeSurfaceNormalAtLocation(refPos.getLatitude(),
                refPos.getLongitude()).normalize3();
            Vec4 northVec = this.wwd.getModel().getGlobe().computeNorthPointingTangentAtLocation(refPos.getLatitude(),
                refPos.getLongitude()).normalize3();
            Vec4 rightVec = northVec.cross3(upVec).normalize3();

            // compute width
            double width1 = Math.abs(Math.sin(this.shape.getHeading().getRadians()) * this.shape.getNorthSouthRadius());
            double width2 = Math.abs(Math.cos(this.shape.getHeading().getRadians()) * this.shape.getEastWestRadius());
            if (width1 > width2)
                radiusScaleFactor = width1;
            else
                radiusScaleFactor = width2;

            //vert = matrix.transformBy3(matrix, 1, 0, 0).add3(refPt);   // right
            Vec4 vert = refPt.add3(rightVec.multiply3(radiusScaleFactor + 2 * radius));
            Position vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            RigidShape controlPoint = new Pyramid(vertexPosition, radius, radius, radius);
            controlPoint.setRoll(Angle.NEG90);
            controlPoint.setAttributes(this.translationControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, CHANGE_LONGITUDE_ACTION);
            this.controlPoints.add(controlPoint);

            Path rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.translationRodAttributes);
            this.controlPointRods.add(rod);

            // compute height
            double height1 = Math.abs(
                Math.cos(this.shape.getHeading().getRadians()) * this.shape.getNorthSouthRadius());
            double height2 = Math.abs(Math.sin(this.shape.getHeading().getRadians()) * this.shape.getEastWestRadius());
            if (height1 > height2)
                radiusScaleFactor = height1;
            else
                radiusScaleFactor = height2;

            //Vec4 vert = matrix.transformBy3(matrix, 0, 1, 0).add3(refPt);   // top
            vert = refPt.add3(northVec.multiply3(radiusScaleFactor + 2 * radius));
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Pyramid(vertexPosition, radius, radius, radius);
            controlPoint.setTilt(Angle.POS90);
            controlPoint.setAttributes(this.translationControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, CHANGE_LATITUDE_ACTION);
            this.controlPoints.add(controlPoint);

            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.translationRodAttributes);
            this.controlPointRods.add(rod);

            //vert = matrix.transformBy3(matrix, 0, 0, 1).add3(refPt);   // front
            vert = refPt.add3(upVec.multiply3(this.shape.getVerticalRadius() + 2 * radius));
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Pyramid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.translationControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, CHANGE_HEIGHT_ACTION);
            controlPoint.setVisible(!dc.is2DGlobe());
            this.controlPoints.add(controlPoint);

            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.translationRodAttributes);
            this.controlPointRods.add(rod);
        }
    }

    protected void assembleScaleControlPoints(DrawContext dc)
    {
        RigidShape shape = this.getShape();

        Matrix matrix = shape.computeRenderMatrix(dc);
        Vec4 refPt = shape.computeReferencePoint(dc);
        Position refPos = shape.getReferencePosition();

        double radius = ShapeUtils.getViewportScaleFactor(wwd) / 12;

        if (controlPoints.size() > 0)
        {
            for (RigidShape controlPoint : controlPoints)
            {
                controlPoint.setEastWestRadius(radius);
                controlPoint.setNorthSouthRadius(radius);
                controlPoint.setVerticalRadius(radius);
            }
        }
        else
        {
            // create vertices at the extrema of the unit shape, and transform them by the
            // render matrix to get their final positions for use as control points

            Vec4 vert = matrix.transformBy3(matrix, 1, 0, 0).add3(refPt);   // right
            Position vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            RigidShape controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_EAST_WEST_ACTION);
            this.controlPoints.add(controlPoint);

            Path rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);

            vert = matrix.transformBy3(matrix, 0, 1, 0).add3(refPt);   // top
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_NORTH_SOUTH_ACTION);
            this.controlPoints.add(controlPoint);

            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);

            vert = matrix.transformBy3(matrix, 0, 0, 1).add3(refPt);   // front
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.scaleControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SCALE_VERTICAL_ACTION);
            controlPoint.setVisible(!dc.is2DGlobe());
            this.controlPoints.add(controlPoint);

            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.scaleRodAttributes);
            this.controlPointRods.add(rod);
        }
    }

    protected void assembleRotationControlPoints(DrawContext dc)
    {
        RigidShape shape = this.getShape();

        Matrix matrix = shape.computeRenderMatrix(dc);
        Vec4 refPt = shape.computeReferencePoint(dc);
        Position refPos = shape.getReferencePosition();

        double radius = ShapeUtils.getViewportScaleFactor(wwd) / 12;

        if (controlPoints.size() > 0)
        {
            for (RigidShape controlPoint : controlPoints)
            {
                controlPoint.setEastWestRadius(radius);
                controlPoint.setNorthSouthRadius(radius);
                controlPoint.setVerticalRadius(radius);
            }
        }
        else
        {
            // create vertices at the extrema of the unit shape, and transform them by the
            // render matrix to get their final positions for use as control points

            Vec4 vert = matrix.transformBy3(matrix, 1.5, 0, 0).add3(refPt);   // right
            Position vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            RigidShape controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.rollGuideAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, CHANGE_ROLL_ACTION);
            controlPoint.setVisible(false);
            this.controlPoints.add(controlPoint);

            Path rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.rotationRodAttributes);
            rod.setVisible(false);
            this.controlPointRods.add(rod);

            vert = matrix.transformBy3(matrix, 0, 1.5, 0).add3(refPt);   // top
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.headingGuideAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, CHANGE_HEADING_ACTION);
            controlPoint.setVisible(false);
            this.controlPoints.add(controlPoint);

            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.rotationRodAttributes);
            rod.setVisible(false);
            this.controlPointRods.add(rod);

            vert = matrix.transformBy3(matrix, 0, 0, 1.5).add3(refPt);   // front
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.tiltGuideAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, CHANGE_TILT_ACTION);
            controlPoint.setVisible(false);
            this.controlPoints.add(controlPoint);

            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.rotationRodAttributes);
            rod.setVisible(false);
            this.controlPointRods.add(rod);

            // create rotation guides
            Ellipsoid rollGuide = new Ellipsoid(this.shape.getReferencePosition(),
                radius / 2, this.shape.getVerticalRadius() * 1.5, this.shape.getEastWestRadius() * 1.5);
            rollGuide.setHeading(this.shape.getHeading());
            rollGuide.setTilt(this.shape.getTilt());
            rollGuide.setRoll(this.shape.getRoll());
            rollGuide.setAttributes(this.rollGuideAttributes);
            rollGuide.setAltitudeMode(this.getAltitudeMode());
            rollGuide.setValue(AVKey.ACTION, CHANGE_ROLL_ACTION);
            rollGuide.setVisible(!dc.is2DGlobe());
            this.controlPoints.add(rollGuide);

            Ellipsoid headingGuide = new Ellipsoid(this.shape.getReferencePosition(),
                this.shape.getNorthSouthRadius() * 1.5, radius / 2, this.shape.getEastWestRadius() * 1.5);
            headingGuide.setHeading(this.shape.getHeading());
            headingGuide.setTilt(this.shape.getTilt());
            headingGuide.setRoll(this.shape.getRoll());
            headingGuide.setAttributes(this.headingGuideAttributes);
            headingGuide.setAltitudeMode(this.getAltitudeMode());
            headingGuide.setValue(AVKey.ACTION, CHANGE_HEADING_ACTION);
            headingGuide.setVisible(true);
            this.controlPoints.add(headingGuide);

            Ellipsoid tiltGuide = new Ellipsoid(this.shape.getReferencePosition(),
                this.shape.getNorthSouthRadius() * 1.5, this.shape.getVerticalRadius() * 1.5, radius / 2);
            tiltGuide.setHeading(this.shape.getHeading());
            tiltGuide.setTilt(this.shape.getTilt());
            tiltGuide.setRoll(this.shape.getRoll());
            tiltGuide.setAttributes(this.tiltGuideAttributes);
            tiltGuide.setAltitudeMode(this.getAltitudeMode());
            tiltGuide.setValue(AVKey.ACTION, CHANGE_TILT_ACTION);
            tiltGuide.setVisible(!dc.is2DGlobe());
            this.controlPoints.add(tiltGuide);
        }
    }

    protected void assembleSkewControlPoints(DrawContext dc)
    {
        if (dc.is2DGlobe())
            return;

        RigidShape shape = this.getShape();

        Matrix matrix = shape.computeRenderMatrix(dc);
        Vec4 refPt = shape.computeReferencePoint(dc);
        Position refPos = shape.getReferencePosition();

        double radius = ShapeUtils.getViewportScaleFactor(wwd) / 12;

        if (controlPoints.size() > 0)
        {
            for (RigidShape controlPoint : controlPoints)
            {
                controlPoint.setEastWestRadius(radius);
                controlPoint.setNorthSouthRadius(radius);
                controlPoint.setVerticalRadius(radius);
            }
        }
        else
        {
            // create vertices at the extrema of the unit shape, and transform them by the
            // render matrix to get their final positions for use as control points

            Vec4 vert = matrix.transformBy3(matrix, 1, 0, 1).add3(refPt);   // right
            Position vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            RigidShape controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SKEW_EAST_WEST_ACTION);
            controlPoint.setVisible(true);
            this.controlPoints.add(controlPoint);

            Path rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.radiusRodAttributes);
            this.controlPointRods.add(rod);

            vert = matrix.transformBy3(matrix, 0, 1, 1).add3(refPt);   // top
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.radiusControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            controlPoint.setValue(AVKey.ACTION, SKEW_NORTH_SOUTH_ACTION);
            controlPoint.setVisible(true);
            this.controlPoints.add(controlPoint);

            rod = new Path(refPos, vertexPosition);
            rod.setAltitudeMode(this.getAltitudeMode());
            rod.setAttributes(this.radiusRodAttributes);
            this.controlPointRods.add(rod);

            // helper control points
            vert = matrix.transformBy3(matrix, 1, 0, 0).add3(refPt);   // (right)
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.rotationControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            //controlPoint.setValue(AVKey.ACTION, SKEW_EAST_WEST_ACTION);
            controlPoint.setVisible(false);
            this.controlPoints.add(controlPoint);

            vert = matrix.transformBy3(matrix, 0, 1, 0).add3(refPt);   // (top)
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.rotationControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            //controlPoint.setValue(AVKey.ACTION, SKEW_NORTH_SOUTH_ACTION);
            controlPoint.setVisible(false);
            this.controlPoints.add(controlPoint);

            vert = matrix.transformBy3(matrix, 0, 0, 1).add3(refPt);   // (front)
            vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);
            controlPoint = new Ellipsoid(vertexPosition, radius, radius, radius);
            controlPoint.setAttributes(this.rotationControlAttributes);
            controlPoint.setAltitudeMode(this.getAltitudeMode());
            //controlPoint.setValue(AVKey.ACTION, SKEW_NORTH_SOUTH_ACTION);
            controlPoint.setVisible(false);
            this.controlPoints.add(controlPoint);

            /*
            Box skewGuide = new Box(this.shape.getReferencePosition(),
                this.shape.getNorthSouthRadius() * 1.5, radius / 2, this.shape.getEastWestRadius() * 1.5);
            skewGuide.setHeading(this.shape.getHeading());
            skewGuide.setTilt(this.shape.getTilt());
            skewGuide.setRoll(this.shape.getRoll());
            skewGuide.setAttributes(this.headingGuideAttributes);
            skewGuide.setAltitudeMode(this.getAltitudeMode());
            skewGuide.setValue(AVKey.ACTION, CHANGE_SKEW_ACTION);
            skewGuide.setVisible(true);
            this.controlPoints.add(skewGuide);
            */
        }
    }

    protected void assembleTextureControlPoints(DrawContext dc)
    {
    }

    protected void assembleVertexControlPoints(DrawContext dc)
    {
        /*
        Terrain terrain = dc.getTerrain();
        RigidShape shape = this.getShape();

        Position refPos = shape.getReferencePosition();
        Vec4 refPoint = terrain.getSurfacePoint(refPos.getLatitude(), refPos.getLongitude(), 0);

        int altitudeMode = shape.getAltitudeMode();
        double height = shape.getHeight();

        Vec4 vaa = null;
        double vaaLength = 0; // used to compute independent length of each cap vertex
        double vaLength = 0;

        int i = 0;
        for (LatLon location : shape.getOuterBoundary())
        {
            Vec4 vert;

            // Compute the top/cap point.
            if (altitudeMode == WorldWind.CONSTANT || !(location instanceof Position))
            {
                if (vaa == null)
                {
                    // Compute the vector lengths of the top and bottom points at the reference position.
                    vaa = refPoint.multiply3(height / refPoint.getLength3());
                    vaaLength = vaa.getLength3();
                    vaLength = refPoint.getLength3();
                }

                // Compute the bottom point, which is on the terrain.
                vert = terrain.getSurfacePoint(location.getLatitude(), location.getLongitude(), 0);

                double delta = vaLength - vert.dot3(refPoint) / vaLength;
                vert = vert.add3(vaa.multiply3(1d + delta / vaaLength));
            }
            else if (altitudeMode == WorldWind.ABSOLUTE)
            {
                vert = terrain.getSurfacePoint(location.getLatitude(), location.getLongitude(),
                    ((Position) location).getAltitude());
            }
            else // WorldWind.ABSOLUTE
            {
                vert = terrain.getGlobe().computePointFromPosition(location.getLatitude(), location.getLongitude(),
                    ((Position) location).getAltitude() * terrain.getVerticalExaggeration());
            }

            Position vertexPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(vert);

            this.controlPoints.add(new ControlPointMarker(MOVE_VERTEX_ACTION, vertexPosition, vert,
                this.vertexControlAttributes, i));
            i++;
        }
        */
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        if (this.frameTimestamp != dc.getFrameTimeStamp())
        {
            this.assembleControlPoints(dc);
            this.frameTimestamp = dc.getFrameTimeStamp();
        }

        for (RigidShape shape : this.controlPoints)
        {
            shape.render(dc);
        }

        for (Path rod : this.controlPointRods)
        {
            rod.render(dc);
        }

        if (this.annotation != null && isShowAnnotation())
        {
            this.annotation.render(dc);
        }

        // testing
        if (this.tempPath != null)
            this.tempPath.render(dc);
        if (this.tempPath2 != null)
            this.tempPath2.render(dc);
        if (this.tempPath3 != null)
            this.tempPath3.render(dc);
    }

    @Override
    protected void doPick(DrawContext dc, Point point)
    {
        this.doRender(dc); // Same logic for picking and rendering
    }

    protected void assembleControlPointAttributes()
    {

        ShapeAttributes translateAttributes = new BasicShapeAttributes();
        translateAttributes.setInteriorMaterial(Material.GREEN);
        translateAttributes.setInteriorOpacity(1);
        translateAttributes.setEnableLighting(true);
        translateAttributes.setDrawInterior(true);
        translateAttributes.setDrawOutline(false);
        this.translationControlAttributes = translateAttributes;

        ShapeAttributes scaleAttributes = new BasicShapeAttributes();
        scaleAttributes.setInteriorMaterial(Material.RED);
        scaleAttributes.setInteriorOpacity(1);
        scaleAttributes.setEnableLighting(true);
        scaleAttributes.setDrawInterior(true);
        scaleAttributes.setDrawOutline(false);
        this.scaleControlAttributes = scaleAttributes;

        ShapeAttributes rotationAttributes = new BasicShapeAttributes();
        rotationAttributes.setInteriorMaterial(Material.YELLOW);
        rotationAttributes.setInteriorOpacity(1);
        rotationAttributes.setEnableLighting(true);
        rotationAttributes.setDrawInterior(true);
        rotationAttributes.setDrawOutline(false);
        this.rotationControlAttributes = rotationAttributes;
        this.textureControlAttributes = rotationAttributes;

        ShapeAttributes heightControlAttributes = new BasicShapeAttributes();
        heightControlAttributes.setInteriorMaterial(Material.RED);
        heightControlAttributes.setInteriorOpacity(1);
        heightControlAttributes.setEnableLighting(true);
        heightControlAttributes.setDrawInterior(true);
        heightControlAttributes.setDrawOutline(false);
        this.heightControlAttributes = heightControlAttributes;

        ShapeAttributes radiusControlAttributes = new BasicShapeAttributes();
        radiusControlAttributes.setInteriorMaterial(Material.BLUE);
        radiusControlAttributes.setInteriorOpacity(1);
        radiusControlAttributes.setEnableLighting(true);
        radiusControlAttributes.setDrawInterior(true);
        radiusControlAttributes.setDrawOutline(false);
        this.radiusControlAttributes = radiusControlAttributes;

        ShapeAttributes rollGuideAttributes = new BasicShapeAttributes();
        rollGuideAttributes.setInteriorMaterial(Material.RED);
        rollGuideAttributes.setInteriorOpacity(0.4);
        rollGuideAttributes.setEnableLighting(true);
        rollGuideAttributes.setDrawInterior(true);
        rollGuideAttributes.setDrawOutline(false);
        this.rollGuideAttributes = rollGuideAttributes;

        ShapeAttributes headingGuideAttributes = new BasicShapeAttributes();
        headingGuideAttributes.setInteriorMaterial(Material.YELLOW);
        headingGuideAttributes.setInteriorOpacity(0.4);
        headingGuideAttributes.setEnableLighting(true);
        headingGuideAttributes.setDrawInterior(true);
        headingGuideAttributes.setDrawOutline(false);
        this.headingGuideAttributes = headingGuideAttributes;

        ShapeAttributes tiltGuideAttributes = new BasicShapeAttributes();
        tiltGuideAttributes.setInteriorMaterial(Material.GREEN);
        tiltGuideAttributes.setInteriorOpacity(0.4);
        tiltGuideAttributes.setEnableLighting(true);
        tiltGuideAttributes.setDrawInterior(true);
        tiltGuideAttributes.setDrawOutline(false);
        this.tiltGuideAttributes = tiltGuideAttributes;

        ShapeAttributes translationRodAttributes = new BasicShapeAttributes();
        translationRodAttributes.setDrawOutline(true);
        translationRodAttributes.setOutlineMaterial(Material.GREEN);
        translationRodAttributes.setOutlineOpacity(0.6);
        this.translationRodAttributes = translationRodAttributes;

        ShapeAttributes scaleRodAttributes = new BasicShapeAttributes();
        scaleRodAttributes.setDrawOutline(true);
        scaleRodAttributes.setOutlineMaterial(Material.RED);
        scaleRodAttributes.setOutlineOpacity(0.6);
        this.scaleRodAttributes = scaleRodAttributes;

        ShapeAttributes rotationRodAttributes = new BasicShapeAttributes();
        rotationRodAttributes.setDrawOutline(true);
        rotationRodAttributes.setOutlineMaterial(Material.YELLOW);
        rotationRodAttributes.setOutlineOpacity(0.6);
        this.rotationRodAttributes = rotationRodAttributes;

        ShapeAttributes radiusRodAttributes = new BasicShapeAttributes();
        radiusRodAttributes.setDrawOutline(true);
        radiusRodAttributes.setOutlineMaterial(Material.BLUE);
        radiusRodAttributes.setOutlineOpacity(0.6);
        radiusRodAttributes.setDrawInterior(false);
        this.radiusRodAttributes = radiusRodAttributes;
    }

    //*******************************************************
    // ***************** Event handling *********************
    //*******************************************************

    public void mouseClicked(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        // Include this test to ensure any derived implementation performs it.
        if (!this.isArmed())
        {
            return;
        }

        if (this.isArmed())
        {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
            {
                Object topObject = null;
                PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
                if (pickedObjects != null)
                    topObject = pickedObjects.getTopObject();

                if (topObject instanceof ControlPointMarker)
                {
                    this.removeVertex((ControlPointMarker) topObject);
                    e.consume();
                }
                else
                {
                    this.addVertex(e.getPoint());
                    e.consume();
                }
            }
        }
    }

    public void mouseDragged(MouseEvent e)
    {
        Point lastMousePoint = this.mousePoint;
        this.mousePoint = e.getPoint();

        if (lastMousePoint == null)
            lastMousePoint = this.mousePoint;

        // update annotation
        if (isShowAnnotation())
        {
            if (this.activeControlPointIndex < 0)
                updateAnnotation(this.shape.getCenterPosition());
            else if (this.controlPoints != null)
                updateAnnotation(this.controlPoints.get(this.activeControlPointIndex).getReferencePosition());
        }

        if (CHANGE_HEIGHT_ACTION.equals(this.activeAction))
        {
            this.setShapeHeight(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (CHANGE_LATITUDE_ACTION.equals(this.activeAction))
        {
            this.moveShapeLatitude(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (CHANGE_LONGITUDE_ACTION.equals(this.activeAction))
        {
            this.moveShapeLongitude(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (MOVE_SHAPE_ACTION.equals(this.activeAction))
        {
            this.moveShape(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_NORTH_SOUTH_ACTION.equals(this.activeAction))
        {
            this.scaleShapeNorthSouth(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_NORTH_ACTION.equals(this.activeAction))
        {
            this.scaleShapeNorth(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_SOUTH_ACTION.equals(this.activeAction))
        {
            this.scaleShapeSouth(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_EAST_WEST_ACTION.equals(this.activeAction))
        {
            this.scaleShapeEastWest(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_EAST_ACTION.equals(this.activeAction))
        {
            this.scaleShapeEast(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_WEST_ACTION.equals(this.activeAction))
        {
            this.scaleShapeWest(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_VERTICAL_ACTION.equals(this.activeAction))
        {
            this.scaleShapeVertical(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_UP_ACTION.equals(this.activeAction))
        {
            this.scaleShapeUp(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_DOWN_ACTION.equals(this.activeAction))
        {
            this.scaleShapeDown(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_NORTHEAST_ACTION.equals(this.activeAction))
        {
            this.scaleShapeNortheast(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_SOUTHWEST_ACTION.equals(this.activeAction))
        {
            this.scaleShapeSouthwest(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_RADIUS_ACTION.equals(this.activeAction))
        {
            this.scaleShapeRadius(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_ANGLE_ACTION.equals(this.activeAction))
        {
            this.scaleShapeAngle(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SCALE_SHAPE_ACTION.equals(this.activeAction))
        {
            this.scaleShape(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (CHANGE_HEADING_ACTION.equals(this.activeAction))
        {
            this.changeShapeHeading(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (CHANGE_TILT_ACTION.equals(this.activeAction))
        {
            this.changeShapeTilt(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (CHANGE_ROLL_ACTION.equals(this.activeAction))
        {
            this.changeShapeRoll(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SKEW_NORTH_SOUTH_ACTION.equals(this.activeAction))
        {
            this.skewShapeNorthSouth(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (SKEW_EAST_WEST_ACTION.equals(this.activeAction))
        {
            this.skewShapeEastWest(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (CHANGE_SKEW_ACTION.equals(this.activeAction))
        {
            this.skewShape(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (TEXTURE_UPPER_LEFT_ACTION.equals(this.activeAction))
        {
            this.moveTextureCorner(lastMousePoint, this.mousePoint, UPPER_LEFT_UV);
            e.consume();
        }
        else if (TEXTURE_UPPER_RIGHT_ACTION.equals(this.activeAction))
        {
            this.moveTextureCorner(lastMousePoint, this.mousePoint, UPPER_RIGHT_UV);
            e.consume();
        }
        else if (TEXTURE_LOWER_LEFT_ACTION.equals(this.activeAction))
        {
            this.moveTextureCorner(lastMousePoint, this.mousePoint, LOWER_LEFT_UV);
            e.consume();
        }
        else if (TEXTURE_LOWER_RIGHT_ACTION.equals(this.activeAction))
        {
            this.moveTextureCorner(lastMousePoint, this.mousePoint, LOWER_RIGHT_UV);
            e.consume();
        }
        else if (TEXTURE_MOVE_ACTION.equals(this.activeAction))
        {
            this.moveTexture(lastMousePoint, this.mousePoint);
            e.consume();
        }
        else if (TEXTURE_SCALE_RIGHT_ACTION.equals(this.activeAction))
        {
            this.scaleTexture(lastMousePoint, this.mousePoint, Direction.RIGHT);
            e.consume();
        }
        else if (TEXTURE_SCALE_LEFT_ACTION.equals(this.activeAction))
        {
            this.scaleTexture(lastMousePoint, this.mousePoint, Direction.LEFT);
            e.consume();
        }
        else if (TEXTURE_SCALE_UP_ACTION.equals(this.activeAction))
        {
            this.scaleTexture(lastMousePoint, this.mousePoint, Direction.UP);
            e.consume();
        }
        else if (TEXTURE_SCALE_DOWN_ACTION.equals(this.activeAction))
        {
            this.scaleTexture(lastMousePoint, this.mousePoint, Direction.DOWN);
            e.consume();
        }
    }

    public void mousePressed(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.mousePoint = e.getPoint();

        Object topObject = null;
        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (pickedObjects != null)
            topObject = pickedObjects.getTopObject();

        if (topObject == this.getShape())
        {
            if (this.editMode.equalsIgnoreCase(TRANSLATION_MODE))
                this.activeAction = MOVE_SHAPE_ACTION;
            else if (this.editMode.equalsIgnoreCase(SCALE_MODE))
                this.activeAction = SCALE_SHAPE_ACTION;
            else if (this.editMode.equalsIgnoreCase(TEXTURE_MODE))
            {
                this.activeAction = SET_TEXTURE_ACTION;
                textureShape(e.getPoint(), e.getPoint());
            }

            // set the shape to be the "active control point"
            this.activeControlPoint = (RigidShape) topObject;
            this.activeControlPointIndex = -1;

            if (!this.editMode.equalsIgnoreCase(TEXTURE_MODE))
            {
                setShowAnnotation(true);
                updateAnnotation(this.shape.getReferencePosition());
            }
            e.consume();
        }
        else if (topObject instanceof RigidShape && ((RigidShape) topObject).getValue(AVKey.ACTION) != null)
        {
            this.activeControlPoint = (RigidShape) topObject;
            this.activeAction = (String) this.activeControlPoint.getValue(AVKey.ACTION);

            if (!this.editMode.equalsIgnoreCase(TEXTURE_MODE))
            {
                setShowAnnotation(true);
                updateAnnotation(this.activeControlPoint.getReferencePosition());
            }

            // update controlPointIndex;
            int i = 0;
            for (RigidShape controlPoint : this.controlPoints)
            {
                if (controlPoint.equals(topObject))
                    break;
                i++;
            }
            this.activeControlPointIndex = i;
            e.consume();
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        if (e == null)
        {
            return;
        }

        this.activeControlPoint = null;
        this.activeAction = null;

        setShowAnnotation(false);
        updateAnnotation(null);

        this.getWorldWindow().redraw();
        e.consume();
    }

    //*************************************************************
    // ***************** Shape manipulation *********************
    //*************************************************************

    protected void moveShape(Point previousMousePoint, Point mousePoint)
    {
        // Intersect a ray through each mouse point, with a geoid passing through the reference elevation.
        // If either ray fails to intersect the geoid, then ignore this event. Use the difference between the two
        // intersected positions to move the control point's location.

        View view = this.wwd.getView();
        Globe globe = this.wwd.getModel().getGlobe();

        Position refPos = this.shape.getReferencePosition();
        if (refPos == null)
            return;

        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousRay = view.computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());

        Vec4 vec = AirspaceEditorUtil.intersectGlobeAt(this.wwd, refPos.getElevation(), ray);
        Vec4 previousVec = AirspaceEditorUtil.intersectGlobeAt(this.wwd, refPos.getElevation(), previousRay);

        if (vec == null || previousVec == null)
        {
            return;
        }

        Position pos = globe.computePositionFromPoint(vec);
        Position previousPos = globe.computePositionFromPoint(previousVec);
        LatLon change = pos.subtract(previousPos);

        this.shape.move(new Position(change.getLatitude(), change.getLongitude(), 0.0));
    }

    protected void moveShapeLatitude(Point previousMousePoint, Point mousePoint)
    {
        // Intersect a ray through each mouse point, with a geoid passing through the reference elevation.
        // If either ray fails to intersect the geoid, then ignore this event. Use the difference between the two
        // intersected positions to move the control point's location.

        View view = this.wwd.getView();
        Globe globe = this.wwd.getModel().getGlobe();

        Position refPos = this.shape.getReferencePosition();
        if (refPos == null)
            return;

        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousRay = view.computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());

        Vec4 vec = AirspaceEditorUtil.intersectGlobeAt(this.wwd, refPos.getElevation(), ray);
        Vec4 previousVec = AirspaceEditorUtil.intersectGlobeAt(this.wwd, refPos.getElevation(), previousRay);

        if (vec == null || previousVec == null)
        {
            return;
        }

        Position pos = globe.computePositionFromPoint(vec);
        Position previousPos = globe.computePositionFromPoint(previousVec);
        LatLon change = pos.subtract(previousPos);

        this.shape.move(new Position(change.getLatitude(), Angle.ZERO, 0.0));
    }

    protected void moveShapeLongitude(Point previousMousePoint, Point mousePoint)
    {
        // Intersect a ray through each mouse point, with a geoid passing through the reference elevation.
        // If either ray fails to intersect the geoid, then ignore this event. Use the difference between the two
        // intersected positions to move the control point's location.

        View view = this.wwd.getView();
        Globe globe = this.wwd.getModel().getGlobe();

        Position refPos = this.shape.getReferencePosition();
        if (refPos == null)
            return;

        Line ray = view.computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousRay = view.computeRayFromScreenPoint(previousMousePoint.getX(), previousMousePoint.getY());

        Vec4 vec = AirspaceEditorUtil.intersectGlobeAt(this.wwd, refPos.getElevation(), ray);
        Vec4 previousVec = AirspaceEditorUtil.intersectGlobeAt(this.wwd, refPos.getElevation(), previousRay);

        if (vec == null || previousVec == null)
        {
            return;
        }

        Position pos = globe.computePositionFromPoint(vec);
        Position previousPos = globe.computePositionFromPoint(previousVec);
        LatLon change = pos.subtract(previousPos);

        this.shape.move(new Position(Angle.ZERO, change.getLongitude(), 0.0));
    }

    protected void setShapeHeight(Point previousMousePoint, Point mousePoint)
    {
        // Find the closest points between the rays through each screen point, and the ray from the control point
        // and in the direction of the globe's surface normal. Compute the elevation difference between these two
        // points, and use that as the change in polygon height.

        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        Vec4 surfaceNormal = this.wwd.getModel().getGlobe().computeSurfaceNormalAtLocation(referencePos.getLatitude(),
            referencePos.getLongitude());
        Line verticalRay = new Line(referencePoint, surfaceNormal);
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        Vec4 pointOnLine = AirspaceEditorUtil.nearestPointOnLine(verticalRay, screenRay);
        Vec4 previousPointOnLine = AirspaceEditorUtil.nearestPointOnLine(verticalRay, previousScreenRay);

        Position pos = this.wwd.getModel().getGlobe().computePositionFromPoint(pointOnLine);
        Position previousPos = this.wwd.getModel().getGlobe().computePositionFromPoint(previousPointOnLine);
        double elevationChange = pos.getElevation() - previousPos.getElevation();

        RigidShape shape = this.getShape();
        double height = shape.getCenterPosition().getElevation();

        if (this.aboveGround)
        {
            // restrict height to stay above terrain surface
            Vec4 lowestPoint = wwd.getSceneController().getTerrain().getSurfacePoint(referencePos.getLatitude(),
                referencePos.getLongitude(), this.shape.getNorthSouthRadius());
            Position lowestPosition = this.wwd.getModel().getGlobe().computePositionFromPoint(lowestPoint);
            if (this.shape.getReferencePosition().getAltitude() < lowestPosition.getAltitude())
            {
                this.shape.setCenterPosition(new Position(referencePos.getLatitude(), referencePos.getLongitude(),
                    lowestPosition.getAltitude()));
                return;
            }
            else if (this.shape.getReferencePosition().getAltitude() == lowestPosition.getAltitude()
                && elevationChange <= 0)
            {
                this.shape.setCenterPosition(new Position(referencePos.getLatitude(), referencePos.getLongitude(),
                    lowestPosition.getAltitude()));
                return;
            }
        }

        this.shape.setCenterPosition(new Position(referencePos.getLatitude(), referencePos.getLongitude(),
            height + elevationChange));
    }

    protected void scaleShapeEastWest(Point previousMousePoint, Point mousePoint)
    {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);

        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;

        RigidShape shape = this.getShape();
        double radius = shape.getEastWestRadius();

        if (radius + radiusChange > 0)
            this.shape.setEastWestRadius(radius + radiusChange);
    }

    protected void scaleShapeEast(Point previousMousePoint, Point mousePoint)
    {
        scaleShapeEastWest(previousMousePoint, mousePoint);
    }

    protected void scaleShapeWest(Point previousMousePoint, Point mousePoint)
    {
        scaleShapeEastWest(previousMousePoint, mousePoint);
    }

    protected void scaleShapeNorthSouth(Point previousMousePoint, Point mousePoint)
    {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);

        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;

        RigidShape shape = this.getShape();
        double radius = shape.getNorthSouthRadius();

        if (radius + radiusChange > 0)
            this.shape.setNorthSouthRadius(radius + radiusChange);
    }

    protected void scaleShapeNorth(Point previousMousePoint, Point mousePoint)
    {
        scaleShapeNorthSouth(previousMousePoint, mousePoint);
    }

    protected void scaleShapeSouth(Point previousMousePoint, Point mousePoint)
    {
        scaleShapeNorthSouth(previousMousePoint, mousePoint);
    }

    protected void scaleShapeVertical(Point previousMousePoint, Point mousePoint)
    {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);

        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;

        RigidShape shape = this.getShape();
        double radius = shape.getVerticalRadius();

        if (radius + radiusChange > 0)
            this.shape.setVerticalRadius(radius + radiusChange);
    }

    protected void scaleShapeUp(Point previousMousePoint, Point mousePoint)
    {
        scaleShapeVertical(previousMousePoint, mousePoint);
    }

    protected void scaleShapeDown(Point previousMousePoint, Point mousePoint)
    {
        scaleShapeVertical(previousMousePoint, mousePoint);
    }

    protected void scaleShapeNortheast(Point previousMousePoint, Point mousePoint)
    {
        // implement in subclass
    }

    protected void scaleShapeSouthwest(Point previousMousePoint, Point mousePoint)
    {
        // implement in subclass
    }

    protected void scaleShapeRadius(Point previousMousePoint, Point mousePoint)
    {
        // implement in subclass
    }

    protected void scaleShapeAngle(Point previousMousePoint, Point mousePoint)
    {
        // implement in subclass
    }

    protected void scaleShape(Point previousMousePoint, Point mousePoint)
    {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(referencePoint);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(referencePoint);

        double distance = nearestPointOnLine.distanceTo3(referencePoint);
        double previousDistance = previousNearestPointOnLine.distanceTo3(referencePoint);
        double radiusChange = distance - previousDistance;

        RigidShape shape = this.getShape();
        double eastWestRadius = shape.getEastWestRadius();
        double northSouthRadius = shape.getNorthSouthRadius();
        double verticalRadius = shape.getVerticalRadius();
        double average = (eastWestRadius + northSouthRadius + verticalRadius) / 3;

        double scalingRatio = (radiusChange + average) / average;

        if (scalingRatio > 0)
        {
            this.shape.setEastWestRadius(eastWestRadius * scalingRatio);
            this.shape.setNorthSouthRadius(northSouthRadius * scalingRatio);
            this.shape.setVerticalRadius(verticalRadius * scalingRatio);
        }
    }

    protected void changeShapeHeading(Point previousMousePoint, Point mousePoint)
    {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        // create rays from mouse click position (current and previous)
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        // get location of the control point
        Position controlPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);

        // get location of the coplanar control point
        Position coplanarPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 coplanarPoint = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition);

        // get location of the perpendicular control point
        Position perpendicularPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 perpendicularPoint = this.wwd.getModel().getGlobe().computePointFromPosition(perpendicularPosition);

        // create control plane
        Plane controlPlane = Plane.fromPoints(referencePoint, controlPoint, coplanarPoint);

        // get mouse click intersections with control point plane
        Vec4 pointOnPlane = controlPlane.intersect(screenRay);
        Vec4 previousPointOnPlane = controlPlane.intersect(previousScreenRay);

        // compute planar vectors of these intersection points
        Vec4 vectorOnPlane = pointOnPlane.subtract3(referencePoint);
        Vec4 previousVectorOnPlane = previousPointOnPlane.subtract3(referencePoint);
        Vec4 perpendicularVector = perpendicularPoint.subtract3(referencePoint);

        // TESTING
        //Position planePos = this.wwd.getModel().getGlobe().computePositionFromPoint(pointOnPlane);
        //Position prevPlanePos = this.wwd.getModel().getGlobe().computePositionFromPoint(previousPointOnPlane);
        //this.tempPath = new Path(perpendicularPosition, referencePos);
        //this.tempPath2 = new Path(planePos, referencePos);
        //this.tempPath3 = new Path(prevPlanePos, referencePos);

        RigidShape shape = this.getShape();

        // compute angle between them
        Angle rotationChange = vectorOnPlane.angleBetween3(previousVectorOnPlane);
        if (vectorOnPlane.cross3(previousVectorOnPlane).dot3(perpendicularVector) < 0)
            rotationChange = Angle.fromRadians(rotationChange.getRadians() * -1);

        // XYZ version
        //Matrix M = Matrix.fromRotationXYZ(shape.getTilt(), shape.getRoll(), shape.getHeading());

        // KML version (YXZ):
        Angle heading = this.shape.getHeading().multiply(-1); // must convert CW rotations to CCW for
        Angle tilt = this.shape.getTilt().multiply(-1);       // compatibility with Matrix class rotation methods
        Angle roll = this.shape.getRoll().multiply(-1);

        Matrix M = Matrix.IDENTITY;
        // roll
        if (roll != null)
            M = M.multiply(Matrix.fromRotationY(roll));
        // tilt
        if (tilt != null)
            M = M.multiply(Matrix.fromRotationX(tilt));
        // heading
        if (heading != null)
            M = M.multiply(Matrix.fromRotationZ(heading));

        Vec4 unitVec = new Vec4(0, 0, -1);                          // use -1 so rotation will be CW
        Vec4 rotVector = unitVec.transformBy4(M).normalize3();      // this is what we will rotate around
        Matrix M2 = Matrix.fromAxisAngle(rotationChange, rotVector);
        Matrix M3 = M2.multiply(M);

        this.shape.setHeading(M3.getKMLRotationZ());
        this.shape.setRoll(M3.getKMLRotationY());
        this.shape.setTilt(M3.getKMLRotationX());
    }

    protected void changeShapeRoll(Point previousMousePoint, Point mousePoint)
    {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        // create rays from mouse click position (current and previous)
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        // get location of the control point
        Position controlPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);

        // get location of the coplanar control point
        Position coplanarPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 coplanarPoint = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition);

        // get location of the perpendicular control point
        Position perpendicularPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 perpendicularPoint = this.wwd.getModel().getGlobe().computePointFromPosition(perpendicularPosition);

        // create control plane
        Plane controlPlane = Plane.fromPoints(referencePoint, controlPoint, coplanarPoint);

        // get mouse click intersections with control point plane
        Vec4 pointOnPlane = controlPlane.intersect(screenRay);
        Vec4 previousPointOnPlane = controlPlane.intersect(previousScreenRay);

        // compute planar vectors of these intersection points
        Vec4 vectorOnPlane = pointOnPlane.subtract3(referencePoint);
        Vec4 previousVectorOnPlane = previousPointOnPlane.subtract3(referencePoint);
        Vec4 perpendicularVector = perpendicularPoint.subtract3(referencePoint);

        // TESTING
        //Position planePos = this.wwd.getModel().getGlobe().computePositionFromPoint(pointOnPlane);
        //Position prevPlanePos = this.wwd.getModel().getGlobe().computePositionFromPoint(previousPointOnPlane);
        //this.tempPath = new Path(perpendicularPosition, referencePos);
        //this.tempPath2 = new Path(planePos, referencePos);
        //this.tempPath3 = new Path(prevPlanePos, referencePos);

        RigidShape shape = this.getShape();

        // compute angle between them
        Angle rotationChange = vectorOnPlane.angleBetween3(previousVectorOnPlane);
        if (vectorOnPlane.cross3(previousVectorOnPlane).dot3(perpendicularVector) < 0)
            rotationChange = Angle.fromRadians(rotationChange.getRadians() * -1);

        // XYZ version:
        //Matrix M = Matrix.fromRotationXYZ(shape.getTilt(), shape.getRoll(), shape.getHeading());

        // KML version (YXZ):
        Angle heading = this.shape.getHeading().multiply(-1);      // must convert CW rotations to CCW
        Angle tilt = this.shape.getTilt().multiply(-1);            // for use with Matrix class rotation methods
        Angle roll = this.shape.getRoll().multiply(-1);

        Matrix M = Matrix.IDENTITY;
        // roll
        if (roll != null)
            M = M.multiply(Matrix.fromRotationY(roll));
        // tilt
        if (tilt != null)
            M = M.multiply(Matrix.fromRotationX(tilt));
        // heading
        if (heading != null)
            M = M.multiply(Matrix.fromRotationZ(heading));

        Vec4 unitVec = new Vec4(0, -1, 0);                          // use -1 so rotation will be CW
        Vec4 rotVector = unitVec.transformBy4(M).normalize3();      // this is what we will rotate around
        Matrix M2 = Matrix.fromAxisAngle(rotationChange, rotVector);
        Matrix M3 = M2.multiply(M);

        this.shape.setHeading(M3.getKMLRotationZ());
        this.shape.setRoll(M3.getKMLRotationY());
        this.shape.setTilt(M3.getKMLRotationX());
    }

    protected void changeShapeTilt(Point previousMousePoint, Point mousePoint)
    {
        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        // create rays from mouse position (current and previous)
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        // get location of the control point
        Position controlPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);

        // get location of the coplanar control point
        Position coplanarPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 coplanarPoint = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition);

        // get location of the perpendicular control point
        Position perpendicularPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 perpendicularPoint = this.wwd.getModel().getGlobe().computePointFromPosition(perpendicularPosition);

        // create control plane
        Plane controlPlane = Plane.fromPoints(referencePoint, controlPoint, coplanarPoint);

        // get mouse click intersections with control point plane
        Vec4 pointOnPlane = controlPlane.intersect(screenRay);
        Vec4 previousPointOnPlane = controlPlane.intersect(previousScreenRay);

        // compute planar vectors of these intersection points
        Vec4 vectorOnPlane = pointOnPlane.subtract3(referencePoint);
        Vec4 previousVectorOnPlane = previousPointOnPlane.subtract3(referencePoint);
        Vec4 perpendicularVector = perpendicularPoint.subtract3(referencePoint);

        // TESTING
        //Position planePos = this.wwd.getModel().getGlobe().computePositionFromPoint(pointOnPlane);
        //Position prevPlanePos = this.wwd.getModel().getGlobe().computePositionFromPoint(previousPointOnPlane);
        //this.tempPath = new Path(perpendicularPosition, referencePos);
        //this.tempPath2 = new Path(planePos, referencePos);
        //this.tempPath3 = new Path(prevPlanePos, referencePos);

        RigidShape shape = this.getShape();

        // compute angle between them
        Angle rotationChange = vectorOnPlane.angleBetween3(previousVectorOnPlane);
        if (vectorOnPlane.cross3(previousVectorOnPlane).dot3(perpendicularVector) < 0)
            rotationChange = Angle.fromRadians(rotationChange.getRadians() * -1);

        // XYZ version
        //Matrix M = Matrix.fromRotationXYZ(shape.getTilt(), shape.getRoll(), shape.getHeading());

        // KML version (YXZ):
        Angle heading = this.shape.getHeading().multiply(-1); // must convert CW rotations to CCW for
        Angle tilt = this.shape.getTilt().multiply(-1);       // compatibility with Matrix class rotation methods
        Angle roll = this.shape.getRoll().multiply(-1);

        Matrix M = Matrix.IDENTITY;
        // roll
        if (roll != null)
            M = M.multiply(Matrix.fromRotationY(roll));
        // tilt
        if (tilt != null)
            M = M.multiply(Matrix.fromRotationX(tilt));
        // heading
        if (heading != null)
            M = M.multiply(Matrix.fromRotationZ(heading));

        Vec4 unitVec = new Vec4(-1, 0, 0);                          // use -1 so rotation will be CW
        Vec4 rotVector = unitVec.transformBy4(M).normalize3();      // this is what we will rotate around
        Matrix M2 = Matrix.fromAxisAngle(rotationChange, rotVector);
        Matrix M3 = M2.multiply(M);

        this.shape.setHeading(M3.getKMLRotationZ());
        this.shape.setRoll(M3.getKMLRotationY());
        this.shape.setTilt(M3.getKMLRotationX());
    }

    protected void skewShapeEastWest(Point previousMousePoint, Point mousePoint)
    {
        RigidShape shape = this.getShape();
        double skew = shape.getSkewEastWest().getDegrees();

        double scale = ShapeUtils.getViewportScaleFactor(wwd);

        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        // get location of the control point
        Position controlPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 controlVector = controlPoint.subtract3(referencePoint).normalize3();

        // create north vector
        Position northPosition = this.controlPoints.get(3).getCenterPosition();
        Vec4 northPoint = this.wwd.getModel().getGlobe().computePointFromPosition(northPosition);
        Vec4 northVector = northPoint.subtract3(referencePoint).normalize3();

        // create front vector
        Position frontPosition = this.controlPoints.get(4).getCenterPosition();
        Vec4 frontPoint = this.wwd.getModel().getGlobe().computePointFromPosition(frontPosition);
        Vec4 frontVector = frontPoint.subtract3(referencePoint).normalize3();

        // get locations of 3 coplanar points
        Vec4 p1 = referencePoint.add3(controlVector.multiply3(this.shape.getEastWestRadius()));
        Vec4 p2 = p1.add3(northVector);
        Vec4 p3 = p1.add3(frontVector);

        // construct plane to determine skew direction
        Plane splitPlane = Plane.fromPoints(p1, p2, p3);

        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(p1);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(p1);

        double distance = nearestPointOnLine.distanceTo3(p1);
        double previousDistance = previousNearestPointOnLine.distanceTo3(p1);
        double skewChange = (distance - previousDistance) / scale;
        skewChange *= 1 - Math.abs(skew - 90) / 90;
        skewChange *= 50;

        // determine if mouse click is on same side of splitPlane as the referencePoint
        int west = splitPlane.onSameSide(referencePoint, nearestPointOnLine);
        if (west != 0)
            skewChange *= -1;

        if (skew + skewChange >= 0 && skew + skewChange < 180)
            this.shape.setSkewEastWest(Angle.fromDegrees(skew + skewChange));
    }

    protected void skewShapeNorthSouth(Point previousMousePoint, Point mousePoint)
    {
        RigidShape shape = this.getShape();
        double skew = shape.getSkewNorthSouth().getDegrees();
        double scale = ShapeUtils.getViewportScaleFactor(wwd);

        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        // get location of the control point
        Position controlPosition = this.controlPoints.get(3).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);
        Vec4 controlVector = controlPoint.subtract3(referencePoint).normalize3();

        // create east vector
        Position eastPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 eastPoint = this.wwd.getModel().getGlobe().computePointFromPosition(eastPosition);
        Vec4 eastVector = eastPoint.subtract3(referencePoint).normalize3();

        // create front vector
        Position frontPosition = this.controlPoints.get(4).getCenterPosition();
        Vec4 frontPoint = this.wwd.getModel().getGlobe().computePointFromPosition(frontPosition);
        Vec4 frontVector = frontPoint.subtract3(referencePoint).normalize3();

        // get locations of 3 coplanar points
        Vec4 p1 = referencePoint.add3(controlVector.multiply3(this.shape.getNorthSouthRadius()));
        Vec4 p2 = p1.add3(eastVector);
        Vec4 p3 = p1.add3(frontVector);

        // construct plane to determine skew direction
        Plane splitPlane = Plane.fromPoints(p1, p2, p3);

        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(p1);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(p1);

        double distance = nearestPointOnLine.distanceTo3(p1);
        double previousDistance = previousNearestPointOnLine.distanceTo3(p1);
        double skewChange = (distance - previousDistance) / scale;
        skewChange *= 1 - Math.abs(skew - 90) / 90;
        skewChange *= 50;

        // determine if mouse click is on same side of splitPlane as the referencePoint
        int south = splitPlane.onSameSide(referencePoint, nearestPointOnLine);
        if (south != 0)
            skewChange *= -1;

        if (skew + skewChange >= 0 && skew + skewChange < 180)
            this.shape.setSkewNorthSouth(Angle.fromDegrees(skew + skewChange));
    }

    protected void skewShape(Point previousMousePoint, Point mousePoint)
    {
        RigidShape shape = this.getShape();
        double eastSkew = shape.getSkewEastWest().getDegrees();
        double northSkew = shape.getSkewNorthSouth().getDegrees();

        double scale = ShapeUtils.getViewportScaleFactor(wwd);

        Position referencePos = this.shape.getReferencePosition();
        if (referencePos == null)
            return;

        Vec4 referencePoint = this.wwd.getModel().getGlobe().computePointFromPosition(referencePos);

        // create east vector
        Position eastPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 eastPoint = this.wwd.getModel().getGlobe().computePointFromPosition(eastPosition);
        Vec4 eastVector = eastPoint.subtract3(referencePoint).normalize3();

        // create north vector
        Position northPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 northPoint = this.wwd.getModel().getGlobe().computePointFromPosition(northPosition);
        Vec4 northVector = northPoint.subtract3(referencePoint).normalize3();

        // create rays from mouse position (current and previous)
        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Line previousScreenRay = this.wwd.getView().computeRayFromScreenPoint(previousMousePoint.getX(),
            previousMousePoint.getY());

        // get location of the control point
        Position controlPosition = this.controlPoints.get(2).getCenterPosition();
        Vec4 controlPoint = this.wwd.getModel().getGlobe().computePointFromPosition(controlPosition);

        // get location of the coplanar control point
        Position coplanarPosition = this.controlPoints.get(1).getCenterPosition();
        Vec4 coplanarPoint = this.wwd.getModel().getGlobe().computePointFromPosition(coplanarPosition);

        // get location of the perpendicular control point
        Position perpendicularPosition = this.controlPoints.get(0).getCenterPosition();
        Vec4 perpendicularPoint = this.wwd.getModel().getGlobe().computePointFromPosition(perpendicularPosition);

        // create control plane
        Plane controlPlane = Plane.fromPoints(referencePoint, controlPoint, coplanarPoint);

        // get mouse click intersections with control point plane
        Vec4 pointOnPlane = controlPlane.intersect(screenRay);
        Vec4 previousPointOnPlane = controlPlane.intersect(previousScreenRay);

        // compute planar vector between these intersection points
        Vec4 vectorOnPlane = pointOnPlane.subtract3(previousPointOnPlane);
        Vec4 perpendicularVector = perpendicularPoint.subtract3(referencePoint);

        // compute east-west and north-south components of the vectorOnPlane
        double eastProjection = vectorOnPlane.dot3(eastVector);
        double eastSkewChange = eastProjection / scale;
        eastSkewChange *= 1 - Math.abs(eastSkew - 90) / 90;
        eastSkewChange *= 50;

        double northProjection = vectorOnPlane.dot3(northVector);
        double northSkewChange = northProjection / scale;
        northSkewChange *= 1 - Math.abs(northSkew - 90) / 90;
        northSkewChange *= 50;

        if (eastSkew + eastSkewChange >= 0 && eastSkew + eastSkewChange < 180)
            this.shape.setSkewEastWest(Angle.fromDegrees(eastSkew + eastSkewChange));
        if (northSkew + northSkewChange >= 0 && northSkew + northSkewChange < 180)
            this.shape.setSkewNorthSouth(Angle.fromDegrees(northSkew + northSkewChange));

/*
        // get location of the control point
        Vec4 controlVector = controlPoint.subtract3(referencePoint).normalize3();

        // create front vector
        Position frontPosition = this.controlPoints.get(4).getCenterPosition();
        Vec4 frontPoint = this.wwd.getModel().getGlobe().computePointFromPosition(frontPosition);
        Vec4 frontVector = frontPoint.subtract3(referencePoint).normalize3();

        // get locations of 3 coplanar points
        Vec4 p1 = referencePoint.add3(controlVector.multiply3(this.shape.getEastWestRadius()));
        Vec4 p2 = p1.add3(northVector);
        Vec4 p3 = p1.add3(frontVector);

        // construct plane to determine skew direction
        Plane splitPlane = Plane.fromPoints(p1, p2, p3);

        Vec4 nearestPointOnLine = screenRay.nearestPointTo(p1);
        Vec4 previousNearestPointOnLine = previousScreenRay.nearestPointTo(p1);

        double distance = nearestPointOnLine.distanceTo3(p1);
        double previousDistance = previousNearestPointOnLine.distanceTo3(p1);
        double skewChange = (distance - previousDistance) / scale;
        skewChange *= 1 - Math.abs(skew - 90) / 90;
        skewChange *= 50;

        // determine if mouse click is on same side of splitPlane as the referencePoint
        int west = splitPlane.onSameSide(referencePoint, nearestPointOnLine);
        if (west != 0)
            skewChange *= -1;

        if (skew + skewChange >= 0 && skew + skewChange < 180)
            this.shape.setSkewEastWest(Angle.fromDegrees(skew + skewChange));
*/
    }

    protected void moveTexture(Point previousMousePoint, Point mousePoint)
    {
    }

    protected void moveTextureCorner(Point previousMousePoint, Point mousePoint, Integer corner)
    {
    }

    protected void scaleTexture(Point previousMousePoint, Point mousePoint, Direction side)
    {
    }

    protected void textureShape(Point previousMousePoint, Point mousePoint)
    {
        RigidShape shape = this.getShape();
        int faces = shape.getFaceCount();
        Matrix renderMatrix = shape.computeRenderMatrix(this.getWorldWindow().getModel().getGlobe(), 0);

        Line screenRay = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());

        try
        {
            double closest = 1e10;
            Integer closestFace = null;
            java.util.List<Intersection> intersections;

            for (int i = 0; i < faces; i++)
            {
                intersections = shape.intersectFace(screenRay, i, renderMatrix);
                if (intersections != null && intersections.size() > 0)
                {
                    for (Intersection intersection : intersections)
                    {
                        double dist = this.wwd.getView().getEyePoint().distanceTo3(
                            intersection.getIntersectionPoint());
                        if (dist < closest)
                        {
                            closest = dist;
                            closestFace = i;
                        }
                    }
                }
            }

            // interpret gesture as "translate texture" if selected face is same as that previously selected
            if (closestFace != null && closestFace.equals(getSelectedFace()))
            {
                moveTexture(previousMousePoint, mousePoint);
            }
            else if (closestFace != null && closestFace >= 0)   // the closest intersected piece to the eye
            {
                setSelectedFace(closestFace);
            }
        }
        catch (InterruptedException e)
        {
            System.out.println("Operation was interrupted");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void moveControlPoint(ControlPointMarker controlPoint, Point moveToPoint)
    {
        /*
        Position newPosition = this.wwd.getView().computePositionFromScreenPoint(moveToPoint.x, moveToPoint.y);

        java.util.List<LatLon> boundary = new ArrayList<LatLon>();
        for (LatLon ll : this.shape.getOuterBoundary())
        {
            boundary.add(ll);
        }

        boundary.set(controlPoint.getIndex(), new LatLon(newPosition));

        // ExtrudedPolygon ensures that the last boundary position is the same as the first. Remove the last point
        // before setting the boundary.
        boundary.remove(boundary.size() - 1);

        this.shape.setOuterBoundary(boundary);
        */
    }

    /**
     * Add a vertex to the polygon's outer boundary.
     *
     * @param mousePoint the point at which the mouse was clicked. The new vertex will be placed as near as possible to
     *                   this point, at the elevation of the polygon.
     */
    protected void addVertex(Point mousePoint)
    {
        /*
        // Try to find the edge that is closest to a ray passing through the screen point. We're trying to determine
        // the user's intent as to which edge a new two control points should be added to.

        Line ray = this.wwd.getView().computeRayFromScreenPoint(mousePoint.getX(), mousePoint.getY());
        Vec4 pickPoint = this.intersectPolygonAltitudeAt(ray);

        double nearestDistance = Double.MAX_VALUE;
        int newVertexIndex = 0;

        // Loop through the control points and determine which edge is closest to the pick point
        for (int i = 0; i < this.controlPoints.size(); i++)
        {
            ControlPointMarker thisMarker = (ControlPointMarker) this.controlPoints.get(i);
            ControlPointMarker nextMarker = (ControlPointMarker) this.controlPoints.get(
                (i + 1) % this.controlPoints.size());

            Vec4 pointOnEdge = AirspaceEditorUtil.nearestPointOnSegment(thisMarker.point, nextMarker.point, pickPoint);
            if (!AirspaceEditorUtil.isPointBehindLineOrigin(ray, pointOnEdge))
            {
                double d = pointOnEdge.distanceTo3(pickPoint);
                if (d < nearestDistance)
                {
                    newVertexIndex = i + 1;
                    nearestDistance = d;
                }
            }
        }

        LatLon newLocation = new LatLon(this.wwd.getModel().getGlobe().computePositionFromPoint(pickPoint));

        // Copy the outer boundary list
        ArrayList<LatLon> locationList = new ArrayList<LatLon>(this.controlPoints.size());
        for (LatLon latLon : this.getShape().getOuterBoundary())
        {
            locationList.add(latLon);
        }

        // Add the new vertex
        locationList.add(newVertexIndex, newLocation);

        this.getShape().setOuterBoundary(locationList);
        */
    }

    /**
     * Remove a vertex from the polygon.
     *
     * @param vertexToRemove the vertex to remove.
     */
    protected void removeVertex(ControlPointMarker vertexToRemove)
    {
        /*
        RigidShape shape = this.getShape();
        ArrayList<LatLon> locations = new ArrayList<LatLon>(this.controlPoints.size() - 1);

        for (LatLon latLon : shape.getOuterBoundary())
        {
            locations.add(latLon);
        }
        locations.remove(vertexToRemove.getIndex());

        shape.setOuterBoundary(locations);
        */
    }

    /**
     * Determine the point at which a ray intersects a the globe at the elevation of the polygon.
     *
     * @param ray Ray to intersect with the globe.
     *
     * @return The point at which the ray intersects the globe at the elevation of the polygon.
     */
    protected Vec4 intersectPolygonAltitudeAt(Line ray)
    {
        //  If there are control points computed, use the elevation of the first control point as the polygon elevation.
        // Otherwise, if there are no control points, intersect the globe at sea level
        double elevation = 0.0;
        if (this.controlPoints.size() > 0)
        {
            elevation = this.controlPoints.get(0).getCenterPosition().getElevation();
        }
        return AirspaceEditorUtil.intersectGlobeAt(this.wwd, elevation, ray);
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mouseMoved(MouseEvent e)
    {

    }

    protected static class ControlPointMarker extends BasicMarker
    {
        protected int index;
        protected String type;
        protected Vec4 point;

        public ControlPointMarker(String type, Position position, Vec4 point, MarkerAttributes attrs, int index)
        {
            super(position, attrs);
            this.point = point;
            this.index = index;
            this.type = type;
        }

        public int getIndex()
        {
            return this.index;
        }

        public String getType()
        {
            return type;
        }

        public Vec4 getPoint()
        {
            return point;
        }
    }

    public void updateAnnotation(Position pos)
    {
        if (pos == null)
        {
            this.annotation.getAttributes().setVisible(false);
            return;
        }

        String displayString = this.getDisplayString(pos);

        if (displayString == null)
        {
            this.annotation.getAttributes().setVisible(false);
            return;
        }

        this.annotation.setText(displayString);

        Vec4 screenPoint = this.computeAnnotationPosition(pos);
        if (screenPoint != null)
            this.annotation.setScreenPoint(new Point((int) screenPoint.x, (int) screenPoint.y));

        this.annotation.getAttributes().setVisible(true);
    }

    protected Vec4 computeAnnotationPosition(Position pos)
    {
        Vec4 surfacePoint = this.wwd.getSceneController().getTerrain().getSurfacePoint(
            pos.getLatitude(), pos.getLongitude());
        if (surfacePoint == null)
        {
            Globe globe = this.wwd.getModel().getGlobe();
            surfacePoint = globe.computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                globe.getElevation(pos.getLatitude(), pos.getLongitude()));
        }

        return this.wwd.getView().project(surfacePoint);
    }

    protected String getDisplayString(Position pos)
    {
        String displayString = null;

        if (pos != null)
        {
            displayString = this.formatMeasurements(pos);
        }

        return displayString;
    }

    protected String formatMeasurements(Position pos)
    {
        StringBuilder sb = new StringBuilder();

        /*
        //sb.append(this.unitsFormat.areaNL(this.getLabel(AREA_LABEL), this.getArea()));
        sb.append(this.unitsFormat.lengthNL(this.getLabel(PERIMETER_LABEL), this.getLength()));
       */

        sb.append(this.unitsFormat.lengthNL(this.getLabel(WIDTH_LABEL), this.shape.getEastWestRadius() * 2));
        sb.append(this.unitsFormat.lengthNL(this.getLabel(LENGTH_LABEL), this.shape.getNorthSouthRadius() * 2));
        sb.append(this.unitsFormat.lengthNL(this.getLabel(HEIGHT_LABEL), this.shape.getVerticalRadius() * 2));

        sb.append(this.unitsFormat.angleNL(this.getLabel(HEADING_LABEL), this.shape.getHeading()));
        sb.append(this.unitsFormat.angleNL(this.getLabel(TILT_LABEL), this.shape.getTilt()));
        sb.append(this.unitsFormat.angleNL(this.getLabel(ROLL_LABEL), this.shape.getRoll()));

        sb.append(this.unitsFormat.angleNL(this.getLabel(EAST_SKEW_LABEL), this.shape.getSkewEastWest()));
        sb.append(this.unitsFormat.angleNL(this.getLabel(NORTH_SKEW_LABEL), this.shape.getSkewNorthSouth()));

        // if "activeControlPoint" is in fact one of the control points
        if (!this.arePositionsRedundant(pos, this.shape.getCenterPosition()))
        {
            sb.append(this.unitsFormat.angleNL(this.getLabel(LATITUDE_LABEL), pos.getLatitude()));
            sb.append(this.unitsFormat.angleNL(this.getLabel(LONGITUDE_LABEL), pos.getLongitude()));
            sb.append(this.unitsFormat.lengthNL(this.getLabel(ALTITUDE_LABEL), pos.getAltitude()));
        }

        // if "activeControlPoint" is the shape itself
        if (this.shape.getCenterPosition() != null)
        {
            sb.append(
                this.unitsFormat.angleNL(this.getLabel(CENTER_LATITUDE_LABEL),
                    this.shape.getCenterPosition().getLatitude()));
            sb.append(this.unitsFormat.angleNL(this.getLabel(CENTER_LONGITUDE_LABEL),
                this.shape.getCenterPosition().getLongitude()));
            sb.append(this.unitsFormat.lengthNL(this.getLabel(CENTER_ALTITUDE_LABEL),
                this.shape.getCenterPosition().getAltitude()));
        }

        return sb.toString();
    }
}

