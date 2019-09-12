/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.render.DrawContext;

/**
 * @author tag
 * @version $Id: Tessellator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Tessellator extends WWObject
{
    /**
     * Tessellate a globe for the currently visible region.
     *
     * @param dc the current draw context.
     *
     * @return the tessellation, or null if the tessellation failed or the draw context identifies no visible region.
     *
     * @throws IllegalStateException if the globe has no tessellator and a default tessellator cannot be created.
     */
    SectorGeometryList tessellate(DrawContext dc);

    /**
     * Indicates whether the tessellator creates "skirts" around the tiles in order to hide gaps between adjacent tiles
     * with differing tessellations.
     *
     * @return true if skirts are created, otherwise false.
     */
    boolean isMakeTileSkirts();

    /**
     * Specifies whether the tessellator creates "skirts" around the tiles in order to hide gaps between adjacent tiles
     * with differing tessellations.
     *
     * @param makeTileSkirts true if skirts are created, otherwise false.
     */
    void setMakeTileSkirts(boolean makeTileSkirts);

    /**
     * Indicates the maximum amount of time that may elapse between re-tessellation. Re-tessellation is performed to
     * synchronize the terrain's resolution into with the current viewing state and availability of elevations.
     *
     * @return the update frequency, in milliseconds.
     */
    long getUpdateFrequency();

    /**
     * Indicates the maximum amount of time that may elapse between re-tessellation. Re-tessellation is performed to
     * synchronize the terrain's resolution into with the current viewing state and availability of elevations.
     *
     * @param updateFrequency the update frequency, in milliseconds.
     */
    void setUpdateFrequency(long updateFrequency);
}
