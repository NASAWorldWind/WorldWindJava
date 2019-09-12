/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Support By Fire Position graphic (2.X.2.5.3.4).
 *
 * @author pabercrombie
 * @version $Id: SupportByFirePosition.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class SupportByFirePosition extends AttackByFirePosition
{
    /** Fourth control point. */
    protected Position position4;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_ARS_SFP);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public SupportByFirePosition(String sidc)
    {
        super(sidc);
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points that orient the graphic. Must provide at least four points.
     */
    @Override
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            Iterator<? extends Position> iterator = positions.iterator();
            this.position1 = iterator.next();
            this.position2 = iterator.next();
            this.position3 = iterator.next();
            this.position4 = iterator.next();
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.paths = null; // Need to recompute path for the new control points
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(this.position1, this.position2, this.position3, this.position4);
    }

    /**
     * Create the paths necessary to draw the graphic.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void createShapes(DrawContext dc)
    {
        this.paths = new Path[5];

        // Create a path for the line parts of the arrows
        this.paths[0] = this.createPath(Arrays.asList(this.position1, this.position3));
        this.paths[1] = this.createPath(Arrays.asList(this.position2, this.position4));

        // Create the arrowheads
        List<Position> positions = this.computeArrowheadPositions(dc, this.position1, this.position3);
        this.paths[2] = createPath(positions);
        positions = this.computeArrowheadPositions(dc, this.position2, this.position4);
        this.paths[3] = createPath(positions);

        // Create the trapezoid base
        positions = this.computeBasePositions(dc, this.position1, this.position2, this.position3);
        this.paths[4] = createPath(positions);
    }
}
