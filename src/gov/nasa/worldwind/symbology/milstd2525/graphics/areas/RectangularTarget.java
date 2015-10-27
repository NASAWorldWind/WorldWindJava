/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Implementation of the Rectangular Target graphic (hierarchy 2.X.4.3.1.1, SIDC: G*FPATR---****X).
 *
 * @author pabercrombie
 * @version $Id: RectangularTarget.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RectangularTarget extends AbstractRectangularGraphic
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_ARATGT_RTGTGT);
    }

    /**
     * Create a new target.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public RectangularTarget(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates this shape's heading, its rotation clockwise from north. Calling this method is equivalent to calling
     * <code>getModifier(SymbologyConstants.AZIMUTH)</code>.
     *
     * @return this shape's heading, or null if no heading has been specified.
     */
    public Angle getHeading()
    {
        return this.quad.getHeading();
    }

    /**
     * Specifies this shape's heading, its rotation clockwise from north. Calling this method is equivalent to calling
     * <code>setModifier(SymbologyConstants.AZIMUTH, heading)</code>.
     *
     * @param heading this shape's heading.
     */
    public void setHeading(Angle heading)
    {
        this.quad.setHeading(heading);
        this.onModifierChanged();
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points. This graphic uses only one control point, which determines the center of the
     *                  quad.
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
        if (!iterator.hasNext())
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.quad.setCenter(iterator.next());
    }

    /** {@inheritDoc} */
    @Override
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.DISTANCE.equals(modifier) && (value instanceof Iterable))
        {
            Iterator iterator = ((Iterable) value).iterator();
            this.setWidth((Double) iterator.next());
            this.setLength((Double) iterator.next());
        }
        else if (SymbologyConstants.AZIMUTH.equals(modifier) && (value instanceof Angle))
        {
            this.setHeading((Angle) value);
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
        if (SymbologyConstants.DISTANCE.equals(modifier))
            return Arrays.asList(this.getWidth(), this.getLength());
        else if (SymbologyConstants.AZIMUTH.equals(modifier))
            return this.quad.getHeading();
        else
            return super.getModifier(modifier);
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(new Position(this.quad.getCenter(), 0));
    }

    /** Create labels for the graphic. */
    @Override
    protected void createLabels()
    {
        String text = this.getText();
        if (!WWUtil.isEmpty(text))
        {
            this.addLabel(this.getText());
        }
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (!WWUtil.isEmpty(this.labels))
        {
            this.labels.get(0).setPosition(new Position(this.quad.getCenter(), 0));
        }
    }
}
