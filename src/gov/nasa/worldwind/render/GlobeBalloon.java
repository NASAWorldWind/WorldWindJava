/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

/**
 * A {@link Balloon} attached to a position on the globe.
 *
 * @author pabercrombie
 * @version $Id: GlobeBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GlobeBalloon extends Balloon
{
    /**
     * Get the position of the balloon.
     *
     * @return The position of the balloon.
     */
    Position getPosition();

    /**
     * Set the balloon to a position on the globe.
     *
     * @param position New position for the balloon.
     */
    void setPosition(Position position);

    /**
     * Returns the balloon's altitude mode. See {@link #setAltitudeMode(int)} for a description of the modes.
     *
     * @return the balloon's altitude mode.
     */
    public int getAltitudeMode();

    /**
     * Specifies the balloon's altitude mode. Recognized modes are: <ul> <li><b>@link WorldWind#CLAMP_TO_GROUND}</b> --
     * the balloon is placed on the terrain at the latitude and longitude of its position.</li> <li><b>@link
     * WorldWind#RELATIVE_TO_GROUND}</b> -- the balloon is placed above the terrain at the latitude and longitude of its
     * position and the distance specified by its elevation.</li> <li><b>{@link gov.nasa.worldwind.WorldWind#ABSOLUTE}</b>
     * -- the balloon is placed at its specified position. </ul>
     *
     * @param altitudeMode the altitude mode
     */
    public void setAltitudeMode(int altitudeMode);
}
