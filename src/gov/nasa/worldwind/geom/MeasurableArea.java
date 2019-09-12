/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.globes.*;

/**
 * This interfaces provides methods to query measurements of surface-defining objects. These methods all require a Globe
 * parameter in order to compute their spatial location, and for terrain-confoming objects, the terrain elevations.
 *
 * @author tag
 * @version $Id: MeasurableArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface MeasurableArea
{
    /**
     * Returns the object's area in square meters. If the object conforms to terrain, the area returned is the surface
     * area of the terrain, including its hillsides and other undulations.
     *
     * @param globe The globe the object is related to.
     * @return the object's area in square meters. Returns -1 if the object does not form an area due to an insufficient
     *         number of vertices or any other condition.
     * @throws IllegalArgumentException if the <code>globe</code> is null.
     */
    double getArea(Globe globe);

    /**
     * Returns the length of the object's perimeter in meters. If the object conforms to terrain, the perimeter is that
     * along the terrain, including its hillsides and other undulations.
     *
     * @param globe The globe the object is related to.
     * @return the object's perimeter in meters. Returns -1 if the object does not form an area due to an insufficient
     *         number of vertices or any other condition.
     * @throws IllegalArgumentException if the <code>globe</code> is null.
     */
    double getPerimeter(Globe globe);

    /**
     * Returns the longitudinal length of the object in meters. The length is the distance from the object's west-most
     * point to its east-most. If the object is terrain conforming then the
     *
     * @param globe The globe the object is related to.
     * @return the width of the object in meters.
     * @throws IllegalArgumentException if the <code>globe</code> is null.
     */
    double getWidth(Globe globe);

    /**
     * Returns the latitudanl length of the object in meters. The length is the distance from the objects south-most
     * point to its east-most position.
     *
     * @param globe The globe the object is related to.
     * @return the height of the object in meters.
     * @throws IllegalArgumentException if the <code>globe</code> is null.
     */
    double getHeight(Globe globe);
}
