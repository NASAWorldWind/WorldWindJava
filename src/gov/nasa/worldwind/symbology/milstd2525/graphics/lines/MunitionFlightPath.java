/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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