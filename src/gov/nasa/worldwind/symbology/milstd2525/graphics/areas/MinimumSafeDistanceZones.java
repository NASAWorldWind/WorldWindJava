/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Minimum Safe Distance Zone graphic (2.X.3.4.1). This graphic requires four control points. The
 * first point defines the center of the circle, and the other three define the radius of the rings. See the graphic
 * template in MIL-STD-2525C, pg. 613. Note that MIL-STD-2525C numbers the radius points as points one, two, and three,
 * but does not assign a number to the center point. This implementation requires the center point to be the first point
 * in the list.
 *
 * @author pabercrombie
 * @version $Id: MinimumSafeDistanceZones.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MinimumSafeDistanceZones extends AbstractMilStd2525TacticalGraphic implements PreRenderable
{
    /**
     * Default angle used to position the graphic's labels. This default angle (60 degrees) is chosen to match the
     * graphic template defined by MIL-STD-2525C, pg. 613.
     */
    public final static Angle DEFAULT_LABEL_ANGLE = Angle.fromDegrees(60.0);

    /** Position of the center of the range fan. */
    protected Iterable<? extends Position> positions;
    /** Rings that make up the range fan. */
    protected List<SurfaceCircle> rings;

    /** Position the labels along a line radiating out from the center of the circle at this angle from North. */
    protected Angle labelAngle = DEFAULT_LABEL_ANGLE;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.MOBSU_CBRN_MSDZ);
    }

    /**
     * Create the graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public MinimumSafeDistanceZones(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates the angle used to position this graphic's labels. The labels are positioned along a line radiating out
     * from the center of the circle, and at this bearing from North.
     *
     * @return The angle used to position this graphic's labels.
     */
    public Angle getLabelAngle()
    {
        return this.labelAngle;
    }

    /**
     * Specifies the angle used to position this graphic's labels. The labels are positioned along a line radiating out
     * from the center of the circle, and at this bearing from North.
     *
     * @param angle The angle used to position this graphic's labels.
     */
    public void setLabelAngle(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.labelAngle = angle;
    }

    /**
     * Indicates the center position of the range ran.
     *
     * @return The range fan center position.
     */
    public Position getPosition()
    {
        return this.getReferencePosition();
    }

    /**
     * Specifies the center position of the range ran.
     *
     * @param position The new center position.
     */
    public void setPosition(Position position)
    {
        this.moveTo(position);
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points. This graphic uses only one control point, which determines the center of the
     *                  circle.
     */
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
            // Ensure that the iterable provides at least four positions.
            Iterator<? extends Position> iterator = positions.iterator();
            for (int i = 0; i < 3; i++)
            {
                iterator.next();
            }
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.positions = positions;
        this.rings = null;
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return this.positions;
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        if (positions != null)
        {
            Iterator<? extends Position> iterator = this.positions.iterator();
            if (iterator.hasNext())
                return iterator.next();
        }
        return null;
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        if (this.rings == null)
        {
            this.createShapes(dc);
        }

        this.determineActiveAttributes();

        for (SurfaceCircle ring : this.rings)
        {
            ring.preRender(dc);
        }
    }

    /**
     * Render the polygon.
     *
     * @param dc Current draw context.
     */
    protected void doRenderGraphic(DrawContext dc)
    {
        for (SurfaceCircle ring : this.rings)
        {
            ring.render(dc);
        }
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        if (this.rings == null)
            return;

        for (SurfaceCircle ring : this.rings)
        {
            ring.setDelegateOwner(owner);
        }
    }

    /**
     * Create the circles used to draw this graphic.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        if (this.positions == null)
            return;

        this.rings = new ArrayList<SurfaceCircle>();

        Iterator<? extends Position> iterator = this.positions.iterator();

        Position center = iterator.next();
        double globeRadius = dc.getGlobe().getRadius();

        while (iterator.hasNext())
        {
            SurfaceCircle ring = this.createCircle();
            ring.setCenter(center);

            Position pos = iterator.next();
            Angle radius = LatLon.greatCircleDistance(center, pos);

            double radiusMeters = radius.radians * globeRadius;
            ring.setRadius(radiusMeters);

            this.rings.add(ring);
        }
    }

    /** Create labels for each ring. */
    @Override
    protected void createLabels()
    {
        for (int i = 1; i <= this.rings.size(); i++)
        {
            this.addLabel(String.valueOf(i));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        Position center = this.getReferencePosition();
        if (center == null)
            return;

        // Position the labels along a line radiating out from the center of the circle. The angle (60 degrees) is
        // chosen to match the graphic template defined by MIL-STD-2525C, pg. 613.
        double globeRadius = dc.getGlobe().getRadius();

        Angle labelAngle = this.getLabelAngle();

        int i = 0;
        for (SurfaceCircle ring : this.rings)
        {
            double radius = ring.getRadius();

            LatLon ll = LatLon.greatCircleEndPosition(center, labelAngle.radians, radius / globeRadius);

            this.labels.get(i).setPosition(new Position(ll, 0));
            i += 1;
        }
    }

    /**
     * Create a circle for a range ring.
     *
     * @return New circle.
     */
    protected SurfaceCircle createCircle()
    {
        SurfaceCircle circle = new SurfaceCircle();
        circle.setDelegateOwner(this.getActiveDelegateOwner());
        circle.setAttributes(this.getActiveShapeAttributes());
        return circle;
    }
}
