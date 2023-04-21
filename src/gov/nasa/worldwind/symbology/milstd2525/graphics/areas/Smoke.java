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
