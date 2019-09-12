/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of rectangular Fire Support graphics. This class implements the following graphics:
 * <ul> <li>Free Fire Area (FFA), Rectangular (2.X.4.3.2.3.2)</li> <li>Restrictive Fire Area (RFA), Rectangular
 * (2.X.4.3.2.5.2)</li> <li>Airspace Coordination Area (ACA), Rectangular (2.X.4.3.2.2.2)</li> <li>Sensor Zone,
 * Rectangular</li> <li>Dead Space Area, Rectangular</li> <li>Zone of Responsibility, Rectangular</li> <li>Target
 * Build-up Area</li> <li>Target Value Area, Rectangular</li> <li>Artillery Target Intelligence Zone, Rectangular
 * (2.X.4.3.3.1.2)</li> <li>Call For Fire Zone, Rectangular (2.X.4.3.3.2.2)</li> <li>Censor Zone, Rectangular
 * (2.X.4.3.3.4.2)</li> <li>Critical Friendly Zone, Rectangular (2.X.4.3.3.6.2)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: RectangularFireSupportArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RectangularFireSupportArea extends AbstractRectangularGraphic implements TacticalQuad, PreRenderable
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
            TacGrpSidc.FSUPP_ARS_C2ARS_FSA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_FFA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_RFA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_ACA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_DA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_TBA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_NFA_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFFZ_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CNS_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFZ_RTG,
            TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_RTG,
            TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_RTG);
    }

    /**
     * Create a new target.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public RectangularFireSupportArea(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates the function IDs of rectangular Fire Support area graphics that display a date/time range as a separate
     * label at the left side of the rectangle. Whether or not a graphic supports this is determined by the graphic's
     * template in MIL-STD-2525C.
     *
     * @return A Set containing the function IDs of graphics that support a date/time label separate from the graphic's
     *         main label.
     */
    public static Set<String> getGraphicsWithTimeLabel()
    {
        return new HashSet<String>(Arrays.asList(
            TacGrpSidc.FSUPP_ARS_C2ARS_FSA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_DA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_TBA_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_RTG,
            TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFFZ_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CNS_RTG,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFZ_RTG));
    }

    /** Create labels for the graphic. */
    @Override
    protected void createLabels()
    {
        FireSupportTextBuilder textBuilder = new FireSupportTextBuilder();
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

        if (allText.length > 1 && !WWUtil.isEmpty(allText[1]))
        {
            TacticalGraphicLabel timeLabel = this.addLabel(allText[1]);
            timeLabel.setTextAlign(AVKey.RIGHT);

            // Align the upper right corner of the time label with the upper right corner of the quad.
            timeLabel.setOffset(new Offset(0d, 0d, AVKey.FRACTION, AVKey.FRACTION));
        }
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        Position center = new Position(this.quad.getCenter(), 0);
        this.labels.get(0).setPosition(center);

        if (this.labels.size() > 1)
        {
            double hw = this.quad.getWidth() / 2.0;
            double hh = this.quad.getHeight() / 2.0;
            double globeRadius = dc.getGlobe().getRadiusAt(center.getLatitude(), center.getLongitude());
            double distance = Math.sqrt(hw * hw + hh * hh);
            double pathLength = distance / globeRadius;

            // Find the upper left corner (looking the quad such that Point 1 is on the left and Point 2 is on the right,
            // and the line between the two is horizontal, as the quad is pictured in the MIL-STD-2525C spec, pg. 652).
            double cornerAngle = Math.atan2(-hh, hw);
            double azimuth = (Math.PI / 2.0) - (cornerAngle - this.quad.getHeading().radians);

            LatLon corner = LatLon.greatCircleEndPosition(center, azimuth, pathLength);

            this.labels.get(1).setPosition(new Position(corner, 0));
        }
    }

    /**
     * Indicates the text alignment to apply to the main label of this graphic.
     *
     * @return Text alignment for the main label.
     */
    protected String getMainLabelTextAlign()
    {
        boolean isACA = TacGrpSidc.FSUPP_ARS_C2ARS_ACA_RTG.equalsIgnoreCase(this.maskedSymbolCode);

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
        boolean isACA = TacGrpSidc.FSUPP_ARS_C2ARS_ACA_RTG.equalsIgnoreCase(this.maskedSymbolCode);

        // Airspace Coordination Area labels are left aligned. Adjust the offset to center the left aligned label
        // in the circle. (This is not necessary with a center aligned label because centering the text automatically
        // centers the label in the circle).
        if (isACA)
            return LEFT_ALIGN_OFFSET;
        else
            return super.getDefaultLabelOffset();
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
        return TacGrpSidc.FSUPP_ARS_C2ARS_NFA_RTG.equalsIgnoreCase(this.maskedSymbolCode)
            || TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_RTG.equalsIgnoreCase(this.maskedSymbolCode)
            || TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_RTG.equalsIgnoreCase(this.maskedSymbolCode);
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