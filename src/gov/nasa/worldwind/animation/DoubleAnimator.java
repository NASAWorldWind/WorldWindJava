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

import gov.nasa.worldwind.util.*;

/**
 * An {@link Animator} implentation for animating values of type Double.
 *
 * @author jym
 * @version $Id: DoubleAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DoubleAnimator extends BasicAnimator
{
    protected double begin;
    protected double end;
    protected final PropertyAccessor.DoubleAccessor propertyAccessor;

    public DoubleAnimator(Interpolator interpolator,
       double begin, double end,
       PropertyAccessor.DoubleAccessor propertyAccessor)
    {
       super(interpolator);
       if (interpolator == null)
       {
           this.interpolator = new ScheduledInterpolator(10000);
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

    public void setBegin(Double begin)
    {
        this.begin = begin;
    }

    public void setEnd(Double end)
    {
        this.end = end;
    }

    public final Double getBegin()
    {
       return this.begin;
    }

    public final Double getEnd()
    {
       return this.end;
    }

    public final PropertyAccessor.DoubleAccessor getPropertyAccessor()
    {
       return this.propertyAccessor;
    }

    protected void setImpl(double interpolant)
    {
       Double newValue = this.nextDouble(interpolant);
       if (newValue == null)
           return;

       boolean success = this.propertyAccessor.setDouble(newValue);
       if (!success)
       {
           this.flagLastStateInvalid();
       }
       if (interpolant >= 1.0)
           this.stop();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Double nextDouble(double interpolant)
    {
       return AnimationSupport.mixDouble(
           interpolant,
           this.begin,
           this.end);
    }

    
}
