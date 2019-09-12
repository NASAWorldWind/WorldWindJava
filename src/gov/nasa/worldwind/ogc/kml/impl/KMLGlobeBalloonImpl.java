/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

/**
 * A KML Balloon attached to a point on the globe.
 *
 * @author pabercrombie
 * @version $Id: KMLGlobeBalloonImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLGlobeBalloonImpl extends KMLAbstractBalloon implements GlobeBalloon
{
    /** The contained balloon. */
    protected GlobeBalloon balloon;

    /**
     * Create the balloon.
     *
     * @param balloon Balloon to apply KML styling to.
     * @param feature The feature that defines the balloon style.
     */
    public KMLGlobeBalloonImpl(GlobeBalloon balloon, KMLAbstractFeature feature)
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
    public GlobeBalloon getBalloon()
    {
        return this.balloon;
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setPosition(Position position)
    {
        this.getBalloon().setPosition(position);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Position getPosition()
    {
        return this.getBalloon().getPosition();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public int getAltitudeMode()
    {
        return this.getBalloon().getAltitudeMode();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setAltitudeMode(int altitudeMode)
    {
        this.getBalloon().setAltitudeMode(altitudeMode);
    }
}
