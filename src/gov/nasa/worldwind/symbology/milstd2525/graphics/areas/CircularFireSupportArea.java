/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.TacticalGraphicLabel;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of circular Fire Support graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Circular Target (2.X.4.3.1.2)</li> <li>Fire Support Area, Circular (2.X.4.3.2.1.3)</li> <li>Free Fire Area
 * (FFA), Circular (2.X.4.3.2.3.3)</li> <li>Restrictive Fire Area (RFA), Circular (2.X.4.3.2.5.3)</li> <li>Airspace
 * Coordination Area (ACA), Circular (2.X.4.3.2.2.3)</li> <li>Sensor Zone, Circular</li> <li>Dead Space Area,
 * Circular</li> <li>Zone of Responsibility, Circular</li> <li>Target Build-up Area, Circular</li> <li>Target Value
 * Area, Circular</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: CircularFireSupportArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CircularFireSupportArea extends AbstractCircularGraphic
{
    /** Path to the image used for the polygon fill pattern. */
    protected static final String DIAGONAL_FILL_PATH = "images/diagonal-fill-16x16.png";
    /** Center text block on label position when the text is left aligned. */
    protected final static Offset LEFT_ALIGN_OFFSET = new Offset(-0.5d, -0.5d, AVKey.FRACTION, AVKey.FRACTION);

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.FSUPP_ARS_ARATGT_CIRTGT,
            TacGrpSidc.FSUPP_ARS_C2ARS_FSA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_FFA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_RFA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_ACA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_DA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TBA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_NFA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_CIRCLR,
            TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_CIRCLR);
    }

    /**
     * Create a new circular area.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public CircularFireSupportArea(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates the function IDs of circular Fire Support area graphics that display a date/time range as a separate
     * label at the left side of the circle. Whether or not a graphic supports this is determined by the graphic's
     * template in MIL-STD-2525C.
     *
     * @return A Set containing the function IDs of graphics that support a date/time label separate from the graphic's
     *         main label.
     */
    public static Set<String> getGraphicsWithTimeLabel()
    {
        return new HashSet<String>(Arrays.asList(
            TacGrpSidc.FSUPP_ARS_C2ARS_FSA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_DA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TBA_CIRCLR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_CIRCLR));
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        FireSupportTextBuilder textBuilder = this.createTextBuilder();
        String[] allText = textBuilder.createText(this);

        String text = allText[0];
        if (!WWUtil.isEmpty(text))
        {
            TacticalGraphicLabel mainLabel = this.addLabel(text);
            mainLabel.setTextAlign(this.getMainLabelTextAlign());

            if (this.isFilled())
            {
                mainLabel.setEffect(AVKey.TEXT_EFFECT_NONE);
                mainLabel.setDrawInterior(true);
            }
        }

        if (allText.length > 1)
        {
            String timeText = allText[1];

            if (!WWUtil.isEmpty(timeText))
            {
                TacticalGraphicLabel timeLabel = this.addLabel(timeText);
                timeLabel.setTextAlign(AVKey.RIGHT);
            }
        }
    }

    protected FireSupportTextBuilder createTextBuilder()
    {
        return new FireSupportTextBuilder();
    }

    /**
     * Indicates the text alignment to apply to the main label of this graphic.
     *
     * @return Text alignment for the main label.
     */
    protected String getMainLabelTextAlign()
    {
        boolean isACA = TacGrpSidc.FSUPP_ARS_C2ARS_ACA_CIRCLR.equalsIgnoreCase(this.maskedSymbolCode);

        // Airspace Coordination Area labels are left aligned. All others are center aligned.
        if (isACA)
            return AVKey.LEFT;
        else
            return AVKey.CENTER;
    }

    /**
     * Indicates the default offset applied to the graphic's main label. This offset may be overridden by the graphic
     * attributes.
     *
     * @return Offset to apply to the main label.
     */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        boolean isACA = TacGrpSidc.FSUPP_ARS_C2ARS_ACA_CIRCLR.equalsIgnoreCase(this.maskedSymbolCode);

        // Airspace Coordination Area labels are left aligned. Adjust the offset to center the left aligned label
        // in the circle. (This is not necessary with a center aligned label because centering the text automatically
        // centers the label in the circle).
        if (isACA)
            return LEFT_ALIGN_OFFSET;
        else
            return super.getDefaultLabelOffset();
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (WWUtil.isEmpty(this.labels))
            return;

        this.labels.get(0).setPosition(new Position(this.circle.getCenter(), 0));

        Position center = new Position(this.circle.getCenter(), 0);
        double radiusRadians = this.circle.getRadius() / dc.getGlobe().getRadius();

        if (this.labels.size() > 1)
        {
            LatLon westEdge = LatLon.greatCircleEndPosition(center, Angle.NEG90 /* Due West */,
                Angle.fromRadians(radiusRadians));
            this.labels.get(1).setPosition(new Position(westEdge, 0));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        if (this.isFilled())
        {
            // Enable the polygon interior and set the image source to draw a fill pattern of diagonal lines.
            attributes.setDrawInterior(true);
            attributes.setImageSource(this.getImageSource());
        }
    }

    /**
     * Indicates whether or not the polygon must be filled with a diagonal line pattern.
     *
     * @return true if the polygon must be filled, otherwise false.
     */
    protected boolean isFilled()
    {
        return TacGrpSidc.FSUPP_ARS_C2ARS_NFA_CIRCLR.equalsIgnoreCase(this.maskedSymbolCode)
            || TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_CIRCLR.equalsIgnoreCase(this.maskedSymbolCode)
            || TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_CIRCLR.equalsIgnoreCase(this.maskedSymbolCode);
    }

    /**
     * Indicates the source of the image that provides the polygon fill pattern.
     *
     * @return The source of the polygon fill pattern.
     */
    protected Object getImageSource()
    {
        return DIAGONAL_FILL_PATH;
    }
}