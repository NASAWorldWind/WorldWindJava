/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

/**
 * An interface for tactical graphics that depict routes: a series of point graphics connected by lines. For example,
 * the MIL-STD-2525 symbology set defines an Air Control Route that is composed of Air Control Points. The route is
 * composed of many tactical graphics, but it is treated as a single graphic. If the route is highlighted all of the
 * control points will also highlight, if the route is set invisible all the control points will be set invisible,
 * etc.
 *
 * @author pabercrombie
 * @version $Id: TacticalRoute.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TacticalGraphicFactory#createRoute(String, Iterable, gov.nasa.worldwind.avlist.AVList)
 */
public interface TacticalRoute extends TacticalGraphic
{
    /**
     * Indicates the control points along this route.
     *
     * @return This route's control points.
     */
    Iterable<? extends TacticalPoint> getControlPoints();

    /**
     * Specifies the control points along this route.
     *
     * @param points New control points.
     */
    void setControlPoints(Iterable<? extends TacticalPoint> points);
}
