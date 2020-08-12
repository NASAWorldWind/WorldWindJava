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

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

/**
 * @author jym
 * @version $Id: PositionAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PositionAnimator extends BasicAnimator
{
    
    protected Position begin;
    protected Position end;
    protected final PropertyAccessor.PositionAccessor propertyAccessor;

    public PositionAnimator(
        Interpolator interpolator,
        Position begin,
        Position end,
        PropertyAccessor.PositionAccessor propertyAccessor)
    {
        super(interpolator);
        if (interpolator == null)
        {
           this.interpolator = new ScheduledInterpolator(10000);
        }
        if (begin == null || end == null)
        {
           String message = Logging.getMessage("nullValue.PositionIsNull");
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
    
    public void setBegin(Position begin)
    {
        this.begin = begin;
    }

    public void setEnd(Position end)
    {
        this.end = end;
    }

    public Position getBegin()
    {
        return this.begin;
    }

    public Position getEnd()
    {
        return this.end;
    }

    public PropertyAccessor.PositionAccessor getPropertyAccessor()
    {
        return this.propertyAccessor;
    }

    protected void setImpl(double interpolant)
    {
        Position newValue = this.nextPosition(interpolant);
        if (newValue == null)
           return;

        boolean success = this.propertyAccessor.setPosition(newValue);
        if (!success)
        {
           this.flagLastStateInvalid();
        }
        if (interpolant >= 1)
        {
            this.stop();
        }
    }

    protected Position nextPosition(double interpolant)
    {
        return Position.interpolateGreatCircle(interpolant, this.begin, this.end);
    }
}
