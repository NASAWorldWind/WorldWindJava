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
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of the Airhead graphic (2.X.2.6.2.2).
 *
 * @author pabercrombie
 * @version $Id: Airhead.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Airhead extends BasicArea
{
    /**
     * Default offset to apply to the label. The default aligns the top center of the label with the label's geographic
     * position, in order to keep the text South of the area.
     */
    public final static Offset DEFAULT_OFFSET = new Offset(0d, 0d, AVKey.FRACTION, AVKey.FRACTION);

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_SPL_ARA_AHD);
    }

    /**
     * Create a new area graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public Airhead(String sidc)
    {
        super(sidc);
        this.setShowHostileIndicator(false);
    }

    @Override
    protected String createLabelText()
    {
        String text = this.getText();

        StringBuilder sb = new StringBuilder();

        sb.append("AIRHEAD LINE\n");
        sb.append("(PL ");

        if (!WWUtil.isEmpty(text))
        {
            sb.append(text);
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Compute the position for the area's main label. This position indicates the position of the first line of the
     * label. If there are more lines, they will be arranged South of the first line.
     *
     * @param dc Current draw context.
     *
     * @return Position for the graphic's main label.
     */
    @Override
    protected Position determineMainLabelPosition(DrawContext dc)
    {
        Iterable<? extends LatLon> locations = this.polygon.getLocations();
        if (locations == null)
            return null;

        Sector sector = Sector.boundingSector(locations);

        Angle minLat = sector.getMinLatitude();
        Angle avgLon = sector.getCentroid().longitude;

        // Place the label at Southern edge of the area, at the average longitude.
        return new Position(minLat, avgLon, 0);
    }

    /** {@inheritDoc} */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        return DEFAULT_OFFSET;
    }
}
