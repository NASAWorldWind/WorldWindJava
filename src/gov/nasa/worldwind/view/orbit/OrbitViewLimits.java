/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.view.ViewPropertyLimits;

/**
 * OrbitViewLimits extends the ViewPropertyLimits interface to include restrictions on the viewing parameters of an
 * {@link OrbitView}.
 *
 * @author dcollins
 * @version $Id: OrbitViewLimits.java 2253 2014-08-22 16:33:46Z dcollins $
 */
public interface OrbitViewLimits extends ViewPropertyLimits
{
    /**
     * Returns the Sector which limits the orbit view center latitude and longitude.
     *
     * @return Sector which limits the center latitude and longitude.
     */
    Sector getCenterLocationLimits();

    /**
     * Sets the Sector which will limit the orbit view center latitude and longitude.
     *
     * @param sector Sector which will limit the center latitude and longitude.
     *
     * @throws IllegalArgumentException if sector is null.
     */
    void setCenterLocationLimits(Sector sector);

    /**
     * Returns the minimum and maximum values for the orbit view center elevation.
     *
     * @return Minimum and maximum allowable values for center elevation.
     */
    double[] getCenterElevationLimits();

    /**
     * Sets the minimum and maximum values which will limit the orbit view center elevation.
     *
     * @param minValue the minimum allowable value for center elevation.
     * @param maxValue the maximum allowable value for center elevation.
     */
    void setCenterElevationLimits(double minValue, double maxValue);

    /**
     * Returns the minimum and maximum values for the orbit view zoom property.
     *
     * @return Minimum and maximum allowable values for zoom.
     */
    double[] getZoomLimits();

    /**
     * Sets the minimum and maximum values which will limit the orbit view zoom property.
     *
     * @param minValue the mimimum allowable value for zoom.
     * @param maxValue the maximum allowable value for zoom.
     */
    void setZoomLimits(double minValue, double maxValue);

    /**
     * Returns a position clamped to the center location limits and center elevation limits specified by this limit
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
    Position limitCenterPosition(View view, Position position);

    /**
     * Returns a distance clamped to the zoom limits specified by this limit object. This method does not modify the
     * specified view's properties, but may use the view as a context for determining how to apply the limits.
     *
     * @param view  the view associated with the zoom distance and the property limits.
     * @param value zoom distance to clamp to the allowed range.
     *
     * @return The clamped value.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    double limitZoom(View view, double value);
}
