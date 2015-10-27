/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;

/**
 * @author tag
 * @version $Id: BasicMarkerAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicMarkerAttributes implements MarkerAttributes
{
    private Material material = Material.WHITE;
    private Material headingMaterial = Material.RED;
    protected double headingScale = 3;
    private String shapeType = BasicMarkerShape.SPHERE;
    private double opacity = 1d;
    private double markerPixels = 8d;
    private double minMarkerSize = 3d;
    private double maxMarkerSize = Double.MAX_VALUE;

    public BasicMarkerAttributes()
    {
    }

    public BasicMarkerAttributes(Material material, String shapeType, double opacity)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (shapeType == null)
        {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.material = material;
        this.shapeType = shapeType;
        this.opacity = opacity;
    }

    public BasicMarkerAttributes(Material material, String shapeType, double opacity, double markerPixels,
        double minMarkerSize)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (shapeType == null)
        {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (opacity < 0)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (markerPixels < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", markerPixels);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (minMarkerSize < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", minMarkerSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.material = material;
        this.shapeType = shapeType;
        this.opacity = opacity;
        this.markerPixels = markerPixels;
        this.minMarkerSize = minMarkerSize;
    }

    public BasicMarkerAttributes(Material material, String shapeType, double opacity, double markerPixels,
        double minMarkerSize, double maxMarkerSize)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (shapeType == null)
        {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (opacity < 0)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (markerPixels < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", markerPixels);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (minMarkerSize < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", minMarkerSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (maxMarkerSize < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", maxMarkerSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.material = material;
        this.shapeType = shapeType;
        this.opacity = opacity;
        this.markerPixels = markerPixels;
        this.minMarkerSize = minMarkerSize;
        this.maxMarkerSize = maxMarkerSize;
    }

    public BasicMarkerAttributes(BasicMarkerAttributes that)
    {
        if (that == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.material = that.material;
        this.headingMaterial = that.headingMaterial;
        this.headingScale = that.headingScale;
        this.shapeType = that.shapeType;
        this.opacity = that.opacity;
        this.markerPixels = that.markerPixels;
        this.minMarkerSize = that.minMarkerSize;
        this.maxMarkerSize = that.maxMarkerSize;
    }

    public Material getMaterial()
    {
        return material;
    }

    public void setMaterial(Material material)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.material = material;
    }

    public Material getHeadingMaterial()
    {
        return headingMaterial;
    }

    public void setHeadingMaterial(Material headingMaterial)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.headingMaterial = headingMaterial;
    }

    public double getHeadingScale()
    {
        return headingScale;
    }

    public void setHeadingScale(double headingScale)
    {
        if (headingScale < 0)
        {
            String message = Logging.getMessage("generic.ScaleOutOfRange", headingScale);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.headingScale = headingScale;
    }

    public String getShapeType()
    {
        return shapeType;
    }

    public void setShapeType(String shapeType)
    {
        if (shapeType == null)
        {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.shapeType = shapeType;
    }

    public MarkerShape getShape(DrawContext dc)
    {
        MarkerShape shape = (MarkerShape) dc.getValue(this.shapeType);

        if (shape == null)
        {
            shape = BasicMarkerShape.createShapeInstance(this.shapeType);
            dc.setValue(this.shapeType, shape);
        }

        return shape;
    }

    public double getOpacity()
    {
        return opacity;
    }

    public void setOpacity(double opacity)
    {
        if (opacity < 0)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.opacity = opacity;
    }

    public double getMarkerPixels()
    {
        return markerPixels;
    }

    public void setMarkerPixels(double markerPixels)
    {
        if (markerPixels < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", markerPixels);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.markerPixels = markerPixels;
    }

    public double getMinMarkerSize()
    {
        return minMarkerSize;
    }

    public void setMinMarkerSize(double minMarkerSize)
    {
        if (minMarkerSize < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", minMarkerSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minMarkerSize = minMarkerSize;
    }

    public double getMaxMarkerSize()
    {
        return maxMarkerSize;
    }

    public void setMaxMarkerSize(double markerSize)
    {
        if (markerSize < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", markerSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxMarkerSize = markerSize;
    }

    public void apply(DrawContext dc)
    {
        if (!dc.isPickingMode() && this.material != null)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (this.opacity < 1)
                this.material.apply(gl, GL2.GL_FRONT, (float) this.opacity);
            else
                this.material.apply(gl, GL2.GL_FRONT);
        }
    }
}
