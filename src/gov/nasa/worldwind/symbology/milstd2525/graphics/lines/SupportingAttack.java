/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Supporting Attack graphic (hierarchy 2.X.2.5.2.1.4.2, SIDC: G*GPOLAGS-****X).
 *
 * @author pabercrombie
 * @version $Id: SupportingAttack.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SupportingAttack extends AbstractAxisArrow
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_AXSADV_GRD_SUPATK);
    }

    public SupportingAttack(String sidc)
    {
        super(sidc);
    }
}
