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

import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: OrbitViewEyePointAnimator.java 2204 2014-08-07 23:35:03Z dcollins $
 */
public class OrbitViewEyePointAnimator implements Animator
{
    protected static final double STOP_DISTANCE = 0.1;

    protected Globe globe;
    protected BasicOrbitView view;
    protected Vec4 eyePoint;
    protected double smoothing;
    protected boolean hasNext;

    public OrbitViewEyePointAnimator(Globe globe, BasicOrbitView view, Vec4 eyePoint, double smoothing)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (view == null)
        {
            String msg = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (eyePoint == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.globe = globe;
        this.view = view;
        this.eyePoint = eyePoint;
        this.smoothing = smoothing;
        this.hasNext = true;
    }

    public void setEyePoint(Vec4 eyePoint)
    {
        this.eyePoint = eyePoint;
    }

    @Override
    public void start()
    {
        this.hasNext = true;
    }

    @Override
    public void stop()
    {
        this.hasNext = false;
    }

    @Override
    public boolean hasNext()
    {
        return this.hasNext;
    }

    @Override
    public void set(double interpolant)
    {
        // Intentionally left blank.
    }

    @Override
    public void next()
    {
        Matrix modelview = this.view.getModelviewMatrix();
        Vec4 point = modelview.extractEyePoint();

        if (point.distanceTo3(this.eyePoint) > STOP_DISTANCE)
        {
            point = Vec4.mix3(1 - this.smoothing, point, this.eyePoint);
            setEyePoint(this.globe, this.view, point);
        }
        else
        {
            setEyePoint(this.globe, this.view, this.eyePoint);
            this.stop();
        }
    }

    public static void setEyePoint(Globe globe, BasicOrbitView view, Vec4 newEyePoint)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (view == null)
        {
            String msg = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (newEyePoint == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Translate the view's modelview matrix to the specified new eye point, and compute the new center point by
        // assuming that the view's zoom distance does not change.
        Vec4 translation = view.getModelviewMatrix().extractEyePoint().subtract3(newEyePoint);
        Matrix modelview = view.getModelviewMatrix().multiply(Matrix.fromTranslation(translation));
        Vec4 eyePoint = modelview.extractEyePoint();
        Vec4 forward = modelview.extractForwardVector();
        Vec4 centerPoint = eyePoint.add3(forward.multiply3(view.getZoom()));

        // Set the view's properties from the new modelview matrix.
        AVList params = modelview.extractViewingParameters(centerPoint, view.getRoll(), globe);
        view.setCenterPosition((Position) params.getValue(AVKey.ORIGIN));
        view.setHeading((Angle) params.getValue(AVKey.HEADING));
        view.setPitch((Angle) params.getValue(AVKey.TILT));
        view.setRoll((Angle) params.getValue(AVKey.ROLL));
        view.setZoom((Double) params.getValue(AVKey.RANGE));
        view.setViewOutOfFocus(true);
    }
}
