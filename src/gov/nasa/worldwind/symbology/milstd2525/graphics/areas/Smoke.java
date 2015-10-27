/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.symbology.TacticalGraphicUtil;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Smoke graphic (hierarchy 2.X.4.3.1.4, SIDC: G*FPATS---****X).
 *
 * @author pabercrombie
 * @version $Id: Smoke.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Smoke extends BasicArea
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_ARATGT_SMK);
    }

    public Smoke(String sidc)
    {
        super(sidc);
        // Do not draw "ENY" labels for hostile entities
        this.setShowHostileIndicator(false);
    }

    /** {@inheritDoc} */
    @Override
    protected String createLabelText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SMOKE\n");

        Object[] dates = TacticalGraphicUtil.getDateRange(this);
        if (dates[0] != null)
        {
            sb.append(dates[0]);
            sb.append(" - \n");
        }

        if (dates[1] != null)
        {
            sb.append(dates[1]);
        }

        return sb.toString();
    }
}
