/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of the Axis of Advance for Feint graphic (2.X.2.3.3).
 *
 * @author pabercrombie
 * @version $Id: AdvanceForFeint.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class AdvanceForFeint extends AbstractAxisArrow
{
    /**
     * Factor used to compute the distance between the solid and dashed lines in the arrow head. A larger value will
     * move the dashed line farther from the solid line.
     */
    protected static final double DASHED_LINE_DISTANCE = 0.2;

    /** Shape attributes for the dashed part of the graphic. */
    protected ShapeAttributes dashedAttributes = new BasicShapeAttributes();

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_DCPN_AAFF);
    }

    /**
     * Create a new graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public AdvanceForFeint(String sidc)
    {
        super(sidc, 2);

        // Path 1 is used to draw the dashed line. This path needs its own shape attributes because it is always drawn
        // dashed.
        this.paths[1].setAttributes(this.dashedAttributes);
    }

    /** {@inheritDoc} Overridden to compute positions for the dashed portion of the arrow head. */
    @Override
    protected double createArrowHeadPositions(List<Position> leftPositions, List<Position> rightPositions,
        List<Position> arrowHeadPositions, Globe globe)
    {
        // This graphic has a double line for the arrowhead, and the outer line is always drawn dashed. Take the
        // arrowhead computed by the super class and create the other line from it.
        double halfWidth = super.createArrowHeadPositions(leftPositions, rightPositions, arrowHeadPositions, globe);

        // The rest of the method assumes that the superclass populates arrowHeadPositions with three positions.
        if (arrowHeadPositions == null || arrowHeadPositions.size() != 3)
            throw new IllegalStateException();

        Iterator<? extends Position> positions = this.positions.iterator();
        Position pos1 = positions.next();
        Position pos2 = positions.next();

        Vec4 pt1 = globe.computePointFromPosition(pos1);
        Vec4 pt2 = globe.computePointFromPosition(pos2);

        // Superclass gives us list of {pt N, pt 1, pt N'} where pt N is the corner of the arrow head on one side of the
        // tip and pt N' is the corner on the other side. See superclass documentation for more details.
        Vec4 ptN = globe.computePointFromPosition(arrowHeadPositions.get(0));
        Vec4 ptN_prime = globe.computePointFromPosition(arrowHeadPositions.get(2));

        // Transform pt N and pt N' to find the ends of the dashed line.
        Vec4 dir = ptN.subtract3(ptN_prime).multiply3(DASHED_LINE_DISTANCE);
        ptN = ptN.add3(dir);
        ptN_prime = ptN_prime.subtract3(dir);

        dir = pt1.subtract3(ptN).multiply3(DASHED_LINE_DISTANCE);
        ptN = ptN.add3(dir);

        dir = pt1.subtract3(ptN_prime).multiply3(DASHED_LINE_DISTANCE);
        ptN_prime = ptN_prime.add3(dir);

        List<Position> newPositions = Arrays.asList(
            globe.computePositionFromPoint(ptN),
            pos1,
            globe.computePositionFromPoint(ptN_prime));

        this.paths[1].setPositions(new ArrayList<Position>(newPositions));

        // Control point 1 specifies the tip of the dashed line, so move the tip of the other arrow toward the body of
        // the graphic.
        double dist = dir.getLength3(); // Shift point the same distance as used above for other points.
        dir = pt2.subtract3(pt1);
        pt1 = pt1.add3(dir.normalize3().multiply3(dist));
        arrowHeadPositions.set(1, globe.computePositionFromPoint(pt1));

        return halfWidth;
    }

    /** Determine active attributes for this frame. */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // Copy active attributes to the dashed attribute bundle.
        this.dashedAttributes.copy(this.getActiveShapeAttributes());

        // Re-apply stipple pattern.
        this.dashedAttributes.setOutlineStipplePattern(this.getOutlineStipplePattern());
        this.dashedAttributes.setOutlineStippleFactor(this.getOutlineStippleFactor());
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        String text = this.getText();
        if (!WWUtil.isEmpty(text))
            this.addLabel(text);
    }

    /**
     * Determine positions for the start and end labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (WWUtil.isEmpty(this.labels))
            return;

        Iterator<? extends Position> positions = this.positions.iterator();
        Position pos1 = positions.next();
        Position pos2 = positions.next();

        // Place label 25% of the distance from point 2 to point 1.
        LatLon mid = LatLon.interpolate(0.25, pos2, pos1);

        this.labels.get(0).setPosition(new Position(mid, 0));
        this.labels.get(0).setOrientationPosition(pos1);
    }
}
