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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.*;

/**
 * @author jym
 * @version $Id: AngleAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */

/**
 * Animates angles, via an interpolator.  {@link #begin} and {@link #end} values can be reset
 * once the animation is already in motion.
 */
public class AngleAnimator extends BasicAnimator
{

    /**
     * The angle the animation begins at.
     */
    protected Angle begin;
    /**
     * The angle the animation ends at.
     */
    protected Angle end;
    /**
     * The @link gov.nasa.worldwind.util.PropertyAccessor used to modify
     * the data value being animated.
     */
    protected final PropertyAccessor.AngleAccessor propertyAccessor;

    /**
     * Construct an AngleAnimator
     *
     * @param interpolator the {@link gov.nasa.worldwind.animation.Interpolator}
     * @param begin angle the animation begins at
     * @param end The angle the animation ends at.
     * @param propertyAccessor The {@link gov.nasa.worldwind.util.PropertyAccessor} used to modify
     * the data value being animated.
     */
    public AngleAnimator(Interpolator interpolator,
       Angle begin, Angle end,
       PropertyAccessor.AngleAccessor propertyAccessor)
    {
       super(interpolator);
       if (interpolator == null)
       {
           this.interpolator = new ScheduledInterpolator(10000);
       }
       if (begin == null || end == null)
       {
           String message = Logging.getMessage("nullValue.AngleIsNull");
           Logging.logger().severe(message);
           throw new IllegalArgumentException(message);
       }
       if (propertyAccessor == null)
       {
           String message = Logging.getMessage("nullValue.ViewPropertyAccessorIsNull");
           Logging.logger().severe(message);
           throw new IllegalArgumentException(message);
       }

       this.begin = begin;
       this.end = end;
       this.propertyAccessor = propertyAccessor;
    }

    /**
     * Set the {@link #begin} value.
     *
     * @param begin the new {@link #begin} value.
     */
    public void setBegin(Angle begin)
    {
        this.begin = begin;
    }

    /**
     * Set the {@link #end} value.
     *
     * @param end the new {@link #end} value.
     */
    public void setEnd(Angle end)
    {
        this.end = end;
    }

    /**
     * Get the current {@link #begin} value.
     *
     * @return the current {@link #begin} value.
     */
    public Angle getBegin()
    {
       return this.begin;
    }

    /**
     * Get the current {@link #end} value.
     *
     * @return the current {@link #end} value.
     */
    public Angle getEnd()
    {
       return this.end;
    }

    /**
     * Get the {@link gov.nasa.worldwind.util.PropertyAccessor} in use by this animation
     * 
     * @return the {@link gov.nasa.worldwind.util.PropertyAccessor} in use by this animation
     */
    public PropertyAccessor.AngleAccessor getPropertyAccessor()
    {
       return this.propertyAccessor;
    }

    /**
     * Set the value being animated via the {@link gov.nasa.worldwind.util.PropertyAccessor}
     * using the passed interpolant. This implementation just does a straight liner interpolation
     * between the {@link #begin} and {@link #end} values.
     *
     * @param interpolant the interpolant used to generate the next value that will be set by the
     * {@link gov.nasa.worldwind.util.PropertyAccessor}
     */
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
    private Angle nextAngle(double interpolant)
    {

       return Angle.mix(
           interpolant,
           this.begin,
           this.end);

        
    }
}
