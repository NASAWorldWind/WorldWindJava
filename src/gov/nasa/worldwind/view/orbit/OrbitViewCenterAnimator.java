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

import gov.nasa.worldwind.animation.MoveToPositionAnimator;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

/**
 * A position animator that has the ability to adjust the view to focus on the
 * terrain when it is stopped.
 *
 * @author jym
 * @version $Id: OrbitViewCenterAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OrbitViewCenterAnimator extends MoveToPositionAnimator
{
    private BasicOrbitView orbitView;
    boolean endCenterOnSurface;
    public OrbitViewCenterAnimator(BasicOrbitView orbitView, Position startPosition, Position endPosition,
        double smoothing, PropertyAccessor.PositionAccessor propertyAccessor, boolean endCenterOnSurface)
    {
        super(startPosition, endPosition, smoothing, propertyAccessor);
        this.endCenterOnSurface = endCenterOnSurface;
        this.orbitView = orbitView;
    }

    public Position nextPosition(double interpolant)
    {
        Position nextPosition = this.end;
        Position curCenter = this.propertyAccessor.getPosition();

        double latlonDifference = LatLon.greatCircleDistance(nextPosition, curCenter).degrees;
        double elevDifference = Math.abs(nextPosition.getElevation() - curCenter.getElevation());
        boolean stopMoving = Math.max(latlonDifference, elevDifference) < this.positionMinEpsilon;
        if (!stopMoving)
        {
            interpolant = 1 - this.smoothing;
            nextPosition = new Position(
                Angle.mix(interpolant, curCenter.getLatitude(), this.end.getLatitude()),
                Angle.mix(interpolant, curCenter.getLongitude(), this.end.getLongitude()),
                (1 - interpolant) * curCenter.getElevation() + interpolant * this.end.getElevation());
        }
        //TODO: What do we do about collisions?
        /*
        try
        {
            // Clear any previous collision state the view may have.
            view.hadCollisions();
            view.setEyePosition(nextCenter);
            // If the change caused a collision, update the target center position with the
            // elevation that resolved the collision.
            if (view.hadCollisions())
                this.eyePositionTarget = new Position(
                        this.eyePositionTarget, view.getEyePosition().getElevation());
            flagViewChanged();
            setViewOutOfFocus(true);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }
        */

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.stop();
            this.propertyAccessor.setPosition(nextPosition);
            if (endCenterOnSurface)
                this.orbitView.setViewOutOfFocus(true);
            return(null);
        }
        return nextPosition;
    }

    protected void setImpl(double interpolant)
    {
        Position newValue = this.nextPosition(interpolant);
        if (newValue == null)
           return;

        this.propertyAccessor.setPosition(newValue);
        this.orbitView.setViewOutOfFocus(true);
    }

    public void stop()
    {
        super.stop();
    }
}
