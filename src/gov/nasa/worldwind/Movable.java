/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Position;

/**
 * An interface provided by objects that can be moved geographically. Typically, implementing objects move the entire
 * object as a whole in response to the methods in this interface. See the documentation for each implementing class to
 * determine whether the class deviates from this.
 *
 * @author tag
 * @version $Id: Movable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Movable
{
    /**
     * A position associated with the object that indicates its aggregate geographic position. The chosen position
     * varies among implementers of this interface. For objects defined by a list of positions, the reference position
     * is typically the first position in the list. For symmetric objects the reference position is often the center of
     * the object. In many cases the object's reference position may be explicitly specified by the application.
     *
     * @return the object's reference position, or null if no reference position is available.
     */
    Position getReferencePosition();

    /**
     * Shift the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
     * North.
     *
     * @param position the latitude and longitude to add to the shape's reference position.
     */
    void move(Position position);

    /**
     * Move the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
     * North.
     *
     * @param position the new position of the shape's reference position.
     */
    void moveTo(Position position);
}
