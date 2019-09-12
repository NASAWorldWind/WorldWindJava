/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

/**
 * An interface for circular tactical graphics. This interface provides methods to access the radius of the circle. The
 * radius can also be set using the SymbologyConstants.DISTANCE modifier.
 *
 * @author pabercrombie
 * @version $Id: TacticalCircle.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TacticalGraphicFactory#createCircle(String, gov.nasa.worldwind.geom.Position, double,
 *      gov.nasa.worldwind.avlist.AVList)
 */
public interface TacticalCircle extends TacticalPoint
{
    /**
     * Indicates the radius of this circle. Calling this method is equivalent to calling
     * <code>getModifier(SymbologyConstants.DISTANCE )</code>.
     *
     * @return The radius of this circle, in meters.
     */
    double getRadius();

    /**
     * Specifies the radius of this circle. Calling this method is equivalent to calling
     * <code>setModifier(SymbologyConstants.DISTANCE, value)</code>.
     *
     * @param radius New radius, in meters.
     */
    void setRadius(double radius);
}
