/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: RandomShapeAttributes.java 2326 2014-09-17 22:35:45Z dcollins $
 */
public class RandomShapeAttributes
{
    protected int attrIndex = -1;
    protected PointPlacemarkAttributes[] pointAttrs;
    protected ShapeAttributes[] shapeAttrs;
    protected AirspaceAttributes[] airspaceAttrs;

    public RandomShapeAttributes()
    {
        this.initialize();
    }

    protected void initialize()
    {
        Color[] shapeColors = {
            new Color(255, 9, 84), // red
            new Color(255, 133, 0), // orange
            new Color(255, 198, 0), // yellow
            new Color(79, 213, 33), // green
            new Color(7, 152, 249), // blue
            new Color(193, 83, 220), // purple
        };

        this.pointAttrs = new PointPlacemarkAttributes[shapeColors.length];
        this.shapeAttrs = new ShapeAttributes[shapeColors.length];
        this.airspaceAttrs = new AirspaceAttributes[shapeColors.length];

        for (int i = 0; i < shapeColors.length; i++)
        {
            this.pointAttrs[i] = this.createPointAttributes(shapeColors[i]);
            this.shapeAttrs[i] = this.createShapeAttributes(shapeColors[i]);
            this.airspaceAttrs[i] = this.createAirspaceAttributes(shapeColors[i]);
        }
    }

    public RandomShapeAttributes nextAttributes()
    {
        this.attrIndex++;

        return this;
    }

    public PointPlacemarkAttributes asPointAttributes()
    {
        return this.pointAttrs[this.attrIndex % this.pointAttrs.length];
    }

    public ShapeAttributes asShapeAttributes()
    {
        return this.shapeAttrs[this.attrIndex % this.shapeAttrs.length];
    }

    public AirspaceAttributes asAirspaceAttributes()
    {
        return this.airspaceAttrs[this.attrIndex % this.airspaceAttrs.length];
    }

    protected PointPlacemarkAttributes createPointAttributes(Color color)
    {
        PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
        attrs.setUsePointAsDefaultImage(true);
        attrs.setLineMaterial(new Material(color));
        attrs.setScale(7d);
        return attrs;
    }

    protected ShapeAttributes createShapeAttributes(Color color)
    {
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setInteriorMaterial(new Material(color));
        attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(color)));
        attrs.setInteriorOpacity(0.5);
        attrs.setOutlineWidth(2);
        return attrs;
    }

    protected AirspaceAttributes createAirspaceAttributes(Color color)
    {
        AirspaceAttributes attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(new Material(color));
        attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(color)));
        attrs.setInteriorOpacity(0.7);
        attrs.setOutlineWidth(2);
        attrs.setDrawOutline(true);
        attrs.setEnableAntialiasing(true);
        attrs.setEnableLighting(true);
        return attrs;
    }
}
