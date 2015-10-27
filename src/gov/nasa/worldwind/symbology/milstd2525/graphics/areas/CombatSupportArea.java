/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of combat support area graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Detainee Holding Area (2.X.5.3.1)</li> <li>Enemy Prisoner of War Holding Area (2.X.5.3.2)</li> <li>Forward
 * Arming and Refueling Area (2.X.5.3.3)</li> <li>Refugee Holding Area (2.X.5.3.4)</li> <li>Support Areas Brigade (BSA)
 * (2.X.5.3.5.1)</li> <li>Support Areas Division (DSA) (2.X.5.3.5.2)</li> <li>Support Areas Regimental (RSA)
 * (2.X.5.3.5.3)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: CombatSupportArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CombatSupportArea extends BasicArea
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.CSS_ARA_DHA,
            TacGrpSidc.CSS_ARA_EPWHA,
            TacGrpSidc.CSS_ARA_FARP,
            TacGrpSidc.CSS_ARA_RHA,
            TacGrpSidc.CSS_ARA_SUPARS_BSA,
            TacGrpSidc.CSS_ARA_SUPARS_DSA,
            TacGrpSidc.CSS_ARA_SUPARS_RSA);
    }

    /**
     * Create a new area.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public CombatSupportArea(String sidc)
    {
        super(sidc);

        // Do not draw "ENY" labels for hostile entities
        this.setShowHostileIndicator(false);
    }

    /** {@inheritDoc} */
    @Override
    protected String getGraphicLabel()
    {
        String code = this.maskedSymbolCode;

        if (TacGrpSidc.CSS_ARA_DHA.equalsIgnoreCase(code))
            return "DETAINEE\nHOLDING\nAREA";
        else if (TacGrpSidc.CSS_ARA_EPWHA.equalsIgnoreCase(code))
            return "EPW\nHOLDING\nAREA";
        else if (TacGrpSidc.CSS_ARA_FARP.equalsIgnoreCase(code))
            return "FARP";
        else if (TacGrpSidc.CSS_ARA_RHA.equalsIgnoreCase(code))
            return "REFUGEE\nHOLDING\nAREA";
        else if (TacGrpSidc.CSS_ARA_SUPARS_BSA.equalsIgnoreCase(code))
            return "BSA";
        else if (TacGrpSidc.CSS_ARA_SUPARS_DSA.equalsIgnoreCase(code))
            return "DSA";
        else if (TacGrpSidc.CSS_ARA_SUPARS_RSA.equalsIgnoreCase(code))
            return "RSA";

        return "";
    }
}
