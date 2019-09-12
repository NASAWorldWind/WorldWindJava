/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes;

/**
 * An interface for controlling aspects of a 2D {@link Globe}.
 *
 * @author tag
 * @version $Id: Globe2D.java 2158 2014-07-19 00:00:57Z pabercrombie $
 */
public interface Globe2D
{
    /**
     * Specifies whether to treat the associated projection as contiguous with itself. If true, the scene controller
     * will make the implementing globe appear to scroll continuously horizontally. Calling this method overrides the
     * associated projection's value for this field.
     *
     * @param continuous <code>true</code> if it makes sense to treat the associated projection as continuous, otherwise
     *                   <code>false</code>.
     *
     * @see gov.nasa.worldwind.globes.GeographicProjection#isContinuous()
     */
    void setContinuous(boolean continuous);

    /**
     * Indicates whether it makes sense to treat the associated projection as contiguous with itself. If true, the scene
     * controller will make the implementing globe appear to scroll continuously horizontally.
     *
     * @return <code>true</code> if it makes sense to treat the associated projection as continuous, otherwise
     *         <code>false</code>.
     */
    boolean isContinuous();

    int getOffset();

    /**
     * Indicates an offset to apply to Cartesian points computed by this globe. The offset is in units of globe widths,
     * e.g., an offset of one indicates a Cartesian offset of the globe's width in meters.
     *
     * @param offset The offset to apply, in units of globe widths.
     */
    void setOffset(int offset);

    /**
     * Specifies the geographic projection for this globe. The geographic projection converts geographic positions to
     * Cartesian coordinates and back. Implementations of this interface define their default projection.
     *
     * @param projection The projection to apply to this globe.
     *
     * @throws IllegalArgumentException if the projection is null.
     * @see GeographicProjection
     */
    void setProjection(GeographicProjection projection);

    /**
     * Returns the geographic projection for this globe.
     *
     * @return The geographic projection for this globe.
     */
    GeographicProjection getProjection();
}
