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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Aviation offensive graphic (hierarchy 2.X.2.5.2.1.1, SIDC: G*GPOLAV--****X).
 *
 * @author pabercrombie
 * @version $Id: Aviation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Aviation extends AbstractAxisArrow
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_AXSADV_AVN);
    }

    /**
     * Create a new Aviation graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public Aviation(String sidc)
    {
        this(sidc, 1);
    }

    /**
     * Create a new Aviation graphic, composed of more than one path. This constructor is for use by subclasses that
     * extend the base Aviation graphic by adding additional paths.
     *
     * @param sidc     Symbol code the identifies the graphic.
     * @param numPaths Number of paths to create.
     */
    protected Aviation(String sidc, int numPaths)
    {
        super(sidc, numPaths);
    }

    /** {@inheritDoc} */
    @Override
    protected double createArrowHeadPositions(List<Position> leftPositions, List<Position> rightPositions,
        List<Position> arrowHeadPositions, Globe globe)
    {
        double halfWidth = super.createArrowHeadPositions(leftPositions, rightPositions, arrowHeadPositions, globe);

        // Aviation graphic is the same as the base graphic, except that the left and right lines cross between
        // points 1 and 2. Swap the control points in the left and right lists to achieve this effect.
        if (rightPositions.size() > 0 && leftPositions.size() > 0)
        {
            Position temp = leftPositions.get(0);

            leftPositions.set(0, rightPositions.get(0));
            rightPositions.set(0, temp);
        }

        // Arrow head points need to be in reverse order to match the reversed first line positions.
        Collections.reverse(arrowHeadPositions);

        return halfWidth;
    }
}
