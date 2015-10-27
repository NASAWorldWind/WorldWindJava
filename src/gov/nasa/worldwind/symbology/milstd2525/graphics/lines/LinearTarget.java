/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Implementation of Linear Target graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Linear Target (2.X.4.2.1)</li> <li>Linear Smoke Target (2.X.4.2.1.1)</li> <li>Final Protective Fire (FPF)
 * (2.X.4.2.1.2)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: LinearTarget.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LinearTarget extends AbstractMilStd2525TacticalGraphic
{
    /** Default length of the arrowhead, as a fraction of the total line length. */
    public final static double DEFAULT_VERTICAL_LENGTH = 0.25;
    /** Length of the vertical segments, as a fraction of the horizontal segment. */
    protected double verticalLength = DEFAULT_VERTICAL_LENGTH;

    /**
     * Offset applied to the graphic's upper label. This offset aligns the bottom edge of the label with the geographic
     * position, in order to keep the label above the graphic as the zoom changes.
     */
    protected final static Offset TOP_LABEL_OFFSET = new Offset(0.0, -1.0, AVKey.FRACTION, AVKey.FRACTION);
    /**
     * Offset applied to the graphic's lower label. This offset aligns the top edge of the label with the geographic
     * position, in order to keep the label above the graphic as the zoom changes.
     */
    protected final static Offset BOTTOM_LABEL_OFFSET = new Offset(0.0, 0.0, AVKey.FRACTION, AVKey.FRACTION);

    /** First control point. */
    protected Position startPosition;
    /** Second control point. */
    protected Position endPosition;

    /**
     * The value of an optional second text string for the graphic. This value is equivalent to the "T1" modifier
     * defined by MIL-STD-2525C. It can be set using {@link #setAdditionalText(String)}, or by passing an Iterable to
     * {@link #setModifier(String, Object)} with a key of {@link SymbologyConstants#UNIQUE_DESIGNATION} (additional text
     * is the second value in the iterable).
     */
    protected String additionalText;

    /** Paths used to render the graphic. */
    protected Path[] paths;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.FSUPP_LNE_LNRTGT,
            TacGrpSidc.FSUPP_LNE_LNRTGT_LSTGT,
            TacGrpSidc.FSUPP_LNE_LNRTGT_FPF
        );
    }

    /**
     * Create a new target graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public LinearTarget(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates the length of the vertical segments in the graphic.
     *
     * @return The length of the vertical segments as a fraction of the horizontal segment.
     */
    public double getVerticalLength()
    {
        return this.verticalLength;
    }

    /**
     * Specifies the length of the vertical segments in the graphic.
     *
     * @param length Length of the vertical segments as a fraction of the horizontal segment. If the vertical length is
     *               0.25, then the vertical segments will be one quarter of the horizontal segment length.
     */
    public void setVerticalLength(double length)
    {
        if (length < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.verticalLength = length;
    }

    /**
     * Indicates an additional text identification for this graphic. This value is equivalent to the "T1" modifier in
     * MIL-STD-2525C (a second Unique Designation modifier).
     *
     * @return The additional text. May be null.
     */
    public String getAdditionalText()
    {
        return this.additionalText;
    }

    /**
     * Indicates an additional text identification for this graphic. Setting this value is equivalent to setting the
     * "T1" modifier in MIL-STD-2525C (a second Unique Designation modifier).
     *
     * @param text The additional text. May be null.
     */
    public void setAdditionalText(String text)
    {
        this.additionalText = text;
        this.onModifierChanged();
    }

    @Override
    public Object getModifier(String key)
    {
        // If two values are set for the Unique Designation, return both in a list.
        if (SymbologyConstants.UNIQUE_DESIGNATION.equals(key) && this.additionalText != null)
        {
            return Arrays.asList(this.getText(), this.getAdditionalText());
        }

        return super.getModifier(key);
    }

    @Override
    public void setModifier(String key, Object value)
    {
        if (SymbologyConstants.UNIQUE_DESIGNATION.equals(key) && value instanceof Iterable)
        {
            Iterator iterator = ((Iterable) value).iterator();
            if (iterator.hasNext())
            {
                this.setText((String) iterator.next());
            }

            // The Final Protective Fire graphic supports a second Unique Designation value
            if (iterator.hasNext())
            {
                this.setAdditionalText((String) iterator.next());
            }
        }
        else
        {
            super.setModifier(key, value);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points that orient the graphic. Must provide at least three points.
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
            Iterator<? extends Position> iterator = positions.iterator();
            this.startPosition = iterator.next();
            this.endPosition = iterator.next();
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
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(this.startPosition, this.endPosition);
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.startPosition;
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        if (this.paths == null)
        {
            this.createShapes(dc);
        }

        for (Path path : this.paths)
        {
            path.render(dc);
        }
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        if (this.paths == null)
            return;

        for (Path path : this.paths)
        {
            path.setDelegateOwner(owner);
        }
    }

    /**
     * Create the list of positions that describe the shape.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc)
    {
        this.paths = new Path[3];

        // The graphic looks like this:
        //
        //       |                  |
        // Pt. 1 |__________________| Pt. 2
        //       |                  |
        //       |                  |

        // Create a path for the horizontal segment
        this.paths[0] = this.createPath(Arrays.asList(this.startPosition, this.endPosition));

        // Create the vertical segments
        Globe globe = dc.getGlobe();
        Vec4 pA = globe.computePointFromPosition(this.startPosition);
        Vec4 pB = globe.computePointFromPosition(this.endPosition);

        // Find vector in the direction of the horizontal segment
        Vec4 vBA = pA.subtract3(pB);

        // Determine the length of the vertical segments
        double verticalRatio = this.getVerticalLength();
        double verticalLength = vBA.getLength3() * verticalRatio;

        // Compute the left vertical segment
        List<Position> positions = this.computeVerticalSegmentPositions(globe, pA, vBA, verticalLength);
        this.paths[1] = createPath(positions);

        // Compute the right vertical segment
        positions = this.computeVerticalSegmentPositions(globe, pB, vBA, verticalLength);
        this.paths[2] = createPath(positions);
    }

    /**
     * Compute positions for one of the vertical segments in the graphic.
     *
     * @param globe          Current globe.
     * @param basePoint      Point at which the vertical segment must meet the horizontal segment.
     * @param segment        Vector in the direction of the horizontal segment.
     * @param verticalLength Length of the vertical segment, in meters.
     *
     * @return Positions that make up the vertical segment.
     */
    protected List<Position> computeVerticalSegmentPositions(Globe globe, Vec4 basePoint, Vec4 segment,
        double verticalLength)
    {
        Vec4 normal = globe.computeSurfaceNormalAtPoint(basePoint);

        // Compute a vector perpendicular to the segment and the normal vector
        Vec4 perpendicular = normal.cross3(segment);
        perpendicular = perpendicular.normalize3().multiply3(verticalLength / 2.0);

        // Find points on the vertical segment
        Vec4 pA = basePoint.add3(perpendicular);
        Vec4 pB = basePoint.subtract3(perpendicular);

        return Arrays.asList(
            globe.computePositionFromPoint(pA),
            globe.computePositionFromPoint(pB));
    }

    /** Create labels for the graphic. */
    @Override
    protected void createLabels()
    {
        String text = this.getText();
        if (!WWUtil.isEmpty(text))
        {
            this.addLabel(text);
        }

        text = this.getBottomLabelText();
        if (!WWUtil.isEmpty(text))
        {
            TacticalGraphicLabel label = this.addLabel(text);
            label.setOffset(this.getBottomLabelOffset());
        }
    }

    /**
     * Determine text for the graphic's bottom label.
     *
     * @return Text for the bottom label. May return null if there is no bottom label.
     */
    protected String getBottomLabelText()
    {
        String code = this.maskedSymbolCode;
        if (TacGrpSidc.FSUPP_LNE_LNRTGT_LSTGT.equalsIgnoreCase(code))
        {
            return "SMOKE";
        }
        else if (TacGrpSidc.FSUPP_LNE_LNRTGT_FPF.equalsIgnoreCase(code))
        {
            StringBuilder sb = new StringBuilder("FPF");
            String additionalText = this.getAdditionalText();
            if (!WWUtil.isEmpty(additionalText))
            {
                sb.append("\n").append(additionalText);
            }
            return sb.toString();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (this.labels == null || this.labels.size() == 0)
            return;

        Globe globe = dc.getGlobe();
        LatLon midpoint = LatLon.interpolateGreatCircle(0.5, this.startPosition, this.endPosition);
        Vec4 pMid = globe.computePointFromLocation(midpoint);

        Vec4 pA = globe.computePointFromPosition(this.startPosition);
        Vec4 pB = globe.computePointFromPosition(this.endPosition);

        // Find vector in the direction of the horizontal segment
        Vec4 vAB = pB.subtract3(pA);

        // Determine an offset for the label. Place the label between the horizontal segment and the ends of the vertical
        // segments.
        double verticalRatio = this.getVerticalLength();
        double offset = vAB.getLength3() * verticalRatio * 0.25;

        // Compute positions along a vertical through the midpoint of the horizontal segment, and also through one of
        // the end points. The second set of positions is necessary to orient the label along the horizontal segment.
        List<Position> positions = this.computeVerticalSegmentPositions(globe, pMid, vAB, offset);
        List<Position> orientationPositions = this.computeVerticalSegmentPositions(globe, pB, vAB, offset);

        // Set position of the main (top) label
        TacticalGraphicLabel topLabel = this.labels.get(0);
        if (topLabel != null)
        {
            topLabel.setPosition(positions.get(0));
            topLabel.setOrientationPosition(orientationPositions.get(0));
        }

        // Set position of the bottom label.
        if (this.labels.size() > 1)
        {
            TacticalGraphicLabel bottomLabel = this.labels.get(1);
            if (bottomLabel != null)
            {
                bottomLabel.setPosition(positions.get(1));
                bottomLabel.setOrientationPosition(orientationPositions.get(1));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        return TOP_LABEL_OFFSET;
    }

    /**
     * Indicates the offset applied to the lower label.
     *
     * @return Offset applied to the bottom label.
     */
    protected Offset getBottomLabelOffset()
    {
        return BOTTOM_LABEL_OFFSET;
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @param positions Positions that define the path.
     *
     * @return New path configured with defaults appropriate for this type of graphic.
     */
    protected Path createPath(List<Position> positions)
    {
        Path path = new Path(positions);
        path.setFollowTerrain(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}