/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

/**
 * An interface for tactical graphics shaped like a quadrilaterals. This interface provides methods to set the length
 * and width of the quad. The length and width can also be set using the SymbologyConstants.DISTANCE modifier.
 *
 * @author pabercrombie
 * @version $Id: TacticalQuad.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TacticalGraphicFactory#createQuad(String, Iterable, gov.nasa.worldwind.avlist.AVList)
 */
public interface TacticalQuad extends TacticalGraphic
{
    /**
     * Indicates the width of the quad.
     *
     * @return The width of the quad, in meters.
     */
    double getWidth();

    /**
     * Specifies the width of the quad.
     *
     * @param width New width, in meters.
     */
    void setWidth(double width);

    /**
     * Indicates the length of the quad.
     *
     * @return The length of the quad, in meters.
     */
    double getLength();

    /**
     * Specifies the length of the quad.
     *
     * @param length New length, in meters.
     */
    void setLength(double length);
}
