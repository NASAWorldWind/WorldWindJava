/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.awt.*;

/**
 * A {@link Balloon} attached to a location on the screen.
 *
 * @author pabercrombie
 * @version $Id: ScreenBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface ScreenBalloon extends Balloon
{
    /**
     * Get the position of the balloon on the screen.
     *
     * @return The screen location of the balloon.
     */
    Point getScreenLocation();

    /**
     * Set the screen position of the balloon. This point is interpreted in a coordinate system with the origin at the
     * upper left corner of the screen.
     *
     * @param point Point in screen coordinates, with origin at upper left corner.
     */
    void setScreenLocation(Point point);
}
