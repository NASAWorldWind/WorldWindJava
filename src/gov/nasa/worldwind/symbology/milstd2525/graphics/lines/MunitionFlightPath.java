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
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * @author pabercrombie
 * @version $Id: MunitionFlightPath.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class MunitionFlightPath extends FireSupportLine
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_LNE_C2LNE_MFP);
    }

    /**
     * Create a new target graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public MunitionFlightPath(String sidc)
    {
        super(sidc);
    }

    /** Create labels for the graphic. */
    @Override
    protected void createLabels()
    {
        this.addLabel("MFP");

        Offset bottomLabelOffset = this.getBottomLabelOffset();
        String bottomText = this.getBottomLabelText();

        if (!WWUtil.isEmpty(bottomText))
        {
            TacticalGraphicLabel label = this.addLabel(bottomText);
            label.setOffset(bottomLabelOffset);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (this.labels == null || this.labels.size() == 0)
            return;

        Object[] pathData = this.computePathLength(dc);
        double pathLength = (Double) pathData[2];

        Iterable<? extends Position> positions = this.getPositions();

        TacticalGraphicLabel label = this.labels.get(0);
        TacticalGraphicUtil.placeLabelsOnPath(dc, positions, label, null, pathLength * 0.5);

        if (this.labels.size() > 1)
        {
            label = this.labels.get(1);
            TacticalGraphicUtil.placeLabelsOnPath(dc, positions, label, null, pathLength * 0.25);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        return TacticalGraphicLabel.DEFAULT_OFFSET;
    }
}