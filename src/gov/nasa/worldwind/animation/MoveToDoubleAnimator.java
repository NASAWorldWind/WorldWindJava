/*
 * Copyright 2006-2009, 2017, 2020 United States Government,
 * as represented by the Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source software:
 *
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 *
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ) can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in the code directory.
 */
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.util.PropertyAccessor;

/**
 * Animates a double value toward a specified end position using a smoothing factor.
 * The animation continues until the current value is within a defined {@code minEpsilon}
 * distance of the end value.
 * <p>
 * For each frame, the animator interpolates between the current value and the target value
 * using {@code (1.0 - smoothing)} as the interpolation factor, until the absolute difference
 * between the two is less than {@code minEpsilon}.
 * </p>
 *
 * @author jym
 * @version $Id: MoveToDoubleAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MoveToDoubleAnimator extends DoubleAnimator {

    /**
     * The minimum difference between the end value and the current value required
     * to stop the animation. Defaults to {@code 0.001}.
     */
    protected double minEpsilon = 1e-3;

    /**
     * The smoothing factor, a number between 0 and 1.
     * Higher values produce smoother, slower movement.
     */
    protected double smoothing = 0.9;

    /**
     * Constructs a {@link MoveToDoubleAnimator}.
     *
     * @param end              The target value to animate toward.
     * @param smoothing        The smoothing factor (0–1). Higher means smoother/slower.
     * @param propertyAccessor The accessor used to get and set the animated value.
     */
    public MoveToDoubleAnimator(Double end, double smoothing,
                                PropertyAccessor.DoubleAccessor propertyAccessor) {
        super(null, 0, end, propertyAccessor);
        this.interpolator = null;
        this.smoothing = smoothing;
    }

    /**
     * Constructs a {@link MoveToDoubleAnimator}.
     *
     * @param end              The target value to animate toward.
     * @param smoothing        The smoothing factor (0–1). Higher means smoother/slower.
     * @param minEpsilon       The minimum difference between the current value and target
     *                         value that triggers the end of the animation.
     * @param propertyAccessor The accessor used to get and set the animated value.
     */
    public MoveToDoubleAnimator(Double end, double smoothing, double minEpsilon,
                                PropertyAccessor.DoubleAccessor propertyAccessor) {
        super(null, 0, end, propertyAccessor);
        this.interpolator = null;
        this.smoothing = smoothing;
        this.minEpsilon = minEpsilon;
    }

    /**
     * Advances the animation to the next frame.
     * <p>
     * Interpolates between the current value and the target value using
     * {@code (1.0 - smoothing)} as the interpolation factor.
     * </p>
     */
    public void next() {
        if (hasNext()) {
            set(1.0 - smoothing);
        }
    }

    /**
     * Computes the next interpolated double value based on the given interpolant.
     * Performs linear interpolation between the current and target values.
     *
     * @param interpolant The interpolation factor (0–1).
     * @return The interpolated value, or {@code null} if the animation has stopped.
     */
    public Double nextDouble(double interpolant) {
        double currentValue = propertyAccessor.getDouble();
        double newValue = (1 - interpolant) * currentValue + interpolant * this.end;

        if (Math.abs(newValue - currentValue) < minEpsilon) {
            this.stop();
            return null;
        }

        return newValue;
    }
}
