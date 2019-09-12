/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;

import java.awt.*;

/**
 * @author tag
 * @version $Id: EditorAnnotation.java 2306 2014-09-15 17:32:55Z tgaskins $
 */
public class EditorAnnotation extends ScreenAnnotation
{
    private Point tooltipOffset = new Point(5, 5);

    /**
     * Create a tool tip using specified text.
     *
     * @param text the text to display in the tool tip.
     */
    public EditorAnnotation(String text)
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
        this.getAttributes().setDrawOffset(
            new Point(this.getBounds(dc).width / 2 + this.getOffsetX(), this.getOffsetY()));
        this.setScreenPoint(this.getScreenPoint());

        super.doRenderNow(dc);
    }
}
