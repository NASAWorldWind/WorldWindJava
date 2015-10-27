/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

/**
 * @author jym
 * @version $Id: MoveToPositionAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MoveToPositionAnimator extends PositionAnimator
{

    protected double positionMinEpsilon = 1e-9;
    protected double smoothing = .9;
    protected boolean useSmoothing = true;

    public MoveToPositionAnimator(
        Position begin,
        Position end,
        double smoothing,
        PropertyAccessor.PositionAccessor propertyAccessor)
    {
        super(null, begin, end, propertyAccessor);
        this.interpolator = null;
        this.smoothing = smoothing;
    }

    public void next()
    {
        if (hasNext())
            set(1.0-smoothing);
    }

    public Position nextPosition(double interpolant)
    {
        Position nextPosition = this.end;
        Position curCenter = this.propertyAccessor.getPosition();

        double latlonDifference = LatLon.greatCircleDistance(nextPosition, curCenter).degrees;
        double elevDifference = Math.abs(nextPosition.getElevation() - curCenter.getElevation());
        boolean stopMoving = Math.max(latlonDifference, elevDifference) < this.positionMinEpsilon;
        if (!stopMoving)
        {
            interpolant = 1 - this.smoothing;
            nextPosition = new Position(
                Angle.mix(interpolant, curCenter.getLatitude(), this.end.getLatitude()),
                Angle.mix(interpolant, curCenter.getLongitude(), this.end.getLongitude()),
                (1 - interpolant) * curCenter.getElevation() + interpolant * this.end.getElevation());
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.stop();
            return(null);
        }
        return nextPosition;
    }
}
