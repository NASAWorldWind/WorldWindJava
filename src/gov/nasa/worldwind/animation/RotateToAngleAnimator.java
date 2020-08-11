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
