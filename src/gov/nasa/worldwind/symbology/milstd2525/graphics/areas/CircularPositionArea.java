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

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Position Area for Artillery, Circular graphic (2.X.4.3.2.6.2).
 *
 * @author pabercrombie
 * @version $Id: CircularPositionArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CircularPositionArea extends AbstractCircularGraphic
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_C2ARS_PAA_CIRCLR);
    }

    /**
     * Create a new circular area.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public CircularPositionArea(String sidc)
    {
        super(sidc);
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        // This graphic has labels at the top, bottom, left, and right of the circle.
        this.addLabel("PAA");
        this.addLabel("PAA");
        this.addLabel("PAA");
        this.addLabel("PAA");
    }

    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        Position center = new Position(this.circle.getCenter(), 0);
        Angle radius = Angle.fromRadians(this.circle.getRadius() / dc.getGlobe().getRadius());

        Angle[] cardinalDirections = new Angle[] {
            Angle.NEG90, // Due West
            Angle.POS90, // Due East
            Angle.ZERO, // Due North
            Angle.POS180 // Due South
        };

        int i = 0;
        for (Angle dir : cardinalDirections)
        {
            LatLon loc = LatLon.greatCircleEndPosition(center, dir, radius);
            this.labels.get(i).setPosition(new Position(loc, 0));
            i += 1;
        }
    }
}