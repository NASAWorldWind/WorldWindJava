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

/**
 * @author jym
 * @version $Id: BasicAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */

/**
 * A base class for an interpolating <code>Animator</code>.
 */
public class BasicAnimator implements Animator
{
    private boolean stopOnInvalidState = false;
    private boolean lastStateValid = true;
    private boolean hasNext = true;

    /**
     * Used to drive the animators next value based on the interpolant returned by the
     * <code>Interpolator</code>'s next interpolant
     */
    protected Interpolator interpolator;

    /**
     * Constructs a <code>BasicAnimator</code>.  Sets the <code>Animator</code>'s <code>Interpolator</code> to
     * <code>null</code>. 
     */
    public BasicAnimator()
    {
        interpolator = null;
    }

    /**
     * Constructs a <code>BasicAnimator</code>.  The <code>next</code> method will use the passed
     * <code>Interpolator</code> to retrieve the <code>interpolant</code>
     *
     * @param interpolator The <code>Interpolator</code> to be used to get the interpolant for
     * setting the next value.
     */
    public BasicAnimator(Interpolator interpolator)
    {
        this.interpolator = interpolator;
    }

    /**
     * Calls the <code>set</code> method with the next <code>interpolant</code> as determined
     * by the <code>interpolator</code> member.
     */
    public void next()
    {
        set(this.interpolator.nextInterpolant());
    }

    /**
     * Calls the setImpl method with the interpolant value.  Deriving classes are expected to
     * implement the desired action of a set operation in thier <code>setImpl</code> method.
     *
     * @param interpolant A value between 0 and 1.
     */
    public void set(double interpolant)
    {
        this.setImpl(interpolant);
        if (isStopOnInvalidState() && !isLastStateValid())
        {
            this.stop();
        }
    }

    /**
     * Returns <code>true</code> if the <code>Animator</code> has more elements.
     *
     * @return <code>true</code> if the <code>Animator</code> has more elements
     */
    public boolean hasNext()
    {
        return this.hasNext;
    }

    /**
     * Starts the <code>Animator</code>, <code>hasNext</code> will now return <code>true</code>
     */
    public void start()
    {
        this.hasNext = true;
    }

    /**
     * Stops the <code>Animator</code>, <code>hasNext</code> will now return <code>false</code>
     */
    public void stop()
    {
        this.hasNext = false;
    }

    /**
     * No-op intended to be overrided by deriving classes.  Deriving classes are expected to
     * implement the desired action of a set operation in this method.
     *
     * @param interpolant A value between 0 and 1.
     */
    protected void setImpl(double interpolant)
    {

    }

    public void setStopOnInvalidState(boolean stop)
    {
       this.stopOnInvalidState = stop;
    }

    public boolean isStopOnInvalidState()
    {
       return this.stopOnInvalidState;
    }

    protected void flagLastStateInvalid()
    {
       this.lastStateValid = false;
    }

    protected boolean isLastStateValid()
    {
       return this.lastStateValid;
    }
}
