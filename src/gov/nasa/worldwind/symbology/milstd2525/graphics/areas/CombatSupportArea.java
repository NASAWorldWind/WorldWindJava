/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of combat support area graphics. This class implements the following graphics:
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
