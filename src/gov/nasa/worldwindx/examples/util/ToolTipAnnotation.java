/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;

import java.awt.*;

/**
 * Displays a tool tip annotation at a specified screen position. Typically used in conjunction with {@link
 * gov.nasa.worldwindx.examples.util.ToolTipController}.
 *
 * @author tag
 * @version $Id: ToolTipAnnotation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ToolTipAnnotation extends ScreenAnnotation
{
    private Point tooltipOffset = new Point(5, 5);

    /**
     * Create a tool tip using specified text.
     *
     * @param text the text to display in the tool tip.
     */
    public ToolTipAnnotation(String text)
    {
        super(text, new Point(0, 0)); // (0,0) is a dummy; the actual point is determined when rendering

        this.initializeAttributes();
    }

    protected void initializeAttributes()
    {
        this.attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        this.attributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        this.attributes.setTextColor(Color.BLACK);
        this.attributes.setBackgroundColor(new Color(1f, 1f, 1f, 0.8f));
        this.attributes.setCornerRadius(5);
        this.attributes.setBorderColor(new Color(0xababab));
        this.attributes.setFont(Font.decode("Arial-PLAIN-12"));
        this.attributes.setTextAlign(AVKey.CENTER);
        this.attributes.setInsets(new Insets(5, 5, 5, 5));
    }

    /**
     * Indicates the screen-coordinate offset of the tool tip from the tool tip position.
     *
     * @return the tool tip offset.
     */
    public Point getTooltipOffset()
    {
        return tooltipOffset;
    }

    /**
     * Specifies the screen-coordinate offset of the tool tip from the tool tip position.
     *
     * @param tooltipOffset the tool tip offset. The default is five pixels to the left and up.
     */
    public void setTooltipOffset(Point tooltipOffset)
    {
        this.tooltipOffset = tooltipOffset;
    }

    protected int getOffsetX()
    {
        return this.tooltipOffset != null ? this.tooltipOffset.x : 0;
    }

    protected int getOffsetY()
    {
        return this.tooltipOffset != null ? this.tooltipOffset.y : 0;
    }

    @Override
    protected void doRenderNow(DrawContext dc)
    {
        if (dc.getPickPoint() == null)
            return;

        this.getAttributes().setDrawOffset(
            new Point(this.getBounds(dc).width / 2 + this.getOffsetX(), this.getOffsetY()));
        this.setScreenPoint(this.adjustDrawPointToViewport(dc.getPickPoint(), this.getBounds(dc),
            dc.getView().getViewport()));

        super.doRenderNow(dc);
    }

    protected Point adjustDrawPointToViewport(Point point, Rectangle bounds, Rectangle viewport)
    {
        int x = point.x;
        int y = (int) viewport.getHeight() - point.y - 1;

        if (x + this.getOffsetX() + bounds.getWidth() > viewport.getWidth())
            x = (int) (viewport.getWidth() - bounds.getWidth()) - 1 - this.getOffsetX();
        else if (x < 0)
            x = 0;

        if (y + this.getOffsetY() + bounds.getHeight() > viewport.getHeight())
            y = (int) (viewport.getHeight() - bounds.getHeight()) - 1 - this.getOffsetY();
        else if (y < 0)
            y = bounds.height;

        return new java.awt.Point(x, y);
    }
}
