/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Sector;

/**
 * An interface for objects that can provide an extent in latitude and longitude.
 *
 * @author tag
 * @version $Id: GeographicExtent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GeographicExtent extends Renderable//, AVList
{
    /**
     * Returns the object's geographic extent.
     *
     * @return the object's geographic extent.
     */
    Sector getSector();
}
