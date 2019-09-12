/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.SymbolCode;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.*;

/**
 * Utility class to construct text for the graphics of Fire Support Area graphics. Many of these graphics come in three
 * versions (quad, circle, and polygon), but share the same text. This class encodes the logic to construct the
 * appropriate label text depending on the type of graphic.
 *
 * @author pabercrombie
 * @version $Id: FireSupportTextBuilder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FireSupportTextBuilder
{
    /**
     * Construct the text for labels in a Fire Support area graphic. All area graphics support main label placed inside
     * the area. Some also support a time range label placed at the left side of the graphic. This method returns text
     * for all applicable labels as a list. The first element of the list is the main label text. The second element (if
     * present) is the time range label text.
     *
     * @param graphic Graphic for which to create text.
     *
     * @return Array of text for labels. This array will always include at least one string: the main label text. It may
     *         include a second element. The second element (if present) is text for a label that must be placed at the
     *         left side of the area.
     */
    public String[] createText(TacticalGraphic graphic)
    {
        if (graphic == null)
        {
            String message = Logging.getMessage("nullValue.GraphicIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] result;

        // Compute the masked SIDC for this graphic.
        SymbolCode symCode = new SymbolCode(graphic.getIdentifier());
        String maskedSidc = symCode.toMaskedString();

        if (TacGrpSidc.FSUPP_ARS_ARATGT_CIRTGT.equalsIgnoreCase(maskedSidc))
        {
            // Circular Target just uses the Unique Designation as a label.
            result = new String[] {graphic.getText()};
        }
        else if (TacGrpSidc.FSUPP_ARS_ARATGT_BMARA.equalsIgnoreCase(maskedSidc))
        {
            // Bomb graphic just says "BOMB"
            result = new String[] {"BOMB"};
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_TGMF.equalsIgnoreCase(maskedSidc))
        {
            // Terminally guided munitions footprint says "TGMF", and does not support modifiers.
            result = new String[] {"TGMF"};
        }
        else
        {
            boolean useSeparateTimeLabel = this.isShowSeparateTimeLabel(maskedSidc);

            String mainText;

            if (this.isAirspaceCoordinationArea(maskedSidc))
            {
                mainText = this.createAirspaceCoordinationText(graphic);
            }
            else
            {
                boolean includeTime = !useSeparateTimeLabel;
                boolean includeAltitude = this.isShowAltitude(maskedSidc);
                mainText = this.createMainText(graphic, maskedSidc, includeTime, includeAltitude);
            }

            if (useSeparateTimeLabel)
            {
                String timeText = this.createTimeRangeText(graphic);
                result = new String[] {mainText, timeText};
            }
            else
            {
                result = new String[] {mainText};
            }
        }
        return result;
    }

    protected boolean isShowSeparateTimeLabel(String maskedSidc)
    {
        return CircularFireSupportArea.getGraphicsWithTimeLabel().contains(maskedSidc)
            || RectangularFireSupportArea.getGraphicsWithTimeLabel().contains(maskedSidc)
            || IrregularFireSupportArea.getGraphicsWithTimeLabel().contains(maskedSidc);
    }

    protected boolean isShowAltitude(String maskedSidc)
    {
        return TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_RTG.equalsIgnoreCase(maskedSidc)
            || TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_CIRCLR.equalsIgnoreCase(maskedSidc)
            || TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_IRR.equalsIgnoreCase(maskedSidc);
    }

    protected boolean isAirspaceCoordinationArea(String functionId)
    {
        return TacGrpSidc.FSUPP_ARS_C2ARS_ACA_IRR.equalsIgnoreCase(functionId)
            || TacGrpSidc.FSUPP_ARS_C2ARS_ACA_RTG.equalsIgnoreCase(functionId)
            || TacGrpSidc.FSUPP_ARS_C2ARS_ACA_CIRCLR.equalsIgnoreCase(functionId);
    }

    protected String createMainText(TacticalGraphic graphic, String functionId, boolean includeTime,
        boolean includeAltitude)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getGraphicLabel(functionId)).append("\n");

        String s = graphic.getText();
        if (!WWUtil.isEmpty(s))
        {
            sb.append(s).append("\n");
        }

        if (includeTime)
        {
            Object[] dates = TacticalGraphicUtil.getDateRange(graphic);
            if (dates[0] != null)
            {
                sb.append(dates[0]);
                sb.append("-");
            }

            if (dates[1] != null)
            {
                sb.append(dates[1]);
            }
        }

        if (includeAltitude)
        {
            Object[] alt = TacticalGraphicUtil.getAltitudeRange(graphic);
            if (alt[0] != null)
            {
                if (sb.length() > 0)
                    sb.append('\n');

                sb.append(alt[0]);
            }
        }

        return sb.toString();
    }

    protected String createTimeRangeText(TacticalGraphic graphic)
    {
        StringBuilder sb = new StringBuilder();

        Object[] dates = TacticalGraphicUtil.getDateRange(graphic);
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

    protected String getGraphicLabel(String sidc)
    {
        if (TacGrpSidc.FSUPP_ARS_C2ARS_FFA_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_FFA_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_FFA_IRR.equalsIgnoreCase(sidc))
        {
            return "FFA";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_RFA_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_RFA_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_RFA_IRR.equalsIgnoreCase(sidc))
        {
            return "RFA";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_FSA_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_FSA_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_FSA_IRR.equalsIgnoreCase(sidc))
        {
            return "FSA";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_SNSZ_IRR.equalsIgnoreCase(sidc))
        {
            return "SENSOR\nZONE";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_DA_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_DA_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_DA_IRR.equalsIgnoreCase(sidc))
        {
            return "DA";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_ZOR_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_DA_IRR.equalsIgnoreCase(sidc))
        {
            return "ZOR";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_TBA_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_TBA_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_TBA_IRR.equalsIgnoreCase(sidc))
        {
            return "TBA";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_TVAR_IRR.equalsIgnoreCase(sidc))
        {
            return "TVAR";
        }
        else if (TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_TGTAQZ_ATIZ_IRR.equalsIgnoreCase(sidc))
        {
            return "ATI ZONE";
        }
        else if (TacGrpSidc.FSUPP_ARS_TGTAQZ_CFFZ_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_TGTAQZ_CFFZ_IRR.equalsIgnoreCase(sidc))
        {
            return "CFF ZONE";
        }
        else if (TacGrpSidc.FSUPP_ARS_TGTAQZ_CNS_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_TGTAQZ_CNS_IRR.equalsIgnoreCase(sidc))
        {
            return "CENSOR ZONE";
        }
        else if (TacGrpSidc.FSUPP_ARS_TGTAQZ_CFZ_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_TGTAQZ_CFZ_IRR.equalsIgnoreCase(sidc))
        {
            return "CF ZONE";
        }
        else if (TacGrpSidc.FSUPP_ARS_C2ARS_NFA_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_NFA_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_C2ARS_NFA_IRR.equalsIgnoreCase(sidc))
        {
            return "NFA";
        }
        else if (TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_KLBOX_BLUE_IRR.equalsIgnoreCase(sidc))
        {
            return "BKB";
        }
        else if (TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_RTG.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_CIRCLR.equalsIgnoreCase(sidc)
            || TacGrpSidc.FSUPP_ARS_KLBOX_PURPLE_IRR.equalsIgnoreCase(sidc))
        {
            return "PKB";
        }

        return "";
    }

    protected String createAirspaceCoordinationText(TacticalGraphic graphic)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ACA\n");

        Object o = graphic.getText();
        if (o != null)
        {
            sb.append(o);
            sb.append("\n");
        }

        Object[] altitudes = TacticalGraphicUtil.getAltitudeRange(graphic);
        if (altitudes[0] != null)
        {
            sb.append("MIN ALT: ");
            sb.append(altitudes[0]);
            sb.append("\n");
        }

        if (altitudes[1] != null)
        {
            sb.append("MAX ALT: ");
            sb.append(altitudes[1]);
            sb.append("\n");
        }

        o = graphic.getModifier(SymbologyConstants.ADDITIONAL_INFORMATION);
        if (o != null)
        {
            sb.append("Grids: ");
            sb.append(o);
            sb.append("\n");
        }

        Object[] dates = TacticalGraphicUtil.getDateRange(graphic);
        if (dates[0] != null)
        {
            sb.append("EFF: ");
            sb.append(dates[0]);
            sb.append("\n");
        }

        if (dates[1] != null)
        {
            sb.append("     "); // TODO do a better job of vertically aligning the start and end time labels
            sb.append(dates[1]);
        }

        return sb.toString();
    }
}
