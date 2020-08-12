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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of aviation area graphics. This class implements the following graphics:
 * <ul> <li>Restricted Operations Zone (2.X.2.2.3.1)</li> <li>Short Range Air Defense Engagement Zone (2.X.2.2.3.2)</li>
 * <li>High Density Airspace Control Zone (2.X.2.2.3.3)</li> <li>Missile Engagement Zone (2.X.2.2.3.4)</li> <li>Low
 * Altitude Missile Engagement Zone (2.X.2.2.3.4.1)</li> <li>High Altitude Missile Engagement Zone (2.X.2.2.3.4.2)</li>
 * </ul>
 *
 * @author pabercrombie
 * @version $Id: AviationZone.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AviationZone extends BasicArea
{
    /** Center text block on label position. */
    protected final static Offset LABEL_OFFSET = new Offset(-0.5d, -0.5d, AVKey.FRACTION, AVKey.FRACTION);

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_AVN_ARS_ROZ,
            TacGrpSidc.C2GM_AVN_ARS_SHRDEZ,
            TacGrpSidc.C2GM_AVN_ARS_HIDACZ,
            TacGrpSidc.C2GM_AVN_ARS_MEZ,
            TacGrpSidc.C2GM_AVN_ARS_MEZ_LAMEZ,
            TacGrpSidc.C2GM_AVN_ARS_MEZ_HAMEZ);
    }

    /**
     * Create a new aviation area.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public AviationZone(String sidc)
    {
        super(sidc);
        // Do not draw "ENY" labels on hostile entities.
        this.setShowHostileIndicator(false);
    }

    @Override
    protected Offset getDefaultLabelOffset()
    {
        return LABEL_OFFSET;
    }

    @Override
    protected String getLabelAlignment()
    {
        return AVKey.LEFT;
    }

    @Override
    protected String createLabelText()
    {
        return doCreateLabelText(true);
    }

    /**
     * Create text for the area's label.
     *
     * @param includeAltitude Indicates whether to include altitude information in the label (if the
     *                        SymbologyConstants.ALTITUDE_DEPTH modifier is set). Not all aviation area graphics support
     *                        the altitude modifier.
     *
     * @return Text for the label, based on the active modifiers.
     */
    protected String doCreateLabelText(boolean includeAltitude)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getGraphicLabel());
        sb.append("\n");

        Object o = this.getModifier(SymbologyConstants.UNIQUE_DESIGNATION);
        if (o != null)
        {
            sb.append(o);
            sb.append("\n");
        }

        if (includeAltitude)
        {
            Object[] altitudes = TacticalGraphicUtil.getAltitudeRange(this);
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
        }

        Object[] dates = TacticalGraphicUtil.getDateRange(this);
        if (dates[0] != null)
        {
            sb.append("TIME FROM: ");
            sb.append(dates[0]);
            sb.append("\n");
        }

        if (dates[1] != null)
        {
            sb.append("TIME TO: ");
            sb.append(dates[1]);
        }

        return sb.toString();
    }

    @Override
    protected String getGraphicLabel()
    {
        String code = this.maskedSymbolCode;

        if (TacGrpSidc.C2GM_AVN_ARS_ROZ.equalsIgnoreCase(code))
            return "ROZ";
        else if (TacGrpSidc.C2GM_AVN_ARS_SHRDEZ.equalsIgnoreCase(code))
            return "SHORADEZ";
        else if (TacGrpSidc.C2GM_AVN_ARS_HIDACZ.equalsIgnoreCase(code))
            return "HIDACZ";
        else if (TacGrpSidc.C2GM_AVN_ARS_MEZ.equalsIgnoreCase(code))
            return "MEZ";
        else if (TacGrpSidc.C2GM_AVN_ARS_MEZ_LAMEZ.equalsIgnoreCase(code))
            return "LOMEZ";
        else if (TacGrpSidc.C2GM_AVN_ARS_MEZ_HAMEZ.equalsIgnoreCase(code))
            return "HIMEZ";

        return "";
    }
}
