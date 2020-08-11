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

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.TacticalPoint;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.AbstractCircularGraphic;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of aviation route control point graphics. This class implements the following graphics:
 * <ul> <li>Air Control Point (2.X.2.2.1.1)</li> <li>Communications Checkpoint (2.X.2.2.1.2)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: RoutePoint.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RoutePoint extends AbstractCircularGraphic implements TacticalPoint, PreRenderable
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_AVN_PNT_ACP,
            TacGrpSidc.C2GM_AVN_PNT_COMMCP
        );
    }

    /**
     * Create a new control point.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public RoutePoint(String sidc)
    {
        super(sidc);
    }

    /**
     * Create the text for the main label on this graphic.
     *
     * @return Text for the main label. May return null if there is no text.
     */
    protected String createLabelText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getGraphicLabel());

        String text = this.getText();
        if (!WWUtil.isEmpty(text))
        {
            sb.append("\n");
            sb.append(this.getText());
        }

        return sb.toString();
    }

    protected String getGraphicLabel()
    {
        String code = this.maskedSymbolCode;

        if (TacGrpSidc.C2GM_AVN_PNT_ACP.equalsIgnoreCase(code))
            return "ACP";
        else if (TacGrpSidc.C2GM_AVN_PNT_COMMCP.equalsIgnoreCase(code))
            return "CCP";

        return "";
    }

    @Override
    protected void createLabels()
    {
        String labelText = this.createLabelText();
        if (!WWUtil.isEmpty(labelText))
        {
            this.addLabel(labelText);
        }
    }

    /**
     * Determine the appropriate position for the graphic's labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        LatLon center = this.circle.getCenter();
        this.labels.get(0).setPosition(new Position(center, 0));
    }
}
