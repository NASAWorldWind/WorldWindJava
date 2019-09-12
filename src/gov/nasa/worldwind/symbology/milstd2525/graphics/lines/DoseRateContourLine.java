/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.BasicArea;

import java.util.*;

/**
 * Implementation of the Dose Rate Contour Line graphic (2.X.3.4.9). The graphic consists of a polygon defined by three
 * or more points, and a text label. The label is placed at over the first control point.
 *
 * @author pabercrombie
 * @version $Id: DoseRateContourLine.java 545 2012-04-24 22:29:21Z pabercrombie $
 */
public class DoseRateContourLine extends BasicArea
{
    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.MOBSU_CBRN_DRCL);
    }

    public DoseRateContourLine(String sidc)
    {
        super(sidc);
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
        return this.getReferencePosition();
    }
}
