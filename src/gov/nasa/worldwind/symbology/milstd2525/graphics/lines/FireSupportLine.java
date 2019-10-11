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
 * Implementation of Fire Support line graphics. This class implements the following graphics:
 * <ul> <li>Fire Support Coordination Line (2.X.4.2.2.1)</li> <li>Coordinated Fire Line (2.X.4.2.2.2)</li>
 * <li>Restrictive Fire Line (2.X.4.2.2.4)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: FireSupportLine.java 709 2012-08-06 18:27:00Z pabercrombie $
 */
public class FireSupportLine extends AbstractMilStd2525TacticalGraphic
{
    /** Factor applied to the stipple pattern used to draw the dashed line for a Coordinated Fire Line. */
    protected static final int CFL_OUTLINE_STIPPLE_FACTOR = 12;

    /**
     * Offset applied to the graphic's upper label. This offset aligns the bottom edge of the label with the geographic
     * position, in order to keep the label above the graphic as the zoom changes.
     */
    protected final static Offset TOP_LABEL_OFFSET = Offset.fromFraction(0.0, 0.1);
    /**
     * Offset applied to the graphic's lower label. This offset aligns the top edge of the label with the geographic
     * position, in order to keep the label above the graphic as the zoom changes.
     */
    protected final static Offset BOTTOM_LABEL_OFFSET = Offset.fromFraction(0.0, -1.1);

    /**
     * The value of an optional second text string for the graphic. This value is equivalent to the "T1" modifier
     * defined by MIL-STD-2525C. It can be set using {@link #setAdditionalText(String)}, or by passing an Iterable to
     * {@link #setModifier(String, Object)} with a key of {@link gov.nasa.worldwind.symbology.SymbologyConstants#UNIQUE_DESIGNATION}
     * (additional text is the second value in the iterable).
     */
    protected String additionalText;

    /** Paths used to render the graphic. */
    protected Path path;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.FSUPP_LNE_C2LNE_FSCL,
            TacGrpSidc.FSUPP_LNE_C2LNE_CFL,
            TacGrpSidc.FSUPP_LNE_C2LNE_RFL
        );
    }

    /**
     * Create a new target graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public FireSupportLine(String sidc)
    {
        super(sidc);
        this.path = this.createPath();
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

        this.path.setPositions(positions);
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return this.path.getPositions();
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.path.getReferencePosition();
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        this.path.render(dc);
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        this.path.setDelegateOwner(owner);
    }

    /** Create labels for the graphic. */
    @Override
    protected void createLabels()
    {
        // First two labels are the start and end labels.
        String text = this.getEndOfLineText();
        this.addLabel(text).setTextAlign(AVKey.RIGHT); // Start label
        this.addLabel(text).setTextAlign(AVKey.LEFT); // End label

        String topText = this.getTopLabelText();
        String bottomText = this.getBottomLabelText();

        Offset topLabelOffset = this.getTopLabelOffset();
        Offset bottomLabelOffset = this.getBottomLabelOffset();

        // Add remaining labels as pairs of top/bottom labels.
        TacticalGraphicLabel label = this.addLabel(topText);
        label.setOffset(topLabelOffset);

        label = this.addLabel(bottomText);
        label.setOffset(bottomLabelOffset);

        if (this.isDrawDoubleLabel())
        {
            label = this.addLabel(topText);
            label.setOffset(topLabelOffset);

            label = this.addLabel(bottomText);
            label.setOffset(bottomLabelOffset);
        }
    }

    /**
     * Indicates whether or not the graphic includes double pairs of top and bottom labels. The Final Support
     * Coordination Line (FSCL) and Restrictive Fire Lines (RFL) include double labels, but the Coordination Fire Line
     * (CFL) does not.
     *
     * @return true if the graphic includes two pairs of top/bottom labels. Both pairs contain the same text content.
     */
    protected boolean isDrawDoubleLabel()
    {
        return !TacGrpSidc.FSUPP_LNE_C2LNE_CFL.equalsIgnoreCase(this.maskedSymbolCode);
    }

    /**
     * /** Determine text for the labels at the start and end of the line.
     *
     * @return Text for the end of line labels.
     */
    protected String getEndOfLineText()
    {
        StringBuilder sb = new StringBuilder("PL");

        String text = this.getAdditionalText();
        if (!WWUtil.isEmpty(text))
        {
            sb.append(" ");
            sb.append(text);
        }

        return sb.toString();
    }

    /**
     * Determine text for the graphic's top label.
     *
     * @return Text for the top label. May return null if there is no top label.
     */
    protected String getTopLabelText()
    {
        StringBuilder sb = new StringBuilder();

        String text = this.getText();
        if (!WWUtil.isEmpty(text))
            sb.append(text);

        if (TacGrpSidc.FSUPP_LNE_C2LNE_FSCL.equalsIgnoreCase(this.maskedSymbolCode))
        {
            sb.append(" FSCL");
        }
        else if (TacGrpSidc.FSUPP_LNE_C2LNE_CFL.equalsIgnoreCase(this.maskedSymbolCode))
        {
            sb.insert(0, "CFL ");
        }
        else if (TacGrpSidc.FSUPP_LNE_C2LNE_RFL.equalsIgnoreCase(this.maskedSymbolCode))
        {
            sb.insert(0, "RFL ");
        }

        return sb.toString();
    }

    /**
     * Determine text for the graphic's bottom label.
     *
     * @return Text for the bottom label. May return null if there is no bottom label.
     */
    protected String getBottomLabelText()
    {
        StringBuilder sb = new StringBuilder();

        Object[] dates = TacticalGraphicUtil.getDateRange(this);
        if (dates[0] != null)
        {
            sb.append(dates[0]);
            sb.append("-\n");
        }

        if (dates[1] != null)
        {
            sb.append(dates[1]);
        }

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (this.labels == null || this.labels.size() == 0)
            return;

        Object[] pathData = this.computePathLength(dc);
        Position startPosition = (Position) pathData[0];
        Position endPosition = (Position) pathData[1];
        double pathLength = (Double) pathData[2];

        // Labels are expected to appear in pairs of top label and bottom label (or start and end label).
        Iterator<TacticalGraphicLabel> labelIterator = this.labels.iterator();

        // First two labels are start and end labels.
        TacticalGraphicLabel startLabel = labelIterator.next();
        startLabel.setPosition(startPosition);

        TacticalGraphicLabel endLabel = labelIterator.next();
        endLabel.setPosition(endPosition);

        // Set the West-most label to right alignment, and the East-most label to left alignment.
        if (startPosition.longitude.degrees < endPosition.longitude.degrees)
        {
            startLabel.setTextAlign(AVKey.RIGHT);
            endLabel.setTextAlign(AVKey.LEFT);
        }
        else
        {
            startLabel.setTextAlign(AVKey.LEFT);
            endLabel.setTextAlign(AVKey.RIGHT);
        }

        // Next two are top and bottom labels.
        TacticalGraphicLabel topLabel = labelIterator.next();
        TacticalGraphicLabel bottomLabel = labelIterator.next();

        Iterable<? extends Position> positions = this.getPositions();

        // Determine if there are more labels. If there are, position this pair 25% of the way along the path. Otherwise
        // put the labels 50% along the path.
        double fraction = labelIterator.hasNext() ? 0.25 : 0.5;
        double dist = pathLength * fraction;
        TacticalGraphicUtil.placeLabelsOnPath(dc, positions, topLabel, bottomLabel, dist);

        // If there are more labels it will be a second top/bottom pair. (Note that CFL graphic has only one top/bottom pair.)
        if (labelIterator.hasNext())
        {
            topLabel = labelIterator.next();
            bottomLabel = labelIterator.next();

            dist = pathLength * 0.75;
            TacticalGraphicUtil.placeLabelsOnPath(dc, positions, topLabel, bottomLabel, dist);
        }
    }

    /**
     * Compute the length of the path, and determine the start and end positions.
     *
     * @param dc Current draw context.
     *
     * @return Returns the path's start position, end position, and length (non-terrain following) as a three element
     *         array: [Position start, Position end, Double length].
     */
    protected Object[] computePathLength(DrawContext dc)
    {
        Iterator<? extends Position> iterator = this.path.getPositions().iterator();

        Globe globe = dc.getGlobe();

        Vec4 pt1, pt2;

        // Find the first and last positions on the path
        Position startPosition = iterator.next();
        Position endPosition = startPosition;

        double pathLength = 0;
        pt1 = globe.computePointFromLocation(startPosition);
        while (iterator.hasNext())
        {
            endPosition = iterator.next();
            pt2 = globe.computePointFromLocation(endPosition);

            pathLength += pt2.distanceTo2(pt1);
            pt1 = pt2;
        }

        return new Object[] {startPosition, endPosition, pathLength};
    }

    /** {@inheritDoc} */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        return TOP_LABEL_OFFSET;
    }

    /**
     * Indicates the offset applied to the upper label.
     *
     * @return Offset applied to the upper label.
     */
    protected Offset getTopLabelOffset()
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

    /** {@inheritDoc} Overridden to draw Coordinated Fire Line with dashed pattern. */
    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        // Coordinated Fire Line always renders with dashed lines.
        if (TacGrpSidc.FSUPP_LNE_C2LNE_CFL.equalsIgnoreCase(this.maskedSymbolCode))
        {
            attributes.setOutlineStippleFactor(CFL_OUTLINE_STIPPLE_FACTOR);
            attributes.setOutlineStipplePattern(this.getOutlineStipplePattern());
        }
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
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }
}
