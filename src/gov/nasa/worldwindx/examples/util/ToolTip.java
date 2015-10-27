/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: ToolTip.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ToolTip implements OrderedRenderable
{
    private final String text;
    private final int x;
    private final int y;

    public ToolTip(String text, int x, int y)
    {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public double getDistanceFromEye()
    {
        // The tool tip should always be rendered on top of anything else.
        return 0;
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().log(java.util.logging.Level.SEVERE, message);
            throw new IllegalArgumentException(message);
        }

        this.doRender(dc);
    }

    public void pick(DrawContext dc, java.awt.Point pickPoint)
    {
        // Don't pick tool tips.
    }

    protected void doRender(DrawContext dc)
    {
        this.drawToolTip(dc, this.text, this.x, this.y);
    }

    protected void drawToolTip(DrawContext dc, String text, int x, int y)
    {
        ToolTipRenderer renderer = new ToolTipRenderer();
        renderer.setUseSystemLookAndFeel(true);
        renderer.render(dc, text, x, y);
    }
}
