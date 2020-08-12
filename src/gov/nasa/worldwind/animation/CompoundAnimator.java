/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
