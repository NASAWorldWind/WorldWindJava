/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.PropertyAccessor;

/**
 * @author jym
 * @version $Id: RotateToAngleAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RotateToAngleAnimator extends AngleAnimator
{
    private double minEpsilon = 1e-4;
    private double smoothing = .9;
    public RotateToAngleAnimator(
       Angle begin, Angle end, double smoothing,
       PropertyAccessor.AngleAccessor propertyAccessor)
    {
        super(null, begin, end, propertyAccessor);
        this.smoothing = smoothing;
    }

    public void next()
    {
        if (hasNext())
            set(1.0-smoothing);
    }

    protected void setImpl(double interpolant)
    {
        Angle newValue = this.nextAngle(interpolant);
        if (newValue == null)
           return;
        boolean success = this.propertyAccessor.setAngle(newValue);
        if (!success)
        {
           flagLastStateInvalid();
        }
        if (interpolant >= 1)
            this.stop();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Angle nextAngle(double interpolant)
    {


        Angle nextAngle = this.end;
        Angle curAngle = this.propertyAccessor.getAngle();

        double difference = Math.abs(nextAngle.subtract(curAngle).degrees);
        boolean stopMoving = difference < this.minEpsilon;

        if (stopMoving)
        {
            this.stop();
        }
        else
        {
            nextAngle = Angle.mix(interpolant, curAngle, this.end);
        }
        return(nextAngle);
    }
}
