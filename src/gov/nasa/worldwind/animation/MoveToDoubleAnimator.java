/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.util.PropertyAccessor;

/**
 * Animates the value to the specified end position, using the specified smoothing, until the value is within the
 * specified minEpsilon of the end value.  For each frame the animator interpolates between the current value and the
 * target(end) value using <code>(1.0-smoothing)</code> as the interpolant, until the difference between the current
 * value and the target(end) value is less than the <code>minEpsilon</code> value.
 *
 * @author jym
 * @version $Id: MoveToDoubleAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MoveToDoubleAnimator extends DoubleAnimator
{
    /**
     * The amount of delta between the end value and the current value that is required to stop the animation. Defaults
     * to .001.
     */
    protected double minEpsilon = 1e-3;
    /** The amount of smoothing.  A number between 0 and 1.  The higher the number the greater the smoothing. */
    protected double smoothing = .9;

    /**
     * Construct a {@link MoveToDoubleAnimator}
     *
     * @param end              The target value, the value to animate to.
     * @param smoothing        The smoothing factor. A number between 0 and 1.  The higher the number the greater the
     *                         smoothing.
     * @param propertyAccessor The accessor used to access the animated value.
     */
    public MoveToDoubleAnimator(
        Double end, double smoothing,
        PropertyAccessor.DoubleAccessor propertyAccessor)
    {
        super(null, 0, end, propertyAccessor);
        this.interpolator = null;
        this.smoothing = smoothing;
    }

    /**
     * Construct a {@link MoveToDoubleAnimator}
     *
     * @param end              The target value, the value to animate to.
     * @param smoothing        smoothing The smoothing factor. A number between 0 and 1.  The higher the number the
     *                         greater the smoothing.
     * @param minEpsilon       The minimum difference between the current value and the target value that triggers the
     *                         end of the animation.  Defaults to .001.
     * @param propertyAccessor The double accessor used to access the animated value.
     */
    public MoveToDoubleAnimator(
        Double end, double smoothing, double minEpsilon,
        PropertyAccessor.DoubleAccessor propertyAccessor)
    {
        super(null, 0, end, propertyAccessor);
        this.interpolator = null;
        this.smoothing = smoothing;
        this.minEpsilon = minEpsilon;
    }

    /**
     * Set the value to the next value in the animation.  This interpolates between the current value and the target
     * value using <code>1.0-smoothing</code> as the interpolant.
     */
    public void next()
    {
        if (hasNext())
            set(1.0 - smoothing);
    }

    /**
     * Get the next value using the given interpolantto perform a linear interplation. between the current value and the
     * target(end) value.
     *
     * @param interpolant The inerpolant to be used to perform the interpolation.  A number between 0 and 1.
     *
     * @return the interpolated value.
     */
    public Double nextDouble(double interpolant)
    {
        double newValue = (1 - interpolant) * propertyAccessor.getDouble() + interpolant * this.end;
        if (Math.abs(newValue - propertyAccessor.getDouble()) < minEpsilon)
        {
            this.stop();
            return (null);
        }
        return newValue;
    }
}
