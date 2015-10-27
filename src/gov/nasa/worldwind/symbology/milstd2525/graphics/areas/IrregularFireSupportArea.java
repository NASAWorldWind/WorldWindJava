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
 * Implementation of the irregular Fire Support area graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Area Target (2.X.4.3.1)</li> <li>Bomb (2.X.4.3.1.5)</li> <li>Airspace Coordination Area (ACA), Irregular
 * (2.X.4.3.2.2.1)</li> <li>Free Fire Area (FFA), Irregular (2.X.4.3.2.3.1)</li> <li>Restrictive Fire Area (RFA),
 * Irregular (2.X.4.3.2.5.1)</li> <li>Terminally Guided Munitions Footprint</li> <li>Sensor Zone, Irregular</li>
 * <li>Dead Space Area, Irregular</li> <li>Zone of Responsibility, Irregular</li> <li>Target Build-up Area,
 * Irregular</li> <li>Target Value Area, Irregular</li> <li>Artillery Target Intelligence Zone, Irregular (
 * 2.X.4.3.3.1.1)</li> <li>Call For Fire Zone, Irregular (2.X.4.3.3.2.1)</li> <li>Censor Zone, Irregular
 * (2.X.4.3.3.4.1)</li> <li>Critical Friendly Zone, Irregular (2.X.4.3.3.6.1)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: IrregularFireSupportArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class IrregularFireSupportArea extends BasicArea
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
            TacGrpSidc.FSUPP_ARS_ARATGT,
            TacGrpSidc.FSUPP_ARS_ARATGT_BMARA,
            TacGrpSidc.FSUPP_ARS_C2ARS_TGMF,
            TacGrpSidc.FSUPP_ARS_C2ARS_FSA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_FFA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_RFA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_ACA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_DA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TBA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_NFA_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFFZ_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CNS_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFZ_IRR,
            TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_IRR,
            TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_IRR);
    }

    /**
     * Create the area graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public IrregularFireSupportArea(String sidc)
    {
        super(sidc);
        this.setShowHostileIndicator(false);
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
            TacGrpSidc.FSUPP_ARS_C2ARS_FSA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_DA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TBA_IRR,
            TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFFZ_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CNS_IRR,
            TacGrpSidc.FSUPP_ARS_TGTAQZ_CFZ_IRR));
    }

    @Override
    protected void createLabels()
    {
        FireSupportTextBuilder textBuilder = new FireSupportTextBuilder();
        String[] allText = textBuilder.createText(this);

        String text = allText[0];
        if (!WWUtil.isEmpty(text))
        {
            TacticalGraphicLabel mainLabel = this.addLabel(text);
            mainLabel.setTextAlign(this.getLabelAlignment());

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

            // Align the upper right corner of the time label with the upper right corner of the polygon.
            timeLabel.setOffset(new Offset(0d, 0d, AVKey.FRACTION, AVKey.FRACTION));
        }
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        // Determine main label position
        super.determineLabelPositions(dc);

        if (this.labels.size() > 1)
        {
            Position pos = this.computeTimeLabelPosition(dc);
            if (pos != null)
            {
                this.labels.get(1).setPosition(pos);
            }
        }
    }

    /**
     * Determine the position of the time range label. This label is placed at the North-West corner of the polygon.
     *
     * @param dc Current draw context.
     *
     * @return Position for the time range label, or null if the position cannot be determined.
     */
    protected Position computeTimeLabelPosition(DrawContext dc)
    {
        Iterable<? extends LatLon> positions = this.polygon.getLocations(dc.getGlobe());

        // Find the North-West corner of the bounding sector.
        Sector boundingSector = Sector.boundingSector(positions);
        LatLon nwCorner = new LatLon(boundingSector.getMaxLatitude(), boundingSector.getMinLongitude());

        Angle minDistance = Angle.POS180;
        LatLon nwMost = null;

        // We want to place the label at the North-West corner of the polygon. Loop through the locations
        // and find the one that is closest so the North-West corner of the bounding sector.
        for (LatLon location : positions)
        {
            Angle dist = LatLon.greatCircleDistance(location, nwCorner);
            if (dist.compareTo(minDistance) < 0)
            {
                minDistance = dist;
                nwMost = location;
            }
        }

        // Place the time label at the North-West position.
        if (nwMost != null)
        {
            return new Position(nwMost, 0);
        }
        return null;
    }

    /**
     * Indicates the alignment of the graphic's main label.
     *
     * @return Alignment for the main label. One of AVKey.CENTER, AVKey.LEFT, or AVKey.RIGHT.
     */
    @Override
    protected String getLabelAlignment()
    {
        boolean isACA = TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_IRR.equalsIgnoreCase(this.maskedSymbolCode);

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
        boolean isACA = TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_IRR.equalsIgnoreCase(this.maskedSymbolCode);

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
        return TacGrpSidc.FSUPP_ARS_C2ARS_NFA_IRR.equalsIgnoreCase(this.maskedSymbolCode)
            || TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_IRR.equalsIgnoreCase(this.maskedSymbolCode)
            || TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_IRR.equalsIgnoreCase(this.maskedSymbolCode);
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
