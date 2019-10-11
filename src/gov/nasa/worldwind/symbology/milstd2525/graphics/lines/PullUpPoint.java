/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.AbstractCircularGraphic;

import java.util.*;

/**
 * Implementation of the Pull-Up Point (PUP) graphic (2.X.2.2.1.3).
 *
 * @author pabercrombie
 * @version $Id: PullUpPoint.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PullUpPoint extends AbstractCircularGraphic implements TacticalPoint, PreRenderable
{
    /** Default radius, in meters, for the circle. */
    public final static double DEFAULT_RADIUS = 1000.0;

    /** Path to draw the bowtie in the middle of the circle. */
    protected Path bowtie;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_AVN_PNT_PUP);
    }

    /**
     * Create a new point.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public PullUpPoint(String sidc)
    {
        super(sidc);
        this.setRadius(DEFAULT_RADIUS);
    }

    /** Invalidate the bowtie shape when the circle changes. */
    @Override
    protected void reset()
    {
        this.bowtie = null;
    }

    /** {@inheritDoc} Overridden to draw airfield graphic. */
    @Override
    protected void doRenderGraphic(DrawContext dc)
    {
        super.doRenderGraphic(dc);

        if (bowtie == null)
        {
            this.bowtie = this.createBowtie(dc);
        }

        this.bowtie.render(dc);
    }

    /**
     * Create a path to draw the bowtie graphic in the middle of the circle.
     *
     * @param dc Current draw context.
     *
     * @return Path for the bowtie.
     */
    protected Path createBowtie(DrawContext dc)
    {
        //  A     C
        //  |\  /|
        //  | \/ |
        //  | /\ |
        //  |/  \|
        // B      D

        LatLon center = this.circle.getCenter();

        double dist = this.circle.getRadius() * 0.75;
        Angle distance = Angle.fromRadians(dist / dc.getGlobe().getRadius());

        LatLon a = LatLon.greatCircleEndPosition(center, Angle.fromDegrees(-65.0), distance);
        LatLon b = LatLon.greatCircleEndPosition(center, Angle.fromDegrees(-115.0), distance);
        LatLon c = LatLon.greatCircleEndPosition(center, Angle.fromDegrees(65.0), distance);
        LatLon d = LatLon.greatCircleEndPosition(center, Angle.fromDegrees(115.0), distance);

        Path bowtie = this.createPath();
        bowtie.setPositions(this.asPositionList(a, b, c, d, a));

        return bowtie;
    }

    /**
     * Convert a list of LatLon to a list of Positions at zero elevation.
     *
     * @param locations Locations to convert to Positions.
     *
     * @return Position list. All elevations will be set to zero.
     */
    protected List<Position> asPositionList(LatLon... locations)
    {
        List<Position> positions = new ArrayList<Position>(locations.length);
        for (LatLon loc : locations)
        {
            positions.add(new Position(loc, 0));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    protected void createLabels()
    {
        TacticalGraphicLabel label = this.addLabel("PUP");
        label.setTextAlign(AVKey.LEFT);
    }

    /**
     * Determine the appropriate position for the graphic's labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        LatLon center = this.circle.getCenter();
        double distance = this.circle.getRadius() * 1.1; // Place the label just beyond the radius.
        Angle radius = Angle.fromRadians(distance / dc.getGlobe().getRadius());

        LatLon eastEdge = LatLon.greatCircleEndPosition(center, Angle.POS90 /* East */, radius);

        this.labels.get(0).setPosition(new Position(eastEdge, 0));
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @return New path configured with defaults appropriate for this type of graphic.
     */
    protected Path createPath()
    {
        Path path = new Path();
        path.setSurfacePath(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setDelegateOwner(this);
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
