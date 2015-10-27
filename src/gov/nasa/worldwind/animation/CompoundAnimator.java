/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.util.Logging;

import java.util.Arrays;

/**
 * A group of two or more {@link Animator}s.  Can be used to animate more than one value at a time, driven by a
 * single {@link Interpolator}.
 *
 * @author jym
 * @version $Id: CompoundAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CompoundAnimator extends BasicAnimator
{
    protected Animator[] animators;

    /**
     * Construct a CompoundAnimator with the given {@link Interpolator}
     * @param interpolator the {@link Interpolator} to use to drive the animation.
     */
    public CompoundAnimator(Interpolator interpolator)
    {
        super(interpolator);
        this.animators = null;
    }

    /**
     * Construct a CompoundAnimator with the given {@link Interpolator}, and the given {@link Animator}s.
     *
     * @param interpolator The {@link Interpolator} to use to drive the {@link Animator}s
     * @param animators The {@link Animator}s that will be driven by this {@link CompoundAnimator}
     */
    public CompoundAnimator(Interpolator interpolator, Animator... animators)
    {
        super(interpolator);
        if (animators == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

        int numAnimators = animators.length;
        this.animators = new Animator[numAnimators];
        System.arraycopy(animators, 0, this.animators, 0, numAnimators);
    }

    /**
     * Set the {@link Animator}s to be driven by this {@link CompoundAnimator}
     * @param animators the {@link Animator}s to be driven by this {@link CompoundAnimator}
     */
    public void setAnimators(Animator... animators)
    {
        int numAnimators = animators.length;
        this.animators = new Animator[numAnimators];
        System.arraycopy(animators, 0, this.animators, 0, numAnimators);
    }

    /**
     * Get an {@link Iterable} list of the {@link Animator}
     * @return the list of {@link Animator}s
     */
    public final Iterable<Animator> getAnimators()
    {
        return Arrays.asList(this.animators);
    }

    /**
     * Set the values attached to each of the {@link Animator}s using the given interpolant.
     *
     * @param interpolant A value between 0 and 1.
     */
    protected void setImpl(double interpolant)
    {
        boolean allStopped = true;
        for (Animator a : animators)
        {
            if (a != null)
            {
                if (a.hasNext())
                {
                    allStopped = false;
                    a.set(interpolant);
                }
            }
        }
        if (allStopped)
        {
            this.stop();
        }
    }

}
