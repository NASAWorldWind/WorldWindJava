/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.awt.*;

/**
 * @author tag
 * @version $Id: ScreenCredit.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface ScreenCredit extends Renderable
{
    void setViewport(Rectangle viewport);

    Rectangle getViewport();

    void setOpacity(double opacity);

    double getOpacity();

    void setLink(String link);

    String getLink();

    public void pick(DrawContext dc, java.awt.Point pickPoint);
}
