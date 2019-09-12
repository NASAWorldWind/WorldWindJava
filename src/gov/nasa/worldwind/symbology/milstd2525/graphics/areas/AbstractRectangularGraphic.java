/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Base class for rectangular area graphics.
 *
 * @author pabercrombie
 * @version $Id: AbstractRectangularGraphic.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AbstractRectangularGraphic extends AbstractMilStd2525TacticalGraphic implements TacticalQuad, PreRenderable
{
    protected Iterable<? extends Position> positions;
    protected SurfaceQuad quad;

    protected boolean shapeInvalid;

    /**
     * Create a new target.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public AbstractRectangularGraphic(String sidc)
    {
        super(sidc);
        this.quad = this.createShape();
    }

    /** {@inheritDoc} */
    public double getWidth()
    {
        return this.quad.getHeight();
    }

    /** {@inheritDoc} */
    public void setWidth(double width)
    {
        //noinspection SuspiciousNameCombination
        this.quad.setHeight(width);
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    public double getLength()
    {
        return this.quad.getWidth();
    }

    /** {@inheritDoc} */
    public void setLength(double length)
    {
        this.quad.setWidth(length);
        this.onModifierChanged();
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points. This graphic uses only two control point, which determine the midpoints of two
     *                  opposite sides of the quad. See Fire Support Area (2.X.4.3.2.1.2) on pg. 652 of MIL-STD-2525C
     *                  for an example of how these points are interpreted.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterator<? extends Position> iterator = positions.iterator();
        try
        {
            Position pos1 = iterator.next();
            Position pos2 = iterator.next();

            LatLon center = LatLon.interpolateGreatCircle(0.5, pos1, pos2);
            this.quad.setCenter(center);

            Angle heading = LatLon.greatCircleAzimuth(pos2, pos1);
            this.quad.setHeading(heading.subtract(Angle.POS90));

            this.positions = positions;
            this.shapeInvalid = true; // Need to recompute quad size
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return this.positions;
    }

    /** {@inheritDoc} */
    @Override
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.DISTANCE.equalsIgnoreCase(modifier))
        {
            if (value instanceof Double)
            {
                this.setWidth((Double) value);
            }
            else if (value instanceof Iterable)
            {
                // Only use the first value of the iterable. This graphic uses two control points and a width.
                Iterator iterator = ((Iterable) value).iterator();
                this.setWidth((Double) iterator.next());
            }
        }
        else
        {
            super.setModifier(modifier, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getModifier(String modifier)
    {
        if (SymbologyConstants.DISTANCE.equalsIgnoreCase(modifier))
            return this.getWidth();
        else
            return super.getModifier(modifier);
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.quad.getReferencePosition();
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        if (this.shapeInvalid)
        {
            this.computeQuadSize(dc);
            this.shapeInvalid = false;
        }

        this.determineActiveAttributes();
        this.quad.preRender(dc);
    }

    protected void computeQuadSize(DrawContext dc)
    {
        if (this.positions == null)
            return;

        Iterator<? extends Position> iterator = this.positions.iterator();

        Position pos1 = iterator.next();
        Position pos2 = iterator.next();

        Angle angularDistance = LatLon.greatCircleDistance(pos1, pos2);
        double length = angularDistance.radians * dc.getGlobe().getRadius();

        this.quad.setWidth(length);
    }

    /**
     * Render the quad.
     *
     * @param dc Current draw context.
     */
    protected void doRenderGraphic(DrawContext dc)
    {
        this.quad.render(dc);
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        this.quad.setDelegateOwner(owner);
    }

    protected SurfaceQuad createShape()
    {
        SurfaceQuad quad = new SurfaceQuad();
        quad.setDelegateOwner(this.getActiveDelegateOwner());
        quad.setAttributes(this.getActiveShapeAttributes());
        return quad;
    }
}