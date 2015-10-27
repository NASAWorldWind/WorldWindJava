/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.RestorableSupport;

/**
 * ViewPropertyLimits defines a restriction on the viewing parameters of a {@link View}.
 *
 * @author jym
 * @version $Id: ViewPropertyLimits.java 2253 2014-08-22 16:33:46Z dcollins $
 */
public interface ViewPropertyLimits
{
    /**
     * Sets the Sector which will limit a view's eye latitude and longitude.
     *
     * @param sector Sector which will limit the eye latitude and longitude.
     *
     * @throws IllegalArgumentException if sector is null.
     */
    void setEyeLocationLimits(Sector sector);

    /**
     * Returns the Sector which limits a view's eye latitude and longitude.
     *
     * @return Sector which limits the eye latitude and longitude.
     */
    Sector getEyeLocationLimits();

    /**
     * Returns the minimum and maximum values for a view's eye elevation.
     *
     * @return Minimum and maximum allowable values for the elevation.
     */
    double[] getEyeElevationLimits();

    /**
     * Sets the minimum and maximum values for a view's eye elevation.
     *
     * @param minValue the minimum elevation.
     * @param maxValue the maximum elevation.
     */
    void setEyeElevationLimits(double minValue, double maxValue);

    /**
     * Returns the minimum and maximum angles for a view's heading property.
     *
     * @return Minimum and maximum allowable angles for heading.
     */
    Angle[] getHeadingLimits();

    /**
     * Sets the minimum and maximum angles which will limit a view's heading property.
     *
     * @param minAngle the minimum allowable angle for heading.
     * @param maxAngle the maximum allowable angle for heading.
     *
     * @throws IllegalArgumentException if either minAngle or maxAngle is null.
     */
    void setHeadingLimits(Angle minAngle, Angle maxAngle);

    /**
     * Returns the minimum and maximum angles for a view's pitch property.
     *
     * @return Minimum and maximum allowable angles for pitch.
     */
    Angle[] getPitchLimits();

    /**
     * Sets the minimum and maximum angles which will limit a view's pitch property.
     *
     * @param minAngle the minimum allowable angle for pitch.
     * @param maxAngle the maximum allowable angle for pitch.
     *
     * @throws IllegalArgumentException if either minAngle or maxAngle is null.
     */
    void setPitchLimits(Angle minAngle, Angle maxAngle);

    /**
     * Returns the minimum and maximum angles for a view's roll property.
     *
     * @return Minimum and maximum allowable angles for roll.
     */
    Angle[] getRollLimits();

    /**
     * Sets the minimum and maximum angles which will limit a view's roll property.
     *
     * @param minAngle the minimum allowable angle for roll.
     * @param maxAngle the maximum allowable angle for roll.
     *
     * @throws IllegalArgumentException if either minAngle or maxAngle is null.
     */
    void setRollLimits(Angle minAngle, Angle maxAngle);

    /** Resets all property limits to their default values. */
    void reset();

    /**
     * Returns a position clamped to the eye location limits and the eye elevation limits specified by this limit
     * object. This method does not modify the specified view's properties, but may use the view as a context for
     * determining how to apply the limits.
     *
     * @param view     the view associated with the center position and the property limits.
     * @param position position to clamp to the allowed range.
     *
     * @return The clamped position.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    Position limitEyePosition(View view, Position position);

    /**
     * Returns an angle clamped to the heading limits specified by this limit object. This method does not modify the
     * specified view's properties, but may use the view as a context for determining how to apply the limits.
     *
     * @param view  the view associated with the heading angle and the property limits.
     * @param angle angle to clamp to the allowed range.
     *
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    Angle limitHeading(View view, Angle angle);

    /**
     * Returns an angle clamped to the pitch limits specified by this limit object. This method does not modify the
     * specified view's properties, but may use the view as a context for determining how to apply the limits.
     *
     * @param view  the view associated with the pitch angle and the property limits.
     * @param angle angle to clamp to the allowed range.
     *
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    Angle limitPitch(View view, Angle angle);

    /**
     * Returns an angle clamped to the roll limits specified by this limit object. This method does not modify the
     * specified view's properties, but may use the view as a context for determining how to apply the limits.
     *
     * @param view  the view associated with the roll angle and the property limits.
     * @param angle angle to clamp to the allowed range.
     *
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    Angle limitRoll(View view, Angle angle);

    void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject context);

    void restoreState(RestorableSupport rs, RestorableSupport.StateObject context);
}
