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
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.animation.MoveToDoubleAnimator;
import gov.nasa.worldwind.util.PropertyAccessor;

/**
 * @author jym
 * @version $Id: OrbitViewMoveToZoomAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OrbitViewMoveToZoomAnimator  extends MoveToDoubleAnimator
{
    BasicOrbitView orbitView;
    boolean endCenterOnSurface;

    OrbitViewMoveToZoomAnimator(BasicOrbitView orbitView, Double end, double smoothing,
        PropertyAccessor.DoubleAccessor propertyAccessor, boolean endCenterOnSurface)
    {
        super(end, smoothing, propertyAccessor);
        this.orbitView = orbitView;
        this.endCenterOnSurface = endCenterOnSurface;
    }

    protected void setImpl(double interpolant)
    {
       Double newValue = this.nextDouble(interpolant);
       if (newValue == null)
           return;

       this.propertyAccessor.setDouble(newValue);
    }

    public Double nextDouble(double interpolant)
    {
        double newValue = (1 - interpolant) * propertyAccessor.getDouble() + interpolant * this.end;
        if (Math.abs(newValue - propertyAccessor.getDouble()) < minEpsilon)
        {
            this.stop();
            if (this.endCenterOnSurface)
                orbitView.setViewOutOfFocus(true);
            return(null);
        }
        return newValue;
    }
}
