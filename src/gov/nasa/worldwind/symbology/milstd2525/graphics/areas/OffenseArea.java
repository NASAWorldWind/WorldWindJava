/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of offense area graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Assault Position (2.X.2.5.3.1)</li> <li>Attack Position (2.X.2.5.3.2)</li> <li>Objective (2.X.2.5.3.5)</li>
 * <li>Penetration Box (2.X.2.5.3.6)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: OffenseArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OffenseArea extends BasicArea
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_OFF_ARS_ASTPSN,
            TacGrpSidc.C2GM_OFF_ARS_ATKPSN,
            TacGrpSidc.C2GM_OFF_ARS_OBJ,
            TacGrpSidc.C2GM_OFF_ARS_PBX);
    }

    /**
     * Create a new area graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public OffenseArea(String sidc)
    {
        super(sidc);
        this.setShowHostileIndicator(false);
    }

    /** {@inheritDoc} */
    @Override
    protected String createLabelText()
    {
        // Penetration box graphic does not support text modifiers.
        if (TacGrpSidc.C2GM_OFF_ARS_PBX.equalsIgnoreCase(this.maskedSymbolCode))
            return null;

        return super.createLabelText();
    }

    @Override
    protected String getGraphicLabel()
    {
        String code = this.maskedSymbolCode;

        if (TacGrpSidc.C2GM_OFF_ARS_ASTPSN.equalsIgnoreCase(code))
            return "ASLT\nPSN";
        else if (TacGrpSidc.C2GM_OFF_ARS_ATKPSN.equalsIgnoreCase(code))
            return "ATK";
        else if (TacGrpSidc.C2GM_OFF_ARS_OBJ.equalsIgnoreCase(code))
            return "OBJ";

        return "";
    }
}
