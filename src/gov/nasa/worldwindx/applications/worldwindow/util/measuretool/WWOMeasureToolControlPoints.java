/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.util.measuretool;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: WWOMeasureToolControlPoints.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWOMeasureToolControlPoints implements WWOMeasureTool.ControlPointList, Renderable
{
    public class ControlPoint extends GlobeAnnotation implements WWOMeasureTool.ControlPoint
    {
        public ControlPoint(Position position)
        {
            super("", position, WWOMeasureToolControlPoints.this.controlPointAttributes);
        }

        public WWOMeasureTool getParent()
        {
            return WWOMeasureToolControlPoints.this.measureTool;
        }

        @Override
        public void setPosition(Position position)
        {
            super.setPosition(position);
        }

        @Override
        public Position getPosition()
        {
            return super.getPosition();
        }

        public void highlight(boolean tf)
        {
            this.getAttributes().setHighlighted(tf);
            this.getAttributes().setBackgroundColor(tf ? this.getAttributes().getTextColor() : null);
        }

        @Override
        public Object setValue(String key, Object value)
        {
            return super.setValue(key, value);
        }

        @Override
        public Object getValue(String key)
        {
            return super.getValue(key);
        }
    }

    protected WWOMeasureTool measureTool;
    protected ArrayList<ControlPoint> points = new ArrayList<ControlPoint>();
    protected AnnotationAttributes controlPointAttributes;

    public WWOMeasureToolControlPoints(WWOMeasureTool measureTool)
    {
        this.measureTool = measureTool;

        this.controlPointAttributes = new AnnotationAttributes();
        // Define an 8x8 square centered on the screen point
        this.controlPointAttributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        this.controlPointAttributes.setLeader(AVKey.SHAPE_NONE);
        this.controlPointAttributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        this.controlPointAttributes.setSize(new Dimension(8, 8));
        this.controlPointAttributes.setDrawOffset(new Point(0, -4));
        this.controlPointAttributes.setInsets(new Insets(0, 0, 0, 0));
        this.controlPointAttributes.setBorderWidth(0);
        this.controlPointAttributes.setCornerRadius(0);
        this.controlPointAttributes.setBackgroundColor(Color.BLUE);    // Normal color
        this.controlPointAttributes.setTextColor(Color.GREEN);         // Highlighted color
        this.controlPointAttributes.setHighlightScale(1.2);
        this.controlPointAttributes.setDistanceMaxScale(1);            // No distance scaling
        this.controlPointAttributes.setDistanceMinScale(1);
        this.controlPointAttributes.setDistanceMinOpacity(1);
    }

    public void addToLayer(RenderableLayer layer)
    {
        layer.addRenderable(this);
    }

    public void removeFromLayer(RenderableLayer layer)
    {
        layer.removeRenderable(this);
    }

    public int size()
    {
        return this.points.size();
    }

    public WWOMeasureTool.ControlPoint createControlPoint(Position position)
    {
        return new ControlPoint(position);
    }

    public WWOMeasureTool.ControlPoint get(int index)
    {
        return this.points.get(index);
    }

    public void add(WWOMeasureTool.ControlPoint controlPoint)
    {
        this.points.add((ControlPoint) controlPoint);
    }

    public void remove(WWOMeasureTool.ControlPoint controlPoint)
    {
        this.points.remove((ControlPoint) controlPoint);
    }

    public void remove(int index)
    {
        this.points.remove(index);
    }

    public void clear()
    {
        this.points.clear();
    }

    public void render(DrawContext dc)
    {
        for (ControlPoint cp : this.points)
        {
            cp.render(dc);
        }
    }
}
