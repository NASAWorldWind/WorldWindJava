/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * A KML Balloon attached to a point on the screen.
 *
 * @author pabercrombie
 * @version $Id: KMLScreenBalloonImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLScreenBalloonImpl extends KMLAbstractBalloon implements ScreenBalloon
{
    /** The contained balloon. */
    protected ScreenBalloon balloon;

    /**
     * Create the balloon.
     *
     * @param balloon Balloon to apply KML styling to.
     * @param feature The feature that defines the balloon style.
     */
    public KMLScreenBalloonImpl(ScreenBalloon balloon, KMLAbstractFeature feature)
    {
        super(feature);

        if (balloon == null)
        {
            String msg = Logging.getMessage("nullValue.BalloonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.balloon = balloon;
        this.initialize(balloon);
    }

    /** {@inheritDoc} */
    public ScreenBalloon getBalloon()
    {
        return this.balloon;
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setScreenLocation(Point point)
    {
        this.getBalloon().setScreenLocation(point);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Point getScreenLocation()
    {
        return this.getBalloon().getScreenLocation();
    }
}
