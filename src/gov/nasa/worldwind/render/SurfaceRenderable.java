/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Sector;

import java.util.List;

/**
 * Common interface for renderables that are drawn on the Globe's terrain surface, such as {@link
 * gov.nasa.worldwind.render.SurfaceShape}. SurfaceRenderable extends the Renderable interface by adding an interface
 * for determining the geographic region that bounds the renderable, and an interface for determining when the
 * renderable has changed.
 *
 * @author dcollins
 * @version $Id: SurfaceRenderable.java 2283 2014-08-30 15:58:43Z dcollins $
 */
public interface SurfaceRenderable extends Renderable
{
    /**
     * Returns a list of sectors indicating the geographic region that bounds this renderable for the specified draw
     * context.
     * <p>
     * The returned list typically contains one sector that bounds this renderable in geographic coordinates. When this
     * renderable spans the anti-meridian - the +/- 180 degree meridian - the returned list contains two sectors, one on
     * either side of the anti-meridian.
     *
     * @param dc the draw context for which to determine this renderable's geographic bounds.
     *
     * @return a list of one or two sectors that bound this renderable.
     */
    List<Sector> getSectors(DrawContext dc);

    /**
     * Returns an object that uniquely identifies this renderable's state for the specified draw context.
     * <p>
     * Callers can perform an equality test on two state keys using {@link Object#equals(Object)} in order to determine
     * whether or not a renderable has changed. The returned object is guaranteed to be globally unique with respect to
     * other SurfaceRenderable state keys; an equality test with a state key from another renderable always returns
     * false.
     *
     * @param dc the draw context for which to determine this renderable's current state.
     *
     * @return an object representing this renderable's current state.
     */
    Object getStateKey(DrawContext dc);
}
